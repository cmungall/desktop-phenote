package phenote.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.bbop.swing.HyperlinkLabel;
import org.bbop.swing.StringLinkListener;
import org.bbop.framework.AbstractGUIComponent;
import org.bbop.framework.GUIComponent;
import org.bbop.framework.GUIComponentWrapper;
import org.bbop.framework.GUIManager;
import org.bbop.framework.GUIComponentFactory;
import org.bbop.framework.ComponentManager;
import org.obo.datamodel.Dbxref;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.DanglingObject;
import org.obo.datamodel.Link;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBORestriction;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.ObsoletableObject;
import org.obo.datamodel.PropertyValue;
import org.obo.datamodel.Synonym;
import org.obo.util.TermUtil;
import org.bbop.swing.SwingUtil;

import phenote.datamodel.CharFieldManager;
import phenote.datamodel.Ontology;
import phenote.datamodel.TermNotFoundException;
import phenote.gui.selection.SelectionManager;
import phenote.gui.selection.TermSelectionEvent;
import phenote.gui.selection.TermSelectionListener;
import phenote.gui.selection.UseTermListener;
import phenote.util.HtmlUtil;
import phenote.util.LinkCollection;
import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.BrowserLauncherRunner;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

/**
 * This is the second implementation of the Term Info window to provide
 * read-only information about ontology terms for the user.
 * <p>
 * This constructs the toolbar as well as the information panels.
 * <p>
 * This uses the {@link StackedBox} and {@link JXCollapsablePane} classes.
 * <p>
 * 
 * Things to fix:
 * <p>
 * <ul>
 * <li>make more generic...so that components can be moved around, right now
 * they are in a set order</li>
 * <li>make sure text doesn't go off the panel to the right. esp important for
 * the term name and definition, as they seem to be having problems</li>
 * <li>fancy things might include remembering state between sessions</li>
 * <li>should be able to close a panel, and never display if not interested
 * perhaps regain it again through a right-click menu or something. could be
 * something for the 'configure this panel' when we move to john's gui stuff</li>
 * <li>The whole termInfo panel should probably be an array of panels, or at least
 * they should be kept track of that way.  that will reduce the size of the code a bit
 * and will make it easier when it comes time for customization to be able to swap 
 * panels around, so that particular panels won't be married to a particular position.</li>
 * </ul>
 * 
 * @author Nicole Washington
 * 
 */
public class TermInfo2 extends AbstractGUIComponent {

	// constants
	private static final int TERM_INFO_DEFAULT_WIDTH = 350;

	private static final int TERM_INFO_DEFAULT_HEIGHT = 400;

	private static final int BUTTON_HEIGHT = 30;

	private static Border contentBorder = BorderFactory.createEmptyBorder(6, 8,
			6, 8);

	// content variables
	private OBOClass currentOboClass;

	private UseTermListener useTermListener;

	private TermHyperlinkListener termHyperlinkListener;

	private static List<String> termInfoNaviHistory = new ArrayList<String>();

	// private List termInfoNaviHistory = new List();
	private static int naviIndex = -1;

	private SelectionManager selectionManager;

	// gui components

	private TermInfoToolbar termInfoToolbar;

	private JScrollPane termInfoScroll;

	private StackedBox termInfoPanel;

	private JPanel basicInfoPanel;

	private JLabel termName;

	private JLabel termID;

	private JLabel ontologyName;

	private HyperlinkLabel definitionTextArea;

	private JPanel considerReplacePanel;

	private JTextArea considerTerms;

	private JTextArea replacementTerms;

	private JPanel synonymPanel;

	// private JTextArea synonyms;

	private JPanel dbxrefPanel;

	private JTextArea dbxrefText;

	private JPanel xpDefPanel;

	private JTextArea xpDefList;

	private JPanel propertyValuesPanel;

	// private JTextArea propValsList;

	private JPanel ontologyLinksPanel;

	private JPanel parentsPanel;

	private JTextArea parentsText;

	private JPanel childrenPanel;

	private JTextArea childrenText;

	private JPanel commentsPanel;

	private JTextArea commentsText;

	private static TermInfo2 singleton;
	
	private boolean showEmptyPanelsFlag = false;
	
	private static final String basicInfoPanelName = "BASIC"; //0
	private static final String considerReplacePanelName = "CONSIDERS"; //2
	private static final String synonymPanelName = "SYNONYMS"; //4
	private static final String xpdefsPanelName = "XPDEFS"; //6
	private static final String linksPanelName = "LINKS"; //8
	private static final String dbxrefPanelName = "DBXREFS";  //10
	private static final String propvalsPanelName = "PROPVALS";  //12
	private static final String commentsPanelName = "COMMENTS";  //14
	
	/** this sets the order of the panels */
	private static String[] panels = {basicInfoPanelName, considerReplacePanelName, synonymPanelName, xpdefsPanelName, linksPanelName, dbxrefPanelName, propvalsPanelName, commentsPanelName};


	/**
	 * Create the panel
	 */

