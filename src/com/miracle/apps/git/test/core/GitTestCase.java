package com.miracle.apps.git.test.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.eclipse.jgit.util.FileUtils;
import org.eclipse.jgit.util.SystemReader;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class GitTestCase {

//	protected final TestUtils testUtils = new TestUtils();
//
//	protected TestProject project;

	protected File gitDir;

//	@BeforeClass
//	public static void setUpClass() {
//		// suppress auto-ignoring and auto-sharing to avoid interference
//		IEclipsePreferences p = InstanceScope.INSTANCE.getNode(Activator
//				.getPluginId());
//		p.putBoolean(GitCorePreferences.core_autoIgnoreDerivedResources, false);
//		p.putBoolean(GitCorePreferences.core_autoShareProjects, false);
//	}

	@Before
	public void setUp() throws Exception {
		// ensure there are no shared Repository instances left
		// when starting a new test
//		Activator.getDefault().getRepositoryCache().clear();
//		MockSystemReader mockSystemReader = new MockSystemReader();
//		SystemReader.setInstance(mockSystemReader);
//		mockSystemReader.setProperty(Constants.GIT_CEILING_DIRECTORIES_KEY,
//				ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile()
//						.getParentFile().getAbsoluteFile().toString());
//		project = new TestProject(true);
//		gitDir = new File(project.getProject().getWorkspace().getRoot()
//				.getRawLocation().toFile(), Constants.DOT_GIT);
//		if (gitDir.exists())
//			FileUtils.delete(gitDir, FileUtils.RECURSIVE | FileUtils.RETRY);
	}

	@After
	public void tearDown() throws Exception {
//		project.dispose();
//		Activator.getDefault().getRepositoryCache().clear();
		if (gitDir.exists())
			FileUtils.delete(gitDir, FileUtils.RECURSIVE | FileUtils.RETRY);
		SystemReader.setInstance(null);
	}

//	protected ObjectId createFile(Repository repository, IProject actProject, String name, String content) throws IOException {
//		File file = new File(actProject.getProject().getLocation().toFile(), name);
//		Writer fileWriter = new OutputStreamWriter(new FileOutputStream(
//				file), "UTF-8");
//		fileWriter.write(content);
//		fileWriter.close();
//		byte[] fileContents = IO.readFully(file);
//		try (ObjectInserter inserter = repository.newObjectInserter()) {
//			ObjectId objectId = inserter.insert(Constants.OBJ_BLOB, fileContents);
//			inserter.flush();
//			return objectId;
//		}
//	}

//	protected ObjectId createFileCorruptShort(Repository repository,
//			IProject actProject, String name, String content)
//			throws IOException {
//		ObjectId id = createFile(repository, actProject, name, content);
//		File file = new File(repository.getDirectory(), "objects/"
//				+ id.name().substring(0, 2) + "/" + id.name().substring(2));
//		byte[] readFully = IO.readFully(file);
//		FileUtils.delete(file);
//		FileOutputStream fileOutputStream = new FileOutputStream(file);
//		try {
//			byte[] truncatedData = new byte[readFully.length - 1];
//			System.arraycopy(readFully, 0, truncatedData, 0,
//					truncatedData.length);
//			fileOutputStream.write(truncatedData);
//		} finally {
//			fileOutputStream.close();
//		}
//		return id;
//	}
}
