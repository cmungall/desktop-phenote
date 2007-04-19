package phenote.gui;

// package phenote.ontology?? dataadapter?

public class SearchParams implements SearchParamsI {
	
  private static SearchParams singleton;
  private boolean searchTerm = false;
  private boolean searchSyn = false;
  private boolean searchDef = false;
  private boolean searchObs = false;
  private final String TERM = "Term Name";  //SearchFilterTypes
  private final String SYN = "Synonyms";
  private final String DEF = "Definitions";
  private final String OBS = "Obsoletes";

  /** Singleton */
  private SearchParams() { //initialize Terms/Syns to true
	return;
  }
  
  public static SearchParams inst() {
    if (singleton == null)
      singleton = new SearchParams();
    return singleton;
  }

  public void setParam(String filter, boolean setting) {
	if (filter==TERM) { searchTerm = setting;}
	else if (filter==SYN) { searchSyn = setting; }
	else if (filter==DEF) { searchDef = setting; }
	else if (filter==OBS) { searchObs = setting; }
  }
  
  //this will takeover the single boolean fxns
  public boolean getParam(String filter)
  {  //need to add a catch or something
	if (filter==TERM) { return searchTerm;}
	else if (filter==SYN) { return searchSyn; }
	else if (filter==DEF) { return searchDef; }
	else if (filter==OBS) { return searchObs; }
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
  /** Whether to include obsoletes in searching terms, syns, & definitions
      This should be in conjunction with the other 3 */
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
