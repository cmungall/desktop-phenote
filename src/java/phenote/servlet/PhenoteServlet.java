package phenote.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Vector;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.geneontology.oboedit.datamodel.OBOClass;
import org.apache.log4j.Logger;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyException;
import phenote.datamodel.OntologyManager;
// hmmm bad to ref gui? or is servlet gui-esque? or move search to own package?
import phenote.gui.field.CompletionTerm;
import phenote.gui.field.CompListSearcher;
import phenote.gui.field.SearchParamsI; 
import phenote.util.HtmlUtil;
import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.main.Phenote;

public class PhenoteServlet extends HttpServlet {

    private static final String CONFIG_FILE_PARAM = "configuration-file";
  // PhenoteServlet.class is the distinct name for this logger
  private static final Logger LOG = Logger.getLogger(PhenoteServlet.class);

    //private String configurationFileName; not sure needs to be var
    private Date initDate;
    private Phenote phenote;

    /**
     * if <load-on-startup>1</load-on-startup> is in web.xml then init will
     * happen when web server started (or if code recompiled) - so this is where
     * the ontology reading & caching goes
     */
    public void init() throws ServletException {
        initDate = new Date();
        super.init();
        // makes links for term info - put this method in Phenote?
        HtmlUtil.setStandAlone(false);
        phenote = Phenote.getPhenote();
        // from web.xml
        String configFile = getInitParameter(CONFIG_FILE_PARAM);
        // just in case not in web.xml
        if (configFile == null || configFile.equals(""))
            configFile = "/initial-zfin.cfg";
        //boolean DEBUG = true;  if (DEBUG) configFile = "/fiddle.cfg";
        try {
            Config.inst().setConfigFile(configFile); // causes parse of file
        } catch (ConfigException e) {
            String m = "Error in config file: " + configFile + " " + e.getMessage();
            LOG.error(m);
            throw new ServletException(m); // ??
        }
        // cheesy - revisit
        //String[] args = {"-c","initial-zfin.cfg"};
        //phenote.initConfig(args); // hardwire for now to zfin
        phenote.initOntologies();
        // this is not running as a separate thread - investigate
//     System.out.println("ontologies loaded - starting file checking thread");
//     OntologyFileCheckThread ofct = new OntologyFileCheckThread();
//     ofct.start(); // is this not running as threaded?
//     System.out.println("file thread launched - moving on");
    }


    /**
     * AUTO COMPLETE REQUEST
     * this should be done in java server faces/pages(?), post comes from ajax
     * autocompleter on typing in stuff
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        // dont know where this goes???
        LOG.debug("servlet doPost " + new Date());
        LOG.info("is term comp request: " + isTermCompletionRequest(request));

        PrintWriter out = response.getWriter();

        if (isTermCompletionRequest(request)) {
            String userInput = getTermCompletionParam(request);
            LOG.info("ontology? " + getOntologyParamString(request) + " param entityInput? " + getTermCompletionParam(request));
//ResourceBundle r=ResourceBundle.getBundle("LocalStrings",request.getLocale());
            //Content-Type: text/html; charset=ISO-8859-1
            response.setContentType("text/html");
            //out.println("Content-Type: text/html; charset=ISO-8859-1"); // this messes things up
//       String list = "<ul><li onmouseover=\"set_ontology()\" id=\"termId\" "+
//         "onclick=\"set_ontology()\">"+userInput+"</li>\n"+
//         "<li onmouseover=\"set_ontology()\" id=\"termId\" onclick=\"set_ontology()\">"+
//         "test</li>\n<li onmouseover=\"set_ontology()\" id=\"termId\" onclick=\"set_ontology()\">dude</li></ul>";
            String ontol = getOntologyParamString(request);
            String field = getFieldParamString(request);
            String list = getCompletionList(userInput, ontol, field);
            LOG.debug("printing to response writer: " + substring(list, 55) + "...");
            out.println(list);
        }

    }

    private boolean isTermCompletionRequest(HttpServletRequest req) {
        return getTermCompletionParam(req) != null;
    }

    /**
     * this should be renamed from unintuitive "ontologyname"
     */
    private String getTermCompletionParam(HttpServletRequest req) {
        String par = req.getParameter("userInput"); // new way
        if (par == null)
            par = req.getParameter("qualityInput"); // for now - pase i think
        if (par == null)
            par = req.getParameter("entityInput");
        return par;
    }

    private String getOntologyParamString(HttpServletRequest req) {
        return req.getParameter("ontologyName");
    }

  /** field param string specifies what gui field the request came from - this is 
      used for UseTermInfo button to populate field from term info */
  private String getFieldParamString(HttpServletRequest req) {
    return req.getParameter("field");
  }



