package phenote.dataadapter.fly;

import org.apache.log4j.Logger;

import phenote.dataadapter.AbstractGroupAdapter;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharacterI;

/** Makes fly style genotype from locus allele 1, locus allele 2, non locus allele,
    and accesories - with datatags LA1, LA2, NLA, ACC */

public class FlyGenotypeGroupAdapter extends AbstractGroupAdapter {

  public FlyGenotypeGroupAdapter(String group) {
    super(group);
  }
  protected String makeNameFromChar(CharacterI c) {
    StringBuilder sb = new StringBuilder();
    try {
      if (c.hasValue("LA1"))
        sb.append(c.getValueString("LA1"));
      if (c.hasValue("LA2"))
        sb.append("/").append(c.getValueString("LA2"));
      // eventually this will be a list
      if (c.hasValue("NLA"))
        sb.append(", ").append(c.getValueString("NLA"));
      // this will also be a list
      if (c.hasValue("ACC"))
        sb.append(" { ").append(c.getValueString("ACC")).append(" }");
      
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
