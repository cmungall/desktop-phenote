package phenote.charactertemplate;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.infonode.docking.DockingWindowAdapter;
import net.infonode.docking.View;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractGUIComponent;
import org.bbop.framework.ComponentManager;
import org.bbop.framework.dock.idw.IDWDriver;
import org.swixml.SwingEngine;

import phenote.config.Config;
import phenote.dataadapter.CharListChangeEvent;
import phenote.dataadapter.CharListChangeListener;
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.LoadSaveManager;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;
import phenote.edit.EditManager;
import phenote.gui.CharacterTableSource;
import phenote.gui.TableColumnPrefsSaver;
import phenote.gui.field.CharFieldMatcherEditor;
import phenote.gui.field.FieldPanel;
import phenote.util.EverythingEqualComparator;
import phenote.util.FileUtil;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

public class CharacterTemplateTable extends AbstractGUIComponent implements TemplateChoiceListener, CharacterTableSource {

  private static final long serialVersionUID = -3936482953613098775L;
  private String group;
  private SortedList<CharacterI> sortedCharacters;
  private FilterList<CharacterI> filteredCharacters;
  private EventSelectionModel<CharacterI> selectionModel;
  private CharacterTemplateTableFormat tableFormat;
  private LoadSaveManager loadSaveManager;
  private CharFieldMatcherEditor filter;
  private Set<CharacterI> markedCharacters = new HashSet<CharacterI>();
  private JPanel tablePanel; // initialized by swix
  private JTable characterTable; // initialized by swix
  private JButton duplicateButton;
  private JButton deleteButton;
  private JButton markButton;
  private JButton unmarkButton;
  private JButton invertSelectionButton;
  private JButton generateButton;
  private JPanel filterPanel; // initialized by swix
  private DockingWindowListener focusListener;

  public CharacterTemplateTable(String group, String id) {
    super(id);
    this.group = group;
  }
  
  @Override
  public void init() {
    this.loadPanelLayout();
    this.sortedCharacters = new SortedList<CharacterI>(this.getCharacterListManager().getCharacterList().getList(), new EverythingEqualComparator<CharacterI>());
    this.sortedCharacters.setMode(SortedList.AVOID_MOVING_ELEMENTS);
    this.filter = new CharFieldMatcherEditor(CharFieldManager.inst().getCharFieldListForGroup(this.group));
    this.filteredCharacters = new FilterList<CharacterI>(this.sortedCharacters, this.filter);
    this.selectionModel = new EventSelectionModel<CharacterI>(this.filteredCharacters);
    this.tableFormat = new CharacterTemplateTableFormat(this.group, this);
    this.getEditManager().addCharChangeListener(new CharacterChangeListener());
    CharacterListManager.getCharListMan(this.group).addCharListChangeListener(new CharacterListChangeListener());
    this.initializeInterface();
    this.addInitialBlankCharacter();
    this.focusListener = new DockingWindowListener();
    this.getView().addListener(this.focusListener);
  }
  
  @Override
  public void cleanup() {
    this.getView().removeListener(this.focusListener);
    super.cleanup();
  }

  public void importCharacters() {
    this.getLoadSaveManager().loadData();
  }
  
  public void exportCharacters() {
    this.getLoadSaveManager().saveData();
  }
  
  public void addNewCharacter() {
    this.getEditManager().addNewCharacter();
  }
  
  public void deleteSelectedCharacters() {
    this.getEditManager().deleteChars(this.getSelectionModel().getSelected());
  }
  
  public void duplicateSelectedCharacters() {
    this.getEditManager().copyChars(this.getSelectionModel().getSelected());
  }
  
  public void markSelectedCharacters() {
    this.setCharactersAreMarked(this.getSelectionModel().getSelected(), true);
  }
  
  public void unmarkSelectedCharacters() {
    this.setCharactersAreMarked(this.getSelectionModel().getSelected(), false);
  }
  
  public void undo() {
    this.getEditManager().undo();
  }
  
  public void invertMarkedCharacters() {
    for (CharacterI character : this.filteredCharacters) {
      this.setCharacterIsMarked(character, !this.isCharacterMarked(character));
    }
  }
  
  public boolean isCharacterMarked(CharacterI character) {
    return this.markedCharacters.contains(character);
  }
  
