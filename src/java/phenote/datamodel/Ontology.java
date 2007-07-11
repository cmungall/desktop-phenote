package phenote.datamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import org.geneontology.oboedit.datamodel.Namespace;
import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.OBOProperty;
import org.geneontology.oboedit.datamodel.OBOSession;
import org.geneontology.oboedit.datamodel.TermCategory;
import org.geneontology.oboedit.datamodel.TermUtil;
import org.geneontology.oboedit.dataadapter.OBOMetaData;
import org.geneontology.oboedit.dataadapter.OBOMetaData.FileMetaData;
import org.geneontology.oboedit.query.Query;
import org.geneontology.oboedit.query.QueryEngine;
import org.geneontology.oboedit.query.impl.NamespaceQuery;
import org.geneontology.oboedit.query.impl.CategoryQuery;

// i think this is ok since datamodel is based on config?? 
// otherwise ODA should be inbetween
import phenote.config.OntologyConfig;

/** Ontology represents at this point the contents of a single obo file (which can
    be more than one ontology) an ontology wraps an obo edit OBOSession - at this point
    there is one OBOSession per obo file - that may change in the future */
public class Ontology {

  private String name;
  //private String filename; // may neec to revive for version/metadata?
  private String version;
  private OBOSession oboSession;
  private Collection<OBOClass> sortedTerms; // was List
  private Collection<OBOClass> sortedObsoleteTerms;
  private List<OBOProperty> sortedRelations;
  private String slim; 
  private String filterOutString;
  /** well this stuff is specific to ontologies from files (eg obo), perhaps there
      needs to be some sort of wrapper or subclass? need to think about this...
      for now just shoving in here */
  private String source; // "source" is slightly generic isnt it?

  /** this may be general? maybe should be a real date - this is the time of the ontology
   for files this would be the modification date, for loading from database somewhere
  obd? zfin? this would be the time of the load */
  private long ontologyTimestamp;
  
  public Ontology(String name) {
    this.name = name;
  }

  /** is it funny to pass in a config object to datamodel - given that config dictates
      the model i actually think its ok */
  public Ontology(Collection<Namespace> spaces,OntologyConfig oc,OBOSession os) {
    oboSession = os;
    // get id filter, slim, & possible namespace from oc...
    name = oc.getName();
    slim = oc.getSlim();
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
    filterLists();
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
    sortedTerms = getSortedTerms(TermUtil.getTerms(oboSession));//(oboSession.getTerms());
    sortedObsoleteTerms = getSortedTerms(TermUtil.getObsoletes(oboSession));
  }

  private void filterLists() {
    //if (!haveFilter() && !hasSlim()) return; // froboSession.getTerm(id)om config
    if (doFiltering())
      sortedTerms = filterList(sortedTerms);
  }


  public String getName() { return name; }

  /** returns null if dont have class for id, throws OntologyException if id is not
      found 
      this was only searching non-obsolete terms changing it to also include obsoletes*/
  public OBOClass getTerm(String id) throws TermNotFoundException {
    // this aint right - if its a slim should only search slim
    //OBOClass oc = oboSession.getTerm(id);
    for (OBOClass term : sortedTerms) {
      if (term.getID().equals(id))
        return term;
    }
    for (OBOClass obs : sortedObsoleteTerms) {
      if (obs.getID().equals(id))
        return obs;
    }
    //if (term == null)
    throw new TermNotFoundException(id +" id not found in ontology "+name);
    //return oc;
  }

  /** Returns true if ontology holds term/oboClass */
  boolean hasTerm(OBOClass term) {
    // if this is too slow can do optimizations with prefixes
    try {getTerm(term.getID()); }
    catch (TermNotFoundException e) { return false; }
    return true; // no exception - it has it
  }


  public List<OBOProperty> getSortedRelations() {
    if (sortedRelations == null) {
      //sortedRelations=new ArrayList<OBOProperty>(); not Comparable!
      List sorRel = new ArrayList();
      // if (oboSession == null) ? shouldnt happen
      sorRel.addAll(TermUtil.getRelationshipTypes(oboSession));
      Collections.sort(sorRel,new RelComparator());
      sortedRelations = sorRel; // ?
    }
    return sortedRelations;
  }

  private class RelComparator<OBOProperty> implements Comparator<OBOProperty> {
    public int compare(OBOProperty r1, OBOProperty r2) {
      return r1.toString().compareTo(r2.toString());
    }
    public boolean equals(OBOProperty r1, OBOProperty r2) {
      return r1.toString().equals(r2.toString());
    }
  }


