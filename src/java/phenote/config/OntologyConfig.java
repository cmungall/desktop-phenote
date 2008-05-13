package phenote.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.apache.log4j.Logger;

import phenote.config.xml.OntologyFileDocument;
import phenote.config.xml.FieldDocument.Field;
import phenote.config.xml.OnTheFlySlimTermDocument.OnTheFlySlimTerm;
import phenote.config.xml.OntologyDocument.Ontology;
import phenote.config.xml.TerminologyDefinitionsDocument.TerminologyDefinitions;

/** May not even have ontology file (free text eg genotype) rename FieldConfig? */
public class OntologyConfig {

  /** just the filename - no path/url */
  public String ontologyFile;
  /** if url given for file then this string gets set (or old backward compatible repos
      subdir & basedir - phase out) otherwise null */
  private String reposUrlString;
  /** load url is url where ontol loaded from, repos or local file cache or jar 
      this is handy for ontology adapter to record - i forget why */
  private URL loadUrl;
  private URL localUrl;
  private FieldConfig fieldConfig;
  private Ontology ontologyBean; // from xml beans - replaces above fields!
  private OntologyFileDocument.OntologyFile.Type.Enum ontologyType; //OWL, OBO, should be enum
  private boolean autoUpdate;  //whether or not to autoupdate this ontology at startup
  private Date mostRecentFileDate;
  private long fileDate;
  


  /** confusing - this is xml bean Ontology NOT datamodel Ontology - this is reading
   in from xml config - if ontology doesnt have name use fieldName (single ontols
   just use field name) */
  OntologyConfig(phenote.config.xml.OntologyDocument.Ontology o,FieldConfig fc) {
    ontologyBean = o;
    fieldConfig = fc;

    TerminologyDefinitions t = fc.getConfig().getTerminologyDefs();
    if (t!=null) { //terminology defs defined, use these
      setOntFileFromTermDefs(o.getName(), fc.getConfig().getOntologyList());
    } else {
    	setFile(o.getFile());
    }
        
    // for now ignoring if name set as there was a bug where name was getting set
    // to "Entity" in the -u .phenote/conf file - woops
    // NO this is bad - as can have multiple rel ontols and now have same name!
    // but before can change need to see who is relying on this!
    // i dont think anything is, but really old configs have no name
    // ok only do if name is null now - for backward compat
    if (isPostCompRel() && getOntologyBean().getName() == null) //&& o.getName()==null // default Relationship name
      setName("Relationship");
  }

  public OntologyConfig(String name,String displayName,FieldConfig fc) {
    fieldConfig = fc;
    setName(name);
    // displayname
  }

  Ontology getOntologyBean() {
    // if ontologyBean == null ontologyBean = new Ontology(); ???
    if (ontologyBean == null) // this is for backward compatible to old ontol ways
      ontologyBean = fieldConfig.getFieldBean().addNewOntology();
    return ontologyBean;
  }

  /** PASE! Ontology stuff in field itself (field only has one ontology) - this is
      getting phased out replaced by field with single ontology element 
      this also doesnt have Ontology xml bean - phase out now??*/
  OntologyConfig(Field field,FieldConfig fc) {
    fieldConfig = fc;
    //name = field.getName();
    setName(field.getName());
    setFile(field.getFile());//ontologyFile = field.getFile();
    getOntologyBean().setFile(field.getFile()); // crucial!
    // downside of strongly types xml beans is filterOut has to be dealt with 
    // separately for field & ontology - annoying - & all other attribs
    //filterOut = field.getFilterOut()!=null ? field.getFilterOut() : null;
    //setFilter(field.getFilterOut());
    //slim = field.getSlim()!=null ? field.getSlim() : null;
    setSlim(field.getSlim());
    //reposSubdir = field.getReposSubdir()!=null ? field.getReposSubdir() : null;
    setReposSubdir(field.getReposSubdir());
    //fc.getFieldBean().setFile(null); // ?
    fc.getFieldBean().xsetFile(null);
  }


  // for makePostCompRelCfg PASE - for backward compatibility
//   private OntologyConfig(String name, String file,FieldConfig fc) {
//     //this(name,fc);
//     fieldConfig = fc;
//     setName(name);
//     setOntologyFile(file);
//   }

