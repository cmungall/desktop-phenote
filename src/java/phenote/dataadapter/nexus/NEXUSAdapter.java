package phenote.dataadapter.nexus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.geneontology.oboedit.datamodel.Link;
import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.TermCategory;

import phenote.dataadapter.AbstractFileAdapter;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.main.PhenoteVersion;

/** Writes Phenote characters to a NEXUS file.
 * Phenote characters are grouped into NEXUS characters via shared "attribute" heritage
 * within PATO.
 * The exporter supports only up to 36 values for any particular attribute, due to
 * various limitations of other widely used NEXUS implementations (e.g. Mesquite & MacClade).
 * There is currently no support for reading of NEXUS files.
*/

public class NEXUSAdapter extends AbstractFileAdapter {
  
  private File file;
  private static final String[] extensions = {"nex", "nxs"};
  private static final String description =  "NEXUS [.nex, .nxs]";
  private static final String TAXON_KEY = "Taxon";
  private static final String ENTITY_KEY = "Entity";
  private static final String VALUE_KEY = "Quality";
  private static String STATE_SYMBOLS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private LinkedHashMap<String, ArrayList<Integer>> genotypes; // ordered map of genotype names and value lists
  private LinkedHashMap<NEXUSCharacter, List<OBOClass>> nexusCharacters; // ordered map of character names and state lists

  public NEXUSAdapter() { super(extensions,description); }

  
  public void commit(CharacterListI charList) {
    if (this.file == null)
      return;
    this.commit(charList, this.file);
  }
  
  public void commit(CharacterListI charList, File f) {
    this.file = f;
    try {
      this.commit(charList, new BufferedWriter(new FileWriter(this.file)));
    } catch (IOException e) {
      log().error("Failed to write NEXUS file", e);
    }
  }
  
  public void commit(CharacterListI charList, Writer writer) {
    this.setGenotypes(new LinkedHashMap<String, ArrayList<Integer>>());
    this.setNexusCharacters(new LinkedHashMap<NEXUSCharacter, List<OBOClass>>());
    this.populateGenotypesAndCharacters(charList);
    this.createDefaultCharacterValues();
    this.populateCharacterValues(charList);
    this.writeNexus(writer);
  }

  public List<String> getExtensions() {
    return Arrays.asList(NEXUSAdapter.extensions);
  }

  public void load() {
    log().error("Cannot read: NEXUS adapter is for import only.");
  }

  public CharacterListI load(File f) {
    this.load();
    return null;
  }

  public void setAdapterValue(String adapterValue) {
    file = new File(adapterValue);
  }
  
  private LinkedHashMap<String, ArrayList<Integer>> getGenotypes() {
    return this.genotypes;
  }
  
  private void setGenotypes(LinkedHashMap<String, ArrayList<Integer>> newGenotypes) {
    this.genotypes = newGenotypes;
  }
  
  private LinkedHashMap<NEXUSCharacter, List<OBOClass>> getNexusCharacters() {
    return this.nexusCharacters;
  }
  
  private void setNexusCharacters(LinkedHashMap<NEXUSCharacter, List<OBOClass>> newNexusCharacters) {
    this.nexusCharacters = newNexusCharacters;
  }
  
  private void populateGenotypesAndCharacters(CharacterListI characterList) {
    for (CharacterI ch : characterList.getList()) {
      if (this.isValidCharacter(ch)) {
        String genotype;
        OBOClass entityTerm;
        OBOClass valueTerm;
        try {
          genotype = ch.getValueString(TAXON_KEY);
          entityTerm = ch.getTerm(ENTITY_KEY);
          valueTerm = ch.getTerm(VALUE_KEY);
        } catch (CharFieldException e) {
          log().error("Could not get terms from character: " + ch, e);
          continue;
        }
        OBOClass attributeTerm = this.getAttributeForValue(valueTerm);
        if (attributeTerm == null) {
          log().error("Failed to find attribute for value: " + valueTerm);
          continue;
        }
        if (!(this.genotypes.containsKey(genotype))) {
          this.genotypes.put(genotype, (new ArrayList<Integer>()));
        }
        NEXUSCharacter newCharacter = new NEXUSCharacter(entityTerm, attributeTerm);
        if (!(this.nexusCharacters.containsKey(newCharacter))) {
          this.nexusCharacters.put(newCharacter, (new ArrayList<OBOClass>()));
        }
        List<OBOClass> states = this.nexusCharacters.get(newCharacter);
        if (!(states.contains(valueTerm))) {
          states.add(valueTerm);
        }
      }
    }
  }
  
