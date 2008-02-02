package phenote.charactertemplate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import jebl.evolution.graphs.Node;
import jebl.evolution.io.ImportException;
import jebl.evolution.io.NewickExporter;
import jebl.evolution.io.NewickImporter;
import jebl.evolution.io.NexusExporter;
import jebl.evolution.io.NexusImporter;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.Tree;
import jebl.evolution.trees.Utils;
import jebl.gui.trees.treeviewer.TreeViewer;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.LoadSaveListener;
import phenote.dataadapter.LoadSaveManager;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharacterI;
import phenote.util.Collections;
import phenote.util.FileUtil;

public class TreeChooser extends AbstractTemplateChooser {
  
  private static final long serialVersionUID = 2683680201895012882L;
  private JFrame window;
  private TreeViewer treeViewer;
  private JTextField newickField; // initialized by swix
  private JLabel newickFieldLabel; // initialized by swix
  private JPanel treeViewerContainer; // initialized by swix
  private JButton applyButton;
  private JButton nexusButton;
  
  public TreeChooser(String id) {
    super(id);
    LoadSaveManager.inst().addListener(new FileListener());
    this.setLayout(new BorderLayout());
    this.add(this.createComponent(), BorderLayout.CENTER);
    this.add(this.createToolBar(), BorderLayout.NORTH);
    this.tryLoadDefaultDataFile(CharacterListManager.main().getCurrentDataFile());
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
      return java.util.Collections.emptyList();
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
    this.updateNewickField(tree);
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
  
  private String getNEXUSTree() {
    final StringWriter writer = new StringWriter();
    final NexusExporter nexus = new NexusExporter(writer);
    final Tree tree = this.getTreeViewer().getTreePane().getTree();
    try {
      nexus.exportTree(tree);
    } catch (IOException e) {
      log().error("Unable to write NEXUS format", e);
    }
    return writer.toString(); 
  }
  
  private void tryLoadDefaultDataFile(File mainFile) {
    if (mainFile == null) {
      return;
    }
    File treeFile = this.getDefaultDataFile(mainFile);
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
  
  private void saveDefaultDataFile() {
    // get the list of taxa
    final List<String> names = new ArrayList<String>();
    for (CharacterI character : CharacterListManager.getCharListMan(this.getGroup()).getCharList()) {
      if (character.hasValue(this.getCharField())) {
        names.add(character.getValue(this.getCharField()).getTerm().getName());
      }
    }
    // check to see if there are any taxa
    if (names.size() < 2) {
      JOptionPane.showMessageDialog(null, "There are too few taxa to export. Enter at least 2 in the Taxon List first.");
      return;
    }
    // check if there is a current data file
    if (CharacterListManager.main().getCurrentDataFile() == null) {
      // if not, warn user
      final Object[] options = { "Save Annotations...", "Cancel" };
      final int result = JOptionPane.showOptionDialog(null, "You must first choose a location to save your main data file.  The NEXUS file will be saved in the same directory.", null, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
      // cancel -> return
      if (result != 0) return;
      // okay -> save main file, continue
      LoadSaveManager.inst().saveData();
    }
    final File mainFile = CharacterListManager.main().getCurrentDataFile();
    // check if there is a current data file
    if (mainFile != null) {
      final File nexusFile = this.getDefaultDataFile(mainFile);
      if (nexusFile.exists()) {
        // warn user that they will overwrite their current tree file
        final Object[] options = { "Replace File", "Cancel" };
        final int result = JOptionPane.showOptionDialog(null, "A tree file exists at the chosen location.  If you proceed it will be replaced.  This cannot be undone.", "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        // okay -> continue
        // cancel -> return
        if (result != 0) return;
      }
      // if so, write out tree file
      log().debug("Newick comb: " + this.getNewickComb(names));
      this.setNewickTree(this.getNewickComb(names));
      try {
        (new FileWriter(nexusFile)).write(this.getNEXUSTree());
      } catch (IOException e) {
        log().error("Failed to write NEXUS file", e);
      }
    }
  }
  
  private File getDefaultDataFile(File mainFile) {
    final int dotLocation = mainFile.getName().lastIndexOf(".");
    final boolean hasExtension = dotLocation > 0;
    final String baseName = hasExtension ? mainFile.getName().substring(0, dotLocation) : mainFile.getName();
    final String defaultFileName = baseName + ".tre";
    return new File(mainFile.getParent(), defaultFileName);
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
  
  private String getNewickComb(List<String> taxonNames) {
    List<String> quotedNames = new ArrayList<String>();
    for (String name : taxonNames) {
      quotedNames.add("'" + name + "'");
    }
    final String separator = ",";
    final String taxonList = Collections.join(quotedNames, separator);
    final String newick = String.format("(%s)", taxonList);
    return newick;
  }
  
  @SuppressWarnings("serial")
  private JToolBar createToolBar() {
    final JToolBar toolBar = new JToolBar("Default Toolbar");
    
    try {
    this.applyButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/square-filled.png"))) {
      public void actionPerformed(ActionEvent e) {
        applySelectionAction();
      }
    });
    this.applyButton.setToolTipText("Apply Selection");
    toolBar.add(this.applyButton);
    
    this.nexusButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/nexus.png"))) {
      public void actionPerformed(ActionEvent e) {
        saveDefaultDataFile();
      }
    });
    this.nexusButton.setToolTipText("Write out default NEXUS file");
    toolBar.add(this.nexusButton);
    
    } catch (FileNotFoundException e) {
      log().error("Couldn't find toolbar icons", e);
    }
    
    toolBar.setFloatable(false);
    return toolBar;
  }
  
  private class FileListener implements LoadSaveListener {

    public void fileLoaded(File f) {
      tryLoadDefaultDataFile(f);
    }

    /* tree files are read only - we don't save */
    public void fileSaved(File f) {}
    
  }
  
  private static Logger log() {
    return Logger.getLogger(TreeChooser.class);
  }

}
