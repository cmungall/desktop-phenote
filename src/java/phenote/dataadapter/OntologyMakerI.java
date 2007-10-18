package phenote.dataadapter;

/** A TermMaker is a "plugin" that can be used to make terms dynamically. for instance
    the proforma allele parser parses a proforma file, extracts alleles and makes
    terms out of them to be used in fields in the genotype maker */

public interface OntologyMakerI {

  /** Make terms and send off to fields that are using such terms */
  public void makeOntology();

  public boolean useButtonToLaunch();

  public String getButtonText();

  /** the destination field that this group is populating (with obo classes), if
      a group is not populating a destination field this would be no-oped
      one could imagine other destinations like main datamodel */
  public void addDestinationField(String field); // CharField? Ex?

}
