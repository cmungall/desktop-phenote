package phenote.gui;

// move to main package??

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.fly.FlyCharList;
import phenote.dataadapter.fly.FlyCharListTransferable;
import phenote.dataadapter.fly.FlyCharacter;
import phenote.dataadapter.fly.FlybaseDataAdapter;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.gui.field.AbstractAutoCompList;
import phenote.gui.field.FieldPanel;
import phenote.main.Phenote;
import phenote.util.HtmlUtil;

// making same package as phenotes giving us access to package methods!

// dont need to subclass TestCase - does tests thgough annotations (1.5)
public class TestPhenote {

  // dont have handle on instance - have to do statics - wierd
  private static Phenote phenote;
  private static FieldPanel fieldPanel;
  private static AbstractAutoCompList entityComboBox;
  private static AbstractAutoCompList qualityComboBox;
  private static TermInfo2 termInfo;
  private static CharacterTableController tableController;

  /** @throws InvocationTargetException 
   * @throws InterruptedException 
   * @throws ConfigException 
   * @BeforeClass says to run this once before all the tests */
  @BeforeClass public static void init() throws InterruptedException, InvocationTargetException, ConfigException {
    Phenote.resetAllSingletons();
    Config.inst().setConfigFile("test.cfg");
    phenote = Phenote.getPhenote();
    phenote.initOntologies();
    phenote.initGui();
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        fieldPanel = phenote.getFieldPanel();
        entityComboBox = fieldPanel.getEntityComboBox();
        entityComboBox.setTestMode(true); // turns off popup, hanging bug only in test
        qualityComboBox = fieldPanel.getQualityComboBox();
        qualityComboBox.setTestMode(true);
        termInfo = phenote.getTermInfo();
        tableController = phenote.getCharacterTableController();
      }
    });
  }
  
//   @Ignore @Test public void displayTermInfoOnCompMouseOverTest() throws InterruptedException, InvocationTargetException {
//     SwingUtilities.invokeAndWait(new Runnable() {
//       public void run() {
//         System.out.println("Testing comp list mouse over term info...");
//         // "he" should have plenty of completion terms associated, heart, head...
//         boolean doCompletion = true;
//         System.out.println("set entity text to hea");
//         //entityComboBox.setText("hea",doCompletion);
//         // already have list from l from previous test - for some reason 2nd l doesnt jibe
//         // even though there are lots of quality terms with 'll' ???
//         //qualityComboBox.simulateLKeyStroke(); // just does 'l'
//         System.out.println("set text - getting 3rd term");
//         //JList entityJList = entityComboBox.getJList();
//         // pick 3rd item
//         //String thirdTerm = (String)entityJList.getModel().getElementAt(2);
//         //String thirdTerm = getEntityThirdAutoTerm();
//         String thirdTerm = getQualityThirdAutoTerm();
//         assertNotNull("3rd term from quality combo shouldnt be null",thirdTerm);
//         //entityJList.setSelectedIndex(2);
//         //entityComboBox.doMouseOver(2); // 2 is 3rd - 0 indexing
//         qualityComboBox.doMouseOver(2);

//         //String info = termInfo.getText();
//         // gets text in term field part where name is displayed
//         String info = termInfo.getTermNameText();
//         //String properInfoPrefix = "Term: "+thirdTerm;
//         // this doesnt work anymore with mtml stuff in there...
//         //boolean isInfoProper = info.startsWith(properInfoPrefix); 
//         boolean isInfoProper = info.contains(thirdTerm);

//         String msg = "term info should contain '"+thirdTerm
//           +"' not getting mouseover but its ["+info+"]";
//         assertTrue(msg,isInfoProper);
//         System.out.println("Completion mouse over term info test succeeded!");
//       }
//     });
    
