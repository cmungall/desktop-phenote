package phenote.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import java.io.FileNotFoundException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;

import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.datamodel.CharFieldManager;
import phenote.datamodel.TermNotFoundException;
import phenote.error.ErrorEvent;
import phenote.error.ErrorListener;
import phenote.error.ErrorManager;
import phenote.gui.selection.SelectionManager;
import phenote.gui.selection.TermSelectionEvent;
import phenote.gui.selection.TermSelectionListener;
import phenote.gui.selection.UseTermEvent;
import phenote.gui.selection.UseTermListener;
import phenote.util.HtmlUtil;
import phenote.util.FileUtil;
import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.BrowserLauncherRunner;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

public class TermInfo {

  //private JEditorPane textArea;
  private JTextComponent textArea;
  private static final boolean DO_HTML = HtmlUtil.DO_HTML;
  private TermHyperlinkListener termHyperlinkListener;
  // current obo class being navigated
  private OBOClass currentOboClass;
  private UseTermListener useTermListener;
  private JPanel termInfoPanel;
  private JPanel naviPanel;
  private JButton useTermButton;
  private JEditorPane termField;
  private static int TERM_INFO_DEFAULT_WIDTH=350;
  private static int TERM_INFO_DEFAULT_HEIGHT=400;
  private static int BUTTON_HEIGHT = 30;
  private List termInfoNaviHistory = new List();
  private int naviIndex=-1;
  private SelectionManager selectionManager;
  
  public TermInfo() { //TermPanel termPanel) {
    this(SelectionManager.inst());
  }
  
  public TermInfo(SelectionManager selManager) {
    this.selectionManager = selManager;
    this.selectionManager.addTermSelectionListener(new InfoTermSelectionListener());
    //ErrorManager.inst().addErrorListener(new InfoErrorListener());
  }

  public JComponent getComponent() {
    termInfoPanel = new JPanel(new BorderLayout(0,0)); // hgap,vgap
    termInfoPanel.setPreferredSize(new Dimension(TERM_INFO_DEFAULT_WIDTH,TERM_INFO_DEFAULT_HEIGHT));
    termInfoPanel.setMinimumSize(new Dimension(200,200));
    //termInfoPanel.setMaximumSize(new Dimension(380,400));
    if (DO_HTML) {
    	JEditorPane editorPane = new JEditorPane(); 
      editorPane.setContentType("text/html"); // sets up HTMLEditorKit
      termHyperlinkListener = new TermHyperlinkListener();
      editorPane.addHyperlinkListener(termHyperlinkListener);
      textArea = editorPane;
    }
    else { // pase - delete?
      JTextArea jTextArea = new JTextArea(17,50);
      jTextArea.setLineWrap(true);
      jTextArea.setWrapStyleWord(true);
      textArea = jTextArea;
    }
    textArea.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setPreferredSize(new Dimension(400,300));
    scrollPane.setMaximumSize(new Dimension(400,300));
    // border - make JPanel for it (there is a disclaimer about non JPanel)
    //scrollPane.setBorder(BorderFactory.createTitledBorder("Term Info"));
    termInfoPanel.setBorder(BorderFactory.createTitledBorder("Term Info"));
    termInfoPanel.add(scrollPane,BorderLayout.CENTER);
    //Layout doesn't look good right now.  Will fix
    ImageIcon ok = new ImageIcon();
    ImageIcon back = new ImageIcon();
    ImageIcon forward = new ImageIcon();
		try {
	    ok = new ImageIcon(FileUtil.findUrl("images/OK.GIF"));
		}	catch (FileNotFoundException ex) {  }

    useTermButton = new JButton(ok);
//    JButton useTermButton = new JButton("Use Term");
    useTermButton.addActionListener(new UseTermActionListener());
    useTermButton.setPreferredSize(new Dimension(BUTTON_HEIGHT, BUTTON_HEIGHT));
    useTermButton.setMinimumSize(new Dimension(BUTTON_HEIGHT, BUTTON_HEIGHT));
    useTermButton.setToolTipText("Use Term");
		try {
	    back = new ImageIcon(FileUtil.findUrl("images/arrow.small.left.gif"));
		}	catch (FileNotFoundException ex) {  }
    JButton backButton = new JButton(back);
    backButton.setToolTipText("Go back a term");
    backButton.setPreferredSize(new Dimension(BUTTON_HEIGHT, BUTTON_HEIGHT));
    backButton.setMinimumSize(new Dimension(BUTTON_HEIGHT, BUTTON_HEIGHT));
    backButton.setMaximumSize(new Dimension(BUTTON_HEIGHT, BUTTON_HEIGHT));
    try {
    forward = new ImageIcon(FileUtil.findUrl("images/arrow.small.right.gif"));
		}	catch (FileNotFoundException ex) {  }
    JButton forwardButton = new JButton(forward);
    forwardButton.setToolTipText("Go forward a term");
    forwardButton.setPreferredSize(new Dimension(BUTTON_HEIGHT, BUTTON_HEIGHT));
    forwardButton.setMinimumSize(new Dimension(BUTTON_HEIGHT, BUTTON_HEIGHT));
    forwardButton.setMaximumSize(new Dimension(BUTTON_HEIGHT, BUTTON_HEIGHT));
    JPanel naviButtons = new JPanel();
    naviButtons.setMinimumSize(new Dimension((3*BUTTON_HEIGHT), BUTTON_HEIGHT));
    naviButtons.setPreferredSize(new Dimension((3*BUTTON_HEIGHT), BUTTON_HEIGHT));
    naviButtons.setMaximumSize(new Dimension((3*BUTTON_HEIGHT), BUTTON_HEIGHT));
    naviButtons.setLayout(new BorderLayout(0,0));
    naviPanel = new JPanel();
    naviPanel.setLayout(new BorderLayout(0,0));
    JPanel bottomPanel = new JPanel();
    bottomPanel.setBackground(Color.lightGray);
    bottomPanel.setLayout(new BorderLayout(0,0));
    forwardButton.addActionListener(new ForwardNaviActionListener());
    backButton.addActionListener(new BackNaviActionListener());
//    bottomPanel.add(backButton, BorderLayout.WEST);
//    bottomPanel.add(forwardButton, BorderLayout.CENTER);
    naviPanel.add(useTermButton);
    //termInfoPanel.add(bottomPanel,BorderLayout.SOUTH);
    termField =  new JEditorPane();
    termField.setContentType("text/html");
    termField.setText("(no term selected)");
    termField.setPreferredSize(new Dimension(TERM_INFO_DEFAULT_WIDTH,BUTTON_HEIGHT));
    termField.setEditable(false);  
    naviButtons.add(useTermButton, BorderLayout.WEST);
    naviButtons.add(backButton, BorderLayout.CENTER);
    naviButtons.add(forwardButton, BorderLayout.EAST);
    naviPanel.add(naviButtons, BorderLayout.EAST);
    naviPanel.add(termField, BorderLayout.CENTER);
    termInfoPanel.add(naviPanel, BorderLayout.NORTH);

    ErrorManager.inst().addErrorListener(new InfoErrorListener());

    return termInfoPanel;
  }
  
//  public void resizeTermInfoPanel() {
//	  //for now, these are absolute dimensions.  in future, modify this
//	  //to accomodate any variable dimensions.
//	    termInfoPanel.setPreferredSize(new Dimension(400,100));
//	    termInfoPanel.revalidate();
//	    termInfoPanel.repaint();
//	  
//  }

