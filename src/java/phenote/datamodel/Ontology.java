package phenote.datamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.Instance;
import org.obo.datamodel.Namespace;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.TermCategory;
import org.obo.query.QueryEngine;
import org.obo.query.impl.CategoryObjQuery;
import org.obo.query.impl.NamespaceObjQuery;
import org.obo.util.QueryUtil;
import org.obo.util.TermUtil;

import phenote.config.OntologyConfig;
import phenote.config.xml.OnTheFlySlimTermDocument.OnTheFlySlimTerm;

/** Ontology represents at this point the contents of a single obo file (which can
    be more than one ontology) an ontology wraps an obo edit OBOSession - at this point
    there is one OBOSession per obo file - that may change in the future */
public class Ontology {

  private String name;
  //private String filename; // may neec to revive for version/metadata?
  private String version;
  private OBOSession oboSession;
  // presorting things is faster but problematic, as syns dont get sorted
  // if we could limit output(??) then sorting on fly might be fast enough
  // with limiting output may wanna do scoring but then lose alphabetical?
  // maybe have different modes/preferences
  // should switch from OBOClass to general OBOObject!
  //private Collection<OBOClass> sortedTerms; // was List
  private Collection<OBOObject> sortedTerms; // was List
  // switch to this!
  //private Collection<OBOObject> sortedObjTerms;
  //private Collection<OBOClass> sortedObsoleteTerms;
  private Collection<OBOObject> sortedObsoleteTerms;
  //private List<OBOProperty> sortedRelations;
  private List<OBOObject> sortedRelations; // more general
  private boolean hasInstances;
  // is it possible to merge sorted instances & sorted terms
  // as they are both LinkedObjects (and annotated obj)
  private Collection<Instance> sortedInstances;
  private String slim; 
  private boolean sortById = false;
//  private String filterOutString; // phase out
  /** well this stuff is specific to ontologies from files (eg obo), perhaps there
      needs to be some sort of wrapper or subclass? need to think about this...
      for now just shoving in here */
  private String source; // "source" is slightly generic isnt it?
  private OntologyConfig ontologyConfig;

  /** this may be general? maybe should be a real date - this is the time of the ontology
   for files this would be the modification date, for loading from database somewhere
  obd? zfin? this would be the time of the load */
  private long ontologyTimestamp;
  
  public Ontology(String name) {
    this.name = name;
  }

  public void setHasInstances(boolean hasInst) {
    this.hasInstances = hasInst;
  }

  /** is it funny to pass in a config object to datamodel - given that config dictates
      the model i actually think its ok */
  public Ontology(Collection<Namespace> spaces,OntologyConfig oc,OBOSession os) {
    oboSession = os;
    ontologyConfig = oc;
    // get id filter, slim, & possible namespace from oc...
    name = oc.getName(); // getName could just call oc.getName?
    slim = oc.getSlim();
    sortById = oc.sortById();
    // if namespace specified and is valid load that
    if (oc.hasNamespace()) {
      String namespace = oc.getNamespace();
      if (validNamespace(spaces,namespace)) {
        loadNamespace(getNamespace(spaces,namespace));
        return;
      }
    }
    // otherwise load all name spaces
    loadNamespaces(spaces);
  }

  private boolean validNamespace(Collection<Namespace> nl, String ns) {
    return getNamespace(nl,ns) != null;
  }
  private Namespace getNamespace(Collection<Namespace> nl,String namespaceString) {
    for (Namespace n : nl) {
      if (n.getID().equals(namespaceString)) return n;
    }
    return null;
  }

  public void setOboSession(OBOSession os) {
    oboSession = os;
    //System.out.println("Ont cats "+os.getCategories());
    makeSortedLists(oboSession);
    //filterLists();
  }

  public Date getOntologyDate() { return new Date(ontologyTimestamp); }

