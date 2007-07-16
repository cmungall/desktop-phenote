package phenote.charactertemplate;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTable;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import phenote.config.Config;
import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharacterI;
import phenote.edit.EditManager;
import phenote.gui.MenuManager;
import phenote.gui.TermInfo;
import phenote.gui.field.FieldPanel;
import phenote.gui.selection.SelectionManager;
import phenote.main.Phenote;

public class CharacterTemplateController implements ActionListener {

  public static final String SHOW_CHARACTER_TEMPLATE_ACTION = "showCharacterTemplate";
  private final String representedGroup;
  private CharacterListManager characterListManager;
  private EditManager editManager;
  private SelectionManager selectionManager;
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
    this.editManager.addInitialCharacter();
    this.selectionManager.selectCharacters(this, this.characterListManager.getCharacterList().getList());
    this.createMenu();
  }

  public void actionPerformed(ActionEvent event) {
    final String actionCommand = event.getActionCommand();
    if (actionCommand.equals(CharacterTemplateController.SHOW_CHARACTER_TEMPLATE_ACTION)) {
      this.showCharacterTemplate();
    } 
  }
  
  public void showCharacterTemplate() {
    this.getWindow().setVisible(true);
  }
  
  public void addNewCharacter() {
    this.editManager.addNewCharacter();
  }
  
  public void deleteSelectedCharacters() {
    this.editManager.deleteChars(this.selectionManager.getSelectedChars());
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
    final List<CharacterI> templates = this.tableModel.getMarkedCharacters();
    final List<CharacterI> newCharacters = new ArrayList<CharacterI>();
    for (CharacterI character : templates) {
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

  private void createMenu() {
    JMenuItem menuItem = new JMenuItem(this.getGroupTitle());
    menuItem.setActionCommand(CharacterTemplateController.SHOW_CHARACTER_TEMPLATE_ACTION);
    menuItem.addActionListener(this);
    MenuManager.inst().addViewMenuItem(menuItem);
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
      JPanel panel = (JPanel)swix.render(new File("conf/character_template.xml"));
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
  
  private Logger getLogger() {
    return Logger.getLogger(this.getClass());
  }
  
}
