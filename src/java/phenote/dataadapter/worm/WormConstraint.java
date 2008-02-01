package phenote.dataadapter.worm;

import java.util.List;

import phenote.datamodel.CharacterI;

import phenote.dataadapter.AbstractCommitConstraint;
import phenote.dataadapter.ConstraintStatus;
import phenote.dataadapter.ConstraintStatus.Status;

public class WormConstraint extends AbstractCommitConstraint {

  /** do constraint check for commit time on a character
   return ConstraintStatus indication if constraint passed and error msg */
  protected ConstraintStatus checkCharCommit(CharacterI chr) {
    
    boolean warning = false;

    String message = ""; // or StringBuffer?

      
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

    
    if (warning) 
      return new ConstraintStatus(Status.WARNING,message);
    
    else
      return ConstraintStatus.makeOK();

  }


}
