package org.phenoscape.view;

import org.bbop.framework.AbstractComponentFactory;
import org.phenoscape.model.PhenoscapeController;

public class CharacterBrowserComponentFactory extends AbstractComponentFactory<CharacterBrowserComponent> {
  
  private final PhenoscapeController controller;

  public CharacterBrowserComponentFactory(PhenoscapeController controller) {
    this.controller = controller;
  }
  
  @Override
  public CharacterBrowserComponent doCreateComponent(String id) {
    return new CharacterBrowserComponent(id, this.controller);
  }

  public FactoryCategory getCategory() {
    return FactoryCategory.ANNOTATION;
  }

  public String getID() {
    return "phenoscape_characters_browser";
  }

  public String getName() {
    return "Characters Browser";
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  
}
