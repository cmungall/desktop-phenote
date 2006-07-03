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

import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyManager;
import phenote.datamodel.SearchParamsI;
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


  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException  {
      

    // dont know where this goes???
    System.out.println("printing from phenote stub to std out...");
    System.err.println("printing from phenote stub to std err...");

    PrintWriter out = response.getWriter();

    if (isTermCompletionRequest(request)) {
      String userInput = getTermCompletionParam(request);
      //ResourceBundle r=ResourceBundle.getBundle("LocalStrings",request.getLocale());
      //Content-Type: text/html; charset=ISO-8859-1
      response.setContentType("text/html");
      //out.println("Content-Type: text/html; charset=ISO-8859-1");
      out.println("<ul><li onmouseover=\"set_ontology()\" id=\"termId\" "+
                  "onclick=\"set_ontology()\">"+userInput+"</li>\n"+
                  "<li onmouseover='set_ontology()' id='termId' "+
                  "onclick=\"set_ontology()\">phenoteservlet</li>\n<li onmouseover=\"set_ontology()\" id=\"termId\" onclick=\"set_ontology()\">"+ initDate +"</li></ul>");
      
    }
        
  }

  private boolean isTermCompletionRequest(HttpServletRequest req) {
    return getTermCompletionParam(req) != null;
  }

  private String getTermCompletionParam(HttpServletRequest req) {
    return req.getParameter("ontologyname");
  }

  private boolean isTermInfoRequest(HttpServletRequest req) {
    return getTermInfoParam(req) != null;
  }

  private String getTermInfoParam(HttpServletRequest req) {
    return req.getParameter("ontologyid");
  }

  

  /** i cant tell ya why but term info is done with a get and term completion
      is done with a post - is there rhyme or reason to this? */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException  {
    if (true || isTermInfoRequest(request)) {
      PrintWriter out = response.getWriter();
      String userInput = getTermInfoParam(request);
      out.println("<table><tr><td class=\"label\">Ontology</td> "+
                  "<td class=\"data\">"+initDate+"</td></tr>\n"+
                  "<tr><td class=\"label\">Term name</td><td class=\"data\">"
                  +userInput+"</td></tr></table>");
    }
  }

  // List<String>? String[]? or String htmlLiString?
  // for now just do html ul-li list
  private String getCompletionList(String userInput) {
    // for now just grab the pato ontology - eventuall redo for multiple/config
    Ontology ontology = OntologyManager.inst().getPatoOntology();
    Vector<OBOClass> v = ontology.getSearchTerms(userInput,getSearchParams());
    for (OBOClass oc : v)
      makeCompListHtmlItem(oc);
    return null;
  }

  private String makeCompListHtmlItem(OBOClass term) {
    return "<li onmouseover=\"set_ontology()";
  }

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



