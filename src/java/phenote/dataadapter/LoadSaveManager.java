package phenote.dataadapter;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;

import phenote.config.Config;
import phenote.datamodel.CharacterListI;
import phenote.edit.DirtyDocumentIndicator;
import phenote.edit.EditManager;
import phenote.main.PhenotePlus;

/** This was formerly just for files - adding in queryable/database data adapters */

public class LoadSaveManager {
  
  private static LoadSaveManager singleton;
  private JFileChooser fileChooser;
  private CharacterListManager characterListManager;
  private List<LoadSaveListener> listeners = new ArrayList<LoadSaveListener>();
  
  private LoadSaveManager() {
    this(CharacterListManager.inst());
  }
  
  public LoadSaveManager(CharacterListManager clManager) {
    this.characterListManager = clManager;
    // I tried making the file chooser open at ".", hoping that would make it start in the Phenote dir rather than in the user's home dir,
    // but it didn't help.
    fileChooser = new JFileChooser("");
    List<DataAdapterI> adapters = Config.inst().getDataAdapters();
    if (adapters == null) {
      log().error("No adapters configged, can't load or save");
      return;
    }
    boolean first = true;
    FileFilter filt1=null;
    for (DataAdapterI adapter: adapters) {
      DataAdapterFileFilter filter = new DataAdapterFileFilter(adapter);
      fileChooser.addChoosableFileFilter(filter);
      if (first) {
        filt1 = filter;
        first = false;
      }
    }
    fileChooser.setFileFilter(filt1);
  }
  
  /**Returns singleton*/
  public static LoadSaveManager inst() {
    if (singleton == null)
      singleton = new LoadSaveManager();
    return singleton;
  }
  
  public static void reset() {
    singleton = null;
  }
  
  public void newData() { 
//  CharacterListI charList = new CharacterList();
//  CharacterListManager.inst().setCharacterList(this,charList);
    log().debug("newData");
  }

  
  /**Loads a new document of characters, prompting the user to choose a file and possibly a data adapter.*/
  public void loadData() {

    // should only hop to file dialog if its known that ALL data adapters are File adapters
    File aFile = runOpenDialog();
    if (aFile != null) {
      FileFilter filter = fileChooser.getFileFilter();
      if (filter instanceof DataAdapterFileFilter) {
        DataAdapterI adapter = ((DataAdapterFileFilter)filter).getAdapter();
        loadData(aFile, adapter);
      } else {
        loadData(aFile);
      }
    }
  }
  
  /**Loads a new document of characters from the given file, using the default data adapter for the file's extension.*/
  public void loadData(File f) {
    DataAdapterI adapter = getDataAdapterForFilename(f.getName());
    loadData(f, adapter);
  }
  
  /**Loads a new document of characters from the given file using the given data adapter.*/
  public void loadData(File f, DataAdapterI adapter) {
    if (adapter == null)
      adapter = getDataAdapterForFilename(f.getName());
    CharacterListI charList = adapter.load(f);
    if (charList == null) {
      String m = "Failed to load in data for file "+f+" using data adapter "
        +adapter.getDescription();
      JOptionPane.showMessageDialog(null,m,"Load failure",JOptionPane.ERROR_MESSAGE); 
      return;
    }
    else {
      this.characterListManager.setCurrentDataFile(f);
      this.characterListManager.setCharacterList(this,charList);
    }
    for (LoadSaveListener listener : this.listeners) {
      listener.fileLoaded(f);
    }
    setTitleOnPhenoteWindowForFile(f);
  }
  
  /**Saves the document's characters to a file, prompting the user to choose a file
     and data adapter.*/
  public void saveData() {
    // CONSTRAINT check
    try { checkConstraints(); } catch (ConstraintEx e) { return; } // failure

    File aFile = runSaveDialog();
    if (aFile != null) {
      FileFilter filter = fileChooser.getFileFilter();
      if (filter instanceof DataAdapterFileFilter) {
        DataAdapterI adapter = ((DataAdapterFileFilter)filter).getAdapter();
        saveData(aFile, adapter);
      } else {
        saveData(aFile, false);
      }
      setTitleOnPhenoteWindowForFile(aFile);
    }
  }
  
