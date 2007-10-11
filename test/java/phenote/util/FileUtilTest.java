package phenote.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;


import java.io.File;
import java.io.IOException;

/**
 * Unit test class for FileUtil.
 */
public class FileUtilTest {

  private static final File testArchiveDir = new File("test" + FileUtil.FILE_SEPARATOR + FileUtil.FILE_SEPARATOR +
          "testfiles", "test-archive-dir");
  private static final File testLoadDirectory = new File("test" + FileUtil.FILE_SEPARATOR + FileUtil.FILE_SEPARATOR +
          "testfiles", "test-load-dir");
  private File testLoadFile;
  private File testPurgeFile;

  @Before
  public void setUp() {
    setTestDirectories();
  }

  @After
  public void tearDown() {
    cleanupTestFilesStructure();
  }

  /**
   * Create a single file and archive it. Make sure it moved into the archive directory.
   */
  @Test
  public void archiveFile() {
    File archivedFile = FileUtil.archiveFile(testLoadFile, testArchiveDir);
    File[] files = testArchiveDir.listFiles();
    assertEquals("Number of files", 1, files.length);
    assertEquals("File Name", archivedFile.getName(), files[0].getName());
    assertTrue("Original File still exists", testLoadFile.exists());
  }

  /**
   * Create a single file and archive it. Make sure it moved into the archive directory.
   * Sleep for a second and then archive another file and then purge the first file while the
   * second file does not get purged.
   */
  @Test
  public void purgeArchiveFile() {
    File archivedFile = FileUtil.archiveFile(testLoadFile, testArchiveDir);
    File[] files = testArchiveDir.listFiles();
    assertEquals("Number of files", 1, files.length);
    assertEquals("File Name", archivedFile.getName(), files[0].getName());
    assertTrue("Original File still exists", testLoadFile.exists());

    // create the second file  and wait a second before archiving it.
    testPurgeFile = new File(testLoadDirectory, "test-file-two.txt");
    try {
      Thread.sleep(1000);
      testPurgeFile.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }

    archivedFile = FileUtil.archiveFile(testPurgeFile, testArchiveDir);
    files = testArchiveDir.listFiles();
    assertEquals("Number of files", 2, files.length);
    FileUtil.purgeArchiveDirectory(testArchiveDir, 990);

    files = testArchiveDir.listFiles();
    assertEquals("Number of files", 1, files.length);
    assertEquals("File Name", archivedFile.getName(), files[0].getName());

  }

  @Test
  public void pureFilename() {
    String fileNameOnly = "filenameAlpha.txt";
    String filename = FileUtil.FILE_SEPARATOR + "dire" + FileUtil.FILE_SEPARATOR + fileNameOnly;
    String pureFilename = FileUtil.getPureFileName(filename);
    assertEquals("Pure File Name", fileNameOnly, pureFilename);

    fileNameOnly = "";
    pureFilename = FileUtil.getPureFileName(fileNameOnly);
    assertEquals("Pure File Name", fileNameOnly, pureFilename);
  }


  /**
   * Create the test directories before the tests are run.
   */
  private void setTestDirectories() {
    testLoadDirectory.mkdir();

    testLoadFile = new File(testLoadDirectory, "test-file-one.txt");
    testPurgeFile = new File(testLoadDirectory, "test-file-two.txt");
    try {
      testLoadFile.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Remove test directories after the tests are done.
   */
  private void cleanupTestFilesStructure() {
    testLoadFile.delete();
    testPurgeFile.delete();
    testLoadDirectory.delete();
    File[] files = testArchiveDir.listFiles();
    if (files != null)
      for (File file : files) {
        file.delete();
      }
    testArchiveDir.delete();
  }

}
