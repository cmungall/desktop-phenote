package org.phenoscape.view;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.bbop.framework.ViewMenu;
import org.phenoscape.model.PhenoscapeController;

import phenote.gui.actions.ResponderChainAction;

public class MenuFactory {
  
  private final PhenoscapeController controller;
  
  public MenuFactory(PhenoscapeController controller) {
    this.controller = controller;
  }

  public Collection<? extends JMenuItem> createMenus() {
    Collection<JMenuItem> menus = new ArrayList<JMenuItem>();
    menus.add(this.createFileMenu());
    menus.add(this.createEditMenu());
    menus.add(new ViewMenu());
    return menus;
  }

  private JMenuItem createFileMenu() {
    final JMenu menu = new JMenu("File");
    final Action openAction = new AbstractAction("Open...") {
      public void actionPerformed(ActionEvent e) { controller.open(); }
    };
    openAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    menu.add(new JMenuItem(openAction));
    menu.addSeparator();
    final Action saveAction = new AbstractAction("Save") {
      public void actionPerformed(ActionEvent e) { controller.save(); }
    };
    saveAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    menu.add(new JMenuItem(saveAction));
    final Action saveAsAction = new AbstractAction("Save As...") {
      public void actionPerformed(ActionEvent e) {
      }
    };
    saveAsAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK));
    menu.add(new JMenuItem(saveAsAction));
    return menu;
  }
  
  private JMenuItem createEditMenu() {
    //TODO finish deleting lines
    final JMenu menu = new JMenu("Edit");
    final Action undoAction = new ResponderChainAction("undo", "Undo");
    undoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    menu.add(new JMenuItem(undoAction));
    final Action redoAction = new ResponderChainAction("redo", "Redo");
    redoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK));
    menu.add(new JMenuItem(redoAction));
    menu.addSeparator();
    JMenuItem cut = new JMenuItem();
    Action cutAction = new ResponderChainAction("cut", "Cut");
    cutAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    cut.setAction(cutAction);
    menu.add(cut);
    JMenuItem copy = new JMenuItem();
    Action copyAction = new ResponderChainAction("copy", "Copy");
    copyAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    copy.setAction(copyAction);
    menu.add(copy);
    JMenuItem paste = new JMenuItem();
    Action pasteAction = new ResponderChainAction("paste", "Paste");
    pasteAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    paste.setAction(pasteAction);
    menu.add(paste);
    JMenuItem selectAll = new JMenuItem();
    Action selectAllAction = new ResponderChainAction("selectAll", "Select All");
    selectAllAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    selectAll.setAction(selectAllAction);
    menu.add(selectAll);
    return menu;
  }

}
