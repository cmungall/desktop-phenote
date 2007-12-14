package phenote.gui;

import org.bbop.framework.GUIComponent;
import org.bbop.framework.dock.LayoutListener;

/**
 * This abstract class lets subclassers implement just the parts of the interface they need.
 */
public abstract class AbstractLayoutListener implements LayoutListener {

  public void add(GUIComponent parent, GUIComponent child) {
    // TODO Auto-generated method stub

  }

  public boolean closing(GUIComponent c) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean docking(GUIComponent component) {
    // TODO Auto-generated method stub
    return false;
  }

  public void focusChanged(GUIComponent old, GUIComponent newComponent) {
    // TODO Auto-generated method stub

  }

  public boolean maximizing(GUIComponent component) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean minimizing(GUIComponent component) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean restoring(GUIComponent component) {
    // TODO Auto-generated method stub
    return false;
  }

  public void titleChanged(GUIComponent component, String newTitle) {
    // TODO Auto-generated method stub

  }

  public boolean undocking(GUIComponent component) {
    // TODO Auto-generated method stub
    return false;
  }

}
