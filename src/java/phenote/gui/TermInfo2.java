package phenote.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractGUIComponent;
import org.bbop.framework.ComponentConfiguration;
import org.bbop.framework.ComponentManager;
import org.bbop.framework.ConfigurationPanel;
import org.bbop.swing.HyperlinkLabel;
import org.bbop.swing.StringLinkListener;
import org.obo.annotation.datamodel.Annotation;
import org.obo.dataadapter.OBDSQLDatabaseAdapter;
import org.obo.dataadapter.OBDSQLDatabaseAdapter.OBDSQLDatabaseAdapterConfiguration;
import org.obo.datamodel.DanglingObject;
import org.obo.datamodel.Dbxref;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.Link;
import org.obo.datamodel.LinkDatabase;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBORestriction;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.ObsoletableObject;
import org.obo.datamodel.PathCapable;
import org.obo.datamodel.PropertyValue;
import org.obo.datamodel.RootAlgorithm;
import org.obo.datamodel.Synonym;
import org.obo.util.AnnotationUtil;
import org.obo.util.TermUtil;
import org.oboedit.controller.SessionManager;
import org.oboedit.gui.ObjectSelector;
import org.oboedit.gui.Selection;
import org.oboedit.gui.event.ExpandCollapseListener;
import org.oboedit.gui.event.SelectionListener;

import phenote.config.Config;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.TermNotFoundException;
import phenote.gui.selection.SelectionManager;
import phenote.gui.selection.TermSelectionEvent;
import phenote.gui.selection.TermSelectionListener;
import phenote.gui.selection.UseTermListener;
import phenote.util.HtmlUtil;
import phenote.util.LinkCollection;
import edu.stanford.ejalbert.BrowserLauncher;
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

	private static final Logger LOG = Logger.getLogger(TermInfo2.class);
	
	protected Map<OBOObject,Float> cachedAnnotationInformationContentByClass = new HashMap<OBOObject,Float>();
	protected Map<OBOObject,Integer> cachedAnnotationCountByClass = new HashMap<OBOObject,Integer>();
	
	
	protected boolean includeImplicitAnnotations = false;
	protected boolean includeExternalDatabaseAnnotations = false;
	// constants
	private static final int TERM_INFO_DEFAULT_WIDTH = 350;

	private static final int TERM_INFO_DEFAULT_HEIGHT = 400;

	private static final int BUTTON_HEIGHT = 30;

	private static Border contentBorder = BorderFactory.createEmptyBorder(3, 3,
			6, 3);  //top, left, bottom, right...orig 6,8,6,8

	//these are used for the spring layout.
	private static final int XPAD = 5; //between elements horizontally
	private static final int YPAD = 3; //between elements vertically
	private static final int INITX = 3;
	private static final int INITY = 3;
	private static final int PREFERREDX = 75;


	// content variables
	private OBOObject currentOboClass = null;

	private UseTermListener useTermListener;

	private TermHyperlinkListener termHyperlinkListener;

	private static List<String> termInfoNaviHistory = new ArrayList<String>();

	// private List termInfoNaviHistory = new List();
	private static int naviIndex = -1;

	private SelectionManager selectionManager;

	// gui components

	private TermInfoToolbar termInfoToolbar;

	private JScrollPane termInfoScroll;

  /** StackedBox is a class in the phenote.gui package, that nicole got from
      web & modified */
	private StackedBox termInfoPanel;

	private JPanel basicInfoPanel;

	//private JLabel termName; // no selection
  //private JTextField termName; // doesnt do html   // Why do we need html?
//  private JEditorPane termName;  // Problematic--hard to adjust alignment, for example.
  private JTextField termName;

	//private JLabel termID;
  private JTextField termID;

	private JLabel ontologyName;

	private JLabel annotationSummaryLabel;
	
	private JEditorPane definitionTextArea;
	
	private HyperlinkLabel annotationSummaryTextArea;

	private JPanel considerReplacePanel;

	private JTextArea considerTerms;

	private JTextArea replacementTerms;

	private JPanel synonymPanel;
	private JPanel annotationPanel;
	
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
	
	private JPanel emptyPanel;

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

	private static final String annotationPanelName = "ANNOTATIONS"; //16
	
	private boolean useOBOSessionManager = false;
	
	/** this sets the order of the panels */
	private static String[] panels = {basicInfoPanelName, considerReplacePanelName, synonymPanelName, xpdefsPanelName, linksPanelName, dbxrefPanelName, propvalsPanelName, commentsPanelName};
	
	public boolean isIncludeImplicitAnnotations() {
		return includeImplicitAnnotations;
	}



