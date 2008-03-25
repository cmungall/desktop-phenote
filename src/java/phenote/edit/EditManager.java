package phenote.edit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterEx;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterIFactory;
import phenote.datamodel.Comparison;

/** The way editing works is gui makes update transaction (see CharFieldGui and
    AutoComboBox) and calls
    EditManager.updateModel(), EM calls UpdateTrans constructs CharFieldValues and calls
    CVF.editModel which call charFieldEnum.setValue which calls the appropriate
    method in CharacterI. EM also shoots out char change event to its listeners 
    UpdateTrans takes Strings(for free text) or OBOClasses(ontologies).  */

public class EditManager {

  //private static EditManager singleton;
  private static Map<String,EditManager> groupToEditMan =
    new HashMap<String,EditManager>();

  private List<CharChangeListener> charListeners = new ArrayList<CharChangeListener>(3);

  private List<TransactionI> transactionList = new ArrayList<TransactionI>();
  
  private CharacterListManager characterListManager;
  
//   public EditManager() {
//     this(CharacterListManager.inst());
//   }
  
  private EditManager(CharacterListManager clManager) {
    this.characterListManager = clManager;
  }

  public List<CharacterI> getDeletedAnnotations() {
    List<CharacterI> l = new ArrayList<CharacterI>();
    for (TransactionI t : getTransactionList()) { l.addAll(t.getDeletedAnnotations()); }
    return l;
  }

  // gets the "default" (group) edit manager
  public static EditManager inst() {
    return getEditManager(CharFieldManager.DEFAULT_GROUP);
//     if (singleton == null)
//       singleton = new EditManager();
//     return singleton;
  }
  
  public static void reset() {
    groupToEditMan.clear();
  }

  /** clear out all transactions - this should be done after every writeback shouldnt it
      should be done by default i think */
  public void clearTransactions() {
    transactionList.clear();
  }

  // ??
  public static EditManager getEditManager(String group) {
    if (group == null) group = CharFieldManager.DEFAULT_GROUP;
    if (groupToEditMan.get(group) == null) {
      CharacterListManager c = CharacterListManager.getCharListMan(group);
      EditManager e = new EditManager(c); // group?
      groupToEditMan.put(group,e);
    }
    return groupToEditMan.get(group);
  }

  public void addCharChangeListener(CharChangeListener l) { charListeners.add(l); }
  public void removeCharChangeListener(CharChangeListener l) {charListeners.remove(l);}

  public void undo() {
    if (!hasUndoableTransaction()) {
      log().info("No transactions to undo");
      return;
    }
    getCurrentTransaction().undo();
    fireChangeEvent(new CharChangeEvent(this,getCurrentTransaction()));
    // for now with no redo just toss that last trans
    transactionList.remove(getCurrentTransaction());
  }

  public boolean hasUndoableTransaction() {
    return !transactionList.isEmpty(); // for now - no redo
  }

  public TransactionI getCurrentTransaction() {
    // for now just return last one - eventually with redo do some tracking
    if (!hasUndoableTransaction()) return null; // ex?
    return transactionList.get(transactionList.size()-1);
  }
  
  public List<TransactionI> getTransactionList() {
	  return transactionList;
  }

//   public void updateModel(Object source, UpdateTransaction ut) {
//     addTransaction(ut);
//     updateModelUpdateTrans(source,ut);
//   }

//   /** fires change event, doesnt add to transaction list */
//   private void updateModelUpdateTrans(Object src, UpdateTransaction ut) {
//     ut.editModel();
//     CharChangeEvent e = new CharChangeEvent(src,ut);
//     fireChangeEvent(e);
//   }

  public void updateModel(Object src, TransactionI ct) { // CompTrans
    //for (TransactionI t : ct.getTransactions())
    //updateModelUpdateTrans(source,t);
    ct.editModel();
    addTransaction(ct);
    fireChangeEvent(new CharChangeEvent(src,ct));
    // or should we send out a char change event with a char list?? probably
  }

//   // pase - remove - need to deal with list of comps
//   public void addComparison(Object src, Comparison c) throws CharacterEx {
//     UpdateTransaction ut = new UpdateTransaction(c);
//     updateModel(src,ut);
//   }

