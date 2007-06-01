package phenote.config;

import java.io.File;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.lang.Enum;


import org.apache.xmlbeans.XmlException;

import phenote.main.PhenoteVersion;
import phenote.config.xml.PhenoteConfigurationDocument;
import phenote.config.xml.DataadapterDocument.Dataadapter;
//import phenote.config.xml.QueryableDataadapterDocument.QueryableDataadapter;
import phenote.config.xml.FieldDocument.Field;
import phenote.config.xml.LogDocument.Log;
import phenote.config.xml.OboRepositoryDocument.OboRepository;
import phenote.config.xml.PhenoteConfigurationDocument.PhenoteConfiguration;
import phenote.config.xml.UvicGraphDocument.UvicGraph;
import phenote.config.xml.TermHistoryDocument.TermHistory;
import phenote.config.xml.AutoUpdateOntologiesDocument.AutoUpdateOntologies;
import phenote.config.xml.UpdateTimerDocument.UpdateTimer;
import phenote.config.xml.MasterToLocalConfigDocument.MasterToLocalConfig;
import phenote.config.xml.AutocompleteSettingsDocument.AutocompleteSettings;

import phenote.dataadapter.DataAdapterI;
import phenote.dataadapter.QueryableDataAdapterI;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldEnum;
import phenote.util.FileUtil;
import phenote.gui.SearchFilterType;
import phenote.gui.SearchParams;


public class Config {

  public final static String  FLYBASE_DEFAULT_CONFIG_FILE = "flybase.cfg";
  private static Config singleton = new Config();
  private String configFile = FLYBASE_DEFAULT_CONFIG_FILE;
  private List<DataAdapterConfig> dataAdapConfList;
  private List<DataAdapterConfig> queryAdapConfList;
  /** only enabled fields */
  private List<FieldConfig> enabledFields = new ArrayList<FieldConfig>();
  /** enabled & disabled */
  private List<FieldConfig> allFields = new ArrayList<FieldConfig>();
  private static final String defaultLogConfigFile = "conf/log4j.xml";
  // maybe should be using xmlbean where possible?
  //private boolean uvicGraphEnabled = false; // default false for now
  //private boolean termHistoryEnabled = false;   //default to false for now
  //private boolean autoUpdateEnabled = true; //default to true if not in config
  //private int updateTimer = 0; //default is to not wait
  //private String reposUrlDir;
  private String version;
//  private String configName;
//  private String configDesc;
//  private String configAuthor;
  //private String masterToLocalConfigMode;
  //private MasterToLocalConfig masterToLocalBean;
  private PhenoteConfigurationDocument phenoDocBean;
  private PhenoteConfiguration phenoConfigBean; // cache the xml parse bean??

  private boolean configInitialized = false;
  private boolean configModified = false; 
  //flag for if any settings during session have changed, such as search params, col widths, etc.

  private final static String myphenoteFile = "my-phenote";

  public static Config inst() {
    return singleton;
  }
  /** singleton */
  private Config() {}

  public boolean isInitialized() { return configInitialized; }
  
  public boolean isConfigModified() { return configModified; }
  public void setConfigModified(boolean setting) { configModified = setting; }

  public void saveModifiedConfig() {
  	//If settings during the Phenote session are modified (such as search
  	//parameters, term history view, col widths (and others, eventually)
  	//then this function will rewrite the config file with whatever the
  	//current settings are (presumably they are changed)
    File localFile = new File(configFile);
    new ConfigWriter().writeConfig(this,localFile);

    setConfigModified(false);  //in case if the person doesn't quit
    return;
  }
  /** This is setting config file with nothing to do with personal config
   this is for the servlet where config file location is set in web.xml */
  public void setConfigFile(String configFile) throws ConfigException {
    //if (configFile==null) configFile = getDefaultFile();
    setConfigFile(configFile,false,false,false); // dont use .phenote by default (servlet)
  }

  public static void changeConfig(String newCfgFile) throws ConfigException {
    Config newCfg = new Config();
    // throws ConfigEx on fail
    newCfg.updateConfigFileWithNewVersion(newCfgFile); // for now???
    singleton = newCfg; // if config succeeds then set 
  }

  /** This is for when phenote is first installed and one of the default config
      files is used - the -i option if you will. The initial config file will
      get copied to .phenote/my-phenote.cfg if it doesnt already exist. if it
      exists then this file is ignored */
//   public void setInitialConfigFile(String configFile) throws ConfigException {
//     //if (configFile==null) configFile = getDefaultFile();
//     //setConfigFile(configFile,true,false,true); // last true should be false!
//     updateConfigFileWithNewVersion(configFile);
//     //setConfigFile(configFile,true,false);
//   }

  // --update - set up cmd line!
  public void updateConfigFileWithNewVersion(String configFile) throws ConfigException {
    if (configFile==null) configFile = getDefaultFile();
    setConfigFile(configFile,true,false,true);
  }