  public String getVersion() { 
    if (version != null) return version;
    // this is wrong - this just says file name loaded from. obo file puts version in
    // remark field but doesnt seem to be way to get that from obo session??
    //return oboSession.getCurrentHistory().getVersion();
    // no longer comes from obo session - but from OboFileAdapter - change this
    // but i dont think this works anyways as obos dont have versions
//     Object o = oboSession.getAdapterMetaData();
//     if (!(o instanceof OBOMetaData)) return "unknown"; // exception?
//     Collection c = ((OBOMetaData)o).getFileMetaData();
//     //System.out.println("# of file meta datas "+c.size()+c);
//     for (Object obj : c) {
//       if (!(obj instanceof FileMetaData)) return "unknown";
//       // hmmm dont know file name in ontology apparently - need to bring back!
//       // for now just return last one as there is only 1
//       version = ((FileMetaData)obj).getVersion();
//       //System.out.println("VERSION "+version);
//       //return version; ??
//     }
    return version;
  }

  private void makeSortedLists(OBOSession oboSession) {
    //log().debug("name "+name+" terms "+oboSession.getTerms()+" propVals "+oboSession.getPropertyValues()+" rels "+oboSession.getRelationshipTypes());
    //sortedTerms = sortTerms(TermUtil.getTerms(oboSession));
    sortedTerms = sortObjects(TermUtil.getTerms(oboSession));
    sortedObsoleteTerms = sortObjects(TermUtil.getObsoletes(oboSession));
    if (hasInstances) // work in progress
      sortedInstances = getSortedInstances(TermUtil.getInstances(oboSession));
  }

//   private void filterLists() {
//     //if (!haveFilter() && !hasSlim()) return; // froboSession.getTerm(id)om config
//     if (doFiltering())
//       sortedTerms = filterList(sortedTerms);
//   }


  public String getName() { return name; }

  /** throws TermNotFoundException if id is not found 
      searches non-obsolete & obsoletes*/
  public OBOClass getTerm(String id) throws TermNotFoundException {
    // this aint right - if its a slim should only search slim
    //OBOClass oc = oboSession.getTerm(id);
    for (OBOObject term : sortedTerms) {
      if (term.getID().equals(id) && term instanceof OBOClass)
        return (OBOClass)term;
    }
    for (OBOObject obs : sortedObsoleteTerms) {
      if (obs.getID().equals(id) && obs instanceof OBOClass)
        return (OBOClass)obs;
    }
    //if (term == null)

    throw new TermNotFoundException(id +" id not found in ontology "+name);
    //return oc;
  }

  // hmmmm - i think we may always want dangler mode that is putting danglers in
  // for terms not found
//   private boolean danglerMode() { 
//     return true;
//     //return false;
//   }

  /** Returns true if ontology holds term/oboClass */
  boolean hasTerm(OBOClass term) {
    // if this is too slow can do optimizations with prefixes
    try {getTerm(term.getID()); }
    catch (TermNotFoundException e) { return false; }
    return true; // no exception - it has it
  }

  /** gets relations, OBOProperties as OBOObjects */
  public List<OBOObject> getSortedRelations() {
    if (sortedRelations == null) {
      //sortedRelations=new ArrayList<OBOProperty>(); 
      List<OBOObject> sorRel = new ArrayList<OBOObject>();
      // if on the fly slim than just get terms from there
      if (hasOnTheFlySlim())
        sorRel.addAll(getOnTheFlySlimObjects());
      // this is bad, gets all rels, only want from configged field/ont
      else
        //sorRel.addAll(TermUtil.getRelationshipTypes(oboSession));
        sorRel.addAll(sortedTerms); // OBOObjects can be rels/props, rename!
      //Collections.sort(sorRel,new RelComparator());
      sortedRelations = sortOboObjects(sorRel);
    }
    return sortedRelations;
  }

//   private class RelComparator implements Comparator<OBOProperty> {
//     public int compare(OBOProperty r1, OBOProperty r2) {
//       return r1.toString().compareTo(r2.toString());
//     }
//     public boolean equals(OBOProperty r1, OBOProperty r2) {
//       return r1.toString().equals(r2.toString());
//     }
//   }

  private List<OBOObject> sortOboObjects(List<OBOObject> list) {
    Collections.sort(list,new OboObjComparator());
    return list;
  }

