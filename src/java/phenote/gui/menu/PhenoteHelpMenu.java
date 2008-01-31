package phenote.gui.menu;


import javax.swing.JMenuItem;

import org.bbop.swing.DynamicMenu;

import phenote.gui.actions.AboutAction;
import phenote.gui.actions.HelpAction;
import phenote.gui.actions.TermRequestAction;
import phenote.main.Phenote;



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

    help = new JMenuItem(new HelpAction());
    add(help); 

    if (!Phenote.isRunningOnMac()) {
      // we don't want to add About to the Help menu on Mac
      // instead there is About under the automatic Phenote menu
      JMenuItem about = new JMenuItem(new AboutAction());
      add(about);
    }
  
    JMenuItem request = new JMenuItem(new TermRequestAction());
    add(request);
    
  }

//   for testing
  public void clickLoad() {
    help.doClick();
  }
}
