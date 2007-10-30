package phenote.gui.field;
// completion package?

import org.obo.datamodel.OBOProperty;

/** This is basically a view object for the auto completer for relations/
    OBOProperties */

class CompletionRelation {
  OBOProperty relation;
  CompletionRelation(OBOProperty rel) {
    relation = rel;
  }
  
  OBOProperty getOboProperty() { return relation; }

  public String toString() { return relation.getName(); }

}
