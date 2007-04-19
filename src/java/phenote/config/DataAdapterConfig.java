package phenote.config;

import phenote.config.xml.DataadapterDocument.Dataadapter;
//should this be:
//import phenote.dataadapter.*;
//so that programmers don't have to modify this file?

import phenote.dataadapter.DataAdapterI;
import phenote.dataadapter.fly.FlybaseDataAdapter;
import phenote.dataadapter.nexus.NEXUSAdapter;
import phenote.dataadapter.phenosyntax.PhenoSyntaxFileAdapter;
import phenote.dataadapter.phenoxml.PhenoXmlAdapter;
import phenote.dataadapter.delimited.DelimitedFileAdapter;

class DataAdapterConfig {

  String name;
  boolean enabled=true; // enabled by default
  // new -> class name, old -> phenoxml|phenosyntax|nexus 
  String configString;
  boolean configStringIsClassName = false;
  // will be null if enabled = false
  DataAdapterI dataAdapter;

  /** construct from Dataadapter xml bean */
  DataAdapterConfig(Dataadapter xmlBean) {
    name = xmlBean.getName();
    configString = xmlBean.getName(); // must be set for setDAFromCfgStr
    
    // yes - this is a way of checking if flag actually specified - nice
    // if not specified default is true actually not false
    if (xmlBean.xgetEnable() != null)
      enabled = xmlBean.getEnable();
    
    if (enabled)
      setDataAdapterFromConfigString();
  }

  protected DataAdapterConfig() {}; // for subclass hmmm

  String getName() { return name; }

  boolean isEnabled() { return enabled; }
  
  /** check if enabled/constructed? */
  DataAdapterI getDataAdapter() { return dataAdapter; }

  boolean hasSameAdapter(DataAdapterConfig dac) {
    if (name.equals(dac.getName())) return true;
    return getConfigString().equals(dac.getConfigString());
  }

  String getConfigString() {
    // attempt to make class string (even if not enabled) - flag for attempt made?
    if (!configStringIsClassName) {
      if (dataAdapter == null)
        setDataAdapterFromConfigString();
      if (dataAdapter != null)
        configString = dataAdapter.getClass().getName();
    }
    return configString;
  }

  // do some other way? DataAdapterManager has mapping? DataAdapter has mapping?
  // DataAdapterManager.getAdapter(name)???
  // just do class string see tracker issue 1649004
  private void setDataAdapterFromConfigString() {
      
    // new way of doing things is class name itself - so 1st try introspect...
    try {
      Class c = Class.forName(configString);
      Object o = c.newInstance();
      if ( ! (o instanceof DataAdapterI))
        throw new Exception("class not instance of DataAdapterI");
      DataAdapterI da = (DataAdapterI)o;
      //addDataAdapter(da);
      dataAdapter = da;
      configStringIsClassName = true;
    }
    catch (Exception e) { 
        
      // backward compatibility - have merger replace these with class names eventually
      if (configString.equalsIgnoreCase("phenoxml"))
        dataAdapter = new PhenoXmlAdapter();
      else if (configString.equalsIgnoreCase("phenosyntax"))
        dataAdapter = new PhenoSyntaxFileAdapter();
      else if (configString.equalsIgnoreCase("flybase")) // pase??
        dataAdapter = new FlybaseDataAdapter(); // for now...
      else if (configString.equalsIgnoreCase("nexus"))
        dataAdapter = new NEXUSAdapter();
      else if (configString.equalsIgnoreCase("delimited"))
    	dataAdapter = new DelimitedFileAdapter();
      // LOG not set up yet???
      else
        System.out.println("Data adapter not recognized "+configString);
      configStringIsClassName = false;
    }
  }
}

