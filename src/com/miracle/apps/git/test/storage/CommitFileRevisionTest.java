package com.miracle.apps.git.test.storage;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

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
import com.miracle.apps.git.core.storage.CommitBlobStorage;
import com.miracle.apps.git.core.storage.CommitFileRevision;
import com.miracle.apps.git.test.core.GitTestCase;

public class CommitFileRevisionTest extends GitTestCase {
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
		workdir= new File("D://Repository1");
		
		if(workdir.exists()){
			FileUtils.delete(workdir, FileUtils.RECURSIVE | FileUtils.RETRY);
		}
		FileUtils.mkdir(workdir,true);
		
		repositoryUtil = new RepositoryUtil(new File(workdir,Constants.DOT_GIT));
		
		repository=repositoryUtil.getRepository();
	}

	@After
	public void tearDown() throws Exception {
		repositoryUtil.dispose();
		repositoryUtil.removeLocalRepository(repository);
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
		
		repositoryUtil.mapCommitIdToRevCommit(firstCommit.getName());
		
		CommitFileRevision cfr=new CommitFileRevision(repository, repositoryUtil.mapCommitIdToRevCommit(firstCommit.getName()), "first.txt");
		
		CommitBlobStorage cbs=cfr.getStorage();
		
		final InputStream objectInputStream = cbs.getContents();
		BufferedReader reader = new BufferedReader(new InputStreamReader(objectInputStream)); 
		String line;
		while((line=reader.readLine())!=null){
			System.out.println(line);
		}
	}
	

	
}