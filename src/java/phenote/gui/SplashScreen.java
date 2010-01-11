package phenote.gui;

/*I grabbed the basics from
http://www.devdaily.com/java/edu/SplashScreen/SplashScreen.java
*/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import phenote.error.ErrorEvent;
import phenote.error.ErrorListener;
import phenote.error.ErrorManager;


public class SplashScreen extends JWindow {
  BorderLayout borderLayout1 = new BorderLayout();
  JLabel imageLabel = new JLabel();
  JPanel southPanel = new JPanel();
  FlowLayout southPanelFlowLayout = new FlowLayout();
  JProgressBar progressBar = new JProgressBar();
//  JTextArea messagePanel = new JTextArea();
  ImageIcon imageIcon;
  private boolean enable = true;
	private final static int DEFAULT_WIDTH = 800;
	private final static int DEFAULT_HEIGHT = 400;
	private static int windowWidth;
	private static int windowHeight;
  
  public SplashScreen(ImageIcon imageIcon) {
    this(imageIcon,true);
  }
  
  // probably should take enable out
  public SplashScreen(ImageIcon imageIcon, boolean enable) {
	  this(imageIcon,enable,DEFAULT_WIDTH,DEFAULT_HEIGHT);
  }


  public SplashScreen(ImageIcon imageIcon, boolean enable, int width, int height) {
    this.enable = enable;
    if (!enable) return;
    this.imageIcon = imageIcon;
    windowWidth = width;
    windowHeight = height;
    this.setPreferredSize(new Dimension(width,height));
    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

 public static Dimension getPreferredDimension() {
    return new Dimension(windowWidth,windowHeight);
 }

  // note - this class created with JBuilder
  void jbInit() throws Exception {
    imageLabel.setIcon(imageIcon);
    this.getContentPane().setLayout(borderLayout1);
    southPanel.setLayout(southPanelFlowLayout);
    southPanel.setBackground(Color.BLUE);
//    messagePanel.setBackground(Color.BLACK);
    this.getContentPane().add(imageLabel, BorderLayout.CENTER);  // I would like the image to actually be centered, and it's not--it's all the way to the left.
    this.getContentPane().add(southPanel, BorderLayout.SOUTH);
//    this.getContentPane().add(messagePanel, BorderLayout.NORTH);
    southPanel.add(progressBar, null);
    this.pack();
    this.getContentPane().repaint(); // ?
    setAlwaysOnTop(false); // doesnt work on linux??? stays on top! annoying!
    ErrorManager.inst().addErrorListener(new SplashErrorListener());
  }

  public void setProgressMax(int maxProgress)
  {
    if (!enable) return;
    progressBar.setMaximum(maxProgress);
  }

  public void setProgress(int progress)
  {
    if (!enable) return;
    final int theProgress = progress;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        progressBar.setValue(theProgress);
      }
    });
  }

  public void setProgress(String message, int progress)
  {
    if (!enable) return;
    final int theProgress = progress;
    final String theMessage = message;
    setProgress(progress);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        progressBar.setValue(theProgress);
        setMessage(theMessage);
      }
    });
  }

  public void setScreenVisible(boolean b)
  {
    final boolean boo = b;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        setVisible(boo);
      }
    });
  }

  public void setMessage(String message) { // was private ???
    if (message==null)
    {
      message = "";
      progressBar.setStringPainted(false);
    }
    else
    {
      progressBar.setStringPainted(true);
    }
    progressBar.setString(message);
//    messagePanel.setText(message);
  }


  /** This is actually useless as message flits by too quickly and error messages
      get lost */
  private class SplashErrorListener implements ErrorListener {
    public void handleError(ErrorEvent e) {
      setProgress(e.getMsg(),0); // 0?? 100??
    }
  }

}
