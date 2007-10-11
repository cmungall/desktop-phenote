package phenote.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geneontology.oboedit.datamodel.Link;
import org.geneontology.oboedit.datamodel.OBOProperty;
import org.geneontology.oboedit.datamodel.impl.InstancePropertyValue;
import org.geneontology.oboedit.datamodel.impl.OBOPropertyImpl;
import org.junit.Assert;
import org.junit.Test;


/**
 * Unit test class for FileUtil.
 */
public class TermLinkComparatorTest {

  /**
   * Create a single file and archive it. Make sure it moved into the archive directory.
   */
  @Test public void testIS_A_Before_PART_Of() {
    List<InstancePropertyValue> links = new ArrayList<InstancePropertyValue>();
    links.add(getLinkUnknownOne());
    links.add(getLinkUnknownTwo());
    links.add(getLinkDevelops_From());
    links.add(getLinkPart_Of());
    links.add(getLinkIS_A());

    Collections.sort(links, new TermLinkComparator());

    Link link = (Link) links.get(0);
    Assert.assertEquals("IS a first", "is_a", link.getType().getName());
    link = (Link) links.get(1);
    Assert.assertEquals("Part of is second", "part of", link.getType().getName());
    link = (Link) links.get(2);
    Assert.assertEquals("Part of is second", "develops from", link.getType().getName());
    link = (Link) links.get(3);
    Assert.assertEquals("IS a first", "Unknown A", link.getType().getName());
    link = (Link) links.get(4);
    Assert.assertEquals("IS a first", "Unknown B", link.getType().getName());

  }

  private InstancePropertyValue getLinkIS_A(){
    InstancePropertyValue link = new InstancePropertyValue();
    OBOProperty prop = new OBOPropertyImpl(TermLinkComparator.RelationshipEnum.IS_A.getName());
    link.setType(prop);
    return link;
  }

  private InstancePropertyValue getLinkPart_Of(){
    InstancePropertyValue link = new InstancePropertyValue();
    OBOProperty prop = new OBOPropertyImpl(TermLinkComparator.RelationshipEnum.PART_OF.getName());
    link.setType(prop);
    return link;
  }

  private InstancePropertyValue getLinkDevelops_From(){
    InstancePropertyValue link = new InstancePropertyValue();
    OBOProperty prop = new OBOPropertyImpl(TermLinkComparator.RelationshipEnum.DEVELOPS_FROM.getName());
    link.setType(prop);
    return link;
  }

  private InstancePropertyValue getLinkUnknownOne(){
    InstancePropertyValue link = new InstancePropertyValue();
    OBOProperty prop = new OBOPropertyImpl("Unknown B");
    link.setType(prop);
    return link;
  }

  private InstancePropertyValue getLinkUnknownTwo(){
    InstancePropertyValue link = new InstancePropertyValue();
    OBOProperty prop = new OBOPropertyImpl("Unknown A");
    link.setType(prop);
    return link;
  }

}
