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
  
  public PhenotypeMatrixColumn(OBOClass entity, OBOClass quality, OBOClass entity2) {
    this.setEntity(entity);
    this.setQuality(quality);
    this.setEntity2(entity2);
  }
  
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
    if (!this.areEqualOrNull(this.getQuality(), OboUtil.getAttributeForValue(character.getQuality()))) {
      return false;
    }
    return true;
  }
  
  private boolean areEqualOrNull(Object o1, Object o2) {
    if ((o1 == null) && (o2 == null)) {
      return true;
    } else if ((o1 == null) || (o2 == null)) {
      return false;
    }
    return o1.equals(o2);
  }
  
  public Object getValue(CharacterI character) {
    return new PhenotypeMatrixCell (character);
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

  public void setEntity(OBOClass entity) {
    this.entity = entity;
  }

  public OBOClass getEntity() {
    return entity;
  }

  public void setQuality(OBOClass quality) {
    this.quality = quality;
  }

  public OBOClass getQuality() {
    return quality;
  }

  public void setEntity2(OBOClass entity2) {
    this.entity2 = entity2;
  }

  public OBOClass getEntity2() {
    return entity2;
  }
  
  public int hashCode() {
    int entityHash, qualityHash, entity2Hash;
    entityHash = entity != null ? entity.hashCode() : 0;
    qualityHash = quality != null ? quality.hashCode() : 0;
    entity2Hash = entity2 != null ? entity2.hashCode() : 0;
    return entityHash ^ qualityHash ^ entity2Hash;
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof PhenotypeMatrixColumn) {
      PhenotypeMatrixColumn pmc = (PhenotypeMatrixColumn)o;
      return entity.equals(pmc.getEntity()) && quality.equals(pmc.getQuality()) && entity2.equals(pmc.getEntity2());
    } else {
      return false;
    }
  }
  
}
