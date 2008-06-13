package org.phenoscape.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.util.Comparator;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;
import org.phenoscape.model.PhenoscapeController;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.State;

import phenote.gui.BugWorkaroundTable;
import phenote.gui.TableColumnPrefsSaver;
import phenote.util.FileUtil;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

public class PhenotypeTableComponent extends PhenoscapeGUIComponent {

  private JButton addPhenotypeButton;
  private JButton deletePhenotypeButton;
  
  public PhenotypeTableComponent(String id, PhenoscapeController controller) {
    super(id, controller);
  }
  
  @Override
  public void init() {
    super.init();
    this.initializeInterface();
  }

  private void initializeInterface() {
    this.setLayout(new BorderLayout());
    final PhenotypesTableFormat tableFormat = new PhenotypesTableFormat();
    final EventTableModel<Phenotype> phenotypesTableModel = new EventTableModel<Phenotype>(this.getController().getPhenotypesForCurrentStateSelection(), tableFormat);
    final JTable phenotypesTable = new BugWorkaroundTable(phenotypesTableModel);
    phenotypesTable.setSelectionModel(this.getController().getCurrentPhenotypesSelectionModel());
    phenotypesTable.setDefaultRenderer(OBOClass.class, new TermRenderer());
    for (int i = 0; i < phenotypesTable.getColumnCount(); i++) {
      final TableCellEditor editor = tableFormat.getColumnEditor(i);
      if (editor != null) { phenotypesTable.getColumnModel().getColumn(i).setCellEditor(editor); }
    }
    phenotypesTable.putClientProperty("Quaqua.Table.style", "striped");
    new TableColumnPrefsSaver(phenotypesTable, this.getClass().getName());
    this.add(new JScrollPane(phenotypesTable), BorderLayout.CENTER);
    this.add(this.createToolBar(), BorderLayout.NORTH);
  }
  
  private void addPhenotype() {
    final State state = this.getSelectedState();
    if (state != null) {
      final Phenotype phenotype = state.newPhenotype();
      final int index = this.getController().getPhenotypesForCurrentStateSelection().indexOf(phenotype);
      this.getController().getCurrentPhenotypesSelectionModel().setSelectionInterval(index, index);
    }
  }
  
  private void deleteSelectedPhenotype() {
    final State state = this.getSelectedState();
    if (state != null) {
      final Phenotype phenotype = this.getSelectedPhenotype();
      if (phenotype != null) { state.removePhenotype(phenotype); }
    }
  }
  
  private State getSelectedState() {
    final EventList<State> selected = this.getController().getCurrentStatesSelectionModel().getSelected();
    if (selected.size() == 1) {
      return selected.get(0);
    } else {
      return null;
    }
  }
  
  private Phenotype getSelectedPhenotype() {
    final EventList<Phenotype> selected = this.getController().getCurrentPhenotypesSelectionModel().getSelected();
    if (selected.size() == 1) {
      return selected.get(0);
    } else {
      return null;
    }
  }

  private JToolBar createToolBar() {
    final JToolBar toolBar = new JToolBar();
    try {
      this.addPhenotypeButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-add.png"))) {
          public void actionPerformed(ActionEvent e) {
            addPhenotype();
          }
        });
      this.addPhenotypeButton.setToolTipText("Add Phenotype");
      toolBar.add(this.addPhenotypeButton);
      this.deletePhenotypeButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-remove.png"))) {
          public void actionPerformed(ActionEvent e) {
            deleteSelectedPhenotype();
          }
        });
      this.deletePhenotypeButton.setToolTipText("Delete Phenotype");
      toolBar.add(this.deletePhenotypeButton);
    } catch (FileNotFoundException e) {
      log().error("Couldn't find toolbar icons", e);
    }
    toolBar.setFloatable(false);
    return toolBar;
  }
  
  private class PhenotypesTableFormat implements WritableTableFormat<Phenotype>, AdvancedTableFormat<Phenotype> {
    
    public boolean isEditable(Phenotype phenotype, int column) {
      return true;
    }

    public Phenotype setColumnValue(Phenotype phenotype, Object editedValue, int column) {
      switch (column) {
      case 0: phenotype.setEntity((OBOClass)editedValue); break;
      case 1: phenotype.setQuality((OBOClass)editedValue); break;
      case 2: phenotype.setRelatedEntity((OBOClass)editedValue); break;
      case 3: phenotype.setCount((Integer)editedValue); break;
      case 4: phenotype.setMeasurement((Float)editedValue); break;
      case 5: phenotype.setUnit((OBOClass)editedValue); break;
      case 6: phenotype.setNotes(editedValue.toString()); break;
      }
      return phenotype;
    }

    public int getColumnCount() {
      return 7;
    }

    public String getColumnName(int column) {
      switch (column) {
      case 0: return "Entity";
      case 1: return "Quality";
      case 2: return "Related Entity";
      case 3: return "Count";
      case 4: return "Measurement";
      case 5: return "Unit";
      case 6: return "Notes";
      default: return null;
      }
    }
    
    public TableCellEditor getColumnEditor(int column) {
      switch (column) {
      case 0: return new TermEditor(new JTextField(), getController().getOntologyController().getEntityTermSet());
      case 1: return new TermEditor(new JTextField(), getController().getOntologyController().getQualityTermSet());
      case 2: return new TermEditor(new JTextField(), getController().getOntologyController().getRelatedEntityTermSet());
      case 3: return null;
      case 4: return null;
      case 5: return new TermEditor(new JTextField(), getController().getOntologyController().getUnitTermSet());
      case 6: return null;
      default: return null;
      }
    }

    public Object getColumnValue(Phenotype phenotype, int column) {
      switch (column) {
      case 0: return phenotype.getEntity();
      case 1: return phenotype.getQuality();
      case 2: return phenotype.getRelatedEntity();
      case 3: return phenotype.getCount();
      case 4: return phenotype.getMeasurement();
      case 5: return phenotype.getUnit();
      case 6: return phenotype.getNotes();
      default: return null;
      }
    }

    public Class<?> getColumnClass(int column) {
      switch (column) {
      case 0: return OBOClass.class;
      case 1: return OBOClass.class;
      case 2: return OBOClass.class;
      case 3: return Integer.class;
      case 4: return Float.class;
      case 5: return OBOClass.class;
      case 6: return String.class;
      default: return null;
      }
    }

    public Comparator<?> getColumnComparator(int arg0) {
      // TODO Auto-generated method stub
      return null;
    }
    
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
}
