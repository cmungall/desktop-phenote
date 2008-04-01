package phenote.presentation.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.taglib.TagUtils;

import phenote.util.FileUtil;

/**
 * Tag that creates the display name for a relationship of a term to a child or a parent.
 */
public class FileUtilTag extends TagSupport {

  private String type;
  private String beanName;
  private String property;
  private String scope;


  public int doStartTag() throws JspException {

    // Default scope is 'Page' scope
    if (StringUtils.isEmpty(scope))
      scope = "Page";
    String fileName = (String) TagUtils.getInstance().lookup(pageContext, beanName, property, scope);
    String pureFileName = FileUtil.getPureFileName(fileName);

    try {
      pageContext.getOut().print(pureFileName);
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
    beanName = null;
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
