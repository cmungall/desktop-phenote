package phenote.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import phenote.gui.SplashScreen;
import phenote.main.PhenoteVersion;


//This is the action for bringing up a splash screen to show the phenote
//version info...possibly other stuff in the future.

public class AboutAction extends AbstractAction {

	public SplashScreen splashScreen;
  private String logoFile = "src/java/phenote/images/phenote_logo.jpg";


	public AboutAction() {
		super("About Phenote");
		putValue(SHORT_DESCRIPTION, "About Phenote"); //tooltip text
		putValue(NAME, "About Phenote");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
		setEnabled(true);
	}
	public void actionPerformed(ActionEvent e) {
		//log this action
		splashScreenInit();
		
//		System.out.println(e.getActionCommand().toString()+" action selected by:\n  "+ e);
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
}  


