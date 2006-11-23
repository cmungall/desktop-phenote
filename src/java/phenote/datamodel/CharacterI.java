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

  public String getEntityName();
  public String getQualityName(); // OBOClass?
  public String getGeneticContextName();

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
 

  /** generic fields!!! */
  public void setValue(CharField cf, CharFieldValue cfv); // ??
  public CharFieldValue getValue(CharField cf);
  // should make an exception for this
  public String getValueString(String fieldName) throws Exception; // yuck
  public OBOClass getTerm(String field) throws Exception;
  public boolean hasValue(CharField cf);
  public boolean hasValue(String fieldName) throws Exception;
  
  public CharacterI cloneCharacter();
  public boolean equals(CharacterI c);
  public boolean hasNoContent();
}
