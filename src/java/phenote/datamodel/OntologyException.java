package phenote.datamodel;

public class OntologyException extends Exception {
  //private wrappedExce ?
  public OntologyException(String m) { super(m); }
  public OntologyException(Exception e) {
    this(e.getMessage());
  }
}
