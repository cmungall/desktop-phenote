package phenote.datamodel;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.impl.DanglingClassImpl;

import phenote.config.FieldConfig;
import phenote.config.xml.FieldDocument.Field;
// type is from xmlbean Field
import phenote.config.xml.FieldDocument.Field.Type;
import phenote.error.ErrorEvent;
import phenote.error.ErrorManager;

// or just Field? or CharField?
// CharField doesnt handle instance data, just specifies what ontologies are 
// associated with what parts of the generic character
// CharFieldValue handles instance data
// CharField gets specified in the configuration - but oddly enough it better
// no contradit CharacterI - as in OBOClasses better have ontologies - this is
// funny i think
public class CharField {

  private static boolean DO_DANGLERS = true; // for testing
  static final String LIST_DELIM = ","; // no space after comma?

  private List<Ontology> ontologyList = new ArrayList<Ontology>(3);
  private CharFieldEnum charFieldEnum; // or subclass
  private String name; // display name
  private String tag; // non display tag to refer to field (no spaces...)
  private boolean postCompAllowed=false;
  /** Whether to copy this field on copy/clone/duplicating */
  private boolean copies = true;
  private Ontology postCompRelOntol;
  // RELATIONSHIP? DANGLER? INSTANCE? use CharFieldEnum?
  // should we actually just use xmlbean
  // phenote.config.xml.FieldDocument.Field.Type???
  //public enum Type { TERM, FREE_TEXT, INT, ID }; 
  private Field fieldXmlBean;
  private FieldConfig fieldConfig;
  private Type.Enum type = Type.FREE_TEXT; // free text default, bkwrd compat
  // index? orderNumber? for order in gui/datamodel?

  /** used for relationship */
  public CharField(CharFieldEnum c) {
    charFieldEnum = c;
    name = c.getName();
    tag = c.getTag(); // name if not set
    type = c.getType(); // may be null
  }

  /** a generic field with no char field enum - get hip
   type is from field xml bean: TERM, FREE_TEXT...*/
  public CharField(String name,String tag,Type.Enum type) {
    this.name = name;
    this.tag = tag;
    if (type != null) this.type = type;
  }

  /** heck why not just pass in the FieldConfig with its xml config bean */
  public CharField(FieldConfig fc) {
    if (fc == null) return; // shouldnt happen
    fieldConfig = fc;
    fieldXmlBean = fieldConfig.getFieldBean();
    name = fieldConfig.getLabel();
    tag = fieldConfig.getDataTag();
    type = fc.getType();
    setCopyEnabled(fc.copies());
  }

  public void setCopyEnabled(boolean cp) {
    copies = cp;
  }

  public boolean getCopyEnabled() { return copies; }

  /** part of constructor? probably */
  public void setType(Type.Enum t) {
    this.type = t;
  }

  public void addOntology(Ontology o) {
    ontologyList.add(o);
    // ??? if you have ontologies you better be a Term
    type = Type.TERM;
  }

  public void setName(String n) {
    name = n;
  }

  public String getName() {
    if (name == null) { // not explicitly set
      if (hasOneOntology()) {
        name =  getOntology().getName();
      }
      else
        name = charFieldEnum.toString();
    }
    return name;
  }

  /** Tag is what comes from datatag in config, used for referencing field
      (like from smart atlas servlet), and as dangling prefix for obo 
      mapping, if datatag not set in config, returns name */
  public String getTag() {
    if (tag == null) return getName();
    return tag;
  }

  /** returns true if field has has tag or name of input tag */
  public boolean isField(String tag) {
    if (tag.equalsIgnoreCase(this.tag)) return true;
    if (tag.equalsIgnoreCase(name)) return true;
    // check with _ substition for spaces?? shouldnt have to
    return false;
  }

  public CharFieldEnum getCharFieldEnum() { return charFieldEnum; }

  /** generic fields dont have enums */
  boolean hasCharFieldEnum() { return charFieldEnum != null; }

//   public boolean isRelationship() {
//     return charFieldEnum == CharFieldEnum.RELATIONSHIP;
//   }

