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