  /** delete all existing comparisons and add new comparisons, and put all this
      in one undoable transaction */
  public void replaceAllComparisons(Object src,List<Comparison> newComps) {
    CompoundTransaction t = new CompoundTransaction();
    
    //List<Comparison> old = characterListManager.getComparisons();
    // do this in compoundTrans?
    for (CharFieldValue oldToDelete : characterListManager.getComparisonValues())
      t.addTransaction(delFromListTransaction(oldToDelete));

    for (Comparison c : newComps)
      t.addTransaction(UpdateTransaction.addComparison(c));

    //CompoundTransaction t = CompoundTransaction.replaceComparisons(old,newComps);
//     for (Comparison c : )
//       ct.addTransaction(UpdateTransaction.delComparison(c));
//     for (Comparison c : newComps)
//       ct.addTransaction(UpdateTransaction.addComparison(c));
    editAddAndFire(t,src);
  }


  public void deleteFromValList(Object src, CharFieldValue kidToDelete) {
//     CharFieldValue oldListParent = c.getValue(cf);
//     CharFieldValue newListParent = oldListParent.cloneCharFieldValue();
//     newListParent.removeKid(kidToDelete);
//     UpdateTransaction t = new UpdateTransaction(oldListParent,newListParent);
    UpdateTransaction t = delFromListTransaction(kidToDelete);
    editAddAndFire(t,src); // sets new value that has kid removed
  }

  private UpdateTransaction delFromListTransaction(CharFieldValue kidToDelete) {
    CharacterI c = kidToDelete.getCharacter();
    CharField cf = kidToDelete.getCharField();
    CharFieldValue oldListParent = c.getValue(cf);
    CharFieldValue newListParent = oldListParent.cloneCharFieldValue();
    newListParent.removeKid(kidToDelete); // crucial!
    UpdateTransaction t = new UpdateTransaction(oldListParent,newListParent);
    return t;
  }

  /** The initial blank character is a fundamental undoable state, so dont
      keep the transaction for this as its not undoable */
  public void addInitialCharacter() {
    //AddTransaction at = new AddTransaction(new Character());
    //at.editModel();
    addCharacter(CharacterIFactory.makeChar(), false);
    // no addTransaction(at)!
    //return at;
  }

  /** same as addInitialChar except we record the adding in the transaction list
      as it can be undone */
  public void addNewCharacter() {
    addCharacter(CharacterIFactory.makeChar(), true);
    //addTransaction(at);
  }

  public void addCharacter(CharacterI c) {
    addCharacter(c,true);
  }

  // fire char change event???
  private void addCharacter(CharacterI c, boolean recordTrans) {
    AddTransaction at = new AddTransaction(c, this.characterListManager);
    at.editModel();
    if (recordTrans) addTransaction(at);
    fireChangeEvent(new CharChangeEvent(this,at)); // this???
  }

  public void copyChars(List<CharacterI> charsToCopy) {
    if (charsToCopy.isEmpty()) {
      log().error("No chars to make copy of");
      return;
    }
    CompoundTransaction ct = CompoundTransaction.makeCopyTrans(charsToCopy, this.characterListManager);
    ct.editModel(); // clones & adds char to char list
    addTransaction(ct);
    // dont need yet an event for this - might eventually
    // would this be a char change or charList change or something else?
    // i think a char list change though thats used for new data load???
    // i think char change is ok?? most listeners just resynch with model ignore evt
    fireChangeEvent(new CharChangeEvent(this,ct));
  }

  public void deleteChars(List<CharacterI> delChars) {
    if (delChars.isEmpty()) {
      log().error("No chars to delete");
      return;
    }
    CompoundTransaction ct = CompoundTransaction.makeDelTrans(delChars, this.characterListManager);
    ct.editModel();
    addTransaction(ct);
    fireChangeEvent(new CharChangeEvent(this, ct));
  }

  private void addTransaction(TransactionI t) {
    transactionList.add(t);
    //System.out.println("got trans "+t); new Throwable().printStackTrace();
  }

  /** edit model via transaction, add transaction to transaction list, and
      fire change event with transaction & src */
  private void editAddAndFire(TransactionI t, Object src) {
    t.editModel();
    addTransaction(t);
    fireChangeEvent(t,src);
  }

  private void fireChangeEvent(TransactionI t, Object src) {
    fireChangeEvent(new CharChangeEvent(src,t));
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
