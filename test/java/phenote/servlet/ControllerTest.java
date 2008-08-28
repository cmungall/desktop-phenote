package phenote.servlet;

import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.gui.field.CompletionTerm;
import phenote.main.Phenote;

/** 
 * If anyone cares about this test they should make it run properly and uncomment the setup stuff.
 */
public class ControllerTest {

  private PhenoteController controller;
  private PhenoteBean bean;
  private static String INPUT = "heart";
  private static String ONTOLOGY = "ZF";
  private static String FIELD = "Entity";

  //@BeforeClass
  public static void initialize() throws ConfigException {
    //need to initialize zfin config...
    Phenote.resetAllSingletons();
    Config.inst().setConfigFile("zfin-standalone.cfg");
    Phenote phenote = Phenote.getPhenote();
    phenote.initOntologies();
  }

  /** setup input bean */
  //@Before 
  public void setup() {

    // Controller
    controller = new PhenoteController();

    // Bean
    bean = new PhenoteBean();
    bean.setUserInput(INPUT); // theres are 6 terms with heart
    bean.setOntologyName(ONTOLOGY);
    bean.setField(FIELD);
    
  }

  @Ignore @Test
  public void doCompletion() {
    // should i use bean itself? or test bean input in separate test?
    // this is currently setup to set bean on separate thread which prob doesnt work
    // with web
    controller.getCompletionList(INPUT,ONTOLOGY,FIELD,bean);
    List<CompletionTerm> compList = bean.getCompletionTermList();
    Assert.assertNotNull(compList);
    Assert.assertTrue("Should have >0 comp terms",compList.size()>0);
    boolean containsInput = false;
    for (CompletionTerm t : compList)
      containsInput |= t.toString().contains(INPUT);
    Assert.assertTrue("Should contain "+INPUT,containsInput);
  }
}
