package phenote.datamodel;

import org.geneontology.oboedit.datamodel.OBOClass;

/** Characters are the EAV building blocks of a Phenotype. Previously this
    was called a Phenotype which was a misnomer */
public class Character implements CharacterI, Cloneable {

//   private String entity="";
//   private String quality="";
//   private String geneticContext="";
  private String pub;
  private String genotype=""; // eventually Genotype class
  // OboClass? OntologyTerm?...
  private OBOClass entity; // CharFieldValue?
  private OBOClass quality;
  private OBOClass geneticContext;

  public String getPub() { return pub; }
  public boolean hasPub() { return pub!=null && !pub.equals(""); }
  public String getGenotype() { return genotype; }
  public OBOClass getEntity() { return entity; }
  public OBOClass getQuality() { return quality; }
  public boolean hasGeneticContext() {
    return geneticContext!=null && !geneticContext.equals("");
  }
  public OBOClass getGeneticContext() { return geneticContext; }

  // convenience functions
  public String getEntityName() { return entity.getName(); }
  public String getQualityName() { return quality.getName(); }
  public String getGeneticContextName() { return geneticContext.getName(); }


  public void setPub(String p ) { pub = p; }
  public void setGenotype(String gt) { genotype = gt; }
  public void setEntity(OBOClass e) { entity = e; }
  public void setQuality(OBOClass p) { quality = p; }
  public void setGeneticContext(OBOClass gc) { geneticContext = gc; }
 
  public boolean equals(CharacterI ch) {
    return eq(genotype,ch.getGenotype()) && eq(entity,ch.getEntity())
      && eq(quality,ch.getQuality()) && eq(geneticContext,ch.getGeneticContext());
  }

  /** check if both are null in addition to .equals() */
  private boolean eq(Object o1, Object o2) {
    if (o1==null && o2==null) return true;
    if (o1 == null) return false;
    return o1.equals(o2);
  }

  public CharacterI cloneCharacter() {
    try {
      // do OBOClasses clone? do we need to clone them?
      return (Character)clone();
    } catch (CloneNotSupportedException e) { return null; }
  }
 
}
