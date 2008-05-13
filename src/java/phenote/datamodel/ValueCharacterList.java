package phenote.datamodel;

import java.util.List;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOObject;

import phenote.config.Config;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;
import phenote.edit.EditManager;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

/**
 * A CharacterListI implementation which represents the contents of a CharFieldValue as a 
 * list of characters which can be edited in a separate character table view.  Currently 
 * the field value is parsed into separate fields by splitting on " " - this is useful for 
 * transparently interacting with the existing CharFieldValue data model, but not very robust.
 * @author Jim Balhoff
 */
public class ValueCharacterList implements CharacterListI {
  
  private EventList<CharacterI> characters = new BasicEventList<CharacterI>();
  private CharFieldValue value;
  private CharacterChangeListener listener = new CharacterChangeListener();
  private static String SEPARATOR = " ";

  public ValueCharacterList(CharFieldValue aValue) {
    this.value = aValue;
    for (CharFieldValue cfv : this.value.getCharFieldValueList()) {
      this.characters.add(this.characterFromValue(cfv));
    }
    this.getEditManager().addCharChangeListener(this.listener);
  }

  public void add(CharacterI c) {
    final CharFieldValue cfv = this.valueFromCharacter(c);
    this.characters.add(c);
    this.value.addKid(cfv);
  }

  public void add(int order, CharacterI c) {
    final CharFieldValue cfv = this.valueFromCharacter(c);
    this.characters.add(order, c);
    this.value.insertKid(order, cfv);
  }

  public void clear() {
    this.characters.clear();
    this.value.removeAllKids();
  }

  public boolean equals(CharacterListI cl) {
    return false;
  }

  public CharacterI get(int i) {
    return this.characters.get(i);
  }

  public EventList<CharacterI> getList() {
    return this.characters;
  }

  public int indexOf(CharacterI c) {
    return this.characters.indexOf(c);
  }

  public boolean isEmpty() {
    return this.characters.isEmpty();
  }

  public void remove(int i) {
    this.characters.remove(i);
    this.value.removeKid(this.value.getCharFieldValueList().get(i));
  }

  public void remove(CharacterI c) {
    final int index = this.characters.indexOf(c);
    this.characters.remove(c);
    this.value.removeKid(this.value.getCharFieldValueList().get(index));
  }

  public int size() {
    return this.characters.size();
  }
  
  public void removeCharChangeListener() {
    this.getEditManager().removeCharChangeListener(listener);
  }
  
  private EditManager getEditManager() {
    return EditManager.getEditManager(this.value.getCharField().getComponentsGroup());
  }
  
  private CharFieldValue valueFromCharacter(CharacterI character) {
    boolean firstValue = true;
    final StringBuffer buffer = new StringBuffer();
    for (String fieldName : Config.inst().getFieldsInGroup(this.value.getCharField().getComponentsGroup())) {
      if (!firstValue) buffer.append(SEPARATOR);
      firstValue = false;
      try {
        buffer.append(character.getValueString(fieldName));
      } catch (CharFieldException e) {
        log().error("Error getting value for charfield: " + fieldName, e);
      }
    }
    final CharFieldValue cfv = new CharFieldValue(buffer.toString(), this.value.getCharacter(), this.value.getCharField());
    cfv.setIsList(false);
    return cfv;
  }
  
  private CharacterI characterFromValue(CharFieldValue aValue) {
    final List<String> fieldNames = Config.inst().getFieldsInGroup(this.value.getCharField().getComponentsGroup());
    String[] values = aValue.getValueAsString().split(SEPARATOR, fieldNames.size());
    final CharacterI character = CharacterIFactory.makeChar();
    for (int i = 0; i < values.length; i++) {
      try {
        final String text;
        final CharField currentField = CharFieldManager.inst().getCharFieldForName(fieldNames.get(i));
        if (currentField.isTerm()) {
          text = this.getTermID(values[i], currentField.getOntology());
        } else {
          text = values[i];
        }
        character.setValue(currentField, text);
      } catch (CharFieldException e) {
        log().error("Couldn't get charfield: " + fieldNames.get(i), e);
      }
    }
    return character;
  }
  
  private void updateCharacterValue(CharacterI character) {
    final int index = this.characters.indexOf(character);
    final CharFieldValue oldValue = this.value.getCharFieldValueList().get(index);
    this.value.removeKid(oldValue);
    this.value.insertKid(index, this.valueFromCharacter(character));
  }
  
  private String getTermID(String termName, Ontology ontology) {
    for (OBOObject term :ontology.getSortedTerms()) {
      if (term.getName().equals(termName)) return term.getID();
    }
    return null;
  }

  private class CharacterChangeListener implements CharChangeListener {
    public void charChanged(CharChangeEvent e) {
      if (e.isUpdate()) {
        for (CharacterI character : e.getTransaction().getCharacters()) {
          updateCharacterValue(character);
        }
      }
    }
  }
  
  @SuppressWarnings("unused")
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
}
