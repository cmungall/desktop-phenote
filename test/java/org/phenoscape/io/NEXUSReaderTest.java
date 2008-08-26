package org.phenoscape.io;

import java.io.File;
import java.io.IOException;

import org.biojava.bio.seq.io.ParseException;
import org.junit.Assert;
import org.junit.Test;

public class NEXUSReaderTest {
  
  @Test
  public void readMixOfLabeledAndUnlabeledCharacters() throws ParseException, IOException {
    final int expectedCharacterCount = 147; // this comes from the test input file
    final NEXUSReader reader = new NEXUSReader(new File("test/testfiles/NEXUSReaderTestFile1.nex"));
    Assert.assertEquals("The reader should create the number of characters present in the matrix, even if some don't have labels", expectedCharacterCount, reader.getCharacters().size());
    Assert.assertEquals("The character labels should be assigned to the correct character", "12.6", (reader.getCharacters().get(17 - 1)).getLabel());
  }

}
