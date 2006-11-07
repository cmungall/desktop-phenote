package phenote.edit;

import java.util.ArrayList;
import java.util.List;

import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.CharacterI;

public class CompoundTransaction implements TransactionI {

  // for now we just have UpdateTransactions - later will have Add & Del trans
  List<UpdateTransaction> childTransactions = new ArrayList<UpdateTransaction>();

  public CompoundTransaction(List<CharacterI>chars,CharFieldEnum e, String newVal) {
    for (CharacterI ch : chars)
      childTransactions.add(new UpdateTransaction(ch,e,newVal));
  }
  public CompoundTransaction(List<CharacterI>chars,CharFieldEnum e, OBOClass newVal) {
    for (CharacterI ch : chars)
      childTransactions.add(new UpdateTransaction(ch,e,newVal));
  }

  List<UpdateTransaction> getTransactions() { return childTransactions; }
}
