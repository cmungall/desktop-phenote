package phenote.dataadapter;

import java.util.List;

import org.geneontology.oboedit.datamodel.Namespace;
import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.OBOSession;
import org.geneontology.oboedit.datamodel.impl.OBOClassImpl;
import org.geneontology.oboedit.datamodel.impl.OBOSessionImpl;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyManager;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;

public class AllFieldsGroupAdapter implements GroupAdapterI {

  private CharChangeListener charListener = new AllCharChangeListener();
  private String group = "genotypeMaker"; // hardwire for now for testing
  private Namespace namespace;
  
  public AllFieldsGroupAdapter(String grp) {
    this.group = grp;
    namespace = new Namespace(group);
  }

  public boolean hasCharChangeListener() { return true; }

  public CharChangeListener getCharChangeListener() {
    return charListener;
  }

  private void loadUpMainField() {
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

  /** If CharField doesnt have Ontology yet, create one */
  private Ontology getDestinationOntology() {
    //new Throwable().printStackTrace();
    CharField cf = getDestinationCharField();
    if (!cf.hasOntologies()) {
      Ontology ont = new Ontology(cf.getName());
      cf.addOntology(ont);
    }
    return cf.getOntology(); // just assume theres only 1 ontol
  }

  private CharField getDestinationCharField() {
    // for now
    try { return OntologyManager.inst().getCharFieldForName("GT"); }
    catch (CharFieldException e) { return null; } // for now
  }

  // protected? - subclass override?
  protected OBOClass makeOboClassFromChar(CharacterI c) {
    if (c.hasNoContent()) return null; // ex?
    StringBuilder sb = new StringBuilder();
    for (CharField cf : c.getAllCharFields()) {
      if (c.hasValue(cf))
        sb.append(c.getValueString(cf)).append('_');
    }
    if (sb.length() == 0) return null; //?
    sb.deleteCharAt(sb.length()-1);
    String name = sb.toString();
    sb.insert(0,':');
    String id = sb.toString();
    OBOClass o = new OBOClassImpl(name,id);
    o.setNamespace(namespace); // ???
    return o;
  }


    

  private List<CharacterI> getGroupChars() {
    return CharacterListManager.getCharListMan(group).getCharacterList().getList();
  }

  private class AllCharChangeListener implements CharChangeListener {
    public void charChanged(CharChangeEvent e) {
      loadUpMainField();
    }
  }

}
