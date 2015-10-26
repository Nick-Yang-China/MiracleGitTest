package com.miracle.apps.git.core.test.op;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.RebaseCommand.Operation;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.TagBuilder;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.FileUtils;
import org.eclipse.jgit.util.RawParseUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.miracle.apps.git.core.op.AddToIndexOperation;
import com.miracle.apps.git.core.op.BranchOperation;
import com.miracle.apps.git.core.op.CloneOperation;
import com.miracle.apps.git.core.op.CommitOperation;
import com.miracle.apps.git.core.op.CreateLocalBranchOperation;
import com.miracle.apps.git.core.op.CreateLocalBranchOperation.UpstreamConfig;
import com.miracle.apps.git.core.op.MergeOperation;
import com.miracle.apps.git.core.op.PushOperation;
import com.miracle.apps.git.core.op.PushOperationResult;
import com.miracle.apps.git.core.op.PushOperationSpecification;
import com.miracle.apps.git.core.op.RebaseOperation;
import com.miracle.apps.git.core.op.RemoveFromIndexOperation;
import com.miracle.apps.git.core.op.TagOperation;
import com.miracle.apps.git.test.core.GitTestCase;
import com.miracle.apps.git.core.RepositoryUtil;
import com.miracle.apps.git.core.errors.CoreException;

public class RebaseOperationTest extends GitTestCase {
	
	private static final String TOPIC = Constants.R_HEADS + "topic";

	private static final String MASTER = Constants.R_HEADS + "master";
	
	File workdir;
	RepositoryUtil repositoryUtil;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		workdir= new File("D://Repository1");
		
		if(workdir.exists()){
			FileUtils.delete(workdir, FileUtils.RECURSIVE | FileUtils.RETRY);
		}
		FileUtils.mkdir(workdir,true);
		
		repositoryUtil = new RepositoryUtil(new File(workdir,Constants.DOT_GIT));
		
		repository=repositoryUtil.getRepository();
		
