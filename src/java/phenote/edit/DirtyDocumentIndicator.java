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
import phenote.main.PhenotePlus;

/**
 * Listens for editing by the user to indicate that the document needs saving before it's closed. Editing state is reset anytime a user saves
 * the document.  Implements VetoableShutdownListener to be used by GUIManager when a user attempts to quit.
 * @author Jim Balhoff
 */
public class DirtyDocumentIndicator implements CharChangeListener, CharListChangeListener,
  LoadSaveListener, VetoableShutdownListener {
  
  final private static String WINDOW_MODIFIED = "windowModified";
  private boolean edited = false;
  final private static String BULLET = String.format("%c", '\u2022'); 
  private static DirtyDocumentIndicator singleton;

  private DirtyDocumentIndicator() {
    this.registerListeners();
  }

  public static DirtyDocumentIndicator inst() {
    if (singleton == null) singleton = new DirtyDocumentIndicator();
    return singleton;
  }

  /** charChanged comes from user edit, so set edited to true, BUT... if just an
      Add - adding a blank row - its an irrelevant edit and not really a dirtying
      of the document, so ignore adds */
  public void charChanged(CharChangeEvent e) {
    if (e.isAdd()) return; // ignore adding blank row
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
  
  /** Currently this returns true if there has been any non-add(add blank row) edits
      However if one does an edit and then does undo this will still return true.
      Even better would be to check the Transaction list in EditManager for non-add
      edits */
  public boolean isDocumentDirty() {
    return this.edited;
  }
  /** from VetoableShutdownListener interface, this method is called from plus/obo/bbop
      framework on exit, if document is dirty it calls up user dialog to query about
      saving changes and cancelling exit, if false is returned(cancel) then shutdown
      is cancelled/vetoed */
  public boolean willShutdown() {
    if (this.isDocumentDirty()) {
      return this.runDialog(true);
    } else {
      return true;
    }
  }
  
  /** brings up dialog asking user if they want to save changes or cancel operation
      (shutdown or new), returning false indicates cancellation
      shutdown is true if this is for shutdown, and false for clearing out data
  */
  private boolean runDialog(boolean shutdown) {
    final String[] options = {"Save", "Cancel", "Don't Save"};
    String saveClear = shutdown ? "quitting?" : "clearing?";
    String m = "There are unsaved changes.  Would you like to save before "+saveClear;
    final int result = 
      JOptionPane.showOptionDialog(GUIManager.getManager().getFrame(),m, "",
                                   JOptionPane.YES_NO_CANCEL_OPTION,
                                   JOptionPane.WARNING_MESSAGE, null, options, "Save");
    if (result == 0) {
      LoadSaveManager.inst().saveData(true);
      return true;
    } else if (result == 1) {
      return false;
    } else {
      return true;
    }
  }

  public boolean okToClearData() {
    if (this.isDocumentDirty()) {
      return this.runDialog(false);
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
  
  /** Update window gui in some way to show modified, different for mac and others
   doesnt work for old phenote, just plus */
  private void updateDirtyDocumentStatus() {
    if (getRootPane()==null) return;
    // this client property is used on the Mac platform to put a dot inside the close
    // button
    this.getRootPane().putClientProperty(WINDOW_MODIFIED, this.edited);
    // there doesn't seem to be a standard for indicating unsaved documents on other platforms,
    // so we put a bullet in front of the window title
    if (!Phenote.isRunningOnMac()) {
      if (this.edited) {
        GUIManager.getManager().getFrame().setTitle(BULLET + " " + PhenotePlus.getAppName());
      } else {
        GUIManager.getManager().getFrame().setTitle(PhenotePlus.getAppName());
      }      
    }
  }
  
  private JRootPane getRootPane() {
    if (GUIManager.getManager()==null || GUIManager.getManager().getFrame()==null) {
      return null;
    }
    return GUIManager.getManager().getFrame().getRootPane();
  }
  
  @SuppressWarnings("unused")
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
