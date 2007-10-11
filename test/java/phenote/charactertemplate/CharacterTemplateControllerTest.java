package phenote.charactertemplate;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharacterI;
import phenote.datamodel.TermNotFoundException;
import phenote.main.Phenote;

public class CharacterTemplateControllerTest {
  
  private CharacterTemplateController controller;
  
  @BeforeClass public static void initialize() throws ConfigException {
    Phenote.resetAllSingletons();
    Config.inst().setConfigFile("phenomap.cfg");
    Phenote phenote = Phenote.getPhenote();
    phenote.initOntologies();
    phenote.initGui();
  }
  
  @Before public void setup() throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        controller = new CharacterTemplateController("specimen-list");
      }
    });
  }
  
  @Test public void addNewCharacter() throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        controller.addNewCharacter();
        Assert.assertEquals("Should be one character", 1, controller.getAllCharacters().size());
      }
    });
  }
  
  @Test public void markCharacter() throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        CharacterI character = addAndMarkNewCharacter();
        Assert.assertTrue("Character should be marked", controller.getMarkedCharacters().contains(character));
      }
    });
  }
  
  @Test public void invertMarkedCharacters() throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        CharacterI character = addAndMarkNewCharacter();
        controller.invertMarkedCharacters();
        Assert.assertFalse("No characters should be marked", controller.getMarkedCharacters().contains(character));
      }
    });
  }
  
  @Test public void generateCharacters() throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        try {
          CharacterListManager.main().clear();
          CharacterI template = addAndMarkNewCharacter();
          template.setValue("Textual Description", "testCharacter");
          template.setValue("Quality", "PATO:0000001");
          controller.generateCharacters();
          Assert.assertTrue("Main character list should have one item", CharacterListManager.main().getCharList().size() == 1);
          CharacterI generatedCharacter = CharacterListManager.main().getCharList().get(0);
          Assert.assertEquals("Generated character should have same value as template character", "testCharacter", generatedCharacter.getValueString("Textual Description"));
          Assert.assertEquals("Generated character should have same value as template character", "PATO:0000001", generatedCharacter.getTerm("Quality").getID());
          Assert.assertNotSame("Generated character should be copy of original, not same object", generatedCharacter, template);
        } catch (CharFieldException e) {
          Assert.fail(e.toString());
        } catch (TermNotFoundException e) {
          Assert.fail(e.toString());
        }
      }
    });
  }
  
  private CharacterI addAndMarkNewCharacter() {
    controller.addNewCharacter();
    CharacterI character = controller.getAllCharacters().get(0);
    controller.setCharacterIsMarked(character, true);
    return character;
  }

}
