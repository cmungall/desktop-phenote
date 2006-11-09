package phenote.dataadapter;

import java.util.List;
import java.io.File;

import phenote.datamodel.CharacterListI;


public interface DataAdapterI {

  // it is weird that these have different return values, but load() should go away in the near future
  public void load();
  public CharacterListI load(File f);

  public void commit(CharacterListI charList);
  public void commit(CharacterListI charList, File f);

  /** Set value to use for loading or writeback, for a file adapter this would be
      the file name - is there a better name for this method? 
      For now just doing String - which may be sufficient - may need an 
      AdapterValue/DataInput object if this gets more involved - but that may
      not be necasary - certianly string ok for now */
  public void setAdapterValue(String adapterValue);
  
  public List<String> getExtensions();
  public String getDescription();

}
