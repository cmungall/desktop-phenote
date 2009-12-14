package phenote.dataadapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URL;
import java.net.URLConnection;
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
import java.util.Map;

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
import phenote.config.Preferences;
import phenote.config.ProxyDialog;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyException;
import phenote.error.ErrorEvent;
import phenote.error.ErrorManager;
import phenote.gui.SynchOntologyDialog;
import phenote.main.Phenote;
import phenote.util.FileUtil;

/** is this really a data adapter? - OntologyLoader? this isnt a data adapter
    it doesnt load & commit character data - just loads ontologies. rename OntologyLoader
    for now can stay in dataadapter package 
    this is specifically a OboFileLoader - other kinds of ontology loading may com along
    OntologyDataAdapter and OntologyDataAdapter2 desperately need to be merged!
    theres a lot of cut&paste code between them and maintaining both is very tedious
    the parts that are different in ODA2 should just be done with a flag, the duplicated
    classes is not the way to go */
public class OntologyDataAdapter {

  private static OntologyDataAdapter singleton;
  //private Config config; cant cache may change
  private CharFieldManager charFieldManager = CharFieldManager.inst();
  private boolean initializingOntologies = false;
  private Map<String,Ontology> fileToOntologyCache = new HashMap<String,Ontology>();
  private OBOMetaData adapterMetaData;
  private Phenote phenote = Phenote.getPhenote(); 
  private static final Logger LOG = Logger.getLogger(OntologyDataAdapter.class);
  private int numFiles;
  private int progressFactor=3;
  // does this need to be false for zfin/web??
  //private static final boolean DO_ONE_OBO_SESSION = true;

  private Preferences prefs = Preferences.getPreferences();

  private OntologyDataAdapter() {
    initOntologies(); // loads up all ontologies
  }

  private Config cfg() { return Config.inst(); }

  /** synchronized so cant reload an ontology while ontologies are being initialized 
   getInstance calls initOntologies - in other words you have to call getInstance to
   initialize the ontologies */
  public static synchronized OntologyDataAdapter getInstance() {
    if (singleton == null)
      singleton = new OntologyDataAdapter();
    return singleton;
  }

  public static void reset() {
    singleton = null;
  }
  
  /** just calls getInstance - but more intuitive for initialization */
  public static synchronized void initialize() {
    getInstance();
  }
  
