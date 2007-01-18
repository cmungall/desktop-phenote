package phenote.config;

import java.net.MalformedURLException;
import java.net.URL;

import phenote.config.xml.FieldDocument.Field;
import phenote.config.xml.OntologyDocument.Ontology;

/** May not even have ontology file (free text eg genotype) rename FieldConfig? */
public class OntologyConfig {

  public String name;
  public String ontologyFile;
  //public String nameSpace; // not used yet - or was this what filter out ended up
  public String filterOut; // public?
  private String slim;
  private String reposSubdir;

  //static OntologyConfig defaultPato = new OntologyConfig("Pato","attribute_and_value.obo");

  OntologyConfig() {} // not sure this is actually needed/used
  OntologyConfig(String name) { this.name = name; }
  OntologyConfig(String name, String file) {
    this(name);
    ontologyFile = file;
    if (isBad(file)) // exception?
      System.out.println("null or empty ontology file given for "+name+" "+file);
  }

  OntologyConfig(String name, String file, String filterOut) {
    this(name,file);
    this.filterOut = filterOut;
  }
  OntologyConfig(String name, String file, String filterOut,String slim) {
    this(name,file,filterOut);
    this.slim = slim;
  }

  /** Ontology stuff in field itself (field only has one ontology) */
  OntologyConfig(Field field) {
    name = field.getName().getStringValue();
    ontologyFile = field.getFile().getStringValue();
    // downside of strongly types xml beans is filterOut has to be dealt with 
    // separately for field & ontology - annoying - & all other attribs
    filterOut = field.getFilterOut()!=null ? field.getFilterOut().getStringValue() : null;
    slim = field.getSlim()!=null ? field.getSlim().getStringValue() : null;
    reposSubdir = field.getReposSubdir()!=null ?
      field.getReposSubdir().getStringValue() : null;
  }

  OntologyConfig(Ontology o) {
    name = o.getName().getStringValue();
    ontologyFile = o.getFile().getStringValue();
    if (o.getFilterOut() != null)
      filterOut = o.getFilterOut().getStringValue();
    slim = o.getSlim()!=null ? o.getSlim().getStringValue() : null;
    if (o.getReposSubdir()!=null)
      reposSubdir = o.getReposSubdir().getStringValue();
  }

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
    if (isBad(file))
      return;
    ontologyFile = file;
  }
  public String getFile() { return ontologyFile; }

  void setReposSubdir(String rs) { reposSubdir = rs; }

  public URL getReposUrl() throws MalformedURLException {
    String urlString="";
    String urlDir = getReposBaseDir();
    if (urlDir != null)
      urlString = urlDir;
    urlString += "/" + reposSubdir + "/" + getFile();
    return new URL(urlString);
  }

  private String getReposBaseDir() {
    return Config.inst().getReposUrlDir();
  }

  public boolean hasReposUrl() {
    if (getFile() == null) return false; // shouldnt happen
    // repos base dir is just a convenience
    if (reposSubdir == null) //&& getReposBaseDir() == null)
      return false;
    //try { getReposUrl(); } catch (MalformedURLException e) { return false; } ??
    return true;
  }

  private boolean isBad(String s) {
    return s == null || s.trim().equals("");
  }

  public boolean hasOntology() {
    return ontologyFile != null;
  }

  public boolean hasFilter() { return filterOut != null; }
  public String getFilter() { return filterOut; } 

  public boolean hasSlim() { return slim != null; }
  public String getSlim() { return slim; }
}
