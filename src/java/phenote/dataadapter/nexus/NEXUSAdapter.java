package phenote.dataadapter.nexus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.geneontology.oboedit.datamodel.Link;
import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.TermCategory;

import phenote.dataadapter.DataAdapterI;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;

/** Writes Phenote characters to a NEXUS file.
 * Phenote characters are grouped into NEXUS characters via shared "attribute" heritage
 * within PATO.
 * The exporter supports only up to 36 values for any particular attribute, due to
 * various limitations of other widely used NEXUS implementations (e.g. Mesquite & MacClade).
 * There is no support for reading of NEXUS files.
*/

public class NEXUSAdapter implements DataAdapterI {
  
  private File file;
  private static String[] extensions = {"nex", "nxs"};
  private static String GENOTYPE_KEY = "Genotype";
  private static String ENTITY_KEY = "Entity";
  private static String VALUE_KEY = "Quality";
  private static String STATE_SYMBOLS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private LinkedHashMap<String, ArrayList<Integer>> genotypes; // ordered map of genotype names and value lists
  private LinkedHashMap<NEXUSCharacter, List<OBOClass>> nexusCharacters; // ordered map of character names and state lists

  
  public void commit(CharacterListI charList) {
    if (this.file == null)
      return;
    this.setGenotypes(new LinkedHashMap<String, ArrayList<Integer>>());
    this.setNexusCharacters(new LinkedHashMap<NEXUSCharacter, List<OBOClass>>());
    this.populateGenotypesAndCharacters(charList);
    this.createDefaultCharacterValues();
    this.populateCharacterValues(charList);
    this.writeNexus(this.file);
  }

  public void commit(CharacterListI charList, File f) {
    this.file = f;
    commit(charList);
  }

  public String getDescription() {
    return "NEXUS files";
  }

  public List<String> getExtensions() {
    return Arrays.asList(NEXUSAdapter.extensions);
  }

  public void load() {
    System.out.println("Cannot read: NEXUS adapter is for import only.");
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
          genotype = ch.getValueString(GENOTYPE_KEY);
          entityTerm = ch.getTerm(ENTITY_KEY);
          valueTerm = ch.getTerm(VALUE_KEY);
        } catch (Exception e) {
          System.out.println("Could not get terms from character: " + e);
          continue;
        }
        OBOClass attributeTerm = this.getAttributeForValue(valueTerm);
        if (attributeTerm == null) {
          System.out.println("Failed to find attribute for value.");
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
        String genotype;
        OBOClass entityTerm;
        OBOClass valueTerm;
        try {
          genotype = ch.getValueString(GENOTYPE_KEY);
          entityTerm = ch.getTerm(ENTITY_KEY);
          valueTerm = ch.getTerm(VALUE_KEY);
        } catch (Exception e) {
          System.out.println("Could not get terms from character: " + e);
          continue;
        }
        OBOClass attributeTerm = this.getAttributeForValue(valueTerm);
        if (attributeTerm == null) {
          System.out.println("Failed to find attribute for value.");
          continue;
        }
        NEXUSCharacter currentCharacter = new NEXUSCharacter(entityTerm, attributeTerm);
        List<OBOClass> currentValueList = this.nexusCharacters.get(currentCharacter);
        int indexOfCharacter = Arrays.asList(this.nexusCharacters.values().toArray()).indexOf(currentValueList);
        int indexOfValue = currentValueList.indexOf(valueTerm);
        this.genotypes.get(genotype).set(indexOfCharacter, indexOfValue);
      }
    }
  }
  
  private boolean isValidCharacter(CharacterI character) {
    try {
      return (character.hasValue(GENOTYPE_KEY) && (character.hasValue(ENTITY_KEY)) && (character.hasValue(VALUE_KEY)));
    } catch (Exception e) {
      return false;
    }
  }
  
  private OBOClass getAttributeForValue(OBOClass valueTerm) {
    Set categories = valueTerm.getCategories();
    Set<String> categoryNames = new HashSet<String>();
    for (Object category : categories) {
      categoryNames.add(((TermCategory)category).getName());
    }
    if (categoryNames.contains("attribute_slim")) {
      return valueTerm;
    } else if ((categoryNames.contains("value_slim")) && !(valueTerm.isRoot())) {
      return this.getAttributeForValue(this.getParentForTerm(valueTerm));
    }
    return null;
  }
  
  private OBOClass getParentForTerm(OBOClass term) {
    Set parents = term.getParents();
    for (Object o : parents) {
      Link link = (Link)o;
      if (link.getType().getName().equals("is_a")) {
        return (OBOClass)(link.getParent());
      }
    }
    return null;
  }
  
  private void writeNexus(File f) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(f));
      writer.write("#NEXUS\n[Generated by Phenote]\n\n");
      writer.write("BEGIN TAXA;\n");
      writer.write("\tDIMENSIONS NTAX=" + this.genotypes.size() + ";\n");
      this.writeTaxLabels(writer);
      writer.write("END;\n\n");
      writer.write("BEGIN CHARACTERS;\n");
      writer.write("\tDIMENSIONS NCHAR=" + this.nexusCharacters.size() + ";\n");
      writer.write("\tFORMAT DATATYPE=STANDARD MISSING=? GAP=- SYMBOLS=\"" + NEXUSAdapter.STATE_SYMBOLS + "\";\n");
      this.writeCharStateLabels(writer);
      this.writeMatrix(writer);
      writer.write("END;\n");
      writer.close();
    } catch (IOException e) {
      System.out.println("Failed to write NEXUS file " + e);
    }
  }
  
  private void writeTaxLabels(Writer writer) throws IOException {
    writer.write("\tTAXLABELS\n");
    for (String genotypeName : this.genotypes.keySet()) {
      writer.write("\t\t" + NEXUSAdapter.nexusEscaped(genotypeName) + "\n");
    }
    writer.write("\t\t;\n");
  }
  
  private void writeCharStateLabels(Writer writer) throws IOException {
    writer.write("\tCHARSTATELABELS\n");
    int characterNum = 1;
    Iterator <Entry<NEXUSCharacter, List<OBOClass>>> entryIterator = this.nexusCharacters.entrySet().iterator();
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
  
  
  private class NEXUSCharacter {
    
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
