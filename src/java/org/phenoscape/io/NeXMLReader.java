package org.phenoscape.io;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.xmlbeans.XmlException;
import org.nexml.x10.AbstractBlock;
import org.nexml.x10.AbstractChar;
import org.nexml.x10.AbstractState;
import org.nexml.x10.AbstractStates;
import org.nexml.x10.NexmlDocument;
import org.nexml.x10.StandardCells;
import org.nexml.x10.StandardChar;
import org.nexml.x10.StandardFormat;
import org.nexml.x10.StandardStates;
import org.nexml.x10.Taxa;
import org.phenoscape.model.Character;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;

public class NeXMLReader {
  
  private final List<Character> characters = new ArrayList<Character>();
  private final List<Taxon> taxa = new ArrayList<Taxon>();
  private final NexmlDocument xmlDoc;
  private String charactersBlockID = UUID.randomUUID().toString();

  public NeXMLReader(File aFile) throws XmlException, IOException {
    this.xmlDoc = NexmlDocument.Factory.parse(aFile);
    this.parseNeXML();
  }
  
  public NeXMLReader(Reader aReader) throws XmlException, IOException {
    this.xmlDoc = NexmlDocument.Factory.parse(aReader);
    this.parseNeXML();
  }
  
  public NexmlDocument getXMLDoc() {
    return this.xmlDoc;
  }
  
  public String getCharactersBlockID() {
    return this.charactersBlockID;
  }
  
  public List<Character> getCharacters() {
    return this.characters;
  }
  
  public List<Taxon> getTaxa() {
    return this.taxa;
  }
  
  private void parseNeXML() {
    for (AbstractBlock block : this.xmlDoc.getNexml().getCharactersArray()) {
      if (block instanceof StandardCells) {
        this.charactersBlockID = block.getId();
        final StandardCells cells = (StandardCells)block;
        this.parseStandardCells(cells);
        final Taxa taxa = NeXMLUtil.findOrCreateTaxa(this.xmlDoc, cells.getOtus());
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
      final Character newCharacter;
      if (standardChar.getStates() != null) {
        newCharacter = new Character(standardChar.getId(), standardChar.getStates());
      } else {
        newCharacter = new Character(standardChar.getId());
      }
      newCharacter.setLabel(standardChar.getLabel());
      final AbstractStates states = NeXMLUtil.findOrCreateStates(format, newCharacter.getStatesNexmlID());
      if (states instanceof StandardStates) {
        for (AbstractState abstractState : states.getStateArray()) {
          final State newState = new State(abstractState.getId());
          newState.setSymbol(abstractState.getSymbol().getStringValue());
          newState.setLabel(abstractState.getLabel());
          newCharacter.addState(newState);
        }
      }
      this.characters.add(newCharacter);
    }
  }
  
  private void parseTaxa(Taxa taxa) {
    for (org.nexml.x10.Taxon xmlTaxon : taxa.getOtuArray()) {
      final Taxon newTaxon = new Taxon(xmlTaxon.getId());
      newTaxon.setPublicationName(xmlTaxon.getLabel());
      this.taxa.add(newTaxon);
    }
  }
  
}
