package phenote.dataadapter.worm;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.CharacterListI;
import phenote.dataadapter.DataAdapterEx;
import phenote.dataadapter.QueryableDataAdapterI;

public class WormAdapter implements QueryableDataAdapterI {

  private List<String> queryableFields = new ArrayList<String>(2);

  public WormAdapter() { init(); }

  private void init() {
    // dont HAVE to use CharFieldEnum but it does enforce using same strings
    // across different data adapters which is good to enforce
    // the worm config needs to have "Pub" and "Allele"
    queryableFields.add(CharFieldEnum.PUB.getName()); // "Pub"
    queryableFields.add(CharFieldEnum.ALLELE.getName()); // "Allele"
    // should their be a check that the current char fields have pub & allele?
  }

  /** return true if data adapter can query for the char field */
  public boolean isFieldQueryable(String field) {
    return queryableFields.contains(field);
  }
  public CharacterListI query(String field, String query) throws DataAdapterEx {
    String m = "Worm adapter query not yet implemented. field: "+field+" query: "+query;
    JOptionPane.showMessageDialog(null,m,"Worm stub",JOptionPane.INFORMATION_MESSAGE);
    
    // if query has failed...
    throw new DataAdapterEx("Worm query of "+query+" of field "+field+" failed");
  }

}
