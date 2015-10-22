package com.miracle.apps.git.test.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.FileUtils;
import org.eclipse.jgit.util.SystemReader;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class GitTestCase {
	public final String AUTHOR = "The Author <The.author@some.com>";

	public final String COMMITTER = "The Commiter <The.committer@some.com>";
	
	protected File gitDir;
	protected Repository repository;
	
	@Before
	public void setUp() throws Exception {
    	//
	}

	@After
	public void tearDown() throws Exception {
		
//		System.out.println(gitDir.getParent());
//		
//		if (gitDir.exists())
//			FileUtils.delete(gitDir, FileUtils.RECURSIVE | FileUtils.RETRY);
//		SystemReader.setInstance(null);
	}


}
