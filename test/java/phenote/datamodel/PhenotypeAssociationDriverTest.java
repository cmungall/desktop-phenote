package phenote.datamodel;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.obo.annotation.datamodel.Annotation;
import org.obo.datamodel.OBOSession;
import org.obo.util.AnnotationUtil;

import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.dataadapter.delimited.DelimitedFileAdapter;
import phenote.main.Phenote;

/**
 * This test is "disabled" at the moment because it has not been maintained 
 * and the required config file ("ncbo-test.cfg") no longer exists.
 */
public class PhenotypeAssociationDriverTest {

 
  private static OBOSession session;
  private static AnnotationModelTestUtil util;
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
    CharacterListI clist = 
      ad.load(new File("test/testfiles/Sox9-human-bbop.tab"));
    System.err.println("clist size: "+clist.size());
    testCharacterList(clist);
    session = CharFieldManager.inst().getOboSession();
    util = new AnnotationModelTestUtil(session);
  }

  public static void testCharacterList(CharacterListI clist) {
    for (CharacterI c : clist.getList()) {
      System.out.println("character ="+c);
      for (CharField cf : c.getAllCharFields()) {
        System.out.println("  "+cf.getName()+" "+c.getValueString(cf));
      }

    }
  }
  
  public  Collection<String> getFilesToLoad() {
    String[] files={};
    return Arrays.asList(files);
  }

  @Ignore @Test
  public void checkAnnotations() {
    Collection<Annotation> annots = AnnotationUtil.getAnnotations(session);
    System.err.println("# annots = "+annots.size());
    for (Annotation annot : annots) {
      util.writeAnnot(annot);

    }
    util.testForAnnotation("OMIM:608160.001","PATO:0001592^OBO_REL:inheres_in(FMA:7474)");
    Assert.assertTrue(annots.size() > 0);
  }

}
