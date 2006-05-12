package phenote.dataadapter;

//import java.util.List;

import phenote.datamodel.CharacterListI;

public interface DataAdapterI {

  public void load();

  public void commit(CharacterListI charList);

}
