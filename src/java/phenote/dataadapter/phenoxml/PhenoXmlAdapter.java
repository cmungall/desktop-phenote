package phenote.dataadapter.phenoxml;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.bioontologies.obd.schema.pheno.PhenosetDocument;
import org.bioontologies.obd.schema.pheno.BearerDocument.Bearer;
import org.bioontologies.obd.schema.pheno.GenotypeDocument.Genotype;
import org.bioontologies.obd.schema.pheno.ManifestInDocument.ManifestIn;
import org.bioontologies.obd.schema.pheno.MeasurementDocument.Measurement;
import org.bioontologies.obd.schema.pheno.PhenosetDocument.Phenoset;
import org.bioontologies.obd.schema.pheno.PhenotypeCharacterDocument.PhenotypeCharacter;
import org.bioontologies.obd.schema.pheno.PhenotypeDocument.Phenotype;
import org.bioontologies.obd.schema.pheno.PhenotypeManifestationDocument.PhenotypeManifestation;
import org.bioontologies.obd.schema.pheno.ProvenanceDocument.Provenance;
import org.bioontologies.obd.schema.pheno.QualifierDocument.Qualifier;
import org.bioontologies.obd.schema.pheno.QualityDocument.Quality;
import org.bioontologies.obd.schema.pheno.RelatedEntityDocument.RelatedEntity;
import org.bioontologies.obd.schema.pheno.TyperefDocument.Typeref;
import org.obo.datamodel.Link;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;

import phenote.dataadapter.AbstractFileAdapter;
import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterIFactory;
import phenote.datamodel.CharacterList;
import phenote.datamodel.CharacterListI;
import phenote.datamodel.OboUtil;
import phenote.datamodel.PhenotypeCharacterWrapper;
import phenote.datamodel.TermNotFoundException;

public class PhenoXmlAdapter extends AbstractFileAdapter {

  private Set<String> genotypesAlreadyAdded = new HashSet<String>(); 
  private static String[] extensions = {"pxml", "xml"};
  private static final String description =  "PhenoXML [.pxml, .xml]";

  public PhenoXmlAdapter() { super(extensions,description); }
  
  public CharacterListI load(File f) {
    CharacterListI charList = new CharacterList(); // we will return this empty one if we fail reading the xml
    try {
      PhenosetDocument doc = PhenosetDocument.Factory.parse(f);
      charList = newCharacterListFromPhenosetDocument(doc);
    }
    catch (XmlException e) {
      log().error("Failed to load file as phenoxml ", e);
    }
    catch (IOException e) {
      log().error("PhenoXml read failure ", e);
    }
    return charList;
  }

  public void load() {
    if (file == null) {
      log().error("No file specified");
      return;
    }
    CharacterListManager.inst().setCharacterList(this, this.load(file));
    file = null; // null it for next load/commit
  }
  
  private CharacterListI newCharacterListFromPhenosetDocument(PhenosetDocument doc) {
    final CharacterListI charList = new CharacterList();
    for (Phenotype aPhenotype : doc.getPhenoset().getPhenotypeList()) {
      final List<CharacterI> newCharacters = this.createCharactersFromPhenotype(aPhenotype);
      for (CharacterI character : newCharacters) { charList.add(character); }
    }
    for (PhenotypeManifestation aManifestation : doc.getPhenoset().getPhenotypeManifestationList()) {
      final List<CharacterI> newCharacters = this.createCharactersFromPhenotypeManifestation(aManifestation);
      for (CharacterI character : newCharacters) { charList.add(character); }
    }
    return charList;
  }
  
