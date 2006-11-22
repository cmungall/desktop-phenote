package phenote.main;

/** Simple class for phenote version number - for standalone & servlet */

public class PhenoteVersion {
  
  //private static final float MAJOR_VERSION_NUM = 0.8f;
  //private static final float SUB_VERSION_NUM = .3f;
  private static final String VERSION = "1.0 dev"; // ??
  // type is "dev" or "release" 
  //private static final String type = " dev"; // "release"
  
  public static String versionString() {
    //return MAJOR_VERSION_NUM +""+ SUB_VERSION_NUM + type; 
    return VERSION;
  }

  
}