  private void createDefaultCharacterValues() {
    for (int i = 0; i < this.nexusCharacters.size(); i++) {
      for (List<Integer> valuesList : this.genotypes.values()) {
        valuesList.add(null); // placeholders
      }
    }
  }
  
  private void populateCharacterValues(CharacterListI characterList) {
    for (CharacterI ch : characterList.getList()) {
      if (this.isValidCharacter(ch)) {
        OBOClass taxonTerm;
        OBOClass entityTerm;
        OBOClass valueTerm;
        try {
          taxonTerm = ch.getTerm(TAXON_KEY);
          entityTerm = ch.getTerm(ENTITY_KEY);
          valueTerm = ch.getTerm(VALUE_KEY);
        } catch (CharFieldException e) {
          log().error("Could not get terms from character: " + ch, e);
          continue;
        }
        OBOClass attributeTerm = this.getAttributeForValue(valueTerm);
        if (attributeTerm == null) {
          log().error("Failed to find attribute for value: " + valueTerm);
          continue;
        }
        NEXUSCharacter currentCharacter = new NEXUSCharacter(entityTerm, attributeTerm);
        List<OBOClass> currentValueList = this.getNexusCharacters().get(currentCharacter);
        int indexOfCharacter = Arrays.asList(this.getNexusCharacters().values().toArray()).indexOf(currentValueList);
        int indexOfValue = currentValueList.indexOf(valueTerm);
        this.getGenotypes().get(taxonTerm.getName()).set(indexOfCharacter, indexOfValue);
      }
    }
  }
  
  private boolean isValidCharacter(CharacterI character) {
    try {
      return ((character.hasValue(TAXON_KEY)) && (character.hasValue(ENTITY_KEY)) && (character.hasValue(VALUE_KEY)));
    } catch (Exception e) {
      return false;
    }
  }
  
  private OBOClass getAttributeForValue(OBOClass valueTerm) {
    Set<TermCategory> categories = valueTerm.getCategories();
    Set<String> categoryNames = new HashSet<String>();
    for (TermCategory category : categories) {
      categoryNames.add(category.getName());
    }
    if (categoryNames.contains("attribute_slim")) {
      return valueTerm;
    } else if ((categoryNames.contains("value_slim"))) {
      return this.getAttributeForValue(this.getParentForTerm(valueTerm));
    }
    return null;
  }
  
  private OBOClass getParentForTerm(OBOClass term) {
    Collection<Link> parents = term.getParents();
    for (Link link : parents) {
      if (link.getType().getName().equals("is_a")) {
        return (OBOClass)(link.getParent());
      }
    }
    return null;
  }
  
  private void writeNexus(Writer writer) {
    try {
      writer.write("#NEXUS\n[Generated by Phenote " + PhenoteVersion.versionString() + "]\n\n");
      writer.write("BEGIN TAXA;\n");
      writer.write("\tDIMENSIONS NTAX=" + this.getGenotypes().size() + ";\n");
      this.writeTaxLabels(writer);
      writer.write("END;\n\n");
      writer.write("BEGIN CHARACTERS;\n");
      writer.write("\tDIMENSIONS NCHAR=" + this.getNexusCharacters().size() + ";\n");
      writer.write("\tFORMAT DATATYPE=STANDARD MISSING=? GAP=- SYMBOLS=\"" + NEXUSAdapter.STATE_SYMBOLS + "\";\n");
      this.writeCharStateLabels(writer);
      this.writeMatrix(writer);
      writer.write("END;\n");
      writer.close();
    } catch (IOException e) {
      log().error("Failed to write NEXUS file", e);
    }
  }
  
