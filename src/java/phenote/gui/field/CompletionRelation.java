package phenote.gui.field;
// completion package?

import org.obo.datamodel.OBOProperty;

/** This is basically a view object for the auto completer for relations/
    OBOProperties
    This is pase - been replaced by more general CompletionObject - delete? 
    CompletionTerm should similarly be replaced by/merged with CompObj i think 
    thats the idea - then can have general stuff that can do both rels & terms
    rather than this separation for it all - this is a first step */

class CompletionRelation {
  OBOProperty relation;

  CompletionRelation(OBOProperty rel) {
    relation = rel;
  }

  /** this is way of future - use more general OBOObj instead of OBOProp */
  //CompletionRelation(OBOObject obj) {  }

  OBOProperty getOboProperty() { return relation; }

  public String toString() { return relation.getName(); }

}
