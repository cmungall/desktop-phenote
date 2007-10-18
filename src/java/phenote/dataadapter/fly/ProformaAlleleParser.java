package phenote.dataadapter.fly;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;

import org.apache.log4j.Logger;

import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.OBOSession;
import org.geneontology.oboedit.datamodel.impl.OBOClassImpl;
import org.geneontology.oboedit.datamodel.impl.OBOSessionImpl;

import phenote.edit.CharChangeListener;
import phenote.dataadapter.CharListChangeListener;
import phenote.dataadapter.AbstractGroupAdapter;
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
    setDestinationField("LA1"); // testing
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

  /** parse proforma file for alleles and such - make OBOClasses, add to OBOSession,
   set obosession for desitnation fields */
  private void parseProforma(File file) throws FileNotFoundException, IOException {
    OBOSession ses = new OBOSessionImpl(); // maybe add to main obo sess?
    Pattern p = Pattern.compile("^\\! GA1a\\..*\\*A \\:(.*)");
    LineNumberReader l = new LineNumberReader(new FileReader(file));
    for (String line = l.readLine(); line != null; line = l.readLine()) {
      // check if have allele
      Matcher m = p.matcher(line);
      if (m.matches()) {
        LOG.debug("Got a match "+line+" allele "+m.group(1));
        // get allele
        String allele = m.group(1);
        // make obo class from allele
        OBOClass c = makeAlleleTerm(allele);
        // add to obo session
        ses.addObject(c);
      }
    }
    //getDestinationOntology().setOboSession(ses);
    setOboSession(ses);
  }

  private OBOClass makeAlleleTerm(String allele) {
    return new OBOClassImpl(allele,"FBAlelle:"+allele);
  }

  //private class ParseEx extends E

}
