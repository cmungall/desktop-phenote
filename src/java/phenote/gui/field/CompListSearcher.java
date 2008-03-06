package phenote.gui.field;
// complist package? field package?

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.bbop.swing.BackgroundUtil;
import org.bbop.util.AbstractTaskDelegate;
import org.bbop.util.TaskDelegate;
import org.obo.datamodel.AnnotatedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.obo.datamodel.OBOProperty;

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
  private String previousInput = null;
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
  public void setOntology(Ontology o) {
    //setSearchAll(false); // ??
    //ontology = o;
    // re init or else will modify list passed in in setOntologies
    ontologyList = new ArrayList<Ontology>(3);
    ontologyList.add(o);
  }

  public void setOntologies(List<Ontology> l) {
    ontologyList = l; // (List<Ontology>)l.clone(); cant clone interfaces
  }

  /** relations are short ontologies - dont need threaded optimization */
//   public List<CompletionRelation> getStringMatchRelations(String input) {
//     List<CompletionRelation> matches = new ArrayList<CompletionRelation>();
//     // most likely only 1 relation ontology, but no harm being general
//     for (Ontology ontology : ontologyList) {
//       //for (OBOProperty rel : ontology.getSortedRelations()) {
//       for (OBOObject rel : ontology.getSortedRelations()) {
//         if (rel.toString().contains(input))
//           matches.add(new CompletionRelation(rel));
//       }
//     }
//     return matches;
//   }
  // --> more general OBOObjects replace OBOProperty
  public List<CompletionObject> getStringMatchRelations(String input) {
    List<CompletionObject> matches = new ArrayList<CompletionObject>();
    // most likely only 1 relation ontology, but no harm being general
    for (Ontology ontology : ontologyList) {
      //for (OBOProperty rel : ontology.getSortedRelations()) {
      for (OBOObject rel : ontology.getSortedRelations()) {
        if (rel.toString().contains(input))
          matches.add(new CompletionObject(rel));
      }
    }
    return matches;
  }

  private CompTaskDelegate compTaskDelegate = null;

  /** Makes a List of CompletionTerms from ontology that contain input string
      constrained by compParams. Gives this list to SearchListener, does search
      in background thread if thread is true.  compParams specifies syns,terms,defs,& obs 
      should input be just part of search params? called by servlet & standalone now */
  public void getStringMatchTermList(String input,SearchListener lis,boolean threaded) {

    //new org.jdesktop.swingworker.SwingWorker();
    // kill old task!
    if (compTaskDelegate!=null) compTaskDelegate.cancel();
    // THREADED
    if (threaded) {
      compTaskDelegate = new CompTaskDelegate(input,lis);
      //BackgroundUtil.scheduleTask(compTaskDelegate);
      GUIManager.getManager().scheduleTask(compTaskDelegate,false);
    }
    // NON THREADED - null task, results sent to listener synchronously
    else {
      getSearchTermList(input,lis,null);
    }
  }

  private class CompTaskDelegate extends AbstractTaskDelegate<List<CompletionTerm>> {

    private String input;
    private SearchListener searchListener;

    private CompTaskDelegate(String input,SearchListener l) {
      this.input = input;
      this.searchListener = l;
    }

    public void execute() {
      try {
        getSearchTermList(input,searchListener,this);
      } // TaskDelegates dont yet deal with runtime exceptions
      catch (RuntimeException x) {
        log().error("got runtime exception in completion "+x);
      }
    
    } // end of execute method

  } // end CompTask inner class

  /** task is null for non threaded case. if threaded/nonnull checks to see if task
      is cancelled - when done passes results to SearchListener */
  private void getSearchTermList(String input, SearchListener searchListener,
                                 TaskDelegate task) {
    List<CompletionTerm> searchTerms = new ArrayList<CompletionTerm>();
    
    // if no input should phenote give no terms? or all terms?
    boolean nothingForNothing = false; // get from FieldConfig!!
    if (nothingForNothing && isBlank(input)) {
      //results = searchTerms; // empty
      searchListener.newResults(searchTerms);
      return;
    }
    
    try {
      // optimization - if user has only typed one more letter (common case)
      // use previous search list - i think this even works with threaded
      // bug - this wont always sort properly
      if (previousInput != null && input.startsWith(previousInput) 
          && input.length() == previousInput.length() + 1) {
        searchTerms = searchPreviousList(input,previousCompList,task);
      }
      
      else {
        // gets term set for currently selected ontology(s)
        //Set ontologyTermList = getCurrentOntologyTermSet();
        for (Ontology ontology : ontologyList) {
          // NON OBSOLETE TERMS
          Collection<OBOClass> ontologyTermList =
            ontology.getSortedTerms();
          List<CompletionTerm> l =
            getOntSearchTermList(input,ontologyTermList,task);
          searchTerms.addAll(l);

          // INSTANCES?
//           Collection<OBOClass> instanceList =
//             ontology.getSortedInstances();
//           l = getOntSearchTermList(input,instanceList,task);
//           searchTerms.addAll(l);
          
          // if obsoletes set then add them in addition to regulars
          if (searchParams.searchObsoletes()) {
            ontologyTermList = ontology.getSortedObsoleteTerms();
            List<CompletionTerm> obsoletes =
              getOntSearchTermList(input,ontologyTermList,task);
            searchTerms.addAll(obsoletes);
          }
        } 
        
      }
      
    } 
    catch (CancelEx x) { return; } // task has been cancelled
    
    previousInput = input;
    previousCompList = searchTerms;
    //return searchTerms;
    //setResults(searchTerms);
    //results = searchTerms; // does task need results?
    if (!isCancelled(task))
      searchListener.newResults(searchTerms); // send off results

  }

  /** for non threaded context task will be null and returns false, otherwise
      queries task */
  private boolean isCancelled(TaskDelegate task) {
    if (task == null) return false;
    return task.isCancelled();
  }

  private boolean isBlank(String s) {
    return s == null || s.equals("");
  }


  private class CancelEx extends Exception {}

  

  /** helper fn for CompTaskDelegate, does search for terms from a single ontology */
  private List<CompletionTerm> getOntSearchTermList(String input,
                                                    Collection<OBOClass> ontologyTermList,
                                                 TaskDelegate task) throws CancelEx {
    SearchTermList searchTermList = new SearchTermList();
    if (ontologyTermList == null)
      return searchTermList.getList();

    int i =0;
    // ontologyTermList (from Ontology) is presorted already by term name
    // if sorted by term id (eg zf stage) SearchTermList i think needs to know that?
    for (OBOClass oboClass : ontologyTermList) {
    //for (int i=0; i<ontologyTermList.size(); i++) {

      if (i++ % 50 == 0 && isCancelled(task)) throw new CancelEx(); 
      CompletionTerm ct = new CompletionTerm(oboClass);
      // if input is blank then add all terms (list all terms on empty input)
      // matches records the kind of hit in CompTerm
      if (ct.matches(input, searchParams)) {
        // a term can have multiple synonym hits, so ct makes new comp terms
        // for each syn hit, getMatches returns list
        searchTermList.addTerm(ct); //.getMatchList());
      }

    }
    return searchTermList.getList();
  }

  private List<CompletionTerm> searchPreviousList(String input,
                                                  List<CompletionTerm> prevList,
                                                  TaskDelegate task)
    throws CancelEx {

    // alternatively could just remove terms from prev list???
    SearchTermList newList = new SearchTermList();
    //for (CompletionTerm ct : prevList) {
    for (int i=0; i<prevList.size(); i++) {
      if (i % 50 == 0 && isCancelled(task)) throw new CancelEx();
      CompletionTerm ct = prevList.get(i);
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

  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}

