package phenote.servlet;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import phenote.dataadapter.OntologyDataAdapter;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyException;
import phenote.gui.SearchFilterType;
import phenote.gui.SearchParams;
import phenote.gui.field.CompListSearcher;
import phenote.gui.field.CompletionTerm;
import phenote.gui.field.SearchListener;
import phenote.util.HtmlUtil;

/**
 * The main controller that receives Ajax ontology requests.
 */
public class PhenoteController extends AbstractCommandController {

  private static final Logger LOG = Logger.getLogger(PhenoteController.class);

  public PhenoteController() {
    setCommandClass(PhenoteBean.class);
  }

  protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command,
                                BindException errors) throws Exception {
    //System.out.println("in PhenoteController.handle()");
    PhenoteBean form = (PhenoteBean) command;
    String ontologyName = form.getOntologyName();
    String field = form.getField();
    // Todo: This is needed as the servlet can be configured to not initialize the ontologies (lazy initialization).
    // this logic should go into the OntologyManger that checks if the configuration file has been loaded or not.
    OntologyDataAdapter.initialize();

    // TERM COMPLETION
    if (form.isTermCompletionRequest()) {
      String userInput = form.getTermCompletionTerm();
      LOG.info("ontology: " + ontologyName);
      LOG.info("param entityInput: " + userInput);
      LOG.info("Field entity: " + field);

      //form.setCompletionTermList( // this is now done by ContSearchListener
      getCompletionList(userInput, ontologyName, field, form);
      LOG.debug(form.getAjaxReturnList()); // ?? should this go in CSL?
    }

    // TERM INFO
    else if (form.isTermInfoRequest()) {
      String termId = form.getTermId();
      LOG.debug("doGet term info param: " + termId + " ont " + ontologyName);
      try {
        Ontology ont = getOntology(ontologyName);
        // ToDo: Put oboClass in the web session, then we do not need to pass in the ontology name in
        // the html ajax call
        OBOClass oboClass = ont.getTerm(termId);
        form.setTerm(oboClass);
        return new ModelAndView("term_info", "formBean", form);
      }
      catch (OntologyException e) {
        LOG.error(e.getMessage(), e);
      }
    }

    return new ModelAndView("term_completion", "formBean", form);
  }

  /** @param userInput is the string entered by user, that needs to be "completed"
      @param form is PhenoteBean from spring that handles input & output to web 
      @param field is name of char field in datamodel
      @param ontologyName name of ontology to use in field, can be ALL */
  void getCompletionList(String userInput, String ontologyName, String field,
                                 PhenoteBean form) {
    //List<CompletionTerm> termList = null;
    SearchListener searchListener = new ContSearchListener(ontologyName,field,form);
    try {
      //termList = its now optionally threaded - term list returned to SearchListener
      // Christian - set this boolean to false to turn off threading
      boolean thread = false; //true; true optimizes but doesnt work for web
      getCompListSearcher(ontologyName).getStringMatchTermList(userInput,searchListener,
                                                               thread);
    } catch (OntologyException e) {
      // Todo: Add this error as an error completion list
      LOG.error(e.getMessage(), e);
    }
    //includeAdditionalAttributes(termList, ontologyName, field);
    //return termList;
  }

  /** listens to list searcher and populates PhenoteBean form with comp list
      results - when threaded this seems to fail (web doesnt get comp list) */
  private class ContSearchListener implements SearchListener {
    private String ontologyName;
    private String field;
    private PhenoteBean form;
    private ContSearchListener(String ontologyName, String field, PhenoteBean form) {
      this.ontologyName = ontologyName;
      this.field = field;
      this.form = form;
    }
    public void newResults(List termList) {
      includeAdditionalAttributes(termList, ontologyName, field);
      form.setCompletionTermList(termList);
    }
  }

  private void includeAdditionalAttributes(List<CompletionTerm> termList, String ontologyName, String field) {
    if (termList == null)
      return;

    for (CompletionTerm term : termList) {
      term.setField(field);
      term.setOntol(ontologyName);
    }
  }


  /**
   * ToDo: Need to cleanup this comment.
   * List<String>? String[]? or String htmlLiString?
   * for now just return html ul-li list w onmouseover
   * userInput is what user has typed which terms will be queried for
   * ontol is the name of the ontology that user is querying
   * field is the gui field user is querying (which may have multiple ontols)
   * if ontologyy not found actually returns error string saying as much - why not
   *
   */
