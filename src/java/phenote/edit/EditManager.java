package phenote.edit;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import phenote.datamodel.CharacterI;

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

  public void undo() {
    if (!haveUndoableTransaction()) {
      log().info("No transactions to undo");
      return;
    }
    getCurrentTransaction().undo();
    fireChangeEvent(new CharChangeEvent(this,getCurrentTransaction()));
    // for now with no redo just toss that last trans
    transactionList.remove(getCurrentTransaction());
  }

  private boolean haveUndoableTransaction() {
    return !transactionList.isEmpty(); // for now - no redo
  }

  private TransactionI getCurrentTransaction() {
    // for now just return last one - eventually with redo do some tracking
    if (!haveUndoableTransaction()) return null; // ex?
    return transactionList.get(transactionList.size()-1);
  }

  public void updateModel(Object source, UpdateTransaction ut) {
    addTransaction(ut);
    updateModelUpdateTrans(source,ut);
  }

  /** fires change event, doesnt add to transaction list */
  private void updateModelUpdateTrans(Object src, UpdateTransaction ut) {
    ut.editModel();
    CharChangeEvent e = new CharChangeEvent(src,ut);
    fireChangeEvent(e);
  }

  public void updateModel(Object src, CompoundTransaction ct) {
    //for (TransactionI t : ct.getTransactions())
    //updateModelUpdateTrans(source,t);
    ct.editModel();
    addTransaction(ct);
    fireChangeEvent(new CharChangeEvent(src,ct));
    // or should we send out a char change event with a char list?? probably
  }

  public void copyChars(List<CharacterI> charsToCopy) {
    if (charsToCopy.isEmpty()) {
      log().error("No chars to make copy of");
      return;
    }
    CompoundTransaction ct = CompoundTransaction.makeCopyTrans(charsToCopy);
    ct.editModel(); // clones & adds char to char list
    addTransaction(ct);
    // dont need yet an event for this - might eventually
    // would this be a char change or charList change or something else?
    // i think a char list change though thats used for new data load???
    // i think char change is ok?? most listeners just resynch with model ignore evt
    fireChangeEvent(new CharChangeEvent(this,ct));
  }

  private void addTransaction(TransactionI t) {
    transactionList.add(t);
    //System.out.println("got trans "+t);
  }

  private void fireChangeEvent(CharChangeEvent e) {
    for (CharChangeListener l : charListeners)
      l.charChanged(e);
  }

  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }

}
