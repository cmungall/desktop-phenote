package phenote.dataadapter.fly;

import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.datamodel.CharacterI;
import phenote.datamodel.Character;
import phenote.datamodel.OntologyManager;

public class FlyCharacter implements FlyCharacterI {

  private CharacterI character;

  private final static String ENTITY = "ENTITY";
  private final static String VALUE = "VALUE";
  private final static String GENETIC_CONTEXT = "GENETIC_CONTEXT";

  FlyCharacter(CharacterI c) {
    this.character = c; //.cloneCharacter(); cloned in flycharList
  }

  public FlyCharacter(String genotype, String proformaeEVString) {
    character = new Character();
    character.setGenotype(genotype);
    parseEVString(proformaeEVString);
  }
  
  CharacterI getCharacter() { return character; }

  private void parseEVString(String evString) {
    // todo...
    String[] fields = evString.split("\\|"); // \| ?
    for (String f : fields) {
      if (!f.contains(">")) {
        print("Skipping proforma field: lacks > field:"+f+" prof: "+evString);
        continue; // skip it
      }
      String[] fieldParts = f.split(">");
      String key = fieldParts[0].trim();
      if (fieldParts.length == 1) {
        print("Skipping prof field, lacks term info: "+key);
        continue;
      }
      String term = fieldParts[1].trim();
      editCharacter(key,term);
    }
  }
  
  private void print(String m) { System.out.println(m); }

  private void editCharacter(String key, String term) {
    // eventually parse term name & id - for now its just a name
    //String termName = term;
    try {
      OBOClass oboClass = termStringToClass(term);
      if (key.equals(ENTITY))
        character.setEntity(oboClass);
      else if (key.equals(VALUE))
        character.setPato(oboClass);
      else if (key.equals(GENETIC_CONTEXT))
        character.setGeneticContext(oboClass);
    }
    catch (TermException e) {
      print("Error retrieving onotolgy term for "+key+": "+e);
    }
  }

  /** termString should be in format "name ; id", find obo class for id 
   throws exception if problem parsing id or finding term or if data name & 
   ontology name for id dont match */
  private OBOClass termStringToClass(String termString) throws TermException {
    String [] termParts = termString.split(";");
    if (termParts == null || termParts.length != 2)
      throw new TermException("No ';' in term string ["+termString+"]");
    String name = termParts[0].trim();
    String id = termParts[1].trim();
    if (id == null || id.equals(""))
      throw new TermException("Failed to get term id "+id);
    OBOClass oc = OntologyManager.inst().getOboClass(id);
    if (oc == null)
      throw new TermException("Couldnt find ontology term for id "+id);
    if (!oc.getName().equals(name))
      throw new TermException("Data name "+name+" and ontology name "+oc.getName()+
                              "are inconsistent for id "+id);
    return oc;
  }

  private class TermException extends Exception {
    private TermException(String s) { super(s); }
  }

  /** Makes string like this: 
      "ENTITY > head ; AO:12 | VALUE > large ; PATO:34 | GENETIC_CONTEXT > dom ; GC:234"
  */
  public String getEVString() {
    // make ev string from character - need ids!
    //return null; //....
    StringBuffer sb = new StringBuffer();
    sb.append(ENTITY).append(" > ").append(termString(character.getEntity()));
    sb.append("|");
    sb.append(VALUE).append(" > ").append(termString(character.getPato()));
    if (character.hasGeneticContext()) {
      sb.append("|");
      sb.append(GENETIC_CONTEXT).append(" > ");
      sb.append(termString(character.getGeneticContext()));
    }
    return sb.toString();
  }

  private String termString(OBOClass oboClass) {
    if (oboClass == null) {
      System.out.println("data faulty, null ontology term found");
      return ""; // ??
    }
    return oboClass.getName() + " ; " + oboClass.getID();
  }

  public String getGenotype() {
    return character.getGenotype();
  }
}