  /** Fires use term event to use term listener with currently browsed term when
      useTermButton is pressed - i think this causes the model to be edited? 
  TermCompList listens for */
  private class UseTermActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      //commitTerm();
      // relation comp list sets to null
      if (useTermListener == null) return;
      if (currentOboClass == null) return; // shouldnt happen
      useTermListener.useTerm(new UseTermEvent(TermInfo.this,currentOboClass));

    }
  }
  
  private class BackNaviActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
    	String id="";
    	if (naviIndex>0) {
    		naviIndex--;
    		id = getTermFromNaviHistory(naviIndex);
    		try {
    			OBOClass term = CharFieldManager.inst().getOboClass(id); // ex
    			setTextFromOboClass(term);
    			// send out term selection (non mouse over) for DAG view
          TermInfo.this.selectionManager.selectTerm(TermInfo.this, term, true);
    		}
    		catch (TermNotFoundException ex) { return; }
    	}
    }
  }
  private class ForwardNaviActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
  	int tot = termInfoNaviHistory.getItemCount();
    	if (naviIndex<(tot-1)) {
    		naviIndex++;
    		String id = getTermFromNaviHistory(naviIndex);
    		try {
    			OBOClass term = CharFieldManager.inst().getOboClass(id); // ex
    			setTextFromOboClass(term);
    			// send out term selection (non mouse over) for DAG view
          TermInfo.this.selectionManager.selectTerm(TermInfo.this, term, true);
    		}
    		catch (TermNotFoundException ex) { return; }
    	}
    }
  }
  // for TestPhenote
  //String getVerbiage() { return textArea.getText(); }
  String getTermFieldText() { return termField.getText(); }

  private void setTextFromOboClass(OBOClass oboClass) {
    currentOboClass = oboClass;

    String html = HtmlUtil.termInfo(oboClass);
    String term = HtmlUtil.termName(oboClass);
    textArea.setText(html);
    termField.setText(term);
    useTermButton.setEnabled(!oboClass.isObsolete());
    // scroll to top (by default does bottom)
    textArea.setCaretPosition(0);
  }
  
  
  /** Listen for selection from phenote (mouse over completion list) */
  private class InfoTermSelectionListener implements TermSelectionListener {
    public boolean termSelected(TermSelectionEvent e) {
    	
      if (!e.isMouseOverEvent() ) {
      	//add the item to the navi history if selected from list only
      	String id=e.getOboClass().getID();
        addTermToNaviHistory(id);
      	return false;
      }
      setTextFromOboClass(e.getOboClass());
      // This sets who now listens to use term button clicks (only 1 listener)
      setUseTermListener(e.getUseTermListener());
      return true;
    } 
  }

  private void setUseTermListener(UseTermListener utl) {
    useTermListener = utl;
  }

  
  /** for testing */
  void simulateHyperlinkEvent(HyperlinkEvent e) {
    termHyperlinkListener.hyperlinkUpdate(e);
  }

  private void addTermToNaviHistory(String link) {
  	int tot = termInfoNaviHistory.getItemCount();
  	if ((tot-1)>naviIndex) { //we're in the middle of the navi
  		if (naviIndex>=0) { //we're not at the beginning
  		//remove all the items between end and here
  			for (int i=(tot-1); i>naviIndex; i--) {
  				termInfoNaviHistory.remove(i);
  			}
  		}
  	}
		termInfoNaviHistory.add(link);
		naviIndex++;  //we should be at the end of the history
//  	System.out.println("tot="+tot+"; naviIndex="+naviIndex);
  }

  private String getTermFromNaviHistory(int position) {
  	if (termInfoNaviHistory.getItemCount() >= 1)
  		return termInfoNaviHistory.getItem(position);
  	else 
  		return "";
  }
  /** inner class TermHyperlink Listener, listens for clicks on term & external
      hyper links and brings up the term or brings up the external web page */
  private class TermHyperlinkListener implements HyperlinkListener {

    public void hyperlinkUpdate(HyperlinkEvent e) {
      if (!(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED))
        return;

      URL url = e.getURL();
      //System.out.println("got url "+url+" desc "+e.getDescription());

      // internal link to term...
      if (HtmlUtil.isPhenoteLink(e)) {
        bringUpTermInTermInfo(e);
        return;
      }

      if (url == null) { // relative urls are null
        System.out.println("invalid url "+url);
        return;
      }

      bringUpInBrowser(url);

    }

    private void bringUpInBrowser(URL url) {
      if (url == null) return;
      try {
        BrowserLauncher bl = new BrowserLauncher(null); // no logger
        BrowserLauncherRunner br = new BrowserLauncherRunner(bl,url.toString(),null);
        new Thread(br).start();
      }
      catch (BrowserLaunchingInitializingException be) {
        System.out.println("cant launch browser "+be);
      }
      catch (UnsupportedOperatingSystemException ue) {
        System.out.println("cant launch browser "+ue);
      }
    }


    private void bringUpTermInTermInfo(HyperlinkEvent e) {
      // or do through obo session?
      String id = HtmlUtil.getIdFromHyperlink(e);
      if (id == null) return;
      try {
        OBOClass term = CharFieldManager.inst().getOboClass(id); // ex
        setTextFromOboClass(term);
        addTermToNaviHistory(id);
        // send out term selection (non mouse over) for DAG view
        TermInfo.this.selectionManager.selectTerm(TermInfo.this, term, true);
      }
      catch (TermNotFoundException ex) { return; }
    }
  }

  private class InfoErrorListener implements ErrorListener {

    // retrieve any errors sitting in error manager and display?
    // this is handy for errors that happened before term info came up
    // is this funny? and if theres more than one then what?
    private InfoErrorListener() {
      for (ErrorEvent e : ErrorManager.inst().getErrors())
        handleError(e);
    }

    public void handleError(ErrorEvent e) {
      GuiUtil.doBlinker(textArea); // and/or termField?
      GuiUtil.doBlinker(termField);
      textArea.setText(e.getMsg());
      termField.setText("ERROR!");
    }
  }

}

//   /** put present term into current character - not used yet...
//    this is now policy of the UseTermListener(AutoComboBox) TermInfo just sends out
//    UseTermEvent and UTL/ACB decides what to do with it */
//   private void commitTerm() {
//     CharacterI ch = SelectionManager.inst().getSelectedCharacter();
//     if (ch == null) { // can this happen?
//       System.out.println("ERROR: no character selected to update");
//       return;
//     }
//     if (currentOboClass == null) { // can this happen?
//       System.out.println("ERROR: no term in term info to add to character");
//       return;
//     }

//     CharFieldEnum cfe = OntologyManager.inst().getCharFieldEnumForOboClass(currentOboClass);
    
//     UpdateTransaction ut = new UpdateTransaction(ch,cfe,currentOboClass);
//     EditManager.inst().updateModel(this,ut);
//     //previousOboClass = currentOboClass;
//   }
     // JEditorPane allows for html formatting - bold... but doesnt do word wrap - bummer!
