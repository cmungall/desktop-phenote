package phenote.charactertemplate;

import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jebl.evolution.graphs.Node;
import jebl.evolution.io.ImportException;
import jebl.evolution.io.NewickImporter;
import jebl.evolution.trees.Tree;
import jebl.evolution.trees.Utils;
import jebl.gui.trees.treeviewer.TreeViewer;
import jebl.gui.trees.treeviewer.painters.BasicLabelPainter;

import org.apache.log4j.Logger;
import org.geneontology.oboedit.datamodel.OBOClass;
import org.swixml.SwingEngine;

import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharacterI;
import phenote.util.FileUtil;

public class TreeChooser extends AbstractTemplateChooser {
  
  private JFrame window;
  private TreeViewer treeViewer;
  private JTextField newickField; // initialized by swix
  private JLabel newickFieldLabel; // initialized by swix
  private JPanel treeViewerContainer; // initialized by swix
  
  public void showChooser() {
    this.getWindow().setVisible(true);
  }

  public void changeNewickTreeAction() {
    this.setNewickTree(this.newickField.getText());
  }
  
  public void applySelectionAction() {
    this.fireTemplateChoiceChanged();
  }
  
  public Collection<CharacterI> getChosenTemplates(Collection<CharacterI> candidates) {
    if (this.getCharField() == null) {
      this.getLogger().error("CharField not set for TreeChooser - no matching templates.");
      return Collections.emptyList();
    }
    final Collection<String> chosenTermIDs = this.getChosenTermIDs();
    final Collection<CharacterI> chosenTemplates = new ArrayList<CharacterI>();
    for (CharacterI character : candidates) {
      final CharFieldValue fieldValue = character.getValue(this.getCharField()); 
      if (fieldValue.isTerm()) {
        OBOClass term = fieldValue.getTerm();
        if ((term != null) && (chosenTermIDs.contains(term.getID()))) {
          chosenTemplates.add(character);
        }
      } else if (chosenTermIDs.contains(fieldValue.getName())){
        chosenTemplates.add(character);
      }
    }
    return chosenTemplates;
  }

  public void setNewickTree(String treeText) {
    try {
      Tree tree = this.importTree(treeText);
      this.getTreeViewer().setTree(tree);
      // must reset label painters every time tree is changed, because TreeViewer creates new ones
      final OntologyTermPainter ontologyTermTipPainter = new OntologyTermPainter("Tip Labels", this.getTreeViewer().getTreePane().getTree(), BasicLabelPainter.PainterIntent.TIP);
      final OntologyTermPainter ontologyTermNodePainter = new OntologyTermPainter("Node Labels", this.getTreeViewer().getTreePane().getTree(), BasicLabelPainter.PainterIntent.NODE);
      this.getTreeViewer().getTreePane().setTaxonLabelPainter(ontologyTermTipPainter);
      this.getTreeViewer().getTreePane().setNodeLabelPainter(ontologyTermNodePainter);
      if (this.newickFieldLabel != null) this.newickFieldLabel.setForeground(Color.BLACK);
    }
    catch (ImportException e) {
      if (this.newickFieldLabel != null) this.newickFieldLabel.setForeground(Color.RED);
    } 
  }
  
  private Set<String> getChosenTermIDs() {
    final Set<String> termIDs = new HashSet<String>();
    final Set<Node> nodes = this.getTreeViewer().getTreePane().getSelectedNodes();
    final Tree tree = this.getTreeViewer().getTreePane().getTree();
    for (Node node : nodes) {
      if (tree.isExternal(node)) {
        termIDs.add(tree.getTaxon(node).getName());
      }
    }
    return termIDs;
  }
  
  private TreeViewer getTreeViewer() {
    if (this.treeViewer == null) {
      this.treeViewer = this.createViewer();
      // starting out with a tree helps the TreeViewer to display correctly
      this.setNewickTree("(((Chimp, Human), Gorilla)Apes, Lemur)");
    }
    return this.treeViewer;
  }

  private JFrame getWindow() {
    if (this.window == null) {
      this.window = new JFrame("Phylogeny Chooser");
      final JComponent component = this.createPanel();
      this.treeViewerContainer.add(this.getTreeViewer());
      this.window.setSize(component.getSize());
      this.window.getContentPane().add(component);
    }
    return this.window;
  }
  
  private JComponent createPanel() {
    SwingEngine swix = new SwingEngine(this);
    try {
      JComponent component = (JComponent)swix.render(FileUtil.findUrl("tree_chooser.xml"));
      return component;
    } catch (Exception e) {
      this.getLogger().error("Unable to render interface", e);
      return new JPanel();
    }
  }
  
  private TreeViewer createViewer() {
    return new TreeViewer();
  }
  
  private Tree importTree(String newickText) throws ImportException {
    Reader reader = new StringReader(newickText);
    NewickImporter importer = new NewickImporter(reader, false);
    try {
      return Utils.rootTheTree(importer.importNextTree());
    } catch (IOException e) {
      // this is unlikely since we're using a StringReader
      this.getLogger().error("Can't read tree, newick text must be null", e);
      return null;
    } 
  }
  
  private Logger getLogger() {
    return Logger.getLogger(this.getClass());
  }

}
