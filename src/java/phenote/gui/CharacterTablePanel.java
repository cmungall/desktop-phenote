package phenote.gui;

import java.util.ArrayList;
import java.util.List;

import java.awt.Dimension;
//import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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

import org.apache.log4j.Logger;

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
import phenote.dataadapter.LoadSaveManager;

  /** Character panel has character table and del add copy buttons to manipulate
   *  table. Modifications to fields modify columns in selected row in table
   * for now no explicit commit - may be configurable later */
public class CharacterTablePanel extends JPanel {

  private JTable charJTable;
  private CharacterTableModel characterTableModel;
  //private FieldPanel fieldPanel;
  //private JButton newButton;
  private JButton copyButton;
  private JButton deleteButton;
  private JButton undoButton;
  private JButton commitButton;
  private JButton graphWindow;
  private JScrollBar verticalScrollBar;
  private boolean scrollToNewLastRowOnRepaint = false;
  private boolean ignoreSelectionChange = false;
  
  //private int selectedRow;
  // get from file menu?
  private static final String SAVE_STRING = "Save data";
  // the idea of "sandbox" is that it doesnt go to db til you save/commit
  private boolean SANDBOX_MODE = true; // get from config...

  public CharacterTablePanel() { //TermPanel tp) {
    //fieldPanel = tp;
    init();
  }

  private void init() {
    //setBorder(new javax.swing.border.LineBorder(java.awt.Color.RED)); debug
    //setLayout(new GridLayout(2,1)); // row,col
    setLayout(new GridBagLayout());
    //setPreferredSize(new Dimension(1800,800));
    setPreferredSize(new Dimension(1400,500));
    //setMinimumSize(new Dimension(1400,630)); // 630   

    characterTableModel = new CharacterTableModel();
    charJTable = new JTable(characterTableModel);
    //charJTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    charJTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    CharacterSelectionListener isl = new CharacterSelectionListener();
    charJTable.getSelectionModel().addListSelectionListener(isl);
    charJTable.setRowSelectionInterval(0,0); // select 1st row
    charJTable.getTableHeader().addMouseListener(new TableSorter());

    JScrollPane tableScroll = new JScrollPane(charJTable);
    verticalScrollBar = tableScroll.getVerticalScrollBar();//needed for scroll to new
    // wierd - changes to scrollbar seem to happen on own thread?
    verticalScrollBar.getModel().addChangeListener(new ScrollChangeListener());
    // width config? 150 * # of cols? set column width? column width config?
    charJTable.setPreferredScrollableViewportSize(new Dimension(500, 600));//150

    GridBagConstraints gbc = GridBagUtil.makeFillingConstraint(0,0);
    add(tableScroll,gbc);

    // add in buttons
    JPanel buttonPanel = new JPanel();
    //buttonPanel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.BLUE));

    ActionListener al = new ButtonActionListener();

    addButton("New",al,buttonPanel); //newButton = 
    copyButton = addButton("Copy",al,buttonPanel);
    deleteButton = addButton("Delete",al,buttonPanel);
    buttonPanel.add(Box.createRigidArea(new Dimension(20,0)));
    undoButton = addButton("Undo",al,buttonPanel);
    buttonPanel.add(Box.createRigidArea(new Dimension(80,0)));
    // should we only add if have data adapter - or disable at least?
    // should this go in a menu?
    commitButton = addButton(SAVE_STRING,al,buttonPanel);
    buttonPanel.add(Box.createRigidArea(new Dimension(20,0)));
    if (Config.inst().uvicGraphIsEnabled())
      graphWindow = addButton("Graph",al,buttonPanel);
    
    // non filling
    gbc = GridBagUtil.makeAnchorConstraint(0,1,GridBagConstraints.CENTER);
    add(buttonPanel,gbc);


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
    return charJTable.getSelectedRow();
  }
  
  /** row number is zero based for tables */
  private void selectRow(int row) {
    if (charJTable == null) return;
    if (row < 0) row = 0; // ???
    // is row 0 based and table 1 based?
    if (row >= charJTable.getRowCount()) row = charJTable.getRowCount() - 1;
    //if (charJTable != null && row >= 0 && row < charJTable.getRowCount())
    charJTable.setRowSelectionInterval(row,row);
  }

  private void selectRows(RowInterval selRows) {
    int start = selRows.startRow, end = selRows.endRow;
    if (charJTable != null && start >= 0 && end < charJTable.getRowCount())
      charJTable.setRowSelectionInterval(start,end);
  }

//   private void selectRows(int start, int end) {
//     if (charJTable != null && start >= 0 && end < charJTable.getRowCount())
//       charJTable.setRowSelectionInterval(start,end);
//     System.out.println("sel start "+start+" end "+end+" ? "+(charJTable != null && start >= 0 && end < charJTable.getRowCount()));
//   }

  // used internally and by test phenote - should use sel man?
  CharacterI getSelectedCharacter() {
    return characterTableModel.getCharacter(getSelectedRow());
  }

