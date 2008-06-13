package org.phenoscape.view;

import org.bbop.framework.AbstractGUIComponent;
import org.obo.datamodel.OBOClass;
import org.phenoscape.model.PhenoscapeController;

import ca.odell.glazedlists.EventList;

public class PhenoscapeGUIComponent extends AbstractGUIComponent {
  
  private final PhenoscapeController controller;

  public PhenoscapeGUIComponent(String id, PhenoscapeController controller) {
    super(id);
    this.controller = controller;
  }
  
  /**
   * Most interface components will need access to data.  The controller is the gatekeeper
   * to the data model.
   */
  public PhenoscapeController getController() {
    return this.controller;
  }
  
  /**
   * Prod an event list to send change notifications for an object it contains.
   * This will cause interface objects displaying items in that list to display the
   * changed value.  This is a Glazed Lists convention.
   */
  protected <T> void updateObjectForGlazedLists(T anObject, EventList<T> aList) {
    final int index = aList.indexOf(anObject);
    if (index > -1) { aList.set(index, anObject); }
  }

  /**
   * Update SelectionManager with current term selection.  This allows components
   * like the Term Info panel to display information about the term.
   */
  protected void updateGlobalTermSelection(OBOClass term) {
    this.getController().getPhenoteSelectionManager().selectTerm(this, term, false);
  }
  
}
