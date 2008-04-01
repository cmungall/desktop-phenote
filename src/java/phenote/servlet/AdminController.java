package phenote.servlet;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import phenote.datamodel.CharFieldManager;
import phenote.datamodel.Ontology;

/**
 * Controller that serves the version page: lists ontologies and their
 * meta data.
 */
public class AdminController implements Controller {

  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

    AdminBean form = new AdminBean();
    List<Ontology> ontologies = CharFieldManager.inst().getAllOntologies();
    form.setOntologies(ontologies);

    return new ModelAndView("admin", "formBean", form);
  }
}
