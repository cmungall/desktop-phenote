package phenote.gui.field;
// completion package?

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.obo.datamodel.OBOClass;
import org.obo.datamodel.Synonym;

import phenote.gui.SearchParamsI;

/** This is basically a view object for the auto completer for terms/OBOClass
 this has been replaced by CompletionObject - i dont think its used anymore?
and should probably be deleted */

public class CompletionTerm {
  private OBOClass term;
  private boolean termMatch = false;
  private boolean isSynMatch = false;
  private String  synMatchString;
  private boolean definitionMatch = false;
  private boolean exactMatch = false;
  private boolean startsWith = false;
  private boolean contains = false; // do we need this?
  // These attributes are needed for the web ajax call
  // because we do not keep a handle to the oboClass for the termInfo.
  private String ontol;
  // Need to track the active gui field for use term in termInfo
  private String field;
  /** A term can have more than one synonym match, all matches get their owm
      comp term and are stored in matchList */
  private List<CompletionTerm> matchList;

  CompletionTerm(OBOClass term) {
    this.term = term;
  }

  OBOClass getOboClass() { return term; }

  boolean isTermMatch() { return termMatch; }
  boolean isSynMatch() { return isSynMatch; }
  boolean isDefinitionMatch() { return definitionMatch; }

  boolean isExactMatch() { return exactMatch; }
  boolean isStartsWithMatch() { return startsWith; }

  void resetMatchState() {
    termMatch = false;
    isSynMatch = false;
    definitionMatch = false;
    exactMatch = false;
    startsWith = false;
    contains = false;
  }

  public String toString() {
    return getCompListDisplayString();
  }


  public String getCompListInformalString() {
	StringBuffer informal = new StringBuffer();
   
	if (isSynMatch()) {
	  informal.append(" [syn: " + synMatchString + "]");  	  
	}
	
	if (term.isObsolete()) { 
	  informal.append(" [obs]");		  
	} 

	return informal.toString();
  }
	
	
  /** used by servlet.PhenoteController as well as CompTermList */
  public String getCompListDisplayString() {
    final StringBuffer display = new StringBuffer(this.getCompListDisplayName());
    final String appends = this.getCompListDisplaySuffix();
    final int allowedLength = 61 - appends.length(); // keep room for appends
    if (display.length() > allowedLength) {
      display.setLength(allowedLength-2); // -2 for ... ???
      display.append("...");
    }
    display.append(appends);
    return display.toString();
  }
  
  String getCompListDisplayName() {
    return (this.isSynMatch()) ? this.synMatchString : this.getName(); 
  }
  
  String getCompListDisplaySuffix() {
    final StringBuffer appends = new StringBuffer();
    if (this.isSynMatch()) appends.append("[syn]");
    if (this.isDefinitionMatch()) appends.append("[def]");
    if (this.term.isObsolete()) appends.append("[obs]");
    return appends.toString();
  }

  public String getID() { return term.getID(); }
  public String getName() { return term.getName(); }
  public String getEscapedName() { return term.getName().replaceAll("\\'","\\\\'"); }
	
  private Set<Synonym> getSyns() { return term.getSynonyms(); }
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

  /** more than one syn is matching, make new comp term for it */
  private void addMatch(String synonym) {

  }

  private void addMatch(CompletionTerm term) {
    // size 1 because majority will only have 1 match, 2?
    if (matchList==null) matchList = new ArrayList<CompletionTerm>(1);
    matchList.add(term);
  }
  
  List<CompletionTerm> getMatchList() {
    return matchList;
  }

  private boolean isBlank(String s) { return s == null || s.equals(""); }

  private boolean stringMatches(String input, String item) {
    // if configged for adding terms on blanks??
    if (isBlank(input)) return true;
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

  public String getOntol() {
    return ontol;
  }

  public void setOntol(String ontol) {
    this.ontol = ontol;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }
}
