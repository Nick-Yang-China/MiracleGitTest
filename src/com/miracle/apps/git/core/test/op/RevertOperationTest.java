package com.miracle.apps.git.core.test.op;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.miracle.apps.git.core.RepositoryUtil;
import com.miracle.apps.git.core.op.RevertCommitOperation;
import com.miracle.apps.git.test.core.GitTestCase;

public class RevertOperationTest extends GitTestCase {
	
	File workdir;
	
	RepositoryUtil repositoryUtil;
	
	RevCommit initialCommit;
	
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
	public void testHardReset() throws Exception {
		setupRepository();
		
		//add another file of file.txt
		File file=new File(workdir, "file.txt");
		FileUtils.createNewFile(file);
		repositoryUtil.appendFileContent(file, "adding the file.txt");
		repositoryUtil.track(file);
		RevCommit firstCommit=repositoryUtil.commit("RevertOperationTest\n\nadding file.txt commit\n");
		
		RevertCommitOperation rco=new RevertCommitOperation(repository, Arrays.asList(firstCommit));
		rco.execute();
		System.out.println(rco.getNewHead().getName());
		System.out.println(file.exists());
		assertFalse(file.exists());
	}
	
	@Test
	public void testHardResetWithStragey() throws Exception {
		setupRepository();
		File file=new File(workdir, "file.txt");
		FileUtils.createNewFile(file);
		repositoryUtil.appendFileContent(file, "adding the file.txt");
		repositoryUtil.track(file);
		repositoryUtil.commit("RevertOperationTest\n\nadding file.txt commit\n");
		
		RevertCommitOperation rco=new RevertCommitOperation(repository, Arrays.asList(initialCommit));
		rco.setStrategyName("ours".toUpperCase());
		rco.execute();
		
		System.out.println(rco.getNewHead().getName());
		assertTrue(file.exists());
		
		assertEquals("test a", getFileContent(new File(workdir, "dummy.txt")));
	}
	
	private void setupRepository() throws Exception {
		// create first commit containing a dummy file
		File file=new File(workdir, "dummy.txt");
		FileUtils.createNewFile(file);
		repositoryUtil.track(file);
		repositoryUtil.commit("RevertOperationTest\n\nfirst commit\n");
		
		//update the file content and commit
		
		repositoryUtil.appendFileContent(file, "test a");
		repositoryUtil.track(file);
		repositoryUtil.commit("RevertOperationTest\n\nsecond commit\n");
		
		//update the file content and commit again
		
		repositoryUtil.appendFileContent(file, "test b");
		repositoryUtil.track(file);
		initialCommit=repositoryUtil.commit("RevertOperationTest\n\nthird commit\n");
	}
	
	private String getFileContent(File file){
		BufferedReader br=null;
		String temp=null;
		try {
			br=new BufferedReader(new FileReader(file));
			
			temp=br.readLine();
			
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return temp;
	}
}
