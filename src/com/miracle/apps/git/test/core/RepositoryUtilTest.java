package com.miracle.apps.git.test.core;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectDatabase;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.WorkingTreeOptions;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
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
	
	File workdir;
	
	RevCommit initialCommit;
	
	File projectFile;

	File untrackedFile;

	File fileInIndex;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		workdir= new File("D://Repository2");
		
		if(workdir.exists()){
			FileUtils.delete(workdir, FileUtils.RECURSIVE | FileUtils.RETRY);
		}
		FileUtils.mkdir(workdir,true);
		
		repositoryUtil = new RepositoryUtil(new File(workdir,Constants.DOT_GIT));
		
		repository=repositoryUtil.getRepository();
//		repositoryUtil=new RepositoryUtil("D://repo");
//		
//		repository=repositoryUtil.getRepository();
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
		System.out.println(repositoryUtil.getShortBranch());
		
		//9.mapCommitToRef
		
		System.out.println(repositoryUtil.mapCommitToRef(commit.getName(), false));
	}
	
	@Test
	public void testGetContentSources() throws Exception{
		// create first commit containing a dummy file
		File file=new File(workdir, "dummy.txt");
		FileUtils.createNewFile(file);
		repositoryUtil.appendFileContent(file, "dummy");
		repositoryUtil.track(file);
		initialCommit=repositoryUtil.commit("dummy commit");
		
		//create second commit modify file dummy.txt and create a new file first.txt
		repositoryUtil.appendFileContent(file, "\nmodify");
		repositoryUtil.track(file);
		File first=new File(workdir, "first.txt");
		FileUtils.createNewFile(first);
		repositoryUtil.appendFileContent(first, "first");
		repositoryUtil.track(first);
		RevCommit firstCommit=repositoryUtil.commit("first commit");
		
		//create third commit modify file dummy.txt and create a new file second.txt
		repositoryUtil.appendFileContent(file, "\nagain");
		repositoryUtil.track(file);
		File second=new File(workdir, "second.txt");
		FileUtils.createNewFile(second);
		repositoryUtil.appendFileContent(second, "second");
		repositoryUtil.track(second);
		RevCommit secondCommit=repositoryUtil.commit("second commit");
		
		TreeWalk tw=TreeWalk.forPath(repository, "first.txt",firstCommit.getTree());
		
		ObjectId blobId=tw.getObjectId(0);
		
		final InputStream objectInputStream = repository.open(blobId,
				Constants.OBJ_BLOB).openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(objectInputStream)); 
		String line;
		while((line=reader.readLine())!=null){
			System.out.println(line);
		}
	}
	
	@Test
	public void testSplitFile() throws Exception{
		File file =new File("D:\\zdt-public-service.json");
		Map<String,String> map=repositoryUtil.getConflictFileContentWithSplit(file);
		for(Entry<String, String> ent:map.entrySet()){
			System.out.println(ent.getKey()+"----->"+ent.getValue());
		}
	}

	
}