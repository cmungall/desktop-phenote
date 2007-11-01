package phenote.gui;
/**
 * $Id: StackedBox.java.txt,v 1.1 2006/03/09 20:48:47 rbair Exp $<p>
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.<p>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.<p>
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.<p>
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA<p>
 *
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.*;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.*;
import org.jdesktop.swingx.*;
//import org.jdesktop.layout.*;


/**
 * Stacks components vertically in boxes. Each box is created with a title and a
 * component.<br>
 * 
 * <p>
 * The <code>StackedBox</code> can be added to a
 * {@link javax.swing.JScrollPane}.
 * 
 * <p>
 * Note: this class is not part of the SwingX core classes. It is just an
 * example of what can be achieved with the components.
 * 
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 * @author Nicole Washington
 *
 *  A helper class i retrieved from the web on 10.15.2007
 *  https://swinglabs.dev.java.net/source/browse/*checkout*
 *  /swinglabs/website/web/docs/components/JXCollapsiblePane/StackedBox.java.txt
 *  <p>
 *  This is to facilitate generating collapsible panels for use in the
 *  TermInfo box
 */
public class StackedBox extends JPanel implements Scrollable {

  private Color titleBackgroundColor;
  private Color titleForegroundColor;
  private Color separatorColor;
  private Border separatorBorder;
//  private JXHyperlink link;


  public StackedBox() {
    setLayout(new VerticalLayout());
    setOpaque(true);
    setBackground(Color.WHITE);

    separatorBorder = new SeparatorBorder();
    setTitleForegroundColor(Color.BLACK);
//  setTitleBackgroundColor(new Color(248, 248, 248));
    setTitleBackgroundColor(Color.LIGHT_GRAY);
    setSeparatorColor(new Color(214, 223, 247));
  }

  public Color getSeparatorColor() {
    return separatorColor;
  }

  public void setSeparatorColor(Color separatorColor) {
    this.separatorColor = separatorColor;
  }

  public Color getTitleForegroundColor() {
    return titleForegroundColor;
  }

  public void setTitleForegroundColor(Color titleForegroundColor) {
    this.titleForegroundColor = titleForegroundColor;
  }

  public Color getTitleBackgroundColor() {
    return titleBackgroundColor;
  }

  public void setTitleBackgroundColor(Color titleBackgroundColor) {
    this.titleBackgroundColor = titleBackgroundColor;
  }

  /**
   * Adds a new component to this <code>StackedBox</code>
   * 
   * @param title
   * @param component
   */
  public void addBox(String title, Component component) {
    final JXCollapsiblePane collapsible = new JXCollapsiblePane();
    collapsible.getContentPane().setBackground(Color.WHITE);
    collapsible.add(component);
    collapsible.setBorder(new CompoundBorder(separatorBorder, collapsible
      .getBorder()));

    Action toggleAction = collapsible.getActionMap().get(
      JXCollapsiblePane.TOGGLE_ACTION);
    // use the collapse/expand icons from the JTree UI
    toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON, UIManager
      .getIcon("Tree.expandedIcon"));
    toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON, UIManager
      .getIcon("Tree.collapsedIcon"));

//    link = new JXHyperlink(toggleAction);
    JXHyperlink link = new JXHyperlink(toggleAction);
    link.setText(title);
    link.setFont(link.getFont().deriveFont(Font.BOLD));
    link.setOpaque(true);
    link.setBackground(getTitleBackgroundColor());
    link.setFocusPainted(false);

    link.setUnclickedColor(getTitleForegroundColor());
    link.setClickedColor(getTitleForegroundColor());

    link.setBorder(new CompoundBorder(separatorBorder, BorderFactory
      .createEmptyBorder(2, 4, 2, 4)));
    link.setBorderPainted(true);

    add(link);
    add(collapsible);
  }

  /**
   * @see Scrollable#getPreferredScrollableViewportSize()
   */
  public Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }

  /**
   * @see Scrollable#getScrollableBlockIncrement(java.awt.Rectangle, int, int)
   */
  public int getScrollableBlockIncrement(Rectangle visibleRect,
    int orientation, int direction) {
    return 10;
  }

  /**
   * @see Scrollable#getScrollableTracksViewportHeight()
   */
  public boolean getScrollableTracksViewportHeight() {
    if (getParent() instanceof JViewport) {
      return (((JViewport)getParent()).getHeight() > getPreferredSize().height);
    } else {
      return false;
    }
  }

  /**
   * @see Scrollable#getScrollableTracksViewportWidth()
   */
  public boolean getScrollableTracksViewportWidth() {
    return true;
  }

  /**
   * @see Scrollable#getScrollableUnitIncrement(java.awt.Rectangle, int, int)
   */
  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation,
    int direction) {
    return 10;
  }

  /**
   * The border between the stack components. It separates each component with a
   * fine line border.
   */
  class SeparatorBorder implements Border {

    boolean isFirst(Component c) {
      return c.getParent() == null || c.getParent().getComponent(0) == c;
    }

    public Insets getBorderInsets(Component c) {
      // if the collapsible is collapsed, we do not want its border to be
      // painted.
      if (c instanceof JXCollapsiblePane) {
        if (((JXCollapsiblePane)c).isCollapsed()) { return new Insets(0, 0, 0,
          0); }
      }
      return new Insets(isFirst(c)?4:1, 0, 1, 0);
    }

    public boolean isBorderOpaque() {
      return true;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width,
      int height) {
      g.setColor(getSeparatorColor());
      if (isFirst(c)) {
        g.drawLine(x, y + 2, x + width, y + 2);
      }
      g.drawLine(x, y + height - 1, x + width, y + height - 1);
    }
  }

  //Nicole's modifications!
  /**
   * This method is designed to change the title of a given collapsible
   * pane.
   * @param title   title of the box
   * @param i    the pane for which the title will be changed
   */
  public void setBoxTitle(String title, int i) {
  	JXHyperlink link = (JXHyperlink)this.getComponent(i);  //i believe this returns the link for a given pane.
    link.setText(title);
//    link.setFont(link.getFont().deriveFont(Font.BOLD));
//    link.setOpaque(true);
//    link.setFocusPainted(false);
//    link.setUnclickedColor(getTitleForegroundColor());
//    link.setClickedColor(getTitleForegroundColor());
//
//    link.setBorder(new CompoundBorder(separatorBorder, BorderFactory
//      .createEmptyBorder(2, 4, 2, 4)));
//    link.setBorderPainted(true);
//    this.add(link,i);
  }
  
  public void setBoxTitleBackgroundColor(int i, Color color) {
  	JXHyperlink link = (JXHyperlink)this.getComponent(i);  //i believe this returns the link for a given pane.
  	link.setBackground(color);
  	
  }

  
  public void hideBox() {
  	
  }

  public void showBox() {
  	
  }
}
