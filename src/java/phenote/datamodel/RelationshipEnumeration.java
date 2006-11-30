package phenote.datamodel;

import org.geneontology.oboedit.datamodel.OBOProperty;

/**
 * Created by IntelliJ IDEA.
 * User: Christian Pich
 * Date: Nov 30, 2006
 * Time: 3:19:18 PM
 * To change this template use File | Settings | File Templates.
 */

// Note: The 'name' and 'id' attribute are not consistent with underscores etc.
//
public enum RelationshipEnumeration {
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

  RelationshipEnumeration(String name, int index) {
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

  static public RelationshipEnumeration getRelationshipEnum(String name) {
    for (RelationshipEnumeration item : values()) {
      if (item.matches(name))
        return item;
    }
    return null;
  }

}
