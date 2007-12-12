package phenote.gui;

import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import net.infonode.docking.DockingWindowAdapter;
import net.infonode.docking.View;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractGUIComponent;
import org.bbop.framework.ComponentManager;
import org.bbop.framework.dock.idw.IDWDriver;

import phenote.datamodel.CharacterI;
import phenote.gui.field.FieldPanel;
import ca.odell.glazedlists.swing.EventSelectionModel;

/**
 * This is a beginning for an implementation of the character table as a 
 * GUIComponent.  The CharacterTableController should really be merged into this.
 * At the moment this class provides a place to implement action methods (e.g. Copy).
 */
public class CharacterTable extends AbstractGUIComponent implements CharacterTableSource {
  
  private static final long serialVersionUID = -3970995454868538116L;
  private CharacterTableController tableController;
  private DockingWindowListener focusListener;

  public CharacterTable(String group, String id) {
    super(id);
    this.tableController = new CharacterTableController(group);
  }
  
  public void init() {
    this.setLayout(new GridLayout());
    this.add(this.tableController.getCharacterTablePanel());
    this.focusListener = new DockingWindowListener();
    this.getView().addListener(this.focusListener);
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

  public String getGroup() {
    return this.tableController.getGroup();
  }

  public EventSelectionModel<CharacterI> getSelectionModel() {
    return this.tableController.getSelectionModel();
  }
  
  @Override
  public void cleanup() {
    this.getView().removeListener(this.focusListener);
    super.cleanup();
  }

  private void gainedFocus() {
    if (this.getFieldPanel() != null) {
      this.getFieldPanel().setTableSource(this);
    }
  }
  
  private FieldPanel getFieldPanel() {
    return FieldPanel.getCurrentFieldPanel();
  }
  
  private View getView() {
    return ((IDWDriver)(ComponentManager.getManager().getDriver())).getView(this);
  }

  private class DockingWindowListener extends DockingWindowAdapter {

    @Override
    public void viewFocusChanged(View previouslyFocusedView, View focusedView) {
      if ((focusedView != null) && (CharacterTable.this.getView().equals(focusedView))) {
        CharacterTable.this.gainedFocus();
      }
    }

  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
