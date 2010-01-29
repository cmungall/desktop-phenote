package phenote.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import phenote.config.Preferences;
import phenote.datamodel.OntologyException;

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

  private static String sep(String a, String b) { return a + FILE_SEPARATOR + b; }


  /** split into findMaster?ConfigUrl & findOboUrl!!! */
  public static URL findUrl(String filename) throws FileNotFoundException {
    if (filename == null) {
      String m = "cant find null file";
      LOG.error(m); System.out.println(m);
      throw new FileNotFoundException(m);
    }
    List<URL> possibleUrls = getPossibleUrls(filename);
    for (URL u : possibleUrls) {  ///there's no particular order here, is there???
      //System.out.println(u+" url exists "+urlExists(u));
      if (urlExists(u)) return u;
    }
    //System.out.println("Failed to find file "+filename);
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
    List<URL> urls = new ArrayList<URL>(5);
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
    


  public static boolean urlExists(URL u) {
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
  synchronized public static boolean copyFileIntoArchive(File file, File archiveFile) {
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

  /** some content from @link http://www.java2s.com/Code/Java/File-Input-Output/CopyfilesusingJavaIOAPI.htm */
  synchronized public static boolean copyFile(URL fromUrl, File toFile) throws IOException {
    boolean success = true;
    InputStream content = null;
    try {
      content = (InputStream) fromUrl.getContent();
    } catch (IOException e) {
      success = false;
      LOG.error("Can't fetch URL " + fromUrl.getPath());
    }
    
    try {
      BufferedWriter output = new BufferedWriter(new FileWriter(toFile));
      int c;
      try {
        while ((c = content.read()) != -1) {
          output.write(c);
        }
      } catch (IOException e2) {
        LOG.error("Can't write byte to " + toFile.getPath());
      }
      output.close();
    } catch (IOException e) {
      success = false;
      LOG.error("Couldn't write to file " + toFile.getPath());
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
  
  public static String getLastModifiedDate(URL localUrl) {
    String display = "(not on local drive)";
    if (localUrl!=null) {
      // Will this always work?  What if we need to use a proxy?  		
      File f = new File(localUrl.getFile());
      long timestamp = f.lastModified();
      Date when = new Date(timestamp);
      SimpleDateFormat sdf = new SimpleDateFormat( "EEEE yyyy/MM/dd hh:mm:ss aa zz : zzzzzz" );
      sdf.setTimeZone(TimeZone.getDefault()); // local time
      display = sdf.format(when);
    }
    return display;
  }
  
  public static String getFileSize(URL url) {
    String text = "unknown";
    if (url!=null) {
      // Will this always work?  What if we need to use a proxy?
      File f = new File(url.getFile());
      long size = f.length();
      float dsize = (new Long(size).floatValue())/1000;  //KB
      DecimalFormat df= new DecimalFormat("0");
      text=df.format(dsize);
    }
    return text;
  }

  /** Should merge with getFileSize and getRemoteFileSize */
  public static int getBufferSize(URLConnection urlConnection) {
    int size = urlConnection.getContentLength();
//	LOG.info("For " + url + ", Content-Length = " + size);
    // The sourceforge URLs don't give the content length (apparently that's optional for servers to provide).
    if (size < 1) {
      size = Integer.MAX_VALUE;
    }
    return size;
  }
  
  // Not currently used
//   public static String getRemoteFileSize(URL url) {
//     URLConnection conn;
//     String text = "unknown";
//     int size=0;
//     if (url!=null) {
//       try {
//         conn = url.openConnection();
//         conn.connect();
//         size = conn.getContentLength();
//         conn.getInputStream().close();
//       } catch (IOException e) {
//         e.printStackTrace();
//       }
//       if(size > 0) {
// //    		System.out.println("Could not determine file size for: "+url.toString());
//         float dsize = size/1000;  //KB
//         DecimalFormat df= new DecimalFormat("0");
//         text=df.format(dsize);
//       }
//     }
//     return text;
// }

  /** local url may be from distrib/jar dir, but needs to be set to 
      .phenote/obo-files dir as thats where the user cache is */
  public static void copyReposToLocal(URL reposUrl, URL localUrl)
    throws OntologyException {
    try {
      URLConnection urlConnection = getURLConnectionWithOrWithoutProxy(reposUrl);
      InputStream is = urlConnection.getInputStream();
      ReadableByteChannel r = Channels.newChannel(is);
      //BufferedReader br = new BufferedReader(new InputStreamReader(is));
      File f = new File(localUrl.getFile());
      FileOutputStream fos = new FileOutputStream(f);
      FileChannel w = fos.getChannel();
      int size = getBufferSize(urlConnection);
      w.transferFrom(r,0,size);
      r.close();
      is.close(); // ??
      w.close();
      fos.close(); // ??
    } catch (IOException e) { throw new OntologyException(e); }
  }

  public static InputStream getInputStreamWithOrWithoutProxy(URL url) throws IOException {
    URLConnection urlConnection = getURLConnectionWithOrWithoutProxy(url);
    return urlConnection.getInputStream();
  }

  public static URLConnection getURLConnectionWithOrWithoutProxy(URL url) throws IOException {
    Proxy proxy = null;
    Preferences prefs = Preferences.getPreferences();

    if (prefs.getProxyIsSet() && !url.getProtocol().equals("file")) {
      String proxyHost = prefs.getProxyHost();
      int proxyPort = prefs.getProxyPort();
      String proxyProtocol = prefs.getProxyProtocol();
      if (proxyHost != null && proxyHost.length()>0) {
        if (proxyProtocol.equals("SOCKS"))
          proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost,proxyPort));
        else
          proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost,proxyPort));
      }
      if (proxy != null)
        LOG.info("Using proxy " + proxyProtocol + ": " + proxyHost + ":" + proxyPort + " to open connection to " + url);
    }
	
    URLConnection urlConnection;
    if (proxy != null) {
      urlConnection = url.openConnection(proxy);
    }
    else {
      LOG.debug("Opening connection (without proxy) to " + url);
      urlConnection = url.openConnection();
    }

    urlConnection.setConnectTimeout(30000); // 30 seconds
//    urlConnection.setReadTimeout(1800000);  // 30 minutes (is that long enough)
    return urlConnection;
  }

  /** Look for file in current directory (.) and jar file 
      throws OntologyException if file not found - wraps FileNFEx */
  public static URL findFile(String fileName) throws OntologyException {
    if (fileName == null) throw new OntologyException("findFile: file is null");
    try { return findUrl(fileName); }
    catch (FileNotFoundException e) { throw new OntologyException(e); }
  }

  /** goes thru url line by line and copies to file - is there a better way to 
      do this? actually should do copy anymore should read in and write out xml
      adding version along the way */
  /** Used by Config */
  public static void copyUrlToFile(URL configUrl, File myPhenote) throws IOException {
    LOG.info("About to copy "+configUrl+" to "+myPhenote);
//       InputStream is = configUrl.openStream();
//       FileOutputStream os = new FileOutputStream(myPhenote);
//       for(int next = is.read(); next != -1; next = is.read()) {
//         os.write(next);
//       }
//       is.close();
//       os.flush();
//      os.close();
    // 1/25/2010: Might need to use a proxy.
    URLConnection urlConnection = getURLConnectionWithOrWithoutProxy(configUrl);
    InputStream is = urlConnection.getInputStream();
    ReadableByteChannel r = Channels.newChannel(is);
    FileOutputStream fos = new FileOutputStream(myPhenote);
    FileChannel w = fos.getChannel();
    int size = getBufferSize(urlConnection);
    w.transferFrom(r,0,size);
    r.close();
    is.close(); // ??
    w.close();
    fos.close(); // ??
  }
}
