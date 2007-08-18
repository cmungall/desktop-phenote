package phenote.dataadapter;

public class DataAdapterEx extends Exception {
  public DataAdapterEx(Exception e) { super(e); }
  public DataAdapterEx(String m) { super(m); }
}
