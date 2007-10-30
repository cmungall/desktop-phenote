package phenote.util;

import org.obo.datamodel.Link;
import org.obo.datamodel.OBOProperty;

import java.util.Comparator;

/**
 * Sort term
 */
public class TermLinkComparator implements Comparator<Link> {

  // Note: The 'name' and 'id' attribute are not consistent with underscores etc.
  //
  public enum RelationshipEnum {
    IS_A(OBOProperty.IS_A.getName(), 1),
    PART_OF("part of", 2) {
      boolean matches(String name) {
        return name.equals("part of") || name.equals("part_of");
      }
    },
    PART_OF_("part_of", 3),
    DEVELOPS_FROM("develops from", 3),
    START_STAGE("start stage", 4),
    END_STAGE("end stage", 5);

    private String name;
    private int index;

    RelationshipEnum(String name, int index) {
      this.name = name;
      this.index = index;
    }

    boolean matches(String name) {
      return this.name.equals(name);
    }

    public String getName() {
      return name;
    }

    public int getIndex() {
      return index;
    }

    static public RelationshipEnum getRelationshipEnum(String name) {
      for (RelationshipEnum item : values()) {
        if (item.matches(name))
          return item;
      }
      return null;
    }
  }

  public int compare(Link link1, Link link2) {


    OBOProperty type = link1.getType();
    if (type.equals(link2.getType()))
      return 0;

    String name1 = link1.getType().getID();
    String name2 = link2.getType().getID();

    RelationshipEnum relationship1 = RelationshipEnum.getRelationshipEnum(name1);
    RelationshipEnum relationship2 = RelationshipEnum.getRelationshipEnum(name2);

    if (relationship1 != null && relationship2 != null) {
      return relationship1.getIndex() - relationship2.getIndex();
    }

    if (relationship1 != null && relationship2 == null) {
      return -1;
    }

    if (relationship2 != null && relationship1 == null) {
      return 1;
    }

    return name1.compareTo(name2);
  }
}
