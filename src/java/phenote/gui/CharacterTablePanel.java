package phenote.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;
import phenote.edit.EditManager;
import phenote.dataadapter.CharListChangeEvent;
import phenote.dataadapter.CharListChangeListener;
import phenote.dataadapter.CharacterListManager;
import phenote.config.Config;
import phenote.gui.selection.SelectionManager;

  /** Character panel has character table and del add copy buttons to manipulate
   *  table. Modifications to fields modify columns in selected row in table
   * for now no explicit commit - may be configurable later */
public class CharacterTablePanel extends JPanel {

  private JTable characterTable;
  private CharacterTableModel characterTableModel;
  private TermPanel termPanel;
  private JButton newButton;
  private JButton copyButton;
  private JButton deleteButton;
  private JButton commitButton;
  private JScrollBar verticalScrollBar;
  private boolean scrollToNewLastRowOnRepaint = false;
  
  private int selectedRow;
  // get from file menu?
  private static final String SAVE_STRING = "Save data";
  private boolean SANDBOX_MODE = true; // get from config...

  public CharacterTablePanel(TermPanel tp) {
    termPanel = tp;
    init();
  }

  private void init() {
    setLayout(new GridLayout(2,1)); // row,col
    setPreferredSize(new Dimension(400,800));
    setMinimumSize(new Dimension(400,500));    

    characterTableModel = new CharacterTableModel();
    characterTable = new JTable(characterTableModel);
    characterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    CharacterSelectionListener isl = new CharacterSelectionListener();
    characterTable.getSelectionModel().addListSelectionListener(isl);
    characterTable.setRowSelectionInterval(0,0); // select 1st row
    characterTable.getTableHeader().addMouseListener(new TableSorter());

    JScrollPane tableScroll = new JScrollPane(characterTable);
    verticalScrollBar = tableScroll.getVerticalScrollBar();//needed for scroll to new
    // wierd - changes to scrollbar seem to happen on own thread?
    verticalScrollBar.getModel().addChangeListener(new ScrollChangeListener());
    // width config? 150 * # of cols? set column width? column width config?
    characterTable.setPreferredScrollableViewportSize(new Dimension(500, 150));

    add(tableScroll);

    // add in buttons
    JPanel buttonPanel = new JPanel();

    ActionListener al = new ButtonActionListener();

    newButton = addButton("New",al,buttonPanel);
    copyButton = addButton("Copy",al,buttonPanel);
    deleteButton = addButton("Delete",al,buttonPanel);
    // should we only add if have data adapter - or disable at least?
    // should this go in a menu?
    buttonPanel.add(Box.createRigidArea(new Dimension(80,0)));
    commitButton = addButton(SAVE_STRING,al,buttonPanel);
    
    add(buttonPanel);


    EditManager.inst().addCharChangeListener(new TableCharChangeListener());
    getCharListManager().addCharListChangeListener(new TableCharListChangeListener());
  }

  private CharacterListManager getCharListManager() {
    return CharacterListManager.inst();
  }

  private JButton addButton(String name,ActionListener al,JPanel parent) {
    JButton button = new JButton(name);
    button.setActionCommand(name);
    button.addActionListener(al);
    parent.add(button);
    return button;
  }


  private boolean hasSelection() {
    return getSelectedRow() != -1;
  }

  private int getSelectedRow() {
    return characterTable.getSelectedRow();
  }
  
  /** row number is zero based for tables */
  private void selectRow(int row) {
    if (characterTable != null && row >= 0 && row < characterTable.getRowCount())
      characterTable.setRowSelectionInterval(row,row);
  }

  CharacterI getSelectedCharacter() {
    return characterTableModel.getCharacter(getSelectedRow());
  }

  // for now, for test
  CharacterListI getCharacterList() {
    return characterTableModel.getCharacterList();
  }

  /** check whether enable/disable del & copy buttons */
  private void buttonEnableCheck() {
    boolean enable = hasRows();
    copyButton.setEnabled(enable);
    deleteButton.setEnabled(enable);
  }

  /** true if row count goes down to 0 - in which case a new blank row should
      be made - at least for sandbox mode. */
  boolean hasRows() { return characterTableModel.hasRows(); }

  private void scrollToLastRow() {
    verticalScrollBar.setValue(verticalScrollBar.getMaximum()+20);
  }


  /** Listens to New, Copy & Delete buttons */
  private class ButtonActionListener implements ActionListener {

