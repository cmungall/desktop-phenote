package phenote.config;

public class ConfigException extends Exception {
  private Exception wrappedEx;
  public ConfigException(String m) { super(m); }
  ConfigException(Exception e) {
    this(e.getMessage());
    wrappedEx = e;
  }
  public Exception getWrappedException() { return wrappedEx; }
}
