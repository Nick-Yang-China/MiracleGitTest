package com.miracle.apps.git.core.test.op;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
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
import com.miracle.apps.git.core.op.ListRemoteOperation;
import com.miracle.apps.git.core.op.RemoveFromIndexOperation;
import com.miracle.apps.git.test.core.GitTestCase;
import com.miracle.apps.git.core.RepositoryUtil;

public class ListRemoteOperationTest extends GitTestCase {
	Repository repository1;
	
	File workdir;
	File workdir2;
	
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
		
		workdir2= new File("D:/Repository2");
		
		if(workdir2.exists()){
			FileUtils.delete(workdir2, FileUtils.RECURSIVE | FileUtils.RETRY);
		}
		FileUtils.mkdir(workdir2,true);
		
		repositoryUtil = new RepositoryUtil(new File(workdir,Constants.DOT_GIT));
		
		repository=repositoryUtil.getRepository();
		
		File file=new File(workdir,"file1.txt");
		FileUtils.createNewFile(file);
		repositoryUtil.appendFileContent(file, "Hello World");
		repositoryUtil.track(file);
		repositoryUtil.commit("Initial Commit");
		
		// let's clone repository to repository1
		
		URIish uri=new URIish("file:///"
				+ repository.getDirectory().toString());
		CloneOperation clop=new CloneOperation(uri.toString(), true, null, 
				workdir2, "refs/heads/master", Constants.DEFAULT_REMOTE_NAME, 0,null,null);
		
		clop.execute();
		
		repository1=new RepositoryUtil(new File(workdir2,Constants.DOT_GIT)).getRepository();
		
		//we create branch of "test" in repository1
		
		RefUpdate createBranch =repository1.updateRef(Constants.R_HEADS+"test");
		createBranch.setNewObjectId(repository1.resolve(Constants.R_HEADS+Constants.MASTER));
		createBranch.update();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		if(repository!=null)
			repository.close();
		
		if(repository1!=null)
			repository1.close();
		
		if (workdir.exists())
			FileUtils.delete(workdir, FileUtils.RECURSIVE | FileUtils.RETRY);
		if (workdir2.exists())
			FileUtils.delete(workdir2, FileUtils.RECURSIVE | FileUtils.RETRY);
		super.tearDown();
	}
	
	/**
	 * List the refs both ways
	 *
	 * @throws Exception
	 */
	@Test
	public void testListRemote() throws Exception {

		URIish uri = new URIish("file:///"
				+ repository1.getDirectory().getPath());
		ListRemoteOperation lrop = new ListRemoteOperation(repository, uri, 0);
		lrop.execute();
		InteratorRemoteRefs(lrop.getRemoteRefs());
		assertEquals(4, lrop.getRemoteRefs().size());
		assertNotNull(lrop.getRemoteRef("refs/heads/test"));
		System.out.println(lrop.toString());
		uri = new URIish("file:///"
				+ repository.getDirectory().getPath());
		lrop = new ListRemoteOperation(repository1, uri, 0);
		lrop.execute();
		InteratorRemoteRefs(lrop.getRemoteRefs());
		assertEquals(2, lrop.getRemoteRefs().size());
		assertNotNull(lrop.getRemoteRef("refs/heads/master"));
		System.out.println(lrop.toString());
	}
	
	/**
	 * Call getRemoteRefs without having run the op
	 *
	 * @throws Exception
	 */
	@Test
	public void testIllegalStateException() throws Exception {

		URIish uri = new URIish("file:///"
				+ repository1.getDirectory().getPath());
		ListRemoteOperation lrop = new ListRemoteOperation(repository
				, uri, 0);
		try {
			lrop.getRemoteRefs();
			fail("Expected Exception not thrown");
		} catch (IllegalStateException e) {
			// expected
		}
	}
	
	/**
	 * Test with illegal URI
	 *
	 * @throws Exception
	 */
	@Test
	public void testIllegalURI() throws Exception {

		URIish uri = new URIish("file:///" + "no/path");
		ListRemoteOperation lrop = new ListRemoteOperation(repository
				, uri, 0);
		try {
			lrop.getRemoteRefs();
			fail("Expected Exception not thrown");
		} catch (IllegalStateException e) {
			// expected
		}
	}
	
	private void InteratorRemoteRefs(Collection<Ref> refs){
		if(refs != null){
			for(Ref ref: refs){
				System.out.println(ref.getName());
			}
			System.out.println("*********************************");
		}
	}
}
