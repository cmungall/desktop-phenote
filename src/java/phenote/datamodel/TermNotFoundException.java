package phenote.datamodel;

import org.geneontology.oboedit.postcomp.TokenMgrError;

/** unclear to me if we really need both ontology ex and termo not found ex */

public class TermNotFoundException extends Exception {
  TermNotFoundException(String m) { super(m); }
  TermNotFoundException(TokenMgrError e) { super(e); }
}
