package phenote.servlet;

import org.apache.commons.lang.StringUtils;

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

  private String ajaxList;

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

  public String getAjaxList() {
    return ajaxList;
  }

  public void setAjaxList(String ajaxList) {
    this.ajaxList = ajaxList;
  }

  public String getTermId() {
    return termId;
  }

  public void setTermId(String termId) {
    this.termId = termId;
  }

}

