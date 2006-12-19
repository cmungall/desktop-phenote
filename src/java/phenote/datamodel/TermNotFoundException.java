package phenote.datamodel;

/** unclear to me if we really need both ontology ex and termo not found ex */

public class TermNotFoundException extends Exception {
  TermNotFoundException(String m) { super(m); }
}
