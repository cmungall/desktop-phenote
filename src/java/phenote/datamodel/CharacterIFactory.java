package phenote.datamodel;

import org.apache.log4j.Logger;

import phenote.config.Config;
import phenote.config.xml.CharacterModeDocument.CharacterMode;

/** Makes instances of CharacterI, according to mode set - OBOAnnotation or Character */

public class CharacterIFactory {
  
  private static final Logger LOG = Logger.getLogger(AnnotationCharacter.class);
  private static CharacterIFactory singleton;

  public static CharacterIFactory inst() { 
    if (singleton == null) {
      singleton = new CharacterIFactory();
      if (singleton.isOboAnnotationMode())
        LOG.info("Using OBO Annotation model");
      else 
        LOG.info("Using regular character model (not obo annotation)");
    }
    return singleton;
  }
  
  public static void reset() {
    singleton = null;
  }

  /** static sugar */
  public static CharacterI makeChar() { 
    return inst().makeCharacter();
  }

  public CharacterI makeCharacter() {
    // need to get mapping driver from configuration?
    if (isOboAnnotationMode()) {
      // Returns Basic by default
      AnnotationMappingDriver d = Config.inst().getAnnotMappingDriver();
      return new AnnotationCharacter(d);
    }
    // default... for now
    return new Character();

  }
  
  // or should this be a push rather than a pull?
  private boolean isOboAnnotationMode() {
    return Config.inst().getCharacterMode() == CharacterMode.Mode.OBO_ANNOTATION;
  }

  public static boolean supportsComparisons() {
    return makeChar().supportsComparisons();
  }

}


// hmmmm
//   public static enum ModeEnum { CHARACTER, OBO_ANNOTATION };
//   private ModeEnum mode = ModeEnum.CHARACTER;
//   public void setMode(ModeEnum me) {
//     mode = me;
//   }

//   public void setMode(String mode) throws FactoryEx {
//     setMode(modeForString(mode));
//   }

//   private ModeEnum modeForString(String mode) throws FactoryEx {
//     for (ModeEnum m : ModeEnum.values()) 
//       if (mode.equalsIgnoreCase(m.toString())) return m;
//     throw new FactoryEx("Unknown CharacterI Factory mode "+mode);
//   }

//   // ?? make own file
//   public static class FactoryEx extends Exception {
//     public FactoryEx(String m) { super(m); }
//   }

