package phenote.dataadapter;

import java.util.List;

import phenote.datamodel.CharField;
import phenote.datamodel.CharacterListI;

public interface QueryableDataAdapterI {
  /** return true if data adapter can query for the char field */
  public boolean isFieldQueryable(String field);

  /** returns a list of group strings of groups that can be queried for with query
      method */
  public List<String> getQueryableGroups();

  /** Throws exception if query fails, and no data to return
      Query with query string of type field for group */
  public CharacterListI query(String group, String field, String query)
    throws DataAdapterEx;

  public void commit(CharacterListI charList);
  /** The label that gets displayed on the db commit button */
  public String getCommitButtonLabel();
}

