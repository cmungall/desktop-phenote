package phenote.gui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import phenote.config.Config;
import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharField;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.datamodel.OntologyManager;
import phenote.edit.EditManager;

/** 
 *  table model for table of characters. currently holds list of characters - this should
 be moved to datamodel - CharacterList object? CharacterSession? Phenotype?
 */
class CharacterTableModel extends AbstractTableModel implements SortableTableModel {

  //private int rowCount = 0;
  // todo - get this from data adapter/model - especially for loaded
  private CharacterListI characterList;// = new CharacterList();
  private CharacterListManager characterListManager;
  private EditManager editManager;
  private String group = "default";
  //private Config config = Config.inst(); cant cache - may change!

  CharacterTableModel(String group, CharacterListManager clManager,
                      EditManager eManager) {
    if (group != null) this.group = group;
    this.characterList = clManager.getCharacterList();
    this.characterListManager = clManager;
    this.editManager = eManager;
    // should supress editing model for this i think???
    addInitialBlankRow(); // add blank row to start off
    //getCharListManager().addCharListChangeListener(new TableCharListChangeListener());
    // --> panel
    //EditManager.inst().addCharChangeListener(new TableCharChangeListener());
  }
  
//   public CharacterTableModel() {
//     this(null,CharacterListManager.inst(), EditManager.inst());
//   }

  private Config cfg() { return Config.inst(); }

  private CharacterListManager getCharListManager() {
    return this.characterListManager;
    }

  CharacterI getCharacter(int i) {
    // check out of bounds
    if (characterList == null) return null; // ex?
    return characterList.get(i);
  }

  /** returns -1 if not found */
  int indexOf(CharacterI ch) {
    return getCharacterList().indexOf(ch);
  }

  // this should come from datamodel not here - refactor
  CharacterListI getCharacterList() {
    return getCharListManager().getCharacterList();
  }

  void setCharacterList(CharacterListI charList) {
    characterList = charList;
    fireTableDataChanged();
  }

  public String getColumnName(int col) {
    return cfg().getFieldLabel(col,group);
  }
  
  public int getColwidth(int col) {
  	return cfg().getFieldColwidth(col);
  }

  public int getColumnCount() {
    return cfg().getEnbldFieldsNum(group);
  }

  public int getRowCount() {
    //return rowCount;
    if (getCharacterList() == null) return 1; // or 0?? always have 1 blank row?
    return getCharacterList().size();
  }

  boolean hasRows() { return getRowCount() > 0; }

  void addInitialBlankRow() {
    this.editManager.addInitialCharacter();
    fireTableRowsInserted(getRowCount(),getRowCount());
  }

  /** Returns row # of new row - handy for selection */
  int addNewBlankRow() {
    this.editManager.addNewCharacter();
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
    
  /** return RowInterval of all new rows to be selected? yes i think so
   the caller (CharTablePanel) wants to know what rows were created
   (so it can selection them) */
  RowInterval copyChars(List<CharacterI> charsToCopy) {
    if (charsToCopy.isEmpty()) {
      log().error("No chars selected to make copy of");
      return new RowInterval(-1,-1); // ex?
    }
    this.editManager.copyChars(charsToCopy); // edits model
    //CompoundTransaction ct = CompoundTransaction.makeCopyTrans(charsToCopy);
    //ct.editModel(); // clones & adds char to char list
    fireTableRowsInserted(getRowCount(),getRowCount()); // updates table view
    int rowEnd = getRowCount() - 1; // -1 -> 0 based
    int rowStart = rowEnd - charsToCopy.size() + 1; // +1 inclusive
    return new RowInterval(rowStart,rowEnd); 
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
    this.editManager.deleteChars(chars);
    fireTableRowsDeleted(0,getRowCount()); // could be more savvy
  }


  public Object getValueAt(int row, int col) {
    CharacterI chr = getCharacter(row);
    if (chr == null) {
      log().error("character is null for row "+row+" in table");
      return null;
    }
    try {
      CharField cf = OntologyManager.inst().getCharField(col,group);
      if (!chr.hasValue(cf))
        return null;
      return chr.getValue(cf);
    } 
    catch (Exception e) {
      log().error("column "+col+" not configured properly in "+
                         "character table, cant retrieve value ");
      e.printStackTrace();
      return null;
    }
  }
    
  public boolean isCellEditable(int r, int c) {
    return false;
  }

  /** hmmmm... do we sort data or some view of data, view is probably cleaner
      but for now just sort data itself? dont yet have separate view objects to
      sort */
  public void sortOnColumn(int column) {
    CharField cf = cfg().getEnbldCharField(column);
    characterList.sortBy(cf);
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

//   private CharFieldEnum getCharFieldEnum(int col) {
//     return config.getCharFieldEnum(col);
//   }

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
