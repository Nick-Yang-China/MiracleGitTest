package com.miracle.apps.git.core.test.op;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.TrackingRefUpdate;
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
import com.miracle.apps.git.core.op.CreateLocalBranchOperation;
import com.miracle.apps.git.core.op.CreateLocalBranchOperation.UpstreamConfig;
import com.miracle.apps.git.core.op.PushOperation;
import com.miracle.apps.git.core.op.PushOperationResult;
import com.miracle.apps.git.core.op.PushOperationSpecification;
import com.miracle.apps.git.core.op.RemoveFromIndexOperation;
import com.miracle.apps.git.test.core.GitTestCase;
import com.miracle.apps.git.core.RepositoryUtil;

public class CreateLocalBranchOperationTest extends GitTestCase {
	File workdir;
	
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
	public void testCreateBranch() throws Exception {
		String br1=repository.getFullBranch();
		File file=new File(workdir,"file1.txt");
		FileUtils.createNewFile(file);
		
		List<String> list=Arrays.asList(repositoryUtil.getRepoRelativePath(file.getAbsolutePath()));
		
		AddToIndexOperation addfile=new AddToIndexOperation(list, repository);
		
		addfile.execute();
		
		CommitOperation commitfile=new CommitOperation(repository, AUTHOR, COMMITTER, "first commit");
		commitfile.execute();
		
		//create branch of "test1"
		
		CreateLocalBranchOperation branchCre=new CreateLocalBranchOperation(repository, "test1", "refs/heads/master");
		branchCre.setCheckOutFlag(true);
		branchCre.execute();
		
		String br2=repository.getFullBranch();
		System.out.println(br2);
		assertNotEquals("expected result", br1, br2);
	}
	
	@Test
	public void testCreateBranchWithTrackedfiles() throws Exception {
		String br1=repository.getFullBranch();
		File file=new File(workdir,"file1.txt");
		FileUtils.createNewFile(file);
		
		List<String> list=Arrays.asList(repositoryUtil.getRepoRelativePath(file.getAbsolutePath()));
		
		AddToIndexOperation addfile=new AddToIndexOperation(list, repository);
		
		addfile.execute();
		
		CommitOperation commitfile=new CommitOperation(repository, AUTHOR, COMMITTER, "first commit");
		commitfile.execute();
		
		file=new File(workdir,"file2.txt");
		FileUtils.createNewFile(file);
		
		list=Arrays.asList(repositoryUtil.getRepoRelativePath(file.getAbsolutePath()));
		
		addfile=new AddToIndexOperation(list, repository);
		
		addfile.execute();
		
//		create branch of "test1"
		Map<String,Ref> refs=repository.getAllRefs();
		
		for(String key:refs.keySet()){
			System.out.println("Key:"+key+"-- values:"+refs.get(key).getName());
		}
		
		CreateLocalBranchOperation branchCre=new CreateLocalBranchOperation(repository, "test1", repository.getAllRefs().get("refs/heads/master"), null);
		branchCre.setCheckOutFlag(true);
		branchCre.execute();
		
		String br2=repository.getFullBranch();
		System.out.println(br2);
		assertNotEquals("expected result", br1, br2);
	}
	
	@Test
	public void testCreateBranchWithRevcommit() throws Exception {
		String br1=repository.getFullBranch();
		File file=new File(workdir,"file1.txt");
		FileUtils.createNewFile(file);
		
		List<String> list=Arrays.asList(repositoryUtil.getRepoRelativePath(file.getAbsolutePath()));
		
		AddToIndexOperation addfile=new AddToIndexOperation(list, repository);
		
		addfile.execute();
		
		CommitOperation commitfile=new CommitOperation(repository, AUTHOR, COMMITTER, "first commit");
		commitfile.execute();
		RevCommit rev=commitfile.getCommit();
		
		file=new File(workdir,"file2.txt");
		FileUtils.createNewFile(file);
		
		list=Arrays.asList(repositoryUtil.getRepoRelativePath(file.getAbsolutePath()));
		
		addfile=new AddToIndexOperation(list, repository);
		
		addfile.execute();
		
		commitfile=new CommitOperation(repository, AUTHOR, COMMITTER, "second commit");
		commitfile.execute();
		
//		create branch of "test1"
		System.out.println(rev.getId().getName());
		CreateLocalBranchOperation branchCre=new CreateLocalBranchOperation(repository, "test1", rev);
		branchCre.setCheckOutFlag(true);
		branchCre.execute();
		
		String br2=repository.getFullBranch();
		
		assertTrue(new File(workdir,"file1.txt").exists());
		assertFalse(new File(workdir,"file2.txt").exists());
		assertNotEquals("expected result", br1, br2);
	}
	
	@Test
	public void testCreateBranchWithUpstreamConfig() throws Exception {
		String br1=repository.getFullBranch();
		File file=new File(workdir,"file1.txt");
		FileUtils.createNewFile(file);
		
		List<String> list=Arrays.asList(repositoryUtil.getRepoRelativePath(file.getAbsolutePath()));
		
		AddToIndexOperation addfile=new AddToIndexOperation(list, repository);
		
		addfile.execute();
		
		CommitOperation commitfile=new CommitOperation(repository, AUTHOR, COMMITTER, "first commit");
		commitfile.execute();
		CreateLocalBranchOperation branchCre=new CreateLocalBranchOperation(repository, "test1",repository.getAllRefs().get("refs/heads/master"),UpstreamConfig.MERGE);
		branchCre.setCheckOutFlag(true);
		branchCre.execute();
		
		String br2=repository.getFullBranch();
		
		assertNotEquals("expected result", br1, br2);
	}
	
}
