package com.miracle.apps.git.core.test.op;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.CredentialsProviderUserInfo;
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
import com.miracle.apps.git.core.op.PushOperation;
import com.miracle.apps.git.core.op.PushOperationResult;
import com.miracle.apps.git.core.op.PushOperationSpecification;
import com.miracle.apps.git.core.op.RemoveFromIndexOperation;
import com.miracle.apps.git.test.core.GitTestCase;
import com.miracle.apps.git.core.RepositoryUtil;

public class PushOperationTest extends GitTestCase {
	private static final String INVALID_URI = "invalid-uri";
	
	Repository repository1;
	Repository repository2;
	
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
		
		repository1=repositoryUtil.getRepository();
		
		File file=new File(workdir,"file1.txt");
		FileUtils.createNewFile(file);
		repositoryUtil.appendFileContent(file, "Contect file1");
		Git git=new Git(repository1);
		
		git.add().addFilepattern("file1.txt").call();
		git.commit().setMessage("First Commit").call();
		
		URIish uri=new URIish("file:///" + repository1.getDirectory().toString());
		
		CloneOperation clop=new CloneOperation(uri.toString(), true, null, workdir2, "refs/heads/master", "origin", 0,null,null);
		clop.execute();
		
		repository2=new FileRepository(new File(workdir2,Constants.DOT_GIT));
		
		RefUpdate createBranch=repository2.updateRef("refs/heads/test");
		createBranch.setNewObjectId(repository2.resolve("refs/heads/master"));
		createBranch.update();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		if(repository1!=null)
			repository1.close();
		if(repository2!=null)
			repository2.close();
		
		if (workdir.exists())
			FileUtils.delete(workdir, FileUtils.RECURSIVE | FileUtils.RETRY);
		if (workdir2.exists())
			FileUtils.delete(workdir2, FileUtils.RECURSIVE | FileUtils.RETRY);
		super.tearDown();
	}
	
	private PushOperation createPushOperation() throws Exception {
		// set up push from repository1 to repository2
		// we cannot re-use the RemoteRefUpdate!!!
		PushOperationSpecification spec = new PushOperationSpecification();
		// the remote is repo2
		URIish remote = new URIish("file:///"
				+ repository2.getDirectory().toString());
		// update master upon master
		List<RemoteRefUpdate> refUpdates = new ArrayList<RemoteRefUpdate>();
		RemoteRefUpdate update = new RemoteRefUpdate(repository1, "HEAD", "refs/heads/test", false, null, null);
		refUpdates.add(update);
		spec.addURIRefUpdates(remote, refUpdates);
		// now we can construct the push operation
		PushOperation pop = new PushOperation(repository1,
				spec, false, 0);
		return pop;
	}
	
	private org.eclipse.jgit.transport.RemoteRefUpdate.Status getStatus(PushOperationResult operationResult) {
		URIish uri = operationResult.getURIs().iterator().next();
		return operationResult.getPushResult(uri).getRemoteUpdates().iterator()
				.next().getStatus();
	}
	
	/**
	 * Push from repository1 "master" into "test" of repository2.
	 *
	 * @throws Exception
	 */
	@Test
	public void testPush() throws Exception {
		// push from repository1 to repository2
		System.out.println(repository2.getBranch());
		PushOperation pop=createPushOperation();
		pop.execute();
		assertEquals(org.eclipse.jgit.transport.RemoteRefUpdate.Status.UP_TO_DATE, getStatus(pop.getOperationResult()));
		
		ArrayList<String> files=new ArrayList<String>();
		
		File file=new File(workdir,"file2.txt");
		FileUtils.createNewFile(file);
		repositoryUtil.appendFileContent(file, "new file");
		files.add(repositoryUtil.getRepoRelativePath(file.getAbsolutePath()));
		
		AddToIndexOperation trop=new AddToIndexOperation(files, repository1);
		trop.execute();
		
		CommitOperation cop =new CommitOperation(repository1, files, files, AUTHOR, COMMITTER, "added files");
		cop.execute();
		
		pop=createPushOperation();
		pop.execute();
		assertEquals(org.eclipse.jgit.transport.RemoteRefUpdate.Status.OK, getStatus(pop.getOperationResult()));
		
		try {
			// assert that we cannot run this again
			pop.execute();
			fail("Expected Exception not thrown");
		} catch (IllegalStateException e) {
			// expected
		}
		
		pop = createPushOperation();
		pop.execute();
		assertEquals(org.eclipse.jgit.transport.RemoteRefUpdate.Status.UP_TO_DATE, getStatus(pop.getOperationResult()));
		
		File testFile = new File(workdir2,repositoryUtil.getRepoRelativePath(file.getAbsolutePath()));
		assertFalse(testFile.exists());
		testFile = new File(workdir, repositoryUtil.getRepoRelativePath(file.getAbsolutePath()));
		assertTrue(testFile.exists());
		
		// check out test and verify the file is there
		new Git(repository2).checkout().setName("refs/heads/test").call();
		testFile = new File(workdir2, repositoryUtil.getRepoRelativePath(file.getAbsolutePath()));
		assertTrue(testFile.exists());
	}
	
	@Test
	public void testPushWithGitHub() throws Exception {		
		// set up push from repository1 to repository2
		// we cannot re-use the RemoteRefUpdate!!!
//		UsernamePasswordCredentialsProvider up=new UsernamePasswordCredentialsProvider("heliangjignjing2011", "ruzhi20141029");
		PushOperationSpecification spec = new PushOperationSpecification();
		// the remote is repo2
		URIish remote = new URIish("https://github.com/heliangjignjing2011/GitTesting.git");
		// update master upon master
		List<RemoteRefUpdate> refUpdates = new ArrayList<RemoteRefUpdate>();
		RemoteRefUpdate update = new RemoteRefUpdate(repository1, "refs/heads/master","refs/heads/master", true, null, null);
		refUpdates.add(update);
		spec.addURIRefUpdates(remote, refUpdates);
		// now we can construct the push operation
		PushOperation pop = new PushOperation(repository1,spec, false, 30);
		pop.setCredentialsProvider("heliangjignjing2011", "ruzhi20141029");
		pop.execute();
	}
	
	private PushOperation createInvalidPushOperation() throws Exception {
		// set up push with invalid URI to provoke an exception
		PushOperationSpecification spec = new PushOperationSpecification();
		// the remote is invalid
		URIish remote = new URIish(INVALID_URI);
		// update master upon master
		Repository local = repository1;
		RemoteRefUpdate update = new RemoteRefUpdate(local, "HEAD", "refs/heads/test",
				false, null, null);
		spec.addURIRefUpdates(remote, Collections.singletonList(update));
		// now we can construct the push operation
		PushOperation pop = new PushOperation(local, spec, false, 0);
		return pop;
	}
