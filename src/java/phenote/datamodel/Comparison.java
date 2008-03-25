package phenote.datamodel;

import org.obo.datamodel.OBOProperty;

/** for making relations between statements */

public class Comparison {
  private CharacterI subject;
  private OBOProperty relation;
  private CharacterI object;
  public Comparison() {}
  Comparison(CharacterI sub, OBOProperty r,CharacterI obj) {
    subject = sub;
    relation = r;
    object = obj;
  }
  public void setSubject(CharacterI s) { subject = s; }
  public CharacterI getSubject() { return subject; }
  public boolean hasSubject() { return subject!=null; }
  public void setRelation(OBOProperty r) { relation = r; }
  public OBOProperty getRelation() { return relation; }
  public void setObject(CharacterI o) { object = o; }
  public CharacterI getObject() { return object; }

//   public boolean hasValue() { return getValues() != null; }
//   /** convenience method for getting CharFieldValue of comparison, if in datamodel
//       if not yet in datamodel returns null(?)
//       The datamodel stores the charFieldValue in the subject (not object)
//       CharFieldValue holds a list of CharFieldValue kid comparisons */
//   public List<CharFieldValue> getValues() {
//     if (!hasSubject()) return null;
//     return getSubject().getComparisonValueKidList();
//   }
  
  /** only show rel with object?? assumes being displayed in subject
      silly? another method displayRelativeToObject? ForObject? */
  public String toString() {
    String rel = relation!=null ? relation.getName() : ""; 
    // return ^() or "" if all are null?
    return charStr(subject)+"^"+rel+"("+charStr(object)+")";
  }

  private String charStr(CharacterI c) {
    if (c == null) return "";
    if (c.hasAnnotId()) return c.getAnnotId();
    return c.toString(); // appending of all fields
  }

  public Comparison cloneComparison() {
    return new Comparison(subject,relation,object);
  }

}
