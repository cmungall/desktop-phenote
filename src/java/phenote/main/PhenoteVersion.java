package phenote.main;

import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;

/** Simple class for phenote version number - for standalone & servlet */

public class PhenoteVersion {

  public static String versionString() {
    final String version = System.getProperty("phenote.version");
    if (version != null) {
      return version;
    } else {
      log().error("Version information not found");
      return "";
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
    GregorianCalendar cal = new GregorianCalendar(2007, GregorianCalendar.JULY, 17);
    return cal.getTime();
  }

  private static Logger log() {
    return Logger.getLogger(PhenoteVersion.class);
  }
}
