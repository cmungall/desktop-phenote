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


import org.apache.xmlbeans.XmlException;

import phenote.main.PhenoteVersion;
import phenote.config.xml.PhenoteConfigurationDocument;
import phenote.config.xml.DataadapterDocument.Dataadapter;
import phenote.config.xml.QueryableDataadapterDocument.QueryableDataadapter;
import phenote.config.xml.FieldDocument.Field;
import phenote.config.xml.LogDocument.Log;
import phenote.config.xml.OboRepositoryDocument.OboRepository;
import phenote.config.xml.PhenoteConfigurationDocument.PhenoteConfiguration;
import phenote.config.xml.UvicGraphDocument.UvicGraph;
import phenote.dataadapter.DataAdapterI;
import phenote.dataadapter.QueryableDataAdapterI;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldEnum;
import phenote.util.FileUtil;

public class Config {

  public final static String  FLYBASE_DEFAULT_CONFIG_FILE = "flybase.cfg";
  private static Config singleton = new Config();
  private String configFile = FLYBASE_DEFAULT_CONFIG_FILE;
  private List<DataAdapterConfig> dataAdapConfList;
  private List<QueryableAdapConfig> queryAdapConfList;
  /** only enabled fields */
  private List<FieldConfig> enabledFields = new ArrayList<FieldConfig>();
  /** enabled & disabled */
  private List<FieldConfig> allFields = new ArrayList<FieldConfig>();
  private String logConfigFile = "conf/log4j.xml"; // default log config file
  private boolean uvicGraphEnabled = false; // default false for now
  private String reposUrlDir;
  private String version;
  private boolean configInitialized = false;

  /** singleton */
  private Config() {}

  public boolean isInitialized() { return configInitialized; }

