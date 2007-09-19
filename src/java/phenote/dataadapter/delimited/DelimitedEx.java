package phenote.dataadapter.delimited;

class DelimitedEx  extends Exception {
  private String line;
  private int lineNumber = -1;
  
  DelimitedEx(String line) {
    this.line = line;
  }
  DelimitedEx(String line, int lineNum) {
    this(line);
    lineNumber = lineNum;
  }
  public String getMessage() {
    if (line.trim().equals(""))
      return ""; // just whitespace - who cares
    StringBuffer s = new StringBuffer();
    s.append("TabDelim Parse Error: ");
    if (lineNumber != -1) s.append("Line number "+lineNumber);
    s.append("Line: "+line+" -- ignoring");
    return s.toString();
  }
}
