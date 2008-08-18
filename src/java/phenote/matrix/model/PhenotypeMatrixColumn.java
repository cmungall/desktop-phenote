package phenote.matrix.model;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharacterI;
import phenote.datamodel.OboUtil;

public class PhenotypeMatrixColumn implements MatrixColumn {

  private OBOClass entity;
  private OBOClass quality;
  private OBOClass entity2;
  
  /** 
   * Creates a new PhenotypeMatrixColumn object with the given entity, quality, and entity2 OBOClass objects
   * 
   * @param entity the entity to use for this matrix column
   * @param quality the quality to use for this matrix column
   * @param entity2 the additional entity to use for this matrix column
   */
  public PhenotypeMatrixColumn(OBOClass entity, OBOClass quality, OBOClass entity2) {
    this.setEntity(entity);
    this.setQuality(quality);
    this.setEntity2(entity2);
  }
  
  /** 
   * Determines whether or not a given character contains the same values as the column
   * 
   * @param character the character to be tested
   * @return a Boolean value indicating the validity of the Character as a column value
   */
  public boolean isValue(CharacterI character) {
    if (!this.areEqualOrNull(this.getEntity(), character.getEntity())) {
      return false;
    }
    try {
      if (!this.areEqualOrNull(this.getEntity2(), character.getTerm("E2"))) {
        return false;
      }
    } catch (CharFieldException e) {
      log().error("Entity2 field is not properly configured", e);
      return false;
    }
    try {
      if (!this.areEqualOrNull(this.getQuality(), OboUtil.getAttributeForValue(character.getQuality()))) {
        return false;
      }      
    } catch (NullPointerException e) {
      return false;
    }
    return true;
  }
  
  /**
   * @param character the character to be used as the basis of the matrix cell
   * @return the given character as a PhenotypeMatrixCell object
   */
  public Object getValue(CharacterI character) {
    return new PhenotypeMatrixCell (character);
  }
  
  /** 
   * Compares two objects for equality, and also returns true if both objects are null.
   * 
   * @param o1 one of the Objects to be compared
   * @param o2 one of the Objects to be compared
   * @return a Boolean value indicating whether or not the two objects are equivalent
   */
  private boolean areEqualOrNull(Object o1, Object o2) {
    if ((o1 == null) && (o2 == null)) {
      return true;
    } else if ((o1 == null) || (o2 == null)) {
      return false;
    }
    return o1.equals(o2);
  }
  
  /**
   * @param entity the entity (as an OBOClass object) to be used for this column
   */
  public void setEntity(OBOClass entity) {
    this.entity = entity;
  }

  /**
   * @return the entity (as an OBOClass object) for this column
   */
  public OBOClass getEntity() {
    return entity;
  }

  /**
   * @param quality the quality (as an OBOClass object) to be used for this column
   */
  public void setQuality(OBOClass quality) {
    this.quality = quality;
  }

  /**
   * @return the quality (as an OBOClass object) for this column
   */
  public OBOClass getQuality() {
    return quality;
  }

  /**
   * @param entity2 the additional entity (as an OBOClass object) to be used for this column
   */
  public void setEntity2(OBOClass entity2) {
    this.entity2 = entity2;
  }

  /**
   * @return the additional entity (as an OBOClass object) for this column
   */
  public OBOClass getEntity2() {
    return entity2;
  }
  
  /**
   * @return an int value representing the hash code computation for the PhenotypeMatrixColumn
   */
  public int hashCode() {
    int entityHash, qualityHash, entity2Hash;
    entityHash = entity != null ? entity.hashCode() : 0;
    qualityHash = quality != null ? quality.hashCode() : 0;
    entity2Hash = entity2 != null ? entity2.hashCode() : 0;
    return entityHash ^ qualityHash ^ entity2Hash;
  }
  
  /**
   * @param o an Object to be compared to the current PhenotypeMatrixColumn object
   * @return the Boolean value indicating whether or not the Object is equivalent to the PhenotypeMatrixColumn
   */
  public boolean equals(Object o) {
    if (o instanceof PhenotypeMatrixColumn) {
      PhenotypeMatrixColumn pmc = (PhenotypeMatrixColumn)o;
      return areEqualOrNull(entity, pmc.getEntity()) && areEqualOrNull(quality, pmc.getQuality()) && areEqualOrNull(entity2, pmc.getEntity2());
    } else {
      return false;
    }
  } 

  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
}