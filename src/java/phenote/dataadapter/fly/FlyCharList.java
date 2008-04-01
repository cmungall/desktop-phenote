package phenote.dataadapter.fly;

import java.util.ArrayList;
import java.util.List;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterList;
import phenote.datamodel.CharacterListI;

/** Has a list of FlyCharacterIs for the clipboard for proformae */
public class FlyCharList {

  private List<FlyCharacter> flyCharList = new ArrayList<FlyCharacter>();
  private CharacterListI characterList;

  FlyCharList(CharacterListI charList) {
    //characterList = charList;
    characterList = new CharacterList();
    for (CharacterI c : charList.getList()) {
      CharacterI clone = c.cloneCharacter();
      characterList.add(clone);
      flyCharList.add(new FlyCharacter(clone));
    }
  }

  public FlyCharList() {
    characterList = new CharacterList();
  }
 
  public void addFlyChar(FlyCharacter flyChar) {
    flyCharList.add(flyChar);
    characterList.add(flyChar.getCharacter()); // ?
  }

  public CharacterListI getCharacterList() {
    // return characterList?
    // for (FlyCharacter fc : flyCharList) characterList.add(fc.getCharacter) ??
    return characterList;
  }


  /** for proformae */
  public List<FlyCharacter> getFlyCharList() {
    return flyCharList;
  }

  public int size() { return flyCharList.size(); }

  /** FlyCharacterI?? */
  public FlyCharacter getFlyCharacter(int i) {
    return flyCharList.get(i);
  }

}
