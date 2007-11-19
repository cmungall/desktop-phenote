package phenote.datamodel;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBOSession;
import org.obo.postcomp.ParseException;
import org.obo.postcomp.PostcompUtil;
import org.obo.postcomp.TokenMgrError;
import org.obo.util.TermUtil;
import org.oboedit.controller.SessionManager;

import phenote.config.Config;

//import phenote.datamodel.CharFieldEnum;
//import phenote.datamodel.OboUtil;

/** Manages all of the ontologies. Should there be an ontology package - whats funny
    is that ontologies have obo filenames that they parse so they are sort of
    data adapterish 
    actually manages CharFields(which may have ontologies) - rename CharFieldManager? yes!
    gets initialized by OntologyDataAdapter which loops through FieldConfigs and passes
    CharFields to OntMan */
public class CharFieldManager {


  private static CharFieldManager singleton;
  /** CharFields generically hold zero or more ontologies - are charFields that dont
   have ontologies in this list?? yes */
  private List<CharField> charFieldList = new ArrayList<CharField>();
  /** OBOSession holds all ontologies */
  private OBOSession oboSession;

  public final static String DEFAULT_GROUP = "default";


  /** Singleton */
  private CharFieldManager() {
    // always have user & date field by default
    // should be at end in case displayed??
//     CharField dateCreated =
//       new CharField("Date Created","date_created",Type.DATE);
//     addField(dateCreated);
  }

  public static CharFieldManager inst() {
    if (singleton == null)
      singleton = new CharFieldManager();
    return singleton;
  }
  
  public static void reset() {
    singleton = null;
  }

  /** OntologyDataAdapter adds fields */
  public void addField(CharField cf) {
    charFieldList.add(cf);
  }

  public void setOboSession(OBOSession s) {
    // set in obo edit
    SessionManager.getManager().setSession(s);
    oboSession = s;
  }
  public OBOSession getOboSession() {
    // get from obo edit?
    if (oboSession == null) SessionManager.getManager().getSession();
    return oboSession;
  }

  /** returns relation of id from obo session, null if dont have */
  public OBOProperty getRelation(String id) {
    for (OBOProperty p : TermUtil.getRelationshipTypes(oboSession)) {
      if (p.getID().equals(id))
        return p;
    }
    return null;
  }

  /** get char field for int. 0 based. based on order in config file
   CharacterTableModel relies on this order */
  public CharField getCharField(int i) throws OntologyException {
    if (i<0) throw new OntologyException("error, asking for negative char field"); 
    if (i >= charFieldList.size())
      throw new OntologyException("number "+i+" char field does not exist"); // ex
    return charFieldList.get(i);
  }

  public CharField getCharField(int i, String group) throws OntologyException {
    if (i<0) throw new OntologyException("error, asking for negative char field"); 
    if (i >= getNumberOfFields(group))
      throw new OntologyException("number "+i+" char field does not exist");
    return getCharFieldListForGroup(group).get(i);
  }

  public CharField getCharFieldForName(String fieldName) throws CharFieldException {
    if (fieldName == null) throw new CharFieldException("Char field string is null");
    for (CharField cf : getCharFieldList()) {
      //if (cf.getName().equalsIgnoreCase(fieldName))
      if (cf.isField(fieldName)) // checks name and datatag
        return cf;
    }
    throw new CharFieldException("No field for "+fieldName);
  }

  // ?
  private CharField getCharFieldForEnum(CharFieldEnum en) throws CharFieldException {
    try { return getCharFieldForName(en.getName()); }
    catch (CharFieldException e) { return getCharFieldForName(en.getTag()); }
  }

  /** If date_created doesnt exist then create it - its a fundamental */
  public CharField getDateCreatedField() {
    try { return getCharFieldForEnum(CharFieldEnum.DATE_CREATED); }
    catch (CharFieldException e) {
      // if !config.doCreateDate throw CFex??
      CharField dateCreated = new CharField(CharFieldEnum.DATE_CREATED);
      addField(dateCreated);
      return dateCreated;
    }
  }


  /** This is where the ontologies are in a generic fashion. A char field
      has one or more ontologies (entity char field often has more than ontology)*/
  public List<CharField> getCharFieldList() { return charFieldList; }
  public int getNumberOfFields() { return charFieldList.size(); }
  private int getNumberOfFields(String group) {
    return getCharFieldListForGroup(group).size();
  }
  
