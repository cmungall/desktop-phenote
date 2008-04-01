package phenote.config;

import java.util.ArrayList;
import java.util.List;

import phenote.config.xml.FieldDocument.Field;
import phenote.config.xml.OntologyDocument.Ontology;
import phenote.dataadapter.ConstraintManager;
import phenote.dataadapter.RequiredFieldConstraint;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldEnum;

public class FieldConfig {

  //private CharFieldEnum charFieldEnum; // phase out
  private CharField charField;
  //private String label;
  private String desc;
  // Entity field can have multiple ontologies
  private List<OntologyConfig> ontologyConfigList;
  //private boolean isPostComp;
  //private OntologyConfig postCompRelOntCfg;
  //private String syntaxAbbrev;
  //private boolean enabled = true; // default if not specified is true
  private Config config;
  private Field fieldBean;
  private String displayName;


  // from gui config
  // public FieldConfig(Config cfg) ?? { 
  // fieldBean = cfg.getPhenCfgBean().addNewField()

  /** construct from xml bean field - READ */
  FieldConfig(Field fieldBean,Config cfg) {
    this.config = cfg;
    this.fieldBean = fieldBean; // test for null - even possible?
    //this.label = fieldBean.getName();
    this.desc = fieldBean.getDesc();
//   try{//phase this out!charFieldEnum = CharFieldEnum.getCharFieldEnum(label);}
//     catch (Exception e) {} // no char field enum for name - new generic!
    //fc = new FieldConfig(name);
    
//     if (fieldBean.xgetEnable() != null)
//       enabled = fieldBean.getEnable();

    if (fieldBean.getTab() != null) { //todo...
    }

//     if (fieldBean.getSyntaxAbbrev() != null) {
//       setSyntaxAbbrev(fieldBean.getSyntaxAbbrev());
//     }
    
    // POST COMP, relationship ontol - OLD WAY - PHASING OUT - now in ont arr below
    if (fieldBean.getPostcomp() != null) {
      //setIsPostComp(true); - set in OC read by ODAe
      String relFile = fieldBean.getPostcomp().getRelationshipOntology();
      OntologyConfig rel = OntologyConfig.makePostCompRelCfg(relFile,this);
      //setPostCompRelOntCfg(rel); dont need anymore - set in OC
      addOntologyConfig(rel); // new way
    }
    
    // ONTOLOGIES if only one ontology file is an attribute... (convenience)
    // this is being phased out - no need and a hassle
    if (fieldBean.getFile() != null && !fieldBean.getFile().equals("")) {
      addOntologyConfig(new OntologyConfig(fieldBean,this));
    }
    // otherwise its multiple ontologies listed in ontology elements (entity)
    // also in new way post comp rel comes in here as well
    else {
      Ontology[] ontologies = fieldBean.getOntologyArray();
      for (Ontology ontBean : ontologies) {
        addOntologyConfig(new OntologyConfig(ontBean,this)); // label -> default name
      }
    }

    // REQUIRED CONSTRAINT - if required then make RequiredConstraint
    // cant do this here - as it does it for every config construction - no good
    //makeRequiredConstraint();
  }

  // for ConfigGui
  public FieldConfig(String label, String displayName,Config cfg) {
    config = cfg;
    setLabel(label);
    this.displayName = displayName;
  }

  /** for auto fields like date_created */
  public FieldConfig(CharFieldEnum fieldEnum, CharField cf) {
    initConfig();
    initFieldBean();
    setCharField(cf);
    setLabel(fieldEnum.getName());
    setDataTag(fieldEnum.getTag());
    setType(fieldEnum.getType());
  }

  Config getConfig() {
    if (config==null) config = Config.inst(); // ?
    return config;
  }

  private void initConfig() { getConfig(); }

  /** return xml bean for field -always non null */
  public Field getFieldBean() {
    if (fieldBean == null) //create one...
      fieldBean = getConfig().addNewFieldBean();
    return fieldBean;
  } 

  private void initFieldBean() { getFieldBean(); }

  /** if required then make RequiredConstraint
      if warnIfNull make RequiredConstraint with warning */
  void makeConstraint() {
    if (!isRequired() && !isWarnIfNull()) return; // its not required do nothing
    RequiredFieldConstraint con =
      new RequiredFieldConstraint(getCharField(),isRequired());
    ConstraintManager.inst().addConstraint(con);
  }

