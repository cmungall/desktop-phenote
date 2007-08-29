package phenote.datamodel;

/** Makes instances of CharacterI, according to mode set - OBOAnnotation or Character */

public class CharacterIFactory {
  
  private static CharacterIFactory singleton = new CharacterIFactory();

  public static enum ModeEnum { CHARACTER, OBO_ANNOTATION };
  private ModeEnum mode = ModeEnum.CHARACTER;

  public static CharacterIFactory inst() { return singleton; }

  /** static sugar */
  public static CharacterI makeChar() { 
    return inst().makeCharacter();
  }

  public CharacterI makeCharacter() {
    // need to get mapping driver from configuration?
    //if (mode == ModeEnum.OBO_ANNOTATION)
    //  return new AnnotationCharacter(new BasicAnnotationMappingDriver());

    // default... for now
    return new Character();

  }

  public void setMode(ModeEnum me) {
    mode = me;
  }

  public void setMode(String mode) throws FactoryEx {
    setMode(modeForString(mode));
  }

  private ModeEnum modeForString(String mode) throws FactoryEx {
    for (ModeEnum m : ModeEnum.values()) 
      if (mode.equalsIgnoreCase(m.toString())) return m;
    throw new FactoryEx("Unknown CharacterI Factory mode "+mode);
  }

  // ?? make own file
  public static class FactoryEx extends Exception {
    public FactoryEx(String m) { super(m); }
  }
}

