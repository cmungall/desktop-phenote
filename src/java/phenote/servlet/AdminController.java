package phenote.servlet;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyManager;

import java.util.List;

/**
 * Controller that serves the version page: lists ontologies and their
 * meta data.
 */
public class AdminController implements Controller {

  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

    AdminBean form = new AdminBean();
    List<Ontology> ontologies = OntologyManager.inst().getAllOntologies();
    form.setOntologies(ontologies);

    return new ModelAndView("admin", "formBean", form);
  }
}
