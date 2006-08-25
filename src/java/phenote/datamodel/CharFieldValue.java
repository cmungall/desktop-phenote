package phenote.datamodel;

import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.datamodel.CharField.CharFieldEnum;

/** At the moment char field values can be Strings (genotype) and OBOClasses, and possibly
    more coming (Genotype?). This class attempts to hide the details of the actual
    data structure of the value for the field - could be String or OBOClass but can
    still deal with both the same - at least thats the idea... */
public class CharFieldValue {

  private OBOClass oboClassValue=null;
  private String stringValue=null;
  private boolean isOboClass=true;
  private CharFieldEnum charFieldEnum;
  // private CharField???
  private CharacterI character;

  public CharFieldValue(String s,CharacterI c, CharFieldEnum e) {
    stringValue = s;
    isOboClass = false;
    character = c;
    charFieldEnum = e;
  }

  public CharFieldValue(OBOClass o,CharacterI c,CharFieldEnum e) {
    oboClassValue = o;
    isOboClass = true;
    character = c;
    charFieldEnum = e;
  }

  // maybe this should be called getString??? why getName???
  public String getName() { 
    if (!isOboClass)
      return stringValue;
    if (oboClassValue != null) // obo class may not be set yet
      return oboClassValue.getName();
    return ""; // null?
  }

  OBOClass getOboClass() { return oboClassValue; }

  public void editModel() {
    if (charFieldEnum == null)
      System.out.println("ERROR no datamodel associated with configuration, cant set"+
                         " value");
    else
      charFieldEnum.setValue(character,this);
  }

  public CharFieldEnum getCharFieldEnum() { return charFieldEnum; }

  public String toString() { return getName(); }
}

