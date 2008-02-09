package phenote.gui.factories;

import org.bbop.framework.AbstractComponentFactory;
import phenote.gui.TermInfo2;

public class TermInfoFactory extends AbstractComponentFactory<TermInfo2> {
		public FactoryCategory getCategory() {
			return FactoryCategory.INFO;
		}

		public String getName() {
			return "Term Info Browser";
		}

		public boolean isSingleton() {
			return true;
		}

		public String getID() {
			return "term-info";
		}

		@Override
		public TermInfo2 doCreateComponent(String id) {
			return TermInfo2.inst();
		}
}

