package phenote.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractGUIComponent;
import org.bbop.framework.ComponentManager;
import org.swixml.SwingEngine;

import phenote.config.Config;
import phenote.dataadapter.CharListChangeEvent;
import phenote.dataadapter.CharListChangeListener;
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.LoadSaveManager;
import phenote.dataadapter.OntologyMakerI;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;
import phenote.edit.EditManager;
import phenote.gui.field.CharFieldMatcherEditor;
import phenote.util.EverythingEqualComparator;
import phenote.util.FileUtil;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

public class CharacterTableController {

	private static final String SAVE_STRING = "Save Data";
	private String representedGroup = "default";
	private SortedList<CharacterI> sortedCharacters;
	private FilterList<CharacterI> filteredCharacters;
	private EventSelectionModel<CharacterI> selectionModel;
	private CharacterTableFormat tableFormat;
	private LoadSaveManager loadSaveManager;
	private CharFieldMatcherEditor filter;
	// private AbstractGUIComponent characterTablePanel; // initialized by swix
	private JPanel characterTablePanel; // initialized by swix
	private JTable characterTable; // initialized by swix
	private JButton duplicateButton; // initialized by swix
	private JButton deleteButton; // initialized by swix
	private JButton commitButton; // initialized by swix
	private JButton graphButton; // initialized by swix
	private JPanel filterPanel; // initialized by swix
	private JButton ontolMakerButton; // initialized by swix
	private OntologyMakerI ontolMaker; // for now just 1 term maker...
	// hmm... its handy
	private static CharacterTableController defaultController;

	public CharacterTableController(String groupName) {
		if (groupName != null)
			this.representedGroup = groupName;
		if (CharFieldManager.isDefaultGroup(groupName))
			defaultController = this;
		this.loadPanelLayout();
		this.sortedCharacters = new SortedList<CharacterI>(this
				.getCharacterListManager().getCharacterList().getList(),
				new EverythingEqualComparator<CharacterI>());
		this.sortedCharacters.setMode(SortedList.AVOID_MOVING_ELEMENTS);
		this.filter = new CharFieldMatcherEditor(CharFieldManager.inst()
				.getCharFieldListForGroup(this.representedGroup));
		this.filteredCharacters = new FilterList<CharacterI>(
				this.sortedCharacters, this.filter);
		this.selectionModel = new EventSelectionModel<CharacterI>(
				this.filteredCharacters);
		this.tableFormat = new CharacterTableFormat(this.representedGroup);
		this.getEditManager().addCharChangeListener(
				new CharacterChangeListener());
		CharacterListManager.getCharListMan(this.representedGroup)
				.addCharListChangeListener(new CharacterListChangeListener());
		this.initializeInterface();
		this.addInitialBlankCharacter();
	}

	// may be null
	public static CharacterTableController getDefaultController() {
		if (defaultController == null)
			defaultController = new CharacterTableController(CharFieldManager
					.getDefaultGroup());
		return defaultController;
	}

	public JPanel getCharacterTablePanel() {
		return this.characterTablePanel;
	}

	// public AbstractGUIComponent getCharacterTablePanel() {
	// return this.characterTablePanel;
	// }

	/**
	 * in swixml config conf/character_table_panel.xml the add button New is set
	 * up to call this method
	 */
	public void addNewCharacter() {
		this.getEditManager().addNewCharacter();
	}

	/**
	 * in swixml config conf/character_table_panel.xml the duplicate button is
	 * set up to call this method
	 */
	public void duplicateSelectedCharacters() {
		this.getEditManager().copyChars(this.selectionModel.getSelected());
	}

	/**
	 * in swixml config conf/character_table_panel.xml the delete button is set
	 * up to call this method
	 */
	public void deleteSelectedCharacters() {
		final int maxSelectedRow = this.selectionModel.getMaxSelectionIndex();
		this.getEditManager().deleteChars(this.selectionModel.getSelected());
		if ((maxSelectedRow > -1) && (!this.filteredCharacters.isEmpty())) {
			final int rowToSelect = Math.min(
					(this.filteredCharacters.size() - 1), maxSelectedRow);
			this.selectionModel.setSelectionInterval(rowToSelect, rowToSelect);
		}
		if (this.getCharacterListManager().getCharacterList().isEmpty()) {
			this.addInitialBlankCharacter();
		}
	}

	/**
	 * in swixml config conf/character_table_panel.xml the undo button is set up
	 * to call this method
	 */
	public void undo() {
		this.getEditManager().undo();
	}

	/**
	 * in swixml config conf/character_table_panel.xml the save button is set up
	 * to call this method
	 */
	public void commitCharacters() {
		if (Config.inst().hasQueryableDataAdapter()) {
			Config.inst().getQueryableDataAdapter().commit(
					this.getCharacterListManager().getCharacterList());
		} else {
			this.getLoadSaveManager().saveData();
		}
	}

	/**
	 * set up as action method in character_table_panel.xml swix config make
	 * terms button has been hit by user - go off and make terms if we have a
	 * TermMaker - otherwise do nothing - error msg, this assumes for moment
	 * that there is only 1 term maker - if we need more than need to deal with
	 * that
	 */
	public void makeOntology() {
		if (ontolMaker == null) {
			log().error("No Ontology Maker to make ontology with");
			return;
		}
		ontolMaker.makeOntology();
	}

	public EventSelectionModel<CharacterI> getSelectionModel() {
		return this.selectionModel;
	}

	/** Instantiates interface objects from Swixml file */
	private void loadPanelLayout() {
		SwingEngine swix = new SwingEngine(this);
		try {
			this.characterTablePanel = (JPanel) swix.render(FileUtil
					.findUrl("character_table_panel.xml"));
			// this.characterTablePanel =
			// (AbstractGUIComponent)swix.render(FileUtil.findUrl("character_table_panel.xml"));
		} catch (Exception e) {
			log().fatal("Unable to render character table interface", e);
		}
	}

