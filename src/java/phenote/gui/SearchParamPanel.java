package phenote.gui;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import phenote.datamodel.SearchParamsI;

class SearchParamPanel {

  private SearchParamsI searchParams;
  private JRadioButton termButton;
  private JRadioButton synonymButton;
  private JRadioButton definitionButton;
  private JRadioButton obsoleteButton;

  // SearchParamPanel() {
  // CompletionList.setSearchParams(new SearchParams()) }

  JPanel getPanel() {
    JPanel panel = new JPanel();
    BoxLayout boxLayout = new BoxLayout(panel,BoxLayout.Y_AXIS);
    panel.setLayout(boxLayout);
    panel.add(new JLabel("Search:"));
    termButton = makeButton("Terms");
    termButton.setSelected(true);
    panel.add(termButton);
    synonymButton = makeButton("Synonyms");
    panel.add(synonymButton);
    definitionButton = makeButton("Definitions");
    panel.add(definitionButton);
    obsoleteButton = makeButton("Obsolete");
    panel.add(obsoleteButton);
    return panel;
  }

  private JRadioButton makeButton(String label) {
    JRadioButton button = new JRadioButton(label);
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
    public boolean searchObsoletes() {
      return obsoleteButton.isSelected();
    }
  }

  // these are actually for TestPhenote
  void setTermSearch(boolean search) {
    termButton.setSelected(search);
  }
  void setSynonymSearch(boolean search) {
    synonymButton.setSelected(search);
  }

}
