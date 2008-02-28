package phenote.datamodel;

// type is from xmlbean Field
import phenote.config.xml.FieldDocument.Field.Type;

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

  PUB("Pub"), GENOTYPE("Genotype","GT"), ALLELE("Allele"),
  GENETIC_CONTEXT("Genetic Context"),
  ENTITY("Entity","E"), ENTITY2("Add'l Entity","E2"), QUALITY("Quality"),
  DATE_CREATED("Date Created","date_created",Type.DATE),
  ASSIGNED_BY("Assigned by"),
  EVIDENCE("Evidence"),
  /** Its questionable if relationship belongs here ??? */
  RELATIONSHIP("Relationship"),
  COMPARISON("Comparison","comparison",Type.COMPARISON);
  
  // CHAR FIELD ENUM vars & methods (make its own class!)
  // char field enum should probably only deal in tag, name is a bit display-ey
  private final String name;
  private final String tag;
  private final Type.Enum type;
  private CharFieldEnum(String name) {
    this(name,null);
  }
  private CharFieldEnum(String name,String tag) {
    this(name,tag,null);
  }
  private CharFieldEnum(String name,String tag,Type.Enum type) {
    this.name = name;
    this.tag = tag;
    this.type = type;
  }
  public String toString() { return getName(); }
  public String getName() { return name; }
  public String getTag() { 
    return tag!= null ? tag : name;
  }
  public Type.Enum getType() { return type; }
  public boolean equals(String s) {
    if (s == null) return false;
    return s.equalsIgnoreCase(name) || s.equalsIgnoreCase(tag);
  }
  public boolean equals(CharField cf) {
    return equals(cf.getName()) || equals(cf.getTag());
  }

  // no longer used?
  //public abstract void setValue(CharacterI c, CharFieldValue v);
  //public abstract CharFieldValue getValue(CharacterI c);
  
  // unclear if we need this??? need it in generic field config
  public static CharFieldEnum getCharFieldEnum(String fieldString) throws Exception {
    for ( CharFieldEnum cfe : CharFieldEnum.values()) {
      if (cfe.equals(fieldString))
        return cfe;
    }
    //System.out.println("ERROR: No Char Field found for string "+fieldString);
    //return null;
    // char field enum not found - thats ok with new generic fields
    throw new Exception("No Char Field found for string "+fieldString);
  }
  
};
