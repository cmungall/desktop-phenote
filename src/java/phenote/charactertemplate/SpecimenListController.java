package phenote.charactertemplate;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import jebl.evolution.io.ImportException;
import jebl.evolution.io.NewickImporter;
import jebl.evolution.io.NexusExporter;
import jebl.evolution.trees.Tree;
import jebl.evolution.trees.Utils;

import org.apache.log4j.Logger;

import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharacterI;
import phenote.util.Collections;

public class SpecimenListController extends CharacterTemplateController {
  
  private static final String COPY_NEWICK_ACTION = "copyAsNewick";
  private static final String COPY_NEXUS_ACTION = "copyAsNEXUS";

  public SpecimenListController(String groupName) {
    super(groupName);
    this.addMenuItems();
  }
  
  public void copyAsNewick() {
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(this.getNewick()), null);
  }
  
  public void copyAsNEXUSTree() {
    final StringWriter writer = new StringWriter();
    final NexusExporter nexus = new NexusExporter(writer);
    final Tree tree = this.importTree(this.getNewick());
    try {
      nexus.exportTree(tree);
    } catch (IOException e) {
      log().error("Unable to write NEXUS to clipboard", e);
    }
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(writer.toString()), null); 
  }
  
  private Set<String> getSelectedTaxonNames() {
    final Set<String> taxa = new HashSet<String>();
    for (CharacterI character : this.getSelectionModel().getSelected()) {
      try {
        taxa.add(String.format("'%s'", character.getValueString("Taxon")));
      } catch (CharFieldException e) {
        log().error("Couldn't get taxon name", e);
        e.printStackTrace();
      }
    }
    return taxa;
  }
  
  private String getNewick() {
    final String separator = ",";
    final String taxonList = Collections.join(this.getSelectedTaxonNames(), separator);
    final String newick = String.format("(%s)", taxonList);
    return newick;
  }
  
  private Tree importTree(String newickText) {
    Reader reader = new StringReader(newickText);
    NewickImporter importer = new NewickImporter(reader, false);
    try {
      return Utils.rootTheTree(importer.importNextTree());
    } catch (IOException e) {
      // this is unlikely since we're using a StringReader
      log().error("Can't read tree, newick text must be null", e);
      return null;
    } catch (ImportException e) {
      log().error("Unable to create tree", e);
      return null;
    } 
  }

  private void addMenuItems() {
    // this is kind of temporary until menus are refactored
    final JMenu editMenu = new JMenu("Edit");
    this.getWindow().getJMenuBar().add(editMenu);
    final SpecimenListActionListener listener = new SpecimenListActionListener();
    final JMenuItem newickItem = new JMenuItem("Copy as Newick tree");
    editMenu.add(newickItem);
    newickItem.setActionCommand(COPY_NEWICK_ACTION);
    newickItem.addActionListener(listener);
    final JMenuItem nexusItem = new JMenuItem("Copy as NEXUS tree");
    editMenu.add(nexusItem);
    nexusItem.setActionCommand(COPY_NEXUS_ACTION);
    nexusItem.addActionListener(listener);
  }
  
  private Logger log() {
    return Logger.getLogger(SpecimenListController.class);
  }
  
  private class SpecimenListActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      if (event.getActionCommand().equals(COPY_NEWICK_ACTION)) {
        SpecimenListController.this.copyAsNewick();
      } else if (event.getActionCommand().equals(COPY_NEXUS_ACTION)) {
        SpecimenListController.this.copyAsNEXUSTree();
      }
    }
  }
  
}
