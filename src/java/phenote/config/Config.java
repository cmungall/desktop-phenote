package phenote.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.apache.xmlbeans.XmlException;
// in phenoteconfigbeans.jar code generate xml beans
import phenote.config.xml.PhenoteConfigurationDocument;
import phenote.config.xml.PhenoteConfigurationDocument.PhenoteConfiguration;
//import phenote.config.xml.CheckForNewOntologiesDocument.CheckForNewOntologies;
import phenote.config.xml.DataadapterDocument.Dataadapter;
import phenote.config.xml.FieldDocument.Field;
import phenote.config.xml.OntologyDocument.Ontology;
import phenote.config.xml.LogDocument.Log;
import phenote.config.xml.UvicGraphDocument.UvicGraph;

import phenote.util.FileUtil;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldEnum;
import phenote.dataadapter.DataAdapterI;
import phenote.dataadapter.fly.FlybaseDataAdapter;
import phenote.dataadapter.phenosyntax.PhenoSyntaxFileAdapter;
import phenote.dataadapter.phenoxml.PhenoXmlAdapter;

public class Config {

  public final static String  DEFAULT_CONFIG_FILE = "flybase.cfg";
  private static Config singleton = new Config();
  private String configFile = DEFAULT_CONFIG_FILE;
  //private String patoFile = "attribute_and_values.obo"; // default value
  //private OntologyConfig patoConfig;
  //private FieldConfig patoConfig;
  private FieldConfig lumpConfig = new FieldConfig(CharFieldEnum.LUMP,"Genotype");
  //private String lumpOntologyFile = null;  private OntologyConfig lumpConfig = new OntologyConfig("Genotype");
  //private List<OntologyConfig> entityConfigList = new ArrayList<OntologyConfig>();
  //private FieldConfig entityConfig;
  //private FieldConfig geneticContextConfig;
  private List<DataAdapterI> dataAdapterList;
  private List<FieldConfig> fieldList = new ArrayList<FieldConfig>();
  //private boolean checkForNewOntologies = false;
  //private int newOntologyCheckMinutes = 10;
  private String logConfigFile = "conf/log4j.xml"; // default log config file
  private boolean uvicGraphEnabled = false; // default false for now

  /** singleton */
  private Config() {
    //parseXmlFile("./conf/initial-flybase.cfg"); // hardwired for now...
  }

  /** This is setting config file with nothing to do with personal config
   this is for the servlet where config file location is set in web.xml */
  public void setConfigFile(String configFile) throws ConfigException {
    setConfigFile(configFile,false,false); // dont use .phenote by default (servlet)
  }

  /** This is for when phenote is first installed and one of the default config
      files is used - the -i option if you will. The initial config file will
      get copied to .phenote/my-phenote.cfg if it doesnt already exist. if it
      exists then this file is ignored */
  public void setInitialConfigFile(String configFile) throws ConfigException {
    setConfigFile(configFile,true,false);
  }

  public void setOverwriteConfigFile(String configFile) throws ConfigException {
    setConfigFile(configFile,true,true);
  }

  /** if usePersonalConfig is false then ignore personal(my-phenote.cfg). if true
      then overwrite personal if overwritePersonal is true, otherwise only write
      to personal if personal doesnt exist, if personal exists ignore passed in
      config file (its an initial config -i) */
  private void setConfigFile(String file, boolean usePersonalConfig,
                            boolean overwritePersonalConfig) 
    throws ConfigException {
    this.configFile = file; // ??
    // look to see if config file in ~/.phenote - if not copy there
    if (usePersonalConfig) { // for standalone not servlet
      configFile = getMyPhenoteConfig(configFile,overwritePersonalConfig);
    }
    System.out.println("Attempting to read config from "+configFile);
    //parseXmlFileWithDom(configFile); // do parse here?
    //URL configUrl = getConfigUrl(filename);
    //System.out.println("config file: "+configUrl);
    parseXmlFile(configFile); // throws ex
  }

