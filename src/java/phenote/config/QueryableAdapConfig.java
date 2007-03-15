package phenote.config;

import phenote.config.xml.QueryableDataadapterDocument.QueryableDataadapter;

import phenote.dataadapter.QueryableDataAdapterI;

class QueryableAdapConfig extends DataAdapterConfig {

  private QueryableDataAdapterI queryableDataAdapter;

  QueryableAdapConfig(QueryableDataadapter xmlBean) {
    // silly cut&paste - QueryableDA has same interface as DA but no way to declare?x
    name = xmlBean.getName();
    configString = xmlBean.getName(); // must be set for setDAFromCfgStr
    
    // yes - this is a way of checking if flag actually specified - nice
    // if not specified default is true actually not false
    if (xmlBean.xgetEnable() != null)
      enabled = xmlBean.getEnable();
    
    if (enabled)
      setDataAdapterFromConfigString();
  }

  /** check if enabled/constructed? */
  QueryableDataAdapterI getQueryableAdapter() { return queryableDataAdapter; }

  private void setDataAdapterFromConfigString() {
      
    // new way of doing things is class name itself - so 1st try introspect...
    try {
      Class c = Class.forName(configString);
      Object o = c.newInstance();
      if ( ! (o instanceof QueryableDataAdapterI))
        throw new Exception("class not instance of QueryableDataAdapterI");
      QueryableDataAdapterI da = (QueryableDataAdapterI)o;
      //addDataAdapter(da);
      queryableDataAdapter = da;
      configStringIsClassName = true;
    }
    catch (Exception e) {
      System.out.println("Failed to retrieve queryable data adapter "+configString);
      enabled = false;
    }
  }

}
