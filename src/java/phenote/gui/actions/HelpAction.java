package phenote.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import phenote.main.HelpManager;

/**
 * This method should be extended to include smart positioning/opening of
 * help to relevant items.  It currently only appears in the basic menu, 
 * but future "smarts" should include opening of the help to a specific place
 * in the documentation.<p>
 * 
 * @author Nicole Washington
 *
 */
public class HelpAction extends AbstractAction {
	
	private HelpBroker hb = HelpManager.getHelpBroker();
	
	public HelpAction() {
		super("Phenote Help", new ImageIcon("images/Help24.gif"));
		putValue(SHORT_DESCRIPTION, "Browse Help Documentation"); // tooltip text
		putValue(NAME, "Phenote Help");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_H));
	}
	
	@Override
	public boolean isEnabled() {
		return (hb!=null);
	}

	public void actionPerformed(final ActionEvent e) {
		//help should really be another panel in the whole kit and caboodle
		new CSH.DisplayHelpFromSource(HelpManager.getHelpBroker());
	}

}
