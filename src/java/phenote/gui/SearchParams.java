package phenote.gui;

// package phenote.ontology?? dataadapter?

public class SearchParams implements SearchParamsI {

  private static SearchParams singleton;
  private boolean searchTerm = true; //false; default true??
  private boolean searchSyn = true; // false; default true?
  private boolean searchDef = false;
  private boolean searchObs = false;

  /**
   * Singleton
   */
  private SearchParams() { //initialize Terms/Syns to true
  }

  public static SearchParams inst() {
    if (singleton == null)
      singleton = new SearchParams();
    return singleton;
  }

  /**
   * Set a search filter type. 
   * @param filter SearchFilterType
   * @param setting boolean
   */
  public void setParam(SearchFilterType filter, boolean setting) {
    if (SearchFilterType.TERM == filter) {
      searchTerm = setting;
    } else if (SearchFilterType.SYN == filter) {
      searchSyn = setting;
    } else if (SearchFilterType.DEF == filter) {
      searchDef = setting;
    } else if (SearchFilterType.OBS == filter) {
      searchObs = setting;
    }
  }

  //this will takeover the single boolean fxns
  public boolean getParam(SearchFilterType filter) {  //need to add a catch or something
    if (SearchFilterType.TERM == filter) {
      return searchTerm;
    } else if (SearchFilterType.SYN == filter) {
      return searchSyn;
    } else if (SearchFilterType.DEF == filter) {
      return searchDef;
    } else if (SearchFilterType.OBS == filter) {
      return searchObs;
    }
    return true;
  }

  public boolean searchTerms() {
    return searchTerm;
  }

  public boolean searchSynonyms() {
    return searchSyn;
  }

  public boolean searchDefinitions() {
    return searchDef;
  }

  /**
   * Whether to include obsoletes in searching terms, syns, & definitions
   * This should be in conjunction with the other 3
   */
  public boolean searchObsoletes() {
    return searchObs;
  }

  public boolean verifySettings() {
    //this function will verify that the user has selected a minimum of
    //a single term/syn/def + optional obs
    if (!(searchTerm || searchDef || searchSyn)) {
      if (searchObs) {
        searchTerm = true;  //default to select Term if obs is chosen, at minimum for obs
        return false;
      }
    }
    return true;
  }

}
