package phenote.dataadapter;

import java.io.File;
import java.util.List;
import java.lang.ClassNotFoundException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import phenote.dataadapter.DataAdapterI;
import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharacterListI;
import phenote.config.Config;


public class LoadSaveManager {
  
  private static LoadSaveManager singleton;
  private JFileChooser fileChooser;
  
  private LoadSaveManager() {
    fileChooser = new JFileChooser();
    List<DataAdapterI> adapters = Config.inst().getDataAdapters();
    for (DataAdapterI adapter: adapters) {
      DataAdapterFileFilter filter = new DataAdapterFileFilter(adapter);
      fileChooser.addChoosableFileFilter(filter);
    }
  }
  
  /**Returns singleton*/
  public static LoadSaveManager inst() {
    if (singleton == null)
      singleton = new LoadSaveManager();
    return singleton;
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
    CharacterListManager.inst().setCharacterList(this,charList);
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
    CharacterListI charList = CharacterListManager.inst().getCharacterList();
    adapter.commit(charList, f);
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
    return Config.inst().getSingleDataAdapter();
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
