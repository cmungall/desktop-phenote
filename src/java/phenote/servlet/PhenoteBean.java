package phenote.servlet;

import org.apache.commons.lang.StringUtils;
import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.Link;
import phenote.gui.field.CompletionTerm;
import phenote.util.TermLinkComparator;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 * This is the main form bean that contains all request parameters.
 * Spring populates these attributes automatically and make them available
 * in the controller class.
 */
public class PhenoteBean {

  private String userInput;
  private String qualityInput;
  private String entityInput;
  private String ontologyName;
  private String termId;
  private String field;

  private String ajaxReturn;
  private List<CompletionTerm> completionTermList;
  private OBOClass term;

  public boolean isTermCompletionRequest() {
    if (!StringUtils.isEmpty(userInput))
      return true;
    if (!StringUtils.isEmpty(qualityInput))
      return true;
    return !StringUtils.isEmpty(entityInput);

  }

  public String getTermCompletionTerm() {
    if (!StringUtils.isEmpty(userInput))
      return userInput;
    if (!StringUtils.isEmpty(qualityInput))
      return qualityInput;
    if (!StringUtils.isEmpty(entityInput))
      return entityInput;

    return null;
  }

  public boolean isTermInfoRequest() {
    return !StringUtils.isEmpty(termId);
  }

  public String getUserInput() {
    return userInput;
  }

  public void setUserInput(String userInput) {
    this.userInput = userInput;
  }

  public String getQualityInput() {
    return qualityInput;
  }

  public void setQualityInput(String qualityInput) {
    this.qualityInput = qualityInput;
  }

  public String getEntityInput() {
    return entityInput;
  }

  public void setEntityInput(String entityInput) {
    this.entityInput = entityInput;
  }

  public String getOntologyName() {
    return ontologyName;
  }

  public void setOntologyName(String ontologyName) {
    this.ontologyName = ontologyName;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public String getAjaxReturnList() {
    return ajaxReturn;
  }

  public void setAjaxReturnList(String ajaxReturnList) {
    this.ajaxReturn = ajaxReturnList;
  }

  public String getTermId() {
    return termId;
  }

  public void setTermId(String termId) {
    this.termId = termId;
  }

  public List<CompletionTerm> getCompletionTermList() {
    return completionTermList;
  }

  public void setCompletionTermList(List<CompletionTerm> completionTermList) {
    this.completionTermList = completionTermList;
  }

  public OBOClass getTerm() {
    return term;
  }

  public List<Link> getParents(){
    List<Link> parents = new ArrayList<Link>();
    parents.addAll(term.getParents());
    removeStages(parents);
    Collections.sort(parents, new TermLinkComparator());
    return parents;
  }

  private void removeStages(List<Link> parents) {
    
  }

  public List<Link> getChildren(){
    List<Link> children = new ArrayList<Link>();
    children.addAll(term.getChildren());
    Collections.sort(children, new TermLinkComparator());
    return children;
  }

  public void setTerm(OBOClass term) {
    this.term = term;
  }
}