    // bug/issue - if last row is deleted should create new blank one...
    public void actionPerformed(ActionEvent e) {
      int selectRow = 0;
      if (e.getActionCommand().equals("New")) {
        selectRow = characterTableModel.addNewBlankRow();
        scrollToNewLastRowOnRepaint = true;//scrollToLastRow(); // scroll to new row
      }
      else if (!hasRows()) {
        return; // no rows to copy or delete
      }
      else if (e.getActionCommand().equals("Copy")) {
        selectRow = characterTableModel.copyRow(getSelectedRow());
        scrollToNewLastRowOnRepaint = true;//scrollToLastRow(); // scroll to new row
      }
      else if (e.getActionCommand().equals("Delete")) {
        selectRow = getSelectedRow();
        characterTableModel.deleteSelectedRow(selectRow);
        if (selectRow >= characterTable.getRowCount())
          selectRow = characterTable.getRowCount()-1; // last row deleted
      }
      
      else if (e.getActionCommand().equals(SAVE_STRING)) {
        Config c = Config.inst();
        if (!c.hasSingleDataAdapter()) {
          System.out.println("Cant commit. No data adapter configged");
          return;
        }
        c.getSingleDataAdapter().commit(characterTableModel.getCharacterList());
      }

      // IF DELETED LAST ROW, then need to make a new blank one (sandbox mode)
      if (!hasRows() && SANDBOX_MODE) {
        //termPanel.clear(); // SelectionManager.clearCharacterSelection()
        selectRow = characterTableModel.addNewBlankRow(); // sr should be 0
      }
      selectRow(selectRow);
      repaint();
      // check whether enable/disable del & copy buttons - disable w no rows
      // doesnt happen in sandbox actually - or shouldnt
      buttonEnableCheck();
    }
  } // end of ButtonActionListener inner class


  /** List/row selection listener - fired when user selects new row of table
   notifies selectionManager of new char selection */
  private class CharacterSelectionListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent e) {
      if (!hasSelection()) // this can happen with a delete row
        return;
      // need to track for reinstating select after data change
      selectedRow = getSelectedRow(); 
      CharacterI character = getSelectedCharacter();
      // is this still needed??
      //termPanel.setFieldsFromCharacter(character); // phase out...
      
      // new way
      SelectionManager.inst().selectCharacter(this,character);
    }
  }

  /** So when underlying data changes it has the unfortunate side effect of 
      kicking out the selection state. so selected row is tracked and reinstated
      when a data change event happens - although yikes - we may get this event
      before the table model - i think this all has to be done together - not
      independently... */
  private class TableCharChangeListener implements CharChangeListener {
    public void charChanged(CharChangeEvent e) {
      //int row = getSelectedRow();
      //fireTableDataChanged(); // ??? causes loss of selection
      //setRowSelectionInterval(row,row);
      repaint(); // will this cause new data to display?
    }
  }

  /** listen for new char lists being loaded */
  private class TableCharListChangeListener implements CharListChangeListener {
    public void newCharList(CharListChangeEvent e) {
      CharacterListI characterList = e.getCharacterList();
      characterTableModel.setCharacterList(characterList);
      // need to repaint & select 1st item in table
      selectRow(0);
      SelectionManager.inst().selectCharacter(this,getSelectedCharacter());
      //repaint();
      //doLayout(); // ??
    }
  }

  /** When the scrollbar changes value or range this gets called, but the task of this
      class is to set the scroll bar at the end/max when a new or copy has been done so 
      the user sees the new thing they are working on - the scroll bar doesnt see the new
      row until the repaint thread comes through, thus have to listen for scroll change
      but only in the context of new & copy (which set a flag) */
  private class ScrollChangeListener implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      if (scrollToNewLastRowOnRepaint) 
        scrollToLastRow();
      scrollToNewLastRowOnRepaint = false;
    }
  }


  private class TableSorter extends MouseAdapter {
    public void mouseClicked(MouseEvent e) {
      if (e.getClickCount() != 1) return; // ?
      Point p = e.getPoint();
      int col = characterTable.getTableHeader().columnAtPoint(p);
      if (col == -1) return;
      // shift for descending
      //  int shiftPressedInt = e.getModifiers()&InputEvent.SHIFT_MASK;
      //  boolean shiftPressed = (shiftPressedInt != 0);
      // boolean descending = shiftPressed;
      //if (model.defaultSortingIsDescending(column))descending = !descending;dont have
      characterTableModel.setSortKey(col); //, descending);
      //table.requestFocus();
      
    }
  }

  // for test
  void pressCommitButtonTest() {
    commitButton.doClick();
  }
}

  // this comes from term panel - replace with MVC stuff....
  // i think this is pase?
//   void setSelectedGenotype(String genotype) {
//     characterTable.getModel().setValueAt(genotype,getSelectedRow(),0);
//   }

//   void setSelectedEntityTerm(String entityTerm) {
//     characterTable.getModel().setValueAt(entityTerm,getSelectedRow(),1);
//   }

//   /** Set pato value for seleted character */
//   void setSelectedPatoTerm(String patoTerm) {
//     // pato is 3rd column (#2)
//     characterTable.getModel().setValueAt(patoTerm,getSelectedRow(),2);
//   }
