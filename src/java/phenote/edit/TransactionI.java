package phenote.edit;

import java.util.List;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharField;
import org.geneontology.oboedit.datamodel.OBOClass;


// interface? class?
public interface TransactionI {

  public List<CharacterI> getDeletedAnnotations();

  public void editModel();

  public void undo();
  
  public OBOClass getNewTerm();
  
  //public String getNewValueString();

  public boolean isUpdate();
  public boolean isAdd();
  public List<CharacterI> getCharacters();
 
  // public boolean isDelete();
  // public String getDBIDString
 
  ///** this is actually update sepcific - but thats ok i think */
  //public CharFieldEnum getCharFieldEnum();
  public boolean isUpdateForCharField(CharField cf);
  //public String getValueString();
}
