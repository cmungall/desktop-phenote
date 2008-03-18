package phenote.gui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import phenote.dataadapter.LoadSaveManager;

public class DbCommitAction extends AbstractAction {

  public DbCommitAction() {
    super("DB commit");
    putValue(SHORT_DESCRIPTION,"Commit to Database");
  }

	public void actionPerformed(ActionEvent e) {
    LoadSaveManager.inst().saveToDbDataadapter();
  }

}