	private void initializeInterface() {
    // EventTableModel is from glazed list jar
		final EventTableModel<CharacterI> eventTableModel = new EventTableModel<CharacterI>(
				this.filteredCharacters, this.tableFormat);
		this.characterTable.setModel(eventTableModel);
		new TableComparatorChooser<CharacterI>(characterTable,
				this.sortedCharacters, false);
		this.characterTable.setSelectionModel(this.selectionModel);
		this.characterTable.putClientProperty("Quaqua.Table.style", "striped");
		if (!Config.inst().hasNCBIAdapter())
			this.commitButton.setText(this.getCommitButtonString());
		if (!Config.inst().uvicGraphIsEnabled()) {
			this.graphButton.getParent().remove(this.graphButton);
		}
		this.filterPanel.add(this.filter.getComponent());
		this.characterTable.addMouseListener(new PopupListener(
				new TableRightClickMenu(this.characterTable)));
		this.characterTablePanel.validate();
		this.selectionModel.addListSelectionListener(new SelectionListener());
		new TableColumnPrefsSaver(this.characterTable, this
				.getTableAutoSaveName());

		OntologyMakerI om = Config.inst().getOntMaker(representedGroup);
		if (om != null && om.useButtonToLaunch()) {
			// TermMakerManager.getTermMaker(group)??
			ontolMaker = om;
			ontolMakerButton.setText(om.getButtonText());
		} else {
			// remove from parent...
			ontolMakerButton.setVisible(false);
		}

	}

	private void addInitialBlankCharacter() {
		this.getEditManager().addInitialCharacter();
	}


	private void updateButtonStates() {
		final boolean hasSelection = !this.selectionModel.isSelectionEmpty();
		this.duplicateButton.setEnabled(hasSelection);
		this.deleteButton.setEnabled(hasSelection);
	}

	private void clearFilter() {
		this.filter.setFilter(null, this);
	}


	private EditManager getEditManager() {
		return EditManager.getEditManager(this.representedGroup);
	}

	private CharacterListManager getCharacterListManager() {
		return CharacterListManager.getCharListMan(this.representedGroup);
	}

	private LoadSaveManager getLoadSaveManager() {
		if (this.loadSaveManager == null) {
			this.loadSaveManager = new LoadSaveManager(this
					.getCharacterListManager());
		}
		return this.loadSaveManager;
	}

	private String getCommitButtonString() {
		if (Config.inst().hasQueryableDataAdapter())
			return Config.inst().getQueryableDataAdapter()
					.getCommitButtonLabel();
		else
			return CharacterTableController.SAVE_STRING;
	}

	private String getTableAutoSaveName() {
		return Config.inst().getConfigName() + this.representedGroup
				+ "CharacterTable";
	}

	private static Logger log() {
		return Logger.getLogger(CharacterTableController.class);
	}

  /** This seems to already know about add & delete - add here is just doing a select of
      characters added - update i dont understand */
	private class CharacterChangeListener implements CharChangeListener {
		public void charChanged(CharChangeEvent e) {
			if (e.isUpdate()) {
				for (CharacterI character : e.getTransaction().getCharacters()) {
					CharacterTableController.this
							.updateCharacterForGlazedLists(character);
				}
			} else if (e.isAdd()) {
				CharacterTableController.this.setSelectionWithCharacters(e
						.getTransaction().getCharacters());
			}
		}
	}
  /** select characters in table */
	private void setSelectionWithCharacters(List<CharacterI> characters) {
		this.clearFilter();
		this.selectionModel.clearSelection();
		for (CharacterI character : characters) {
			final int index = this.filteredCharacters.indexOf(character);
			if (index > -1) {
				this.selectionModel.addSelectionInterval(index, index);
				Rectangle rect = this.characterTable.getCellRect(index, 0,
						false);
				this.characterTable.scrollRectToVisible(rect);
			}
		}
	}

  // ????? this seems to be setting what its getting???
	private void updateCharacterForGlazedLists(CharacterI character) {
		final int index = this.getCharacterListManager().getCharacterList()
				.getList().indexOf(character);
		if (index > -1) {
			this.getCharacterListManager().getCharacterList().getList().set(
					index, character);
		}
	}

	/** Listens for loading of new data files and clears the search filter */
	private class CharacterListChangeListener implements CharListChangeListener {
		public void newCharList(CharListChangeEvent e) {
			CharacterTableController.this.clearFilter();
			if (!CharacterTableController.this.filteredCharacters.isEmpty()) {
				CharacterTableController.this.selectionModel
						.setSelectionInterval(0, 0);
			}
			setComponentTitleFromFilename();
		}
	}


	private class SelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			CharacterTableController.this.updateButtonStates();
		}
	}

	private class PopupListener extends MouseAdapter {
		JPopupMenu popup;
		int col;
		int row;
		Point p;

		PopupListener(JPopupMenu popupMenu) {
			popup = popupMenu;
		}

		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			p = e.getPoint();
			col = CharacterTableController.this.characterTable.getTableHeader()
					.columnAtPoint(p);
			row = CharacterTableController.this.characterTable.rowAtPoint(p);
			if (e.isPopupTrigger()) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	public void setComponentTitleFromFilename() {
		// String title = CharacterTableController.this.getTableAutoSaveName();
		String title = CharacterListManager.inst().getCurrentDataFile()
				.getName();
		// if (title==null)
		// title="error";
		// ComponentManager.getManager().setLabel(this.characterTablePanel,title);

	}

}
