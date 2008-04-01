package phenote.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.swing.event.HyperlinkEvent;

import org.obo.datamodel.Dbxref;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.Link;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBORestriction;
import org.obo.datamodel.ObsoletableObject;
import org.obo.datamodel.PropertyValue;
import org.obo.datamodel.Synonym;
import org.obo.util.TermUtil;


public class HtmlUtil {

	public static final boolean DO_HTML = true; // take out
	static final String PHENOTE_LINK_PREFIX = "Phenote?id=";
	

//	/** hmmmm - this is state - should probably do an object? 
//	should this be somewhere else? gui.Phenote? */
	private static boolean isStandAlone = true;
	private static String ontologyName;
	private static String field;

	/** Stand alone and web app do different things for term links */
	public static void setStandAlone(boolean standAlone) {
		isStandAlone = standAlone;
	}
	
	public static String termInfo(OBOClass oboClass, String ontology,String field) {
		// funny - revisit for sure - either should pass through all methods
		// or util should actually be an object - singleton? i think maybe its
		// an object???
		setOntologyName(ontology);
		setField(field);
		return termInfo(oboClass);
	}

	public static String termName(OBOClass oboClass) {
		//creates the term name to display in a different component
		StringBuffer sb = new StringBuffer();
		if (oboClass.isObsolete()) {
			sb.append(bold(colorFont(bold("(OBSOLETE) "), "red")));
		}
		sb.append(bold(oboClass.getName()));
		return sb.toString();
	}

	public static String termInfo(OBOClass oboClass) {
		//This page is basically a html table.  
		//rhs are each single table cells but are separated by line breaks.
		//It would be really nice if these could show up as configurable items for the user...
		//they could decide what is normally displayed, possibly with being able to get more
		//info by clicking a button...or maybe we can set these up as expandable items
		if (oboClass == null) {
			System.out.println("null obo class for HtmlUtil.termInfo");
			return ""; // null? exception?
		}
		StringBuffer sb = new StringBuffer();
		sb.append("<table>");
		sb.append(makeRow(makeLeftCol(bold("ONTOLOGY"))+makeRightCol(oboClass.getNamespace().toString())));
		if (oboClass.isObsolete()) {
			sb.append(makeObsLinks(oboClass));  //considers&replaced-bys
		}
		else {
			//sb.append(makeRow(makeLeftCol(bold("TERM"))+makeRightCol(bold(oboClass.getName()))));
			//Term name has been thrown out of the term info box proper, and is
			//in a different java element.
		}

		sb.append(makeRow(makeLeftCol(bold("ID"))+makeRightCol(oboClass.getID())));

		String synonyms = makeSyns(true, oboClass.getSynonyms());
		if (synonyms!=null) {
			sb.append(synonyms);
		}

		sb.append(makeDbxrefs(oboClass));
		
		String definition = oboClass.getDefinition();

		if ((definition != null) && !(definition.equals(""))) {
			sb.append(makeRow(makeLeftCol(bold("Definition"))+makeRightCol(definition)));
		}
		if (TermUtil.isIntersection(oboClass)) {
			//this term is an intersection term, show the xp definition
			sb.append(makeRow(makeLeftCol(bold("XP Definition:"))+makeRightCol(getIntersectionParents(oboClass).toString())));
		}
		//what if parents/children were placed side-by-side???  might look nicer
		if (!oboClass.isObsolete()) {//don't really want to navigate around obs terms
			sb.append(makeRow("<tr><td colspan=2 align=center valign=top><font size=-1><hr></font></td></tr>"));
			sb.append(getParentalString(oboClass));
			sb.append(makeRow(""));
			sb.append(getChildrenString(oboClass));
			sb.append(getIntersectionChildren(oboClass)); //cross-products it is present in
		}
		String comments = oboClass.getComment();
		if ((comments != null) && !(comments.equals(""))) {
			sb.append(makeRow("<tr><td colspan=2 align=center valign=top><font size=-1><hr></font></td></tr>"));
			sb.append(makeRow(makeLeftCol(bold("Comments"))+makeRightCol(comments)));
		}
		//comment this out if you don't want to display the property values anymore.
		Set<PropertyValue> properties = oboClass.getPropertyValues();
		if (!properties.isEmpty()) {
			sb.append(makeRow("<tr><td colspan=2 align=center valign=top><font size=-1><hr></font></td></tr>"));
			sb.append(getPropertiesString(properties));
		}
		sb.append("</table>");
		return sb.toString();
	}

