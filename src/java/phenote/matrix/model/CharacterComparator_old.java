package phenote.matrix.model;

import java.util.Comparator;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharFieldException;

public class CharacterComparator_old implements Comparator<CharacterI> {

	private String comparisonField;
	
	public CharacterComparator_old (String field) {
		comparisonField = field;
	}
	
	/**
	 * A method to perform a String-style comparison on two Characters
	 * 
	 * @param c1 a Character
	 * @param c2 a Character
	 * @return the standard String comparison value of the Character's names
	 * @see String.compareTo(String anotherString)
	 */
	public int compare(CharacterI c1, CharacterI c2) {
		String c1value = null, c2value = null;
		try {
			c1value = c1.getValue(c1.getCharFieldForName(comparisonField)).getName();
			c2value = c2.getValue(c2.getCharFieldForName(comparisonField)).getName();
		} catch (CharFieldException e) {
			// TO-DO: Do something here! Probably log it...
		} finally {
			return c1value.compareTo(c2value);	
		}
	}
}
