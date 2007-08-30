package phenote.dataadapter;

import phenote.edit.CharChangeListener;

public interface GroupAdapterI {

  /** If a group reacts to char changes(edits) then a char change listener should be 
      added, for instance a user edits group fields and dest field is automatically 
      repopulated, otherwise no-op */
  boolean hasCharChangeListener();
  CharChangeListener getCharChangeListener();
  
  /** similar to char change, for doing something on loading data into group, like
      repopulating dest field, no-op if dont need this */
  boolean hasCharListChangeListener();
  CharListChangeListener getCharListChangeListener();

  /** the destination field that this group is populating (with obo classes), if
      a group is not populating a destination field this would be no-oped
      one could imagine other destinations like main datamodel */
  void setDestinationField(String field); // CharField? Ex?
}
