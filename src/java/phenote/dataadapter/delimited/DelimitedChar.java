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
    delimiter = "\t";  //delimiter is currently set to tab
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
 * should i include the config file on the first line?...maybe in the future
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
 	    sb.append(Config.inst().getLabelForCharField(cf));
 	    //for now, i'll call on the syntax abbrev, but i'll want to use the acutal name
        if (!isFreeText(cf)) { 
          sb.append(" ID").append(delimiter);
          sb.append(Config.inst().getLabelForCharField(cf)).append(" Name");
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
    System.out.println("DelimChar term.getID: "+term.getID());
    return term.getID() + delimiter + term.getName();
  }

  // READ

  /** Parse syntax line into character */
  //try just splitting at tab.
  void parseLine(String line) throws SyntaxParseException {
		character = new Character();
//		System.out.println("input line="+line);
		Pattern p = Pattern.compile("\t");
		//parse based on tab...will be delimiter in future
		String[] items = p.split(line);
		boolean found = (items.length>0); //m.find();
//		System.out.println("numcols="+items.length);
		if (!found)
		  throw new SyntaxParseException(line); // skips whitespace lines
		int colCount = 0;
		int fieldCount = 0;
		while (found) {
		  String value = items[colCount];
		  addDelValToChar(fieldCount,value);
		  CharField c = Config.inst().getCharField(fieldCount);
//		  System.out.println("col="+colCount+";  fieldCount="+fieldCount+"; val="+value+"; charfieldname ="+c.getName());
	      if (isOntology(c)) {
	        colCount++; //skip over the Name, only keep ID
	      }
		  colCount++;
		  fieldCount++;
		  found = (colCount<items.length); // if parsing last tag found will be false - at end
		}
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


  
  private void addDelValToChar(int fieldNum, String value) {
//  it doesn't really matter if there's a blank column in this mode
    if (value.equals("")) {
//      log().error("No value given for column"+fieldNum);
      return;  //don't need to populate a CharField if no value
    }    
    OntologyManager om = OntologyManager.inst();
    try {
      // there can be more than one field for a fieldNum??? - MG
      List<CharField> fields = Config.inst().getCharFieldsForDelimited(fieldNum);//Ex
      //      List<CharField> fields = Config.inst().getCharFieldsForSyntaxAbbrev(tag);//Ex

      for (CharField cf : fields) {
    	try {
    	    //System.out.println("column="+fieldNum+"; value = "+value);
          // set String -> for obo class automatically find term
          //this assumes that you are loading data the same order you saved it
          character.setValue(cf,value); // throws TermNotFoundEx
    	return; // if no ex thrown were done
        }
        catch (TermNotFoundException e) {} // do nothing - try next char field
      }
    }
    catch (ConfigException e) { log().error(e.getMessage()); } // field not found
    //catch (TermNotFoundException e) {
    System.out.println("Error1: term not found ("+value+")");
    //log().error(e.getMessage());
    log().error("Error2: Term not found ("+value+")"); // list char field?
    //}      
  }

  CharacterI getCharacter() { return character; }

  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}
