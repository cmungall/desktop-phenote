package phenote.config;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import phenote.datamodel.CharField.CharFieldEnum;
import phenote.dataadapter.DataAdapterI;
import phenote.dataadapter.fly.FlybaseDataAdapter;
import phenote.dataadapter.phenoxml.PhenoXmlAdapter;

public class Config {

  public final static String  DEFAULT_CONFIG_FILE = "/initial-flybase.cfg";
  private static Config singleton = new Config();
  private String configFile = DEFAULT_CONFIG_FILE;
  //private String patoFile = "attribute_and_values.obo"; // default value
  //private OntologyConfig patoConfig;
  private FieldConfig patoConfig;
  private FieldConfig lumpConfig = new FieldConfig(CharFieldEnum.LUMP,"Genotype");
  //private String lumpOntologyFile = null;  private OntologyConfig lumpConfig = new OntologyConfig("Genotype");
  //private List<OntologyConfig> entityConfigList = new ArrayList<OntologyConfig>();
  private FieldConfig entityConfig;
  private FieldConfig geneticContextConfig;
  private List<DataAdapterI> dataAdapterList;

  /** singleton */
  private Config() {
    //parseXmlFile("./conf/initial-flybase.cfg"); // hardwired for now...
  }

  public void setConfigFile(String configFile) {
    this.configFile = configFile; // ??
    System.out.println("Attempting to read config from "+configFile);
    parseXmlFile(configFile); // do parse here?
  }

  public static Config inst() {
    return singleton;
  }

  public boolean hasDataAdapters() {
    return dataAdapterList != null && !dataAdapterList.isEmpty();
  }

  /** Return true if have a dataadapter, and only 1 data adapter */
  public boolean hasSingleDataAdapter() {
    return hasDataAdapters() && dataAdapterList.size() == 1;
  }

  public DataAdapterI getSingleDataAdapter() {
    if (!hasDataAdapters()) return null;
    return dataAdapterList.get(0);
  }

  private FieldConfig getLumpConfig() {
    // name = Taxonmony, file = BTO.obo...
    return lumpConfig;
  }

  public boolean hasLumpField() {
    return true; // config this - ctol may not have lumps... for now true
  }

  public boolean hasLumpOntology() {
    if (!hasLumpField())
      return false;
    return lumpConfig.hasOntology();
  }

  public OntologyConfig getLumpOntologyConfig() {
    if (!hasLumpField()) return null;
    return lumpConfig.getOntologyConfig();
  }

  public OntologyConfig getPatoOntologyConfig() {
    return getPatoConfig().getOntologyConfig();
  }

  /** paot config should always be present, make default if not set from xml */
  private FieldConfig getPatoConfig() {
    if (patoConfig == null) {
      OntologyConfig o = OntologyConfig.defaultPato;
      patoConfig = new FieldConfig(CharFieldEnum.PATO,o);
    }
    return patoConfig;
  }

  public boolean hasGeneticContextField() {
    return geneticContextConfig != null;
  }

  public OntologyConfig getGeneticContextOntologyConfig() {
    if (!hasGeneticContextField()) return null;
    return getGeneticContextConfig().getOntologyConfig(); // check if has ont?
  }

  public FieldConfig getGeneticContextConfig() {
    return geneticContextConfig;
  }
  
  private void initGeneticContextConfig(OntologyConfig oc) {
    geneticContextConfig = new FieldConfig(CharFieldEnum.GENETIC_CONTEXT,oc);
  }

  private FieldConfig getEntityConfig() {
    if (entityConfig == null)
      entityConfig = new FieldConfig(CharFieldEnum.ENTITY,"Entity");
    return entityConfig;
  }

  public List<OntologyConfig> getEntityOntologyConfigs() {
//     if (entityConfigList.isEmpty()) entityConfigList = defaultEntityConfigList();
//     return entityConfigList;
    return getEntityConfig().getOntologyConfigList();
  }

  public int getNumberOfFields() {
    return getFieldConfigList().size();
  }

  /** Gives name of field at index, 0 based (for table heading) */
  public String getFieldLabel(int index) {
    return getFieldConfig(index).getLabel();
  }

//   public String getFieldName(int index) {
//     return getFieldConfig(index).getLabel();
//   }

  public CharFieldEnum getCharFieldEnum(int index) {
    return getFieldConfig(index).getCharFieldEnum();
  }

  private FieldConfig getFieldConfig(int index) {
    return getFieldConfigList().get(index);
  }

  private List<FieldConfig> fieldList;

  public List<FieldConfig> getFieldConfigList() {
    if (fieldList == null)
      initFieldConfigList();
    return fieldList;
  }

  // refactor! - just have it come straight from xml parse!
  private void initFieldConfigList() {
    fieldList = new ArrayList<FieldConfig>();
    if (hasLumpField()) {
      fieldList.add(getLumpConfig());
    }
    if (hasGeneticContextField()) {
      fieldList.add(getGeneticContextConfig());
    }
    // entity config should always be present shouldnt it?
    fieldList.add(getEntityConfig());
    fieldList.add(getPatoConfig()); // pato required
  }


  /** Default entity list is the anatomy ontology */
  private List<OntologyConfig> defaultEntityConfigList() {
    OntologyConfig oc = new OntologyConfig("Anatomy","anatomy.obo");
    List<OntologyConfig> l = new ArrayList<OntologyConfig>(1);
    l.add(oc);
    return l;
  }

