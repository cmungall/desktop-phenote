package phenote.dataadapter;

import phenote.datamodel.CharField;
import phenote.datamodel.CharacterI;

/** Commit Constraint that checks that field has a value, if not returns FAILURE status */

public class RequiredFieldConstraint extends AbstractCommitConstraint {

  private CharField charField;

  public RequiredFieldConstraint(CharField cf) {
    charField = cf;
  }

  ConstraintStatus checkCharCommit(CharacterI chr) {
    if (!chr.hasValue(charField)) {
      String m=chr+" field "+charField+" is required to have a value";
      return new ConstraintStatus(ConstraintStatus.Status.FAILURE,m);
    }
    else
      return ConstraintStatus.makeOK();
  }

}
