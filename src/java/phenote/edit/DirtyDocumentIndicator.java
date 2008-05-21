package phenote.edit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JRootPane;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.bbop.framework.VetoableShutdownListener;

import phenote.config.Config;
import phenote.config.xml.GroupDocument.Group;
import phenote.dataadapter.CharListChangeEvent;
import phenote.dataadapter.CharListChangeListener;
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.LoadSaveListener;
import phenote.dataadapter.LoadSaveManager;
import phenote.main.Phenote;
import phenote.main.Phenote2;

/**
 * Listens for editing by the user to indicate that the document needs saving before it's closed. Editing state is reset anytime a user saves
 * the document.  Implements VetoableShutdownListener to be used by GUIManager when a user attempts to quit.
 * @author Jim Balhoff
 */
public class DirtyDocumentIndicator implements CharChangeListener, CharListChangeListener, LoadSaveListener, VetoableShutdownListener {
  
  final private static String WINDOW_MODIFIED = "windowModified";
  private boolean edited = false;
  final private static String BULLET = String.format("%c", '\u2022'); 
  
  public DirtyDocumentIndicator() {
    this.registerListeners();
  }

  /** charChanged comes from user edit, so set edited to true, BUT... if just an
      Add - adding a blank row - its an irrelevant edit and not really a dirtying
      of the document, so ignore adds */
  public void charChanged(CharChangeEvent e) {
    if (e.isAdd()) return;
    this.edited = true;
    this.updateDirtyDocumentStatus();
  }
  
  public void newCharList(CharListChangeEvent e) {
    this.edited = false;
    this.updateDirtyDocumentStatus();
  }
  
  public void fileLoaded(File f) {}

  public void fileSaved(File f) {
    this.edited = false;
    this.updateDirtyDocumentStatus();
  }
  
  public boolean isDocumentDirty() {
    return this.edited;
  }
  
  public boolean willShutdown() {
    if (this.isDocumentDirty()) {
      return this.runDialog();
    } else {
      return true;
    }
  }
  
  private boolean runDialog() {
    final String[] options = {"Save", "Cancel", "Don't Save"};
    final int result = JOptionPane.showOptionDialog(GUIManager.getManager().getFrame(), "You have unsaved changes.  Would you like to save before quitting?", "", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, "Save");
    if (result == 0) {
      LoadSaveManager.inst().saveData(true);
      return true;
    } else if (result == 1) {
      return false;
    } else {
      return true;
    }
  }
  
  private void registerListeners() {
    for (EditManager editManager : this.getEditManagers()) {
      editManager.addCharChangeListener(this);
    }
    CharacterListManager.main().addCharListChangeListener(this);
    LoadSaveManager.inst().addListener(this);
  }
  
  private List<EditManager> getEditManagers() {
    final List<EditManager> editManagers = new ArrayList<EditManager>();
    for (Group group : Config.inst().getFieldGroups()) {
      editManagers.add(EditManager.getEditManager(group.getName()));
    }
    final EditManager defaultEditManager = EditManager.inst();
    if (!editManagers.contains(defaultEditManager)) editManagers.add(defaultEditManager);
    return editManagers;
  }
  
  private void updateDirtyDocumentStatus() {
    // this client property is used on the Mac platform to put a dot inside the close
    // button
    this.getRootPane().putClientProperty(WINDOW_MODIFIED, this.edited);
    // there doesn't seem to be a standard for indicating unsaved documents on other platforms,
    // so we put a bullet in front of the window title
    if (!Phenote.isRunningOnMac()) {
      if (this.edited) {
        GUIManager.getManager().getFrame().setTitle(BULLET + " " + Phenote2.getAppName());
      } else {
        GUIManager.getManager().getFrame().setTitle(Phenote2.getAppName());
      }      
    }
  }
  
  private JRootPane getRootPane() {
    return GUIManager.getManager().getFrame().getRootPane();
  }
  
  @SuppressWarnings("unused")
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
