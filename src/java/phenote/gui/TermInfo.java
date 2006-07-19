package phenote.gui;

import java.net.URL;
import java.util.Iterator;
//import java.util.Set;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.BrowserLauncherRunner;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

//import org.geneontology.oboedit.datamodel.Link;
//import org.geneontology.oboedit.datamodel.LinkedObject;
import org.geneontology.oboedit.datamodel.OBOClass;
//import org.geneontology.oboedit.datamodel.OBOProperty;

import phenote.datamodel.CharacterI;
import phenote.datamodel.OntologyManager;
import phenote.util.HtmlUtil;
import phenote.gui.selection.SelectionManager;
import phenote.gui.selection.TermSelectionEvent;
import phenote.gui.selection.TermSelectionListener;

class TermInfo {

  //private JEditorPane textArea;
  private JTextComponent textArea;
  //private static final boolean DO_HTML = false;
  private static final boolean DO_HTML = HtmlUtil.DO_HTML;
  //static final String PHENOTE_LINK_PREFIX = "Phenote?id=";
  private TermHyperlinkListener termHyperlinkListener;
  // current obo class being navigated
  private OBOClass currentOboClass;

  
  TermInfo(TermPanel termPanel) {
    // do this more generically? some sort of controller?
//     CompletionListListener l = new CompletionListListener();
//     termPanel.getEntityComboBox().addCompletionListListener(l);
//     termPanel.getPatoComboBox().addCompletionListListener(l);
//     if (termPanel.hasLumpComboBox())
//       termPanel.getLumpComboBox().addCompletionListListener(l);
    SelectionManager.inst().addTermSelectionListener(new InfoTermSelectionListener());
  }

  JComponent getComponent() {
    // JEditorPane allows for html formatting - bold... but doesnt do word wrap - bummer!
    // as far as i can tell
    // would have to explicitly put <br> in text. can also do hyperlinks!
    // for now just doing JTextArea
    if (DO_HTML) {
      JEditorPane editorPane = new JEditorPane(); 
      editorPane.setContentType("text/html"); // sets up HTMLEditorKit
      termHyperlinkListener = new TermHyperlinkListener();
      editorPane.addHyperlinkListener(termHyperlinkListener);
      textArea = editorPane;
    }
    else {
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
    scrollPane.setBorder(BorderFactory.createTitledBorder("Term Info"));
    return scrollPane;
  }

  // for TestPhenote
  String getText() { return textArea.getText(); }

  private void setTextFromOboClass(OBOClass oboClass) {
    currentOboClass = oboClass;

    String html = HtmlUtil.termInfo(oboClass);

    textArea.setText(html);
    // scroll to top (by default does bottom)
    textArea.setCaretPosition(0);
  }
  
  


  private class InfoTermSelectionListener implements TermSelectionListener {
    public boolean termSelected(TermSelectionEvent e) {
      setTextFromOboClass(e.getOboClass());
      return true;
    } 
  }

  /** put present term into current character */
  private void commitTerm() {
    CharacterI ch = SelectionManager.inst().getSelectedCharacter();
    // currentOboClass...
  }
  
  /** for testing */
  void simulateHyperlinkEvent(HyperlinkEvent e) {
    termHyperlinkListener.hyperlinkUpdate(e);
  }


  /** inner class TermHyperlink Listener, listens for clicks on term & external
      hyper links and birngs up the term or brings up the external web page */
  private class TermHyperlinkListener implements HyperlinkListener {

    public void hyperlinkUpdate(HyperlinkEvent e) {
      if (!(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED))
        return;

      URL url = e.getURL();
      //System.out.println("got url "+url+" desc "+e.getDescription());

      // internal link to term...
      if (HtmlUtil.isPhenoteLink(e)) {
        bringUpTermInPhenote(e);
        return;
      }

      if (url == null) { // relative urls are null
        System.out.println("invalid url "+url);
        return;
      }

      bringUpInBrowser(url);

      // ..... BrowserLauncher2? webstart BasicService? config browser...
      // somehow distinguish local links from non local?? 
      // look for localhost? - 
      // set up localhost servlet? or just pick out text from url?
      // hmmmmm......
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

//     private boolean isPhenoteLink(HyperlinkEvent e) {
//       return e.getURL() == null && e.getDescription().startsWith(PHENOTE_LINK_PREFIX);
//     }

    private void bringUpTermInPhenote(HyperlinkEvent e) {
//       String desc = e.getDescription();
//       if (desc == null || desc.equals("")) return;
//       String id = getIdFromDescription(desc);
      // or do through obo session?
      String id = HtmlUtil.getIdFromHyperlink(e);
      if (id == null) return;
      OBOClass term = OntologyManager.inst().getOboClass(id);
      setTextFromOboClass(term);
    }
//     private String getIdFromDescription(String desc) {
//       return desc.substring(PHENOTE_LINK_PREFIX.length());
//     }
  }
}


//   /** Listens for completion list selection (via moouse over) and populates
//       term info with term selected -- this is pase - replaced by
//       TermSelectionEvent/Listener */
//   private class CompletionListListener implements ListSelectionListener {
//     public void valueChanged(ListSelectionEvent e) {
//       //int index = e.getFirstIndex();
//       Object source = e.getSource();
//       // hate to cast but it is handy here... and it is in fact a JList
//       //System.out.println("TI got sel - src: "+source.getClass()+" list? "+(source instanceof javax.swing.JList));
//       if (!(source instanceof JList)) {
//         System.out.println("source of combo box mouse over is not JList "+
//                            source.getClass());
//         return;
//       }
//       JList jList = (JList)source;
//       Object selectedValue = jList.getSelectedValue();
//       if (selectedValue == null)
//         return;
//       //System.out.println("sel val "+selectedValue.getClass()+" name "+selectedValue);
//       // the selected item should be an OBOClass
//       if (!(selectedValue instanceof OBOClass)) {
//         System.out.println("selected completion term is not obo class "
//                            +selectedValue.getClass());
//         return;
//       }
//       OBOClass oboClass = (OBOClass)selectedValue;
//       setTextFromOboClass(oboClass);
//     }
//   } // end of CompletionListListener inner class
