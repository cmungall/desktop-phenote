package phenote.gui;
// --> phenote.gui.menu ??

import java.io.FileNotFoundException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/** this is old regular phenote - not used in plus - phase out eventually */
public class MenuManager {
  
  private JMenuBar menuBar;

  private FileMenu fileMenu=null;
  
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
//     try {
// 			fileMenu = new FileMenu();
// 		} catch (FileNotFoundException e) {
// 			// TODO Auto-generated catch block
// 			e.printStackTrace();
// 		}
//    this.menuBar.add(fileMenu);

    editMenu = new EditMenu();
    menuBar.add(new phenote.gui.menu.FileMenu()); // phenote2 file menu
    settingsMenu = new SettingsMenu();
    helpMenu = new HelpMenu();
    this.menuBar.add(editMenu);
    this.menuBar.add(settingsMenu);
    this.menuBar.add(helpMenu);
    frame.setJMenuBar(this.menuBar);
  }


  
  // for testing actually
  public FileMenu getFileMenu() { return fileMenu; }
}

