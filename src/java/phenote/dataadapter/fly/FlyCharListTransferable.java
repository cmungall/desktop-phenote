package phenote.dataadapter.fly;

// is it funny to have awt stuff in dataadapter?
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

//import phenote.datamodel.CharacterListI;

public class FlyCharListTransferable implements Transferable {

  private static DataFlavor charListDataFlavor = new FlyCharListDataFlavor();
  private static DataFlavor[] flavors = new DataFlavor[] {charListDataFlavor};

  private FlyCharList flyCharList;

  public FlyCharListTransferable(FlyCharList cl) {
    flyCharList = cl;
  }

  /** Transferable */
  public Object getTransferData(DataFlavor flavor) {
    if (flavor == charListDataFlavor)
      return flyCharList;
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

  private static class FlyCharListDataFlavor extends DataFlavor {
    private FlyCharListDataFlavor() {
      super(FlyCharList.class,"Fly Character List"); 
    }
  }


}
