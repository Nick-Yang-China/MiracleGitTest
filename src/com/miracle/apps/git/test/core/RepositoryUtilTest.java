package com.miracle.apps.git.test.core;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.miracle.apps.git.core.RepositoryUtil;
import com.miracle.apps.git.core.op.AddToIndexOperation;
import com.miracle.apps.git.core.op.CommitOperation;

public class RepositoryUtilTest extends GitTestCase {
	RepositoryUtil repositoryUtil;
	
	Repository repository;
	@Before
	public void setUp() throws Exception {
		repositoryUtil=new RepositoryUtil("D://repo");
		
		repository=repositoryUtil.getRepository();
	}

	@After
	public void tearDown() throws Exception {
		repositoryUtil.dispose();
		repositoryUtil.removeLocalRepository(repository);
	}

	@Test
	public void getRepoRelativePathwithMulitPaths() {
		ArrayList<String> paths=new ArrayList<String>();
		
		
		File fil1=new File("D:/repo/sub/file1.txt");
		
		File fil2=new File("D:\\repo\\file2.txt");
		
		paths.add(fil1.getAbsolutePath());
		paths.add(fil2.getAbsolutePath());
		
		paths=(ArrayList<String>) repositoryUtil.getRepoRelativePathwithMulitPaths(paths);
		
		for(String path: paths){
			System.out.println(path);
		}
		
	}
	
	@Test 
	public void testMethods() throws IOException, NoFilepatternException, GitAPIException{
		//1.getWorkDirPrefix
		System.out.println(repositoryUtil.getWorkDirPrefix());
		//2.inHead
		File file=new File(repositoryUtil.getWorkDirPrefix(),"file1.txt");
		FileUtils.createNewFile(file);
		repositoryUtil.appendFileContent(file, "Hello World");
		Collection<String>  rsrcs=repositoryUtil.getRepoRelativePathwithMulitPaths(file.getAbsolutePath());
		new AddToIndexOperation(rsrcs, repository).execute();
		new CommitOperation(repository, AUTHOR, COMMITTER, "Initial Commit").execute();
		
		System.out.println(repositoryUtil.inHead("file1.txt"));
		
		//3.inIndex
		
		System.out.println(repositoryUtil.inIndex(file.getAbsolutePath()));
		
	    //4.lastModifiedInIndex
		System.out.println(repositoryUtil.lastModifiedInIndex(file.getAbsolutePath()));
		
		//5.getDirCacheEntryLength
		System.out.println(repositoryUtil.getDirCacheEntryLength(file.getAbsolutePath()));
		
		//6.isDetachedHead
		System.out.println(repository.getFullBranch());
		System.out.println(RepositoryUtil.isDetachedHead(repository));
		
		//7.parseHeadCommit
		RevCommit commit=RepositoryUtil.parseHeadCommit(repository);
		System.out.println(commit.getName());
		
		//8.getShortBranch
		System.out.println(repositoryUtil.getShortBranch(repository));
		
		//9.mapCommitToRef
		
		System.out.println(repositoryUtil.mapCommitToRef(repository, commit.getName(), false));
	}

	
}
