package phenote.gui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import phenote.dataadapter.OntologyMakerI;


public class MakeOntolAction extends AbstractAction {

	private OntologyMakerI ontolMaker;

  public MakeOntolAction(OntologyMakerI om) {
    super(om.getButtonText()); // ??
    ontolMaker = om;
  }

  public void actionPerformed(ActionEvent e) {
		if (ontolMaker == null) {
			log().error("No Ontology Maker to make ontology with");
			return;
		}
		ontolMaker.makeOntology();
  }

	private static Logger log() {
		return Logger.getLogger(MakeOntolAction.class);
	}
}
