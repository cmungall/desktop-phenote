package phenote.dataadapter.worm;

import java.util.regex.*;

import org.apache.log4j.Logger;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharFieldException;
import phenote.dataadapter.AbstractGroupAdapter;

/* Makes worm Reference list based off of Publication, Person, NBP, and
 * OtherRemark */

public class WormReferenceGroupAdapter extends AbstractGroupAdapter {

  public WormReferenceGroupAdapter(String group) {
    super(group);
  }
  protected String makeNameFromChar(CharacterI c) {
    StringBuilder sb = new StringBuilder();
    try {
      if (c.hasValue("Pub")) {
        String pubID = c.getTerm("Pub").getID();	// get the Papers's ID
        String match = find("([0-9]+)", pubID);         // Find a set of digits
        sb.append(match);				// append it to the stringbuilder
        String title = c.getValueString("Pub");		// get the Paper's value (the title)
        match = find("^(.{15})", title);         	// Find the first 15 characters
        if (match != null) { sb.append("_").append(match); }	// if there's a match append it
          else { sb.append("_").append(title); } }		// otherwise append the full title
      else { sb.append("_"); }				// if there's no publication append a single underscore
      if (c.hasValue("Person")) {
        String personID = c.getTerm("Person").getID();
        String match = find("([0-9]+)", personID);      // Find a set of digits
        sb.append("_").append(match);
      // eventually this will be a list
        String name = c.getValueString("Person");
        match = find("^(.{15})", name);         	// Find the first 15 characters
        if (match != null) { sb.append("_").append(match); }
          else { sb.append("_").append(name); } }
      else { sb.append("_").append("_"); }
      if (c.hasValue("NBP")) {
      // this will also be a list
        String nbp = c.getValueString("NBP");
        String match = find("^(.{15})", nbp);		// Find the first 15 characters
        if (match != null) { sb.append("_").append(match); }
          else { sb.append("_").append(nbp); } }
      else { sb.append("_"); }
// OtherRemark is not part of the Ref ID
//      if (c.hasValue("OtherRemark")) {
//        String othRem = c.getValueString("OtherRemark");
//        String match = find("^(.{15})", othRem);	// Find the first 15 characters
//        if (match != null) { sb.append("_").append(match); }
//          else { sb.append("_").append(othRem); } }
//      else { sb.append("_"); }
      
    } catch (CharFieldException e) {
      log().error(e.getMessage());
    }
    return sb.toString();
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