  /** should this be stored as a data structure? */
  public List<CharField> getCharFieldListForGroup(String groupName) {
    List<CharField> charFields = new ArrayList<CharField>();
    final List<String> fieldNames = Config.inst().getFieldsInGroup(groupName); 
    for (CharField field : this.getCharFieldList()) {
      if (fieldNames.contains(field.getName())) {
        charFields.add(field);
      }
    }
    return charFields;
  }

  public static boolean isDefaultGroup(String s) {
    return DEFAULT_GROUP.equals(s);
  }

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
//    	for(IdentifiedObject temp : o.getOboSession().getObjects()) {
//    		if (temp.getID().startsWith("ZFS"))
//    			System.err.println("WHAT?!");
//    	}
      try { oboClass = o.getTerm(id); }
      catch (TermNotFoundException e) { continue; }
      if (oboClass != null)
        return oboClass;
    }
    // not sure if we need both onotlogy exception & termnotfoundEx ?? redundant?
    throw new TermNotFoundException("ID "+id+" not found in loaded ontologies");
  }

  // used to be used by phenoSynChar - no longer
//   public OBOClass getTermOrPostComp(String id) throws TermNotFoundException {
//     if (isPostComp(id))
//       return getPostComp(id);
//     else
//       return getOboClassWithExcep(id);
//   }

  // phase out - put ex in getOboClass!
  public OBOClass getOboClassWithExcep(String id) throws TermNotFoundException {
    OBOClass term = getOboClass(id);
    if (term == null)
      throw new TermNotFoundException("ID "+id+" not found in loaded ontologies");
    return term;
  }


  /** returns true if contains ^ - is this rather presumptious or is ^ reserved */
  boolean isPostComp(String id) {
    if (id == null) return false;
    return id.contains("^");
  }

  /** parse string GO:123^part_of(AO:345) into post comp obo class 
      This will be replaced with obo edits post comp parse utility */
  OBOClass getPostComp(OBOSession os, String id) throws TermNotFoundException {
//     Pattern pat = Pattern.compile("([^\\^]+)\\^([^\\(]*)\\(([^\\)]*)\\)");
//     Matcher m = pat.matcher(id);
//     boolean found = m.find();
//     if (!found) throw new TermNotFoundException("Invalid post comp expression "+id);
//     String genus,rel,diff;
//     try {
//       //log().debug("pattern found for "+id+"? "+found+" g0 "+m.group(0)+" g1 "+m.group(1)+" g2 "+m.group(2)+" g3 "+m.group(3));
//       genus = m.group(1);
//       rel = m.group(2);
//       diff = m.group(3);
//     } catch (RuntimeException e) { // IllegalState, IndexOutOfBounds
//       throw new TermNotFoundException("Invalid post comp expression "+id);
//     }

//     OBOClass gTerm = getOboClassWithExcep(genus); // throws ex
//     // OBOProperty = getOboRelationshipProperty(rel) - from rel obo - todo
//     OBOProperty p = new OBOPropertyImpl("OBO_REL:"+rel,rel);
//     OBOClass dTerm = getOboClassWithExcep(diff);
    
//     return OboUtil.makePostCompTerm(gTerm,p,dTerm);
    
    // from obo edit!
    try {
      // System.out.println("OntMan getting postcomp for "+id+" OS "+os);
      // obo session now has all ontologies!
      return PostcompUtil.createPostcompObject(os,id);
    }
    catch (ParseException e) {

      // ok this is hacky but pre1.2 phenote used relationship names instead of ids
      // eg located_in instead of OBO_REL:located_in, so before we throw our hands up
      // we should try slipping in an OBO_REL: and see if that works
      if (!id.contains("^OBO_REL:")) {
        log().error("Post comp failed for "+id+" gonna try inserting ^OBO_REL:");
        String oboRelId = id.replace("^","^OBO_REL:");
        try {
          OBOClass o = PostcompUtil.createPostcompObject(os,oboRelId);
          // no exception - it worked!
          log().error("inserting OBO_REL: worked - please update your datafile");
          return o;
        }
        catch (ParseException x) { log().error("Inserting ^OBO_REL: didnt work"); }
      }

      String m = "\nInvalid post comp expression "+id+" "+e.getMessage();
      log().error(m);
      throw new TermNotFoundException(m);
    }
    // post comp parser can throw a plain old exception on parse failure
    // this should be an exception not an error!!!
    catch (TokenMgrError x) {
      log().error("parse of post comp term failed "+x);
      throw new TermNotFoundException(x);
    }
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

