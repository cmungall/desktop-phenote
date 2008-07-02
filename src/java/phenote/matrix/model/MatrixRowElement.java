package phenote.matrix.model;

public class MatrixRowElement {
	
	// Could/Should this class use OBOClass objects instead of Strings
	private String elementFieldName;
	private String elementValue;
	
	public MatrixRowElement (String cnf, String cnv) {
		setColNameField(cnf);
		setColNameValue(cnv);
	}

	/**
	 * Set the field name for this element
	 * 
	 * @param colNameField The name of the field to which this element refers
	 */
	public void setColNameField(String colNameField) {
		this.elementFieldName = colNameField;
	}

	/**
	 * Retrieve the field name for this element
	 * 
	 * @return The name of the field to which this element refers
	 */
	public String getColNameField() {
		return elementFieldName;
	}

	/**
	 * Set the value for this element
	 * 
	 * @param colNameValue The value for this element
	 */
	public void setColNameValue(String colNameValue) {
		this.elementValue = colNameValue;
	}

	/**
	 * Retrieve the value for this element
	 * 
	 * @return The value for this element
	 */
	public String getColNameValue() {
		return elementValue;
	}
}