  /** refactor to use OBOObjects! a bit of work */
  private List<OBOClass> sortOboClasses(List<OBOClass> list) {
    Collections.sort(list,new OboClassComparator());
    return list;
  }

  private class OboObjComparator implements Comparator<OBOObject> {
    public int compare(OBOObject r1, OBOObject r2) {
      return r1.getName().compareTo(r2.getName()); // getName? toString?
    }
    public boolean equals(OBOProperty r1, OBOProperty r2) {
      return r1.getName().equals(r2.getName());
    }
  }
  private class OboClassComparator implements Comparator<OBOClass> {
    public int compare(OBOClass o1, OBOClass o2) {
      return o1.getName().compareTo(o2.getName()); // getName? toString?
    }
    public boolean equals(OBOProperty o1, OBOProperty o2) {
      return o1.getName().equals(o2.getName());
    }
  }


  /** non obsolete terms - sorted */
  //public Collection<OBOClass> getSortedTerms() {
  public Collection<OBOObject> getSortedTerms() { // getSortedObjects?
    return sortedTerms;
  }

  public Collection<OBOObject> getSortedObsoleteTerms() {
    return sortedObsoleteTerms;
  }


  public OBOSession getOboSession() { return oboSession; }

  private List<OBOClass> sortTerms(Collection terms) {
    List<OBOClass> sortedTerms = new ArrayList<OBOClass>();
    sortedTerms.addAll(terms);
    Collections.sort(sortedTerms);
    return sortedTerms;
  }

  private List<OBOObject> sortObjects(Collection terms) {
    List<OBOObject> sortedTerms = new ArrayList<OBOObject>();
    sortedTerms.addAll(terms);
    Collections.sort(sortedTerms);
    return sortedTerms;
  }


  // can this be merged in with sortTerms - return List<AnnotatedObj>?
  // this isnt used yet - work in progress, instance is not a comparable
  // also should be renamed sortInstances as this is doing the sorting or should be
  public List<Instance> getSortedInstances(Collection instances) {
    List<Instance> sortedInstances = new ArrayList<Instance>();
    sortedInstances.addAll(instances);
    // Instance doesnt work - Instance is not a Comparable!!!
    //Collections.sort(sortedInstances);
    return sortedInstances;
  }

  /** might move this elsewhere - subclass? data adap specific wrapper? */
  public void setTimestamp(long t) {
    ontologyTimestamp = t;
    log().info("\n"+getName()+" Ontology date: "+getOntologyDate()
               +"\nVersion: "+getVersion());
  }
  public long getTimestamp() { return ontologyTimestamp; }
  /** for obo files this is the filename */
  public void setSource(String s) { source = s; }
  public String getSource() { return source; }


  /** should be smarter about slims - currently has Ontology & obo session for each field
      that wants is using a slim of the same ontology - really should be one ontology & 
      1 obo session with multiple slims - and even gather all the slims in 1 pass - would
      need to query all ontology configs ahead of time to gather all slims being queried
      by phenote - no need to build slim data structure if not being used */
  public void setSlim(String slim) { this.slim = slim; }
  private boolean hasSlim() { return slim != null; }
  private boolean inSlim(OBOClass term) {
    if (!hasSlim()) return true; // no slim - everything qualifies/doesnt get filtered
    for (Object category : term.getCategories()) {
      if (((TermCategory)category).getName().equalsIgnoreCase(slim))
        return true;
    }
    return false; // none of the terms slims are in
  }

//   public void setFilter(String filterOutString) {
//     this.filterOutString = filterOutString;
//   }
//   private String getFilter() { return filterOutString; }
  
//   private boolean filterOut(OBOClass term) {
//     if (!hasFilter()) return false;
//     return term.getID().startsWith(getFilter());
//   }

//   private boolean hasFilter() {
//     return filterOutString != null;
//     // minimally filter out obo: obo edit internal terms
//     //return true;
//   }

  private boolean doFiltering() {
    return filterOboArtifacts() || hasSlim(); // || hasFilter()
  }

  private boolean filterOboArtifacts() { return true; }

