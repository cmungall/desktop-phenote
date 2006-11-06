package phenote.datamodel;

import java.util.ArrayList;
//import java.util.Collection;
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

import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.OBOProperty;
import org.geneontology.oboedit.datamodel.OBOSession;

/** rename Ontology? - yes - this isnt a completion list - a completion list is a 
    subset of ontology terms that matches user input. This is a listing of terms of
    the whole ontology */
public class Ontology {

  private String name;
  //private String filename;
  private OBOSession oboSession;
  private List<OBOClass> sortedTerms;
  private List<OBOClass> sortedObsoleteTerms;
  private List<OBOProperty> sortedRelations;
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

  public void setOboSession(OBOSession os) {
    oboSession = os;
    makeSortedLists(oboSession);
    filterLists();
  }

  public Date getOntologyDate() { return new Date(ontologyTimestamp); }

  private void makeSortedLists(OBOSession oboSession) {
    //log().debug("name "+name+" terms "+oboSession.getTerms()+" propVals "+oboSession.getPropertyValues()+" rels "+oboSession.getRelationshipTypes());
    sortedTerms = getSortedTerms(oboSession.getTerms());
    sortedObsoleteTerms = getSortedTerms(oboSession.getObsoleteTerms());
  }

  private void filterLists() {
    if (!haveFilter()) return; // from config
    sortedTerms = filterList(sortedTerms,filterOutString);
  }


  public String getName() { return name; }

  /** returns null if dont have class for id, throws OntologyException if id is not
      found */
  public OBOClass getOboClass(String id) throws OntologyException {
    OBOClass oc = oboSession.getTerm(id);
    if (oc == null) throw new OntologyException(id +" id not found in ontology "+name);
    return oc;
  }

  /** Returns true if ontology holds obo class */
  boolean hasOboClass(OBOClass oboClass) {
    // if this is too slow can do optimizations with prefixes
    try {getOboClass(oboClass.getID()); }
    catch (OntologyException e) { return false; }
    return true; // no exception - it has it
  }


