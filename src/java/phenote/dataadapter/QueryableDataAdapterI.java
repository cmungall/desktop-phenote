package phenote.dataadapter;

import phenote.datamodel.CharField;
import phenote.datamodel.CharacterListI;

public interface QueryableDataAdapterI {
  /** return true if data adapter can query for the char field */
  public boolean isCharFieldQueryable(CharField cf);
  public CharacterListI query(CharField cf, String query);
}