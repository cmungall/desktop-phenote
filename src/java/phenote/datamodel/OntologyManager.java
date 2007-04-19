package phenote.datamodel;

import org.apache.log4j.Logger;
import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.OBOProperty;
import org.geneontology.oboedit.datamodel.impl.OBOPropertyImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import phenote.datamodel.CharFieldEnum;
//import phenote.datamodel.OboUtil;

/** Manages all of the ontology. Eventually will get config info (xml? OntologyConfig?)
    and set itself up from that. Should there be an ontology package - whats funny
    is that ontologies have obo filenames that they parse so they are sort of
    data adapterish 
    actually manages CharFields(which may have ontologies) - rename CharFieldManager?*/
public class OntologyManager {


  private static OntologyManager singleton;
  // isnt this redundant with charFieldList? convenience? - phase out! just getOClass
  //private List<Ontology> allOntologyList = new ArrayList<Ontology>();
  /** CharFields generically hold one or more ontologies - are charFields that dont
   have ontologies in this list?? not sure */
  private List<CharField> charFieldList = new ArrayList<CharField>(6);


  /** Singleton */
  private OntologyManager() {}
  
  public static OntologyManager inst() {
    if (singleton == null)
      singleton = new OntologyManager();
    return singleton;
  }

  
  public void addField(CharField cf) {
    //addOntologyList(cf.getOntologyList());
    charFieldList.add(cf);
  }

  /** get char field for int. 0 based. based on order in config file */
  public CharField getCharField(int i) throws Exception {
    if (i<0) throw new Exception(); 
    if (i >= charFieldList.size()) throw new Exception(); // ex
    return charFieldList.get(i);
  }

  /** This is where the ontologies are in a generic fashion. A char field
      has one or more ontologies (entity char field often has more than ontology)*/
  public List<CharField> getCharFieldList() { return charFieldList; }
  public int getNumberOfFields() { return charFieldList.size(); }

  public List<Ontology> getAllOntologies() {
    List<Ontology> ontologies = new ArrayList<Ontology>();
    for (CharField cf : charFieldList)
      ontologies.addAll(cf.getOntologyList());
    return ontologies;
  }

  /** Returns ontology with name, null if not found */
  public Ontology getOntologyForName(String ontologyName) { // static?
    for (CharField cf : inst().getCharFieldList()) {
      if (cf.hasOntology(ontologyName))
        return cf.getOntologyForName(ontologyName);
    }
    return null;
  }

  /** Searches all ontologies for id - this could be even more savvy and utilize
      the id prefix AO,GO,PATO... 
      Should this deal with post comp? if ^ then create a post comp term on fly? 
      im not sure if this is the right place for it, maybe method should be renamed
      but ill put it here for now 
      merge this with getOboClassWithEx? */
  public OBOClass getOboClass(String id) throws TermNotFoundException {
    OBOClass oboClass;
// this seems to be the sole reason for ontology list - silly! use char fields!
    //Iterator<Ontology> iter = getAllOntologies().iterator(); // allOntologyList
    //while (iter.hasNext()) {
    for (Ontology o : getAllOntologies()) {
      //Ontology o = iter.next();
      try { oboClass = o.getTerm(id); }
      catch (TermNotFoundException e) { continue; }
      if (oboClass != null)
        return oboClass;
    }
    // not sure if we need both onotlogy exception & termnotfoundEx ?? redundant?
    throw new TermNotFoundException("ID "+id+" not found in loaded ontologies");
  }

  public OBOClass getTermOrPostComp(String id) throws TermNotFoundException {
    if (isPostComp(id))
      return getPostComp(id);
    else
      return getOboClassWithExcep(id);
  }

  // phase out - put ex in getOboClass!
  public OBOClass getOboClassWithExcep(String id) throws TermNotFoundException {
    OBOClass term = getOboClass(id);
    if (term == null)
      throw new TermNotFoundException("ID "+id+" not found in loaded ontologies");
    return term;
  }


  boolean isPostComp(String id) {
    if (id == null) return false;
    return id.contains("^");
  }

  /** parse string GO:123^part_of(AO:345) into post comp obo class 
      This will be replaced with obo edits post comp parse utility */
  OBOClass getPostComp(String id) throws TermNotFoundException {
    Pattern pat = Pattern.compile("([^\\^]+)\\^([^\\(]*)\\(([^\\)]*)\\)");
    Matcher m = pat.matcher(id);
    boolean found = m.find();
    if (!found) throw new TermNotFoundException("Invalid post comp expression "+id);
    String genus,rel,diff;
    try {
      //log().debug("pattern found for "+id+"? "+found+" g0 "+m.group(0)+" g1 "+m.group(1)+" g2 "+m.group(2)+" g3 "+m.group(3));
      genus = m.group(1);
      rel = m.group(2);
      diff = m.group(3);
    } catch (RuntimeException e) { // IllegalState, IndexOutOfBounds
      throw new TermNotFoundException("Invalid post comp expression "+id);
    }

    OBOClass gTerm = getOboClassWithExcep(genus); // throws ex
    // OBOProperty = getOboRelationshipProperty(rel) - from rel obo - todo
    OBOProperty p = new OBOPropertyImpl("OBO_REL:"+rel,rel);
    OBOClass dTerm = getOboClassWithExcep(diff);
    
    return OboUtil.makePostCompTerm(gTerm,p,dTerm);
  }
  
  /** Currently iterates through every ontology looking for term, if this proves too
      inefficient we could do something with ID prefixes */
  public Ontology getOntologyForTerm(OBOClass term) throws OntologyException {
    for (CharField cf : charFieldList) {
      for (Ontology o : cf.getOntologyList())
        if (o.hasTerm(term))
          return o;
    }
    throw new OntologyException(term+" not found in ontologies");
  }


  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }

}

  // previously used by term info
//   /** for obo class find its char field enum via ontology & char field */
//   public CharFieldEnum getCharFieldEnumForOboClass(OBOClass oboClass) {
//     for (CharField cf : charFieldList) {
//       //if (!cf.hasOntologies()) continue; // is this needed? not sure
//       for (Ontology ont : cf.getOntologyList()) {
//         if (ont.hasOboClass(oboClass))
//           return cf.getCharFieldEnum();
//       }
//     }
//     return null; // this shouldnt happen - err msg?
//   } 