	public TermInfo2() { // TermPanel termPanel) {
		this(SelectionManager.inst());
	}

	public TermInfo2(SelectionManager selManager) {
		super("term-info:term-info");
		initTermInfo();
		this.selectionManager = selManager;
		this.selectionManager
				.addTermSelectionListener(new InfoTermSelectionListener());
		// ErrorManager.inst().addErrorListener(new InfoErrorListener());
	}

	public static TermInfo2 inst() {
		if (singleton == null) singleton = new TermInfo2();
		return singleton;
	}

	private void initTermInfo() {

		// create the panel the whole thing will live in (including toolbars,
		// etc.)
		this.setLayout(new BorderLayout(0,0));
		this.setPreferredSize(new Dimension(TERM_INFO_DEFAULT_WIDTH,TERM_INFO_DEFAULT_HEIGHT));

		// create the toolbar
		termInfoToolbar = new TermInfoToolbar();
		this.add(termInfoToolbar, BorderLayout.NORTH);

		// create the stackedbox that the term info will live in
		termInfoPanel = new StackedBox();

		// put it in a scrollpane
		termInfoScroll = new JScrollPane(termInfoPanel);
		termInfoScroll.setBorder(null);

		// put the scrollpane into the whole bucket
		this.add(termInfoScroll, BorderLayout.CENTER);

		// The first part, which includes all the basic information,
		// ontology, id, def
		basicInfoPanel = new JPanel();
		basicInfoPanel.setOpaque(false);
		basicInfoPanel.setBackground(Color.WHITE);
		basicInfoPanel.setLayout(new SpringLayout());
		basicInfoPanel.setBorder(contentBorder);
		basicInfoPanel.setName(basicInfoPanelName);

		// Create and populate the panel.
		JLabel nameLabel = new JLabel("Term: ", JLabel.TRAILING);
		basicInfoPanel.add(nameLabel);
		termName = new JLabel();
		termName.setText("(No Selection)");
		nameLabel.setLabelFor(termName);
		basicInfoPanel.add(termName);

		JLabel idLabel = new JLabel("ID: ", JLabel.TRAILING);
		basicInfoPanel.add(idLabel);
		termID = new JLabel();
		idLabel.setLabelFor(termID);
		basicInfoPanel.add(termID);

		ontologyName = new JLabel();
		JLabel ontologyLabel = new JLabel("Ontology: ", JLabel.TRAILING);
		basicInfoPanel.add(ontologyLabel);
		ontologyLabel.setLabelFor(ontologyName);
		basicInfoPanel.add(ontologyName);

		JLabel definitionLabel = new JLabel("Definition: ", JLabel.TRAILING);
		definitionLabel.setVerticalAlignment(JLabel.TOP);

		basicInfoPanel.add(definitionLabel);
		definitionTextArea = new HyperlinkLabel();
		definitionLabel.setLabelFor(definitionTextArea);
		basicInfoPanel.add(definitionTextArea);

		SpringLayout layout = new SpringLayout();
		// line up the rel type with the items
		basicInfoPanel.setLayout(layout);

		
		// Lay out the panel.
		SpringUtilities.makeCompactGrid(basicInfoPanel, 4, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad

		layout.putConstraint(SpringLayout.NORTH, definitionLabel, 0, SpringLayout.NORTH, definitionTextArea);

		termInfoPanel.addBox("Basic Info", basicInfoPanel);

		considerReplacePanel = new JPanel();
		considerReplacePanel.setOpaque(false);
		considerReplacePanel.setLayout(new SpringLayout());
		considerReplacePanel.setBorder(contentBorder);
		considerReplacePanel.setBackground(Color.WHITE);
		considerTerms = new JTextArea();
		considerReplacePanel.add(considerTerms);
		replacementTerms = new JTextArea();
		considerReplacePanel.add(replacementTerms);
		considerReplacePanel.setName(considerReplacePanelName);

		termInfoPanel.addBox("Consider & Replacement Terms", considerReplacePanel);

		// Add the next section, which is the synonyms
		synonymPanel = new JPanel();
		synonymPanel.setOpaque(false);
		synonymPanel.setLayout(new SpringLayout());
		synonymPanel.setBorder(contentBorder);
		synonymPanel.setName(synonymPanelName);


		termInfoPanel.addBox("Synonyms", synonymPanel);


		// xp definitions
		xpDefPanel = new JPanel();
		xpDefPanel.setBackground(Color.WHITE);
		xpDefPanel.setLayout(new BoxLayout(xpDefPanel, BoxLayout.PAGE_AXIS));
		xpDefList = new JTextArea();
		xpDefList.setLineWrap(true);
		xpDefList.setText(" ");
		xpDefPanel.add(xpDefList);
		xpDefPanel.setName(xpdefsPanelName);

		termInfoPanel.addBox("Cross Product Definitions", xpDefPanel);


		//Links - parents and children
		ontologyLinksPanel = new JPanel();
		ontologyLinksPanel.setBackground(Color.WHITE);
		ontologyLinksPanel.setName(linksPanelName);

		ontologyLinksPanel.setLayout(new SpringLayout());
		termInfoPanel.addBox("Parents & Children", ontologyLinksPanel);

		parentsPanel = new JPanel(new SpringLayout());
		parentsText = new JTextArea();
		parentsPanel.add(parentsText);
		parentsPanel.setBackground(Color.WHITE);
		ontologyLinksPanel.add(parentsPanel);

		childrenPanel = new JPanel(new SpringLayout());
		childrenText = new JTextArea();
		childrenPanel.add(childrenText);
		childrenPanel.setBackground(Color.WHITE);
		ontologyLinksPanel.add(childrenPanel);

		SpringUtilities.makeCompactGrid(ontologyLinksPanel, 2, 1, // rows,
				// cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
		
		// dbxrefs
		dbxrefPanel = new JPanel();
		dbxrefPanel.setBackground(Color.WHITE);
		dbxrefPanel.setLayout(new SpringLayout());
		dbxrefText = new JTextArea();
		dbxrefPanel.add(dbxrefText);
		dbxrefPanel.setName(dbxrefPanelName);
		// dbxrefPanel.add(dbxrefsList);

		termInfoPanel.addBox("DBxrefs", dbxrefPanel);

		
		// propertyValues (often used for OWL ontologies)
		propertyValuesPanel = new JPanel();
		propertyValuesPanel.setOpaque(false);
		propertyValuesPanel.setLayout(new SpringLayout());
		propertyValuesPanel.setBorder(contentBorder);
		propertyValuesPanel.setName(propvalsPanelName);

		termInfoPanel.addBox("Other Properties", propertyValuesPanel);

		commentsPanel = new JPanel();
		commentsPanel.setOpaque(false);
		commentsPanel.setBackground(Color.WHITE);
		commentsPanel.setLayout(new BoxLayout(commentsPanel, BoxLayout.PAGE_AXIS));
		commentsText = new JTextArea();
		commentsText.setLineWrap(true);
		commentsText.setWrapStyleWord(true);
		commentsPanel.add(commentsText);
		commentsPanel.setBorder(contentBorder);
		commentsPanel.setName(commentsPanelName);


		termInfoPanel.addBox("Comments", commentsPanel);

		// refresh
		this.validate();
		this.setVisible(true);

//		add(entirePanel);
	}

//	public static JComponent getComponent() {
//		return entirePanel;
//	}

	private void setTextFromOboClass(OBOClass oboClass) {
		currentOboClass = oboClass;

		// basicInfoPanel
		if (!oboClass.isObsolete()) {
			termInfoPanel.setBoxTitleBackgroundColor(0, Color.LIGHT_GRAY);
			termInfoPanel.setBoxTitle("Basic Info", 0);
			// hide the consider/replace panel
			termInfoPanel.getComponent(2).setVisible(false);
			considerReplacePanel.setVisible(false);
		} else {
			termInfoPanel.setBoxTitleBackgroundColor(0, Color.RED);
			termInfoPanel.setBoxTitle("Basic Info  [OBSOLETE]", 0);
			// show the consider/replacements
			termInfoPanel.getComponent(2).setVisible(true);
			considerReplacePanel.setVisible(true);
		}

		termInfoToolbar.setTermFieldText(oboClass);
		termID.setText(oboClass.getID());
		ontologyName.setText(oboClass.getNamespace().toString());
		definitionTextArea.setText("<html>"+oboClass.getDefinition()+"</html>");

		if ((!showEmptyPanelsFlag) && ((oboClass.getConsiderReplacements().size()+oboClass.getReplacedBy().size()) == 0)) {
			termInfoPanel.getComponent(2).setVisible(false);
			considerReplacePanel.setVisible(false);
		} else {
			termInfoPanel.getComponent(2).setVisible(true);
			// Consider/replace panel
			makeObsPanel(oboClass);
			termInfoPanel.setBoxTitle("Consider ("
					+ oboClass.getConsiderReplacements().size() + ") & Replacement ("
					+ oboClass.getReplacedBy().size() + ") Terms", 2);
			// if there are items here, should always expand. perhaps always
			// collapse
			// all except comments
			considerReplacePanel.validate();
			considerReplacePanel.repaint();
			considerReplacePanel.setVisible(true);
		}
		
		if ((!showEmptyPanelsFlag) && (oboClass.getSynonyms().size() == 0)) {
			termInfoPanel.getComponent(4).setVisible(false);
			synonymPanel.setVisible(false);
		} else {
			// SynonymPanel
			termInfoPanel.getComponent(4).setVisible(true);
			makeSynPanel(oboClass.getSynonyms());
			termInfoPanel.setBoxTitle("Synonyms (" + oboClass.getSynonyms().size()
					+ ")", 4);
			synonymPanel.validate();
			synonymPanel.repaint();
			synonymPanel.setVisible(true);
		}
		
		
		int linkCount = 0;

		// xpDefPanel
		linkCount = makeLinksPanel(oboClass.getParents(), true, false, xpDefPanel);
		termInfoPanel.setBoxTitle("Cross-product Definitions (" + linkCount + ")",
				6);
		if ((!showEmptyPanelsFlag) && (linkCount == 0)) {
			termInfoPanel.getComponent(6).setVisible(false);
			xpDefPanel.setVisible(false);
		} else {
			termInfoPanel.getComponent(6).setVisible(true);
			xpDefPanel.setVisible(true);
		}


		// parentsPanel
		//always show the links panels, even if no parents/children
		linkCount = makeLinksPanel(oboClass.getParents(), false, false,
				parentsPanel);
		parentsPanel.validate();
		parentsPanel.repaint();

		// childrenPanel
		linkCount += makeLinksPanel(oboClass.getChildren(), false, true,
				childrenPanel);
		childrenPanel.validate();
		childrenPanel.repaint();


		termInfoPanel.setBoxTitle("Links (" + linkCount + ")", 8);

		
		// dbxrefsPanel
		if ((!showEmptyPanelsFlag) && (oboClass.getDbxrefs().size() == 0)) {
			termInfoPanel.getComponent(10).setVisible(false);
			dbxrefPanel.setVisible(false);
		} else {
			termInfoPanel.getComponent(10).setVisible(true);
			makeDbxrefPanel(oboClass.getDbxrefs());
		termInfoPanel.setBoxTitle("DBxrefs (" + oboClass.getDbxrefs().size() + ")",
				10);
		dbxrefPanel.validate();
		dbxrefPanel.repaint();
		dbxrefPanel.setVisible(true);
		}

		// propValues
		// propertyValuesList.setText(oboClass.getPropertyValues().toString());
		linkCount = makePropValsPanel(oboClass.getPropertyValues());
		termInfoPanel.setBoxTitle("Other Properties (" + linkCount + ")", 12);
		if (!showEmptyPanelsFlag && (linkCount == 0)) {
			termInfoPanel.getComponent(12).setVisible(false);
			propertyValuesPanel.setVisible(false);
		} else {
			termInfoPanel.getComponent(12).setVisible(true);
			propertyValuesPanel.setVisible(true);
		}

		propertyValuesPanel.validate();
		propertyValuesPanel.repaint();

		// commentsPanel
		commentsText.setText(oboClass.getComment().toString());
		termInfoPanel.getComponent(14).setVisible(true);
		commentsPanel.setVisible(true);
		if (oboClass.getComment().length() == 0) {
			termInfoPanel.setBoxTitle("Comments (none)", 14);
			if (!showEmptyPanelsFlag) {
				termInfoPanel.getComponent(14).setVisible(false);
				commentsPanel.setVisible(false);
			}
		} else {
			termInfoPanel.setBoxTitle("Comments*", 14);
		}
		commentsPanel.validate();
		commentsPanel.repaint();

		// this refreshes the main panel
		// this should move the scrollbar to the top, but it doesn't!
		termName.setText(oboClass.getName());
		basicInfoPanel.validate();
		basicInfoPanel.repaint();
		termInfoScroll.getVerticalScrollBar().setValue(0);
		termInfoScroll.getViewport().setViewPosition(new Point(0, 0));
		termInfoPanel.validate();
		termInfoPanel.repaint();
		this.validate();
		this.repaint();
		// entirePanel.setVisible(true);

	}
	public void setComponentTitleFromOBOClass (OBOClass oboClass) {
		String title = "Term Info: "+ oboClass.getName();
		ComponentManager.getManager().setLabel(this,title);

	}

	/** Listen for selection from phenote (mouse over completion list) */
	private class InfoTermSelectionListener implements TermSelectionListener {
		public void termSelected(TermSelectionEvent e) {

			if (!e.isMouseOverEvent()) {
				// add the item to the navi history if selected from list only
				String id = e.getOboClass().getID();
				addTermToNaviHistory(id);
				return;
			}
			setTextFromOboClass(e.getOboClass());
			// This sets who now listens to use term button clicks (only 1
			// listener)
			setUseTermListener(e.getUseTermListener());
			//change the name of the item being browsed in the term info header
//			GUIComponent gc = ComponentManager.getManager().getActiveComponent("term-info");
//			GUIComponent gc2 = SwingUtil.getAncestorOfClass(GUIComponent.class, (JComponent)e.getSource());
//			if (gc2!=null) {
			setComponentTitleFromOBOClass(e.getOboClass());
//			} else {
//				System.out.print("can't find component or not active!");
//			}
		}
	}

	private void setUseTermListener(UseTermListener utl) {
		useTermListener = utl;
	}

	private void addTermToNaviHistory(String link) {
		int tot = termInfoNaviHistory.size();
		// int tot = termInfoNaviHistory.getItemCount();
		if ((tot - 1) > naviIndex) { // we're in the middle of the navi
			if (naviIndex >= 0) { // we're not at the beginning
				// remove all the items between end and here
				for (int i = (tot - 1); i > naviIndex; i--) {
					termInfoNaviHistory.remove(i);
				}
			}
		}
		termInfoNaviHistory.add(link);
		// termComboBox.insertItemAt(link,0);
		// termComboBox.setSelectedIndex(0);
		naviIndex++; // we should be at the end of the history
		// System.out.println("tot="+tot+"; naviIndex="+naviIndex);
	}

	public static String getTermFromNaviHistory(int position) {
		if (termInfoNaviHistory.size() >= 1)
			// if (termInfoNaviHistory.getItemCount() >= 1)
			return termInfoNaviHistory.get(position).toString();
		// return termInfoNaviHistory.getItem(position);
		else
			return "";
	}

	/**
	 * inner class TermHyperlink Listener, listens for clicks on term & external
	 * hyper links and brings up the term or brings up the external web page
	 */
	private class TermHyperlinkListener implements StringLinkListener,
			HyperlinkListener {

		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (!(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)) return;

			URL url = e.getURL();
			System.out.println("got url "+url+" desc "+e.getDescription());

			// internal link to term...
			if (HtmlUtil.isPhenoteLink(e)) {
				bringUpTermInTermInfo(e);
				return;
			}

			if (url == null) { // relative urls are null
				System.out.println("invalid url " + url);
				return;
			}

			bringUpInBrowser(url);

		}

		private void bringUpInBrowser(URL url) {
			if (url == null) return;
			try {
				BrowserLauncher bl = new BrowserLauncher(null); // no logger
				BrowserLauncherRunner br = new BrowserLauncherRunner(bl,
						url.toString(), null);
				new Thread(br).start();
			} catch (BrowserLaunchingInitializingException be) {
				System.out.println("cant launch browser " + be);
			} catch (UnsupportedOperatingSystemException ue) {
				System.out.println("cant launch browser " + ue);
			}
		}

		private void bringUpTermInTermInfo(HyperlinkEvent e) {
			// or do through obo session?
			String id = HtmlUtil.getIdFromHyperlink(e);
			if (id == null) return;
			bringUpTermInTermInfo(id);
//			try {
//				OBOClass term = CharFieldManager.inst().getOboClass(id); // ex
//				setTextFromOboClass(term);
//				addTermToNaviHistory(id);
//				// send out term selection (non mouse over) for DAG view
////				TermInfo2.this.selectionManager.selectTerm(TermInfo2.this, term, true);
//				//				TermInfo2.this.selectionManager.selectTerm(TermInfo2.this, term, true);
//			} catch (TermNotFoundException ex) {
//				return;
//			}
		}

		private void bringUpTermInTermInfo(String id) {
			// or do through obo session?
			if (id == null) return;
//			try {
				for(Ontology o : CharFieldManager.inst().getAllOntologies()) {
					OBOSession session = o.getOboSession();
					IdentifiedObject io = session.getObject(id);
					if (io instanceof OBOClass) {
						OBOClass c = (OBOClass) io;
						setTextFromOboClass(c);
						break;
					}
				}
//				OBOClass term = CharFieldManager.inst().getOboClass(id); // ex
//				setTextFromOboClass(term);
//				addTermToNaviHistory(id);
//				// send out term selection (non mouse over) for DAG view
//				TermInfo2.this.selectionManager.selectTerm(TermInfo2.this, term, true);
//			} catch (TermNotFoundException ex) {
//				return;
//			}
		}

		public void link(String href) {
			bringUpTermInTermInfo(href);
			// TODO Auto-generated method stub

		}

	}

	private void makeSynPanel(Set someSet) {
		// private JList makeSynList (Set someSet) {

		String[] synTypes = { "BROAD", "NARROW", "EXACT", "RELATED", "OTHER" };
		String[] syns = { "", "", "", "", "" };
		int numSynTypes = 5;
		int rowCount = 0;

		Synonym syn;

		int index = -1;
		for (Iterator it = someSet.iterator(); it.hasNext();) {
			syn = (Synonym) it.next();
			if (syn.getScope() == Synonym.BROAD_SYNONYM) {
				syns[0] += " " + syn + "\n";
			} else if (syn.getScope() == Synonym.NARROW_SYNONYM) {
				syns[1] += " " + syn + "\n";
			} else if (syn.getScope() == Synonym.EXACT_SYNONYM) {
				syns[2] += " " + syn + "\n";
			} else if (syn.getScope() == Synonym.RELATED_SYNONYM) {
				syns[3] += " " + syn + "\n";
			} else if (syn.getScope() == Synonym.UNKNOWN_SCOPE) {
				syns[4] += " " + syn + "\n";
			} else {
				syns[4] += " " + syn + "\n";
			}
		}

		synonymPanel.removeAll(); // clear out the old ones
		synonymPanel.setBorder(contentBorder);
		synonymPanel.setOpaque(false);
		synonymPanel.setBackground(Color.WHITE);

		// create the synonym items
		for (int i = 0; i < numSynTypes; i++) {
			if (syns[i].length() > 0) { // only add the item if there's this
				// category
				JLabel synTypeLabel = new JLabel(synTypes[i], JLabel.TRAILING);
				synTypeLabel.setVerticalAlignment(JLabel.TOP);

				synonymPanel.add(synTypeLabel);
				JTextArea synList = new JTextArea();
				synList.setLineWrap(true);
				synList.setWrapStyleWord(true);
				synList.setText(syns[i]);
				synTypeLabel.setLabelFor(synList);
				synonymPanel.add(synList);
				rowCount++;
				SpringLayout layout = new SpringLayout();
				// line up the rel type with the items
				layout.putConstraint(SpringLayout.NORTH, synTypeLabel, 5,
						SpringLayout.NORTH, synonymPanel);
				layout.putConstraint(SpringLayout.NORTH, synList, 5,
						SpringLayout.NORTH, synonymPanel);
				synonymPanel.setLayout(layout);

			}
		}

		// Lay out the panel.
		SpringUtilities.makeCompactGrid(synonymPanel, rowCount, 2, // rows,
				// cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
		synonymPanel.setVisible(true);
	}

	private void makeDbxrefPanel(Set someSet) {
		String[] dbxrefSources = {};
		int rowCount = 0;
		String tempID;
		OBOClass tempOboClass = null;
		String panelText = "<html>";
		Dbxref dbxrefObj;
		dbxrefPanel.removeAll(); // clear out the old ones

		for (Iterator it = someSet.iterator(); it.hasNext();) {
			dbxrefObj = (Dbxref) it.next();
			if (dbxrefObj != null) {
				// will make this linkable - internal & external
				// eventually, get smart and enable adding ontology!
				tempID = dbxrefObj.getDatabase() + ":" + dbxrefObj.getDatabaseID();
				JLabel dbxrefItem = new JLabel(" (" + tempID + ")");

				// check if the term is in the current obosession
				try {
					tempOboClass = CharFieldManager.inst().getOboClass(tempID);
				} catch (TermNotFoundException ex) {
					tempOboClass = null;
				}

				if (tempOboClass != null) {
					panelText += HtmlUtil.termLink(tempOboClass) + " (" + tempID + ")"; // use
																																							// the
																																							// name
																																							// &
																																							// link
				} else {
					panelText += tempID; // just use the ID for external refs
				}
				// dbxrefPanel.add(dbxrefItem);
				rowCount++;
			}
			if (it.hasNext()) {
				panelText += "<br>";
			}
		}
		HyperlinkLabel textArea = new HyperlinkLabel();
		termHyperlinkListener = new TermHyperlinkListener();
		textArea.addStringLinkListener(termHyperlinkListener);
		panelText += "</html>";
		textArea.setText(panelText);
		dbxrefPanel.add(textArea);
		dbxrefPanel.validate();
		dbxrefPanel.repaint();

		// Lay out the panel.
		if (rowCount > 0) {
			SpringUtilities.makeCompactGrid(dbxrefPanel, 1, 1, // rows, cols
					6, 6, // initX, initY
					6, 6); // xPad, yPad
		}
		dbxrefPanel.setVisible(true);
	}

	/**
	 * Given a collection of links, a {@link JPanel} is return to the implementing
	 * class that contains a set of hyperlinks that will trigger refresh to that
	 * new term.
	 * <p>
	 * 
	 * @param links
	 *          a collection of oboclass links to display in a panel
	 * @param isXP
	 *          flag to indicate if panel is to display cross-products or regular
	 *          links
	 * @param isChild
	 *          flag indicating if a panel displays parent or child links. This
	 *          may be important in the future for display purposes.
	 * @param panel
	 *          The panel into which the links will be placed.
	 * @return the number of links created
	 */
	private int makeLinksPanel(Collection<Link> links, boolean isXP,
			boolean isChild, JPanel panel) {

		// would be best to sort these into alpha order
		IdentifiedObject temp;

		panel.removeAll();
		List<LinkCollection> allLinks = linksList(links, isXP, isChild);
		JLabel linkLabel;
		JLabel typeLabel;
		panel.setBorder(contentBorder);
		panel.setOpaque(false);
		panel.setBackground(Color.WHITE);
		int rowCount = 0;
		int itemCount = 0;
		int totalItems = 0;
		String panelText = "";
		SpringLayout layout = new SpringLayout();

		if (isChild) { 
			linkLabel = new JLabel("<html><b>Children</b></html>");
		} else {
			linkLabel = new JLabel("<html><b>Parents</b></html>");
		}
		if (!isXP) {
			panel.add(new JLabel("")); //blank column for spring layout
			panel.add(linkLabel);
			rowCount+=1;
		}
		for (ListIterator<LinkCollection> lit = allLinks.listIterator(); lit
				.hasNext();) {
			// group by each relationship type
			LinkCollection linkCol = (LinkCollection) lit.next();
			List<Link> listOfLinks = linkCol.getLinks();
			// the elements should be sorted in alpha order
			panelText = "<html>"; // reset the text
			itemCount = 0;
			for (Iterator<Link> it = listOfLinks.iterator(); it.hasNext();) {
				// create all the clickable links first
				Link link = (Link) it.next();
				if (isChild) {
					temp = (IdentifiedObject) link.getChild();
				} else {
					temp = (IdentifiedObject) link.getParent();
				}
				if (TermUtil.isClass(temp)) { // only show actual classes...not danglers
					// only put in items that are not xps
					if (TermUtil.isIntersection((OBOClass) temp)) // {
						panelText += "<i>";
					panelText += "<a href='" + temp.getID() + "'>" + temp.getName()
							+ "</a>";
					if (TermUtil.isIntersection((OBOClass) temp)) // {
						panelText += "</i>";
					// panelText += HtmlUtil.termLink(temp);
					// panelText += temp.getName();
					if (it.hasNext()) {
						panelText += ", ";
					}
					itemCount++;
					// }
				} else if (TermUtil.isDangling(temp)) {
					if (!TermUtil.isIntersection((DanglingObject) temp)) {
						panelText += temp.getName(); // dangling objects don't have links
						// eventually want to be smart and allow user to import the
						// necessary ontology to resolve danglers!
						if (it.hasNext()) {
							panelText += ", ";
						}
						itemCount++;
					}
				}
			}
			if (itemCount > 0) {
				// if there's at least one item, add the section to the pane
				panelText += "</html>";
				String tempType = linkCol.getLinkName();
				typeLabel = new JLabel(tempType, JLabel.TRAILING);
				typeLabel.setVerticalAlignment(JLabel.TOP);
				panel.add(typeLabel);
				HyperlinkLabel textArea = new HyperlinkLabel(panelText);
				textArea.addStringLinkListener(termHyperlinkListener);

				typeLabel.setLabelFor(textArea);
				panel.add(textArea);
				rowCount++;
				layout = new SpringLayout();
				// line up the rel type with the items
				layout.putConstraint(SpringLayout.NORTH, typeLabel, 5,
						SpringLayout.NORTH, panel);
				layout.putConstraint(SpringLayout.NORTH, textArea, 5,
						SpringLayout.NORTH, panel);
				panel.setLayout(layout);
			}
			totalItems += itemCount;
		}
		if (rowCount > 0) {
			SpringUtilities.makeCompactGrid(panel, rowCount, 2, // rows, cols
					5, 5, // initX, initY
					5, 5); // xPad, yPad
		} else {
			panel.add(new JLabel("(none)"));
			panel.setLayout(layout);
		}
		panel.setVisible(true);
		panel.validate();
		panel.repaint();
		return totalItems;
	}

	/**
	 * Given a collection of oboclass links, this processes the collection to
	 * separate out the links and group by relationship type+parent/child xp state
	 * It will always put the is_a links in the list first.
	 * 
	 * @param links
	 *          a collection of oboclass links
	 * @param xp
	 *          specifies if the request is for cross-product-specific list
	 * @param isChild
	 *          specifies if the links are parents or children of the implementing
	 *          class
	 * @return a list of links sorted by relationship type.
	 * 
	 */
	private static List<LinkCollection> linksList(Collection<Link> links,
			boolean xp, boolean isChild) {

		HashSet<OBOProperty> relSet = new HashSet<OBOProperty>();
		List<LinkCollection> allLinks = new ArrayList<LinkCollection>();
		for (Iterator<Link> it = links.iterator(); it.hasNext();) {
			Link link = (Link) it.next();
			if (((OBORestriction) link).completes() == xp) {
				// only add to links list those that match the desired xp state
				OBOProperty type = link.getType();
				if (!relSet.contains(type)) {
					relSet.add(type);
					LinkCollection linkSet = new LinkCollection(link);
					if (type.equals(OBOProperty.IS_A))
						allLinks.add(0, linkSet);
					else
						allLinks.add(linkSet);
				} else {
					for (ListIterator<LinkCollection> lit = allLinks.listIterator(); lit
							.hasNext();) {
						LinkCollection temp = (LinkCollection) lit.next();
						if (temp.get(0).getType() == type) {
							temp.addLink(link);
							allLinks.set(lit.nextIndex() - 1, temp);
						}
					}
				}
			}
		}
		return allLinks;
	}

	/**
	 * Given a set of "Consider" and "Replaced-by" links, navigable links are
	 * created in a panel.
	 * 
	 * @param oboClass
	 *          the item that is obsoleted
	 * @param panel
	 *          the panel to populate with this information
	 */
	private void makeObsPanel(OBOClass oboClass) {
		// to display the replaced-bys and consider term links for obsoleted
		// terms
		// need to make this its own panel so i can hide/show it as well as
		// make it easier to create the hyperlinks. will do this similar to
		// synonyms.
		// if this is its own stacked box, maybe ought to make the bar a
		// different
		// color, like red or something
		// really should compact this whole thing into a for-loop

		considerReplacePanel.removeAll();
		String panelText = "";
		Set obsItems;
		int rowCount = 0;
		JLabel typeLabel;
		boolean replaceFlag = false;
		boolean considerFlag = false;
		ObsoletableObject obsObj;

		for (int i = 0; i < 2; i++) { // do this 2x, once for each Obs type
			if (i == 0) {
				obsItems = oboClass.getReplacedBy();
				replaceFlag = !obsItems.isEmpty();
				typeLabel = new JLabel("Replaced by: ");
			} else {
				obsItems = oboClass.getConsiderReplacements();
				considerFlag = !obsItems.isEmpty();
				typeLabel = new JLabel("Consider using: ");
			}
			// JEditorPane.registerEditorKitForContentType
			// ("text/html", "HTMLEditorKit2");
			// JEditorPane textArea = new JEditorPane();
			// textArea.setEditorKitForContentType
			// ("text/html", new HTMLEditorKit2());
			//
			// textArea.setContentType("text/html");
			HyperlinkLabel textArea = new HyperlinkLabel();
			termHyperlinkListener = new TermHyperlinkListener();
			textArea.addStringLinkListener(termHyperlinkListener);
			// textArea.setEditable(false);
			for (Iterator it = obsItems.iterator(); it.hasNext();) {
				obsObj = (ObsoletableObject) it.next();
				if (obsObj != null) {
					panelText += HtmlUtil.termLink(obsObj);
				}
				if (it.hasNext()) {
					panelText += ", ";
				}
			}
			if (!obsItems.isEmpty()) {
				rowCount++;
				typeLabel.setLabelFor(textArea);
				textArea.setText(panelText);
				textArea.validate();
				considerReplacePanel.add(typeLabel);
				considerReplacePanel.add(textArea);
			}
		}

		if (rowCount < 1) // no items, hide
		{
			considerReplacePanel.setVisible(false);
		} else {

			SpringUtilities.makeCompactGrid(considerReplacePanel, rowCount, 2, // rows,
					// cols
					6, 6, // initX, initY
					6, 6); // xPad, yPad

			considerReplacePanel.validate();
			considerReplacePanel.setVisible(true);
		}
	}

	private int makePropValsPanel(Set<PropertyValue> propertyValues) {

		int rowCount = 0;
		JLabel propLabel;
		JLabel valLabel;
		PropertyValue propVal;

		propertyValuesPanel.removeAll();
		for (Iterator it = propertyValues.iterator(); it.hasNext();) {
			propVal = (PropertyValue) it.next();
			if (propVal != null) {
				propLabel = new JLabel(propVal.getProperty());
				valLabel = new JLabel(propVal.getValue());
				propLabel.setLabelFor(valLabel);
				propertyValuesPanel.add(propLabel);
				propertyValuesPanel.add(valLabel);
				propertyValuesPanel.validate();
				rowCount++;
			}
		}
		if (rowCount > 0) {
			SpringUtilities.makeCompactGrid(propertyValuesPanel, rowCount, 2, // rows,
					// cols
					6, 6, // initX, initY
					6, 6); // xPad, yPad
		}
		propertyValuesPanel.validate();
		propertyValuesPanel.setVisible(true);

		return rowCount;
	}

	public void naviRefresh(String action) {
		int tot = termInfoNaviHistory.size();

		if (action.equals("back")) {
			if (naviIndex > 0) {
				naviIndex--;
			}
		} else if (action.equals("forward")) {
			if (naviIndex < (tot - 1)) {
				naviIndex++;
			}
		}
		String id = getTermFromNaviHistory(naviIndex);
		System.out.println(id);
		try {
			OBOClass term = CharFieldManager.inst().getOboClass(id); // ex
			setTextFromOboClass(term);
			// send out term selection (non mouse over) for DAG view
			this.selectionManager.selectTerm(this, term, true);
		
			// Since items are added in reverse order, "back" is actually
			// forward
			// termComboBox.setSelectedIndex(termComboBox.getSelectedIndex()+1);
		} catch (TermNotFoundException ex) {
			return;
		}
	}

	// for testing
	String getTermNameText() {
		return termName == null ? null : termName.getText();
	}

	/** for testing */
	void simulateHyperlinkEvent(HyperlinkEvent e) {
		termHyperlinkListener.hyperlinkUpdate(e);
	}

	public List<String> getTermInfoNaviHistory() {
		return termInfoNaviHistory;
	}

	public int getNaviIndex() {
		return naviIndex;
	}

	public void setNaviIndex(int index) {
		naviIndex = index;
	}

	class HTMLEditorKit2 extends HTMLEditorKit {
		public Document createDefaultDocument() {
			HTMLDocument doc = (HTMLDocument) (super.createDefaultDocument());
			doc.setAsynchronousLoadPriority(-1); // load synchronously
			return doc;
		}
	}
	
	public void setShowEmptyPanelsFlag (boolean flag) {
		showEmptyPanelsFlag = flag;
	}
	
	public boolean getShowEmptyPanelsFlag () {
		return showEmptyPanelsFlag;
	}
	
}
