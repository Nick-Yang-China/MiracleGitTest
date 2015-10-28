package com.miracle.apps.git.test.core;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.util.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.miracle.apps.git.core.RepositoryUtil;

public class RepositoryConfigTest {
	RepositoryUtil repositoryUtil;
	
	Repository repository;
	@Before
	public void setUp() throws Exception {
		repositoryUtil=new RepositoryUtil("D://Repository1");
		
		repository=repositoryUtil.getRepository();
	}

	@After
	public void tearDown() throws Exception {
		repositoryUtil.dispose();
		repositoryUtil.removeLocalRepository(repository);
	}

	@Test
	public void getRepoConfigInfo() {
		StoredConfig sc=repository.getConfig();
		Set<String> strs=sc.getSections();
		for(String str:strs){
			System.out.println(str);
		}
		strs=sc.getNames(ConfigConstants.CONFIG_CORE_SECTION, null);
		for(String str:strs){
			System.out.println(str);
		}
		strs=sc.getNames("user",true);
		for(String str:strs){
			System.out.println(str);
		}
		
		System.out.println(sc.getBoolean("core", "filemode", true));
	}

}
