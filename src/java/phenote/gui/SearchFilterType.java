package phenote.gui;

/**
 * This enumerates all search filter types.
 */
public enum SearchFilterType {

  TERM("Term Name"), SYN("Synonyms"), DEF("Definitions"), OBS("Obsoletes"), XRF("DBxrefs");

  private String name;

  SearchFilterType(String s) {
    name = s;
  }

  public String getName() {
    return name;
  }


}
