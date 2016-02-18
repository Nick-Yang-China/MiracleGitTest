package com.miracle.apps.git.core.test.op;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.miracle.apps.git.core.op.AddToIndexOperation;
import com.miracle.apps.git.core.op.CommitOperation;
import com.miracle.apps.git.core.op.RemoveFromIndexOperation;
import com.miracle.apps.git.test.core.GitTestCase;
import com.miracle.apps.git.core.RepositoryUtil;

public class CommitOperationTest extends GitTestCase {
	
	ArrayList<String> list=new ArrayList<String>();

	RepositoryUtil repositoryUtil;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		gitDir = new File("D://Repository1", Constants.DOT_GIT);
		
		repositoryUtil = new RepositoryUtil(gitDir);
		
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
	public void testCommitAddedToIndex() throws Exception {
		
		File file1 = new File(repository.getWorkTree(), "c.txt");
		FileUtils.createNewFile(file1);
		PrintWriter writer = new PrintWriter(file1);
		writer.print("content c");
		writer.close();
		
		list.add(repositoryUtil.getRepoRelativePath(file1.getAbsolutePath()));
		
		new AddToIndexOperation(list, repository).execute();
		
		CommitOperation commitOperation = new CommitOperation(repository,list, null, AUTHOR, COMMITTER, "third commit c.txt");
		commitOperation.execute();
		testCommitStatus();
	}
	
	@Test
	public void testDoubleCommitException() throws Exception {
		
		File file1 = new File(repository.getWorkTree(), "c.txt");
		FileUtils.createNewFile(file1);
		PrintWriter writer = new PrintWriter(file1);
		writer.print("content c");
		writer.close();
		
		list.add(repositoryUtil.getRepoRelativePath(file1.getAbsolutePath()));
		
		new AddToIndexOperation(list, repository).execute();
		
//		System.out.println(CommitOperation.CheckIfNoChangesBeforeCommit(repository));
		
		CommitOperation commitOperation = new CommitOperation(repository,list, null, AUTHOR, COMMITTER, "third commit c.txt");
//		commitOperation.setCommitAll(true);
		commitOperation.execute();
		System.out.println(commitOperation.isIfNoChanges());
		
		commitOperation = new CommitOperation(repository,list, null, AUTHOR, COMMITTER, "third commit c.txt");
//		commitOperation.setCommitAll(true);
		commitOperation.execute();
		System.out.println(commitOperation.isIfNoChanges());
		
		
		commitOperation = new CommitOperation(repository,list, null, AUTHOR, COMMITTER, "third commit c.txt");
//		commitOperation.setCommitAll(true);
		commitOperation.execute();
		System.out.println(commitOperation.isIfNoChanges());
//		testCommitStatus();
	}

	@Test
	public void testCommitAll()throws Exception {
		FileUtils.mkdir(new File(repository.getWorkTree(),"sub"));
		File file1 = new File(repository.getWorkTree(), "sub/c.txt");
		FileUtils.createNewFile(file1);
		PrintWriter writer = new PrintWriter(file1);
		writer.print("content sub/c");
		writer.close();
		File file2 = new File(repository.getWorkTree(), "sub/d.txt");
		FileUtils.createNewFile(file2);
		writer = new PrintWriter(file2);
		writer.print("content sub/d");
		writer.close();
		
		list.add(repositoryUtil.getRepoRelativePath(file1.getAbsolutePath()));
		list.add(repositoryUtil.getRepoRelativePath(file2.getAbsolutePath()));
		
		new AddToIndexOperation(list, repository).execute();
		
		CommitOperation commitOperation = new CommitOperation(repository,list, null, AUTHOR, COMMITTER, "fourth commit sub");
		commitOperation.setCommitAll(true);
		commitOperation.execute();
		testCommitStatus();
	}
	
	@Test
	public void testCommitAllwithOtherConstructor()throws Exception {
		FileUtils.mkdir(new File(repository.getWorkTree(),"sub"));
		File file1 = new File(repository.getWorkTree(), "sub/c.txt");
		FileUtils.createNewFile(file1);
		PrintWriter writer = new PrintWriter(file1);
		writer.print("content sub/c");
		writer.close();
		File file2 = new File(repository.getWorkTree(), "sub/d.txt");
		FileUtils.createNewFile(file2);
		writer = new PrintWriter(file2);
		writer.print("content sub/d");
		writer.close();
		
		list.add(repositoryUtil.getRepoRelativePath(file1.getAbsolutePath()));
		list.add(repositoryUtil.getRepoRelativePath(file2.getAbsolutePath()));
		
		new AddToIndexOperation(list, repository).execute();
		CommitOperation commitOperation = new CommitOperation(repository, AUTHOR, COMMITTER, "fourth commit sub");
//		commitOperation.setCommitAll(true);
		commitOperation.execute();
		testCommitStatus();
	}
	
	
	@Test
	public void testCommitUntracked() throws Exception {
		FileUtils.mkdir(new File(repository.getWorkTree(),"sub"));
		File file1 = new File(repository.getWorkTree(), "sub/e.txt");
		FileUtils.createNewFile(file1);
		PrintWriter writer = new PrintWriter(file1);
		writer.print("content e");
		writer.close();
		
		File file2 = new File(repository.getWorkTree(), "sub/F.txt");
		FileUtils.createNewFile(file2);
		writer = new PrintWriter(file2);
		writer.print("content F");
		writer.close();
		
		list.add(repositoryUtil.getRepoRelativePath(file1.getAbsolutePath()));
		
		new AddToIndexOperation(list, repository).execute();
		
		list.add(repositoryUtil.getRepoRelativePath(file2.getAbsolutePath()));
		
		CommitOperation commitOperation = new CommitOperation(repository,list, list, AUTHOR, COMMITTER, "untracked commit testing");
		commitOperation.execute();
		testCommitStatus();
		
	}
	
