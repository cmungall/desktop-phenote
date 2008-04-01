package phenote.dataadapter;

import java.util.ArrayList;
import java.util.List;

/** ConstraintManager holds all constraints. a constraint can be for edit and/or
    commit time. ConstraintStatus is returned from checking constraint that indicates
    failure and gives error message. Singleton.
    should there be a constraint package? subpackage? */

public class ConstraintManager {
  
  private static ConstraintManager singleton;

  private List<Constraint> constraintList = new ArrayList<Constraint>(4);

  public static ConstraintManager inst() {
    if (singleton==null) singleton = new ConstraintManager();
    return singleton;
  }

  /** private singleton constructor */
  private ConstraintManager() {}

  public ConstraintStatus checkCommitConstraints() {
    ConstraintStatus statusList = ConstraintStatus.makeList(); //?
    for (Constraint c : constraintList) {
      if (c.isCommitConstraint()) {
        ConstraintStatus cs = c.checkCommit();
        statusList.addStatusChild(cs);
      }
    }
    return statusList;
  }

  public ConstraintStatus checkEditConstraints() {
    ConstraintStatus statusList = ConstraintStatus.makeList(); //?
    for (Constraint c : constraintList) {
      if (c.isEditConstraint()) {
        ConstraintStatus cs = c.checkEdit();
        statusList.addStatusChild(cs);
      }
    }
    return statusList;
  }

  

  public void addConstraint(Constraint c) {
    constraintList.add(c);
  }

}
