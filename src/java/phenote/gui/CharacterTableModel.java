package phenote.gui;

import java.util.List;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import phenote.datamodel.Character;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.datamodel.CharFieldEnum;
import phenote.dataadapter.CharacterListManager;
import phenote.config.Config;

/** 
 *  table model for table of characters. currently holds list of characters - this should
 be moved to datamodel - CharacterList object? CharacterSession? Phenotype?
 */
class CharacterTableModel extends AbstractTableModel {

  //private int rowCount = 0;
  // todo - get this from data adapter/model - especially for loaded
  private CharacterListI characterList;// = new CharacterList();
  private Config config = Config.inst();

  CharacterTableModel() {
    characterList = getCharListManager().getCharacterList();
    addNewBlankRow(); // add blank row to start off
    //getCharListManager().addCharListChangeListener(new TableCharListChangeListener());
    // --> panel
    //EditManager.inst().addCharChangeListener(new TableCharChangeListener());
  }

  private CharacterListManager getCharListManager() { return CharacterListManager.inst(); }

  CharacterI getCharacter(int i) {
    // check out of bounds
    return characterList.get(i);
  }

  // this should come from datamodel not here - refactor
  CharacterListI getCharacterList() {
    return characterList;
  }

  void setCharacterList(CharacterListI charList) {
    characterList = charList;
    fireTableDataChanged();
  }

  public String getColumnName(int col) {
    return config.getFieldLabel(col);
  }

  public int getColumnCount() {
    return config.getNumberOfFields();
  }

  public int getRowCount() {
    //return rowCount;
    return getCharacterList().size();
  }

  boolean hasRows() { return getRowCount() > 0; }

  /** Returns row # of new row - handy for selection */
  int addNewBlankRow() {
    return addCharacter(new Character());
  }

  /** returns row # of new copied into row */
  int copyRow(int rowToCopy) {
    if (!hasRows())
      return -1; // err msg?
    CharacterI copy = getCharacter(rowToCopy).cloneCharacter();
    return addCharacter(copy);
  }
    
  /** Returns row # of row inserted */
  private int addCharacter(CharacterI character) {
    //++rowCount;
    // changes data model (fire event?)
    characterList.add(character);
    fireTableRowsInserted(getRowCount(),getRowCount());
    return getRowCount() -1;
  }

  void deleteSelectedRow(int deleteRow) {
    if (!hasRows())
      return; // err msg?
    characterList.remove(deleteRow);
    fireTableRowsDeleted(deleteRow,deleteRow);
  }


  public Object getValueAt(int row, int col) {
    CharacterI inst = getCharacter(row);
    if (inst == null) {
      System.out.println("ERROR: character is null for row "+row+" in table");
      return null;
    }
    if (config.getCharFieldEnum(col) == null) {
      System.out.println("Error column "+col+" not configured proplerly in "+
                         "character table, cant retrieve value ");
      return null;
    }
    return config.getCharFieldEnum(col).getValue(inst).getName();
  }

  private CharFieldEnum getCharFieldEnum(int col) {
    return config.getCharFieldEnum(col);
  }

    
  public boolean isCellEditable(int r, int c) {
    return false;
  }

  /** hmmmm... do we sort data or some view of data, view is probably cleaner
      but for now just sort data itself? dont yet have separate view objects to
      sort */
  void setSortKey(int col) {
    CharFieldEnum cfe = getCharFieldEnum(col);
    characterList.sortBy(cfe); // ???
    fireTableDataChanged();
  }

}
    //--rowCount;
    // hmmmmm - this cant be hardwired, needs to work with config 
    // not sure how to do this....
    // getField(col).getValue(inst);
    //switch (col) { case(0) : return inst.getGenotype(); case(1) : return inst.getEntity();
    //case(2) : return inst.getPato();}
//   // i dont think this is still used??? if it is refactor! its wrong actually
//   public void setValueAtDELETEhmmmmm(Object value, int row, int col) {
//     CharacterI inst = getCharacter(row);
//     String valString = (String)value; // better be a string

//     // getField(col).setValue(valString); // setValString? setVal(Obj)?

//     switch (col) {
//       case(0) : 
//         inst.setGenotype(valString);
//         break;
//       case(1) : 
//         inst.setEntity(valString);
//         break;
//       case(2) : 
//         inst.setPato(valString);
//     }
//     fireTableCellUpdated(row,col); // causes repaint
//   }
//   private class TableCharListChangeListener implements CharListChangeListener {
//     public void newCharList(CharListChangeEvent e) {
//       characterList = e.getCharacterList();
//       // need to repaint & select 1st item in table
//       // do all of this in Panel?
//     }
//   }

  // --> CharTablePanel
//   private class TableCharChangeListener implements CharChangeListener {
//     public void charChanged(CharChangeEvent e) {
//       //int row = getSelectedRow();
//       //fireTableDataChanged(); // ??? causes loss of selection
//       //setRowSelectionInterval(row,row);
//       repaint(); // will this cause new data to come in?
//     }
//   }


