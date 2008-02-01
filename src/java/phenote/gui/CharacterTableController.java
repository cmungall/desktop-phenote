package phenote.gui;

import java.awt.Container;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;
import org.swixml.SwingEngine;

import phenote.config.Config;
import phenote.dataadapter.CharListChangeEvent;
import phenote.dataadapter.CharListChangeListener;
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.ConstraintManager;
import phenote.dataadapter.ConstraintStatus;
import phenote.dataadapter.LoadSaveManager;
import phenote.dataadapter.OntologyMakerI;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterIFactory;
import phenote.datamodel.OboUtil;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;
import phenote.edit.EditManager;
import phenote.gui.field.CharFieldMatcherEditor;
import phenote.util.EverythingEqualComparator;
import phenote.util.FileUtil;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

public class CharacterTableController {

  private static final int ROW_HEIGHT = 16;
	private static final String SAVE_STRING = "Save Data";
	private String representedGroup = "default";
	private SortedList<CharacterI> sortedCharacters;
	private FilterList<CharacterI> filteredCharacters;
	private EventSelectionModel<CharacterI> selectionModel;
  //private CharacterTableFormat tableFormat;
  private AdvancedTableFormat<CharacterI> tableFormat;
	private LoadSaveManager loadSaveManager;
	private CharFieldMatcherEditor filter;
	// private AbstractGUIComponent characterTablePanel; // initialized by swix
	private JPanel characterTablePanel; // initialized by swix
	private JTable characterTable; // initialized by swix
	private JButton addButton; // initialized by swix
	private JButton duplicateButton; // initialized by swix
	private JButton deleteButton; // initialized by swix
	private JButton undoButton; // initialized by swix
	private JButton commitButton; // initialized by swix
	private JButton graphButton; // initialized by swix
	private JPanel filterPanel; // initialized by swix
  private JPanel buttonPanel; // initialized by swix
	private JButton ontolMakerButton; // initialized by swix
  private JPanel ontolMakerSpacer;  // initialized by swix
	private OntologyMakerI ontolMaker; // for now just 1 term maker...
	private JButton compareButton; // initialized by swix -> compare()
  private JPanel compareSpacer;	// initialized by swix
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
    characterTable.setDefaultRenderer(Object.class,new CharTableRenderer());
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

    ConstraintStatus cs = ConstraintManager.inst().checkCommitConstraints();
    
    // FAILURE - no commit
    if (cs.isFailure()) { 
      JOptionPane.showMessageDialog(null,cs.getFailureMessage(),"Commit failed",
                                    JOptionPane.ERROR_MESSAGE);
      return;
    }

    // WARNING - ask user if still wants to commit
    if (cs.isWarning()) {
      String m = "There is a problem with your commit:\n\n"+cs.getWarningMessage()
        +"\n\nDo you want to commit anyway?";
      int ret = JOptionPane.showConfirmDialog(null,
                                              m,
                                              "Commit Warning",
                                              JOptionPane.YES_NO_OPTION,
                                              JOptionPane.ERROR_MESSAGE);
      if (ret != JOptionPane.YES_OPTION)
        return;
    }

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

  /** this method is fired when comparison button is pressed (if configged)
      set up as action in conf/character_table_panel.xml swixml config
      bring up comparison window to compare 2 statements - by default only
      do 1 comparison at a time (may have pref for doing multiple with commit button?
      for now if 2 statements not selected then put up error message
      (eventually put drag & drop in comparator)  */
  public void compare() {
    List<CharacterI> selChars = getSelectionModel().getSelected();
    if (selChars.size() != 2) {
      String m = "You must have exactly 2 rows selected to do comparison(for now)";
      JOptionPane.showMessageDialog(null,m,"Error",JOptionPane.ERROR_MESSAGE);
      return;
    }
    // could just Phenote.inst().getFrame(), but phenote2??
    // should at least probably make this a util fn
    Container c = characterTablePanel.getTopLevelAncestor();
    Frame frame = null;
    if (c instanceof Frame) frame = (Frame)frame;
    else log().error("Top level ancestor of table is not a Frame");

    CharacterI c1 = selChars.get(0);
    CharacterI c2 = selChars.get(1);
    new Comparison(frame,c1,c2);
  }

	public EventSelectionModel<CharacterI> getSelectionModel() {
		return this.selectionModel;
	}
	
	public String getGroup() {
	  return this.representedGroup;
	}
	
