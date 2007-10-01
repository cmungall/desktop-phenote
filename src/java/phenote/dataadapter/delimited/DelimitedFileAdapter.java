package phenote.dataadapter.delimited;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.LineNumberReader;
import java.util.List;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.datamodel.CharacterList;
import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharField;
import phenote.dataadapter.DataAdapterI;
import phenote.dataadapter.AbstractFileAdapter;
import phenote.dataadapter.phenoxml.PhenoXmlAdapter;

/** Originated with PhenoSyntaxFileAdapter and modified.  Writes the basic text to 
 *  a file, with 'tab' delimiters.  First line is column headings, only printed 
 *  once.
 *  e.g. E=head Q=large */

public class DelimitedFileAdapter extends AbstractFileAdapter {

  private File previousFile;
  private File file;
  private static String[] extensions = {"tab", "xls"};
  private static String desc = "Tab Delimited [.tab, .xls]";
  private static Logger LOG = Logger.getLogger(DelimitedFileAdapter.class);

  public DelimitedFileAdapter() {
    super(extensions,desc);
  }

  /** command line setting of file */
  public void setAdapterValue(String filename) {
    file = new File(filename);
  }

  /** this should return CharacterList and caller should load CharListMan
      or CLM makes the call itself? */
  public void load() {
//this doesn't seem to do anything
  }
  
  public CharacterListI load(File f) {
    CharacterListI charList = new CharacterList();
    try {
      LineNumberReader lnr = new LineNumberReader(new FileReader(f));
      DelimitedChar delChar = new DelimitedChar();
      System.out.println("Reading tab-delimited data from file "+f);
      //List<CharField> fields;
      try {
        // the header is being tossed and it should be used for char field mapping!
        // the standard for headers is 1 col for free texts, 2 cols for terms
        // term cols: fieldName(space)ID(tab)fieldName(space)Name
        boolean isHeader = false;
        while (!isHeader) {
          String headerLine=lnr.readLine(); //reading header line
          if (headerLine == null) // EndOfFile no header!
            throw new DelimitedEx("No valid header found");
          try { 
            delChar.setHeader(headerLine);
            isHeader = true; // no ex thrown - success
          } catch (DelimitedEx e) {
            isHeader = false;
            LOG.error("Invalid header: "+headerLine); //for debug?log?  
          }
        }
        //fields = headerToCharFields(headerLine);
      } catch (DelimitedEx x) {
        LOG.error("Tab-delimited read failure "+x);
        // failed to get header(?) - shouldnt go on i think
        // return null??? throw ex?? return empty charList?
        return charList;
      }
      catch (IOException e) {
        LOG.error("Tab-delimited read failure "+e);
        return charList;
      }
      for (String line=lnr.readLine(); line != null; line = lnr.readLine()) {
        try {
          delChar.parseLine(line);
          CharacterI ch = delChar.getCharacter();
          charList.add(ch);
          System.out.println(line);
        } catch (DelimitedEx e) { // thrown for blank line - who cares right
          // log? errorEvent? do nothing - does it even throw for blanks?
          //(e.getMessage()); // jut "" for whitespace line
        }
      }	
      lnr.close();
    }	
    catch (IOException e) {
      System.out.println("Tab-delimited read failure "+e);
    }
    return charList;
  }

//   /** Takes header line and maps it into char fields, error if char field 
//       doesnt exist -- pase --> DelChar.setHeader */
//   private List<DelimFieldParser> headerToParsers(String headerLine) {
//     Pattern p = Pattern.compile("\t");
//     //parse based on tab...will be delimiter in future
//     String[] headers = p.split(headerLine);
//     if (headers.length==0) throw new DelimitedEx(headerLine,0);
//     //List<CharField> fields = new ArrayList<CharField>();
    
//     int i=0;
//     List<DelimFieldParser> parsers = new ArrayList<DelimFieldParser>();
//     while (i<headers.length) {
//       // if a field is not found/configged then insert null to be ignored
//       // but theres 2 kinds of ignore term & free text
//       // or do int to field mapping/hash?
//       DelimFieldParser p = DelimFieldParser.makeNextParser(headers,i);
//       parsers.add(p);
//       // may parse 1 field, may parse 2, may also skip unfound fields
//       i = p.getLastParseField() + 1;
//     }
//     return parsers;
//   }

  /** returns null if user fails to pick a file */
 // private File getFileFromUser(File dir) {
 //   return PhenoXmlAdapter.getFileFromUser(dir); // perhaps a util class
 // }
  
  private File getFileFromUserForSave(File dir) {
    return PhenoXmlAdapter.getFileFromUserForSave(dir);
  }
  
  private File getFileFromUserForOpen(File dir) {
    return PhenoXmlAdapter.getFileFromUserForOpen(dir);
  }

  public void commit(CharacterListI charList) {
    if (charList.isEmpty()) {
      System.out.println("No Data to save"); // popup!
      return;
    }

    if (file == null)
      file = getFileFromUserForSave(previousFile);
    if (file == null) return;
    previousFile = file;

    PrintWriter pw;
    try {
      pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
    } catch (IOException e) {
      System.out.println("Failed to open file "+file);
      return;
    }

    System.out.println("Writing tab-delimited output to file "+file);
    //first write out header, then write out contents
    //header determined from first CharI
    try {
    	CharacterI ch = charList.get(0);
    	String c = new DelimitedChar(ch).getDelimitedHeaderString();   
        System.out.println(c);
        pw.println(c);   	
    }
    catch (DelimitedChar.BadCharException e) {
        System.out.println(e.getMessage()+" Not writing out header");
      }

    for (CharacterI ch : charList.getList()) {
      try {
        String c2 = new DelimitedChar(ch).getDelimitedString();
        System.out.println(c2);
        pw.println(c2);
      }
      catch (DelimitedChar.BadCharException e) {
        System.out.println(e.getMessage()+" Not writing out character");
      }
    }
    pw.close();
    file = null;
  }
  
  public void commit(CharacterListI charList, File f) {
    file = f;
    commit(charList);
  }
  
  // im changing my mind - should just go to log and have appender from log
  // rather than vice versa
//   private void debug(String s) {
//     phenote.error.ErrorManager.inst().debug(this,s);
//   }

//   public List<String> getExtensions() {
//     return Arrays.asList(extensions);
//   }

  /** should both be in DataAdapterI convenience and in an AbstactDataAdapter?
   */
//   public boolean hasExtension(String ext) {
//     for (String x : getExtensions())
//       if (x.equals(ext)) return true;
//     return false;
//   }
  
//   public String getDescription() {
//     return "Tab Delimited [.tab, .xls]";
//   }

}
