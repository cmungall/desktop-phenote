package phenote.matrix.view;

import org.bbop.framework.AbstractGUIComponent;

import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.LoadSaveListener;
import phenote.dataadapter.LoadSaveManager;
import phenote.matrix.model.MatrixController;
import phenote.matrix.model.MatrixRow;
import java.awt.BorderLayout;
import java.io.File;
import java.util.Comparator;

import javax.swing.JTable;
import javax.swing.JScrollPane;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

public class MatrixComponent extends AbstractGUIComponent {
	
	private final MatrixController controller;
	private EventTableModel<MatrixRow> matrixTableModel;
	private JTable matrixTable;
	private LoadSaveManager loadSaveManager;
	
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
		this.getController().getMatrix().addListEventListener(new MatrixListener());
	    LoadSaveManager.inst().addListener(new FileListener());
		this.initializeInterface();
	}
	
	/**
	 * Creates the table model for the matrix and add the table to the interface component
	 */
	public void initializeInterface() {
		this.setLayout(new BorderLayout());
		matrixTableModel = new EventTableModel<MatrixRow>(this.getController().getMatrix(), new MatrixTableFormat());
		matrixTable = new JTable(matrixTableModel);
		matrixTable.putClientProperty("Quaqua.Table.style", "striped");
		this.add(new JScrollPane(matrixTable), BorderLayout.CENTER);
	}
	
	public <T> void updateObjectForGlazedLists(T anObject, EventList<T> aList) {
		final int index = aList.indexOf(anObject);
		if (index > -1) {
			aList.set(index, anObject);
		}
	}
	
	/**
	 * Mostly copied from CharacterTemplateTable.java
	 * @param mainFile
	 */
	private void tryLoadDefaultDataFile(File mainFile) {
	    final File file = (mainFile == null) ? CharacterListManager.main().getCurrentDataFile() : mainFile;
	    if (file == null) {
	      return;
	    }
	    File templatesFile = this.getDefaultDataFile(mainFile);
	    if (templatesFile.exists()) {
	      this.getLoadSaveManager().loadData(templatesFile);
	    }
	  }
	
	/**
	 * Mostly copied from CharacterTemplateTable.java
	 * @param mainFile
	 */  
	  private File getDefaultDataFile(File mainFile) {
	    final int dotLocation = mainFile.getName().lastIndexOf(".");
	    final boolean hasExtension = dotLocation > 0;
	    final String extension = hasExtension ? mainFile.getName().substring(dotLocation) : "";
	    final String baseName = hasExtension ? mainFile.getName().substring(0, dotLocation) : mainFile.getName();
	    //final String defaultFileName = baseName + "-" + this.getGroup() +  extension;
	    final String defaultFileName = baseName + extension;
	    return new File(mainFile.getParent(), defaultFileName);
	  }
	
	  private LoadSaveManager getLoadSaveManager() {
		  if (this.loadSaveManager == null) {
		      this.loadSaveManager = new LoadSaveManager(this.getCharacterListManager());
		  }
		  return this.loadSaveManager;
	  }
	  
	  private CharacterListManager getCharacterListManager() {
		    return CharacterListManager.main();
		  }
	  
	  public void saveCharacters(File f) {
		    this.getLoadSaveManager().saveData(f);
		  }
	
	//--------------------------------------------------------------------------------------------------------------------------------------
	private class MatrixTableFormat implements WritableTableFormat<MatrixRow>, AdvancedTableFormat<MatrixRow> {

		@Override
		public boolean isEditable(MatrixRow arg0, int arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public MatrixRow setColumnValue(MatrixRow arg0, Object arg1, int arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getColumnName(int column) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object getColumnValue(MatrixRow baseObject, int column) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Class getColumnClass(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Comparator getColumnComparator(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	//--------------------------------------------------------------------------------------------------------------------------------------
	private class MatrixListener implements ListEventListener<MatrixRow> {
		@Override
		public void listChanged(ListEvent<MatrixRow> arg0) {
			updateObjectForGlazedLists((MatrixRow) arg0.getSource(), arg0.getSourceList());
		}
	}
	
	//--------------------------------------------------------------------------------------------------------------------------------------
	private class FileListener implements LoadSaveListener {
	    public void fileLoaded(File f) {
	      tryLoadDefaultDataFile(f);
	    }
	    public void fileSaved(File f) {
	      saveCharacters(getDefaultDataFile(f));
	    }
	  }
}
