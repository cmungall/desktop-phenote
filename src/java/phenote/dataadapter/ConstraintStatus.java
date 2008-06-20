package phenote.dataadapter;

import java.util.ArrayList;
import java.util.List;

/** A constraint returns creates & returns one of these, says whether contraint checks 
    out ok and gives error message
    A constraintStatus is recursive - it can hold ConstraintStatus kids - so if theres 
    a list of constraints all their status is returned in one ConstraintStatus */

public class ConstraintStatus {
  
  /** is there more states than OK & Fail? if so will need an enum */
  //private boolean isFailure = false;
  private Status status = Status.OK;
  private String message = "";
  private List<ConstraintStatus> kids;

  //public static enum Status { OK, WARNING, FAILURE };
  

  // causes endless loops - woops
  //public static ConstraintStatus OK_STATUS = new ConstraintStatus(Status.OK);

  public static ConstraintStatus makeOK() {
    return new ConstraintStatus(Status.OK);
  }
  /** list & OK same thing really - for clarity */
  public static ConstraintStatus makeList() { return makeOK(); }

  public ConstraintStatus(Status status,String message) {
    this(status);
    if (message!=null) this.message = message;
  }

  public ConstraintStatus(Status status) {
    this.status = status;
  }

  /** recurses through kids status's */
  public boolean isFailure() {
    return recursiveCheckForStatus(Status.FAILURE);
  }

  /** non recursive - just for self */
  private boolean selfIsFailure() { return status == Status.FAILURE; }

  public boolean isWarning() {
    return recursiveCheckForStatus(Status.WARNING);
  }
  private boolean selfIsWarning() { return status == Status.WARNING; }

  
  /** recurively goes through kids and if any kid or parent has status returns true */
  private boolean recursiveCheckForStatus(Status status) {
    if (!hasKids())
      return this.status == status;
    else {
      for (ConstraintStatus kid : kids) {
        if (kid.recursiveCheckForStatus(status)) return true;
      }
      return this.status == status; // ? self
    }
  }

  private boolean isSelfStatus(Status status) { return this.status == status; }

  /** recurse through self & kids, append any message from CS that has status */
  private String getMessage(Status status) {
    if (!hasKids())
      return message;
    else {
      StringBuffer sb = new StringBuffer();
      // can parent have independent message?
      if (this.status == status && message!=null && !message.equals(""))
        sb.append(message);//.append("\n");
      for (ConstraintStatus kid : kids) {
        if (kid.recursiveCheckForStatus(status))
          sb.append(kid.getMessage(status)).append("\n");
      }
      return sb.toString();
    }
  }

  public String getFailureMessage() { return getMessage(Status.FAILURE); }

  public String getWarningMessage() { return getMessage(Status.WARNING); }

  public void addStatusChild(ConstraintStatus kid) {
    getKids().add(kid);
  }

  private boolean hasKids() { 
    return kids!= null && !kids.isEmpty();
  }

  private List<ConstraintStatus> getKids() {
    if (kids==null) kids = new ArrayList<ConstraintStatus>(3);
    return kids;
  }

}