	//This method is important because after a conversion of OWL->OBO, many of the properties aren't 
	//assigned to specific OBO properties (like synonyms).  This allows all the class/annotation
	//properties to be displayed in the termInfo box.  They'll show up at the bottom for now.
	//Note:  right now these show up at least duplicated (if not quaduplicated)...but i think
	//that's an oboedit problem
	private static String getPropertiesString(Set<PropertyValue> properties) {
		StringBuffer sb = new StringBuffer();
		PropertyValue propVal;
		for (Iterator<PropertyValue> it = properties.iterator(); it.hasNext(); ) {
			propVal = it.next();
			sb.append(makeRow(makeLeftCol(bold(propVal.getProperty()+":"))+makeRightCol(propVal.getValue())));
		}
		return sb.toString();
	}
	
	private static String makeObsLinks(OBOClass oboClass) {
		//to display the replaced-bys and consider term links for obsoleted terms
		StringBuffer sb = new StringBuffer();
		Set<ObsoletableObject> obsReplacements = oboClass.getReplacedBy();
		StringBuffer replace = new StringBuffer();
		boolean replaceFlag = false;
		boolean considerFlag = false;
		ObsoletableObject obsObj;
		for (Iterator<ObsoletableObject> it = obsReplacements.iterator(); it.hasNext(); ) {
			obsObj = it.next();
			replaceFlag = true;
			if (obsObj!=null) {
				replace.append(termLink(obsObj)+"<br>");
			}
		}
		if (replaceFlag)			
			sb.append(makeRow(makeLeftCol(bold(italic("Replaced by:")))+makeRightCol(replace.toString())));

		Set<ObsoletableObject> obsConsiders = oboClass.getConsiderReplacements();
		StringBuffer considers = new StringBuffer();
		for (Iterator<ObsoletableObject> it = obsConsiders.iterator(); it.hasNext(); ) {
			obsObj = it.next();
			considerFlag = true;
			if (obsObj!=null) {
				considers.append(termLink(obsObj)+"<br>");
			}
		}
		if (considerFlag)
			sb.append(makeRow(makeLeftCol(bold(italic("Consider using:")))+makeRightCol(considers.toString())));	
		if (replaceFlag || considerFlag)
			return sb.toString();
		else
			return "";
	}	

	private static String makeDbxrefs(OBOClass oboClass) {
		//will display any dbxrefs.  
		//this will eventually resolve to clickable URL refs, but not yet
		Set<Dbxref> dbxrefs = oboClass.getDbxrefs();
		StringBuffer dbxrefString = new StringBuffer();
		boolean dbxrefFlag = false;
		if (dbxrefs != null) {
			Dbxref dbxrefObj;
			for (Iterator<Dbxref> it = dbxrefs.iterator(); it.hasNext(); ) {
				dbxrefObj = it.next();
				dbxrefFlag=true;
				if (dbxrefObj!=null) {
					dbxrefString.append(dbxrefObj.getDatabase()+":"+dbxrefObj.getID()+"<br>");
				}
			}
		}
		if (dbxrefFlag)
			return makeRow(makeLeftCol(bold("X-refs"))+makeRightCol(dbxrefString.toString()));			
		else	
			return "";
	}
	
