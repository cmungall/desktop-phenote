package phenote.config;

import phenote.config.xml.DataadapterDocument.Dataadapter;
//should this be:
//import phenote.dataadapter.*;
//so that programmers don't have to modify this file?

import phenote.dataadapter.DataAdapterI;
import phenote.dataadapter.QueryableDataAdapterI;
import phenote.dataadapter.fly.FlybaseDataAdapter;
import phenote.dataadapter.nexus.NEXUSAdapter;
import phenote.dataadapter.phenosyntax.PhenoSyntaxFileAdapter;
import phenote.dataadapter.phenoxml.PhenoXmlAdapter;
import phenote.dataadapter.delimited.DelimitedFileAdapter;

class DataAdapterConfig {

  //String name;
  //boolean enabled=true; // enabled by default
  // new -> class name, old -> phenoxml|phenosyntax|nexus 
  //String configString;
  //boolean configStringIsClassName = false;
  // will be null if enabled = false
  private DataAdapterI fileAdapter;
  private QueryableDataAdapterI queryableDataAdapter;
  private Dataadapter xmlBean;

  /** construct from Dataadapter xml bean */
  DataAdapterConfig(Dataadapter xmlBean) {
    this.xmlBean = xmlBean;
    //name = xmlBean.getName();
    //configString = xmlBean.getName(); // must be set for setDAFromCfgStr
    
    // this is a way of checking if flag actually specified - nice
    // if not specified default is true actually not false
//     if (xmlBean.xgetEnable() != null)
//       enabled = xmlBean.getEnable();
    
    if (isEnabled())
      setDataAdapterFromName();
  }

  protected DataAdapterConfig() {}; // for subclass hmmm

  String getName() { return xmlBean.getName(); }
  private void setName(String name) { xmlBean.setName(name); }

  boolean isQueryable() { return xmlBean.getIsQueryable(); }
  /** Either its a file adapter or its a queryable/database adapter */
  boolean isFileAdapter() { return !isQueryable(); }

  /** default is true */
  boolean isEnabled() { 
    if (xmlBean.xgetEnable() == null)
      xmlBean.setEnable(true);
    return xmlBean.getEnable();
  }
  
  /** check if enabled/constructed? */
  DataAdapterI getFileAdapter() { return fileAdapter; }

  boolean hasSameAdapter(DataAdapterConfig dac) {
    return getName().equals(dac.getName());
    //return getConfigString().equals(dac.getConfigString());
  }

   QueryableDataAdapterI getQueryableAdapter() { return queryableDataAdapter; }

  // do some other way? DataAdapterManager has mapping? DataAdapter has mapping?
  // DataAdapterManager.getAdapter(name)???
  // just do class string see tracker issue 1649004
  private void setDataAdapterFromName() {
    try {
      if (!isQueryable())
        setFileAdapterFromName(); // throws config ex
      else
       setQueryableAdapterFromName();
    }
    catch (ConfigException e) {
      System.out.println(e.getMessage()+" trying queryable");
      setQueryableAdapterFromName(); // why not
    }
     
  }
  
  private void setFileAdapterFromName() throws ConfigException {
    // new way of doing things is class name itself - so 1st try introspect...
    try {
      Object o = getInstanceForName();
      if ( ! (o instanceof DataAdapterI))
        throw new Exception("class not instance of DataAdapterI");
      fileAdapter = (DataAdapterI)o;
      //configStringIsClassName = true;
    }
    catch (Exception e) { 
        
      // backward compatibility, merge/update replaces these with class names
      // PLEASE DONT ADD NEW ADAPTERS TO THIS LIST - FORCE THEM TO USE NEW WAY
      if (getName().equalsIgnoreCase("phenoxml"))
        fileAdapter = new PhenoXmlAdapter();
      else if (getName().equalsIgnoreCase("phenosyntax"))
        fileAdapter = new PhenoSyntaxFileAdapter();
      else if (getName().equalsIgnoreCase("flybase")) // pase??
        fileAdapter = new FlybaseDataAdapter(); // for now...
      else if (getName().equalsIgnoreCase("nexus"))
        fileAdapter = new NEXUSAdapter();
      else if (getName().equalsIgnoreCase("delimited"))
    	fileAdapter = new DelimitedFileAdapter();
      else // LOG not set up yet???
        throw new ConfigException("File adapter not recognized "+getName());

      //configStringIsClassName = false;
      setName(fileAdapter.getClass().getName());
    }

  }

  private void setQueryableAdapterFromName() {
    try {
      Object o = getInstanceForName();
      if ( ! (o instanceof QueryableDataAdapterI))
        throw new Exception("class not instance of QueryableDataAdapterI");
      queryableDataAdapter = (QueryableDataAdapterI)o;
    }
    catch (Exception e) {
      System.out.println("Queryable data adapter not recognized "+getName());
    }
  }

  private Object getInstanceForName() throws Exception {
    Class c = Class.forName(getName()); //configString);
    return c.newInstance();
  }

}

//   String getConfigString() {
//     // attempt to make class string (even if not enabled) - flag for attempt made?
//     if (!configStringIsClassName) {
//       if (fileAdapter == null)
//         setDataAdapterFromConfigString();
//       if (fileAdapter != null)
//         configString = fileAdapter.getClass().getName();
//     }
//     return configString;
//   }
