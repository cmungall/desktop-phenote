package phenote.dataadapter.fly;

import java.util.List;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.Toolkit;
import java.io.File;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.dataadapter.DataAdapterI;
import phenote.dataadapter.CharacterListManager;

/** Just pastes characters to clipboard for proforma 
 6/30/06 - changing this to use new Pheno-Syntax - should this be renamed
 Pheno-Syntax data adapter? Pheno Syntax need to be able to write to a file
 as well as also write to flybases proforma app - i think those are 2 different
 data adapters sharing Syntax objects - PhenoSyntaxFileAdapter 
 and FlybaseProformaAdapter or something like that - todo... 
 this is really a serialized clipboard adapter at this point - i think
flybase is going to be more tightly integrated */
public class FlybaseDataAdapter implements DataAdapterI {

  private static ClipboardOwner clipboardOwner = new PhenoteClipboardOwner();

  /** no adapter value (ie filename)  to set for clipboard - no op */
  public void setAdapterValue(String filename) {}

  public void commit(CharacterListI charList) {
    System.out.println("copying character list to clipboard");

    FlyCharList fcl = new FlyCharList(charList);
    //Transferable tr = new CharacterListTransferable(charList);
    Transferable tr = new FlyCharListTransferable(fcl);

    getClipboard().setContents(tr,clipboardOwner);
    
  }
  
  public void commit(CharacterListI charList, File f) {
    commit(charList);
  }
  
  public CharacterListI load(File f) {
    // this is pretty much a dummy implementation
    load();
    return null;
  }

  /** load character set from clipboard if there is one */
  public void load() {
    System.out.println("loading CharacterList from clipboard if there");

    DataFlavor charListFlavor = FlyCharListTransferable.getCharListDataFlavor();
    try {
      Object o = getClipboard().getData(charListFlavor);
      if (o == null) {
        print("No CharacterLists in clipboard");
        return;
      }
      if (!(o instanceof FlyCharList)) { // shouldnt happen due to flavor but just in case
        print("Failure: clipboard transferrable is not a FlyCharList "+o);
        return;
      }
      FlyCharList flyCharList = (FlyCharList)o;
      CharacterListI charList = flyCharList.getCharacterList();
      getCharListManager().setCharacterList(this,charList);
    }
    catch (Exception e) {
      print("Failed to get character list from clipboard: "+e);
    }
    
  }

  private CharacterListManager getCharListManager() { return CharacterListManager.inst(); }

  private Clipboard getClipboard() {
    Toolkit t = Toolkit.getDefaultToolkit();
    return t.getSystemClipboard();
  }

  public static ClipboardOwner getClipboardOwner() {
    return clipboardOwner;
  }
  
  public List<String> getExtensions() {
    return null;
  }
  
  public String getDescription() {
    return "FlyBase adapter";
  }


  private static class PhenoteClipboardOwner implements ClipboardOwner {
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
      // ownership has changed - should check if proforma has plopped something...
    }
  }

  private void print(String m) { System.out.println(m); }
}
