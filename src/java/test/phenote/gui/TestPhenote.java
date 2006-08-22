package phenote.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import javax.swing.JList;
import javax.swing.event.HyperlinkEvent;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull; // wierd
import static org.junit.Assert.assertTrue;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.fly.FlyCharacter;
import phenote.dataadapter.fly.FlyCharList;
import phenote.dataadapter.fly.FlyCharListTransferable;
import phenote.dataadapter.fly.FlybaseDataAdapter;
import phenote.util.HtmlUtil;

// making same package as phenotes giving us access to package methods!

// dont need to subclass TestCase - does tests thgough annotations (1.5)
public class TestPhenote {

  // dont have handle on instance - have to do statics - wierd
  private static Phenote phenote;
  private static TermPanel termPanel;
  private static SearchParamPanel searchParamPanel;
  private static AutoComboBox entityComboBox;
  private static AutoComboBox qualityComboBox;
  private static TermInfo termInfo;
  private static CharacterTablePanel characterTablePanel;

  /** @BeforeClass says to run this once before all the tests */
  @BeforeClass public static void init() {
    System.out.println("Initializing Phenote...");
    Phenote.main(null);
    // so the gui actually needs a little time to do layout on gui thread
    // or else ACB.doComp() showPopup causes a hang - wierd!
//     System.out.println("sleeping...");
//     try {Thread.currentThread().sleep(5000); } // millis
//     catch (InterruptedException e) { System.out.println(e); }
//     System.out.println("^^^done sleeping - is gui ready?");
    phenote = Phenote.getPhenote();
    termPanel = phenote.getTermPanel();
    searchParamPanel = termPanel.getSearchParamPanel();
    entityComboBox = termPanel.getEntityComboBox();
    entityComboBox.setTestMode(true); // turns off popup, hanging bug only in test
    qualityComboBox = termPanel.getQualityComboBox();
    qualityComboBox.setTestMode(true);
    termInfo = phenote.getTermInfo();
    characterTablePanel = phenote.getCharacterTablePanel();
  }

  /** @Test is an annotation defined in Test - Test looks for Test methods */
  @Test public void test() {
    //selectionPopupTest(); not done proper
    compListSelectionTest(); 
    displayTermInfoOnCompMouseOverTest();
    comboTermSelectionGoesToTableTest();
    synonymDupTest();
    termInfoSelectTermTest();
    //attributeInQualityCompletionTest(); --> need 3 keystrokes 'ttr'
    //backspaceInComboBoxTest(); cant sim backspace.....
    //flyDataAdapterTest(); out of date its now doing phenoxml - soon will be syntax
  }

  private void displayTermInfoOnCompMouseOverTest() {
    System.out.println("Testing comp list mouse over term info...");
    // "he" should have plenty of completion terms associated, heart, head...
    boolean doCompletion = true;
    System.out.println("set entity text to hea");
    //entityComboBox.setText("hea",doCompletion);
    // already have list from l from previous test - for some reason 2nd l doesnt jibe
    // even though there are lots of quality terms with 'll' ???
    //qualityComboBox.simulateLKeyStroke(); // just does 'l'
    System.out.println("set text - getting 3rd term");
    //JList entityJList = entityComboBox.getJList();
    // pick 3rd item
    //String thirdTerm = (String)entityJList.getModel().getElementAt(2);
    //String thirdTerm = getEntityThirdAutoTerm();
    String thirdTerm = getQualityThirdAutoTerm();
    assertNotNull("3rd term from quality combo shouldnt be null",thirdTerm);
    //entityJList.setSelectedIndex(2);
    entityComboBox.doMouseOver(2); // 2 is 3rd - 0 indexing

    String info = termInfo.getText();
    //String properInfoPrefix = "Term: "+thirdTerm;
    // this doesnt work anymore with mtml stuff in there...
    //boolean isInfoProper = info.startsWith(properInfoPrefix); 
    boolean isInfoProper = info.contains(thirdTerm);

    String msg = "term info should contain '"+thirdTerm
      +"' not getting mouseover but its ["+info+"]";
    assertTrue(msg,isInfoProper);
    System.out.println("Completion mouse over term info test succeeded!");
  }

