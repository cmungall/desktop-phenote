package phenote.edit;

import phenote.datamodel.CharField;

// interface? class?
public interface TransactionI {

  public void editModel();

  public void undo();

  public boolean isUpdate();

  ///** this is actually update sepcific - but thats ok i think */
  //public CharFieldEnum getCharFieldEnum();
  public boolean isUpdateForCharField(CharField cf);
  //public String getValueString();
}
