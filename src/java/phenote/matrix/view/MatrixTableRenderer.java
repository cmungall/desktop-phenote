package phenote.matrix.view;

import javax.swing.table.DefaultTableCellRenderer;
import phenote.matrix.model.MatrixCell;
import phenote.matrix.model.PhenotypeMatrixCell;

public class MatrixTableRenderer extends DefaultTableCellRenderer {

	public MatrixTableRenderer () {
		super();
	}
	
	/**
	 * If the Object is a MatrixCell, the relevant information is extracted from the object to form the cell's contents
	 * I only tested examples of counts and additional entities; this method should be expanded to include unit,
	 * measurement, etc. values, but I did not append them to the cell value because I was not sure which conditions to test.
	 * 
	 * If a String object is given as the parameter, the contents of the string are used as the contents of the cell.
	 * 
	 * @param value the Object used to set the value inside the table cell
	 */
	public void setValue (Object value) {
	  String strValue = "";
	  if (value!= null && value instanceof MatrixCell) {
		  PhenotypeMatrixCell cellValue = (PhenotypeMatrixCell) value;
			strValue = cellValue.getQuality().getName();
			try{
			  if (strValue.equalsIgnoreCase("count")) { 
			      strValue = strValue + " = " + cellValue.getCount();
			  } 
			  if (cellValue.getEntity2() != null) {
			    strValue = strValue + " " + cellValue.getEntity2();
			  }
			} catch (NullPointerException e) {
          System.out.println("ERROR - String was null.");
      }
	  }
	  else if (value instanceof String) {
	    strValue = (String) value;
	  }
		super.setText(strValue);
	}
}