  // phasing out PASE - now just doing rel as another ontology - backward compatible
  // called from field config
  static OntologyConfig makePostCompRelCfg(String file,FieldConfig fc) {
    OntologyConfig rel = new OntologyConfig("Relationship",null,fc);
    rel.setOntologyFile(file);
    rel.setIsPostCompRel(true);
    rel.getOntologyBean().setFile(file); // crucial!
    fc.getFieldBean().unsetPostcomp(); // also crucial - get rid of it
    return rel;
  }



  /** File can be url(repos) or filename (from cache/jar/app), if url sets 
      reposUrlString and ontologyFile with end of url */
  public void setFile(String file) {
    if (file == null) {
      System.out.println("ERROR: null ontology file "+getName());
      new Throwable().printStackTrace();
      return;
    }
    if (file.startsWith("http:") || file.startsWith("https:") ||
    		file.startsWith("ftp:") || file.startsWith("sftp:")) {
      reposUrlString = file;
      ontologyFile = file.substring(file.lastIndexOf('/')+1);
    }
    else {
      ontologyFile = file;
      // reposUrlString is left null (unless pase repossubdir set)
    }
  }

  public boolean isPostCompRel() { return getOntologyBean().getIsPostcompRel(); }
  private void setIsPostCompRel(boolean b) { getOntologyBean().setIsPostcompRel(b); }

  boolean hasName() {
    return !isBad(getName());
  }

  public void setName(String name) {
    if (isBad(name))
      return;
    //this.name = name;
    getOntologyBean().setName(name);
  }

  public String getName() {
    //return name; 
    // if ontology doesnt have name(single ontol field), set to field name
    if (getOntologyBean().getName() == null)
      setName(fieldConfig.getLabel());
    return getOntologyBean().getName();
  }

  void setOntologyFile(String file) {
    if (isBad(file)) {
      System.out.println("null or empty ontology file given for "+getName()+" "+file);
      return; // ex?
    }
    ontologyFile = file;
  }
  public String getFile() { return ontologyFile; }


  public URL getReposUrl() throws MalformedURLException {
    return new URL(getReposUrlString());
  }

  private String getReposUrlString() {
    if (reposUrlString == null) { // old way - construct from base & subdir - silly
      if (getReposBaseDir()==null || hasReposSubdir() || getFile()==null) return null;
      reposUrlString = getReposBaseDir()+"/"+getReposSubdir() + "/" + getFile();
    }
    return reposUrlString;
  }

  // pase
  private String getReposBaseDir() {
    // Config.inst() is rather presumptious!
    //return Config.inst().getReposUrlDir();
    return fieldConfig.getConfig().getReposUrlDir();
  }

  // pase
  private void setReposSubdir(String rs) { getOntologyBean().setReposSubdir(rs); }
  private String getReposSubdir() { return getOntologyBean().getReposSubdir(); }
  private boolean hasReposSubdir() { return getReposSubdir() != null; }

  public boolean hasReposUrl() {
    //if (getFile() == null) return false; // shouldnt happen
    return getReposUrlString() != null;
    // repos base dir is just a convenience
    //if (reposSubdir == null) //&& getReposBaseDir() == null)
    //return false;
    //try { getReposUrl(); } catch (MalformedURLException e) { return false; } ??
    //return true;
  }

  private boolean isBad(String s) {
    return s == null || s.trim().equals("");
  }

  public boolean hasOntology() {
    return ontologyFile != null;
  }

  public boolean hasNamespace() { return getNamespace() != null; }
  public String getNamespace() { return getOntologyBean().getNamespace(); }
  public void setNamespace(String n) { getOntologyBean().setNamespace(n); }

  // --> namespace
//   public boolean hasFilter() { return getFilter() != null; }
//   public String getFilter() { return getOntologyBean().getFilterOut(); } 
//   public void setFilter(String f) { getOntologyBean().setFilterOut(f); }

  public boolean hasSlim() { return getSlim() != null; }
  public String getSlim() { return getOntologyBean().getSlim(); }
  public void setSlim(String s) {
    if (s == null) return; // necasary?
    getOntologyBean().setSlim(s);
  }

  /** The actual url used to load ontology - this may be file: or http:
   so if from file cache this is different than repository url */
  public void setLoadUrl(URL u) { loadUrl = u; }
  public void setLocalUrl(URL u) {localUrl = u; }
  public URL getLocalUrl() { return localUrl; }
  public URL getLoadUrl() { return loadUrl; }
  public boolean hasLoadUrl() { return loadUrl != null; }

  public String toString() { return getName(); }


