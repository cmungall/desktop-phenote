package phenote.gui;
// --> phenote.gui.menu ??

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MenuManager {
  
  private JMenuBar menuBar;

  private FileMenu fileMenu;
  
  private EditMenu editMenu;
  
  private JMenu viewMenu;
  
  private HelpMenu helpMenu;

  private SettingsMenu settingsMenu;
  
  private static MenuManager singleton;

  private MenuManager() {}

  public static MenuManager inst() {
    if (singleton == null) {
      singleton = new MenuManager();
    }
    return singleton; 
    }
  
  public static void reset() {
    singleton = null;
  }

  public static void createMenuManager(JFrame frame) {
    inst().initMenus(frame);
  }
  
  public void addViewMenuItem(JMenuItem menuItem) {
    if (this.viewMenu == null) {
      this.viewMenu = new JMenu("View");
      this.menuBar.add(this.viewMenu, 2);
      this.menuBar.getParent().validate();
    }
    this.viewMenu.add(menuItem);
  }

  private void initMenus(JFrame frame) {
    this.menuBar = new JMenuBar();
    fileMenu = new FileMenu();
    editMenu = new EditMenu();
    settingsMenu = new SettingsMenu();
    helpMenu = new HelpMenu();
    this.menuBar.add(fileMenu);
    this.menuBar.add(editMenu);
    this.menuBar.add(settingsMenu);
    this.menuBar.add(helpMenu);
    frame.setJMenuBar(this.menuBar);
  }


  
  // for testing actually
  public FileMenu getFileMenu() { return fileMenu; }
}

