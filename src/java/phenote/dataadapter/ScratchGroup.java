package phenote.dataadapter;

import java.util.Observable;

/**
 * Manages title and ID (group name) for a scratch group.
 * A scratch group allows the user to maintain a temporary 
 * table of characters to be used as templates.
 * @author Jim Balhoff
 */
public class ScratchGroup extends Observable {
  
  private String title = "";
  private final String id;
  
  /**
   * Create a new ScratchGroup with the given ID.
   * This ID should be unique and cannot be changed.
   */
  public ScratchGroup(String uniqueID) {
    super();
    this.id = uniqueID;
  }
  
  public String getTitle() {
    return title;
  }
  
  public void setTitle(String title) {
    this.title = title;
    this.setChanged();
    this.notifyObservers();
  }
  
  public String getId() {
    return id;
  }
  
}
