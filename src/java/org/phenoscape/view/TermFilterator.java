package org.phenoscape.view;

import java.util.List;

import org.obo.datamodel.OBOClass;
import org.obo.datamodel.Synonym;

import ca.odell.glazedlists.TextFilterator;

public class TermFilterator implements TextFilterator<OBOClass> {

  public void getFilterStrings(List<String> list, OBOClass term) {
    list.add(term.getName());
    for (Synonym synonym : term.getSynonyms()) {
      list.add(synonym.getText());
    }
  }
  
}
