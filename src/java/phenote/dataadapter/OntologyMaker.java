package phenote.dataadapter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import org.geneontology.oboedit.datamodel.OBOSession;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.Ontology;

public abstract class OntologyMaker implements OntologyMakerI {
  
  // --> List - can have multiple destinations
  //private CharField destinationCharField;
  private List<CharField>destinationCharFields = new ArrayList<CharField>(4);

  public void addDestinationField(String fieldName) {
    try {
      CharField cf = CharFieldManager.inst().getCharFieldForName(fieldName);
      destinationCharFields.add(cf);
    }
    catch (CharFieldException e) { // popup? throw ex?
      log().error("Cant find destination field "+fieldName+" for ontology maker. "
                  +"Check config file.");
    }
  }

  public boolean useButtonToLaunch() { return false; }
  public String getButtonText() { return null; } // ""?

  protected void setOboSession(OBOSession os) {
    for (Ontology o : getDestinationOntologies())
      o.setOboSession(os);
  }
  
  /** If CharField doesnt have Ontology yet, create one, assumes thers only one ontol
      per char field - ok for now */
  private List<Ontology> getDestinationOntologies() {
    List<Ontology> ontList = new ArrayList<Ontology>();
    for (CharField cf : destinationCharFields) {
    
      //CharField cf = getDestinationCharField();
      if (!cf.hasOntologies()) {
        Ontology ont = new Ontology(cf.getName());
        cf.addOntology(ont);
      }
      //return cf.getOntology(); // just assume theres only 1 ontol
      ontList.add(cf.getOntology());
    }
    return ontList;
  }
  protected boolean hasDestinationCharField() {
    return !destinationCharFields.isEmpty();
  }

//   private CharField getDestinationCharField() {
//     return destinationCharField;
//   }

  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }


}
