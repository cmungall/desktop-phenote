package phenote.gui.actions;

//import javax.swing.JFrame;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
//import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.text.JTextComponent;

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
		Phenote.getPhenote().getTermInfo().naviRefresh("forward");
		System.out.println(e.getActionCommand().toString()+" action selected by:\n  "+ e);
	}
}  


