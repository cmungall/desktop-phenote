package phenote.dataadapter.fly;

// is it funny to have awt stuff in dataadapter?
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import phenote.datamodel.CharacterListI;

// --> FlyCharListTransferable
public class CharacterListTransferable implements Transferable {

  private static DataFlavor charListDataFlavor = new CharacterListDataFlavor();
  private static DataFlavor[] flavors = new DataFlavor[] {charListDataFlavor};

  private CharacterListI characterList;

  CharacterListTransferable(CharacterListI cl) {
    characterList = cl;
  }

  /** Transferable */
  public Object getTransferData(DataFlavor flavor) {
    if (flavor == charListDataFlavor)
      return characterList;
    return null; // ?
  }
  public DataFlavor[] getTransferDataFlavors() {
    return flavors; // ?
  }
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return flavor == charListDataFlavor;
  }

  public static DataFlavor getCharListDataFlavor() {
    return charListDataFlavor;
  }

  private static class CharacterListDataFlavor extends DataFlavor {
    private CharacterListDataFlavor() {
      super(CharacterListI.class,"Character List"); 
    }
  }


}