//   }

  private String getQualityThirdAutoTerm() {
    assertNotNull(qualityComboBox.getJComboBox().getModel());
    assertNotNull("3rd term from quality combo shouldnt be null",
                  qualityComboBox.getJComboBox().getModel().getElementAt(2));
    return qualityComboBox.getJComboBox().getModel().getElementAt(2).toString();
  }

  private String getQualityTerm(int index) {
    assertNotNull(qualityComboBox.getJComboBox().getModel());
    assertNotNull(index+" term from quality combo shouldnt be null",
                  qualityComboBox.getJComboBox().getModel().getElementAt(index));
    return qualityComboBox.getJComboBox().getModel().getElementAt(index).toString();
  }

  /** Selecting item in entity combo box should cause that item to appear in 
      table in entity column 
   * @throws InvocationTargetException 
   * @throws InterruptedException */
  @Ignore @Test public void comboTermSelectionGoesToTableTest() throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        // selecting item should make it go in table...
        tableController.addNewCharacter();
        tableController.getSelectionModel().setSelectionInterval(0,0);
        System.out.println("Selecting 3rd entity item");
        qualityComboBox.getJComboBox().setSelectedIndex(2); // 2 is 3rd
        String selectedQualityTerm = getQualityThirdAutoTerm();
        CharacterI selPheno = tableController.getSelectionModel().getSelected().get(0);
        String tableQuality = selPheno.getQuality().getName(); // oboclass
        assertEquals(selectedQualityTerm,tableQuality);
        System.out.println("term to table test passed, selected quality term "
                           +selectedQualityTerm+" quality in table "+tableQuality+"\n");
      }
    });

  }

  // cant get null pointer to fly - gotta love testing guis
  /** theres a null pointer on selcting item for 1st time, not sure i can replicate
   this is an awful test - assumes lacking physical part is the 2nd indexed
   L term in quality
   ontology - assumes theres a quality ontology 
   * @throws InvocationTargetException 
   * @throws InterruptedException */
  @Ignore @Test public void compListSelectionTest() throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
      //qualityComboBox.setText("larg",true);
        qualityComboBox.simulateLKeyStroke();
        qualityComboBox.getJComboBox().setSelectedIndex(2);
        // this is admittedly presumptious of quality
        assertEquals("lacking physical part",qualityComboBox.getText());
        System.out.println("comp list sel ok "+qualityComboBox.getText());
      }
    });
  }
  
  /** After term selected in comp list popup should not come up - this doesnt actually
      test this - the popup does go away with setSelInd - only with mouse click it
      sometimes doesnt - need simulated mouse click! 
   * @throws InvocationTargetException 
   * @throws InterruptedException */
  @Ignore @Test public void selectionPopupTest() throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        qualityComboBox.setText("larg",true);
        qualityComboBox.getJComboBox().setSelectedIndex(2);
        assertFalse(qualityComboBox.getJComboBox().isPopupVisible());
      }
    });
    
  }

  /** with searching on synonyms hit bug where terms come in more than once if have
      2 syns 
   * @throws InvocationTargetException 
   * @throws InterruptedException */
  @Ignore @Test public void synonymDupTest() throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        qualityComboBox.setText("");
        // searchParamPanel not used anymore - need to set in menu
        //searchParamPanel.setTermSearch(false);
        //searchParamPanel.setSynonymSearch(true);
        SearchParams.inst().setParam(SearchFilterType.TERM,false);
        SearchParams.inst().setParam(SearchFilterType.SYN,true);
        simulateAQualityKeyStroke();
        String first = getQualityTerm(0);
        String second = getQualityTerm(1);
        assertFalse(first.equals(second));
      }
    });
    
  }

  // utlimatley need to do mouse click on term - how to do that???
  @Test public void termInfoSelectTermTest() throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        simulatePhenoteHyperlinkUpdate();
        String m = "term info hyper link test fail, term info should have urostyle val "
          +" term info: "+termInfo.getTermNameText();
        // how to make this test not so pato specific??
        assertTrue(m,termInfo.getTermNameText().contains("urostyle"));
      }
    });
  }

  private void simulatePhenoteHyperlinkUpdate() {
    HyperlinkEvent.EventType type = HyperlinkEvent.EventType.ACTIVATED;
    // 0000732 -> "BodyToneValue"
    String desc = HtmlUtil.makePhenoIdLink("TAO:0000158");
    HyperlinkEvent e = new HyperlinkEvent(termInfo,type,null,desc);
    termInfo.simulateHyperlinkEvent(e);
  }

  private void simulateAQualityKeyStroke() {
    simulateQualityKeyStroke(KeyEvent.VK_A,'a');
  }

  private void simulateQualityKeyStroke(int keyCode, char c) {
    qualityComboBox.simulateKeyStroke(keyCode,c);
  }
  
  @Ignore @Test public void flyDataAdapterTest() throws UnsupportedFlavorException, IOException {
    CharacterListI cl = CharacterListManager.main().getCharacterList();
    DataFlavor charListFlavor = FlyCharListTransferable.getCharListDataFlavor();
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