  /** whether field is required for commit, default false, from config bean */
  private boolean isRequired() {
    if (getFieldBean().xgetIsRequired() == null) return false; //default
    return getFieldBean().getIsRequired();
  }
  private boolean isWarnIfNull() {
    if (getFieldBean().xgetWarnIfNull() == null) return false; //default
    return getFieldBean().getWarnIfNull();
  }


  // --> getName?
  public String getLabel() { return fieldBean.getName(); } 
  boolean hasLabel(String label) { return label.equals(getLabel()); } 
  public void setLabel(String label) {
    getFieldBean().setName(label);
    displayName = label;
  } // ??
  public String toString() { 
    if (displayName != null) return displayName;
    return getLabel();
  }

  public Field.Type.Enum getType() {
    // if not configged default to term if have ontols, otherwise free text
    if (fieldBean.getType() == null) {
      if (hasOntologies()) fieldBean.setType(Field.Type.TERM);
      else fieldBean.setType(Field.Type.FREE_TEXT);
    }
    return fieldBean.getType();
  }

  private void setType(Field.Type.Enum type) {
    getFieldBean().setType(type);
  }

  public boolean isID() {
    if (getType() == null) return false;
    return getType() == Field.Type.ID;
  }

  public boolean isComparison() {
    if (getType() == null) return false;
    return getType() == Field.Type.COMPARISON;
  }

  //public String getLabel() { return label; }
  public String getDesc() { return desc; }
  
  public int getColwidth() { 
  	//if 0 then uninitialized;
  	//if -1 indicates not to display
   	return fieldBean.getColwidth(); 
  }
  
  public void setColwidth(int w) {
  	fieldBean.setColwidth(w);
  	return;
  }

  /** Return the amount of letters/chars a user has to type before completion
      kicks in - this helps with slow completion with many ontologies on a slow
      computer */
  public int getMinCompletionChars() {
    // i think if unconfigged/uninitialized defaults to 0 which is what we want
    return fieldBean.getMinCompletionChars();
  }
  
  // boolean hasLabel(String label) { return label.equals(this.label); } 
  boolean hasDesc(String desc) { return desc.equals(this.desc); }

  //private void setOntologyFile(String f){getOntologyConfig().setOntologyFile(f);}
  public boolean hasOntologies() {
    return ontologyConfigList != null && !ontologyConfigList.isEmpty();
  }

  public boolean hasOntology() { return hasOntologies(); }

//   // assume only 1 ontology???
//   public OntologyConfig getOntologyConfig() {
//     if (!hasOntologies()) { // probably doenst happen, just in case...
//       addOntologyConfig(new OntologyConfig());
//     }
//     return getOntologyConfigList().get(0);
//   }
  
  public String getPickSourceFieldName() {
    return fieldBean.getPickSource();
  }

