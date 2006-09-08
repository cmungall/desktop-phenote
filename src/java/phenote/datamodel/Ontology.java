package phenote.datamodel;

import java.util.ArrayList;
//import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.geneontology.oboedit.datamodel.OBOClass;
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

  private void makeSortedLists(OBOSession oboSession) {
    sortedTerms = getSortedTerms(oboSession.getTerms());
    sortedObsoleteTerms = getSortedTerms(oboSession.getObsoleteTerms());
  }

  private void filterLists() {
    if (!haveFilter()) return; // from config
    sortedTerms = filterList(sortedTerms,filterOutString);
  }


  public String getName() { return name; }

  /** returns null if dont have class for id */
  public OBOClass getOboClass(String id) {
    return oboSession.getTerm(id);
  }

  /** Returns true if ontology holds obo class */
  boolean hasOboClass(OBOClass oboClass) {
    // if this is too slow can do optimizations with prefixes
    return getOboClass(oboClass.getID()) != null;
  }

  /** Returns a Vector of OBOClass from ontology that contain input string
      constrained by compParams. compParams specifies syns,terms,defs,& obs 
      should input be just part of search params? 
      its a vector as thats what ComboBox requires 
      put in separate class? */
  public Vector<OBOClass> getSearchTerms(String input,SearchParamsI searchParams) {
   Vector<OBOClass> searchTerms = new Vector<OBOClass>();
    if (input == null || input.equals(""))
      return searchTerms;

    // gets term set for currently selected ontology
    //Set ontologyTermList = getCurrentOntologyTermSet();
    List<OBOClass> ontologyTermList = getSortedTerms(); // non obsolete
    searchTerms = getSearchTerms(input,ontologyTermList,searchParams);

    // if obsoletes set then add them in addition to regulars
    if (searchParams.searchObsoletes()) {
      ontologyTermList = getSortedObsoleteTerms();
      Vector obsoletes = getSearchTerms(input,ontologyTermList,searchParams);
      searchTerms.addAll(obsoletes);
    }
    return searchTerms;
  }

  private Vector getSearchTerms(String input, List<OBOClass> ontologyTermList,
                                    SearchParamsI searchParams) {
    // need a unique list - UniqueTermList has quick check for uniqueness, checking
    // whole list is very very slow
    UniqueTermList uniqueTermList = new UniqueTermList();
    //Vector searchTerms = new Vector();
    if (ontologyTermList == null)
      return uniqueTermList.getVector();//searchTerms;

    boolean ignoreCase = true; // param?
    if (ignoreCase)
      input = input.toLowerCase();

    // i think iterators are more efficient than get(i) ??
    Iterator<OBOClass> iter = ontologyTermList.iterator();
    while (iter.hasNext()) {
      // toString extracts name from OBOClass
      OBOClass oboClass = iter.next();
      String originalTerm = oboClass.getName();//toString();
      
      boolean termAdded = false;

      // SKIP PATO ATTRIBUTES - only want values - or do !contains "value"?
      // yes do contains value - as theres other crap to filter too
      // apparently curators want to see attribs as well according to Michael
      //if (filterAttributes() && isAttribute(originalTerm))
      //continue;

      if (searchParams.searchTerms()) {
        // adds originalTerm to searchTerms if match (1st if exact)
        termAdded = compareAndAddTerm(input,originalTerm,oboClass,uniqueTermList);
        if (termAdded)
          continue;
      }


      if (searchParams.searchSynonyms()) {
        Set synonyms = oboClass.getSynonyms();
        for (Iterator i = synonyms.iterator(); i.hasNext(); ) {
          String syn = i.next().toString();
          //System.out.println("syn "+syn+" for "+originalTerm);
          termAdded = compareAndAddTerm(input,syn,oboClass,uniqueTermList);
          if (termAdded)
            continue;
        }
      }


      if (searchParams.searchDefinitions()) {
        String definition = oboClass.getDefinition();
        if (definition != null & !definition.equals(""))
          termAdded = compareAndAddTerm(input,definition,oboClass,uniqueTermList);
          if (termAdded)
            continue;
      }

    }
    return uniqueTermList.getVector();//searchTerms;
  }

  // pato subclass?
  private boolean isAttribute(String term) {
    return contains(term.toLowerCase(),"attribute");
  }
           
  // part of search params?
  //private boolean filterAttributes() { return isPato(); }

  // make enum!!
  private boolean isPato() {
    //return ont == PATO;
    // for now - eventually config somehow
    return name.equals("Pato");
  }
  


  /** User input is already lower cased, this potentially adds oboClass to
   * searchTerms if input & compareTerm match. Puts it first if exact. 
   * for term names comp = obo, for syns comp is the syn. 
   Returns true if term is a match & either gets added or already is added
   * Also checks if term is in list already 
   Theres a speed issue here - the vector needs to be unique. if check every time
   with whole list very very slow. 2 alternatives to try. 
   1) have separate hash for checking uniqueness (downside 2 data structures). 
   could make a data structure that had both map & vector
   2) use LinkedHashSet, which does uniqueness & maintains order of insertion
   nice - its 1 data structure BUT exact matches go 1st, no way in linked hash set
   to insert 1st elements so would need to keep separate list of exact matches, which
   i guess there can be more than one with synonyms (do syns count for exact matches?
   should they?) - downside of #2 is need Vector for combo box */
  private boolean compareAndAddTerm(String input, String compareTerm, OBOClass oboClass,
                                    UniqueTermList uniqueTermList) {
    
    String oboTerm = oboClass.getName();

    String lowerComp = compareTerm;
    boolean ignoreCase = true; // discard? param for?
    if (ignoreCase)
      lowerComp = compareTerm.toLowerCase();

    boolean doContains = true; // discard? param for?
    // exact match goes first in list
    if (lowerComp.equals(input)) {
      //if (!searchTerms.contains(oboClass)) {// this takes a long time w long lists!
      //searchTerms.add(0,oboClass);
      uniqueTermList.addTermFirst(oboClass); // adds if not present
      return true;
        //}
    }
    // Contains
    else if (doContains) {
      if (contains(lowerComp,input) && !termFilter(lowerComp)) {
        //if(!searchTerms.contains(oboClass))takes long time!searchTerms.add(oboClass);
        uniqueTermList.addTerm(oboClass); // does unique check
        return true;
      }
    }
//     // Starts with - not doing this for now
//     else if (lowerComp.startsWith(input)) {
//       if (!searchTerms.contains(oboClass))
//         searchTerms.add(oboClass);
//     }
    return false;
  }

  // 1.5 has a contains! use when we shift
  private boolean contains(String term, String input) {
    return term.contains(input); // 1.5!!
    //return term.indexOf(input) != -1;
  }
  /** Oboedit getTerms returns some terms with obo: prefix that should be filtered
   * out. Returns true if starts with obo: */
  private boolean termFilter(String term) {
    return term.startsWith("obo:");
  }

  /** non obsolete terms - sorted */
  private List<OBOClass> getSortedTerms() {
    return sortedTerms;
  }

  private List<OBOClass> getSortedObsoleteTerms() {
    return sortedObsoleteTerms;
  }


  private OBOSession getOboSession() { return oboSession; }

  private List<OBOClass> getSortedTerms(Set terms) {
    List<OBOClass> sortedTerms = new ArrayList<OBOClass>();
    sortedTerms.addAll(terms);
    Collections.sort(sortedTerms);
    return sortedTerms;
  }

  /** moght move this elsewhere - subclass? data adap specific wrapper? */
  public void setTimestamp(long t) { ontologyTimestamp = t; }
  public long getTimestamp() { return ontologyTimestamp; }
  /** for obo files this is the filename */
  public void setSource(String s) { source = s; }
  public String getSource() { return source; }


  /** does unique check w map */
  private class UniqueTermList {
    private Vector<OBOClass> searchTerms = new Vector<OBOClass>();
    private Map<OBOClass,OBOClass> uniqueCheck = new HashMap<OBOClass,OBOClass>();
    private void addTerm(OBOClass oboClass) {
      addTerm(oboClass,false);
    }
    private void addTermFirst(OBOClass oboClass) {
      addTerm(oboClass,true);
    }
    private void addTerm(OBOClass oboClass,boolean first) {
      if (uniqueCheck.containsKey(oboClass))
        return;
      if (first) searchTerms.add(0,oboClass);
      else searchTerms.add(oboClass);
      uniqueCheck.put(oboClass,null); // dont need value
    }
    private Vector<OBOClass> getVector() { return searchTerms; }
  }


  public void setFilter(String filterOutString) {
    this.filterOutString = filterOutString;
  }

  private boolean haveFilter() { return filterOutString != null; }

  /** This is not generic - this looks for ids that have the filterOut string
      as a prefix and tosses them - for example "ZFS" filters out all zf stage
      terms - can add more flexibility as needed - this is all thats needed for now*/
  private List<OBOClass> filterList(List<OBOClass> list, String filterOut) {
    List<OBOClass> filteredList = new ArrayList<OBOClass>();
    for (OBOClass term : list) {
      // or could do remove on list?
      if (!term.getID().startsWith(filterOut))
        filteredList.add(term);
    }
    return filteredList;
  }

}



// GARBAGE

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
