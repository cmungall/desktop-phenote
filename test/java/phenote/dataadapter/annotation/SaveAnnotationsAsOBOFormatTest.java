package phenote.dataadapter.annotation;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;
import org.obo.datamodel.OBOSession;

import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.dataadapter.delimited.DelimitedFileAdapter;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.main.Phenote;

/**
 * This test is "disabled" at the moment because it has not been maintained 
 * and the required config file ("ncbo-test.cfg") no longer exists.
 */
public class SaveAnnotationsAsOBOFormatTest {

  AnnotationOBOFileAdapter adapter = new AnnotationOBOFileAdapter();
  private static CharacterListI clist;
  private static OBOSession session;
  protected static String getConfigFileName() { return "ncbo-test.cfg"; }
  protected static String getDataFilePath() { return "test/testfiles/Sox9-human-bbop.tab";}

  //@BeforeClass
  public static void initialize() throws ConfigException {
    Phenote.resetAllSingletons();
    Config.inst().setConfigFile("ncbo-test.cfg");
    Phenote phenote = Phenote.getPhenote();
    phenote.initOntologies();
    phenote.initGui();
    DelimitedFileAdapter ad = new DelimitedFileAdapter();
    clist = 
      ad.load(new File(getDataFilePath()));
    System.err.println("clist size: "+clist.size());
    testCharacterList(clist);
    session = CharFieldManager.inst().getOboSession();
  }

  public static void testCharacterList(CharacterListI clist) {
    for (CharacterI c : clist.getList()) {
      System.out.println("character ="+c);
      for (CharField cf : c.getAllCharFields()) {
        System.out.println("  "+cf.getName()+" "+c.getValueString(cf));
      }

    }
  }

  @Ignore @Test
  public void saveAnnotations()   {
    adapter.setAdapterValue("/tmp/foo.obo");
    adapter.commit(clist);
  }

}
