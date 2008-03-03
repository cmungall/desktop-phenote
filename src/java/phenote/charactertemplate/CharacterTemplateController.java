package phenote.charactertemplate;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTable;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import phenote.config.Config;
import phenote.config.xml.GroupDocument.Group;
import phenote.config.xml.TemplatechooserDocument.Templatechooser;
import phenote.dataadapter.CharListChangeEvent;
import phenote.dataadapter.CharListChangeListener;
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.LoadSaveManager;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;
import phenote.edit.EditManager;
import phenote.gui.MenuManager;
import phenote.gui.TableColumnPrefsSaver;
import phenote.gui.TermInfo;
import phenote.gui.field.CharFieldMatcherEditor;
import phenote.gui.field.FieldPanel;
import phenote.gui.selection.SelectionManager;
import phenote.main.Phenote;
import phenote.util.FileUtil;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

public class CharacterTemplateController implements ActionListener, TemplateChoiceListener, CharChangeListener, CharListChangeListener {

  public static final String SHOW_CHARACTER_TEMPLATE_ACTION = "showCharacterTemplate";
  public static final String IMPORT_TEMPLATE_CHARACTERS_ACTION = "importCharacters";
  public static final String EXPORT_TEMPLATE_CHARACTERS_ACTION = "exportCharacters";
  private final String representedGroup;
  private CharacterListManager characterListManager;
  private EditManager editManager;
  private SelectionManager selectionManager;
  private LoadSaveManager loadSaveManager;
  private JFrame window;
  private JPanel charFieldPanelContainer; // initialized by swix
  private JPanel termInfoPanelContainer; // initialized by swix
  private JTable characterTemplateTable; // initialized by swix
  private JPanel filterPanel; // initialized by swix
  private JPanel tablePanel; // inititalized by swix
  private List<TemplateChooser> templateChoosers = new ArrayList<TemplateChooser>();
  private SortedList<CharacterI> sortedCharacters;
  private FilterList<CharacterI> filteredCharacters;
  private EventSelectionModel<CharacterI> selectionModel;
  private Set<CharacterI> markedCharacters = new HashSet<CharacterI>();
  private CharFieldMatcherEditor filter;
  
  
  public CharacterTemplateController(String groupName) {
    super();
    this.representedGroup = groupName;
    this.characterListManager = CharacterListManager.getCharListMan(this.representedGroup);
    CharacterListManager.main().addCharListChangeListener(this);
    this.editManager = EditManager.getEditManager(this.representedGroup);
    this.editManager.addCharChangeListener(this);
    this.selectionManager = new SelectionManager();
    this.sortedCharacters = new SortedList<CharacterI>(this.characterListManager.getCharacterList().getList(), new EverythingEqualComparator<CharacterI>());
    this.sortedCharacters.setMode(SortedList.AVOID_MOVING_ELEMENTS);
    this.configureTemplateChoosers();
    this.configureMenus();
  }

  public void actionPerformed(ActionEvent event) {
    final String actionCommand = event.getActionCommand();
    if (actionCommand.equals(CharacterTemplateController.SHOW_CHARACTER_TEMPLATE_ACTION)) {
      this.showCharacterTemplate();
    } else if (actionCommand.equals(CharacterTemplateController.IMPORT_TEMPLATE_CHARACTERS_ACTION)) {
      this.importCharacters();
    } else if (actionCommand.equals(CharacterTemplateController.EXPORT_TEMPLATE_CHARACTERS_ACTION)) {
      this.exportCharacters();
    }
  }
  
  public void showCharacterTemplate() {
    this.getWindow().setVisible(true);
  }
  
  public void importCharacters() {
    this.getLoadSaveManager().loadData();
    this.showCharacterTemplate();
  }
  
  public void exportCharacters() {
    this.getLoadSaveManager().saveData();
  }
  
  public void addNewCharacter() {
    this.editManager.addNewCharacter();
  }
  
  public void deleteSelectedCharacters() {
    this.editManager.deleteChars(this.getSelectionModel().getSelected());
  }
  
  public void duplicateSelectedCharacters() {
    this.editManager.copyChars(this.getSelectionModel().getSelected());
  }
  
  public void markSelectedCharacters() {
    this.setCharactersAreMarked(this.getSelectionModel().getSelected(), true);
  }
  
  public void unmarkSelectedCharacters() {
    this.setCharactersAreMarked(this.getSelectionModel().getSelected(), false);
  }
  
  public void undo() {
    this.editManager.undo();
  }
  
