

package phenote.gui;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;



import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.DataAdapterI;
import phenote.dataadapter.LoadSaveManager;
import phenote.config.Config;
import phenote.config.ConfigFileQueryGui;
import phenote.config.ConfigException;
//import phenote.gui.prefswindow.PrefsWindowController; ???
import phenote.gui.SearchParams;
import phenote.gui.SearchParamsI;
import phenote.gui.SelectionHistory;

class SettingsMenu extends JMenu {

	private final int	ITEM_PLAIN	=	0;	// Item types
	private final int	ITEM_CHECK	=	1;
	private final int	ITEM_RADIO	=	2;
	private final String TERM = "Term Name";  //SearchFilterTypes
	private final String SYN = "Synonyms";
	private final String DEF = "Definitions";
	private final String OBS = "Obsoletes";
	private JMenu menuSearchFilter;
    private JMenuItem menuSearchFilterTerm;
    private JMenuItem menuSearchFilterSynonym;
    private JMenuItem menuSearchFilterDefinition;
    private JMenuItem menuSearchFilterObsolete;
    private JMenuItem showHistory;
    public SearchParams searchParams;
    private SelectionHistory selHist = SelectionHistory.inst();


  SettingsMenu() {
    super("Settings");
    init();
  }

  private void init() {

	// Build the search types sub-menu
	menuSearchFilter = new JMenu( "Search Filters" );
	menuSearchFilter.setMnemonic( 'F' );

	//set defaults
	getSearchParams().setParam(TERM, true); //default Term = on
	getSearchParams().setParam(SYN, true); //default Syn = on

	// Create property items based on status
	menuSearchFilterTerm = CreateMenuItem( menuSearchFilter, ITEM_CHECK,
							TERM, null, 'T', "Look for partial matches within Term Names", 
							getSearchParams().getParam(TERM));
	menuSearchFilterSynonym = CreateMenuItem( menuSearchFilter, ITEM_CHECK,
							SYN, null, 'S', "Look for partial matches within Synonyms of Terms", 
							getSearchParams().getParam(SYN) );
	menuSearchFilterDefinition = CreateMenuItem( menuSearchFilter, ITEM_CHECK,
							DEF, null, 'D', "Look for partial matches within the Definition of Terms", 
							getSearchParams().getParam(DEF) );

	addSeparator();  //obs are in a class by themselves.
	menuSearchFilterObsolete = CreateMenuItem( menuSearchFilter, ITEM_CHECK,
							OBS, null, 'O', "Look for partial matches within Obsolete Term Names",
							getSearchParams().getParam(OBS) );

//	searchParams = new SearchParams();
    
//    JMenuItem searchSettings = new JMenuItem("Search");
////    searchSettings.setEnabled(Config.inst().hasDataAdapters());
//    searchSettings.setActionCommand("search");
//    searchSettings.addActionListener(actionListener);
    add(menuSearchFilter);
	
	addSeparator();
    
    JMenuItem loadConfig = new JMenuItem("Configuration...");
    loadConfig.addActionListener(new ConfigActionListener());
    add(loadConfig);
    
    //not sure if it should be in this menu
	addSeparator();
	showHistory = new JCheckBoxMenuItem("Show History");
	showHistory.setSelected(Config.inst().termHistoryIsEnabled());
	showHistory.addActionListener(new ShowHistoryActionListener());
	add(showHistory);
  }

public JMenuItem CreateMenuItem( JMenu menu, int iType, String sText,
			ImageIcon image, int acceleratorKey,
			String sToolTip, boolean selected)  {
  // Create the item
  JMenuItem menuItem;

  switch( iType )
    {
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

  // Add the item test
  menuItem.setText( sText );

  // Add the optional icon
  if( image != null )
    menuItem.setIcon( image );

  // Add the accelerator key
  if( acceleratorKey > 0 )
    menuItem.setMnemonic( acceleratorKey );

  // Add the optional tool tip text
  if( sToolTip != null )
    menuItem.setToolTipText( sToolTip );
  
  // Add an action handler to this menu item
  menuItem.addActionListener(new SearchActionListener() );

  menu.add( menuItem );

  return menuItem;
}

SearchParamsI getSearchParams() {
    return searchParams.inst();
  }

  private class SearchActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      String m = "";
    	
      if (e.getActionCommand().equals(TERM)) {
    	getSearchParams().setParam(TERM, menuSearchFilterTerm.isSelected());
//  	      m = "TERM search is now *"+getSearchParams().searchTerms();
      } else if (e.getActionCommand().equals(SYN)) {
      	  getSearchParams().setParam(SYN, menuSearchFilterSynonym.isSelected());
//    		m = "SYN search is now *"+getSearchParams().searchSynonyms();
      } else if (e.getActionCommand().equals(DEF)) {
    	  getSearchParams().setParam(DEF, menuSearchFilterDefinition.isSelected());
//    		m = "DEF search is now *"+getSearchParams().searchDefinitions();
      } else if (e.getActionCommand().equals(OBS)) {
    	  getSearchParams().setParam(OBS, menuSearchFilterObsolete.isSelected());
//    		m = "OBS search is now *"+getSearchParams().searchObsoletes();
      }
      if (!(getSearchParams().verifySettings())) {
    	m = "You must select at least one of Term/Synonym/Definition.\nFilter is reset to -Term-.";
        JOptionPane.showMessageDialog(null,m,"Search Parameter Change",
        		JOptionPane.ERROR_MESSAGE);    	  
//    	  System.out.println("settings not right.  modifying...");
       	  
     	menuSearchFilterTerm.setSelected(getSearchParams().getParam(TERM));
      }
//   	  JOptionPane.showMessageDialog(null,m,"Search Parameter Change",
//            JOptionPane.INFORMATION_MESSAGE);
//   	  System.out.println("changing search setting");
      return;
    }
  }

  private class ConfigActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      // 1st step just set my-phenote.cfg & user restart phenote
      // eventually reconfigure phenote in same session, cfg,obo,gui
      boolean showCancel = true;
      try {
        String cfg = ConfigFileQueryGui.queryUserForConfigFile(showCancel);
        if (cfg != null && !cfg.equals(""))
          Config.writeMyPhenoteDefaultFile(cfg);
        String m = "You must restart phenote for new config to take effect";
        JOptionPane.showMessageDialog(null,m,"Please restart",
                                      JOptionPane.INFORMATION_MESSAGE);
      }
      catch (ConfigFileQueryGui.CancelEx ex) {} // its cancelled do nothing
      catch (ConfigException x) {
        String m = "Failed to change configuration "+x.getMessage();
        JOptionPane.showMessageDialog(null,m,"Config error",
                                      JOptionPane.ERROR_MESSAGE);
      }
    }
  }
  
  private class ShowHistoryActionListener implements ActionListener {
	    public void actionPerformed(ActionEvent e) {
	      // 1st step just set my-phenote.cfg & user restart phenote
	      // eventually reconfigure phenote in same session, cfg,obo,gui
	    	boolean status = showHistory.isSelected();
//	    	System.out.println("showHistory ="+status);
	    	Config.inst().setTermHistory(status);
	    	selHist.showSwitch();
	    	
	    }
  }

  // for testing
//  public void clickLoad() {
//    loadMenuItem.doClick();
//  }
}
