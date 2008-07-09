package org.phenoscape.model;

import java.util.UUID;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

/**
 * A State is a possible evolutionary state for a Character, and represents a possible cell 
 * value in an evolutionary character matrix.
 * A State is described by free text as well as zero or more ontology-based Phenotypes.
 * @author Jim Balhoff
 */
public class State {
  
  private final String nexmlID;
  private final EventList<Phenotype> phenotypes = new BasicEventList<Phenotype>();
  private String label;
  private String symbol;
  
  public State() {
   this(UUID.randomUUID().toString()); 
  }
  
  public State(String nexmlID) {
    this.nexmlID = nexmlID;
  }
  
  public String getNexmlID() {
    return this.nexmlID;
  }
  
  public Phenotype newPhenotype() {
    final Phenotype newPhenotype = new Phenotype();
    this.addPhenotype(newPhenotype);
    return newPhenotype;
  }
  
  public void addPhenotype(Phenotype aPhenotype) {
    this.phenotypes.add(aPhenotype);
  }
  
  public void removePhenotype(Phenotype aPhenotype) {
    this.phenotypes.remove(aPhenotype);
  }
  
  public EventList<Phenotype> getPhenotypes() {
    return this.phenotypes;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String aLabel) {
    this.label = aLabel;
  }
  
  /**
   * Returns the single-character symbol used as shorthand for this
   * state in an evolutionary character matrix.
   */
  public String getSymbol() {
    return this.symbol;
  }

  /**
   * Set the shorthand symbol for this state. The symbol should be 
   * a single-character string.
   */
  public void setSymbol(String aSymbol) {
    this.symbol = aSymbol;
  }
  
}
