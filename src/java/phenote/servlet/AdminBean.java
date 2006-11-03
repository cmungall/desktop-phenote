package phenote.servlet;

import phenote.datamodel.Ontology;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Christian Pich
 * Date: Nov 3, 2006
 * Time: 10:57:20 AM
 * To change this template use File | Settings | File Templates.
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
