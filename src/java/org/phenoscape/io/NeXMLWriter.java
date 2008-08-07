package org.phenoscape.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlOptions;
import org.nexml.x10.AbstractBlock;
import org.nexml.x10.AbstractChar;
import org.nexml.x10.AbstractState;
import org.nexml.x10.AbstractStates;
import org.nexml.x10.Dict;
import org.nexml.x10.NexmlDocument;
import org.nexml.x10.StandardChar;
import org.nexml.x10.StandardState;
import org.nexml.x10.StandardStates;
import org.nexml.x10.StandardToken;
import org.nexml.x10.Taxa;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.Specimen;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;
import org.w3c.dom.Element;

public class NeXMLWriter {
  
  private final NexmlDocument xmlDoc;
  private final String charactersBlockID;
  private DataSet data;
  private final XmlOptions options = new XmlOptions();
  
  public NeXMLWriter(String charactersBlockID) {
    this(charactersBlockID, NexmlDocument.Factory.newInstance());
  }
  
  public NeXMLWriter(String charactersBlockID, NexmlDocument startingDoc) {
    this.charactersBlockID = charactersBlockID;
    this.xmlDoc = startingDoc;
    this.options.setSavePrettyPrint();
  }
  
  public void setDataSet(DataSet data) {
    this.data = data;
  }
  
  public void write(File aFile) throws IOException {
    this.constructXMLDoc().save(aFile, this.options);
  }
  
  public void write(OutputStream aStream) throws IOException {
    this.constructXMLDoc().save(aStream, this.options);
  }
  
  public void write(Writer aWriter) throws IOException {
    this.constructXMLDoc().save(aWriter, this.options);
  }
  
  private NexmlDocument constructXMLDoc() {
    final NexmlDocument newDoc = (NexmlDocument)(xmlDoc.copy());
    if (newDoc.getNexml() == null) { newDoc.addNewNexml(); }
    Dict metadata = NeXMLUtil.findOrCreateMetadataDict(newDoc);
    this.writeToMetadata(metadata, this.data.getCurators(), this.data.getPublication(), this.data.getPublicationNotes());
    final AbstractBlock charBlock = NeXMLUtil.findOrCreateCharactersBlock(newDoc, this.charactersBlockID);
    log().debug("Charblock id is: " + charBlock.getId());
    this.writeCharacters(charBlock);
    final String taxaID;
    if ((charBlock.getOtus() == null) || (charBlock.getOtus().equals(""))) {
      taxaID = UUID.randomUUID().toString();
      charBlock.setOtus(taxaID);
    } else {
      taxaID = charBlock.getOtus();
    }
    final Taxa taxaBlock = NeXMLUtil.findOrCreateTaxa(newDoc, taxaID);
    this.writeTaxa(taxaBlock);
    return newDoc;
  }
  
  private void writeCharacters(AbstractBlock charBlock) {
    final List<AbstractChar> existingChars = Arrays.asList(charBlock.getFormat().getCharArray());
    final List<AbstractStates> existingStatesList = Arrays.asList(charBlock.getFormat().getStatesArray());
    final List<AbstractChar> newCharacters = new ArrayList<AbstractChar>();
    final List<AbstractStates> newStatesBlocks = new ArrayList<AbstractStates>();
    final Set<String> usedStatesIDs = new HashSet<String>();
    for (Character character : this.data.getCharacters()) {
      final AbstractChar xmlChar = this.findOrCreateCharWithID(existingChars, character.getNexmlID());
      newCharacters.add(xmlChar);
      xmlChar.setLabel(character.getLabel());
      final AbstractStates statesBlock = this.findOrCreateStatesBlockWithID(existingStatesList, character.getStatesNexmlID());
      final AbstractStates usableStatesBlock;
      if (usedStatesIDs.contains(statesBlock.getId())) {
        usableStatesBlock = (AbstractStates)(statesBlock.copy());
        usableStatesBlock.setId(UUID.randomUUID().toString());
      } else {
        usableStatesBlock = statesBlock;
      }
      newStatesBlocks.add(usableStatesBlock);
      usedStatesIDs.add(usableStatesBlock.getId());
      xmlChar.setStates(usableStatesBlock.getId());
      final List<AbstractState> existingStates = Arrays.asList(usableStatesBlock.getStateArray());
      final List<AbstractState> newStates = new ArrayList<AbstractState>();
      for (State state : character.getStates()) {
        final AbstractState xmlState = this.findOrCreateStateWithID(existingStates, state.getNexmlID());
        newStates.add(xmlState);
        xmlState.setLabel(state.getLabel());
        xmlState.setSymbol(StandardToken.Factory.newValue(state.getSymbol()));
      }
      usableStatesBlock.setStateArray(newStates.toArray(new AbstractState[] {}));
    }
    charBlock.getFormat().setCharArray(newCharacters.toArray(new AbstractChar[] {}));
    charBlock.getFormat().setStatesArray(newStatesBlocks.toArray(new AbstractStates[] {}));
  }
  
