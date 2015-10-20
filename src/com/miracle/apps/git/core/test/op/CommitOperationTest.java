package com.miracle.apps.git.core.test.op;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
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
	public final String AUTHOR = "The Author <The.author@some.com>";

	public final String COMMITTER = "The Commiter <The.committer@some.com>";
	
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
	public void testCommitAddedToIndexDeletedInWorkspace() throws Exception {
		
		File file1 = new File(repository.getWorkTree(), "c.txt");
		FileUtils.createNewFile(file1);
		PrintWriter writer = new PrintWriter(file1);
		writer.print("content c");
		writer.close();
		
		list.add(repositoryUtil.getRepoRelativePath(file1.getAbsolutePath()));
		
		new AddToIndexOperation(list, repository).execute();
		
		
//		CommitOperation commitOperation = new CommitOperation(null, null, AUTHOR, COMMITTER, "first commit");
//		commitOperation.setCommitAll(true);
//		commitOperation.setRepository(repository);
//		commitOperation.execute();
		
		
		
		CommitOperation commitOperation = new CommitOperation(list, null, AUTHOR, COMMITTER, "third commit c.txt");
		commitOperation.setRepository(repository);
		commitOperation.execute();

	}

	@Test
	public void testCommitAll()throws Exception {
		File file1 = new File(repository.getWorkTree(), "sub/c.txt");
		File file2 = new File(repository.getWorkTree(), "sub/d.txt");

		list.add(repositoryUtil.getRepoRelativePath(file1.getAbsolutePath()));
		list.add(repositoryUtil.getRepoRelativePath(file2.getAbsolutePath()));
		
		new AddToIndexOperation(list, repository).execute();
		
		CommitOperation commitOperation = new CommitOperation(list, null, AUTHOR, COMMITTER, "fourth commit sub");
		commitOperation.setRepository(repository);
		commitOperation.execute();
	}
	
	
	@Test
	public void testCommitUntracked() throws Exception {
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
		list.add(repositoryUtil.getRepoRelativePath(file2.getAbsolutePath()));
		
		CommitOperation commitOperation = new CommitOperation(repository,list, list, AUTHOR, COMMITTER, "untracked commit testing");
		commitOperation.execute();
		
	}
	
	@Test
	public void testCommitStatus() throws Exception {
		
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
