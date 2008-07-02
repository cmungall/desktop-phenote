package phenote.matrix.model;

import java.util.Comparator;

public class MatrixRowComparator implements Comparator<MatrixRow> {
	
	/**
	 * Use standard String comparison logic to compare two rows in the Matrix.
	 * First compare the rows' field names, and if they match, compare their values.
	 * 
	 * @param r1 the first row to be compared
	 * @param r2 the second row to be compared
	 * @return the result of the String comparison between the two rows
	 * @see String.compareTo(String anotherString)
	 */
	public int compare(MatrixRow r1, MatrixRow r2) {
		int result = r1.getRowFieldName().compareTo(r2.getRowFieldName());	
		if (result == 0) {
			result = r1.getRowValue().compareTo(r2.getRowValue());
		}
		return result;
	}
}
