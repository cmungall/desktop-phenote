package phenote.gui.factories;

import javax.swing.JPanel;

import main.ProtocolEditor;

import org.bbop.framework.AbstractComponentFactory;
import org.bbop.framework.GUIComponentWrapper;

public class ProtocolEditorFactory extends AbstractComponentFactory<GUIComponentWrapper> {

    public FactoryCategory getCategory() {
      return FactoryCategory.ANNOTATION;
    }
    public String getName() { return "Protocol Editor"; }
    public String getID() { return "protocol-editor"; }
    public GUIComponentWrapper doCreateComponent(String id) {
      JPanel p = ProtocolEditor.getUniqueInstance().getMainPanel();
      // 1st is id, 2nd id -> title bar string
      return new GUIComponentWrapper(id, id, p);
    }
  }