  public List<OBOProperty> getSortedRelations() {
    if (sortedRelations == null) {
      //sortedRelations=new ArrayList<OBOProperty>(); not Comparable!
      List sorRel = new ArrayList();
      // if (oboSession == null) ? shouldnt happen
      sorRel.addAll(oboSession.getRelationshipTypes());
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
  public List<OBOClass> getSortedTerms() {
    return sortedTerms;
  }

  public List<OBOClass> getSortedObsoleteTerms() {
    return sortedObsoleteTerms;
  }


  private OBOSession getOboSession() { return oboSession; }

  public List<OBOClass> getSortedTerms(Set terms) {
    List<OBOClass> sortedTerms = new ArrayList<OBOClass>();
    sortedTerms.addAll(terms);
    Collections.sort(sortedTerms);
    return sortedTerms;
  }

  /** meght move this elsewhere - subclass? data adap specific wrapper? */
  public void setTimestamp(long t) {
    ontologyTimestamp = t;
    log().info(getName()+" Ontology date: "+getOntologyDate());
  }
  public long getTimestamp() { return ontologyTimestamp; }
  /** for obo files this is the filename */
  public void setSource(String s) { source = s; }
  public String getSource() { return source; }




  public void setFilter(String filterOutString) {
    this.filterOutString = filterOutString;
  }

  private boolean haveFilter() {
    //return filterOutString != null;
    // minimally filter out obo: obo edit internal terms
    return true;
  }

  /** This is not generic - this looks for ids that have the filterOut string
      as a prefix and tosses them - for example "ZFS" filters out all zf stage
      terms - can add more flexibility as needed - this is all thats needed for now*/
  private List<OBOClass> filterList(List<OBOClass> list, String filterOut) {
    List<OBOClass> filteredList = new ArrayList<OBOClass>();
    for (OBOClass term : list) {
      // or could do remove on list?
      // also filter out obo: terms as they are internal obo edit thingies it seems
      // funny logic but more efficient to do in one pass - refactor somehow?
      if (term.getName().startsWith("obo:"))
        continue; // filter our obo:
      if (filterOut != null && term.getID().startsWith(filterOut))
        continue;
      filteredList.add(term); // passed 2 filters above - add it
    }
    return filteredList;
  }

  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}



// GARBAGE
//   /** Returns a Vector of OBOClass from ontology that contain input string
//       constrained by compParams. compParams specifies syns,terms,defs,& obs 
//       should input be just part of search params? 
//       its a vector as thats what ComboBox requires 
//       put in separate class? */
//   public Vector<OBOClass> getSearchTerms(String input,SearchParamsI searchParams) {
//    Vector<OBOClass> searchTerms = new Vector<OBOClass>();
//     if (input == null || input.equals(""))
//       return searchTerms;

//     // gets term set for currently selected ontology
//     //Set ontologyTermList = getCurrentOntologyTermSet();
//     List<OBOClass> ontologyTermList = getSortedTerms(); // non obsolete
//     searchTerms = getSearchTerms(input,ontologyTermList,searchParams);

//     // if obsoletes set then add them in addition to regulars
//     if (searchParams.searchObsoletes()) {
//       ontologyTermList = getSortedObsoleteTerms();
//       Vector obsoletes = getSearchTerms(input,ontologyTermList,searchParams);
//       searchTerms.addAll(obsoletes);
//     }
//     return searchTerms;
//   }

//   /** helper fn for getSearchTerms(String,SearhParamsI) */
//   private Vector<OBOClass> getSearchTerms(String input, List<OBOClass> ontologyTermList,
//                                     SearchParamsI searchParams) {
//     // need a unique list - UniqueTermList has quick check for uniqueness, checking
//     // whole list is very very slow - how is it possible to get a dup term? i forget?
//     // the dup term was a BUG! in synonym - woops
//     SearchTermList uniqueTermList = new SearchTermList();
//     //Vector searchTerms = new Vector();
//     if (ontologyTermList == null)
//       return uniqueTermList.getVector();//searchTerms;

//     boolean ignoreCase = true; // param?
//     if (ignoreCase)
//       input = input.toLowerCase();

//     // i think iterators are more efficient than get(i) ??
//     Iterator<OBOClass> iter = ontologyTermList.iterator();
//     while (iter.hasNext()) {
//       // toString extracts name from OBOClass
//       OBOClass oboClass = iter.next();
//       String originalTerm = oboClass.getName();//toString();
      
//       boolean termAdded = false;

//       if (searchParams.searchTerms()) {
//         // adds originalTerm to searchTerms if match (1st if exact)
//         termAdded = compareAndAddTerm(input,originalTerm,oboClass,uniqueTermList);
//         if (termAdded)
//           continue;
//       }


//       if (searchParams.searchSynonyms()) {
//         Set synonyms = oboClass.getSynonyms();
//         for (Iterator i = synonyms.iterator(); i.hasNext() &&!termAdded; ) {
//           String syn = i.next().toString();
//           //log().debug("syn "+syn+" for "+originalTerm);
//           termAdded = compareAndAddTerm(input,syn,oboClass,uniqueTermList);
//           //if (termAdded) continue; // woops continues this for not the outer!
//         }
//       }
//       if (termAdded) continue;


//       if (searchParams.searchDefinitions()) {
//         String definition = oboClass.getDefinition();
//         if (definition != null & !definition.equals(""))
//           termAdded = compareAndAddTerm(input,definition,oboClass,uniqueTermList);
//           if (termAdded)
//             continue; // not really necesary as its last
//       }

//     }
//     return uniqueTermList.getVector();//searchTerms;
//   }

//   public Vector<OBOProperty> getStringMatchRelations(String input) {
//     Vector<OBOProperty> matches = new Vector<OBOProperty>();
//     for (OBOProperty rel : getSortedRelations()) {
//       if (rel.toString().contains(input))
//         matches.add(rel);
//     }
//     return matches;
//   }
//   /** User input is already lower cased, this potentially adds oboClass to
//    * searchTerms if input & compareTerm match. Puts it first if exact. 
//    * for term names comp = obo, for syns comp is the syn. 
//    Returns true if term is a match & either gets added or already is added
//    * Also checks if term is in list already - not needed - woops!
//    Theres a speed issue here - the vector needs to be unique. if check every time
//    with whole list very very slow. 2 alternatives to try. 
//    1) have separate hash for checking uniqueness (downside 2 data structures). 
//    could make a data structure that had both map & vector
//    2) use LinkedHashSet, which does uniqueness & maintains order of insertion
//    nice - its 1 data structure BUT exact matches go 1st, no way in linked hash set
//    to insert 1st elements so would need to keep separate list of exact matches, which
//    i guess there can be more than one with synonyms (do syns count for exact matches?
//    should they?) - downside of #2 is need Vector for combo box
 
//    this wont fly for having different display strings for syn & obs as compareTerm
//    can be syn obs term or def and its lost here 
//    i think these methods need to be moved to gui.TermSearcher that utilizes Ontology 
//    but produces CompListTerms */
//   private boolean compareAndAddTerm(String input, String compareTerm, OBOClass oboClass,
//                                     SearchTermList searchTermList) {
    
//     String oboTerm = oboClass.getName();

//     String lowerComp = compareTerm;
//     boolean ignoreCase = true; // discard? param for?
//     if (ignoreCase)
//       lowerComp = compareTerm.toLowerCase();

//     //boolean doContains = true; // discard? param for?
//     // exact match goes first in list
//     if (lowerComp.equals(input)) {
//       searchTermList.addTermFirst(oboClass); // adds if not present
//       return true;
//     }
//     // new paradigm - put starts with first
//     else if (lowerComp.startsWith(input)) {
//       searchTermList.addTerm(oboClass);
//       return true;
//     }
//     // Contains
//     else if (contains(lowerComp,input) && !termFilter(lowerComp)) {
//       searchTermList.addContainsTerm(oboClass);
//       return true;
//     }
//     return false;
//   }

//   // 1.5 has a contains! use when we shift
//   private boolean contains(String term, String input) {
//     return term.contains(input); // 1.5!!
//     //return term.indexOf(input) != -1;
//   }
//   /** Oboedit getTerms returns some terms with obo: prefix that should be filtered
//    * out. Returns true if starts with obo: */
//   private boolean termFilter(String term) {
//     return term.startsWith("obo:");
//   }
      //if (!searchTerms.contains(oboClass)) {// this takes a long time w long lists!
      //searchTerms.add(0,oboClass);
      //if (!searchTerms.contains(oboClass))  searchTerms.add(oboClass);
        //if(!searchTerms.contains(oboClass))takes long time!searchTerms.add(oboClass);
      // SKIP PATO ATTRIBUTES - only want values - or do !contains "value"?
      // yes do contains value - as theres other crap to filter too
      // apparently curators want to see attribs as well according to Michael
      //if (filterAttributes() && isAttribute(originalTerm))
      //continue;

  // pato subclass?
//   private boolean isAttribute(String term) {
//     return contains(term.toLowerCase(),"attribute");
//   }
  // part of search params?
  //private boolean filterAttributes() { return isPato(); }

//   // make enum!!
//   private boolean isPato() {
//     //return ont == PATO;
//     // for now - eventually config somehow
//     return name.equals("Pato");
//   }

//   public Ontology(String name,OBOSession oboSession) {
//     this.name = name;
//     this.oboSession = oboSession;
//     makeSortedLists(oboSession);
//   }
  // pase - Ontology shouldnt load file
//   public Ontology(String name, String filename) {
//     //loadAllOntologyTerms();
//     this.name = name;
//     loadOntology(filename);
//   }

//   /** Load up/cache Sets for all ontologies used, anatomyOntologyTermSet
//    * and patoOntologyTermSet -- move to dataadapter/OntologyDataAdapter... */
//   private void loadOntology(String filename) {//loadAllOntologyTerms() {
//     oboSession = getOboSession(findFile(filename));
//     sortedTerms = getSortedTerms(oboSession.getTerms());
//     sortedObsoleteTerms = getSortedTerms(oboSession.getObsoleteTerms());
//   }

  

//   /** Look for file in current directory (.) and jar file */
//   private URL findFile(String fileName) {
//     String oboFileDir = "obo-files/";
//     // try current directory + obo-file dir
//     String currentDir = "./" + oboFileDir + fileName;
//     File file = new File(currentDir);
//     if (file.exists())
//       return makeUrl(currentDir);

//     // try jar - hopefully this works... jar files have to have '/' prepended
//     // first try without obo-files dir (in jar)
//     String jarFile = "/" + fileName;
//     URL url = Ontology.class.getResource(jarFile); // looks in jar
//     // 2nd try with obo-files dir in jar file (i used to do it this way)
//     if (url == null) {
//       jarFile = "/" + oboFileDir + fileName;
//       url = Ontology.class.getResource(jarFile); // looks in jar
//     }

//     if (url == null) {
//       System.out.println("No file found in pwd or jar for "+fileName);
//       return null;
//     }
//     return url;
//   }
  
//   private URL makeUrl(String file) {
//     try {
//       return new URL("file:"+file);
//     }
//     catch (MalformedURLException e) {
//       System.out.println("malformed url "+file+" "+e);
//       return null;
//     }
//   }


//   // String -> url to handle web start jar obo files
//   private OBOSession getOboSession(URL oboUrl) {
//     if (oboUrl == null)
//       return new OBOSessionImpl(); // ??

//     OBOFileAdapter fa = new OBOFileAdapter();
//     FileAdapterConfiguration cfg = new OBOFileAdapter.OBOAdapterConfiguration();
//     Collection fileList = new ArrayList();
//     fileList.add(oboUrl.toString());
//     cfg.setReadPaths(fileList);
//     try { // throws data adapter exception
//       OBOSession os = (OBOSession)fa.doOperation(IOOperation.READ,cfg,null);
//       return os;
//     }
//     catch (DataAdapterException e) {
//       System.out.println("got data adapter exception: "+e);
//       return null; // empty session?
//     }
//   }


//   private Set getObsoleteTerms() {
//     Set s = getOboSession().getObsoleteTerms();
//     if (s == null)
//       System.out.println("No ontology terms loaded for "+name);
//     return s;
//   }
//   private Set getNonObsoleteTerms() {
//     Set s = getOboSession().getTerms(); // is this inefficient? cache?
//     if (s == null)
//       System.out.println("No ontology terms loaded for "+name);
//     return s;
//   }
