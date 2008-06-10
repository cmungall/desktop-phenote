package phenote.gui.field;
// completion package?

import java.util.Set;

import org.apache.log4j.Logger;

import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.Synonym;
import phenote.gui.SearchParamsI;

/** This is basically a view object for the auto completer - right now used for relations
 OBOProperty - but terms should also go in here as OBOObject has both OBOClass and
OBOProperty as subclasses - thats the idea at least - simplify! 
*/

class CompletionObject {
  private OBOObject obj;
  private boolean termMatch = false;
  private boolean isSynMatch = false;
  private String  synMatchString;
  private boolean definitionMatch = false;
  private boolean exactMatch = false;
  private boolean startsWith = false;
  private boolean contains = false; // do we need this?

  CompletionObject(OBOObject obj) {
    this.obj = obj;
  }

  /** this is way of future - use more general OBOObj instead of OBOProp */
  //CompletionRelation(OBOObject obj) {  }

  OBOObject getOboObject() { return obj; }

  public String toString() {
    return getCompListDisplayString();
  }

  private String getCompListDisplayString() {
    if (getCompListDisplayName()==null) {
      log().error("no display string for term match "+obj);
      return "";
    }
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

  private String getCompListDisplayName() {
    return (this.isSynMatch()) ? this.synMatchString : this.getName(); 
  }
  private String getCompListDisplaySuffix() {
    final StringBuffer appends = new StringBuffer();
    if (this.isSynMatch()) appends.append("[syn]");
    if (this.isDefinitionMatch()) appends.append("[def]");
    if (this.obj.isObsolete()) appends.append("[obs]");
    return appends.toString();
  }

  public boolean hasOboProperty() {
    return getOboObject() instanceof OBOProperty;
  }

  public String getName() { return obj.getName(); }
  boolean isObsolete() { return obj.isObsolete(); }

  boolean isTermMatch() { return termMatch; }
  boolean isSynMatch() { return isSynMatch; }
  boolean isDefinitionMatch() { return definitionMatch; }
  boolean isExactMatch() { return exactMatch; }
  boolean isStartsWithMatch() { return startsWith; }

  private Set<Synonym> getSyns() { return obj.getSynonyms(); }

  private String getDefinition() { return obj.getDefinition(); }
  private boolean hasDefinition() {
    return getDefinition() != null && !getDefinition().equals("");
  }

  boolean hasOboClass() { return getOboClass()!=null; }
  OBOClass getOboClass() {
    if (!(obj instanceof OBOClass)) return null; // ex?
    return (OBOClass)obj;
  }

  /** returns null if not an obo property - exception? */
  public OBOProperty getOboProperty() {
    if (!hasOboProperty()) return null;
    return (OBOProperty)getOboObject();
  }
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


  void resetMatchState() {
    termMatch = false;
    isSynMatch = false;
    definitionMatch = false;
    exactMatch = false;
    startsWith = false;
    contains = false;
  }

  private boolean isBlank(String s) { return s == null || s.equals(""); }

  private Logger log() {
    return Logger.getLogger(getClass());
  }

}