  /** Throws ParserConfig,SAXEx, & IOException if problems - sep class? */
  private Document getDocument(String filename) throws Exception {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = dbf.newDocumentBuilder();
    Document document = tryFile(builder,filename);
    if (document == null) 
      document = tryFile(builder,"conf/"+filename);
    if (document == null) { // try jar file
      URL url = Config.class.getResource(filename);
      if (url == null)
        System.out.println("failed to get file from jar");
      else
        document = tryFile(builder,url.toString());
    }
    if (document == null) {
      System.out.println("Failed to find config file "+filename);
      throw new FileNotFoundException(filename);
    }
    return document;
  }

  /** Returns null if cant find file, throws exception if parse fails */
  private Document tryFile(DocumentBuilder builder, String uri) throws Exception {
    if (uri == null) return null;
    Document document = null;
    try {
      document = builder.parse(uri);
    }
    catch (FileNotFoundException e) {
      return null;
    }
    return document;
  }

  // do this with xml beans & xsd of config?
  private void parseXmlFile(String filename) {
    Document document=null;
    try {
      document = getDocument(filename);
    } catch (Exception e) {
      System.out.println("Xml config parse error: "+e);
      e.printStackTrace();
      return;
    }
    Element root = document.getDocumentElement();
    NodeList kids = root.getChildNodes();
    int size = kids.getLength();
    for (int i=0; i<size; i++) {
      Node node = kids.item(i);
      parsePato(node); // if not pato does nothing
      parseEntity(node);
      parseGeneticContext(node);
      parseDataAdapter(node);
    }
  }

  // private void parseLump(Node node) ...

  // return true if sucessfully parsed?
  private void parsePato(Node node) {
    if (!node.getNodeName().equals("pato")) return;
    OntologyConfig oc = makeOntologyConfig(node,"Pato");
    getPatoConfig().addOntologyConfig(oc);
  }

  private void parseEntity(Node node) {
    if (!node.getNodeName().equals("entity")) return;

    // while (hasMoreOntologies(node) ??? na!
    
    // List <OntologyConfig> getOntologyConfigs(node) {}

    makeOntologyConfigs(node,getEntityConfig());
    //getEntityConfig().addOntologyConfig(oc);
    //getEntityConfig().setOntologyConfigs(configs);
    //entityConfigList.add(fc);
  }

  private void parseGeneticContext(Node node) {
    if (!node.getNodeName().equals("genetic-context")) return;
    OntologyConfig oc = makeOntologyConfig(node,"Genetic Context");
    initGeneticContextConfig(oc);
    // check if has ontology?
    //getGeneticContextConfig().addOntologyConfig(oc);
  }

  /** Return null if node doesnt actually have ontology info - like if there
      is no file attribute which is required */
  private OntologyConfig makeOntologyConfig(Node node, String defaultName) {
    String name = getNameAttribute(node);
    if (name == null || name.equals(""))
      name = defaultName;
    // if field only has one ontology
    if (!hasFileAttribute(node))
      return null;

    String file = getFileAttribute(node);

    // see if theres mutliple ontologies specified as ontology elements
    //if (file == null) 
    OntologyConfig oc = new OntologyConfig(name,file);
    return oc;
  }

  /** so this is funny but the field node may contain info on a single ontology
      or it may contain ontology kid nodes, in the case it has multiple ontolgies
      like entity */
  private void makeOntologyConfigs(Node fieldNode, FieldConfig fieldConfig) {
    String name = getNameAttribute(fieldNode);
    if (name == null || name.equals(""))
      name = fieldConfig.getLabel();

    // if field only has one ontology will be specified in node attribs
    OntologyConfig oc = makeOntologyConfig(fieldNode,name);
    if (oc != null)
      fieldConfig.addOntologyConfig(oc); // return?

    // see if theres mutliple ontologies specified as ontology elements
    NodeList kids = fieldNode.getChildNodes();
    for (int i=0; i<kids.getLength(); i++) {
      Node node = kids.item(i);
      if (node.getNodeName().equals("ontology")) {
        oc = makeOntologyConfig(node,name);
        fieldConfig.addOntologyConfig(oc);
      }
    }
  }

  private boolean hasFileAttribute(Node node) {
    return hasContent(getFileAttribute(node));
  }
  
  private boolean hasContent(String s) {
    return s != null && !s.equals("");
  }

  private String getFileAttribute(Node node) {
    return getAttribute(node,"file");
  }

  private String getNameAttribute(Node node) {
    return getAttribute(node,"name");
  }

  /** Returns null if node is not element, or attrib not attribute */
  private String getAttribute(Node node, String attrib) {
    Element element = elementCast(node);
    if (element == null) return null;
    return element.getAttribute(attrib);
  }

  private Element elementCast(Node node) {
    if (node instanceof Element)
      return (Element)node;
    System.out.println("Config xml parsing error, expected element "
                       +node.getNodeName());
    return null; // throw exception?
  }

  private void parseDataAdapter(Node node) {
    if (!node.getNodeName().equals("data-adapter")) return;
    String name = getNameAttribute(node);
    // do some other way? DataAdapterManager has mapping? DataAdapter has mapping?
    // DataAdapterManager.getAdapter(name)???
    if (name.equalsIgnoreCase("flybase"))
      addDataAdapter(new FlybaseDataAdapter()); // for now...
    else if (name.equalsIgnoreCase("phenoxml"))
      addDataAdapter(new PhenoXmlAdapter());
  }

  private void addDataAdapter(DataAdapterI da) {
    if (dataAdapterList == null)
      dataAdapterList = new ArrayList<DataAdapterI>(3);
    dataAdapterList.add(da);
  }
}