  public void setOverwriteConfigFile(String configFile) throws ConfigException {
    if (configFile==null) configFile = getDefaultFile();
    setConfigFile(configFile,true,true,false);
  }

  /** Changed this to actually do updating of config file by default given that this
      is now the route in from webstart, without this users will never get config
      updates - eventually get more sophisticated - give user options of
      updateFromDefault, revertToMain/Default or personal/ignoreMain/dontupdate */
  public void loadDefaultConfigFile() throws ConfigException {
    boolean updatePersonalFromMainCfg = true; //false;
    setConfigFile(getDefaultFile(),true,false,updatePersonalFromMainCfg);
  }

  /** if all else fails revert to flybase which should be there */
  public void loadDefaultFlybaseConfigFile() throws ConfigException {
    setConfigFile(FLYBASE_DEFAULT_CONFIG_FILE,true,false,false); // merge true?
  }

  /** default file should be in .phenote/conf/my-phenote.cfg. if not set yet then just
      do good ol flybase.cfg - actually if not there then query user */
  private String getDefaultFile() {
    String file=null;
    try {
//      LineNumberReader r = new LineNumberReader(new FileReader(getMyPhenoteFile()));
//      file = r.readLine();
      file = getMyPhenoteConfigString();
    } catch (IOException e) {}
    if (file == null || file.equals("")) {
      //file = FLYBASE_DEFAULT_CONFIG_FILE;
      file = queryUserForConfigFile();
    }
    return file;
  }

  /** Get config file string that is in my-phenote file - throw io exception if file
      doesnt exist. todo: should also throw ex if doesnt have a valid file in it? */
  public String getMyPhenoteConfigString() throws IOException {
    LineNumberReader r = new LineNumberReader(new FileReader(getMyPhenoteFile()));
    String configFile = r.readLine();
    return configFile;
  }
  
  private String queryUserForConfigFile() {
    return ConfigFileQueryGui.queryUserForConfigFile();
  }


  /** if usePersonalConfig is false then ignore personal(my-phenote.cfg). if true
      then overwrite personal if overwritePersonal is true, otherwise only write
      to personal if personal doesnt exist, if personal exists ignore passed in
      config file */
  private void setConfigFile(String file, boolean usePersonalConfig,
                            boolean overwritePersonalConfig,boolean mergeConfigs) 
    throws ConfigException {
    this.configFile = file; // ??
    // look to see if config file in ~/.phenote - if not copy there
    if (usePersonalConfig) { // for standalone not servlet
      configFile = getMyPhenoteConfig(configFile,overwritePersonalConfig,mergeConfigs);
    }
    System.out.println("Attempting to read config from "+configFile);
    //parseXmlFileWithDom(configFile); // do parse here?
    //URL configUrl = getConfigUrl(filename);
    //System.out.println("config file: "+configUrl);
    parseXmlFile(configFile); // throws ex

    // if we got here we succeeded in setting config
    configInitialized = true;

    //System.out.println("testing config writeback");
    //new ConfigWriter().writeConfig(this,new File(FileUtil.getDotPhenoteDir(),"my-phenote.cfg"));
  }

  /** masterConfig is filename from cmd line or from my-phenote
      First test if masterConfig exists, then looks for file in .phenote cache.
      throws excpetion if neither exist
      if mergeConfigs is true and masterConfig exists it then tries to merge
      passedIn(master) with dotConf(cache)
     why return string? why not file or url? */
  private String getMyPhenoteConfig(String masterConfig,boolean overwritePersonalCfg,
                                    boolean mergeConfigs)
    throws ConfigException {
    
//       boolean masterExists = true;
//       URL masterUrl=null;
//       // currently getConfigUrl doesnt search .phenote/conf - which is handy here
//       // if passed in conf doesnt exist, carry on with dotConf - funny logic?
//       try { masterUrl = getConfigUrl(masterConfig); }
//       catch (FileNotFoundException fe) { masterExists = false; }

//       // strips path to just file (but isnt it already stripped??)
//       String nameOfFile = FileUtil.getNameOfFile(masterConfig); 
//       // this is the "species" conf file - eg ~/.phenote/conf/flybase.cfg
//       File dotConfFile = new File(getDotPhenoteConfDir(),nameOfFile);

//       if (!masterExists && !dotConfFile.exists())
//         throw new ConfigException("Cfg file doesnt exist in app nor .phenote/conf");

    // master conf may have attribute that overrides merge/overwrite settings - need to
    // check those before going through with below -- throws Ex
    ConfigMode mode = new ConfigMode(masterConfig,mergeConfigs,overwritePersonalCfg);

    //if (mergeConfigs && masterExists) {
    if (mode.isUpdate())
      mergeMasterWithLocal(mode); // eventuall pass in mode as well
    
      
    // if file doesnt exist yet or overwrite, copy over masterConfig
    else if (mode.isWipeout()) {
      mode.doWipeout(); // ?? thorws ConfigEx
      // this should probably do a read & write of cfg to get version in there
      // however if writeback is missing something its problematic
      // mode.doWipeout?
      //copyUrlToFile(mode.masterUrl,mode.localFile); // Exx
    }
    
    // new way - set new default(no param) config file name in my-phenote.cfg
    writeMyPhenoteDefaultFile(masterConfig); // ? master?
    
    return mode.localFileString(); // ?
  }


