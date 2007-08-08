package phenote.charactertemplate;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

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
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.LoadSaveManager;
import phenote.datamodel.CharField;
import phenote.datamodel.CharacterI;
import phenote.edit.EditManager;
import phenote.gui.MenuManager;
import phenote.gui.TermInfo;
import phenote.gui.field.FieldPanel;
import phenote.gui.selection.SelectionManager;
import phenote.main.Phenote;
import phenote.util.FileUtil;

public class CharacterTemplateController implements ActionListener, TemplateChoiceListener {

  public static final String SHOW_CHARACTER_TEMPLATE_ACTION = "showCharacterTemplate";
  public static final String IMPORT_TEMPLATE_CHARACTERS_ACTION = "importCharacters";
  public static final String EXPORT_TEMPLATE_CHARACTERS_ACTION = "exportCharacters";
  private final String representedGroup;
  private CharacterListManager characterListManager;
  private EditManager editManager;
  private SelectionManager selectionManager;
  private LoadSaveManager loadSaveManager;
  private CharacterTemplateTableModel tableModel;
  private JFrame window;
  private JPanel charFieldPanelContainer; // initialized by swix
  private JPanel termInfoPanelContainer; // initialized by swix
  private JTable characterTemplateTable; // initialized by swix
  private List<TemplateChooser> templateChoosers = new ArrayList<TemplateChooser>();
  
  public CharacterTemplateController(String groupName) {
    super();
    this.representedGroup = groupName;
    this.characterListManager = new CharacterListManager();
    this.editManager = new EditManager(this.characterListManager);
    this.selectionManager = new SelectionManager();
    this.tableModel = new CharacterTemplateTableModel(this.representedGroup, this.characterListManager, this.editManager);
    this.addInitialBlankCharacter();
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
    this.editManager.deleteChars(this.selectionManager.getSelectedChars());
    if (this.characterListManager.getCharacterList().isEmpty()) {
      this.addInitialBlankCharacter();
    }
  }
  
  public void duplicateSelectedCharacters() {
    this.editManager.copyChars(this.selectionManager.getSelectedChars());
  }
  
  public void undo() {
    this.editManager.undo();
  }
  
  public void invertMarkedCharacters() {
    this.tableModel.invertCharacterMarks();
  }
  
  public void generateCharacters() {
    final List<CharacterI> newCharacters = new ArrayList<CharacterI>();
    for (CharacterI character : this.tableModel.getMarkedCharacters()) {
      final CharacterI newCharacter = character.cloneCharacter();
      EditManager.inst().addCharacter(newCharacter);
      newCharacters.add(newCharacter);
    }
    SelectionManager.inst().selectCharacters(this, newCharacters);
    Phenote.getPhenote().getFrame().toFront();
  }
  
  public void templateChoiceChanged(TemplateChooser source) {
    this.showCharacterTemplate();
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
  
  private JFrame getWindow() {
    if (this.window == null) {
      this.window = new JFrame(this.getGroupTitle());
      final JComponent panel = this.createPanel();
      this.window.getContentPane().add(panel);
      this.window.setSize(panel.getSize());
    }
    return this.window;
  }
  
  private JComponent createPanel() {
    SwingEngine swix = new SwingEngine(this);
    try {
      JComponent component = (JComponent)swix.render(FileUtil.findUrl("character_template.xml"));
      this.characterTemplateTable.setModel(this.tableModel);
      this.characterTemplateTable.setSelectionModel(new SelectionManagerListSelectionModel(this.characterListManager, this.editManager, this.selectionManager));  
      FieldPanel fieldPanel = new FieldPanel(true, false, this.representedGroup, this.selectionManager, this.editManager);
      this.charFieldPanelContainer.add(fieldPanel);
      TermInfo termInfo = new TermInfo(this.selectionManager);
      this.termInfoPanelContainer.add(termInfo.getComponent());
      return component;
    } catch (Exception e) {
      this.getLogger().error("Unable to render interface", e);
      return new JPanel();
    }
  }
  
  private void addInitialBlankCharacter() {
    this.editManager.addInitialCharacter();
    this.selectionManager.selectCharacters(this, this.characterListManager.getCharacterList().getList());
  }
  
  private void configureTemplateChoosers() {
    for (Group group : Config.inst().getFieldGroups()) {
      if (group.getName() == this.representedGroup) {
       for (Templatechooser chooserConfig : group.getTemplatechooserArray()) {
         TemplateChooser chooser = this.createTemplateChooserInstance(chooserConfig.getAdapter());
         chooser.setCharField(this.getCharFieldWithName(chooserConfig.getField()));
         chooser.setTitle(chooserConfig.getTitle());
         chooser.addTemplateChoiceListener(this.tableModel);
         chooser.addTemplateChoiceListener(this);
         this.templateChoosers.add(chooser);
       }
      }
    }
  }
  
  private TemplateChooser createTemplateChooserInstance(String className) {
    final String errorMessage = "Failed creating TemplateChooser";
    try {
      Class adapterClass = Class.forName(className);
      Object chooser = adapterClass.newInstance();
      return (TemplateChooser)chooser;
    } catch (ClassNotFoundException e) {
      this.getLogger().error(errorMessage, e);
    } catch (InstantiationException e) {
      this.getLogger().error(errorMessage, e);
    } catch (IllegalAccessException e) {
      this.getLogger().error(errorMessage, e);
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
  
  private LoadSaveManager getLoadSaveManager() {
    if (this.loadSaveManager == null) {
      this.loadSaveManager = new LoadSaveManager(this.characterListManager);
    }
    return this.loadSaveManager;
  }
  
  private Logger getLogger() {
    return Logger.getLogger(this.getClass());
  }
  
}