  /** make ontology xml bean and add to field xml bean for writeback */
  // now that working directly with xml bean this should happen for free!!
//   void writeOntology(Field f) {
//     Ontology oBean = f.addNewOntology();
//     oBean.setName(getName());
    
//     oBean.setFile(getFile());
//     // new style - if there is a repos url that set file to it
//     if (hasReposUrl())
//       oBean.setFile(getReposUrlString());
//     //if (hasFilter()) oBean.setFilterOut(getFilter());
//     if (hasNamespace())
//       oBean.setNamespace(getNamespace());
//     if (hasSlim())
//       oBean.setSlim(getSlim());
//     if (isPostCompRel)
//       oBean.setIsPostcompRel(true); // will it write if not set? hope not
//   }
  
  void mergeWithOldConfig(FieldConfig oldFC) {
    // NEW - ADD
    if (!oldFC.hasOntConfig(this)) {
      oldFC.insertOntologyConfig(fieldConfig.getOntCfgIndex(this),this);
      return;
    }
    // UPDATE
    OntologyConfig oldOC = oldFC.getOntConfig(getName());
    // or should even null gets transmitted in which case this replaces oldOC??
    if (reposUrlString != null) {
      oldOC.ontologyFile = reposUrlString;
    }
    else if (ontologyFile!=null) {
      oldOC.ontologyFile = ontologyFile;
    }
    //if (getFilter()!=null) oldOC.filterOut = filterOut; // pase! ??
    if (hasSlim()) oldOC.setSlim(getSlim());;
    //if (reposSubdir!=null) oldOC.reposSubdir = reposSubdir;
    if (hasNamespace()) oldOC.setNamespace(getNamespace());
    if (isPostCompRel()) // only set if true, if not there default false
      oldOC.setIsPostCompRel(isPostCompRel());
  }

  public boolean sortById() {
    if (fieldConfig.getFieldBean().getSortBy() == null) return false;
    return fieldConfig.getFieldBean().getSortBy() == Field.SortBy.ID;
  }
  
  /** look for ontology in ontology list with name, and set OntConfig state from that
      ontology */
  public void setOntFileFromTermDefs(String name,
                                     OntologyFileDocument.OntologyFile[] ontologyList) {
  	boolean foundIt = false;
	  for (OntologyFileDocument.OntologyFile of : ontologyList) {
	  	String thisOntol = of.getHandle();
      if (thisOntol==null) {
        log().error("Ontology config "+of+" lacks a handle, cant use");
        continue;
      }
	  	if (thisOntol.equals(name)) { 
	  		foundIt=true;  //the current item in the ontology def is the matching ontology
	  		reposUrlString = of.getLocation();
	  		ontologyFile = of.getFilename();
	  		autoUpdate = of.getAutoUpdate();
	  		//probably should be smart here, and if the type isn't defined, then grab it from the filename extension, or default
	  		ontologyType = of.getType();  //OBO, OWL or FLAT, need flavor
	  		//check to see if there's a repository location
	  		//check to see if there's a file name
	      if (ontologyFile == null) {
	        System.out.println("ERROR: "+ name + "ontology filename undefined ");
	        new Throwable().printStackTrace();
	        return;
	      }
	  	}
	  }
	  if (!foundIt) {
      System.out.println("ERROR: " + name + " terminology not defined");
      new Throwable().printStackTrace();
      return;

	  }
  }

  public boolean hasOnTheFlySlim() {
    return getOnTheFlySlimTerms()!=null && getOnTheFlySlimTerms().length > 0;
  }
  
  
  public OnTheFlySlimTerm[] getOnTheFlySlimTerms() {
    return getOntologyBean().getOnTheFlySlimTermArray();
  }
  
  public void setUpdateDate(long date) {
  	fileDate = date;
  }
  
  public long getUpdateDate() {
  	return fileDate;
  }

	private static Logger log() {
		return Logger.getLogger(OntologyConfig.class);
	}
  
}

  //public String name;
  //public String namespace; // this should replace filterOut
  //public String filterOut; // public?
  //private String slim;
  //private String reposSubdir; // pase!
  //private boolean isPostCompRel = false;
    //name = o.getName()!=null ? o.getName() : fieldName;
    //if (o.getNamespace() != null) namespace = o.getNamespace();
//    if (o.getFilterOut() != null) filterOut = o.getFilterOut();
    //slim = o.getSlim()!=null ? o.getSlim() : null;
    //if (o.getReposSubdir()!=null) reposSubdir = o.getReposSubdir();
    // if xgetIsPostCompRel != null
    //isPostCompRel = o.getIsPostcompRel(); // hmm what will return when not set??
