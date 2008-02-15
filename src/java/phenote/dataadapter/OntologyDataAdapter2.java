package phenote.dataadapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.bbop.dataadapter.DataAdapterException;
import org.obo.dataadapter.OBOAdapter;
import org.obo.dataadapter.OBOFileAdapter;
import org.obo.dataadapter.OBOMetaData;
import org.obo.datamodel.Namespace;
import org.obo.datamodel.OBOSession;

import phenote.config.Config;
import phenote.config.FieldConfig;
import phenote.config.OntologyConfig;
import phenote.config.xml.OntologyFileDocument.OntologyFile;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyException;
import phenote.error.ErrorEvent;
import phenote.error.ErrorManager;
import phenote.gui.OntologyUpdate;
import phenote.gui.SynchOntologyDialog;
import phenote.main.Phenote;
import phenote.util.FileUtil;

/** is this really a data adapter? - OntologyLoader? this isnt a data adapter
    it doesnt load & commit character data - just loads ontologies. rename OntologyLoader
    for now can stay in dataadapter package 
    this is specifically a OboFileLoader - other kinds of ontology loading may com along
    i'm working on refactoring this to work with the new terminology definitions*/
public class OntologyDataAdapter2 {

  private static OntologyDataAdapter2 singleton;
  //private Config config; cant cache may change
  private CharFieldManager charFieldManager = CharFieldManager.inst();
  private boolean initializingOntologies = false;
  private Map<String,Ontology> fileToOntologyCache = new HashMap<String,Ontology>();
  private OBOMetaData adapterMetaData;
  private Phenote phenote = Phenote.getPhenote(); 
  private static final Logger LOG = Logger.getLogger(OntologyDataAdapter2.class);
  private int numFiles;
  private int progressFactor=3;
  // does this need to be false for zfin
  private static final boolean DO_ONE_OBO_SESSION = true;

  private OntologyDataAdapter2() {
    //config = Config.inst();
//    initOntologies(); // loads up all ontologies
    //if (config.checkForNewOntologies()){new OntologyFileCheckThread().start();}
  }

  private Config cfg() { return Config.inst(); }

  /** synchronized so cant reload an ontology while ontologies are being initialized 
   getInstance calls initOntologies - in other words you have to call getInstance to
   initialize the ontologies */
  public static synchronized OntologyDataAdapter2 getInstance() {
    if (singleton == null)
      singleton = new OntologyDataAdapter2();
    return singleton;
  }

  public static void reset() {
    singleton = null;
  }
  
  /** just calls getInstance - but more intuitive for initialization */
  public static synchronized void initialize() {
    getInstance();
  }
  

   // public void setDoOneOboSession(boolean doOneOboSession) ??

  public void initOntologies() {
    // to prevent reload during init , maybe dont need with synchronization?
    Object[] options = {"Cancel", "Update", "Update All"};
    final Object defaultSelection = "Update All";
    Object selection = defaultSelection;
    selection = OntologyUpdate.queryForOntologyUpdate();
    System.out.println("you've selected:  "+ selection.toString());
    
    initializingOntologies = true; 

    // new paradigm
    if (DO_ONE_OBO_SESSION) {
      initOntolsOneOboSession();
    }
    else { // pase - phase out
      initOntolsSeparateOboSessions();
    }
    initializingOntologies = false;
  }



