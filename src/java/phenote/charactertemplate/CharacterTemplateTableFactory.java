package phenote.charactertemplate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractComponentFactory;

import phenote.config.Config;

public class CharacterTemplateTableFactory extends AbstractComponentFactory<CharacterTemplateTable> {
  
  private String group;
  private String groupClassName;

  public CharacterTemplateTableFactory(String group, String adapterClassName) {
    super();
    this.group = group;
    this.groupClassName = adapterClassName;
  }
  
  public String getID() {
    return this.group;
  }
  
  @Override
  public CharacterTemplateTable doCreateComponent(String id) {
    return this.createTemplateTable(this.groupClassName, id);
  }

  public FactoryCategory getCategory() {
    return FactoryCategory.ANNOTATION;
  }

  public String getName() {
    return Config.inst().getTitleForGroup(this.group);
  }
  
  public boolean isSingleton() {
    return true;
  }
  
  private CharacterTemplateTable createTemplateTable(String className, String id) {
    final String errorMessage = "Failed creating CharacterTemplateTable";
    try {
      Class<?> adapterClass = Class.forName(className);
      Constructor<?> constructor = adapterClass.getConstructor(String.class, String.class);
      Object templateTable = constructor.newInstance(this.group, id);
      return (CharacterTemplateTable)templateTable;
    } catch (ClassNotFoundException e) {
      log().error(errorMessage, e);
    } catch (InstantiationException e) {
      log().error(errorMessage, e);
    } catch (IllegalAccessException e) {
      log().error(errorMessage, e);
    } catch (NoSuchMethodException e) {
      log().error(errorMessage, e);
    } catch (InvocationTargetException e) {
      log().error(errorMessage, e);
    }
    return null;
  }

  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
}
