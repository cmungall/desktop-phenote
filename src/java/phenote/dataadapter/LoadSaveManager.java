package phenote.dataadapter;

import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import phenote.config.Config;
import phenote.datamodel.CharacterListI;


public class LoadSaveManager {
  
  private static LoadSaveManager singleton;
  private JFileChooser fileChooser;
  private CharacterListManager characterListManager;
  
  private LoadSaveManager() {
    this(CharacterListManager.inst());
  }
  
  public LoadSaveManager(CharacterListManager clManager) {
    this.characterListManager = clManager;
    fileChooser = new JFileChooser();
    List<DataAdapterI> adapters = Config.inst().getDataAdapters();
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
  System.out.println("NEW!");
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
    CharacterListI charList = adapter.load(f);
    if (charList == null || charList.isEmpty()) {
      String m = "Failed to load in data for file "+f+" using data adapter "
        +adapter.getDescription();
      JOptionPane.showMessageDialog(null,m,"Load failure",JOptionPane.ERROR_MESSAGE); 
      return;
    }
    else {
      this.characterListManager.setCurrentDataFile(f);
      this.characterListManager.setCharacterList(this,charList);
    }
  }
  
  /**Saves the document's characters to a file, prompting the user to choose a file and data adapter.*/
  public void saveData() {
    File aFile = runSaveDialog();
    if (aFile != null) {
      FileFilter filter = fileChooser.getFileFilter();
      if (filter instanceof DataAdapterFileFilter) {
        DataAdapterI adapter = ((DataAdapterFileFilter)filter).getAdapter();
        saveData(aFile, adapter);
      } else {
        saveData(aFile);
      }
    }
  }
  
  /**Saves the document's characters to the given file, using the default data adapter for the file's extension.*/
  public void saveData(File f) {
    DataAdapterI adapter = getDataAdapterForFilename(f.getName());
    saveData(f, adapter);
  }
  
  /**Saves the document's characters to the given file using the given data adapter.*/
  public void saveData(File f, DataAdapterI adapter) {
    CharacterListI charList = this.characterListManager.getCharacterList();
    adapter.commit(charList, f);
  }
  
  public void exportData() {
    saveData();
  }

  
  private File runOpenDialog() {
    fileChooser.setAcceptAllFileFilterUsed(true);
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
    for (DataAdapterI adapter: adapters) {
      List<String> extensions = adapter.getExtensions();
      for (String extension: extensions) {
        if (filename.endsWith("." + extension)) {
          return adapter;
        }
      }
    }
    // just try first adapter if none match extension
    DataAdapterI da = Config.inst().getDefaultFileAdapter();
    String m = "Data adapter has not been specified, and file suffix does not map to any"
      +" data adapter, using default/first data adapter: "+da.getDescription();
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

}
