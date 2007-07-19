package phenote.util;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class FileUtil {

  public static final String FILE_SEPARATOR = System.getProperty("file.separator");
  public static final String DASH = "-";
  public static final String UNDERSCORE = "_";
  public static final String DOT = ".";

  private static final Logger LOG =  Logger.getLogger(FileUtil.class);

  /** if ~/.phenote doesnt exist yet its created */
  public static File getDotPhenoteDir() {
    String home = System.getProperty("user.home");
    File dotPhenote = new File(sep(home,".phenote"));
    if (!dotPhenote.exists()) {
      LOG.info("creating "+dotPhenote+" directory");
      dotPhenote.mkdir();
    }
    return dotPhenote;
  }

  public static File getDotPhenoteOboDir() {
    return getDotPhenoteSubDir("obo-files");
  }

  public static File getDotPhenoteConfDir() {
    return getDotPhenoteSubDir("conf");
  }

  private static File getDotPhenoteSubDir(String subdirString) {
    File d = getDotPhenoteDir();
    File subdir = new File(d,subdirString);
    if (!subdir.exists()) {
      LOG.info("creating "+subdir+" directory");
      subdir.mkdir();
    }
    return subdir;
  }


  /** get actual name of file sans path */
  public static String getNameOfFile(URL urlFile) {
    return getNameOfFile(urlFile.getPath());
//     int lastSlashIndex = path.lastIndexOf('/');
//     if (lastSlashIndex == -1) return path;
//     return path.substring(lastSlashIndex);
  }

  public static String getNameOfFile(String path) {
    int lastSlashIndex = path.lastIndexOf('/');
    if (lastSlashIndex == -1) return path;
    return path.substring(lastSlashIndex);
  }

  private static String getDotPhenoteString() {
    return getDotPhenoteDir().getPath();
  }
  
  private static String sep(String a, String b) { return a + FILE_SEPARATOR + b; }


  /** split into findMaster?ConfigUrl & findOboUrl!!! */
  public static URL findUrl(String filename) throws FileNotFoundException {
    if (filename == null) {
      String m = "cant find null file";
      LOG.error(m); System.out.println(m);
      throw new FileNotFoundException(m);
    }
    List<URL> possibleUrls = getPossibleUrls(filename);
    for (URL u : possibleUrls) {
      //System.out.println(u+" url exists "+urlExists(u));
      if (urlExists(u)) return u;
    }
    System.out.println("Failed to find file "+filename);
    //LOG.error("Failed to find file "+filename);
    throw new FileNotFoundException(filename+" not found");
  }

  // this is muddling config and obo - probably should be 2 methods? or be smart about
  // suffix - or who cares?
  // ok this is even sillier as it will look in .phenote for obo files and not for 
  // conf files - which is what the app wants but really there needs to be 2 different
  // methods - this is sheer laziness! also with separate methods can print out
  // better error message
  // split into getPossibleMaster?ConfigUrls & getPossibleOboUrls
  private static List<URL> getPossibleUrls(String filename) {
    List<URL> urls = new ArrayList(5);
    if (filename == null) {
      System.out.println("cant find null file");
      LOG.error("cant find null file");
      return urls; // ?? ex?
    }
    // hmmm - should full path go last? can be problem with running from
    // jar as config files are in root(fix), obo files finally given dir
    addFile(filename,urls); // full path or relative to pwd
    addFile("conf/"+filename,urls);
    addFile("images/"+filename,urls);
    // ~/.phenote/obo-files cache for obo files - overrides phenote obo-files
    // eventually may have configured obo dir as well...
    addFile(getDotPhenoteOboDir().getPath()+"/"+filename,urls);
    // addFile(getDotPhenoteConfDir().getPath() ???
    addFile("obo-files/"+filename,urls);//this is obo-files specific - separate method?
//     URL jarUrl = FileUtil.class.getResource(filename);
//     if (jarUrl != null) urls.add(jarUrl);
//     jarUrl = FileUtil.class.getResource("/"+filename);
//     if (jarUrl != null) urls.add(jarUrl);
    return urls;
  }

  // make an inner class for this?
  private static void addFile(String filename,List<URL> urls) {
    if (filename == null) {
      System.out.println("cant find null file");
      LOG.error("cant find null file");
      return;
    }
    try {
      URL u = new File(filename).toURL();
      if (u != null) urls.add(u);

      URL jarUrl = FileUtil.class.getResource(filename);
      if (jarUrl != null) urls.add(jarUrl);
      jarUrl = FileUtil.class.getResource("/"+filename);
      if (jarUrl != null) urls.add(jarUrl);
    } catch (MalformedURLException e) {
      //System.out.println("bad file url "+e);
      LOG.error("bad file url "+e);
    }
  }
    


  private static boolean urlExists(URL u) {
    try { u.openStream(); }
    catch (IOException e) { return false; }
    return true;
  }

  /**
   * This method moves (renames) a given file into an archive directory.
   * The file name is changed to include a time stamp.
   *
   * @param file
   * @param archiveFolderPath
   * @param archiveFolderName
   */
  public static File archiveFile(File file, String archiveFolderPath, String archiveFolderName) {
    File dir = new File(archiveFolderPath, archiveFolderName);
    return archiveFile(file, dir);
  }

  /**
   * This method moves (renames) a given file into an archive directory.
   * The file name is changed to include a time stamp:
   *  original file name: <file_name.txt>
   *  archived file name: <file_name-yyyy-MM-dd_HH_MI_ss.txt>
   *
   * The archive file is returned.
   * The original file is removed.
   * The archive directory is created if it does not exist.
   *
   * @param file             file that should be archived
   * @param archiveDirectory
   * @return File archive file
   */
  public static File archiveFile(File file, File archiveDirectory) {
    // Create the directory if it does not exist yet.
    if (!archiveDirectory.exists()) {
      boolean success = archiveDirectory.mkdirs();
      if (!success)
        LOG.error("Error while creating the archive directory '" + archiveDirectory.getAbsolutePath());
    }

    GregorianCalendar cal = new GregorianCalendar();
    String fileName = file.getName();
    int indexOfLastDot = fileName.lastIndexOf(DOT);
    String extension = fileName.substring(indexOfLastDot + 1);

    StringBuilder archiveFileName = new StringBuilder(file.getName().substring(0, indexOfLastDot));
    archiveFileName.append(DASH);
    archiveFileName.append(cal.get(Calendar.YEAR));
    archiveFileName.append(DASH);
    archiveFileName.append(cal.get(Calendar.MONTH));
    archiveFileName.append(DASH);
    archiveFileName.append(cal.get(Calendar.DAY_OF_MONTH));
    archiveFileName.append(UNDERSCORE);
    archiveFileName.append(cal.get(Calendar.HOUR));
    archiveFileName.append(UNDERSCORE);
    archiveFileName.append(cal.get(Calendar.MINUTE));
    archiveFileName.append(UNDERSCORE);
    archiveFileName.append(cal.get(Calendar.SECOND));
    archiveFileName.append(DOT);
    archiveFileName.append(extension);
    File archiveFile = new File(archiveDirectory, archiveFileName.toString());
    if (archiveFile.exists())
      LOG.info("Archive file " + archiveFile.getAbsolutePath() + " already exists. Cannot overwrite it.");
    else {
      boolean success = copyFileIntoArchive(file, archiveFile);
      if (!success)
        LOG.error("Error while renaming the file '" + file.getAbsolutePath());
    }
    return archiveFile;
  }

  /**
   * Copy one file into another file.
   * 
   * @param file
   * @param archiveFile
   */
  synchronized private static boolean copyFileIntoArchive(File file, File archiveFile) {
      boolean success = true;
      FileInputStream fis = null;
      FileOutputStream fos = null;
      try {
        fis = new FileInputStream(file);
        fos = new FileOutputStream(archiveFile);
        FileChannel inChannel = fis.getChannel();
        FileChannel outChannel = fos.getChannel();
        int bytesWritten = 0;
        long byteCount = 0;
        while(bytesWritten < byteCount)
          bytesWritten +=inChannel.transferTo(bytesWritten, byteCount - bytesWritten, outChannel);

        fos.close();
        fis.close();

      } catch (Exception e) {
          LOG.error(e.getMessage(), e);
          return false;
      } finally {
          try {
              if (fis != null)
                  fis.close();
              if (fos != null)
                  fos.close();
          } catch (IOException e) {
              LOG.error(e.getMessage(), e);
              success = false;
          }
      }
      return success;
  }


  /**
   * Purge an archive directory for files that are older than the purge time.
   *
   * @param archiveDirectory
   * @param purgeTime        in milliseconds
   */
  public static void purgeArchiveDirectory(File archiveDirectory, long purgeTime) {
    File[] files = archiveDirectory.listFiles();
    long time = System.currentTimeMillis();
    for (File file : files) {
      long fileModified = file.lastModified();
      long fileAge = time - fileModified;
      if (fileAge > purgeTime) {
        boolean success = file.delete();
        if (!success)
          LOG.error("Error while purging archive file '" + file.getAbsolutePath());
      }
    }
  }

  /**
   * Strip off all the path information from a file name.
   * @param filename
   */
  public static String getPureFileName(String filename){
    if(StringUtils.isEmpty(filename) )
      return filename;

    File file = new File(filename);
    return file.getName();
  }

}