  //boolean isGeneticContext() { return charFieldEnum == CharFieldEnum.GENETIC_CONTEXT; }

  public List<Ontology> getOntologyList() { return ontologyList; }

  //booelan isFreeText() return !hasOntologies() ??
  public boolean isFreeText() {		//  now have third type isInt, so can't just negate 2007 07 09
    //if ( hasOntologies() || isInt() )  return false; 
    //return true; 
    return type == Type.FREE_TEXT;
  }

  public boolean isInt() {		//  now have third type isInt, so can't just negate 2007 07 09
    //if ( hasOntologies() || isFreeText() )  return false; 
    //return true; 
    return type == Type.INT;
  }

  /** This should be redundant with hasOntologies (though with instances this
      may change?) when ontology is added type is automatically set to TERM */
  public boolean isTerm() {
    return type == Type.TERM;
  }

  public boolean isDate() { return type == Type.DATE; }

  public boolean isID() { return type == Type.ID; }

  public boolean isReadOnly() { 
    return type == Type.READ_ONLY; 
  }

  /** return true if holds a list of CharFieldValues, false if just 1 char field
      value */
  public boolean isList() {
    //return getTag().equals("NLA"); // || getTag().equals("GC"); // just testing...
    if (fieldXmlBean == null) return false;
    if (fieldXmlBean.xgetIsList() == null) return false;
    return fieldXmlBean.getIsList();
    //return false;
  }

  public boolean hasOntologies() {
    return ontologyList != null && !ontologyList.isEmpty();
  }
  public boolean hasOneOntology() {
    return hasOntologies() && getOntologySize() == 1;
  }
  public boolean hasMoreThanOneOntology() {
    return hasOntologies() && !hasOneOntology(); 
  }
  
  public Ontology getOntology() {
    if (!hasOntologies()) return null;
    return getFirstOntology();
  }
  public Ontology getFirstOntology() {
    if (ontologyList.isEmpty()) return null; // ex?
    return ontologyList.get(0);
  }

  private int getOntologySize() {
    if (!hasOntologies()) return 0;
    return ontologyList.size();
  }

  public boolean hasOntology(String ontologyName) {
    return getOntologyForName(ontologyName) != null; 
  }

  /** Returns Ontology with name ontologyName (ignores case), ont ex if dont have it */
  public Ontology getOntologyForName(String ontologyName){
    for (Ontology o : getOntologyList()) {
      if (o.getName().equalsIgnoreCase(ontologyName))
        return o;
    }
    return null;
  }

  // set whether post composition allowed (from config) */
  public void setPostCompAllowed(boolean pca) {
    postCompAllowed = pca;
  }

  /** whether this field allows for post composition - from config - allowed? how bout has? */
  public boolean postCompAllowed() {
    return postCompAllowed;
  }

  public void setPostCompRelOntol(Ontology o) {
    postCompRelOntol = o;
  }

  public Ontology getPostCompRelOntol() { return postCompRelOntol; }

  /** if free text returns string charfieldValue, for ontology field valueString is
      id and searches ontologies for id, throws CharFieldException if invalid date for
      date field (used to for term not found but now creates dangler instead)
      this deals with postcomp too)
      if term is dangler then dangler gets id (valueString) but no name
      use makeValue(Char,String,String) to get dangler name set as well
      also deals with lists in format: "xxx","yyy",...
  */
  public CharFieldValue makeValue(CharacterI c, String valueString)
    throws CharFieldException {
    return makeValue(c,valueString,null,true);
  }

  /** makes value - value string for term is id - if term is dangler (not in ontology)
      and danglerName is non null, then danglers name is set to danglerName,
      danglerName is ONLY used for danglers */
  public CharFieldValue makeValue(CharacterI c, String valueString, String danglerName) 
    throws CharFieldException {
    return makeValue(c,valueString,danglerName,true);
  }

