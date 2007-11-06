package phenote.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.bbop.framework.GUIManager;
import org.bbop.swing.AbstractDynamicMenuItem;
import org.bbop.swing.DynamicActionMenuItem;
import org.bbop.swing.DynamicMenu;

import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.config.ConfigFileQueryGui;
import phenote.dataadapter.LoadSaveManager;
import phenote.gui.actions.NewFileAction;
import phenote.gui.actions.OpenFileAction;
import phenote.gui.actions.SaveAsFileAction;

/**
 * This is the standard File menu for the main Phenote2 configuration.  It is
 * to include basic file and software operations:  New, Open, Save As, Exit.<p>
 * 
 * Future items will include (re)Save, Print, Properties?, Close, list of
 * recent annotation files (open recent).<p>
 * 
 * This menu has been adapted to work with the new bbop framework.<p>
 *
 * @author Mark Gibson
 * @author Nicole Washington
 *
 */
public class FileMenu extends DynamicMenu {

  public FileMenu() {
    super("File");
    init();
  }

  private void init() {
    
    JMenuItem newMenuItem = new DynamicActionMenuItem(new NewFileAction());
    add(newMenuItem);
    
    JMenuItem loadMenuItem = new DynamicActionMenuItem(new OpenFileAction());
    add(loadMenuItem);

    JMenuItem save = new DynamicActionMenuItem(new SaveAsFileAction());
    add(save);

//    JMenuItem export = new JMenuItem("Export...");
//    export.setEnabled(Config.inst().hasDataAdapters());
//    export.setActionCommand("export");
//    export.addActionListener(actionListener);
//    add(export);
    
    addSeparator();
    
    JMenuItem exit = new JMenuItem("Exit") {
    	@Override
    	public void setEnabled(boolean b) {
    		// TODO Auto-generated method stub
    		super.setEnabled(b);
    	}
    };
    exit.setEnabled(true);
    exit.setActionCommand("exit");
    exit.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    		GUIManager.exit(0); 
    	} });
    add(exit);

  }
  
  // for testing
  public void clickLoad() {
    //loadMenuItem.doClick();
  }
}
