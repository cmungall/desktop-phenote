package phenote.dataadapter.delimited;

import org.apache.log4j.Logger;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;

/** A class that parses a single field out of a tab delimited line.
    Gets its info from header line. static method makeNextParser 
    is used to iterate
    This is for reading/parsing - not writing */

class DelimFieldParser {

  private static Logger LOG = Logger.getLogger(DelimFieldParser.class);

  private CharField charField;
  private int columnIndex; // 0 based
  private static String unknownFieldMessages = "";

  /** Given array of column headers and index col to start looking, find next valid
      field -- a field that is in datamodel/configged -- and return Parser for it
      throws DelimitedEx if fails to find a char field for all headers at index col
      and greater*/
  static DelimFieldParser makeNextParser(String[] colHeaders,int col) 
    throws DelimitedEx {
    for (int i=col; i<colHeaders.length; i++) {
      String head = colHeaders[i];
      CharField cf=null;
      try {
        // should i strip "ID" now, or see if there is afield with ID at end?
        // which of course would be awkward but not inconceivable
        cf = CharFieldManager.inst().getCharFieldForName(head);
      }
      catch (CharFieldException e) { // column not a char field
        // so term id headers have ID at end so try stripping off
        if (head.endsWith("ID")) {
          head = head.substring(0,head.length()-3);
          try { cf = CharFieldManager.inst().getCharFieldForName(head); }
          catch (CharFieldException x) {
            LOG.error("CharFieldException:"+x); // DEL
        	  cf = null; 
          } // already is really
        }
      }
      if (cf == null) {
        addUnknownFieldMessage(head);
        LOG.error("Header column "+head+" not in current configuration--ignoring column. (Any values that were in this column will be lost if you save the data to a file!)");
      }
      else 
        return new DelimFieldParser(cf,i);
    }
    // no parser found, end of line hit - throw ex?
    throw new DelimitedEx("Header Fields at end not found"); //??
    //return null; 
  }

  private DelimFieldParser(CharField c, int i) {
    charField = c;
    columnIndex = i;
  }

  int getLastParseField() {
    if (isTerm()) return columnIndex + 1; // id col & name col
    else return columnIndex; // just 1 col
  }

  private boolean isTerm() { return charField.isTerm(); }

  void parseField(String items[],CharacterI chr) throws DelimitedEx {
    if (columnIndex >= items.length)
      throw new DelimitedEx("No more fields in line to parse "+items+" "+chr);
    // if term, first id then name
    String value = items[columnIndex];
    // If value is empty, can't we just return now?  Or is it important to create a record with charField="" for this field?
    try {
      String danglerName = null;
      // if it is a term then grab next item for dangler name, but 
      if (charField.isTerm()) {
        //look out for mangled data that lacks a term name (user mutzed)
        if (columnIndex+1 >= items.length)
          throw new DelimitedEx("No more fields in line to parse "+items+" "+chr);
        danglerName = items[columnIndex+1];
      }
      chr.setValue(charField,value,danglerName);
      // if term, and id not found, dangler -> set name of dangler
      //if (cfv.isDangler()) // throw error if not term?
      //  cfv.setName(items[columnIndex+1]); // or should CF do this?
    }
    // no longer thrown for term not found - dangler created instead!
    // this is thrown for faulty dates
    catch (CharFieldException e) {
      LOG.error(e.getMessage());
    } 
  }

  private static void addUnknownFieldMessage(String m) {
    unknownFieldMessages += "\n" + m;
  }

  public static String getUnknownFieldMessages() {
    return unknownFieldMessages;
  }
}
