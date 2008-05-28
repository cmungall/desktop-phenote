package phenote.datamodel;

import org.obo.postcomp.TokenMgrError;

/** unclear to me if we really need both ontology ex and termo not found ex */

public class TermNotFoundException extends Exception {
  
  public TermNotFoundException(String m) { super(m); }
  
  public TermNotFoundException(TokenMgrError e) { super(e); }
  
}
