package phenote.gui;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.dataadapter.CharacterListManager;
import phenote.main.Phenote;

public class CharacterTableControllerTest {
  
  private CharacterTableController controller;
  
  @BeforeClass public static void initialize() throws ConfigException {
    Phenote.resetAllSingletons();
    Config.inst().setConfigFile("test.cfg");
    Phenote phenote = Phenote.getPhenote();
    phenote.initOntologies();
    phenote.initGui();
  }
  
  @Before public void setup() throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        controller = new CharacterTableController(null);
        CharacterListManager.main().getCharacterList().clear();
      }
    });
  }
  
  @Test public void addNewCharacter() throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        controller.addNewCharacter();
        Assert.assertEquals("Should be one character", 1, CharacterListManager.main().getCharList().size());
      }
    });
  }
  
  @Test public void deleteSelectedCharacters() throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        controller.addNewCharacter();
        controller.addNewCharacter();
        Assert.assertEquals("Should be two characters", 2, CharacterListManager.main().getCharList().size());
        controller.getSelectionModel().setSelectionInterval(0, 1);
        Assert.assertEquals("Should be two characters selected", 2, controller.getSelectionModel().getSelected().size());
        controller.deleteSelectedCharacters();
        Assert.assertEquals("Should be one character after automatic insertion of blank", 1, CharacterListManager.main().getCharList().size());
      }
    });
  }
  
  @Test public void duplicateSelectedCharacters() throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        controller.addNewCharacter();
        Assert.assertEquals("Should be one character", 1, CharacterListManager.main().getCharList().size());
        controller.getSelectionModel().setSelectionInterval(0, 0);
        controller.duplicateSelectedCharacters();
        Assert.assertEquals("Should be two characters", 2, CharacterListManager.main().getCharList().size());
      }
    });
  }
  
  @Test public void getCharacterTablePanel() throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        Assert.assertNotNull("Character table panel should not be null", controller.getCharacterTablePanel());
      }
    });
  }
  
}
