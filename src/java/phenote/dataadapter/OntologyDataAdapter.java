package phenote.dataadapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import phenote.datamodel.OntologyException;
import phenote.datamodel.OntologyManager;
import phenote.config.Config;
import phenote.config.FieldConfig;
import phenote.config.OntologyConfig;

/** is this really a data adapter? - OntologyLoader? this isnt a data adapter
    it doesnt load & commit character data - just loads ontologies. rename OntologyLoader
    for now can stay in dataadapter package 
    this is specifically a OboFileLoader - other kinds of ontology loading may com along*/
public class OntologyDataAdapter {

  private static OntologyDataAdapter singleton;
  private Config config;
  private OntologyManager ontologyManager = OntologyManager.inst();
  private boolean initializingOntologies = false;
  private Map<String,Ontology> fileToOntologyCache = new HashMap<String,Ontology>();
  
  private static final Logger LOG = Logger.getLogger(OntologyDataAdapter.class);

  private OntologyDataAdapter() {
    config = Config.inst();
    initOntologies(); // loads up all ontologies
    //if (config.checkForNewOntologies()){new OntologyFileCheckThread().start();}
  }

  /** synchronized so cant reload an ontology while ontologies are being initialized 
   getInstance calls initOntologies - in other words you have to call getInstance to
   initialize the ontologies */
  public static synchronized OntologyDataAdapter getInstance() {
    if (singleton == null)
      singleton = new OntologyDataAdapter();
    return singleton;
  }

  /** just calls getInstance - but more intuitive for initialization */
  public static synchronized void initialize() {
    getInstance();
  }

  private void initOntologies() {
    // to prevent reload during init , maybe dont need with synchronization?
    initializingOntologies = true; 
    for (FieldConfig fieldConfig : config.getFieldConfigList()) {
      // may not have char field enum!
      CharField cf;
      // this is where char field enums happen - scrap entirely????
//       if (fieldConfig.hasCharFieldEnum()) {
//         CharFieldEnum fce = fieldConfig.getCharFieldEnum();
//         cf = new CharField(fce);
//       }
      cf = new CharField(fieldConfig.getLabel());
      fieldConfig.setCharField(cf);

      // ONTOLOGIES
      if (fieldConfig.hasOntologies()) {
        for (OntologyConfig oc : fieldConfig.getOntologyConfigList()) {
          try {
            Ontology o = initOntology(oc);
            cf.addOntology(o);
          } catch (OntologyException e) {
            //System.out.println(e.getMessage()+" ignoring ontology, fix config! ");
            LOG.error(e.getMessage()+" ignoring init ontology, fix config? ");
          }
        }
      }
      else {
        cf.setName(fieldConfig.getLabel());
      }

      // POST COMP
      if (fieldConfig.isPostComp()) {
        cf.setPostCompAllowed(true);
        try {
          //Ontology o = initRelationshipOntology(fieldConfig.getPostCompRelOntCfg());
          Ontology o = initOntology(fieldConfig.getPostCompRelOntCfg());
          cf.setPostCompRelOntol(o);
        } catch (OntologyException e) {
          LOG.error(e.getMessage()+" ignoring ontology, fix config? ");
        }
      }
      ontologyManager.addField(cf);
    }
    //initRelationshipOntology();
    
    initializingOntologies = false;
  }

  /** Load up/cache Sets for all ontologies used, anatomyOntologyTermSet
   * and patoOntologyTermSet -- move to dataadapter/OntologyDataAdapter... */
  private Ontology initOntology(OntologyConfig ontCfg) throws OntologyException {
    Ontology ontology = new Ontology(ontCfg.name);
    if (ontCfg.hasFilter()) // set filter before loading obo session
      ontology.setFilter(ontCfg.getFilter());
    if (ontCfg.hasSlim())
      ontology.setSlim(ontCfg.getSlim());
      
    loadOboSession(ontology,ontCfg.getFile()); // throws FileNotFoundEx
    return ontology;
  }

  /** Load obo session with obo edit adapter, unless previously loaded - reuse */
  private void loadOboSession(Ontology o,String filename) throws OntologyException {
    // check cache of ontologies to see if ontology file already loaded
    if (fileIsInCache(filename)) {
      loadOboSessionFromCache(o,filename);
    }
    else {
      loadOboSessionIgnoreCache(o,filename);
    }
  }

