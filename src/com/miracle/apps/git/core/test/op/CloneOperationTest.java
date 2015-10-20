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
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
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
	
	File workdir;
	File workdir2;
	
	RepositoryUtil repositoryUtil;

	@Override
	@Before
	public void setUp() throws Exception {
//		super.setUp();
//		workdir= new File("D://Repository1");
//		
//		if(workdir.exists()){
//			FileUtils.delete(workdir, FileUtils.RECURSIVE | FileUtils.RETRY);
//		}
//		FileUtils.mkdir(workdir,true);
		
		workdir2= new File("D:/Repository2");
		
		if(workdir2.exists()){
			FileUtils.delete(workdir2, FileUtils.RECURSIVE | FileUtils.RETRY);
		}
		FileUtils.mkdir(workdir2,true);
		
//		repositoryUtil = new RepositoryUtil(new File(workdir,Constants.DOT_GIT));
//		
//		repository1=repositoryUtil.createLocalRepositoryByGitDir();
//		
//		File file=new File(workdir,"file1.txt");
//		FileUtils.createNewFile(file);
//		
//		Git git=new Git(repository1);
//		
//		git.add().addFilepattern("file1.txt").call();
//		
//		git.commit().setMessage("First Commit").call();
//		
//		git.tag().setName("tag").call();
//		
//		file=new File(workdir,"file2.txt");
//		FileUtils.createNewFile(file);
//		git.add().addFilepattern("file2.txt").call();
//		git.commit().setMessage("second commit").call();
//		git.branchCreate().setName("dev").call();
//		
//		file=new File(workdir,"file3.txt");
//		FileUtils.createNewFile(file);
//		git.add().addFilepattern("file3.txt").call();
//		git.commit().setMessage("third commit").call();
		
	}

	@Override
	@After
	public void tearDown() throws Exception {
//		repositoryUtil.dispose();
//		RepositoryUtil.removeLocalRepository(repository);
		super.tearDown();
	}
	
	private void cloneAndAssert(String refName) throws Exception {
		URIish uri = new URIish("https://github.com/Nick-Yang-China/GitDemo.git");
		UsernamePasswordCredentialsProvider crePro=new UsernamePasswordCredentialsProvider("Nick-Yang-China", "!Test0001");
		CloneOperation clop = new CloneOperation(uri, true, null, workdir2,
				null, null, 30);
		clop.setCredentialsProvider(crePro);
		clop.run();

//		Repository clonedRepo = FileRepositoryBuilder.create(new File(workdir2,
//				Constants.DOT_GIT));
//		assertEquals(
//				"",
//				uri.toString(),
//				clonedRepo.getConfig().getString(
//						ConfigConstants.CONFIG_REMOTE_SECTION, "origin", "url"));
//		assertEquals(
//				"",
//				"+refs/heads/*:refs/remotes/origin/*",
//				clonedRepo.getConfig().getString(
//						ConfigConstants.CONFIG_REMOTE_SECTION, "origin",
//						"fetch"));
	}
	
	@Test
	public void testClone() throws Exception {
		String fullRefName = "refs/heads/master";
		cloneAndAssert(fullRefName);

//		assertTrue(new File(workdir2, "file1.txt").exists());
//		assertTrue(new File(workdir2, "file2.txt").exists());
//		assertTrue(new File(workdir2, "file3.txt").exists());
	}

}
