package phenote.gui;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractGUIComponent;
import org.bbop.framework.ComponentManager;
import org.bbop.framework.GUIComponent;
import org.bbop.framework.dock.LayoutAdapter;

import phenote.config.Config;
import phenote.dataadapter.ScratchGroup;
import phenote.datamodel.CharacterI;
import phenote.edit.EditManager;
import phenote.gui.field.FieldPanelContainer;
import phenote.util.FileUtil;
import ca.odell.glazedlists.swing.EventSelectionModel;

/**
 * This is a beginning for an implementation of the character table as a 
 * GUIComponent.  The CharacterTableController should really be merged into this.
 * At the moment this class provides a place to implement action methods (e.g. Copy).
 */
public class CharacterTable extends AbstractGUIComponent implements CharacterTableSource {
  
  private static final long serialVersionUID = -3970995454868538116L;
  private CharacterTableController tableController;
  private ComponentLayoutListener focusListener;
  private JButton addButton;
  private JButton duplicateButton;
  private JButton deleteButton;

  public CharacterTable(String group, String id) {
    super(id);
    this.tableController = new CharacterTableController(group);
  }
  
  public CharacterTable(ScratchGroup group, String id) {
    super(id);
    this.tableController = new CharacterTableController(group);
  }
  
  @Override
  public void init() {
    super.init();
    this.setLayout(new BorderLayout());
    this.add(this.createToolBar(), BorderLayout.NORTH);
    this.tableController.removeOldButtons();
    this.add(this.tableController.getCharacterTablePanel(), BorderLayout.CENTER);
    this.focusListener = new ComponentLayoutListener();
    ComponentManager.getManager().addLayoutListener(this.focusListener);
  }
  
  public void addNewCharacter() {
    this.tableController.addNewCharacter();
  }
  
  public void deleteSelectedCharacters() {
    this.tableController.deleteSelectedCharacters();
  }
  
  public void duplicateSelectedCharacters() {
    this.tableController.duplicateSelectedCharacters();
  }
  
  public void cut() {
    this.copy();
    this.tableController.deleteSelectedCharacters();
  }
  
  public void copy() {
    final StringBuffer buffer = new StringBuffer();
    for (CharacterI character :this.tableController.getSelectionModel().getSelected()) {
      buffer.append(character);
      buffer.append(System.getProperty("line.separator"));
    }
    if (buffer.length() > 0) {
      final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      final StringSelection stringSelection = new StringSelection(buffer.toString());
      clipboard.setContents(stringSelection, null);
    }
  }
  
  public void undo() {
    this.tableController.undo();
  }
  
  public void setControlsEnabled(boolean enable) {
    addButton.setEnabled(enable);
    duplicateButton.setEnabled(enable);
    deleteButton.setEnabled(enable);
  }

  public String getGroup() {
    return this.tableController.getGroup();
  }

  public EventSelectionModel<CharacterI> getSelectionModel() {
    return this.tableController.getSelectionModel();
  }
  
  public EditManager getEditManager() {
    return this.tableController.getEditManager();
  }
  
  @Override
  public void cleanup() {
    ComponentManager.getManager().removeLayoutListener(this.focusListener);
    super.cleanup();
  }

  private void gainedFocus() {
    if ((this.getFieldPanelContainer() != null) && (Config.inst().getGroupUsesFieldPanel(this.getGroup()))) {
      this.getFieldPanelContainer().setTableSource(this);
    }
  }
  
  private FieldPanelContainer getFieldPanelContainer() {
    return FieldPanelContainer.getCurrentFieldPanelContainer();
  }
  
  @SuppressWarnings("serial")
  private JToolBar createToolBar() {
    final JToolBar toolBar = new JToolBar("Default Toolbar");
    
    try {
      this.addButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-add.png"))) {
          public void actionPerformed(ActionEvent e) {
            addNewCharacter();
          }
        });
      this.addButton.setToolTipText("Add");
      toolBar.add(this.addButton);
      
      this.deleteButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-remove.png"))) {
          public void actionPerformed(ActionEvent e) {
            deleteSelectedCharacters();
          }
        });
      this.deleteButton.setToolTipText("Delete");
      toolBar.add(this.deleteButton);
      
      this.duplicateButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-duplicate.png"))) {
          public void actionPerformed(ActionEvent e) {
            duplicateSelectedCharacters();
          }
        });
      this.duplicateButton.setToolTipText("Duplicate");
      toolBar.add(this.duplicateButton);
      
      // OntologyMaker optionally configged - todo todo
//       OntologyMakerI om = Config.inst().getOntMaker(getGroup());
//       if (om != null && om.useButtonToLaunch()) {
//         ontolMaker = om;
//         ontolMakerButton.setText(om.getButtonText());
//       }      

    } catch (FileNotFoundException e) {
      log().error("Couldn't find toolbar icons", e);
    }
    
    toolBar.setFloatable(false);
    return toolBar;
  }
  
  private class ComponentLayoutListener extends LayoutAdapter {

    @Override
    public void focusChanged(GUIComponent old, GUIComponent newComponent) {
      if ((newComponent != null) && (CharacterTable.this.equals(newComponent))) {
        CharacterTable.this.gainedFocus();
      }
    }

  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
