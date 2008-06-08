package org.phenoscape.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.phenoscape.model.Character;
import org.phenoscape.model.PhenoscapeController;
import org.phenoscape.model.State;

import phenote.util.FileUtil;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

public class CharacterBrowserComponent extends PhenoscapeGUIComponent {

  private JButton addCharacterButton;
  private JButton deleteCharacterButton;
  private JButton addStateButton;
  private JButton deleteStateButton;

  public CharacterBrowserComponent(String id, PhenoscapeController controller) {
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
    final JTable charactersTable = new JTable(charactersTableModel);
    charactersTable.setSelectionModel(this.getController().getCharactersSelectionModel());
    charactersTable.putClientProperty("Quaqua.Table.style", "striped");
    final JPanel charactersPanel = new JPanel(new BorderLayout());
    charactersPanel.add(new JScrollPane(charactersTable), BorderLayout.CENTER);
    charactersPanel.add(this.createCharactersToolBar(), BorderLayout.NORTH);
    final EventTableModel<State> statesTableModel = new EventTableModel<State>(this.getController().getStatesForCurrentCharacterSelection(), new StatesTableFormat());
    final JTable statesTable = new JTable(statesTableModel);
    statesTable.setSelectionModel(this.getController().getCurrentStatesSelectionModel());
    statesTable.putClientProperty("Quaqua.Table.style", "striped");
    final JPanel statesPanel = new JPanel(new BorderLayout());
    statesPanel.add(new JScrollPane(statesTable), BorderLayout.CENTER);
    statesPanel.add(this.createStatesToolBar(), BorderLayout.NORTH);
    this.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, charactersPanel, statesPanel), BorderLayout.CENTER);
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
  
  private void addState() {
    final Character character = this.getSelectedCharacter();
    if (character != null) {
      final State state = character.newState();
      final int index = this.getController().getStatesForCurrentCharacterSelection().indexOf(state);
      this.getController().getCurrentStatesSelectionModel().setSelectionInterval(index, index);
    }
  }
  
  private void deleteSelectedState() {
    final Character character = this.getSelectedCharacter();
    if (character != null) {
      final State state = this.getSelectedState();
      if (state != null) { character.removeState(state); }
    }
  }
  
  private Character getSelectedCharacter() {
    final EventList<Character> selected = this.getController().getCharactersSelectionModel().getSelected();
    if (selected.size() == 1) {
      return selected.get(0);
    } else {
      return null;
    }
  }
  
  private State getSelectedState() {
    final EventList<State> selected = this.getController().getCurrentStatesSelectionModel().getSelected();
    if (selected.size() == 1) {
      return selected.get(0);
    } else {
      return null;
    }
  }
  
  private JToolBar createCharactersToolBar() {
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
  
  private JToolBar createStatesToolBar() {
    final JToolBar toolBar = new JToolBar();
    try {
      this.addStateButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-add.png"))) {
          public void actionPerformed(ActionEvent e) {
            addState();
          }
        });
      this.addStateButton.setToolTipText("Add State");
      toolBar.add(this.addStateButton);
      this.deleteStateButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-remove.png"))) {
          public void actionPerformed(ActionEvent e) {
            deleteSelectedState();
          }
        });
      this.deleteStateButton.setToolTipText("Delete State");
      toolBar.add(this.deleteStateButton);
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

  private class StatesTableFormat implements WritableTableFormat<State> {

    public boolean isEditable(State state, int column) {
      return true;
    }

    public State setColumnValue(State state, Object editedValue, int column) {
      switch(column) {
      case 0: state.setSymbol(editedValue.toString()); break;
      case 1: state.setLabel(editedValue.toString()); break;
      }
      return state;
    }

    public int getColumnCount() {
      return 2;
    }

    public String getColumnName(int column) {
      switch(column) {
      case 0: return "Symbol";
      case 1: return "State Description";
      default: return null;
      }
    }

    public Object getColumnValue(State state, int column) {
      switch(column) {
      case 0: return state.getSymbol();
      case 1: return state.getLabel();
      default: return null;
      }
    }
    
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
