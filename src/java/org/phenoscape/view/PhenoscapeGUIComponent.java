package org.phenoscape.view;

import org.bbop.framework.AbstractGUIComponent;
import org.phenoscape.model.PhenoscapeController;

public class PhenoscapeGUIComponent extends AbstractGUIComponent {
  
  final PhenoscapeController controller;

  public PhenoscapeGUIComponent(String id, PhenoscapeController controller) {
    super(id);
    this.controller = controller;
  }
  
  public PhenoscapeController getController() {
    return this.controller;
  }

}