  private boolean isOboArtifact(OBOClass term) {
    if (!filterOboArtifacts()) return false;
    return term.getName().startsWith("obo:");
  }


  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }

  private void loadNamespace(Namespace n) {
    Collection<Namespace> c = new ArrayList(1);
    c.add(n);
    loadNamespaces(c);
  }

  /** this is the workhorse. this loads namespaces (usually come from obo file)
      as in all terms & such from namespace, uses QueryEngine */
  private void loadNamespaces(Collection<Namespace> spaces) {
    if (spaces == null || spaces.isEmpty()) {
      log().error("No namespace to load"); // ex?
      return;
    }
    
    // for now just grab 1st namespace as namespacequery only takes 1 namespace
    Namespace[] spacesArray = spaces.toArray(new Namespace[0]);
    // NamespaceQuery makes OBOClasses, need to change to make OBOObjects!
    // or make new query
    //NamespaceQuery nsQuery = new NamespaceQuery(spacesArray);
    NamespaceObjQuery nsQuery = new NamespaceObjQuery(spacesArray);
    // sortById is really a hack so zfin can have stages sorted (which happen to be
    // sorted by id), obo needs to have a sorting thingy
    if (sortById) nsQuery.setComparator(new IdComparator()); // inner class
    // create a new query engine on the session we just loaded
    QueryEngine engine = new QueryEngine(oboSession);
    
    // run the namespace query and dont cache the results(false)
    // BUG - this includes obsoletes! (fixed?)
    nsQuery.setAllowObsoletes(false);
    // NameSpaceQuery sorts by term name
    sortedTerms = engine.query(nsQuery, false);

    nsQuery.setAllowObsoletes(true);
    nsQuery.setAllowNonObsoletes(false);
    sortedObsoleteTerms = engine.query(nsQuery, false);

    if (hasOnTheFlySlim()) {
      // can either make slim for query below - or just make slim by hand
      // might as well do by hand
      sortedTerms = getOnTheFlySlimObjectsSorted();
      // empty out obsoletes?
    }
    // shouldnt have both real slim and on the fly slim right?
    // Category is what obo calls slim
    else if (hasSlim()) {
      CategoryObjQuery catQuery = new CategoryObjQuery(slim);
      sortedTerms = QueryUtil.getResults(engine.query(sortedTerms,catQuery));
      // obsoletes?
      sortedObsoleteTerms =
        QueryUtil.getResults(engine.query(sortedObsoleteTerms,catQuery));
    }

  }

  private boolean hasOnTheFlySlim() {
    //if (ontologyConfig==null) return false; return ontologyConfig.hasOnTheFlySlim();
    return getOnTheFlySlimObjects()!=null && !getOnTheFlySlimObjects().isEmpty();
  }

  private List<OBOObject> onTheFlySlimList=null;

  /** an onTheFlySlim is a slim that is set up in the phenote config file rather than
      in an obo file as most slims are done
      returns null if dont have on the fly slim terms */
  private List<OBOObject> getOnTheFlySlimObjects() {
    if (onTheFlySlimList!=null) return onTheFlySlimList;
    if (ontologyConfig == null) return null;
    List<OBOObject> list = new ArrayList<OBOObject>();
    OnTheFlySlimTerm[] termBeans = ontologyConfig.getOnTheFlySlimTerms();
    for (OnTheFlySlimTerm termBean : termBeans) {
      String id = termBean.getTerm();
      if (id==null) continue;
      IdentifiedObject io = oboSession.getObject(id);
      if (!(io instanceof OBOObject)) {
        log().error(io+" not instance of OBOObject??"); // shouldnt happen
        continue;
      }
      list.add((OBOObject)io);
    }
    onTheFlySlimList = list;
    return onTheFlySlimList;
  }
  private List<OBOClass> onTheFlySlimClassList=null;

  /** an onTheFlySlim is a slim that is set up in the phenote config file rather
      than in an obo file as most slims are done
      returns null if dont have on the fly slim terms
      im dying to refactor obo classes to obo objects so dont have this redundancy
      but that will take a bit of work - for one NamespaceQuery yada yada*/
  private List<OBOClass> getOnTheFlySlimClasses() {
    if (onTheFlySlimClassList!=null) return onTheFlySlimClassList;
    if (ontologyConfig == null) return null;
    List<OBOClass> list = new ArrayList<OBOClass>();
    OnTheFlySlimTerm[] termBeans = ontologyConfig.getOnTheFlySlimTerms();
    for (OnTheFlySlimTerm termBean : termBeans) {
      String id = termBean.getTerm();
      if (id==null) continue;
      IdentifiedObject io = oboSession.getObject(id);
      if (!(io instanceof OBOClass)) {
        log().error(io+" not instance of OBOClass??"); // shouldnt happen
        continue;
      }
      list.add((OBOClass)io);
    }
    onTheFlySlimClassList = list;
    return onTheFlySlimClassList;
  }

  /** returns null if dont have on the fly slim terms
   cant use this yet for terms - as havent refactored OBOClasses to OBOObjects 
   bummer */
  private List<OBOObject> getOnTheFlySlimObjectsSorted() {
    if (!hasOnTheFlySlim()) return null; // ex?
    List<OBOObject> list = getOnTheFlySlimObjects();
    return sortOboObjects(list); // cache?
  }

  /** redundancy waiting for refactor */
  private List<OBOClass> getOnTheFlySlimClassesSorted() {
    if (!hasOnTheFlySlim()) return null; // ex?
    List<OBOClass> list = getOnTheFlySlimClasses();
    return sortOboClasses(list); // cache?
  }

  public OntologyConfig getOntologyConfig() {
  	return ontologyConfig;
  }

  // hacky id sort for zfin stages
  private class IdComparator implements Comparator<OBOObject> {
    public int compare(OBOObject o1, OBOObject o2) {
      return o1.getID().compareToIgnoreCase(o2.getID());
    }
  }

