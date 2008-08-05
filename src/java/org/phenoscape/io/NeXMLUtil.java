package org.phenoscape.io;

import java.util.Arrays;
import java.util.List;

import org.nexml.x10.AbstractBlock;
import org.nexml.x10.AbstractStates;
import org.nexml.x10.NexmlDocument;
import org.nexml.x10.StandardCells;
import org.nexml.x10.StandardFormat;
import org.nexml.x10.Taxa;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NeXMLUtil {
  
  public static Taxa findOrCreateTaxa(NexmlDocument doc, String id) {
    for (Taxa taxaBlock : doc.getNexml().getOtusArray()) {
      if (taxaBlock.getId().equals(id)) return taxaBlock;
    }
    // no taxa block was found, so create one for that id
    final Taxa newTaxa = doc.getNexml().addNewOtus();
    newTaxa.setId(id);
    return newTaxa;
  }
  
  public static AbstractBlock findOrCreateCharactersBlock(NexmlDocument doc, String id) {
    for (AbstractBlock block : doc.getNexml().getCharactersArray()) {
      if (block.getId().equals(id)) return block;
    }
    // no characters block was found, so create one for that id
    final AbstractBlock newBlock = StandardCells.Factory.newInstance();
    final AbstractBlock[] currentBlocksArray = doc.getNexml().getCharactersArray();
    final List<AbstractBlock> currentBlocks = Arrays.asList(currentBlocksArray);
    currentBlocks.add(newBlock);
    doc.getNexml().setCharactersArray(currentBlocks.toArray(currentBlocksArray));
    newBlock.setId(id);
    return newBlock;
  }
  
  public static AbstractStates findOrCreateStates(StandardFormat format, String id) {
    for (AbstractStates abstractStates : format.getStatesArray()) {
      if (abstractStates.getId().equals(id)) return abstractStates;
    }
    // no states block was found, so create one for that id
    final AbstractStates newStates = format.addNewStates();
    newStates.setId(id);
    return newStates;
  }
  
  public static Element getFirstChildWithTagName(Element parent, String tagName) {
    final NodeList elements = parent.getElementsByTagName(tagName);
    return (elements.getLength() > 0) ? (Element)(elements.item(0)) : null;
  }
  
  public static String getTextContent(Node node) {
    // this method is useful when DOM Level 3 "getTextContent" is not implemented
    if (node.getNodeType() == Node.TEXT_NODE) { return ((CharacterData)node).getData(); }
    final StringBuffer pieces = new StringBuffer();
    final NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      final Node child = children.item(i);
      if (child.getNodeType() == Node.TEXT_NODE) {
        pieces.append(((CharacterData)child).getData());
      } else {
        pieces.append(getTextContent(child));
      }
    }
    return pieces.toString();
  }

}
