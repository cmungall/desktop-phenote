package phenote.dataadapter.phenoxml;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.bioontologies.obd.schema.pheno.PhenosetDocument;
import org.bioontologies.obd.schema.pheno.BearerDocument.Bearer;
import org.bioontologies.obd.schema.pheno.GenotypeDocument.Genotype;
import org.bioontologies.obd.schema.pheno.ManifestInDocument.ManifestIn;
import org.bioontologies.obd.schema.pheno.PhenosetDocument.Phenoset;
import org.bioontologies.obd.schema.pheno.PhenotypeCharacterDocument.PhenotypeCharacter;
import org.bioontologies.obd.schema.pheno.PhenotypeDocument.Phenotype;
import org.bioontologies.obd.schema.pheno.PhenotypeManifestationDocument.PhenotypeManifestation;
import org.bioontologies.obd.schema.pheno.ProvenanceDocument.Provenance;
import org.bioontologies.obd.schema.pheno.QualifierDocument.Qualifier;
import org.bioontologies.obd.schema.pheno.QualityDocument.Quality;
import org.bioontologies.obd.schema.pheno.TyperefDocument.Typeref;
import org.obo.datamodel.Link;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;

import phenote.dataadapter.AbstractFileAdapter;
import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterIFactory;
import phenote.datamodel.CharacterList;
import phenote.datamodel.CharacterListI;
import phenote.datamodel.OboUtil;
import phenote.datamodel.TermNotFoundException;

public class PhenoXmlAdapter extends AbstractFileAdapter {

  private Set<String> genotypesAlreadyAdded = new HashSet<String>(); 
  private File previousFile;
  private static String[] extensions = {"pxml", "xml"};
  private static final String description =  "PhenoXML [.pxml, .xml]";

  public PhenoXmlAdapter() { super(extensions,description); }
  
  public CharacterListI load(File f) {
    // this method temporarily duplicates code from load(), which will soon be removed
    CharacterListI charList = new CharacterList();  // we will return this empty one if we fail reading the xml
    try {
      PhenosetDocument doc = PhenosetDocument.Factory.parse(f);
      charList = newCharacterListFromPhenosetDocument(doc);
    }
    catch (XmlException e) {
      System.out.println("Failed to load file as phenoxml " + e);
    }
    catch (IOException e) {
      System.out.println("PhenoXml read failure " + e);
    }
    return charList;
  }

  public void load() {
	  try {
		  if (file == null) {
			  file = getFileFromUserForOpen(previousFile);
		  }
		  if (file == null) return;
		  previousFile = file;
		  PhenosetDocument doc = PhenosetDocument.Factory.parse(file);
		  CharacterListI charList = newCharacterListFromPhenosetDocument(doc);
		  CharacterListManager.inst().setCharacterList(this,charList);
	  }
	  catch (XmlException e) {
		  System.out.println("Failed to load file as phenoxml " + e);
	  }
	  catch (IOException e) {
		  System.out.println("PhenoXml read failure " + e);
	  }
	  file = null; // null it for next load/commit
  }
  
  private CharacterListI newCharacterListFromPhenosetDocument(PhenosetDocument doc) {
    Phenoset phenoset = doc.getPhenoset();
    List<PhenotypeManifestation> phenotypeManifestations = phenoset.getPhenotypeManifestationList();
    CharacterListI charList = new CharacterList();
    for (PhenotypeManifestation aManifestation : phenotypeManifestations) {
      CharacterI newCharacter = newCharacterFromPhenotypeManifestation(aManifestation);
      charList.add(newCharacter); 
    }
    return charList;
  }
  
