package phenote.gui.selection;

import java.util.EventObject;

import org.geneontology.oboedit.datamodel.OBOClass;

public class TermSelectionEvent extends EventObject {

  private OBOClass oboClass;
  private UseTermListener useTermListener;

  TermSelectionEvent(Object source, OBOClass oboClass, UseTermListener utl) {
    super(source);
    this.oboClass = oboClass;
    useTermListener = utl;
  }

  public OBOClass getOboClass() { return oboClass; }

  public UseTermListener getUseTermListener() { return useTermListener; }
}
