package phenote.util;

import java.util.Iterator;
import java.util.Set;

import javax.swing.event.HyperlinkEvent;

import org.geneontology.oboedit.datamodel.Link;
import org.geneontology.oboedit.datamodel.LinkedObject;
import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.OBOProperty;

public class HtmlUtil {

  public static final boolean DO_HTML = true; // take out
  static final String PHENOTE_LINK_PREFIX = "Phenote?id=";

//   /** hmmmm - this is state - should probably do an object? 
//    should this be somewhere else? gui.Phenote? */
  private static boolean isStandAlone = true;
  private static String ontologyName;
  private static String field;

  /** Stand alone and web app do different things for term links */
  public static void setStandAlone(boolean standAlone) {
    isStandAlone = standAlone;
  }

  public static String termInfo(OBOClass oboClass) {
    if (oboClass == null) {
      System.out.println("null obo class for HtmlUtil.termInfo");
      return ""; // null? exception?
    }
    StringBuffer sb = new StringBuffer();
    if (oboClass.isObsolete())
      sb.append("This term is OBSOLETE").append(newLine());
    sb.append(bold("TERM: ")).append(oboClass.getName());
    sb.append(nl()).append(bold("ID: ")).append(oboClass.getID());
    Set syns = oboClass.getSynonyms();
    for (Iterator it = syns.iterator(); it.hasNext(); ) {
      sb.append(newLine()).append(bold("Synonym: ")).append(it.next());
    }
    
    sb.append(nl()).append(nl()).append(bold("PARENTS: "));
    sb.append(getParentalString(oboClass));
    sb.append(nl()).append(nl()).append(bold("CHILDREN: "));
    sb.append(getChildrenString(oboClass));

    String definition = oboClass.getDefinition();
    // definition = lineWrap(definition);
    if (definition != null && !definition.equals(""))
      sb.append(nl()).append(nl()).append(bold("Definition: ")).append(definition);

    // if (DEBUG) System.out.println(sb);

    return sb.toString();
  }

  public static String termInfo(OBOClass oboClass, String ontology,String field) {
    // funny - revisit for sure - either should pass through all methods
    // or util should actually be an object - singleton? i think maybe its
    // an object???
    setOntologyName(ontology);
    setField(field);
    return termInfo(oboClass);
  }

  // maybe this should be an object? as this is stateful
  private static void setOntologyName(String ont) {
    ontologyName = ont;
  }
  /** string for web to track source of term info for UseTermInfo */
  private static void setField(String f) { field = f; }
  private static String getField() { return field; }

  private static String getOntologyName() { return ontologyName; }

  /** Only works in html mode - do with string buffers? */
  private static String bold(String text) {
    if (!DO_HTML) return text;
    return "<b>"+text+"</b>";
  }

  private static String nl() { return newLine(); }

  private static String newLine() {
    if (DO_HTML) return "\n<br>";
    return "\n";
  }
  private static StringBuffer getParentalString(OBOClass oboClass) {
    Set parents = oboClass.getParents();
    return getLinksString(parents,false);
  }

  private static StringBuffer getChildrenString(OBOClass oboClass) {
    Set children = oboClass.getChildren();
    return getLinksString(children,true);
  }

