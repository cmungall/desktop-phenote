package phenote.config;

import java.util.ArrayList;
import java.util.List;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldEnum; // phase out

public class FieldConfig {

  private CharFieldEnum charFieldEnum; // phase out
  private CharField charField;
  private String label;
  // Entity field can have multiple ontologies
  private List<OntologyConfig> ontologyConfigList;
  private boolean isPostComp;
  private OntologyConfig postCompRelOntCfg;
  private String syntaxAbbrev;

//   FieldConfig(CharFieldEnum c,OntologyConfig o) {
//     charFieldEnum = c;
//     if (o == null) return; // shouldnt happen
//     label = o.getName();
//     addOntologyConfig(o);
//   }

  FieldConfig(CharFieldEnum c, String label) {
    charFieldEnum = c;
    this.label = label;
  }

  /** No char field enum - its a generic not in hard wired datamodel! */
  FieldConfig(String label) {
    this.label = label;
    //isGeneric = true;
  }

  /** with generic fields these are going to become pase' */
  public CharFieldEnum getCharFieldEnum() { return charFieldEnum; }

  public boolean hasCharFieldEnum() { return charFieldEnum != null; }

  // --> getName?
  public String getLabel() { return label; }
  boolean hasLabel(String label) { return label.equals(this.label); } 

  //private void setOntologyFile(String f){getOntologyConfig().setOntologyFile(f);}
  public boolean hasOntologies() {
    return ontologyConfigList != null && !ontologyConfigList.isEmpty();
  }

  public boolean hasOntology() { return hasOntologies(); }

  // assume only 1 ontology???
  public OntologyConfig getOntologyConfig() {
    if (!hasOntologies()) { // probably doenst happen, just in case...
      addOntologyConfig(new OntologyConfig());
    }
    return getOntologyConfigList().get(0);
  }

  void addOntologyConfig(OntologyConfig o) {
    if (o == null)
      return;
    getOntologyConfigList().add(o);
  }

  void setOntologyConfigList(List<OntologyConfig> configs) {
    ontologyConfigList = configs;
  }

  public List<OntologyConfig> getOntologyConfigList() {
    if (ontologyConfigList == null)
      ontologyConfigList = new ArrayList<OntologyConfig>(1);
    return ontologyConfigList;
  }

  // POST COMP config... 
  void setIsPostComp(boolean isPostComp) {
    this.isPostComp = isPostComp;
  }
  public boolean isPostComp() {
    return isPostComp;
  }
  void setPostCompRelOntCfg(OntologyConfig oc) {
    postCompRelOntCfg = oc;
  }

  /** If isPostComp() return relationship ontology filename */
  public OntologyConfig getPostCompRelOntCfg() {
    return postCompRelOntCfg;
  }

  void setSyntaxAbbrev(String syn) {
    this.syntaxAbbrev = syn;
  }
  //void String getSyntaxAbbrev() { return syntaxAbbrev; }
  boolean hasSyntaxAbbrev(String abb) { return abb.equals(syntaxAbbrev); }

  public void setCharField(CharField cf) { charField = cf; }
  CharField getCharField() { return charField; }
}
