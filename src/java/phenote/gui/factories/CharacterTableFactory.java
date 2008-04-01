package phenote.gui.factories;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractComponentFactory;

import phenote.config.Config;
import phenote.datamodel.CharFieldManager;
import phenote.gui.CharacterTable;


public class CharacterTableFactory extends AbstractComponentFactory<CharacterTable> {
  
  private String group;
  
  public CharacterTableFactory(String group) {
    super();
    this.group = group;
  }

  @Override
  public CharacterTable doCreateComponent(String id) {
    return new CharacterTable(this.group, id);
  }

  public FactoryCategory getCategory() {
    return FactoryCategory.ANNOTATION;
  }

  public String getID() {
    return "annotation_table_" + this.group;
  }

  public String getName() {
    if (this.group.equals(CharFieldManager.getDefaultGroup())) {
      return "Annotation Table (" + Config.inst().getTitleForGroup(this.group) + ")";
    } else {
      return Config.inst().getTitleForGroup(this.group);
    }
    
  }
  
  @Override
  public boolean isSingleton() {
    return true;
  }

  @SuppressWarnings("unused")
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
