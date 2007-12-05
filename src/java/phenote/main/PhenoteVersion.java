package phenote.main;

import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;

/** Simple class for phenote version number - for standalone & servlet */

public class PhenoteVersion {

  private static String hardwiredDefaultVersion = "1.5";

  public static String versionString() {
    final String version = System.getProperty("phenote.version");
    if (version != null) {
      return version;
    } else {
      log().info("Version information not found as property, going with hardwired default of "+hardwiredDefaultVersion);
      
      return hardwiredDefaultVersion;
    }
  }
  
  public static String buildString() {
    final String build = System.getProperty("phenote.build"); 
    if (build != null) {
      return build;
    } else {
      log().error("Build information not found");
      return "";
    }
  }

  public static Date getDateOfVersion(){
    GregorianCalendar cal = new GregorianCalendar(2007, GregorianCalendar.NOVEMBER, 6);
    return cal.getTime();
  }

  private static Logger log() {
    return Logger.getLogger(PhenoteVersion.class);
  }
}
