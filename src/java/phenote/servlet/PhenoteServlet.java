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

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyManager;
import phenote.datamodel.SearchParamsI;
import phenote.util.HtmlUtil;
import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.gui.Phenote; // move to main package

public class PhenoteServlet extends HttpServlet {

  private static final String CONFIG_FILE_PARAM = "configuration-file";

  //private String configurationFileName; not sure needs to be var
  private Date initDate;
  private Phenote phenote;

  /** if <load-on-startup>1</load-on-startup> is in web.xml then init will
      happen when web server started (or if code recompiled) - so this is where
      the ontology reading & caching goes */
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
    try {
      Config.inst().setConfigFile(configFile); // causes parse of file
    } catch (ConfigException e) {
      String m = "Error in config file: "+configFile+" "+e.getMessage();
      System.out.println(m);
      throw new ServletException(m); // ??
    }
    // cheesy - revisit
    //String[] args = {"-c","initial-zfin.cfg"};
    //phenote.initConfig(args); // hardwire for now to zfin
    phenote.initOntologies();
    // this is not running as a separate thread - investigate
//     System.out.println("ontologies loaded - starting file checking thread");
//     OntologyFileCheckThread ofct = new OntologyFileCheckThread();
//     ofct.run(); // is this not running as threaded?
//     System.out.println("file thread launched - moving on");
  }


  /** AUTO COMPLETE REQUEST
      this should be done in java server faces/pages(?), post comes from ajax
      autocompleter on typing in stuff */
  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException  {
      

    // dont know where this goes???
    System.out.println("servlet doPost "+new Date());
    System.err.println("is term comp request: "+isTermCompletionRequest(request));

    PrintWriter out = response.getWriter();

    if (isTermCompletionRequest(request)) {
      String userInput = getTermCompletionParam(request);
      System.out.println("ontology? "+getOntologyParamString(request)+" param entityInput? "+getTermCompletionParam(request));
//ResourceBundle r=ResourceBundle.getBundle("LocalStrings",request.getLocale());
      //Content-Type: text/html; charset=ISO-8859-1
      response.setContentType("text/html");
      //out.println("Content-Type: text/html; charset=ISO-8859-1"); // this messes things up
//       String list = "<ul><li onmouseover=\"set_ontology()\" id=\"termId\" "+
//         "onclick=\"set_ontology()\">"+userInput+"</li>\n"+
//         "<li onmouseover=\"set_ontology()\" id=\"termId\" onclick=\"set_ontology()\">"+
//         "test</li>\n<li onmouseover=\"set_ontology()\" id=\"termId\" onclick=\"set_ontology()\">dude</li></ul>";
      String ontol = getOntologyParamString(request);
      String list = getCompletionList(userInput,ontol);
      System.out.println("printing to response writer: "+substring(list,55)+"...");
      out.println(list);
    }
        
  }

  private boolean isTermCompletionRequest(HttpServletRequest req) {
    return getTermCompletionParam(req) != null;
  }

  /** this should be renamed from unintuitive "ontologyname" */
  private String getTermCompletionParam(HttpServletRequest req) {
    String par = req.getParameter("userInput"); // new way
    if (par ==null)
      par = req.getParameter("qualityInput"); // for now - pase i think
    if (par == null)
      par = req.getParameter("entityInput");
    return par;
  }

  private String getOntologyParamString(HttpServletRequest req) {
    return req.getParameter("ontologyName");
  }

  

  /** TERM INFO request
      i cant tell ya why but term info is done with a get and term completion
      is done with a post - is there rhyme or reason to this? */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException  {
    if (true || isTermInfoRequest(request)) {
      PrintWriter out = response.getWriter();
      String termId = getTermIdFromTermInfoRequest(request);
      String ontologyName = getOntologyParamString(request);
      System.out.println("doGet term info param: "+termId+" ont "+ontologyName);
//       out.println("<table><tr><td class=\"label\">Ontology</td> "+
//                   "<td class=\"data\">"+initDate+"</td></tr>\n"+
//                   "<tr><td class=\"label\">Term name...</td><td class=\"data\">"
//                   +userInput+"</td></tr></table>");
      Ontology ont = getOntology(ontologyName);
      if (ont == null) {
        System.out.println("ERROR: Failed to get ontology for "+ontologyName);
        return;
      }
      OBOClass oboClass = getOntology(ontologyName).getOboClass(termId);
      if (oboClass == null) {
        System.out.println("term info: no obo class found for "+termId);
        return;
      }
      System.out.println("term info "+substring(HtmlUtil.termInfo(oboClass),60));
      out.println(HtmlUtil.termInfo(oboClass,ontologyName));
    }
  }

  private String substring(String s,int sz) {
    sz = (s.length() <= sz) ? s.length() : sz;
    return s.substring(0,sz);
  }

  private boolean isTermInfoRequest(HttpServletRequest req) {
    return getTermIdFromTermInfoRequest(req) != null;
  }

  private String getTermIdFromTermInfoRequest(HttpServletRequest req) {
    return req.getParameter("termId");
  }


  // List<String>? String[]? or String htmlLiString?
  // for now just return html ul-li list w onmouseover
  private String getCompletionList(String userInput,String ontol) {
    StringBuffer sb = new StringBuffer("<ul>");
    // for now just grab the pato ontology - eventuall redo for multiple/config
    Ontology ontology = getOntology(ontol);
    if (ontology == null) {
      System.out.println("failed to get "+ontol+" from ontology manager");
      return "ontology retrieval failed";
    }
    Vector<OBOClass> v = ontology.getSearchTerms(userInput,getSearchParams());
    for (OBOClass oc : v)
      sb.append(makeCompListHtmlItem(oc,ontol));
    sb.append("</ul>");
    return sb.toString();
  }

  /** returns null if ontolName not found */
  private Ontology getOntology(String ontolName) { // termid?? or ontology name?
    //return getQualityOntology();
    return OntologyManager.getOntologyForName(ontolName);
  }
  
  // for now...
  private Ontology getQualityOntology() {
    for (CharField cf : OntologyManager.inst().getCharFieldList())
      if (cf.getCharFieldEnum() == CharFieldEnum.QUALITY)
        return cf.getFirstOntology();
    System.out.println("quality ontology not found in ontology manager");
    return null;
  }

  private String makeCompListHtmlItem(OBOClass term,String ontol) {
    String id = "'"+term.getID()+"'";
    String info = "\"getTermInfo("+id+",'"+ontol+"')\"";
    return "<li onmouseover="+info+" id="+id+" "+
      "onclick="+info+">"+term.getName()+"</li>\n";
  }

  /** for now search params hard wired - eventually from buttons on web page */
  private SearchParamsI getSearchParams() {
    return new HardWiredSearchParams();
  }

  private class HardWiredSearchParams implements SearchParamsI {
    public boolean searchTerms() { return true; }
    public boolean searchSynonyms() { return false; }
    public boolean searchDefinitions() { return false; }
  /** Whether to include obsoletes in searching terms, syns, & definitions
      This should be in conjunction with the other 3 */
    public boolean searchObsoletes() { return false; }
  }


  /** thread wakes up and checks if theres a new ontology file - if so loads it
   this should go in ontology data adapter... not here and configged */
  private class OntologyFileCheckThread extends Thread {

    public void run() {

      while(true) {
        System.out.println("checking for new files...");
        // check for files...
        
        
        // sleep in milliseconds - sleep for 5 seconds for now (test)
        try { sleep(5000); }
        catch (InterruptedException e) { System.out.println("interrupted"); }
      }
    }
  }

}



