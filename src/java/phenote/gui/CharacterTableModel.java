package phenote.gui;

import java.util.List;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import phenote.datamodel.CharField;
import phenote.datamodel.Character;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.OntologyManager;
//import phenote.edit.CompoundTransaction;
import phenote.edit.EditManager;
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
    // should supress editing model for this i think???
    addInitialBlankRow(); // add blank row to start off
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

  void addInitialBlankRow() {
    EditManager.inst().addInitialCharacter();
    fireTableRowsInserted(getRowCount(),getRowCount());
  }

  /** Returns row # of new row - handy for selection */
  int addNewBlankRow() {
    EditManager.inst().addNewCharacter();
    fireTableRowsInserted(getRowCount(),getRowCount());
    return getRowCount() - 1; // last row
    //return addCharacter(new Character());
  }


//   /** returns row # of new copied into row */
//   int copyRow(int rowToCopy) {
//     if (!hasRows())
//       return -1; // err msg?
//     CharacterI copy = getCharacter(rowToCopy).cloneCharacter();
//     return addCharacter(copy);
//   }
    
  /** return int[]? of all new rows to be selected? yes i think so */
  RowInterval copyChars(List<CharacterI> charsToCopy) {
    if (charsToCopy.isEmpty()) {
      log().error("No chars selected to make copy of");
      return new RowInterval(-1,-1); // ex?
    }
    EditManager.inst().copyChars(charsToCopy); // edits model
    //CompoundTransaction ct = CompoundTransaction.makeCopyTrans(charsToCopy);
    //ct.editModel(); // clones & adds char to char list
    fireTableRowsInserted(getRowCount(),getRowCount()); // updates table view
    int rowEnd = getRowCount() - 1; // -1 -> 0 based
    int rowStart = rowEnd - charsToCopy.size() + 1; // +1 inclusive
    return new RowInterval(rowStart,rowEnd); // fix this - return int[]!
  }

//   /** Returns row # of row inserted */
//   private int addCharacter(CharacterI character) {
//     //++rowCount;
//     // changes data model (fire event?) add transaction for undo! edit man?
//     characterList.add(character);
//     fireTableRowsInserted(getRowCount(),getRowCount());
//     return getRowCount() -1;
//   }

//   void deleteSelectedRow(int deleteRow) {
//     if (!hasRows())
//       return; // err msg?
//     // This needs to make a delete transaction for undo! edit man?
//     characterList.remove(deleteRow);
//     fireTableRowsDeleted(deleteRow,deleteRow);
//   }

  void deleteChars(List<CharacterI> chars) {
    if (chars.isEmpty()) {
      log().error("No rows/chars to delete");
      return;
    }
    EditManager.inst().deleteChars(chars);
    fireTableRowsDeleted(0,getRowCount()); // could be more savvy
  }


  public Object getValueAt(int row, int col) {
    CharacterI chr = getCharacter(row);
    if (chr == null) {
      log().error("character is null for row "+row+" in table");
      return null;
    }
//    if (config.getCharFieldEnum(col) == null) {
    // for now - eventually get from char not config
//     if (!config.hasCharField(col)) {
//       log().error("column "+col+" not configured properly in "+
//                          "character table, cant retrieve value ");
//       return null;
//     }
    //return config.getCharFieldEnum(col).getValue(chr).getName();
    // CF = chr.getCharField(col); - yes! hard w hardwired optional model items
    // return chr.getValue(col).getName(); // ???
    try {
      CharField cf = OntologyManager.inst().getCharField(col);
      if (!chr.hasValue(cf))
        return null;
      return chr.getValue(cf).getName();
    } 
    catch (Exception e) {
      log().error("column "+col+" not configured properly in "+
                         "character table, cant retrieve value ");
      e.printStackTrace();
      return null;
    }
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
  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }

}

class RowInterval {
  RowInterval(int start,int end) { startRow = start; endRow = end; }
  int startRow;
  int endRow;
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


