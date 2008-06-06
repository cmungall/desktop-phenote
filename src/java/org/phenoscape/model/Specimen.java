package org.phenoscape.model;

import org.obo.datamodel.OBOClass;

public class Specimen {
  
  private OBOClass collectionCode;
  private String catalogID;
  
  public OBOClass getCollectionCode() {
    return this.collectionCode;
  }
  public void setCollectionCode(OBOClass collectionCode) {
    this.collectionCode = collectionCode;
  }
  public String getCatalogID() {
    return this.catalogID;
  }
  public void setCatalogID(String catalogID) {
    this.catalogID = catalogID;
  }
  
  public String toString() {
    final StringBuffer buffer = new StringBuffer();
    if (this.collectionCode != null) { buffer.append(this.collectionCode.getName()); }
    if (this.catalogID != null) { buffer.append(this.catalogID); }
    return buffer.toString();
  }

}
