package org.phenoscape.io;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.xmlbeans.XmlException;
import org.nexml.x10.AbstractBlock;
import org.nexml.x10.AbstractChar;
import org.nexml.x10.AbstractState;
import org.nexml.x10.AbstractStates;
import org.nexml.x10.NexmlDocument;
import org.nexml.x10.StandardCells;
import org.nexml.x10.StandardChar;
import org.nexml.x10.StandardFormat;
import org.nexml.x10.StandardState;
import org.nexml.x10.StandardStates;
import org.nexml.x10.Taxa;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;

public class NeXMLReader {
  
  private final List<Character> characters = new ArrayList<Character>();
  private final List<Taxon> taxa = new ArrayList<Taxon>();
  private final NexmlDocument xmlDoc;
  private final Set<String> statesIDs = new HashSet<String>();

  public NeXMLReader(File aFile) throws XmlException, IOException {
    this.xmlDoc = NexmlDocument.Factory.parse(aFile);
    this.parseNeXML();
  }
  
  public NeXMLReader(Reader aReader) throws XmlException, IOException {
    this.xmlDoc = NexmlDocument.Factory.parse(aReader);
    this.parseNeXML();
  }
  
  public List<Character> getCharacters() {
    return this.characters;
  }
  
  public List<Taxon> getTaxa() {
    return this.taxa;
  }
  
  public DataSet getDataSet() {
    return null;
  }
  
  private void parseNeXML() {
    for (AbstractBlock block : this.xmlDoc.getNexml().getCharactersArray()) {
      if (block instanceof StandardCells) {
        final StandardCells cells = (StandardCells)block;
        this.parseStandardCells(cells);
        final Taxa taxa = this.findOrCreateTaxa(cells.getOtus());
        this.parseTaxa(taxa);
        break;
      }
    }
  }
  
  private void parseStandardCells(StandardCells standardCells) {
    if (!(standardCells.getFormat() instanceof StandardFormat)) return;
    final StandardFormat format = (StandardFormat)(standardCells.getFormat());
    for (AbstractChar abstractChar : format.getCharArray()) {
      if (!(abstractChar instanceof StandardChar)) continue;
      final StandardChar standardChar = (StandardChar)abstractChar;
      //final Character newCharacter = new Character(standardChar);
      final AbstractStates states = this.findOrCreateStates(standardChar.getStates(), format);
      if (states instanceof StandardStates) {
        //TODO
      }
    }
  }
  
  private void parseTaxa(Taxa taxa) {
  }
  
  private Taxa findOrCreateTaxa(String id) {
    for (Taxa taxaBlock : this.xmlDoc.getNexml().getOtusArray()) {
      if (taxaBlock.getId().equals(id)) return taxaBlock;
    }
    // no taxa block was found, so create one for that id
    final Taxa newTaxa = this.xmlDoc.getNexml().addNewOtus();
    newTaxa.setId(id);
    return newTaxa;
  }
  
  private AbstractStates findOrCreateStates(String id, StandardFormat format) {
    for (AbstractStates abstractStates : format.getStatesArray()) {
      if (abstractStates.getId().equals(id)) return abstractStates;
    }
    // no states block was found, so create one for that id
    final AbstractStates newStates = format.addNewStates();
    newStates.setId(id);
    return newStates;
  }
  
  private void addStatesToCharacter(StandardStates statesBlock, Character character) {
    for (AbstractState state : statesBlock.getStateArray()) {
      if (!(state instanceof StandardState)) continue;
      character.addState(new State((StandardState)state));
    }
  }
}