  private String getEntityThirdAutoTerm() {
    return entityComboBox.getModel().getElementAt(2).toString();
  }

  private String getQualityThirdAutoTerm() {
    assertNotNull(qualityComboBox.getModel());
    assertNotNull("3rd term from quality combo shouldnt be null",
                  qualityComboBox.getModel().getElementAt(2));
    return qualityComboBox.getModel().getElementAt(2).toString();
  }

  private String getQualityTerm(int index) {
    assertNotNull(qualityComboBox.getModel());
    assertNotNull(index+" term from quality combo shouldnt be null",
                  qualityComboBox.getModel().getElementAt(index));
    return qualityComboBox.getModel().getElementAt(index).toString();
  }

  /** Selecting item in entity combo box should cause that item to appear in 
      table in entity column */
  private void comboTermSelectionGoesToTableTest() {
    // selecting item should make it go in table...
    System.out.println("Selecting 3rd entity item");
    qualityComboBox.setSelectedIndex(2); // 2 is 3rd
    String selectedQualityTerm = getQualityThirdAutoTerm();
    CharacterI selPheno = characterTablePanel.getSelectedCharacter();
    String tableQuality = selPheno.getQuality().getName(); // oboclass
    assertEquals(selectedQualityTerm,tableQuality);
    System.out.println("term to table test passed, selected quality term "
                       +selectedQualityTerm+" quality in table "+tableQuality+"\n");
  }

  /** Test that attributes are being filtered out of quality term completion list */
  private void attributeInQualityCompletionTest() {
    boolean doCompletion = true;
    System.out.println("Testing quality for attribute filtering");
    // need to do this with key strokes now - set text doesnt work
    // need at least 3 key strokes 'ttr' for attributes - i seem to have problems
    // getting more than one key stroke in - hmmmmm
    qualityComboBox.setText("attribute",doCompletion);
    int count = qualityComboBox.getItemCount();
    String m = "Attributes are not being filtered out of Quality completion "+
      "There are "+count+" terms with 'attribute'";
    System.out.println("There are "+count+" attributes in comp list");
    assertTrue(m,count == 0);
  }

  // cant get null pointer to fly - gotta love testing guis
  /** theres a null pointer on selcting item for 1st time, not sure i can replicate*/
  private void compListSelectionTest() {
    //qualityComboBox.setText("larg",true);
    qualityComboBox.simulateLKeyStroke();
    qualityComboBox.setSelectedIndex(2);
    // this is admittedly presumptious of quality
    assertEquals("acute angle",qualityComboBox.getText());
    System.out.println("comp list sel ok "+qualityComboBox.getText());
  }
  
  /** After term selected in comp list popup should not come up - this doesnt actually
      test this - the popup does go away with setSelInd - only with mouse click it
      sometimes doesnt - need simulated mouse click! */
  private void selectionPopupTest() {
    qualityComboBox.setText("larg",true);
    qualityComboBox.setSelectedIndex(2);
    assertFalse(qualityComboBox.isPopupVisible());
  }

  /** with searching on synonyms hit bug where terms come in more than once if have
      2 syns */
  private void synonymDupTest() {
    qualityComboBox.setText("");
    searchParamPanel.setTermSearch(false);
    searchParamPanel.setSynonymSearch(true);
    simulateAQualityKeyStroke();
    String first = getQualityTerm(0);
    String second = getQualityTerm(1);
    assertFalse(first.equals(second));
  }

  // utlimatley need to do mouse click on term - how to do that???
  private void termInfoSelectTermTest() {
    simulatePhenoteHyperlinkUpdate();
    String m = "term info hyper link test fail, term info should have body tone val "
      +" term info: "+termInfo.getText();
    // how to make this test not so pato specific??
    assertTrue(m,termInfo.getText().contains("body tone value"));
  }

