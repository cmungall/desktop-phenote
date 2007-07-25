package phenote.dataadapter;

import phenote.edit.CharChangeListener;

public interface GroupAdapterI {

  boolean hasCharChangeListener();
  CharChangeListener getCharChangeListener();
  // boolean makesFieldValues()? makesRows? makesFieldNames?
  void setDestinationField(String field); // CharField? Ex?
}
