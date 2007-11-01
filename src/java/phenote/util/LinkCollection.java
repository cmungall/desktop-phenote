package phenote.util;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Collections;



import org.geneontology.oboedit.datamodel.Link;
import org.geneontology.oboedit.datamodel.OBOProperty;

public class LinkCollection {
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
		this.linkName = link.getType().getName();
	}		
	public void setType(OBOProperty type) {
		linkType = type;
		linkName = type.getName();
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
	public List<Link> sortedLinks() {
		Collections.sort((List)links);
		return links;
	}
}