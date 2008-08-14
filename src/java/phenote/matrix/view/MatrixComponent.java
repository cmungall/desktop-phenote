package phenote.matrix.view;

import org.bbop.framework.AbstractGUIComponent;
import org.phenoscape.swing.PlaceholderRenderer;

import phenote.dataadapter.LoadSaveListener;
import phenote.dataadapter.LoadSaveManager;
import phenote.datamodel.CharFieldException;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;
import phenote.edit.EditManager;
import phenote.matrix.model.MatrixController;
import phenote.matrix.model.PhenotypeMatrixRow;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class MatrixComponent extends AbstractGUIComponent {
	
	private final MatrixController controller;
	private MatrixTableModel matrixTableModel;
	private JTable matrixTable;
	private JScrollPane matrixPane;
	
	public MatrixComponent(String id, MatrixController controller) {
		super(id);
		this.controller = controller;
	}

	private MatrixController getController() {
		return controller;
	}
	
	/**
	 * This method sets up the event listeners and call the proper methods to create the interface
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
		matrixTable.setDefaultRenderer(Object.class, new MatrixTableRenderer());
		matrixTable.putClientProperty("Quaqua.Table.style", "striped");
		matrixPane = new JScrollPane (matrixTable);
		matrixTable.setFillsViewportHeight(true);
		this.add(matrixTable.getTableHeader(), BorderLayout.PAGE_START);
		this.add(matrixPane, BorderLayout.CENTER);
	}
		
	//--------------------------------------------------------------------------------------------------------------------------------------
	private class MatrixTableModel extends AbstractTableModel {

    public int getColumnCount() {
      return controller.getNumColumns() + 1;
    }

    public int getRowCount() {
      return controller.getNumRows();
    }
    
    public String getColumnName(int columnIndex) {
      if (columnIndex == 0) {
        return "Taxon";
      }
      else {
        return controller.getColumnHeader(columnIndex-1);        
      }
    }

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
