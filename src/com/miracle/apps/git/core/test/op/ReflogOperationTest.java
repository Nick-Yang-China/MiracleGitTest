package com.miracle.apps.git.core.test.op;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ReflogEntry;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.miracle.apps.git.core.op.CommitOperation;
import com.miracle.apps.git.core.op.CreateLocalBranchOperation;
import com.miracle.apps.git.core.op.ReflogOperation;
import com.miracle.apps.git.test.core.GitTestCase;
import com.miracle.apps.git.core.RepositoryUtil;

public class ReflogOperationTest extends GitTestCase {
	
	File workdir;
	
	RepositoryUtil repositoryUtil;
	
	RevCommit initialCommit;
	
	File file;
	
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
		
	}

	@Override
	@After
	public void tearDown() throws Exception {
		
		repositoryUtil.dispose();
		
//		if (workdir.exists())
//			FileUtils.delete(workdir, FileUtils.RECURSIVE | FileUtils.RETRY);
		super.tearDown();
	}
	
	@Test
	public void testHeadReflog() throws Exception {
		setupRepository();
		
		//create branch of dev01 and check out
		CreateLocalBranchOperation clbo=new CreateLocalBranchOperation(repository, "dev01", repository.getRef(Constants.MASTER), null);
		clbo.setCheckOutFlag(true);
		clbo.execute();
		//delete the file in branch of dev01
		Git.wrap(repository).rm().addFilepattern("dummy.txt").setCached(false).call();
		//commit the info
		new CommitOperation(repository, AUTHOR, COMMITTER, "Removed file").execute();
		
		ReflogOperation ro=new ReflogOperation(repository);
		ro.execute();
		Collection<ReflogEntry> re=ro.getReflogResults();
		System.out.println(re.size());
		for(Iterator<ReflogEntry> it=re.iterator();it.hasNext();){
			ReflogEntry log=it.next();
			System.out.print(log.getComment()+"--->");
			System.out.print(log.getNewId()+"--->");
			System.out.println(log.getOldId());
			System.out.println("************************************************");
		}
	}
	
	
	@Test
	public void testHeadReflogWithRef() throws Exception {
		setupRepository();
		
		//create branch of dev01 and check out
		CreateLocalBranchOperation clbo=new CreateLocalBranchOperation(repository, "dev01", repository.getRef(Constants.MASTER), null);
		clbo.setCheckOutFlag(true);
		clbo.execute();
		//delete the file in branch of dev01
		Git.wrap(repository).rm().addFilepattern("dummy.txt").call();
		//commit the info
		new CommitOperation(repository, AUTHOR, COMMITTER, "Removed file").execute();
		
		ReflogOperation ro=new ReflogOperation(repository, "master");
		ro.execute();
		Collection<ReflogEntry> re=ro.getReflogResults();
		System.out.println(re.size());
		for(Iterator<ReflogEntry> it=re.iterator();it.hasNext();){
			ReflogEntry log=it.next();
			System.out.print(log.getComment()+"--->");
			System.out.print(log.getNewId()+"--->");
			System.out.println(log.getOldId());
			System.out.println("************************************************");
		}
	}
	
	private void setupRepository() throws Exception {
		// create first commit containing a dummy file
			// create first commit containing a dummy file
		file=new File(workdir, "dummy.txt");
		FileUtils.createNewFile(file);
		repositoryUtil.track(file);
		repositoryUtil.commit("first commit");
		
		//update the file content and commit
		
		repositoryUtil.appendFileContent(file, "test a");
		repositoryUtil.track(file);
		repositoryUtil.commit("second commit");
		
		//update the file content and commit again
		
		repositoryUtil.appendFileContent(file, "test b");
		repositoryUtil.track(file);
		initialCommit=repositoryUtil.commit("third commit");
	}

}