  private String getMyPhenoteConfig(String passedInConfig,
                                    boolean overwritePersonalCfg)
    throws ConfigException {
    String home = System.getProperty("user.home");
    File dotPhenote = new File(home+"/.phenote");
    if (!dotPhenote.exists()) {
      System.out.println("creating "+dotPhenote+" directory");
      dotPhenote.mkdir();
    }
    File myPhenote = new File(dotPhenote,"my-phenote.cfg");
    // if file doesnt exist yet or overwrite, copy over passedInConfig
    if (!myPhenote.exists() || overwritePersonalCfg) {
      String s = overwritePersonalCfg ? " getting overwritten" : " does not exist";
      System.out.println(myPhenote+s+" Copying "+passedInConfig);
      try {
        URL passedInUrl = getConfigUrl(passedInConfig);
        copyUrlToFile(passedInUrl,myPhenote);
      } catch (FileNotFoundException e) {
        throw new ConfigException(e);
      }
    }
    return myPhenote.toString(); // ?
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

  public static Config inst() {
    return singleton;
  }

  public boolean hasDataAdapters() {
    return dataAdapterList != null && !dataAdapterList.isEmpty();
  }
  
  public List<DataAdapterI> getDataAdapters() {
    return new ArrayList(dataAdapterList);
  }

  /** Return true if have a dataadapter, and only 1 data adapter */
  public boolean hasSingleDataAdapter() {
    return hasDataAdapters() && dataAdapterList.size() == 1;
  }

  public DataAdapterI getSingleDataAdapter() {
    if (!hasDataAdapters()) return null;
    return dataAdapterList.get(0);
  }

  // --> quartz
//   /** perhaps not best name - check if ontology is still fresh, if something newer
//       than load it - for obo files check file date - get this into config file! */
//   public boolean checkForNewOntologies() {
//     return checkForNewOntologies;
//   }
//   /** How many minutes between checks for new ontologies */
//   public int getOntologyCheckMinutes() { return newOntologyCheckMinutes; }

  /** config flag for enabling uvic shrimp dag graph */ 
  public boolean uvicGraphIsEnabled() { return uvicGraphEnabled; }

  public URL getLogConfigUrl() throws FileNotFoundException {
    return FileUtil.findUrl(logConfigFile);
  }

  public int getNumberOfFields() {
    return getFieldConfigList().size();
  }

  /** Gives name of field at index, 0 based (for table heading) */
  public String getFieldLabel(int index) {
    return getFieldConfig(index).getLabel();
  }

  public CharFieldEnum getCharFieldEnum(int index) {
    return getFieldConfig(index).getCharFieldEnum();
  }

  /** needed for getFieldLabel for table */
  private FieldConfig getFieldConfig(int index) {
    return getFieldConfigList().get(index);
  }

  /** OntologyDataAdapter calls this to figure which ontologies to load */
  public List<FieldConfig> getFieldConfigList() {
    return fieldList;
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

  public String getSyntaxAbbrevForCharField(CharField cf) throws ConfigException {
    for (FieldConfig fc : getFieldConfigList()) {
      if (fc.hasCharField(cf))
        return fc.getSyntaxAbbrev();
    }
    // failed to find field config for char field - shouldnt happen
    throw new ConfigException("Syn Abbrev for "+cf+" not found");
  }

  /** should this just be a part of fieldConfigList? and main window would filter it
      out when making up fields? rel is for post comp gui - or maybe FieldConfig
      should have isPostComp, getPostCompRelFile - yes! */
  //public FieldConfig getRelationshipFieldConfig() { }


  /** parse xml file with xml beans (phenoteconfigbeans.xml). Put in own class? */
  private void parseXmlFile(String filename) throws ConfigException {
    try {
      URL configUrl = getConfigUrl(filename);
      System.out.println("config file: "+configUrl);
      PhenoteConfigurationDocument pcd = 
        PhenoteConfigurationDocument.Factory.parse(configUrl);//configFile);
      pcd.validate(); //???
      PhenoteConfiguration pc = pcd.getPhenoteConfiguration();


      // LOG CONFIG FILE
      Log log = pc.getLog();
      if (log != null && log.getConfigFile() != null) {
        logConfigFile = log.getConfigFile().getStringValue();
      }
      
      // DATA ADAPTERS
      Dataadapter[] adapters = pc.getDataadapterArray();
      for (Dataadapter da : adapters) {
        String name = da.getName().toString();
        addDataAdapterFromString(name);
      }

      // GRAPH
      UvicGraph gr = pc.getUvicGraph();
      if (gr != null)
        uvicGraphEnabled = gr.getEnable();

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

  // do some other way? DataAdapterManager has mapping? DataAdapter has mapping?
  // DataAdapterManager.getAdapter(name)???
  private void addDataAdapterFromString(String daString) {
    if (daString.equalsIgnoreCase("phenoxml"))
      addDataAdapter(new PhenoXmlAdapter());
    else if (daString.equalsIgnoreCase("phenosyntax"))
      addDataAdapter(new PhenoSyntaxFileAdapter());
    else if (daString.equalsIgnoreCase("flybase")) // pase??
      addDataAdapter(new FlybaseDataAdapter()); // for now...
  }

  private void addDataAdapter(DataAdapterI da) {
    if (dataAdapterList == null)
      dataAdapterList = new ArrayList<DataAdapterI>(3);
    dataAdapterList.add(da);
  }

  private void makeFieldConfig(Field field) {
    String name = field.getName().getStringValue(); //toString();
    // has to be a valid value - no longer true for generic free types
    FieldConfig fc;
    try { // phase this out!!
      CharFieldEnum cfe = CharFieldEnum.getCharFieldEnum(name);
      //if (cfe == null) ???
      fc = new FieldConfig(cfe,name);
    }
    catch (Exception e) { // no char field enum for name - new generic!
      fc = new FieldConfig(name);
    }

    if (field.getSyntaxAbbrev() != null) {
      fc.setSyntaxAbbrev(field.getSyntaxAbbrev().getStringValue());
    }
    
    // POST COMP, relationship ontol
    if (field.getPostcomp() != null) {
      fc.setIsPostComp(true);
      String relFile = field.getPostcomp().getRelationshipOntology().getStringValue();
      fc.setPostCompRelOntCfg(makeOntologyConfig("Relationship",relFile));
    }

    // ONTOLOGIES if only one ontology file is an attribute... (convenience)
    if (field.getFile() != null) {
      String file = field.getFile().getStringValue();
      // downside of strogly types xml beans is filterOut has to be dealt with 
      // separately for field & ontology - annoying - & all other attribs
      String filterOut = 
        field.getFilterOut()!=null ? field.getFilterOut().getStringValue() : null;
      String slim = field.getSlim()!=null ? field.getSlim().getStringValue() : null;
      fc.addOntologyConfig(makeOntologyConfig(name,file,filterOut,slim));
    }
    // otherwise its multiple ontologies listed in ontology elements (entity)
    else {
      Ontology[] ontologies = field.getOntologyArray();
      for (Ontology o : ontologies) {
        String oName = o.getName().getStringValue();
        String oFile = o.getFile().getStringValue();
        String filterOut=null;
        if (o.getFilterOut() != null)
          filterOut = o.getFilterOut().getStringValue();
        String slim = o.getSlim()!=null ? o.getSlim().getStringValue() : null;
        OntologyConfig oc = makeOntologyConfig(oName,oFile,filterOut,slim);
        fc.addOntologyConfig(oc);
      }
    }
    fieldList.add(fc);
  }

  

  private OntologyConfig makeOntologyConfig(String name, String file) {
    OntologyConfig oc = new OntologyConfig(name,file);
    return oc;
  }
  private OntologyConfig makeOntologyConfig(String name, String file, String filterOut) {
    return new OntologyConfig(name,file,filterOut);
  }
  private OntologyConfig makeOntologyConfig(String name, String file, String filterOut,
                                            String slim) {
    return new OntologyConfig(name,file,filterOut,slim);
  }

}

//       // CHECK FOR ONTOLOGIES
//       CheckForNewOntologies cfno = pc.getCheckForNewOntologies();
//       if (cfno != null) { // ?
//         checkForNewOntologies = true;
//         BigInteger bi = cfno.getIntervalMinutes();
//         if (bi != null)
//           newOntologyCheckMinutes = bi.intValue();
//       }

