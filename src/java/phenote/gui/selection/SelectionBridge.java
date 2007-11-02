package phenote.gui.selection;

import javax.swing.JComponent;

import org.obo.datamodel.OBOClass;
import org.oboedit.gui.event.SelectionEvent;
import org.oboedit.gui.event.SelectionListener;

public class SelectionBridge implements SelectionListener,
		TermSelectionListener {

	protected SelectionManager phenoteSelectionManager = SelectionManager
			.inst();
	protected org.oboedit.controller.SelectionManager oboeditSelectionManager = org.oboedit.controller.SelectionManager
			.getManager();

	public void selectionChanged(SelectionEvent e) {
		if (e.getSelection().getTermSubSelection() instanceof OBOClass) {
			oboeditSelectionManager.removeSelectionListener(this);
			phenoteSelectionManager.removeTermSelectionListener(this);
			phenoteSelectionManager.selectTerm(e.getSource(), (OBOClass) e
					.getSelection().getTermSubSelection(), false);
			phenoteSelectionManager.addTermSelectionListener(this);
			oboeditSelectionManager.addSelectionListener(this);
		}

	}

	public void termSelected(TermSelectionEvent e) {
		if (!e.isMouseOverEvent()) {
			oboeditSelectionManager.removeSelectionListener(this);
			phenoteSelectionManager.removeTermSelectionListener(this);
			JComponent source = null;
			if (e.getSource() instanceof JComponent)
				source = (JComponent) e.getSource();
			oboeditSelectionManager.select(source, e.getOboClass());
			phenoteSelectionManager.addTermSelectionListener(this);
			oboeditSelectionManager.addSelectionListener(this);
		}
	}

	public void install() {
		phenoteSelectionManager.addTermSelectionListener(this);
		oboeditSelectionManager.addSelectionListener(this);
	}

	public void uninstall() {
		phenoteSelectionManager.removeTermSelectionListener(this);
		oboeditSelectionManager.removeSelectionListener(this);

	}

}
