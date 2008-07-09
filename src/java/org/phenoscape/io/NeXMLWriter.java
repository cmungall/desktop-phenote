package org.phenoscape.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.nexml.x10.NexmlDocument;
import org.phenoscape.model.Character;
import org.phenoscape.model.Taxon;

public class NeXMLWriter {
  
  private final NexmlDocument xmlDoc;
  private final String charactersBlockID;
  private List<Character> characters = new ArrayList<Character>();
  private List<Taxon> taxa = new ArrayList<Taxon>();
  
  public NeXMLWriter(String charactersBlockID) {
    this(charactersBlockID, NexmlDocument.Factory.newInstance());
  }
  
  public NeXMLWriter(String charactersBlockID, NexmlDocument startingDoc) {
    this.charactersBlockID = charactersBlockID;
    this.xmlDoc = startingDoc;
  }
  
  public void setCharacters(List<Character> characters) {
    this.characters = characters;
  }

  public void setTaxa(List<Taxon> taxa) {
    this.taxa = taxa;
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
    
    return newDoc;
  }
  
}
