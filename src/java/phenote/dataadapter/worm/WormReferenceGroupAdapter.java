package phenote.dataadapter.worm;

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
      if (c.hasValue("Pub"))
        sb.append(c.getValueString("Pub"));
      if (c.hasValue("Person"))
        sb.append("/").append(c.getValueString("Person"));
      // eventually this will be a list
      if (c.hasValue("NBP"))
        sb.append(", ").append(c.getValueString("NBP"));
      // this will also be a list
      if (c.hasValue("OtherRemark"))
        sb.append(" { ").append(c.getValueString("OtherRemark")).append(" }");
      
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

}