  /** non obsolete terms - sorted */
  public Collection<OBOClass> getSortedTerms() {
    return sortedTerms;
  }

  public Collection<OBOClass> getSortedObsoleteTerms() {
    return sortedObsoleteTerms;
  }


  public OBOSession getOboSession() { return oboSession; }

  public List<OBOClass> getSortedTerms(Collection terms) {
    List<OBOClass> sortedTerms = new ArrayList<OBOClass>();
    sortedTerms.addAll(terms);
    Collections.sort(sortedTerms);
    return sortedTerms;
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

  public void setFilter(String filterOutString) {
    this.filterOutString = filterOutString;
  }
  private String getFilter() { return filterOutString; }
  
  private boolean filterOut(OBOClass term) {
    if (!hasFilter()) return false;
    return term.getID().startsWith(getFilter());
  }

  private boolean hasFilter() {
    return filterOutString != null;
    // minimally filter out obo: obo edit internal terms
    //return true;
  }

  private boolean doFiltering() {
    return filterOboArtifacts() || hasFilter() || hasSlim();
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

  private void loadNamespaces(Collection<Namespace> spaces) {
    if (spaces == null || spaces.isEmpty()) {
      log().error("No namespace to load"); // ex?
      return;
    }
    
    // for now just grab 1st namespace as namespacequery only takes 1 namespace
    //Namespace ns = spaces.toArray(new Namespace[0])[0];
    //Query<OBOClass, OBOClass> nsQuery =
    NamespaceQuery nsQuery =
      new NamespaceQuery(spaces.toArray(new Namespace[0]));
    // create a new query engine on the session we just loaded
    QueryEngine engine = new QueryEngine(oboSession);
    
    // run the namespace query and cache the results
    long time = System.currentTimeMillis();
    //Collection<OBOClass> 
    // true -> cache result ??? do we need to cache - i dont think so
    // BUG - this includes obsoletes!
    nsQuery.setAllowObsoletes(false);
    sortedTerms = engine.query(nsQuery, false);
    //System.out.println("Non-obsolete: got " + sortedTerms.size()+" namespace hits in " + (System.currentTimeMillis() - time)+ "ms # of namespaces: "+spaces.size()+" printing terms in order: ");

    nsQuery.setAllowObsoletes(true);
    nsQuery.setAllowNonObsoletes(false);
    sortedObsoleteTerms = engine.query(nsQuery, false);
    //System.out.println("Obsolete: got " + sortedTerms.size()+" namespace hits in " + (System.currentTimeMillis() - time)+ "ms # of namespaces: "+spaces.size()+" printing terms in order: ");

    if (hasSlim()) {
      CategoryQuery catQuery = new CategoryQuery(slim);
      time = System.currentTimeMillis();
      sortedTerms = engine.query(sortedTerms,catQuery, false);
//      System.out.println("Category query (" +slim+ ") got "+sortedTerms.size()+" in " + (System.currentTimeMillis() - time) + "ms");
      // obsoletes?
      sortedObsoleteTerms = engine.query(sortedObsoleteTerms,catQuery,false);
    }

    //for (Namespace n : spaces) System.out.println(n);
    //for (OBOClass o : sortedTerms) System.out.println(o);
    
  }

  /** This is not generic - this looks for ids that have the filterOut string
      as a prefix and tosses them - for example "ZFS" filters out all zf stage
      terms - can add more flexibility as needed - this is all thats needed for now
      also filters out obo: terms - those are obo edit artifacts i think 
      also filters for slim! - phase out for obo edit stuff  */
  private List<OBOClass> filterList(Collection<OBOClass> list) {
    List<OBOClass> filteredList = new ArrayList<OBOClass>();
    for (OBOClass term : list) {
      // or could do remove on list?
      // also filter out obo: terms as they are internal obo edit thingies it seems
      // funny logic but more efficient to do in one pass - refactor somehow?
      //if (term.getName().startsWith("obo:"))
      if (isOboArtifact(term))
        continue; // filter our obo:
      //if (hasFilter() && term.getID().startsWith(getFilter()))
      if (filterOut(term))
        continue;
      if (!inSlim(term))
        continue;
      filteredList.add(term); // passed 2 filters above - add it
    }
    return filteredList;
  }
}

