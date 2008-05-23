package phenote.gui.field;

import java.util.Arrays;
import java.util.List;

import org.obo.datamodel.Link;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOClass;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharacterI;
import phenote.datamodel.TermNotFoundException;
import ca.odell.glazedlists.matchers.Matcher;

/**
 * A GlazedLists Matcher implementation which matches characters using a given charfield.
 * @author Jim Balhoff
 */
public class CharFieldMatcher implements Matcher<CharacterI> {
  
  private final CharField charField;
  private final String filter;
  private final boolean broad;

  /**
   * @param charField The charfield examined to match characters.
   * @param filter The value required for character to match.  For free text charfields, this will match substrings.
   * @param broad Whether to match all descendants of an ontology term in addition to matching the exact ontology term, or for text, whether to match substrings in addition to exactly.
   */
  public CharFieldMatcher(CharField charField, String filter, boolean broad) {
    this.charField = charField;
    this.filter = filter;
    this.broad = broad;
  }

  public boolean matches(CharacterI character) {
    if ((this.filter == null) || (this.filter.equals(""))) return true;
    if (this.charField.isTerm()) {
      return this.matchTerm(character);
    } else {
      return this.matchText(character);
    }
  }
  
  private boolean matchTerm(CharacterI character) {
    try {
      final CharFieldValue value = character.getValue(this.charField);
      final OBOClass candidateTerm = value.getTerm();
      final OBOClass filterTerm = CharFieldManager.inst().getOboClass(this.filter);
      if (this.broad) {
        return this.termIsAncestorOfTerm(filterTerm, candidateTerm);
      } else {
        return filterTerm.equals(candidateTerm);
      }
    } catch (TermNotFoundException e) {
      return false;
    }
  }
  
  private boolean matchText(CharacterI character) {
    final String value;
    if (this.charField.equals(CharFieldMatcherEditor.ANY_FIELD)) {
      final StringBuffer allFieldValues = new StringBuffer();
      for (CharField field : character.getAllCharFields()) {
        allFieldValues.append(character.getValueString(field));
        allFieldValues.append(" ");
      }
      value = allFieldValues.toString();
    } else {
      value = character.getValueString(this.charField);
    }
    if (this.broad) {
      return this.matchFilterList(Arrays.asList(this.filter.split(" ")), value);
    } else {
      return value.equals(this.filter);
    }
  }
  
  /**
   * Match a value by matching every word in the list
   */
  private boolean matchFilterList(List<String> filterList, String value) {
    final boolean foundElsewhere = filterList.size() > 1 ? this.matchFilterList(filterList.subList(1, filterList.size()), value) : true;
    return (value.toLowerCase().indexOf(filterList.get(0).toLowerCase()) != -1) && foundElsewhere;
  }
  
  private boolean termIsAncestorOfTerm(LinkedObject ancestor, LinkedObject descendant) {
    if (ancestor.equals(descendant)) return true;
    for (Link link : ancestor.getChildren()) {
      if (this.termIsAncestorOfTerm(link.getChild(), descendant)) {
        return true;
      }
    }
    return false;
  }

}
