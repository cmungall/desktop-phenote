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

  List<TransactionI> getTransactions() { return childTransactions; }

//   /** assumes all transactions have same enum which is true at moment */
//   public CharFieldEnum getCharFieldEnum() {
//     return firstChild().getCharFieldEnum();
//   }

  /** queries 1st child for this. assumes all transactions have same charfield/update
      status which is true at moment (change if need) */
  public boolean isUpdateForCharField(CharField cf) {
    return firstChild().isUpdateForCharField(cf);
  }

  private TransactionI firstChild() { return childTransactions.get(0); }
}
