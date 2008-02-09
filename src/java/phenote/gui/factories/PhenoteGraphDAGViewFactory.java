package phenote.gui.factories;

import java.util.Collections;
import java.util.List;

import org.bbop.framework.AbstractComponentFactory;
import org.bbop.framework.GUIComponentFactory;
import org.bbop.framework.GUIComponentFactory.FactoryCategory;
import org.oboedit.gui.components.DAGViewCanvas;
import org.oboedit.gui.factory.GraphDAGViewFactory;

public class PhenoteGraphDAGViewFactory extends GraphDAGViewFactory {

	public PhenoteGraphDAGViewFactory() {
	}
	
	public String getID() {
		return "GRAPH_DAG_VIEW";
	}
	
	public DAGViewCanvas doCreateComponent(String id) {
		return new DAGViewCanvas(id);
	}

	public String getName() {
		return "Graph View";
	}
	
	
	public FactoryCategory getCategory() {
		return FactoryCategory.ONTOLOGY;
	}
}