  public void setCharacterIsMarked(CharacterI character, boolean selected) {
    if (selected) {
      this.markedCharacters.add(character);
    } else {
      this.markedCharacters.remove(character);
    }
    this.updateCharacterForGlazedLists(character);
  }
  
  public List<CharacterI> getMarkedCharacters() {
    // make a list to make sure the characters are in the same order as they are in the character list
    List<CharacterI> characters = new ArrayList<CharacterI>();
    for (CharacterI character : this.filteredCharacters) {
      if (this.isCharacterMarked(character)){
        characters.add(character);
      }
    }
    return characters;
  }
  
  public void setMarkedCharacters(Collection<CharacterI> charactersToMark) {
    for (CharacterI character : this.sortedCharacters) {
      this.setCharacterIsMarked(character, charactersToMark.contains(character));
    }
  }
  
  public void setCharactersAreMarked(List<CharacterI> characters, boolean marked) {
    for (CharacterI character : characters) {
      this.setCharacterIsMarked(character, marked);
    }
  }
  
  public void generateCharacters() {
    EditManager.inst().copyChars(this.getMarkedCharacters());
    //TODO should bring main character table to front - need to find way to do this
  }
  
  public EventSelectionModel<CharacterI> getSelectionModel() {
    return this.selectionModel;
  }
  
  public List<CharacterI> getAllCharacters() {
    return this.sortedCharacters;
  }
  
  public void templateChoiceChanged(TemplateChooser source) {
    this.setMarkedCharacters(source.getChosenTemplates(Collections.unmodifiableList(this.sortedCharacters)));
  //TODO should should come to front
  }
  
  public void cut() {
    this.copy();
    this.deleteSelectedCharacters();
  }
  
  public void copy() {
    final StringBuffer buffer = new StringBuffer();
    for (CharacterI character :this.getSelectionModel().getSelected()) {
      buffer.append(character);
      buffer.append(System.getProperty("line.separator"));
    }
    if (buffer.length() > 0) {
      final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      final StringSelection stringSelection = new StringSelection(buffer.toString());
      clipboard.setContents(stringSelection, null);
    }
  }
  
  public String getGroup() {
    return this.group;
  }
  
  private void initializeInterface() {
    this.setLayout(new BorderLayout());
    this.add(this.createToolBar(), BorderLayout.NORTH);
    this.add(this.tablePanel, BorderLayout.CENTER);
    final EventTableModel<CharacterI> eventTableModel = new EventTableModel<CharacterI>(this.filteredCharacters, this.tableFormat);
    this.characterTable.setModel(eventTableModel);
    new TableComparatorChooser<CharacterI>(characterTable, this.sortedCharacters, false);
    this.characterTable.setSelectionModel(this.selectionModel);
    this.characterTable.putClientProperty("Quaqua.Table.style", "striped");
    this.characterTable.getActionMap().getParent().remove("copy");
    this.characterTable.getActionMap().getParent().remove("paste");
    this.filterPanel.add(this.filter.getComponent());
    this.tablePanel.validate();
    this.selectionModel.addListSelectionListener(new SelectionListener());
    new TableColumnPrefsSaver(this.characterTable, this.getTableAutoSaveName());
  }
  
  /** Instantiate interface objects from Swixml file */
  private void loadPanelLayout() {
    SwingEngine swix = new SwingEngine(this);
    try {
      this.tablePanel = (JPanel)swix.render(FileUtil.findUrl("character_template_table.xml"));
    } catch (Exception e) {
      log().fatal("Unable to render character table interface", e);
    }
  }
  
  private String getTableAutoSaveName() {
    return Config.inst().getConfigName() + this.group + "CharacterTemplateTable";
  }
  
  private void clearFilter() {
    this.filter.setFilter(null, this);
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
    this.markButton.setEnabled(hasSelection);
    this.unmarkButton.setEnabled(hasSelection);
  }

  private void updateCharacterForGlazedLists(CharacterI character) {
    final int index = this.getCharacterListManager().getCharacterList().getList().indexOf(character);
    if (index > -1) {
      this.getCharacterListManager().getCharacterList().getList().set(index, character);
    }
  }
  
  private void gainedFocus() {
    if (this.getFieldPanel() != null) {
      this.getFieldPanel().setTableSource(this);
    }
  }
  
  private CharacterListManager getCharacterListManager() {
    return CharacterListManager.getCharListMan(this.group);
  }
  
