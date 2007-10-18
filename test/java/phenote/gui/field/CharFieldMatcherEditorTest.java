package phenote.gui.field;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.LoadSaveManager;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.main.Phenote;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;

public class CharFieldMatcherEditorTest {
  
  private FilterList<CharacterI> filteredList;
  private CharFieldMatcherEditor matcherEditor;
  
  @BeforeClass public static void initialize() throws ConfigException {
    Phenote.resetAllSingletons();
    Config.inst().setConfigFile("test.cfg");
    Phenote phenote = Phenote.getPhenote();
    phenote.initOntologies();
  }
  
  @Before public void setup() {
    this.matcherEditor = new CharFieldMatcherEditor(CharFieldManager.inst().getCharFieldList());
    this.filteredList = new FilterList<CharacterI>(this.getTestData(), matcherEditor);
  }
  
  @Test public void getComponent() throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        Assert.assertNotNull("Gui component should not be null", matcherEditor.getComponent());
      }
    });
  }
  
  @Test public void testFilter() throws CharFieldException, InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(new Runnable() {      
      public void run() {
        try {
          matcherEditor.setEditedCharField(CharFieldManager.inst().getCharFieldForName("Publication"));
          matcherEditor.setFilter("wayne", this);
          Assert.assertTrue("No characters should match", filteredList.isEmpty());
          matcherEditor.setFilter("har", this);
          Assert.assertEquals("Two characters should match", 2, filteredList.size());
          matcherEditor.setEditedCharField(CharFieldManager.inst().getCharFieldForName("Entity"));
          matcherEditor.setExactFilterMode();
          matcherEditor.setFilter("TAO:0000108", this);
          Assert.assertEquals("One character should match", 1, filteredList.size());
          matcherEditor.setInheritFilterMode();
          Assert.assertEquals("Three characters should match", 3, filteredList.size());
        } catch (CharFieldException e) {
          Assert.fail(e.getMessage());
        }
      }
    });
  }
  
  private EventList<CharacterI> getTestData() {
    final File f = new File("test/testfiles/CharFieldMatcherEditorTest.tab");
    final CharacterListManager charManager = new CharacterListManager();
    new LoadSaveManager(charManager).loadData(f);
    return charManager.getCharacterList().getList();
  }
}