  /**
   * TERM INFO request
   * i cant tell ya why but term info is done with a get and term completion
   * is done with a post - is there rhyme or reason to this?
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
    if (isTermInfoRequest(request)) {
      PrintWriter out = response.getWriter();
      String termId = getTermIdFromTermInfoRequest(request);
      String ontologyName = getOntologyParamString(request);
      String field = getFieldParamString(request);
      LOG.debug("doGet term info param: " + termId + " ont " + ontologyName);
      try {
        Ontology ont = getOntology(ontologyName); // throws ex
        OBOClass oboClass = ont.getOboClass(termId); // throws ex
        //if (oboClass == null) {
        //  LOG.error("term info: no obo class found for " + termId); return;  }
        LOG.debug("term info " + substring(HtmlUtil.termInfo(oboClass), 60));
        out.println(HtmlUtil.termInfo(oboClass, ontologyName,field));
      }
      catch (OntologyException e) {
        LOG.error(e.getMessage());
      }
    }
  }

    private String substring(String s, int sz) {
        sz = (s.length() <= sz) ? s.length() : sz;
        return s.substring(0, sz);
    }

    private boolean isTermInfoRequest(HttpServletRequest req) {
        return getTermIdFromTermInfoRequest(req) != null;
    }

    private String getTermIdFromTermInfoRequest(HttpServletRequest req) {
        return req.getParameter("termId");
    }


  /** List<String>? String[]? or String htmlLiString?
      for now just return html ul-li list w onmouseover
      userInput is what user has typed which terms will be queried for
      ontol is the name of the ontology that user is querying
      field is the gui field user is querying (which may have multiple ontols) 
      if ontologyy not found actually returns error string saying as much - why not*/
  private String getCompletionList(String userInput,String ontol,String field) {
    StringBuffer sb = new StringBuffer("<ul>");
    try {
      Vector<CompletionTerm> v = getCompListSearcher(ontol).getStringMatchTerms(userInput);
      //Vector<OBOClass> v = ontology.getStringMatchTerms(userInput, getSearchParams());
      for (CompletionTerm ct : v)
        sb.append(makeCompListHtmlItem(ct, ontol, field));
    }
    catch (OntologyException e) { sb.append(e.getMessage()); }
    sb.append("</ul>");
    return sb.toString();
  }

  private CompListSearcher getCompListSearcher(String ontol) throws OntologyException {
    return new CompListSearcher(getOntology(ontol),getSearchParams());
  }

  /**
   * throws ex if ontolName not found 
   */
  private Ontology getOntology(String ontolName) throws OntologyException { 
//     if (ontology == null) {
//       LOG.error("failed to get " + ontol + " from ontology manager");
//       //return "ontology retrieval failed";
//       throw new Exception("ontology retrieval failed");  }
    return OntologyManager.inst().getOntologyForName(ontolName);
  }

//   // for now...
//   private Ontology getQualityOntology() {
//     for (CharField cf : OntologyManager.inst().getCharFieldList())
//       if (cf.getCharFieldEnum() == CharFieldEnum.QUALITY)
//         return cf.getFirstOntology();
//     LOG.error("quality ontology not found in ontology manager");
//     return null;
//   }

  private String makeCompListHtmlItem(CompletionTerm term, String ontol,String field) {
    String id = term.getID(), name=term.getName();
    // pass in id, name & ontology - name for setting field on UseTerm
    StringBuffer info = dq(fn("getTermInfo",new String[]{id,name,ontol,field}));
    //String info = "\"getTermInfo("+id +","+q(name)+","+ q(ontol) + ")\"";
    return "<li onmouseover=" + info + " id=" + q(id) + " " +
      "onclick=" + info + ">" + name + "</li>\n";
  }

  private static StringBuffer fn(String fnName, String[] params) {
    return HtmlUtil.fn(fnName,params);
  }
//     StringBuffer s = new StringBuffer(fnName).append("(").append(q(params[0]));
//     for (int i=1; i<params.length; i++) s.append(",").append(q(params[i]));
//     s.append(")");    return s;   }

  private static StringBuffer dq(StringBuffer sb) {
    return new StringBuffer("\""+sb+"\"");
  }

  private static StringBuffer q(StringBuffer sb) {
    return new StringBuffer("'"+sb+"'");
  }

  private static String q(String s) {
    return "'"+s+"'";
  } 

    /**
     * for now search params hard wired - eventually from buttons on web page
     */
    private SearchParamsI getSearchParams() {
        return new HardWiredSearchParams();
    }

    private class HardWiredSearchParams implements SearchParamsI {
        public boolean searchTerms() {
            return true;
        }

        public boolean searchSynonyms() {
            return false;
        }

        public boolean searchDefinitions() {
            return false;
        }

        /**
         * Whether to include obsoletes in searching terms, syns, & definitions
         * This should be in conjunction with the other 3
         */
        public boolean searchObsoletes() {
            return false;
        }
    }

}

//      if (ont == null) {
//       LOG.error("ERROR: Failed to get ontology for " + ontologyName); return; }
// ---> OntologyDataAdapter - moved to
//   /** thread wakes up and checks if theres a new ontology file - if so loads it
//    this should go in ontology data adapter... not here and configged */
//   private class OntologyFileCheckThread extends Thread {

//     public void run() {

//       while(true) {
//         System.out.println("checking for new files...");
//         // check for files...

//         // sleep in milliseconds - sleep for 5 seconds for now (test)
//         try { sleep(5000); }
//         catch (InterruptedException e) { System.out.println("interrupted"); }
//       }
//     }
//   }



