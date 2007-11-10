package phenote.gui.actions;

//import javax.swing.JFrame;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
//import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.text.JTextComponent;

import org.obo.datamodel.OBOClass;

import phenote.datamodel.CharFieldManager;
import phenote.datamodel.TermNotFoundException;
import phenote.gui.TermInfo2;
import phenote.gui.selection.SelectionManager;
import phenote.main.Phenote;


/**
 * @author Nicole Washington
 *
 * When using the Term Info browser, this action will move the user forward in
 * the navigation history.
 */
public class ForwardAction extends AbstractAction  {
		
	public ForwardAction(JTextComponent source) {
		super("Forward", new ImageIcon("images/arrow.small.right.gif"));
		init();

//		System.out.println("class="+textComponent.getClass());
	}
	
	
	public ForwardAction() {
		super("Forward", new ImageIcon("images/arrow.small.right.gif"));
		init();
	}

	private void init() {
		putValue(SHORT_DESCRIPTION, "Go forward"); //tooltip text
		putValue(NAME, "Forward");
//		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
	}
	
	public void actionPerformed(ActionEvent e) {
//		TermInfo2.inst().naviRefresh("forward");
		int tot = TermInfo2.inst().getTermInfoNaviHistory().size();
		int naviIndex = TermInfo2.inst().getNaviIndex();
		System.out.println("naviIndex before="+naviIndex);
		if (naviIndex < (tot - 1)) { //only move the navi if not at end
			naviIndex++;
			TermInfo2.inst().setNaviIndex(naviIndex);
		}
		System.out.println("naviIndex after="+naviIndex);
		String id = TermInfo2.inst().getTermFromNaviHistory(naviIndex);
		try {
			OBOClass term = CharFieldManager.inst().getOboClass(id); // ex
			SelectionManager.inst().selectMouseOverTerm(this, term, null);
			System.out.println("found back term: "+term);
		} catch (TermNotFoundException ex) {
			return;
		}
		System.out.println(e.getActionCommand().toString()+" action selected by:\n  "+ e);
	}
}  


