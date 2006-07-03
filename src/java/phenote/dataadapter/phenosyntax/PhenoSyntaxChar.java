package phenote.dataadapter.phenosyntax;

import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.datamodel.CharacterI;

/** A phenotype character thats basically a dataadapter object for datamodel
    CharacterI. It can make a phenosyntax string from a CharacterI and make
    a CharacterI from a phenosyntax string  e.g. E=head Q=large
    See http://www.fruitfly.org/~cjm/obd/pheno-syntax.html for a full description
    of pheno syntax.*/

public class PhenoSyntaxChar {

  private CharacterI character;
  private String phenoSyntaxString;

  PhenoSyntaxChar(CharacterI ch) {
    character = ch;
  }

  String getPhenoSyntaxString() {
    if (phenoSyntaxString == null) {
      phenoSyntaxString = makeSyntaxString();
    }
    return phenoSyntaxString;
  }

  private String makeSyntaxString() {
    if (character == null) { // shouldnt happen
      System.out.println("Error: no Character to make phenoSyntax string with");
      return ""; //??
    }
    StringBuffer sb = new StringBuffer();
    sb.append("E= ").append(idPrefixAndName(character.getEntity()));
    //sb.append

    // work in progress - abandoned for pheno xml adapter - revisit for flybase...

    return null;
  }

  /** Merges id prefix and name, so for id GO:1234 with name "growth" returns
      "GO:growth", which is readable and computable & syn acceptable */
  private String idPrefixAndName(OBOClass term) {
    return getIdPrefix(term)+term.getName();
  }
    

  /** for GO:12345 returns GO: - with colon! */
  private String getIdPrefix(OBOClass term) {
    String id = term.getID();
    int colonIndex = id.indexOf(":");
    return id.substring(colonIndex);
  }

}
