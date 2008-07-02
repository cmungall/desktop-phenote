package phenote.matrix.view;

import javax.swing.table.DefaultTableCellRenderer;
import org.obo.datamodel.OBOClass;

public class MatrixTableRenderer extends DefaultTableCellRenderer {

	public MatrixTableRenderer () {
		super();
	}
	
	/**
	 *  Assuming the entry to put into the matrix element is of type OBOClass,
	 *  the name representation (a String) is retrieved and used
	 *  My MatrixRowElement class will have to be rewritten to use this if this is preferable
	 */
	public void setValue (Object value) {
		if (value!= null && value instanceof OBOClass) {
			OBOClass cellValue = (OBOClass) value;
			value = cellValue.getName();
		}
		super.setValue(value);
	}
}
