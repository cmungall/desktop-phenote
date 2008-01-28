package phenote.dataadapter;

import java.io.File;

public interface LoadSaveListener {
  
  public void fileSaved(File f);
  
  public void fileLoaded(File f);

}
