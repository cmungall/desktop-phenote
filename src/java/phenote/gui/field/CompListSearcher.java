package phenote.gui.field;
// complist package? field package?

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
//import java.util.Vector;

import org.geneontology.util.AbstractTaskDelegate;
import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.OBOProperty;

import phenote.datamodel.Ontology;
import phenote.gui.SearchParams;
import phenote.gui.SearchParamsI;


/** An aid to completion lists. Searches ontology with user input and search params
    and returns vectors of completion list objects CompletionTerms or 
    CompletionRelations 
    Moved search methods from Ontology to here, as now cant just return model
    objects, as OBOClass.toString doesnt cut it anymore - need the gui objects
    for syns & obsoletes & to cut off long terms */
public class CompListSearcher {

  //private Ontology ontology;
  //private boolean searchAll=false;
  private List<Ontology> ontologyList = new ArrayList<Ontology>(3);
  private SearchParamsI searchParams = SearchParams.inst(); // singleton
  private String previousInput = "";
  private List<CompletionTerm> previousCompList = new ArrayList<CompletionTerm>();

  /** Ontology - the initial ontology to search, setOntology changes this,
      initially only searches the one ontology (not ALL) - used by servlet which
      currently doesnt do ALL */
  public CompListSearcher(Ontology o) {
    setOntology(o);
  }

  /** l - initial list of ontologies to search, as in ALL. used by CharFieldGui
      to set list to all ontols in field as initial setting is ALL - is this funny? */
  public CompListSearcher(List<Ontology> l) {
    setOntologies(l);
  }

  //void setSearchAll(boolean s) { searchAll = s; }

  /** Set the ontology to search. Ontology chooser may set this */
  void setOntology(Ontology o) {
    //setSearchAll(false); // ??
    //ontology = o;
    // re init or else will modify list passed in in setOntologies
    ontologyList = new ArrayList<Ontology>(3);
    ontologyList.add(o);
  }

  void setOntologies(List<Ontology> l) {
    ontologyList = l; // (List<Ontology>)l.clone(); cant clone interfaces
  }

  /** relations are short ontologies - dont need threaded optimization */
  public List<CompletionRelation> getStringMatchRelations(String input) {
    List<CompletionRelation> matches = new ArrayList<CompletionRelation>();
    // most likely only 1 relation ontology, but no harm being general
    for (Ontology ontology : ontologyList) {
      for (OBOProperty rel : ontology.getSortedRelations()) {
        if (rel.toString().contains(input))
          matches.add(new CompletionRelation(rel));
      }
    }
    return matches;
  }

  private CompTaskDelegate compTaskDelegate = null;

  /** Returns a List of CompletionTerms from ontology that contain input string
      constrained by compParams. compParams specifies syns,terms,defs,& obs 
      should input be just part of search params? called by servlet & standalone now */
  public /*List<CompletionTerm>*/ void getStringMatchTermList(String input,
                                                     SearchListener lis) {

    //new org.jdesktop.swingworker.SwingWorker();
    // kill old task!
    if (compTaskDelegate!=null) compTaskDelegate.cancel();
    compTaskDelegate = new CompTaskDelegate(input,lis);
    org.geneontology.swing.BackgroundUtil.scheduleTask(compTaskDelegate);
    
  }

  private class CompTaskDelegate
    extends AbstractTaskDelegate<List<CompletionTerm>> {

    private String input;
    private SearchListener searchListener;

    private CompTaskDelegate(String input,SearchListener l) {
      this.input = input;
      this.searchListener = l;
    }

    public void execute() {
      List<CompletionTerm> searchTerms = new ArrayList<CompletionTerm>();
      
      // if no input should phenote give no terms? or all terms?
      boolean nothingForNothing = false; // get from FieldConfig!!
      if (nothingForNothing && isBlank(input)) {
        results = searchTerms; // empty
        searchListener.newResults(results);
        return;
      }
      
      // optimization - if user has only typed one more letter (common case)
      // use previous search list
      if (input.startsWith(previousInput) 
          && input.length() == previousInput.length() + 1) {
        searchTerms = searchPreviousList(input,previousCompList);
      }
      
      else {
        // gets term set for currently selected ontology(s)
        //Set ontologyTermList = getCurrentOntologyTermSet();
        // THIS IS WRONG! or is it?
        for (Ontology ontology : ontologyList) {
          Collection<OBOClass> ontologyTermList = ontology.getSortedTerms(); // non obsolete
          List<CompletionTerm> l = getSearchTermList(input,ontologyTermList);
          searchTerms.addAll(l);
          
          // if obsoletes set then add them in addition to regulars
          if (searchParams.searchObsoletes()) {
            ontologyTermList = ontology.getSortedObsoleteTerms();
            List<CompletionTerm> obsoletes = getSearchTermList(input,ontologyTermList);
            searchTerms.addAll(obsoletes);
          }
        }
      }

      previousInput = input;
      previousCompList = searchTerms;
      //return searchTerms;
      //setResults(searchTerms);
      results = searchTerms;
      searchListener.newResults(results); // send off results
      
    }
  }