public void setIncludeImplicitAnnotations(boolean includeImplicitAnnotations) {
		this.includeImplicitAnnotations = includeImplicitAnnotations;
	}
	
	public boolean isIncludeExternalDatabaseAnnotations() {
		return includeExternalDatabaseAnnotations;
	}

	public void setIncludeExternalDatabaseAnnotations(
			boolean includeExternalDatabaseAnnotations) {
		this.includeExternalDatabaseAnnotations = includeExternalDatabaseAnnotations;

	}

	@Override
	public ConfigurationPanel getConfigurationPanel() {
		ConfigurationPanel p = new ConfigurationPanel() {

			protected JCheckBox includeImplicitAnnotationsBox = new JCheckBox(
					"Include implicit annotations",
					false);
			protected JCheckBox includeExternalDatabaseAnnotationsBox = new JCheckBox(
					"Include external database annotations",
					false);

			{
				setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				add(includeImplicitAnnotationsBox);
				add(includeExternalDatabaseAnnotationsBox);
			}

			@Override
			public void commit() {
				TermInfo2Config config = (TermInfo2Config) getComponent()
						.getConfiguration();
				config.setIncludeImplicitAnnotations(includeImplicitAnnotationsBox.isSelected());
				config.setIncludeExternalDatabaseAnnotations(includeExternalDatabaseAnnotationsBox.isSelected());
				getComponent().setConfiguration(config);
				// getComponent().getComponent().updateUI(); TODO: update UI
			}

			@Override
			public void init() {
				TermInfo2Config config = (TermInfo2Config) getComponent()
						.getConfiguration();
				includeImplicitAnnotationsBox.setSelected(config.isIncludeImplicitAnnotations());
				includeExternalDatabaseAnnotationsBox.setSelected(config.isIncludeExternalDatabaseAnnotations());
			}
		};
		return p;
	}
	
	@Override
	public ComponentConfiguration getConfiguration() {
		return new TermInfo2Config(isIncludeImplicitAnnotations(),
				isIncludeExternalDatabaseAnnotations());
	}

	public void setConfiguration(ComponentConfiguration config) {
		if (config instanceof TermInfo2Config) {
			setIncludeImplicitAnnotations(((TermInfo2Config) config)
					.isIncludeImplicitAnnotations());
			setIncludeExternalDatabaseAnnotations(((TermInfo2Config) config)
					.isIncludeExternalDatabaseAnnotations());
		}
	}

	
	public static class TermInfo2Config implements ComponentConfiguration {
		protected boolean includeImplicitAnnotations = false;
		protected boolean includeExternalDatabaseAnnotations = false;

		public TermInfo2Config(boolean includeImplicitAnnotations, boolean includeExternalDatabaseAnnotations) {
			super();
			this.includeImplicitAnnotations = includeImplicitAnnotations;
			this.includeExternalDatabaseAnnotations = includeExternalDatabaseAnnotations;
		}

		public TermInfo2Config() {
		}

		public boolean isIncludeImplicitAnnotations() {
			return includeImplicitAnnotations;
		}

		public void setIncludeImplicitAnnotations(boolean includeImplicitAnnotations) {
			this.includeImplicitAnnotations = includeImplicitAnnotations;
		}

		public boolean isIncludeExternalDatabaseAnnotations() {
			return includeExternalDatabaseAnnotations;
		}

		public void setIncludeExternalDatabaseAnnotations(
				boolean includeExternalDatabaseAnnotations) {
			this.includeExternalDatabaseAnnotations = includeExternalDatabaseAnnotations;
		}

	}
	 

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
	
	/**
	 * @param useOBOSessionManager When true, the TermInfo panel uses the global SessionManager
	 * to get the OBO session for looking up term IDs.  This allows the panel to be used when 
	 * the CharFieldManager is not in use.
	 */
	public TermInfo2(boolean useOBOSessionManager) {
	  this();
	  this.useOBOSessionManager = useOBOSessionManager;
	}

	public static TermInfo2 inst() {
		if (singleton == null) singleton = new TermInfo2();
		return singleton;
	}

	private void initTermInfo() {
		// create the panel the whole thing will live in (including toolbars,
		// etc.)
		//this.setLayout(new BorderLayout(0,0));
    getComponent().setLayout(new BoxLayout(getComponent(),BoxLayout.Y_AXIS));
		this.setPreferredSize(new Dimension(TERM_INFO_DEFAULT_WIDTH,TERM_INFO_DEFAULT_HEIGHT));

		// create the toolbar
		termInfoToolbar = new TermInfoToolbar();
		termInfoToolbar.setPreferredSize(new Dimension(TERM_INFO_DEFAULT_WIDTH, BUTTON_HEIGHT+5));
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		termInfoToolbar.setMaximumSize(new Dimension(screenSize.width, BUTTON_HEIGHT+5));
		this.add(termInfoToolbar); //, BorderLayout.NORTH);
		

		// create the stackedbox that the term info will live in
		termInfoPanel = new StackedBox();

		// put it in a scrollpane
		termInfoScroll = new JScrollPane(termInfoPanel);
		termInfoScroll.setBorder(null);

		// put the scrollpane into the whole bucket
		this.add(termInfoScroll); //, BorderLayout.CENTER);

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
//		termName = new JEditorPane(); // handles html & text select/copy
		termName = new JTextField();
                termName.setEditable(false);
                termName.setBorder(null);
                termName.setBackground(Color.WHITE);
//    termName.setContentType("text/html");
		termName.setText("(No Selection)");
//                termName.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
//                termName.setFont(new Font("Arial", Font.BOLD, 12));
                // Whatever the termName font is, make it bold
                Font tnf = termName.getFont();
                termName.setFont(tnf.deriveFont(tnf.getStyle() ^ Font.BOLD));

		nameLabel.setLabelFor(termName);
		basicInfoPanel.add(termName);
		// make nameLabel draggable to drag term to fields
		//nameLabel.setTransferHandler(new InfoTransferHandler());

		JLabel idLabel = new JLabel("ID: ", JLabel.TRAILING);
		basicInfoPanel.add(idLabel);
		termID = new JTextField(); //Label(); -> selectable text!
		termID.setEditable(false);
		termID.setBorder(null); // ?
		termID.setBackground(Color.WHITE);
		idLabel.setLabelFor(termID);
		basicInfoPanel.add(termID);

		ontologyName = new JLabel();
		JLabel ontologyLabel = new JLabel("Ontology: ", JLabel.TRAILING);
		basicInfoPanel.add(ontologyLabel);
		ontologyLabel.setLabelFor(ontologyName);
		basicInfoPanel.add(ontologyName);

		JLabel definitionLabel = new JLabel("Definition: ", JLabel.TRAILING);
		definitionLabel.setVerticalAlignment(JLabel.TOP);  // Doesn't help--label still hangs below text.

		basicInfoPanel.add(definitionLabel);
		definitionTextArea = new JEditorPane();
		definitionTextArea.setText(" ");
		definitionTextArea.setEditable(false);
		definitionTextArea.setBorder(null);
		definitionTextArea.setBackground(Color.WHITE);
		definitionTextArea.setContentType("text/html");
                definitionTextArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
                definitionTextArea.setFont(new Font("Arial", Font.PLAIN, 12));
//                definitionTextArea.setVerticalAlignment(JLabel.BOTTOM); // doesn't work

		
		termHyperlinkListener = new TermHyperlinkListener();
		definitionLabel.setLabelFor(definitionTextArea);
		basicInfoPanel.add(definitionTextArea);
		
//removing for 1.5 release		
//		if (this.isIncludeExternalDatabaseAnnotations()) {
			annotationSummaryLabel = new JLabel("<html><p align=right>External Annotations:</p></html>", JLabel.TRAILING);
			annotationSummaryLabel.setVerticalAlignment(JLabel.TOP);

			basicInfoPanel.add(annotationSummaryLabel);
			annotationSummaryTextArea = new HyperlinkLabel(" ");
			annotationSummaryLabel.setLabelFor(annotationSummaryTextArea);
			basicInfoPanel.add(annotationSummaryTextArea);
			annotationSummaryLabel.setVisible(this.isIncludeExternalDatabaseAnnotations());
			annotationSummaryTextArea.setVisible(this.isIncludeExternalDatabaseAnnotations());
//		}

		// Lay out the panel.
		int[] maxX = {PREFERREDX,-1};
		int[] maxY = null;
                int rows = 4;
                if (this.isIncludeExternalDatabaseAnnotations())
                  rows++;

		SpringLayout layout = new SpringLayout();
		basicInfoPanel.setLayout(layout);
		SpringUtilities.makeCompactGrid(basicInfoPanel, rows, 2, // rows, cols
				INITX, INITY, // initX, initY
				XPAD, YPAD); // xPad, yPad

		SpringUtilities.fixCellWidth(basicInfoPanel, rows, 2, // rows,
				// cols
				INITX, INITY, // initX, initY
				XPAD, YPAD,  // xPad, yPad
				maxX, maxY);

		termInfoPanel.addBox("Basic Info", basicInfoPanel);

		//consider & replacement terms - for obo1.2+ format
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
		considerReplacePanel.setVisible(false);

		termInfoPanel.addBox("Consider & Replacement Terms", considerReplacePanel);

		// synonyms
		synonymPanel = new JPanel();
		synonymPanel.setOpaque(false);
		synonymPanel.setLayout(new SpringLayout());
		synonymPanel.setBorder(contentBorder);
		synonymPanel.setName(synonymPanelName);
		synonymPanel.setVisible(false);

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
		xpDefPanel.setVisible(false);
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
		ontologyLinksPanel.setVisible(false);
		//the two panels are placed in a spring grid within the larger panel
		SpringUtilities.makeCompactGrid(ontologyLinksPanel, 2, 1, 
				0, 0, // initX, initY
				0, 0); // xPad, yPad

		// dbxrefs
		dbxrefPanel = new JPanel();
		dbxrefPanel.setBackground(Color.WHITE);
		dbxrefPanel.setLayout(new SpringLayout());
		dbxrefText = new JTextArea();
		dbxrefPanel.add(dbxrefText);
		dbxrefPanel.setName(dbxrefPanelName);
		dbxrefPanel.setVisible(false);

		termInfoPanel.addBox("DBxrefs", dbxrefPanel);


		// propertyValues (often used for OWL ontologies)
		propertyValuesPanel = new JPanel();
		propertyValuesPanel.setOpaque(false);
		propertyValuesPanel.setLayout(new SpringLayout());
		propertyValuesPanel.setBorder(contentBorder);
		propertyValuesPanel.setName(propvalsPanelName);
		propertyValuesPanel.setVisible(false);
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
		commentsPanel.setVisible(false);

		termInfoPanel.addBox("Comments", commentsPanel);

		// Annotations
		// Do we want a single panel here?
		annotationPanel = new JPanel();
		annotationPanel.setOpaque(false);
		annotationPanel.setLayout(new SpringLayout());
		annotationPanel.setBorder(contentBorder);
		annotationPanel.setName(annotationPanelName);
		annotationPanel.setVisible(false);

		termInfoPanel.addBox("Annotations", annotationPanel);
		
		//okay, this is a hack to make the refresh work properly... one extra panel
		//does it
		emptyPanel = new JPanel();
		termInfoPanel.add(emptyPanel);
		// refresh
		validate();
		repaint();
		setVisible(true);
		
	}

