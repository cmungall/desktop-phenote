package phenote.matrix.view;

import javax.swing.table.DefaultTableCellRenderer;
import phenote.matrix.model.MatrixCell;
import phenote.matrix.model.PhenotypeMatrixCell;

public class MatrixTableRenderer extends DefaultTableCellRenderer {

	public MatrixTableRenderer () {
		super();
	}
	
	/**
	 *  Assuming the entry to put into the matrix element is of type OBOClass,
	 *  the name representation (a String) is retrieved and used
	 */
	public void setValue (Object value) {
	  String strValue = "";
	  if (value!= null && value instanceof MatrixCell) {
		  PhenotypeMatrixCell cellValue = (PhenotypeMatrixCell) value;
			strValue = cellValue.getQuality().getName();
		}
	  if (! (value instanceof MatrixCell)) {
	    strValue = "Not a Matrix Cell";
	    //strValue = value.getClass().getName();
	  }
	  if (value == null) {
	    strValue = "I'm null!";
	  }
	  if (value instanceof String) {
	    strValue = (String) value;
	  }
		super.setText(strValue);
	}
}
