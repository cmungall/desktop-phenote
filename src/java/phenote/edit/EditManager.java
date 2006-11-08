package phenote.edit;

import java.util.ArrayList;
import java.util.List;


/** The way editing works is gui makes update transaction (see CharFieldGui and
    AutoComboBox) and calls
    EditManager.updateModel(), EM calls UpdateTrans constructs CharFieldValues and calls
    CVF.editModel which call charFieldEnum.setValue which calls the appropriate
    method in CharacterI. EM also shoots out char change event to its listeners 
    UpdateTrans takes Strings(for free text) or OBOClasses(ontologies).  */

public class EditManager {

  private static EditManager singleton;

  private List<CharChangeListener> charListeners = new ArrayList<CharChangeListener>(3);

  private List<TransactionI> transactionList = new ArrayList<TransactionI>();

  private EditManager() {}

  public static EditManager inst() {
    if (singleton == null)
      singleton = new EditManager();
    return singleton;
  }

  public void addCharChangeListener(CharChangeListener l) {
    charListeners.add(l);
  }

  public void updateModel(Object source, UpdateTransaction ut) {
    transactionList.add(ut);
    updateModelUpdateTrans(source,ut);
  }

  /** fires change event, doesnt add to transaction list */
  private void updateModelUpdateTrans(Object src, UpdateTransaction ut) {
    ut.editModel();
    CharChangeEvent e = new CharChangeEvent(src,ut);
    fireChangeEvent(e);
  }

  public void updateModel(Object src, CompoundTransaction ct) {
    transactionList.add(ct);
    //for (TransactionI t : ct.getTransactions())
    //updateModelUpdateTrans(source,t);
    ct.editModel();
    fireChangeEvent(new CharChangeEvent(src,ct));
    // or should we send out a char change event with a char list?? probably
  }

  private void fireChangeEvent(CharChangeEvent e) {
    for (CharChangeListener l : charListeners)
      l.charChanged(e);
  }

}
