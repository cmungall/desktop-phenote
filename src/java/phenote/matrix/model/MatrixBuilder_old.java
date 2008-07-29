package phenote.matrix.model;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.SortedList;
import org.obo.datamodel.OBOClass;
import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.OboUtil;

public class MatrixBuilder_old {

	String groupingField;
	CharacterListManager clm;
	CharacterComparator_old ccompare;
	EventList<CharacterI> list;
	SortedList<CharacterI> sorted;
	BasicEventList<MatrixRow_old> matrix;
	
	/**
	 * Initialize the MatrixBuilder object
	 * 
	 * @param gf the grouping field to use for this matrix (the field that will determine rows in the matrix)
	 */
	public MatrixBuilder_old (String gf) {
		// Is String best here? I was thinking of "taxa" as a parameter, but should I use OBOClass type instead, as in the NEXUSAdapter?
		groupingField = gf;
		clm = CharacterListManager.main();
		ccompare = new CharacterComparator_old (groupingField);
		buildSortedList();
	}
	
	public void buildSortedList() {
	  list = (EventList<CharacterI>) clm.getCharList();
    sorted = new SortedList<CharacterI>(list, ccompare);
	}
	
	/**
	 * Iterates through the sorted character list and creates new rows in the matrix/adds a column to the current row as necessary
	 * 
	 * @throws CharFieldException
	 */
	public void buildMatrix () throws CharFieldException {
	  matrix = new BasicEventList<MatrixRow_old>();
	  MatrixRow_old currRow = null;
		String currGroup, prevGroup = "";
		for (CharacterI ch : sorted) {
			currGroup = ch.getValue(ch.getCharFieldForName(groupingField)).getName();
			if (!currGroup.equals(prevGroup)) {
				currRow = new MatrixRow_old (groupingField, currGroup);
				addRowToMatrix(currRow);
				prevGroup = currGroup;
				addColumnToRow (currRow, ch);
			}
			else {
				addColumnToRow (currRow, ch);		
			}
		}
	}
	
	/**
	 * Adds a newly-created row to the current matrix
	 * 
	 * @param row the row object to be added to the current matrix
	 */
	public void addRowToMatrix (MatrixRow_old row) {
		matrix.add(row);
	}
	
	/**
	 * Adds a new column to the specified matrix row to represent the specified character
	 * 
	 * @param row the current MatrixRow that needs a new column
	 * @param character the character that needs to be added to the row
	 * @throws CharFieldException
	 */
	public void addColumnToRow (MatrixRow_old row, CharacterI character) throws CharFieldException {
		OBOClass entityTerm, valueTerm, attributeTerm;
		entityTerm = character.getTerm("Entity");
		// We have a problem if a term is null - it causes a CharFieldException ... has no value
		valueTerm = character.getTerm("Quality");
		attributeTerm = OboUtil.getAttributeForValue(valueTerm);
		row.addColumn(entityTerm, attributeTerm, valueTerm);
	}
	
	/**
	 * Return the matrix.
	 * 
	 * @return the matrix being built
	 */
	public BasicEventList<MatrixRow_old> getMatrix() {
		return this.matrix;
	}
	
	public int getNumRows() {
	  return matrix.size();
	}
}
