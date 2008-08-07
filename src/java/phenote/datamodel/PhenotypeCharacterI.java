package phenote.datamodel;

import org.obo.datamodel.OBOClass;

public interface PhenotypeCharacterI {
  
  public OBOClass getEntity();
  
  public void setEntity(OBOClass term);

  public OBOClass getQuality();
  
  public void setQuality(OBOClass term);

  public OBOClass getAdditionalEntity();
  
  public void setAdditionalEntity(OBOClass term);

  public boolean hasCount();

  public int getCount();
  
  public void setCount(int count);

  public boolean hasMeasurement();

  public float getMeasurement();
  
  public void setMeasurement(float measurement);

  public OBOClass getUnit();
  
  public void setUnit(OBOClass term);
  
  public String getDescription();
  
  public void setDescription(String desc);
  
  public static interface PhenotypeCharacterFactory {
    
    public PhenotypeCharacterI newPhenotypeCharacter();
    
  }

}