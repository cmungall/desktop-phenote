package org.phenoscape.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.biojava.bio.seq.io.ParseException;
import org.biojavax.bio.phylo.io.nexus.CharactersBlock;
import org.biojavax.bio.phylo.io.nexus.NexusBlock;
import org.biojavax.bio.phylo.io.nexus.NexusFileBuilder;
import org.biojavax.bio.phylo.io.nexus.NexusFileFormat;
import org.biojavax.bio.phylo.io.nexus.TaxaBlock;
import org.phenoscape.model.Character;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;

public class NEXUSReader {
  
  private final List<Taxon> taxa = new ArrayList<Taxon>();
  private final List<Character> characters = new ArrayList<Character>();
  private final Map<String, Map<String, String>> matrix = new HashMap<String, Map<String, String>>();
  
  public NEXUSReader(File nexusFile) throws ParseException, IOException {
    this.parseNEXUS(nexusFile);
  }
  
  public List<Taxon> getTaxa() {
    return this.taxa;
  }
  
  public List<Character> getCharacters() {
    return this.characters;
  }
  
  public Map<String, Map<String, String>> getMatrix() {
    return this.matrix;
  }
  
  @SuppressWarnings("unchecked")
  private void parseNEXUS(File nexusFile) throws ParseException, IOException {
    final NexusFileBuilder builder = new NexusFileBuilder(); 
    NexusFileFormat.parseFile(builder, nexusFile);
    Iterator<NexusBlock> blocks = builder.getNexusFile().blockIterator();
    while (blocks.hasNext()) {
      final NexusBlock block = blocks.next();
      if (block instanceof TaxaBlock) {
        final TaxaBlock taxaBlock = (TaxaBlock)block;
        for (String taxLabel : (List<String>)taxaBlock.getTaxLabels()) {
          final Taxon newTaxon = new Taxon();
          newTaxon.setPublicationName(taxLabel);
          this.taxa.add(newTaxon);
        }
      }
      if (block instanceof CharactersBlock) {
        final CharactersBlock charactersBlock = (CharactersBlock)block;
        for (int i = 0; i < charactersBlock.getDimensionsNChar(); i++) {
          this.characters.add(new Character());
        }
        for (Object o : charactersBlock.getAllCharStates()) {
          final String charNumberString = o.toString();
          final Character newChar = this.characters.get(Integer.parseInt(charNumberString) - 1);
          newChar.setLabel(charactersBlock.getCharStateLabel(charNumberString));
          for (int i = 0; i < charactersBlock.getSymbols().size(); i++) {
            final String symbol = (String)(charactersBlock.getSymbols().get(i));
            final State newState = new State();
            newState.setSymbol(symbol);
            if (i < charactersBlock.getCharStateLabelKeywords(charNumberString).size()) {
              newState.setLabel(charactersBlock.getCharStateLabelKeywords(charNumberString).get(i).toString());
            }            
            newChar.addState(newState);
          }
        }
        final Set<State> usedStates = new HashSet<State>();
        for (Taxon taxon : this.taxa) {
          final Map<String, String> currentMap = new HashMap<String, String>();
          this.matrix.put(taxon.getNexmlID(), currentMap);
          List states = charactersBlock.getMatrixData(taxon.getPublicationName());
          states.remove(0);
          for (int i = 0; i < states.size(); i++) {
            if (states.get(i) instanceof String) {
              final String symbol = (String)(states.get(i));
              if (i < this.characters.size()) {
                final Character character = this.characters.get(i);
                for (State state : character.getStates()) {
                  if (state.getSymbol().equals(symbol)) {
                    currentMap.put(character.getNexmlID(), state.getNexmlID());
                    usedStates.add(state);
                  }
                }
              } 
            } // else should handle polymorphism
          }
        }
        // remove unused states
        for (Character character : this.characters) {
          final List<State> statesToRemove = new ArrayList<State>();
          for (State state : character.getStates()) {
            if (!usedStates.contains(state) && state.getLabel() == null) {
              statesToRemove.add(state);
            }
          }
          character.getStates().removeAll(statesToRemove);
        }
      }
    }
  }
  
  @SuppressWarnings("unused")
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
}