  /** ConfigMode INNER CLASS - outer class? */
  private class ConfigMode {

    //private String mode = "";
    //private boolean updateWithNewVersion=false;
    private boolean masterExists = true;
    private URL masterUrl;
    private File localFile; //dotConfFile;
    private boolean cmdLineWipeout=false;
    private MasterToLocalConfig masterToLocalBean;

    private ConfigMode(String masterConfig,boolean merge,boolean cmdLineWipeout) 
      throws ConfigException {
      this.cmdLineWipeout = cmdLineWipeout;
      // currently getConfigUrl doesnt search .phenote/conf - which is handy here
      // if passed in conf doesnt exist, carry on with dotConf - funny logic?
      try { masterUrl = getConfigUrl(masterConfig); }
      catch (FileNotFoundException fe) { masterExists = false; }

      // strips path to just file (but isnt it already stripped??)
      String nameOfFile = FileUtil.getNameOfFile(masterConfig); 
      // this is the "species" conf file - eg ~/.phenote/conf/flybase.cfg
      localFile = new File(getDotPhenoteConfDir(),nameOfFile);

      if (!masterExists && !localFile.exists())
        throw new ConfigException("Cfg file doesnt exist in app nor .phenote/conf");

      parseModeXml(masterConfig);
    }

    private void parseModeXml(String masterConfig) throws ConfigException {
      if (!masterExists) return;
      Config cfg = new Config();
      try {
        cfg.parseXmlFile(masterConfig);
        //if (cfg.masterToLocalConfigMode != null)  mode = cfg.masterToLocalConfigMode;
        if (cfg.phenoConfigBean == null) return;
        masterToLocalBean = cfg.phenoConfigBean.getMasterToLocalConfig();
        //mode = masterToLocalBean.getMode()
        loadMasterOverrideUrl();
      } catch (ConfigException x) {} // do nothing? err msg?
    }

    /** if have master override - load it! */
    private void loadMasterOverrideUrl() {
      if (!haveXmlBean()) return;
      String urlString = getXmlBean().getOverridingMasterUrl();
      if (urlString==null) return;
      try {
        URL u = new URL(urlString); // throws MalformedURLEx
        u.openStream(); // throws IOEx
        masterUrl = u; // no exception thrown - we're ok
        masterExists = true; // actually this has to be true silly
      } catch (Exception e) {} // masterUrl not set
    }

    private boolean haveXmlBean() { return masterToLocalBean != null; }
    private MasterToLocalConfig getXmlBean() { return masterToLocalBean; }

    private boolean isWipeout() {
      if (!masterExists) return false;
      if (!localFileExists()) return true; // init
      if (cmdLineWipeout) return true;
      return configIsWipeout(); //mode.equals("WIPEOUT");
      //return (!dotConfFile.exists() || overwritePersonalCfg)
    }
    
    private boolean configIsWipeout() {
      if (!haveXmlBean() || masterToLocalBean.getMode() == null)
        return false;
      return masterToLocalBean.getMode().equals("WIPEOUT");
    }

    /** not sure if this belongs in this class?? */
    private void doWipeout() throws ConfigException {
      
      if (!isAlways() && versionSame()) {
        sameVersionMessage("writing over");
        return;
      }

      doWipeoutMessage();
      // this shouldnt be copy!!! read in write out to get version!
      copyUrlToFile(masterUrl,localFile); // Ex  put method in inner class?
    }

    private void sameVersionMessage(String type) {
      System.out.println("Template & local config have same version "+phenoteVersion()+
                         ", not "+type+" local cfg");
    }

    private boolean versionSame() throws ConfigException {
      if (!localFileExists()) return false;
      Config localCfg = new Config();
      localCfg.parseXmlFile(localFileString());
      return versionSame(localCfg.version);
    }

    private String phenoteVersion() { return PhenoteVersion.versionString(); }

    private boolean versionSame(String version) {
      if (version == null) return false;
      if (PhenoteVersion.versionString() == null) return false;
      return (version.equals(phenoteVersion()));
    }

    private void doWipeoutMessage() {
      String s = !localFileExists() ? " does not exist" : " getting overwritten";
      System.out.println(localFile+s+" Copying "+masterUrl);
    }

