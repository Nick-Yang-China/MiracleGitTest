package com.miracle.apps.git.core.test.op;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.eclipse.jgit.lib.TagBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.FileUtils;
import org.eclipse.jgit.util.RawParseUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.miracle.apps.git.core.op.AddToIndexOperation;
import com.miracle.apps.git.core.op.BranchOperation;
import com.miracle.apps.git.core.op.CloneOperation;
import com.miracle.apps.git.core.op.CommitOperation;
import com.miracle.apps.git.core.op.CreateLocalBranchOperation;
import com.miracle.apps.git.core.op.CreateLocalBranchOperation.UpstreamConfig;
import com.miracle.apps.git.core.op.PushOperation;
import com.miracle.apps.git.core.op.PushOperationResult;
import com.miracle.apps.git.core.op.PushOperationSpecification;
import com.miracle.apps.git.core.op.RemoveFromIndexOperation;
import com.miracle.apps.git.core.op.TagOperation;
import com.miracle.apps.git.test.core.GitTestCase;
import com.miracle.apps.git.core.RepositoryUtil;
import com.miracle.apps.git.core.errors.CoreException;

public class TagOperationTest extends GitTestCase {
	
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
		
		File file=new File(workdir, "sub/a.txt");
		file.getParentFile().mkdirs();
		FileUtils.createNewFile(file);
		repositoryUtil.appendFileContent(file, "hello world", true);
		repositoryUtil.track(file);
		repositoryUtil.commit("Initial commit");
		
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
	public void testTagOperation() throws Exception {
		assertTrue("Tags should be empty", repository.getTags().isEmpty());
		
		TagBuilder newTag=new TagBuilder();
		newTag.setTag("TheNewTag");
		newTag.setMessage("Well,I'm the tag");
		newTag.setTagger(RawParseUtils.parsePersonIdent(AUTHOR));
		newTag.setObjectId(repository.resolve("refs/heads/master"), Constants.OBJ_COMMIT);
		TagOperation top=new TagOperation(repository, newTag, false);
		top.execute();
		assertFalse("Tags should not be empty", repository.getTags().isEmpty());
		System.out.println(top.toString());
		try {
			top.execute();
			fail("Expected Exception not thrown");
		} catch (CoreException e) {
			// expected
		}
		
		top=new TagOperation(repository, newTag, true);
		
		try {
			top.execute();
			fail("Expected Exception not thrown");
		} catch (CoreException e) {
			// expected
		}
		
		Ref tagRef=repository.getTags().get("TheNewTag");
		
		try(RevWalk walk= new RevWalk(repository)){
			RevTag tag=walk.parseTag(repository.resolve(tagRef.getName()));
			
			newTag.setMessage("another message");
			
			assertFalse("Message should differ",tag.getFullMessage().equals(newTag.getMessage()));
			
			top.execute();
			
			tag=walk.parseTag(repository.resolve(tagRef.getName()));
			
			assertTrue("Message be same", tag.getFullMessage().equals(newTag.getMessage()));
			
			walk.dispose();
		}
	}
	
}
