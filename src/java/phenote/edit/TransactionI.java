package phenote.edit;

import phenote.datamodel.CharField;
import org.geneontology.oboedit.datamodel.OBOClass;


// interface? class?
public interface TransactionI {

  public void editModel();

  public void undo();
  
  public OBOClass getNewTerm();
  
  //public String getNewValueString();

  public boolean isUpdate();
  
  ///** this is actually update sepcific - but thats ok i think */
  //public CharFieldEnum getCharFieldEnum();
  public boolean isUpdateForCharField(CharField cf);
  //public String getValueString();
}