  public void invertMarkedCharacters() {
    for (CharacterI character : this.getCurrentCharacters()) {
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
    for (CharacterI character : this.getCurrentCharacters()) {
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
    Phenote.getPhenote().getFrame().toFront();
  }
  
  public EventSelectionModel<CharacterI> getSelectionModel() {
    return this.selectionModel;
  }
  
  public List<CharacterI> getAllCharacters() {
    return this.sortedCharacters;
  }
  
  public JFrame getWindow() {
    if (this.window == null) {
      this.window = new JFrame(this.getGroupTitle());
      final JComponent panel = this.createPanel();
      this.window.getContentPane().add(panel);
      this.window.setSize(panel.getSize());
    }
    return this.window;
  }
  
  public JComponent getComponent() {
    this.getWindow(); //TODO get rid of need for window
    return this.tablePanel;
  }
  
  public void templateChoiceChanged(TemplateChooser source) {
    this.setMarkedCharacters(source.getChosenTemplates(Collections.unmodifiableList(this.sortedCharacters)));
    this.showCharacterTemplate();
  }

  public void charChanged(CharChangeEvent e) {
    if (e.isUpdate()) {
      for (CharacterI character : e.getTransaction().getCharacters()) {
        this.updateCharacterForGlazedLists(character);
      }
    } else if (e.isAdd()) {
      this.setSelectionWithCharacters(e.getTransaction().getCharacters());
    }
  }
  
  public void newCharList(CharListChangeEvent e) {
    // this is assumed to be coming from the main CharacterListManager
    this.tryLoadDefaultDataFile();
  }

  private void tryLoadDefaultDataFile() {
    final File file = CharacterListManager.main().getCurrentDataFile();
    if (file == null) {
      return;
    }
    final int dotLocation = file.getName().lastIndexOf(".");
    final boolean hasExtension = dotLocation > 0;
    final String extension = hasExtension ? file.getName().substring(dotLocation) : "";
    final String baseName = hasExtension ? file.getName().substring(0, dotLocation) : file.getName();
    final String defaultFileName = baseName + "-" + this.representedGroup +  extension;
    File templatesFile = new File(file.getParent(), defaultFileName);
    if (templatesFile.exists()) {
      this.getLoadSaveManager().loadData(templatesFile);
    }
  }
  
  private void setSelectionWithCharacters(List<CharacterI> characters) {
    this.filter.setFilter(null, this);
    this.getSelectionModel().clearSelection();
    for (CharacterI character : characters) {
      final int index = this.filteredCharacters.indexOf(character);
      if (index > -1) {
        this.getSelectionModel().addSelectionInterval(index, index);
        Rectangle rect = this.characterTemplateTable.getCellRect(index, 0, false);
        this.characterTemplateTable.scrollRectToVisible(rect);
      }
    }
  }

  private void updateCharacterForGlazedLists(CharacterI character) {
    final int index = this.characterListManager.getCharacterList().getList().indexOf(character);
    if (index > -1) {
      this.characterListManager.getCharacterList().getList().set(index, character);
    }
  }

  private String getGroupTitle() {
    return Config.inst().getTitleForGroup(this.representedGroup);
  }

  private void configureMenus() {
    this.addFileMenuItems();
    this.addViewMenuItem();
    this.addChooserMenuItems();
  }
  
  private void addViewMenuItem() {
    final JMenuItem menuItem = new JMenuItem(this.getGroupTitle());
    menuItem.setActionCommand(CharacterTemplateController.SHOW_CHARACTER_TEMPLATE_ACTION);
    menuItem.addActionListener(this);
    MenuManager.inst().addViewMenuItem(menuItem);
  }
  
  private void addFileMenuItems() {
    final JMenu templateMenu = new JMenu(this.getGroupTitle());
    final JMenuItem loadItem = new JMenuItem("Open...");
    loadItem.setActionCommand(CharacterTemplateController.IMPORT_TEMPLATE_CHARACTERS_ACTION);
    loadItem.addActionListener(this);
    final JMenuItem saveItem = new JMenuItem("Save As...");
    saveItem.setActionCommand(CharacterTemplateController.EXPORT_TEMPLATE_CHARACTERS_ACTION);
    saveItem.addActionListener(this);
    templateMenu.add(loadItem);
    templateMenu.add(saveItem);
    final JMenu fileMenu = MenuManager.inst().getFileMenu();
    Component[] menuComponents = fileMenu.getMenuComponents();
    int i;
    for (i = 0; i < menuComponents.length; i++) {
      Component component = menuComponents[i];
      if (component instanceof JSeparator) {
        break;
      }
    }
    fileMenu.add(templateMenu, i);
  }
  
  private void addChooserMenuItems() {
    this.getWindow().setJMenuBar(new JMenuBar());
    final JMenu chooserMenu = new JMenu("Choosers");
    this.getWindow().getJMenuBar().add(chooserMenu);
    for (TemplateChooser chooser : this.templateChoosers) {
      final JMenuItem menuItem = new JMenuItem(chooser.getTitle());
      chooserMenu.add(menuItem);
      menuItem.setActionCommand(TemplateChooser.SHOW_CHOOSER_ACTION);
      menuItem.addActionListener(chooser);
    }
  }
  
  
  
  private JComponent createPanel() {
    SwingEngine swix = new SwingEngine(this);
    try {
      JComponent component = (JComponent)swix.render(FileUtil.findUrl("character_template.xml"));
      this.filter = new CharFieldMatcherEditor(CharFieldManager.inst().getCharFieldListForGroup(this.representedGroup));
      this.filterPanel.add(this.filter.getComponent());
      this.filteredCharacters = new FilterList<CharacterI>(this.sortedCharacters, this.filter);
      final CharacterTemplateTableFormat tableFormat = new CharacterTemplateTableFormat(this.representedGroup, this);
      EventTableModel<CharacterI> eventTableModel = new EventTableModel<CharacterI>(this.filteredCharacters, tableFormat);
      this.characterTemplateTable.setModel(eventTableModel);
      new TableComparatorChooser<CharacterI>(this.characterTemplateTable, this.sortedCharacters, false);
      this.selectionModel = new EventSelectionModel<CharacterI>(this.filteredCharacters);
      this.characterTemplateTable.setSelectionModel(this.selectionModel);
      this.characterTemplateTable.putClientProperty("Quaqua.Table.style", "striped");
      new TableColumnPrefsSaver(this.characterTemplateTable, this.getTableAutoSaveName());
      FieldPanel fieldPanel = new FieldPanel(true, false, this.representedGroup, this.selectionManager, this.editManager, this.selectionModel);
      this.charFieldPanelContainer.add(fieldPanel);
      TermInfo termInfo = new TermInfo(this.selectionManager);
      this.termInfoPanelContainer.add(termInfo.getComponent());
      return component;
    } catch (Exception e) {
      this.log().error("Unable to render interface", e);
      return new JPanel();
    }
  }
  
  private void configureTemplateChoosers() {
    for (Group group : Config.inst().getFieldGroups()) {
      if (group.getName() == this.representedGroup) {
       for (Templatechooser chooserConfig : group.getTemplatechooserArray()) {
         TemplateChooser chooser = this.createTemplateChooserInstance(chooserConfig.getAdapter());
         chooser.setCharField(this.getCharFieldWithName(chooserConfig.getField()));
         chooser.setTitle(chooserConfig.getTitle());
         chooser.addTemplateChoiceListener(this);
         this.templateChoosers.add(chooser);
       }
      }
    }
  }
  
  private TemplateChooser createTemplateChooserInstance(String className) {
    final String errorMessage = "Failed creating TemplateChooser";
    try {
      Class<?> adapterClass = Class.forName(className);
      Constructor<?> constructor = adapterClass.getConstructor(String.class);
      Object chooser = constructor.newInstance("dummy id");
      return (TemplateChooser)chooser;
    } catch (ClassNotFoundException e) {
      this.log().error(errorMessage, e);
    } catch (InstantiationException e) {
      this.log().error(errorMessage, e);
    } catch (IllegalAccessException e) {
      this.log().error(errorMessage, e);
    } catch (SecurityException e) {
      this.log().error(errorMessage, e);
    } catch (NoSuchMethodException e) {
      this.log().error(errorMessage, e);
    } catch (IllegalArgumentException e) {
      this.log().error(errorMessage, e);
    } catch (InvocationTargetException e) {
      this.log().error(errorMessage, e);
    }
    return null;
  }
  
  private CharField getCharFieldWithName(String fieldName) {
    final int fieldsNum = Config.inst().getEnbldFieldsNum();
    for (int i = 0; i < fieldsNum; i++) {
      final CharField field = Config.inst().getEnbldCharField(i);
      if (field.getName().equals(fieldName)) return field;
    }
    return null;
  }
  
  private String getTableAutoSaveName() {
    return Config.inst().getConfigName() + this.representedGroup + "CharacterTemplateTable";
  }
  
  private List<CharacterI> getCurrentCharacters() {
    return (this.filteredCharacters == null) ? this.sortedCharacters : this.filteredCharacters;
  }
  
  private LoadSaveManager getLoadSaveManager() {
    if (this.loadSaveManager == null) {
      this.loadSaveManager = new LoadSaveManager(this.characterListManager);
    }
    return this.loadSaveManager;
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
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

}
