package phenote.matrix.model;

import phenote.datamodel.CharacterI;

public interface MatrixRow {
  
  /** 
   * @param aCharacter - given a Character from the main data list
   * @return true if the character has a value for this row
   */
  public boolean isValue (CharacterI aCharacter);
  
  /**
   * @param aCharacter - given a Character from the main data list
   * @return the Character's value for this particular row
   */
  public Object getValue (CharacterI aCharacter);
}