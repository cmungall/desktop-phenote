package phenote.datamodel;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;

/**
 * Provides a mapping between a character representing a phenotype, such as is required by 
 * the PhenoXML adapter, and the standard character with generic fields. Some of these properties 
 * are provided by CharacterI, but putting everything here will allow CharacterI to remain generic.
 * @author Jim Balhoff
 */
public class PhenotypeCharacterWrapper {
  
  private static final String ENTITY = "E";
  private static final String QUALITY = "Q";
  private static final String ADDL_ENTITY = "E2";
  private static final String COUNT = "C";
  private static final String MEASUREMENT = "M";
  private static final String UNIT = "U";
  private static final String PUB = "PUB";
  private static final String GENOTYPE = "GT";
  private static final String GENETIC_CONTEXT = "GC";
  
  private final CharacterI character;

  public PhenotypeCharacterWrapper(CharacterI character) {
    this.character = character;
  }
  
  public CharacterI getCharacter() {
    return character;
  }
  
  public OBOClass getEntity() {
    return this.getTerm(ENTITY);
  }
  
  public void setEntity(OBOClass term) {
    this.setTerm(ENTITY, term);
  }
  
  public OBOClass getQuality() {
    return this.getTerm(QUALITY);
  }
  
  public void setQuality(OBOClass term) {
    this.setTerm(QUALITY, term);
  }
  
  public OBOClass getAdditionalEntity() {
    return this.getTerm(ADDL_ENTITY);
  }
  
  public void setAdditionalEntity(OBOClass term) {
    this.setTerm(ADDL_ENTITY, term);
  }
  
  public boolean hasCount() {
    final String text = this.getText(COUNT);
    return (text != null) && (text != "");
  }
  
  public int getCount() {
    return new Integer(this.getText(COUNT));
  }
  
  public void setCount(int count) {
    this.setText(COUNT, "" + count);
  }
  
  public boolean hasMeasurement() {
    final String text = this.getText(MEASUREMENT);
    return (text != null) && (text != "");
  }
  
  public float getMeasurement() {
    return new Float(this.getText(MEASUREMENT));
  }
  
  public void setMeasurement(float measurement) {
    this.setText(MEASUREMENT, "" + measurement);
  }
  
  public OBOClass getUnit() {
    return this.getTerm(UNIT);
  }
  
  public void setUnit(OBOClass term) {
    this.setTerm(UNIT, term);
  }
  
  public String getPub() {
    return this.getText(PUB);
  }
  
  public void setPub(String publication) {
    this.setText(PUB, publication);
  }
  
  public String getGenotype() {
    return this.getText(GENOTYPE);
  }
  
  public void setGenotype(String genotype) {
    this.setText(GENOTYPE, genotype);
  }
  
  public OBOClass getGeneticContext() {
    return this.getTerm(GENETIC_CONTEXT);
  }
  
  public void setGeneticContext(OBOClass term) {
    this.setTerm(GENETIC_CONTEXT, term);
  }
  
  private OBOClass getTerm(String tag) {
    final CharFieldValue cfv = this.getValue(tag);
    return cfv != null ? cfv.getOboClass() : null;
  }
  
  private String getText(String tag) {
    final CharFieldValue cfv = this.getValue(tag);
    return cfv != null ? cfv.getValueAsString() : null;
  }
  
  private CharFieldValue getValue(String tag) {
    try {
      return this.character.getValue(CharFieldManager.inst().getCharFieldForName(tag));
    } catch (CharFieldException e) {
      log().warn("No charfield configured with tag: " + tag, e);
      return null;
    }
  }
  
  private void setText(String tag, String text) {
    try {
      final CharField cf = CharFieldManager.inst().getCharFieldForName(tag);
      this.character.setValue(cf, new CharFieldValue(text, this.character, cf));
    } catch (CharFieldException e) {
      log().warn("No charfield configured with tag: " + tag, e);
    }
  }
  
  private void setTerm(String tag, OBOClass term) {
    try {
      final CharField cf = CharFieldManager.inst().getCharFieldForName(tag);
      this.character.setValue(cf, new CharFieldValue(term, this.character, cf));
    } catch (CharFieldException e) {
      log().warn("No charfield configured with tag: " + tag, e);
    }
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
