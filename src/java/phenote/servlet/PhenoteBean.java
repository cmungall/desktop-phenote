package phenote.servlet;

import org.apache.commons.lang.StringUtils;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.Link;
import phenote.gui.field.CompletionTerm;
import phenote.util.TermLinkComparator;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 * This is the main form bean that contains all request parameters.
 * Spring populates these attributes automatically and make them available
 * in the controller class.
 * This also receives the term completion list which gets populated by comp list
 searcher do completion using input params - userInput, field, ontology
 */
public class PhenoteBean {

  public enum View{
      INTERNAL,EXTERNAL
  }

  private String userInput;
  private String qualityInput;
  private String entityInput;
  private String ontologyName;
  private String termId;
  private String field;
  private String viewType;

  private String ajaxReturn;
  private List<CompletionTerm> completionTermList;
  private OBOClass term;
  private Link startStage;
  private Link endStage;

  public boolean getIsExternalViewType(){
      return (viewType!=null && viewType.equals(View.EXTERNAL.toString())) ; 
  }

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
    parents.remove(startStage);
    parents.remove(endStage);
    Collections.sort(parents, new TermLinkComparator());
    return parents;
  }

  private void filterStages(List<Link> parents) {
    List<Link> removeTerms = new ArrayList<Link>();
    for(Link term: parents){
      String name = term.getType().getName();
      if(name.equals("start stage"))
        startStage = term;
      if(name.equals("end stage"))
        endStage = term;
    }
    parents.removeAll(removeTerms);
  }

  public List<Link> getChildren(){
    List<Link> children = new ArrayList<Link>();
    children.addAll(term.getChildren());
    Collections.sort(children, new TermLinkComparator());
    return children;
  }

  public Link getStartStage(){
       return startStage;
  }

  public Link getEndStage(){
       return endStage;
  }

  public void setTerm(OBOClass term) {
    this.term = term;
    List<Link> parents = new ArrayList<Link>();
    parents.addAll(term.getParents());
    filterStages(parents);
  }
  
  public String getViewType()
  {
      return viewType;
  }
  
  public void setViewType(String view)
  {
      this.viewType = view;
  }
}

