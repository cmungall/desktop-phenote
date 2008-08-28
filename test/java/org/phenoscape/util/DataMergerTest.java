package org.phenoscape.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.junit.Assert;
import org.junit.Test;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.impl.OBOSessionImpl;
import org.phenoscape.io.CharacterTabReader;
import org.phenoscape.io.NeXMLReader;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.State;

public class DataMergerTest {
  
  @Test
  public void mergeCharacters() throws IOException, XmlException {
    final OBOSession session = new OBOSessionImpl();
    final CharacterTabReader reader = new CharacterTabReader(new File("test/testfiles/CharacterTabReaderTestFile1.tab"), session);
    final NeXMLReader nexmlReader = new NeXMLReader(new File("test/testfiles/DataMergerTest.xml"), session);
    final DataSet data = nexmlReader.getDataSet();
    Assert.assertNull("Character 2, State 0, should  not exist in the original data set", this.findState(data.getCharacters().get(1).getStates(), "0"));
    final Phenotype originalPhenotypeC1S0 = data.getCharacters().get(0).getStates().get(0).getPhenotypes().get(0);
    final int originalPhenotypeCountC2S0 = data.getCharacters().get(1).getStates().get(0).getPhenotypes().size();
    Assert.assertEquals("Character 2, State 1, should not have any Phenotypes", 0, originalPhenotypeCountC2S0);
    DataMerger.mergeCharacters(reader, data);
    Assert.assertNotNull("Character 2, State 0, should now exist because there is data for it in the tab file", this.findState(data.getCharacters().get(1).getStates(), "0"));
    final Phenotype newPhenotypeC1S0 = data.getCharacters().get(0).getStates().get(0).getPhenotypes().get(0);
    Assert.assertNotSame("The phenotypes for this state should have been replaced with new ones", newPhenotypeC1S0, originalPhenotypeC1S0);
    final int newPhenotypeCountC2S0 = data.getCharacters().get(1).getStates().get(0).getPhenotypes().size();
    Assert.assertEquals("Character 2, State 1, should have had a Phenotype added", 1, newPhenotypeCountC2S0);
  }
  
  private  State findState(List<State> states, String symbol) {
    for (State state: states) {
      if (symbol.equals(state.getSymbol())) { return state; }
    }
    return null;
  }

}