	@Test
	public void testCommitwithDeleteFiles() throws Exception {
		
		File file1 = new File(repository.getWorkTree(), "c.txt");
		FileUtils.createNewFile(file1);
		PrintWriter writer = new PrintWriter(file1);
		writer.print("content c");
		writer.close();
		
		File file2 = new File(repository.getWorkTree(), "d.txt");
		FileUtils.createNewFile(file2);
		writer = new PrintWriter(file2);
		writer.print("content d");
		writer.close();
		
		
		list.add(file1.getAbsolutePath());
		list.add(file2.getAbsolutePath());
		
		Collection<String> paths=repositoryUtil.getRepoRelativePathwithMulitPaths(list);
		
		new AddToIndexOperation(paths, repository).execute();
		
		CommitOperation commitOperation = new CommitOperation(repository,paths, null, AUTHOR, COMMITTER, "third commit c.txt");
		commitOperation.execute();
		
		//delete file of d.txt 
		file2.delete();
		
		testStatus();
		//方法一
		commitOperation = new CommitOperation(repository,Arrays.asList("d.txt"), null, AUTHOR, COMMITTER, "fourth commit c.txt");
		commitOperation.execute();
		//方法二
//		commitOperation = new CommitOperation(repository, AUTHOR, COMMITTER, "Deleted the file of d.txt");
//		commitOperation.setCommitAll(true);
//		commitOperation.execute();
		testCommitLog();
		testStatus();
	}
	
	private void testCommitLog() throws Exception {
		
		Git git=new Git(repository);
			System.out.println("----------------commit details-----------");
			Iterator<RevCommit> commits = git.log().call().iterator();
			while(commits.hasNext()){
			RevCommit secondCommit = commits.next();
			assertTrue(secondCommit.getCommitTime() > 0);

			System.out.println("commit "+secondCommit.getId().getName());
			System.out.print("Author: "+secondCommit.getAuthorIdent().getName()+"<-->");
			System.out.println(secondCommit.getAuthorIdent().getEmailAddress());
			System.out.print("Committer: "+secondCommit.getCommitterIdent().getName()+"<-->");
			System.out.println(secondCommit.getCommitterIdent().getEmailAddress());
			System.out.println("Commit Contents:"+secondCommit.getFullMessage());
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			try (TreeWalk treeWalk = new TreeWalk(repository)) {
				treeWalk.addTree(secondCommit.getTree().getId());
				treeWalk.setRecursive(true);
				treeWalk.setPostOrderTraversal(false);
				
				while(treeWalk.next()){
					System.out.println(treeWalk.getPathString());
				}
			}
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			System.out.println("*******************************************************");
		}
	}
	
	private void testStatus() throws NoWorkTreeException, GitAPIException{
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
			System.out.println("----------------Uncommit Changes-----------");
			set=ss.getUncommittedChanges();
			
			for(String temp:set){
				System.out.println(temp);
			}
			
			System.out.println("----------------get Changes-----------");
			set=ss.getChanged();
			
			for(String temp:set){
				System.out.println(temp);
			}
			
			System.out.println("----------------get Missing-----------");
			set=ss.getMissing();
			
			for(String temp:set){
				System.out.println(temp);
			}
			
			System.out.println("----------------get Modified-----------");
			set=ss.getModified();
			
			for(String temp:set){
				System.out.println(temp);
			}
			
			System.out.println("----------------get removed-----------");
			set=ss.getRemoved();
			
			for(String temp:set){
				System.out.println(temp);
			}
	}
	
	private void testCommitStatus() throws NoWorkTreeException, GitAPIException, MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException{
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
			System.out.println("----------------Uncommit Changes-----------");
			set=ss.getUncommittedChanges();
			
			for(String temp:set){
				System.out.println(temp);
			}
			
			System.out.println("----------------get Changes-----------");
			set=ss.getChanged();
			
			for(String temp:set){
				System.out.println(temp);
			}
			
			System.out.println("----------------get Missing-----------");
			set=ss.getMissing();
			
			for(String temp:set){
				System.out.println(temp);
			}
			
			System.out.println("----------------get Modified-----------");
			set=ss.getModified();
			
			for(String temp:set){
				System.out.println(temp);
			}
			
			System.out.println("----------------get removed-----------");
			set=ss.getRemoved();
			
			for(String temp:set){
				System.out.println(temp);
			}
			
			System.out.println("----------------commit details-----------");
			Iterator<RevCommit> commits = git.log().call().iterator();
			while(commits.hasNext()){
			RevCommit secondCommit = commits.next();
			assertTrue(secondCommit.getCommitTime() > 0);

			System.out.println("commit "+secondCommit.getId().getName());
			System.out.print("Author: "+secondCommit.getAuthorIdent().getName()+"<-->");
			System.out.println(secondCommit.getAuthorIdent().getEmailAddress());
			System.out.print("Committer: "+secondCommit.getCommitterIdent().getName()+"<-->");
			System.out.println(secondCommit.getCommitterIdent().getEmailAddress());
			System.out.println("Commit Contents:"+secondCommit.getFullMessage());
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			try (TreeWalk treeWalk = new TreeWalk(repository)) {
				treeWalk.addTree(secondCommit.getTree().getId());
				treeWalk.setRecursive(true);
				treeWalk.setPostOrderTraversal(false);
				
				while(treeWalk.next()){
					System.out.println(treeWalk.getPathString());
				}
			}
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			System.out.println("*******************************************************");
		}
	}
}
