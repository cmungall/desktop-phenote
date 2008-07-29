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
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.phenoscape.model.PhenoscapeController;
import org.phenoscape.model.Specimen;
import org.phenoscape.model.Taxon;
import org.phenoscape.swing.PlaceholderRenderer;

import phenote.gui.BugWorkaroundTable;
import phenote.gui.SortDisabler;
import phenote.gui.TableColumnPrefsSaver;
import phenote.util.EverythingEqualComparator;
import phenote.util.FileUtil;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.eekboom.utils.Strings;

public class SpecimenTableComponent extends PhenoscapeGUIComponent {

  private JButton addSpecimenButton;
  private JButton deleteSpecimenButton;
  private final SortedList<Specimen> sortedSpecimens;

  public SpecimenTableComponent(String id, PhenoscapeController controller) {
    super(id, controller);
    this.sortedSpecimens = new SortedList<Specimen>(controller.getSpecimensForCurrentTaxonSelection(), new EverythingEqualComparator<Specimen>());
  }
  
  @Override
  public void init() {
    super.init();
    this.initializeInterface();
  }
  
  private void addSpecimen() {
    final Taxon taxon = this.getSelectedTaxon();
    if (taxon != null) {
      final Specimen specimen = taxon.newSpecimen();
      final int index = this.getController().getSpecimensForCurrentTaxonSelection().indexOf(specimen);
      this.getController().getCurrentSpecimensSelectionModel().setSelectionInterval(index, index);
    }
  }
  
  private void deleteSelectedSpecimen() {
    final Taxon taxon = this.getSelectedTaxon();
    if (taxon != null) {
      final Specimen specimen = this.getSelectedSpecimen();
      if (specimen != null) { taxon.removeSpecimen(specimen); }
    }
  }
  
  private Taxon getSelectedTaxon() {
    final EventList<Taxon> selected = this.getController().getTaxaSelectionModel().getSelected();
    if (selected.size() == 1) {
      return selected.get(0);
    } else {
      return null;
    }
  }

  private Specimen getSelectedSpecimen() {
    final EventList<Specimen> selected = this.getController().getCurrentSpecimensSelectionModel().getSelected();
    if (selected.size() == 1) {
      return selected.get(0);
    } else {
      return null;
    }
  }
  
  private void initializeInterface() {
    this.setLayout(new BorderLayout());
    final EventTableModel<Specimen> specimensTableModel = new EventTableModel<Specimen>(this.sortedSpecimens, new SpecimensTableFormat());
    final JTable specimensTable = new BugWorkaroundTable(specimensTableModel);
    specimensTable.setSelectionModel(this.getController().getCurrentSpecimensSelectionModel());
    specimensTable.setDefaultRenderer(Object.class, new PlaceholderRenderer("None"));
    specimensTable.setDefaultRenderer(OBOObject.class, new TermRenderer("None"));
    specimensTable.getColumnModel().getColumn(0).setCellEditor(this.createAutocompleteEditor(this.getController().getOntologyController().getCollectionTermSet().getTerms()));
    specimensTable.putClientProperty("Quaqua.Table.style", "striped");
    new TableColumnPrefsSaver(specimensTable, this.getClass().getName());
    final TableComparatorChooser<Specimen> sortChooser = new TableComparatorChooser<Specimen>(specimensTable, this.sortedSpecimens, false);
    sortChooser.addSortActionListener(new SortDisabler());
    this.add(new JScrollPane(specimensTable), BorderLayout.CENTER);
    this.add(this.createToolBar(), BorderLayout.NORTH);
  }
  
  private JToolBar createToolBar() {
    final JToolBar toolBar = new JToolBar();
    try {
      this.addSpecimenButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-add.png"))) {
        public void actionPerformed(ActionEvent e) {
          addSpecimen();
        }
      });
      this.addSpecimenButton.setToolTipText("Add Specimen");
      toolBar.add(this.addSpecimenButton);
      this.deleteSpecimenButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-remove.png"))) {
        public void actionPerformed(ActionEvent e) {
          deleteSelectedSpecimen();
        }
      });
      this.deleteSpecimenButton.setToolTipText("Delete Specimen");
      toolBar.add(this.deleteSpecimenButton);
    } catch (FileNotFoundException e) {
      log().error("Couldn't find toolbar icons", e);
    }
    toolBar.setFloatable(false);
    return toolBar;
  }
  
  private class SpecimensTableFormat implements WritableTableFormat<Specimen>, AdvancedTableFormat<Specimen> {

    public boolean isEditable(Specimen specimen, int column) {
      return true;
    }

    public Specimen setColumnValue(Specimen specimen, Object editedValue, int column) {
      if (editedValue instanceof OBOClass) { updateGlobalTermSelection((OBOClass)editedValue); }
      switch(column) {
      case 0: specimen.setCollectionCode((OBOClass)editedValue); break;
      case 1: specimen.setCatalogID(editedValue.toString()); break;
      }
      updateObjectForGlazedLists(specimen, getController().getSpecimensForCurrentTaxonSelection());
      return specimen;
    }

    public int getColumnCount() {
      return 2;
    }

    public String getColumnName(int column) {
      switch(column) {
      case 0: return "Collection";
      case 1: return "Catalog ID";
      default: return null;
      }
    }

    public Object getColumnValue(Specimen specimen, int column) {
      switch(column) {
      case 0: return specimen.getCollectionCode();
      case 1: return specimen.getCatalogID();
      default: return null;
      }
    }

    public Class<?> getColumnClass(int column) {
      switch(column) {
      case 0: return OBOObject.class;
      case 1: return String.class;
      default: return null;
      }
    }

    public Comparator<?> getColumnComparator(int column) {
      switch(column) {
      case 0: return GlazedLists.comparableComparator();
      case 1: return Strings.getNaturalComparator();
      default: return null;
      }
    }
    
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
}