  private boolean isBlank(String s) {
    return s == null || s.equals("");
  }

  /** helper fn for getSearchTerms(String,SearhParamsI) */
  private List<CompletionTerm> getSearchTermList(String input,
                                                Collection<OBOClass> ontologyTermList) {
    SearchTermList searchTermList = new SearchTermList();
    if (ontologyTermList == null)
      return searchTermList.getList();

    for (OBOClass oboClass : ontologyTermList) {

      CompletionTerm ct = new CompletionTerm(oboClass);
      // if input is blank then add all terms (list all terms on empty input)
      // matches records the kind of hit in CompTerm
      if (ct.matches(input, searchParams)) {
        searchTermList.addTerm(ct);
      }

    }
    return searchTermList.getList();
  }

  private List<CompletionTerm> searchPreviousList(String input,
                                                  List<CompletionTerm> prevList) {
    // alternatively could just remove terms from prev list???
    SearchTermList newList = new SearchTermList();
    for (CompletionTerm ct : prevList) {
      ct.resetMatchState(); // reusing ct has stale match state from previous search
      if (ct.matches(input,searchParams))
        newList.addTerm(ct);
    }
    return newList.getList();
  }


  /** User input is already lower cased, this potentially adds oboClass to
   * searchTerms if input & compareTerm match. Puts it first if exact. 
   * for term names comp = obo, for syns comp is the syn. 
   Returns true if term is a match & either gets added or already is added
   * Also checks if term is in list already - not needed - woops!
   Theres a speed issue here - the vector needs to be unique. if check every time
   with whole list very very slow. 2 alternatives to try. 
   1) have separate hash for checking uniqueness (downside 2 data structures). 
   could make a data structure that had both map & vector
   2) use LinkedHashSet, which does uniqueness & maintains order of insertion
   nice - its 1 data structure BUT exact matches go 1st, no way in linked hash set
   to insert 1st elements so would need to keep separate list of exact matches, which
   i guess there can be more than one with synonyms (do syns count for exact matches?
   should they?) - downside of #2 is need Vector for combo box
 
   this wont fly for having different display strings for syn & obs as compareTerm
   can be syn obs term or def and its lost here 
   i think these methods need to be moved to gui.TermSearcher that utilizes Ontology 
   but produces CompListTerms */
  private boolean contains(String term, String input) {
    return term.contains(input); // 1.5!!
  }


  /** SearchTermList INNER CLASS
      this data structure is handy for putting starts with
      before contains! UniqueTermList -> SearchTermList*/
  private class SearchTermList {
    private List<CompletionTerm> startsWithTerms = new ArrayList<CompletionTerm>();
    //private Map<OBOClass,OBOClass> uniqueCheck = new HashMap<OBOClass,OBOClass>();
    // list of terms that are contained but NOT startsWith
    private List<CompletionTerm> containTerms = new ArrayList<CompletionTerm>();
    private List<CompletionTerm> startsWithSyns = new ArrayList<CompletionTerm>();
    private List<CompletionTerm> containSyns = new ArrayList<CompletionTerm>();
    private List<CompletionTerm> obsoletes = new ArrayList<CompletionTerm>();
    private List<CompletionTerm> definitions = new ArrayList<CompletionTerm>();

    private void addTerm(CompletionTerm ct) {
      if (ct.isObsolete())
        obsoletes.add(ct); // start with/contains?
      else if (ct.isTermMatch())
        addTerm(ct,startsWithTerms,containTerms);
      else if (ct.isSynMatch())
        addTerm(ct,startsWithSyns,containSyns);
      else if (ct.isDefinitionMatch())
        definitions.add(ct); // start with/contains?
    }

    private void addTerm(CompletionTerm ct, List<CompletionTerm> startsWith,
                         List<CompletionTerm> contains) {
      if (ct.isExactMatch()) // for syns as well? sure why not?
        startsWith.add(0,ct);
      else if (ct.isStartsWithMatch())
        startsWith.add(ct);
      else
        contains.add(ct);
    }

//     private Vector<CompletionTerm> getVector() { 
//       //startsWithTerms.addAll(containTerms); return startsWithTerms; 
//       Vector<CompletionTerm> sortedTerms = new Vector<CompletionTerm>();
//       sortedTerms.addAll(startsWithTerms);
//       sortedTerms.addAll(containTerms);
//       sortedTerms.addAll(startsWithSyns);
//       sortedTerms.addAll(containSyns);
//       sortedTerms.addAll(definitions);
//       sortedTerms.addAll(obsoletes);
//       return sortedTerms;
//     }

