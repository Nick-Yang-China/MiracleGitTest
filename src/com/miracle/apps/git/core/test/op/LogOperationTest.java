package com.miracle.apps.git.core.test.op;

import java.io.File;
import java.util.Iterator;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.miracle.apps.git.core.op.LogOperation;
import com.miracle.apps.git.test.core.GitTestCase;
import com.miracle.apps.git.core.RepositoryUtil;

public class LogOperationTest extends GitTestCase {
	
	File workdir;
	
	RepositoryUtil repositoryUtil;
	
	RevCommit initialCommit;
	
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
	public void testCommitHistoryAll() throws Exception {
		setupRepository();
		LogOperation cho=new LogOperation(repository);
		cho.execute();
		Iterator<RevCommit> it=cho.getCommitResults().iterator();
		while(it.hasNext()){
			RevCommit rc=it.next();
			System.out.println(rc.getName()+"<---->"+rc.getShortMessage());
		}
	}
	
	@Test
	public void testCommitHistoryAllWithMaxCount() throws Exception {
		setupRepository();
		LogOperation cho=new LogOperation(repository);
		cho.setMaxCount(2);
		cho.execute();
		Iterator<RevCommit> it=cho.getCommitResults().iterator();
		while(it.hasNext()){
			RevCommit rc=it.next();
			System.out.println(rc.getName()+"<---->"+rc.getShortMessage());
		}
	}
	
	@Test
	public void testCommitHistoryAnyObjectId() throws Exception {
		setupRepository();
		LogOperation cho=new LogOperation(repository,initialCommit.getId());
		cho.execute();
		Iterator<RevCommit> it=cho.getCommitResults().iterator();
		while(it.hasNext()){
			RevCommit rc=it.next();
			System.out.println(rc.getName()+"<---->"+rc.getShortMessage());
		}
	}
	
	@Test
	public void testCommitHistoryAnyObjectIdWithMaxCount() throws Exception {
		setupRepository();
		LogOperation cho=new LogOperation(repository,initialCommit.getId());
		cho.setMaxCount(2);
		cho.execute();
		Iterator<RevCommit> it=cho.getCommitResults().iterator();
		while(it.hasNext()){
			RevCommit rc=it.next();
			System.out.println(rc.getName()+"<---->"+rc.getShortMessage());
		}
	}
	
	
	@Test
	public void testCommitHistoryPath() throws Exception {
		setupRepository();
		LogOperation cho=new LogOperation(repository,"dummy.txt");
		cho.execute();
		Iterator<RevCommit> it=cho.getCommitResults().iterator();
		while(it.hasNext()){
			RevCommit rc=it.next();
			System.out.println(rc.getName()+"<---->"+rc.getShortMessage());
		}
	}
	
	@Test
	public void testCommitHistoryPathWithMaxCount() throws Exception {
		setupRepository();
		LogOperation cho=new LogOperation(repository,"dummy.txt");
		cho.setMaxCount(1);
		cho.execute();
		Iterator<RevCommit> it=cho.getCommitResults().iterator();
		while(it.hasNext()){
			RevCommit rc=it.next();
			System.out.println(rc.getName()+"<---->"+rc.getShortMessage());
		}
	}
	
	private void setupRepository() throws Exception {
		// create first commit containing a dummy file
		file=new File(workdir, "dummy.txt");
		FileUtils.createNewFile(file);
		repositoryUtil.track(file);
		repositoryUtil.commit("first commit");
		
		//update the file content and commit
		
		repositoryUtil.appendFileContent(file, "test a");
		repositoryUtil.track(file);
		repositoryUtil.commit("second commit");
		
		//update the file content and commit again
		
		repositoryUtil.appendFileContent(file, "test b");
		repositoryUtil.track(file);
		initialCommit=repositoryUtil.commit("third commit");
	}

}
