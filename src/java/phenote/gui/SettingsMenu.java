package phenote.gui;


import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.config.ConfigFileQueryGui;
import phenote.main.Phenote;

import java.io.IOException;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class SettingsMenu extends JMenu {

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
  


  SettingsMenu() {
    super("Settings");
    init();
  }

  private void init() {

    // Build the search types sub-menu
//    menuSearchFilter = new JMenu("Search Filters");
//    menuSearchFilter.setMnemonic('F');

    //set defaults - these are actually the same as SearchParams defaults but it 
    // doesnt hurt to be sure
  	//Set these from the Config!
//    getSearchParams().setParam(SearchFilterType.TERM, true); //default Term = on
//    getSearchParams().setParam(SearchFilterType.SYN, true); //default Syn = on
//    getSearchParams().setParam(SearchFilterType.DEF, false); //default Def = off
//    getSearchParams().setParam(SearchFilterType.OBS, false); //default Obs = off
    getSearchParams().setParam(SearchFilterType.TERM, config.getAutocompleteSettings().getTerm()); 
    getSearchParams().setParam(SearchFilterType.SYN, config.getAutocompleteSettings().getSynonym());
    getSearchParams().setParam(SearchFilterType.DEF, config.getAutocompleteSettings().getDefinition()); 
    getSearchParams().setParam(SearchFilterType.OBS, config.getAutocompleteSettings().getObsolete());
    

    // Create property items based on status
    menuSearchFilterTerm = CreateMenuItem(this, ITEM_CHECK,
        SearchFilterType.TERM.getName(), null, 'T', "Look for partial matches within Term Names",
        getSearchParams().getParam(SearchFilterType.TERM));
    menuSearchFilterSynonym = CreateMenuItem(this, ITEM_CHECK,
            SearchFilterType.SYN.getName(), null, 'S', "Look for partial matches within Synonyms of Terms",
            getSearchParams().getParam(SearchFilterType.SYN));
    menuSearchFilterDefinition = CreateMenuItem(this, ITEM_CHECK,
            SearchFilterType.DEF.getName(), null, 'D', "Look for partial matches within the Definition of Terms",
            getSearchParams().getParam(SearchFilterType.DEF));

//    addSeparator();  //obs are in a class by themselves.
    menuSearchFilterObsolete = CreateMenuItem(this, ITEM_CHECK,
            SearchFilterType.OBS.getName(), null, 'O', "Look for partial matches within Obsolete Term Names",
            getSearchParams().getParam(SearchFilterType.OBS));

//	searchParams = new SearchParams();

//    JMenuItem searchSettings = new JMenuItem("Search");
////    searchSettings.setEnabled(Config.inst().hasDataAdapters());
//    searchSettings.setActionCommand("search");
//    searchSettings.addActionListener(actionListener);
//    add(menuSearchFilter);

    addSeparator();

    JMenuItem loadConfig = new JMenuItem("Set Configuration...");
    loadConfig.addActionListener(new ConfigActionListener());
    add(loadConfig);

    JMenuItem showToolbar = new JMenuItem("Show Toolbar");
    showToolbar.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    		//show the toolbar
    		Phenote.getPhenote().standardToolbar.showToolbar();
    		
    	}
    });
    add(showToolbar);
    
    // proto gui config
    JMenuItem browseConfig = new JMenuItem("Browse configuration(dev)");
    browseConfig.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) { new ConfigGui(); } } );
    add(browseConfig);

    //not sure if it should be in this menu
    addSeparator();
    showHistory = new JCheckBoxMenuItem("Show History");
    showHistory.setSelected(Config.inst().termHistoryIsEnabled());
    showHistory.addActionListener(new ShowHistoryActionListener());
    add(showHistory);
  }

  public JMenuItem CreateMenuItem(JMenu menu, int iType, String sText,
                                  ImageIcon image, int acceleratorKey,
                                  String sToolTip, boolean selected) {
    // Create the item
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

    // Add the item test
    menuItem.setText(sText);

    // Add the optional icon
    if (image != null)
      menuItem.setIcon(image);

    // Add the accelerator key
    if (acceleratorKey > 0)
      menuItem.setMnemonic(acceleratorKey);

    // Add the optional tool tip text
    if (sToolTip != null)
      menuItem.setToolTipText(sToolTip);

    // Add an action handler to this menu item
    menuItem.addActionListener(new SearchActionListener());

    menu.add(menuItem);

    return menuItem;
  }

  SearchParamsI getSearchParams() {
    return searchParams.inst();
  }

  private class SearchActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      String m = "";

      if (e.getActionCommand().equals(SearchFilterType.TERM.getName())) {
        getSearchParams().setParam(SearchFilterType.TERM, menuSearchFilterTerm.isSelected());
//  	      m = "TERM search is now *"+getSearchParams().searchTerms();
      } else if (e.getActionCommand().equals(SearchFilterType.SYN.getName())) {
        getSearchParams().setParam(SearchFilterType.SYN, menuSearchFilterSynonym.isSelected());
//    		m = "SYN search is now *"+getSearchParams().searchSynonyms();
      } else if (e.getActionCommand().equals(SearchFilterType.DEF.getName())) {
        getSearchParams().setParam(SearchFilterType.DEF, menuSearchFilterDefinition.isSelected());
//    		m = "DEF search is now *"+getSearchParams().searchDefinitions();
      } else if (e.getActionCommand().equals(SearchFilterType.OBS.getName())) {
        getSearchParams().setParam(SearchFilterType.OBS, menuSearchFilterObsolete.isSelected());
//    		m = "OBS search is now *"+getSearchParams().searchObsoletes();
      }
      if (!(getSearchParams().verifySettings())) {
        m = "You must select at least one of Term/Synonym/Definition.\nFilter is reset to -Term-.";
        JOptionPane.showMessageDialog(null, m, "Search Parameter Change",
                JOptionPane.ERROR_MESSAGE);
//    	  System.out.println("settings not right.  modifying...");

        menuSearchFilterTerm.setSelected(getSearchParams().getParam(SearchFilterType.TERM));
      }
