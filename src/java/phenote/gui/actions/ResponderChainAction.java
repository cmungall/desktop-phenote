package phenote.gui.actions;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import org.apache.log4j.Logger;

/**
 * An action which traverses the component hierarchy, beginning with the currently focused component, 
 * until it finds a object implementing a method with the name of the action's actionCommand.  The
 * method must accept no arguments.
 * @author Jim Balhoff
 */
@SuppressWarnings("serial")
public class ResponderChainAction extends AbstractAction {
  
  private String actionCommand;

  /**
   * Creates a ResponderChainAction object which will try to invoke a method named by the given actionCommand.
   * The ResponderChainAction will use the actionCommand as the description string and a default icon.
   * @param actionCommand The name of the method this action invokes.
   */
  public ResponderChainAction(String actionCommand) {
    this(actionCommand, actionCommand, null);
  }

  /**
   * Creates a ResponderChainAction object which will try to invoke a method named by the given actionCommand.
   * The ResponderChainAction will use the specified description string and a default icon.
   * @param actionCommand The name of the method this action invokes.
   * @param name The action's description string.
   */
  public ResponderChainAction(String actionCommand, String name) {
    this(actionCommand, name, null);
  }

  /**
   * Creates a ResponderChainAction object which will try to invoke a method named by the given actionCommand.
   * The ResponderChainAction will use the specified description string and the specified icon.
   * @param actionCommand The name of the method this action invokes.
   * @param name The action's description string.
   * @param icon The action's icon.
   */
  public ResponderChainAction(String actionCommand, String name, Icon icon) {
    super(name, icon);
    this.setActionCommand(actionCommand);
  }

  /**
   * Invoked when an action occurs.  The action searches the component hierarchy for a object
   * which can respond to the action's actionCommand, and if successful invokes that method on the object.
   */
  public void actionPerformed(ActionEvent event) {
    final Object target = this.getValidResponder();
    if (target == null) {
      log().debug("No valid targets");
      return;
    }
    final Method method = this.getActionMethod(target, this.getActionCommand());
    try {
      method.invoke(target);
    } catch (IllegalArgumentException e) {
      log().error("Unable to invoke method on target", e);
    } catch (IllegalAccessException e) {
      log().error("Unable to invoke method on target", e);
    } catch (InvocationTargetException e) {
      log().error("Unable to invoke method on target", e);
    }
  }

  public String getActionCommand() {
    return actionCommand;
  }

  public void setActionCommand(String actionCommand) {
    this.actionCommand = actionCommand;
  }

  private Component getFocusOwner() {
    return KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
  }
  
  /**
   * Returns the first found object which implements a method named by the action's
   * actionCommand, or null if none is found.
   */
  private Object getValidResponder() {
    if (this.getActionCommand() == null) return null;
    Object responder = this.getNextResponder(null);
    while (responder != null) {
      if (this.objectRespondsToActionCommand(responder, this.getActionCommand())) {
        return responder;
      } else {
        responder = this.getNextResponder(responder);
      }
    }
    return responder;
  }

  /**
   * Returns the next object which should be queried for an implementation of the
   * actionCommand.  This is typically the parent Component of an existing Component,
   * but in the future we may want to establish a convention for querying a delegate of
   * the final component.  This would presumably be a controller class.  If null is 
   * passed, the currently focused Component is returned.
   */
  private Object getNextResponder(Object currentResponder) {
    if (currentResponder == null) return this.getFocusOwner();
    if (currentResponder instanceof Component) {
      final Component parent = ((Component)currentResponder).getParent();
      if (parent != null) {
        return parent;
      } else {
        // in the future we may want to establish conventions for delegate objects to search here after exhausting the component hierarchy
      }
    }
    return null;
  }
  
  private boolean objectRespondsToActionCommand(Object target, String action) {
    return this.getActionMethod(target, action) != null;
  }
  
  private Method getActionMethod(Object target, String action) {
    try {
      final Method method = target.getClass().getMethod(action);
      return method;
    } catch (SecurityException e) {
      return null;
    } catch (NoSuchMethodException e) {
      return null;
    }
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
}
