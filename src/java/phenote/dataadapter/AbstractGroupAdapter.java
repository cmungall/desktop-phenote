package phenote.dataadapter;

import java.util.List;

import org.apache.log4j.Logger;

import org.geneontology.oboedit.datamodel.Namespace;
import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.OBOSession;
import org.geneontology.oboedit.datamodel.impl.OBOSessionImpl;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyManager;
import phenote.edit.CharChangeListener;
import phenote.edit.CharChangeEvent;

abstract class AbstractGroupAdapter implements GroupAdapterI {

  private CharChangeListener charListener;
  private String group;
  private Namespace namespace;
  private CharField destinationCharField;

  protected AbstractGroupAdapter(String group)  {
    this.group = group;
    namespace = new Namespace(group);
    setCharChangeListener(new GroupCharChangeListener());
  }

  public boolean hasCharChangeListener() { return charListener != null; }

  public CharChangeListener getCharChangeListener() {
    return charListener;
  }
  protected void setCharChangeListener(CharChangeListener l) {
    charListener = l;
  }

  protected Namespace getNamespace() { return namespace; }

  /** This is the real workhorse here */
  protected void loadUpMainField() {
    if (!hasDestinationCharField()) return; // err msg?
    // clear out old obo classes??? query be namespace and OBOSess.removeObject?

    // make new OBOSession?? or use existing OBOSession - might be bad for bridge?
    OBOSession os = new OBOSessionImpl();

    // make OBOClasses from characters
    for (CharacterI c : getGroupChars()) {
      OBOClass o = makeOboClassFromChar(c);
      if (o == null) continue;
      os.addObject(o);
    }

    // load obo classes into main field -> namespace query? 
    getDestinationOntology().setOboSession(os);
  }

  /** This is where subclasses come in and do their thing */
  protected abstract OBOClass makeOboClassFromChar(CharacterI c);

  public void setDestinationField(String fieldName) {
    try {
      destinationCharField = OntologyManager.inst().getCharFieldForName(fieldName);
    }
    catch (CharFieldException e) { // popup? throw ex?
      log().error("Cant find destination field "+fieldName+" for group "+group
                  +" Check config file.");
    }
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
  private boolean hasDestinationCharField() {
    return getDestinationCharField() != null;
  }

  private CharField getDestinationCharField() {
    return destinationCharField;
  }

  private List<CharacterI> getGroupChars() {
    return CharacterListManager.getCharListMan(group).getCharacterList().getList();
  }

  /** just triggers loadUpMainField */
  private class GroupCharChangeListener implements CharChangeListener {
    public void charChanged(CharChangeEvent e) {
      loadUpMainField();
    }
  }
  
  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }

}
