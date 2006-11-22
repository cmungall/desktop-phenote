package phenote.config;

/** May not even have ontology file (free text eg genotype) rename FieldConfig? */
public class OntologyConfig {

  public String name;
  public String ontologyFile;
  //public String nameSpace; // not used yet - or was this what filter out ended up
  public String filterOut; // public?
  private String slim;

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
