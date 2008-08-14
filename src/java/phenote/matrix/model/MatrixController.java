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
	
  CharacterListManager clm;
  EventList<CharacterI> list;
  Set<MatrixColumn> columnSet;
  Set<MatrixRow> rowSet;
  ArrayList<MatrixColumn> columns; 
  private ArrayList<MatrixRow> rows;
  
  public MatrixController () {
    initializeVars();
  }
  
  public void buildMatrix () throws CharFieldException {
    initializeVars();
    list = (EventList<CharacterI>) clm.getCharList();
    buildRows();
    buildColumns();
  }
  
  public void buildRows () throws CharFieldException {
    for (CharacterI ch : list) {
      OBOClass currTaxa;
      currTaxa = ch.getTerm("GC");
      PhenotypeMatrixRow pmr = new PhenotypeMatrixRow (currTaxa);
      rowSet.add (pmr);
    }
    for (MatrixRow mr : rowSet) {
      rows.add(mr);
    }
  }

  public void buildColumns () {
    for (CharacterI ch : list) {
      // Build the matrix column from the current Character in the list
      OBOClass currEntity, currQuality, currEntity2;
      try {
        currEntity = ch.getTerm("E");
      } catch (CharFieldException e) {
        currEntity = null;
      }
      currQuality = OboUtil.getAttributeForValue(ch.getQuality());
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
    
  public Object getValueAt(int rowIndex, int colIndex) {
    MatrixRow row = rows.get(rowIndex);
    MatrixColumn column = columns.get(colIndex);
    for (CharacterI ch : list) {
     if (row.isValue(ch) && column.isValue(ch)) {
       // This println statement only happens for the character that has an entity2 - why?
       System.out.println("Found a match for " + rowIndex + ", " + colIndex + ".");
       return column.getValue(ch);
     }
    }
    // This gets returned WAY too often - why?
    return null;
  }
  
  public int getNumRows() {
    return rows.size();
  }
  
  public int getNumColumns() {
    return columns.size();
  }
  
  public String getColumnHeader (int index) {
    PhenotypeMatrixColumn pmc = (PhenotypeMatrixColumn) columns.get(index);
    return pmc.getEntity().getName();
  }
  
  public void initializeVars () {
    clm = CharacterListManager.main();
    columnSet = new HashSet<MatrixColumn>();
    columns = new ArrayList<MatrixColumn>();
    rowSet = new HashSet<MatrixRow>();
    rows = new ArrayList<MatrixRow>();
  }

  public ArrayList<MatrixRow> getRows() {
    return rows;
  }
}