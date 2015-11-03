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
import org.eclipse.jgit.api.PushCommand;
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
import org.eclipse.jgit.transport.RefSpec;
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

public class PushOperationTestWithGitHub extends GitTestCase {
	
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
		
		File file=new File(workdir,"file1.txt");
		FileUtils.createNewFile(file);
		repositoryUtil.appendFileContent(file, "Contect file1");
		Git git=new Git(repository);
		
		git.add().addFilepattern("file1.txt").call();
		git.commit().setMessage("First Commit").call();
		
	}

	@Override
	@After
	public void tearDown() throws Exception {
		repositoryUtil.dispose();
		
		if (workdir.exists())
			FileUtils.delete(workdir, FileUtils.RECURSIVE | FileUtils.RETRY);
		super.tearDown();
	}
	

	
	private org.eclipse.jgit.transport.RemoteRefUpdate.Status getStatus(PushOperationResult operationResult) {
		URIish uri = operationResult.getURIs().iterator().next();
		return operationResult.getPushResult(uri).getRemoteUpdates().iterator()
				.next().getStatus();
	}
	

	
	@Test
	public void testPushWithGitHub() throws Exception {		
		// set up push from repository1 to repository2
		// we cannot re-use the RemoteRefUpdate!!!
		UsernamePasswordCredentialsProvider up=new UsernamePasswordCredentialsProvider("heliangjignjing2011", "ruzhi20141029");
		PushOperationSpecification spec = new PushOperationSpecification();
		// the remote is repo2
		URIish remote = new URIish("https://github.com/heliangjignjing2011/GitTesting.git");
		// update master upon master
		List<RemoteRefUpdate> refUpdates = new ArrayList<RemoteRefUpdate>();
		RemoteRefUpdate update = new RemoteRefUpdate(repository, "refs/heads/master","refs/heads/master", true, null, null);
		refUpdates.add(update);
		spec.addURIRefUpdates(remote, refUpdates);
		// now we can construct the push operation
		PushOperation pop = new PushOperation(repository,spec, false, 30);
		pop.setCredentialsProvider(up);
		pop.execute();
		
		assertEquals(org.eclipse.jgit.transport.RemoteRefUpdate.Status.OK, getStatus(pop.getOperationResult()));
	}
	@Test
	public void testPushWithGitHubWithRefs() throws Exception {
		//setup credentials
		UsernamePasswordCredentialsProvider up=new UsernamePasswordCredentialsProvider("heliangjignjing2011", "ruzhi20141029");
		//Setup RefSpec
		URIish remote = new URIish("https://github.com/heliangjignjing2011/GitTesting.git");
		RefSpec specs=new RefSpec();
		specs=specs.setSourceDestination("refs/heads/master", "refs/heads/master");
		specs=specs.setForceUpdate(true);
		PushOperation pop = new PushOperation(repository, remote.toString(),Arrays.asList(specs), false, 30);
		pop.setCredentialsProvider(up);
		pop.execute();
		assertEquals(org.eclipse.jgit.transport.RemoteRefUpdate.Status.OK, getStatus(pop.getOperationResult()));
	}
	

}
