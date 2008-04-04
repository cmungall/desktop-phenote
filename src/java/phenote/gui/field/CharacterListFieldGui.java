package phenote.gui.field;

import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;
import org.bbop.framework.ComponentManager;
import org.bbop.framework.GUIComponent;

import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharacterI;
import phenote.datamodel.ValueCharacterList;
import phenote.edit.CompoundTransaction;
import phenote.gui.CharacterTable;

/**
 * A CharFieldGui which manages a separate CharacterList and CharacterTable representing the contents of the field value it edits.
 * This way a Character can have a list of Characters as the value for a single field.
 * @author Jim Balhoff
 */
public class CharacterListFieldGui extends CharFieldGui {
  
  private CharFieldValue currentValue;
  private ValueCharacterList characterList;
  private JTextField textField;

  public CharacterListFieldGui(CharField charField) {
    super(charField);
    this.textField = new JTextField();
    this.textField.setEditable(false);
  }

  @Override
  public TableCellEditor getTableCellEditor() {
    // can't edit lists in the table
    return null;
  }

  @Override
  protected CharFieldValue getCharFieldValue() {
    return this.currentValue;
  }

  @Override
  protected String getText() {
    return null;
  }

  @Override
  protected JComponent getUserInputGui() {
    return this.textField;
  }

  @Override
  protected boolean hasFocus() {
    return false;
  }

  @Override
  protected void setCharFieldValue(CharFieldValue value) {
    final List<CharacterI> chars = this.getSelectedChars();
    if (chars.isEmpty() || chars.size() > 1) {
      this.currentValue = null;
      this.disableTable();
      return;
    }
    this.enableTableControls(true);
    if (value == null || value.isEmpty()) {
      // need to make sure the character has a charfieldvalue with an empty list to use
      final CompoundTransaction ct = CompoundTransaction.makeUpdate(chars, this.getCharField(), CharFieldValue.makeListParentValue(chars.get(0), this.getCharField()));
      this.getEditManager().updateModel(this, ct);
      this.currentValue = chars.get(0).getValue(this.getCharField());
    } else {
      this.currentValue = value;
    }
    if (this.characterList != null) this.characterList.removeCharChangeListener();
    this.setText(this.currentValue.getValueAsString());
    this.characterList = new ValueCharacterList(this.currentValue);
    CharacterListManager.getCharListMan(this.getCharField().getComponentsGroup()).setCharacterList(this.currentValue, this.characterList);
  }

  @Override
  protected void setText(String text) {
    this.textField.setText(text);
  }

  @Override
  protected void updateModel() {}
  
  @Override
  protected void setGuiForNoSelection() {
    super.setGuiForNoSelection();
    this.currentValue = null;
    this.disableTable();
  }
  
  @Override
  protected void setGuiForMultipleValues() {
    super.setGuiForMultipleValues();
    this.currentValue = null;
    this.disableTable();
  }
  
  private void disableTable() {
    if (this.characterList != null) this.characterList.removeCharChangeListener();
    this.characterList = new ValueCharacterList(CharFieldValue.emptyValue(null, this.getCharField()));
    CharacterListManager.getCharListMan(this.getCharField().getComponentsGroup()).setCharacterList(this, this.characterList);
    this.enableTableControls(false);
  }
  
  private void enableTableControls(boolean enable) {
    final CharacterTable table = this.findCharacterTable();
    if (table != null) table.setControlsEnabled(enable);
  }
  
  private CharacterTable findCharacterTable() {
    for (GUIComponent component : ComponentManager.getManager().getActiveComponents()) {
      //TODO there should be a better way to find particular components
      if (component instanceof CharacterTable) {
        if (((CharacterTable)component).getGroup().equals(this.getCharField().getComponentsGroup())) {
          return (CharacterTable)component;
        }
      }
    }
    return null;
  }
  
  @SuppressWarnings("unused")
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
