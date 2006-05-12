package phenote.edit;

import java.util.EventObject;

public class CharChangeEvent extends EventObject {

  // UpdateTrans -> Transaction?
  private UpdateTransaction transaction;

  CharChangeEvent(Object source, UpdateTransaction ut) {
    super(source);
    transaction = ut;
  }
}
