package phenote.dataadapter.fly;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;

import org.apache.log4j.Logger;
import org.obo.datamodel.Instance;
import org.obo.datamodel.Namespace;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.impl.InstanceImpl;
import org.obo.datamodel.impl.OBOClassImpl;
import org.obo.datamodel.impl.OBOSessionImpl;

import phenote.dataadapter.OntologyMaker;

/** not sure if this is an appropriate implementation of GroupAdapterI, group adapters
    originally were groups of fields this is not, but one thing group adapters do is make
    terms/ontology on the fly - and thats what this does - but it does make ya wanna
    change the name of GroupAdap if it applies to things that arent even groups of terms
    maybe TermMaker? 
    theres really 2 things going on - grouping of fields and making of terms - and
    character template which is a grouping of fields but it makes truncated char lists
    not terms - but char temp is not a group adapteri but maybe should be? 
    - hmmmm - i think grouping of fields needs to be separated from making of terms
    and making of characters - i think group adapter should be renamed TermMaker
    as thats what it is! rename ProformaAlleleOntMaker? */


public class ProformaAlleleParser extends OntologyMaker { //extends AbstractGroupAdapter {

  private static final Logger LOG = Logger.getLogger(ProformaAlleleParser.class);

  public ProformaAlleleParser() {
    addDestinationField("LA1"); // testing, eventually from cfg
    addDestinationField("LA2");
    addDestinationField("NLA");
    addDestinationField("ACC");
  }

  public boolean useButtonToLaunch() { return true; }
  public String getButtonText() { return "Get alleles from Proforma"; }

//   /** If a group reacts to char changes(edits) then a char change listener should be 
//       added, for instance a user edits group fields and dest field is automatically 
//       repopulated, otherwise no-op */
//   public boolean hasCharChangeListener() { return false; }
//   public CharChangeListener getCharChangeListener() { return null; }
  
//   /** similar to char change, for doing something on loading data into group, like
//       repopulating dest field, no-op if dont need this */
//   public boolean hasCharListChangeListener() { return false; }
//   public CharListChangeListener getCharListChangeListener() { return null; }

  // OntologyChangeListener or TermListChangeListener

  /** the destination field that this group is populating (with obo classes), if
      a group is not populating a destination field this would be no-oped
      one could imagine other destinations like main datamodel
  change to take List<String>! */
  //public void setDestinationField(String field) {} // CharField? Ex?

  /** a user has requested to make a new ontology from proforma
      bring up file chooser for them to select proforma file,
      and then parse it and populate field with ontology from parse */
  public void makeOntology() {
    //File file = new File("/home/mgibson/p/data/fly/ds868.proforma"); // testing
    File file = getFileFromUser();
    if (file == null) return; // no file selected - cancel
    try { parseProforma(file); }
    catch (Exception e) {
      LOG.error("Proforma file parsing failed "+e);
    }
  }

  /** Returns null if user cancels - ex? */
  private File getFileFromUser() {
    JFileChooser fileChooser = new JFileChooser();
    int returnValue = fileChooser.showOpenDialog(null);
    if (returnValue == JFileChooser.APPROVE_OPTION) {
      return fileChooser.getSelectedFile();
    } else {
      return null;
    }
  }

//  protected String makeNameFromChar(phenote.datamodel.CharacterI c) {return null;}

  /** flag to switch between making instances or making classes - should 
      ultimately make instances - but phenote doesnt yet handle instances
      just classes */
  private boolean isInstances() {
    return false; // true; flip flop flip flop
  }

  /** parse proforma file for alleles and such - make OBOClasses, add to OBOSession,
   set obosession for desitnation fields */
  private void parseProforma(File file) throws FileNotFoundException, IOException {
    OBOSession ses = new OBOSessionImpl(); // maybe add to main obo sess?
    Pattern allelePat = Pattern.compile("^\\! GA1a\\..*\\*A \\:(.*)");
    Pattern abPat = Pattern.compile("^\\! A1a\\..*\\*a \\:(.*)");
    LineNumberReader l = new LineNumberReader(new FileReader(file));
    for (String line = l.readLine(); line != null; line = l.readLine()) {
      // ALLELES - if pattern matches, makes allele oboClasses & adds to ses
      matchAndAddToSession(line,allelePat,ses);
      
      // ABBERATIONS
      matchAndAddToSession(line,abPat,ses);
    }
    //getDestinationOntology().setOboSession(ses);
    setOboSession(ses,isInstances());
  }

  /** check if pattern matches - and if so get groups, split with " # "
      make obo classes, and add them to obo session, if doesnt match then
      nothing added - used for allele & abberation patterns */
  private void matchAndAddToSession(String line, Pattern p, OBOSession os) {
    Matcher m = p.matcher(line);
    if (m.matches()) {
      LOG.debug("Got a match "+line+" allele "+m.group(1));
      // get allele
      String alleles = m.group(1);
      Pattern delim = Pattern.compile(" # ");
      String[] alleleArray = delim.split(alleles);
      for (String allele : alleleArray) {
        // INSTANCE 
        if (isInstances()) {
          Instance i = makeAlleleInstance(allele);
          os.addObject(i);
        }
        // CLASS  make obo class from allele
        else { 
          OBOClass c = makeAlleleTerm(allele);
          // add to obo session
          os.addObject(c);
        }
        
      }
    }
  }

  // ???
  private static final Namespace ALLELE_NAMESPACE = new Namespace("Allele");

  private OBOClass makeAlleleTerm(String allele) {
    OBOClass oc = new OBOClassImpl(allele,"FBAlelle:"+allele);
    oc.setNamespace(ALLELE_NAMESPACE);
    return oc;
  }

  /** really they should be instances - not used yet */
  private Instance makeAlleleInstance(String allele) {
    Instance i = new InstanceImpl("FBAllele:"+allele,getAlleleClass());
    i.setName(allele);
    return i;
  }


  // this should really come from SO - for now just mimicing this
  // not used yet - needed for instances
  private OBOClass getAlleleClass() {
    OBOClass oc = new OBOClassImpl("allele","SO:0000704");
    oc.setNamespace(ALLELE_NAMESPACE); // ??
    return oc;
  }

  //private class ParseEx extends E

}
