package phenote.matrix.model;

import java.util.ArrayList;

import org.obo.datamodel.OBOClass;

import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharacterI;
import phenote.datamodel.OboUtil;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;

public class MatrixController {
	
  CharacterListManager clm;
  EventList<CharacterI> list;
  SortedList<CharacterI> sortedTaxa, sortedQuality;
  String columnString, rowString;
  CharacterComparator columnCompare, rowCompare;
  ArrayList<MatrixColumn> columns; 
  ArrayList<MatrixRow> rows;
  
  public MatrixController () {
    clm = CharacterListManager.main();
    columns = new ArrayList<MatrixColumn>();
    rows = new ArrayList<MatrixRow>();
  }
  
  public void buildMatrix () throws CharFieldException {
    list = (EventList<CharacterI>) clm.getCharList();
    buildRows();
    buildColumns();
  }
  
  public void buildRows () throws CharFieldException {
    rowString = "Valid Taxon";
    rowCompare = new CharacterComparator (rowString);
    sortedTaxa = new SortedList<CharacterI>(list, rowCompare);
    String currGroup, prevGroup = "";
    for (CharacterI ch : sortedTaxa) {
      currGroup = ch.getValue(ch.getCharFieldForName(rowString)).getName();
      if (!currGroup.equals(prevGroup)) {
        OBOClass currTaxa = ch.getTerm(rowString);
        rows.add(new PhenotypeMatrixRow(currTaxa));
        prevGroup = currGroup;
      }
    }
  }

  public void buildColumns () {
    for (CharacterI ch : list) {
      // Build the matrix column from the current Character in the list
      OBOClass currEntity, currQuality, currEntity2;
      try {
        currEntity = ch.getTerm("Entity");
      } catch (CharFieldException e) {
        currEntity = null;
      }
      try {
        currQuality = OboUtil.getAttributeForValue(ch.getTerm("Quality"));
      } catch (CharFieldException e) {
        currQuality = null;
      }
      try {
        currEntity2 = ch.getTerm("E2");
      } catch (CharFieldException e) {
        currEntity2 = null;
      }
      PhenotypeMatrixColumn newColumn = new PhenotypeMatrixColumn(currEntity, currQuality, currEntity2);
      System.out.println("Created a new candidate column");
      // Now that we have the new column formed, check to see if this column is already present in the list of columns
      boolean newColNeeded = true;
      for (MatrixColumn mc : columns) {
        if (newColumn.hashCode() == mc.hashCode())
          newColNeeded = false;
      }
      // If the column didn't show up, then newColNeeded will still be true, so we need to added to our list of columns
      if (newColNeeded) {
        System.out.println("Created a new ACTUAL column");
        columns.add(newColumn);
      }
    }
  }
  
//  public void buildColumns () {
//    columnString = "Quality";
//    columnCompare = new CharacterComparator (columnString);
//    sortedQuality = new SortedList<CharacterI>(list, columnCompare);
//    String currGroup, prevGroup = "";
//    OBOClass currEntity, currQuality, currEntity2;
//    int i = 0;
//    for (CharacterI ch : sortedQuality) {
//      try {
//        currGroup = ch.getValue(ch.getCharFieldForName(columnString)).getName();
//      } catch (CharFieldException e1) {
//        currGroup = prevGroup;
//      }
//      if (!currGroup.equals(prevGroup)) {
//        try {
//          currEntity = ch.getTerm("Entity");
//        } catch (CharFieldException e) {
//          currEntity = null;
//        }
//        try {
//          currQuality = OboUtil.getAttributeForValue(ch.getTerm("Quality"));
//        } catch (CharFieldException e) {
//          currQuality = null;
//        }
//        try {
//          currEntity2 = ch.getTerm("E2");
//        } catch (CharFieldException e) {
//          currEntity2 = null;
//        }
//        columns.add(new PhenotypeMatrixColumn(currEntity, currQuality, null)); // change back to currEntity2
//        i++;
//        prevGroup = currGroup;
//      }
//    }
//  }
  
  public Object getValueAt(int rowIndex, int colIndex) {
    MatrixRow row = rows.get(rowIndex);
    MatrixColumn column = columns.get(colIndex);
    for (CharacterI ch : list) {
     if (row.isValue(ch) && column.isValue(ch)) {
       System.out.println ("getValueAt: " + column.getValue(ch).toString());
       return column.getValue(ch);
     }
    }
    return null;
  }
  
  public int getNumRows() {
    return rows.size();
  }
  
  public int getNumColumns() {
    return columns.size();
  }
}