package phenote.gui.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.obo.datamodel.OBOClass;

import phenote.datamodel.CharFieldManager;

/** Controller for term & character selection */
public class SelectionManager {

	// private static SelectionManager singleton;
	private static Map<String, SelectionManager> groupToSelMan = new HashMap<String, SelectionManager>();

	private List<TermSelectionListener> termListenerList;

	/** return main/default instance */
	public static SelectionManager inst() {
		return getSelMan(CharFieldManager.DEFAULT_GROUP);
		// if (singleton == null) singleton = new SelectionManager();
		// return singleton;
	}

	public static void reset() {
		groupToSelMan.clear();
	}

	public static SelectionManager getSelMan(String group) {
		if (group == null)
			group = CharFieldManager.DEFAULT_GROUP; // ??
		if (groupToSelMan.get(group) == null) {
			SelectionManager s = new SelectionManager();
			groupToSelMan.put(group, s);
		}
		return groupToSelMan.get(group);
	}

	public SelectionManager() {
		termListenerList = new ArrayList<TermSelectionListener>(5);
	}

	// TERM SELECTION

	public void addTermSelectionListener(TermSelectionListener l) {
		termListenerList.add(l);
	}

	public void removeTermSelectionListener(TermSelectionListener l) {
		termListenerList.remove(l);
	}

	// void addCharacterSelectionListener(CharacterSelectionListener l) {}

	public void selectHistoryTerm(Object source, OBOClass oboClass,
			UseTermListener l) {
		boolean isMouseOver = false;
		boolean isHyperlink = true;
		TermSelectionEvent e = makeTermEvent(source, oboClass, l, isMouseOver,
				isHyperlink);
		fireTermSelect(e);
	}

	public void selectMouseOverTerm(Object source, OBOClass oboClass,
			UseTermListener l) {
		boolean isMouseOver = true;
		boolean isHyperlink = false;
		TermSelectionEvent e = makeTermEvent(source, oboClass, l, isMouseOver,
				isHyperlink);
		fireTermSelect(e);
	}

	public void selectTerm(Object source, OBOClass oboClass, boolean isHyperlink) {
		boolean isMouseOver = false;
		// System.out.println("ishyperlink="+isHyperlink);
		TermSelectionEvent e = makeTermEvent(source, oboClass, null,
				isMouseOver, isHyperlink);
		if (!isHyperlink)
			fireTermSelect(e);
	}

	private void fireTermSelect(TermSelectionEvent e) {
		// need to make a copy of the term listener list to avoid
		// co-modification problems
		List<TermSelectionListener> temp = new ArrayList<TermSelectionListener>(
				termListenerList);
		Iterator<TermSelectionListener> it = temp.iterator();
		while (it.hasNext())
			it.next().termSelected(e);
	}

	// void selectTerm(String termName) {} ???

	private TermSelectionEvent makeTermEvent(Object src, OBOClass oc,
			UseTermListener l, boolean mouse, boolean link) {
		return new TermSelectionEvent(src, oc, l, mouse, link);
	}

}
