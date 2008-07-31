package phenote.matrix.view;

import org.bbop.framework.AbstractGUIComponent;

import phenote.dataadapter.LoadSaveListener;
import phenote.dataadapter.LoadSaveManager;
import phenote.datamodel.CharFieldException;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;
import phenote.edit.EditManager;
import phenote.matrix.model.MatrixController;
import java.awt.BorderLayout;
import java.io.File;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

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
      controller.buildMatrix();
    } catch (CharFieldException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
		matrixTableModel = new MatrixTableModel();
		matrixTable = new JTable(matrixTableModel);
		System.out.println("The table has this many rows: " + matrixTable.getRowCount());
		System.out.println("The table has this many columns: " + matrixTable.getColumnCount());
		matrixTable.putClientProperty("Quaqua.Table.style", "striped");
		this.add(matrixTable, BorderLayout.CENTER);
	}
		
	//--------------------------------------------------------------------------------------------------------------------------------------
	private class MatrixTableModel extends AbstractTableModel {

    public int getColumnCount() {
      return controller.getNumColumns();
    }

    public int getRowCount() {
      return controller.getNumRows();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      return controller.getValueAt(rowIndex, columnIndex);
    }
	}
	
	//--------------------------------------------------------------------------------------------------------------------------------------
	private class MatrixListener implements CharChangeListener {
    public void charChanged(CharChangeEvent e) {
      System.out.println("Matrix Listener fired");
      try {
        controller.buildMatrix();
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
          controller.buildMatrix();
        } catch (CharFieldException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
	    }
	  
	  public void fileSaved(File f) {
        System.out.println("File Listener, file saved fired");
	      try {
          controller.buildMatrix();
        } catch (CharFieldException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
	    }
	  }
}
