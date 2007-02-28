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

    //this is Mark's old termInfo box.  I'll keep it around
    //just in case we need to go back to it.  note that it might not
    //work quite right because i've now changed the other functions it
    //calls.
  public static String termInfoOld(OBOClass oboClass) {
    if (oboClass == null) {
      System.out.println("null obo class for HtmlUtil.termInfo");
      return ""; // null? exception?
    }
    StringBuffer sb = new StringBuffer();
    if (oboClass.isObsolete())
      sb.append("This term is OBSOLETE").append(newLine());
    sb.append(bold("TERM: ")).append(oboClass.getName());
    sb.append(nl()).append(bold("ID: ")).append(colorFont(oboClass.getID(), "red"));

    Set syns = oboClass.getSynonyms();
    for (Iterator it = syns.iterator(); it.hasNext(); ) {
      sb.append(newLine()).append(bold("Synonym: ")).append(it.next());
    }

    String definition = oboClass.getDefinition();
    // definition = lineWrap(definition);
    if (definition != null && !definition.equals(""))
      sb.append(nl()).append(nl()).append(bold("Definition: ")).append(definition);
    
    sb.append(nl()).append(nl()).append(bold("CHILDREN: "));
    sb.append(getChildrenString(oboClass));


    // if (DEBUG) System.out.println(sb);

    return sb.toString();
  }

    //Nicole's attempt at making the Term Info box look a little better   
    //I'm puting all the information into an html table.  rhs are each single table cells
    //but are separated by line breaks.  
  public static String termInfo(OBOClass oboClass) {
    if (oboClass == null) {
      System.out.println("null obo class for HtmlUtil.termInfo");
      return ""; // null? exception?
    }
    StringBuffer sb = new StringBuffer();
    sb.append("<table>");
    if (oboClass.isObsolete()) {
	sb.append(makeRow(makeLeftCol(bold("TERM"))+makeRightCol(bold(oboClass.getName())+colorFont(bold("   (OBSOLETE)"), "red"))));
    }
    else {
	sb.append(makeRow(makeLeftCol(bold("TERM"))+makeRightCol(bold(oboClass.getName()))));
    }
    sb.append(makeRow(makeLeftCol(bold("ID"))+makeRightCol(oboClass.getID())));
    
    Set syns = oboClass.getSynonyms();
    int pos = 0;
    StringBuffer temp = new StringBuffer();
    for (Iterator it = syns.iterator(); it.hasNext(); ) {
	temp.append(it.next()+"<br>");
	pos++;
    }
    if (pos>0) {
	sb.append(makeRow(makeLeftCol(bold("Synonyms"))+makeRightCol(temp.toString())));
    }

    String definition = oboClass.getDefinition();

    if ((definition != null) && !(definition.equals(""))) {
	sb.append(makeRow(makeLeftCol(bold("Definition"))+makeRightCol(definition)));
    }

    sb.append(makeRow(makeLeftCol("")+makeRightCol("")));
    sb.append(makeRow(makeLeftCol(bold("Parents"))+makeRightCol("<hr>")));
    sb.append(getParentalString(oboClass));
    sb.append(makeRow(""));
    sb.append(makeRow(makeLeftCol(bold("Children"))+makeRightCol("<hr>")));
    sb.append(getChildrenString(oboClass));
    sb.append("</table>");
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

  private static String italic(String text) {
    if (!DO_HTML) return text;
    return "<i>"+text+"</i>";
  }

    private static String colorFont(String text, String color) {
	if (!DO_HTML) return text;
	return "<font color="+color+">"+text+"</font>";
    }

    private static String makeRow(String text) {
	if (!DO_HTML) return text;
	return "<tr>"+text+"</tr>";
    }
    private static String makeLeftCol(String text) {
	if (!DO_HTML) return text;
	return "<td width=70 align=right valign=top><font size=-1>"+text+"</font></td>";
    }
    private static String makeRightCol(String text) {
	if (!DO_HTML) return text;
	return "<td align=left valign=top>"+text+"</td>";
    }

    private static String makeTable(String text) {
	if (!DO_HTML) return text;
	return "<table>"+text+"</table>";
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
    // YES...that would be a better method i think -nlw
    //method:  make all the rt hand columns, then wrap around the left col of the type for the table entry
    StringBuffer isaStringBuf = new StringBuffer();
    StringBuffer partofStringBuf = new StringBuffer();
    StringBuffer haspartStringBuf = new StringBuffer();
    StringBuffer devFromStringBuf = new StringBuffer();
    StringBuffer otherStringBuf = new StringBuffer();
    int countIsa=0;
    int countPartof=0;
    int countHaspart=0;
    int countDevfrom=0;
    int countOther=0;
   //i would like to clean this up to be able to group all relationships together, despite
    //the fact that they could be non-standard, like in SO.  might need two for-loops.  
    //would this be a drain on space/time?
    for (Iterator it = links.iterator(); it.hasNext(); ) {
      Link link = (Link)it.next();
      OBOProperty type = link.getType();
      if (type.getName().equals("is_a")) {
	  countIsa++;
	  appendLink(isaStringBuf,isChild,link);
      }
      else if (type.getName().equals("part of") || type.getName().equals("part_of")) {	  
	  countPartof++;
	  appendLink(partofStringBuf,isChild,link);
      }
      else if (type.getName().equals("has part") || type.getName().equals("has_part")) {
	  //i'm using this to catch the wierd reciprocal stuff in FMA
	  //not sure yet if this is the best solution
	  //probably need a new way to visualize reciprocal 'part' relationships
	  countHaspart++;
	  appendLink(haspartStringBuf,isChild,link);
      }	  
	else if (type.getName().equals("develops from") || type.getName().equals("develops_from")) {
	    countDevfrom++;
	    appendLink(devFromStringBuf,isChild,link);
	}
      else { //catch all other relationships
	  countOther++;
	  otherStringBuf.append("<tr>");
	  otherStringBuf.append(makeLeftCol(italic(capitalize(type.getName())))+"<td>");
	  appendLink(otherStringBuf,isChild,link);
	  otherStringBuf.append("</td></tr>");
      }
    }
    if (countIsa>0) 
	sb.append(makeRow(makeLeftCol(italic( isChild ? "Subclass (is_a)" : "Superclass (is_a)"))+makeRightCol(isaStringBuf.toString())));
    if (countPartof>0)
	sb.append(makeRow(makeLeftCol(italic( isChild ? "Has part" : "Part of"))+makeRightCol(partofStringBuf.toString())));
    if (countDevfrom>0)
	sb.append(makeRow(makeLeftCol(italic( isChild ? "Develops into" : "Develops from"))+makeRightCol(devFromStringBuf.toString())));
    if (countOther>0)
	sb.append(makeRow(makeLeftCol(otherStringBuf.toString())));
    return sb;
  }

  private static void appendLink(StringBuffer sb, boolean isChild, Link link) {
      if (isChild)
	  sb.append(termLink(link.getChild())+"<br>");
      else
	  sb.append(termLink(link.getParent())+"<br>");
      //    if (isChild)
      //sb.append(makeRightCol(termLink(link.getChild())));
      //else
      //sb.append(makeRightCol(termLink(link.getParent()))); 
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
      return "href=" + dq("javascript:;") + onClickJavaScript(id,name);
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
    s = escapeSingleQuotes(s);
    return "'"+s+"'";
  }
  private static String escapeSingleQuotes(String s) {
    if (s == null) return null; // ???
    return s.replace("'","\\'");
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