  private CharacterI newCharacterFromPhenotypeManifestation(PhenotypeManifestation pm) {
    CharacterI character = CharacterIFactory.makeChar();
    ManifestIn mi = pm.getManifestIn();
    if (mi != null) {
      String genotype = mi.getGenotype();
      if (genotype != null) {
        character.setGenotype(genotype);
      }
      List<Typeref> typerefList = mi.getTyperefList();
      if ((typerefList != null) && (typerefList.size() > 0)) {
        // only load the first typeref
        Typeref typeref = typerefList.get(0);
        try {
          character.setGeneticContext(this.getTermForTyperef(typeref));
        }
        catch (TermNotFoundException e) {
          log().error("Genetic context term not found ", e);
        }
      }
    }
    Phenotype phenotype = pm.getPhenotype();
    PhenotypeCharacter phenotypeCharacter = null;
    if (phenotype != null) {
      List<PhenotypeCharacter> phenotypeCharacters = phenotype.getPhenotypeCharacterList();
      if ((phenotypeCharacters != null) && (phenotypeCharacters.size() > 0)) {
        // we only load the first character in the phenotype for now
        phenotypeCharacter = phenotypeCharacters.get(0);
      }
    }   
    try {
      character.setEntity(this.getTermForTyperef(phenotypeCharacter.getBearer().getTyperef()));
    } catch (TermNotFoundException e) {
      System.out.println("Entity term not found " + e);
    }
    try {
      List<Quality> qualityList = phenotypeCharacter.getQualityList();
      if ((qualityList != null) && (qualityList.size() > 0)) {
        // we only load the first quality for now
        Quality quality = qualityList.get(0);
        character.setQuality(this.getTermForTyperef(quality.getTyperef()));
      }
    } catch (TermNotFoundException e) {
      System.out.println("Quality term not found " + e);
    }
    List<Provenance> provenanceList = pm.getProvenanceList();
    if ((provenanceList != null) && (provenanceList.size() > 0)) {
      // only load the first provenance for now
      Provenance provenance = provenanceList.get(0);
      String id = provenance.getId();
      if (id != null) {
        character.setPub(id);
      }
    }
    return character;  
  }

  public void commit(CharacterListI charList) {
    if (file == null)
      file = getFileFromUserForSave(previousFile);
    if (file == null)
      return;
    previousFile = file;

    PhenosetDocument doc = PhenosetDocument.Factory.newInstance();
    Phenoset phenoset = doc.addNewPhenoset();
    
    for (CharacterI chr : charList.getList()) {
      // builds Phenoset from characters
      addCharAndGenotypeToPhenoset(chr,phenoset);
    }

    System.out.println("doc schme type "+doc.schemaType()+" name "+doc.schemaType().getName());

    try {
      doc.save(file,getXmlOptions());
      System.out.println("Wrote file "+file);
    }
    catch (IOException e) {
      System.out.println("Failed to save "+e);
    }
  }
  
  public void commit(CharacterListI charList, File f) {
    file = f;
    commit(charList);
  }

  public static File getFileFromUserIsSave(File dir, boolean isSave) {
    // todo - remember last accessed dir
    JFileChooser fileChooser = new JFileChooser(dir);
    
    //String[] dataAdapterNames = {"PhenoSyntax", "PhenoXML"};
    //JComboBox comboBox = new JComboBox(dataAdapterNames);
    //fileChooser.setAccessory(comboBox);
    //FileFilter phenoXMLFilter = new FileNameExtensionFilter("PhenoXML file", "pxml");
    //FileFilter phenoSyntaxFilter = new FileNameExtensionFilter("PhenoSyntax file", "psx", "syn");
    //fileChooser.addChoosableFileFilter(phenoXMLFilter);
    //fileChooser.addChoosableFileFilter(phenoSyntaxFilter);
    
    
    // todo - file filter - only .xml or .phenoxml?
    int returnVal;
    if (isSave) {
      returnVal = fileChooser.showSaveDialog(null);
    } else {
      returnVal = fileChooser.showOpenDialog(null);
    }
    if (returnVal == JFileChooser.APPROVE_OPTION)
      return fileChooser.getSelectedFile();
    else {
      System.out.println("no file chosen");
      return null;
    }
  }
  
  public static File getFileFromUserForSave(File dir) {
    return getFileFromUserIsSave(dir, true);
  }
  
  public static File getFileFromUserForOpen(File dir) {
    return getFileFromUserIsSave(dir, false);
  }

  private XmlOptions getXmlOptions() {
    XmlOptions options = new XmlOptions();
    options.setSavePrettyPrint();
    options.setSavePrettyPrintIndent(2);
    // cant get rid of ns: ??
//     java.util.HashMap m = new java.util.HashMap(2);
//     m.put("ns:","");
//     options.setSaveImplicitNamespaces(m);
//     options.setSaveAggressiveNamespaces();
//     options.setUseDefaultNamespace();
    return options;
  }


  private void addCharAndGenotypeToPhenoset(CharacterI chr, Phenoset phenoset) {
    PhenotypeManifestation pm = phenoset.addNewPhenotypeManifestation();
    addGenotypeAndContext(chr, phenoset, pm);
    addPhenotype(chr,pm);
    addPub(chr, pm);
  }
  
