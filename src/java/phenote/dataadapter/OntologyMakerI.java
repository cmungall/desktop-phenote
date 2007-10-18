package phenote.dataadapter;

/** A TermMaker is a "plugin" that can be used to make terms dynamically. for instance
    the proforma allele parser parses a proforma file, extracts alleles and makes
    terms out of them to be used in fields in the genotype maker */

public interface OntologyMakerI {

  /** Make terms and send off to fields that are using such terms */
  public void makeOntology();

  public boolean useButtonToLaunch();

  public String getButtonText();

}
