package phenote.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.obo.dataadapter.OBDSQLDatabaseAdapter.OBDSQLDatabaseAdapterConfiguration;

import phenote.config.xml.PhenoteConfigurationDocument;
import phenote.config.xml.AutoUpdateOntologiesDocument.AutoUpdateOntologies;
import phenote.config.xml.AutocompleteSettingsDocument.AutocompleteSettings;
import phenote.config.xml.CharacterModeDocument.CharacterMode;
import phenote.config.xml.ComparisonDocument.Comparison;
import phenote.config.xml.DataInputServletDocument.DataInputServlet;
import phenote.config.xml.DataadapterDocument.Dataadapter;
import phenote.config.xml.ExternaldbDocument.Externaldb;
import phenote.config.xml.FieldDocument.Field;
import phenote.config.xml.FieldPanelTabsDocument.FieldPanelTabs;
import phenote.config.xml.OntologyFileDocument;
import phenote.config.xml.GroupDocument.Group;
import phenote.config.xml.LogDocument.Log;
import phenote.config.xml.TerminologyDefinitionsDocument.TerminologyDefinitions;
import phenote.config.xml.MasterToLocalConfigDocument.MasterToLocalConfig;
import phenote.config.xml.OntologyFileDocument.OntologyFile;
import phenote.config.xml.OntologyLoadingDocument.OntologyLoading;
import phenote.config.xml.PhenoteConfigurationDocument.PhenoteConfiguration;
import phenote.config.xml.TermHistoryDocument.TermHistory;
import phenote.config.xml.UpdateTimerDocument.UpdateTimer;
import phenote.config.xml.UvicGraphDocument.UvicGraph;
import phenote.dataadapter.DataAdapterI;
import phenote.dataadapter.GroupAdapterI;
import phenote.dataadapter.OntologyMakerI;
import phenote.dataadapter.QueryableDataAdapterI;
import phenote.dataadapter.ncbi.NCBIDataAdapterI;
import phenote.datamodel.AnnotationMappingDriver;
import phenote.datamodel.BasicAnnotationMappingDriver;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldManager;
import phenote.gui.SearchFilterType;
import phenote.gui.SearchParams;
import phenote.main.PhenoteVersion;
import phenote.util.FileUtil;


public class Config {

  public final static String  FLYBASE_DEFAULT_CONFIG_FILE = "flybase.cfg";
  private static Config singleton;

  private String configFile = FLYBASE_DEFAULT_CONFIG_FILE; // default
  private List<DataAdapterConfig> dataAdapConfList;
  private List<DataAdapterConfig> queryAdapConfList;
  /** only enabled fields */
  private List<FieldConfig> enabledFields = new ArrayList<FieldConfig>();
  /** enabled & disabled */
  private List<FieldConfig> allFields = new ArrayList<FieldConfig>();
  private List<OntologyConfig> allOntologies = new ArrayList<OntologyConfig>();
  
  public static final String defaultLogConfigFile = "conf/log4j-standalone.xml";
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

  // so name is both the filename and the display name for the config - which simplifies 
  // things - but if it becomes a drag we'll break this up
  private String name;
  private String displayName; // for gui config to get html italics & grey in there - cheesy?

  private boolean configInitialized = false;
  private boolean configModified = false; 
  private boolean alwaysOverride = false;
   //flag for if any settings during session have changed, such as search params, col widths, etc.
  
  private static Map<String,OBDSQLDatabaseAdapterConfiguration> jdbcPathToOBDConfiguration = 
	  new HashMap<String,OBDSQLDatabaseAdapterConfiguration>();

  private final static String myphenoteFile = "my-phenote";

  public static Config inst() {
    if (singleton == null) {
      singleton = new Config();
    }
    return singleton;
  }
  
  public static void reset() {
    singleton = null;
  }
  
  /** singleton */
  private Config() {}

  // for gui config
  public static Config newInstance(String name,String displayName) {
    Config c = new Config();
    c.phenoDocBean = PhenoteConfigurationDocument.Factory.newInstance();
    c.phenoConfigBean = c.phenoDocBean.addNewPhenoteConfiguration();
    c.setConfigName(name);
    c.displayName = displayName;
    return c;
  }

  public static Config makeConfigFromFile(String file) throws ConfigException {
    Config c = new Config();
    c.setConfigFile(file,true,false,false);
    //c.setConfigName(file); // or in setConfigFile?
    return c;
  }

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