    private List<CompletionTerm> getList() {
      //startsWithTerms.addAll(containTerms); return startsWithTerms;
      List<CompletionTerm> sortedTerms = new ArrayList<CompletionTerm>();
      sortedTerms.addAll(startsWithTerms);
      sortedTerms.addAll(containTerms);
      sortedTerms.addAll(startsWithSyns);
      sortedTerms.addAll(containSyns);
      sortedTerms.addAll(definitions);
      sortedTerms.addAll(obsoletes);
      return sortedTerms;
    }
  }
}

  // now done in Ontology.filterList() so dont have to check every time
//   /** Oboedit getTerms returns some terms with obo: prefix that should be filtered
//    * out. Returns true if starts with obo: */
//   private boolean termFilter(String term) {
//     return term.startsWith("obo:"); // take these out of ontology?
//   }
  // finally got rid of default vector model for AbstractAutoCompList!
//   /** Returns a Vector of OBOClass from ontology that contain input string
//       constrained by compParams. compParams specifies syns,terms,defs,& obs
//       should input be just part of search params?
//       its a vector as thats what ComboBox/TermCompList requires */
//   public Vector<CompletionTerm> getStringMatchTerms(String input) {
//    Vector<CompletionTerm> searchTerms = new Vector<CompletionTerm>();
//     if (input == null || input.equals(""))
//       return searchTerms;

//     // gets term set for currently selected ontology
//     //Set ontologyTermList = getCurrentOntologyTermSet();
//     List<OBOClass> ontologyTermList = ontology.getSortedTerms(); // non obsolete
//     searchTerms = getSearchTerms(input,ontologyTermList);

//     // if obsoletes set then add them in addition to regulars
//     if (searchParams.searchObsoletes()) {
//       ontologyTermList = ontology.getSortedObsoleteTerms();
//       Vector obsoletes = getSearchTerms(input,ontologyTermList);
//       searchTerms.addAll(obsoletes);
//     }
//     return searchTerms;
//   }

//   /** helper fn for getSearchTerms(String,SearhParamsI) */
//   private Vector<CompletionTerm> getSearchTerms(String input,
//                                                 List<OBOClass> ontologyTermList) {
//     SearchTermList searchTermList = new SearchTermList();
//     if (ontologyTermList == null)
//       return searchTermList.getVector();

//     //boolean ignoreCase = true; // param? //if (ignoreCase)
//     //input = input.toLowerCase(); // done in CompletionTerm

//     // i think iterators are more efficient than get(i) ??
//     Iterator<OBOClass> iter = ontologyTermList.iterator();
//     while (iter.hasNext()) {

//       OBOClass oboClass = iter.next();

//       CompletionTerm ct = new CompletionTerm(oboClass);
//       if (ct.matches(input,searchParams)) {
//         searchTermList.addTerm(ct);
//       }

//     }
//     return searchTermList.getVector();
//   }

//   private boolean compareAndAddTerm(String input, String compareTerm, OBOClass oboClass,
//                                     SearchTermList searchTermList) {
    
//     //String oboTerm = oboClass.getName();

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
//     else if (contains(lowerComp,input)) { // && !termFilter(lowerComp) -> Ont
//       searchTermList.addContainsTerm(oboClass);
//       return true;
//     }
//     return false;
//   }
//       String originalTerm = oboClass.getName();//toString();
//       boolean termAdded = false;
//       if (searchParams.searchTerms()) {
//         // adds originalTerm to searchTerms if match (1st if exact)
//         termAdded = compareAndAddTerm(input,originalTerm,oboClass,searchTermList);
//         if (termAdded) continue;}
//       if (searchParams.searchSynonyms()) {
//         Set synonyms = oboClass.getSynonyms();
//         for (Iterator i = synonyms.iterator(); i.hasNext() &&!termAdded; ) {
//           String syn = i.next().toString();
//           //log().debug("syn "+syn+" for "+originalTerm);
//           termAdded = compareAndAddTerm(input,syn,oboClass,searchTermList);
//           //if (termAdded) continue; // woops continues this for not the outer!
//         }
//       }
//       if (termAdded) continue;
//       if (searchParams.searchDefinitions()) {
//         String definition = oboClass.getDefinition();
//         if (definition != null & !definition.equals(""))
//           termAdded = compareAndAddTerm(input,definition,oboClass,searchTermList);
//           if (termAdded) continue; // not really necesary as its last }
//     private void addTerm(OBOClass oboClass) {
//       addTerm(oboClass,false);
//     }
//     private void addTermFirst(OBOClass oboClass) {
//       addTerm(oboClass,true);
//     }
//     private void addTerm(OBOClass oboClass,boolean first) {
//       //if (uniqueCheck.containsKey(oboClass)) {
//       //log().debug("dup term in search "+oboClass);
//       //  new Throwable().printStackTrace();  return;   }
//       CompletionTerm t = new CompletionTerm(oboClass);
//       if (first) startsWithTerms.add(0,t);
//       else startsWithTerms.add(t);
//       //uniqueCheck.put(oboClass,null); // dont need value
//     }
//     /** Add term thats not startsWith but contains */
//     private void addContainsTerm(OBOClass oboClass) {
//       containTerms.add(new CompletionTerm(oboClass));
//     }
