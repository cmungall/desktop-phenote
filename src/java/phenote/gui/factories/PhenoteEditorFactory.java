package phenote.gui.factories;

import org.bbop.framework.AbstractComponentFactory;

import phenote.gui.field.FieldPanelContainer;

public class PhenoteEditorFactory extends AbstractComponentFactory<FieldPanelContainer> {

		private String panelName = "Annotation Editor";

		public FactoryCategory getCategory() {
			return FactoryCategory.ANNOTATION;
		}

		public String getName() {
			return panelName;
		}

		public String getID() {
			return "phenote-editor";
		}
		
		public boolean isSingleton() {
      return true;
    }

		@Override
		public FieldPanelContainer doCreateComponent(String id) {
			return new FieldPanelContainer(id);
		}
}
