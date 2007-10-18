package phenote.dataadapter;

import phenote.edit.CharChangeListener;
/** group adapters
    originally were groups of fields this is not, but one thing group adapters do is make
    terms/ontology on the fly - and thats what this does - but it does make ya wanna
    change the name of GroupAdap if it applies to things that arent even groups of terms
    maybe TermMaker? 
    theres really 2 things going on - grouping of fields and making of terms - and
    character template which is a grouping of fields but it makes truncated char lists
    not terms - but char temp is not a group adapteri but maybe should be? 
    - hmmmm - i think grouping of fields needs to be separated from making of terms
    and making of characters - i think group adapter should be renamed TermMaker
    as thats what it is! 
    and config should change with it - maybe call "plugin" in config instead of group?

    Also im wondering if char change listener and char list change listener
    should just be replaced with a TermListChange event? to make it more
    destination oriented and less about the source? a listener just cares if
    theres new terms - a listener doesnt care if that happened via char list or char 
    change or who knows what 
    also setDestinationField needs to take a list

    This should be replaced by OntologyMakerI - phase out!
    thiscould be CharOntMakerI but i dont think thats necasary
*/

public interface GroupAdapterI extends OntologyMakerI {

  /** If a group reacts to char changes(edits) then a char change listener should be 
      added, for instance a user edits group fields and dest field is automatically 
      repopulated, otherwise no-op */
  boolean hasCharChangeListener();
  CharChangeListener getCharChangeListener();
  
  /** similar to char change, for doing something on loading data into group, like
      repopulating dest field, no-op if dont need this */
  boolean hasCharListChangeListener();
  CharListChangeListener getCharListChangeListener();

}
