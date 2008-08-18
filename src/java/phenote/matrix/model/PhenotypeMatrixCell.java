package phenote.matrix.model;

import org.obo.datamodel.OBOClass;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharacterI;

public class PhenotypeMatrixCell implements MatrixCell {

  private OBOClass quality;
  private OBOClass entity2;
  private String count;
  private String measurement;
  private String unit;
  
  
  /** 
   * Extracts the desired information from a given character and sets the contents of the matrix cell 
   * 
   * @param ch the Character being used to build the matrix cell
   */
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
  
  /** 
   * @return the quality characteristic of the matrix cell (as an OBOClass object) 
   */
  public OBOClass getQuality() {
    return quality;
  }
  
  /** 
   * @param quality the Character quality to use for this matrix cell
   */
  public void setQuality(OBOClass quality) {
    this.quality = quality;
  }
  
  /** 
   * @return the additional entity characteristic of the matrix cell (as an OBOClass object) 
   */
  public OBOClass getEntity2() {
    return entity2;
  }
  
  /** 
   * @param entity2 the additional entity to use for this matrix cell
   */
  public void setEntity2(OBOClass entity2) {
    this.entity2 = entity2;
  }

  /** 
   * @return the count characteristic of the matrix cell (as an String object) 
   */
  public String getCount() {
    return count;
  }
  
  /** 
   * @param count the count to use for this matrix cell
   */
  public void setCount(String count) {
    this.count = count;
  }

  /** 
   * @return the measurement characteristic of the matrix cell (as an String object) 
   */
  public String getMeasurement() {
    return measurement;
  }

  /** 
   * @param measurement the measurement to use for this matrix cell
   */
  public void setMeasurement(String measurement) {
    this.measurement = measurement;
  }

  /** 
   * @return the unit characteristic of the matrix cell (as an String object) 
   */
  public String getUnit() {
    return unit;
  }
  
  /** 
   * @param unit the unit to use for this matrix cell
   */
  public void setUnit(String unit) {
    this.unit = unit;
  }
}