  private void initOntologies() {
    // to prevent reload during init, maybe dont need with synchronization?
    initializingOntologies = true; 
    // new paradigm   //if (DO_ONE_OBO_SESSION) {
    initOntolsOneOboSession();
    //else initOntolsSeparateOboSessions();
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
      LOG.debug("ontologies initialized!");
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
    for (FieldConfig fieldConfig : cfg().getEnbldFieldCfgs()) {
      for (OntologyConfig oc : fieldConfig.getOntologyConfigList()) {
        try {
          // this actually does repos synching, should only do once per obo file,
          // but it wont download something twice as 2nd time it will be in synch
          String file = findOboUrlString(oc); // throws oex if not found
          if (!files.contains(file)) { // dont load file twice
            files.add(file);
          }
        }
        catch (OntologyException e) {
          String m = "Got exception while trying to load obo file--please check config & obo files.  Exception: " + e.getMessage();
          LOG.error(m); System.out.println(m);
        }
      }
    }
    // should we have singleOboSession instance var?? or confusing with multi?
    numFiles = files.size();
    // i have funny test configs with no ontol files - / 0 exception
    int div = numFiles == 0 ? 1 : numFiles; 
    progressFactor = 1+(10/div); //this is kind of a hack right now until we get threading
    System.out.println("numfiles="+numFiles);
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
          if (!oc.hasLoadUrl()) {
            LOG.error("Failed to find obo file for "+oc.getName()+" ontology");
            // in case log not set up right - need to work on that
            System.out.println("Failed to find obo file for "+oc.getName()+" ontology");
            continue;
          }
          String urlString = oc.getLoadUrl().toString();
          Collection<Namespace> spaces = adapterMetaData.getNamespaces(urlString);
          // loads ontology from spaces
          //ah!  but this is loading the ontologies multiple times i think!
          Ontology o = new Ontology(spaces,oc,oboSession);
          this.updateLoadingScreen("Loading: "+o.getName(), true);

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
        LOG.debug(reposUrl+": checking repository for newer version\n");
        this.updateLoadingScreen("Checking for updates: "+ontCfg.getName(), true);
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
        LOG.error("got OntEx trying to parse date from " + reposUrl + ": "+oe); 
        // already have local copy, no date to synch with, dont bother downloading from
        // repos, there should probably be a config to override this (always download)
        if (localUrl != null)
          useRepos = false; 
      }
      LOG.debug("For " + reposUrl + ", obodate = " + reposDate);
      long loc = 0;
      int timer = cfg().getUpdateTimer();
      boolean autoUpdate = cfg().autoUpdateIsEnabled();
      if (localUrl != null) {
        try { loc = getOboDate(localUrl); }
        catch (OntologyException e2) { loc = 0; } // no local date - keep as 0
	LOG.debug("For " + localUrl + ", obodate = " + loc);
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
        this.updateLoadingScreen("Downloading " + ontol + " ontology from "+ reposUrl + "...", true);

        copyReposToLocal(reposUrl,localUrl);
      }
      catch (MalformedURLException e) { 
	      LOG.error("Got error while trying to download ontology from repository "+reposUrl+" to "+localUrl);
	      throw new OntologyException(e); 
      }
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
              "url seems to have no content: "+oboUrl);
        if (line == null)
          throw new OntologyException("No date found in url "+oboUrl);

        // eg date: 22:08:2006 15:38
        if (!line.startsWith("date:")) continue;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd:MM:yyyy HH:mm");
        Date d = dateFormat.parse(line, new ParsePosition(6));
        //LOG.debug("date "+d+" for url "+oboUrl+" line "+line);
        br.close();
        if (d == null)
          throw new OntologyException("getOboDate: couldn't parse date line from " + oboUrl + ": "+line);
        return d.getTime();
      }
      throw new OntologyException("No date found in "+oboUrl);
    } catch (IOException e) { throw new OntologyException(e); }
  }

  /** local url may be from distrib/jar dir, but needs to be set to 
      .phenote/obo-files dir as thats where the user cache is */
  private void copyReposToLocal(URL reposUrl, URL localUrl)
  throws OntologyException {
    try {
	Proxy proxy = null;
	if (ProxyDialog.proxyIsSet()) {
		String proxyHost = prefs.getProxyHost();  // "proxy.charite.de";
		int proxyPort = prefs.getProxyPort();  // 888;
		String proxyProtocol = prefs.getProxyProtocol();
		if (proxyHost != null && proxyHost.length()>0) {
			if (proxyProtocol.equals("SOCKS"))
				proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost,proxyPort));
			else
				proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost,proxyPort));
		}
		if (proxy != null)
			LOG.info("Using proxy " + proxyProtocol + ": " + proxyHost + ":" + proxyPort + " to open connection to " + reposUrl);
	}
	
	URLConnection urlConnection;
	if (proxy != null) {
		urlConnection = reposUrl.openConnection(proxy);
	}
	else {
		LOG.info("Opening connection to " + reposUrl);
		urlConnection = reposUrl.openConnection();
	}

	urlConnection.setConnectTimeout(30000);

	int size = urlConnection.getContentLength();
	LOG.info("For " + reposUrl + ", Content-Length = " + size);
	// The sourceforge URLs don't give the content length (apparently that's optional for servers to provide).
	if (size < 1) {
		size = Integer.MAX_VALUE;
	}

	InputStream is = urlConnection.getInputStream();

      // nio & old io do the same time - oh well (~1 minute for GO)
      //startTimer("nio file copy "+reposUrl+" to "+localUrl);
//      InputStream is = reposUrl.openStream();
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
//      long size = 99999999999999l; // cant get actual size - whole file
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
    this.updateLoadingScreen("loading ontologies into memory", false);
    
    try { // throws data adapter exception
      
      OBOSession os = fa.doOperation(OBOAdapter.READ_ONTOLOGY,cfg,null);
      adapterMetaData = fa.getMetaData(); // check for null?
      return os;
    }
    catch (DataAdapterException e) {
      // cause is crucial!
      String m = "got obo data adapter exception: "+e+" message "+e.getMessage()
      +" cause "+e.getCause()+"\nTHIS IS FATAL!\nCannot load ontologies. Phenote must"
      +" exit.\nConsider clearing out bad file from ~/.phenote/obo-files";
      ErrorManager.inst().error(new ErrorEvent(this,m));
      //LOG.error(m); // error manager should do this for free 
      // actually theres really no point in going on i think as we have failed to get
      // an obo session - todo - give user options for ammending this failure, ignore
      // failing file, or fetch from somewhere else, but for now throw up popup and exit
      // is the best we can do - or should this be done by catcher of OntEx?
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
    //timerMsg = m;
    //LOG.debug(timerMsg+" Start clock "+startTime.getTime()); // ??
  }

  private void stopTimer(String m) {
    Calendar endTime = Calendar.getInstance();
    long seconds = (endTime.getTimeInMillis() - startTime.getTimeInMillis())/1000;
    LOG.debug(m+" number of seconds: "+seconds);
  }

}
