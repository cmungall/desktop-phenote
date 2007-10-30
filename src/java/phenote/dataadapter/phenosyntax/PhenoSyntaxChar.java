package phenote.dataadapter.phenosyntax;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;

import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterIFactory;

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

    try {

      // ontology manager should have char fields in order of config which should be
      // syntax order - hope this isnt too presumptious
      for (CharField cf : CharFieldManager.inst().getCharFieldList()) {
        if (character.hasValue(cf)) {
          sb.append(Config.inst().getSyntaxAbbrevForCharField(cf)).append("=");//ex
          if (isFreeText(cf)) sb.append('"'); // free text gets quoted
          sb.append(makeValue(character.getValue(cf)));
          if (isFreeText(cf)) sb.append("\" ");
//          sb.append("\n");
        }
        // check for entity & quality??
      }

    }
    catch (ConfigException e) {
      throw new BadCharException(e.getMessage());
    }
    return sb.toString();
  }
   
  /** If a char field has ontologies it is not free text */
  private boolean isFreeText(CharField cf) {
    return cf.isFreeText();//!cf.hasOntologies();
  }



  // this may be more general than just this class
  class BadCharException extends Exception {
    BadCharException(String m) { super(m); }
  }

  private String makeValue(CharFieldValue v) {
    if (v.isTerm()) return makeTermValue(v.getOboClass());
    return v.getName()+" "; // just in case
  }

  private String makeTermValue(OBOClass term) {
    // id & commented out name for readability
    return term.getID() + " /*" + term.getName() + "*/ ";
  }


  // READ

  /** Parse syntax line into character */
  void parseLine(String line) throws SyntaxParseException {
    character = CharacterIFactory.makeChar();
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
    //    if (value.endsWith(q)) value = value.substring(0,value.length()-2);
    //  changed this to "-1" because it was messing up the reader
    if (value.endsWith(q)) value = value.substring(0,value.length()-1);
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
    
    CharFieldManager om = CharFieldManager.inst();
    
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
        catch (CharFieldException e) {} // do nothing - try next char field
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
