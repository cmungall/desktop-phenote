package phenote.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class FileUtil {

  //private static final Logger LOG =  Logger.getLogger(FileUtil.class);

  public static URL findUrl(String filename) throws FileNotFoundException {
    List<URL> possibleUrls = getPossibleUrls(filename);
    for (URL u : possibleUrls)
      if (urlExists(u)) return u;
    System.out.println("Failed to find file "+filename);
    //LOG.error("Failed to find file "+filename);
    throw new FileNotFoundException(filename+" not found");
  }

  private static List<URL> getPossibleUrls(String filename) {
    List<URL> urls = new ArrayList(5);
    addFile(filename,urls); // full path or relative to pwd
    addFile("conf/"+filename,urls);
    addFile("obo-files/"+filename,urls);//this is obo-files specific - separate method?
//     URL jarUrl = FileUtil.class.getResource(filename);
//     if (jarUrl != null) urls.add(jarUrl);
//     jarUrl = FileUtil.class.getResource("/"+filename);
//     if (jarUrl != null) urls.add(jarUrl);
    return urls;
  }

  private static void addFile(String filename,List<URL> urls) {
    try {
      URL u = new File(filename).toURL();
      if (u != null) urls.add(u);
      URL jarUrl = FileUtil.class.getResource(filename);
      if (jarUrl != null) urls.add(jarUrl);
      jarUrl = FileUtil.class.getResource("/"+filename);
      if (jarUrl != null) urls.add(jarUrl);
    } catch (MalformedURLException e) {
      System.out.println("bad file url "+e);
      //LOG.error("bad file url "+e);
    }
  }

  private static boolean urlExists(URL u) {
    try { u.openStream(); }
    catch (IOException e) { return false; }
    return true;
  }

}
