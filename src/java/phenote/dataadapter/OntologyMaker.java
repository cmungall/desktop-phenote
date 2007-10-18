package phenote.dataadapter;

import org.apache.log4j.Logger;

import org.geneontology.oboedit.datamodel.OBOSession;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.Ontology;

public abstract class OntologyMaker implements OntologyMakerI {
  
  // --> List
  private CharField destinationCharField;

  public void setDestinationField(String fieldName) {
    try {
      destinationCharField = CharFieldManager.inst().getCharFieldForName(fieldName);
    }
    catch (CharFieldException e) { // popup? throw ex?
      log().error("Cant find destination field "+fieldName+" for ontology maker. "
                  +"Check config file.");
    }
  }

  public boolean useButtonToLaunch() { return false; }
  public String getButtonText() { return null; } // ""?

  protected void setOboSession(OBOSession os) {
    getDestinationOntology().setOboSession(os);
  }
  
  /** If CharField doesnt have Ontology yet, create one */
  private Ontology getDestinationOntology() {
    if (!hasDestinationCharField()) return null;

    CharField cf = getDestinationCharField();
    if (!cf.hasOntologies()) {
      Ontology ont = new Ontology(cf.getName());
      cf.addOntology(ont);
    }
    return cf.getOntology(); // just assume theres only 1 ontol
  }
  protected boolean hasDestinationCharField() {
    return getDestinationCharField() != null;
  }

  private CharField getDestinationCharField() {
    return destinationCharField;
  }

  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }


}
