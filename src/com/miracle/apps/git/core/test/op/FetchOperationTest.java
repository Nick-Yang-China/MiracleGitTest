package com.miracle.apps.git.core.test.op;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;

import org.eclipse.jgit.api.Git;
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

import com.miracle.apps.git.core.op.CloneOperation;
import com.miracle.apps.git.core.op.CreateLocalBranchOperation;
import com.miracle.apps.git.core.op.FetchOperation;
import com.miracle.apps.git.test.core.GitTestCase;
import com.miracle.apps.git.core.RepositoryUtil;

public class FetchOperationTest extends GitTestCase {
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
	public void testFetchOperationwithRemoteConfig() throws Exception{
		//create file of file2.txt in repository
		File file2=new File(workdir,"file2.txt");
		FileUtils.createNewFile(file2);
		repositoryUtil.appendFileContent(file2, "testing fetch");
		repositoryUtil.track(file2);
		RevCommit secondcommit=repositoryUtil.commit("second Commit");
		
		// the repository1 fetch from repository
		URIish uri=new URIish("file:///" + repository.getDirectory().toString());
		
		RemoteConfig config=new RemoteConfig(repository.getConfig(), Constants.DEFAULT_REMOTE_NAME);
		config.addURI(uri);
		
		System.out.println(config.getName());
		
		FetchOperation fo=new FetchOperation(repository1, config, 0, false);
		fo.run();
		FetchResult result=fo.getOperationResult().getFetchResult();
		TrackingRefUpdate tru=result.getTrackingRefUpdate("refs/remotes/origin/master");
		assertEquals(secondcommit.getId(), tru.getNewObjectId());
		
	}
	
	@Test
	public void testFetchOperationwithSpecs() throws Exception{
		//create file of file2.txt in repository
		File file2=new File(workdir,"file2.txt");
		FileUtils.createNewFile(file2);
		repositoryUtil.appendFileContent(file2, "testing fetch");
		repositoryUtil.track(file2);
		RevCommit secondcommit=repositoryUtil.commit("second Commit");
		
		// the repository1 fetch from repository
		URIish uri=new URIish("file:///" + repository.getDirectory().toString());
		
//		RefSpec rs=new RefSpec("refs/heads/master:refs/remotes/origin/master");
		RefSpec rs=new RefSpec();
		rs=rs.setForceUpdate(true);
		rs=rs.setSourceDestination(Constants.R_HEADS+Constants.MASTER, "refs/remotes/origin/master");
		System.out.println(rs.getDestination());
		System.out.println(rs.getSource());
		FetchOperation fo=new FetchOperation(repository1, uri, Arrays.asList(rs), 0, false);
		fo.run();
		FetchResult result=fo.getOperationResult().getFetchResult();
		
		TrackingRefUpdate tru=result.getTrackingRefUpdate("refs/remotes/origin/master");
		assertEquals(secondcommit.getId(), tru.getNewObjectId());
		
	}
	
	@Test
	public void testFetchOperationwithOtherBranch() throws Exception{
		//create branch of test and check out
		new CreateLocalBranchOperation(repository, "test", repository.getRef("master"), null).setCheckOutFlag(false).execute();
		
		assertEquals("test", repository.getBranch());
		
		//create file of file2.txt on branch of test in repository
		File file2=new File(workdir,"file2.txt");
		FileUtils.createNewFile(file2);
		repositoryUtil.appendFileContent(file2, "testing fetch");
		repositoryUtil.track(file2);
		RevCommit secondcommit=repositoryUtil.commit("second Commit");
		
		
		
		// the repository1 fetch from repository
		URIish uri=new URIish("file:///" + repository.getDirectory().toString());
		
//		RefSpec rs=new RefSpec("refs/heads/master:refs/remotes/origin/master");
		RefSpec rs=new RefSpec();
		rs=rs.setForceUpdate(false);
		rs=rs.setSourceDestination(Constants.R_HEADS+"test", "refs/remotes/origin/master");
		System.out.println(rs.getDestination());
		System.out.println(rs.getSource());
		FetchOperation fo=new FetchOperation(repository1, uri, Arrays.asList(rs), 0, false);
		fo.run();
		FetchResult result=fo.getOperationResult().getFetchResult();
		System.out.println(result.getTrackingRefUpdates());
		TrackingRefUpdate tru=result.getTrackingRefUpdate("refs/remotes/origin/master");
		assertEquals(secondcommit.getId(), tru.getNewObjectId());
		
	}
	
}