		File file1=new File(workdir, "dummy.txt");
		FileUtils.createNewFile(file1);
		repositoryUtil.track(file1);
		repositoryUtil.commit("testRebaseOperation\n\nfirst commit\n");
	}

	@Override
	@After
	public void tearDown() throws Exception {
		
		repositoryUtil.dispose();
		
		if (workdir.exists())
			FileUtils.delete(workdir, FileUtils.RECURSIVE | FileUtils.RETRY);
		super.tearDown();
	}
	
	@Test
	// currently not working as expected; see also TODO in RebaseCommand
	public void testUpToDate() throws Exception {
		File file2=new File(workdir, "theFile.txt");
		FileUtils.createNewFile(file2);
		repositoryUtil.appendFileContent(file2, "Hello, world");
		repositoryUtil.track(file2);
		// first commit in master: add theFile.txt
		RevCommit first=repositoryUtil.commit("Adding theFile.txt");
		
		repositoryUtil.createBranch(MASTER, TOPIC);

		// checkout topic
		new BranchOperation(repository, TOPIC).execute();
		
		file2=new File(workdir, "theSecondFile.txt");
		FileUtils.createNewFile(file2);
		repositoryUtil.appendFileContent(file2, "Hello, world");
		repositoryUtil.track(file2);
		// first commit in master: add theFile.txt
		RevCommit topicCommit=repositoryUtil.commit("Adding theSecondFile.txt");

		// parent of topic commit should be first master commit before rebase
		assertEquals(first, topicCommit.getParent(0));
		
		// rebase topic onto master
		RebaseOperation op = new RebaseOperation(
				repository, repository.getRef(MASTER));
		op.execute();

		RebaseResult res = op.getResult();
		assertEquals(RebaseResult.Status.UP_TO_DATE, res.getStatus());

		try (RevWalk rw = new RevWalk(repository)) {
			RevCommit newTopic=rw.parseCommit(repository.resolve(TOPIC));
			assertEquals(topicCommit, newTopic);
			assertEquals(first, newTopic.getParent(0));
		}
	}

	@Test
	public void testNoConflict() throws Exception {
		File file2=new File(workdir, "theFile.txt");
		FileUtils.createNewFile(file2);
		repositoryUtil.appendFileContent(file2, "Hello, world");
		repositoryUtil.track(file2);
		// first commit in master: add theFile.txt
		RevCommit first=repositoryUtil.commit("Adding theFile.txt");
		
		repositoryUtil.createBranch(MASTER, TOPIC);
		
		//second commit in master: modify theFile.txt
		repositoryUtil.appendFileContent(file2, "---->second Hello world too", true);
		repositoryUtil.track(file2);
		RevCommit second=repositoryUtil.commit("Modify theFile.txt");
		
		assertEquals(first, second.getParent(0));
		
		// checkout topic
		new BranchOperation(repository, TOPIC).execute();
		
		file2=new File(workdir, "theSecondFile.txt");
		FileUtils.createNewFile(file2);
		repositoryUtil.appendFileContent(file2, "Hello, world");
		repositoryUtil.track(file2);
		// topic commit: add second file
		RevCommit topicCommit=repositoryUtil.commit("Adding theSecondFile.txt");

		// parent of topic commit should be first master commit before rebase
		assertEquals(first, topicCommit.getParent(0));
		
		//rebase topic onto master
		
		RebaseOperation op=new RebaseOperation(repository, repository.getRef(MASTER));
		
		op.execute();
		
		RebaseResult res=op.getResult();
		
		assertEquals(RebaseResult.Status.OK, res.getStatus());
		
		try(RevWalk rw=new RevWalk(repository)){
			RevCommit newTopic=rw.parseCommit(repository.resolve(TOPIC));
			
			assertEquals(second, newTopic.getParent(0));
		}
	}
	
	@Test
	public void testStopAndAbortOnConflict() throws Exception {
		File file2=new File(workdir, "theFile.txt");
		FileUtils.createNewFile(file2);
		repositoryUtil.appendFileContent(file2, "Hello, world");
		repositoryUtil.track(file2);
		// first commit in master: add theFile.txt
		RevCommit first=repositoryUtil.commit("Adding theFile.txt");
		
		repositoryUtil.createBranch(MASTER, TOPIC);
		
		//second commit in master: modify theFile.txt
		repositoryUtil.appendFileContent(file2, "---->second Hello world too", true);
		repositoryUtil.track(file2);
		RevCommit second=repositoryUtil.commit("Modify theFile.txt");
		
		assertEquals(first, second.getParent(0));
		
		// checkout topic
		new BranchOperation(repository, TOPIC).execute();
		
		// set conflicting content in topic
		repositoryUtil.appendFileContent(file2, "/n third Hello world too");
		repositoryUtil.track(file2);
		RevCommit topicCommit=repositoryUtil.commit("Changing theFile.txt again");
		
		assertEquals(first, topicCommit.getParent(0));
		
		RebaseOperation op=new RebaseOperation(repository, repository.getRef(MASTER));
		op.execute();
		
		RebaseResult res=op.getResult();
		
		assertEquals(RebaseResult.Status.STOPPED, res.getStatus());
		
		//let's try to abort this here
		RebaseOperation abort = new RebaseOperation(repository, Operation.ABORT);
		abort.execute();
		RebaseResult abortResult=abort.getResult();
		assertEquals(RebaseResult.Status.ABORTED, abortResult.getStatus());
		
		assertEquals(topicCommit, repository.resolve(Constants.HEAD));
		
	}
	
	@Test
	public void testExceptionWhenRestartingStoppedRebase() throws Exception {
		File file2=new File(workdir, "theFile.txt");
		FileUtils.createNewFile(file2);
		repositoryUtil.appendFileContent(file2, "Hello, world");
		repositoryUtil.track(file2);
		// first commit in master: add theFile.txt
		RevCommit first=repositoryUtil.commit("Adding theFile.txt");
		
		repositoryUtil.createBranch(MASTER, TOPIC);
		
		//second commit in master: modify theFile.txt
		repositoryUtil.appendFileContent(file2, "---->second Hello world too", true);
		repositoryUtil.track(file2);
		RevCommit second=repositoryUtil.commit("Modify theFile.txt");
		
		assertEquals(first, second.getParent(0));
		
		// checkout topic
		new BranchOperation(repository, TOPIC).execute();
		
		// set conflicting content in topic
		repositoryUtil.appendFileContent(file2, "/n third Hello world too");
		repositoryUtil.track(file2);
		RevCommit topicCommit=repositoryUtil.commit("Changing theFile.txt again");
		
		assertEquals(first, topicCommit.getParent(0));
		
		RebaseOperation op=new RebaseOperation(repository, repository.getRef(MASTER));
		op.execute();
		
		RebaseResult res=op.getResult();
		
		assertEquals(RebaseResult.Status.STOPPED, res.getStatus());
		
		try {
			// let's try to start again, we should get a wrapped
			// WrongRepositoryStateException
			op = new RebaseOperation(repository, repository.getRef(MASTER));
			op.execute();
			fail("Expected Exception not thrown");
		} catch (CoreException e) {
			Throwable cause = e.getCause();
			assertTrue(cause instanceof WrongRepositoryStateException);
		}
	}
}
