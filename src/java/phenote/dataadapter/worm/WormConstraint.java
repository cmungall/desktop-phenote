package phenote.dataadapter.worm;

import java.util.List;

import phenote.datamodel.CharacterI;

import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.Constraint;
import phenote.dataadapter.ConstraintStatus;
import phenote.dataadapter.ConstraintStatus.Status;

public class WormConstraint implements Constraint {

  /** Return true if constraint should be checked at commit time to 
      dataadapter */
  public boolean isCommitConstraint() { return true; }
  /** do constraint check for commit time - should char list be passed in? 
   return ConstraintStatus indication if constraint passed and error msg
   should only be called if isCommitConstraint is true */
  public ConstraintStatus checkCommit() {
    
    List<CharacterI> list = CharacterListManager.inst().getCharacterList().getList();

    boolean warning = false;

    String message = ""; // or StringBuffer?

    for (CharacterI chr : list) {

      
      if (chr.hasValue("NBP")) { } else { 
        warning = true;
        message += "Character has no NBP"; }
//       if (chr.hasValue("Object Name")) { } else { 
//         warning = true;
//         message += "Character has no Object Name"; }
// 
      // get fields ya need from chr
      // chr.hasValue("NBP")
      // ...
      

      // if doesnt have what you need... make a warning or failure?
      // if yada is null || xyz is null...
      // warning = true;
      // message += "Character such&such is lacking such&such";


    }

    if (warning) 
      return new ConstraintStatus(Status.WARNING,message);
    
    else
      return ConstraintStatus.makeOK();

  }

  /** Return true if constraint should be checked after user edits */
  public boolean isEditConstraint() { return false; }
  /** do constraint check after user edit - should char list be passed in? 
   return ConstraintStatus indication if constraint passed and error msg
   should only be called if isEditConstraint is true */
  public ConstraintStatus checkEdit() { return null; }


}
