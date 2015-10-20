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
import com.miracle.apps.git.core.op.RemoveFromIndexOperation;
import com.miracle.apps.git.test.core.GitTestCase;
import com.miracle.apps.git.core.RepositoryUtil;

public class RemoveFromIndexOperationTest extends GitTestCase {
	ArrayList<String> list=new ArrayList<String>();

	RepositoryUtil repositoryUtil;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		gitDir = new File("D://MLGitHome", Constants.DOT_GIT);
		
		repositoryUtil = new RepositoryUtil(gitDir);
		
		repository=repositoryUtil.createLocalRepositoryByGitDir();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		repositoryUtil.dispose();
//		RepositoryUtil.removeLocalRepository(repository);
		super.tearDown();
	}

	@Test
	public void testUntrackFile() throws Exception {
		
		File file1 = new File(repository.getWorkTree(), "a.txt");
		
		File file2 = new File(repository.getWorkTree(), "b.txt");
		
		list.add(repositoryUtil.getRepoRelativePath(file1.getAbsolutePath()));
		list.add(repositoryUtil.getRepoRelativePath(file2.getAbsolutePath()));
		
		assertTrue(repositoryUtil.inIndex(file1.getAbsolutePath()));
		assertTrue(repositoryUtil.inIndex(file2.getAbsolutePath()));

		
		new RemoveFromIndexOperation(list, repository).execute();
		
		assertFalse(repositoryUtil.inIndex(file1.getAbsolutePath()));
		assertFalse(repositoryUtil.inIndex(file2.getAbsolutePath()));
	}

	@Test
	public void testUntrackFilesInFolder() throws Exception {
		File file1 = new File(repository.getWorkTree(), "sub/c.txt");

		File file2 = new File(repository.getWorkTree(), "sub/d.txt");

		list.add(repositoryUtil.getRepoRelativePath(file1.getAbsolutePath()));
		list.add(repositoryUtil.getRepoRelativePath(file2.getAbsolutePath()));
		
		assertTrue(repositoryUtil.inIndex(file1.getAbsolutePath()));
		assertTrue(repositoryUtil.inIndex(file2.getAbsolutePath()));

		new RemoveFromIndexOperation(list, repository).execute();
		
		assertFalse(repositoryUtil.inIndex(file1.getAbsolutePath()));
		assertFalse(repositoryUtil.inIndex(file2.getAbsolutePath()));
	}
	
	
	@Test
	public void testInIndex() throws Exception {
		File file1 = new File(repository.getWorkTree(), "sub/c.txt");

		File file2 = new File(repository.getWorkTree(), "sub/d.txt");

		assertFalse(repositoryUtil.inIndex(file1.getAbsolutePath()));
		assertFalse(repositoryUtil.inIndex(file2.getAbsolutePath()));
		
		
		assertTrue(repositoryUtil.inIndex(file1.getAbsolutePath()));
		assertTrue(repositoryUtil.inIndex(file2.getAbsolutePath()));
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
