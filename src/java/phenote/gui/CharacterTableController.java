package phenote.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import phenote.config.Config;
import phenote.dataadapter.CharListChangeEvent;
import phenote.dataadapter.CharListChangeListener;
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.LoadSaveManager;
import phenote.datamodel.CharField;
import phenote.datamodel.CharacterI;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;
import phenote.edit.EditManager;
import phenote.util.FileUtil;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

public class CharacterTableController {
  
  private static final String SAVE_STRING = "Save Data";
  private String representedGroup = "default";
  private SortedList<CharacterI> sortedCharacters;
  private FilterList<CharacterI> filteredCharacters;
  private EventSelectionModel<CharacterI> selectionModel;
  private CharacterTableFormat tableFormat;
  private LoadSaveManager loadSaveManager;
  private JPanel characterTablePanel; // initialized by swix
  private JTable characterTable; // initialized by swix
  private JTextField filterField; // initialized by swix
  private JButton duplicateButton; // initialized by swix
  private JButton deleteButton; // initialized by swix
  private JButton commitButton; // initialized by swix
  private JButton graphButton; // initialized by swix
  
  public CharacterTableController(String groupName) {
    if (groupName != null) this.representedGroup = groupName;
    this.loadPanelLayout();
    this.sortedCharacters = new SortedList<CharacterI>(this.getCharacterListManager().getCharacterList().getList(), new EverythingEqualComparator<CharacterI>());
    this.sortedCharacters.setMode(SortedList.AVOID_MOVING_ELEMENTS);
    this.filteredCharacters = new FilterList<CharacterI>(this.sortedCharacters, new TextComponentMatcherEditor<CharacterI>(this.filterField, new CharacterFilterator()));
    this.selectionModel = new EventSelectionModel<CharacterI>(this.filteredCharacters);
    this.tableFormat = new CharacterTableFormat(this.representedGroup);
    this.getEditManager().addCharChangeListener(new CharacterChangeListener());
    CharacterListManager.getCharListMan(this.representedGroup).addCharListChangeListener(new CharacterListChangeListener());
    this.initializeInterface();
    this.addInitialBlankCharacter();
  }
    
  public JPanel getCharacterTablePanel() {
    return this.characterTablePanel;
  }
  
  public void addNewCharacter() {
    this.getEditManager().addNewCharacter();
  }
  
  public void duplicateSelectedCharacters() {
    this.getEditManager().copyChars(this.selectionModel.getSelected());
  }
  
  public void deleteSelectedCharacters() {
    final int maxSelectedRow = this.selectionModel.getMaxSelectionIndex();
    this.getEditManager().deleteChars(this.selectionModel.getSelected());
    if ((maxSelectedRow > -1) && (!this.filteredCharacters.isEmpty())) {
      final int rowToSelect = Math.min((this.filteredCharacters.size() - 1), maxSelectedRow);
      this.selectionModel.setSelectionInterval(rowToSelect, rowToSelect);
    }
    if (this.getCharacterListManager().getCharacterList().isEmpty()) {
      this.addInitialBlankCharacter();
    }
  }
  
  public void undo() {
    this.getEditManager().undo();
  }
  
  public void commitCharacters() {
    if (Config.inst().hasQueryableDataAdapter()) {
      Config.inst().getQueryableDataAdapter().commit(this.getCharacterListManager().getCharacterList());
    }
    else {
      this.getLoadSaveManager().saveData();
    }
  }
  
  public EventSelectionModel<CharacterI> getSelectionModel() {
    return this.selectionModel;
  }
  
  private void loadPanelLayout() {
    SwingEngine swix = new SwingEngine(this);
    try {
      this.characterTablePanel = (JPanel)swix.render(FileUtil.findUrl("character_table_panel.xml"));
    } catch (Exception e) {
      log().fatal("Unable to render character table interface", e);
    }
  }
  
  private void initializeInterface() { 
    final EventTableModel<CharacterI> eventTableModel = new EventTableModel<CharacterI>(this.filteredCharacters, this.tableFormat);
    this.characterTable.setModel(eventTableModel);
    new TableComparatorChooser<CharacterI>(characterTable, this.sortedCharacters, false);
    this.characterTable.setSelectionModel(this.selectionModel);
    this.characterTable.putClientProperty("Quaqua.Table.style", "striped");
    this.commitButton.setText(this.getCommitButtonString());
    if (!Config.inst().uvicGraphIsEnabled()) {
      this.graphButton.getParent().remove(this.graphButton);
    }
    this.filterField.putClientProperty("Quaqua.TextField.style", "search");
    this.characterTable.addMouseListener(new PopupListener(new TableRightClickMenu(this.characterTable)));
    this.characterTablePanel.validate();
    this.selectionModel.addListSelectionListener(new SelectionListener());
    new TableColumnPrefsSaver(this.characterTable, this.getTableAutoSaveName());
  }
  
