package phenote.datamodel;

import org.geneontology.oboedit.datamodel.OBOClass;

/** CharacterIs are the building blocks of phenotypes. All the Characters for a 
    genotype make up a Phenotype - at least thats my understanding. */
public interface CharacterI {

  public String getEntityName();
  public String getQualityName(); // OBOClass?
  public String getGeneticContextName();

  public String getGenotype();
  public OBOClass getEntity();
  public OBOClass getQuality(); // OBOClass?
  public boolean hasGeneticContext();
  public OBOClass getGeneticContext();

  /** eventually have Genotype object? probably */
  public void setGenotype(String gt);
  public void setEntity(OBOClass e);
  public void setQuality(OBOClass p);
  public void setGeneticContext(OBOClass gc);
 
  public CharacterI cloneCharacter();
  public boolean equals(CharacterI c);
}