  private void simulatePhenoteHyperlinkUpdate() {
    HyperlinkEvent.EventType type = HyperlinkEvent.EventType.ACTIVATED;
    // 0000732 -> "BodyToneValue"
    String desc = HtmlUtil.makePhenoIdLink("PATO:0000732");
    HyperlinkEvent e = new HyperlinkEvent(termInfo,type,null,desc);
    termInfo.simulateHyperlinkEvent(e);
  }

  private void simulateAQualityKeyStroke() {
    simulateQualityKeyStroke(KeyEvent.VK_A,'a');
  }

  private void simulateQualityKeyStroke(int keyCode, char c) {
    qualityComboBox.simulateKeyStroke(keyCode,c);
  }
  
  private void flyDataAdapterTest() {
    CharacterListI cl = characterTablePanel.getCharacterList();
    characterTablePanel.pressCommitButtonTest();
    DataFlavor charListFlavor = FlyCharListTransferable.getCharListDataFlavor();
    try {
      Object o = getClipboard().getData(charListFlavor);
      String m = "Failure: clipboard transferrable is not a FlyCharList "+o;
      assertTrue(m,o instanceof FlyCharList);
      FlyCharList fcl = (FlyCharList)o;
      CharacterListI clipboardCharList = fcl.getCharacterList();
      //assertEquals(clipboardCharList,cl); // its a clone???
      assertTrue(clipboardCharList.equals(cl));

      // simulate proforma - grab strings and recreate char list & shove in clipboard
      // & load it up
      FlyCharList newFlyCharList = new FlyCharList();
      for (FlyCharacter fc : fcl.getFlyCharList()) {
        FlyCharacter fcNew = new FlyCharacter(fc.getGenotype(),fc.getEVString());
        newFlyCharList.addFlyChar(fcNew);
      }
      Transferable tr = new FlyCharListTransferable(newFlyCharList);
      ClipboardOwner clipboardOwner = FlybaseDataAdapter.getClipboardOwner();
      getClipboard().setContents(tr,clipboardOwner);

      MenuManager.inst().getFileMenu().clickLoad();
      CharacterListI newCL = CharacterListManager.inst().getCharacterList();
      //assertEquals(cl,newCL); == NOT .equals()
      assertTrue(cl.equals(newCL));
    }
    catch (Exception e) {
      System.out.println("FAILURE: Exception thrown "+e);
      e.printStackTrace();
    }
  }

//   // put in CharList class?
//   private boolean charListEquals(CharacterListI c1, CharacterListI c2) {
//     if (c1.size() != c2.size()) return false;
//     for (int i=0; i<c1.size(); i++) {
//       if (!c1.get(i).equals(c2,get(i)))
//         return false;
//     }
//     return true;
//   }

  private Clipboard getClipboard() {
    Toolkit t = Toolkit.getDefaultToolkit();
    return t.getSystemClipboard();
  }


  // I cant find a way to simulate a backspace in jcombobox - very frustrating!!
//   /** there is/was a bug where delete/backspace was not triggering a new
//       completion list */
//   private void backspaceInComboBoxTest() {
//     boolean doCompletion = true;
//     qualityComboBox.clear();
//     qualityComboBox.setText("larg",doCompletion);
//     int preBackspaceCount = qualityComboBox.getItemCount();
 
//     // simulate backspace/delete key
//     KeyEvent ke = new KeyEvent(qualityComboBox,KeyEvent.VK_DELETE,Calendar.getInstance().getTimeInMillis(),0,KeyEvent.VK_UNDEFINED,KeyEvent.CHAR_UNDEFINED);
//     //qualityComboBox.processKeyEvent(ke);
//     qualityComboBox.getEditor().getEditorComponent().processKeyEvent(ke);

//     String postDelText = qualityComboBox.getText();
//     int postBackspaceCount = qualityComboBox.getItemCount();
//     System.out.println("post text "+postDelText+" pre count "+preBackspaceCount+" post count "+postBackspaceCount);
//     assertTrue(preBackspaceCount != postBackspaceCount);
//   }

}
