package phenote.dataadapter.phenosyntax;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.LineNumberReader;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.datamodel.CharacterList;
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.DataAdapterI;
import phenote.dataadapter.phenoxml.PhenoXmlAdapter;

/** Writes pheno syntax characters to a file.
    See http://www.fruitfly.org/~cjm/obd/pheno-syntax.html for a full description
    of pheno syntax. Its basically a human readable version of pheno xml
    e.g. E=head Q=large */

public class PhenoSyntaxFileAdapter implements DataAdapterI {

  private File previousFile;

  /** this should return CharacterList and caller should load CharListMan
      or CLM makes the call itself? */
  public void load() {

    File file = getFileFromUser(previousFile);
    if (file == null) return;
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
          System.out.println(e.getMessage()); // jut "" for whitespace line
        }
      }
      CharacterListManager.inst().setCharacterList(this,charList);
      lnr.close();
    }
    catch (IOException e) {
      System.out.println("PhenoSyntax read failure "+e);
    }
  }

  /** returns null if user fails to pick a file */
  private File getFileFromUser(File dir) {
    return PhenoXmlAdapter.getFileFromUser(dir); // perhaps a util class
  }

  public void commit(CharacterListI charList) {
    
    File file = getFileFromUser(previousFile);
    if (file == null) return;
    previousFile = file;

    PrintWriter pw;
    try {
      pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
    } catch (IOException e) {
      System.out.println("Failed to open file "+file);
      return;
    }

    System.out.println("Writing pheno syntax to file "+file);

    for (CharacterI ch : charList.getList()) {
      try {
        String c = new PhenoSyntaxChar(ch).getPhenoSyntaxString();
        System.out.println(c);
        pw.println(c);
      }
      catch (PhenoSyntaxChar.BadCharException e) {
        System.out.println(e.getMessage()+" Not writing out character");
      }
    }
    pw.close();
  }

}
