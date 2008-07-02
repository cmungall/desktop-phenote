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

  /** Checks all "non-blank" chars. blanks are just an artifact of the gui
      and should be filtered out before committing. auto-gen fields (date_created)
      dont factor into blankness. */
  protected ConstraintStatus checkEachChar() {
    ConstraintStatus statusBundle = ConstraintStatus.makeOK();

    // get list with no blanks
    List<CharacterI> list = CharacterListManager.inst().getNonBlankList();
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
