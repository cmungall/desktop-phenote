package phenote.gui;

import phenote.datamodel.CharacterI;
import ca.odell.glazedlists.swing.EventSelectionModel;

/**
 * Methods used by FieldPanel to update its interface for the currently edited character table.
 */
public interface CharacterTableSource {
  
  public String getGroup();
  
  public EventSelectionModel<CharacterI> getSelectionModel();

}
