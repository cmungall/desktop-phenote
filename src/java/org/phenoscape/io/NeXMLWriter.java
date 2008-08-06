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
import org.nexml.x10.AbstractBlock;
import org.nexml.x10.AbstractChar;
import org.nexml.x10.AbstractState;
import org.nexml.x10.AbstractStates;
import org.nexml.x10.Dict;
import org.nexml.x10.NexmlDocument;
import org.nexml.x10.StandardChar;
import org.nexml.x10.StandardState;
import org.nexml.x10.StandardStates;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.State;
import org.w3c.dom.Element;

public class NeXMLWriter {
  
  private final NexmlDocument xmlDoc;
  private final String charactersBlockID;
  private DataSet data;
  
  public NeXMLWriter(String charactersBlockID) {
    this(charactersBlockID, NexmlDocument.Factory.newInstance());
  }
  //TODO fix this to handle null doc
  public NeXMLWriter(String charactersBlockID, NexmlDocument startingDoc) {
    this.charactersBlockID = charactersBlockID;
    this.xmlDoc = startingDoc;
  }
  
  public void setDataSet(DataSet data) {
    this.data = data;
  }
  
  public void write(File aFile) throws IOException {
    this.constructXMLDoc().save(aFile);
  }
  
  public void write(OutputStream aStream) throws IOException {
    this.constructXMLDoc().save(aStream);
  }
  
  public void write(Writer aWriter) throws IOException {
    this.constructXMLDoc().save(aWriter);
  }
  
  private NexmlDocument constructXMLDoc() {
    final NexmlDocument newDoc = (NexmlDocument)(xmlDoc.copy());
    Dict metadata = NeXMLUtil.findOrCreateMetadataDict(newDoc);
    this.writeToMetadata(metadata, this.data.getCurators(), this.data.getPublication(), this.data.getPublicationNotes());
    final AbstractBlock charBlock = NeXMLUtil.findOrCreateCharactersBlock(newDoc, this.charactersBlockID);
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
        //TODO this doesn't work
        xmlState.getSymbol().setStringValue(state.getSymbol());
      }
      usableStatesBlock.setStateArray(newStates.toArray(new AbstractState[] {}));
    }
    charBlock.getFormat().setCharArray(newCharacters.toArray(new AbstractChar[] {}));
    charBlock.getFormat().setStatesArray(newStatesBlocks.toArray(new AbstractStates[] {}));
    return newDoc;
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
    log().debug("Writing metadata");
    final Element any = NeXMLUtil.getFirstChildWithTagName(((Element)(metadata.getDomNode())), "any");
    final Element curators = NeXMLUtil.getFirstChildWithTagName(any, "curators");
    NeXMLUtil.setTextContent(curators, curatorsText);
    final Element publication = NeXMLUtil.getFirstChildWithTagName(any, "publication");
    NeXMLUtil.setTextContent(publication, publicationText);
    final Element pubNotes = NeXMLUtil.getFirstChildWithTagName(any, "publicationNotes");
    NeXMLUtil.setTextContent(pubNotes, pubNotesText);
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
}
