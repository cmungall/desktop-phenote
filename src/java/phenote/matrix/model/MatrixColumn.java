package phenote.matrix.model;

import phenote.datamodel.CharacterI;

public interface MatrixColumn {
  
  /** 
   * @param aCharacter a Character from the main data list
   * @return true if the character has a value for this column
   */
  public boolean isValue (CharacterI aCharacter);
  
  /**
   * @param aCharacter - given a Character from the main data list
   * @return the Character's value for this particular column
   */
  public Object getValue (CharacterI aCharacter);
}