package phenote.matrix.model;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;

import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharacterI;

public class PhenotypeMatrixCell implements MatrixCell {

  private OBOClass quality;
  private OBOClass count;
  private OBOClass measurement;
  private OBOClass unit;
  
  public PhenotypeMatrixCell (CharacterI ch) {
    setQuality(ch.getQuality());
    try {
      setCount(ch.getTerm("C"));
      setMeasurement(ch.getTerm("M"));
      setUnit(ch.getTerm("U"));
    }catch (CharFieldException e) {
      log().error("Matrix Cell Character is not properly configured", e);
    }
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

  public void setQuality(OBOClass quality) {
    this.quality = quality;
  }

  public OBOClass getQuality() {
    return quality;
  }

  public void setCount(OBOClass count) {
    this.count = count;
  }

  public OBOClass getCount() {
    return count;
  }

  public void setMeasurement(OBOClass measurement) {
    this.measurement = measurement;
  }

  public OBOClass getMeasurement() {
    return measurement;
  }

  public void setUnit(OBOClass unit) {
    this.unit = unit;
  }

  public OBOClass getUnit() {
    return unit;
  }
  
}
