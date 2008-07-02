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
import org.phenoscape.model.PhenoscapeController;
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

public class TaxonTableComponent extends PhenoscapeGUIComponent {

  private JButton addTaxonButton;
  private JButton deleteTaxonButton;
  private final SortedList<Taxon> sortedTaxa;

  public TaxonTableComponent(String id, PhenoscapeController controller) {
    super(id, controller);
    this.sortedTaxa = new SortedList<Taxon>(controller.getDataSet().getTaxa(), new EverythingEqualComparator<Taxon>());
  }

  @Override
  public void init() {
    super.init();
    this.initializeInterface();
  }
  
  private void addTaxon() {
    final Taxon taxon = this.getController().getDataSet().newTaxon();
    final int index = this.getController().getDataSet().getTaxa().indexOf(taxon);
    this.getController().getTaxaSelectionModel().setSelectionInterval(index, index);
  }
  
  private void deleteSelectedTaxon() {
    final Taxon taxon = this.getSelectedTaxon();
    if (taxon != null) { this.getController().getDataSet().removeTaxon(taxon); }
  }
  
  private Taxon getSelectedTaxon() {
    final EventList<Taxon> selected = this.getController().getTaxaSelectionModel().getSelected();
    if (selected.size() == 1) {
      return selected.get(0);
    } else {
      return null;
    }
  }

  private void initializeInterface() {
    this.setLayout(new BorderLayout());
    final EventTableModel<Taxon> taxaTableModel = new EventTableModel<Taxon>(this.sortedTaxa, new TaxaTableFormat());
    final JTable taxaTable = new BugWorkaroundTable(taxaTableModel);
    taxaTable.setSelectionModel(this.getController().getTaxaSelectionModel());
    taxaTable.setDefaultRenderer(Object.class, new PlaceholderRenderer("None"));
    taxaTable.setDefaultRenderer(OBOClass.class, new TermRenderer("None"));
    taxaTable.getColumnModel().getColumn(0).setCellEditor(this.createAutocompleteEditor(this.getController().getOntologyController().getTaxonTermSet().getTerms()));
    taxaTable.putClientProperty("Quaqua.Table.style", "striped");
    new TableColumnPrefsSaver(taxaTable, this.getClass().getName());
    final TableComparatorChooser<Taxon> sortChooser = new TableComparatorChooser<Taxon>(taxaTable, this.sortedTaxa, false);
    sortChooser.addSortActionListener(new SortDisabler());
    this.add(new JScrollPane(taxaTable), BorderLayout.CENTER);
    this.add(this.createToolBar(), BorderLayout.NORTH);
  }
  
  private JToolBar createToolBar() {
    final JToolBar toolBar = new JToolBar();
    try {
      this.addTaxonButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-add.png"))) {
          public void actionPerformed(ActionEvent e) {
            addTaxon();
          }
        });
      this.addTaxonButton.setToolTipText("Add Taxon");
      toolBar.add(this.addTaxonButton);
      this.deleteTaxonButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-remove.png"))) {
          public void actionPerformed(ActionEvent e) {
            deleteSelectedTaxon();
          }
        });
      this.deleteTaxonButton.setToolTipText("Delete Taxon");
      toolBar.add(this.deleteTaxonButton);
    } catch (FileNotFoundException e) {
      log().error("Couldn't find toolbar icons", e);
    }
    toolBar.setFloatable(false);
    return toolBar;
  }

  private class TaxaTableFormat implements WritableTableFormat<Taxon>, AdvancedTableFormat<Taxon> {

    public boolean isEditable(Taxon taxon, int column) {
      return true;
    }

    public Taxon setColumnValue(Taxon taxon, Object editedValue, int column) {
      if (editedValue instanceof OBOClass) { updateGlobalTermSelection((OBOClass)editedValue); }
      switch (column) {
      case 0: taxon.setValidName((OBOClass)editedValue); break;
      case 1: taxon.setPublicationName(editedValue.toString()); break;
      }
      return taxon;
    }

    public int getColumnCount() {
      return 2;
    }

    public String getColumnName(int column) {
      switch (column) {
      case 0: return "Valid Taxon";
      case 1: return "Publication Taxon";
      default: return null;
      }
    }

    public Object getColumnValue(Taxon taxon, int column) {
      switch (column) {
      case 0: return taxon.getValidName();
      case 1: return taxon.getPublicationName();
      default: return null;
      }
    }

    public Class<?> getColumnClass(int column) {
      switch (column) {
      case 0: return OBOClass.class;
      case 1: return String.class;
      default: return null;
      }
    }

    public Comparator<?> getColumnComparator(int column) {
      switch (column) {
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
