package phenote.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
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
import phenote.gui.ConfigGui;
import phenote.gui.SearchFilterType;
import phenote.gui.SearchParams;
import phenote.gui.SearchParamsI;
import phenote.gui.SelectionHistory;
import phenote.gui.actions.NCBIQueryAction;
import phenote.main.Phenote;

import java.io.IOException;

import javax.swing.ImageIcon;

import javax.swing.JRadioButtonMenuItem;


/**
 * This is the standard Settings menu for the main Phenote2 configuration.  It is
 * to include basic accessory operations such as setting the term-completion
 * searching capabilities (name, syn, def, obsolete), configuration setting, 
 * etc.<p>
 * 
 * 
 * This menu has been adapted to work with the new bbop framework.<p>
 *
 * @author Mark Gibson
 * @author Nicole Washington
 *
 */
public class SettingsMenu extends DynamicMenu {

  private final int ITEM_PLAIN = 0;  // Item types
  private final int ITEM_CHECK = 1;
  private final int ITEM_RADIO = 2;
//  private JMenu menuSearchFilter;
  private JMenuItem menuSearchFilterTerm;
  private JMenuItem menuSearchFilterSynonym;
  private JMenuItem menuSearchFilterDefinition;
  private JMenuItem menuSearchFilterObsolete;
  private JMenuItem showHistory;
  public SearchParams searchParams;
  private SelectionHistory selHist = SelectionHistory.inst();
  private Config config = Config.inst();
  
  public SettingsMenu() {
    super("Settings");
    init();
  }

  private void init() {
    
//    getSearchParams().setParam(SearchFilterType.TERM, config.getAutocompleteSettings().getTerm()); 
//    getSearchParams().setParam(SearchFilterType.SYN, config.getAutocompleteSettings().getSynonym());
//    getSearchParams().setParam(SearchFilterType.DEF, config.getAutocompleteSettings().getDefinition()); 
//    getSearchParams().setParam(SearchFilterType.OBS, config.getAutocompleteSettings().getObsolete());
//    
//
//    // Create property items based on status
//    menuSearchFilterTerm = CreateMenuItem(this, ITEM_CHECK,
//        SearchFilterType.TERM.getName(), null, 'T', "Look for partial matches within Term Names",
//        getSearchParams().getParam(SearchFilterType.TERM));
//    menuSearchFilterSynonym = CreateMenuItem(this, ITEM_CHECK,
//            SearchFilterType.SYN.getName(), null, 'S', "Look for partial matches within Synonyms of Terms",
//            getSearchParams().getParam(SearchFilterType.SYN));
//    menuSearchFilterDefinition = CreateMenuItem(this, ITEM_CHECK,
//            SearchFilterType.DEF.getName(), null, 'D', "Look for partial matches within the Definition of Terms",
//            getSearchParams().getParam(SearchFilterType.DEF));
//
////    addSeparator();  //obs are in a class by themselves.
//    menuSearchFilterObsolete = CreateMenuItem(this, ITEM_CHECK,
//            SearchFilterType.OBS.getName(), null, 'O', "Look for partial matches within Obsolete Term Names",
//            getSearchParams().getParam(SearchFilterType.OBS));
//
//    addSeparator();

//    JMenuItem loadConfig = new JMenuItem("Set Configuration...");
//    loadConfig.addActionListener(new ConfigActionListener());
//    add(loadConfig);
//
//    JMenuItem showToolbar = new JMenuItem("Show Toolbar");
//    showToolbar.addActionListener(new ActionListener() {
//    	public void actionPerformed(ActionEvent e) {
//    		//show the toolbar
//    		Phenote.getPhenote().standardToolbar.showToolbar();
//    		
//    	}
//    });
//    add(showToolbar);
//    
//    // proto gui config
//    JMenuItem browseConfig = new JMenuItem("Browse configuration(dev)");
//    browseConfig.addActionListener(new ActionListener() {
//        public void actionPerformed(ActionEvent e) { new ConfigGui(); } } );
//    add(browseConfig);
//
//    //not sure if it should be in this menu
//    addSeparator();
//    showHistory = new JCheckBoxMenuItem("Show History");
//    showHistory.setSelected(Config.inst().termHistoryIsEnabled());
//    showHistory.addActionListener(new ShowHistoryActionListener());
//    add(showHistory);
  	

    JMenuItem search = new JMenuItem("Search");
    search.setEnabled(false);
    add(search);
    

  }

  public JMenuItem CreateMenuItem(JMenu menu, int iType, String sText,
  		ImageIcon image, int acceleratorKey,
  		String sToolTip, boolean selected) {
//	Create the item
  	JMenuItem menuItem;

  	switch (iType) {
  	case ITEM_RADIO:
  		menuItem = new JRadioButtonMenuItem();
  		break;

  	case ITEM_CHECK:
  		menuItem = new JCheckBoxMenuItem();
  		menuItem.setSelected(selected);
  		menuItem.setActionCommand(sText);
  		break;

  	default:
  		menuItem = new JMenuItem();
  	break;
  	}

//	Add the item test
  	menuItem.setText(sText);

//	Add the optional icon
  	if (image != null)
  		menuItem.setIcon(image);

//	Add the accelerator key
  	if (acceleratorKey > 0)
  		menuItem.setMnemonic(acceleratorKey);

//	Add the optional tool tip text
  	if (sToolTip != null)
  		menuItem.setToolTipText(sToolTip);

//	Add an action handler to this menu item
// doesnt compile - taking out for now
//  	menuItem.addActionListener(new SearchActionListener());

  	menu.add(menuItem);

  	return menuItem;
  }

  SearchParamsI getSearchParams() {
  	return searchParams.inst();
  }
  
  // for testing
  public void clickLoad() {
    //loadMenuItem.doClick();
  }
  
  
}
