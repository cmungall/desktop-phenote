package phenote.dataadapter;

public interface Constraint {

  /** Return true if constraint should be checked at commit time to 
      dataadapter */
  public boolean isCommitConstraint();
  /** do constraint check for commit time - should char list be passed in? 
   return ConstraintStatus indication if constraint passed and error msg
   should only be called if isCommitConstraint is true */
  public ConstraintStatus checkCommit();

  /** Return true if constraint should be checked after user edits */
  public boolean isEditConstraint();
  /** do constraint check after user edit - should char list be passed in? 
   return ConstraintStatus indication if constraint passed and error msg
   should only be called if isEditConstraint is true */
  public ConstraintStatus checkEdit();
  

}
