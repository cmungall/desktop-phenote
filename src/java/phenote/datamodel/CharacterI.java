package phenote.datamodel;

import java.util.List;
import org.obo.datamodel.OBOClass;

/** CharacterIs are the building blocks of phenotypes. All the Characters for a 
    genotype make up a Phenotype - at least thats my understanding. 
    an alternate approach would be to just have a list of CharFields, it would
    certainly be more generic - hybrid approach would be to retain these fields
    but also have a CharField list that can just come from config (& take out enum
    in config xsd for field names) - downside: lose strong typing, and a config 
    misspelling would end up in a char field */
public interface CharacterI {

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
  public CharField getCharFieldForName(String fieldName) throws CharFieldException;
  public CharFieldValue getValue(CharField cf);
  public List<CharFieldValue> getValueList(CharField cf);
  /** used in particular for lists of values */
  public void deleteValue(CharField cf, CharFieldValue cfv);
  public String getValueString(CharField cf);

  public String getValueString(String fieldName) throws CharFieldException;
  public OBOClass getTerm(String fieldName) throws CharFieldException;
  // public void setTerm(String field, OBOClass term);
  // needed for protege plugin
  // public void setTerm(String field, String id) throws OboException;
  public boolean hasValue(CharField cf);
  public boolean hasValue(String fieldName);//throws CharFieldException?
  
  public CharacterI cloneCharacter();
  public boolean equals(CharacterI c);
  public boolean hasNoContent();

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

