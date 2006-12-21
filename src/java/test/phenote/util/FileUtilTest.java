package phenote.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.io.File;
import java.io.IOException;

/**
 * Unit test class for FileUtil.
 */
public class FileUtilTest extends TestCase {

  private static final File testArchiveDir = new File("src" + FileUtil.FILE_SEPARATOR +
                                                      "java" + FileUtil.FILE_SEPARATOR +
                                                      "test", "test-archive-dir");
  private static final File testLoadDirectory = new File("src" + FileUtil.FILE_SEPARATOR +
                                                      "java" + FileUtil.FILE_SEPARATOR +
                                                      "test", "test-load-dir");
  private File testLoadFile;
  private File testPurgeFile;

  public static void main(String args[]) {
    TestRunner.run(suite());
  }

  public static Test suite() {
    return new TestSuite(FileUtilTest.class);
  }

  protected void setUp() {
    setTestDirectories();
  }

  protected void tearDown() {
    cleanupTestFilesStructure();
  }

  /**
   * Create a single file and archive it. Make sure it moved into the archive directory.
   */
  public void testArchiveFile() {
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
  public void testPurgeArchiveFile() {
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

  public void testPureFilename(){
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
    for (File file : files) {
      file.delete();
    }
    testArchiveDir.delete();
  }

}
