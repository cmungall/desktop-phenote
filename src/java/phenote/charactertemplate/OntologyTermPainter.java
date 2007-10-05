package phenote.charactertemplate;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.RootedTree;
import jebl.gui.trees.treeviewer.painters.BasicLabelPainter;

import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.datamodel.CharFieldManager;
import phenote.datamodel.TermNotFoundException;

public class OntologyTermPainter extends BasicLabelPainter {

  public OntologyTermPainter(String title, RootedTree tree, PainterIntent intent) {
    super(title, tree, intent);
  }

  public OntologyTermPainter(String title, RootedTree tree, PainterIntent intent, int defaultSize) {
    super(title, tree, intent, defaultSize);
  }

  @Override
  protected String getLabel(Node node) {
    final String defaultLabel =  super.getLabel(node);
    if (defaultLabel == null) return defaultLabel;
    try {
      OBOClass term = CharFieldManager.inst().getOboClass(defaultLabel.trim());
      return " " + term.getName() + " [" + defaultLabel.trim() + "] ";
    } catch (TermNotFoundException e) {
      return defaultLabel;
    }
  }
 
}
