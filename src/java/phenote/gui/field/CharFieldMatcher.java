package phenote.gui.field;

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
 * @author Jim Balhoff
 * A GlazedLists Matcher implementation which matches characters using a given charfield.
 */
public class CharFieldMatcher implements Matcher<CharacterI> {
  
  private final CharField charField;
  private final String filter;
  private final boolean inherit;

  /**
   * @param charField The charfield examined to match characters.
   * @param filter The value required for character to match.  For free text charfields, this will match substrings.
   * @param inherit Whether to match all descendants of an ontology term in addition to matching the exact ontology term.
   */
  public CharFieldMatcher(CharField charField, String filter, boolean inherit) {
    this.charField = charField;
    this.filter = filter;
    this.inherit = inherit;
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
      if (this.inherit) {
        return this.termIsAncestorOfTerm(filterTerm, candidateTerm);
      } else {
        return filterTerm.equals(candidateTerm);
      }
    } catch (TermNotFoundException e) {
      return false;
    }
  }
  
  private boolean matchText(CharacterI character) {
    final String value = character.getValueString(this.charField);
    return value.toLowerCase().indexOf(this.filter.toLowerCase()) != -1;
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
