package com.miracle.apps.git.core.test.op;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ReflogEntry;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.miracle.apps.git.core.op.CommitOperation;
import com.miracle.apps.git.core.op.CreateLocalBranchOperation;
import com.miracle.apps.git.core.op.DiffOperation;
import com.miracle.apps.git.core.op.ReflogOperation;
import com.miracle.apps.git.test.core.GitTestCase;
import com.miracle.apps.git.core.RepositoryUtil;

public class DiffOperationTest extends GitTestCase {
	
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
	public void testDiffModified() throws Exception {
		setupRepository();
		//modify the file content
		repositoryUtil.appendFileContent(file, "modify file");
		
		DiffOperation dop=new DiffOperation(repository,"dummy.txt");
		dop.execute();
		
		System.out.println(dop.getDiffInfo().length());
		System.out.println(dop.getDiffInfo());
		
	}
	
	@Test
	public void testDiffCached() throws Exception {
		setupRepository();
		File folder=new File(workdir, "folder");
		FileUtils.mkdir(folder, true);
		folder=new File(folder, "file.txt");
		FileUtils.createNewFile(folder);
		repositoryUtil.track(folder);
		System.out.println(repositoryUtil.getRepoRelativePath(folder.getAbsolutePath()));
		DiffOperation dop=new DiffOperation(repository,repositoryUtil.getRepoRelativePath(folder.getAbsolutePath()));
		dop.execute();
		
		System.out.println(dop.getDiffInfo().length());
		System.out.println(dop.getDiffInfo());
		
	}
	
	@Test
	public void testDiffWithCommit() throws Exception {
		setupRepository();
		
//		//modify the file content
//		repositoryUtil.appendFileContent(file, "modify file");
		
		DiffOperation dop=new DiffOperation(repository,"dummy.txt");
		dop.execute();
		System.out.println(dop.getDiffInfo());
		
	}
	
	private void setupRepository() throws Exception {
		// create first commit containing a dummy file
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
