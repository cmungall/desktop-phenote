package phenote.dataadapter.phenoxml;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFileChooser;

import org.apache.xmlbeans.XmlOptions;

import org.bioontologies.obd.schema.pheno.BearerDocument.Bearer;
import org.bioontologies.obd.schema.pheno.GenotypeDocument.Genotype;
import org.bioontologies.obd.schema.pheno.ManifestInDocument.ManifestIn;
import org.bioontologies.obd.schema.pheno.PhenotypeDocument.Phenotype;
import org.bioontologies.obd.schema.pheno.PhenotypeCharacterDocument.PhenotypeCharacter;
import org.bioontologies.obd.schema.pheno.PhenosetDocument.Phenoset;
import org.bioontologies.obd.schema.pheno.QualityDocument.Quality;
import org.bioontologies.obd.schema.pheno.PhenosetDocument;
import org.bioontologies.obd.schema.pheno.PhenotypeManifestationDocument.PhenotypeManifestation;
import org.bioontologies.obd.schema.pheno.TyperefDocument.Typeref;
//import org.bioontologies.obd.schema.pheno.*.*;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.dataadapter.DataAdapterI;

public class PhenoXmlAdapter implements DataAdapterI {

  private Set genotypesAlreadyAdded = new HashSet<String>(); 

  public void load() {}

  public void commit(CharacterListI charList) {

    File file = getFile();
    if (file == null)
      return;

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

  public static File getFile() {
    // todo - remember last accessed dir
    JFileChooser fileChooser = new JFileChooser();
    // todo - file filter - only .xml or .phenoxml?
    int returnVal = fileChooser.showOpenDialog(null);
    if(returnVal == JFileChooser.APPROVE_OPTION)
      return fileChooser.getSelectedFile();
    else {
      System.out.println("no file chosen");
      return null;
    }
  }

  private XmlOptions getXmlOptions() {
    XmlOptions options = new XmlOptions();
    options.setSavePrettyPrint();
    options.setSavePrettyPrintIndent(2);
    return options;
  }


  private void addCharAndGenotypeToPhenoset(CharacterI chr, Phenoset phenoset) {
    String genotype = chr.getGenotype();
    PhenotypeManifestation pm = phenoset.addNewPhenotypeManifestation();
    addGenotype(genotype,phenoset,pm);
    addPhenotype(chr,pm);
  }
  
  private void addGenotype(String genotype,Phenoset ps,PhenotypeManifestation pm) {
    // check if we've already added this genotype, if so dont need to add again
    if (!genotypesAlreadyAdded.contains(genotype)) {
      Genotype gt = ps.addNewGenotype();
      gt.setName(genotype);
      genotypesAlreadyAdded.add(genotype);
    }
    ManifestIn mi = pm.addNewManifestIn();
    mi.setGenotype(genotype);
  }

  private void addPhenotype(CharacterI chr, PhenotypeManifestation pm) {
    Phenotype p = pm.addNewPhenotype();
    PhenotypeCharacter pc = p.addNewPhenotypeCharacter();

    // should entity-less phenotypes even be saved?
    if (chr.getEntity() != null) {
      Bearer b = pc.addNewBearer();
      Typeref tr = b.addNewTyperef();
      tr.setAbout(chr.getEntity().getID());
    }
    else {
      System.out.println("Character "+chr+" has no entity");
    }

    if (chr.getQuality() != null) {
      Quality q = pc.addNewQuality();
      Typeref trq = q.addNewTyperef();
      trq.setAbout(chr.getQuality().getID());
    }
    else {
      System.out.println("Character "+chr+" has no quality");
    }
  }
}