  /** -c from command line does this - should be always but isnt??? */
  public void setOverwriteConfigFile(String configFile) throws ConfigException {
    if (configFile==null) configFile = getDefaultFile();
    alwaysOverride = true; // make sure it wipes out always
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
    setNameFromConfigFile(file); // ??
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
    /** url from overriding-master-url config */
    private URL masterUrl;
    private File localFile; //dotConfFile;
    private boolean cmdLineWipeout=false;
    private MasterToLocalConfig masterToLocalBean;

    /** @param masterConfig config file in jar/source, not cache, not overriding url */
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

    /** @param masterConfig config file in jar/source, not cache, not overriding url */
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

    /** if have master override (possibly) from http - load it! */
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
      if (alwaysOverride) return true;
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
    log().info("Copying "+configUrl+" to "+myPhenote);
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

  public Collection<String> getExternalDatabasePaths() {
	  Collection<String> paths = new HashSet<String>();
	  for (Externaldb x : Arrays.asList(phenoConfigBean.getExternaldbArray())) {
		  paths.add(x.getPath());
	  }
	  return paths;
  }


  public Collection<OBDSQLDatabaseAdapterConfiguration> getExternalDatabaseConfigs() {
	  Collection<OBDSQLDatabaseAdapterConfiguration> configs = new HashSet<OBDSQLDatabaseAdapterConfiguration>();
	  for (Externaldb x : Arrays.asList(phenoConfigBean.getExternaldbArray())) {
		  String jdbcPath = x.getPath();
		  if (jdbcPathToOBDConfiguration.get(jdbcPath) != null)
			  configs.add(jdbcPathToOBDConfiguration.get(jdbcPath));
		  else {
			  OBDSQLDatabaseAdapterConfiguration config = new OBDSQLDatabaseAdapterConfiguration();
			  config.setReadPaths(Collections.singletonList(jdbcPath));
			  jdbcPathToOBDConfiguration.put(jdbcPath,config);
			  configs.add(config);
		  }
	  }
	  return configs;
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
    ArrayList<DataAdapterI> daList = new ArrayList<DataAdapterI>(dataAdapConfList.size());
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

  public boolean dataInputServletIsEnabled() {
    return getDataInputServletBean().getEnable();
  }
  
  private DataInputServlet getDataInputServletBean() {
    DataInputServlet d = phenoConfigBean.getDataInputServlet();
    if (d == null) d = phenoConfigBean.addNewDataInputServlet();
    return d;
  }
  public void setDataInputServletIsEnabled(boolean enable) {
    getDataInputServletBean().setEnable(enable);
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
  
  public boolean shouldUseFieldPanelTabs() {
    FieldPanelTabs bean = phenoConfigBean.getFieldPanelTabs();
    if (bean != null) {
      return bean.getEnable();
    } else {
      return true;
    }
  }

  /** returns enum from character mode xml bean itself - why not right? */
  public CharacterMode.Mode.Enum getCharacterMode() {
    return getCharModeBean().getMode();
  }

  /** mapping for character to obo annot, used for OBO_ANNOTATION character mode
      meaningless in pure character mode, returns BasicAnnotationMappingDriver by
      default */
  public AnnotationMappingDriver getAnnotMappingDriver() {
    String map = getCharModeBean().getMapping();
    if (map == null) {
      //map = "phenote.datamodel.BasicAnnotationMappingDriver"; // default
      map = "phenote.datamodel.DefaultMappingDriver"; // CJM TODO change back!!!
      //map = "phenote.datamodel.PhenotypeAssociationMappingDriver"; // CJM TODO change back!!!
           getCharModeBean().setMapping(map);
    }
    try {
      Object o = getInstanceForString(map);
      if (!(o instanceof AnnotationMappingDriver))
        throw new Exception(map+" not an instance of AnnotationMappingDriver");
      return (AnnotationMappingDriver)o;
    }
    catch (Exception e) {
      log().error("failed to get annot mapping driver "+e+" going with "
                  +"BasicAnnotationMappingDriver");
      return new BasicAnnotationMappingDriver(); // return null?
    }
  }

  private Object getInstanceForString(String classString) throws Exception {
    Class<?> c = Class.forName(classString);
    return c.newInstance();
  }

  private CharacterMode getCharModeBean() {
    CharacterMode mode = phenoConfigBean.getCharacterMode();
    if (mode == null) {
      mode = phenoConfigBean.addNewCharacterMode();
      mode.setMode(CharacterMode.Mode.CHARACTER); // character default CJM TODO change back!!!
      //mode.setMode(CharacterMode.Mode.OBO_ANNOTATION); // character default CJM TODO change back!!!
    }
    return mode;
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

  /** Adds & returns xml Field bean */
  Field addNewFieldBean() {
    return phenoConfigBean.addNewField();
  }

  public int getEnbldFieldsNum() {
    return getEnbldFieldCfgs().size();
  }

  public int getEnbldFieldsNum(String group) {
    return getFieldCfgsInGroup(group).size();
  }

  /** Gives name of field at index, 0 based (for table heading) */
  public String getFieldLabel(int index,String group) {
    if (getEnbldFieldCfg(index,group)==null) {
      System.out.println("ERROR: no field for col "+index+" group "+group);
      return null; // ? ex? ""?
    }
    return getEnbldFieldCfg(index,group).getLabel();
  }
  
  public int getFieldColwidth(int index) {
  	return getEnbldFieldCfg(index).getColwidth();
  }
  
  public void setFieldColwidth(int index, int width) {
  	getEnbldFieldCfg(index).setColwidth(width);
  }

  public int getMinCompChars(int fieldIndex) {
    if (!hasEnbldCharField(fieldIndex)) {
      log().error("No char field for index "+fieldIndex);
      return 0; // throw ex?
    }
    return getEnbldFieldCfg(fieldIndex).getMinCompletionChars();
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

  private FieldConfig getEnbldFieldCfg(int index,String group) {
    if (index >= getEnbldFieldsNum(group)) return null;
    return getFieldCfgsInGroup(group).get(index);
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
  
  public List<OntologyConfig> getAllOntologies() { return allOntologies; }

  /** returns true if has field config with same name - contents may differ */
//   boolean hasEnbldFieldCfg(FieldConfig newFC) {
//     return getEnbldFieldCfg(newFC.getLabel()) != null;
//   }

  boolean hasFieldCfgAll(FieldConfig fc) {
    return getAllFieldCfg(fc.getLabel()) != null;
  }

  /** returns field config with label fieldName or tag */
  private FieldConfig getEnbldFieldCfg(String nameOrTag) {
    for (FieldConfig fc : getEnbldFieldCfgs()) {
      if (fc.getLabel().equals(nameOrTag))
        return fc;
      if (fc.getDataTag().equals(nameOrTag))
        return fc;
    }
    return null; // ex?
  }

  /** should first check field with datatag if has one, and if not then label
      as label may not be unique - fix this - only uses label at the moment */
  FieldConfig getAllFieldCfg(String fieldName) {
    for (FieldConfig fc : getAllFieldCfgs()) {
      if (fc.getLabel().equals(fieldName)) // bad!
        return fc;
    }
    return null; // ex?
  }

  public boolean isVisible(CharField cf) {
    FieldConfig fc = getFieldConfig(cf);
    if (fc == null) return false; // ex?
    return fc.isVisible();
  }

  /** returns field config for char field - tries both tag & name, returns null
      if none */
  private FieldConfig getFieldConfig(CharField cf) {
    FieldConfig fc = getEnbldFieldCfg(cf.getTag());
    if (fc != null) return fc;
    return getEnbldFieldCfg(cf.getName());
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
  
//   public String getLabelForCharField(CharField cf) throws ConfigException {
//     for (FieldConfig fc : getEnbldFieldCfgs()) {
//       if (fc.hasCharField(cf))
//         return fc.getLabel();
//     }
//     // failed to find field config for char field - shouldnt happen
//     throw new ConfigException("Label for "+cf+" not found");
//   }
  
  public String getSyntaxAbbrevForCharField(CharField cf) { //throws ConfigException {
    for (FieldConfig fc : getEnbldFieldCfgs()) {
      if (fc.hasCharField(cf))
        return fc.getSyntaxAbbrev();
    }
    // failed to find field config for char field - so currently this happens for
    // "Date Created" field that is added by CharFieldManager even if not in config
    // as its thought every annot should have a date create (also owner, and date update!)
    // this method is solely for pheno syntax adapter which is getting phased out for tab delim
    // so i think it should then just get tag from char field in this case...
    return cf.getTag();

    //throw new ConfigException("Syn Abbrev for "+cf+" not found");
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

  //right now the ncbi adapters are under the queryable flag...need to make their
  //own flag i think
  public boolean hasQueryableDataAdapter() {
    return (queryAdapConfList != null && 
    			getQueryableDataAdapter()!= null);
  }
  
  public boolean hasNCBIAdapter() {
  	return getNCBIDataAdapters()!=null;
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
  
//  /**There can be >1 adapter.  should return a list*/
//  public NCBIDataAdapterI getNCBIDataAdapter() {
//  	if (queryAdapConfList == null) return null;
//  	for (DataAdapterConfig q : getQueryAdapCfgs())
//  		if (q.isEnabled()) return q.getNCBIAdapter();
//  	return null;
//  }
  public List<NCBIDataAdapterI> getNCBIDataAdapters() {
    if (queryAdapConfList==null) return null; // ex?
    ArrayList<NCBIDataAdapterI> qaList = new ArrayList<NCBIDataAdapterI>(queryAdapConfList.size());
    for (DataAdapterConfig q : queryAdapConfList) {
      if (q.isEnabled())
        qaList.add(q.getNCBIAdapter());
    }
    return qaList; //new ArrayList(queryAdapConfList);
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
  public void addFieldConfig(FieldConfig fc) {
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
  
  public String toString() {
    if (displayName != null) return displayName;
    return getConfigName();
  }

  // i dont think we should do it this way - i think the name & filename should be 
  // synonomous - and the user shouldnt have to know anything about files
  public String getConfigName() {	
    //String name = phenoConfigBean.getName();
    return this.name;  
  }
  public void setConfigName(String name) {
    this.name = name; //phenoConfigBean.setName(name);
    displayName = name; // displayName = null?
    setConfigFileFromName();
  }

  private void setConfigFileFromName() {
    configFile =  name.replaceAll(" ","-");
    configFile += ".cfg";
  }

  private void setNameFromConfigFile(String file) {
    name = file;
    if (name.endsWith(".cfg")) name = name.substring(0,name.length()-4);
    name.replaceAll("-"," ");
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

  /** Loading screen is the elephant splash screen that displays when phenote
      is initializing ontologies & such at startup. The current LoadingScreen
      can go into threadlock and seize up phenote so need ability to disable it
      defaults to true (false?) */
  public boolean showLoadingScreen() {
    OntologyLoading ol = phenoConfigBean.getOntologyLoading();
    if (ol == null) return true; 
    if (ol.xgetShowLoadingScreen() == null) return true;
    return ol.getShowLoadingScreen(); 
  }

  
  public List<Group> getFieldGroups() {
    return Arrays.asList(this.phenoConfigBean.getGroupArray());
  }
  
  public List<String> getFieldsInGroup(String groupName) {
    List<String> fields = new ArrayList<String>();
    for (Field aField : this.phenoConfigBean.getFieldArray()) {
      final List<?> groups = aField.getGroups();
      if (groups != null) {
        for (Object aGroup : groups) {
          if (((String)aGroup).equals(groupName)) {
            fields.add(aField.getName());
          }
        }
      }
      // if we are looking for default group and field hasnt specified group
      // then field goes into default group
      else if (groups == null && groupName.equals("default")) {
        fields.add(aField.getName());
      }
    }
    return fields;
  }

  /** get enabled field configs for a group - or should we just use Field? */
  private List<FieldConfig> getFieldCfgsInGroup(String group) {
    List<FieldConfig> fCfgs = new ArrayList<FieldConfig>();
    for (FieldConfig fc : getEnbldFieldCfgs()) {
      if (fc.inGroup(group))
        fCfgs.add(fc);
    }
    return fCfgs;
  }
  
  /** Returns null if group w groupName doesnt exist */
  public String getTitleForGroup(String groupName) {
    Group g = getGroup(groupName);
    if (g == null) return null;
    return g.getTitle();
  }

  /** Returns xmlbean Group for groupName, returns null if no such group
   should it create group for "default" if it doesnt exist - i think so - just 
  have to make sure not creating 2 defaults */
  private Group getGroup(String groupName) {
    if (groupName == null) return getDefaultGroup();
    for (Group aGroup : this.phenoConfigBean.getGroupArray()) {
      if (aGroup.getName().equals(groupName)) {
        return aGroup;
      }
    }
    if (groupName.equals(CharFieldManager.DEFAULT_GROUP)) // OntMan?
      return createDefaultGroup(); // ??
    //return null; // ex? return default?
    return getDefaultGroup(); // if all fails just return default?? or null??
  }

  public Group getDefaultGroup() {
    return getGroup(CharFieldManager.DEFAULT_GROUP);
  }

  private Group createDefaultGroup() {
    Group g = phenoConfigBean.addNewGroup();
    g.setTitle("Main");
    g.setInterface(Group.Interface.DEFAULT);
    g.setName(CharFieldManager.DEFAULT_GROUP);
    return g;
  }

  public boolean hasGroupAdapter(String group) {
    return getGroupAdapter(group) != null;
    //getGroup(group).getGroupAdapter() != null;
  }

  /** returns null if group adapter for group not found - ex? */
  public GroupAdapterI getGroupAdapter(String groupStr) {
    Group grp = getGroup(groupStr);
    String classString = grp.getGroupAdapter();
    if (classString == null) return null;
    if (grp.getInterface().equals(Group.Interface.CHARACTER_TEMPLATE)) return null;
    try {
      // should we cache in hash and insure 1 instance???
      Class<?> c = Class.forName(classString);
      Constructor<?> cr = c.getConstructor(String.class);
      Object o = cr.newInstance(groupStr);
      if (!(o instanceof GroupAdapterI)) {
        log().error("group_adapter cfg is not a GroupAdapterI "+classString);
        return null;
      }
      GroupAdapterI ga = (GroupAdapterI)o;
      String destField = grp.getDestinationField();
      ga.addDestinationField(destField); // ex/err msg if fail?
      return ga;
    }
    catch (Exception e) {
      log().error("Unable to find group adapter for "+classString+"\n"+e);
      e.printStackTrace();
      return null;
    }
  }

  /** returns whether have term maker for given group - there could also be a term
      maker manager? */
  public boolean hasOntMakerForGroup(String group) {
    // for now hardwire - set up config
    return getOntMaker(group) != null;
  }
  // OntMaker manager?
  public OntologyMakerI getOntMaker(String group) {
    // testing
    if (!group.equals("genotypeMaker")) return null; // testing
    return new phenote.dataadapter.fly.ProformaAlleleParser();
  }

  public boolean compareStatementEnabled() {
    Comparison c = phenoConfigBean.getComparison();
    if (c == null) return false; // default false?
    
    return c.getEnableStatementComparison(); // i think defaults to false
  }
  
  
  /**
   * This will retrieve all of the ontology/terminology definitions in a 
   * configuration file.  Included here will be all ontology files, their
   * handle, repository location, and whether or not to update.
   * @return An object that defines all ontologies/terminologies 
   * utilized in the configuration, together with a convenience flag to indicate 
   * whether to update some, none, or all ontologies.
   * 
   */
  public TerminologyDefinitions getTerminologyDefs() { 
    TerminologyDefinitions termDefs = phenoConfigBean.getTerminologyDefinitions();
    return termDefs; 
  }
  public void setTerminologyDefs(TerminologyDefinitions termDefs) {
  	phenoConfigBean.setTerminologyDefinitions(termDefs);
  	return;
  }
  
  /**
   * @return This returns the an array of ontologies themselves, including
   * the file names, and other metadata (such as version, etc.)
   */
  public OntologyFileDocument.OntologyFile[] getOntologyList() { 
    OntologyFileDocument.OntologyFile[] fileArray;
    fileArray = phenoConfigBean.getTerminologyDefinitions().getOntologyFileArray();
    return fileArray; 
  }
  
  public void setOntologyList(OntologyFileDocument.OntologyFile[] ontologyList) {
  	phenoConfigBean.getTerminologyDefinitions().setOntologyFileArray(ontologyList);
  	return;
  }
  
  
  /**
   * This method will make the "update ontologies" flag pase.
   * @return Convenience method to determine the global status of updating
   * ontology files.  This will either be None, Some, or All ontologies.  This
   * defaults to None if not set in the configuration.
   */
  public TerminologyDefinitions.Update.Enum getUpdateTerminologies() {
  	TerminologyDefinitions.Update.Enum updateStatus;
  	if (!phenoConfigBean.getTerminologyDefinitions().isNil()) {
  		updateStatus = 
  			phenoConfigBean.getTerminologyDefinitions().getUpdate();
  	} else {
  		updateStatus = TerminologyDefinitions.Update.NONE;
  	}
  	return updateStatus;
  }
  
  public OntologyFile getOntologyFileByHandle(String handle) {
  	for (OntologyFile ontology : phenoConfigBean.getTerminologyDefinitions().getOntologyFileArray()) {
    	
    	if (handle.equals(ontology.getHandle())) {
    		return ontology;
    	}
  	}
  	return null;

  }
  
//this will need to get smarter so that it determines what flags are set for all
//individual ontologies
  public void setUpdateTerminologies(TerminologyDefinitions.Update.Enum updateStatus) {
  	phenoConfigBean.getTerminologyDefinitions().setUpdate(updateStatus);  	
  }
  
  /** load up all constraints from config into ConstraintManager,
      from constaint list as well as required config fields */
  public void loadConstraints() {
    for (FieldConfig fc : getEnbldFieldCfgs())
      fc.makeRequiredConstraint();
  }


  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }

}