  /**Saves the document's characters to the file it was loaded from, or prompting the user to choose a file and data adapter if there is no current file.*/
  public void saveData(boolean useCurrentFile) {
    // CONSTRAINT check
    try { checkConstraints(); } catch (ConstraintEx e) { return; } // failure ret

    if (useCurrentFile && (this.characterListManager.getCurrentDataFile() != null)) {
      this.saveData(this.characterListManager.getCurrentDataFile(), false);
    } else {
      // there isn't a current file or we should choose a new one
      this.saveData();
    }
    
  }
  
  /**Saves the document's characters to the given file, using the default data adapter for the file's extension.*/
  public void saveData(File f) {
    saveData(f, true); // check constraints
  }

  public void saveData(File f, boolean checkConstraints) {
    // CONSTRAINT check
    if (checkConstraints) {
      try { checkConstraints(); } catch (ConstraintEx e) { return; } // failure ret
    }

    DataAdapterI adapter = getDataAdapterForFilename(f.getName());
    saveData(f, adapter);
  }
  
  /**Saves the document's characters to the given file using the given data adapter.*/
  private void saveData(File f, DataAdapterI adapter) {

    // COMMIT TO FILE if constraints passed
    CharacterListI charList = this.characterListManager.getCharacterList();
    adapter.commit(charList, f);
    this.characterListManager.setCurrentDataFile(f);
    for (LoadSaveListener listener : this.listeners) {
      listener.fileSaved(f);
    }
  }

  /** This class needs a little refactoring - the above methods should be renamed
      saveFileData - this method check if have queryable/db adapter and if so 
      saves to that - otherwise brings up file chooser */
  public void saveToDbOrFile() {

    if (Config.inst().hasQueryableDataAdapter()) {
      saveToDbDataadapter();
    } else {
      saveData(); // saveFileData really
    }

  }

  /** Checks contraints and if pass/override then save to QueryableDataAdapter
      and clear transactions */
  public void saveToDbDataadapter() {
    if (!Config.inst().hasQueryableDataAdapter()) return; // err? ex?

    // CONSTRAINT check - puts up error dialog on warn/fail
    try { checkConstraints(); }
    // failure - return
    catch (ConstraintEx e) { return; } // is this funny?

    // COMMIT TO DB
    CharacterListI nonBlankList = characterListManager.getNonBlankCharList();
    Config.inst().getQueryableDataAdapter().commit(nonBlankList);
    
    // after commit should clear out trans, file commit should do same!
    EditManager.inst().clearTransactions();
    
  }

  private class ConstraintEx extends Exception {
    private ConstraintEx(String m) { super(m); }
  }

  /** If constraints fail and or warning and user decided to not commit then
      throws ConstraintEx (or should it just return boolean?)
      Also puts up error message */
  private void checkConstraints() throws ConstraintEx {
    ConstraintStatus cs = ConstraintManager.inst().checkCommitConstraints();
    
    // FAILURE - no commit
    if (cs.isFailure()) {
      String m = "There is a problem with your commit:\n"+cs.getFailureMessage()+
        "Commit cancelled. You must fix this.";
      JTextArea area = new JTextArea(m);
      //      area.setRows(20);
      //      area.setColumns(80);
      area.setLineWrap(true);
      JScrollPane pane = new JScrollPane(area);
      pane.setPreferredSize(new Dimension(500,200));
      JOptionPane.showMessageDialog(null,pane,"Commit failed",JOptionPane.ERROR_MESSAGE);
      throw new ConstraintEx(m);
    }

    // WARNING - ask user if still wants to commit
    if (cs.isWarning()) {
      String m = "There is a problem with your commit--save anyway?\n\n" +
        cs.getWarningMessage();
      JTextArea area = new JTextArea(m);
      //      area.setRows(20);
      //      area.setColumns(80);
      area.setLineWrap(true);
      JScrollPane pane = new JScrollPane(area);
      pane.setPreferredSize(new Dimension(500,200));
      int ret = JOptionPane.showConfirmDialog(null,pane,"Commit Warning",
                                              JOptionPane.YES_NO_OPTION,
                                              JOptionPane.ERROR_MESSAGE);
      if (ret != JOptionPane.YES_OPTION)
        throw new ConstraintEx(m);
    }
    
  }
  
