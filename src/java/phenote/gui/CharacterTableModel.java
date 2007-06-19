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
  //private Config config = Config.inst(); cant cache - may change!

  CharacterTableModel() {
    characterList = getCharListManager().getCharacterList();
    // should supress editing model for this i think???
    addInitialBlankRow(); // add blank row to start off
    //getCharListManager().addCharListChangeListener(new TableCharListChangeListener());
    // --> panel
    //EditManager.inst().addCharChangeListener(new TableCharChangeListener());
  }

  private Config cfg() { return Config.inst(); }

  private CharacterListManager getCharListManager() { return CharacterListManager.inst(); }

  CharacterI getCharacter(int i) {
    // check out of bounds
    if (characterList == null) return null; // ex?
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
    return cfg().getFieldLabel(col);
  }
  
  public int getColwidth(int col) {
  	return cfg().getFieldColwidth(col);
  }

  public int getColumnCount() {
    return cfg().getEnbldFieldsNum();
  }

  public int getRowCount() {
    //return rowCount;
    if (getCharacterList() == null) return 1; // or 0?? always have 1 blank row?
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
//    if (cfg().getCharFieldEnum(col) == null) {
    // for now - eventually get from char not config
//     if (!cfg().hasCharField(col)) {
//       log().error("column "+col+" not configured properly in "+
//                          "character table, cant retrieve value ");
//       return null;
//     }
    //return cfg().getCharFieldEnum(col).getValue(chr).getName();
    // CF = chr.getCharField(col); - yes! hard w hardwired optional model items
    // return chr.getValue(col).getName(); // ???
    try {
      CharField cf = OntologyManager.inst().getCharField(col);
      if (!chr.hasValue(cf))
        return null;
      return chr.getValue(cf);
      //      return chr.getValue(cf).getName();
    } 
    catch (Exception e) {
      log().error("column "+col+" not configured properly in "+
                         "character table, cant retrieve value ");
      e.printStackTrace();
      return null;
    }
  }

//   private CharFieldEnum getCharFieldEnum(int col) {
//     return config.getCharFieldEnum(col);
//   }

    
  public boolean isCellEditable(int r, int c) {
    return false;
  }

  /** hmmmm... do we sort data or some view of data, view is probably cleaner
      but for now just sort data itself? dont yet have separate view objects to
      sort */
  void setSortKey(int col) {
    CharField cf = cfg().getEnbldCharField(col);
    characterList.sortBy(cf); // ???
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

