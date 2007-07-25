package phenote.dataadapter;


import org.apache.log4j.Logger;

import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.impl.OBOClassImpl;

import phenote.datamodel.CharField;
import phenote.datamodel.CharacterI;

public class AllFieldsGroupAdapter extends AbstractGroupAdapter {

  public AllFieldsGroupAdapter(String group) {
    super(group);
  }

  // public boolean isFieldValueMaker() { return true; } ??

  // protected? - subclass override?
  protected OBOClass makeOboClassFromChar(CharacterI c) {
    if (c.hasNoContent()) return null; // ex?
    StringBuilder sb = new StringBuilder();
    for (CharField cf : c.getAllCharFields()) {
      if (c.hasValue(cf))
        sb.append(c.getValueString(cf)).append('_');
    }
    if (sb.length() == 0) return null; //?
    sb.deleteCharAt(sb.length()-1);
    String name = sb.toString();
    sb.insert(0,':');
    String id = sb.toString();
    OBOClass o = new OBOClassImpl(name,id);
    o.setNamespace(getNamespace()); // ???
    return o;
  }



  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }

}
