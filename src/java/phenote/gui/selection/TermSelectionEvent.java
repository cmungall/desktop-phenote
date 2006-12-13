package phenote.gui.selection;

import java.util.EventObject;

import org.geneontology.oboedit.datamodel.OBOClass;

public class TermSelectionEvent extends EventObject {

  private OBOClass oboClass;
  private UseTermListener useTermListener;
  private boolean isMouseOverEvent;

  TermSelectionEvent(Object source, OBOClass oboClass, UseTermListener utl,boolean isMouse) {
    super(source);
    this.oboClass = oboClass;
    useTermListener = utl;
    isMouseOverEvent = isMouse;
  }

  public OBOClass getOboClass() { return oboClass; }

  public UseTermListener getUseTermListener() { return useTermListener; }
  
  public boolean isMouseOverEvent() { return isMouseOverEvent; }
}
