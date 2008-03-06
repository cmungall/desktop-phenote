package phenote.dataadapter;

import java.util.List;

import phenote.datamodel.CharacterI;

public abstract class AbstractCommitConstraint implements Constraint {

  /** Return true if constraint should be checked at commit time to 
      dataadapter */
  public boolean isCommitConstraint() {
    return true;
  }
  /** do constraint check for commit time - should char list be passed in? 
   return ConstraintStatus indication if constraint passed and error msg
   should only be called if isCommitConstraint is true */
  public ConstraintStatus checkCommit() {
    return checkEachChar();
  }
  
  protected ConstraintStatus checkEachChar() {
    ConstraintStatus statusBundle = ConstraintStatus.makeOK();

    List<CharacterI> list = CharacterListManager.inst().getCharacterList().getList();
    for (CharacterI chr : list) {
      ConstraintStatus charStatus = checkCharCommit(chr);
      statusBundle.addStatusChild(charStatus);
    }
    return statusBundle;
  }

  /** Check char and return ConstraintStatus */
  protected abstract ConstraintStatus checkCharCommit(CharacterI chr);
  

  /** false - its just a commit constraint */
  public boolean isEditConstraint() { return false; }
  public ConstraintStatus checkEdit() { return null; }

}
