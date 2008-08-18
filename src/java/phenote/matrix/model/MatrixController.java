package phenote.matrix.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.obo.datamodel.OBOClass;
import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharacterI;
import phenote.datamodel.OboUtil;
import ca.odell.glazedlists.EventList;

public class MatrixController {
	
  private CharacterListManager clm;
  private EventList<CharacterI> list;
  private Set<MatrixColumn> columnSet;
  private Set<MatrixRow> rowSet;
  private ArrayList<MatrixColumn> columns; 
  private ArrayList<MatrixRow> rows;
  
  /** 
   * Creates a new matrix controller
   */
  public MatrixController () {
    initializeVars();
  }
  
  /** 
   * Creates new instances of all instance variables
   */
  public void initializeVars () {
    clm = CharacterListManager.main();
    columnSet = new HashSet<MatrixColumn>();
    columns = new ArrayList<MatrixColumn>();
    rowSet = new HashSet<MatrixRow>();
    rows = new ArrayList<MatrixRow>();
  }

  /** 
   * Gets the current list of characters and calls methods to build the rows and columns of the matrix
   */
  public void buildMatrix () throws CharFieldException {
    initializeVars();
    list = (EventList<CharacterI>) clm.getCharList();
    buildRows();
    buildColumns();
  }
  
  /** 
   * Traverses the character list and extracts the valid taxon from each one.
   * Each taxon is made into a PhenotypeMatrixRow object, and that object is
   * put into a Set, assuring that there will be no duplicates. Once the set is 
   * complete, each element of the set is added to an ArrayList.
   * (This was done to maintain compatibility with other parts of the code that 
   * expected the rows to be stored in an ArrayList; it probably could be 
   * refactored to use the Set and eliminate the ArrayList entirely.)
   */
  public void buildRows () {
    for (CharacterI ch : list) {
      OBOClass currTaxa;
      try {
        currTaxa = ch.getTerm("GC");
      } catch (CharFieldException e ) {
        currTaxa = null;
      }
      catch (NullPointerException e) {
        currTaxa = null;
      }
      PhenotypeMatrixRow pmr = new PhenotypeMatrixRow (currTaxa);
      rowSet.add (pmr);
    }
    for (MatrixRow mr : rowSet) {
      rows.add(mr);
    }
  }

  /** 
   * Traverses the character list and extracts the entity, quality, and additional entity from each one.
   * The resulting tuple is made into a PhenotypeMatrixColumn object, and that object is
   * put into a Set, assuring that there will be no duplicates. Once the set is 
   * complete, each element of the set is added to an ArrayList.
   * (This was done to maintain compatibility with other parts of the code that 
   * expected the columns to be stored in an ArrayList; it probably could be refactored
   * to use the Set and eliminate the ArrayList entirely.)
   */
  public void buildColumns () {
    for (CharacterI ch : list) {
      OBOClass currEntity, currQuality, currEntity2;
      try {
        currEntity = ch.getTerm("E");
      } catch (CharFieldException e) {
        currEntity = null;
      }
      try{
        currQuality = OboUtil.getAttributeForValue(ch.getQuality());
      }
      catch (NullPointerException e) {
        currQuality = null;
      }
      try {
        currEntity2 = ch.getTerm("E2");
      } catch (CharFieldException e) {
        currEntity2 = null;
      }
      PhenotypeMatrixColumn newColumn = new PhenotypeMatrixColumn(currEntity, currQuality, currEntity2);
      columnSet.add(newColumn);
    }
    for (MatrixColumn mc : columnSet) {
      columns.add(mc);
    }
  }
  
  /** 
   * This method iterates through each character in the main data list and checks to see if it is a valid character for
   * the given row and given column in the matrix. Once it finds a match, it uses the getValue() method from 
   * PhenotypeMatrixColumn class to create a new PhenotypeMatrixCell object, and returns it.
   * 
   * @param rowIndex the number of the row being requested
   * @param colIndex the number of the column being requested
   * @return an Object (actually the PhenotypeMatrixCell) representing the contents of the matrix at the requested location 
   */
  public Object getValueAt(int rowIndex, int colIndex) {
    MatrixRow row = rows.get(rowIndex);
    MatrixColumn column = columns.get(colIndex);
    for (CharacterI ch : list) {
     if (row.isValue(ch)) {
       if (column.isValue(ch)) {
         return column.getValue(ch);         
       }
     }
    }
    return null;
  }

  /** 
   * @param index the index of the column whose header is being requested
   * @return a String representing the name of the requested column
   */
  public String getColumnHeader (int index) {
    PhenotypeMatrixColumn pmc = (PhenotypeMatrixColumn) columns.get(index);
    return pmc.getEntity().getName();
  }
  
  /** 
   * @return the number of rows in the current matrix
   */
  public int getNumRows() {
    return rows.size();
  }

  /** 
   * @return the number of columns in the current matrix
   */
  public int getNumColumns() {
    return columns.size();
  }

  /** 
   * @return an ArrayList describing the rows of the matrix
   */
  public ArrayList<MatrixRow> getRows() {
    return rows;
  }
  
  /** 
   * @return an ArrayList describing the columns of the matrix
   */
  public ArrayList<MatrixColumn> getColumns() {
    return columns;
  }
}