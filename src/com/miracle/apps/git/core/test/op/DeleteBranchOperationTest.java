package com.miracle.apps.git.core.test.op;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
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
import com.miracle.apps.git.core.op.DeleteBranchOperation;
import com.miracle.apps.git.core.op.PushOperation;
import com.miracle.apps.git.core.op.PushOperationResult;
import com.miracle.apps.git.core.op.PushOperationSpecification;
import com.miracle.apps.git.core.op.RemoveFromIndexOperation;
import com.miracle.apps.git.test.core.GitTestCase;
import com.miracle.apps.git.core.RepositoryUtil;

public class DeleteBranchOperationTest extends GitTestCase {
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
	public void testDeleteBranch() throws Exception {
		String br1=repository.getFullBranch();
		System.out.println(br1);
		File file=new File(workdir,"file1.txt");
		FileUtils.createNewFile(file);
		
		List<String> list=Arrays.asList(repositoryUtil.getRepoRelativePath(file.getAbsolutePath()));
		
		AddToIndexOperation addfile=new AddToIndexOperation(list, repository);
		
		addfile.execute();
		
		CommitOperation commitfile=new CommitOperation(repository, AUTHOR, COMMITTER, "first commit");
		commitfile.execute();
		
		this.getListBranchs(repository);
		
		//create branch of "test1"
		
		CreateLocalBranchOperation branchCre=new CreateLocalBranchOperation(repository, "test1", "master");
		branchCre.setCheckOutFlag(true);
		branchCre.execute();
		
		String br2=repository.getFullBranch();
		System.out.println(br2);
		this.getListBranchs(repository);
		//create delete branch of master
		
		DeleteBranchOperation dbo=new DeleteBranchOperation(repository, repository.getAllRefs().get("refs/heads/master"), false);
		
		dbo.execute();
		this.getListBranchs(repository);
		System.out.println(dbo.toString());
		
	}
	
	@Test
	public void testDeleteBranchwithCheckOut() throws Exception {
		String br1=repository.getFullBranch();
		System.out.println(br1);
		File file=new File(workdir,"file1.txt");
		FileUtils.createNewFile(file);
		
		List<String> list=Arrays.asList(repositoryUtil.getRepoRelativePath(file.getAbsolutePath()));
		
		AddToIndexOperation addfile=new AddToIndexOperation(list, repository);
		
		addfile.execute();
		
		CommitOperation commitfile=new CommitOperation(repository, AUTHOR, COMMITTER, "first commit");
		commitfile.execute();
		
		this.getListBranchs(repository);
		
		//create branch of "test1"
		
		CreateLocalBranchOperation branchCre=new CreateLocalBranchOperation(repository, "test1", "master");
		branchCre.setCheckOutFlag(true);
		branchCre.execute();
		
		String br2=repository.getFullBranch();
		System.out.println(br2);
		this.getListBranchs(repository);
		
		//create delete branch of test1
		
		DeleteBranchOperation dbo=new DeleteBranchOperation(repository, "test1", false);
		
		dbo.execute();
		this.getListBranchs(repository);
		System.out.println(dbo.toString());
	}
	
	@Test
	public void testDeleteBranchWithMulitBranchs() throws Exception {
		String br1=repository.getFullBranch();
		System.out.println(br1);
		File file=new File(workdir,"file1.txt");
		FileUtils.createNewFile(file);
		
		List<String> list=Arrays.asList(repositoryUtil.getRepoRelativePath(file.getAbsolutePath()));
		
		AddToIndexOperation addfile=new AddToIndexOperation(list, repository);
		
		addfile.execute();
		
		CommitOperation commitfile=new CommitOperation(repository, AUTHOR, COMMITTER, "first commit");
		commitfile.execute();
		
		this.getListBranchs(repository);
		
		//create branch of "test1"
		
		CreateLocalBranchOperation branchCre=new CreateLocalBranchOperation(repository, "test1", "master");
		branchCre.setCheckOutFlag(true);
		branchCre.execute();
		
		//create branch of test2
		branchCre=new CreateLocalBranchOperation(repository, "test2", "master");
		branchCre.execute();
		
		String br2=repository.getFullBranch();
		System.out.println(br2);
		this.getListBranchs(repository);
		
		//create delete branch of master and test2
		
		List<String> branchs=new ArrayList<>();
		
		branchs.add("test2");
		branchs.add("master");
		
		DeleteBranchOperation dbo=new DeleteBranchOperation(repository, branchs, false);
		
		dbo.execute();
		this.getListBranchs(repository);
		System.out.println(dbo.toString());
		
	}
	
	private void getListBranchs(Repository repository){
		Map<String,Ref> refs=repository.getAllRefs();
		
		for(String key:refs.keySet()){
			System.out.println("Key:"+key+" <--> values:"+refs.get(key).getName());
		}
		
		System.out.println("******************************************");
	}
}
