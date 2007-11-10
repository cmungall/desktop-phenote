package phenote.datamodel;


import org.obo.datamodel.OBOProperty;


public class DefaultMappingDriver extends BasicAnnotationMappingDriver {

  private final OBOProperty DEFAULT_REL = getRelation("OBOL:influences","influences");
  
  public OBOProperty getPropertyForField(CharField cf) {
     return super.getPropertyForField(cf); // pub, genotype
  }  

  /** this is the relationship between subject & object should be renamed
      subjectObjectRelationship */
  public OBOProperty getDefaultRelationship() {
    return DEFAULT_REL; // genotype influences phenotype
  }

  // there should probably be some check that the genotype field exists in 
  // present configuration - in fact the config should specify a subject field
  public boolean isSubjectField(CharField cf) {
    return cf.getName().equals("AnnotatedEntity");
  }

  public boolean isObjectGenusField(CharField cf) {
	    return cf.getName().equals("Term");
 }

  /** both entity and entity2 are object differentia fields
   entity is inheres_in, E2 is towards?? */
  public boolean isObjectDifferentiaField(CharField cf) {
    return false;
  }

}
