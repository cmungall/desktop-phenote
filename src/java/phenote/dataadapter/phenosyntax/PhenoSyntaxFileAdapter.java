package phenote.dataadapter.phenosyntax;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.dataadapter.DataAdapterI;
import phenote.dataadapter.phenoxml.PhenoXmlAdapter;

/** Writes pheno syntax characters to a file.
    See http://www.fruitfly.org/~cjm/obd/pheno-syntax.html for a full description
    of pheno syntax. Its basically a human readable version of pheno xml
    e.g. E=head Q=large */

public class PhenoSyntaxFileAdapter implements DataAdapterI {

  public void load() {}

  public void commit(CharacterListI charList) {

    File file = PhenoXmlAdapter.getFile(); // perhaps a util class
    if (file == null)
      return;

    PrintWriter pw;
    try {
      pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
    } catch (IOException e) {
      System.out.println("Failed to open file "+file);
      return;
    }
    
    for (CharacterI ch : charList.getList()) {
      String c = new PhenoSyntaxChar(ch).getPhenoSyntaxString();
        System.out.println(c);
      pw.println(c);
    }
    pw.close();
  }

}
