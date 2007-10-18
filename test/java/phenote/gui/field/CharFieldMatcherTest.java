package phenote.gui.field;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterIFactory;
import phenote.main.Phenote;

public class CharFieldMatcherTest {
  
  @BeforeClass public static void initialize() throws ConfigException {
    Phenote.resetAllSingletons();
    Config.inst().setConfigFile("test.cfg");
    Phenote phenote = Phenote.getPhenote();
    phenote.initOntologies();
  }
  
  @Test public void testMatchesFreeText() throws CharFieldException {
    final CharField pubField = CharFieldManager.inst().getCharFieldForName("Publication");
    final CharacterI character = CharacterIFactory.makeChar();
    character.setValue(pubField, "testpub");
    final CharFieldMatcher internalMatcher = new CharFieldMatcher(pubField, "stp", false);
    Assert.assertTrue("Should match internal string", internalMatcher.matches(character));
    final CharFieldMatcher unMatcher = new CharFieldMatcher(pubField, "akz", false);
    Assert.assertFalse("Shouldn't match unmatching text", unMatcher.matches(character));
    final CharFieldMatcher upperMatcher = new CharFieldMatcher(pubField, "TEST", false);
    Assert.assertTrue("Should match case-insensitive", upperMatcher.matches(character));
    final CharFieldMatcher inheritMatcher = new CharFieldMatcher(pubField, "test", true);
    Assert.assertTrue("Should ignore inherit flag", inheritMatcher.matches(character));
    final CharFieldMatcher nullMatcher = new CharFieldMatcher(pubField, null, false);
    Assert.assertTrue("Null search should match anything", nullMatcher.matches(character));
    final CharFieldMatcher emptyMatcher = new CharFieldMatcher(pubField, "", false);
    Assert.assertTrue("Empty string should match anything", emptyMatcher.matches(character));
  }
  
  @Test public void testMatchesTerm() throws CharFieldException {
    // for this test to work TAO:0001161 (pectoral fin) must descend from TAO:0000108 (fin)
    // TAO:0000277 (scale) must not inherit from fin
    // perhaps these facts could be tested to make sure
    final CharField entityField = CharFieldManager.inst().getCharFieldForName("Entity");
    final CharacterI character = CharacterIFactory.makeChar();
    character.setValue(entityField, "TAO:0001161");
    final CharFieldMatcher sameExactMatcher = new CharFieldMatcher(entityField, "TAO:0001161", false);
    Assert.assertTrue("Should match same term ID", sameExactMatcher.matches(character));
    final CharFieldMatcher differentExactMatcher = new CharFieldMatcher(entityField, "TAO:0000108", false);
    Assert.assertFalse("Shouldn't match different term ID", differentExactMatcher.matches(character));
    final CharFieldMatcher inheritsMatcher = new CharFieldMatcher(entityField, "TAO:0000108", true);
    Assert.assertTrue("Should match descendant term", inheritsMatcher.matches(character));
    final CharFieldMatcher noInheritMatcher = new CharFieldMatcher(entityField, "TAO:0000277", true);
    Assert.assertFalse("Should not match non-descendant term", noInheritMatcher.matches(character));
    final CharFieldMatcher nullMatcher = new CharFieldMatcher(entityField, null, true);
    Assert.assertTrue("Null search should match anything", nullMatcher.matches(character));
    final CharFieldMatcher badTextMatcher =  new CharFieldMatcher(entityField, "blah", true);
    Assert.assertFalse("Shouldn't match with bad input text", badTextMatcher.matches(character));
  }
  
}
