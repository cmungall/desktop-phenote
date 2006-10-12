package phenote.gui.field;
// completion package?

import org.geneontology.oboedit.datamodel.OBOClass;

/** This is basically a view object for the auto completer for terms/OBOClass */

public class CompletionTerm {
  OBOClass term;
  CompletionTerm(OBOClass term) {
    this.term = term;
  }

  OBOClass getOboClass() { return term; }

  public String toString() {
    return compListDisplayString();
  }

  private String compListDisplayString() {
    StringBuffer s = new StringBuffer(term.getName());
    if (term.isObsolete())
      s.append("[obs]");
    return s.toString();
  }

  public String getID() { return term.getID(); }
  public String getName() { return term.getName(); }

}
