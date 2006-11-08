package phenote.edit;

import phenote.datamodel.CharField;

// interface? class?
public interface TransactionI {

  public void editModel();

  ///** this is actually update sepcific - but thats ok i think */
  //public CharFieldEnum getCharFieldEnum();
  public boolean isUpdateForCharField(CharField cf);
  //public String getValueString();
}
