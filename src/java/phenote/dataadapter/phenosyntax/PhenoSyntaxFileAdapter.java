package phenote.dataadapter.phenosyntax;

import phenote.datamodel.CharacterListI;
import phenote.dataadapter.DataAdapterI;

/** Writes pheno syntax characters to a file.
    See http://www.fruitfly.org/~cjm/obd/pheno-syntax.html for a full description
    of pheno syntax. Its basically a human readable version of pheno xml
    e.g. E=head Q=large */

public class PhenoSyntaxFileAdapter implements DataAdapterI {

  public void load() {}

  public void commit(CharacterListI charList) {

  }

}
