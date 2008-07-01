package org.phenoscape.model;

import org.nexml.x10.StandardChar;
import org.nexml.x10.StandardStates;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

public class Character {
  
  private final StandardChar storedCharacterXML;
  private final StandardStates storedStatesXML;
  private final EventList<State> states = new BasicEventList<State>();
  private String label;
  
  public Character() {
    this(StandardChar.Factory.newInstance(), StandardStates.Factory.newInstance());
  }

  public Character(StandardChar characterXML, StandardStates statesXML) {
    this.storedCharacterXML = characterXML;
    this.storedStatesXML = statesXML;
  }
  
  public StandardChar getStoredCharacterXML() {
    return this.storedCharacterXML;
  }
  
  public StandardStates getStoredStatesXML() {
    return this.storedStatesXML;
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
  
  public String getLabel() {
    return label;
  }

  public void setLabel(String aLabel) {
    this.label = aLabel;
  }
  
}
