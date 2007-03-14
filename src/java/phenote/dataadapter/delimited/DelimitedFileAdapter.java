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

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.datamodel.CharacterList;
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.DataAdapterI;
import phenote.dataadapter.phenoxml.PhenoXmlAdapter;

/** Originated with PhenoSyntaxFileAdapter and modified.  Writes the basic text to 
 *  a file, with 'tab' delimiters.  First line is column headings, only printed 
 *  once.
 *  e.g. E=head Q=large */

public class DelimitedFileAdapter implements DataAdapterI {

  private File previousFile;
  private File file;
  private static String[] extensions = {"tab"};

  /** command line setting of file */
  public void setAdapterValue(String filename) {
    file = new File(filename);
  }

  /** this should return CharacterList and caller should load CharListMan
      or CLM makes the call itself? */
  public void load() {

    if (file == null)
      file = getFileFromUserForOpen(previousFile);
    if (file == null) return;
    previousFile = file;
    try {
      CharacterListI charList = new CharacterList();
      LineNumberReader lnr = new LineNumberReader(new FileReader(file));
      DelimitedChar synChar = new DelimitedChar();
      for (String line=lnr.readLine(); line != null; line = lnr.readLine()) {
        try {
          synChar.parseLine(line);
        CharacterI ch = synChar.getCharacter();
        charList.add(ch);
        } catch (DelimitedChar.SyntaxParseException e) {
          System.out.println(e.getMessage()); // jut "" for whitespace line
        }
      }
      CharacterListManager.inst().setCharacterList(this,charList);
      lnr.close();
    }
    catch (IOException e) {
      System.out.println("Delimited read failure "+e);
    }
    file = null; // null it for next load/commit
  }
  
  public CharacterListI load(File f) {
    // this method temporarily duplicates code from load() - soon load() will be removed
    CharacterListI charList = new CharacterList();
    try {
      LineNumberReader lnr = new LineNumberReader(new FileReader(f));
      DelimitedChar synChar = new DelimitedChar();
      for (String line=lnr.readLine(); line != null; line = lnr.readLine()) {
        try {
          synChar.parseLine(line);
        CharacterI ch = synChar.getCharacter();
        charList.add(ch);
        } catch (DelimitedChar.SyntaxParseException e) {
          System.out.println(e.getMessage()); // jut "" for whitespace line
        }
      }
      lnr.close();
    }
    catch (IOException e) {
      System.out.println("Tab-delimited read failure "+e);
    }
    return charList;
  }

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
  
  public List<String> getExtensions() {
    return Arrays.asList(extensions);
  }
  
  public String getDescription() {
    return "Tab Delimited [.tab]";
  }

}
