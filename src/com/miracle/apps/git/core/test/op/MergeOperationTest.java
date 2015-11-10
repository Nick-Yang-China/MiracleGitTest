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
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
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
import com.miracle.apps.git.core.op.RemoveFromIndexOperation;
import com.miracle.apps.git.core.op.TagOperation;
import com.miracle.apps.git.test.core.GitTestCase;
import com.miracle.apps.git.core.RepositoryUtil;
import com.miracle.apps.git.core.errors.CoreException;

public class MergeOperationTest extends GitTestCase {
	
	private static final String MASTER = Constants.R_HEADS +  Constants.MASTER;
	private static final String SIDE = Constants.R_HEADS + "side";
	private RevCommit secondCommit;
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
		
		File file1=new File(workdir, "file1.txt");
		FileUtils.createNewFile(file1);
		repositoryUtil.track(file1);
		repositoryUtil.commit("master commit 1");
		repositoryUtil.createBranch(MASTER, SIDE);
		repositoryUtil.appendFileContent(file1, "file1-2");
		repositoryUtil.track(file1);
		secondCommit=repositoryUtil.commit("master commit 2");
		//checkout branch of side
		new BranchOperation(repository, SIDE).execute();
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
	public void testMergeFF() throws Exception {
		MergeOperation mo=new MergeOperation(repository, MASTER, null);
		mo.setFastForwardMode(FastForwardMode.FF);
		mo.execute();
		System.out.println(mo.getResult().getMergeStatus().toString());
		System.out.println(secondCommit);
		System.out.println(repository.resolve(SIDE));
		assertTrue(repository.resolve(SIDE).equals(secondCommit));
		assertEquals(2, countCommitsInHead());
		System.out.println(mo.toString());
		
	}
	
	@Test
	public void testMergeNoFF() throws Exception{
		setMerge(FastForwardMode.NO_FF);
		
		MergeOperation operation=new MergeOperation(repository, MASTER, null);
//		operation.setFastForwardMode(FastForwardMode.NO_FF);
		operation.execute();
		System.out.println(operation.getResult().getMergeStatus().toString());
		assertEquals(3, countCommitsInHead());
		System.out.println(operation.toString());
	}
	
	@Test
	public void testMergeFFOnly()throws Exception{
		setMerge(FastForwardMode.FF_ONLY);
		File file2=new File(workdir, "file2.txt");
		FileUtils.createNewFile(file2);
		repositoryUtil.appendFileContent(file2, "File2-1");
		repositoryUtil.track(file2);
		RevCommit commit=repositoryUtil.commit("side commit 1");
		
		MergeOperation operation=new MergeOperation(repository, MASTER, null);
//		operation.setFastForwardMode(FastForwardMode.FF_ONLY);
		operation.execute();
		
		System.out.println(operation.getResult().getMergeStatus().toString());
		System.out.println(operation.toString());
		assertTrue(repository.resolve(SIDE).equals(operation.getResult().getNewHead()));
	}

	@Test
	public void testMergeoptionsNoFF() throws Exception{
		setMergeOptions("side", FastForwardMode.NO_FF);
		
		MergeOperation operation=new MergeOperation(repository, MASTER, null);
		operation.execute();
		assertEquals(3, countCommitsInHead());
		System.out.println(operation.toString());
	}
	
	@Test
	public void testMergeoptionsFFOnly() throws Exception{
		setMergeOptions("side",FastForwardMode.FF_ONLY);
		File file2=new File(workdir, "file2.txt");
		FileUtils.createNewFile(file2);
		repositoryUtil.appendFileContent(file2, "File2-1");
		repositoryUtil.track(file2);
		RevCommit commit=repositoryUtil.commit("side commit 1");
		
		MergeOperation operation=new MergeOperation(repository, MASTER, null);
		operation.execute();
		assertTrue(repository.resolve(SIDE).equals(commit));
		System.out.println(operation.toString());
	}
	
	@Test
	public void testMergeWithConflicts() throws Exception{
		setMergeOptions("side",FastForwardMode.FF);
		File file2=new File(workdir, "file1.txt");
		repositoryUtil.appendFileContent(file2, "\nFile2-1");
		repositoryUtil.track(file2);
		RevCommit commit=repositoryUtil.commit("side commit 1");
		
		MergeOperation operation=new MergeOperation(repository, MASTER, "RECURSIVE");
		operation.execute();
		assertTrue(repository.resolve(SIDE).equals(commit));
		System.out.println(operation.toString());
	}
	
	@Test
	public void testMergeWithCheckOutConflicts() throws Exception{
		setMergeOptions("side",FastForwardMode.FF);
		File file2=new File(workdir, "file1.txt");
		repositoryUtil.appendFileContent(file2, "\nFile2-1");
		repositoryUtil.track(file2);
		
		MergeOperation operation=new MergeOperation(repository, MASTER, null);
		operation.execute();
		System.out.println(operation.toString());
	}
	
	private void setMerge(FastForwardMode ffMode)throws IOException{
		StoredConfig config=repository.getConfig();
		config.setEnum(ConfigConstants.CONFIG_KEY_MERGE, null, ConfigConstants.CONFIG_KEY_FF,
				FastForwardMode.Merge.valueOf(ffMode));
		config.save();
	}
	
	
	private void setMergeOptions(String branch,FastForwardMode ffMode)
			throws IOException{
		StoredConfig config=repository.getConfig();
		config.setEnum(ConfigConstants.CONFIG_BRANCH_SECTION, branch, 
				ConfigConstants.CONFIG_KEY_MERGEOPTIONS, ffMode);
		config.save();
	}
	
	private int countCommitsInHead() throws GitAPIException {
		LogCommand log = new Git(repository).log();
		Iterable<RevCommit> commits = log.call();
		int result = 0;
		for (Iterator i = commits.iterator(); i.hasNext();) {
			i.next();
			result++;
		}
		return result;
	}
}
