package org.phenoscape.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.bbop.framework.GUIManager;
import org.nexml.x10.NexmlDocument;
import org.phenoscape.app.DocumentController;
import org.phenoscape.io.NeXMLReader;
import org.phenoscape.io.NeXMLWriter;
import org.phenoscape.swing.ListSelectionMaintainer;

import phenote.gui.selection.SelectionManager;
import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.EventSelectionModel;

public class PhenoscapeController extends DocumentController {
  
  private final OntologyController ontologyController = new OntologyController();
  private final DataSet dataSet = new DataSet();
  private final EventSelectionModel<Character> charactersSelectionModel;
  private final EventSelectionModel<Taxon> taxaSelectionModel;
  private final EventList<Specimen> currentSpecimens;
  private final EventSelectionModel<Specimen> currentSpecimensSelectionModel;
  private final EventList<State> currentStates;
  private final EventSelectionModel<State> currentStatesSelectionModel;
  private final EventList<Phenotype> currentPhenotypes;
  private final EventSelectionModel<Phenotype> currentPhenotypesSelectionModel;
  private String charactersBlockID = UUID.randomUUID().toString();
  private NexmlDocument xmlDoc = NexmlDocument.Factory.newInstance();
  private String appName;
  private final List<NewDataListener> newDataListeners = new ArrayList<NewDataListener>();
  
  public PhenoscapeController() {
    super();
    this.charactersSelectionModel = new EventSelectionModel<Character>(this.dataSet.getCharacters());
    new ListSelectionMaintainer<Character>(this.dataSet.getCharacters(), this.charactersSelectionModel);
    this.charactersSelectionModel.setSelectionMode(EventSelectionModel.SINGLE_SELECTION);
    this.taxaSelectionModel = new EventSelectionModel<Taxon>(this.dataSet.getTaxa());
    new ListSelectionMaintainer<Taxon>(this.dataSet.getTaxa(), this.taxaSelectionModel);
    this.taxaSelectionModel.setSelectionMode(EventSelectionModel.SINGLE_SELECTION);
    this.currentSpecimens = new CollectionList<Taxon, Specimen>(this.taxaSelectionModel.getSelected(),
        new CollectionList.Model<Taxon, Specimen>(){
      public List<Specimen> getChildren(Taxon parent) {
        return parent.getSpecimens();
      }
    } 
    );
    this.currentSpecimensSelectionModel = new EventSelectionModel<Specimen>(this.currentSpecimens);
    new ListSelectionMaintainer<Specimen>(this.currentSpecimens, this.currentSpecimensSelectionModel);
    this.currentStates = new CollectionList<Character, State>(this.charactersSelectionModel.getSelected(),
        new CollectionList.Model<Character, State>() {
      public List<State> getChildren(Character parent) {
        return parent.getStates();
      }
    }
    );
    this.currentStatesSelectionModel = new EventSelectionModel<State>(this.currentStates);
    new ListSelectionMaintainer<State>(this.currentStates, this.currentStatesSelectionModel);
    this.currentStatesSelectionModel.setSelectionMode(EventSelectionModel.SINGLE_SELECTION);
    this.currentPhenotypes = new CollectionList<State, Phenotype>(this.currentStatesSelectionModel.getSelected(),
        new CollectionList.Model<State, Phenotype>() {
      public List<Phenotype> getChildren(State parent) {
        return parent.getPhenotypes();
      }
    }
    );
    this.currentPhenotypesSelectionModel = new EventSelectionModel<Phenotype>(this.currentPhenotypes);
    new ListSelectionMaintainer<Phenotype>(this.currentPhenotypes, this.currentPhenotypesSelectionModel);
    this.currentPhenotypesSelectionModel.setSelectionMode(EventSelectionModel.MULTIPLE_INTERVAL_SELECTION);
  }
  
  public OntologyController getOntologyController() {
    return this.ontologyController;
  }
  
  public DataSet getDataSet() {
    return this.dataSet;
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
      final NeXMLReader reader = new NeXMLReader(aFile, this.getOntologyController().getOBOSession());
      this.xmlDoc = reader.getXMLDoc();
      this.charactersBlockID = reader.getCharactersBlockID();
      this.dataSet.getCharacters().clear(); //TODO this is not well encapsulated
      this.dataSet.getCharacters().addAll(reader.getCharacters());
      this.getDataSet().getTaxa().clear(); //TODO this is not well encapsulated
      this.getDataSet().getTaxa().addAll(reader.getTaxa());
      this.getDataSet().setCurators(reader.getCuratorsText());
      this.getDataSet().setPublication(reader.getPublicationText());
      this.getDataSet().setPublicationNotes(reader.getPubNotesText());
      this.getDataSet().setMatrix(reader.getMatrix());
      
      this.fireDataChanged();
      return true;
    } catch (XmlException e) {
      log().error("Unable to parse XML", e);
    } catch (IOException e) {
      log().error("Unable to read file", e);
    }
    return false;
  }
  
  @Override
  public boolean writeData(File aFile) {
    final NeXMLWriter writer = new NeXMLWriter(this.charactersBlockID, this.xmlDoc);
    writer.setDataSet(this.dataSet);
    writer.setGenerator(this.getAppName() + " " + this.getAppVersion());
    try {
      writer.write(aFile);
      return true;
    } catch (IOException e) {
      log().error("Unable to write NeXML file", e);
      return false;
    }
  }
  
  public JFrame getWindow() {
    return GUIManager.getManager().getFrame();
  }
    
  @Override
  public String getAppName() {
    return this.appName;
  }
  
  public void setAppName(String name) {
    this.appName = name;
  }
  
  public String getAppVersion() {
    return System.getProperty("phenex.version");
  }

  public SelectionManager getPhenoteSelectionManager() {
    return SelectionManager.inst();
  }
  
  public void addNewDataListener(NewDataListener listener) {
    this.newDataListeners.add(listener);
  }
  
  public void removeNewDataListener(NewDataListener listener) {
    this.newDataListeners.remove(listener);
  }
  
  private void fireDataChanged() {
    for (NewDataListener listener : this.newDataListeners) {
      listener.reloadData();
    }
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
