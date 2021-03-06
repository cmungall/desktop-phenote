package phenote.dataadapter.worm;

import java.util.List;

import phenote.datamodel.CharacterI;
import phenote.edit.EditManager;


import phenote.dataadapter.AbstractCommitConstraint;
import phenote.dataadapter.ConstraintStatus;
import phenote.dataadapter.Status;


public class WormConstraint extends AbstractCommitConstraint {

  
  @Override
  /** Overrides AbsCC to check deletes */
  public ConstraintStatus checkCommit() {
    boolean warning = false;
    String message = "";

    // Check for DELETES
    List<CharacterI> l = EditManager.inst().getDeletedAnnotations();
    try {
      for (CharacterI echr : l) {
        String joinkey = echr.getValueString("PgdbId");
        if (joinkey.equals("")) { continue; }
        message += "Deleting character for "+joinkey+".  ";  warning = true;	// give warning for any deleted characters
      }
    } catch (Exception e) {
      System.out.println("Could not delete character: " + e);
    }
    ConstraintStatus statusBundle = ConstraintStatus.makeOK();
    if (warning) 
      statusBundle.addStatusChild(new ConstraintStatus(Status.WARNING,message));
   
    // CHECK EACH CHAR
    // from abstract - calls checkCharCommit for each char
    ConstraintStatus charListStatus = checkEachChar();
    statusBundle.addStatusChild(charListStatus);

    return statusBundle;
  }

  /** do constraint check for commit time on a character
   return ConstraintStatus indication if constraint passed and error msg */
  protected ConstraintStatus checkCharCommit(CharacterI chr) {
    
    boolean warning = false;

    String message = ""; // or StringBuffer?
    String temp_message = "";

    Integer flagBad = 0;


    try {
      String pgdbid = chr.getValueString("PgdbId");
      if ( (pgdbid == null) || (pgdbid == "") ) { 
        String allele = chr.getValueString("Object Name");
        message += "New character for "+allele+".  ";  warning = true;	// give warning for any new characters
        pgdbid = "with new value for "+allele;
      }

      String tag_name = "Allele Status";
      if ( chr.hasValue(tag_name) ) { 
        String allele_status = chr.getTerm(tag_name).getID(); 
        if (allele_status.equals("mapped_wrong")) { 
          if (chr.hasValue("Object Remark")) { } else {
            warning = true; message += "Character "+pgdbid+" Mapped Wrong and has no Object Remark.  "; } } }

      tag_name = "Object Type";
      if ( chr.hasValue(tag_name) ) { 
        String object_type = chr.getValueString(tag_name);
        if (object_type.equals("Strain")) { 
          if (chr.hasValue("Host Strain")) {
            warning = true; message += "Character "+pgdbid+" is of type Strain and has data for Host Strain.  "; } } }


      if (chr.hasValue("NBP")) { 
          return ConstraintStatus.makeOK(); }
        else { 
          temp_message += "Character "+pgdbid+" has no NBP.  "; }	// only add this if flagBad is bad (no NBP is okay if other fields are okay)
  
      if (chr.hasValue("Phenotype")) { } else { 
        flagBad++;
        message += "Character "+pgdbid+" has no Phenotype Term.  "; }
  
      if (chr.hasValue("Curator")) { } else { 
        flagBad++;
        message += "Character "+pgdbid+" has no Curator.  "; }
  
      if ( (chr.hasValue("Person")) || (chr.hasValue("Pub")) ) { } else { 
        flagBad++;
        message += "Character "+pgdbid+" has neither Person nor Paper.  "; }
  
      if (flagBad > 0) { warning = true; message += temp_message; }


//      if (chr.hasValue("NBP")) { } else { 
//        warning = true;
//        message += "Character has no NBP for char: "+chr; }
//         if (chr.hasValue("Object Name")) { } else { 
//           warning = true;
//           message += "Character has no Object Name"; }
//      
    // get fields ya need from chr
    // chr.hasValue("NBP")
      // ...
    
    
      // if doesnt have what you need... make a warning or failure?
      // if yada is null || xyz is null...
      // warning = true;
      // message += "Character such&such is lacking such&such";

    
    } catch (Exception e) {
      System.out.println("Could not delete character: " + e);
    }

    if (warning) 
      return new ConstraintStatus(Status.WARNING,message);
    
    else
      return ConstraintStatus.makeOK();


  }


}
