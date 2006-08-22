package phenote.datamodel;

import java.util.List;

/** holds a list of Characters - rename phenotype? i dont think so - can have
 multiple genotypes in it - transfreable allows it to be plopped on the
 clipboard 
hmmm isnt this just a List<CharacterI>? */
public interface CharacterListI {

  public CharacterI get(int i);
  public void add(CharacterI c);
  public void remove(int i);
  public int size();
  public List<CharacterI> getList();
  public boolean equals(CharacterListI cl);
}
