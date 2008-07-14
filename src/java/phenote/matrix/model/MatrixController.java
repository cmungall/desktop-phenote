package phenote.matrix.model;

import phenote.datamodel.CharFieldException;
import ca.odell.glazedlists.BasicEventList;

public class MatrixController {
	MatrixBuilder mb;
	
	/**
	 *  A new MatrixController creates a new MatrixBuilder with the specified grouping field string.
	 *  This should be refactored to be more general (use a String parameter of something instead of 
	 *  hard-coding Taxa).
	 */
	public MatrixController () {
		mb = new MatrixBuilder("Valid Taxon");
	}
	
	/**
	 * A method to access the matrix associated with this controller's MatrixBuilder
	 * 
	 * @return The matrix from the associated MatrixBuilder
	 */
	public BasicEventList<MatrixRow> getMatrix() {
		return mb.getMatrix();
	}
	
	public void buildMatrixCharList() throws CharFieldException {
	  mb.buildSortedList();
	  mb.buildMatrix();
	}
	
	public int getMatrixSize() {
	  return mb.getNumRows();
	}
}
