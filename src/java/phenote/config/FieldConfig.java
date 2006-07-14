package phenote.config;

import java.util.ArrayList;
import java.util.List;

import phenote.datamodel.CharField.CharFieldEnum;

public class FieldConfig {

  private CharFieldEnum charFieldEnum;
  private String label;
  // Entity field can have multiple ontologies
  private List<OntologyConfig> ontologyConfigList;

  FieldConfig(CharFieldEnum c,OntologyConfig o) {
    charFieldEnum = c;
    if (o == null) return; // shouldnt happen
    label = o.getName();
    addOntologyConfig(o);
  }

  FieldConfig(CharFieldEnum c, String label) {
    charFieldEnum = c;
    this.label = label;
  }

  public CharFieldEnum getCharFieldEnum() { return charFieldEnum; }

  // --> getName?
  public String getLabel() { return label; }

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
    
}
