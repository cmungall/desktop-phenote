package phenote.gui.factories;

import java.util.Collections;
import java.util.List;

import org.bbop.framework.AbstractComponentFactory;
import org.bbop.framework.GUIComponentFactory;
import org.bbop.framework.GUIComponentFactory.FactoryCategory;
import org.oboedit.gui.components.GraphViewCanvas;
import org.oboedit.gui.factory.GraphViewFactory;

public class PhenoteGraphViewFactory extends GraphViewFactory {

	public PhenoteGraphViewFactory() {
	}
	
	public String getID() {
//		return "GRAPH_DAG_VIEW";
		return "GRAPH_VIEW";
	}
	
	public GraphViewCanvas doCreateComponent(String id) {
		return new GraphViewCanvas(id);
	}

	public String getName() {
		return "Graph View";
	}
	
	
	public FactoryCategory getCategory() {
		return FactoryCategory.ONTOLOGY;
	}

	@Override
	public String getHelpTopicID() {
		return "Graph_Viewer";
	}
}
