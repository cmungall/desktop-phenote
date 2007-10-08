package phenote.datamodel;


import org.geneontology.oboedit.datamodel.OBOProperty;
import org.geneontology.oboedit.datamodel.impl.OBOPropertyImpl;


public class PhenotypeMapping extends BasicAnnotationMappingDriver {

  /** inheres_in is not in RO which is surprising - but its in RO proposed */
  private final OBOProperty INHERES_IN_REL =
  getRelation("OBO_REL:inheres_in","inheres_in");
  private final OBOProperty INFLUENCES_REL = getRelation("OBOL:influences","influences");
  private final OBOProperty TOWARDS_REL = getRelation("OBOL:towards","towards");
  

  public OBOProperty getPropertyForField(CharField cf) {
    if (CharFieldEnum.ENTITY.equals(cf))
      return INHERES_IN_REL;
    if (CharFieldEnum.ENTITY2.equals(cf))
      return TOWARDS_REL;
    return super.getPropertyForField(cf); // pub, genotype
  }  

  /** this is the relationship between subject & object should be renamed
      subjectObjectRelationship */
  public OBOProperty getDefaultRelationship() {
    return INFLUENCES_REL; // genotype influences phenotype
  }

  // there should probably be some check that the genotype field exists in 
  // present configuration - in fact the config should specify a subject field
  public boolean isSubjectField(CharField cf) {
    return CharFieldEnum.GENOTYPE.equals(cf);
  }

  public boolean isObjectGenusField(CharField cf) {
    return CharFieldEnum.QUALITY.equals(cf);
  }

  /** both entity and entity2 are object differentia fields
   entity is inheres_in, E2 is towards?? */
  public boolean isObjectDifferentiaField(CharField cf) {
    return CharFieldEnum.ENTITY.equals(cf) || CharFieldEnum.ENTITY2.equals(cf);
  }

}
