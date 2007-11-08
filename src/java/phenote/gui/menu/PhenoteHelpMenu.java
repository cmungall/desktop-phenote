package phenote.gui.menu;


import javax.swing.JMenuItem;

import phenote.gui.actions.TermRequestAction;
import phenote.gui.actions.HelpAction;
import phenote.gui.actions.AboutAction;

//import org.bbop.framework.HelpMenu;
//import org.bbop.framework.GUIManager;
//import org.bbop.swing.AbstractDynamicMenuItem;
import org.bbop.swing.DynamicActionMenuItem;
import org.bbop.swing.DynamicMenu;



/**
 * The basic Help menu for the main set of menus.  This is to include functions
 * of About, Help, etc.  Currently, the "Term Request" gui is in here, but
 * probably ought to be in a different menu.<p>
 * This should utilize the basic Help infrastructure that is included in the 
 * bbop framework...but it doesn't yet.<p>
 * Additional features for this menu in the future might include the ability
 * to query the repository for updates to the software.<p>
 * 
 * @author Nicole Washington
 *
 */
public class PhenoteHelpMenu extends DynamicMenu {

	private JMenuItem help;
	
  public PhenoteHelpMenu() {
    super("Help");
    init();
  }

  private void init() {

    help = new DynamicActionMenuItem(new HelpAction());
    add(help); 

    JMenuItem about = new DynamicActionMenuItem(new AboutAction());
    add(about);

    JMenuItem request = new DynamicActionMenuItem(new TermRequestAction());
    add(request);
    
  }

//   for testing
  public void clickLoad() {
    help.doClick();
  }
}
