package phenote.charactertemplate;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTable;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import phenote.config.Config;
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.LoadSaveManager;
import phenote.datamodel.CharacterI;
import phenote.edit.EditManager;
import phenote.gui.MenuManager;
import phenote.gui.TermInfo;
import phenote.gui.field.FieldPanel;
import phenote.gui.selection.SelectionManager;
import phenote.main.Phenote;
import phenote.util.FileUtil;

public class CharacterTemplateController implements ActionListener {

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
  
  public CharacterTemplateController(String groupName) {
    super();
    this.representedGroup = groupName;
    this.characterListManager = new CharacterListManager();
    this.editManager = new EditManager(this.characterListManager);
    this.selectionManager = new SelectionManager();
    this.tableModel = new CharacterTemplateTableModel(this.representedGroup, this.characterListManager, this.editManager);
    this.addInitialBlankCharacter();
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
  
  private String getGroupTitle() {
    return Config.inst().getTitleForGroup(this.representedGroup);
  }

  private void configureMenus() {
    this.addFileMenuItems();
    this.addViewMenuItem();
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
  
  private JFrame getWindow() {
    if (this.window == null) {
      this.window = new JFrame(this.getGroupTitle());
      final JPanel panel = this.createPanel();
      this.window.getContentPane().add(panel);
      this.window.setSize(panel.getSize());
    }
    return this.window;
  }
  
  private JPanel createPanel() {
    SwingEngine swix = new SwingEngine(this);
    try {
      JPanel panel = (JPanel)swix.render(FileUtil.findUrl("character_template.xml"));
      this.characterTemplateTable.setModel(this.tableModel);
      this.characterTemplateTable.setSelectionModel(new SelectionManagerListSelectionModel(this.characterListManager, this.editManager, this.selectionManager));  
      FieldPanel fieldPanel = new FieldPanel(true, false, this.representedGroup, this.selectionManager, this.editManager);
      this.charFieldPanelContainer.add(fieldPanel, BorderLayout.NORTH);
      TermInfo termInfo = new TermInfo(this.selectionManager);
      this.termInfoPanelContainer.add(termInfo.getComponent());
      return panel;
    } catch (Exception e) {
      this.getLogger().error("Unable to render interface", e);
      return new JPanel();
    }
  }
  
  private void addInitialBlankCharacter() {
    this.editManager.addInitialCharacter();
    this.selectionManager.selectCharacters(this, this.characterListManager.getCharacterList().getList());
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
