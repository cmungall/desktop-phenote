package phenote.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import org.obo.datamodel.OBOClass;

import phenote.main.Phenote;
import phenote.main.Phenote2;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.TermNotFoundException;
import phenote.gui.TermInfo2;
import phenote.gui.selection.SelectionManager;

/**
 * @author Nicole Washington
 *
 * When using the Term Info browser, this action will move the user back in
 * the navigation history.
 */
public class BackAction extends AbstractAction  {
	
	JComponent textComponent;
	
	public BackAction(JTextComponent source) {
		super("Back", new ImageIcon("images/arrow.small.left.gif"));
		init();

//		System.out.println("class="+textComponent.getClass());
	}
	
	
	public BackAction() {
		super("Back", new ImageIcon("images/arrow.small.left.gif"));
		init();
	}

	private void init() {
		putValue(SHORT_DESCRIPTION, "Go back a term"); //tooltip text
		putValue(NAME, "Back");
//		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
	}
	
	public void actionPerformed(ActionEvent e) {
//		TermInfo2.inst().naviRefresh("back");
		int tot = TermInfo2.inst().getTermInfoNaviHistory().size();
		int naviIndex = TermInfo2.inst().getNaviIndex();
		System.out.println("naviIndex before="+naviIndex);
		if (naviIndex > 0) { //only move the navi if not at beginning
			naviIndex--;
			TermInfo2.inst().setNaviIndex(naviIndex);
		}
		System.out.println("naviIndex after="+naviIndex);
		String id = TermInfo2.inst().getTermFromNaviHistory(naviIndex);
		System.out.println(id);
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