//	@Test
//	public void testInvalidUriDuringPush() throws Exception {
//
//		PushOperation pop = createInvalidPushOperation();
//		pop.execute();
//		PushOperationResult result = pop.getOperationResult();
//		String errorMessage = result.getErrorMessage(new URIish(INVALID_URI));
//		
//		System.out.println(errorMessage);
//		
//		assertNotNull(errorMessage);
//		assertTrue(errorMessage.contains(INVALID_URI));
//	}
	
	@Test
	public void testIllegalStateExceptionOnGetResultWithoutRun()
			throws Exception {
		// push from repository1 to repository2
		PushOperation pop = createPushOperation();
		try {
			pop.getOperationResult();
			fail("Expected Exception not thrown");
		} catch (IllegalStateException e) {
			// expected
		}
	}
	
	@Test
	public void testUpdateTrackingBranchIfSpecifiedInRemoteRefUpdate() throws Exception {
		// Commit on repository 2
//		RevCommit commit = repository2.addAndCommit(project, new File(workdir2, "test.txt"), "Commit in repository 2");
		System.out.println(repository2.getBranch());
		new Git(repository2).checkout().setName("refs/heads/test").call();
		System.out.println(repository2.getBranch());
		ArrayList<String> files=new ArrayList<String>();
		File file=new File(workdir2,"test.txt");
		FileUtils.createNewFile(file);
		repositoryUtil.appendFileContent(file, "create file of test.txt in repository 2");
		files.add(repositoryUtil.getRepoRelativePath(file.getAbsolutePath()));
		
		AddToIndexOperation trop=new AddToIndexOperation(files, repository2);
		trop.execute();
		
		CommitOperation cop =new CommitOperation(repository2, files, files, AUTHOR, COMMITTER, "Commit in repository 2");
		cop.execute();
		// We want to push from repository 2 to 1 (because repository 2 already
		// has tracking set up)
//		URIish remote = repository1.getUri();
		URIish remote=new URIish("file:///" + repository1.getDirectory().toString());
		
		String trackingRef = "refs/remotes/origin/master";
		RemoteRefUpdate update = new RemoteRefUpdate(
				repository2, "HEAD", "refs/heads/master", false,
				trackingRef, null);
		PushOperationSpecification spec = new PushOperationSpecification();
		spec.addURIRefUpdates(remote, Arrays.asList(update));

		PushOperation push = new PushOperation(repository2,
				spec, false, 0);
		push.execute();

		PushOperationResult result = push.getOperationResult();
		PushResult pushResult = result.getPushResult(remote);
		TrackingRefUpdate trf=pushResult.getTrackingRefUpdate(trackingRef);
		System.out.println(trf.getLocalName());
		System.out.println(trf.getRemoteName());
		assertNotNull("Expected result to have tracking ref update", pushResult.getTrackingRefUpdate(trackingRef));

		ObjectId trackingId = repository2.resolve(trackingRef);
		assertEquals("Expected tracking branch to be updated", cop.getCommit().getId(), trackingId);
		new Git(repository1).checkout().setName("refs/heads/master").call();
		File testFile = new File(workdir2, repositoryUtil.getRepoRelativePath(file.getAbsolutePath()));
		assertTrue(testFile.exists());
	}
}