	 /**
   * This method is temporary and should be removed once we move fully
   * to Phenote 2.  This method should only be called in Phenote 2; 
   * the old buttons are left in for compatibility with Phenote 1.
   */
  public void removeOldButtons() {
    this.getCharacterTablePanel().remove(this.buttonPanel);
    this.getCharacterTablePanel().validate();
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
		// Remove keyboard actions from table since we are custom handling these from menu accelerators
		this.characterTable.getActionMap().getParent().remove("copy");
    this.characterTable.getActionMap().getParent().remove("paste");
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
      // this is the awkward side of swixml i suppose - as you then have to 
      // actively set unvis button and panel spacer, rather than just not adding
			// remove from parent... - setting vis to false is apparently sufficient
			ontolMakerButton.setVisible(false);
      //buttonPanel.remove(ontolMakerButton); // dont need
      ontolMakerSpacer.setVisible(false);
		}

    // Take out comparison buttons if not configged for them
    if (!Config.inst().compareStatementEnabled() 
        || !CharacterIFactory.supportsComparisons()) {
			compareButton.setVisible(false);
      compareSpacer.setVisible(false);
      // if configured for comparable, but dont have comparable datamodel - error
      if (Config.inst().compareStatementEnabled()) {
        String m="Error: Comparisons are configged, but configged datamodel does not "
          +"support comparisons.\nDisabling.\nTry configging OBO_ANNOTATION mode";
       JOptionPane.showMessageDialog(null,m,"error",JOptionPane.ERROR_MESSAGE);
      }
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

  /**
   * The table is automatically updated through Glazed Lists (with EventList and
   * EventTableModel).  But this lets us find out when a character is edited, so 
   * we can tell the table to refresh that row.  Glazed Lists doesn't automatically 
   * select newly inserted objects, so we listen for the add here and then select
   * those rows.
   */
	private class CharacterChangeListener implements CharChangeListener {
		public void charChanged(CharChangeEvent e) {
      List<CharacterI> chars = e.getTransaction().getCharacters();
      setRowHeights(chars);
			if (e.isUpdate()) {
				for (CharacterI character : chars) {
					CharacterTableController.this
							.updateCharacterForGlazedLists(character);
				}
			} else if (e.isAdd()) {
				CharacterTableController.this.setSelectionWithCharacters(chars);
			}
		}
	}

  private void setRowHeights(List<CharacterI> chars) {
    for (CharacterI c : chars) {
      int i = filteredCharacters.indexOf(c);
      //if (i < 0) return; // ??
      int numRows = getNumRowsForChar(c);
      characterTable.setRowHeight(i,ROW_HEIGHT*numRows);
    }
  }

  /** Return # of lines needed for char. If char has post comp then an extra line
   * 	for every diff
   * @param c charcterI to examine
   * @return number of lines
   */
  private int getNumRowsForChar(CharacterI c) {
    int lines = 1;
    for (CharField cf : CharFieldManager.inst().getPostCompFields()) {
      OBOClass term = c.getValue(cf).getTerm();
      if (term != null) {
        int termLines = 1 + OboUtil.getNumOfDiffs(term);
        if (termLines > lines) lines = termLines;
      }
    }
    return lines;
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

	/**
	 * This method does nothing but tell Glazed Lists that the object has been updated.
	 * This triggers the EventTableModel, which is part of Glazed Lists, to update the 
	 * display of the row value in the table.
	 */
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

  /** This is clearly unfinshed as it doesnt do anything - everything is commented
      out. I think it means to set the title of the component when theres a new
      data load - current data file is set by loading new files - however loading
      from a db (eg worm) doesnt set a file - this should be more genereically named
      so loading from db can utilize this and this should be wired up to actually
      set component window title in phenote2 as i think john intended here
      here is his note from cvs commit of this:
      Fixed some component/factory titling problems, fixed a bug where Phenote Editor
      couldn't be the first component displayed */
	public void setComponentTitleFromFilename() {
		// String title = CharacterTableController.this.getTableAutoSaveName();
    java.io.File dataSource =  CharacterListManager.inst().getCurrentDataFile();
    if (dataSource == null) return;
		String title = CharacterListManager.inst().getCurrentDataFile()
				.getName();
		// if (title==null)
		// title="error";
		// ComponentManager.getManager().setLabel(this.characterTablePanel,title);

	}

  /** Breaks post comps (and should do lists) into multiple lines */
  private class CharTableRenderer extends DefaultTableCellRenderer {
    public void setValue(Object value) {
      if (value == null || value.toString()==null) setText("");
      String s = value.toString();
      s = s.replaceAll("\\^","<br>");
      setText("<html>"+s);
    }
  }
}
