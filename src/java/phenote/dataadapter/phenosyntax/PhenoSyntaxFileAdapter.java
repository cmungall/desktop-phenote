package phenote.dataadapter.phenosyntax;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

import phenote.dataadapter.AbstractFileAdapter;
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.phenoxml.PhenoXmlAdapter;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterList;
import phenote.datamodel.CharacterListI;

/** Writes pheno syntax characters to a file.
    See http://www.fruitfly.org/~cjm/obd/pheno-syntax.html for a full description
    of pheno syntax. Its basically a human readable version of pheno xml
    e.g. E=head Q=large
    This should be renamed TagValueFileAdapter as thats what it really is
    Also DataAdapterI should be renamed FileDataAdapterI to distinguish from 
    QueryableDataAdapterI
*/

public class PhenoSyntaxFileAdapter extends AbstractFileAdapter {

  private File previousFile;
  /** psx phenosyntax, syn syntax, tv tag-value, ptv phenotagvalue */
  private static String[] extensions = {"psx", "syn","tv","ptv","tagval"};
  private static String description = "PhenoSyntax [.psx, .syn]";

  public PhenoSyntaxFileAdapter() { super(extensions,description); }


  /** this should return CharacterList and caller should load CharListMan
      or CLM makes the call itself? */
  public void load() {
    if (file == null) {
      log().error("No file was specified");
      return;
    }
    previousFile = file;
    try {
      CharacterListI charList = new CharacterList();
      LineNumberReader lnr = new LineNumberReader(new FileReader(file));
      PhenoSyntaxChar synChar = new PhenoSyntaxChar();
      for (String line=lnr.readLine(); line != null; line = lnr.readLine()) {
        try {
          synChar.parseLine(line);
        CharacterI ch = synChar.getCharacter();
        charList.add(ch);
        } catch (PhenoSyntaxChar.SyntaxParseException e) {
          log().error("Error parsing phenosyntax", e);
        }
      }
      CharacterListManager.inst().setCharacterList(this,charList);
      lnr.close();
    }
    catch (IOException e) {
      log().error("PhenoSyntax read failure ", e);
    }
    file = null; // null it for next load/commit
  }
  
  public CharacterListI load(File f) {
    // this method temporarily duplicates code from load() - soon load() will be removed
    CharacterListI charList = new CharacterList();
    try {
      LineNumberReader lnr = new LineNumberReader(new FileReader(f));
      PhenoSyntaxChar synChar = new PhenoSyntaxChar();
      for (String line=lnr.readLine(); line != null; line = lnr.readLine()) {
        try {
          synChar.parseLine(line);
        CharacterI ch = synChar.getCharacter();
        charList.add(ch);
        } catch (PhenoSyntaxChar.SyntaxParseException e) {
          log().error("Error parsing phenosyntax", e);
        }
      }
      lnr.close();
    }
    catch (IOException e) {
      log().error("PhenoSyntax read failure ", e);
    }
    return charList;
  }

  public void commit(CharacterListI charList) {
    if (file == null) {
      log().error("No file was specified");
      return;
    }
    previousFile = file;

    PrintWriter pw;
    try {
      pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
    } catch (IOException e) {
      log().error("Failed to open file ", e);
      return;
    }

    log().info("Writing pheno syntax to file " + file);

    for (CharacterI ch : charList.getList()) {
      String c = new PhenoSyntaxChar(ch).getPhenoSyntaxString();
      log().debug(c);
      pw.println(c);
    }
    pw.close();
    file = null;
  }
  
  public void commit(CharacterListI charList, File f) {
    file = f;
    commit(charList);
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
}
