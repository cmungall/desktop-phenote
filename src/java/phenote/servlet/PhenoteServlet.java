package phenote.servlet;

import java.io.File;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.web.servlet.DispatcherServlet;

import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.dataadapter.OntologyDataAdapter;
import phenote.util.HtmlUtil;

public class PhenoteServlet extends DispatcherServlet {

  private static final String CONFIG_FILE_PARAM = "configuration-file";
  private static final Logger LOG = Logger.getLogger(PhenoteServlet.class);
  private static final String LOG4J_FILE_NAME = "log4j.xml";
  private static String webInfDir;
  private static boolean lazyLoading = false;
  private static boolean useLog4j = false ; 

  /**
   * Initialization of this servlet upon server startup.
   * Happens when <load-on-startup> tag in the web.xml is set to '1'.
   *
   * @param config ServletConfig
   * @throws ServletException
   */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    webInfDir = getServletContext().getRealPath("/WEB-INF/");

    PhenoteWebConfiguration.getInstance().setWebRoot(getServletContext().getRealPath("/"));
    getIntialParameters();

    // ToDo:
    // makes links for term info - put this method in Phenote?
    // Yes, this looks ood in here. Can Phenote get this info from the
    // property file?
    HtmlUtil.setStandAlone(false);
    String configFile = getInitParameter(CONFIG_FILE_PARAM);
    // set default value
    if (StringUtils.isEmpty(configFile))
      configFile = "/initial-zfin.cfg";

    // ToDo: how do we deal with: file not found?
    try {
      Config.inst().setConfigFile(configFile); // causes parse of file
    } catch (ConfigException e) {
      String errorMessage = "Error in config file: " + configFile + " " + e.getMessage();
      LOG.error(errorMessage, e);
      throw new ServletException(errorMessage); // ??
    }
    // Ensure the Ontologies are read during start up. This takes a while and should be done
    // before a different client calls and has to wait.
    // ToDo: Shall we create a new method called  OntologyDataAdapter.start()? YES
    if (!lazyLoading){
      OntologyDataAdapter.initialize();
    }

    // zfin does not use log4j
    if(useLog4j){
        initLog4j();
    }
  }

  private void getIntialParameters() {
    ServletContext context = getServletContext();
    String lazyload = context.getInitParameter("lazy-load");
    if (!StringUtils.isEmpty(lazyload) && lazyload.equals("true"))
      lazyLoading = true;

  }

  private void initLog4j() {
    String log4jFileName = getLog4JFile();
    // if the log4j-init-file is set do not initialize log4j.
    if (log4jFileName != null) {
      DOMConfigurator.configure(log4jFileName);
    }
  }

  private String getLog4JFile() {
    File log4jFile = new File(webInfDir, LOG4J_FILE_NAME);
    String log4jFileName = log4jFile.getAbsolutePath();
    if (!log4jFile.exists()){
      LOG.info("Cannot find the log4j file: " + log4jFileName );
      LOG.info(" Log4j is not configured in this servlet. Use App servers configuration " +
               "make sure you have a log4j.properties file in the classes directory.");
      return null;
    }
    return log4jFileName;
  }


}

