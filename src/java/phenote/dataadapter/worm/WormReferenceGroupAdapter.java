package phenote.dataadapter.worm;

import java.util.regex.*;

import org.apache.log4j.Logger;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.TermNotFoundException;
import phenote.dataadapter.AbstractGroupAdapter;

/* Makes worm Reference list based off of Publication, Person, NBP, and
 * OtherRemark */

public class WormReferenceGroupAdapter extends AbstractGroupAdapter {

  public WormReferenceGroupAdapter(String group) {
    super(group);
  }

  protected boolean recordsId() { return true; }

  /** this should be read only and probably hidden */
  protected void setIdField(CharacterI c, String id) {
    try { c.setValue("RefID",id); }
    catch (CharFieldException e) {log().error("cant set id field"+e.getMessage());}
    catch (TermNotFoundException x) { log().error(x); }
  }

  protected String makeNameFromChar(CharacterI c) {
    try {
        String pubID = null;
        if (c.getTerm("Pub") != null) { pubID = c.getTerm("Pub").getID(); }	// get the Papers's ID
        String title = c.getValueString("Pub");		// get the Paper's value (the title)
        String personID = null;
        if (c.getTerm("Person") != null) { personID = c.getTerm("Person").getID(); }
        String name = c.getValueString("Person");
        String nbp = c.getValueString("NBP");
        String refID = makeNameFromPubPersonNBP(pubID, title, personID, name, nbp);
        return refID;      
    } catch (CharFieldException e) {
      log().error(e.getMessage());
      return null;
    }
  }

  static String makeNameFromPubPersonNBP( String pubID, String title, String personID, String name, String nbp ) {
    StringBuilder sb = new StringBuilder();
    if (pubID != null) {
        String match = find("([0-9]+)", pubID);         // Find a set of digits
        sb.append(match);				// append it to the stringbuilder
        match = find("^(.{15})", title);         	// Find the first 15 characters
        if (match != null) { sb.append("_").append(match); }	// if there's a match append it
          else { sb.append("_").append(title); } }		// otherwise append the full title
      else { sb.append("_"); }				// if there's no publication append a single underscore
    if (personID != null) {
        String match = find("([0-9]+)", personID);      // Find a set of digits
        sb.append("_").append(match);
      // eventually this will be a list
        match = find("^(.{15})", name);         	// Find the first 15 characters
        if (match != null) { sb.append("_").append(match); }
          else { sb.append("_").append(name); } }
      else { sb.append("_").append("_"); }
    if (nbp != null) {
      // this will also be a list
        String match = find("^(.{15})", nbp);		// Find the first 15 characters
        if (match != null) { sb.append("_").append(match); }
          else { sb.append("_").append(nbp); } }
      else { sb.append("_"); }
    return sb.toString();
  }

  static String[] getPubPersonNBPFromReference( String reference, String joinkey ) {
    System.out.println("getBoxFromPubPersonNBP reference --"+reference+"-- end");
    Integer pubID = 0; String title = null;
    Integer personID = 0; String name = null;
    String nbp = null;
    String match = find(":(.*)_.*_.*_.*_.*", reference);
    if (match != null) { 
      try { pubID = Integer.parseInt(match); }
        catch (ArrayIndexOutOfBoundsException e) { System.out.println("this is okay : can't parse pubID "+e); }
        catch (NumberFormatException e) { System.out.println("this is okay : can't parse pubID "+e); } }	// get the pubID number if there is one
    System.out.println("pubID "+pubID+" end");
    match = find(":.*_(.*)_.*_.*_.*", reference);
    if (match != null) { title = match; }
    System.out.println("title "+title+" end");
    match = find(":.*_.*_(.*)_.*_.*", reference);
    if (match != null) { 
    try { personID = Integer.parseInt(match); }
      catch (ArrayIndexOutOfBoundsException e) { System.out.println("this is okay : can't parse personID "+e); }
      catch (NumberFormatException e) { System.out.println("this is okay : can't parse personID "+e); } }	// get the personID number if there is one
    System.out.println("personID "+personID+" end");
    match = find(":.*_.*_.*_(.*)_.*", reference);
    if (match != null) { name = match; }
    System.out.println("name "+name+" end");
    match = find(":.*_.*_.*_.*_(.*)", reference);
    if (match != null) { nbp = match; }
    System.out.println("nbp "+nbp+" end");
    System.out.println("pubID "+pubID+" title "+title+" personID "+personID+" name "+name+" nbp "+nbp+" end");

    String value[] = new String[3];
    if (pubID < 1) { value[0] = ""; }
      else if (pubID < 10) { value[0] = "WBPaper0000000" + pubID; }
      else if (pubID < 100) { value[0] = "WBPaper000000" + pubID; }
      else if (pubID < 1000) { value[0] = "WBPaper00000" + pubID; }
      else if (pubID < 10000) { value[0] = "WBPaper0000" + pubID; }
      else if (pubID < 100000) { value[0] = "WBPaper000" + pubID; }
      else if (pubID < 1000000) { value[0] = "WBPaper00" + pubID; }
      else if (pubID < 10000000) { value[0] = "WBPaper0" + pubID; }
      else if (pubID < 100000000) { value[0] = "WBPaper" + pubID; }
    if (personID < 1) { value[1] = ""; }
      else { value[1] = "WBPerson" + personID; }
    match = find("(postgres value )", nbp);
    if (match != null) { value[2] = ""; }
      else { value[2] = nbp; }

//    Connection c = connectToDB();
//    Statement s = null;
//    try { s = c.createStatement(); }
//      catch (SQLException se) {
//      System.out.println("We got an exception while creating a statement: that probably means we're no longer connected."); se.printStackTrace(); System.exit(1); }
//
//    SELECT app_box, app_person FROM app_person WHERE joinkey = 'joinkey' ORDER BY app_timestamp

    return value;
  }

  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }

  public static String find(String patternStr, CharSequence input) {
      Pattern pattern = Pattern.compile(patternStr);
      Matcher matcher = pattern.matcher(input);
//       System.out.println("Pattern "+pattern+" end");
      if (matcher.find()) { return matcher.group(1); }
      return null;
  }


}
