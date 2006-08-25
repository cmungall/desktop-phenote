package phenote.edit;

import java.util.EventObject;

import phenote.datamodel.CharField;

/** This actually is for a CharField change - rename this CharFieldChangeEvent? */
public class CharChangeEvent extends EventObject {

  // UpdateTrans -> Transaction?
  private UpdateTransaction transaction;
  // boolean undo??? or in trans? of just editManager?

  CharChangeEvent(Object source, UpdateTransaction ut) {
    super(source);
    transaction = ut;
  }

  public String getValueString() { // getNewVal, getOldVal???
    // if undo return ut.getOldValue().getName()???
    return transaction.getNewValueString();
  }

  public boolean isForCharField(CharField cf) {
    return transaction.getCharFieldEnum() == cf.getCharFieldEnum();
  }
}