  private void addGenotypeAndContext(CharacterI chr, Phenoset ps, PhenotypeManifestation pm) {
    ManifestIn mi = pm.addNewManifestIn(); // only add if have gt or gc?
    try {
      //chr.getGenotype(); unfortunate new way due to new generic datamodel
      String genotype = chr.getValueString("Genotype"); // CFEx
      // check if its there??
      addGenotypeToPhenoset(genotype, ps);
      mi.setGenotype(genotype);
    }
    catch (CharFieldException e) {} // do nothing just skip it (??)

    String gc = CharFieldEnum.GENETIC_CONTEXT.toString();
    if (chr.hasValue(gc)) { // hasGeneticCont
      try {
        final Typeref typeref = this.getTyperefForTerm(chr.getTerm(gc));
        mi.setTyperefArray(new Typeref[] {typeref});
      } catch (CharFieldException e) {
        log().error("failed to get gc field");  // shouldnt happen with hasValue
      }
    }
  }
  
  private void addGenotypeToPhenoset(String genotype, Phenoset ps) {
    // check if we've already added this genotype, if so dont need to add again
    if (!genotypesAlreadyAdded.contains(genotype)) {
      Genotype gt = ps.addNewGenotype();
      gt.setName(genotype);
      genotypesAlreadyAdded.add(genotype);
    }
  }
  
  private void addPhenotype(CharacterI chr, PhenotypeManifestation pm) {
    Phenotype p = pm.addNewPhenotype();
    PhenotypeCharacter pc = p.addNewPhenotypeCharacter();

    // should entity-less phenotypes even be saved?
    if (chr.getEntity() != null) {
      Bearer b = pc.addNewBearer();
      b.setTyperef(this.getTyperefForTerm(chr.getEntity()));
    }
    else {
      log().warn("Character "+chr+" has no entity");
    }

    if (chr.getQuality() != null) {
      Quality q = pc.addNewQuality();
      q.setTyperef(this.getTyperefForTerm(chr.getQuality()));
    }
    else {
      log().warn("Character "+chr+" has no quality");
    }
  }
  
  private void addPub(CharacterI chr, PhenotypeManifestation pm) {
    if (!chr.hasPub()) return;  // no pub, early return
    Provenance provenance = pm.addNewProvenance();
    provenance.setId(chr.getPub());
  }
  
  private Typeref getTyperefForTerm(OBOClass term) {
    final Typeref tr = Typeref.Factory.newInstance();
    if (OboUtil.isPostCompTerm(term)) {
      tr.setAbout(OboUtil.getGenusTerm(term).getID());
      for (Link link : OboUtil.getAllDifferentia(term)) {
        final LinkedObject parent = link.getParent();
        if (!(parent instanceof OBOClass)) continue;
        final OBOClass differentia = (OBOClass)parent;
        final Qualifier qualifier = tr.addNewQualifier();
        qualifier.setRelation(link.getType().getID());
        qualifier.addNewHoldsInRelationTo().setTyperef(this.getTyperefForTerm(differentia));
      }
    } else {
      tr.setAbout(term.getID());
    }
    return tr;
  }
  
  private OBOClass getTermForTyperef(Typeref tr) throws TermNotFoundException {
    final OBOClass genus = this.getTerm(tr.getAbout());
    if (tr.sizeOfQualifierArray() > 0) {
      // need to create post-comp
      final OboUtil postCompUtil = OboUtil.initPostCompTerm(genus);
      for (Qualifier qualifier : tr.getQualifierList()) {
        final OBOProperty relation = this.getRelation(qualifier.getRelation());
        final OBOClass differentia = this.getTermForTyperef(qualifier.getHoldsInRelationTo().getTyperef());
        postCompUtil.addRelDiff(relation, differentia);
      }
      return postCompUtil.getPostCompTerm();
    } else {
      return genus;
    }
  }
  
  private OBOClass getTerm(String id) throws TermNotFoundException {
    return CharFieldManager.inst().getOboClass(id);
  }
  
  private OBOProperty getRelation(String id) throws TermNotFoundException {
    final OBOProperty relation = CharFieldManager.inst().getRelation(id);
    if (relation != null) {
      return relation;
    } else {
      throw new TermNotFoundException(id);
    }
  }
  
  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }

}
