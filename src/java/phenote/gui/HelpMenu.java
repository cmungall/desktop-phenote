package phenote.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;

import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.DataAdapterI;
import phenote.dataadapter.LoadSaveManager;
import phenote.config.Config;
import phenote.config.ConfigFileQueryGui;
import phenote.config.ConfigException;
//import phenote.gui.prefswindow.PrefsWindowController; ???
import phenote.gui.SplashScreen;
import phenote.main.PhenoteVersion;

class HelpMenu extends JMenu {

  private JMenuItem help;
  public SplashScreen splashScreen;
  private String logoFile = "src/java/phenote/images/phenote_logo.jpg";

  HelpMenu() {
    super("Help");
    init();
  }

  private void init() {

    HelpActionListener actionListener = new HelpActionListener();


    help = new JMenuItem("Phenote Help");
    help.setEnabled(Config.inst().hasDataAdapters());
    help.setActionCommand("help");
    help.addActionListener(actionListener);
    add(help); 

    JMenuItem about = new JMenuItem("About");
    about.setEnabled(Config.inst().hasDataAdapters());
    about.setActionCommand("about");
    about.addActionListener(actionListener);
    add(about);
  }

  private class HelpActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
    	boolean showCancel = false;
    	if (e.getActionCommand().equals("help")) {
    		String m = "<html><p>This feature is not yet integrated.</p>" +
    				"<p> Please visit www.phenote.org/help</p></html>";
    		JOptionPane.showMessageDialog(null, m, "Phenote Help",
    			JOptionPane.INFORMATION_MESSAGE);
    		System.out.println("help selected");	
    	}
    	else if (e.getActionCommand().equals("about")) {
    		//throw up splash screen here.  make sure to make different pic.
//    	  String m = "This is Phenote v (version here)....[more to come]";
//    	  JOptionPane.showMessageDialog(null, m, "About", 
//    			  JOptionPane.INFORMATION_MESSAGE);
    	  splashScreenInit();
//    	  splashScreenDestruct();
//      	  System.out.println("about selected");	
      	}
      }
  }

  private void splashScreenInit() {
	ImageIcon myImage = new ImageIcon(logoFile);
	SplashScreenListener splashScreenListener = new SplashScreenListener();
	splashScreen = new SplashScreen(myImage);
	splashScreen.setLocationRelativeTo(null);
//	splashScreen.setProgressMax(100);
	splashScreen.setScreenVisible(true);
	splashScreen.addMouseListener(splashScreenListener);
	splashScreen.setProgress("Phenote version "+PhenoteVersion.versionString(), 0);
	
  }

  private class SplashScreenListener implements MouseListener {
		public void mouseClicked(MouseEvent e) {
		  //the splash screen will go away when clicked w/ mouse
		  splashScreen.setScreenVisible(false);
		  splashScreen = null;
		  return;
		}	
		public void mouseExited(MouseEvent e) {;}
	    public void mousePressed(MouseEvent e) {;}
	    public void mouseReleased(MouseEvent e) {;}
	    public void mouseEntered(MouseEvent e) {;}
	  }	

  
  // for testing
  public void clickLoad() {
    help.doClick();
  }
}
