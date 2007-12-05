package phenote.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import phenote.main.HelpManager;
import phenote.main.PhenoteVersion;

public class HelpMenu extends JMenu {

  private JMenuItem help;
  public SplashScreen splashScreen;
  public TermRequestGUI termRequester;
  private String logoFile = "src/java/phenote/images/phenote_logo.jpg";

    public HelpMenu() {
    super("Help");
    init();
  }

  private void init() {

    HelpActionListener actionListener = new HelpActionListener();


    help = new JMenuItem("Phenote Help");
    help.setEnabled(true);
    help.setActionCommand("help");
    HelpBroker hb = HelpManager.getHelpBroker();
    if (hb == null) {
      System.out.println("Unable to retrieve help broker");
    }
    else {
      help.addActionListener(new CSH.DisplayHelpFromSource(HelpManager.getHelpBroker()));
      //help.addActionListener(actionListener);
      add(help); 
    }

    JMenuItem about = new JMenuItem("About");
    about.setEnabled(true);
    about.setActionCommand("about");
    about.addActionListener(actionListener);
    add(about);

    JMenuItem request = new JMenuItem("Request Term");
    request.setEnabled(true);
    request.setActionCommand("request");
//    termRequester.addMouseListener(splashScreenListener);

    request.addActionListener(actionListener);
    add(request);
    
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
	    showAboutFrame();
//    	  splashScreenDestruct();
//      	  System.out.println("about selected");	
      	}
    	else if (e.getActionCommand().equals("request")) {
    		try {
		    String m="This feature is still in development.  Any information\n"+
			"you enter will be lost\n";
			
		    JOptionPane.showMessageDialog(null, m, "Phenote message", JOptionPane.PLAIN_MESSAGE);

    			termRequester = new TermRequestGUI();
    			termRequester.setVisible(true);
    		} catch (Exception ex) {
    			ex.printStackTrace();
    		}
    	}
    }
  }

    public void showAboutFrame() {
	splashScreenInit();  // For now
    }

  private void splashScreenInit() {
  	ImageIcon myImage = new ImageIcon(logoFile);
  	SplashScreenListener splashScreenListener = new SplashScreenListener();
  	splashScreen = new SplashScreen(myImage,true); // true->enable
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
