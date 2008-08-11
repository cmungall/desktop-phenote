package phenote.matrix.model;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;

import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharacterI;

public class PhenotypeMatrixRow implements MatrixRow {

  private OBOClass taxon;
  
  public PhenotypeMatrixRow (OBOClass t) {
    setTaxon(t);
  }
  
  public boolean isValue(CharacterI character) {
    try {
      return areEqualOrNull (getTaxon(), character.getTerm("Valid Taxon"));
    } catch (CharFieldException e) {
      log().error("Entity2 field is not properly configured", e);
      return false;
    }
  }
  
  public Object getValue(CharacterI character) {
    try {
      return character.getTerm("Valid Taxon");
    } catch (CharFieldException e) {
      log().error("Entity2 field is not properly configured", e);
      return null;
    }
  }

  private boolean areEqualOrNull(Object o1, Object o2) {
    if ((o1 == null) && (o2 == null)) {
      return true;
    } else if ((o1 == null) || (o2 == null)) {
      return false;
    }
    return o1.equals(o2);
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

  public void setTaxon(OBOClass taxon) {
    this.taxon = taxon;
  }

  public OBOClass getTaxon() {
    return taxon;
  }
  
  public int hashCode() {
    int taxonHash;
    taxonHash = taxon != null ? taxon.hashCode() : 0;
    return taxonHash;
  }
  
  public boolean equals(Object o) {
    if (o instanceof PhenotypeMatrixRow) {
      PhenotypeMatrixRow pmr = (PhenotypeMatrixRow)o;
      return areEqualOrNull(taxon, pmr.getTaxon());
    } else {
      return false;
    }
  }
}
