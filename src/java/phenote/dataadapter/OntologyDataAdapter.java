package phenote.dataadapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.geneontology.oboedit.datamodel.OBOSession;
import org.geneontology.oboedit.datamodel.impl.OBOSessionImpl;
import org.geneontology.dataadapter.DataAdapterException;
import org.geneontology.dataadapter.FileAdapterConfiguration;
import org.geneontology.dataadapter.IOOperation;
import org.geneontology.oboedit.dataadapter.OBOFileAdapter;

import phenote.util.FileUtil;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyManager;
import phenote.config.Config;
import phenote.config.FieldConfig;
import phenote.config.OntologyConfig;

/** is this really a data adapter? - OntologyLoader? this isnt a data adapter
    it doesnt load & commit character data - just loads ontologies. rename OntologyLoader
    for now can stay in dataadapter package 
    this is specifically a OboFileLoader - other kinds of ontology loading may com along*/
public class OntologyDataAdapter {

  private Config config;
  private OntologyManager ontologyManager = OntologyManager.inst();
  private boolean loadingOntologies = false;
  private static final Logger LOG = Logger.getLogger(OntologyDataAdapter.class);

  public OntologyDataAdapter() {
    config = Config.inst();
    if (config.checkForNewOntologies()) {
      new OntologyFileCheckThread().start();
    }
  }

  public void loadOntologies() {
    loadingOntologies = true;
    for (FieldConfig fieldConfig : config.getFieldConfigList()) {
      CharFieldEnum fce = fieldConfig.getCharFieldEnum();
      CharField cf = new CharField(fce);
      if (fieldConfig.hasOntologies()) {
        //cf = new CharField(fce,o);
        for (OntologyConfig oc : fieldConfig.getOntologyConfigList()) {
          try {
            Ontology o = loadOntology(oc);
            cf.addOntology(o);
          } catch (FileNotFoundException e) {
            //System.out.println(e.getMessage()+" ignoring ontology, fix config! ");
            LOG.error(e.getMessage()+" ignoring ontology, fix config! ");
          }
        }
      }
      else {
        cf.setName(fieldConfig.getLabel());
      }
      ontologyManager.addField(cf);
    }
    loadingOntologies = false;
  }


  /** Load up/cache Sets for all ontologies used, anatomyOntologyTermSet
   * and patoOntologyTermSet -- move to dataadapter/OntologyDataAdapter... */
  private Ontology loadOntology(OntologyConfig ontCfg) throws FileNotFoundException {
    Ontology ontology = new Ontology(ontCfg.name);
    if (ontCfg.hasFilter()) // set filter before loading obo session
      ontology.setFilter(ontCfg.getFilter());
    loadOboSession(ontology,ontCfg.ontologyFile); // throws FileNotFoundEx
    return ontology;
  }

  private void loadOboSession(Ontology o,String filename) throws FileNotFoundException {
    URL url = findFile(filename); // throws FileNotFoundEx
    OBOSession oboSession = getOboSession(url);
    o.setOboSession(oboSession);
    File file = new File(url.getFile());
    long date = file.lastModified();
    //System.out.println(" file "+file+" mod "+date+" "+new Date(date));
    if (date > 0) { // jar files have 0 date???
      o.setTimestamp(date);
      o.setSource(file.toString());
    }
  }

  

  /** Look for file in current directory (.) and jar file */
  private URL findFile(String fileName) throws FileNotFoundException {
    return FileUtil.findUrl(fileName);
  }

//     // first try file as is (full path provided)
//     File file = new File(fileName);
//     if (file.exists())
//       return makeUrl(fileName);

//     String oboFileDir = "obo-files/";
//     // try current directory + obo-file dir
//     String currentDir = "./" + oboFileDir + fileName;
//     file = new File(currentDir);
//     if (file.exists())
//       return makeUrl(currentDir);

//     // try jar - hopefully this works... jar files have to have '/' prepended
//     // first try without obo-files dir (in jar)
//     String jarFile = "/" + fileName;
//     URL url = Ontology.class.getResource(jarFile); // looks in jar
//     // 2nd try with obo-files dir in jar file (i used to do it this way)
//     if (url == null) {
//       jarFile = "/" + oboFileDir + fileName;
//       url = Ontology.class.getResource(jarFile); // looks in jar
//     }

//     if (url == null) {
//       throw new FileNotFoundException("No file found for "+fileName);
//     }
//     return url;
//   }
  
//   private URL makeUrl(String file) {
//     try {
//       return new URL("file:"+file);
//     }
//     catch (MalformedURLException e) {
//       //System.out.println("malformed url "+file+" "+e);
//       LOG.error("malformed url "+file+" "+e);
//       return null;
//     }
//   }


