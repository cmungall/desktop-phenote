package phenote.charactertemplate;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharacterI;
import phenote.util.Collections;

public class SpecimenListController extends CharacterTemplateController {
  
  private static final String COPY_NEWICK_ACTION = "copyAsNewick";

  public SpecimenListController(String groupName) {
    super(groupName);
    this.addMenuItems();
  }
  
  public void copyAsNewick() {
    final String separator = ",";
    final String taxonList = Collections.join(this.getSelectedTaxonNames(), separator);
    final String newick = String.format("(%s)", taxonList);
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(newick), null);
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

  private void addMenuItems() {
    // this is kind of temporary until menus are refactored
    final JMenu editMenu = new JMenu("Edit");
    this.getWindow().getJMenuBar().add(editMenu);
    final JMenuItem menuItem = new JMenuItem("Copy as Newick tree");
    editMenu.add(menuItem);
    menuItem.setActionCommand(COPY_NEWICK_ACTION);
    menuItem.addActionListener(new SpecimenListActionListener());
  }
  
  private Logger log() {
    return Logger.getLogger(SpecimenListController.class);
  }
  
  private class SpecimenListActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      if (event.getActionCommand().equals(COPY_NEWICK_ACTION)) {
        SpecimenListController.this.copyAsNewick();
      }
    }
  }
  
}
