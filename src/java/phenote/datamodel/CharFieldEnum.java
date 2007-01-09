package phenote.datamodel;

// labels? methods? subclasses?
// is this taking enums too far? or a good use of them?
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
  public abstract void setValue(CharacterI c, CharFieldValue v);
  public abstract CharFieldValue getValue(CharacterI c);
  
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