//   	  JOptionPane.showMessageDialog(null,m,"Search Parameter Change",
//            JOptionPane.INFORMATION_MESSAGE);
//   	  System.out.println("changing search setting");
      Config.inst().setConfigModified(true);
      return;
    }
  }

  private class ConfigActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      // 1st step just set my-phenote.cfg & user restart phenote
      // eventually reconfigure phenote in same session, cfg,obo,gui
      boolean showCancel = true;
      try {
        String currentConfig = null;
        try { currentConfig = Config.inst().getMyPhenoteConfigString(); }
        catch (IOException x) {}

        String newCfg = ConfigFileQueryGui.queryUserForConfigFile(showCancel);
    
        if (newCfg == null || newCfg.equals(""))
          throw new ConfigException("ERROR: Got no config from user");

        if (currentConfig != null && newCfg.equals(currentConfig))
          throw new ConfigException("Config not changed"); // ?? or do nothing?


        Config.writeMyPhenoteDefaultFile(newCfg);
        
        restartMessage();
        //changeConfig(newCfg); // not working yet
      }
      catch (ConfigFileQueryGui.CancelEx ex) {} // its cancelled do nothing
      catch (ConfigException x) {
        String m = "Failed to change configuration " + x.getMessage();
        JOptionPane.showMessageDialog(null, m, "Config error",
                JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void restartMessage() {
    String m = "You must restart phenote for new config to take effect";
    JOptionPane.showMessageDialog(null, m, "Restart Phenote",
                JOptionPane.INFORMATION_MESSAGE);    
  }

  
  /** This doesnt work yet Wipe out data and load new configuration */
  private void changeConfig(String newCfg) {
    String m = "This will wipe out your current data - are you sure you want "
      +"to do this?";
    int ret = JOptionPane.showConfirmDialog(null, m, "Confirm data wipeout",
                      JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
    if (ret != JOptionPane.YES_OPTION) return;
    Phenote.getPhenote().changeConfig(newCfg);
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
