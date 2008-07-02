package phenote.datamodel;

import ca.odell.glazedlists.EventList;

/** holds a list of Characters - rename phenotype? i dont think so - can have
 multiple genotypes in it - transfreable allows it to be plopped on the
 clipboard 
hmmm isnt this just a List<CharacterI>? */
public interface CharacterListI {

  public CharacterI get(int i);
  public void add(CharacterI c);
  public void add(int order,CharacterI c);
  public void remove(int i);
  public void remove(CharacterI c);
  public void clear();
  public int size();
  public boolean isEmpty();
  public int indexOf(CharacterI c);
  public EventList<CharacterI> getList();
   /** return all characters that arent blanks. this is handy as blanks are really
      just an artifact of the gui. a character that has auto-generated fields filled
      in is still considered blank (like date_created). uses hasNoContent() */
  public EventList<CharacterI> getNonBlankList();
  // should CharacterListI just be collapsed into List<CharacterI>?
  public CharacterListI getNonBlankCharList();
  public boolean equals(CharacterListI cl);
}
