package phenote.gui.field;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.edit.EditManager;
import phenote.gui.CharacterTableController;
import phenote.gui.selection.SelectionManager;
import phenote.main.Phenote;
import ca.odell.glazedlists.swing.EventSelectionModel;

public class FreeTextFieldTest {

  @BeforeClass public static void initialize() throws ConfigException {
    Phenote.resetAllSingletons();
    Config.inst().setConfigFile("test.cfg");
    Phenote phenote = Phenote.getPhenote();
    phenote.initOntologies();
  }
  
  @Before public void setup() {
    
  }
  
  @Test public void testCommitOnRowSelectionChange() throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(new Runnable() {      
      public void run() {
        try {
          final String textInput = "pub";
          final CharacterTableController characterTable = CharacterTableController.getDefaultController();
          final EventSelectionModel<CharacterI> selectionModel = characterTable.getSelectionModel();
          final CharField charField = CharFieldManager.inst().getCharFieldForName("Publication");
          final CharFieldGui fieldGui = CharFieldGui.makeCharFieldGui(charField, 0);
          fieldGui.setSelectionManager(SelectionManager.inst());
          fieldGui.setListSelectionModel(selectionModel);
          fieldGui.setEditManager(EditManager.inst());
          characterTable.addNewCharacter();
          selectionModel.setSelectionInterval(0, 0);
          //must set text directly through JTextField so that FreeTextField thinks it's coming from user
          ((JTextField)(fieldGui.getUserInputGui())).requestFocus();
          ((JTextField)(fieldGui.getUserInputGui())).setText(textInput);
          selectionModel.setSelectionInterval(1, 1);
          fieldGui.focusLost();
          final List<CharacterI> characters = CharacterListManager.main().getCharList();
          Assert.assertEquals("Value should have been committed when selection changed", characters.get(0).getValueString(charField), textInput);
        } catch (CharFieldException e) {
          Assert.fail(e.getMessage());
        }
      }
    });
  }
  
}
