package phenote.gui.factories;

import java.awt.Dimension;

import org.bbop.framework.AbstractComponentFactory;
import org.bbop.framework.GUIComponentFactory.FactoryCategory;

import phenote.gui.NcbiInfo;

public class NCBIInfoFactory extends AbstractComponentFactory<NcbiInfo> {
		private String panelName = "Publication Browser";
		private String displayName = null;

		public FactoryCategory getCategory() {
			return FactoryCategory.INFO;
		}

		public String getName() { // what is displayed in the menu
			return panelName;
		}

		public String getID() {
			return "NCBI";
		}

		@Override
		public NcbiInfo doCreateComponent(String id) {
			NcbiInfo info = new NcbiInfo();
			info.setMinimumSize(new Dimension(200, 200));
			info.setPreferredSize(new Dimension(200, 200));
			info.setTitle("Publication Browser");
			return info;
		}

	}
