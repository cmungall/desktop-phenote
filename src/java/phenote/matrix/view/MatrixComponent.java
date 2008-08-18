package phenote.matrix.view;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.bbop.framework.AbstractGUIComponent;

import phenote.dataadapter.LoadSaveListener;
import phenote.dataadapter.LoadSaveManager;
import phenote.datamodel.CharFieldException;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;
import phenote.edit.EditManager;
import phenote.matrix.model.MatrixController;
import phenote.matrix.model.PhenotypeMatrixRow;

public class MatrixComponent extends AbstractGUIComponent {
	
	private final MatrixController controller;
	private MatrixTableModel matrixTableModel;
	private JTable matrixTable;
	private JScrollPane matrixPane;
	
	/**
	 * Creates a new GUI component with the given ID and controller
	 * 
	 * @param id the String identifier for this component
	 * @param controller the controller object for this component
	 */
	public MatrixComponent(String id, MatrixController controller) {
		super(id);
		this.controller = controller;
	}

	/**
	 * @return the controller associated with this GUI component
	 */
	public MatrixController getController() {
		return controller;
	}
	
	/**
	 * This method sets up the event listeners and calls the proper methods to create the interface
	 */
	public void init() {
		super.init();
	  EditManager.inst().addCharChangeListener(new MatrixListener());
		LoadSaveManager.inst().addListener(new FileListener());
		this.initializeInterface();
	}
	
	/**
	 * This method creates the table model for the matrix and adds the table to the interface component
	 */
	public void initializeInterface() {
		this.setLayout(new BorderLayout());
	   try {
      controller.buildMatrix();
    } catch (CharFieldException e) {
      e.printStackTrace();
    }
    
		matrixTableModel = new MatrixTableModel();
		matrixTable = new JTable(matrixTableModel); 
		matrixTable.setDefaultRenderer(Object.class, new MatrixTableRenderer());
		matrixTable.putClientProperty("Quaqua.Table.style", "striped");
		matrixPane = new JScrollPane (matrixTable);
		this.add(matrixTable.getTableHeader(), BorderLayout.PAGE_START);
		this.add(matrixPane, BorderLayout.CENTER);
	}
		
	private class MatrixTableModel extends AbstractTableModel {

	  /**
	   * Returns one greater than the count of the ArrayList because the Valid Taxon column isn't in there
	   */
    public int getColumnCount() {
      return controller.getNumColumns() + 1;
    }

    public int getRowCount() {
      return controller.getNumRows();
    }
    
    /**
     * Since the Taxon column isn't part of the columns ArrayList, this method handles that special case individually, or 
     * otherwise subtracts one from the requested index and retrieves the value from the ArrayList
     */
    public String getColumnName(int columnIndex) {
      if (columnIndex == 0) {
        return "Taxon";
      }
      else {
        return controller.getColumnHeader(columnIndex-1);        
      }
    }

    /**
     * If the requested value is located in column 0, then it is a Valid Taxon name and it is retrieved from the ArrayList of PhenotypeMatrixRows
     * Otherwise, the columnIndex value is adjusted (by subtracting one) and the value is retrieved from the controller using the getValueAt method
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
      if (columnIndex == 0) {
        // Yes, this is tightly coupled with the PhenotypeMatrixRow class, I know...
        return ((PhenotypeMatrixRow) controller.getRows().get(rowIndex)).getTaxon().getName();
      }
      else {
        return controller.getValueAt(rowIndex, columnIndex-1);        
      }
    }
	}
	
	private class MatrixListener implements CharChangeListener {
    public void charChanged(CharChangeEvent e) {
      try {
        controller.buildMatrix();
      } catch (CharFieldException e1) {
        e1.printStackTrace();
      }
    }
	}
	
	private class FileListener implements LoadSaveListener {
	  
	  public void fileLoaded(File f) {
	      try {
          controller.buildMatrix();
        } catch (CharFieldException e) {
          e.printStackTrace();
        }
	    }
	  
	  public void fileSaved(File f) {
	      try {
          controller.buildMatrix();
        } catch (CharFieldException e) {
          e.printStackTrace();
        }
	    }
	  }
}