    private boolean isUpdate() {
      if (!masterExists) return false; // cant update without master
      if (isWipeout()) return false;
      else return true; // for now...
    }
    private boolean isUpdateWithNewVersion() {
      return isUpdate() && !isUpdateAlways(); // for now
    }
    private boolean isUpdateAlways() {
      if (!isUpdate()) return false;  //!isUpdateWithNewVersion();
      return isAlways();
    }
    
    private boolean isAlways() {
      if (!haveXmlBean()) return false;
      String when = masterToLocalBean.getWhen();
      if (when == null) return false;
      return when.equals("ALWAYS");
    }


    private boolean isUpdateable(String version) {
      if (!isUpdate()) return false;
      if (isUpdateAlways()) return true;
      if (!isUpdateWithNewVersion()) return false;
      // version
      return !versionSame(version);
    }


    private boolean localFileExists() {
      return localFile!=null && localFile.exists();
    }
    private String localFileString() { return localFile.toString(); }
  }

  private static File getDotPhenoteConfDir() {
    return FileUtil.getDotPhenoteConfDir();
    //File conf = new File(dotPhenote,"conf");conf.mkdir();return conf;
  }

  private static File getMyPhenoteFile() {
    return new File(getDotPhenoteConfDir(),myphenoteFile);
  }

  /** Write name of config file loaded out to .phenote/conf/my-phenote.cfg for use
      by future startups with no config specified 
      Throws ConfigException if fails to find file */
  public static void writeMyPhenoteDefaultFile(String newDefaultFileString)
    throws ConfigException {
    try {
      File myPhenote = getMyPhenoteFile();
      PrintStream os = new PrintStream(new FileOutputStream(myPhenote));
      os.print(newDefaultFileString);
      os.close();
    } catch (FileNotFoundException e) { throw new ConfigException(e); }
  }

  /** goes thru url line by line and copies to file - is there a better way to 
      do this? actually should do copy anymore should read in and write out xml
      adding version along the way */
  private void copyUrlToFile(URL configUrl,File myPhenote) throws ConfigException {

    try {
      InputStream is = configUrl.openStream();
      FileOutputStream os = new FileOutputStream(myPhenote);
      for(int next = is.read(); next != -1; next = is.read()) {
        os.write(next);
      }
      is.close();
      os.flush();
      os.close();
    } catch (Exception e) { throw new ConfigException(e); }
  }


  /** MERGING/UPDATING Load in 2 configs, anything new in newConfig gets put into my phenote 
      only merge if versions are different(?) in other words only merge on version
      change/phenote upgrade - if version same then leave in users mucking */
  private void mergeMasterWithLocal(ConfigMode mode) throws ConfigException { //URL newConfig,File oldDotConfFile
    // this actually is redundant as covered in isWipeout()
//     if (!mode.localFileExists()) { 
//       System.out.println(mode.localFile+" doesnt exist, creating");
//       // this should actually do a read in and writeback to get version in there...
//       copyUrlToFile(mode.masterUrl,mode.localFile);
//       return;
//     }
    Config newCfg = new Config();
    newCfg.parseXmlUrl(mode.masterUrl); //??
    Config oldCfg = new Config();
    oldCfg.parseXmlFile(mode.localFileString());

    // so actually new/sys config may have out of date version or none - just use
    // PhenoteVersion itself!
    //String version = PhenoteVersion.versionString();//newCfg.version
    //if (mode.isUpdateWithNewVersion() && version != null && version.equals(oldCfg.version)) {
    if (!mode.isUpdateable(oldCfg.version)) {
      //System.out.println("System & user config same version, not updating cfg");
      mode.sameVersionMessage("updating");
      return;
    }
    else
      System.out.println("System config is newer than user, updating user config");

    // File Adapters - preserves enable state of old/local - should it?
    for (DataAdapterConfig dac : newCfg.getFileAdapConfigs()) {
      if (!oldCfg.hasFileAdapConfig(dac))
        oldCfg.addFileAdapConfig(dac);
    }
    for (DataAdapterConfig dac : newCfg.getQueryAdapCfgs()) {
      if (!oldCfg.hasQueryAdapCfg(dac))
        oldCfg.addQueryAdapCfg(dac);
    }
    // log defaulted and probably wont change - dont worry about?? or check if set?
    // but it is a version change so check if diff? maybe? not sure hmmm
    // but i do think ok to update uvic - version change may go from false to true
    //oldCfg.uvicGraphEnabled = newCfg.uvicGraphEnabled; // hmmmm?
    oldCfg.setUvicGraphIsEnabled(newCfg.uvicGraphIsEnabled());
    oldCfg.setTermHistory(newCfg.termHistoryIsEnabled()); //preserve old user pref?
    oldCfg.setReposUrlDir(newCfg.getReposUrlDir()); // ?? pase
    oldCfg.setAutoUpdate(newCfg.autoUpdateIsEnabled());
    oldCfg.setUpdateTimer(newCfg.getUpdateTimer()); // ??? master shouldnt override???
    // should it merge just enabled fields or all fields - probably all fields
    //for (FieldConfig newFC : newCfg.getEnabledFieldCfgs())
    for (FieldConfig newFC : newCfg.getAllFieldCfgs())
      newFC.mergeWithOldConfig(oldCfg,newCfg);

    // write out updated old cfg
    new ConfigWriter().writeConfig(oldCfg,mode.localFile);
  }



