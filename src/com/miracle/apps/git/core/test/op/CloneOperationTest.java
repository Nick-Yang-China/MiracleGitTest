package com.miracle.apps.git.core.test.op;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.miracle.apps.git.core.op.AddToIndexOperation;
import com.miracle.apps.git.core.op.CloneOperation;
import com.miracle.apps.git.core.op.CommitOperation;
import com.miracle.apps.git.core.op.RemoveFromIndexOperation;
import com.miracle.apps.git.test.core.GitTestCase;
import com.miracle.apps.git.core.RepositoryUtil;

public class CloneOperationTest extends GitTestCase {
//	Repository repository1;
	
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
		
		Git git=new Git(repository);
		
		git.add().addFilepattern("file1.txt").call();
		git.commit().setMessage("First Commit").call();
		git.tag().setName("tag").call();
		
		file=new File(workdir,"file2.txt");
		FileUtils.createNewFile(file);
		git.add().addFilepattern("file2.txt").call();
		git.commit().setMessage("second commit").call();
		git.branchCreate().setName("dev").call();
		
		file=new File(workdir,"file3.txt");
		FileUtils.createNewFile(file);
		git.add().addFilepattern("file3.txt").call();
		git.commit().setMessage("third commit").call();
		
	}

	@Override
	@After
	public void tearDown() throws Exception {
		if(repository!=null)
			repository.close();
		
		if (workdir.exists())
			FileUtils.delete(workdir, FileUtils.RECURSIVE | FileUtils.RETRY);
		if (workdir2.exists())
			FileUtils.delete(workdir2, FileUtils.RECURSIVE | FileUtils.RETRY);
		super.tearDown();
	}
	
	private void cloneAndAssert(String refName,boolean BranchFlag) throws Exception {		
		String uri = "file:///"+ repository.getDirectory().toString();
//		String uri = "ssh://root@192.168.1.111:22/project/gitserver/831server/test.git";
		CloneOperation clop;
		if(BranchFlag)
			 clop = new CloneOperation(uri, false, Arrays.asList("refs/heads/master"), workdir2, refName, "origin", 0, "root", "111111");
		else
			 clop = new CloneOperation(uri, true, null, workdir2, refName, "origin", 0, null, null);
		
		clop.execute();
		System.out.println(clop.getCloneStatus());
		Repository clonedRepo = FileRepositoryBuilder.create(new File(workdir2,
				Constants.DOT_GIT));
		System.out.println(clonedRepo.getWorkTree());
		System.out.println(uri.toString());
		System.out.println(clonedRepo.getConfig().getString(
						ConfigConstants.CONFIG_REMOTE_SECTION, "origin", "url"));
		
		System.out.println(clonedRepo.getConfig().getString(
						ConfigConstants.CONFIG_REMOTE_SECTION, "origin",
						"fetch"));
		assertEquals(
				"",
				uri.toString(),
				clonedRepo.getConfig().getString(
						ConfigConstants.CONFIG_REMOTE_SECTION, "origin", "url"));
		assertEquals(
				"",
				"+refs/heads/*:refs/remotes/origin/*",
				clonedRepo.getConfig().getString(
						ConfigConstants.CONFIG_REMOTE_SECTION, "origin",
						"fetch"));
		
		for(Ref ref:Git.wrap(clonedRepo).branchList().call()){
			System.out.println(ref.getName());
		}
		
		clonedRepo.close();
	}
	
	@Test
	public void testClone() throws Exception {
		String fullRefName = "master";
		cloneAndAssert(fullRefName,false);

		assertTrue(new File(workdir2, "file1.txt").exists());
		assertTrue(new File(workdir2, "file2.txt").exists());
		assertTrue(new File(workdir2, "file3.txt").exists());
	}
	@Test
	public void testCloneBranch() throws Exception {
		String branchName = "dev";
		cloneAndAssert(branchName,false);

		assertTrue(new File(workdir2, "file1.txt").exists());
		assertTrue(new File(workdir2, "file2.txt").exists());
		assertFalse(new File(workdir2, "file3.txt").exists());
	}

	@Test
	public void testCloneTag() throws Exception {
		String tagName = "tag";
		cloneAndAssert(tagName,false);

		assertTrue(new File(workdir2, "file1.txt").exists());
		assertFalse(new File(workdir2, "file2.txt").exists());
		assertFalse(new File(workdir2, "file3.txt").exists());
	}
		
	@Test
	public void testCloneWithBranchList() throws Exception{
		String branchName = "master";
		cloneAndAssert(branchName,true);
		
	}
	
	@Test
	public void testWithWrongInfo()throws Exception{
		CloneOperation co=new CloneOperation("D:/EgitShow/.git", true, null, new File("D:/test1"), "master", Constants.DEFAULT_REMOTE_NAME, 0, "root", "123123");
	    co.execute();
		System.out.println(co.getCloneStatus());
		System.out.println(co.getFlag());
	}
	
	
	@Test
	public void testCloneSSH2RSAKey()throws Exception{
		String uri = "ssh://Administrator@192.168.1.109/icw/home/administrator/831test.git";
		
		CloneOperation	clop = new CloneOperation(uri, false, Arrays.asList("refs/heads/master"), workdir2, "master", "origin", 0, "Administrator", "1");
		clop.execute();
		System.out.println(clop.getCloneStatus());
		Repository clonedRepo = FileRepositoryBuilder.create(new File(workdir2,
				Constants.DOT_GIT));
		System.out.println(clonedRepo.getWorkTree());
		System.out.println(clonedRepo.getConfig().getString(
						ConfigConstants.CONFIG_REMOTE_SECTION, "origin", "url"));
		
		System.out.println(clonedRepo.getConfig().getString(
						ConfigConstants.CONFIG_REMOTE_SECTION, "origin",
						"fetch"));
		assertEquals(
				"",
				uri.toString(),
				clonedRepo.getConfig().getString(
						ConfigConstants.CONFIG_REMOTE_SECTION, "origin", "url"));
		assertEquals(
				"",
				"+refs/heads/*:refs/remotes/origin/*",
				clonedRepo.getConfig().getString(
						ConfigConstants.CONFIG_REMOTE_SECTION, "origin",
						"fetch"));
		
		for(Ref ref:Git.wrap(clonedRepo).branchList().call()){
			System.out.println(ref.getName());
		}
		
		clonedRepo.close();
	}
	
}