  private EditManager getEditManager() {
    return EditManager.getEditManager(this.group);
  }
  
  private LoadSaveManager getLoadSaveManager() {
    if (this.loadSaveManager == null) {
      this.loadSaveManager = new LoadSaveManager(this.getCharacterListManager());
    }
    return this.loadSaveManager;
  }
  
  private View getView() {
    return ((IDWDriver)(ComponentManager.getManager().getDriver())).getView(this);
  }
  
  private FieldPanel getFieldPanel() {
    return FieldPanel.getCurrentFieldPanel();
  }
  
  private JToolBar createToolBar() {
    final JToolBar toolBar = new JToolBar("Default Toolbar");
    
    final JButton addButton = new JButton(new AbstractAction(null, new ImageIcon("images/list-add.png")) {
      public void actionPerformed(ActionEvent e) {
        addNewCharacter();
      }
    });
    addButton.setToolTipText("Add");
    
    toolBar.add(addButton);
    
    this.deleteButton = new JButton(new AbstractAction(null, new ImageIcon("images/list-remove.png")) {
      public void actionPerformed(ActionEvent e) {
        deleteSelectedCharacters();
      }
    });
    this.deleteButton.setToolTipText("Delete");
    toolBar.add(this.deleteButton);
    
    this.duplicateButton = new JButton(new AbstractAction(null, new ImageIcon("images/list-duplicate.png")) {
      public void actionPerformed(ActionEvent e) {
        duplicateSelectedCharacters();
      }
    });
    this.duplicateButton.setToolTipText("Duplicate");
    toolBar.add(this.duplicateButton);
    
    this.markButton = new JButton(new AbstractAction(null, new ImageIcon("images/square-filled.png")) {
      public void actionPerformed(ActionEvent e) {
        markSelectedCharacters();
      }
    });
    this.markButton.setToolTipText("Mark");
    toolBar.add(this.markButton);
    
    this.unmarkButton = new JButton(new AbstractAction(null, new ImageIcon("images/square-empty.png")) {
      public void actionPerformed(ActionEvent e) {
        unmarkSelectedCharacters();
      }
    });
    this.unmarkButton.setToolTipText("Unmark");
    toolBar.add(this.unmarkButton);
    
    this.invertSelectionButton = new JButton(new AbstractAction(null, new ImageIcon("images/square-invert.png")) {
      public void actionPerformed(ActionEvent e) {
        invertMarkedCharacters();
      }
    });
    this.invertSelectionButton.setToolTipText("Invert marks");
    toolBar.add(this.invertSelectionButton);
    
    this.generateButton = new JButton(new AbstractAction(null, new ImageIcon("images/generate.png")) {
      public void actionPerformed(ActionEvent e) {
        generateCharacters();
      }
    });
    this.generateButton.setToolTipText("Generate characters");
    toolBar.add(this.generateButton);
    
    toolBar.setFloatable(false);
    return toolBar;
  }

  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
  private class CharacterChangeListener implements CharChangeListener {
    public void charChanged(CharChangeEvent e) {
      if (e.isUpdate()) {
        for (CharacterI character : e.getTransaction().getCharacters()) {
          CharacterTemplateTable.this.updateCharacterForGlazedLists(character);
        }
      } else if (e.isAdd()) {
        CharacterTemplateTable.this.setSelectionWithCharacters(e.getTransaction().getCharacters());
      }
    }
  }
  
  /** Listens for loading of new data files and clears the search filter */
  private class CharacterListChangeListener implements CharListChangeListener {
    public void newCharList(CharListChangeEvent e) {
      CharacterTemplateTable.this.clearFilter();
      if (!CharacterTemplateTable.this.filteredCharacters.isEmpty()) {
        CharacterTemplateTable.this.selectionModel.setSelectionInterval(0, 0);
      }
    }
  }
  
  private class SelectionListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent e) {
      CharacterTemplateTable.this.updateButtonStates();
    }
  }
  
  private class DockingWindowListener extends DockingWindowAdapter {
    
    @Override
    public void viewFocusChanged(View previouslyFocusedView, View focusedView) {
      if ((focusedView != null) && (CharacterTemplateTable.this.getView().equals(focusedView))) {
        CharacterTemplateTable.this.gainedFocus();
      }
    }
    
  }

}