  // --> hasFileDataAdapters
  public boolean hasDataAdapters() {
    //return dataAdapConfList != null && !dataAdapConfList.isEmpty();
    return getDataAdapters() != null && !getDataAdapters().isEmpty();
  }
  
  List<DataAdapterConfig> getFileAdapConfigs() {
    if (dataAdapConfList == null)
      dataAdapConfList = new ArrayList<DataAdapterConfig>(4);
    return dataAdapConfList;
  }
  /** Check if has file adapter config with same name */
  private boolean hasFileAdapConfig(DataAdapterConfig dac) {
    for (DataAdapterConfig d : getFileAdapConfigs()) {
      if (d.hasSameAdapter(dac))
        return true;
    }
    return false;
  }
  /** Check if has file adapter config with same name */
  private boolean hasQueryAdapCfg(DataAdapterConfig dac) {
    for (DataAdapterConfig d : getQueryAdapCfgs()) {
      if (d.hasSameAdapter(dac))
        return true;
    }
    return false;
  }

  /** Returns enabled data adapters - empty list if none enabled */
  public List<DataAdapterI> getDataAdapters() {
    if (dataAdapConfList==null) return null; // ex?
    ArrayList daList = new ArrayList(dataAdapConfList.size());
    for (DataAdapterConfig d : dataAdapConfList) {
      if (d.isEnabled())
        daList.add(d.getFileAdapter());
    }
    return daList; //new ArrayList(dataAdapConfList);
  }

//   /** Return true if have a dataadapter, and only 1 data adapter */
//   public boolean hasSingleDataAdapter() {
//     return hasDataAdapters() && dataAdapConfList.size() == 1;
//   }

  /** LoadSaveMangr uses if no extension works. Returns null if no data adapters loaded
      Otherwise returns 1st enabled adapter. Todo: add isdefault config */
  public DataAdapterI getDefaultFileAdapter() {
    if (!hasDataAdapters()) return null; // ex?
    // if defaultFileAdapter != null retrun defaultFileAdapter - set by config
    for (DataAdapterConfig d : dataAdapConfList)
      if (d.isEnabled()) return d.getFileAdapter();
    //return dataAdapConfList.get(0);
    return null; // none enabled - shouldnt happen - ex?
  }



  /** config flag for enabling uvic shrimp dag graph */ 
  public boolean uvicGraphIsEnabled() {
    //return uvicGraphEnabled;
    return getUvicGraphBean().getEnable();
  }

  private UvicGraph getUvicGraphBean() {
    UvicGraph g = phenoConfigBean.getUvicGraph();
    if (g == null) {
      g = phenoConfigBean.addNewUvicGraph(); // will this default to false?
    }
    return g;
  }

  public void setUvicGraphIsEnabled(boolean enable) {
    getUvicGraphBean().setEnable(enable);
  }

  public boolean termHistoryIsEnabled() { 
    return getHistoryBean().getEnable();
  }

  private TermHistory getHistoryBean() {
    TermHistory history = phenoConfigBean.getTermHistory();
    if (history == null) {
      history = phenoConfigBean.addNewTermHistory();
      history.setEnable(false); // default false
    }
    return history;
  }

  public void setTermHistory(boolean setter) { 
    getHistoryBean().setEnable(setter);
  }

  public boolean autoUpdateIsEnabled() {
    return getAutoUpdateBean().getEnable();
  }

  public void setAutoUpdate(boolean enable) {
    getAutoUpdateBean().setEnable(enable);
  }

  private AutoUpdateOntologies getAutoUpdateBean() {
    AutoUpdateOntologies au = phenoConfigBean.getAutoUpdateOntologies();
    if (au == null) {
      au = phenoConfigBean.addNewAutoUpdateOntologies();
      au.setEnable(true); // default true??
    }
    return au;
  }

  // rename getUpdateTimerSeconds?
  public int getUpdateTimer() {
    return getTimerBean().getTimer();
  }

  public void setUpdateTimer(int seconds) {
    getTimerBean().setTimer(seconds);
  }

  private UpdateTimer getTimerBean() {
    UpdateTimer u = phenoConfigBean.getUpdateTimer();
    if (u == null) {
      u = phenoConfigBean.addNewUpdateTimer();
      u.setTimer(0); // default is not to wait???
    }
    return u;
  }
    
  public AutocompleteSettings getAutocompleteSettings() { 	
 // 	boolean[] temp;
  	AutocompleteSettings s = phenoConfigBean.getAutocompleteSettings();
   	if (s==null) {
   		//set defaults if not in configuration file
   		s = phenoConfigBean.addNewAutocompleteSettings();
   		s.setTerm(true);
   		s.setSynonym(true);
   		s.setDefinition(false);
   		s.setObsolete(false);
   	}
   	return s;
  }
   	
