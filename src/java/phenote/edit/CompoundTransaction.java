package phenote.edit;

import java.util.ArrayList;
import java.util.List;

import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.CharacterI;

public class CompoundTransaction implements TransactionI {

  // for now we just have UpdateTransactions - later will have Add & Del trans
  List<TransactionI> childTransactions = new ArrayList<TransactionI>();

  /** for bulk updates of string/free text */
  public CompoundTransaction(List<CharacterI>chars,CharFieldEnum e, String newVal) {
    for (CharacterI ch : chars)
      childTransactions.add(new UpdateTransaction(ch,e,newVal));
  }
  /** bulk updates of terms */
  public CompoundTransaction(List<CharacterI>chars,CharFieldEnum e, OBOClass newVal) {
    for (CharacterI ch : chars)
      childTransactions.add(new UpdateTransaction(ch,e,newVal));
  }

  private CompoundTransaction() {}

  private void addTransaction(TransactionI trans) {
    childTransactions.add(trans);
  }

  public static CompoundTransaction makeCopyTrans(List<CharacterI> charsToCopy) {
    CompoundTransaction copyTrans = new CompoundTransaction();
    // error if empty list?
    for (CharacterI c : charsToCopy) {
      CharacterI copy = c.cloneCharacter();
      AddTransaction at = new AddTransaction(copy);
      copyTrans.addTransaction(at);
    }
    return copyTrans;
  }

  /** just call editModel on kids */
  public void editModel() {
    for (TransactionI childTrans : childTransactions)
      childTrans.editModel();
  }

  public void undo() {
    for (TransactionI childTrans : childTransactions)
      childTrans.undo();
  }

  List<TransactionI> getTransactions() { return childTransactions; }

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

  private TransactionI firstChild() {
    if (!hasTransactions()) return null; // ex?
    return childTransactions.get(0);
  }
}
