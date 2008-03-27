package phenote.dataadapter;

import java.util.Collection;
import java.util.UUID;

import org.bbop.framework.ComponentManager;

import phenote.gui.factories.ScratchGroupTableFactory;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

/**
 * Manages group names and component factories for temporary collections of characters 
 * (scratch groups). These characters could be edited to use as templates which can be 
 * drag-copied into the main annotation table.
 * @author Jim Balhoff
 */
public class ScratchGroupsManager {
  
  private static ScratchGroupsManager inst;
  private EventList<ScratchGroup> scratchGroups = new BasicEventList<ScratchGroup>();
  
  private ScratchGroupsManager() {};
  
  public static ScratchGroupsManager inst() {
    if (inst == null) {
      inst = new ScratchGroupsManager();
    }
    return inst;
  }
  
  public static void reset() {
    //TODO
  }
  
  public EventList<ScratchGroup> getScratchGroups() {
    return this.scratchGroups;
  }
  
  /**
   * Returns the ScratchGroup with the given ID.
   */
  public ScratchGroup getScratchGroup(String id) {
    for (ScratchGroup group : this.getScratchGroups()) {
      if (group.getId().equals(id)) return group;
    }
    return null;
  }

  /**
   * Creates and returns a new ScratchGroup, meanwhile installing a GUIComponentFactory for it.
   */
  public ScratchGroup newScratchGroup() {
    final ScratchGroup pseudoGroup = new ScratchGroup(UUID.randomUUID().toString());
    pseudoGroup.setTitle("Untitled");
    this.scratchGroups.add(pseudoGroup);
    ScratchGroupTableFactory factory = new ScratchGroupTableFactory(pseudoGroup.getId());
    ComponentManager.getManager().install(factory);
    return pseudoGroup;
  }

  /**
   * Deletes the scratch groups in the collection as well as the GUIComponentFactory for each.
   */
  public void deleteScratchGroups(Collection<ScratchGroup> groups) {
    for (ScratchGroup group : groups) {
      ComponentManager.getManager().uninstall(ComponentManager.getManager().getFactory(group.getId()));
      scratchGroups.remove(group);
    }
  }
  
  /**
   * Shows the GUIComponent table representing each scratch group in the interface.
   */
  public void showScratchGroups(Collection<ScratchGroup> groups) {
    for (ScratchGroup group : groups) {
      ComponentManager.getManager().showComponent(ComponentManager.getManager().getFactory(group.getId()), false);
    }
  }
  
}
