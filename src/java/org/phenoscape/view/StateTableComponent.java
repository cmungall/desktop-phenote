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
import org.phenoscape.model.Character;
import org.phenoscape.model.PhenoscapeController;
import org.phenoscape.model.State;
import org.phenoscape.swing.PlaceholderRenderer;

import phenote.gui.BugWorkaroundTable;
import phenote.gui.SortDisabler;
import phenote.gui.TableColumnPrefsSaver;
import phenote.util.EverythingEqualComparator;
import phenote.util.FileUtil;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.eekboom.utils.Strings;

public class StateTableComponent extends PhenoscapeGUIComponent {
  
  private JButton addStateButton;
  private JButton deleteStateButton;
  private final SortedList<State> sortedStates;

  public StateTableComponent(String id, PhenoscapeController controller) {
    super(id, controller);
    this.sortedStates = new SortedList<State>(controller.getStatesForCurrentCharacterSelection(), new EverythingEqualComparator<State>());
  }

  @Override
  public void init() {
    super.init();
    this.initializeInterface();
  }
  
  private void initializeInterface() {
    this.setLayout(new BorderLayout());
    final EventTableModel<State> statesTableModel = new EventTableModel<State>(this.sortedStates, new StatesTableFormat());
    final JTable statesTable = new BugWorkaroundTable(statesTableModel);
    statesTable.setSelectionModel(this.getController().getCurrentStatesSelectionModel());
    statesTable.setDefaultRenderer(Object.class, new PlaceholderRenderer("None"));
    statesTable.putClientProperty("Quaqua.Table.style", "striped");
    new TableColumnPrefsSaver(statesTable, this.getClass().getName());
    final TableComparatorChooser<State> sortChooser = new TableComparatorChooser<State>(statesTable, this.sortedStates, false);
    sortChooser.addSortActionListener(new SortDisabler());
    this.add(new JScrollPane(statesTable), BorderLayout.CENTER);
    this.add(this.createToolBar(), BorderLayout.NORTH);
  }
  
  private void addState() {
    final Character character = this.getSelectedCharacter();
    if (character != null) {
      final State state = character.newState();
      final int index = this.getController().getStatesForCurrentCharacterSelection().indexOf(state);
      this.getController().getCurrentStatesSelectionModel().setSelectionInterval(index, index);
    }
  }
  
  private void deleteSelectedState() {
    final Character character = this.getSelectedCharacter();
    if (character != null) {
      final State state = this.getSelectedState();
      if (state != null) { character.removeState(state); }
    }
  }
  
  private Character getSelectedCharacter() {
    final EventList<Character> selected = this.getController().getCharactersSelectionModel().getSelected();
    if (selected.size() == 1) {
      return selected.get(0);
    } else {
      return null;
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
  
  private JToolBar createToolBar() {
    final JToolBar toolBar = new JToolBar();
    try {
      this.addStateButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-add.png"))) {
          public void actionPerformed(ActionEvent e) {
            addState();
          }
        });
      this.addStateButton.setToolTipText("Add State");
      toolBar.add(this.addStateButton);
      this.deleteStateButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-remove.png"))) {
          public void actionPerformed(ActionEvent e) {
            deleteSelectedState();
          }
        });
      this.deleteStateButton.setToolTipText("Delete State");
      toolBar.add(this.deleteStateButton);
    } catch (FileNotFoundException e) {
      log().error("Couldn't find toolbar icons", e);
    }
    toolBar.setFloatable(false);
    return toolBar;
  }

  private class StatesTableFormat implements WritableTableFormat<State>, AdvancedTableFormat<State> {

    public boolean isEditable(State state, int column) {
      return true;
    }

    public State setColumnValue(State state, Object editedValue, int column) {
      switch(column) {
      case 0: state.setSymbol(editedValue.toString()); break;
      case 1: state.setLabel(editedValue.toString()); break;
      }
      return state;
    }

    public int getColumnCount() {
      return 2;
    }

    public String getColumnName(int column) {
      switch(column) {
      case 0: return "Symbol";
      case 1: return "State Description";
      default: return null;
      }
    }

    public Object getColumnValue(State state, int column) {
      switch(column) {
      case 0: return state.getSymbol();
      case 1: return state.getLabel();
      default: return null;
      }
    }

    public Class<?> getColumnClass(int column) {
      switch(column) {
      case 0: return String.class;
      case 1: return String.class;
      default: return null;
      }
    }

    public Comparator<?> getColumnComparator(int column) {
      switch(column) {
      case 0: return Strings.getNaturalComparator();
      case 1: return Strings.getNaturalComparator();
      default: return null;
      }
    }
    
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
