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
  
  public MatrixController () {
    initializeVars();
  }
  
  public void buildMatrix () throws CharFieldException {
    initializeVars();
    list = (EventList<CharacterI>) clm.getCharList();
    buildRows();
    buildColumns();
  }
  
  public void initializeVars () {
    clm = CharacterListManager.main();
    columnSet = new HashSet<MatrixColumn>();
    columns = new ArrayList<MatrixColumn>();
    rowSet = new HashSet<MatrixRow>();
    rows = new ArrayList<MatrixRow>();
  }
 
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

  public ArrayList<MatrixRow> getRows() {
    return rows;
  }
  
  public ArrayList<MatrixColumn> getColumns() {
    return columns;
  }
}