  private void addInitialBlankCharacter() {
    this.getEditManager().addInitialCharacter();
  }
  
  private void setSelectionWithCharacters(List<CharacterI> characters) {
    this.clearFilter();
    this.selectionModel.clearSelection();
    for (CharacterI character : characters) {
      final int index = this.filteredCharacters.indexOf(character);
      if (index > -1) {
        this.selectionModel.addSelectionInterval(index, index);
        Rectangle rect = this.characterTable.getCellRect(index, 0, false);
        this.characterTable.scrollRectToVisible(rect);
      }
    }
  }
  
  private void updateButtonStates() {
    final boolean hasSelection = !this.selectionModel.isSelectionEmpty();
    this.duplicateButton.setEnabled(hasSelection);
    this.deleteButton.setEnabled(hasSelection);
  }
  
  private void clearFilter() {
    this.filterField.setText("");
  }

  private void updateCharacterForGlazedLists(CharacterI character) {
    final int index = this.getCharacterListManager().getCharacterList().getList().indexOf(character);
    if (index > -1) {
      this.getCharacterListManager().getCharacterList().getList().set(index, character);
    }
  }
  
  private EditManager getEditManager() {
    return EditManager.getEditManager(this.representedGroup);
  }
  
  private CharacterListManager getCharacterListManager() {
    return CharacterListManager.getCharListMan(this.representedGroup);
  }
  
  private LoadSaveManager getLoadSaveManager() {
    if (this.loadSaveManager == null) {
      this.loadSaveManager = new LoadSaveManager(this.getCharacterListManager());
    }
    return this.loadSaveManager;
  }
  
  private String getCommitButtonString() {
    if (Config.inst().hasQueryableDataAdapter())
      return Config.inst().getQueryableDataAdapter().getCommitButtonLabel();
    else return CharacterTableController.SAVE_STRING;
  }
  
  private String getTableAutoSaveName() {
    return Config.inst().getConfigName() + this.representedGroup + "CharacterTable";
  }
  
  private static Logger log() {
    return Logger.getLogger(CharacterTableController.class);
  }
  
  private class CharacterChangeListener implements CharChangeListener {
    public void charChanged(CharChangeEvent e) {
      if (e.isUpdate()) {
        for (CharacterI character : e.getTransaction().getCharacters()) {
          CharacterTableController.this.updateCharacterForGlazedLists(character);
        }
      } else if (e.isAdd()) {
        CharacterTableController.this.setSelectionWithCharacters(e.getTransaction().getCharacters());
      }
    }
  }
  
  private class CharacterListChangeListener implements CharListChangeListener {
    public void newCharList(CharListChangeEvent e) {
      CharacterTableController.this.clearFilter();
      if (!CharacterTableController.this.filteredCharacters.isEmpty()) {
        CharacterTableController.this.selectionModel.setSelectionInterval(0, 0);
      }
    }
  }
  
  private class SelectionListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent e) {
      CharacterTableController.this.updateButtonStates();
    }
  }
  
  private static class EverythingEqualComparator<T> implements Comparator<T> {
    public int compare(T o1, T o2) {
      return 0;
    }
  }
  
  private static class CharacterFilterator implements TextFilterator<CharacterI> {
    public void getFilterStrings(List<String> baseList, CharacterI character) {
      for (CharField charField : character.getAllCharFields()) {
        baseList.add(character.getValueString(charField));
      }
    }
  }
  
  private class PopupListener extends MouseAdapter {
    JPopupMenu popup;
    int col; int row;
    Point p;
    PopupListener(JPopupMenu popupMenu) {
      popup = popupMenu;
    }

    public void mousePressed(MouseEvent e) {
      maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
      maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
      p= e.getPoint();
      col = CharacterTableController.this.characterTable.getTableHeader().columnAtPoint(p);
      row = CharacterTableController.this.characterTable.rowAtPoint(p);
      if (e.isPopupTrigger()) {
        popup.show(e.getComponent(),
            e.getX(), e.getY());
      }
    }
  }
  
}
 