package phenote.dataadapter.delimited;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;

import phenote.config.Config;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterIFactory;

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
  private List<DelimFieldParser> fieldParsers = new ArrayList<DelimFieldParser>();

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
  private String makeDelimitedHeaderString() { //throws BadCharException { 
    if (character == null) { // shouldnt happen
      System.out.println("Error: could not make tab-delimited header");
      return ""; //??
    }
    StringBuffer sb = new StringBuffer();
    //try {
    // ontology manager should have char fields in order of config which should be
    // syntax order - hope this isnt too presumptious
    for (CharField cf : CharFieldManager.inst().getCharFieldList()) {
      //if its a free text field - only one col necessary.
      //if its an ontology field - need two cols (one for ID, the other for text)
      // the label from config & char field should be the same, so dont need to get
      // from config - was there some reason for this???
      //String label = Config.inst().getLabelForCharField(cf);
      String label = cf.getName();
      sb.append(label);
      //for now, i'll call on the syntax abbrev, but i'll want to use the acutal name
      // if (!isFreeText(cf)) { // }		// now have third type isInt, so can't just negate 2007 07 09
      if (isTerm(cf)) { 
        sb.append(" ID").append(delimiter);
        sb.append(label).append(" Name");
        }
      sb.append(delimiter);
    }
//     }
//     catch (ConfigException e) {
//       throw new BadCharException(e.getMessage());
//     }
    return sb.toString();
    
  }
  
  // Character -> String, for writeback of course
  private String makeDelimitedString() throws BadCharException {
    if (character == null) { // shouldnt happen
      log().error("Error: no Character to make delimited string with");
      return ""; //??
    }
    StringBuffer sb = new StringBuffer();
    //try {
    // ontology manager should have char fields in order of config which should be
    // syntax order - hope this isnt too presumptious
    for (CharField cf : CharFieldManager.inst().getCharFieldList()) {
      if (character.hasValue(cf)) {
        sb.append(makeValue(character.getValue(cf)));
      }
      // if empty term then do 2 delimiters since it takes up 2 spots
      else if (isTerm(cf)) sb.append(delimiter); 
      //need to make sure to add extra delimiter for ontology fields
      sb.append(delimiter);
    }
    
//  }catch(ConfigException e){throw new BadCharException(e.getMessage());}
    
    return sb.toString();
  }
   
  /** If a char field has ontologies it is not free text */
  private boolean isFreeText(CharField cf) {
    // return !cf.hasOntologies();	// now have third type isInt, so can't just negate 2007 07 09
    return cf.isFreeText();
  }
  
  private boolean isTerm(CharField cf) {
    return cf.isTerm(); //hasOntologies();
  }

  // this may be more general than just this class
  class BadCharException extends Exception {
    BadCharException(String m) { super(m); }
  }

  private String makeValue(CharFieldValue v) {
    if (v.isTerm()) return makeTermValue(v.getOboClass());
    return v.getValueAsString();
  }

  private String makeTermValue(OBOClass term) {
	//if the term comes from an ontology, need to include a col for both
	//id and name
    System.out.println("DelimChar term.getID: "+term.getID());
    return term.getID() + delimiter + term.getName();
  }

  // READ - split read & write into 2 classes?

  /** make DelimFieldParsers from headerLine, throw exception 
      if line fails to be a header */
  void setHeader(String headerLine) throws DelimitedEx {
    String[] colHeaders = splitLine(headerLine);
    if (colHeaders.length == 0) throw new DelimitedEx(headerLine);
    int i=0;
    boolean done = false;
    while (i<colHeaders.length && !done) {
      try {
        DelimFieldParser p = DelimFieldParser.makeNextParser(colHeaders,i);
        fieldParsers.add(p);
        // may parse 1 field, may parse 2, may also skip unfound fields
        i = p.getLastParseField() + 1;
      } //?? thrown if rest of fields not configged
      catch (DelimitedEx e) { done = true; } 
    }
    if (fieldParsers.isEmpty())
      throw new DelimitedEx(headerLine);
  }

  private String[] splitLine(String line) {
    //parse based on tab...will be delimiter in future
    final String delimiter = "\t";
    final Pattern p = Pattern.compile(delimiter);
    final String trimmedLine = (line.endsWith(delimiter)) ? (line.substring(0, line.length() -1)) : line;
    return p.split(trimmedLine, -1);
  }


  void parseLine(String line) throws DelimitedEx {
    character = CharacterIFactory.makeChar();
    String[] items = splitLine(line);
    if (items.length==0) throw new DelimitedEx(line); // BlankEx?
    for (DelimFieldParser p : fieldParsers) {
      p.parseField(items,character); // add cfv to char or ret cfv?
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


  /** READ, if term then value is ID and termName is name of term, if free text then
      value is free text and termName is null */
  private void addDelValToChar(int fieldNum, String value, String termName) {
//  it doesn't really matter if there's a blank column in this mode
    if (value.equals("")) {
//      log().error("No value given for column"+fieldNum);
      return;  //don't need to populate a CharField if no value
    }    
    CharFieldManager om = CharFieldManager.inst();
    //try { // there can be more than one field for a fieldNum??? - MG
    //List<CharField> fields = Config.inst().getCharFieldsForDelimited(fieldNum);//Ex
    if (!Config.inst().hasEnbldCharField(fieldNum)) {
      log().error("No Field configged for column # "+fieldNum);
      return;
    }
    CharField cf = Config.inst().getEnbldCharField(fieldNum);
    //List<CharField> fields=Config.inst().getCharFieldsForSyntaxAbbrev(tag);//Ex
    //for (CharField cf : fields) {
    try {
      //System.out.println("column="+fieldNum+"; value = "+value);
      // set String -> for obo class automatically find term
      // if term not found makes a dangler
      //this assumes that you are loading data the same order you saved it
      CharFieldValue v = character.setValue(cf,value); // throws CFEx for bad date
      if (v.isDangler())
        v.setName(termName); // ???
      return; // if no ex thrown were done
    }
    // thrown for bad date, not for not found - dangler created instead!
    catch (CharFieldException e) {
      System.out.println(e.getMessage()); // ?
      log().error(e.getMessage());
    } 
  }

  CharacterI getCharacter() { return character; }

  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}

//   /** Parse syntax line into character - throw DelimitedEx if blank line/no
//       columns found - just splitting at tab.  */
//   void parseLineOld_DELETE(String line) throws DelimitedEx { // cf list?
//     character = CharacterIFactory.makeChar();
// //		System.out.println("input line="+line);
//     String[] items = splitLine(line);

//     // doesnt split always return at least 1 item even for empty line???
//     boolean found = (items.length>0); //m.find();
// //		System.out.println("numcols="+items.length);
//     if (!found)
//       throw new DelimitedEx(line); // skips whitespace lines ??
//     int colCount = 0;
//     int fieldCount = 0;
//     while (found) {
//       String value = items[colCount];
//       // this aint right - this hardwires column # to field, should use column name!
//       CharField c = Config.inst().getEnbldCharField(fieldCount);
// //System.out.println("col="+colCount+";  fieldCount="+fieldCount+"; val="+value+"; charfieldname ="+c.getName());
//       String termName = null;
//       if (c.isTerm()) {
//         colCount++; //skip over the Name, only keep ID
//         termName = items[colCount];
//       }
//       addDelValToChar(fieldCount,value,termName); // termName null if not term
//       colCount++;
//       fieldCount++;
//       found = (colCount<items.length); // if parsing last tag found will be false - at end
//     }
//  }