  void addOntologyConfig(OntologyConfig o) {
    if (o == null)  return;
    getOntologyConfigList().add(o);
  }
  void insertOntologyConfig(int index,OntologyConfig o) {
    // hmmm funny parallel data structures hmm
    // hafta make space in array for new bean
    //  cause index out of bounds exception
    getFieldBean().insertNewOntology(index);
//     while (getFieldBean().sizeOfOntologyArray() <= index)
//       getFieldBean().addNewOntology();
    getFieldBean().setOntologyArray(index,o.getOntologyBean());
    // fix for now, this can throw index ex in merging field with no ontols
    // to field with many ontols, with rel-postcomp ontols
    // may be a better way to fix this - cant add nulls
    //while (getOntologyConfigList().size() <= index)
    //  getOntologyConfigList().add(null);
    getOntologyConfigList().add(index,o);
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

  int getOntCfgIndex(OntologyConfig oc) {
    return ontologyConfigList.indexOf(oc);
  }


  void setSyntaxAbbrev(String syn) {
    //this.syntaxAbbrev = syn;
    fieldBean.setSyntaxAbbrev(syn);
  }
  /** gets from datatag then label if no syntax abbrev explicitly set,
      replaces spaces with underscores in label
      as pheno syntax is sensitive to spaces (in theory at least) */
  String getSyntaxAbbrev() {
    String s = fieldBean.getSyntaxAbbrev();
    if (s == null) s = fieldBean.getDatatag();
    if (s == null) {
      s = getLabel(); // setSynAbb?
      s = s.replace(' ','_');
    }
    return s;
  }

  /** DataTag should replace syntax abbrev has a handle to the field, used by
      DataInputServlet (and should be used by syn adapter) */
  void setDataTag(String dt) {
    getFieldBean().setDatatag(dt);
  }

  /** First tries to return explicitly set datatag, 2nd syn abbrev, 3rd label w _ */
  public String getDataTag() {
    String s = getFieldBean().getDatatag();
    if (s == null) s = getSyntaxAbbrev();
    return s;
  }

  //void String getSyntaxAbbrev() { return syntaxAbbrev; }
  /** Test both syntaxAbbrev & label - also test for replacing spaces with underscores */
  boolean hasSyntaxAbbrev(String abb) {
    return equalsWithSpaceUnderscore(abb,getSyntaxAbbrev());
  }

  /** returns true if strings equal or if strings equal after replacing space with
      underscore */
  private boolean equalsWithSpaceUnderscore(String abbrev,String s) {
    if (abbrev==null || s==null) return false;
    if (abbrev.equalsIgnoreCase(s)) return true;
    String underForSpace = s.replace(' ','_');
    return abbrev.equalsIgnoreCase(underForSpace);
  }

  public boolean isVisible() {
    if (fieldBean.xgetIsVisible() == null) return true;
    return fieldBean.getIsVisible();
  }

  public void setIsVisible(boolean vis) {
    getFieldBean().setIsVisible(vis);
  }

  public  boolean isEnabled() {
    //return enabled;
    if (fieldBean.xgetEnable() == null)
      fieldBean.setEnable(true); // default true
    return fieldBean.getEnable();
  }

  public void setEnabled(boolean e) {
    fieldBean.setEnable(e);
  }

  /** specifies whether to copy field to new character when char is duplicated
      default is true, db ids generally shouldnt copy */
  public boolean copies() {
    if ( fieldBean.xgetCopyField() == null ) { return true; }
    else { return fieldBean.getCopyField(); }
  }

  public void setCharField(CharField cf) { charField = cf; }

  /** Actually creates char field if null, as char fields utimately come from field
      configs - so this is funny but its actually not funny */
  public CharField getCharField() {
    // heck why not just pass in the char field bean itself - simpler!
    if (charField == null)
      //charField = new CharField(getLabel(),getDataTag(),getType());
      charField = new CharField(this);
    //charField.setCopyEnabled(copies());
    return charField;
  }
  boolean hasCharField(CharField cf) { return charField == cf; }

  boolean inGroup(String group) {
    List<?> l = fieldBean.getGroups();
    if (group == null && l == null) return true;
    if (group == null && l.contains("default")) return true;
    if (l == null) return group.equals("default");
    return l.contains(group);
  }

  void mergeWithOldConfig(Config oldConfig,Config thisCfg) { // should FC know its Cfg?
    // ADD - 1st see if field is new - if so add it and done
    if (!oldConfig.hasFieldCfgAll(this)) {
      // cant just add - need to add in proper place! insert!
      //oldConfig.addFieldConfig(this);
      int index = thisCfg.getAllFieldIndex(this); // enabled??? disabled?
      System.out.println("field config index merge "+index+" fc "+this);
      oldConfig.insertFieldConfig(index,this);
      return;
    }

    // UPDATE - its not new - need to check contents
    FieldConfig oldFieldConfig = oldConfig.getAllFieldCfg(getLabel());
    // replace syntax abbrev or fill in if blank?? - it is a change in version so replace?
    // yea if syn has changed that prob means its a cng in format
    //if (syntaxAbbrev != null)
    // only do if in bean, not getSA which grabs label if bean is null
    if (fieldBean.getSyntaxAbbrev() != null)
      oldFieldConfig.setSyntaxAbbrev(fieldBean.getSyntaxAbbrev());
    oldFieldConfig.setEnabled(isEnabled()); // ??? mode?
    // ONTOLOGIES
    for (OntologyConfig newOC : getOntologyConfigList()) {
      newOC.mergeWithOldConfig(oldFieldConfig);
    }
  }

}

