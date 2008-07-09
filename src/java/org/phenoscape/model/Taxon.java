package org.phenoscape.model;

import java.util.UUID;

import org.obo.datamodel.OBOClass;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

public class Taxon {
  
  private final String nexmlID;
  private OBOClass validName;
  private String publicationName;
  private final EventList<Specimen> specimens = new BasicEventList<Specimen>();
  
  public Taxon() {
    this(UUID.randomUUID().toString());
  }
  
  public Taxon(String nexmlID) {
    this.nexmlID = nexmlID;
  }
  
  public String getNexmlID() {
    return this.nexmlID;
  }
  
  public OBOClass getValidName() {
    return this.validName;
  }
  
  public void setValidName(OBOClass validName) {
    this.validName = validName;
  }
  
  public String getPublicationName() {
    return this.publicationName;
  }
  
  public void setPublicationName(String publicationName) {
    this.publicationName = publicationName;
  }
  
  public Specimen newSpecimen() {
    final Specimen newSpecimen = new Specimen();
    this.addSpecimen(newSpecimen);
    return newSpecimen;
  }
  
  public void addSpecimen(Specimen aSpecimen) {
    this.specimens.add(aSpecimen);
  }
  
  public void removeSpecimen(Specimen aSpecimen) {
    this.specimens.remove(aSpecimen);
  }
  
  public EventList<Specimen> getSpecimens() {
    return this.specimens;
  }

}