  /** This is setting config file with nothing to do with personal config
   this is for the servlet where config file location is set in web.xml */
  public void setConfigFile(String configFile) throws ConfigException {
    //if (configFile==null) configFile = getDefaultFile();
    setConfigFile(configFile,false,false,false); // dont use .phenote by default (servlet)
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

  public void loadDefaultConfigFile() throws ConfigException {
    setConfigFile(getDefaultFile(),true,false,false);
  }

  /** if all else fails revert to flybase which should be there */
  public void loadDefaultFlybaseConfigFile() throws ConfigException {
    setConfigFile(FLYBASE_DEFAULT_CONFIG_FILE,true,false,false);
  }

  /** default file should be in .phenote/conf/my-phenote.cfg. if not set yet then just
      do good ol flybase.cfg - actually if not there then query user */
  private String getDefaultFile() {
    String file=null;
    try {
      LineNumberReader r = new LineNumberReader(new FileReader(getMyPhenoteFile()));
      file = r.readLine();
    } catch (IOException e) {}
    if (file == null || file.equals("")) {
      //file = FLYBASE_DEFAULT_CONFIG_FILE;
      file = queryUserForConfigFile();
    }
    return file;
  }
  
  private String queryUserForConfigFile() {
    return ConfigFileQueryGui.queryUserForConfigFile();
  }


  /** if usePersonalConfig is false then ignore personal(my-phenote.cfg). if true
      then overwrite personal if overwritePersonal is true, otherwise only write
      to personal if personal doesnt exist, if personal exists ignore passed in
      config file (its an initial config -i) */
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

  // why return string? why not file or url?
  private String getMyPhenoteConfig(String passedInConfig,boolean overwritePersonalCfg,
                                    boolean mergeConfigs)
    throws ConfigException {
    //File dotPhenote = FileUtil.getDotPhenoteDir();
    // this wont work with merging/updating
    //File myPhenote = new File(dotPhenote,"my-phenote.cfg");

    //try {
      boolean passedInExists = true;
      URL passedInUrl=null;
      // currently getConfigUrl doesnt search .phenote/conf - which is handy here
      // if passed in conf doesnt exist, carry on with dotConf - funny logic?
      try { passedInUrl = getConfigUrl(passedInConfig); }
      catch (FileNotFoundException fe) { passedInExists = false; }

      String nameOfFile = FileUtil.getNameOfFile(passedInConfig);
      // this is the "species" conf file - eg ~/.phenote/conf/flybase.cfg
      File dotConfFile = new File(getDotPhenoteConfDir(),nameOfFile);

      if (!passedInExists && !dotConfFile.exists())
        throw new ConfigException("Cfg file doesnt exist in app nor .phenote/conf");

      if (mergeConfigs && passedInExists) {
        mergeNewWithOld(passedInUrl,dotConfFile);
      }
      
      // if file doesnt exist yet or overwrite, copy over passedInConfig
      else if (passedInExists && (!dotConfFile.exists() || overwritePersonalCfg)) {
        String s = overwritePersonalCfg ? " getting overwritten" : " does not exist";
        System.out.println(dotConfFile+s+" Copying "+passedInUrl);
        //try {
        //URL passedInUrl = getConfigUrl(passedInConfig);
        copyUrlToFile(passedInUrl,dotConfFile);
      }
      
      // new way - set new default(no param) config file name in my-phenote.cfg
      writeMyPhenoteDefaultFile(passedInConfig); // ? passedIn?

      return dotConfFile.toString(); // ?
      
     //} catch (FileNotFoundException e) {throw new ConfigException(e);}
    //return dotConfFile.toString(); // ?
  }

  private static File getDotPhenoteConfDir() {
    return FileUtil.getDotPhenoteConfDir();
    //File conf = new File(dotPhenote,"conf");conf.mkdir();return conf;
  }

  private File getMyPhenoteFile() {
    return new File(getDotPhenoteConfDir(),"my-phenote.cfg");
  }

  /** Write name of config file loaded out to .phenote/conf/my-phenote.cfg for use
      by future startups with no config specified */
  private void writeMyPhenoteDefaultFile(String newDefaultFileString)
    throws ConfigException {
    try {
      File myPhenote = getMyPhenoteFile();
      PrintStream os = new PrintStream(new FileOutputStream(myPhenote));
      os.print(newDefaultFileString);
      os.close();
    } catch (FileNotFoundException e) { throw new ConfigException(e); }
  }

  /** goes thru url line by line and copies to file - is there a better way to 
      do this? */
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
  private void mergeNewWithOld(URL newConfig,File oldDotConfFile) throws ConfigException {
    if (!oldDotConfFile.exists()) {
      System.out.println(oldDotConfFile+" doesnt exist, creating");
      copyUrlToFile(newConfig,oldDotConfFile);
      return;
    }
    Config newCfg = new Config();
    newCfg.parseXmlUrl(newConfig); //??
    Config oldCfg = new Config();
    oldCfg.parseXmlFile(oldDotConfFile.toString());

    // so actually new/sys config may have out of date version or none - just use
    // PhenoteVersion itself!
    String version = PhenoteVersion.versionString();//newCfg.version
    if (version != null && version.equals(oldCfg.version)) {
      System.out.println("System & user config same version, not updating cfg");
      return;
    }
    else
      System.out.println("System config is newer than user, updating user config");

    // Data Adapters
    for (DataAdapterConfig dac : newCfg.getAdapConfigs()) {
      if (!oldCfg.hasAdapConfig(dac))
        oldCfg.addAdapConfig(dac);
    }
    // log defaulted and probably wont change - dont worry about?? or check if set?
    // but it is a version change so check if diff? maybe? not sure hmmm
    // but i do think ok to update uvic - version change may go from false to true
    oldCfg.uvicGraphEnabled = newCfg.uvicGraphEnabled; // hmmmm?
    oldCfg.reposUrlDir = newCfg.reposUrlDir; // ??
    for (FieldConfig newFC : newCfg.getFieldConfigList())
      newFC.mergeWithOldConfig(oldCfg);

    // write out updated old cfg - todo write out to .phenote/conf/filename.cfg not mycfg
    new ConfigWriter().writeConfig(oldCfg,oldDotConfFile);//getMyPhenoteCfgFile());
  }

//   private File getMyPhenoteCfgFile() {
//     return new File(FileUtil.getDotPhenoteDir(),"my-phenote.cfg");
//   }


  public static Config inst() {
    return singleton;
  }

  // --> hasFileDataAdapters
  public boolean hasDataAdapters() {
    //return dataAdapConfList != null && !dataAdapConfList.isEmpty();
    return getDataAdapters() != null && !getDataAdapters().isEmpty();
  }
  
  List<DataAdapterConfig> getAdapConfigs() {
    if (dataAdapConfList == null)
      dataAdapConfList = new ArrayList<DataAdapterConfig>(4);
    return dataAdapConfList;
  }
  /** Check if has data adapter config with same name */
  private boolean hasAdapConfig(DataAdapterConfig dac) {
    for (DataAdapterConfig d : getAdapConfigs()) {
      //if (d.getName().equals(dac.getName()))
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
        daList.add(d.getDataAdapter());
    }
    return daList; //new ArrayList(dataAdapConfList);
  }

//   /** Return true if have a dataadapter, and only 1 data adapter */
//   public boolean hasSingleDataAdapter() {
//     return hasDataAdapters() && dataAdapConfList.size() == 1;
//   }

  /** LoadSaveMangr uses if no extension works */
  public DataAdapterI getSingleDataAdapter() {
    if (!hasDataAdapters()) return null; // ex?
    for (DataAdapterConfig d : dataAdapConfList)
      if (d.isEnabled()) return d.getDataAdapter();
    //return dataAdapConfList.get(0);
    return null; // none enabled - shouldnt happen - ex?
  }



  /** config flag for enabling uvic shrimp dag graph */ 
  public boolean uvicGraphIsEnabled() { return uvicGraphEnabled; }

  public URL getLogConfigUrl() throws FileNotFoundException {
    return FileUtil.findUrl(logConfigFile);
  }

  String getLogConfigFile() { return logConfigFile; }

  public int getNumberOfFields() {
    return getFieldConfigList().size();
  }

  /** Gives name of field at index, 0 based (for table heading) */
  public String getFieldLabel(int index) {
    return getFieldConfig(index).getLabel();
  }

//   public CharFieldEnum getCharFieldEnum(int index) {
//     return getFieldConfig(index).getCharFieldEnum();
//   }
  public CharField getCharField(int index) {
    return getFieldConfig(index).getCharField();
  }

  /** needed for getFieldLabel for table */
  private FieldConfig getFieldConfig(int index) {
    return getFieldConfigList().get(index);
  }

  /** OntologyDataAdapter calls this to figure which ontologies to load */
  public List<FieldConfig> getFieldConfigList() {
    return enabledFields;
  }

  /** returns true if has field config with same name - contents may differ */
  boolean hasFieldConfig(FieldConfig newFC) {
    return getFieldConfig(newFC.getLabel()) != null;
  }

  /** returns field config with label fieldName */
  FieldConfig getFieldConfig(String fieldName) {
    for (FieldConfig fc : getFieldConfigList()) {
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
    for (FieldConfig fc : getFieldConfigList()) {
      if (fc.hasSyntaxAbbrev(abb) || fc.hasLabel(abb)) // abbrev or label
        fields.add(fc.getCharField());
        //return fc.getCharField();
    }
    if (fields.isEmpty())
      throw new ConfigException("No Field configured with syntax abbrev "+abb);
    return fields;
  }

  public String getLabelForCharField(CharField cf) throws ConfigException {
	    for (FieldConfig fc : getFieldConfigList()) {
	      if (fc.hasCharField(cf))
	        return fc.getLabel();
	    }
	    // failed to find field config for char field - shouldnt happen
	    throw new ConfigException("Syn Abbrev for "+cf+" not found");
	  }
  
  public String getSyntaxAbbrevForCharField(CharField cf) throws ConfigException {
    for (FieldConfig fc : getFieldConfigList()) {
      if (fc.hasCharField(cf))
        return fc.getSyntaxAbbrev();
    }
    // failed to find field config for char field - shouldnt happen
    throw new ConfigException("Syn Abbrev for "+cf+" not found");
  }

  String getReposUrlDir() { return reposUrlDir; }

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
      
  private void parseXmlUrl(URL configUrl) throws ConfigException {
    try {
      System.out.println("config file: "+configUrl);
      PhenoteConfigurationDocument pcd = 
        PhenoteConfigurationDocument.Factory.parse(configUrl);//configFile);
      pcd.validate(); //???
      PhenoteConfiguration pc = pcd.getPhenoteConfiguration();

      version = pc.getVersion();

      // LOG CONFIG FILE
      Log log = pc.getLog();
      if (log != null && log.getConfigFile() != null) {
        logConfigFile = log.getConfigFile();
      }
      
      // DATA ADAPTERS  <dataadapter name="phenoxml" enable="true"/>

      Dataadapter[] adapters = pc.getDataadapterArray();
      for (Dataadapter da : adapters) {
        DataAdapterConfig dac = new DataAdapterConfig(da);
        addAdapConfig(dac);
        //String name = da.getName().toString();
        //addDataAdapterFromString(name);
      }

      QueryableDataadapter[] queryAdaps = pc.getQueryableDataadapterArray();
      for (QueryableDataadapter da : queryAdaps) {
        QueryableAdapConfig qac = new QueryableAdapConfig(da);
        addQueryAdapCfg(qac);
      }


      // GRAPH
      UvicGraph gr = pc.getUvicGraph();
      if (gr != null)
        uvicGraphEnabled = gr.getEnable();

      // Repos url dir
      OboRepository or = pc.getOboRepository();
      if (or != null && or.getUrlDir() != null)
        reposUrlDir = or.getUrlDir();

      // FIELDS
      Field[] fields = pc.getFieldArray();
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


  private void addAdapConfig(DataAdapterConfig dac) {
    getAdapConfigs().add(dac);
  }

  public boolean hasQueryableDataAdapter() {
    return queryAdapConfList != null && getQueryableDataAdapter()!= null;
  }
  
  /** Just get first one - for now assume theres one */
  public QueryableDataAdapterI getQueryableDataAdapter() {
    //if (!hasQueryableDataAdapter()) return null; // ex?
    if (queryAdapConfList == null) return null;
    //queryAdapConfList.get(0).getQueryableAdapter();
    // return first enabled adap
    for (QueryableAdapConfig q : getQueryAdapCfgs())
      if (q.isEnabled()) return q.getQueryableAdapter();
    return null;
  }

  private List<QueryableAdapConfig> getQueryAdapCfgs() {
    if (queryAdapConfList == null)
      queryAdapConfList = new ArrayList<QueryableAdapConfig>(1);
    return queryAdapConfList;
  }

  private void addQueryAdapCfg(QueryableAdapConfig qac) {
    if (qac == null) return;
    getQueryAdapCfgs().add(qac);
  }

  private void makeFieldConfig(Field field) {
    FieldConfig fc = new FieldConfig(field);
    addFieldConfig(fc);
  }
  void addFieldConfig(FieldConfig fc) {
    allFields.add(fc);
    if (fc.isEnabled())
      enabledFields.add(fc);
  }
  
}
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

//     /** construct from Dataadapter xml bean */
//     private DataAdapterConfig(Dataadapter xmlBean) {
//       String name = da.getName();
//       addDataAdapterFromString(name);
//     }

//     boolean enabled=true; // enabled by default
//     // new -> class name, old -> phenoxml|phenosyntax|nexus 
//     String configString;
//     // will be null if enabled = false
//     DataAdapterI dataAdapter;

//     // do some other way? DataAdapterManager has mapping? DataAdapter has mapping?
//     // DataAdapterManager.getAdapter(name)???
//     // just do class string see tracker issue 1649004
//     private void addDataAdapterFromString(String daString) {
      
//       // new way of doing things is class name itself - so 1st try introspect...
//       try {
//         Class c = Class.forName(daString);
//         Object o = c.newInstance();
//         if ( ! (o instanceof DataAdapterI))
//           throw new Exception("class not instance of DataAdapterI");
//         DataAdapterI da = (DataAdapterI)o;
//         addDataAdapter(da);
//       }
//       catch (Exception e) { 
        
//         // backward compatibility - have merger replace these with class names eventually
//         if (daString.equalsIgnoreCase("phenoxml"))
//           addDataAdapter(new PhenoXmlAdapter());
//         else if (daString.equalsIgnoreCase("phenosyntax"))
//           addDataAdapter(new PhenoSyntaxFileAdapter());
//         else if (daString.equalsIgnoreCase("flybase")) // pase??
//           addDataAdapter(new FlybaseDataAdapter()); // for now...
//         else if (daString.equalsIgnoreCase("nexus"))
//           addDataAdapter(new NEXUSAdapter());
//         // LOG not set up yet???
//         else
//           System.out.println("Data adapter not recognized "+daString);
//       }
//     }
//   }

//}

//     String name = field.getName(); //toString();
//     // has to be a valid value - no longer true for generic free types
//     FieldConfig fc;
//     try { // phase this out!!
//       CharFieldEnum cfe = CharFieldEnum.getCharFieldEnum(name);
//       //if (cfe == null) ???
//       fc = new FieldConfig(cfe,name);
//     }
//     catch (Exception e) { // no char field enum for name - new generic!
//       fc = new FieldConfig(name);
//     }

//     if (field.getSyntaxAbbrev() != null) {
//       fc.setSyntaxAbbrev(field.getSyntaxAbbrev());
//     }
    
//     // POST COMP, relationship ontol
//     if (field.getPostcomp() != null) {
//       fc.setIsPostComp(true);
//       String relFile = field.getPostcomp().getRelationshipOntology();
//       fc.setPostCompRelOntCfg(OntologyConfig.makeRelCfg(relFile));
//     }


//     // ONTOLOGIES if only one ontology file is an attribute... (convenience)
//     if (field.getFile() != null) {
//       fc.addOntologyConfig(new OntologyConfig(field));
//     }
//     // otherwise its multiple ontologies listed in ontology elements (entity)
//     else {
//       Ontology[] ontologies = field.getOntologyArray();
//       for (Ontology o : ontologies) {
//         fc.addOntologyConfig(new OntologyConfig(o));
//       }
//     }

//   private OntologyConfig makeOntologyConfig(String name, String file) {
//     OntologyConfig oc = new OntologyConfig(name,file);
//     return oc;
//   }


//       // CHECK FOR ONTOLOGIES
//       CheckForNewOntologies cfno = pc.getCheckForNewOntologies();
//       if (cfno != null) { // ?
//         checkForNewOntologies = true;
//         BigInteger bi = cfno.getIntervalMinutes();
//         if (bi != null)
//           newOntologyCheckMinutes = bi.intValue();
//       }