//	public static JComponent getComponent() {
//	return entirePanel;
//	}

	private void setTextFromOboClass(OBOObject oboClass) {
		TermInfo2.this.currentOboClass = oboClass;
                if (oboClass == null)
                  return;
		
		// basicInfoPanel
		if (oboClass.isObsolete()) {  // Obsolete term
			termInfoPanel.setBoxTitleBackgroundColor(0, Color.RED);
			termInfoPanel.setBoxTitle("Basic Info  [OBSOLETE]", 0);
			// show the consider/replacements
			termInfoPanel.getComponent(2).setVisible(true);
			considerReplacePanel.setVisible(true);
		} else if (oboClass.getNamespace() == null) { // No namespace == dangler
			termInfoPanel.setBoxTitleBackgroundColor(0, Color.ORANGE);
			termInfoPanel.setBoxTitle("UNKNOWN TERM", 0);
			// hide the consider/replace panel
			termInfoPanel.getComponent(2).setVisible(false);
			considerReplacePanel.setVisible(false);
                } else { // Normal term
			termInfoPanel.setBoxTitleBackgroundColor(0, Color.LIGHT_GRAY);
			termInfoPanel.setBoxTitle("Basic Info", 0);
			// hide the consider/replace panel
			termInfoPanel.getComponent(2).setVisible(false);
			considerReplacePanel.setVisible(false);
		}

		termInfoToolbar.setTermFieldText(oboClass); //is the toolbar now out of date b/c of the component title?
		//always show the basics, even if empty...shouldn't be empty.
                termID.setText(oboClass.getID() == null ? "" : oboClass.getID());
		if (oboClass.getNamespace()!=null) {
		  ontologyName.setText(oboClass.getNamespace().toString());
                }
		else {
                  ontologyName.setText("No namespace/ontology specified");
		  // LOG.error("No namespace for term "+oboClass);
                }
		if (oboClass.getDefinition() != null && oboClass.getDefinition().length()>0) {
      String def = oboClass.getDefinition();
      // definitions can have refs in brackets after def
      Set<Dbxref> defRefs = oboClass.getDefDbxrefs();
      if (defRefs!=null && !defRefs.isEmpty()) {
        int i=1;
        for (Dbxref x : defRefs) {
          def  += "<br>Definition ref "+ i++ +": " +x; 
        }
      }        
			definitionTextArea.setText("<html>"+def+"</html>"); 
			
		} else {
			definitionTextArea.setText("<html> (no definition provided) </html>");
		}


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
			considerReplacePanel.validate();
			considerReplacePanel.repaint();
			considerReplacePanel.setVisible(true);
		}
		
		if (isIncludeExternalDatabaseAnnotations()) {
			annotationSummaryTextArea.setText("<html>annotation count: <b>"+ this.getAnnotationCountByClass(oboClass) + " "+
				"</b><br>information content: <b>"+this.getAnnotationInformationContentByClass(oboClass) +
				"</b></html>");
		} else {
			annotationSummaryTextArea.setText("");
		}
		annotationSummaryLabel.setVisible(this.isIncludeExternalDatabaseAnnotations());
		annotationSummaryTextArea.setVisible(this.isIncludeExternalDatabaseAnnotations());


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


		// xpDefPanel
		int linkCount = 0;
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
		linkCount = 0;
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

		if ((!showEmptyPanelsFlag) && (linkCount == 0)) {
			termInfoPanel.getComponent(8).setVisible(false);
			ontologyLinksPanel.setVisible(false);
		} else {
			ontologyLinksPanel.validate();
			ontologyLinksPanel.repaint();
			ontologyLinksPanel.setVisible(true);
			termInfoPanel.getComponent(8).setVisible(true);
		}

		// dbxrefsPanel
		if ((!showEmptyPanelsFlag) && (oboClass.getDbxrefs().size() == 0)) {
			termInfoPanel.getComponent(10).setVisible(false);
			dbxrefPanel.setVisible(false);
		} else {
			makeDbxrefPanel(oboClass.getDbxrefs());
			termInfoPanel.setBoxTitle("DBxrefs (" + oboClass.getDbxrefs().size() + ")",
					10);
			dbxrefPanel.validate();
			dbxrefPanel.repaint();
			dbxrefPanel.setVisible(true);
			termInfoPanel.getComponent(10).setVisible(true);
		}

		// propValues
		// propertyValuesList.setText(oboClass.getPropertyValues().toString());
		linkCount = makePropValsPanel(oboClass.getPropertyValues());
		termInfoPanel.setBoxTitle("Other Properties (" + linkCount + ")", 12);
		if (!showEmptyPanelsFlag && (linkCount == 0)) {
			termInfoPanel.getComponent(12).setVisible(false);
			propertyValuesPanel.setVisible(false);
		} else {
			propertyValuesPanel.validate();
			propertyValuesPanel.repaint();
			propertyValuesPanel.setVisible(true);
			termInfoPanel.getComponent(12).setVisible(true);
		}


		// commentsPanel
		if (oboClass.getComment() == null || oboClass.getComment().length() == 0) {
			termInfoPanel.setBoxTitle("Comments (none)", 14);
			if (!showEmptyPanelsFlag) {
				termInfoPanel.getComponent(14).setVisible(false);
				commentsPanel.setVisible(false);
			}
		} else {
			termInfoPanel.setBoxTitle("Comments*", 14);
			commentsText.setText(oboClass.getComment().toString());
			termInfoPanel.getComponent(14).setVisible(true);
			commentsPanel.setVisible(true);
			commentsPanel.validate();
			commentsPanel.repaint();
		}

		// Annotations
		//moving annotations to only show if the button is clicked.
		//want to hide the annotation panel until the person clicks the button
