package com.github.m5c.livepoll.persistence;

import com.github.m5c.livepoll.pollutils.DateAndTopicIdGenerator;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class FileSystemInitializerTest {


  public static void runFileSystemInitializer(String testBaseDir) throws IOException {

    // Run initializer, to ensure needed file system strucutre is present
    FileSystemInitializer initializer =
        new FileSystemInitializer(new DateAndTopicIdGenerator(), new PackPersistence(testBaseDir),
            testBaseDir);
    initializer.ensureBaseDirIsReady();
  }

  @Test
  public void testStructureCreationInRam() throws IOException {

    // Set up mock basedir location
    String testBaseDir = System.getProperty("java.io.tmpdir").toString() + "/.dummybasedir";
    File testBaseDirFile = new File(testBaseDir);

    runFileSystemInitializer(testBaseDir);

    // Verify the created structure is sane
    Assert.assertTrue("Initilizer did not create required basedir.", testBaseDirFile.exists());
    Assert.assertTrue("Basedir created by initilizer is not a directory.",
        FileUtils.isDirectory(testBaseDirFile));

    // Erase the created test structure, so this does not interfere woth other / later tests.
    FileUtils.deleteDirectory(testBaseDirFile);
  }
}