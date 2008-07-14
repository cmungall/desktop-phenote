package phenote.matrix.view;

import org.bbop.framework.AbstractGUIComponent;

//import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.LoadSaveListener;
import phenote.dataadapter.LoadSaveManager;
import phenote.datamodel.CharFieldException;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;
import phenote.edit.EditManager;
import phenote.matrix.model.MatrixController;
import phenote.matrix.model.MatrixRow;
import java.awt.BorderLayout;
import java.io.File;
import java.util.Comparator;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
//import javax.swing.JScrollPane;
//import ca.odell.glazedlists.EventList;
//import ca.odell.glazedlists.event.ListEvent;
//import ca.odell.glazedlists.event.ListEventListener;
//import ca.odell.glazedlists.gui.AdvancedTableFormat;
//import ca.odell.glazedlists.gui.WritableTableFormat;
//import ca.odell.glazedlists.swing.EventTableModel;

public class MatrixComponent extends AbstractGUIComponent {
	
	private final MatrixController controller;
	private MatrixTableModel matrixTableModel;
	private JTable matrixTable;
	
	public MatrixComponent(String id, MatrixController controller) {
		super(id);
		this.controller = controller;
	}

	private MatrixController getController() {
		return controller;
	}
	
	/**
	 * This method should set up the event listeners and call the proper methods to create the interface
	 * I don't think it is working correctly!
	 */
	public void init() {
		super.init();
	  EditManager.inst().addCharChangeListener(new MatrixListener());
		LoadSaveManager.inst().addListener(new FileListener());
		this.initializeInterface();
	}
	
	/**
	 * Creates the table model for the matrix and adds the table to the interface component
	 */
	public void initializeInterface() {
		this.setLayout(new BorderLayout());
	   try {
      controller.buildMatrixCharList();
    } catch (CharFieldException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    // Just use a TableModel (built into Swing) instead?
		matrixTableModel = new MatrixTableModel();
		matrixTable = new JTable(matrixTableModel);
		System.out.println("The table has this many rows: " + matrixTable.getRowCount());
		// ************** When testing with the Group Exercise data, the above println tells me
		// the matrix has six rows, but nothing shows up in the GUI!! ************************
		matrixTable.putClientProperty("Quaqua.Table.style", "striped");
		this.add(matrixTable, BorderLayout.CENTER);
	}
		
	//--------------------------------------------------------------------------------------------------------------------------------------
	// rewrite this to subclass AbstractTableModel instead
	private class MatrixTableModel extends AbstractTableModel {

    public int getColumnCount() {
      // TODO Auto-generated method stub
      return 0;
    }

    public int getRowCount() {
      // TODO Auto-generated method stub
      return 0;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      // TODO Auto-generated method stub
      return null;
    }
	}
	
	//--------------------------------------------------------------------------------------------------------------------------------------
	private class MatrixListener implements CharChangeListener {
    public void charChanged(CharChangeEvent e) {
      System.out.println("Matrix Listener fired");
      try {
        controller.buildMatrixCharList();
      } catch (CharFieldException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }
	}
	
	//--------------------------------------------------------------------------------------------------------------------------------------
	private class FileListener implements LoadSaveListener {
	    public void fileLoaded(File f) {
	      System.out.println("File Listener, file loaded fired");
	      try {
          controller.buildMatrixCharList();
        } catch (CharFieldException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
	    }
	    public void fileSaved(File f) {
        System.out.println("File Listener, file saved fired");
	      try {
          controller.buildMatrixCharList();
        } catch (CharFieldException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
	    }
	  }
}
