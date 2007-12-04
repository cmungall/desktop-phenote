package phenote.dataadapter.delimited;

import org.apache.log4j.Logger;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharacterI;

/** A class that parses a single field out of a tab delimited line.
    Gets its info from header line. static method makeNextParser 
    is used to iterate
    This is for reading/parsing - not writing */

class DelimFieldParser {

  private static Logger LOG = Logger.getLogger(DelimFieldParser.class);

  private CharField charField;
  private int columnIndex; // 0 based

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
          catch (CharFieldException x) { cf = null; } // already is really
        }
      }
      if (cf == null)
        LOG.error("Header column "+head+" not configged, skipping");
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

  void parseField(String items[],CharacterI chr) {
    // if term, first id then name
    String value = items[columnIndex];
    try {
      CharFieldValue cfv = chr.setValue(charField,value);
      // if term, and id not found, dangler -> set name of dangler
      if (cfv.isDangler()) // throw error if not term?
        cfv.setName(items[columnIndex+1]);
    }
    // no longer thrown for term not found - dangler created instead!
    // this is thrown for faulty dates
    catch (CharFieldException e) {
      LOG.error(e.getMessage());
    } 
  }
}
