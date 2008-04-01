package phenote.dataadapter;


import org.apache.log4j.Logger;

import phenote.datamodel.CharField;
import phenote.datamodel.CharacterI;

public class AllFieldsGroupAdapter extends AbstractGroupAdapter {

  public AllFieldsGroupAdapter(String group) {
    super(group);
  }

  // public boolean isFieldValueMaker() { return true; } ??


  protected String makeNameFromChar(CharacterI c) {
    StringBuilder sb = new StringBuilder();
    for (CharField cf : c.getAllCharFields()) {
      if (c.hasValue(cf))
        sb.append(c.getValueString(cf)).append('_');
    }
    if (sb.length() == 0) return null; //?
    sb.deleteCharAt(sb.length()-1);
    String name = sb.toString();
    return name;
  }

  private Logger log;
  @SuppressWarnings("unused")
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }

}
