package phenote.gui.actions;

//import javax.swing.JFrame;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
//import javax.swing.JMenuItem;
import javax.swing.ImageIcon;
import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.*;
import java.awt.event.*;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import javax.swing.text.DefaultEditorKit;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import javax.swing.JComponent;

import org.geneontology.oboedit.datamodel.OBOClass;


import phenote.dataadapter.LoadSaveManager;
import phenote.config.Config;
import phenote.edit.EditManager;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.TermNotFoundException;
import phenote.gui.TermInfo;
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
		Phenote.getPhenote().getTermInfo().naviRefresh("forward");
		System.out.println(e.getActionCommand().toString()+" action selected by:\n  "+ e);
	}
}  


