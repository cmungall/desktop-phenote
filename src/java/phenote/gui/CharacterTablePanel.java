package phenote.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

import phenote.config.Config;
import phenote.dataadapter.CharListChangeEvent;
import phenote.dataadapter.CharListChangeListener;
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.LoadSaveManager;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;
import phenote.edit.EditManager;
import phenote.gui.selection.SelectionManager;

  /** Character panel has character table and del add copy buttons to manipulate
   *  table. Modifications to fields modify columns in selected row in table
   * for now no explicit commit - may be configurable later */
public class CharacterTablePanel extends JPanel {

  private CharacterTableModel characterTableModel;
  private JTable charJTable;
  private static int DEFAULT_COLWIDTH=150; //default column width for table
  private static int INIT_TABLE_WIDTH=1400;
  private static int INIT_TABLE_HEIGHT=500;
  private static int DEFAULT_VIEW_WIDTH=500;
  private static int DEFAULT_VIEW_HEIGHT=600;
  //private FieldPanel fieldPanel;
  //private JButton newButton;
  private JButton copyButton;
  private JButton deleteButton;
  private JButton undoButton;
  private JButton commitButton;
  private JButton graphWindow;
  private JScrollBar verticalScrollBar;
  private JScrollBar horizontalScrollBar;
  private boolean scrollToNewLastRowOnRepaint = false;
  private boolean ignoreSelectionChange = false;
  private Point currentMousePoint = new Point(0,0);
  private int tableWidth=INIT_TABLE_WIDTH;
  private int tableHeight=INIT_TABLE_HEIGHT;
  private CharacterListManager characterListManager;
  private EditManager editManager;
  private SelectionManager selectionManager;
  
  //private int selectedRow;
  // get from file menu?
  private static final String SAVE_STRING = "Save data";
  // the idea of "sandbox" is that it doesnt go to db til you save/commit
  private boolean SANDBOX_MODE = true; // get from config...

  public CharacterTablePanel() { //TermPanel tp) {
    //fieldPanel = tp;
    this(CharacterListManager.inst(), EditManager.inst(), SelectionManager.inst());
  }
  
  public CharacterTablePanel(CharacterListManager clManager, EditManager eManager, SelectionManager selManager) {
    super();
    this.characterListManager = clManager;
    this.editManager = eManager;
    this.selectionManager = selManager;
    this.characterTableModel = new CharacterTableModel(this.characterListManager, this.editManager);
    this.charJTable = new JTable(this.characterTableModel);
    init();
  }

  private void init() {
    //setBorder(new javax.swing.border.LineBorder(java.awt.Color.RED)); debug
    //setLayout(new GridLayout(2,1)); // row,col
    setLayout(new GridBagLayout());
    //setPreferredSize(new Dimension(1800,800));
    setColumns();
    setPreferredSize(new Dimension(tableWidth, tableHeight));
    //setMinimumSize(new Dimension(1400,630)); // 630   

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
    horizontalScrollBar = tableScroll.getHorizontalScrollBar();
    if (tableWidth>DEFAULT_VIEW_WIDTH)
    	charJTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    // width config? 150 * # of cols? set column width? column width config?
    charJTable.setPreferredScrollableViewportSize(new Dimension(DEFAULT_VIEW_WIDTH, DEFAULT_VIEW_HEIGHT));//150

    GridBagConstraints gbc = GridBagUtil.makeFillingConstraint(0,0);

    //Add listener to components that can bring up popup menus.
    JPopupMenu popup = new TableRightClickMenu();
    MouseListener popupListener = new PopupListener(popup);
    charJTable.addMouseListener(popupListener);

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
    commitButton = addButton(getCommitButtonString(),al,buttonPanel);
    buttonPanel.add(Box.createRigidArea(new Dimension(20,0)));
    if (Config.inst().uvicGraphIsEnabled())
      graphWindow = addButton("Graph",al,buttonPanel);
    
    // non filling
    gbc = GridBagUtil.makeAnchorConstraint(0,1,GridBagConstraints.CENTER);
    add(buttonPanel,gbc);


    this.editManager.addCharChangeListener(new TableCharChangeListener());
    getCharListManager().addCharListChangeListener(new TableCharListChangeListener());
  }

  private void setColumns() {
  	//this method sets the column widths according to those in the configuration
  	//default width=30
  	//will determine the total table width according to dimensions in config
  	int columnCount = characterTableModel.getColumnCount();
  	int totalWidth = 0;
    for (int i = 0; i < columnCount; i++) {
      TableColumn column = charJTable.getColumnModel().getColumn(i);
      //get the field width from the model
      int width = characterTableModel.getColwidth(i);
      if (width==0) //-1 is no display
      	{width=DEFAULT_COLWIDTH;}
      column.setPreferredWidth(width);
      column.addPropertyChangeListener(new ColwidthChangeListener());
      charJTable.getTableHeader().setResizingColumn(column);
      column.setWidth(width);
      totalWidth+=width;
     	//System.out.println("Set column "+characterTableModel.getColumnName(i)+" width = "+width);  	
    }
    //System.out.println("Total width = "+totalWidth);
    tableWidth=totalWidth;
  }
  
  private String getCommitButtonString() {
    if (Config.inst().hasQueryableDataAdapter())
      return Config.inst().getQueryableDataAdapter().getCommitButtonLabel();
    else return SAVE_STRING;
  }