  public void exportData() {
    saveData();
  }
  
  public void addListener(LoadSaveListener listener) {
    this.listeners.add(listener);
  }
  
  public void removeListener(LoadSaveListener listener) {
    this.listeners.remove(listener);
  }
  
  private File runOpenDialog() {
    // First check that user didn't make any changes to the current dataset (if any)
    if (!DirtyDocumentIndicator.inst().okToClearData()) {
      log().debug("Data was dirty--user cancelled load."); // DEL
      return null;
    }
    fileChooser.setAcceptAllFileFilterUsed(true);
//    log().debug("runOpenDialog: current dir = " + fileChooser.getCurrentDirectory()); // DEL
    int returnValue = fileChooser.showOpenDialog(null);
    if (returnValue == JFileChooser.APPROVE_OPTION) {
      return fileChooser.getSelectedFile();
    } else {
      return null;
    }
  }
  
  private File runSaveDialog() {
    fileChooser.setAcceptAllFileFilterUsed(false);
    int returnValue = fileChooser.showSaveDialog(null);
    if (returnValue == JFileChooser.APPROVE_OPTION) {
      return fileChooser.getSelectedFile();
    } else {
      return null;
    }
  }
  
  private DataAdapterI getDataAdapterForFilename(String filename) {
    List<DataAdapterI> adapters = Config.inst().getDataAdapters();
    String adapterList = "\n\nAvailable data adapters and file suffixes:";
    for (DataAdapterI adapter: adapters) {
      adapterList += "\n" + adapter.getDescription();

      List<String> extensions = adapter.getExtensions();
      for (String extension: extensions) {
        if (filename.endsWith("." + extension)) {
          return adapter;
        }
      }
    }
    // just try first adapter if none match extension
    DataAdapterI da = Config.inst().getDefaultFileAdapter();
    String suffix = "";
    if (filename.indexOf(".") > 0)
      suffix = filename.substring(filename.indexOf("."));
    String m = "File suffix " + suffix + " does not map to any"
      +" known data adapter.  Using default/first data adapter: "+da.getDescription();
    m += adapterList;
    JOptionPane.showMessageDialog(null,m,"No data adapter specified",
                                  JOptionPane.INFORMATION_MESSAGE);
    return da;
  }
  
  
  private class DataAdapterFileFilter extends FileFilter {

    private DataAdapterI dataAdapter;

    DataAdapterFileFilter(DataAdapterI adapter) {
      super();
      dataAdapter = adapter;
    }

    public boolean accept(File f) {
      if (f.isDirectory()) {
        return true;
      }
      boolean matches = false;
      List<String> extensions = getExtensions();
      for (String extension: extensions) {
        matches = (f.getName().endsWith("." + extension));
        if (matches) break;
      }  
      return matches;
    }

    public String getDescription() {
      return dataAdapter.getDescription();
    }

    public List<String> getExtensions() {
      return dataAdapter.getExtensions();
    }

    public DataAdapterI getAdapter() {
      return dataAdapter;
    }
  }

  private static Logger log() {
    return Logger.getLogger(LoadSaveManager.class);
  }

  private static void setTitleOnPhenoteWindowForFile(File file) {
    final String docName = file != null ? file.getName() : "Untitled";
    final String windowTitle;
//    windowTitle = docName + " - " + PhenoteStartupTask.getAppName();  // This would be better, but that method can't be static.
    windowTitle = PhenotePlus.getAppName() + ": " + docName;
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          GUIManager.getManager().getFrame().setTitle(windowTitle);
        }
      });
  }
}
