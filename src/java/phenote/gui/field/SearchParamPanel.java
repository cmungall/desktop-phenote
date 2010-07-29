package phenote.gui.field;

// also used by servlet - repackage to phenote.ontologysearch?

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import phenote.gui.SearchFilterType;
import phenote.gui.SearchParamsI;

/** this isnt used at the moment - nicole moved search params to menu - maybe delete?
    or maybe this is something for the docking interface 
SearchParams need to get up to snuff with new interface if we bring this back*/
//import phenote.datamodel.SearchParamsI;
// only public for TestPhenote
public class SearchParamPanel {

  private SearchParamsI searchParams;
  private JCheckBox termButton;
  private JCheckBox synonymButton;
  private JCheckBox definitionButton;
  private JCheckBox obsoleteButton;

  // SearchParamPanel() {
  // CompletionList.setSearchParams(new SearchParams()) }

  JPanel getPanel() {
    JPanel panel = new JPanel();
    //panel.setMinimumSize(new Dimension(305,150));
    panel.setPreferredSize(new Dimension(60,50));
    // panel.setPreferredSize(new Dimension(250,250)); ??
    BoxLayout boxLayout = new BoxLayout(panel,BoxLayout.Y_AXIS);
    panel.setLayout(boxLayout);
    panel.add(new JLabel("Search:"));
    termButton = makeButton("Term");
    termButton.setSelected(true);
    panel.add(termButton);
    synonymButton = makeButton("Syn");
    panel.add(synonymButton);
    definitionButton = makeButton("Def");
    panel.add(definitionButton);
    obsoleteButton = makeButton("Obs");
    panel.add(obsoleteButton);
    return panel;
  }

  private JCheckBox makeButton(String label) {
    JCheckBox button = new JCheckBox(label);
    return button;
  }
  

  // static SearchParamsI getSearchParams() ???
  // but may want separate guis/params for each ontology/field??

  SearchParamsI getSearchParams() {
    if (searchParams == null)
      searchParams = new SearchParams();
    return searchParams;
  }

  private class SearchParams implements SearchParamsI {
    /** non-obsolete terms that is */
    public boolean searchTerms() {
      return termButton.isSelected();
    }
    public boolean searchSynonyms() {
      return synonymButton.isSelected();
    }
    public boolean searchDefinitions() {
      return definitionButton.isSelected();
    }
    public boolean searchDbxrefs() {
      return false; // for now--haven't added a dbxrefs button.
    }
    public boolean searchObsoletes() {
      return obsoleteButton.isSelected();
    }
    public boolean verifySettings() { return false; } // ?
    public boolean getParam(SearchFilterType filter) { return false; } // ?
    public void setParam(SearchFilterType filter, boolean setting) {} // ?
  }

  // these are actually for TestPhenote
  public void setTermSearch(boolean search) {
    termButton.setSelected(search);
  }
  public void setSynonymSearch(boolean search) {
    synonymButton.setSelected(search);
  }

}
