package phenote.servlet;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.geneontology.oboedit.datamodel.OBOClass;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import phenote.gui.field.CompletionTerm;
import phenote.gui.field.CompListSearcher;
import phenote.gui.field.SearchParamsI;
import phenote.datamodel.OntologyException;
import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyManager;
import phenote.util.HtmlUtil;

import java.util.Vector;

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
    PhenoteBean form = (PhenoteBean) command;
    String ontologyName = form.getOntologyName();
    String field = form.getField();
    if (form.isTermCompletionRequest()) {
      String userInput = form.getTermCompletionTerm();
      LOG.info("ontology: " + ontologyName);
      LOG.info("param entityInput: " + userInput);
      LOG.info("Field entity: " + field);

      form.setAjaxList(getCompletionList(userInput, ontologyName, field));
      LOG.debug(form.getAjaxList());
    } else if (form.isTermInfoRequest()) {
      String termId = form.getTermId();
      LOG.debug("doGet term info param: " + termId + " ont " + ontologyName);
      try {
        Ontology ont = getOntology(ontologyName);
        OBOClass oboClass = ont.getOboClass(termId);
        String termInfo = HtmlUtil.termInfo(oboClass);
        LOG.debug("term info " + termInfo.substring(0,60));
        String termInfoObo = HtmlUtil.termInfo(oboClass, ontologyName, field);
        form.setAjaxList(termInfoObo);
      }
      catch (OntologyException e) {
        LOG.error(e.getMessage(), e);
      }
    }
    return new ModelAndView("ajax", "formBean", form);
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
   * @param userInput
   * @param ontologyName
   * @param field
   */
  private String getCompletionList(String userInput, String ontologyName, String field) {
    StringBuffer sb = new StringBuffer("<ul>");
    try {
      Vector<CompletionTerm> v = getCompListSearcher(ontologyName).getStringMatchTerms(userInput);
      //Vector<OBOClass> v = ontology.getStringMatchTerms(userInput, getSearchParams());
      for (CompletionTerm ct : v)
        sb.append(makeCompListHtmlItem(ct, ontologyName, field));
    }
    catch (OntologyException e) {
      sb.append(e.getMessage());
    }
    sb.append("</ul>");
    return sb.toString();
  }

  private Object makeCompListHtmlItem(CompletionTerm term, String ontol, String field) {
    String id = term.getID(), name = term.getName();
    // pass in id, name & ontology - name for setting field on UseTerm
    StringBuffer info = dq(fn("getTermInfo", new String[]{id, name, ontol, field}));
    //String info = "\"getTermInfo("+id +","+q(name)+","+ q(ontol) + ")\"";
    return "<li onmouseover=" + info + " id=" + q(id) + " " +
            "onclick=" + info + ">" + name + "</li>\n";
  }

  private CompListSearcher getCompListSearcher(String ontologyName) throws OntologyException {
    return new CompListSearcher(getOntology(ontologyName), getSearchParams());
  }

  /**
   * throws ex if ontolName not found
   */
  private Ontology getOntology(String ontolName) throws OntologyException {
    return OntologyManager.inst().getOntologyForName(ontolName);
  }

  private static String q(String s) {
    return "'" + s + "'";
  }

  /**
   * for now search params hard wired - eventually from buttons on web page
   */
  private SearchParamsI getSearchParams() {
    return new HardWiredSearchParams();
  }

  private static StringBuffer dq(StringBuffer sb) {
    return new StringBuffer("\"" + sb + "\"");
  }

  private static StringBuffer fn(String fnName, String[] params) {
    return HtmlUtil.fn(fnName, params);
  }

  private class HardWiredSearchParams implements SearchParamsI {
    public boolean searchTerms() {
      return true;
    }

    public boolean searchSynonyms() {
      return false; // --> true
    }

    public boolean searchDefinitions() {
      return false; // ?? w [def]??
    }

    /**
     * Whether to include obsoletes in searching terms, syns, & definitions
     * This should be in conjunction with the other 3
     */
    public boolean searchObsoletes() {
      return false; // --> true w [obs], disallow selection
    }
  }


}
