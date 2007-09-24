package phenote.gui.field;

import java.util.List;

public interface SearchListener {
  // should there be an event - not needed at this pt - maybe later
  // could add a CompItem super class of term & rel?
  public void newResults(List results);
}
