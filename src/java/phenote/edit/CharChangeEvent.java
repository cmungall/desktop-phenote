package phenote.edit;

import java.util.EventObject;

import phenote.datamodel.CharField;

/** This actually is for a CharField change - rename this CharFieldChangeEvent? */
public class CharChangeEvent extends EventObject {

  // UpdateTrans -> Transaction? yes!
  //private UpdateTransaction transaction;
  private TransactionI transaction;
  // boolean undo??? or in trans? of just editManager?

  CharChangeEvent(Object source, TransactionI t) {
    super(source);
    transaction = t;
  }

  public boolean isUpdate() { return transaction.isUpdate(); }


  public boolean isUpdateForCharField(CharField cf) {
    //return transaction.getCharFieldEnum() == cf.getCharFieldEnum();
    //if (!transaction.isUpdate()) return false;
    return transaction.isUpdateForCharField(cf);
  }
//   public String getValueString() { // getNewVal, getOldVal???
//     // if undo return ut.getOldValue().getName()???
//     return transaction.getNewValueString();
//   }

}
