package phenote.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;

import phenote.datamodel.CharFieldManager;
import phenote.datamodel.TermNotFoundException;
import phenote.gui.TermInfo2;
import phenote.gui.selection.SelectionManager;
import phenote.gui.selection.UseTermListener;

/**
 * @author Nicole Washington
 *
 * When using the Term Info browser, this action will move the user back in
 * the navigation history.
 */
public class BackAction extends AbstractAction  {
	
  @SuppressWarnings("unused")
  private static final Logger LOG =  Logger.getLogger(BackAction.class);

	JComponent textComponent;
	
// 	public BackAction(JTextComponent source) {
//     super("Back");
//     try { setImageIcon(new ImageIcon(FileUtil.findUrl("images/Back24.gif"))); }
//     catch (FileNotFoundException e) { LOG.error(e.getMessage()); }

// 		init();
// 		source.setEnabled(false);

// //		System.out.println("class="+textComponent.getClass());
// 	}
	
	/** Actions only take image icons in their constructor - annoying */
	public BackAction(ImageIcon icon) {
		super("Back", icon);
		init();
	}

	private void init() {
		putValue(SHORT_DESCRIPTION, "Go back a term"); //tooltip text
		putValue(NAME, "Back");
//		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
	}
		
	public void actionPerformed(ActionEvent e) {
//		TermInfo2.inst().naviRefresh("back");
		int naviIndex = TermInfo2.inst().getNaviIndex();
//		System.out.println("naviIndex before="+naviIndex);
		if (naviIndex > 0) { //only move the navi if not at beginning
			naviIndex--;
			TermInfo2.inst().setNaviIndex(naviIndex);
		}
//		System.out.println("naviIndex after="+naviIndex);
		String id = TermInfo2.getTermFromNaviHistory(naviIndex);
//		System.out.println(id);
		
		try {
			OBOClass term = CharFieldManager.inst().getOboClass(id); // ex
			UseTermListener utl = TermInfo2.inst().getUseTermListener();
			SelectionManager.inst().selectMouseOverTerm(this, term, utl);
//			System.out.println("found back term: "+term);
		} catch (TermNotFoundException ex) {
			return;
		}
		//System.out.println(e.getActionCommand().toString()+" action selected by:\n  "+ e);
	}
}  


