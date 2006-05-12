package phenote.gui.selection;

import java.util.EventObject;

import org.geneontology.oboedit.datamodel.OBOClass;

public class TermSelectionEvent extends EventObject {

  private OBOClass oboClass;

  TermSelectionEvent(Object source, OBOClass oboClass) {
    super(source);
    this.oboClass = oboClass;
  }

  public OBOClass getOboClass() { return oboClass; }
}