  private void writeTaxLabels(Writer writer) throws IOException {
    writer.write("\tTAXLABELS\n");
    for (String genotypeName : this.getGenotypes().keySet()) {
      writer.write("\t\t" + NEXUSAdapter.nexusEscaped(genotypeName) + "\n");
    }
    writer.write("\t\t;\n");
  }
  
  private void writeCharStateLabels(Writer writer) throws IOException {
    writer.write("\tCHARSTATELABELS\n");
    int characterNum = 1;
    Iterator <Entry<NEXUSCharacter, List<OBOClass>>> entryIterator = this.getNexusCharacters().entrySet().iterator();
    while (entryIterator.hasNext()) {
      Entry<NEXUSCharacter, List<OBOClass>> entry = entryIterator.next();
      NEXUSCharacter nexChar = entry.getKey();
      writer.write("\t\t" + characterNum + " " + nexChar.toString() + " /\n");
      int valueCount = 0;
      for (OBOClass valueTerm : entry.getValue()) {
        if (valueCount < NEXUSAdapter.STATE_SYMBOLS.length()) {
          writer.write("\t\t\t" + NEXUSAdapter.nexusEscaped(valueTerm.getName()) + "[" + valueTerm.getID() + "]\n");
        } else {
          System.out.println("Error: NEXUS output supports no more than" + NEXUSAdapter.STATE_SYMBOLS.length() + "states per character.");
        }
        valueCount++;
      }
      if (entryIterator.hasNext()) writer.write("\t\t\t,\n");
      characterNum++;
    }
    writer.write("\t\t;\n");
  }
  
  private void writeMatrix(Writer writer) throws IOException {
    writer.write("\tMATRIX\n");
    for (Entry<String, ArrayList<Integer>> entry : this.genotypes.entrySet()) {
      writer.write("\t\t" + NEXUSAdapter.nexusEscaped(entry.getKey()) + "\t");
      for (Integer state : entry.getValue()) {
        String writeableState;
        if ((state != null) && (state < NEXUSAdapter.STATE_SYMBOLS.length())) {
          writeableState = NEXUSAdapter.STATE_SYMBOLS.substring(state, (state + 1));
        } else {
          writeableState = "?";
        }
        writer.write(writeableState);
      }
      writer.write("\n");
    }
    writer.write("\t\t;\n");
  }
  
  private static String nexusEscaped(String text) {
    text = text.replaceAll("'", "''");
    return ("'" + text + "'");
  }
  
  private static Logger log() {
    return Logger.getLogger(NEXUSAdapter.class);
  }
  
  private static class NEXUSCharacter {
    
    private OBOClass entity;
    private OBOClass attribute;
    
    public NEXUSCharacter(OBOClass anEntity, OBOClass anAttribute) {
      this.entity = anEntity;
      this.attribute = anAttribute;
    }
    
    public OBOClass getEntity() {
      return this.entity;
    }
    
    public OBOClass getAttribute() {
      return this.attribute;
    }
    
    public boolean equals(Object obj) {
      if (!(obj instanceof NEXUSCharacter)) {
        return false;
      }
      NEXUSCharacter otherChar = (NEXUSCharacter)obj;
      return ((this.getEntity() == otherChar.getEntity()) && (this.getAttribute() == otherChar.getAttribute()));
    }
    
    public int hashCode() {
       return this.entity.hashCode() ^ this.attribute.hashCode();
    }
    
    public String toString() {
      return NEXUSAdapter.nexusEscaped(this.entity.getName() + " " + this.attribute.getName()) + " [" + this.entity.getID() + "+" + this.attribute.getID() + "]";
    }
  }

}
