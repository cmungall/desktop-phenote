package phenote.datamodel;

import org.geneontology.oboedit.datamodel.OBOClass;

/** CharacterIs are the building blocks of phenotypes. All the Characters for a 
    genotype make up a Phenotype - at least thats my understanding. 
    an alternate approach would be to just have a list of CharFields, it would
    certainly be more generic - hybrid approach would be to retain these fields
    but also have a CharField list that can just come from config (& take out enum
    in config xsd for field names) - downside: lose strong typing, and a config 
    misspelling would end up in a char field */
public interface CharacterI {

  /** generic fields!!! */
  public void setValue(String fieldString, String valueString)
    throws CharFieldException,TermNotFoundException;
  public void setValue(CharField cf, CharFieldValue cfv); // ??
  public void setValue(CharField cf, String valueString) throws TermNotFoundException;
  public CharField getCharFieldForName(String fieldName) throws CharFieldException;
  public CharFieldValue getValue(CharField cf);
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