//   @SuppressWarnings("unused")
// private String  {
//     StringBuffer sb = new StringBuffer("<ul>");
//     try {
//       Vector<CompletionTerm> v = getCompListSearcher(ontologyName).getStringMatchTerms(userInput);
//       //Vector<OBOClass> v = ontology.getStringMatchTerms(userInput, getSearchParams());
//       // a tad cheesy but if hit no terms then auto comp shows nothing
//       // add an empty item should show then an empty list?
//       if (v.isEmpty())
//         sb.append("<li></li>");
//       for (CompletionTerm ct : v)
//         sb.append(makeCompListHtmlItem(ct, ontologyName, field));
//     }
//     catch (OntologyException e) {
//       sb.append(e.getMessage());
//     }
//     sb.append("</ul>");
//     return sb.toString();
//   }

  private String makeCompListHtmlItem(CompletionTerm term, String ontol, String field) {
    String id = term.getID();
    String display = term.getCompListDisplayString();
    String name = term.getName();
    // pass in id, name & ontology - name for setting field on UseTerm
    StringBuffer info = dq(fn("getTermInfo", new String[]{id, name, ontol, field}));
    StringBuffer select = dq(fn("selectTerm", new String[]{name, field}));
    //String info = "\"getTermInfo("+id +","+q(name)+","+ q(ontol) + ")\"";
    return "<li onmouseover=" + info + " id=" + q(id) + " termTest='dude' " +
            " onclick=" + select + ">" + display + "</li>\n";
  }

  private CompListSearcher getCompListSearcher(String ontologyName) throws OntologyException {
	// This is currently rigged for 1 ontology, eventually may want to search all ontologies
	// in a field as standalone does - with ALL option
    SearchParams searchParams = SearchParams.inst();
    searchParams.setParam(SearchFilterType.TERM, true);
    searchParams.setParam(SearchFilterType.SYN, true);
    return new CompListSearcher(getOntology(ontologyName));
  }

  /**
   * throws ex if ontolName not found
   */
  private Ontology getOntology(String ontolName) throws OntologyException {
    return CharFieldManager.inst().getOntologyForName(ontolName);
  }

  private static String q(String s) {
    return "'" + s + "'";
  }

  /**
   * for now search params hard wired - eventually from buttons on web page
   */
//  private SearchParamsI getSearchParams() {
//    return new HardWiredSearchParams();
//  }

  private static StringBuffer dq(StringBuffer sb) {
    return new StringBuffer("\"" + sb + "\"");
  }

  private static StringBuffer fn(String fnName, String[] params) {
    return HtmlUtil.fn(fnName, params);
  }

//  private class HardWiredSearchParams implements SearchParamsI {
//    public boolean searchTerms() {
//      return true;
//    }
//
//    public boolean searchSynonyms() {
//      return true; // --> true
//    }
//
//    public boolean searchDefinitions() {
//      return false; // ?? w [def]?? zfin not keen on defs
//    }
//
//    /**
//     * Whether to include obsoletes in searching terms, syns, & definitions
//     * This should be in conjunction with the other 3
//     */
//    public boolean searchObsoletes() {
//      return true; // --> true w [obs], disallow selection
//    }
//  }

}

//   private Object makeCompListHtmlItemOLD(CompletionTerm term, String ontol, String field) {
//     String id = term.getID(), name = term.getName();
//     // pass in id, name & ontology - name for setting field on UseTerm
//     StringBuffer info = dq(fn("getTermInfo", new String[]{id, name, ontol, field}));
//     //String info = "\"getTermInfo("+id +","+q(name)+","+ q(ontol) + ")\"";
//     return "<li onmouseover=" + info + " id=" + q(id) + " " +
//             "onclick=" + info + ">" + name + "</li>\n";
//   }