  public void setAutocompleteSettings() {
  	SearchParams searchParams = SearchParams.inst();
  	getAutocompleteSettings().setTerm(searchParams.getParam(SearchFilterType.TERM));
  	getAutocompleteSettings().setSynonym(searchParams.getParam(SearchFilterType.SYN));
  	getAutocompleteSettings().setDefinition(searchParams.getParam(SearchFilterType.DEF));
  	getAutocompleteSettings().setObsolete(searchParams.getParam(SearchFilterType.OBS));
  }
 
  
  public URL getLogConfigUrl() throws FileNotFoundException {
    return FileUtil.findUrl(getLogConfigFile());
  }

  String getLogConfigFile() { 
    if (phenoConfigBean.getLog() == null) {
      Log log = phenoConfigBean.addNewLog();
      log.setConfigFile(defaultLogConfigFile);
    }
    return phenoConfigBean.getLog().getConfigFile();
  }

  public int getEnbldFieldsNum() {
    return getEnbldFieldCfgs().size();
  }

  /** Gives name of field at index, 0 based (for table heading) */
  public String getFieldLabel(int index) {
    return getEnbldFieldCfg(index).getLabel();
  }

//   public CharFieldEnum getCharFieldEnum(int index) {
//     return getFieldConfig(index).getCharFieldEnum();
//   }

  public boolean hasEnbldCharField(int index) {
    return getEnbldCharField(index) != null;
  }

  public CharField getEnbldCharField(int index) {
    if (index >= getEnbldFieldsNum()) return null;
    return getEnbldFieldCfg(index).getCharField();
  }

  /** needed for getFieldLabel for table */
  private FieldConfig getEnbldFieldCfg(int index) {
    if (index >= getEnbldFieldsNum()) return null;
    return getEnbldFieldCfgs().get(index);
  }

  /** OntologyDataAdapter calls this to figure which ontologies to load
   This is a list of enabled fields - does not include disabled fields!
  retname getEnabledFieldConfigs? */
  public List<FieldConfig> getEnbldFieldCfgs() {
    return enabledFields;
  }

  private void reconstructEnabledFieldList() {
    enabledFields.clear();
    for (FieldConfig fc : getAllFieldCfgs()) {
      if (fc.isEnabled())
        enabledFields.add(fc);
    }
  }

  public List<FieldConfig> getAllFieldCfgs() { return allFields; }

  /** returns true if has field config with same name - contents may differ */
  boolean hasEnbldFieldCfg(FieldConfig newFC) {
    return getEnbldFieldCfg(newFC.getLabel()) != null;
  }
  boolean hasFieldCfgAll(FieldConfig fc) {
    return getAllFieldCfg(fc.getLabel()) != null;
  }

  /** returns field config with label fieldName */
  FieldConfig getEnbldFieldCfg(String fieldName) {
    for (FieldConfig fc : getEnbldFieldCfgs()) {
      if (fc.getLabel().equals(fieldName))
        return fc;
    }
    return null; // ex?
  }
  FieldConfig getAllFieldCfg(String fieldName) {
    for (FieldConfig fc : getAllFieldCfgs()) {
      if (fc.getLabel().equals(fieldName))
        return fc;
    }
    return null; // ex?
  }

  /** kinda silly to return list?? so there are 2 fields for "Tag" which perhaps is silly
      but thats whats happening so need to deal with it */
  public List<CharField> getCharFieldsForSyntaxAbbrev(String abb) throws ConfigException {
    // cache in hash??
    List<CharField> fields = new ArrayList<CharField>(2);
    for (FieldConfig fc : getEnbldFieldCfgs()) {
      if (fc.hasSyntaxAbbrev(abb) || fc.hasLabel(abb)) // abbrev or label
        fields.add(fc.getCharField());
        //return fc.getCharField();
    }
    if (fields.isEmpty())
      throw new ConfigException("No Field configured with syntax abbrev "+abb);
    return fields;
  }

//   public List<CharField> getCharFieldsForDelimited(int colNum) throws ConfigException {
//     // cache in hash??
//     List<CharField> fields = new ArrayList<CharField>(2);
//     for (FieldConfig fc : getEnbldFieldCfgs()) {
//       fields.add(getEnbldCharField(colNum));
//       //return fc.getCharField();
//     }
//     if (fields.isEmpty())
//       throw new ConfigException("No Field configured");
//     return fields;
//   }
  
  public String getLabelForCharField(CharField cf) throws ConfigException {
	    for (FieldConfig fc : getEnbldFieldCfgs()) {
	      if (fc.hasCharField(cf))
	        return fc.getLabel();
	    }
	    // failed to find field config for char field - shouldnt happen
	    throw new ConfigException("Syn Abbrev for "+cf+" not found");
	  }
  