  private List<CharacterI> createCharactersFromPhenotypeManifestation(PhenotypeManifestation pm) {
    final CharacterI template = CharacterIFactory.makeChar();
    final PhenotypeCharacterWrapper phenoTemplate = new PhenotypeCharacterWrapper(template);
    if ((pm.getManifestIn() != null) && (pm.getManifestIn().getGenotype() != null)) {
      phenoTemplate.setGenotype(pm.getManifestIn().getGenotype());
      if (!pm.getManifestIn().getTyperefList().isEmpty()) {
        // only load the first typeref
        try {
          phenoTemplate.setGeneticContext(this.getTermForTyperef(pm.getManifestIn().getTyperefList().get(0)));
        }
        catch (TermNotFoundException e) {
          log().error("Genetic context term not found ", e);
        }
      }
    }
    if (!pm.getProvenanceList().isEmpty()) {
      // only load the first provenance for now
      if (pm.getProvenanceList().get(0).getId() != null) {
        phenoTemplate.setPub(pm.getProvenanceList().get(0).getId());
      }
    }
    if (pm.getPhenotype() != null) {
      final List<CharacterI> phenotypes = this.createCharactersFromPhenotype(pm.getPhenotype(), template);
      if (!phenotypes.isEmpty()) return phenotypes;
    }
    return Collections.singletonList(template); // only if we didn't return any phenotypes for this genotype
  }
  
  private List<CharacterI> createCharactersFromPhenotype(Phenotype phenotype) {
    return this.createCharactersFromPhenotype(phenotype, null);
  }
  
  private List<CharacterI> createCharactersFromPhenotype(Phenotype phenotype, CharacterI template) {
    final List<CharacterI> characters = new ArrayList<CharacterI>();
    for (PhenotypeCharacter phenotypeCharacter : phenotype.getPhenotypeCharacterList()) {
      final CharacterI character = (template == null) ? CharacterIFactory.makeChar() : template.cloneCharacter();
      final PhenotypeCharacterWrapper phenoCharacter = new PhenotypeCharacterWrapper(character);
      try {
        if ((phenotypeCharacter.getBearer() != null) && (phenotypeCharacter.getBearer().getTyperef() != null)) {
          phenoCharacter.setEntity(this.getTermForTyperef(phenotypeCharacter.getBearer().getTyperef()));
        }
      } catch (TermNotFoundException e) {
        log().error("Entity term not found ", e);
      }
      try {
        if (!phenotypeCharacter.getQualityList().isEmpty()) {
          // we only load the first quality for now
          final Quality quality = phenotypeCharacter.getQualityList().get(0);
          if (quality.getTyperef() != null) {
            phenoCharacter.setQuality(this.getTermForTyperef(quality.getTyperef()));
          }
          if (quality.getCount() != null) {
            phenoCharacter.setCount(quality.getCount().intValue());
          }
        }
      } catch (TermNotFoundException e) {
        log().error("Quality term not found ", e);
      }
      characters.add(character);
    }
    return characters;
  }

  public void commit(CharacterListI charList) {
    if (file == null) {
      log().error("No file specified");
      return;
    }
    this.commit(charList, file);
    file = null;
  }
  
  public void commit(CharacterListI charList, File f) {
    final PhenosetDocument doc = PhenosetDocument.Factory.newInstance();
    final Phenoset phenoset = doc.addNewPhenoset();
    for (CharacterI chr : charList.getList()) {
      // builds Phenoset from characters
      this.addCharAndGenotypeToPhenoset(chr, phenoset);
    }
    try {
      doc.save(f, this.getXmlOptions());
    } catch (IOException e) {
      log().error("Failed to save ", e);
    }
  }

  private XmlOptions getXmlOptions() {
    final XmlOptions options = new XmlOptions();
    options.setSavePrettyPrint();
    options.setSavePrettyPrintIndent(2);
    return options;
  }

  private void addCharAndGenotypeToPhenoset(CharacterI chr, Phenoset phenoset) {
    final PhenotypeCharacterWrapper phenoCharacter = new PhenotypeCharacterWrapper(chr);
    PhenotypeManifestation pm = phenoset.addNewPhenotypeManifestation();
    addGenotypeAndContext(chr, phenoset, pm);
    addPhenotype(chr, pm);
    final Provenance provenance = this.getProvenance(phenoCharacter);
    if (provenance != null) { pm.setProvenanceArray(new Provenance[] {provenance}); }
  }
  
