package phenote.gui.factories;

import org.bbop.framework.AbstractComponentFactory;

import phenote.gui.ScratchGroupsView;

/**
 * Factory for ScratchGroupsView.
 * @author Jim Balhoff
 */
public class ScratchGroupsViewFactory extends AbstractComponentFactory<ScratchGroupsView> {

  public ScratchGroupsViewFactory() {}

  @Override
  public ScratchGroupsView doCreateComponent(String id) {
    return new ScratchGroupsView(id);
  }

  public FactoryCategory getCategory() {
    return FactoryCategory.ANNOTATION;
  }

  public String getID() {
    return this.getClass().toString();
  }

  public String getName() {
    return "Scratch Lists";
  }

}
