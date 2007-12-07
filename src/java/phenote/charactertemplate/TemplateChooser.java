package phenote.charactertemplate;

import java.awt.event.ActionListener;
import java.util.Collection;

import org.bbop.framework.GUIComponent;

import phenote.datamodel.CharField;
import phenote.datamodel.CharacterI;

public interface TemplateChooser extends ActionListener, GUIComponent {

  public static final String SHOW_CHOOSER_ACTION = "showChooser";
  
  public void showChooser();
  
  public void setCharField(CharField field);
  
  public CharField getCharField();
  
  public void setTitle(String title);
  
  public String getTitle();
  
  public void setGroup(String group);
  
  public String getGroup();
  
  public void addTemplateChoiceListener(TemplateChoiceListener listener);
  
  public void removeTemplateChoiceListener(TemplateChoiceListener listener);
  
  public Collection<CharacterI> getChosenTemplates(Collection<CharacterI> candidates);
  
}
