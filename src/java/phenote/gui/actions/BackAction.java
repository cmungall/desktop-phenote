package phenote.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import phenote.main.Phenote;

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
		Phenote.getPhenote().getTermInfo().naviRefresh("back");
		System.out.println(e.getActionCommand().toString()+" action selected by:\n  "+ e);
	}
}  


