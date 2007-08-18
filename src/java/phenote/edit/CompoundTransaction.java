package phenote.edit;

import java.util.ArrayList;
import java.util.List;

import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharField;
import phenote.datamodel.CharacterI;

/** used for bulk updating (see TermCompList) */
public class CompoundTransaction implements TransactionI {

  // for now we just have UpdateTransactions - later will have Add & Del trans
  List<TransactionI> childTransactions = new ArrayList<TransactionI>();

//   /** for bulk updates of string/free text */
//   public CompoundTransaction(List<CharacterI>chars,CharFieldEnum e, String newVal) {
//     for (CharacterI ch : chars)
//       childTransactions.add(new UpdateTransaction(ch,e,newVal));
//   }
  public static CompoundTransaction makeUpdate(List<CharacterI>crs,CharField cf,String v){
    CompoundTransaction upTrans = new CompoundTransaction();
    for (CharacterI ch : crs)
      upTrans.addTransaction(new UpdateTransaction(ch,cf,v));
    return upTrans;
  }

//   /** bulk updates of terms */
//   public CompoundTransaction(List<CharacterI>chars,CharFieldEnum e, OBOClass newVal) {
//     for (CharacterI ch : chars)
//       childTransactions.add(new UpdateTransaction(ch,e,newVal));
//   }
  public static CompoundTransaction makeUpdate(List<CharacterI>l,CharField c,OBOClass o){
    CompoundTransaction upTrans = new CompoundTransaction();
    for (CharacterI ch : l)
      upTrans.addTransaction(new UpdateTransaction(ch,c,o));
    return upTrans;
  }

  public List<CharacterI> getDeletedAnnotations() {
    List<CharacterI>l = new ArrayList<CharacterI>();
    for (TransactionI ch : childTransactions) { l.addAll(ch.getDeletedAnnotations()); }
    return l;
  }

  private CompoundTransaction() {}

  private void addTransaction(TransactionI trans) {
    childTransactions.add(trans);
  }

  public static CompoundTransaction makeCopyTrans(List<CharacterI> charsToCopy, CharacterListManager clManager) {
    CompoundTransaction copyTrans = new CompoundTransaction();
    // error if empty list?
    for (CharacterI c : charsToCopy) {
      CharacterI copy = c.cloneCharacter();
      AddTransaction at = new AddTransaction(copy, clManager);
      copyTrans.addTransaction(at);
    }
    return copyTrans;
  }

  public static CompoundTransaction makeDelTrans(List<CharacterI> delChars, CharacterListManager clManager) {
    CompoundTransaction delTrans = new CompoundTransaction();
    for (CharacterI c : delChars)
      delTrans.addTransaction(new DeleteTransaction(c, clManager));
    return delTrans;
  }

  /** just call editModel on kids */
  public void editModel() {
    for (TransactionI childTrans : childTransactions)
      childTrans.editModel();
  }

  public void undo() {
    // actually i think for undo it should be done in reverse - this actually
    // matters for delete getting order correct
    //for (TransactionI childTrans : childTransactions)
    //childTrans.undo();
    for (int i=childTransactions.size()-1; i>=0; i--)
      childTransactions.get(i).undo();
  }

  List<TransactionI> getTransactions() { return childTransactions; }

  public List<CharacterI> getCharacters() {
    List<CharacterI>l = new ArrayList<CharacterI>();
    for (TransactionI t : getTransactions())
      l.addAll(t.getCharacters());
    return l;
  }

  public boolean isAdd() {
    if (!hasTransactions()) return false;
    for (TransactionI t : getTransactions())
      if (!t.isAdd()) return false;
    return true;
  }

  /** returns true if first child is update, assumes kids are homogenous
      which currently is a true assumption, change this if that becomes untrue */
  public boolean isUpdate() {
    if (!hasTransactions()) return false;
    return firstChild().isUpdate();
  }

//   /** assumes all transactions have same enum which is true at moment */
//   public CharFieldEnum getCharFieldEnum() {
//     return firstChild().getCharFieldEnum();
//   }

  /** queries 1st child for this. assumes all transactions have same charfield/update
      status which is true at moment (change if need) */
  public boolean isUpdateForCharField(CharField cf) {
    if (!hasTransactions()) return false;
    return firstChild().isUpdateForCharField(cf);
  }

  // should this even be allowed?
  private boolean hasTransactions() {
    return !childTransactions.isEmpty();
  }
  
  public OBOClass getNewTerm() { 
//	  System.out.println("trying to get new term for compound transaction");
	if (firstChild().isUpdate())
	    return firstChild().getNewTerm(); 
	else return null; 
  }

  private TransactionI firstChild() {
    if (!hasTransactions()) return null; // ex?
    return childTransactions.get(0);
  }
  
  public String getNewValueString() { return "blah blah blah";}


  
}



