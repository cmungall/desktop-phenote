package phenote.edit;

import java.util.ArrayList;
import java.util.List;

public class EditManager {

  private static EditManager singleton;

  private List<CharChangeListener> charListeners = new ArrayList<CharChangeListener>(3);

  private EditManager() {}

  public static EditManager inst() {
    if (singleton == null)
      singleton = new EditManager();
    return singleton;
  }

  public void addCharChangeListener(CharChangeListener l) {
    charListeners.add(l);
  }

  public void updateModel(Object source, UpdateTransaction ut) {
    ut.editModel();
    CharChangeEvent e = new CharChangeEvent(source,ut);
    for (CharChangeListener l : charListeners)
      l.charChanged(e);
  }

}
