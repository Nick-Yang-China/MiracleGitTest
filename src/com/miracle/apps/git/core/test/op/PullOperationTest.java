package com.miracle.apps.git.core.test.op;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.RemoveNoteCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevWalkUtils;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.miracle.apps.git.core.op.AddToIndexOperation;
import com.miracle.apps.git.core.op.BranchOperation;
import com.miracle.apps.git.core.op.CloneOperation;
import com.miracle.apps.git.core.op.CommitOperation;
import com.miracle.apps.git.core.op.CreateLocalBranchOperation;
import com.miracle.apps.git.core.op.CreateLocalBranchOperation.UpstreamConfig;
import com.miracle.apps.git.core.op.FetchOperation;
import com.miracle.apps.git.core.op.PullOperation;
import com.miracle.apps.git.test.core.GitTestCase;
import com.miracle.apps.git.core.RepositoryUtil;

public class PullOperationTest extends GitTestCase {
	Repository repository1;
	
	File workdir;
	File workdir2;
	
	RepositoryUtil repositoryUtil;
	RepositoryUtil repositoryUtil1;
	
	File file;
	
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		workdir= new File("D://Repository1");
		
		if(workdir.exists()){
			FileUtils.delete(workdir, FileUtils.RECURSIVE | FileUtils.RETRY);
		}
		FileUtils.mkdir(workdir,true);
		
		workdir2= new File("D:/Repository2");
		
		if(workdir2.exists()){
			FileUtils.delete(workdir2, FileUtils.RECURSIVE | FileUtils.RETRY);
		}
		FileUtils.mkdir(workdir2,true);
		
		repositoryUtil = new RepositoryUtil(new File(workdir,Constants.DOT_GIT));
		
		repository=repositoryUtil.getRepository();
		
		file=new File(workdir,"file1.txt");
		FileUtils.createNewFile(file);
		repositoryUtil.appendFileContent(file, "[{Hello World");
		repositoryUtil.track(file);
		repositoryUtil.commit("Initial Commit");
		
		// let's clone repository to repository1
		
		URIish uri=new URIish("file:///"
				+ repository.getDirectory().toString());
		CloneOperation clop=new CloneOperation(uri.toString(), true, null, 
				workdir2, "refs/heads/master", Constants.DEFAULT_REMOTE_NAME, 0,null,null);
		
		clop.execute();
		
		repositoryUtil1=new RepositoryUtil(new File(workdir2,Constants.DOT_GIT));
		