  // String -> url to handle web start jar obo files
  private OBOSession getOboSession(URL oboUrl) {
    if (oboUrl == null)
      return new OBOSessionImpl(); // ??

    OBOFileAdapter fa = new OBOFileAdapter();
    FileAdapterConfiguration cfg = new OBOFileAdapter.OBOAdapterConfiguration();
    Collection fileList = new ArrayList();
    fileList.add(oboUrl.toString());
    cfg.setReadPaths(fileList);
    try { // throws data adapter exception
      OBOSession os = (OBOSession)fa.doOperation(IOOperation.READ,cfg,null);
      return os;
    }
    catch (DataAdapterException e) {
      //System.out.println("got data adapter exception: "+e);
      LOG.error("got data adapter exception: "+e);
      return null; // empty session?
    }
  }

  private class OntologyFileCheckThread extends Thread {

    public void run() {

      int checkMilliSecs = config.getOntologyCheckMinutes() * 60000;
      //int checkMilliSecs = 6000;//0.6 * 60000; // debug - 10 secs

      while(true) {
        // sleep in milliseconds
        try { sleep(checkMilliSecs); }
        catch (InterruptedException e) { LOG.error("thread interrupted??"); }

        // if still loading ontologies from previous run then dont bother
        if (loadingOntologies) {
          //System.out.println("Ontologies are being loaded - ontology checker going "+
          //                 "back to sleep");
          LOG.info("Ontologies are being loaded - ontology checker going "+
                   "back to sleep");
          continue;
        }
        LOG.info("checking for new obo files..."); 
        //System.out.println("checking for new obo files...");
        // check for files...
        synchOntologies();
      }
    }
    
    /** Checks for new obo files */
    private void synchOntologies() {
      for (CharField cf : ontologyManager.getCharFieldList()) {
        for (Ontology o : cf.getOntologyList()) {
          if (o.getSource() == null) continue;
          String file = o.getSource();
          long oldTimestamp = o.getTimestamp();
          long newTimestamp = new File(file).lastModified();
          if (newTimestamp > oldTimestamp) {
            Date d = new Date(newTimestamp);
            //System.out.println("loading new obo file "+file+" new date "+d);
            LOG.info("LOG loading new obo file "+file+" new date "+d);
            try {
              loadOboSession(o,file);
            } catch (FileNotFoundException e) { // shouldnt happen
              //System.out.println(e.getMessage()+" ignoring ontology, fix config! ");
              LOG.error(e.getMessage()+" ignoring ontology, fix config! ");
              // LOG.debug(stacktrace)??? no string for stack trace... hmm...
            }
          }
        }
      }
    }
  }

}




// GARBAGE
//     if (date > 0) { // jar files have 0 date???
//       ontology.setTimestamp(date);
//       ontology.setSource(file.toString());
//     }
//     URL url = findFile(ontCfg.ontologyFile); // throws FileNotFoundEx
//     File file = new File(url.getFile());
//     long date = file.lastModified();
//     System.out.println("url path "+url.getPath()+" file "+file+" mod "+date+" "+new Date(date));
//     OBOSession oboSession = getOboSession(url);
//     Ontology pato = loadOntology(config.getPatoOntologyConfig());
//     ontologyManager.setPatoOntology(pato);

//     if (config.hasLumpOntology()) {
//       Ontology lump = loadOntology(config.getLumpOntologyConfig());
//       ontologyManager.setLumpOntology(lump);
//     }
    
//     List<OntologyConfig> entities = config.getEntityOntologyConfigs();
//     Iterator<OntologyConfig> it = entities.iterator();
//     while(it.hasNext()) {
//       Ontology o = loadOntology(it.next());
//       ontologyManager.addEntityOntology(o);
//     }

//     if (config.hasGeneticContextField()) {
//       FieldConfig fc = config.getGeneticContextConfig();
//       Ontology o = loadOntology(fc.getOntologyConfig());
//       //ontologyManager.setGeneticContextOntology(o);
//       // alternatively... ... // or (new CharField(cfe,o)) ??
//       //ontologyManager.addOntology(fc.getCharFieldEnum(),o);
//       ontologyManager.addField(new CharField(fc.getCharFieldEnum(),o));
//     }
