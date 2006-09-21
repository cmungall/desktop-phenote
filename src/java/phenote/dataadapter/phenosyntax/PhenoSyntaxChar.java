package phenote.dataadapter.phenosyntax;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.datamodel.Character;
import phenote.datamodel.CharacterI;
import phenote.datamodel.OntologyManager;

/** A phenotype character thats basically a dataadapter object for datamodel
    CharacterI. It can make a phenosyntax string from a CharacterI and make
    a CharacterI from a phenosyntax string  e.g. E=head Q=large
    See http://www.fruitfly.org/~cjm/obd/pheno-syntax.html for a full description
    of pheno syntax.

    Phenote additions: syntax doesnt do genotype or genetic context but heck we
    need them - so i added GT=genotype (do we gen context?) GC=geneticContext
*/

public class PhenoSyntaxChar {

  private CharacterI character;
  private String phenoSyntaxString;

  PhenoSyntaxChar() {}

  PhenoSyntaxChar(CharacterI ch) {
    character = ch;
  }

  // WRITE
  String getPhenoSyntaxString() throws BadCharException {
    if (phenoSyntaxString == null) {
      phenoSyntaxString = makeSyntaxString();
    }
    return phenoSyntaxString;
  }

  private String makeSyntaxString() throws BadCharException {
    if (character == null) { // shouldnt happen
      System.out.println("Error: no Character to make phenoSyntax string with");
      return ""; //??
    }
    StringBuffer sb = new StringBuffer();

    if (character.hasPub())
      sb.append("PUB=").append(character.getPub());
    // Genotype - not strictly part of pheno syntax but lets face it we need it
    // i would say its an omission from syntax
    sb.append(" GT=").append(character.getGenotype());
    if (character.hasGeneticContext())
      sb.append(" GC=").append(makeValue(character.getGeneticContext()));

    if (character.getEntity() == null)
      throw new BadCharException("Error: character has no entity, ignoring");
    sb.append(" E=").append(makeValue(character.getEntity()));

    if (character.getQuality() == null)
      throw new BadCharException("Error: character has no quality, ignoring");
    sb.append(" Q=").append(makeValue(character.getQuality()));

    return sb.toString();
  }

  // this may be more general than just this class
  class BadCharException extends Exception {
    BadCharException(String m) { super(m); }
  }

  private String makeValue(OBOClass term) {
    // return idPrefixAndName(term); // michael wants ids...
    //return term.getID();
    // id & commented out name for readability
    return term.getID() + " /*" + term.getName() + "*/";
  }

  /** Merges id prefix and name, so for id GO:1234 with name "growth" returns
      "GO:growth", which is readable and computable & syn acceptable - pase - doing ids*/
  private String idPrefixAndName(OBOClass term) {
    return getIdPrefix(term)+term.getName();
  }
    

  /** for GO:12345 returns GO: - with colon! - pase - doing ids */
  private String getIdPrefix(OBOClass term) {
    if (term == null) return ""; // shouldnt happen
    String id = term.getID();
    int colonIndex = id.indexOf(":");
    return id.substring(0,colonIndex+1); // +1 retain colon
  }



  // READ

  /** Parse syntax line into character */
  void parseLine(String line) throws SyntaxParseException {
    character = new Character();
    Pattern p = Pattern.compile("\\S+=");//\\S+=");
    Matcher m = p.matcher(line);
    boolean found = m.find();
    if (!found)
      throw new SyntaxParseException(line); // skips whitespace lines
    int tagStart = m.start();
    int tagEnd = m.end();
    while (found) {
      String tag = line.substring(tagStart,tagEnd-1); // -1 take off =
      int valueStart = tagEnd;
      // find next one will give end of value
      found = m.find(); // if parsing last tag found will be false - at end
      tagStart = found ? m.start() : line.length();
      if (found) tagEnd = m.end(); // dont need if not found (last one)
      String value = line.substring(valueStart,tagStart).trim();
      value = stripComments(value).trim();
      //System.out.println("tag ."+tag+". val ."+value+".");
      addTagValToChar(tag,value);
    }
  }

  private String stripComments(String value) {
    value = value.replaceAll("/\\*.*\\*/","");
    return value;
  }

  class SyntaxParseException extends Exception {
    private String syntaxLine;
    SyntaxParseException(String syntaxLine) {
      this.syntaxLine = syntaxLine;
    }
    public String getMessage() {
      if (syntaxLine.trim().equals(""))
        return ""; // just whitespace - who cares
      return syntaxLine+" failed to parse - ignoring";
    }
  }

  private void addTagValToChar(String tag, String value) {
    if (value.equals("")) {
      System.out.println("No value given for "+tag);
      return;
    }
    
    OntologyManager om = OntologyManager.inst();

    try {
      if (tag.equals("PUB")) 
        character.setPub(value);
      else if (tag.equals("GT"))
        character.setGenotype(value);
      else if (tag.equals("GC"))
        character.setGeneticContext(om.getOboClassWithExcep(value)); // throws ex
      else if (tag.equals("E"))
        character.setEntity(om.getOboClassWithExcep(value));
      else if (tag.equals("Q"))
        character.setQuality(om.getOboClassWithExcep(value));
      else // throw exception? or let rest of char go through?
        System.out.println("pheno syntax tag "+tag+" not recognized (value "+value+")");
    }
    catch (OntologyManager.TermNotFoundException e) {
      log().error("Term not found for tag "+tag+" value "+value+" in loaded "
                  +"ontologies - check syntax with ontology files.");
      return;
    }
      
  }


  CharacterI getCharacter() { return character; }

  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}
