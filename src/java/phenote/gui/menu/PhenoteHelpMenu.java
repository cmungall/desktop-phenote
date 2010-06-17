package phenote.gui.menu;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;

import org.bbop.framework.GUIManager;
import org.bbop.framework.HelpManager;
import org.bbop.swing.BackgroundImagePanel;
import org.bbop.swing.DynamicMenu;
import org.bbop.swing.SwingUtil;

import org.apache.log4j.*;

import phenote.config.Preferences;
import phenote.gui.SplashScreen;
import phenote.main.Phenote;
import phenote.main.PhenoteVersion;
import phenote.util.FileUtil;

//public class PhenoteHelpMenu extends HelpMenu {
public class PhenoteHelpMenu extends DynamicMenu {

	public static SplashScreen splashScreen;

	private static String logoFile = "images/phenote_logo.png";

	//initialize logger
	protected final static Logger logger = Logger.getLogger(PhenoteHelpMenu.class);

	public PhenoteHelpMenu() {
		super("Help");
		JMenuItem helpItem = new JMenuItem("User Guide");
		helpItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					HelpManager.getManager().displayHelp();
				}
			});
		add(helpItem);

                logger.debug("PhenoteHelpMenu: setting helpfile to " + Preferences.getInstallationDirectory() + 
                             "/doc/phenote-website/help/Phenote.hs");
		HelpManager.getManager().setHelpSetFile(
			new File(Preferences.getInstallationDirectory() + 
				 "/doc/phenote-website/help/Phenote.hs"));

		JMenuItem aboutItem = new JMenuItem("About");
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showAboutFrame();
//				AboutAction.splashScreenInit();
			}
		});

		add(aboutItem);
	}

	public static void showAboutFrame() {
//		ImageIcon myImage = new ImageIcon(logoFile);
//		URL imageURL = ClassLoader.getSystemResource(logoFile);
		ImageIcon myImage = null;
		try {
			URL imageURL = FileUtil.findUrl(logoFile);
//			logger.debug("imageURL = " + imageURL); // DEL
			myImage = new ImageIcon(imageURL);
		} catch (Exception e) {
			logger.error("Error trying to find logo file " + logoFile + ": " + e.getMessage());
		}
		splashScreen = new SplashScreen(myImage,true,215,90); // true->enable
		splashScreen.setLocationRelativeTo(null);
//	splashScreen.setProgressMax(100);
		splashScreen.setScreenVisible(true);
		SplashScreenListener splashScreenListener = new SplashScreenListener();
		splashScreen.addMouseListener(splashScreenListener);
		splashScreen.setProgress(" Phenote version "+PhenoteVersion.versionString() + " ", 0);
	}

	private static class SplashScreenListener implements MouseListener {
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
