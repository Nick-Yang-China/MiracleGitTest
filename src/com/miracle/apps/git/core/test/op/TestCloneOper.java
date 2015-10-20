package com.miracle.apps.git.core.test.op;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FileUtils;

import com.miracle.apps.git.core.errors.CoreException;

public class TestCloneOper {
	
	public static void main(String[] args) throws IOException {
		Repository repository = null;
		try {
		File workdir2= new File("D:/Repository2");
				
				if(workdir2.exists()){
					FileUtils.delete(workdir2, FileUtils.RECURSIVE | FileUtils.RETRY);
				}
				FileUtils.mkdir(workdir2,true);
		
		
		URIish uri;
		
			uri = new URIish("https://github.com/Nick-Yang-China/GitDemo.git");

		UsernamePasswordCredentialsProvider crePro=new UsernamePasswordCredentialsProvider("Nick-Yang-China", "!Test0001");

			CloneCommand cloneRepository = Git.cloneRepository();
			cloneRepository.setCredentialsProvider(crePro);
//			if (refName != null)
//				cloneRepository.setBranch(refName);
//			else
//				cloneRepository.setNoCheckout(true);
			cloneRepository.setDirectory(workdir2);
//			cloneRepository.setRemote(remoteName);
			cloneRepository.setURI(uri.toString());
//			cloneRepository.setTimeout(timeout);
			cloneRepository.setCloneAllBranches(true);
			cloneRepository.setCloneSubmodules(true);
//			if (selectedBranches != null) {
//				List<String> branches = new ArrayList<String>();
//				for (Ref branch : selectedBranches)
//					branches.add(branch.getName());
//				cloneRepository.setBranchesToClone(branches);
//			}
			Git git = cloneRepository.call();
			repository = git.getRepository();
		}catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidRemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
