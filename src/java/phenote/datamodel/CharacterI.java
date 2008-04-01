package phenote.datamodel;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.List;

import org.obo.annotation.datamodel.Annotation;
import org.obo.datamodel.OBOClass;

/** CharacterIs are the building blocks of phenotypes. All the Characters for a 
    genotype make up a Phenotype - at least thats my understanding. 
    an alternate approach would be to just have a list of CharFields, it would
    certainly be more generic - hybrid approach would be to retain these fields
    but also have a CharField list that can just come from config (& take out enum
    in config xsd for field names) - downside: lose strong typing, and a config 
    misspelling would end up in a char field
    For drag&drop Character extends Transferable */
public interface CharacterI extends Transferable {

  public static DataFlavor CHAR_FLAVOR = new DataFlavor(CharacterI.class,"Character");

  /** generic fields!!! */
  public List<CharField> getAllCharFields();
  /** Looks up CharField with fieldString using both field name & datatag,
      if CharField isnt found throws CharFieldException.
      Then creates CharFieldValue using valueString. If field is TERM then
      valueString is ID and is looked up, if not found then creates dangler.
      danglers actually make TermNotFoundEx irrelevant (take out?). 
      CFV.isDangler() will indicate if you got a dangler */
  public CharFieldValue setValue(String fieldString, String valueString)
    throws CharFieldException,TermNotFoundException;
  public void setValue(CharField cf, CharFieldValue cfv); // ??
  /** @returns char field value created
      if cf is a term will look for OBOClass with ID in valueString, if ID not 
      found will create dangler which actually makes TermNotFoundEx irrelevant
      take out? or do we want a non-dangler mode?? */
  public CharFieldValue setValue(CharField cf, String valueString) throws CharFieldException;
  /** if term field, string should be id, obo class will be searched for, if class
      not found then dangler is created. if free text field just uses string of
      course. the dangler makes termNotFound Ex irrelevant - take out? or will there
      be a no dangler mode? probably not right?
      CharFieldEx thrown if improper date for date field
      if cf is a term, can optionally pass in non null danglerName, if term ends up
      being dangler (not found in ontology) will set dangler id to s and dangler name
      to danglerName */
  public CharFieldValue setValue(CharField cf, String s,String danglerName)
    throws CharFieldException;
  public CharField getCharFieldForName(String fieldName) throws CharFieldException;
  public CharFieldValue getValue(CharField cf);
  /** im wondering if a better way is to make CFVs recursive, so a CFV can be a list of
      CFV's - that might be easier - not sure */
  public List<CharFieldValue> getValueList(CharField cf);
  public boolean fieldEquals(CharacterI c, CharField cf);
  /** used in particular for lists of values */
  //public void deleteValue(CharField cf, CharFieldValue cfv);
  public String getValueString(CharField cf);

  public String getValueString(String fieldName) throws CharFieldException;
  public OBOClass getTerm(String fieldName) throws CharFieldException;
  // public void setTerm(String field, OBOClass term);
  // needed for protege plugin
  // public void setTerm(String field, String id) throws OboException;
  public boolean hasValue(CharField cf);
  public boolean hasValue(String fieldName);//throws CharFieldException?
  
  /** Returns true if there is auto generated annot ids for character/config */
  public boolean hasAnnotId();
  /** Returns auto generated annot id for character, returns null if dont have one */
  public String getAnnotId();

  public CharacterI cloneCharacter();
  public boolean equals(CharacterI c);
  public boolean hasNoContent();

  /** Returns true if in fact charI has OboAnnotation under the hood, and thus
      getOboAnnotation will have non null return. Char returns false */
  public boolean hasOboAnnotation();

  /** if hasOboAnnotation() this returns non null Annotation from obo model */
  public Annotation getOboAnnotation();
  
  /** Returns true if charI implementation supports comparisons, eg Character doesnt
      AnnotChar does, this can return true even if hasComparison is false */
  public boolean supportsComparisons();
  /** Returns true if character is a SUBJECT of a comparison, NOT object
      in otherwords returns false if getComparison is null */
  public boolean hasComparison();
  /** Return Comparisons if char is subject of comparisons, not object.
      returns null otherwise */
  public List<Comparison> getComparisons();
  /** CharFieldValue may hold a list of values, with char as subject */
  public List<CharFieldValue> getComparisonValueKidList();

  /** make comparison statement using relation to relatedChar. throw exception
      if comparisons are not supported (Character doesnt support) */
  //public void addComparison(OBOProperty r,CharacterI c)throws CharacterEx;

  // public void setDBString(String? Object?)??
  // public String getDBString()

  
  /** these methods are pase and need to be phased out! */
  public String getPub();
  public boolean hasPub();
  public String getGenotype();
  public OBOClass getEntity();
  public OBOClass getQuality();
  public boolean hasGeneticContext();
  public OBOClass getGeneticContext();

  /** eventually have Genotype object? probably */
  public void setPub(String p);
  public void setGenotype(String gt);
  public void setEntity(OBOClass e);
  public void setQuality(OBOClass p);
  public void setGeneticContext(OBOClass gc);
}

