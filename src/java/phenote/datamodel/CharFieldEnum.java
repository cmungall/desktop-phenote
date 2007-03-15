package phenote.datamodel;

// labels? methods? subclasses?
// is this taking enums too far? or a good use of them?
/** so this class has evolved - this was actually doing the setting of field values at one
    point - but now that we've gone to a generic datamodel, the datamodel is now determined
    by the configuration - and not restricted to CharFieldEnums - you can have a field that is
    not in CharFieldEnum - which leads to the question should we get rid of this class?
    and the answer at the moment is maybe not - as you can still use this in data adapters
    to get at predefined strings for definied fields - to make sure data adapters are using
    the same string for the pub of entity field. which is what enums are right? the
    queryable data adapter can use these strings from enum to specify the fields it allows
    for querying - so for now i guess CharFieldEnum lives on in a much more limited fashion
    the setValue & getValue need to be taken out - no longer used - ya just have to
    hope that the strings here are the same ones in config - is there some way to enforce
    or check this - i dont think there is */
public enum CharFieldEnum {

  PUB("Pub") {
    public void setValue(CharacterI c, CharFieldValue v) {
      c.setPub(v.getName());
    }
    public CharFieldValue getValue(CharacterI c) {
      return new CharFieldValue(c.getPub(),c,this);
    }
  },
  GENOTYPE("Genotype") { // genotype? default?
    public void setValue(CharacterI c, CharFieldValue v) {
      c.setGenotype(v.getName());
    }
    public CharFieldValue getValue(CharacterI c) {
      return new CharFieldValue(c.getGenotype(),c,this);
    }
  },
  ALLELE("Allele"),
  GENETIC_CONTEXT("Genetic Context") {
    public void setValue(CharacterI c, CharFieldValue v) {
      c.setGeneticContext(v.getOboClass());
    }
    public CharFieldValue getValue(CharacterI c) {
      return new CharFieldValue(c.getGeneticContext(),c,this);
    }
  },
  ENTITY("Entity") {
    public void setValue(CharacterI c, CharFieldValue v) {
        c.setEntity(v.getOboClass());
    }
    public CharFieldValue getValue(CharacterI c) {
      return new CharFieldValue(c.getEntity(),c,this);
    }
  },
  QUALITY("Quality") {
    public void setValue(CharacterI c, CharFieldValue v) {
        c.setQuality(v.getOboClass());
    }
    public CharFieldValue getValue(CharacterI c) {
      return new CharFieldValue(c.getQuality(),c,this);
    }
  },
  /** Its questionable if relationship belongs here ??? */
  RELATIONSHIP("Relationship") {
    public void setValue(CharacterI c, CharFieldValue v) {}
    public CharFieldValue getValue(CharacterI c) { return null; }
  };
  
  
  // CHAR FIELD ENUM vars & methods (make its own class!)
  private final String name;
  private CharFieldEnum(String name) { this.name = name; }
  public String toString() { return getName(); }
  public String getName() { return name; }
  // no longer used?
  //public abstract void setValue(CharacterI c, CharFieldValue v);
  //public abstract CharFieldValue getValue(CharacterI c);
  
  // unclear if we need this??? need it in generic field config
  public static CharFieldEnum getCharFieldEnum(String fieldString) throws Exception {
    for ( CharFieldEnum cfe : CharFieldEnum.values()) {
      if (cfe.name.equalsIgnoreCase(fieldString))
        return cfe;
    }
    //System.out.println("ERROR: No Char Field found for string "+fieldString);
    //return null;
    // char field enum not found - thats ok with new generic fields
    throw new Exception("No Char Field found for string "+fieldString);
  }
  
};
