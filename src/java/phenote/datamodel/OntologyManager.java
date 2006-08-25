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


  private static OntologyManager singleton;
  // isnt this redundant with charFieldList? convenience?
  private List<Ontology> allOntologyList = new ArrayList<Ontology>();
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
    addOntologyList(cf.getOntologyList());
    charFieldList.add(cf);
  }

  /** This is where the ontologies are in a generic fashion. A char field
      has one or more ontologies (entity char field often has more than ontology)*/
  public List<CharField> getCharFieldList() { return charFieldList; }

  /** Returns ontology with name, null if not found */
  public static Ontology getOntologyForName(String ontologyName) { // static?
    for (CharField cf : inst().getCharFieldList()) {
      if (cf.hasOntology(ontologyName))
        return cf.getOntologyForName(ontologyName);
    }
    System.out.println("ERROR: no ontology found for name "+ontologyName);
    return null;
  }


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

  /** for obo class find its char field enum via ontology & char field */
  public CharFieldEnum getCharFieldEnumForOboClass(OBOClass oboClass) {
    for (CharField cf : charFieldList) {
      //if (!cf.hasOntologies()) continue; // is this needed? not sure
      for (Ontology ont : cf.getOntologyList()) {
        if (ont.hasOboClass(oboClass))
          return cf.getCharFieldEnum();
      }
    }
    return null; // this shouldnt happen - err msg?
  } 

}

  // i think char field now does this
    //fieldToOntologyList = new HashMap<CharFieldEnum,List<Ontology>>();
  //private Map<CharFieldEnum,List<Ontology>> fieldToOntologyList;
//  private List<Ontology> entityOntologyList;
  //private Ontology geneticContextOntology;
  //private Ontology lumpOntology;
  //private Ontology entityOntology;
  //private Ontology patoOntology;

//   final static String ANATOMY = "Anatomy";
//   final static String PATO = "Pato";
//   final static String TAXONOMY = "Taxonomy";
    // for now...
    // if hasOnlyOne? for (Ontology :...)?
//     if (cf.isGeneticContext())
//       setGeneticContextOntology(cf.getFirstOntology());

  // for now i know that only genetic context is in char field list...
//   public CharField getGeneticContextCharField() {
//     if (charFieldList == null || charFieldList.isEmpty())
//       return null;
//     return charFieldList.get(0); // revisit this!!!!
//   }
  

//   private void addOntologyToMap(CharFieldEnum c, Ontology o) {
//     List<Ontology> l = fieldToOntologyList.get(c);
//     if (l == null) {
//       l = new ArrayList<Ontology>(3);
//       fieldToOntologyList.put(c,l);
//     }
//     l.add(o);
//   }


  // public List<Ontology> getOntologyList(CharFieldEnum e) {}

//   public boolean haveLumpOntology() { return getLumpOntology() != null; }

//   public void setLumpOntology(Ontology lo) {
//     lumpOntology = lo;
//     addOntology(lumpOntology);
//   }

//   public Ontology getLumpOntology() {
//     return lumpOntology;
//   }

//   public void setEntityOntologyList(List<Ontology> entList) {
//     entityOntologyList = entList;
//   }

//   public void addEntityOntology(Ontology o) {
//     getEntityOntologyList().add(o);
//     allOntologyList.add(o);
//   }

//   public List<Ontology> getEntityOntologyList() {
//     if (entityOntologyList == null)
//       entityOntologyList = new ArrayList<Ontology>();
//     return entityOntologyList;
//   }

//   // for now just 1
//   public Ontology getEntityOntology() {
//     return getEntityOntologyList().get(0);
//   }

  /** Set from OntologyDataAdapter */
//   public void setPatoOntology(Ontology patoOntology) {
//     this.patoOntology = patoOntology;
//     addOntology(patoOntology);
//   }

  // pase?
//   public Ontology getPatoOntology() {
//     //if (patoOntology != null)
//     return patoOntology;
//   }

//   public boolean hasGeneticContext() {
//     return geneticContextOntology != null;
//   }

//   public void setGeneticContextOntology(Ontology gc) {
//     geneticContextOntology = gc;
//     addOntology(geneticContextOntology);
//   }

//   public Ontology getGeneticContextOntology() {
//     return geneticContextOntology;
//   }


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
