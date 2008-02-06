package phenote.gui.field;
// completion package?

import org.obo.datamodel.OBOObject;
import org.obo.datamodel.OBOProperty;

/** This is basically a view object for the auto completer - right now used for relations
 OBOProperty - but terms should also go in here as OBOObject has both OBOClass and
OBOProperty as subclasses - thats the idea at least - simplify! */

class CompletionObject {
  OBOObject obj;

  CompletionObject(OBOObject obj) {
    this.obj = obj;
  }

  /** this is way of future - use more general OBOObj instead of OBOProp */
  //CompletionRelation(OBOObject obj) {  }

  OBOObject getOboObject() { return obj; }

  public String toString() { return obj.getName(); }

  public boolean hasOboProperty() {
    return getOboObject() instanceof OBOProperty;
  }

  /** returns null if not an obo property - exception? */
  public OBOProperty getOboProperty() {
    if (!hasOboProperty()) return null;
    return (OBOProperty)getOboObject();
  }
}
