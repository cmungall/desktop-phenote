package phenote.matrix.model;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.SortedList;
import org.obo.datamodel.OBOClass;
import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.OboUtil;

public class MatrixBuilder {

	String groupingField;
	CharacterListManager clm;
	CharacterComparator ccompare;
	EventList<CharacterI> list;
	SortedList<CharacterI> sorted;
	BasicEventList<MatrixRow> matrix;
	
	/**
	 * Initialize the MatrixBuilder object
	 * 
	 * @param gf the grouping field to use for this matrix (the field that will determine rows in the matrix)
	 */
	public MatrixBuilder (String gf) {
		// Is String best here? I was thinking of "taxa" as a parameter, but should I use OBOClass type instead, as in the NEXUSAdapter?
		groupingField = gf;
		clm = CharacterListManager.main();
		System.out.println("Got the character manager");
		ccompare = new CharacterComparator (groupingField);
		list = (EventList<CharacterI>) clm.getCharList();
		System.out.println("Got the character list");
		sorted = new SortedList<CharacterI>(list, ccompare);
		System.out.println("Created the sorted list");
		matrix = new BasicEventList<MatrixRow>();
	}
	
	/**
	 * Iterates through the sorted character list and creates new rows in the matrix/adds a column to the current row as necessary
	 * 
	 * @throws CharFieldException
	 */
	public void buildMatrix () throws CharFieldException {
		MatrixRow currRow = null;
		String currGroup, prevGroup = "";
		for ( CharacterI ch : sorted) {
			currGroup = ch.getValue(ch.getCharFieldForName(groupingField)).getName();
			if (!currGroup.equals(prevGroup)) {
				currRow = new MatrixRow (groupingField, currGroup);
				addRowToMatrix(currRow);
				System.out.println("Added a row");
				prevGroup = currGroup;
				addColumnToRow (currRow, ch);
				System.out.println("Added a column");

			}
			else {
				addColumnToRow (currRow, ch);		
				System.out.println("Added a column");

			}
		}			
	}
	
	/**
	 * Adds a newly-created row to the current matrix
	 * 
	 * @param row the row object to be added to the current matrix
	 */
	public void addRowToMatrix (MatrixRow row) {
		matrix.add(row);
	}
	
	/**
	 * Adds a new column to the specified matrix row to represent the specified character
	 * 
	 * @param row the current MatrixRow that needs a new column
	 * @param character the character that needs to be added to the row
	 * @throws CharFieldException
	 */
	public void addColumnToRow (MatrixRow row, CharacterI character) throws CharFieldException {
		OBOClass entityTerm, valueTerm, attributeTerm;
		entityTerm = character.getTerm("Entity");
		valueTerm = character.getTerm("Quality");
		attributeTerm = OboUtil.getAttributeForValue(valueTerm);
		row.addColumn(entityTerm, attributeTerm, valueTerm);
	}
	
	/**
	 * Return the matrix.
	 * 
	 * @return the matrix being built
	 */
	public BasicEventList<MatrixRow> getMatrix() {
		return this.matrix;
	}
}