  private static StringBuffer getLinksString(Set links, boolean isChild) {
    StringBuffer sb = new StringBuffer();
    // or should thi sjust be done more generically with a hash of string bufs
    // for each unique link type name?
    StringBuffer isaStringBuf = new StringBuffer();
    StringBuffer partofStringBuf = new StringBuffer();
    StringBuffer devFromStringBuf = new StringBuffer();
    StringBuffer otherStringBuf = new StringBuffer();
    for (Iterator it = links.iterator(); it.hasNext(); ) {
      Link link = (Link)it.next();
      OBOProperty type = link.getType();
      //sb.append(newLine());
      //if (type == OBOProperty.IS_A) - somehow theres 2 instances???
      if (type.getName().equals("is_a")) {
	isaStringBuf.append(newLine());
        isaStringBuf.append(bold( isChild ? "Subclass" : "Superclass"));
        isaStringBuf.append(bold("(ISA): "));
	appendLink(isaStringBuf,isChild,link);
      }
      else if (type.getName().equals("part of")) {
	partofStringBuf.append(newLine());
        partofStringBuf.append(bold( isChild ? "Subpart: " : "Part of: "));
	appendLink(partofStringBuf,isChild,link);
      }
      else if (type.getName().equals("develops from")) {
	devFromStringBuf.append(newLine());
	devFromStringBuf.append(bold( isChild ? "Develops into: ":"Develops from: "));
	appendLink(devFromStringBuf,isChild,link);
      }
      // catch all - any relationships missed just do its name capitalize? _->' '?
      else {
	otherStringBuf.append(newLine());
        otherStringBuf.append(bold(capitalize(type.getName()))).append(": ");
	appendLink(otherStringBuf,isChild,link);
      }
//       if (isChild)
//         termBuf.append(termLink(link.getChild()));
//       else
//         termBuf.append(termLink(link.getParent())); 
    }
    sb.append(isaStringBuf).append(partofStringBuf);
    sb.append(devFromStringBuf).append(otherStringBuf);
    return sb;
  }

  private static void appendLink(StringBuffer sb, boolean isChild, Link link) {
    if (isChild)
      sb.append(termLink(link.getChild()));
    else
      sb.append(termLink(link.getParent())); 
  }

  private static String termLink(LinkedObject term) {
    String clickString = getClickString(term.getID(),term.getName());
    //System.out.println(clickString);
    return "<a "+clickString+">"+term.getName()+"</a>";
  }

  private static String getClickString(String id,String name) {
    if (isStandAlone)
      return "href='"+makePhenoIdLink(id)+"'";
    else // this needs some reworking - causes page refresh and goes to top
      return "href=# "+onClickJavaScript(id,name);
  }

  /**<A href='#' onClick='getTermInfo("id","name","ontology")'> - added in name for
     UseTerm */
  private static StringBuffer onClickJavaScript(String id,String name) {
    //String c = ",";
    //return " onClick='getTermInfo("+q(id)+c+q(name)+c+q(getOntologyName())+")' ";
    StringBuffer sb = new StringBuffer("onClick=");
    String ont = getOntologyName(), f = getField();
    StringBuffer fn = fn("getTermInfo",new String[]{id,name,ont,f});
    return sb.append(dq(fn));
  }
  /** quoter */
  private static String dq(String s) {
    return "\""+s+"\"";
  } 

  private static StringBuffer dq(StringBuffer sb) {
    return new StringBuffer("\""+sb+"\"");
  }

  private static StringBuffer q(StringBuffer sb) {
    return new StringBuffer("'"+sb+"'");
  }
  private static String q(String s) {
    return "'"+s+"'";
  }

  public static StringBuffer fn(String fnName, String[] params) {
    StringBuffer s = new StringBuffer(fnName).append("(").append(q(params[0]));
    for (int i=1; i<params.length; i++)
      s.append(",").append(q(params[i]));
    s.append(")");
    return s;
  }

  /** used internally & by TestPhenote */
  public static String makePhenoIdLink(String id) {
    return PHENOTE_LINK_PREFIX + id;
  }

  private static String capitalize(String s) {
    if (s == null || s.equals("")) return "";
    String firstLetter = s.substring(0,1);
    return firstLetter.toUpperCase() + s.substring(1,s.length());
  }

  public static boolean isPhenoteLink(HyperlinkEvent e) {
    return e.getURL() == null && e.getDescription().startsWith(PHENOTE_LINK_PREFIX);
  }

  /** extracts id from link, returns null if fails */
  public static String getIdFromHyperlink(HyperlinkEvent e) {
      String desc = e.getDescription();
      if (desc == null || desc.equals("")) return null;
      String id = getIdFromHyperlinkDesc(desc);
      return id;
  }

  /** extract id from hyperlink description string */
  private static String getIdFromHyperlinkDesc(String desc) {
    return desc.substring(PHENOTE_LINK_PREFIX.length());
  }

}