  /** with multi select can have multiple characters selected */
  List<CharacterI> getSelectedChars() {
    int[] selRows = charJTable.getSelectedRows();
    List<CharacterI> selChars = new ArrayList(selRows.length);
    for (int i : selRows)
      selChars.add(characterTableModel.getCharacter(i)); // get from datamodel?
    return selChars;
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

  private boolean hasOneEmptySelectedRow() {
    if (getCharacterList().size() > 1) return false;
    if (getSelectedChars().size() != 1) return false; // shouldnt happen
    CharacterI c = getSelectedChars().get(0);
    return c.hasNoContent(); // method name??
  }

  private void scrollToLastRow() {
    verticalScrollBar.setValue(verticalScrollBar.getMaximum()+20);
  }


  /** Listens to New, Copy & Delete buttons */
  private class ButtonActionListener implements ActionListener {

    // bug/issue - if last row is deleted should create new blank one...
    public void actionPerformed(ActionEvent e) {
      int selectRow = 0;
      RowInterval selectRows=null;
      boolean doSelection = true;
      if (e.getActionCommand().equals("New")) {
        selectRow = characterTableModel.addNewBlankRow();
        scrollToNewLastRowOnRepaint = true;//scrollToLastRow(); // scroll to new row
      }
      else if (!hasRows()) {
        return; // its empty! no rows to copy or delete or save or undo
      }
      else if (e.getActionCommand().equals("Copy")) {
        //selectRow = characterTableModel.copyRow(getSelectedRow());
        if (!hasSelection()) {
          log().error("No rows selected for copy - this shouldnt happen!");
          return;
        }
        selectRows = characterTableModel.copyChars(getSelectedChars());
        scrollToNewLastRowOnRepaint = true;//scrollToLastRow(); // scroll to new row
      }
      else if (e.getActionCommand().equals("Delete")) {
        // if just deleting first blank row dont do anything otherwise get "false" trans
        if (hasOneEmptySelectedRow())
          return;

        selectRow = getSelectedRow();
        //characterTableModel.deleteSelectedRow(selectRow);
        // this will cause a valueChanged() to CharSelListener when things arent in
        // synch yet - supress selection listener with flag
        ignoreSelectionChange = true;
        characterTableModel.deleteChars(getSelectedChars());
        ignoreSelectionChange = false;
        if (selectRow >= charJTable.getRowCount())
          selectRow = charJTable.getRowCount()-1; // last row deleted
      }
      
      // SAVE
      else if (e.getActionCommand().equals(SAVE_STRING)) {
        // commented out this check because it won't work if there are multiple data adapters
        // error should probably be printed in LoadSaveManager anyway
        //Config c = Config.inst();
        //if (!c.hasSingleDataAdapter()) {
        //  System.out.println("Cant commit. No data adapter configged");
        //  return;
        //}
        //c.getSingleDataAdapter().commit(characterTableModel.getCharacterList());
        
        // DATABASE if theres a queryable data adapter (database) then that steals
        // the button from the file adapters
        if (Config.inst().hasQueryableDataAdapter()) {
          CharacterListI c = CharacterListManager.inst().getCharacterList();
          Config.inst().getQueryableDataAdapter().commit(c);
        }
        // FILE
        else {
          // should be renamed FileLoadSaveManager as its just for files
          LoadSaveManager.inst().saveData();
        }
      }

      // UNDO
      else if (e.getActionCommand().equals("Undo")) {
        // let char change deal with selection i think?? undo is different
        doSelection = false;
        EditManager.inst().undo();
      }

      else if (e.getActionCommand().equals("Graph")) {
          ShrimpDag.inst().display();
      }

      // IF DELETED LAST ROW, then need to make a new blank one (sandbox mode)
      if (!hasRows() && SANDBOX_MODE) {
        //fieldPanel.clear(); // SelectionManager.clearCharacterSelection()
        // adding new row can cause a out of synch selection event - wierd
        ignoreSelectionChange = true;
        characterTableModel.addInitialBlankRow(); // sr should be 0
        selectRow = 0;
        ignoreSelectionChange = false;
      }
      if (selectRows != null) // multi select
        selectRows(selectRows);
      else if (doSelection) // single row select
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
      if (ignoreSelectionChange) return; // for bulk delete error out of synch
      if (!hasSelection()) // this can happen with a delete row
        return;
      // need to track for reinstating select after data change hmm not used anymore??
      //selectedRow = getSelectedRow(); 
      //CharacterI character = getSelectedCharacter();
      // is this still needed??
      //fieldPanel.setFieldsFromCharacter(character); // phase out...
      
      // new way - change this to take list - multi!
      //SelectionManager.inst().selectCharacter(this,character);
      SelectionManager.inst().selectCharacters(this,getSelectedChars());
    }
  }

  /** So when underlying data changes it has the unfortunate side effect of 
      kicking out the selection state. so selected row is tracked and reinstated
      when a data change event happens - although yikes - we may get this event
      before the table model - i think this all has to be done together - not
      independently... */
  private class TableCharChangeListener implements CharChangeListener {
    public void charChanged(CharChangeEvent e) {
      if (e.isUpdate()) {
        repaint(); // repaint causes new cell val to get displayed
        return;
      }
      int row = getSelectedRow(); // multi select?? row del? row add?
      // this was commented out repaint was suffic but now needed for undo of copy/delete
      // if this is slow we may need to get more savvy - but ill bet its fine
      characterTableModel.fireTableDataChanged(); // ??? causes loss of selection
      //setRowSelectionInterval(row,row);
      // big problem, select row causes char select evt, CFG gets this and tries to
      // update itself but causes ex if in middle of edit. need to be more savvy, if
      // just char update then need to supress select event as its really just the table
      // reinstating its selection actually if update just do repaint and return, 
      // if add or del then need to
      // send out event
      selectRow(row);
      // repaint(); // is this needed after fireTableDataChanged?
    }
  }

  /** listen for new char lists being loaded */
  private class TableCharListChangeListener implements CharListChangeListener {
    public void newCharList(CharListChangeEvent e) {
      CharacterListI characterList = e.getCharacterList();
      characterTableModel.setCharacterList(characterList);
      // need to repaint & select 1st item in table
      selectRow(0);
      SelectionManager.inst().selectCharacters(this,getSelectedChars());
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
      int col = charJTable.getTableHeader().columnAtPoint(p);
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
  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
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
