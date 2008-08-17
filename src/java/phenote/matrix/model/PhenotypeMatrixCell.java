package phenote.matrix.model;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;

import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharacterI;

public class PhenotypeMatrixCell implements MatrixCell {

  private OBOClass quality;
  private OBOClass entity2;
  private String count;
  private String measurement;
  private String unit;
  
  public PhenotypeMatrixCell (CharacterI ch) {
    setQuality(ch.getQuality());
    try {
      setEntity2(ch.getTerm("E2"));
      setCount(ch.getValueString("C"));
      setMeasurement(ch.getValueString("M"));
      setUnit(ch.getValueString("U"));
    } catch (CharFieldException e) {
      e.printStackTrace();
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
  
  public OBOClass getEntity2() {
    return entity2;
  }
  
  public void setEntity2(OBOClass entity2) {
    this.entity2 = entity2;
  }

  public void setCount(String count) {
    this.count = count;
  }

  public String getCount() {
    return count;
  }

  public void setMeasurement(String measurement) {
    this.measurement = measurement;
  }

  public String getMeasurement() {
    return measurement;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public String getUnit() {
    return unit;
  }
  
}
