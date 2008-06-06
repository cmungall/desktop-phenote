package org.phenoscape.model;

import org.nexml.x10.StandardChar;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.EventSelectionModel;

public class Character {
  
  private final StandardChar storedXML;
  private final EventList<State> states = new BasicEventList<State>();
  private final EventSelectionModel<State> statesSelectionModel = new EventSelectionModel<State>(this.states);
  private String label;
  
  public Character() {
    this(StandardChar.Factory.newInstance());
  }

  public Character(StandardChar characterXML) {
    this.storedXML = characterXML;
  }
  
  public StandardChar getStoredXML() {
    return this.storedXML;
  }
  
  public State newState() {
    final State newState = new State();
    this.addState(newState);
    return newState;
  }
  
  public void addState(State aState) {
    this.states.add(aState);
  }
  
  public void removeState(State aState) {
    this.states.remove(aState);
  }
  
  public EventList<State> getStates() {
    return this.states;
  }
  
  public EventSelectionModel<State> getStatesSelectionModel() {
    return this.statesSelectionModel;
  }
  
  public String getLabel() {
    return label;
  }

  public void setLabel(String aLabel) {
    this.label = aLabel;
  }
  
}
