package com.miracle.apps.git.test.core;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.miracle.apps.git.core.RepositoryUtil;

public class RepositoryUtilTest {
	RepositoryUtil repositoryUtil;
	
	Repository repository;
	@Before
	public void setUp() throws Exception {
		repositoryUtil=new RepositoryUtil("D://repo");
		
		repository=repositoryUtil.getRepository();
	}

	@After
	public void tearDown() throws Exception {
		if(repositoryUtil.getGitDir().getParentFile().exists()){
			FileUtils.delete(repositoryUtil.getGitDir().getParentFile(), FileUtils.RECURSIVE | FileUtils.RETRY);
		}
		repositoryUtil.dispose();
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

}