  private void writeTaxa(Taxa taxaBlock) {
    final List<org.nexml.x10.Taxon> existingOTUs = Arrays.asList(taxaBlock.getOtuArray());
    final List<org.nexml.x10.Taxon> newOTUs = new ArrayList<org.nexml.x10.Taxon>();
    for (Taxon taxon : this.data.getTaxa()) {
      final org.nexml.x10.Taxon otu = this.findOrCreateOTUWithID(existingOTUs, taxon.getNexmlID());
      newOTUs.add(otu);
      otu.setLabel(taxon.getPublicationName());
      this.writeOBOID(otu, taxon);
      this.writeSpecimens(otu, taxon);
    }
    taxaBlock.setOtuArray(newOTUs.toArray(new org.nexml.x10.Taxon[] {}));
  }
  
  private org.nexml.x10.Taxon findOrCreateOTUWithID(List<org.nexml.x10.Taxon> list, String id) {
    for (org.nexml.x10.Taxon otu : list) {
      if (otu.getId().equals(id)) { return otu; }
    }
    final org.nexml.x10.Taxon newOTU = org.nexml.x10.Taxon.Factory.newInstance();
    newOTU.setId(id);
    return newOTU;
  }

  private AbstractChar findOrCreateCharWithID(List<AbstractChar> list, String id) {
    for (AbstractChar character : list) {
      if (character.getId().equals(id)) { return character; }
    }
    final AbstractChar newCharacter = StandardChar.Factory.newInstance();
    newCharacter.setId(id);
    return newCharacter;
  }
  
  private AbstractStates findOrCreateStatesBlockWithID(List<AbstractStates> list, String id) {
    for (AbstractStates statesBlock : list) {
      if (statesBlock.getId().equals(id)) { return statesBlock; }
    }
    final AbstractStates newStatesBlock = StandardStates.Factory.newInstance();
    newStatesBlock.setId(id);
    return newStatesBlock;
  }
  
  private AbstractState findOrCreateStateWithID(List<AbstractState> list, String id) {
    for (AbstractState state : list) {
      if (state.getId().equals(id)) { return state; }
    }
    final AbstractState newState = StandardState.Factory.newInstance();
    newState.setId(id);
    return newState;
  }
  
  private void writeToMetadata(Dict metadata, String curatorsText, String publicationText, String pubNotesText) {
    final Element any = NeXMLUtil.getFirstChildWithTagName(((Element)(metadata.getDomNode())), "any");
    final Element curators = NeXMLUtil.getFirstChildWithTagName(any, "curators");
    NeXMLUtil.setTextContent(curators, curatorsText);
    final Element publication = NeXMLUtil.getFirstChildWithTagName(any, "publication");
    NeXMLUtil.setTextContent(publication, publicationText);
    final Element pubNotes = NeXMLUtil.getFirstChildWithTagName(any, "publicationNotes");
    NeXMLUtil.setTextContent(pubNotes, pubNotesText);
  }
  
  private void writeOBOID(org.nexml.x10.Taxon otu, Taxon taxon) {
    final Dict oboIDDict = NeXMLUtil.findOrCreateDict(otu, "OBO_ID", otu.getDomNode().getOwnerDocument().createElement("string"));
    final Element stringNode = NeXMLUtil.getFirstChildWithTagName((Element)(oboIDDict.getDomNode()), "string");
    NeXMLUtil.setTextContent(stringNode, (taxon.getValidName() != null ? taxon.getValidName().getID() : null));
  }
  
  private void writeSpecimens(org.nexml.x10.Taxon otu, Taxon taxon) {
    final Dict specimensDict = NeXMLUtil.findOrCreateDict(otu, "OBO_specimens", otu.getDomNode().getOwnerDocument().createElement("any"));
    final Element any = NeXMLUtil.getFirstChildWithTagName((Element)(specimensDict.getDomNode()), "any");
    NeXMLUtil.clearChildren(any);
    for (Specimen specimen : taxon.getSpecimens()) {
      final Element specimenXML = any.getOwnerDocument().createElement("specimen");
      specimenXML.setAttribute("collection", specimen.getCollectionCode() != null ? specimen.getCollectionCode().getID() : null);
      specimenXML.setAttribute("accession", specimen.getCatalogID());
      any.appendChild(specimenXML);
    }
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
}
