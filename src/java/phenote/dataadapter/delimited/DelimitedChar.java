package phenote.dataadapter.delimited;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.datamodel.Character;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.OntologyManager;
import phenote.datamodel.TermNotFoundException;
import phenote.config.Config;
import phenote.config.ConfigException;

/** I stole this from phenosyntaxchar, but modifying for tab delimited.  will
 *  initially hard code in the tab-delimiter, but eventually it should be generic to 
 *  handle any delimiter.
 * A phenotype character thats basically a dataadapter object for datamodel CharacterI. 
*/

public class DelimitedChar {

  private CharacterI character;
  private String delimitedString;
  private String delimitedHeaderString;
  private String delimiter;

  DelimitedChar() {}

  DelimitedChar(CharacterI ch) {
    character = ch;
    delimiter = "\t";
  }

  // WRITE
  String getDelimitedString() throws BadCharException {
    if (delimitedString == null) {
      delimitedString = makeDelimitedString();
    }
    return delimitedString;
  }
  String getDelimitedHeaderString() throws BadCharException {
	if (delimitedHeaderString == null) {
      delimitedHeaderString = makeDelimitedHeaderString();
	}
	return delimitedHeaderString;
  }
  
/** the idea for the tab-delimited output writer is to 
 * 1) identify what the header/column names should be (like they present in the phenote 
 * window:  pub, entity, quality, etc.).  Need to include all column headers even if there
 * isn't data for them...they'd just have a blank column...in future will get smart to this.
 * basically its creating the headers based on the config file.
 * 2) insert delimiter (initially "tab") between each field
 * 3) write each phenotype character on an individual line under headers.  one PC for each 
 * hard return
 * should i include the config file on the first line?
 * */
  private String makeDelimitedHeaderString() throws BadCharException { 
	if (character == null) { // shouldnt happen
 	  System.out.println("Error: could not make tab-delimited header");
	  return ""; //??
	}
	StringBuffer sb = new StringBuffer();
	try {
    // ontology manager should have char fields in order of config which should be
    // syntax order - hope this isnt too presumptious
      for (CharField cf : OntologyManager.inst().getCharFieldList()) {
    	  //if its a free text field - only one col necessary.
    	  //if its an ontology field - need two cols (one for ID, the other for text)
 	    sb.append(Config.inst().getSyntaxAbbrevForCharField(cf));
 	    //for now, i'll call on the syntax abbrev, but i'll want to use the acutal name
        if (!isFreeText(cf)) { 
          sb.append(" ID").append(delimiter);
          sb.append(Config.inst().getSyntaxAbbrevForCharField(cf)).append(" Name");
        }
        sb.append(delimiter);
      }
	}
    catch (ConfigException e) {
      throw new BadCharException(e.getMessage());
    }
    return sb.toString();
	  
  }
  
  private String makeDelimitedString() throws BadCharException {
    if (character == null) { // shouldnt happen
      System.out.println("Error: no Character to make delimited string with");
      return ""; //??
    }
    StringBuffer sb = new StringBuffer();
//*****************
    try {

        // ontology manager should have char fields in order of config which should be
        // syntax order - hope this isnt too presumptious
        for (CharField cf : OntologyManager.inst().getCharFieldList()) {
          if (character.hasValue(cf)) {
        	  Config.inst().getLabelForCharField(cf);
        	  //           sb.append(Config.inst().getLabelForCharField(cf));
            sb.append(makeValue(character.getValue(cf)));
          }
          else if (isOntology(cf)) sb.append(delimiter);  
          //need to make sure to add extra delimiter for ontology fields
        sb.append(delimiter);
        }

      }
      catch (ConfigException e) {
        throw new BadCharException(e.getMessage());
      }
    

     return sb.toString();
  }
   
  /** If a char field has ontologies it is not free text */
  private boolean isFreeText(CharField cf) {
    return !cf.hasOntologies();
  }
  
  private boolean isOntology(CharField cf) {
	return cf.hasOntologies();
  }

  // this may be more general than just this class
  class BadCharException extends Exception {
    BadCharException(String m) { super(m); }
  }

  private String makeValue(CharFieldValue v) {
    if (v.isTerm()) return makeTermValue(v.getOboClass());
    return v.getName();
  }

