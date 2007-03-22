package phenote.config;

import java.util.ArrayList;
import java.util.List;

import phenote.datamodel.CharField;
import phenote.config.xml.OntologyDocument.Ontology;
import phenote.datamodel.CharFieldEnum; // phase out

// xml beans for writeback
import phenote.config.xml.FieldDocument.Field;
import phenote.config.xml.PhenoteConfigurationDocument.PhenoteConfiguration;

public class FieldConfig {

  //private CharFieldEnum charFieldEnum; // phase out
  private CharField charField;
  private String label;
  // Entity field can have multiple ontologies
  private List<OntologyConfig> ontologyConfigList;
  //private boolean isPostComp;
  //private OntologyConfig postCompRelOntCfg;
  private String syntaxAbbrev;
  private boolean enabled = true; // default if not specified is true


  /** construct from xml bean field - READ */
  FieldConfig(Field fieldBean) {
    this.label = fieldBean.getName();
    
//   try{//phase this out!charFieldEnum = CharFieldEnum.getCharFieldEnum(label);}
//     catch (Exception e) {} // no char field enum for name - new generic!
    //fc = new FieldConfig(name);
    
    if (fieldBean.xgetEnable() != null)
      enabled = fieldBean.getEnable();

    if (fieldBean.getSyntaxAbbrev() != null) {
      setSyntaxAbbrev(fieldBean.getSyntaxAbbrev());
    }
    
    // POST COMP, relationship ontol - OLD WAY - PHASING OUT - now in ont arr below
    if (fieldBean.getPostcomp() != null) {
      //setIsPostComp(true); - set in OC read by ODA
      String relFile = fieldBean.getPostcomp().getRelationshipOntology();
      OntologyConfig rel = OntologyConfig.makePostCompRelCfg(relFile);
      //setPostCompRelOntCfg(rel); dont need anymore - set in OC
      addOntologyConfig(rel); // new way
    }
    
    // ONTOLOGIES if only one ontology file is an attribute... (convenience)
    // this is being phased out - no need and a hassle
    if (fieldBean.getFile() != null) {
      addOntologyConfig(new OntologyConfig(fieldBean));
    }
    // otherwise its multiple ontologies listed in ontology elements (entity)
    // also in new way post comp rel comes in here as well
    else {
      Ontology[] ontologies = fieldBean.getOntologyArray();
      for (Ontology ontBean : ontologies) {
        addOntologyConfig(new OntologyConfig(ontBean,getLabel())); // label -> default name
      }
    }
  }

//   FieldConfig(CharFieldEnum c, String label) {
//     charFieldEnum = c;
//     this.label = label;
//   }

  /** No char field enum - its a generic not in hard wired datamodel! */
//   FieldConfig(String label) {
//     this.label = label;
//     //isGeneric = true;
//   }
  /** with generic fields these are going to become pase' */
  //public CharFieldEnum getCharFieldEnum() { return charFieldEnum; }
  //public boolean hasCharFieldEnum() { return charFieldEnum != null; }

  // --> getName?
  public String getLabel() { return label; }
  boolean hasLabel(String label) { return label.equals(this.label); } 

  //private void setOntologyFile(String f){getOntologyConfig().setOntologyFile(f);}
  public boolean hasOntologies() {
    return ontologyConfigList != null && !ontologyConfigList.isEmpty();
  }

  public boolean hasOntology() { return hasOntologies(); }

  // assume only 1 ontology???
  public OntologyConfig getOntologyConfig() {
    if (!hasOntologies()) { // probably doenst happen, just in case...
      addOntologyConfig(new OntologyConfig());
    }
    return getOntologyConfigList().get(0);
  }

  void addOntologyConfig(OntologyConfig o) {
    if (o == null)
      return;
    getOntologyConfigList().add(o);
  }

  boolean hasOntConfig(OntologyConfig oc) {
    return getOntConfig(oc.getName()) != null;
  }

  /** return OC with name - null if none */
  OntologyConfig getOntConfig(String name) {
    for (OntologyConfig oc : getOntologyConfigList()) {
      if (name.equals(oc.getName()))
        return oc;
    }
    return null;
  }

  void setOntologyConfigList(List<OntologyConfig> configs) {
    ontologyConfigList = configs;
  }

  public List<OntologyConfig> getOntologyConfigList() {
    if (ontologyConfigList == null)
      ontologyConfigList = new ArrayList<OntologyConfig>(1);
    return ontologyConfigList;
  }


  void setSyntaxAbbrev(String syn) {
    this.syntaxAbbrev = syn;
  }
  String getSyntaxAbbrev() {
    if (syntaxAbbrev == null) return getLabel();
    return syntaxAbbrev;
  }
  //void String getSyntaxAbbrev() { return syntaxAbbrev; }
  boolean hasSyntaxAbbrev(String abb) { return abb.equals(syntaxAbbrev); }

  public  boolean isEnabled() { return enabled; }

  public void setCharField(CharField cf) { charField = cf; }
  CharField getCharField() { return charField; }
  boolean hasCharField(CharField cf) { return charField == cf; }

  /** create xml bean and add it to phenCfg for writeback */
  void write(PhenoteConfiguration phenCfg) {
    Field f = phenCfg.addNewField();
    f.setName(getLabel());
    f.setSyntaxAbbrev(getSyntaxAbbrev());
    f.setEnable(isEnabled());
    // f.setType(getType()); // do we need this - no - maybe in future?
    // new way is to just write out with other ontol configs - delete this
//     if (isPostComp()) getPostCompRelOntCfg().writePostComp(f);
    // everything else is in ontology config
    // change here - writing out ontology element even for single ontology - phasing
    // out shoving ontology in field attribs
    // this also will have post comp rel - new way
    for (OntologyConfig oc : getOntologyConfigList())
      oc.writeOntology(f);
  }

  void mergeWithOldConfig(Config oldConfig) {
    // ADD - 1st see if field is new - if so add it and done
    if (!oldConfig.hasFieldConfig(this)) {
      oldConfig.addFieldConfig(this);
      return;
    }

    // UPDATE - its not new - need to check contents
    FieldConfig oldFieldConfig = oldConfig.getFieldConfig(getLabel());
    // replace syntax abbrev or fill in if blank?? - it is a change in version so replace?
    // yea if syn has changed that prob means its a cng in format
    if (syntaxAbbrev != null)
      oldFieldConfig.syntaxAbbrev = syntaxAbbrev;
    // ONTOLOGIES
    for (OntologyConfig newOC : getOntologyConfigList()) {
      newOC.mergeWithOldConfig(oldFieldConfig);
    }
  }

}
  // POST COMP config... 
//   void setIsPostComp(boolean isPostComp) {
//     this.isPostComp = isPostComp;
//   }
//   public boolean isPostComp() {
//     return isPostComp;
//   }
//   void setPostCompRelOntCfg(OntologyConfig oc) {
//     setIsPostComp(true);
//     postCompRelOntCfg = oc;
//   }

//   /** If isPostComp() return relationship ontology filename */
//   public OntologyConfig getPostCompRelOntCfg() {
//     return postCompRelOntCfg;
//   }

//   FieldConfig(CharFieldEnum c,OntologyConfig o) {
//     charFieldEnum = c;
//     if (o == null) return; // shouldnt happen
//     label = o.getName();
//     addOntologyConfig(o);
//   }
