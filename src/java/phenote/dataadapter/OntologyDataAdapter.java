package phenote.dataadapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

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
      
    loadOboSession(ontology,ontCfg); // throws FileNotFoundEx
    return ontology;
  }

  /** Load obo session with obo edit adapter, unless previously loaded - reuse */
  private void loadOboSession(Ontology o,OntologyConfig oc) throws OntologyException {
    // check cache of ontologies to see if ontology file already loaded
    if (fileIsInCache(oc.getFile())) {
      setOboSessionFromCache(o,oc.getFile());
    }
    else {
      loadOboSessionCheckRepos(o,oc);
    }
  }

  private boolean fileIsInCache(String filename) {
    return fileToOntologyCache.containsKey(filename);
  }

  private void setOboSessionFromCache(Ontology o,String filename)
  throws OntologyException {
    Ontology previousOntol = fileToOntologyCache.get(filename);
    o.setOboSession(previousOntol.getOboSession()); // throws OntEx if error
    System.out.println("obo file already loaded using, obo file from cache "+o.getOboSession());
    o.setTimestamp(previousOntol.getTimestamp());
    o.setSource(previousOntol.getSource());
  }

  /** If repository is configured loads obo from repos if local out of date */
  private void loadOboSessionCheckRepos(Ontology o,OntologyConfig oc)
  throws OntologyException {
    String filename = oc.getFile();
    URL url = findFile(filename); // throws OntologyEx if file not found
    
    // if ontCfg.hasSynchUrl() ?
    // URL synchUrl = ontCfg.getSynchUrl
    if (oc.hasReposUrl()) {
      try {
        URL reposUrl = oc.getReposUrl();//new URL("http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/evidence_code.obo");
        url = checkRepositoryUrl(url,reposUrl,o.getName());

        // to do - if from repos need to load repos into local obo cache!

      } catch (/*MalformedURL & IO*/Exception e) { LOG.error(e); }
    }
    
    loadOboSessionFromUrl(o,url,filename);
//     o.setOboSession(getOboSession(url)); // throws OntEx if error
//     fileToOntologyCache.put(filename,o);
//     File file = new File(url.getFile());
//     long date = file.lastModified();
//     if (date > 0) { // jar files have 0 date???
//       o.setTimestamp(date);
//       o.setSource(file.toString());
//     }
  }

  /** url is either local file or repos url */
  private void loadOboSessionFromUrl(Ontology o, URL url, String filename)
  throws OntologyException {
    //URL url = findFile(filename); // throws OntologyEx if file not found
    o.setOboSession(getOboSession(url)); // throws OntEx if error
    if (filename!=null)
      fileToOntologyCache.put(filename,o); // ??
    File file = new File(url.getFile());
    long date = file.lastModified();
    if (date > 0) { // jar files have 0 date???
      o.setTimestamp(date);
      o.setSource(file.toString());
    }
  }

  private URL checkRepositoryUrl(URL localUrl, URL reposUrl, String ontol)
    throws OntologyException {
    long repos = getOboDate(reposUrl);
    long loc = getOboDate(localUrl); // throws ont ex
    boolean useRepos = false;
    if (repos > loc)
      useRepos = queryUserAboutRepos(ontol);
    if (useRepos) {
      LOG.info("Loading new ontology from repository "+reposUrl);
      return reposUrl;
    }
    return localUrl;
  }

  // is it bad to have a popup from data adapter?
  private boolean queryUserAboutRepos(String ontol) {
    String m = "There is a more current ontology in the repository for "+ontol+".\nWould"
      +" you like to load the new version? (may take a few minutes)";
    int yn = JOptionPane.showConfirmDialog(null,m,"Synch ontology?",
                                           JOptionPane.YES_NO_OPTION,
                                           JOptionPane.QUESTION_MESSAGE);
    return yn == JOptionPane.YES_OPTION;
  }

  /** Get the date of the file from the header of the obo file - just read header dont
      read in whole file. should it also query urlConnection for date first? */
  private long getOboDate(URL oboUrl) throws OntologyException {
    try {
      InputStream is = oboUrl.openStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      // just try first 15 lines? try til hit [Term]?
      for (int i=1; i<=15; i++) {
        String line = br.readLine();
        // eg date: 22:08:2006 15:38
        if (!line.startsWith("date:")) continue;
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd:MM:yyyy HH:mm");
        Date d = dateFormat.parse(line, new ParsePosition(6));
        LOG.debug("date "+d+" for url "+oboUrl+" line "+line);
        br.close();
        if (d == null)
          throw new OntologyException("couldnt parse date "+line);
        return d.getTime();
      }
      throw new OntologyException("No date found in "+oboUrl);
    } catch (IOException e) { throw new OntologyException(e); }
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
      throw new OntologyException("No url to retrieve");//return new OBOSessionImpl();

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
    URL url = findFile(ont.getSource()); // ex
    loadOboSessionFromUrl(ont,url,ont.getSource()); // throws ex
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
