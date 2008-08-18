package phenote.matrix.model;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharacterI;

public class PhenotypeMatrixRow implements MatrixRow {

  private OBOClass taxon;
  
  /**
   * Creates a new PhenotypeMatrixRow based on the given Valid Taxon
   * 
   * @param t the taxon to use as the basis for this PhenotypeMatrixRow
   */
  public PhenotypeMatrixRow (OBOClass t) {
    setTaxon(t);
  }
  
  /** 
   * Determines whether or not a given character contains the same values as the matrix row
   * 
   * @param character the character to be tested
   * @return a Boolean value indicating the validity of the Character as a row value
   */
  public boolean isValue(CharacterI character) {
    try {
      return areEqualOrNull (getTaxon(), character.getTerm("GC"));
    } catch (CharFieldException e) {
      log().error("Taxon field is not properly configured", e);
      return false;
    }
  }
  
  /**
   * @character the character to be used to extract the row value
   * @return the Valid Taxon OBOClass term representing the value of this row
   */
  public Object getValue(CharacterI character) {
    try {
      return character.getTerm("GC");
    } catch (CharFieldException e) {
      log().error("Taxon field is not properly configured", e);
      return null;
    }
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
   * @param taxon the taxon (as an OBOClass object) to be used for this row in the matrix
   */
  public void setTaxon(OBOClass taxon) {
    this.taxon = taxon;
  }

  /**
   * @return the taxon (as an OBOClass object) of the current row in the matrix
   */
  public OBOClass getTaxon() {
    return taxon;
  }
  
  /**
   * @return an int value representing the hash code computation for the PhenotypeMatrixRow
   */
  public int hashCode() {
    int taxonHash;
    taxonHash = taxon != null ? taxon.hashCode() : 0;
    return taxonHash;
  }
  
  /**
   * @param o an Object to be compared to the current PhenotypeMatrixRow object
   * @return the Boolean value indicating whether or not the Object is equivalent to the PhenotypeMatrixRow
   */
  public boolean equals(Object o) {
    if (o instanceof PhenotypeMatrixRow) {
      PhenotypeMatrixRow pmr = (PhenotypeMatrixRow)o;
      return areEqualOrNull(taxon, pmr.getTaxon());
    } else {
      return false;
    }
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
}