  private boolean fileIsInCache(String filename) {
    return fileToOntologyCache.containsKey(filename);
  }

  private void loadOboSessionFromCache(Ontology o,String filename)
  throws OntologyException {
    Ontology previousOntol = fileToOntologyCache.get(filename);
    o.setOboSession(previousOntol.getOboSession()); // throws OntEx if error
    System.out.println("obo file already loaded using, obo file from cache "+o.getOboSession());
    o.setTimestamp(previousOntol.getTimestamp());
    o.setSource(previousOntol.getSource());
  }

  private void loadOboSessionIgnoreCache(Ontology o,String filename)
  throws OntologyException {
    URL url = findFile(filename); // throws OntologyEx if file not found
    o.setOboSession(getOboSession(url)); // throws OntEx if error
    fileToOntologyCache.put(filename,o);
    File file = new File(url.getFile());
    long date = file.lastModified();
    if (date > 0) { // jar files have 0 date???
      o.setTimestamp(date);
      o.setSource(file.toString());
    }
  }
  

  /** Look for file in current directory (.) and jar file 
      throws OntologyException if file not found - wraps FileNFEx */
  private URL findFile(String fileName) throws OntologyException {
    try { return FileUtil.findUrl(fileName); }
    catch (FileNotFoundException e) { throw new OntologyException(e); }
  }



  // String -> url to handle web start jar obo files
  private OBOSession getOboSession(URL oboUrl) throws OntologyException {
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
      LOG.error("got data adapter exception: "+e); // ??
      throw new OntologyException(e);
    }
  }

  /** The ontology has been determined to be out of date (by quartz) and thus directed
      to reload itself from its file - in other words there is a new obo file to load 
      in place of old one */
  public void reloadOntology(Ontology ont) throws OntologyException {
    loadOboSessionIgnoreCache(ont,ont.getSource()); // throws ex
  }

//   private void loadRelationshipOntology() { hmmmmmm
//     // for now - todo configure! post comp relationship-ontology
//     // FieldConfig rfc = config.getRelationshipFieldConfig();
//     //CharFieldEnum relEnum = rfc.getCharFieldEnum();
//     CharFieldEnum relEnum = CharFieldEnum.RELATIONSHIP;
//     CharField cf = new CharField(relEnum);
//   }


}




// GARBAGE
  // refactor -> Quartz scheduler, reloadOntology()
//   private class OntologyFileCheckThread extends Thread {

//     public void run() {

//       int checkMilliSecs = config.getOntologyCheckMinutes() * 60000;
//       //int checkMilliSecs = 6000;//0.6 * 60000; // debug - 10 secs

//       while(true) {
//         // sleep in milliseconds
//         try { sleep(checkMilliSecs); }
//         catch (InterruptedException e) { LOG.error("thread interrupted??"); }

//         // if still loading ontologies from previous run then dont bother
//         if (initializingOntologies) {
//           //System.out.println("Ontologies are being loaded - ontology checker going "+
//           //                 "back to sleep");
//           LOG.info("Ontologies are being initialized - ontology checker going "+
//                    "back to sleep");
//           continue;
//         }
//         LOG.info("checking for new obo files..."); 
//         //System.out.println("checking for new obo files...");
//         // check for files...
//         synchOntologies();
//       }
//     }
    
//     /** Checks for new obo files */
//     private void synchOntologies() {
//       for (CharField cf : ontologyManager.getCharFieldList()) {
//         for (Ontology o : cf.getOntologyList()) {
//           if (o.getSource() == null) continue;
//           String file = o.getSource();
//           long oldTimestamp = o.getTimestamp();
//           long newTimestamp = new File(file).lastModified();
//           if (newTimestamp > oldTimestamp) {
//             Date d = new Date(newTimestamp);
//             //System.out.println("loading new obo file "+file+" new date "+d);
//             LOG.info("LOG loading new obo file "+file+" new date "+d);
//             try {
//               loadOboSession(o,file);
//             } catch (FileNotFoundException e) { // shouldnt happen
//               //System.out.println(e.getMessage()+" ignoring ontology, fix config! ");
//               LOG.error(e.getMessage()+" ignoring ontology, fix config! ");
//               // LOG.debug(stacktrace)??? no string for stack trace... hmm...
//             }
//           }
//         }
//       }
//     }

  
//     if (date > 0) { // jar files have 0 date???
//       ontology.setTimestamp(date);
//       ontology.setSource(file.toString());
//     }
