package phenote.gui.field;

// package phenote.ontology?? dataadapter?

public interface SearchParamsI {

  public boolean searchTerms();
  public boolean searchSynonyms();
  public boolean searchDefinitions();
  /** Whether to include obsoletes in searching terms, syns, & definitions
      This should be in conjunction with the other 3 */
  public boolean searchObsoletes();

}
