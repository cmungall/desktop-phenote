package phenote.dataadapter;

import phenote.datamodel.CharField;
import phenote.datamodel.CharacterI;

/** Commit Constraint that checks that field has a value, if not returns FAILURE status */

public class RequiredFieldConstraint extends AbstractCommitConstraint {

  private CharField charField;
  /** if true do failure, false -> warning */
  private boolean doFailure = true;

  /** @param CharField that is required
      @param doFailure true -> fails, false -> warns */ 
  public RequiredFieldConstraint(CharField cf,boolean doFailure) {
    charField = cf;
    this.doFailure = doFailure;
  }

  protected ConstraintStatus checkCharCommit(CharacterI chr) {
    if (!chr.hasValue(charField)) {
      String m="Field "+charField.getName()+" is required to have a value for row:"+chr;
      Status st = doFailure ? Status.FAILURE : Status.WARNING;
      return new ConstraintStatus(st,m);
    }
    else
      return ConstraintStatus.makeOK();
  }

}
