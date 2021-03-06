package com.miracle.apps.git.core.test.op;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
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
import com.miracle.apps.git.core.op.PushOperation;
import com.miracle.apps.git.core.op.PushOperationResult;
import com.miracle.apps.git.core.op.PushOperationSpecification;
import com.miracle.apps.git.core.op.RemoveFromIndexOperation;
import com.miracle.apps.git.core.op.ResetOperation;
import com.miracle.apps.git.core.op.TagOperation;
import com.miracle.apps.git.test.core.GitTestCase;
import com.miracle.apps.git.core.RepositoryUtil;
import com.miracle.apps.git.core.errors.CoreException;

public class ResetOperationTest extends GitTestCase {
	
	File workdir;
	
	RepositoryUtil repositoryUtil;
	
	// members filled by setupRepository()
	RevCommit initialCommit;
	
	File projectFile;

	File untrackedFile;

	File fileInIndex;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		workdir= new File("D://Repository2");
		
		if(workdir.exists()){
			FileUtils.delete(workdir, FileUtils.RECURSIVE | FileUtils.RETRY);
		}
		FileUtils.mkdir(workdir,true);
		
		repositoryUtil = new RepositoryUtil(new File(workdir,Constants.DOT_GIT));
		
		repository=repositoryUtil.getRepository();
		
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
	public void testHardReset() throws Exception {
		setupRepository();
		System.out.println(initialCommit.getName());
		String fileInIndexPath = fileInIndex.getAbsolutePath();
		ResetOperation reset=new ResetOperation(repository, initialCommit.getName(), ResetType.HARD);
		reset.execute();
		// .project must disappear
		assertFalse(projectFile.exists());
		// check if HEAD points to initial commit now
		assertTrue(repository.resolve("HEAD").equals(initialCommit));
		// check if files were removed
		assertFalse(fileInIndex.exists());
		// the untracked not removed
		assertTrue(untrackedFile.exists());
		// fileInIndex must no longer be in HEAD and in the index
		assertFalse(repositoryUtil.inHead(fileInIndexPath));
		assertFalse(repositoryUtil.inIndex(fileInIndexPath));
		System.out.println(reset.toString());
	}
	
	private void setupRepository() throws Exception {
		// create first commit containing a dummy file
		File file=new File(workdir, "dummy.txt");
		FileUtils.createNewFile(file);
		repositoryUtil.track(file);
		initialCommit=repositoryUtil.commit("testResetOperation\n\nfirst commit\n");
		// add .project to version control
		projectFile=new File(workdir, ".project");
		FileUtils.createNewFile(projectFile);
		repositoryUtil.track(projectFile);
		// add fileInIndex to version control
		fileInIndex=new File(workdir,"fileInIndex.txt");
		FileUtils.createNewFile(fileInIndex);
		repositoryUtil.track(fileInIndex);
		repositoryUtil.commit("Add .project file");
		// modify fileInIndex and add it to the index
		repositoryUtil.appendFileContent(fileInIndex, "index");
		repositoryUtil.track(fileInIndex);
		// create an untracked file
		untrackedFile=new File(workdir,"untrackedFile.txt");
		FileUtils.createNewFile(untrackedFile);
	}
	
	@Test
	public void testSoftReset() throws Exception {
		setupRepository();
		String fileInIndexPath = fileInIndex.getAbsolutePath();
		new ResetOperation(repository, initialCommit.getName(), ResetType.SOFT)
				.execute();
		// .project must remain
		assertTrue(projectFile.exists());
		// check if SOFT points to initial commit now
		assertTrue(repository.resolve("HEAD").equals(initialCommit));
		// untrackedFile and fileInIndex must still exist
		assertTrue(untrackedFile.exists());
		assertTrue(fileInIndex.exists());
		// fileInIndex must no longer be in HEAD
		assertFalse(repositoryUtil.inHead(fileInIndexPath));
		// fileInIndex must exist in the index
		assertTrue(repositoryUtil.inIndex(fileInIndexPath));
	}
	
	@Test
	public void testMixedReset() throws Exception {
		setupRepository();
		String fileInIndexPath = fileInIndex.getAbsolutePath();
		new ResetOperation(repository, initialCommit.getName(), ResetType.MIXED)
				.execute();
		// .project must remain
		assertTrue(projectFile.exists());
		// check if HEAD points to initial commit now
		assertTrue(repository.resolve("HEAD").equals(initialCommit));
		// untrackedFile and fileInIndex must still exist
		assertTrue(untrackedFile.exists());
		assertTrue(fileInIndex.exists());
		// fileInIndex must no longer be in HEAD
		assertFalse(repositoryUtil.inHead(fileInIndexPath));
		// fileInIndex must not in the index
		assertFalse(repositoryUtil.inIndex(fileInIndexPath));
	}
	
	@Test
	public void testResetWithFilePath() throws Exception {
		
		// create first commit containing a dummy file
		File file=new File(workdir, "dummy.txt");
		FileUtils.createNewFile(file);
		repositoryUtil.appendFileContent(file, "dummy");
		repositoryUtil.track(file);
		initialCommit=repositoryUtil.commit("dummy commit");
		
		//create second commit modify file dummy.txt and create a new file first.txt
		repositoryUtil.appendFileContent(file, "\nmodify");
		repositoryUtil.track(file);
		File first=new File(workdir, "first.txt");
		FileUtils.createNewFile(first);
		repositoryUtil.appendFileContent(first, "first");
		repositoryUtil.track(first);
		RevCommit firstCommit=repositoryUtil.commit("first commit");
		
		//create third commit modify file dummy.txt and create a new file second.txt
		repositoryUtil.appendFileContent(file, "\nagain");
		repositoryUtil.track(file);
		File second=new File(workdir, "second.txt");
		FileUtils.createNewFile(second);
		repositoryUtil.appendFileContent(second, "second");
		repositoryUtil.track(second);
		RevCommit secondCommit=repositoryUtil.commit("second commit");
		
//		String fileInIndexPath = fileInIndex.getAbsolutePath();
		ResetOperation ro=new ResetOperation(repository, firstCommit.getName(),null).setPath("dummy.txt");
		ro.execute();
		
		System.out.println(ro.toString());
		
	}
}
