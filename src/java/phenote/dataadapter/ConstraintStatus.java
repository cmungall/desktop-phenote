package phenote.dataadapter;

import java.util.ArrayList;
import java.util.List;

/** A constraint returns creates & returns one of these, says whether contraint checks 
    out ok and gives error message
    A constraintStatus is recursive - it can hold ConstraintStatus kids - so if theres 
    a list of constraints all their status is returned in one ConstraintStatus */

public class ConstraintStatus {
  
  /** is there more states than OK & Fail? if so will need an enum */
  private boolean isFailure = false;
  private String message = "";
  private List<ConstraintStatus> kids;

  public static ConstraintStatus OK = new ConstraintStatus(false);

  public ConstraintStatus(boolean isFailure,String message) {
    this(isFailure);
    if (message!=null) this.message = message;
  }

  public ConstraintStatus(boolean isFailure) {
    this.isFailure = isFailure;
  }

  public boolean constraintFailed() {
    if (!hasKids())
      return isFailure;
    else {
      for (ConstraintStatus cs : kids) {
        if (cs.constraintFailed()) return true;
      }
      return false; // ?
    }
  }

  public String getMessage() {
    if (!hasKids())
      return message;
    else {
      StringBuffer sb = new StringBuffer();
      // can parent have independent message?
      if (message!=null & !message.equals("")) sb.append(message).append("\n");
      for (ConstraintStatus kid : kids) {
        sb.append(kid.getMessage()).append("\n\n");
      }
      return sb.toString();
    }
  }

  void addStatusChild(ConstraintStatus kid) {
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