//		termInfoPanel.getComponent(16).setVisible(false);
//		annotationPanel.setVisible(false);
		


		// this refreshes the main panel
		//this seems a little out of order, but its to get the scrollbar movement right
//		termName.setText("<html><b>"+oboClass.getName()+"</b></html>"); 
		termName.setText(oboClass.getName());
		termName.validate();
		termName.repaint();  //really trying to make sure the scroll works properly.
		basicInfoPanel.validate();
		basicInfoPanel.repaint();
		basicInfoPanel.setVisible(true);
		termInfoScroll.getVerticalScrollBar().setValue(0);
		termInfoScroll.getViewport().setViewPosition(new Point(0, 0));
		termInfoPanel.validate();
		termInfoPanel.repaint();
		
		emptyPanel.repaint();
		
		validate();
		repaint();

	}

	/**
	 * Puts the currently browsed term name into the component title <p>
	 * @param oboClass the term being browsed 
	 */
	public void setComponentTitleFromOBOObject (OBOObject oboClass) {
          if (oboClass == null)
            return;
		String title = "Term Info: "+ oboClass.getName();
    // this only works and only makes sense in phenote2 with docking framework
    // where each gui item has a border around it with a title - otherwise throws
    // null pointer - just catch null pointer and do nothing
    try {	ComponentManager.getManager().setLabel(this,title); }
    catch (NullPointerException x) {}
	}

	/** Listen for selection from phenote (mouse over completion list) */
	private class InfoTermSelectionListener implements TermSelectionListener {
		public void termSelected(TermSelectionEvent e) {
                  clearAnnotations(); // ???

//                  LOG.debug("TermInfo2.termSelected " + e); // DEL

			//navi selection is a mouseover event
			if (!e.isMouseOverEvent()) {
				// add the item to the navi history if selected from list only
				String id = e.getOboClass().getID();
				addTermToNaviHistory(id); //only add the item if its not from navigation
				termInfoToolbar.setNaviButtonStatus();
			}
                        if (e.getOboClass() != null) {
                          setTextFromOboClass(e.getOboClass());
                          // This sets who now listens to use term button clicks (only 1
                          // listener)
                          //setUseTermListener(e.getUseTermListener());
                          termInfoToolbar.setUseTermListener(e.getUseTermListener());
                          //change the name of the item being browsed in the term info header
                          setComponentTitleFromOBOObject(e.getOboClass());
                          termInfoToolbar.setNaviButtonStatus();
                        }
                }
	}

  /** just gets useTermListener from terminfotoolbar, which back & forward
      action use, which actually probably isnt right as back & forward
      might be hitting terms with different use term listeners 
      but good enough for now */
  public UseTermListener getUseTermListener() {
    return termInfoToolbar.getUseTermListener();
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
			LOG.debug("got url "+url+" desc "+e.getDescription());

			// internal link to term...
			if (HtmlUtil.isPhenoteLink(e)) {
				bringUpTermInTermInfo(e);
				return;
			}

			if (url == null) { // relative urls are null
				LOG.error("invalid url " + url);
				return;
			}

			bringUpInBrowser(url);

		}

		private void bringUpInBrowser(URL url) {
			if (url == null) return;
			try {
				BrowserLauncher bl = new BrowserLauncher(null); // no logger
				bl.openURLinBrowser(url.toString());
			} catch (BrowserLaunchingInitializingException be) {
				LOG.error("cant launch browser ", be);
			} catch (UnsupportedOperatingSystemException ue) {
			  LOG.error("cant launch browser ", ue);
			}
		}

		private void bringUpTermInTermInfo(HyperlinkEvent e) {
			// or do through obo session?
			String id = HtmlUtil.getIdFromHyperlink(e);
			if (id == null) return;
			bringUpTermInTermInfo(id);

		}

		private void bringUpTermInTermInfo(String id) {
		  // or do through obo session?
		  if (id == null) return;
//		  try {
		  OBOSession session = null;
		  if (TermInfo2.this.useOBOSessionManager) {
		    session = SessionManager.getManager().getSession();
		  } else if (!CharFieldManager.inst().getAllOntologies().isEmpty()) {
		    session = CharFieldManager.inst().getAllOntologies().get(0).getOboSession();
		  }

		  if (session != null) {
		    IdentifiedObject io = session.getObject(id);
		    if (io instanceof OBOObject) {
		      OBOObject c = (OBOObject) io;
                      if (c == null)
                        return;

		      setTextFromOboClass(c);
		      setComponentTitleFromOBOObject(c);
		      addTermToNaviHistory(id);
		      termInfoToolbar.setNaviButtonStatus();
		      // send out term selection (non mouse over) for DAG view
		      // im guessing this is a work in progress as currently true for isHyperLink
		      // causes SelMan to not fireTermSelect and thus this does nothing
		      // whats the idea here?? - MG
		      if (c instanceof OBOClass)
//		        selectionManager.selectTerm(TermInfo2.this, (OBOClass)c, true);
                        // This fixes bug 2970568 (Term info browser fails to sync with other components properly when browsing by clicking parent or child terms).
                        // Makes OE components like Graph Editor change to show selected term when we follow hyperlinks
                        // to parent or child terms in the Term Info browser.
		        selectionManager.selectTerm(TermInfo2.this, (OBOClass)c, false);
		    }
		  }
		}
		
		private void selectTerm(OBOObject term) {
		}

		public void link(String href) {
			bringUpTermInTermInfo(href);
			// TODO Auto-generated method stub

		}

	} // end of inner class TermHyperlinkListener

	private void makeSynPanel(Set<Synonym> someSet) {
		// private JList makeSynList (Set someSet) {

		String[] synTypes = { "BROAD", "NARROW", "EXACT", "RELATED", "OTHER" };
		String[] syns = { "", "", "", "", "" };
		int numSynTypes = 5;
		int rowCount = 0;

		Synonym syn;

		for (Iterator<Synonym> it = someSet.iterator(); it.hasNext();) {
			syn = it.next();
			if (syn.getScope() == Synonym.BROAD_SYNONYM) {
				syns[0] += syn + "<br>";
			} else if (syn.getScope() == Synonym.NARROW_SYNONYM) {
				syns[1] += syn + "<br>";
			} else if (syn.getScope() == Synonym.EXACT_SYNONYM) {
				syns[2] += syn + "<br>";
			} else if (syn.getScope() == Synonym.RELATED_SYNONYM) {
				syns[3] += syn + "<br>";
			} else if (syn.getScope() == Synonym.UNKNOWN_SCOPE) {
				syns[4] += syn + "<br>";
			} else {
				syns[4] += syn + "<br>";
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
				JLabel synList = new JLabel();
				synList.setText("<html>"+syns[i]+"</html>");
				synTypeLabel.setLabelFor(synList);
				synonymPanel.add(synList);
				rowCount++;
//				SpringLayout layout = new SpringLayout();
//				// line up the rel type with the items
//				layout.putConstraint(SpringLayout.NORTH, synTypeLabel, 5,
//						SpringLayout.NORTH, synonymPanel);
//				layout.putConstraint(SpringLayout.NORTH, synList, 5,
//						SpringLayout.NORTH, synonymPanel);
//				synonymPanel.setLayout(layout);

			}
		}

		// Lay out the panel.
		SpringUtilities.makeCompactGrid(synonymPanel, rowCount, 2, // rows,
				// cols
				INITX, INITY, // initX, initY
				XPAD, YPAD); // xPad, yPad

		int[] maxX = {PREFERREDX,-1};
		int[] maxY = null;
		SpringUtilities.fixCellWidth(synonymPanel, rowCount, 2, // rows,
				// cols
				INITX, INITY, // initX, initY
				XPAD, YPAD,  // xPad, yPad
				maxX, maxY);

		synonymPanel.setVisible(true);
	}

  private void clearAnnotations() {
    annotationPanel.removeAll();
    termInfoPanel.getComponent(16).setVisible(false);
		annotationPanel.validate();
		annotationPanel.repaint();
  }

	private void makeAnnotationPanel(Collection<Annotation> annots) {
	
		int rowCount = 0;

		annotationPanel.removeAll(); // clear out the old ones
		annotationPanel.setBorder(contentBorder);
		annotationPanel.setOpaque(false);
		annotationPanel.setBackground(Color.WHITE);

		for (Annotation annot : annots) {
			LinkedObject ae = annot.getSubject();
			if (ae == null) {
				LOG.error("no subject for "+annot);
				continue;
			}
			LinkedObject annotatedTo = annot.getObject();
			if (annotatedTo == null) {
				LOG.error("no object for "+annot);
				continue;
			}
			JLabel aeLabel = new JLabel(ae.getID());
			
//			aeLabel.setToolTipText("Name: "+ae.getName()+"\n"+"Annotated to: "+annotatedTo);
			
			// annotationPanel.add(getObjHrefLabel(annotatedTo));
			annotationPanel.add(new JLabel("")); // TODO: show blank until we prettify the subj
			annotationPanel.add(aeLabel);
			rowCount++;
			SpringLayout layout = new SpringLayout();
//			layout.putConstraint(SpringLayout.NORTH, aeLabel, 5,
//					SpringLayout.NORTH, annotationPanel);
			annotationPanel.setLayout(layout);

		}

		// Lay out the panel.
		SpringUtilities.makeCompactGrid(annotationPanel, rowCount, 2, // rows,
				// cols
				INITX, INITY, // initX, initY
				XPAD, YPAD); // xPad, yPad
		
		int[] maxX = {PREFERREDX,-1};
		int[] maxY = null;
		SpringUtilities.fixCellWidth(annotationPanel, rowCount, 2, // rows,
				// cols
				INITX, INITY, // initX, initY
				XPAD, YPAD,  // xPad, yPad
        maxX, maxY);

//		annotationPanel.setVisible(true);
		annotationPanel.validate();
		annotationPanel.repaint();
	}

	private void makeDbxrefPanel(Set<Dbxref> someSet) {
		int rowCount = 0;
		String tempID;
		OBOObject tempOboClass = null;
		String panelText = "<html>";
		Dbxref dbxrefObj;
		dbxrefPanel.removeAll(); // clear out the old ones

		for (Iterator<Dbxref> it = someSet.iterator(); it.hasNext();) {
			dbxrefObj = it.next();
			if (dbxrefObj != null) {
				// will make this linkable - internal & external
				// eventually, get smart and enable adding ontology!
				tempID = dbxrefObj.getDatabase() + ":" + dbxrefObj.getDatabaseID();

				// check if the term is in the current obosession
				try {
					tempOboClass = CharFieldManager.inst().getOboClass(tempID);
				} catch (TermNotFoundException ex) {
					tempOboClass = null;
				}

				if (tempOboClass != null) {
//					panelText += HtmlUtil.termLink(tempOboClass) + " (" + tempID + ")";
					panelText+= "<a href='" + tempOboClass.getID() + "'>" + tempOboClass.getName();

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

		// Lay out the panel.
		if (rowCount > 0) {
			SpringUtilities.makeCompactGrid(dbxrefPanel, 1, 1, // rows, cols
					INITX+10, INITY, // initX, initY
					XPAD, YPAD); // xPad, yPad
		}
		
		dbxrefPanel.validate();
		dbxrefPanel.repaint();
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

		if (isChild) { 
			linkLabel = new JLabel("<html><b>Children</b></html>");
		} else {
			linkLabel = new JLabel("<html><b>Parents</b></html>");
		}
		if (!isXP) { //only add the parent/child heading if not cross-products
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
					if (TermUtil.isIntersection((OBOObject) temp)) // {
						panelText += "<i>";
					panelText += "<a href='" + temp.getID() + "'>" + temp.getName()
					+ "</a>";
					if (TermUtil.isIntersection((OBOObject) temp)) // {
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
				String tempType = "<html><p align=right>"+linkCol.getLinkName()+"</p></html>";
				typeLabel = new JLabel(tempType, JLabel.TRAILING);
				typeLabel.setVerticalAlignment(JLabel.TOP);
				panel.add(typeLabel);
				HyperlinkLabel textArea = new HyperlinkLabel(panelText);
				termHyperlinkListener = new TermHyperlinkListener();
				textArea.addStringLinkListener(termHyperlinkListener);

				typeLabel.setLabelFor(textArea);
				panel.add(textArea);
				rowCount++;
			}
			totalItems += itemCount;
		}
		if (rowCount > 0) {  //first row is the header
			SpringUtilities.makeCompactGrid(panel, rowCount, 2, // rows, cols
					INITX, INITY, // initX, initY
					XPAD, YPAD); // xPad, yPad
                        // Need more space for some of the longer relationships (e.g., has_soma_location)
                        // Could we somehow find the longest relationship among the parents & children of the current term
                        // and set maxX accordingly?
			int[] maxX = {PREFERREDX+32,-1};
			int[] maxY = null;
			SpringUtilities.fixCellWidth(panel, rowCount, 2, // rows,
					// cols
					INITX, INITY, // initX, initY
					XPAD, YPAD,  // xPad, yPad
					maxX, maxY);

			panel.setVisible(true);
		} else {
			panel.setVisible(false); //hide the parents/children panel if empty
		}
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
			if (((OBORestriction) link).getCompletes() == xp) {
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
	private void makeObsPanel(OBOObject oboClass) {
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
		Set<ObsoletableObject> obsItems;
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
			for (Iterator<ObsoletableObject> it = obsItems.iterator(); it.hasNext();) {
				obsObj = it.next();
				if (obsObj != null) {
//					panelText += HtmlUtil.termLink(obsObj);
					panelText += "<a href='" + obsObj.getID() + "'>" + obsObj.getName();

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
					INITX, INITY, // initX, initY
					XPAD, YPAD); // xPad, yPad
			int[] maxX = {PREFERREDX,-1};
			int[] maxY = null;

			SpringUtilities.fixCellWidth(considerReplacePanel, rowCount, 2, // rows,
					// cols
					INITX, INITY, // initX, initY
					XPAD, YPAD,  // xPad, yPad
					maxX, maxY);

			considerReplacePanel.validate();
			considerReplacePanel.setVisible(true);
		}
	}

	private int makePropValsPanel(Set<PropertyValue> propertyValues) {

		int rowCount = 0;
		JLabel propLabel;
		JLabel valLabel;
		PropertyValue propVal;
		String name="";
		String value="";

		propertyValuesPanel.removeAll();
		//right now property values on terms are stored as tag value where the tag 
		//is property_value, and the value is the tag+value.  these need to be
		//split
		for (Iterator<PropertyValue> it = propertyValues.iterator(); it.hasNext();) {
		  propVal = it.next();

		  LOG.trace("prop val "+propVal+" prop "+propVal.getProperty());
		  if (propVal != null) {
		    if (propVal.getProperty().equals("property_value")) {
		      LOG.trace("prop val ok");
		      /*
		       * property_value: married_to heather
		       * property_value: shoe_size "8" xsd:positiveInteger			
		       * 
		       * these go into propertyValSet
		       */
//		      int quoteIndex = findUnescaped(propVal.getValue(), '"', 0, propVal.getValue().length());
		      int quoteIndex = -1;
		      if (quoteIndex == -1) {
		        /* no quotes: 
		         * this means we have a pv of the following form:
		         * property_value: eats id:1234
		         * 
		         * ie linking two instanes
		         */
		        StringTokenizer tokenizer = new StringTokenizer(propVal.getValue());
		        List tokens = new Vector();
		        while (tokenizer.hasMoreTokens()) {
		          tokens.add(tokenizer.nextToken());
		        }
		        name = (String)tokens.get(0);
		        value = (String)tokens.get(1);
		      } else {
//		        /*  quotes: 
//		        * this means we have a pv of the following form:
//		        * property_value:  shoe_size "8" xsd:positiveInteger
//		        * 
//		        * ie linking an instance with a property value
//		        */
//		        SOPair p = unescape(value, '"', quoteIndex + 1, value
//		        .length(), true);
//		        String propID = value.substring(0, quoteIndex).trim();
//		        String optional = value.substring(p.index + 1,
//		        value.length()).trim();
		        //testing to see if in someproperty: value
		      }
		    } else {
		      name = propVal.getProperty();
		      value = propVal.getValue();
		    }
		    propLabel = new JLabel("<html>"+name+"</html>");
		    propLabel.validate();
		    valLabel = new JLabel("<html>"+value+"</html>");
		    valLabel.validate();
		    propLabel.setLabelFor(valLabel);
		    propLabel.setVerticalAlignment(JLabel.TOP);
		    propertyValuesPanel.add(propLabel);
		    propertyValuesPanel.add(valLabel);
		    rowCount++;
		  }
		}
			if (rowCount > 0) {
				SpringUtilities.makeCompactGrid(propertyValuesPanel, rowCount, 2, // rows,
						// cols
						INITX, INITY, // initX, initY
						XPAD, YPAD); // xPad, yPad

				int[] maxX = {PREFERREDX,-1};
				int[] maxY = null;
				SpringUtilities.fixCellWidth(propertyValuesPanel, rowCount, 2, // rows,
						// cols
						INITX, INITY, // initX, initY
						XPAD, YPAD,  // xPad, yPad
						maxX, maxY);
				propertyValuesPanel.validate();
				propertyValuesPanel.repaint();
				propertyValuesPanel.setVisible(true);
			} else {
				propertyValuesPanel.removeAll();
				propertyValuesPanel.setVisible(false);
			}
			LOG.trace("rows= "+ rowCount);
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
		try {
			OBOObject term = CharFieldManager.inst().getOboClass(id); // ex
			setTextFromOboClass(term);
			// send out term selection (non mouse over) for DAG view
//			this.selectionManager.selectTerm(this, term, true);
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
	
	public String getObjHref(LinkedObject obj) {
		return
			"<html><a href='" + obj.getID() + "'>" + obj.getName() + "</a></html>";
	}

	public HyperlinkLabel getObjHrefLabel(LinkedObject obj) {

		HyperlinkLabel textArea = new HyperlinkLabel();
		termHyperlinkListener = new TermHyperlinkListener();
		textArea.addStringLinkListener(termHyperlinkListener);
		textArea.setText(getObjHref(obj));
		return textArea;
	}

	
	

	float getAnnotationInformationContentByClass(OBOObject oboClass) {
		OBOSession session = CharFieldManager.inst().getOboSession();
		
		if (!isIncludeExternalDatabaseAnnotations())
			return 0;
		
		if (cachedAnnotationInformationContentByClass.containsKey(oboClass))
			return cachedAnnotationInformationContentByClass.get(oboClass);
		
		// TODO: more sensible behavior when we have >1 externaldb
		for (OBDSQLDatabaseAdapterConfiguration config : Config.inst().getExternalDatabaseConfigs()) {
			OBDSQLDatabaseAdapter adapter = new OBDSQLDatabaseAdapter();
			adapter.setConfiguration(config);
			config.setAnnotationMode(OBDSQLDatabaseAdapterConfiguration.AnnotationMode.ANNOTATIONS_ONLY);
			
			try {
				adapter.connect();
				//OBOSession session = CharFieldManager.inst().getOboSession();
				float ic =
					adapter.fetchAnnotationInformationContentByObject(session, oboClass);

				cachedAnnotationInformationContentByClass.put(oboClass,ic);
				adapter.disconnect();
				return ic;
				//allAnnots.addAll(adapter.retrieveAllAnnotations(session));
				//adapter.fetchAll(session);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	int getAnnotationCountByClass(OBOObject oboClass) {
		OBOSession session = CharFieldManager.inst().getOboSession();
		
		if (!isIncludeExternalDatabaseAnnotations())
			return 0;

		int total = 0;
		if (cachedAnnotationCountByClass.containsKey(oboClass))
			return cachedAnnotationCountByClass.get(oboClass);

		for (OBDSQLDatabaseAdapterConfiguration config : Config.inst().getExternalDatabaseConfigs()) {
			OBDSQLDatabaseAdapter adapter = new OBDSQLDatabaseAdapter();
			adapter.setConfiguration(config);
			config.setAnnotationMode(OBDSQLDatabaseAdapterConfiguration.AnnotationMode.ANNOTATIONS_ONLY);
			
			try {
				adapter.connect();
				//OBOSession session = CharFieldManager.inst().getOboSession();
				int num =
					adapter.fetchAnnotationCountByObject(session, oboClass);
				total += num;
				adapter.disconnect();
				//allAnnots.addAll(adapter.retrieveAllAnnotations(session));
				//adapter.fetchAll(session);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		cachedAnnotationCountByClass.put(oboClass,total);
		return total;
	}

	
	Collection<Annotation> getAnnotationsByClass(OBOObject oboClass) {
		OBOSession session = CharFieldManager.inst().getOboSession();
		Collection<Annotation> allAnnots = AnnotationUtil.getAnnotations(session);
		Collection<Annotation> matches = new HashSet<Annotation>();
		
		// TODO: complete this. Demo mode only
		if (isIncludeExternalDatabaseAnnotations()) {
			for (OBDSQLDatabaseAdapterConfiguration config : Config.inst().getExternalDatabaseConfigs()) {
				OBDSQLDatabaseAdapter adapter = new OBDSQLDatabaseAdapter();
				adapter.setConfiguration(config);
				config.setAnnotationMode(OBDSQLDatabaseAdapterConfiguration.AnnotationMode.ANNOTATIONS_ONLY);
				
				try {
					adapter.connect();
					//OBOSession session = CharFieldManager.inst().getOboSession();
					Collection<Annotation> annots = 
						adapter.fetchAnnotationsByObject(session, oboClass);
					matches.addAll(annots);
					//allAnnots.addAll(adapter.retrieveAllAnnotations(session));
					//adapter.fetchAll(session);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}

		}
		
		/* currently we have a very crude way of matching.
		 * ideally we would use the reasoner, or at least ancestors.
		 * however, we don't want to get stuck on explosive cycles so
		 * lets be safe for now.
		 * Just use exact matches OR matches to components of a post-comp
		 */
		for (Annotation annot : allAnnots) {
//			LOG.debug("annot:" +annot);
			
			LinkedObject o = annot.getObject();
			if (o == null)
				LOG.error("annotation has null object: "+o);
			boolean match = false;
			if (isIncludeImplicitAnnotations()) {
				//System.out.println("Testing for ancestors: "+o+" -> "+oboClass);
				if (TermUtil.hasAncestor(o, oboClass))
					match = true;
			}
			//System.out.println("Testing for direct: "+o+" -> "+oboClass);
			if (o.equals(oboClass))
				match = true;
			if (!match) {
				for (Link link : o.getParents())
					if (TermUtil.isIntersection(link) &&
							link.getParent().equals(oboClass))
						match = true;
			}
			
			if (match)
				matches.add(annot);
			LOG.trace("match="+match);

		}
		return matches;
	}
	
	public void getCurrentAnnotations() {
		//in this case, you've clicked the button and you want to see the annots,
		//so want to display if there's none.  but this shouldn't even 
		//be possible based on the counts
//		System.out.println("getting annotations!"+currentOboClass);
		if (currentOboClass!=null) {
			Collection<Annotation> annots = getAnnotationsByClass(currentOboClass);
//			if ((!showEmptyPanelsFlag) && (annots.size() == 0)) {
//				termInfoPanel.getComponent(16).setVisible(false);
//				annotationPanel.setVisible(false);
//			} else {
				// AnnotationPanel
				termInfoPanel.getComponent(16).setVisible(true);

				makeAnnotationPanel(annots);
				termInfoPanel.setBoxTitle("Annotations (" + annots.size()
						+ ")", 16);
				annotationPanel.validate();
				annotationPanel.repaint();
				annotationPanel.setVisible(true);
			}
		termInfoPanel.validate();
		termInfoPanel.repaint();
		this.validate();
		this.repaint();
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

  /** To drag main term (not yet parents/kids) from term info to fields
   subcalss SelectionTransferHandler, or just use straight up? */
  private class InfoTransferHandler extends TransferHandler {

    /** does a copy operation */
    public int getSourceActions(JComponent c) {
      return COPY;
    }

  }

  /** attempting to model oboedit selection to use for drag & drop of terms from term
      info to fields to mimic whats already done between graph and fields */
  private class InfoSelector implements ObjectSelector {
    public void select(Selection selection) {}

    /**
     * Most object selectors will just use this method as a delegate to
     * {@link #getSelection()}. This method should be overridden if a special
     * selection should be used for right-clicks, drags, etc based on mouse position
     */
    public Selection getSelection(MouseEvent e) { return null; }
    public Selection getSelection() { return null; }
    public LinkDatabase getLinkDatabase() { return null; }
    public RootAlgorithm getRootAlgorithm() { return null; }
    public void setLive(boolean isLive) {}
    public boolean isLive() { return false; }
    public void addSelectionListener(SelectionListener listener) {}
    public void removeSelectionListener(SelectionListener listener) {}
    public boolean hasCombinedTermsAndLinks() { return false; }
    public void addExpansionListener(ExpandCollapseListener listener) {}
    public void removeExpansionListener(ExpandCollapseListener listener) {}
    public Collection<PathCapable> getVisibleObjects() { return null; }
  }

}