//   /** This is not generic - this looks for ids that have the filterOut string
//       as a prefix and tosses them - for example "ZFS" filters out all zf stage
//       terms - can add more flexibility as needed - this is all thats needed for now
//       also filters out obo: terms - those are obo edit artifacts i think 
//       also filters for slim! - phase out for obo edit stuff
//       this is pase now im pretty sure as loadNamespaces takes care of all this
//       via namespace & slim querying
//   */
//   private List<OBOClass> filterList(Collection<OBOClass> list) {
//     List<OBOClass> filteredList = new ArrayList<OBOClass>();
//     for (OBOClass term : list) {
//       // or could do remove on list?
//       // also filter out obo: terms as they are internal obo edit thingies it seems
//       // funny logic but more efficient to do in one pass - refactor somehow?
//       //if (term.getName().startsWith("obo:"))
//       if (isOboArtifact(term))
//         continue; // filter our obo:
//       // this is no longer used
// //       if (filterOut(term))
// //         continue;
//       if (!inSlim(term))
//         continue;
//       filteredList.add(term); // passed 2 filters above - add it
//     }
//     return filteredList;
//   }

}

//long time = System.currentTimeMillis();
    //log().debug("Obsolete: got " + sortedTerms.size()+" namespace hits in " + (System.currentTimeMillis() - time)+ "ms # of namespaces: "+spaces.size());
    //sortedTerms = QueryUtil.getResults(engine.query(nsQuery, false));
    //log().debug(spacesArray[0]+" Non-obsolete: got " + sortedTerms.size()+" namespace hits in " + (System.currentTimeMillis() - time)+ "ms # of namespaces: "+spaces.size());
//     int i=0;
//     for (OBOClass oc : sortedTerms) {
//       System.out.println(++i +" "+spacesArray[0]+" "+oc.getName()+" ns:"+oc.getNamespace());
//     }
//      System.out.println("Category query (" +slim+ ") got "+sortedTerms.size()+" in " + (System.currentTimeMillis() - time) + "ms");
//   private class IdSortedNamespaceQuery extends NamespaceQuery {
//     protected Comparator<OBOClass> comparator = new Comparator<OBOClass>() {
//       public int compare(OBOClass o1, OBOClass o2) {
//         return o1.getID().compareToIgnoreCase(o2.getID());
//       }
//     };
//   }