  public String getSyntaxAbbrevForCharField(CharField cf) throws ConfigException {
    for (FieldConfig fc : getEnbldFieldCfgs()) {
      if (fc.hasCharField(cf))
        return fc.getSyntaxAbbrev();
    }
    // failed to find field config for char field - shouldnt happen
    throw new ConfigException("Syn Abbrev for "+cf+" not found");
  }

  // pase - phase out - returns null if doesnt have
  String getReposUrlDir() {
    if (phenoConfigBean.getOboRepository() == null) return null;
    return phenoConfigBean.getOboRepository().getUrlDir();
  }
  private void setReposUrlDir(String d) { // phase out
    if (d==null) return;
    if (phenoConfigBean.getOboRepository() == null)
      phenoConfigBean.addNewOboRepository();
    phenoConfigBean.getOboRepository().setUrlDir(d);
  }

  /** should this just be a part of fieldConfigList? and main window would filter it
      out when making up fields? rel is for post comp gui - or maybe FieldConfig
      should have isPostComp, getPostCompRelFile - yes! */
  //public FieldConfig getRelationshipFieldConfig() { }


  /** parse xml file with xml beans (phenoteconfigbeans.xml).
      Put in own class? YES ConfigReader */
  private void parseXmlFile(String filename) throws ConfigException {
    //try {
    try {
      URL configUrl = getConfigUrl(filename);
      parseXmlUrl(configUrl);
    }
    catch (FileNotFoundException e) { throw new ConfigException(e); }
  }

  PhenoteConfigurationDocument getPhenoDocBean() { return phenoDocBean; }
      
  private void parseXmlUrl(URL configUrl) throws ConfigException {
    try {
      System.out.println("config file: "+configUrl);
      //PhenoteConfigurationDocument pcd = 
      phenoDocBean = PhenoteConfigurationDocument.Factory.parse(configUrl);
      phenoDocBean.validate(); //???
      phenoConfigBean = phenoDocBean.getPhenoteConfiguration();

      version = phenoConfigBean.getVersion();
      
//      configName = phenoConfigBean.getName();
      
//      configDesc = phenoConfigBean.getDescription();
      
//      configAuthor = phenoConfigBean.getAuthor();

//      System.out.println("version:  "+version+"\nname:  "+configName+"\nDesc:  "+
//      		configDesc+"\nAuthor:  "+getConfigAuthor());
      //MasterToLocalConfig m 
//         = phenoConfigBean.getMasterToLocalConfig();
//       if (m != null && m.getMode() != null)
//         masterToLocalConfigMode = m.getMode();

      // LOG CONFIG FILE
//       Log log = phenoConfigBean.getLog();
//       if (log != null && log.getConfigFile() != null) {
//         logConfigFile = log.getConfigFile();
//       }
      
      // DATA ADAPTERS  <dataadapter name="phenoxml" enable="true"/>

      Dataadapter[] adapters = phenoConfigBean.getDataadapterArray();
      for (Dataadapter da : adapters) {
        DataAdapterConfig dac = new DataAdapterConfig(da);
        if (dac.isQueryable())
          addQueryAdapCfg(dac);
        else
          addFileAdapConfig(dac);
      }

//       QueryableDataadapter[] queryAdaps = phenoConfigBean.getQueryableDataadapterArray();
//       for (QueryableDataadapter da : queryAdaps) {
//         QueryableAdapConfig qac = new QueryableAdapConfig(da);
//         addQueryAdapCfg(qac);
//       }


      // GRAPH - now accesses bean directly
//       UvicGraph gr = phenoConfigBean.getUvicGraph();
//       if (gr != null)
//         uvicGraphEnabled = gr.getEnable();

      // TERM HISTORY
//       TermHistory history = phenoConfigBean.getTermHistory();
//       if (history != null)
//         termHistoryEnabled = history.getEnable();

      // AUTO UPDATE OF ONTOLOGIES
//       AutoUpdateOntologies autoUpdate = phenoConfigBean.getAutoUpdateOntologies();
//       if (autoUpdate != null)
//         autoUpdateEnabled = autoUpdate.getEnable();

      // TIMER for UPDATE OF ONTOLOGIES
//       UpdateTimer time = phenoConfigBean.getUpdateTimer();
//       if (time != null)
// 	  updateTimer = time.getTimer().intValue();

      // Repos url dir
//       OboRepository or = phenoConfigBean.getOboRepository();
//       if (or != null && or.getUrlDir() != null)
//         reposUrlDir = or.getUrlDir();

      // FIELDS
      Field[] fields = phenoConfigBean.getFieldArray();
      for (Field f : fields) {
        makeFieldConfig(f);
      }

    }
    catch (IOException ie) {
      System.out.println("IOException on config parse "+ie);
      throw new ConfigException("io exception with config file "+ie.getMessage());
    }
    catch (XmlException xe) {
      System.out.println("Parse of config xml file failed "+xe);
      throw new ConfigException("Xml exception in config file "+xe.getMessage());
    }
  }

