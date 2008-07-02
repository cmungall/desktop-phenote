package phenote.matrix.model;

import ca.odell.glazedlists.BasicEventList;

// Should I extend PhenoscapeController?

public class MatrixController {
	MatrixBuilder mb;
	
	/**
	 *  A new MatrixController creates a new MatrixBuilder with the specified grouping field string.
	 *  This should be refactored to be more general (use a String parameter of something instead of 
	 *  hard-coding Taxon).
	 */
	public MatrixController () {
		System.out.println("******************** Controller created ********************");
		mb = new MatrixBuilder("Taxon");
		System.out.println("******************** Builder created ********************");
	}
	
	/**
	 * A method to access the matrix associated with this controller's MatrixBuilder
	 * 
	 * @return The matrix from the associated MatrixBuilder
	 */
	public BasicEventList<MatrixRow> getMatrix() {
		return mb.getMatrix();
	}
}
