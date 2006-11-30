package phenote.presentation.tags;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.taglib.TagUtils;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.JspException;
import java.io.IOException;

import phenote.util.TermLinkComparator;
import phenote.datamodel.RelationshipEnumeration;

/**
 * Tag that creates the display name for a relationship of a term to a child or a parent.
 */
public class RelationshipTag extends TagSupport {

  private String type;
  private String beanName;
  private String property;
  private String scope;


  public int doStartTag() throws JspException {

      // Default scope is 'Page' scope
      if (StringUtils.isEmpty(scope))
          scope = "Page";
      String relationshipString = (String) TagUtils.getInstance().lookup(pageContext, beanName, property, scope);

      StringBuilder sb = new StringBuilder();
      if (type != null && type.equals("parent")) {
        if(relationshipString != null && relationshipString.equals(RelationshipEnumeration.IS_A.getName()))
          sb.append("Is a type of");
        if(relationshipString != null && relationshipString.equals(RelationshipEnumeration.PART_OF.getName()))
          sb.append("Is part of");
        if(relationshipString != null && relationshipString.equals(RelationshipEnumeration.DEVELOPS_FROM.getName()))
          sb.append("Develops from");
      } else if (type != null && type.equals("child")) {
        if(relationshipString != null && relationshipString.equals(RelationshipEnumeration.IS_A.getName()))
          sb.append("Has subtype");
        if(relationshipString != null && relationshipString.equals(RelationshipEnumeration.PART_OF.getName()))
          sb.append("Has parts");
        if(relationshipString != null && relationshipString.equals(RelationshipEnumeration.DEVELOPS_FROM.getName()))
          sb.append("Develops into");
      }

      try {
          pageContext.getOut().print(sb);
      } catch (IOException ioe) {
          throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
      }
      release();
      return Tag.SKIP_BODY;
  }

  /**
   * Release all allocated resources.
   */
  public void release() {
      super.release();

      type = null;
      property = null;
      beanName =null;
      id = null;
      scope = null;

  }



  public String getType() {
      return type;
  }

  public void setType(String type) {
      this.type = type;
  }

  public String getBeanName() {
      return beanName;
  }

  public void setBeanName(String beanName) {
      this.beanName = beanName;
  }
  public String getScope() {
      return scope;
  }

  public void setScope(String scope) {
      this.scope = scope;
  }

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }
}
