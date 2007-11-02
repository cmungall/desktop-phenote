package phenote.gui;

import javax.swing.JFrame;

import org.obo.datamodel.OBOSession;

import phenote.datamodel.CharFieldManager;
import phenote.datamodel.Ontology;
import phenote.gui.selection.SelectionManager;
import phenote.gui.selection.TermSelectionEvent;
import phenote.gui.selection.TermSelectionListener;
import ca.uvic.csr.shrimp.gui.QueryView.OBOViewer;
import ca.uvic.csr.shrimp.gui.QueryView.QueryView;

public class ShrimpDag {

	// private JFrame window;
	private OBOViewer oboViewer;
	private static ShrimpDag singleton;

	public static ShrimpDag inst() {
		if (singleton == null)
			singleton = new ShrimpDag();
		return singleton;
	}

	public static void reset() {
		singleton = null;
	}

	private ShrimpDag() {
		// window = new JFrame("Shrimp ontology viewer");
		// window.pack();
		// window.setVisible(true);
		init();
	}

	// public void display() {}

	/**
	 * send obo sessions to shrimp so it can set them up in its datamodel
	 */
	public void initOntologies() {
		// do this in a separate thread! no need to hold up phenote with this
		for (Ontology o : CharFieldManager.inst().getAllOntologies()) {
			// oboViewer.loadOboSession(o.getOboSession());
		}
	}

	private void init() {
		// initShrimp();
		SelectionManager.inst().addTermSelectionListener(
				new ShrimpSelectionListener());
		// initOntologies();
	}

	public void display() {

		// queryView = new QueryView(); false - show query view?
		boolean showQueryPanel = true;
		oboViewer = new OBOViewer(showQueryPanel);
		// new OBOViewer("Shrimp ontology
		// viewer",getOboSession(),showQueryPanel);
		// This doesnt compile as OBOSession has package jumped, shrimp needs
		// change
		// oboViewer.loadOBOSession(getOboSession()); // change this!

		JFrame frame = new JFrame("Shrimp DAG view");
		// frame.setDefaultCloseOperation(java.awt.WindowListener.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(oboViewer.getView());
		frame.pack();
		frame.setSize(600, 600);
		frame.setLocation(400, 200);
		frame.setVisible(true);

	}

	private OBOSession getOboSession() {
		// just hard wire to go for now
		// Ontology o = OntologyManager.inst().getOntologyForName("ZF");
		// fly causes an endless loop - oh my
		Ontology o = CharFieldManager.inst().getOntologyForName("Fly");
		// Ontology o = OntologyManager.inst().getOntologyForName("Human
		// Anatomy");
		if (o == null) {
			System.out.println("no ontol for dag");
			return null;
		}
		return o.getOboSession();
	}

	private QueryView getQueryView() {
		return oboViewer.getQueryView();
	}

	private class ShrimpSelectionListener implements TermSelectionListener {

		public void termSelected(TermSelectionEvent e) {
			if (e.isMouseOverEvent())
				return;
			String term = e.getOboClass().getName();
			boolean animate = true;
			// getQueryView().query(term, animate);
			// This doesnt compile as OBOSession has package jumped, shrimp
			// needs change
			// oboViewer.query(getOboSession(), e.getOboClass(), animate);
			return;
		}

	}

}
