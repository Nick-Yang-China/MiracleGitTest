package com.miracle.apps.git.core.test.op;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.miracle.apps.git.core.op.AddToIndexOperation;
import com.miracle.apps.git.test.core.GitTestCase;
import com.miracle.apps.git.core.RepositoryUtil;

public class AddToIndexOperationTest extends GitTestCase {
	ArrayList<String> list=new ArrayList<>();

	RepositoryUtil repositoryUtil;
	File workdir;
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
		repositoryUtil.removeLocalRepository(repository);
		super.tearDown();
	}

	@Test
	public void testTrackFile() throws Exception {
		File file1 = new File(repository.getWorkTree(), "a.txt");
		FileUtils.createNewFile(file1);
		PrintWriter writer = new PrintWriter(file1);
		writer.print("content a");
		writer.close();
		
		File file2 = new File(repository.getWorkTree(), "b.txt");
		FileUtils.createNewFile(file2);
		writer = new PrintWriter(file2);
		writer.print("content b");
		writer.close();
		
		list.add(repositoryUtil.getRepoRelativePath(file1.getAbsolutePath()));
		list.add(repositoryUtil.getRepoRelativePath(file2.getAbsolutePath()));
		
		assertFalse(repositoryUtil.inIndex(file1.getAbsolutePath()));
		assertFalse(repositoryUtil.inIndex(file2.getAbsolutePath()));
		
		new AddToIndexOperation(list, repository).execute();
		
		assertTrue(repositoryUtil.inIndex(file1.getAbsolutePath()));
		assertTrue(repositoryUtil.inIndex(file2.getAbsolutePath()));
	}

	@Test
	public void testTrackFilesInFolder() throws Exception {
		FileUtils.mkdir(new File(repository.getWorkTree(), "sub"));
		File file1 = new File(repository.getWorkTree(), "sub/c.txt");
		FileUtils.createNewFile(file1);
		PrintWriter writer = new PrintWriter(file1);
		writer.print("content c");
		writer.close();

		File file2 = new File(repository.getWorkTree(), "sub/d.txt");
		FileUtils.createNewFile(file2);
		writer = new PrintWriter(file2);
		writer.print("content d");
		writer.close();

		list.add(repositoryUtil.getRepoRelativePath(file1.getAbsolutePath()));
		list.add(repositoryUtil.getRepoRelativePath(file2.getAbsolutePath()));
		
		assertFalse(repositoryUtil.inIndex(file1.getAbsolutePath()));
		assertFalse(repositoryUtil.inIndex(file2.getAbsolutePath()));
		
		new AddToIndexOperation(list, repository).execute();
		
		assertTrue(repositoryUtil.inIndex(file1.getAbsolutePath()));
		assertTrue(repositoryUtil.inIndex(file2.getAbsolutePath()));
	}
	
	
	@Test
	public void testInIndex() throws Exception {
		File file1 = new File(repository.getWorkTree(), "sub/c.txt");

		File file2 = new File(repository.getWorkTree(), "sub/d.txt");

		assertFalse(repositoryUtil.inIndex(file1.getAbsolutePath()));
		assertFalse(repositoryUtil.inIndex(file2.getAbsolutePath()));
	}
	
	@Test
	public void testIndexStatus() throws Exception {
		
		Git git=new Git(repository);
		StatusCommand statu=git.status();
		
			Status ss=statu.call();
			System.out.println("--------------untracked files-------------");
			Set<String> set=ss.getUntracked();
			
			for(String temp:set){
				System.out.println(temp);
			}
		    System.out.println("----------------tracked files-----------");
			set=ss.getAdded();
			
			for(String temp:set){
				System.out.println(temp);
			}
	}
}
