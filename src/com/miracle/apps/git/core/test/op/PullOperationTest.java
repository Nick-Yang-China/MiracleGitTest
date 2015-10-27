package com.miracle.apps.git.core.test.op;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.RemoveNoteCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.miracle.apps.git.core.op.BranchOperation;
import com.miracle.apps.git.core.op.CloneOperation;
import com.miracle.apps.git.core.op.CreateLocalBranchOperation;
import com.miracle.apps.git.core.op.FetchOperation;
import com.miracle.apps.git.core.op.PullOperation;
import com.miracle.apps.git.test.core.GitTestCase;
import com.miracle.apps.git.core.RepositoryUtil;

public class PullOperationTest extends GitTestCase {
	Repository repository1;
	
	File workdir;
	File workdir2;
	
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
		
		workdir2= new File("D:/Repository2");
		
		if(workdir2.exists()){
			FileUtils.delete(workdir2, FileUtils.RECURSIVE | FileUtils.RETRY);
		}
		FileUtils.mkdir(workdir2,true);
		
		repositoryUtil = new RepositoryUtil(new File(workdir,Constants.DOT_GIT));
		
		repository=repositoryUtil.getRepository();
		
		File file=new File(workdir,"file1.txt");
		FileUtils.createNewFile(file);
		repositoryUtil.appendFileContent(file, "Hello World");
		repositoryUtil.track(file);
		repositoryUtil.commit("Initial Commit");
		
		// let's clone repository to repository1
		
		URIish uri=new URIish("file:///"
				+ repository.getDirectory().toString());
		CloneOperation clop=new CloneOperation(uri, true, null, 
				workdir2, "refs/heads/master", Constants.DEFAULT_REMOTE_NAME, 0);
		
		clop.run();
		
		repository1=new RepositoryUtil(new File(workdir2,Constants.DOT_GIT)).getRepository();
		
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
		
	}
	
}
