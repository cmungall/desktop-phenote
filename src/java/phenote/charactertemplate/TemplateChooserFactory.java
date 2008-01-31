package phenote.charactertemplate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractComponentFactory;

import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharFieldManager;

public class TemplateChooserFactory extends AbstractComponentFactory<TemplateChooser> {
  
  private String group;
  private String adapterClassName;
  private String title;
  private String field;

  public TemplateChooserFactory(String group, String adapterClassName, String title, String field) {
    super();
    this.group = group;
    this.adapterClassName = adapterClassName;
    this.title = title;
    this.field = field;
  }
  
  public String getID() {
    return this.group + "-chooser";
  }
  
  public boolean isSingleton() {
    return true;
  }

  @Override
  public TemplateChooser doCreateComponent(String id) {
    TemplateChooser chooser = this.createTemplateChooser(this.adapterClassName, id);
    chooser.setTitle(this.title);
    chooser.setGroup(this.group);
    try {
      chooser.setCharField(CharFieldManager.inst().getCharFieldForName(this.field));
    } catch (CharFieldException e) {
      log().error("Couldn't get CharField with name " + this.field, e);
    }
    return chooser;
  }

  public FactoryCategory getCategory() {
    return FactoryCategory.ANNOTATION;
  }

  public String getName() {
    return this.title;
  }
  
  private TemplateChooser createTemplateChooser(String className, String id) {
    final String errorMessage = "Failed creating CharacterTemplateTable";
    try {
      Class<?> adapterClass = Class.forName(className);
      Constructor<?> constructor = adapterClass.getConstructor(String.class);
      Object templateChooser = constructor.newInstance(id);
      return (TemplateChooser)templateChooser;
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
