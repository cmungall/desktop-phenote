package phenote.gui;

import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

/**
 * This table provides some workarounds to bugs in Sun's JTable.
 * @author Jim Balhoff
 */
public class BugWorkaroundTable extends JTable {

  public BugWorkaroundTable() {
    super();
}

  public BugWorkaroundTable(TableModel dm) {
    super(dm);
  }

  public BugWorkaroundTable(TableModel dm, TableColumnModel cm) {
    super(dm, cm);
  }

  public BugWorkaroundTable(int numRows, int numColumns) {
    super(numRows, numColumns);
  }

  @SuppressWarnings("unchecked")
  public BugWorkaroundTable(Vector rowData, Vector columnNames) {
    super(rowData, columnNames);
  }

  public BugWorkaroundTable(Object[][] rowData, Object[] columnNames) {
    super(rowData, columnNames);
  }

  public BugWorkaroundTable(TableModel dm, TableColumnModel cm,
      ListSelectionModel sm) {
    super(dm, cm, sm);
  }

  /**
   * JTable incorrectly begins editing of table cells when various modifier keys are pressed.  This 
   * results in bizarre behavior when trying to select or copy rows.
   * See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4820794
   * @see javax.swing.JTable#processKeyBinding(javax.swing.KeyStroke, java.awt.event.KeyEvent, int, boolean)
   */
  @Override
  protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
    if (e.getKeyCode() == KeyEvent.VK_CAPS_LOCK) {
      return false;
    }
    // from http://lists.apple.com/archives/Java-dev/2004/Dec/msg00283.html
    boolean retValue = false;
    if (e.getKeyCode()!=KeyEvent.VK_META || e.getKeyCode()!=KeyEvent.VK_CONTROL || e.getKeyCode()!=KeyEvent.VK_ALT) {
      if (e.isControlDown() || e.isMetaDown() || e.isAltDown()) {
        InputMap map = this.getInputMap(condition);
        ActionMap am = getActionMap();
        if (map != null && am != null && isEnabled()) {
          Object binding = map.get(ks);
          Action action = (binding == null) ? null : am.get(binding);
          if (action != null) {
            SwingUtilities.notifyAction(action, ks, e, this, e.getModifiers());
            retValue = false;
          }
          else {
            try {
              JComponent ancestor = (JComponent)
              SwingUtilities.getAncestorOfClass(Class.forName("javax.swing.JComponent"), this);
              ancestor.dispatchEvent(e);
            }
            catch (ClassNotFoundException fr) {
              log().error(fr.toString());
            }
          }
        }
        else {
          retValue = super.processKeyBinding(ks, e, condition, pressed);
        }
      }
      else {
        retValue = super.processKeyBinding(ks, e, condition, pressed);
      }
    }
    return retValue;
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
}
