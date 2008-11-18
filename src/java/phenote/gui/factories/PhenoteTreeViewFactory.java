package phenote.gui.factories;

import org.oboedit.gui.components.treeView.TreeView;
import org.oboedit.gui.factory.TreeViewFactory;


/**
 * Phenote folks wanted their DAG view to be called a Tree view to minimize 
 * confusion with the different components.  So, until OBO-edit calls it a 
 * Tree view, we'll just rename it here.
 * @author Nicole
 *
 */
public class PhenoteTreeViewFactory extends TreeViewFactory {
	
	public PhenoteTreeViewFactory() {
	}
	
	public TreeView doCreateComponent(String id) {
		return new TreeView(id);
	}

	@Override
	public String getName() {
		return "Paths-to-Root Tree View";
	}
	
	public String getID() {
//		return "DAG_VIEW";
		return "TREE_VIEW";
	}
	
	@Override
	public boolean getPreferSeparateWindow() {
		return true;
	}
	
	public FactoryCategory getCategory() {
		return FactoryCategory.ONTOLOGY;
	}

	@Override
	public String getHelpTopicID() {
//		return "The_DAG_Viewer";
		return "Tree_Viewer";
	}
}
