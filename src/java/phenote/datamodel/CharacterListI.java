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
  public boolean equals(CharacterListI cl);
}
