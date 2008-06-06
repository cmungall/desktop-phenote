package org.phenoscape.model;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.phenoscape.app.DocumentController;
import org.phenoscape.io.NeXMLReader;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.EventSelectionModel;

public class PhenoscapeController extends DocumentController {
  
  private final EventList<Character> characters = new BasicEventList<Character>();
  private final EventList<Taxon> taxa = new BasicEventList<Taxon>();
  private final EventSelectionModel<Character> charactersSelectionModel = new EventSelectionModel<Character>(this.characters);
  private final EventSelectionModel<Taxon> taxaSelectionModel = new EventSelectionModel<Taxon>(this.taxa);
  private final EventList<Specimen> currentSpecimens = new CollectionList<Taxon, Specimen>(this.taxaSelectionModel.getSelected(),
      new CollectionList.Model<Taxon, Specimen>(){
    public List<Specimen> getChildren(Taxon parent) {
      return parent.getSpecimens();
    }
  } 
  );
  private final EventSelectionModel<Specimen> currentSpecimensSelectionModel = new EventSelectionModel<Specimen>(this.currentSpecimens);
  private final EventList<State> currentStates = new CollectionList<Character, State>(this.charactersSelectionModel.getSelected(),
      new CollectionList.Model<Character, State>() {
    public List<State> getChildren(Character parent) {
      return parent.getStates();
    }
  }
  );
  private final EventSelectionModel<State> currentStatesSelectionModel = new EventSelectionModel<State>(this.currentStates);
  private final EventList<Phenotype> currentPhenotypes = new CollectionList<State, Phenotype>(this.currentStatesSelectionModel.getSelected(),
      new CollectionList.Model<State, Phenotype>() {
    public List<Phenotype> getChildren(State parent) {
      return parent.getPhenotypes();
    }
  }
  );
  private final EventSelectionModel<Phenotype> currentPhenotypesSelectionModel = new EventSelectionModel<Phenotype>(this.currentPhenotypes);

  public PhenoscapeController() {
    this.charactersSelectionModel.setSelectionMode(EventSelectionModel.SINGLE_SELECTION);
    this.currentStatesSelectionModel.setSelectionMode(EventSelectionModel.SINGLE_SELECTION);
    this.currentPhenotypesSelectionModel.setSelectionMode(EventSelectionModel.MULTIPLE_INTERVAL_SELECTION);
  }
  
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

  public EventSelectionModel<Character> getCharactersSelectionModel() {
    return this.charactersSelectionModel;
  }
  
  public EventList<State> getStatesForCurrentCharacterSelection() {
    return this.currentStates;
  }
  
  public EventSelectionModel<State> getCurrentStatesSelectionModel() {
    return this.currentStatesSelectionModel;
  }
  
  public EventList<Phenotype> getPhenotypesForCurrentStateSelection() {
    return this.currentPhenotypes;
  }
  
  public EventSelectionModel<Phenotype> getCurrentPhenotypesSelectionModel() {
    return this.currentPhenotypesSelectionModel;
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
  
  public EventSelectionModel<Taxon> getTaxaSelectionModel() {
    return this.taxaSelectionModel;
  }
  
  public EventList<Specimen> getSpecimensForCurrentTaxonSelection() {
    return this.currentSpecimens;
  }
  
  public EventSelectionModel<Specimen> getCurrentSpecimensSelectionModel() {
    return this.currentSpecimensSelectionModel;
  }
  
  @Override
  public boolean readData(File aFile) {
    log().debug("Read file: " + aFile);
    try {
      final NeXMLReader reader = new NeXMLReader(aFile);
      this.setCurrentFile(aFile);
      this.characters.clear();
      this.characters.addAll(reader.getCharacters());
      this.taxa.clear();
      this.taxa.addAll(reader.getTaxa());
      return true;
    } catch (XmlException e) {
      log().error("Unable to parse XML", e);
    } catch (IOException e) {
      log().error("Unable to read file", e);
    }
    return false;
  }
  
  @Override
  public void writeData(File aFile) {
    
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
