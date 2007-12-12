package phenote.gui;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractComponentFactory;

import phenote.config.Config;


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
    return "Annotation Table (" + Config.inst().getTitleForGroup(this.group) + ")";
  }
  
  @Override
  public boolean isSingleton() {
    return true;
  }

  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
