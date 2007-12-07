package phenote.charactertemplate;

import java.awt.Color;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
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
import jebl.evolution.io.NewickExporter;
import jebl.evolution.io.NewickImporter;
import jebl.evolution.io.NexusImporter;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.Tree;
import jebl.evolution.trees.Utils;
import jebl.gui.trees.treeviewer.TreeViewer;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import phenote.dataadapter.CharListChangeEvent;
import phenote.dataadapter.CharListChangeListener;
import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharacterI;
import phenote.util.FileUtil;

public class TreeChooser extends AbstractTemplateChooser implements CharListChangeListener {
  
  private static final long serialVersionUID = 2683680201895012882L;
  private JFrame window;
  private TreeViewer treeViewer;
  private JTextField newickField; // initialized by swix
  private JLabel newickFieldLabel; // initialized by swix
  private JPanel treeViewerContainer; // initialized by swix
  
  public TreeChooser(String id) {
    super(id);
    CharacterListManager.main().addCharListChangeListener(this);
    this.setLayout(new GridLayout());
    this.add(this.createComponent());
  }
  
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
      log().error("CharField not set for TreeChooser - no matching templates.");
      return Collections.emptyList();
    }
    final Collection<String> selectedTaxa = this.getSelectedTaxonNames();
    final Collection<CharacterI> chosenTemplates = new ArrayList<CharacterI>();
    for (CharacterI character : candidates) {
      final String valueString = character.getValueString(this.getCharField());
      if (selectedTaxa.contains(valueString)) {
        chosenTemplates.add(character);
      }
    }
    return chosenTemplates;
  }

  public void setNewickTree(String treeText) {
    try {
      final Tree tree = this.importTree(treeText);
      this.setTree(tree);
      if (this.newickFieldLabel != null) this.newickFieldLabel.setForeground(Color.BLACK);
    }
    catch (ImportException e) {
      if (this.newickFieldLabel != null) this.newickFieldLabel.setForeground(Color.RED);
      log().error("Invalid Newick tree", e);
    } 
  }
  
  public void setTree(Tree tree) {
    this.replaceTaxonUnderscoresWithSpaces(tree);
    this.getTreeViewer().setTree(tree);
  }
  
  public void newCharList(CharListChangeEvent e) {
    // this is assumed to be coming from the main CharacterListManager
    this.tryLoadDefaultDataFile();
  }
  
  private Set<String> getSelectedTaxonNames() {
    final Set<String> taxa = new HashSet<String>();
    final Set<Node> nodes = this.getTreeViewer().getTreePane().getSelectedNodes();
    final Tree tree = this.getTreeViewer().getTreePane().getTree();
    for (Node node : nodes) {
      if (tree.isExternal(node)) {
        taxa.add(tree.getTaxon(node).getName());
      }
    }
    return taxa;
  }
  
  private void updateNewickField(Tree tree) {
    try {
      final StringWriter writer = new StringWriter();
      final NewickExporter exporter = new NewickExporter(writer);
      exporter.exportTree(tree);
      this.newickField.setText(writer.toString());
    } catch (IOException e) {
      log().error("Error exporting Newick tree", e);
    }
  }
  
  protected TreeViewer getTreeViewer() {
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
      final JComponent component = this.createComponent();
      this.window.setSize(component.getSize());
      this.window.getContentPane().add(component);
    }
    return this.window;
  }
  
  private JComponent createComponent() {
    final JComponent component = this.createPanel();
    this.treeViewerContainer.add(this.getTreeViewer());
    return component;
  }
  
  private JComponent createPanel() {
    SwingEngine swix = new SwingEngine(this);
    try {
      JComponent component = (JComponent)swix.render(FileUtil.findUrl("tree_chooser.xml"));
      return component;
    } catch (Exception e) {
      log().error("Unable to render interface", e);
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
      log().error("Can't read tree, newick text must be null", e);
      return null;
    } 
  }
  
  private void tryLoadDefaultDataFile() {
    final File file = CharacterListManager.main().getCurrentDataFile();
    if (file == null) {
      return;
    }
    final int dotLocation = file.getName().lastIndexOf(".");
    final boolean hasExtension = dotLocation > 0;
    final String baseName = hasExtension ? file.getName().substring(0, dotLocation) : file.getName();
    final String defaultFileName = baseName + ".tre";
    File treeFile = new File(file.getParent(), defaultFileName);
    if (treeFile.exists()) {
      // the tree file must be in NEXUS format
      try {
        NexusImporter importer = new NexusImporter(new BufferedReader(new FileReader(treeFile)));
        Tree tree = importer.importNextTree();
        this.setTree(tree);
      } catch (FileNotFoundException e) {
        log().error("Could not open tree file", e);
      } catch (IOException e) {
        log().error("Could not open tree file", e);
      } catch (ImportException e) {
        log().error("Could not read tree", e);
      }
    }
  }
  
  private void replaceTaxonUnderscoresWithSpaces(Tree tree) {
    for (Node node : tree.getNodes()) {
      if (tree.isExternal(node)) {
        log().debug("Taxon: " + tree.getTaxon(node).getName());
        final String original = tree.getTaxon(node).getName();
        final String replaced = original.replaceAll("_", " ");
        tree.renameTaxa(tree.getTaxon(node), Taxon.getTaxon(replaced));
      } else {
        log().debug("Label: " + node.getAttribute("label"));
        final Object label = node.getAttribute("label");
        if (label != null) {
          final String original = node.getAttribute("label").toString();
          final String replaced = original.replaceAll("_", " ");
          node.setAttribute("label", replaced);
        }
      }
    }
  }
  
  private static Logger log() {
    return Logger.getLogger(TreeChooser.class);
  }

}
