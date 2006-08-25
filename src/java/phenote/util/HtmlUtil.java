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

    System.out.println(sb);

    return sb.toString();
  }

  public static String termInfo(OBOClass oboClass, String ontology) {
    // funny - revisit for sure - either should pass through all methods
    // or util should actually be an object - singleton? i think maybe its
    // an object???
    setOntologyName(ontology);
    return termInfo(oboClass);
  }

  private static void setOntologyName(String ont) {
    ontologyName = ont;
  }

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
    String clickString = getClickString(term.getID());
    return "<a "+clickString+">"+term.getName()+"</a>";
  }

  private static String getClickString(String id) {
    if (isStandAlone)
      return "href='"+makePhenoIdLink(id)+"'";
    else
      return "href=# "+onClickJavaScript(id);
  }

  //<A href='#' onClick='getTermInfo(".$_->term_id.")'>
  private static String onClickJavaScript(String id) {
    // need ontology name????
    return " onClick='getTermInfo(\""+id+"\",\""+getOntologyName()+"\")' ";
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
