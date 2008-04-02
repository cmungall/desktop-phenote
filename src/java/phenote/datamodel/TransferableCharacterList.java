package phenote.datamodel;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TransferableCharacterList implements Transferable {
  
  public static final DataFlavor CHARACTER_LIST_FLAVOR = new DataFlavor(Collection.class, "List of characters");
  private static final DataFlavor[] SUPPORTED_FLAVORS = {CHARACTER_LIST_FLAVOR};
  private final List<CharacterI> characters;

  public TransferableCharacterList(List<CharacterI> theCharacters) {
    this.characters = theCharacters;
  }

  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    if (flavor.equals(CHARACTER_LIST_FLAVOR)) {
      return this.characters;
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
  }

  public DataFlavor[] getTransferDataFlavors() {
    return SUPPORTED_FLAVORS;
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return Arrays.asList(SUPPORTED_FLAVORS).contains(flavor);
  }

}