  private void initOntolsOneOboSession() {
    try {
      LOG.debug("loading obo files");
      OBOSession os = loadAllOboFilesIntoOneOboSession();
      LOG.debug("initializing char fields");
      initCharFields();
      LOG.debug("setting char field managers obo session");
      charFieldManager.setOboSession(os);
      // map namespaces to ontologies? 2 methods?
      LOG.debug("mapping namespaces to ontologies");
      mapNamespacesToOntologies(os);
      LOG.debug("ontologies initialiazed!");
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
    OntologyFile[] ontologies = cfg().getOntologyList();
    //in this paradigm, utilize the ontology defs to get the files
    //note, in this new way, all files will already be updated, simply load locally
    for (OntologyFile ontology : ontologies) {
    	files.add(getLocalFile(ontology).toString());
    }
    // should we have singleOboSession instance var?? or confusing with multi?
    OBOSession os = getOboSession(files);
    return os;
  }

  // create char fields - add to ont manager? or do in mapNamespace?
  // should Config actually set OntologyManager up with char fields - maybe this shouldnt
  // be ont data adapters responsibility - its just the 1st time that we need it
  private void initCharFields() {
    for (FieldConfig fieldConfig : cfg().getEnbldFieldCfgs()) {
      CharField cf = fieldConfig.getCharField(); // creates char field (if not there)
      charFieldManager.addField(cf);
    }
  }

  /** This actually creates both CharFields and Ontologies and maps namespaces from
      obo file adapter meta data */
  private void mapNamespacesToOntologies(OBOSession oboSession) throws OntologyException {
  	OntologyFile[] ontologies = cfg().getOntologyList();
    for (FieldConfig fieldConfig : cfg().getEnbldFieldCfgs()) {
      CharField cf = fieldConfig.getCharField(); // creates char field (if not there)
      // ontology manager.addCF???
      if (fieldConfig.hasOntologies()) {
        for (OntologyConfig oc : fieldConfig.getOntologyConfigList()) {
          // get namespaces for ont cfg
          // if adapterMetaData == null print err? throw ex? return?
          if (adapterMetaData == null)
            throw new OntologyException("No namespace/meta data for ontologies");
          // i think we need url it was loaded with
          //i don't think we should be able to get here.
//          if (!oc.hasLoadUrl()) {
//            LOG.error("Failed to find obo for "+oc.getName()+" can not load");
//            // in case log not set up right - need to work on that
//            System.out.println("Failed to find obo for "+oc.getName()+" can not load");
//            continue;
//          }
//          String urlString = oc.getLoadUrl().toString();
          
          String urlString = getLocalFile(oc.getName()).toString();
          Collection<Namespace> spaces = adapterMetaData.getNamespaces(urlString);
          // loads ontology from spaces
          //ah!  but this is loading the ontologies multiple times i think!
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

  private void updateLoadingScreen(String message, boolean addProgress) {
    if (phenote.loadingScreen == null) return;
    phenote.loadingScreen.setMessageText(message);
    if (addProgress) {
      final int progress = phenote.loadingScreen.getStartupProgress();
      phenote.loadingScreen.setProgress(progress + progressFactor);
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
      if no repos gets from cache(.phenote/obo-files), if no cache gets from
      app/obo-files throws ontology ex if fails to find in
      url/repos, .phenote/obo-files and app/obo-files */
  private URL findOboUrl(OntologyConfig ontCfg) throws OntologyException {
    // first get local url (if there is one) to use to compare against repos url
    // get normal/cached/local ontology
    String filename = ontCfg.getFile();
    if (filename == null)
      throw new OntologyException(ontCfg.getName()+" has null file");
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
        this.updateLoadingScreen("checking for updates: "+ontCfg.getName(), true);
        // if out of synch copies repos to local(.phenote/obo-files)
        // url may be jar/obo-files or svn/obo-files but this function will put file
        // in cache ~/.phenote/obo-files
        url = synchWithRepositoryUrl(url,reposUrl,ontCfg.getName(),filename);
        //} catch (/*MalfURL & Ontol*/Exception e) { LOG.error(e); e.printStackTrace(); }
      } catch (MalformedURLException m) {
        LOG.error("URL is malformed "+m);
      }
    }

    if (url == null) 
      throw new OntologyException("obo file "+filename+" not found in repos nor local");

    ontCfg.setLoadUrl(url);

    return url;

  }



  /** this copies obo file from reposUrl to local cache (~/.phenote/obo-files 
   called by findOboUrl which has already established that the reposUrl exists */
  private URL synchWithRepositoryUrl(URL localUrl, URL reposUrl, String ontol,
      String filename)

  throws OntologyException {

    boolean useRepos = false;
    if (localUrl == null) {
      useRepos = true;
    }
    else {
      long reposDate = 0;
      try {
        reposDate = getOboDate(reposUrl); // throws ex if no date
      }
      catch (OntologyException oe) { // no reposDate
        LOG.error("got OntEx trying to parse date from repos "+oe); 
        // already have local copy, no date to synch with, dont bother downloading from
        // repos, there should probably be a config to override this (always download)
        if (localUrl != null)
          useRepos = false; 
      }
      long loc = 0;
      int timer = cfg().getUpdateTimer();
      boolean autoUpdate = cfg().autoUpdateIsEnabled();
      if (localUrl != null) {
        try { loc = getOboDate(localUrl); }
        catch (OntologyException e2) { loc = 0; } // no local date - keep as 0
      }
      else
        useRepos = true;
      //if autoupdate without popup
      //LOG.debug("repos date "+reposDate+" local date "+loc);
      if ((autoUpdate && (timer==0)) && (reposDate > loc)) {
        useRepos = true;
      } else if (reposDate > loc || useRepos) {
        useRepos = SynchOntologyDialog.queryUserForOntologyUpdate(ontol);
      }
    }
    if (useRepos) {

      // i think its always better to download as http/repos is slow
//    boolean downloadToLocal = false; // from Config!
//    if (!downloadToLocal) {
//    LOG.info("Using obo file straight from repository "+reposUrl+
//    " NOT downloading to local cache");
//    return reposUrl; // just use straight from repos
//    }

      // download obo to local cache (takes time!)
      String file = localUrl!=null ? FileUtil.getNameOfFile(localUrl) : filename;
      //String file = FileUtil.getNameOfFile(localUrl);
      this.updateLoadingScreen("updating ontology file: "+ontol, true);


      try { 
        localUrl = new File(FileUtil.getDotPhenoteOboDir(),file).toURL();
        LOG.info("Downloading new ontology from repository "+reposUrl+" to "+localUrl);
        this.updateLoadingScreen("downloading from repository: "+ontol, true);

        copyReposToLocal(reposUrl,localUrl);
      }
      catch (MalformedURLException e) { throw new OntologyException(e); }
    }
    return localUrl;
  }



  /** Get the date of the file from the header of the obo file - just read header dont
      read in whole file. should it also query urlConnection for date first? 
      throws OntologyException if unable to parse date DateEx?*/
  private long getOboDate(URL oboUrl) throws OntologyException {
    try {
      InputStream is = oboUrl.openStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      // just try first 15 lines? try til hit [Term]?
      for (int i=1; i<=15; i++) {
        String line = br.readLine();
        if (i == 1 && line == null)
          throw new OntologyException("readLine returns null, "+
              "url seems to have no content "+oboUrl);
        if (line == null)
          throw new OntologyException("No date found in url "+oboUrl);

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
//    int i = 0;
//    //while((i=br.read())!=-1) {
//    String line;
//    while ((line=br.readLine())!=null) {
//    //bos.write(i); //buf, 0, i);
//    ps.print(line+"\n");
//    }
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
    if (fileName == null) throw new OntologyException("file is null");
    try { return FileUtil.findUrl(fileName); }
    catch (FileNotFoundException e) { throw new OntologyException(e); }
  }



  // String -> url to handle web start jar obo files
  private OBOSession getOboSession(URL oboUrl) throws OntologyException {
    if (oboUrl == null)
      throw new OntologyException("No url to retrieve");

    Collection<String> fileList = new ArrayList<String>();
    fileList.add(oboUrl.toString());
    return getOboSession(fileList);
  }

  /** file list is a list of file/url Strings */
  private OBOSession getOboSession(Collection<String> fileList)
  throws OntologyException {
    OBOFileAdapter fa = new OBOFileAdapter();
    OBOFileAdapter.OBOAdapterConfiguration cfg = new OBOFileAdapter.OBOAdapterConfiguration();
    // takes strings not urls!
    cfg.setReadPaths(fileList);
    cfg.setBasicSave(false);     //i think i need this for dangling references
    cfg.setAllowDangling(true);  //setting this to true for now!  should be configrable

    System.out.println("Loading ontologies");
    try { // throws data adapter exception
      
      OBOSession os = fa.doOperation(OBOAdapter.READ_ONTOLOGY,cfg,null);
      adapterMetaData = fa.getMetaData(); // check for null?
      return os;
    }
    catch (DataAdapterException e) {
    	//this is where we should go back, and replace with the old file.
    	//need to figure out which is the bad one, and switch it out, then
    	//try reloading
      // cause is crucial!
      String m = "got obo data adapter exception: "+e+" message "+e.getMessage()
      +" cause "+e.getCause()+"\nTHIS IS FATAL!\nCan not load ontologies. Phenote must"
      +" exit.\nConsider clearing out bad file from ~/.phenote/obo-files";
      ErrorManager.inst().error(new ErrorEvent(this,m));
      LOG.fatal(m);
      JOptionPane.showMessageDialog(null,m,"Load failure",JOptionPane.ERROR_MESSAGE);
      System.exit(1);
      throw new OntologyException(e);
    }
  }


  /** The ontology has been determined to be out of date (by quartz) and thus directed
      to reload itself from its file - in other words there is a new obo file to load 
      in place of old one */
//public void reloadOntology(Ontology ont) throws OntologyException {
//URL url = findFile(ont.getSource()); // ex
//loadOboSessionFromUrl(ont,url,ont.getSource()); // throws ex
//}
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
  }

  private void stopTimer(String m) {
    Calendar endTime = Calendar.getInstance();
    long seconds = (endTime.getTimeInMillis() - startTime.getTimeInMillis())/1000;
    LOG.debug(m+" number of seconds: "+seconds);
  }


  /** Load up/cache Sets for all ontologies used, anatomyOntologyTermSet
   * and patoOntologyTermSet -- move to dataadapter/OntologyDataAdapter... 
   this is old - phase out? */
  private Ontology initOntology(OntologyConfig ontCfg) throws OntologyException {
    Ontology ontology = new Ontology(ontCfg.getName()); // new Ontology(ontCfg)?
    if (ontCfg.hasFilter()) // set filter before loading obo session
      ontology.setFilter(ontCfg.getFilter());
    if (ontCfg.hasSlim())
      ontology.setSlim(ontCfg.getSlim());

    loadOboSession(ontology,ontCfg); // throws FileNotFoundEx->OntolEx
    return ontology;
  }

  /** Load obo session with obo edit adapter, unless previously loaded - reuse 
   this is old - phase out! - multi obos */
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
  /** If repository is configured loads obo from repos if local out of date
   old - phase out - multi obo sessions*/
  private void loadOboSessionCheckRepos(Ontology o,OntologyConfig oc)
  throws OntologyException {
//  // first get normal/cached/local ontology
//  String filename = oc.getFile();
//  // throws OntologyEx if file not found -- need to catch - should still try url
//  URL url = null;
//  try { url = findFile(filename); }
//  catch (OntologyException oe) {
//  System.out.println(filename+" not found locally, trying url if configured ");
//  }

//  // if ontCfg.hasSynchUrl() ?
//  // URL synchUrl = ontCfg.getSynchUrl
//  if (oc.hasReposUrl()) {
//  try {
//  URL reposUrl = oc.getReposUrl();
//  // if out of synch copies repos to local(.phenote/obo-files)
//  // url may be jar/obo-files or svn/obo-files but this function may put file
//  // in cache ~/.phenote/obo-files
//  url = synchWithRepositoryUrl(url,reposUrl,o.getName(),filename);

//  // to do - if from repos need to load repos into local obo cache!

//  } catch (/*MalformedURL & IO*/Exception e) { LOG.error(e); }
//  }

    URL url = findOboUrl(oc);

    long mem = Runtime.getRuntime().totalMemory()/1000000;
    LOG.debug(url+" checking with repos... loading obo session mem "+mem+"\n");
    this.updateLoadingScreen("checking for updates: "+o.getName(), true);
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
    this.updateLoadingScreen("doing something with "+o.getName(), false);
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

  // pase - phase out
  private void initOntolsSeparateOboSessions() {
    // getFieldConfigList gives enabled fields - not disabled
    for (FieldConfig fieldConfig : cfg().getEnbldFieldCfgs()) {
      //if (!fieldConfig.isEnabled()) continue; // not necasary actually
      CharField cf = fieldConfig.getCharField();
      //new CharField(fieldConfig.getLabel(),fieldConfig.getDataTag());
      //fieldConfig.setCharField(cf);

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
      charFieldManager.addField(cf);
    }



  }


  //this function will leverage what mark does for snagging the date.  in
  //the future, this could utilize the bioportal versioning system.
  /** @return A flag indicating if the given ontology has an update available */
  public boolean checkForUpdate(OntologyFile ontologyFile) throws OntologyException{
    String filename = ontologyFile.getFilename();
    boolean useRepos = false; //by default, i'll say there's no ontologies to update
    if (filename == null)
      throw new OntologyException(ontologyFile.getHandle()+" has null file");
    // throws OntologyEx if file not found -- catch & try url
    URL localUrl = null;
    try { 
    	localUrl = findFile(filename);
    	if (!(localUrl.toString().contains(".phenote"))) {
    		LOG.info(filename+" found in system at "+localUrl);
    		System.out.println(filename+" found in system at "+localUrl);
    		copyOntologyToDotPhenote(localUrl,filename);
    	}
    } catch (OntologyException oe) {
    	LOG.info(filename+" not found locally, trying url if configured ");
      System.out.println(filename+" not found locally, trying url if configured ");
    }

    // if repos url configured then check if its more up to date - if so copy
    // repos obo file to .phenote cache
    if ((ontologyFile.getLocation()!=null) && (!(ontologyFile.getLocation().equals("")))) {
      try {
        URL reposUrl = new URL(ontologyFile.getLocation()+ontologyFile.getFilename());  //should really have a method to check for correctness
        LOG.debug(reposUrl+" checking with repos for newer ontol\n");
        long reposDate = 0;
        long locDate = 0;
        try {
        	reposDate = getOboDate(reposUrl); // throws ex if no date
        } catch (OntologyException oe) { // no reposDate
        	LOG.error("got OntEx trying to parse date from repos "+oe); 
            // already have local copy, no date to synch with, dont bother downloading from
            // repos, there should probably be a config to override this (always download)
        	//i think this should download by default if there's no date in the file
        }
        if (localUrl != null) {
          	try { locDate = getOboDate(localUrl); }
            catch (OntologyException e2) { 
            	locDate = -1; // no local date - set to -1 so that if no repos date, will download
            }
            useRepos=false;
        }	else  //don't have it locally, need to download it.
        	useRepos = true;
        if (reposDate > locDate)
        	useRepos=true;
      } catch (MalformedURLException m) {
          LOG.error("URL is malformed "+m);
      }
    }
  	return useRepos; 
  }
  
  public void downloadUpdate(String ontology) throws OntologyException {
  	OntologyFile[] ontologies = Config.inst().getTerminologyDefs().getOntologyFileArray();
  	URL reposUrl = null;
  	URL localUrl = null;
  	for (OntologyFile of : ontologies) {
  		if (of.getHandle().equals(ontology)) {
  			try {
  				reposUrl = new URL(of.getLocation()+of.getFilename());
  				localUrl = getLocalFile(of);
  				startTimer();
  				//for some reason, this isn't logging
  				LOG.info("Downloading ontology from: "+reposUrl+"to: "+localUrl);
  				//this uses marks, but maybe this should use john's?
  				//need to add in here the copying of the old ontology file to temp, in
  				//case the new one is botched.
  				copyReposToLocal(reposUrl,localUrl);
  				stopTimer(of.getFilename());
  				System.out.println(of.getFilename()+" updated from "+ reposUrl);
  			} catch (MalformedURLException e) { throw new OntologyException(e); }
  		}
  	}
  	return;
  }
  
  private URL getLocalFile(OntologyFile ontology) throws OntologyException{
  	URL localUrl = null;
  	try {
  		localUrl = new File(FileUtil.getDotPhenoteOboDir(),ontology.getFilename()).toURL();
    	return localUrl;
  	} catch (MalformedURLException e) { throw new OntologyException(e); }
  }
  
  private URL getLocalFile(String handle) throws OntologyException{
  	//given an ontology handle, find the file in the term defs
  	URL localUrl = null;
  	for (OntologyFile ontology : cfg().getOntologyList()) {
  		if (ontology.getHandle().equals(handle)) {
  			try {
    		localUrl = new File(FileUtil.getDotPhenoteOboDir(),ontology.getFilename()).toURL();
    		break;
  			} catch (MalformedURLException e) { throw new OntologyException(e); }
  		}
  	}
  	return localUrl;
  }
  
  public boolean checkForLocalFileExists(OntologyFile ontology) {
  	URL localUrl = null;
  	try {
  		File f = new File(getLocalFile(ontology).getFile());
    	return f.exists();
  	} catch (OntologyException e) {
			e.printStackTrace();
			return false;
		} 
  }
  
  public boolean checkForLocalFileExists(String filename) {
  	URL localUrl = null;
  	try {
  		localUrl = new File(FileUtil.getDotPhenoteOboDir(),filename).toURL();
  		File f = new File(localUrl.getFile());
    	return f.exists();
  	} catch (MalformedURLException e) { return false;}
  }

  public boolean copyOntologyToDotPhenote(URL localUrl, String filename) {
  	File dotPhenoteFile = new File(FileUtil.getDotPhenoteOboDir(),filename);
  	File phenoteFile = null;
//		try {
			try {
				phenoteFile = new File(localUrl.toURI());
			} catch (URISyntaxException e1) {
			LOG.error("could not find "+filename+" locally using URI "+localUrl.toString());
				e1.printStackTrace();
			}
//			phenoteFile = new File(FileUtil.findUrl(localUrl.getPath()).toString());
//		} catch (FileNotFoundException e1) {
//			LOG.error("could not find "+filename+" locally at "+localUrl.toString());
//	  	System.out.println("could not find "+filename+" locally at "+localUrl.toString());
//
//			e1.printStackTrace();
//		}

  	try {
//  		FileUtil.copyFileIntoArchive(phenoteFile,dotPhenoteFile);
  		FileUtil.copyFile(phenoteFile, dotPhenoteFile);
	  	LOG.info(filename+" copied from "+phenoteFile.getPath()+" to "+dotPhenoteFile.getPath());
	  	System.out.println(filename+" copied from "+phenoteFile.getPath()+" to "+dotPhenoteFile.getPath());
		} catch (IOException e) {
			LOG.error("error copying from "+phenoteFile.getPath()+" to "+dotPhenoteFile.getPath());
	  	System.out.println("error copying from "+phenoteFile.getPath()+" to "+dotPhenoteFile.getPath());
	  	e.printStackTrace();
			
		}
  	return dotPhenoteFile.exists();
  }  
}

