package phenote.dataadapter.phenosyntax;

import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.datamodel.CharacterI;

/** A phenotype character thats basically a dataadapter object for datamodel
    CharacterI. It can make a phenosyntax string from a CharacterI and make
    a CharacterI from a phenosyntax string  e.g. E=head Q=large
    See http://www.fruitfly.org/~cjm/obd/pheno-syntax.html for a full description
    of pheno syntax.

    Phenote additions: syntax doesnt do genotype or genetic context but heck we
    need them - so i added GT=genotype (do we gen context?) GC=geneticContext
*/

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

    if (character.hasPub())
      sb.append("PUB=").append(character.getPub());
    // Genotype - not strictly part of pheno syntax but lets face it we need it
    // i would say its an omission from syntax
    sb.append(" GT=").append(character.getGenotype());
    if (character.hasGeneticContext())
      sb.append(" GC=").append(idPrefixAndName(character.getGeneticContext()));
    if (character.getEntity() == null) {
      System.out.println("Error: character has no entity, ignoring");
      return "";
    }
    sb.append(" E=").append(idPrefixAndName(character.getEntity()));
    if (character.getQuality() == null) {
      System.out.println("Error: character has no quality, ignoring");
      return "";
    }
    sb.append(" Q=").append(idPrefixAndName(character.getQuality()));

    return sb.toString();
  }

  /** Merges id prefix and name, so for id GO:1234 with name "growth" returns
      "GO:growth", which is readable and computable & syn acceptable */
  private String idPrefixAndName(OBOClass term) {
    return getIdPrefix(term)+term.getName();
  }
    

  /** for GO:12345 returns GO: - with colon! */
  private String getIdPrefix(OBOClass term) {
    if (term == null) return ""; // shouldnt happen
    String id = term.getID();
    int colonIndex = id.indexOf(":");
    return id.substring(0,colonIndex+1); // +1 retain colon
  }

}
