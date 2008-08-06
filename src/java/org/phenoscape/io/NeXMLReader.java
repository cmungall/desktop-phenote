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
import org.nexml.x10.Dict;
import org.nexml.x10.NexmlDocument;
import org.nexml.x10.StandardCells;
import org.nexml.x10.StandardChar;
import org.nexml.x10.StandardFormat;
import org.nexml.x10.StandardStates;
import org.nexml.x10.Taxa;
import org.phenoscape.model.Character;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NeXMLReader {
  
  private final List<Character> characters = new ArrayList<Character>();
  private final List<Taxon> taxa = new ArrayList<Taxon>();
  private final NexmlDocument xmlDoc;
  private String charactersBlockID = UUID.randomUUID().toString();
  private String curatorsText;
  private String publicationText;
  private String pubNotesText; 

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

  public String getCuratorsText() {
    return this.curatorsText;
  }

  public String getPublicationText() {
    return this.publicationText;
  }

  public String getPubNotesText() {
    return this.pubNotesText;
  }
  
  private void parseNeXML() {
    final Dict metadata = NeXMLUtil.findOrCreateMetadataDict(this.xmlDoc);
    this.parseMetadata(metadata);
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
  
  private void parseMetadata(Dict metadataDict) {
    final Element any = NeXMLUtil.getFirstChildWithTagName(((Element)(metadataDict.getDomNode())), "any");
    if (any != null) {
      final Element curators = NeXMLUtil.getFirstChildWithTagName(any, "curators");
      this.curatorsText = curators != null ? NeXMLUtil.getTextContent(curators) : null;
      final Element publication = NeXMLUtil.getFirstChildWithTagName(any, "publication");
      this.publicationText = publication != null ? NeXMLUtil.getTextContent(publication) : null;
      final Element pubNotes = NeXMLUtil.getFirstChildWithTagName(any, "publicationNotes");
      this.pubNotesText = pubNotes != null ? NeXMLUtil.getTextContent(pubNotes) : null;
    }
  }
  
  
}
