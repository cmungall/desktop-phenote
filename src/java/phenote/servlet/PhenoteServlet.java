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
import phenote.datamodel.CharField.CharFieldEnum;
import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyManager;
import phenote.datamodel.SearchParamsI;
import phenote.util.HtmlUtil;
import phenote.gui.Phenote; // move to main package

public class PhenoteServlet extends HttpServlet {

  private Date initDate;
  private Phenote phenote;

  /** if <load-on-startup>1</load-on-startup> is in web.xml then init will
      happen when web server started (or if code recompiled) - so this is where
      the ontology reading & caching goes */
  public void init() throws ServletException {
    initDate = new Date();
    super.init();
    phenote = Phenote.getPhenote();
    phenote.initConfig(null); // no args for now, use default config file
    phenote.initOntologies();
  }


  /** this should be done in java server faces/pages(?), post comes from ajax
      autocompleter on typing in stuff */
  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException  {
      

    // dont know where this goes???
    System.out.println("servlet doPost "+new Date());
    System.err.println("is term comp request: "+isTermCompletionRequest(request));

    PrintWriter out = response.getWriter();

    if (isTermCompletionRequest(request)) {
      String userInput = getTermCompletionParam(request);
      System.out.println("ontology? "+getOntologyParamString(request)+" param aa? "+request.getParameter("aa"));
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
      System.out.println("printing to response writer: "+list.substring(0,85)+"...");
      out.println(list);
    }
        
  }

  private boolean isTermCompletionRequest(HttpServletRequest req) {
    return getTermCompletionParam(req) != null;
  }

  /** this should be renamed from unintuitive "ontologyname" */
  private String getTermCompletionParam(HttpServletRequest req) {
    return req.getParameter("patoInput");
  }

  private String getOntologyParamString(HttpServletRequest req) {
    return req.getParameter("ontologyName");
  }

  

  /** i cant tell ya why but term info is done with a get and term completion
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
      // for now hard wire to pato
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
      System.out.println("term info "+HtmlUtil.termInfo(oboClass));
      out.println(HtmlUtil.termInfo(oboClass));
    }
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
      System.out.println("failed to get pato from ontology manager");
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
    //return getPatoOntology();
    return OntologyManager.getOntologyForName(ontolName);
  }
  
  // for now...
  private Ontology getPatoOntology() {
    for (CharField cf : OntologyManager.inst().getCharFieldList())
      if (cf.getCharFieldEnum() == CharFieldEnum.PATO)
        return cf.getFirstOntology();
    System.out.println("pato ontology not found in ontology manager");
    return null;
  }

  private String makeCompListHtmlItem(OBOClass term,String ontol) {
    String id = "'"+term.getID()+"'";
    return "<li onmouseover=\"getTermInfo("+id+",'"+ontol+"')\" id="+id+" "+
      "onclick=\"set_ontology("+id+")\">"+term.getName()+"</li>\n";
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


}



