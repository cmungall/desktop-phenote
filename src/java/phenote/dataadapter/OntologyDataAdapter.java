package phenote.dataadapter;

import java.io.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Channels;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JComponent;
import java.awt.Component;

import org.apache.log4j.Logger;

import org.geneontology.dataadapter.DataAdapterException;
import org.geneontology.dataadapter.FileAdapterConfiguration;
import org.geneontology.dataadapter.IOOperation;

import org.geneontology.oboedit.datamodel.Namespace;
import org.geneontology.oboedit.datamodel.OBOSession;
import org.geneontology.oboedit.datamodel.impl.OBOSessionImpl;
import org.geneontology.oboedit.dataadapter.OBOFileAdapter;
import org.geneontology.oboedit.dataadapter.OBOMetaData;

import phenote.util.FileUtil;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyException;
import phenote.datamodel.OntologyManager;
import phenote.config.Config;
import phenote.config.FieldConfig;
import phenote.config.OntologyConfig;
import phenote.gui.SynchOntologyDialog;

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
  private OBOMetaData adapterMetaData;
  private SynchOntologyDialog synchDialog;
  
  private static final Logger LOG = Logger.getLogger(OntologyDataAdapter.class);

  // does this need to be false for zfin
  private static final boolean DO_ONE_OBO_SESSION = true;

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

  // public void setDoOneOboSession(boolean doOneOboSession) ??

  private void initOntologies() {
    // to prevent reload during init , maybe dont need with synchronization?
    initializingOntologies = true; 

    // new paradigm
    if (DO_ONE_OBO_SESSION) {
      initOntolsOneOboSession();
    }
    else {
      initOntolsSeparateOboSessions();
    }
    initializingOntologies = false;
  }

  private void initOntolsSeparateOboSessions() {
    // getFieldConfigList gives enabled fields - not disabled
    for (FieldConfig fieldConfig : config.getFieldConfigList()) {
      if (!fieldConfig.isEnabled()) continue; // not necasary actually
      CharField cf = new CharField(fieldConfig.getLabel());
      fieldConfig.setCharField(cf);

      // ONTOLOGIES
      if (fieldConfig.hasOntologies()) {
        for (OntologyConfig oc : fieldConfig.getOntologyConfigList()) {
          try {
            Ontology o = initOntology(oc); // LOAD OBO SESSION
            if (oc.isPostCompRel()) { // POST COMP REL ONTOLOGY
              cf.setPostCompAllowed(true);
              cf.setPostCompRelOntol(o);
            }
            else { // REGULAR ONTOLOGY
              cf.addOntology(o);
            }
          } catch (OntologyException e) {
            LOG.error(e.getMessage()+" ignoring init ontology, fix config? "+oc);
          }
        }
      }
      else {

        cf.setName(fieldConfig.getLabel());
      }
      // i think this order needs to be same as config order
      ontologyManager.addField(cf);
    }
  }


  private void initOntolsOneOboSession() {
    try {
      OBOSession os = loadAllOboFilesIntoOneOboSession();
      initCharFields();
      // map namespaces to ontologies? 2 methods?
      mapNamespacesToOntologies(os);
      // load ontologies from namespaces
    }
    catch (OntologyException e)  { // parse ex - not file not found ex
      LOG.error(e.getMessage()+" unable to load ontologies - yikes");
    }


  }


  /** new paradigm - rather than one obo session per obo file - have one obo 
      session for all - this is possible with indexed namespaces for searching
      also need to record meta data from load to map obo files to namespaces */
  private OBOSession loadAllOboFilesIntoOneOboSession() throws OntologyException {
    // get unique list of obo files
    Collection<String> files = new ArrayList<String>();
    for (FieldConfig fieldConfig : config.getFieldConfigList()) {
      for (OntologyConfig oc : fieldConfig.getOntologyConfigList()) {
        try {
          String file = findOboUrlString(oc); // throws oex if not found
          files.add(file);
        }
        catch (OntologyException e) {
          LOG.warn(e.getMessage()+" Please check config & obo files");
        }
      }
    }
    // should we have singleOboSession instance var?? or confusing with multi?
    OBOSession os = getOboSession(files);
    return os;
  }

  // create char fields - add to ont manager? or do in mapNamespace?
  // should Config actually set OntologyManager up with char fields - maybe this shouldnt
  // be ont data adapters responsibility - its just the 1st time that we need it
  private void initCharFields() {
    for (FieldConfig fieldConfig : config.getFieldConfigList()) {
      CharField cf = fieldConfig.getCharField(); // creates char field (if not there)
      ontologyManager.addField(cf);
    }
  }

  /** This actually creates both CharFields and Ontologies and maps namespaces from
      obo file adapter meta data */
  private void mapNamespacesToOntologies(OBOSession oboSession) throws OntologyException {
    for (FieldConfig fieldConfig : config.getFieldConfigList()) {
      CharField cf = fieldConfig.getCharField(); // creates char field (if not there)
      // ontology manager.addCF???
      if (fieldConfig.hasOntologies()) {
        for (OntologyConfig oc : fieldConfig.getOntologyConfigList()) {
          // get namespaces for ont cfg
          // if adapterMetaData == null print err? throw ex? return?
          if (adapterMetaData == null)
            throw new OntologyException("No namespace/meta data for ontologies");
          // i think we need url it was loaded with
          if (!oc.hasLoadUrl()) {
            LOG.error("Failed to find obo for "+oc.name+" can not load");
            // in case log not set up right - need to work on that
            System.out.println("Failed to find obo for "+oc.name+" can not load");
            continue;
          }
          String urlString = oc.getLoadUrl().toString();
          Collection<Namespace> spaces = adapterMetaData.getNamespaces(urlString);
          // loads ontology from spaces
          Ontology o = new Ontology(spaces,oc,oboSession);

          // ADD TO CHAR FIELD
          if (oc.isPostCompRel()) { // POST COMP REL ONTOLOGY
            cf.setPostCompAllowed(true);
            cf.setPostCompRelOntol(o);
          }
          else { // REGULAR ONTOLOGY
            cf.addOntology(o);
          }
        }
      }
    }    
  }


  /** Load up/cache Sets for all ontologies used, anatomyOntologyTermSet
   * and patoOntologyTermSet -- move to dataadapter/OntologyDataAdapter... */
  private Ontology initOntology(OntologyConfig ontCfg) throws OntologyException {
    Ontology ontology = new Ontology(ontCfg.name); // new Ontology(ontCfg)?
    if (ontCfg.hasFilter()) // set filter before loading obo session
      ontology.setFilter(ontCfg.getFilter());
    if (ontCfg.hasSlim())
      ontology.setSlim(ontCfg.getSlim());
      
    loadOboSession(ontology,ontCfg); // throws FileNotFoundEx->OntolEx
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

  /** if configged checks if repos has more recent - if so copies to cache
      if no repos gets from cache(.phenote/obo-files), if no cache gets from app/obo-files
      throws ontology ex if fails to find in
      url/repos, .phenote/obo-files and app/obo-files */
  private String findOboUrlString(OntologyConfig ontCfg) throws OntologyException {
    return findOboUrl(ontCfg).toString();
  }
  

  /** if configged checks if repos has more recent - if so copies to cache
      if no repos gets from cache(.phenote/obo-files), if no cache gets from app/obo-files
      throws ontology ex if fails to find in
      url/repos, .phenote/obo-files and app/obo-files */
  private URL findOboUrl(OntologyConfig ontCfg) throws OntologyException {
    // first get local url (if there is one) to use to compare against repos url
    // get normal/cached/local ontology
    String filename = ontCfg.getFile();
    // throws OntologyEx if file not found -- catch & try url
    URL url = null;
    try { url = findFile(filename); }
    catch (OntologyException oe) {
      System.out.println(filename+" not found locally, trying url if configured ");
    }

    // if repos url configured then check if its more up to date - if so copy
    // repos obo file to .phenote cache
    if (ontCfg.hasReposUrl()) {
      try {
        URL reposUrl = ontCfg.getReposUrl(); // throws MalfUrlEx
        //long mem = Runtime.getRuntime().totalMemory()/1000000; //startTimer();
        LOG.debug(reposUrl+" checking with repos for newer ontol\n");
        // if out of synch copies repos to local(.phenote/obo-files)
        // url may be jar/obo-files or svn/obo-files but this function will put file
        // in cache ~/.phenote/obo-files
        url = synchWithRepositoryUrl(url,reposUrl,ontCfg.name,filename);
      } catch (/*MalfURL & Ontol*/Exception e) { LOG.error(e); }
    }
 
    if (url == null) 
      throw new OntologyException("obo file "+filename+" not found in repos nor local");

    ontCfg.setLoadUrl(url);

    return url;
   
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

//     // first get normal/cached/local ontology
//     String filename = oc.getFile();
//     // throws OntologyEx if file not found -- need to catch - should still try url
//     URL url = null;
//     try { url = findFile(filename); }
//     catch (OntologyException oe) {
//       System.out.println(filename+" not found locally, trying url if configured ");
//     }

//     // if ontCfg.hasSynchUrl() ?
//     // URL synchUrl = ontCfg.getSynchUrl
//     if (oc.hasReposUrl()) {
//       try {
//         URL reposUrl = oc.getReposUrl();
//         // if out of synch copies repos to local(.phenote/obo-files)
//         // url may be jar/obo-files or svn/obo-files but this function may put file
//         // in cache ~/.phenote/obo-files
//         url = synchWithRepositoryUrl(url,reposUrl,o.getName(),filename);
        
//         // to do - if from repos need to load repos into local obo cache!

//       } catch (/*MalformedURL & IO*/Exception e) { LOG.error(e); }
//     }

    URL url = findOboUrl(oc);

    long mem = Runtime.getRuntime().totalMemory()/1000000;
    LOG.debug(url+" checking with repos... loading obo session mem "+mem+"\n");
    startTimer();
    
    loadOboSessionFromUrl(o,url,oc.getFile());
    stopTimer(url+" checked against repos... obo session loaded");
    mem = Runtime.getRuntime().totalMemory()/1000000;
    long max = Runtime.getRuntime().maxMemory()/1000000;
    LOG.debug("mem after load "+mem+" max "+max);
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

  /** this copies obo file to local cache (~/.phenote/obo-files */
  private URL synchWithRepositoryUrl(URL localUrl, URL reposUrl, String ontol,
                                     String filename)
    throws OntologyException {

    boolean useRepos = false;
    if (localUrl == null) {
      useRepos = true;
    }
    else {
      long repos = getOboDate(reposUrl); // throws ex if no date
      long loc = 0;
      int timer = config.getUpdateTimer();
      boolean autoUpdate = config.autoUpdateIsEnabled();
      if (localUrl != null)
        loc = getOboDate(localUrl); // throws ont ex
      else
        useRepos = true;
      //if autoupdate without popup
      if ((autoUpdate && (timer==0)) && (repos > loc)) {
    	useRepos = true;
      } else if (repos > loc || useRepos) {
        useRepos = synchDialog.queryUserForOntologyUpdate(ontol);
      }
    }
    if (useRepos) {

      // i think its always better to download as http/repos is slow
//       boolean downloadToLocal = false; // from Config!
//       if (!downloadToLocal) {
//         LOG.info("Using obo file straight from repository "+reposUrl+
//                  " NOT downloading to local cache");
//         return reposUrl; // just use straight from repos
//       }

      // download obo to local cache (takes time!)
      String file = localUrl!=null ? FileUtil.getNameOfFile(localUrl) : filename;
      //String file = FileUtil.getNameOfFile(localUrl);
      try { 
        localUrl = new File(FileUtil.getDotPhenoteOboDir(),file).toURL();
        LOG.info("Downloading new ontology from repository "+reposUrl+" to "+localUrl);
        copyReposToLocal(reposUrl,localUrl);
      }
      catch (MalformedURLException e) { throw new OntologyException(e); }
    }
    return localUrl;
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
        //LOG.debug("date "+d+" for url "+oboUrl+" line "+line);
        br.close();
        if (d == null)
          throw new OntologyException("couldnt parse date "+line);
        return d.getTime();
      }
      throw new OntologyException("No date found in "+oboUrl);
    } catch (IOException e) { throw new OntologyException(e); }
  }


  /** local url may be from distrib/jar dir, but needs to be set to 
      .phenote/obo-files dir as thats where the user cache is */
  private void copyReposToLocal(URL reposUrl, URL localUrl)
    throws OntologyException {
    // is there a better way then just reading & writing lines? nio?
    try {
      // nio & old io do the same time - oh well (~1 minute for GO)
      //startTimer("nio file copy "+reposUrl+" to "+localUrl);
      InputStream is = reposUrl.openStream();
      // nio actually does the same with the buffer
      //BufferedInputStream bis = new BufferedInputStream(is);
      ReadableByteChannel r = Channels.newChannel(is);
      //BufferedReader br = new BufferedReader(new InputStreamReader(is));
      File f = new File(localUrl.getFile());
      FileOutputStream fos = new FileOutputStream(f);
      //BufferedOutputStream bos = new BufferedOutputStream(fos);
      //OutputStreamWriter osw = new OutputStreamWriter(fos);
      //FileWriter fw = new FileWriter(f);
      //BufferedWriter bw = new BufferedWriter(fw);
      //PrintStream ps = new PrintStream(bos);
      FileChannel w = fos.getChannel();
      long size = 99999999999999l; // cant get actual size - whole file
      w.transferFrom(r,0,size);
      //byte[] buf = new byte[1024];
//       int i = 0;
//       //while((i=br.read())!=-1) {
//       String line;
//       while ((line=br.readLine())!=null) {
//         //bos.write(i); //buf, 0, i);
//         ps.print(line+"\n");
//       }
      //stopTimer();
      r.close();
      is.close(); // ??
      w.close();
      fos.close(); // ??
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
      throw new OntologyException("No url to retrieve");

    Collection fileList = new ArrayList();
    fileList.add(oboUrl.toString());
    return getOboSession(fileList);
  }

  /** file list is a list of file/url Strings */
  private OBOSession getOboSession(Collection<String> fileList)
    throws OntologyException {
    OBOFileAdapter fa = new OBOFileAdapter();
    FileAdapterConfiguration cfg = new OBOFileAdapter.OBOAdapterConfiguration();
    // takes strings not urls!
    cfg.setReadPaths(fileList);
    try { // throws data adapter exception
      OBOSession os = (OBOSession)fa.doOperation(IOOperation.READ,cfg,null);
      adapterMetaData = fa.getMetaData(); // check for null?
      return os;
    }
    catch (DataAdapterException e) {
      LOG.error("got obo data adapter exception: "+e); // ??
      throw new OntologyException(e);
    }
  }


  /** The ontology has been determined to be out of date (by quartz) and thus directed
      to reload itself from its file - in other words there is a new obo file to load 
      in place of old one */
//   public void reloadOntology(Ontology ont) throws OntologyException {
//     URL url = findFile(ont.getSource()); // ex
//     loadOboSessionFromUrl(ont,url,ont.getSource()); // throws ex
//   }
  public void reloadOntologies() throws OntologyException {
    fileToOntologyCache = new HashMap<String,Ontology>();
    // i dont think we need to clear out ontologies from OntMans char field as they
    // will just get replaced
    initOntologies();
  }

  // eventually move to util class
  private Calendar startTime;
  //private String timerMsg;
  private void startTimer() {
    startTime = Calendar.getInstance();
    //timerMsg = m;
    //LOG.debug(timerMsg+" Start clock "+startTime.getTime()); // ??
  }

  private void stopTimer(String m) {
    Calendar endTime = Calendar.getInstance();
    long seconds = (endTime.getTimeInMillis() - startTime.getTimeInMillis())/1000;
    LOG.debug(m+" number of seconds: "+seconds);
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
//       // POST COMP
//       if (fieldConfig.isPostComp()) {
//         cf.setPostCompAllowed(true);
//         try {
//           //Ontology o = initRelationshipOntology(fieldConfig.getPostCompRelOntCfg());
//           Ontology o = initOntology(fieldConfig.getPostCompRelOntCfg());
//           cf.setPostCompRelOntol(o);
//         } catch (OntologyException e) {
//           LOG.error(e.getMessage()+" ignoring ontology, fix config? ");
//         }
//       }
      // i think this order needs to be same as config order
    //initRelationshipOntology();
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
