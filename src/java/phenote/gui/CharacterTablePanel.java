package phenote.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
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
class CharacterTablePanel extends JPanel {

  private JTable characterTable;
  private CharacterTableModel characterTableModel;
  private TermPanel termPanel;
  private JButton newButton;
  private JButton copyButton;
  private JButton deleteButton;
  private JButton commitButton;
  
  private int selectedRow;
  private boolean SANDBOX_MODE = true; // get from config...

  CharacterTablePanel(TermPanel tp) {
    termPanel = tp;
    init();
  }

  private void init() {
    setLayout(new GridLayout(2,1)); // row,col
    
    characterTableModel = new CharacterTableModel();
    characterTable = new JTable(characterTableModel);
    characterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    CharacterSelectionListener isl = new CharacterSelectionListener();
    characterTable.getSelectionModel().addListSelectionListener(isl);
    characterTable.setRowSelectionInterval(0,0); // select 1st row

    JScrollPane tableScroll = new JScrollPane(characterTable);
    // width config? 150 * # of cols? set column width? column width config?
    characterTable.setPreferredScrollableViewportSize(new Dimension(600, 150));

    add(tableScroll);

    // add in buttons
    JPanel buttonPanel = new JPanel();

    ActionListener al = new ButtonActionListener();

    newButton = addButton("New",al,buttonPanel);
    copyButton = addButton("Copy",al,buttonPanel);
    deleteButton = addButton("Delete",al,buttonPanel);
    // should we only add if have data adapter - or disable at least?
    // should this go in a menu?
    commitButton = addButton("Commit",al,buttonPanel);
    
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


  /** Listens to New, Copy & Delete buttons */
  private class ButtonActionListener implements ActionListener {

    // bug/issue - if last row is deleted should create new blank one...
    public void actionPerformed(ActionEvent e) {
      int selectRow = 0;
      if (e.getActionCommand().equals("New")) {
        selectRow = characterTableModel.addNewBlankRow();
      }
      else if (!hasRows()) {
        return; // no rows to copy or delete
      }
      else if (e.getActionCommand().equals("Copy")) {
        selectRow = characterTableModel.copyRow(getSelectedRow());
      }
      else if (e.getActionCommand().equals("Delete")) {
        selectRow = getSelectedRow();
        characterTableModel.deleteSelectedRow(selectRow);
        if (selectRow >= characterTable.getRowCount())
          selectRow = characterTable.getRowCount()-1; // last row deleted
      }
      
      else if (e.getActionCommand().equals("Commit")) {
        Config c = Config.inst();
        if (!c.hasSingleDataAdapter()) {
          System.out.println("Cant commit. No data adapter configged");
          return;
        }
        c.getSingleDataAdapter().commit(characterTableModel.getCharacterList());
      }

      // if deleted last row, then need to make a new blank one (sandbox mode)
      if (!hasRows() && SANDBOX_MODE) {
        //termPanel.clear(); // SelectionManager.clearCharacterSelection()
        selectRow = characterTableModel.addNewBlankRow(); // sr should be 0
      }
      selectRow(selectRow);
      // check whether enable/disable del & copy buttons - disable w no rows
      // doesnt happen in sandbox actually - or shouldnt
      buttonEnableCheck();
    }
  } // end of inner class


  /** List/row selection listener - fired when user selects new row of table
      refactor - this should send out CharacterSelectionEvent */
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