	private static String makeSyns(boolean sortByScope, Set<Synonym> syns) {
		//This method creates a table of synonyms for a term, sorted by scope as
		//defined in the oboedit class Synonym
		//will expand this method to be able to sort by category, not just scope
		//kinda ugly right now...need to clean up.  
		StringBuffer sb = new StringBuffer();
		if (sortByScope) {
			Synonym syn;
			int broadSynCount = 0;
			StringBuffer broadBuf = new StringBuffer();
			int exactSynCount = 0;
			StringBuffer exactBuf = new StringBuffer();
			int narrowSynCount = 0;
			StringBuffer narrowBuf = new StringBuffer();
			int relatedSynCount = 0;
			StringBuffer relatedBuf = new StringBuffer();
			int unknownScopeCount = 0;
			StringBuffer unknownBuf = new StringBuffer();
			for (Iterator<Synonym> it = syns.iterator(); it.hasNext(); ) {
				syn = it.next();
				if (syn.getScope()==Synonym.BROAD_SYNONYM) {
					broadSynCount++;
					broadBuf.append(syn.toString());
					if (syn.getSynonymCategory()!=null) 
						broadBuf.append(italic(" ["+syn.getSynonymCategory().getName()+"]"));
					broadBuf.append("<br>");
				}
				else if (syn.getScope()==Synonym.NARROW_SYNONYM) {
					narrowSynCount++;
					narrowBuf.append(syn.toString());
					if (syn.getSynonymCategory()!=null) 
						narrowBuf.append(italic(" ["+syn.getSynonymCategory().getName()+"]"));
					narrowBuf.append("<br>");
				}
				else if (syn.getScope()==Synonym.EXACT_SYNONYM) {
					exactSynCount++;
					exactBuf.append(syn.toString());
					if (syn.getSynonymCategory()!=null) 
						exactBuf.append(italic(" ["+syn.getSynonymCategory().getName()+"]"));
					exactBuf.append("<br>");
				}
				else if (syn.getScope()==Synonym.RELATED_SYNONYM) {
					relatedSynCount++;
					relatedBuf.append(syn.toString());
					if (syn.getSynonymCategory()!=null) 
						relatedBuf.append(italic(" ["+syn.getSynonymCategory().getName()+"]"));
					relatedBuf.append("<br>");
				}
				else if (syn.getScope()==Synonym.UNKNOWN_SCOPE) {
					unknownScopeCount++;
					unknownBuf.append(syn.toString());
					if (syn.getSynonymCategory()!=null) 
						unknownBuf.append(italic(" ["+syn.getSynonymCategory().getName()+"]"));
					unknownBuf.append("<br>");
				}
			}
//			int totSyns = 0;
//			totSyns=broadSynCount+exactSynCount+narrowSynCount+relatedSynCount+unknownScopeCount;
//			if (totSyns>0) {
//			sb.append(makeRow(makeLeftCol(bold("Synonyms:"))+makeRightCol(temp.toString())));
//			}
			if (exactSynCount>0)
				sb.append(makeRow(makeLeftCol(bold("Exact Synonyms:"))+makeRightCol(exactBuf.toString())));
			if (broadSynCount>0)
				sb.append(makeRow(makeLeftCol(bold("Broad Synonyms:"))+makeRightCol(broadBuf.toString())));
			if (narrowSynCount>0)
				sb.append(makeRow(makeLeftCol(bold("Narrow Synonyms:"))+makeRightCol(narrowBuf.toString())));
			if (relatedSynCount>0)
				sb.append(makeRow(makeLeftCol(bold("Related Synonyms:"))+makeRightCol(relatedBuf.toString())));
			if (unknownScopeCount>0)
				sb.append(makeRow(makeLeftCol(bold("Other Synonyms:"))+makeRightCol(unknownBuf.toString())));
		}
		return sb.toString();
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

	private static String newLine() {
		if (DO_HTML) return "\n<br>";
		return "\n";
	}
	
	private static StringBuffer getIntersectionChildren(OBOClass oboClass) {
		StringBuffer sb = new StringBuffer();
		Collection<Link> children = oboClass.getChildren();
		sb.append(getLinksString(linksList(children, true, true), true, true));
		return (sb);
	}
	
	private static StringBuffer getIntersectionParents(OBOClass oboClass) {
		//these will be present if the term is a xp
		StringBuffer sb = new StringBuffer();
		Collection<Link> parents = oboClass.getParents();
		sb.append(xpDefs(linksList(parents,true,false)));
		return (sb);		
	}
	
	private static StringBuffer getParentalString(OBOClass oboClass) {
		Collection<Link> parents = oboClass.getParents();
		return (getLinksString(linksList(parents, false,false), false, false));
//		return getLinksString(parents,false, false);
	}

	private static StringBuffer getChildrenString(OBOClass oboClass) {
		Collection<Link> children = oboClass.getChildren();
		return (getLinksString(linksList(children, false,true), true, false));

//		return getLinksString(children,true, false);
	}


	
	private static class LinkCollection {
		//a little class to manage links for navigation.  
		//hope this isn't duplicating stuff
		//each link collection is for only one kind of relationship type
		private List<Link> links = new ArrayList<Link>();
		private String linkName;
		private OBOProperty linkType;
		private boolean xp;  			//true=this list is for cross-products

		public LinkCollection(Link link) {
			//creates the first link in a list.
			this.addLink(link);
			this.linkType = link.getType();
			this.linkName = link.getType().toString();
		}		
		public void setType(OBOProperty type) {
			linkType = type;
			linkName = type.toString();
		}
		public OBOProperty getType() {
			return linkType;
		}
		public String getLinkName() {
			return linkName;
		}
		public void setXP(boolean flag) {
			xp = flag;			
		}
		public boolean getXP() {
			return xp;
		}
		public List<Link> getLinks() {
			return links;
		}
		public Link get(int i) {
			return links.get(i);
		}
		public void addLink(Link link) {
			links.add(link);
		}
		public void setLinks(List<Link> newLinks) {
			links = newLinks;
		}
		public int size() {
			return links.size();
		}
	}
	
	private static List<LinkCollection> linksList(Collection<Link> links, boolean xp, boolean isChild) {
		//given a collection of oboclass links, this processes the collection to
		//separate out the links and group by releationship type+parent/child+xp state
		HashSet<OBOProperty> relSet= new HashSet<OBOProperty>();
		List<LinkCollection> allLinks = new ArrayList<LinkCollection>();
		for (Iterator<Link> it = links.iterator(); it.hasNext(); ) {
			Link link = (Link)it.next();
			if (((OBORestriction)link).completes()==xp) {
				//only add to links list those that match the desired xp state
				OBOProperty type = link.getType();
				if (!relSet.contains(type)) {
						relSet.add(type);
					LinkCollection linkSet = new LinkCollection(link);
					if (type.equals(OBOProperty.IS_A))
						allLinks.add(0,linkSet);
					else
						allLinks.add(linkSet);
				} else {
					for (ListIterator<LinkCollection> lit=allLinks.listIterator();lit.hasNext();) {
						LinkCollection temp = (LinkCollection)lit.next();
						if (temp.get(0).getType()==type) {
							temp.addLink(link);
							allLinks.set(lit.nextIndex()-1, temp);
						}
					}				
				}
			}
		}
		return allLinks;
	}
	private static StringBuffer getLinksString(List<LinkCollection> allLinks, boolean isChild, boolean xp) {
		/* This functions creates/groups the tabular entries for parents/children 
		 * ontology links for term info.  This has the caveat that if there are
		 * >1 is_a xp genus terms, i think the xp display will be screwy 
		 */
		StringBuffer sb = new StringBuffer();
		for (ListIterator<LinkCollection> lit=allLinks.listIterator();lit.hasNext();) {
			//for each relationship type
			LinkCollection temp = (LinkCollection)lit.next();
			String tempType = temp.getLinkName();
			List<Link> links = temp.getLinks();
			StringBuffer tempSB = new StringBuffer();
			for (Iterator<Link> it = links.iterator(); it.hasNext(); ) {
				//create all the clickable links first
				Link link = (Link)it.next();
				appendLink(tempSB,isChild,link,xp);
			}
			if (temp.getLinkName().contains("is_a")) {
				//add in the relationship type for lhs
				if (isChild) {
					if (xp) {
						tempType = "Crossed in";
					} else {
						tempType = "Subclass";
					}
				} else {
					if (xp) {
						tempType = "Genus";
					} else {
						tempType = "Superclass";
					}
				}
				//put it at the front of the list if it isn't already there!
				sb.insert(0,makeRow(makeLeftCol(bold(italic(tempType))) + 
						makeRightCol(tempSB.toString())));

			} else { //not an is_a
				if (xp) {
					tempType="Differentia<br> ("+tempType+")";
				}
				if (tempType.contains("part")) {
					if (isChild) {
						tempType = "Has part";
					}
				}
				if (tempType.contains("from")) {
					if (isChild) {
						tempType = tempType.replaceAll("from","into");
					}
				}
				if (tempType.endsWith("of")) {
					if (isChild)
					{//change x_of -> has_x
						tempType = tempType.replaceAll("_*of", "");
						tempType="has "+tempType;
						}
					}
				//add the table row 
				sb.append(makeRow(makeLeftCol(bold(italic(tempType))) + 
						makeRightCol(tempSB.toString())));
			} 			
		}
		return sb;
	}
	
	private static StringBuffer xpDefs (List<LinkCollection> allLinks) {
		/* This functions creates/groups the tabular entries for parents/children 
		 * ontology links for term info, adds line breaks for isa 
		 */
		StringBuffer sb = new StringBuffer();
		String genusLink = "";
		String differentiaLink = "";
		for (ListIterator<LinkCollection> lit=allLinks.listIterator();lit.hasNext();) {
			LinkCollection temp = lit.next();
			List<Link> links = temp.getLinks();
			StringBuffer tempSB = new StringBuffer();
			for (Iterator<Link> it = links.iterator(); it.hasNext(); ) {
				Link link = it.next();
				appendLink(tempSB,false,link,true);
			}
			if (temp.getLinkName().contains("is_a")) {
				genusLink = tempSB.toString();
			}
			else { differentiaLink = tempSB.toString();
			sb.append(genusLink +" "+ temp.getLinkName()+" "+differentiaLink+"<br>");
			}		
		}
		return sb;
	}

	private static void appendLink(StringBuffer sb, boolean isChild, Link link, boolean xp) {
		IdentifiedObject term;
		if (isChild)
			term = link.getChild();
		else
			term = link.getParent();
		sb.append(termLink(term)+" ");
		if (!xp || (isChild&&xp))
			sb.append("<br>");
	}

	public static String termLink(IdentifiedObject term) {
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
////this is Mark's old termInfo box.  I'll keep it around
////just in case we need to go back to it.  note that it might not
////work quite right because i've now changed the other functions it
////calls.
//public static String termInfoOld(OBOClass oboClass) {
//if (oboClass == null) {
//	System.out.println("null obo class for HtmlUtil.termInfo");
//	return ""; // null? exception?
//}
//StringBuffer sb = new StringBuffer();
//sb.append(bold("TERM: ")).append(oboClass.getName());
//if (oboClass.isObsolete()) {
//	sb.append("This term is OBSOLETE").append(newLine());
//
//
//}		
//sb.append(nl()).append(bold("ID: ")).append(colorFont(oboClass.getID(), "red"));
//
//Set syns = oboClass.getSynonyms();
//for (Iterator it = syns.iterator(); it.hasNext(); ) {
//	sb.append(newLine()).append(bold("Synonym: ")).append(it.next());
//}
//
//String definition = oboClass.getDefinition();
//// definition = lineWrap(definition);
//if (definition != null && !definition.equals(""))
//	sb.append(nl()).append(nl()).append(bold("Definition: ")).append(definition);
//
//sb.append(nl()).append(nl()).append(bold("CHILDREN: "));
//sb.append(getChildrenString(oboClass));
//
//
//// if (DEBUG) System.out.println(sb);
//
//return sb.toString();
//}


//private static StringBuffer getLinksString(Collection links, boolean isChild, boolean xp) {
//StringBuffer sb = new StringBuffer();
//// or should thi sjust be done more generically with a hash of string bufs
//// for each unique link type name?
//// YES...that would be a better method i think -nlw
////method:  make all the rt hand columns, then wrap around the left col of the type for the table entry
//StringBuffer isaStringBuf = new StringBuffer();
//StringBuffer partofStringBuf = new StringBuffer();
//StringBuffer haspartStringBuf = new StringBuffer();
//StringBuffer devFromStringBuf = new StringBuffer();
//StringBuffer otherStringBuf = new StringBuffer();
//StringBuffer startStageStringBuf = new StringBuffer();
//StringBuffer endStageStringBuf = new StringBuffer();
//int countIsa=0;
//int countPartof=0;
//int countHaspart=0;
//int countDevfrom=0;
//int countStartStage=0;
//int countEndStage=0;    
//int countOther=0;
////i would like to clean this up to be able to group all relationships together, despite
////the fact that they could be non-standard, like in SO.  might need two for-loops.  
////would this be a drain on space/time?
//for (Iterator it = links.iterator(); it.hasNext(); ) {
//Link link = (Link)it.next();
//OBOProperty type = link.getType();
//if (type.getName().equals("is_a")) {
//	countIsa++;
//	appendLink(isaStringBuf,isChild,link,xp);
//}
//else if (type.getName().equals("part of") || type.getName().equals("part_of")) {	  
//	countPartof++;
//	appendLink(partofStringBuf,isChild,link,xp);
//}
//else if (type.getName().equals("has part") || type.getName().equals("has_part")) {
//	//i'm using this to catch the wierd reciprocal stuff in FMA
//	//not sure yet if this is the best solution
//	//probably need a new way to visualize reciprocal 'part' relationships
//	countHaspart++;
//	appendLink(haspartStringBuf,isChild,link,xp);
//}	  
//else if (type.getName().equals("develops from") || type.getName().equals("develops_from")) {
//	countDevfrom++;
//	appendLink(devFromStringBuf,isChild,link,xp);
//}
////else if (type.getName().equals("start stage") || type.getName().equals("start_stage")) {
////countStartStage++;
////appendLink(startStageStringBuf,isChild,link);
//
////}
////else if (type.getName().equals("end stage") || type.getName().equals("end_stage")) {
////countEndStage++;
////appendLink(endStageStringBuf,isChild,link);
////}
//else { //catch all other relationships
//	countOther++;
//	otherStringBuf.append("<tr>");
//	otherStringBuf.append(makeLeftCol(bold(italic(capitalize(type.getName()))))+"<td>");
//	appendLink(otherStringBuf,isChild,link,xp);
//	otherStringBuf.append("</td></tr>");
//}
//}
//if (!xp) {
//if (countIsa>0) 
//	sb.append(makeRow(makeLeftCol(bold(italic( 
//			isChild ? "Subclass (is_a)" : "Superclass (is_a)"))) + 
//			makeRightCol(isaStringBuf.toString())));
//if (countPartof>0)
//	sb.append(makeRow(makeLeftCol(bold(italic( 
//			isChild ? "Has part" : "Part of"))) + 
//			makeRightCol(partofStringBuf.toString())));
//if (countDevfrom>0)
//	sb.append(makeRow(makeLeftCol(bold(italic( 
//			isChild ? "Develops into" : "Develops from"))) +
//			makeRightCol(devFromStringBuf.toString())));   	
//if (countOther>0)
//	sb.append(makeRow(makeLeftCol(otherStringBuf.toString())));
//} else {
//if (countIsa>0) {
//	sb.append(makeRow(makeLeftCol(bold(italic( 
//			"genus:"))) + 
//			makeRightCol(isaStringBuf.toString())));
//} else {
//	sb.append(makeRow(makeLeftCol(bold(italic( 
//			"differentia: has_part"))) + 
//			makeRightCol(partofStringBuf.toString())));
//	sb.append(makeRow(makeLeftCol(bold(italic(
//	"differentia: develops_from"))) +
//			makeRightCol(devFromStringBuf.toString())));
//	sb.append(makeRow(makeLeftCol(otherStringBuf.toString())));
//}
//}
//return sb;
//}
