package phenote.charactertemplate;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractGUIComponent;
import org.bbop.framework.ComponentManager;
import org.bbop.framework.GUIComponent;

import phenote.datamodel.CharField;
import phenote.datamodel.CharacterI;

public abstract class AbstractTemplateChooser extends AbstractGUIComponent implements TemplateChooser {
  
  private CharField charField;
  private String title;
  private String group;
  private Set<TemplateChoiceListener> templateChoiceListeners = new HashSet<TemplateChoiceListener>();
  
  public AbstractTemplateChooser(String id) {
    super(id);
    // TODO Auto-generated constructor stub
  }

  public void addTemplateChoiceListener(TemplateChoiceListener listener) {
    this.templateChoiceListeners.add(listener);
  }
  
  public void removeTemplateChoiceListener(TemplateChoiceListener listener) {
    this.templateChoiceListeners.remove(listener);
  }

  public void setCharField(CharField field) {
    this.charField = field;
  }
  
  public CharField getCharField() {
    return charField;
  }
  
  public void setGroup(String group) {
    this.group = group;
  }
  
  public String getGroup() {
    return this.group;
  }
  
  public void setTitle(String aTitle) {
    this.title = aTitle;
  }
  
  public String getTitle() {
    return this.title;
  }

  public abstract Collection<CharacterI> getChosenTemplates(Collection<CharacterI> candidates);

  public abstract void showChooser();

  public void actionPerformed(ActionEvent event) {
    final String actionCommand = event.getActionCommand();
    if (actionCommand.equals(TemplateChooser.SHOW_CHOOSER_ACTION)) {
      this.showChooser();
    }
  }
  
  protected void fireTemplateChoiceChanged() {
    for (TemplateChoiceListener listener : this.templateChoiceListeners) {
      listener.templateChoiceChanged(this);
    }
    GUIComponent component = ComponentManager.getManager().getActiveComponent(this.getGroup() + ":main");
    log().debug("All components are: " + ComponentManager.getManager().getActiveComponentMap().keySet());
    //TODO check for null and class of component
    CharacterTemplateTable template = (CharacterTemplateTable)component;
    template.templateChoiceChanged(this);
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
