package phenote.gui;
// --> phenote.gui.menu ??

import javax.swing.JFrame;
import javax.swing.JMenuBar;

public class MenuManager {

  private FileMenu fileMenu;

  private static MenuManager singleton = new MenuManager();

  private MenuManager() {}

  public static MenuManager inst() { return singleton; }

  public static void createMenuManager(JFrame frame) {
    singleton.initMenus(frame);
  }

  private void initMenus(JFrame frame) {
    JMenuBar menuBar = new JMenuBar();
    fileMenu = new FileMenu();
    menuBar.add(fileMenu);
    frame.setJMenuBar(menuBar);
  }

  // for testing actually
  public FileMenu getFileMenu() { return fileMenu; }
}