		repository1=repositoryUtil1.getRepository();
		
	}

	@Override
	@After
	public void tearDown() throws Exception {
		if(repository!=null)
			repository.close();
		
		if(repository1!=null)
			repository1.close();
		
		if (workdir.exists())
			FileUtils.delete(workdir, FileUtils.RECURSIVE | FileUtils.RETRY);
		if (workdir2.exists())
			FileUtils.delete(workdir2, FileUtils.RECURSIVE | FileUtils.RETRY);
		super.tearDown();
	}
	
	@Test
	public void testPullOperation() throws Exception{
		//create file of file2.txt in repository
		File file2=new File(workdir,"file2.txt");
		FileUtils.createNewFile(file2);
		repositoryUtil.appendFileContent(file2, "testing fetch");
		repositoryUtil.track(file2);
		RevCommit secondcommit=repositoryUtil.commit("second Commit");
		
//		the repository1 pull from repository
		URIish uri=new URIish("file:///" + repository.getDirectory().toString());
		
		PullOperation po=new PullOperation(repository1, 0,null);
		po.execute();
		PullResult pr=po.getPullResult();
		System.out.println(pr.getFetchedFrom());
		
		assertTrue(new File(workdir2,"file2.txt").exists());
		System.out.println(po.toString());
	}
	
	@Test
	public void testPullOperationWithOtherBranch() throws Exception{
		//create branch of test and check out
		new CreateLocalBranchOperation(repository, "test", repository.getRef("master"), null).setCheckOutFlag(true).execute();
		
		assertEquals("test", repository.getBranch());
		
		//create file of file2.txt on branch of test in repository
		File file2=new File(workdir,"file2.txt");
		FileUtils.createNewFile(file2);
		repositoryUtil.appendFileContent(file2, "testing fetch");
		repositoryUtil.track(file2);
		RevCommit secondcommit=repositoryUtil.commit("second Commit");
		
//		the repository1 pull from repository
		PullOperation po=new PullOperation(repository1, 0,"test");
		po.execute();
		PullResult pr=po.getPullResult();
		System.out.println(pr.getFetchedFrom());
		
		assertTrue(new File(workdir2,"file2.txt").exists());
		System.out.println(po.toString());
	}
	
	@Test
	public void testPullOperationWithOtherBranchNoCheckOut() throws Exception{
		//create branch of test and check out
		new CreateLocalBranchOperation(repository, "test", repository.getRef("master"), null).setCheckOutFlag(true).execute();
		assertEquals("test", repository.getBranch());
		
		//create file of file2.txt on branch of test in repository
		File file2=new File(workdir,"file2.txt");
		FileUtils.createNewFile(file2);
		repositoryUtil.appendFileContent(file2, "testing fetch");
		repositoryUtil.track(file2);
		RevCommit secondcommit=repositoryUtil.commit("second Commit");
		
		//check out the branch of master
		new BranchOperation(repository, Constants.R_HEADS+Constants.MASTER).execute();
		assertEquals("master", repository.getBranch());
		
		//the repository1 pull from repository
		PullOperation po=new PullOperation(repository1, 0,"test");
		po.execute();
		PullResult pr=po.getPullResult();
		System.out.println(pr.getFetchedFrom());
		
		assertTrue(new File(workdir2,"file2.txt").exists());
		System.out.println(po.toString());
	}
	
	@Test
	public void testPullOperationWithOtherLocalBranchCheckOut()throws Exception{
		//create branch of test and check out in repository1 
		new CreateLocalBranchOperation(repository1, "test", repository1.getRef("master"), UpstreamConfig.MERGE).setCheckOutFlag(true).execute();
		assertEquals("test", repository1.getBranch());
		
		//create file of file2.txt on branch of master in repository
		File file2=new File(workdir,"file2.txt");
		FileUtils.createNewFile(file2);
		repositoryUtil.appendFileContent(file2, "testing pull");
		repositoryUtil.track(file2);
		RevCommit secondcommit=repositoryUtil.commit("second Commit");		
		
		//the repository1 pull from repository
		PullOperation po=new PullOperation(repository1, 0,"master");
		po.execute();
		PullResult pr=po.getPullResult();
		System.out.println(pr.toString());
		System.out.println(pr.getFetchResult().getMessages());
		System.out.println(pr.getMergeResult().getMergeStatus().name());
//		System.out.println(pr.getRebaseResult().toString());
		assertTrue(new File(workdir2,"file2.txt").exists());
		System.out.println(po.toString());
	}
	
	@Test
	public void testPullOperationWithNonBranch()throws Exception{
		//create file of file2.txt in repository
		File file2=new File(workdir,"file2.txt");
		FileUtils.createNewFile(file2);
		repositoryUtil.appendFileContent(file2, "testing fetch");
		repositoryUtil.track(file2);
		RevCommit secondcommit=repositoryUtil.commit("second Commit");
		
		//the repository1 pull from repository with none branch name
		URIish uri=new URIish("file:///" + repository.getDirectory().toString());
		
		PullOperation po=new PullOperation(repository1, 0,"refs/heads/dev");
		try {
			po.execute();
			fail("expected exception here");
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		System.out.println(po.toString());
	}
	
	@Test
	public void testPullOperationWithFileConflictContent()throws Exception{
		//modify file1.txt in repository1
		file=new File(workdir,"file1.txt");
		repositoryUtil.appendFileContent(file, "updating from 1");
		repositoryUtil.track(file);
		RevCommit secondcommit=repositoryUtil.commit("second Commit");
		 
		//modify file1.txt in repository2
		
		file=new File(workdir2,"file1.txt");
		repositoryUtil1.appendFileContent(file, "adding from 2");
		repositoryUtil1.track(file);
		RevCommit thirdcommit=repositoryUtil1.commit("third Commit");
		
//		the repository1 pull from repository
//		URIish uri=new URIish("file:///" + repository.getDirectory().toString());
		
		PullOperation po=new PullOperation(repository1, 0,"master");
		po.execute();
		System.out.println(po.toString());
		
	}
	
	@Test
	public void testPullOperationWithFileConflictContentAndResolved()throws Exception{
		//modify file1.txt in repository1
		file=new File(workdir,"file1.txt");
		repositoryUtil.appendFileContent(file, "updating from 1}]");
		repositoryUtil.track(file);
		RevCommit secondcommit=repositoryUtil.commit("second Commit");
		 
		//modify file1.txt in repository2
		
		file=new File(workdir2,"file1.txt");
		repositoryUtil1.appendFileContent(file, "adding from 2}]");
		repositoryUtil1.track(file);
		RevCommit thirdcommit=repositoryUtil1.commit("third Commit");
		
        //the repository1 pull from repository
		System.out.println(repository1.getRepositoryState().toString());
		PullOperation po=new PullOperation(repository1, 0,"master");
		po.execute();
		System.out.println(po.toString());
		System.out.println(repository1.getRepositoryState().toString());
		Map<String,String> map=repositoryUtil1.getConflictFileContentWithSplit(file);
		
		String temp=map.get("HEAD");
		
		FileOutputStream fos=new FileOutputStream(file);
		fos.write(temp.getBytes());
		fos.flush();
		fos.close();
		System.out.println(repository1.getRepositoryState().toString());
		
		new AddToIndexOperation(Arrays.asList("file1.txt"), repository1).execute();
		System.out.println(repository1.getRepositoryState().toString());
		CommitOperation co=new CommitOperation(repository1, AUTHOR, COMMITTER, "TEST PULL OPERATION");
		co.setCommitAll(true);
		co.execute();
		System.out.println(repository1.getRepositoryState().toString());
	}
}