  private String makeTermValue(OBOClass term) {
	//if the term comes from an ontology, need to include a col for both
	//id and name
    return term.getID() + delimiter + term.getName();
  }

//********READ NOT YET IMPLEMENTED!!!
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
      //value = stripQuotesFromFreeText(value); // free text gets quoted
      //System.out.println("tag ."+tag+". val ."+value+".");
      addTagValToChar(tag,value);
    }
  }

  private String stripComments(String value) {
    value = value.replaceAll("/\\*.*\\*/","");
    return value;
  }

  private static final String q = "\"";

  private String stripQuotesFromFreeText(String value,CharField cf) {
    if (!isFreeText(cf)) return value;
    if (value.startsWith(q)) value = value.substring(1);
    if (value.endsWith(q)) value = value.substring(0,value.length()-2);
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
      log().error("No value given for "+tag);
      return;
    }
    
    OntologyManager om = OntologyManager.inst();
    
    try {
      // so this is funny but there can be more than one char field for an abbrev - for
      // instance Tag is for both abormal and absent. with setValue only the proper char
      // field will get set, eg abnormal char field will throw exception for "absent"
      List<CharField> fields = Config.inst().getCharFieldsForSyntaxAbbrev(tag);//Ex
      for (CharField cf : fields) {
        
        value = stripQuotesFromFreeText(value,cf);

        if (cf.getName().equals("Stage")) {
          // todo - a general relationship extracter?
          value = extractStageHack(value); // for now - fix for real later
        }
        try {
          // set String -> for obo class automatically find term
          character.setValue(cf,value); // throws TermNotFoundEx
          return; // if no ex thrown were done
        }
        catch (TermNotFoundException e) {} // do nothing - try next char field
      }
    }
    catch (ConfigException e) { log().error(e.getMessage()); } // field not found
    //catch (TermNotFoundException e) {
    System.out.println("PhSynCh term not found "+value);
    //log().error(e.getMessage());
    log().error("Term not found "+value); // list char field?
    //}

      
  }

  /** Stages come with rel - eg during(adult) - for now just assuming its during and ripping
      off - in future need to read in rel, and store as relationship between Instances
      (not OBOClasses!) - big refactor but go for it! 
      this extracts the "adult" in above example out of "during(adult)" 
      or alternatively just record relationship in CharFieldValue? */
  private String extractStageHack(String stageWithRel) {
    Pattern p = Pattern.compile("during\\(([^\\)]+)\\)");
    Matcher m = p.matcher(stageWithRel);
    if (m.matches())
      return m.group(1);
    return stageWithRel;
  }

  CharacterI getCharacter() { return character; }

  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}
//     try {
//       if (tag.equals("PUB")) 
//         character.setPub(value);
//       else if (tag.equals("GT"))
//         character.setGenotype(value);
//       else if (tag.equals("GC"))
//         character.setGeneticContext(om.getOboClassWithExcep(value)); // throws ex
//       else if (tag.equals("E"))
//         character.setEntity(om.getTermOrPostComp(value));
//       else if (tag.equals("Q"))
//         character.setQuality(om.getOboClassWithExcep(value));
//       else // throw exception? or let rest of char go through?
//         System.out.println("pheno syntax tag "+tag+" not recognized (value "+value+")");
//     }
//     catch (OntologyManager.TermNotFoundException e) {
//       log().error("Term not found for tag "+tag+" value "+value+" in loaded "
//                   +"ontologies - check syntax with ontology files.");
//       return;
//     }
//       if (character.hasValue("Pub")) // hasPub
//         sb.append("PUB=").append(character.getValueString("Pub")); //Pub());
//       // Genotype - not strictly part of pheno syntax but lets face it we need it
//       // i would say its an omission from syntax
//       //sb.append(" GT=").append(character.getGenotype());
//       if (character.hasValue("Genotype"))
//         sb.append(" GT=").append(character.getValueString("Genotype"));
//       if (character.hasValue("Genetic Context"))
//         sb.append(" GC=").append(makeValue(character.getTerm("Genetic Context")));
      
//       if (!character.hasValue("Entity"))
//         throw new BadCharException("Error: character has no entity, ignoring");
//       //sb.append(" E=").append(makeValue(character.getEntity()));
//       sb.append(" E=").append(makeValue(character.getTerm("Entity")));

//       // if (character.hasValue(CharFieldEnum.STAGE))
      
//       //if (character.getQuality() == null)
//       if (!character.hasValue("Quality"))
//         throw new BadCharException("Error: character has no quality, ignoring");
//       sb.append(" Q=").append(makeValue(character.getTerm("Quality")));
