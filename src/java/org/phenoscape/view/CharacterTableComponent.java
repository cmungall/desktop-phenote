package org.phenoscape.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.util.Comparator;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.phenoscape.model.Character;
import org.phenoscape.model.PhenoscapeController;
import org.phenoscape.swing.PlaceholderRenderer;

import phenote.gui.BugWorkaroundTable;
import phenote.gui.SortDisabler;
import phenote.gui.TableColumnPrefsSaver;
import phenote.util.EverythingEqualComparator;
import phenote.util.FileUtil;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.eekboom.utils.Strings;

public class CharacterTableComponent extends PhenoscapeGUIComponent {

  private JButton addCharacterButton;
  private JButton deleteCharacterButton;
  private final SortedList<Character> sortedCharacters;

  public CharacterTableComponent(String id, PhenoscapeController controller) {
    super(id, controller);
    this.sortedCharacters = new SortedList<Character>(controller.getDataSet().getCharacters(), new EverythingEqualComparator<Character>());
  }
  
  @Override
  public void init() {
    super.init();
    this.initializeInterface();
  }

  private void initializeInterface() {
    this.setLayout(new BorderLayout());
    final EventTableModel<Character> charactersTableModel = new EventTableModel<Character>(this.sortedCharacters, new CharactersTableFormat());
    final JTable charactersTable = new BugWorkaroundTable(charactersTableModel);
    charactersTable.setSelectionModel(this.getController().getCharactersSelectionModel());
    charactersTable.setDefaultRenderer(Object.class, new PlaceholderRenderer("None"));
    charactersTable.putClientProperty("Quaqua.Table.style", "striped");
    new TableColumnPrefsSaver(charactersTable, this.getClass().getName());
    final TableComparatorChooser<Character> sortChooser = new TableComparatorChooser<Character>(charactersTable, this.sortedCharacters, false);
    sortChooser.addSortActionListener(new SortDisabler());
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

  private class CharactersTableFormat implements WritableTableFormat<Character>, AdvancedTableFormat<Character> {

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

    public Class<?> getColumnClass(int column) {
      switch(column) {
      case 0: return Integer.class;
      case 1: return String.class;
      default: return null;
      }
    }

    public Comparator<?> getColumnComparator(int column) {
      switch(column) {
      case 0: return GlazedLists.comparableComparator();
      case 1: return Strings.getNaturalComparator();
      default: return null;
      }
    }

  }

  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
