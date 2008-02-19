package phenote.gui.field;

import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import org.bbop.framework.AbstractGUIComponent;

import phenote.gui.CharacterTableSource;

/**
 * A component which dynamically swaps in the correct FieldPanel for the most recently focused character table.  
 * It relies on updates from the character tables via setTableSource to know when one has become active.
 * @author Jim Balhoff
 */
public class FieldPanelContainer extends AbstractGUIComponent {
  
  private static final long serialVersionUID = 8979068659549011611L;
  private static FieldPanelContainer currentFieldPanelContainer;
  private Map<CharacterTableSource, FieldPanel> panels = new HashMap<CharacterTableSource, FieldPanel>();
  private Object responderDelegate;

  public FieldPanelContainer(String id) {
    super(id);
    FieldPanelContainer.currentFieldPanelContainer = this;
    this.setLayout(new GridLayout());
  }

  /**
   * Returns the most recently created FieldPanelContainer, so that tables can notify it when they gain focus.
   * There is probably a better way for tables to keep track of this.
   */
  public static FieldPanelContainer getCurrentFieldPanelContainer() {
    return FieldPanelContainer.currentFieldPanelContainer;
  }
  
  /**
   * Resets the FieldPanel gui to edit the given table.
   */
  public void setTableSource(CharacterTableSource table) {
    if (!this.panels.containsKey(table)) {
      this.panels.put(table, new FieldPanel(table));
    }
    this.removeAll();
    final FieldPanel panel = this.panels.get(table);
    this.add(panel);
    for (CharFieldGui gui : panel.getCharFieldGuiList()) {
      gui.setListSelectionModel(table.getSelectionModel());
    }
    this.setResponderDelegate(table);
    this.repaint();
  }
  
  public Object getResponderDelegate() {
    return this.responderDelegate;
  }
  
  private void setResponderDelegate(Object delegate) {
    this.responderDelegate = delegate;
  }

}
