package phenote.datamodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.datamodel.CharField.CharFieldEnum;

/** Manages all of the ontology. Eventually will get config info (xml? OntologyConfig?)
    and set itself up from that. Should there be an ontology package - whats funny
    is that ontologies have obo filenames that they parse so they are sort of
    data adapterish 
    actually manages CharFields(which may have ontologies) - rename CharFieldManager?*/
public class OntologyManager {

  final static String ANATOMY = "Anatomy";
  final static String PATO = "Pato";
  final static String TAXONOMY = "Taxonomy";

  private static OntologyManager singleton;
  private Ontology lumpOntology;
  private List<Ontology> entityOntologyList;
  //private Ontology entityOntology;
  private Ontology patoOntology;
  private List<Ontology> allOntologyList = new ArrayList<Ontology>();
  private Ontology geneticContextOntology;
  //private Map<CharFieldEnum,List<Ontology>> fieldToOntologyList;
  private List<CharField> charFieldList = new ArrayList<CharField>(6);


  /** Singleton */
  private OntologyManager() {
    //fieldToOntologyList = new HashMap<CharFieldEnum,List<Ontology>>();
  }
  
  public static OntologyManager inst() {
    if (singleton == null)
      singleton = new OntologyManager();
    return singleton;
  }

  // public List<Ontology> getOntologyList(CharFieldEnum e) {}

  public boolean haveLumpOntology() { return getLumpOntology() != null; }

  public void setLumpOntology(Ontology lo) {
    lumpOntology = lo;
    addOntology(lumpOntology);
  }

  public Ontology getLumpOntology() {
    return lumpOntology;
  }

  public void setEntityOntologyList(List<Ontology> entList) {
    entityOntologyList = entList;
  }

  public void addEntityOntology(Ontology o) {
    getEntityOntologyList().add(o);
    allOntologyList.add(o);
  }

  public List<Ontology> getEntityOntologyList() {
    if (entityOntologyList == null)
      entityOntologyList = new ArrayList<Ontology>();
    return entityOntologyList;
  }

  // for now just 1
  public Ontology getEntityOntology() {
    return getEntityOntologyList().get(0);
  }

  /** Set from OntologyDataAdapter */
  public void setPatoOntology(Ontology patoOntology) {
    this.patoOntology = patoOntology;
    addOntology(patoOntology);
  }

  // pase?
  public Ontology getPatoOntology() {
    //if (patoOntology != null)
    return patoOntology;
  }

  public boolean hasGeneticContext() {
    return geneticContextOntology != null;
  }

  public void setGeneticContextOntology(Ontology gc) {
    geneticContextOntology = gc;
    addOntology(geneticContextOntology);
  }

  public Ontology getGeneticContextOntology() {
    return geneticContextOntology;
  }


  // eventually...
  // Ontology getOntology(CharFieldEnum c) { return fieldToOntology.get(c); }

//   public void addOntology(CharFieldEnum c, Ontology o) {
//     // for now...
//     if (c == CharFieldEnum.GENETIC_CONTEXT)
//       setGeneticContextOntology(o);
//     addOntology(o);

//     // OR
//     // addOntologyToMap(c,o);

//     // OR
//     charFieldList.add(new CharField(c,o));
//   }
  
  public void addField(CharField cf) {
    // for now...
    // if hasOnlyOne? for (Ontology :...)?
    if (cf.isGeneticContext())
      setGeneticContextOntology(cf.getFirstOntology());
    addOntologyList(cf.getOntologyList());
    charFieldList.add(cf);
  }

  /** This is where the ontologies are in a generic fashion. A char field
      has one or more ontologies (entity char field often has more than ontology)*/
  public List<CharField> getCharFieldList() { return charFieldList; }

  // for now i know that only genetic context is in char field list...
  public CharField getGeneticContextCharField() {
    if (charFieldList == null || charFieldList.isEmpty())
      return null;
    return charFieldList.get(0); // revisit this!!!!
  }
  

//   private void addOntologyToMap(CharFieldEnum c, Ontology o) {
//     List<Ontology> l = fieldToOntologyList.get(c);
//     if (l == null) {
//       l = new ArrayList<Ontology>(3);
//       fieldToOntologyList.put(c,l);
//     }
//     l.add(o);
//   }

  private void addOntologyList(List<Ontology> l) {
    allOntologyList.addAll(l);
  }

  private void addOntology(Ontology o) {
    allOntologyList.add(o);
  }

  /** Searches all ontologies for id - this could be even more savvy and utilize
      the id prefix AO,GO,PATO... */
  public OBOClass getOboClass(String id) {
    OBOClass oboClass;
    Iterator<Ontology> iter = allOntologyList.iterator();
    while (iter.hasNext()) {
      Ontology o = iter.next();
      oboClass = o.getOboClass(id);
      if (oboClass != null)
        return oboClass;
    }
    return null; // not found - null
  }

  // ???



}
