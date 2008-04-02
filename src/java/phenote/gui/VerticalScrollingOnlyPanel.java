package phenote.gui;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Scrollable;

/**
 * A panel which can be placed into a JScrollPane and will only cause the scrollbar to appear 
 * vertically.  It will not grow any wider than its container.
 * @author Jim Balhoff
 */
public class VerticalScrollingOnlyPanel extends JPanel implements Scrollable {
  
  private final static int SCROLL_INCREMENT = new JTextField().getPreferredSize().height;

  
  public VerticalScrollingOnlyPanel() {
    super();
  }

  public VerticalScrollingOnlyPanel(boolean isDoubleBuffered) {
    super(isDoubleBuffered);
  }

  public VerticalScrollingOnlyPanel(LayoutManager layout, boolean isDoubleBuffered) {
    super(layout, isDoubleBuffered);
  }

  public VerticalScrollingOnlyPanel(LayoutManager layout) {
    super(layout);
  }

  public Dimension getPreferredScrollableViewportSize() {
    return this.getPreferredSize();
  }

  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    return 1;
  }

  public boolean getScrollableTracksViewportHeight() {
    return false;
  }

  public boolean getScrollableTracksViewportWidth() {
    return true;
  }

  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    return SCROLL_INCREMENT;
  }
  
}