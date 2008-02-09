package phenote.gui.factories;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.bbop.framework.AbstractComponentFactory;
import org.bbop.framework.GUIComponentFactory;
import org.bbop.framework.GUIComponentFactory.FactoryCategory;
import org.oboedit.gui.components.DAGView;
import org.oboedit.gui.factory.DAGViewFactory;


/**
 * Phenote folks wanted their DAG view to be called a Tree view to minimize 
 * confusion with the different components.  So, until OBO-edit calls it a 
 * Tree view, we'll just rename it here.
 * @author Nicole
 *
 */
public class PhenoteDAGViewFactory extends DAGViewFactory {
	
	public PhenoteDAGViewFactory() {
	}
	
	public DAGView doCreateComponent(String id) {
		return new DAGView(id);
	}

	@Override
	public String getName() {
		return "Paths-to-Root Tree View";
	}
	
	public String getID() {
		return "DAG_VIEW";
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
		return "The_DAG_Viewer";
	}
}
