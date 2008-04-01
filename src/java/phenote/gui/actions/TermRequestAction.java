package phenote.gui.actions;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import phenote.gui.TermRequestGUI;


/**
 * This is the action to bring up the dialog for users to request new
 * ontology terms.<p>
 * This feature is currently in a pre-alpha phase.
 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 */
public class TermRequestAction extends AbstractAction {
	public TermRequestGUI termRequester;
	
	public TermRequestAction() {
		super("Request Term", new ImageIcon("images/New24.gif"));
		putValue(SHORT_DESCRIPTION, "Submit a request for a new Ontology Term"); // tooltip text
		putValue(NAME, "Request Term");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_T));
	}
	
	@Override
	public boolean isEnabled() {
		return true;
//		return Config.inst().hasDataAdapters();
	}


	public void actionPerformed(ActionEvent e) {
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
