package phenote.servlet;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.DispatcherServlet;
import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.dataadapter.OntologyDataAdapter;
import phenote.util.HtmlUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class PhenoteServlet extends DispatcherServlet {

  private static final String CONFIG_FILE_PARAM = "configuration-file";
  private static final Logger LOG = Logger.getLogger(PhenoteServlet.class);

  /**
   * Initialization of this servlet upon server startup.
   * Happens when <load-on-startup> tag in the web.xml is set to '1'.
   *
   * @param config
   * @throws ServletException
   */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    PhenoteWebConfiguration.getInstance().setWebRoot(getServletContext().getRealPath("/"));

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
    OntologyDataAdapter.initialize();
  }

}

