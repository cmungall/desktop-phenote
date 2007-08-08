package phenote.charactertemplate;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import phenote.datamodel.CharField;
import phenote.datamodel.CharacterI;

public abstract class AbstractTemplateChooser implements TemplateChooser {
  
  private CharField charField;
  private String title;
  private Set<TemplateChoiceListener> templateChoiceListeners = new HashSet<TemplateChoiceListener>();

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
  }

}
