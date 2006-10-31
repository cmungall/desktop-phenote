package phenote.gui.field;
// completion package?

import java.util.Set;

import org.geneontology.oboedit.datamodel.OBOClass;

/** This is basically a view object for the auto completer for terms/OBOClass */

public class CompletionTerm {
  private OBOClass term;
  private boolean termMatch = false;
  private boolean isSynMatch = false;
  private String  synMatchString;
  private boolean definitionMatch = false;
  private boolean exactMatch = false;
  private boolean startsWith = false;
  private boolean contains = false; // do we need this?

  CompletionTerm(OBOClass term) {
    this.term = term;
  }

  OBOClass getOboClass() { return term; }

  boolean isTermMatch() { return termMatch; }
  boolean isSynMatch() { return isSynMatch; }
  boolean isDefinitionMatch() { return definitionMatch; }

  boolean isExactMatch() { return exactMatch; }
  boolean isStartsWithMatch() { return startsWith; }

  public String toString() {
    return getCompListDisplayString();
  }

  public String getCompListDisplayString() {
    StringBuffer display = new StringBuffer();
    StringBuffer appends = new StringBuffer();
    if (isSynMatch()) {
      display.append(synMatchString);
      appends.append("[syn]");
    }
    else {
      display.append(getName());
    }
    if (isDefinitionMatch())
      appends.append("[def]");
    if (term.isObsolete())
      appends.append("[obs]");
    // font metrics? fixed font? query length of Text gui?
    // if in standalone mode should do fontmetrics
    int allowedLength = 61 - appends.length(); // keep room for appends
    if (display.length() > allowedLength) {
      display.setLength(allowedLength-2); // -2 for ... ???
      display.append("...");
    }
      //display = display.substring(0,allowedLength - 3); // -3 for ...
    display.append(appends);
    return display.toString();
  }

  public String getID() { return term.getID(); }
  public String getName() { return term.getName(); }
  private Set getSyns() { return term.getSynonyms(); }
  private boolean hasDefinition() {
    return getDefinition() != null && !getDefinition().equals("");
  }
  private String getDefinition() { return term.getDefinition(); }
  boolean isObsolete() { return term.isObsolete(); }

  /** Returns true if term matches input according to searchParams. has
      side effect of recording the kind of match - term, syn, desc... */
  boolean matches(String input, SearchParamsI searchParams) {
    // TERMS
    if (searchParams.searchTerms()) {
      if (stringMatches(input,getName())) {
        termMatch = true; // is this needed?
        return true;
      }
    }
    // SYNS
    if (searchParams.searchSynonyms()) {
      for (Object o : getSyns()) {
        String syn = o.toString();
        if (stringMatches(input,syn)) {
          isSynMatch = true;
          synMatchString = syn;
          return true;
        }
      }
    }
    // DEFS
    if (searchParams.searchDefinitions() && hasDefinition()) {
      if (stringMatches(input,getDefinition())) {
        definitionMatch = true;
        return true;
      }
    }
    return false;
  }

  private boolean stringMatches(String input, String item) {
    input = input.toLowerCase().trim();
    item = item.toLowerCase().trim();
    if (input.equals(item)) {
      exactMatch = true;
      return true;
    }
    else if (item.startsWith(input)) {
      startsWith = true;
      return true;
    }
    else if (item.contains(input)) {
      contains = true;
      return true;
    }
    return false; // all above failed - no match
  }

}
