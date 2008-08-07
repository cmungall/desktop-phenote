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
import org.bioontologies.obd.schema.pheno.DescriptionDocument.Description;
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
import org.bioontologies.obd.schema.pheno.UnitDocument.Unit;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.Link;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBOSession;

import phenote.dataadapter.AbstractFileAdapter;
import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterIFactory;
import phenote.datamodel.CharacterList;
import phenote.datamodel.CharacterListI;
import phenote.datamodel.OboUtil;
import phenote.datamodel.PhenotypeCharacterI;
import phenote.datamodel.PhenotypeCharacterWrapper;
import phenote.datamodel.TermNotFoundException;
import phenote.datamodel.PhenotypeCharacterI.PhenotypeCharacterFactory;

public class PhenoXmlAdapter extends AbstractFileAdapter {

  private Set<String> genotypesAlreadyAdded = new HashSet<String>(); 
  private static String[] extensions = {"pxml", "xml"};
  private static final String description =  "PhenoXML [.pxml, .xml]";
  private final OBOSession session;

  public PhenoXmlAdapter(OBOSession session) {
    super(extensions,description);
    this.session = session;
  }
  
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
        final Typeref typeref = getTyperefForTerm(character.getGeneticContext());
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
    PhenotypeCharacterI character = new PhenotypeCharacterWrapper(chr);
    final PhenotypeCharacter pc = createPhenotypeCharacter(character);
    // don't make a phenotype if no entity or quality
    if (pc == null) return;
    final Phenotype p = pm.addNewPhenotype();
    p.setPhenotypeCharacterArray(new PhenotypeCharacter[] {pc});
  }
  
  public static PhenotypeCharacter createPhenotypeCharacter(PhenotypeCharacterI phenoCharacter) {
    if ((phenoCharacter.getEntity() == null) && (phenoCharacter.getQuality() == null)) {
      return null;
    }
    final PhenotypeCharacter phenotypeCharacter = PhenotypeCharacter.Factory.newInstance();
    if (phenoCharacter.getEntity() != null) {
      final Bearer bearer = phenotypeCharacter.addNewBearer();
      bearer.setTyperef(getTyperefForTerm(phenoCharacter.getEntity()));
    }
    if (phenoCharacter.getQuality() != null) {
      final Quality quality = phenotypeCharacter.addNewQuality();
      quality.setTyperef(getTyperefForTerm(phenoCharacter.getQuality()));
      if (phenoCharacter.getAdditionalEntity() != null) {
        final RelatedEntity relatedEntity = quality.addNewRelatedEntity();
        relatedEntity.setTyperef(getTyperefForTerm(phenoCharacter.getAdditionalEntity()));
      }
      if (phenoCharacter.hasCount()) {
        quality.setCount(BigInteger.valueOf(phenoCharacter.getCount()));
      }
      if (phenoCharacter.hasMeasurement()) {
        final Measurement measurement = quality.addNewMeasurement();
        measurement.setValue(phenoCharacter.getMeasurement());
        if (phenoCharacter.getUnit() != null) {
          final Unit unit = measurement.addNewUnit();
          unit.setTyperef(getTyperefForTerm(phenoCharacter.getUnit()));
        }
      }
    }
    if (phenoCharacter.getDescription() != null) {
      final Description description = phenotypeCharacter.addNewDescription();
      description.setStringValue(phenoCharacter.getDescription());
    }
    return phenotypeCharacter;
  }
  
  public static Phenotype createPhenotype(List<PhenotypeCharacter> phenotypeCharacters) {
    final Phenotype phenotype = Phenotype.Factory.newInstance();
    phenotype.setPhenotypeCharacterArray(phenotypeCharacters.toArray(new PhenotypeCharacter[] {}));
    return phenotype;
  }
  
  public List<PhenotypeCharacterI> parsePhenotype(Phenotype phenotype, PhenotypeCharacterFactory factory) {
    final List<PhenotypeCharacterI> characters = new ArrayList<PhenotypeCharacterI>();
    for (PhenotypeCharacter pc : phenotype.getPhenotypeCharacterList()) {
      characters.add(parsePhenotypeCharacter(pc, factory));
    }
    return characters;
  }
  
  public PhenotypeCharacterI parsePhenotypeCharacter(PhenotypeCharacter pc, PhenotypeCharacterFactory factory) {
      final PhenotypeCharacterI newCharacter = factory.newPhenotypeCharacter();
      try {
        if ((pc.getBearer() != null) && (pc.getBearer().getTyperef() != null)) {
          newCharacter.setEntity(this.getTermForTyperef(pc.getBearer().getTyperef()));
        }
      } catch (TermNotFoundException e) {
        log().error("Entity term not found ", e);
      }
      try {
        if (!pc.getQualityList().isEmpty()) {
          // we only load the first quality for now
          final Quality quality = pc.getQualityList().get(0);
          if (quality.getTyperef() != null) {
            newCharacter.setQuality(this.getTermForTyperef(quality.getTyperef()));
          }
          if (!quality.getRelatedEntityList().isEmpty()) {
            //we only use one related entity for now
            final RelatedEntity e2 = quality.getRelatedEntityList().get(0);
            if (e2.getTyperef() != null) {
              newCharacter.setAdditionalEntity(this.getTermForTyperef(e2.getTyperef()));
            }
          }
          if (quality.getCount() != null) {
            newCharacter.setCount(quality.getCount().intValue());
          }
          if (!quality.getMeasurementList().isEmpty()) {
            // we only use one measurement for now
            final Measurement measurement = quality.getMeasurementList().get(0);
            newCharacter.setMeasurement(measurement.getValue());
            if ((measurement.getUnit() != null) && (measurement.getUnit().getTyperef() != null)) {
              newCharacter.setUnit(this.getTermForTyperef(measurement.getUnit().getTyperef()));
            }
                
          }
        }
      } catch (TermNotFoundException e) {
        log().error("Quality term not found ", e);
      }
      if (pc.getDescription() != null) {
        newCharacter.setDescription(pc.getDescription().getStringValue());
      }
      return newCharacter;
  }
  
  private Provenance getProvenance(PhenotypeCharacterWrapper phenoCharacter) {
    if (phenoCharacter.getPub() == null) return null;
    final Provenance provenance = Provenance.Factory.newInstance();
    provenance.setId(phenoCharacter.getPub());
    return provenance;
  }
  
  private static Typeref getTyperefForTerm(OBOClass term) {
    final Typeref tr = Typeref.Factory.newInstance();
    if (OboUtil.isPostCompTerm(term)) {
      tr.setAbout(OboUtil.getGenusTerm(term).getID());
      for (Link link : OboUtil.getAllDifferentia(term)) {
        final LinkedObject parent = link.getParent();
        if (!(parent instanceof OBOClass)) continue;
        final OBOClass differentia = (OBOClass)parent;
        final Qualifier qualifier = tr.addNewQualifier();
        qualifier.setRelation(link.getType().getID());
        qualifier.addNewHoldsInRelationTo().setTyperef(getTyperefForTerm(differentia));
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
    final IdentifiedObject term = this.session.getObject(id);
    if (term instanceof OBOClass) {
      return (OBOClass)term;
    } else {
      throw new TermNotFoundException(id);
    }
  }
  
  private OBOProperty getRelation(String id) throws TermNotFoundException {
    final OBOProperty relation = CharFieldManager.inst().getRelation(id);
    if (relation != null) {
      return relation;
    } else {
      throw new TermNotFoundException(id);
    }
  }
  
  private static Logger log;
  private static Logger log() {
    if (log == null) log = Logger.getLogger(PhenoXmlAdapter.class);
    return log;
  }

}
