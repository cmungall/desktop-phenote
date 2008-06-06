package org.phenoscape.model;

import org.nexml.x10.StandardState;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

public class State {
  
  private final StandardState storedXML;
  private final EventList<Phenotype> phenotypes = new BasicEventList<Phenotype>();
  private String label;
  private String symbol;
  
  public State() {
   this(StandardState.Factory.newInstance()); 
  }
  
  public State(StandardState stateXML) {
    this.storedXML = stateXML;
  }
  
  public StandardState getStoredXML() {
    return this.storedXML;
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
  
  public String getSymbol() {
    return this.symbol;
  }

  public void setSymbol(String aSymbol) {
    this.symbol = aSymbol;
  }
}
