package org.phenoscape.model;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

public class DataSet {
  
  private final EventList<Character> characters = new BasicEventList<Character>();
  private final EventList<Taxon> taxa = new BasicEventList<Taxon>();
  private String publication;
  
  public Character newCharacter() {
    final Character newCharacter = new Character();
    this.addCharacter(newCharacter);
    return newCharacter;
  }
  
  public void addCharacter(Character aCharacter) {
    this.characters.add(aCharacter);
  }
  
  public void removeCharacter(Character aCharacter) {
    this.characters.remove(aCharacter);
  }
  
  public EventList<Character> getCharacters() {
    return this.characters;
  }
  
  public Taxon newTaxon() {
    final Taxon newTaxon = new Taxon();
    this.addTaxon(newTaxon);
    return newTaxon;
  }
  
  public void addTaxon(Taxon aTaxon) {
    this.taxa.add(aTaxon);
  }
  
  public void removeTaxon(Taxon aTaxon) {
    this.taxa.remove(aTaxon);
  }
  
  public EventList<Taxon> getTaxa() {
    return this.taxa;
  }
  
  public String getPublication() {
    return this.publication;
  }
  
  public void setPublication(String aPublication) {
    this.publication = aPublication;
  }

}