  private void addGenotypeAndContext(CharacterI chr, Phenoset ps, PhenotypeManifestation pm) {
    final PhenotypeCharacterWrapper character = new PhenotypeCharacterWrapper(chr);
    final String genotype = character.getGenotype();
    final OBOClass geneticContext = character.getGeneticContext();
    if ((genotype != null) || (geneticContext != null)) {
      final ManifestIn mi = pm.addNewManifestIn();
      if (genotype != null) {
        addGenotypeToPhenoset(genotype, ps);
        mi.setGenotype(genotype);
      }
      if (character.getGeneticContext() != null) {
        final Typeref typeref = this.getTyperefForTerm(character.getGeneticContext());
        mi.setTyperefArray(new Typeref[] {typeref});
      }
    }
  }
  
  private void addGenotypeToPhenoset(String genotype, Phenoset ps) {
    // check if we've already added this genotype, if so don't need to add again
    if (!genotypesAlreadyAdded.contains(genotype)) {
      Genotype gt = ps.addNewGenotype();
      gt.setName(genotype);
      genotypesAlreadyAdded.add(genotype);
    }
  }
  
  private void addPhenotype(CharacterI chr, PhenotypeManifestation pm) {
    PhenotypeCharacterWrapper character = new PhenotypeCharacterWrapper(chr);
    // don't make a phenotype if no entity or quality
    if ((character.getEntity() == null) && (character.getQuality() == null)) return;
    final Phenotype p = pm.addNewPhenotype();
    final PhenotypeCharacter pc = p.addNewPhenotypeCharacter();
    if (character.getEntity() != null) {
      final Bearer b = pc.addNewBearer();
      b.setTyperef(this.getTyperefForTerm(character.getEntity()));
    } else {
      log().warn("Character " + chr + " has no entity");
    }
    if (character.getQuality() != null) {
      final Quality q = pc.addNewQuality();
      q.setTyperef(this.getTyperefForTerm(character.getQuality()));
      if (character.getAdditionalEntity() != null) {
        final RelatedEntity relatedEntity = q.addNewRelatedEntity();
        relatedEntity.setTyperef(this.getTyperefForTerm(character.getAdditionalEntity()));
      }
      if (character.hasCount()) {
        q.setCount(BigInteger.valueOf(character.getCount()));
      }
      if (character.hasMeasurement()) {
        final Measurement measurement = q.addNewMeasurement();
        measurement.setValue(character.getMeasurement());
        if (character.getUnit() != null) {
          measurement.setUnit(character.getUnit().getName());
        }
      }
    } else {
      log().warn("Character " + chr + " has no quality");
    }    
  }
  
  private PhenotypeCharacter getPhenotypeCharacter(PhenotypeCharacterWrapper phenoCharacter) {
    if ((phenoCharacter.getEntity() == null) && (phenoCharacter.getQuality() == null)) {
      return null;
    }
    final PhenotypeCharacter phenotypeCharacter = PhenotypeCharacter.Factory.newInstance();
    if (phenoCharacter.getEntity() != null) {
      final Bearer bearer = phenotypeCharacter.addNewBearer();
      bearer.setTyperef(this.getTyperefForTerm(phenoCharacter.getEntity()));
    }
    if (phenoCharacter.getQuality() != null) {
      final Quality quality = phenotypeCharacter.addNewQuality();
      quality.setTyperef(this.getTyperefForTerm(phenoCharacter.getQuality()));
      if (phenoCharacter.getAdditionalEntity() != null) {
        final RelatedEntity relatedEntity = quality.addNewRelatedEntity();
        relatedEntity.setTyperef(this.getTyperefForTerm(phenoCharacter.getAdditionalEntity()));
      }
      if (phenoCharacter.hasCount()) {
        quality.setCount(BigInteger.valueOf(phenoCharacter.getCount()));
      }
      if (phenoCharacter.hasMeasurement()) {
        final Measurement measurement = quality.addNewMeasurement();
        measurement.setValue(phenoCharacter.getMeasurement());
        if (phenoCharacter.getUnit() != null) {
          measurement.setUnit(phenoCharacter.getUnit().getName());
        }
      }
    }
    return phenotypeCharacter;
  }
  
  private Provenance getProvenance(PhenotypeCharacterWrapper phenoCharacter) {
    if (phenoCharacter.getPub() == null) return null;
    final Provenance provenance = Provenance.Factory.newInstance();
    provenance.setId(phenoCharacter.getPub());
    return provenance;
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
