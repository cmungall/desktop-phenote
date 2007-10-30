package phenote.main;

import java.io.FileNotFoundException;
import java.net.URL;

import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;

import org.apache.log4j.Logger;

import phenote.util.FileUtil;

public class HelpManager {
  protected static HelpBroker helpBroker;
  protected static String helpsetPath = "doc/phenote-website/help/Phenote.hs";
  private static String jarHelpsetPath = "help/Phenote.hs";
  private static final Logger LOG =  Logger.getLogger(HelpManager.class);
  
  public static HelpBroker getHelpBroker() {
    if (helpBroker == null) {
      HelpSet hs;
      //File docsDir = new File(helpsetPath);
      URL url;
      try { url = FileUtil.findUrl(helpsetPath); }
      catch (FileNotFoundException e) { // try jar path (webstart)
        try { 
          LOG.info("Didnt find help at "+helpsetPath+" trying "+jarHelpsetPath);
          url = FileUtil.findUrl(jarHelpsetPath);
          LOG.info("Found help at "+jarHelpsetPath);
        }
        catch  (FileNotFoundException e2) {
          LOG.error("phenote help not found "+e);
          return null;
        }
      }
      
      try {
        hs = new HelpSet(null, url); //docsDir.toURL());
      } catch (HelpSetException ee) {
        LOG.error("HelpSet error: " + ee.getMessage());
        return null;
      }
      helpBroker = hs.createHelpBroker();
    }
    
    return helpBroker;
  }
}