  /** if free text returns string charfieldValue, for ontology field valueString is
      id and searches ontologies for id, throws CharFieldException if invalid date for
      date field (used to for term not found but now creates dangler instead)
      this deals with postcomp too
      if doList is true and char field is a list then parse & add kids,
      if false then dont -> making kid not parent, kid char field has isList = true
      not sure how else to do that?
  */
  private CharFieldValue makeValue(CharacterI c, String valueString,
                                   String danglerName, boolean doList)
    throws CharFieldException {

    // empty value
    if (valueString == null || valueString.trim().equals(""))
      return CharFieldValue.emptyValue(c,this);

    if (isList() && doList) {
      return parseList(valueString,danglerName,c);
    }

    // TERM
    if (isTerm()) {
      OBOClass oboClass=null;

      // POST COMP - move this to char field value?
      if (CharFieldManager.inst().isPostComp(valueString)) {
        try {
          // just gets first ontology for now - fix this!
          OBOSession os = getOntology().getOboSession();
          oboClass = CharFieldManager.inst().getPostComp(os,valueString);
        }
        catch (TermNotFoundException e) {}
      }
      

      // TERM regular, not post comp
      else {
        for (Ontology ont : ontologyList) {
          try { oboClass = ont.getTerm(valueString); }
          catch (TermNotFoundException e) {} // move on to next ontology
        }
      }
      
      // DANGLER if cant find it make a dangler!
      if (oboClass == null && danglerMode()) {
        oboClass =  new DanglingClassImpl(valueString);
        if (danglerName != null) oboClass.setName(danglerName);
        error(valueString+" not found in loaded obo files. Creating a 'dangler'");
      }

      if (oboClass != null)
        return new CharFieldValue(oboClass,c,this);
      // if danglers are permanent this should be tossed as makes irrelevant
      else {
        TermNotFoundException e =
          new TermNotFoundException(valueString+" not found in ontologies for "+this);
        throw new CharFieldException(e);
      }
    }

    // DATE
    else if (isDate()) {
      try { return CharFieldValue.makeDate(valueString,c,this); }
      catch (ParseException e) {
        throw new CharFieldException(e);
      }
    }

    // FREE TEXT FIELD
    //if (!hasOntologies()) // -> !isTerm() or isFreeText()
    else
      return new CharFieldValue(valueString,c,this);
  }

  private void error(String errMsg) {
    // this should be changed to just do log.error and have log appender to error
    // window i think
    ErrorManager.inst().error(new ErrorEvent(this,errMsg));
  }

  /** parse listString - should be in format "xxx","yyy",...
      if danglerNameList non null use it to set names of dangling terms 
      should be in same format */
  private CharFieldValue parseList(String listString,String danglerNameList,CharacterI c)
    throws CharFieldException {
    // strip quotes
    listString = stripQuotes(listString);
    String delim = '"'+LIST_DELIM+'"'; // quotes separate each item
    Pattern p = Pattern.compile(delim);
    String[] kids = p.split(listString);
    danglerNameList = stripQuotes(danglerNameList);
    String[] kidDanglerNames = danglerNameList!=null ? p.split(danglerNameList) : null;
    CharFieldValue listVal = CharFieldValue.makeListParentValue(c,this);
    for (int i=0; i<kids.length; i++) {
      boolean doList = false;
      String dngl =
        (kidDanglerNames!=null && i < kidDanglerNames.length) ? kidDanglerNames[i] : null;
      CharFieldValue kid = makeValue(c,kids[i],dngl,doList); //tws Ex
      listVal.addKid(kid);
    }
    return listVal;
  }

  private static String stripQuotes(String s) {
    s = s.trim(); // just in case
    if (s.startsWith("\"")) s = s.substring(1);
    if (s.endsWith("\"")) s = s.substring(0,s.length()-1);
    return s;
  }

  // i think we eventually will want this always to be so
  private boolean danglerMode() { return true; }

  public String toString() { return "CharField: "+getName(); }
}