  private CharacterListManager getCharListManager() {
    return this.characterListManager;
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

  public int getSelectedRow() {
    return charJTable.getSelectedRow();
  }
  
  public String getCellString(int row, int col) {
  	Object s = charJTable.getValueAt(row,col);
  	if (s==null) {
  		return ("");
  	} else return s.toString();
  }
  
  public CharFieldValue getCellCharField(int row, int col) {
  	CharFieldValue cfv = (CharFieldValue) charJTable.getValueAt(row,col);
  	return cfv;
  }
  
  /** row number is zero based for tables */
  private void selectRow(int row) {
    if (charJTable == null) return;
    if (charJTable.getRowCount() == 0) return;
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
      else if (e.getActionCommand().equals(getCommitButtonString())) {
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
        CharacterTablePanel.this.editManager.undo();
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

      // SELECT ROWS
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
      // selection sends out many spurious events, the only one we want is non-adjust
      if (e.getValueIsAdjusting()) return;
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
      CharacterTablePanel.this.selectionManager.selectCharacters(this,getSelectedChars());
    }
  }

  /** So when underlying data changes it has the unfortunate side effect of 
      kicking out the selection state. so selected row is tracked and reinstated
      when a data change event happens - although yikes - we may get this event
      before the table model - i think this all has to be done together - not
      independently... triggered by undo, copyChars, update field */
  private class TableCharChangeListener implements CharChangeListener {
    public void charChanged(CharChangeEvent e) {
      if (e.isUpdate()) {
        repaint(); // repaint causes new cell val to get displayed
        return; // avoid select below
      }
      if (e.isAdd()) { // works for add, compound add, delete-undo
        charJTable.clearSelection();
        characterTableModel.fireTableDataChanged(); // ??? causes loss of selection
        for (CharacterI c : e.getTransaction().getCharacters()) { // compound add??
          int addIndex = characterTableModel.indexOf(c);
          if (addIndex != -1)
            charJTable.addRowSelectionInterval(addIndex,addIndex);
        }
      }
      else { // delete(undo add)
        int selRow = getSelectedRow(); // multi select?? row del? row add?
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
        selectRow(selRow); // ???
        // repaint(); // is this needed after fireTableDataChanged?
      }
    }
  }

  /** listen for new char lists being loaded */
  private class TableCharListChangeListener implements CharListChangeListener {
    public void newCharList(CharListChangeEvent e) {
      CharacterListI characterList = e.getCharacterList();
      characterTableModel.setCharacterList(characterList);
      // need to repaint & select 1st item in table
      selectRow(0);
      CharacterTablePanel.this.selectionManager.selectCharacters(this,getSelectedChars());
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
  
  private class ColwidthChangeListener implements PropertyChangeListener {
  	public void propertyChange(PropertyChangeEvent e)
  	{
  		//listens for changes to the column widths, makes changes to config,
  		//and flags that there's been changes (so that they'll be saved)
  		if (e.getPropertyName().equals("width")) {
  			TableColumn col = (TableColumn)e.getSource();
  			int colIndex = col.getModelIndex();
  			Integer newWidth = (Integer)e.getNewValue();
  			int w = newWidth.intValue();
  			if (w!=(Config.inst().getFieldColwidth(colIndex))) {
  				//only flag modified if changed from original settings
  				Config.inst().setFieldColwidth(colIndex, w);
  				Config.inst().setConfigModified(true);
//  				System.out.println("column "+colIndex+" "+e.getPropertyName()+" changed: "+ e.getOldValue()+" --> "+w);
  			}
  		}
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
  private void setPoint (Point p) {
  	currentMousePoint= p;
  	return;
  }
  public Point getPoint() {
  	return currentMousePoint;
  }
  
  public Point getTableCoord() {
  	Point coord;
  	int col = charJTable.getTableHeader().columnAtPoint(currentMousePoint);
    int row = charJTable.rowAtPoint(currentMousePoint);
    coord = new Point(row,col);
    return (coord);
  }
  
  private class PopupListener extends MouseAdapter {
  	JPopupMenu popup;
  	int col; int row;
  	Point p;
  	PopupListener(JPopupMenu popupMenu) {
  		popup = popupMenu;
  	}

  	public void mousePressed(MouseEvent e) {
//  		super.mousePressed(e);
  		maybeShowPopup(e);
  	}

  	public void mouseReleased(MouseEvent e) {
//  		super.mouseReleased(e);
  		maybeShowPopup(e);
  	}

  	private void maybeShowPopup(MouseEvent e) {
  		String m="";
  		col=e.getX();
  		row=e.getY();
  		p= e.getPoint();
      col = charJTable.getTableHeader().columnAtPoint(p);
      row = charJTable.rowAtPoint(p);
      setPoint(p);
//      System.out.println("col="+col+" row= "+row);
//  		System.out.println("button="+e.getButton());
//  		System.out.println(e.paramString());
//  		System.out.println("popuptrigger="+e.isPopupTrigger());
//  		if(e.getButton()==MouseEvent.BUTTON3) {
  		if (e.isPopupTrigger()) {
    		m = "popuptrigger!";
    		popup.show(e.getComponent(),
  					e.getX(), e.getY());
  		}
//  		else {
//  			m="no trigger, its "+e.paramString()+"!";
//  		}
//  		JOptionPane.showMessageDialog(null, m, "Phenote Help",
//    			JOptionPane.INFORMATION_MESSAGE);
//  		e.consume();
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
