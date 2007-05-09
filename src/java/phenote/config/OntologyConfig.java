package phenote.config;

import java.net.MalformedURLException;
import java.net.URL;

import phenote.config.xml.FieldDocument.Field;
import phenote.config.xml.OntologyDocument.Ontology;
import phenote.config.xml.PostcompDocument.Postcomp;

/** May not even have ontology file (free text eg genotype) rename FieldConfig? */
public class OntologyConfig {

  public String name;
  public String ontologyFile;
  public String namespace; // this should replace filterOut
  public String filterOut; // public?
  private String slim;
  private String reposSubdir;
  private boolean isPostCompRel = false;
  /** load url is url where ontol loaded from, repos or local file cache or jar */
  private URL loadUrl;
  private String reposUrlString;
  private FieldConfig fieldConfig;

  //static OntologyConfig defaultPato = new OntologyConfig("Pato","attribute_and_value.obo");

  //OntologyConfig() {} // not sure this is actually needed/used
  private OntologyConfig(String name,FieldConfig fc) {
    this.name = name;
    fieldConfig = fc;
  }



  /** Ontology stuff in field itself (field only has one ontology) - this is getting phased 
      out replaced by field with single ontology element */
  OntologyConfig(Field field,FieldConfig fc) {
    fieldConfig = fc;
    name = field.getName();
    ontologyFile = field.getFile();
    // downside of strongly types xml beans is filterOut has to be dealt with 
    // separately for field & ontology - annoying - & all other attribs
    filterOut = field.getFilterOut()!=null ? field.getFilterOut() : null;
    slim = field.getSlim()!=null ? field.getSlim() : null;
    reposSubdir = field.getReposSubdir()!=null ?
      field.getReposSubdir() : null;
  }

  /** confusing - this is xml bean Ontology NOT datamodel Ontology - this is reading
   in from xml config - if ontology doesnt have name use fieldName (single ontols
   just use field name) */
  OntologyConfig(phenote.config.xml.OntologyDocument.Ontology o, String fieldName,
                 FieldConfig fc) {
    fieldConfig = fc;
    name = o.getName()!=null ? o.getName() : fieldName;
    setFile(o.getFile());
    if (o.getNamespace() != null)
      namespace = o.getNamespace();
    if (o.getFilterOut() != null)
      filterOut = o.getFilterOut();
    slim = o.getSlim()!=null ? o.getSlim() : null;
    if (o.getReposSubdir()!=null)
      reposSubdir = o.getReposSubdir();
    // if xgetIsPostCompRel != null
    isPostCompRel = o.getIsPostcompRel(); // hmm what will return when not set??
    
    // for now ignoring if name set as there was a bug where name was getting set
    // to "Entity" in the -u .phenote/conf file - woops
    if (isPostCompRel /*&& o.getName()==null*/) // default Relationship name
      name = "Relationship";
    //if (isPostCompRel)  fc.setPostCompRelOntCfg(this);
  }

  // for makePostCompRelCfg
  private OntologyConfig(String name, String file,FieldConfig fc) {
    this(name,fc);
    setOntologyFile(file);
  }

  // phasing out - now just doing rel as another ontology - backward compatible
  static OntologyConfig makePostCompRelCfg(String file,FieldConfig fc) {
    OntologyConfig rel = new OntologyConfig("Relationship",file,fc);
    rel.isPostCompRel = true;
    return rel;
  }

//   OntologyConfig(String name, String file, String filterOut) {
//     this(name,file);
//     this.filterOut = filterOut;
//   }
//   OntologyConfig(String name, String file, String filterOut,String slim) {
//     this(name,file,filterOut);
//     this.slim = slim;
//   }


  /** File can be url(repos) or filename (from cache/jar/app), if url sets 
      reposUrlString and ontologyFile with end of url */
  private void setFile(String file) {
    if (file.startsWith("http:")) {
      reposUrlString = file;
      ontologyFile = file.substring(file.lastIndexOf('/')+1);
    }
    else {
      ontologyFile = file;
    }
  }

  public boolean isPostCompRel() { return isPostCompRel; }

  boolean hasName() {
    return !isBad(name);
  }

  void setName(String name) {
    if (isBad(name))
      return;
    this.name = name;
  }

  String getName() { return name; }

  void setOntologyFile(String file) {
    if (isBad(file)) {
      System.out.println("null or empty ontology file given for "+name+" "+file);
      return; // ex?
    }
    ontologyFile = file;
  }
  public String getFile() { return ontologyFile; }

  void setReposSubdir(String rs) { reposSubdir = rs; }

  public URL getReposUrl() throws MalformedURLException {
    return new URL(getReposUrlString());
  }

  private String getReposUrlString() {
    if (reposUrlString == null) { // old way - construct from base & subdir - silly
      if (getReposBaseDir()==null || reposSubdir==null || getFile()==null) return null;
      reposUrlString = getReposBaseDir()+"/"+reposSubdir + "/" + getFile();
    }
    return reposUrlString;
  }

  private String getReposBaseDir() {
    // Config.inst() is rather presumptious!
    //return Config.inst().getReposUrlDir();
    return fieldConfig.getConfig().getReposUrlDir();
  }

  private String getReposSubdir() { return reposSubdir; }

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

  public boolean hasNamespace() { return namespace != null; }
  public String getNamespace() { return namespace; }

  public boolean hasFilter() { return filterOut != null; }
  public String getFilter() { return filterOut; } 

  public boolean hasSlim() { return slim != null; }
  public String getSlim() { return slim; }

  /** The actual url used to load ontology - this may be file: or http:
   so if from file cache this is different than repository url */
  public void setLoadUrl(URL u) { loadUrl = u; }
  public URL getLoadUrl() { return loadUrl; }
  public boolean hasLoadUrl() { return loadUrl != null; }

  public String toString() { return name; }


  /** make ontology xml bean and add to field xml bean for writeback */
  void writeOntology(Field f) {
    Ontology oBean = f.addNewOntology();
    oBean.setName(getName());
    
    oBean.setFile(getFile());
    // new style - if there is a repos url that set file to it
    if (hasReposUrl())
      oBean.setFile(getReposUrlString());
    if (hasFilter())
      oBean.setFilterOut(getFilter());
    if (hasNamespace())
      oBean.setNamespace(getNamespace());
    if (hasSlim())
      oBean.setSlim(getSlim());
    if (isPostCompRel)
      oBean.setIsPostcompRel(true); // will it write if not set? hope not
  }
  
  void mergeWithOldConfig(FieldConfig oldFC) {
    // NEW - ADD
    if (!oldFC.hasOntConfig(this)) {
      oldFC.addOntologyConfig(this);
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
    if (filterOut!=null) oldOC.filterOut = filterOut;
    if (slim!=null) oldOC.slim = slim;
    //if (reposSubdir!=null) oldOC.reposSubdir = reposSubdir;
    if (namespace != null) oldOC.namespace = namespace;
    oldOC.isPostCompRel = isPostCompRel;
  }
}

//   void writePostComp(Field f) {// annoying as shares a lot with ontology
//     Postcomp pc = f.addNewPostcomp();
//     pc.setRelationshipOntology(getFile());  }
