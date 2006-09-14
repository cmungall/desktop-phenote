package phenote.dataadapter;

//import java.util.List;

import phenote.datamodel.CharacterListI;

public interface DataAdapterI {

  public void load();

  public void commit(CharacterListI charList);

  /** Set value to use for loading or writeback, for a file adapter this would be
      the file name - is there a better name for this method? 
      For now just doing String - which may be sufficient - may need an 
      AdapterValue/DataInput object if this gets more involved - but that may
      not be necasary - certianly string ok for now */
  public void setAdapterValue(String adapterValue);

}
