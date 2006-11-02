package phenote.servlet;

/**
 * This contains configuration parameters pertaining to the web application only.
 */
public class PhenoteWebConfiguration {

  private String webRoot;
  private static PhenoteWebConfiguration instance;

  private PhenoteWebConfiguration(){

  }

  public static PhenoteWebConfiguration getInstance(){
    if(instance == null){
      instance = new PhenoteWebConfiguration();
    }
    return instance;
  }


  public String getWebRoot() {
    return webRoot;
  }

  public void setWebRoot(String webRoot) {
    this.webRoot = webRoot;
  }
}

