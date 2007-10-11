package phenote.charactertemplate;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.gui.trees.treeviewer.TreePane;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterIFactory;
import phenote.datamodel.TermNotFoundException;
import phenote.main.Phenote;

public class TreeChooserTest extends TreeChooser {
  
  private TreeChooser treeChooser;
  private static final String[] termIDs = {"TTO:1003114", "TTO:1004143", "TTO:1030110"};

  @BeforeClass public static void initialize() {
    final String[] emptyArgs = {};
    Phenote.main(emptyArgs);
  }

  @Before public void setup() throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        treeChooser = new TreeChooser();
      }
    });
  }

  @Test public void getChosenTemplates() throws InterruptedException, InvocationTargetException {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        try {
          final List<CharacterI> characters = getTestCharacters();
          final String newickTree = String.format("('%s', ('%s', '%s'))", characters.get(0).getValueString("Taxon"), characters.get(1).getValueString("Taxon"), characters.get(2).getValueString("Taxon"));
          treeChooser.setNewickTree(newickTree);
          treeChooser.getTreeViewer().clearSelectedTaxa();
          final TreePane treePane = treeChooser.getTreeViewer().getTreePane();
          final RootedTree tree = treeChooser.getTreeViewer().getTreePane().getTree();
          for (Taxon taxon : tree.getTaxa()) {
            if ((taxon.getName().equals(characters.get(0).getValueString("Taxon"))) || (taxon.getName().equals(characters.get(2).getValueString("Taxon")))) {
              treePane.addSelectedNode(tree.getNode(taxon));
              System.out.println("Adding selected taxon: " + taxon);
            }
          }
          // test a term field
          treeChooser.setCharField(CharFieldManager.inst().getCharFieldForName("Taxon"));
          Collection<CharacterI> chosenByTaxon = treeChooser.getChosenTemplates(characters);
          Assert.assertTrue(chosenByTaxon.contains(characters.get(0)));
          Assert.assertFalse(chosenByTaxon.contains(characters.get(1)));
          Assert.assertTrue(chosenByTaxon.contains(characters.get(2)));
          // test a free text field
          treeChooser.setCharField(CharFieldManager.inst().getCharFieldForName("Textual Description"));
          Collection<CharacterI> chosenByDescription = treeChooser.getChosenTemplates(characters);
          Assert.assertTrue("First character should be chosen by phylogeny chooser", chosenByDescription.contains(characters.get(0)));
          Assert.assertFalse("Second character should not be chosen by phylogeny chooser", chosenByDescription.contains(characters.get(1)));
          Assert.assertTrue("Third character should be chosen by phylogeny chooser", chosenByDescription.contains(characters.get(2)));
        } catch (CharFieldException e) {
          Assert.fail(e.toString());
        } catch (TermNotFoundException e) {
          Assert.fail(e.toString());
        }
      }
    });
  }
  
  private List<CharacterI> getTestCharacters() throws CharFieldException, TermNotFoundException {
    final List<CharacterI> characters = new ArrayList<CharacterI>();
    for (String term : termIDs) {
      final CharacterI character = CharacterIFactory.makeChar();
      character.setValue("Taxon", term);
      character.setValue("Textual Description", character.getTerm("Taxon").getName());
      characters.add(character);
    }
    return characters;
  }
  
}
