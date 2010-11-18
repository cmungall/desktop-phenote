package phenote.main;

import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;

/** Simple class for phenote version number - for standalone & servlet */

public class PhenoteVersion {

  private static String hardwiredDefaultVersion = "1.8.4";
  private static final Logger LOG = Logger.getLogger(PhenoteVersion.class);

  public static String versionString() {
    final String version = System.getProperty("phenote.version");
    if (version != null) {
      return version;
    } else {
      // This seems to happen every time--can we fix it?
      LOG.info("Version information not found as property, going with hardwired default of "+hardwiredDefaultVersion);
      
      return hardwiredDefaultVersion;
    }
  }
  
  public static String buildString() {
    final String build = System.getProperty("phenote.build"); 
    if (build != null) {
      return build;
    } else {
      LOG.error("Build information not found");
      return "";
    }
  }

  public static Date getDateOfVersion(){
    GregorianCalendar cal = new GregorianCalendar(2008, GregorianCalendar.APRIL, 15);
    return cal.getTime();
  }
}
