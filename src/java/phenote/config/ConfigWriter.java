package phenote.config;

import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.XmlOptions;
import phenote.config.xml.DataadapterDocument.Dataadapter;
import phenote.config.xml.QueryableDataadapterDocument.QueryableDataadapter;
import phenote.config.xml.LogDocument.Log;
import phenote.config.xml.OboRepositoryDocument.OboRepository;
import phenote.config.xml.OntologyDocument.Ontology;
import phenote.config.xml.PhenoteConfigurationDocument;
import phenote.config.xml.PhenoteConfigurationDocument.PhenoteConfiguration;
import phenote.config.xml.UvicGraphDocument.UvicGraph;

import phenote.dataadapter.DataAdapterI;
import phenote.main.PhenoteVersion;

/** Im realizing now ive done this all wrong - as theres really 2 redundant datamodels
    for the config - the xml beans, and phenotes own and there really should be just
    the xml beans - then you dont have to do all this back & forth between them
    hindsight is 20 20 */
class ConfigWriter {

  private Config config;
  private PhenoteConfiguration phenCfg;

  void writeConfig(Config config, File file) { //throws IOException? {
    this.config = config; // convenience
    PhenoteConfigurationDocument doc = PhenoteConfigurationDocument.Factory.newInstance();
    phenCfg = doc.addNewPhenoteConfiguration();
    phenCfg.setVersion(PhenoteVersion.versionString());
    // namespace??? need to get that working - xsd as a check - dont know how

    addDataAdapters();

    addQueryDataAdapters();

    addLog();

    addUvicGraph();

    addRepository();

    addFields();

    try { // xmlOptions? pretty print
    doc.save(file,getXmlOptions()); } 
    catch (IOException e) { 
      System.out.println("Failed to save config file "+e); }
  }

  private void addDataAdapters() {
    for (DataAdapterConfig dac : config.getAdapConfigs()) {
      addDataAdapter(dac);
    }
  }
  private void addDataAdapter(DataAdapterConfig dac) {
    // ideally config string is the class name (unless its an invalid class??)
    String daClassString = dac.getConfigString(); //getClass().getName();
    Dataadapter daBean = phenCfg.addNewDataadapter();
    daBean.setName(daClassString);
    daBean.setEnable(dac.isEnabled());
  }

  private void addQueryDataAdapters() {
    for (QueryableAdapConfig q : config.getQueryAdapCfgs()) {
      addQueryAdapter(q);
    }
  }

  private void addQueryAdapter(QueryableAdapConfig qac) {
    String qacClassString = qac.getConfigString();
    QueryableDataadapter qdBean = phenCfg.addNewQueryableDataadapter();
    qdBean.setName(qacClassString);
    qdBean.setEnable(qac.isEnabled());
  }  
  private void addLog() {
    Log log = phenCfg.addNewLog();
    log.setConfigFile(config.getLogConfigFile());
  }

  private void addUvicGraph() {
    UvicGraph uvic = phenCfg.addNewUvicGraph();
    uvic.setEnable(config.uvicGraphIsEnabled());
  }

  private void addRepository() {
    if (config.getReposUrlDir() == null) return; // should there be a default
    OboRepository rep = phenCfg.addNewOboRepository();
    rep.setUrlDir(config.getReposUrlDir());
  }

  private void addFields() {
    for (FieldConfig fc : config.getFieldConfigList()) {
      fc.write(phenCfg);
    }
  }

  private XmlOptions getXmlOptions() {
    XmlOptions options = new XmlOptions();
    options.setSavePrettyPrint();
    options.setSavePrettyPrintIndent(2);
    return options;
  }
}
