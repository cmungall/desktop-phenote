package phenote.servlet;

import phenote.datamodel.Ontology;

import java.util.List;

/**
 * Bean used on the admin page that shows information about the ontologies
 * loaded into Phenote. 
 */
public class AdminBean {

  private List<Ontology> ontologies;

  public List<Ontology> getOntologies() {
    return ontologies;
  }

  public void setOntologies(List<Ontology> ontologies) {
    this.ontologies = ontologies;
  }
}
