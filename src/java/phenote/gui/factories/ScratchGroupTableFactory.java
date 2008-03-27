package phenote.gui.factories;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIComponent;

import phenote.dataadapter.ScratchGroup;
import phenote.dataadapter.ScratchGroupsManager;
import phenote.gui.CharacterTable;

/**
 * Creates table components for scratch groups.
 * @author Jim Balhoff
 */
public class ScratchGroupTableFactory extends CharacterTableFactory {
  
  private final String scratchGroupID;
  
  public ScratchGroupTableFactory(String group) {
    super(group);
    this.scratchGroupID = group;
  }
  
  @Override
  public CharacterTable doCreateComponent(String id) {
    final CharacterTable table = new CharacterTable(this.getScratchGroup(), id);
    new TitleUpdater(table, this.getScratchGroup());
    return table;
  }

  @Override
  public String getName() {
    return this.getScratchGroup().getTitle();
  }
  
  @Override
  public String getID() {
    return this.scratchGroupID;
  }

  private ScratchGroup getScratchGroup() {
    return ScratchGroupsManager.inst().getScratchGroup(this.getID());
  }
  
  /**
   * Keeps GUIComponent title updated when scratch group's title is changed.
   * This doesn't actually work.
   */
  private static class TitleUpdater implements Observer {
    
    private final GUIComponent component;
    private final ScratchGroup group;
    
    public TitleUpdater(GUIComponent aComponent, ScratchGroup aGroup) {
      this.component = aComponent;
      this.group = aGroup;
      this.group.addObserver(this);
    }

    public void update(Observable o, Object arg) {
      log().debug("Setting title: " + this.group.getTitle());
      // this doesn't seem to update the UI, unfortunately
      this.component.setTitle(this.group.getTitle());
    }
  }

  private static Logger log() {
    return Logger.getLogger(ScratchGroupTableFactory.class);
  }
}
