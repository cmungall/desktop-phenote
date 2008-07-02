package phenote.matrix.model;

import ca.odell.glazedlists.EventList;
import org.obo.datamodel.OBOClass;

public class MatrixRow {
	private String rowFieldName;
	private String rowValue;
	private EventList<MatrixRowElement> columns;
	
	public MatrixRow (String rfn, String rv) {
		rowFieldName = rfn;
		rowValue = rv;
	}
		
	/**
	 * A new column is added to a matrix row by creating a new name (heading) for the column using 
	 * the entity and attribute term names, and the value is retrieved from the value term name.
	 * 
	 * @param entityTerm An OBOClass representing the entity
	 * @param attributeTerm An OBOClass representing the attribute
	 * @param valueTerm An OBOClass representing the value
	 */
	public void addColumn(OBOClass entityTerm, OBOClass attributeTerm, OBOClass valueTerm) {
		String name, value;
		name = entityTerm.getName() + " " + attributeTerm.getName();
		value = valueTerm.getName();
		columns.add(new MatrixRowElement(name, value));
	}

	/**
	 * Set the field name for this row.
	 * 
	 * @param rfn The new field name for this row in the matrix 
	 */
	public void setRowFieldName(String rfn) {
		this.rowFieldName = rfn;
	}

	/**
	 * Retrieve the row's field name.
	 * 
	 * @return The field name of the current row
	 */
	public String getRowFieldName() {
		return rowFieldName;
	}

	/**
	 * Set the value for the current row
	 * 
	 * @param rv The new value for the current row
	 */
	public void setRowValue(String rv) {
		this.rowValue = rv;
	}

	/**
	 * Retrieve the row's value
	 * 
	 * @return The value of the current row
	 */
	public String getRowValue() {
		return rowValue;
	}
}
