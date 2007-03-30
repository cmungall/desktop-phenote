package phenote.dataadapter;

import phenote.datamodel.CharField;
import phenote.datamodel.CharacterListI;

public interface QueryableDataAdapterI {
  /** return true if data adapter can query for the char field */
  public boolean isFieldQueryable(String field);
  /** Throws exception if query fails, and no data to return */
  public CharacterListI query(String field, String query) throws DataAdapterEx;

  public void commit(CharacterListI charList);
  /** The label that gets displayed on the db commit button */
  public String getCommitButtonLabel();
}

