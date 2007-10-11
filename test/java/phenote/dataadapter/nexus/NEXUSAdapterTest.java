package phenote.dataadapter.nexus;

import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import phenote.datamodel.CharacterList;
import phenote.datamodel.CharacterListI;

public class NEXUSAdapterTest {
  // this test is very lame so far
  
  private NEXUSAdapter adapter;
  private CharacterListI  characterList;
  
  @Before public void setup() {
    this.adapter = new NEXUSAdapter();
    this.characterList = this.getTestCharacterList();
  }
  
  @Test public void commit() {
    StringWriter writer = new StringWriter();
    this.adapter.commit(this.characterList, writer);
    Assert.assertTrue("It should at least write a document without encountering exceptions", writer.toString().startsWith("#NEXUS"));
  }

  private CharacterListI getTestCharacterList() {
    // need to build up a more interesting character list for this test
    return new CharacterList();
  }
}
