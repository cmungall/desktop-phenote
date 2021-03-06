package phenote.dataadapter.delimited;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import phenote.config.Config;
import phenote.datamodel.CharField;
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
  private static final String DEFAULT_DELIMITER = "\t";
  private List<DelimFieldParser> fieldParsers = new ArrayList<DelimFieldParser>();

  DelimitedChar() {}

  DelimitedChar(CharacterI ch) {
    character = ch;
    delimiter = DEFAULT_DELIMITER;  //delimiter is currently set to tab
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
  public static String makeDelimitedHeaderString() { //throws BadCharException { 
    StringBuffer sb = new StringBuffer();
    for (CharField cf : CharFieldManager.inst().getCharFieldList()) {
      //if its a free text field - only one col necessary.
      //if its an ontology field - need two cols (one for ID, the other for text)
      // the label from config & char field should be the same, so dont need to get
      // from config - was there some reason for this?
      //String label = Config.inst().getLabelForCharField(cf);
      String label = cf.getName();
      sb.append(label);
      //for now, i'll call on the syntax abbrev, but i'll want to use the acutal name
      // if (!isFreeText(cf)) { // } // now have third type isInt, so can't just negate 2007 07 09
      if (cf.isTerm()) { 
        sb.append(" ID").append(DEFAULT_DELIMITER);
        sb.append(label).append(" Name");
        }
      sb.append(DEFAULT_DELIMITER);
    }
    return sb.toString();
    
  }
  
  /** Character -> String, writeback */
  private String makeDelimitedString() throws BadCharException {
    if (character == null) { // shouldnt happen
      log().error("Error: no Character to make delimited string with");
      return ""; //??
    }
    StringBuffer sb = new StringBuffer();
    for (CharField cf : CharFieldManager.inst().getCharFieldList()) {
      // NON-EMPTY
      if (character.hasValue(cf)) {
        sb.append(makeValue(character.getValue(cf)));
      }
      // if EMPTY term then do a 2nd delimiter since it takes up 2 spots
      else if (isTerm(cf)) {
        sb.append(delimiter);
      } 
      // delimiter for empty & non-empty
      sb.append(delimiter);
    }
    
    return sb.toString();
  }
  
  private boolean isTerm(CharField cf) {
    return cf.isTerm(); //hasOntologies();
  }

  // this may be more general than just this class
  class BadCharException extends Exception {
    BadCharException(String m) { super(m); }
  }

  private String makeValue(CharFieldValue v) {
    if (v.isTerm()) return makeTermValue(v);
    return v.getValueAsString();
  }

  private String makeTermValue(CharFieldValue termVal) {
    //for terms need to include a col for both id and name
    //System.out.println("DelimChar term.getID: "+term.getID());
    // if termVal is a list, id & name will be quoted & comma separated (by CFV)
    return termVal.getID() + delimiter + termVal.getName();
  }

  // READ - split read & write into 2 classes?

  /** make DelimFieldParsers from headerLine, throw exception 
      if line fails to be a header */
  void setHeader(String headerLine) throws DelimitedEx {
    String[] colHeaders = splitLine(headerLine);
    if (colHeaders.length == 0) throw new DelimitedEx(headerLine);
    int i=0;
    boolean done = false;
    DelimFieldParser p = null;
    while (i<colHeaders.length && !done) {
      try {
        p = DelimFieldParser.makeNextParser(colHeaders,i);
        fieldParsers.add(p);
        // may parse 1 field, may parse 2, may also skip unfound fields
        i = p.getLastParseField() + 1;
      } //?? thrown if rest of fields not configged
      catch (DelimitedEx e) { done = true; } 
    }
    if (fieldParsers.isEmpty())
      throw new DelimitedEx(headerLine);

    // Warn user if there were columns in the input that are not defined in the current configuration.
    String unknownFieldMessages = "";
    if (p != null)
      unknownFieldMessages = p.getUnknownFieldMessages();
    if (unknownFieldMessages.length() > 1) {
      String conf = "";
      try {
        conf = Config.inst().getMyPhenoteConfigString();
      } catch (IOException e) {}
      // Pop up warning message if there are unknown fields
      JOptionPane.showMessageDialog(null,
                                    "The following column(s) were found in the input datafile but are not defined in the current configuration (" + conf +
                                    "):" + unknownFieldMessages +
                                    "\n\nAny values that were in the unrecognized columns will be lost when you save your work.\n" +
                                    "Before editing this data file, you should choose a configuration that includes column definitions for all columns.\n",
                                    "Unrecognized column(s) in input",JOptionPane.ERROR_MESSAGE);
    }
  }

  private String[] splitLine(String line) {
    //parse based on tab...will be delimiter in future
    final String delimiter = "\t";
    final Pattern p = Pattern.compile(delimiter);
    final String trimmedLine =
      (line.endsWith(delimiter)) ? (line.substring(0, line.length() -1)) : line;
    return p.split(trimmedLine, -1);
  }

  void parseLine(String line) throws DelimitedEx {
    character = CharacterIFactory.makeChar();
    String[] items = splitLine(line);
    if (items.length==0) throw new DelimitedEx(line); // BlankEx?
    for (DelimFieldParser p : fieldParsers) {
      // throws DelEx if array not long enough for Parser (missing tabs)
      // should ex be caught and go on?
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

  CharacterI getCharacter() { return character; }

  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}