  private URL getConfigUrl(String filename) throws FileNotFoundException {
    return FileUtil.findUrl(filename);
  }

  private void addQueryAdapCfg(DataAdapterConfig dac) {
    getQueryAdapCfgs().add(dac);
  }

  private void addFileAdapConfig(DataAdapterConfig dac) {
    getFileAdapConfigs().add(dac);
  }

  public boolean hasQueryableDataAdapter() {
    return queryAdapConfList != null && getQueryableDataAdapter()!= null;
  }
  
  /** Just get first one thats enabled - for now assume theres one */
  public QueryableDataAdapterI getQueryableDataAdapter() {
    //if (!hasQueryableDataAdapter()) return null; // ex?
    if (queryAdapConfList == null) return null;
    //queryAdapConfList.get(0).getQueryableAdapter();
    // return first enabled adap
    //for (QueryableAdapConfig q : getQueryAdapCfgs())
    for (DataAdapterConfig q : getQueryAdapCfgs())
      if (q.isEnabled()) return q.getQueryableAdapter();
    return null;
  }

  /** all configs for query adaps - enabled or not */
  List<DataAdapterConfig> getQueryAdapCfgs() {
    if (queryAdapConfList == null)
      queryAdapConfList = new ArrayList<DataAdapterConfig>(1);
    return queryAdapConfList;
  }

  private void addQueryAdapCfg(QueryableAdapConfig qac) {
    if (qac == null) return;
    getQueryAdapCfgs().add(qac);
  }

  private void makeFieldConfig(Field field) {
    FieldConfig fc = new FieldConfig(field,this);
    addFieldConfig(fc);
  }
  void addFieldConfig(FieldConfig fc) {
    allFields.add(fc);
    if (fc.isEnabled())
      enabledFields.add(fc);
  }

  /** get index of field config in enabled field config list - returns -1 if
      not in there -- ex? */
//   int getEnabledFieldIndex(FieldConfig fc) {
//     return enabledFields.indexOf(fc);
//   }

  int getAllFieldIndex(FieldConfig fc) {
    return allFields.indexOf(fc);
  }

  /** adds to both field config list and xml beans field list */
  void insertFieldConfig(int index, FieldConfig fc) {
    allFields.add(index,fc); // ??
    if (fc.isEnabled())
      reconstructEnabledFieldList();//enabledFields.add(index,fc);
    // make space for new field
    phenoConfigBean.insertNewField(index);
    // set bean for that field
    phenoConfigBean.setFieldArray(index,fc.getFieldBean());
  }
  
  public String getConfigName() {	
  	String name = phenoConfigBean.getName();
  	return name;  
  	}
  public void setConfigName(String name) {
  	phenoConfigBean.setName(name);
  	return;
  }
  public String getConfigDesc() { 
  	String desc = phenoConfigBean.getDescription();
  	return desc; 
  	}
  public void setConfigDesc(String desc) {
  	phenoConfigBean.setDescription(desc);
  	return;
  }
  public String getConfigAuthor() { 
  	String author = phenoConfigBean.getAuthor();
    return author;
  }
  public void setConfigAuthor(String author) {
  	phenoConfigBean.setAuthor(author);
  	return;
  }
  
}

//       if (overwrite || mode.equals("WIPEOUT_ALWAYS"))
//         wipeoutAlways = true;
//       else
//         updateWithNewVersion = true; // for now
//   private File getMyPhenoteCfgFile() {
//     return new File(FileUtil.getDotPhenoteDir(),"my-phenote.cfg");
//   }

    //File dotPhenote = FileUtil.getDotPhenoteDir();
    // this wont work with merging/updating
    //File myPhenote = new File(dotPhenote,"my-phenote.cfg");
      //} catch (FileNotFoundException e) {throw new ConfigException(e);}
    //return dotConfFile.toString(); // ?
   //parseXmlFile("./conf/initial-flybase.cfg"); // hardwired for now...
  //private FieldConfig lumpConfig = new FieldConfig(CharFieldEnum.LUMP,"Genotype");
  //private String lumpOntologyFile = null;  private OntologyConfig lumpConfig = new OntologyConfig("Genotype");

  //private boolean checkForNewOntologies = false;
  //private int newOntologyCheckMinutes = 10;
  // --> quartz
//   /** perhaps not best name - check if ontology is still fresh, if something newer
//       than load it - for obo files check file date - get this into config file! */
//   public boolean checkForNewOntologies() {
//     return checkForNewOntologies;
//   }
//   /** How many minutes between checks for new ontologies */
//   public int getOntologyCheckMinutes() { return newOntologyCheckMinutes; }
//   private class DataAdapterConfig {

