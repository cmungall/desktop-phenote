package phenote.gui;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import phenote.config.Config;


public class SynchOntologyDialog {

  // i think its ok to cache config as this object is not reused
  private Config config = Config.inst();

  private ButtonGroup buttonGroup;
//  private boolean hasCancelButton = false;
  private boolean isCancelled = false;
  private java.awt.Frame f = phenote.main.Phenote.getPhenote().getFrame();
  // true -> modal -> this is crucial! 
  private JDialog dialog = new JDialog(f, true);

  private boolean update = config.autoUpdateIsEnabled();  //set to the default, unless changed


  public static boolean queryUserForOntologyUpdate(String ontol) {
    try { return queryUserForSynchOntology(ontol); }
    catch (CancelEx e) {return false;} // shouldnt happen with cancel false
  }

  public static boolean queryUserForSynchOntology(String ontol) throws CancelEx {

    SynchOntologyDialog s = new SynchOntologyDialog();
    return s.queryUser(ontol);
  }

  private boolean queryUser(String ontol) throws CancelEx {
   	makeQueryDialog(ontol);
    if (isCancelled) throw new CancelEx();
    System.out.println("update="+update);
    return update; 

  }

  public static class CancelEx extends Exception {}

  private void makeQueryDialog(String ontol) {
  	//This makes the query dialog.  Will automatically close if there's a time limit set
  	//and will follow the rules as set up the "autoUpdate" in the config
  	//will add a smart counter in awhile
    dialog.setLayout(new GridBagLayout());
    dialog.setTitle("Update Ontology?");
    String html = "<html><p>There is a more current ontology in the repository for <b>"+ontol+"</b></p>" +
    		"<p>Would you like to load the new version? (may take a few minutes)</p></html>";
    JLabel text = new JLabel(html);
    int center = GridBagConstraints.CENTER;
    GridBagConstraints gbc = GridBagUtil.makeAnchorConstraint(0,0,center);
    gbc.gridwidth=3;
    dialog.add(text,gbc);
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));
    buttonGroup = new ButtonGroup();
    ++gbc.gridy;
    dialog.add(buttonPanel,gbc);
    JButton ok = new JButton("Yes");
    gbc.gridwidth=1;
    ++gbc.gridy;
    dialog.add(ok,gbc);
    ok.addActionListener(new OkActionListener());
    JButton cancel = new JButton("No");
    cancel.addActionListener(new CancelActionListener());
    ++gbc.gridx;
    dialog.add(cancel,gbc);
    centerOnScreen(dialog);
    dialog.addWindowListener(new WindowCancel());
    Timer t = new Timer();
    int limit = config.getUpdateTimer();    
    if (limit>0) {//only move on if there's a time limit
    	t.schedule(new SetLimit(), limit*1000);
     	if (config.autoUpdateIsEnabled()) {
    		html="<html><p>Files will automatically update ";
    	} else {
    		html="<html><p>Will bypass update ";
    	}
    	html+="in <b>"+limit+"</b> seconds.</p></html>";
      JLabel text3 = new JLabel(html);
      center = GridBagConstraints.CENTER;
      gbc = GridBagUtil.makeAnchorConstraint(0,0,center);
      gbc.gridwidth=2;
      ++gbc.gridy;
      dialog.add(text3,gbc);
    }
    dialog.pack();
    dialog.setVisible(true);
  }

  private class OkActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
    	update = true;
      dialog.dispose();
    }  
  }
  private class CancelActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      isCancelled = true;
      update = false;
      dialog.dispose();
    }
  }
  private class WindowCancel extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
      // should this do a System.exit if we are at startup?
      dialog.dispose();
    }
  }


  /** generic util? */
  private void centerOnScreen(Component c) {
    Toolkit t = Toolkit.getDefaultToolkit();
    Dimension screen = t.getScreenSize();
    int x = (int)screen.getWidth()/2 - c.getWidth()/2;
    int y = (int)screen.getHeight()/2 - c.getHeight()/2;
    Point p = new Point(x,y);
    c.setLocation(p);
  }

  private class SetLimit extends TimerTask {
  	public void run () {
  		dialog.dispose();
//  		System.out.println("timer done.  moving along...");
  		return ;
  	}
  }

  
}

