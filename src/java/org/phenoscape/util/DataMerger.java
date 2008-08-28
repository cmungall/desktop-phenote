package org.phenoscape.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.phenoscape.io.CharacterTabReader;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.State;

public class DataMerger {

  /**
   * Merge EQ annotations from a tab file into an existing data set. The "Character Number" 
   * and "State Number" columns are used to match a character (by index) and state (by symbol) 
   * in the existing data set.  If the index falls outside the current range of characters, 
   * a new character is appended to the existing data set.  If a state with the given symbol does 
   * not exist a new state is appended to the given character.
   */
  public static void mergeCharacters(CharacterTabReader reader, DataSet existingData) {
    final Map<Integer, Character> characterMap = reader.getCharacters();
    final List<Entry<Integer, Character>> unusedEntries = new ArrayList<Entry<Integer, Character>>();
    for (Entry<Integer, Character> entry : characterMap.entrySet()) {
      if (existingData.getCharacters().size() >= entry.getKey()) {
        // merge
        log().debug("Merging character originally numbered: " + entry.getKey());
        final int entryIndex = entry.getKey() - 1;
        final Character character = existingData.getCharacters().get(entryIndex);
        character.setLabel(entry.getValue().getLabel());
        for (State newState: entry.getValue().getStates()) {
          final State state = findState(character.getStates(), newState.getSymbol());
          if (state != null) {
            state.setLabel(newState.getSymbol());
            state.getPhenotypes().clear();
            state.getPhenotypes().addAll(newState.getPhenotypes());
          } else {
            character.addState(newState);
          }
        }
      } else {
        unusedEntries.add(entry);
      }
    }
    Collections.sort(unusedEntries, new Comparator<Entry<Integer, Character>>() {
      public int compare(Entry<Integer, Character> o1, Entry<Integer, Character> o2) {
        return o1.getKey().compareTo(o2.getKey());
      }
    });
    for (Entry<Integer, Character> entry : unusedEntries) {
      log().debug("Adding new character originally numbered: " + entry.getKey());
      existingData.addCharacter(entry.getValue());
    }

  }

  private static State findState(List<State> states, String symbol) {
    for (State state: states) {
      if (symbol.equals(state.getSymbol())) { return state; }
    }
    return null;
  }
  
  private static Logger log() {
    return Logger.getLogger(DataMerger.class);
  }

}
