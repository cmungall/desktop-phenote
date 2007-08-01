package phenote.gui;

// dataadapter?? datamodel??

import java.util.ArrayList;
import java.util.List;

public class ErrorManager {

  // group based?? separate managers for sep groups?? for now just 1
  private static ErrorManager singleton;
  private List<ErrorListener> errorListeners = new ArrayList<ErrorListener>(2);

  public static ErrorManager inst() {
    if (singleton == null) singleton = new ErrorManager();
    return singleton;
  }

  public void error(ErrorEvent e) {
    for (ErrorListener l : errorListeners) {
      l.handleError(e);
    }
  }

  public void addErrorListener(ErrorListener el) {
    errorListeners.add(el);
  }

}
