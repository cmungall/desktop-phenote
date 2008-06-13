package org.phenoscape.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.phenoscape.model.Character;
import org.phenoscape.model.PhenoscapeController;

import phenote.gui.BugWorkaroundTable;
import phenote.util.FileUtil;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

public class CharacterTableComponent extends PhenoscapeGUIComponent {

  private JButton addCharacterButton;
  private JButton deleteCharacterButton;

  public CharacterTableComponent(String id, PhenoscapeController controller) {
    super(id, controller);
  }
  
  @Override
  public void init() {
    super.init();
    this.initializeInterface();
  }

  private void initializeInterface() {
    this.setLayout(new BorderLayout());
    final EventTableModel<Character> charactersTableModel = new EventTableModel<Character>(this.getController().getDataSet().getCharacters(), new CharactersTableFormat());
    final JTable charactersTable = new BugWorkaroundTable(charactersTableModel);
    charactersTable.setSelectionModel(this.getController().getCharactersSelectionModel());
    charactersTable.putClientProperty("Quaqua.Table.style", "striped");
    this.add(new JScrollPane(charactersTable), BorderLayout.CENTER);
    this.add(this.createToolBar(), BorderLayout.NORTH);
  }

  private void addCharacter() {
    final Character character = this.getController().getDataSet().newCharacter();
    final int index = this.getController().getDataSet().getCharacters().indexOf(character);
    this.getController().getCharactersSelectionModel().setSelectionInterval(index, index);
  }
  
  private void deleteSelectedCharacter() {
    final Character character = this.getSelectedCharacter();
    if (character != null) { this.getController().getDataSet().removeCharacter(character); }
  }
  
  private Character getSelectedCharacter() {
    final EventList<Character> selected = this.getController().getCharactersSelectionModel().getSelected();
    if (selected.size() == 1) {
      return selected.get(0);
    } else {
      return null;
    }
  }
  
  private JToolBar createToolBar() {
    final JToolBar toolBar = new JToolBar();
    try {
      this.addCharacterButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-add.png"))) {
          public void actionPerformed(ActionEvent e) {
            addCharacter();
          }
        });
      this.addCharacterButton.setToolTipText("Add Character");
      toolBar.add(this.addCharacterButton);
      this.deleteCharacterButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-remove.png"))) {
          public void actionPerformed(ActionEvent e) {
            deleteSelectedCharacter();
          }
        });
      this.deleteCharacterButton.setToolTipText("Delete Character");
      toolBar.add(this.deleteCharacterButton);
    } catch (FileNotFoundException e) {
      log().error("Couldn't find toolbar icons", e);
    }
    toolBar.setFloatable(false);
    return toolBar;
  }

  private class CharactersTableFormat implements WritableTableFormat<Character> {

    public boolean isEditable(Character character, int column) {
      return column == 1;
    }

    public Character setColumnValue(Character character, Object editedValue, int column) {
      if (column == 1) { character.setLabel(editedValue.toString()); }
      return character;
    }

    public int getColumnCount() {
      return 2;
    }

    public String getColumnName(int column) {
      switch(column) {
      case 0: return "Number";
      case 1: return "Character Description";
      default: return null;
      }
    }

    public Object getColumnValue(Character character, int column) {
      switch(column) {
      case 0: return getController().getDataSet().getCharacters().indexOf(character) + 1;
      case 1: return character.getLabel();
      default: return null;
      }
    }

  }

  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
