package phenote.dataadapter;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Observable;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;

import org.apache.log4j.Logger;


public class Image extends Observable {

	private static final Logger LOG = Logger.getLogger(Image.class);

  private String id;  //how to make this unique?
  private String title;  // this is a nice readable title
  private URL url;  //if located external to the local filesystem
  private String filename;  //if located on the local filesystem
  private String timestamp;
  private String description;
	private BufferedImage image;

	//other things to add in time:
	//*information about image (date, resolution, etc)
	//*experimental information for how image was captured?
	private Vector<Polygon> polys ;
	private Vector<Shape> regions;

	//*ROIs, if defined
	//*collection information, if part of a collection	

  
  /**
   * Create a new Image with the given ID.
   * This ID should be unique and cannot be changed.
   */
  public Image() { }
  
  public Image(URL u) {
  	setImage(u);
  	id = u.toString();
  }
  
  public Image(String f) {
  	setImage(f);
  	id = f;
  	setROIs();
  }

  public void setTitle(String t) {
  	title = t;
  }

  public String getTitle() {
    return title;
  }
  
  public String getId() {
    return id;
  }
  
  public BufferedImage getImage() {
  	return image;
  }
  
  public void setImage(URL u) {
		try {
		   image = ImageIO.read(u);
			} catch (IOException e) { LOG.error(e.getMessage()); }
			url = u;
			setTitle(url.getFile());
  }
  
  public void setImage(String imagePath) {
		File f = new File(imagePath);
  	try {
			image = ImageIO.read(f);
			} catch (IOException e) { LOG.error(e.getMessage()); }
			filename = imagePath;
			setTitle(f.getName());  //by default, the title of the image will be the filename.
			//setFileTimestamp();
  }
  
  public String getFileTimestamp() {
  	return timestamp;
  }

  private void setFileTimestamp() {
  	String out="";
  	try {
  		Runtime runtime = Runtime.getRuntime();
    	BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
    	Process output = runtime.exec("cmd /c dir "+filename);
    	BufferedReader bufferedReader = new BufferedReader (new 
    			InputStreamReader(output.getInputStream()));
    	String line = null;
    	
    	int step=1;
    	while((line = bufferedReader.readLine()) != null ) {
    		if(step==6) {
    			out=line;
    		}	
    		step++;
    	}               
    	try { 
    		out=out.replaceAll(" ","");
    	}
    	catch(Exception se) {
    		LOG.error(se.getMessage());
    	}
    }
    catch(Exception e){
    	LOG.error(e.getMessage());
    }
  }
  
  public Vector<Shape> getROIs() {
  	return regions;
  }
  
  public void setROIs() {
  	//this will eventuall be used as a proper setter when ROIs are read 
  	//from the accompanying xml file.  for now, dummy values
  	this.regions = new Vector<Shape>();
  	Shape circle = new Ellipse2D.Float(200.0f, 70.0f, 50.0f, 20.0f);
    Shape square = new Rectangle2D.Double(100,200,30, 30);
		int[] x;
		int[] y;
		x = new int[]{3,48,92,49,2};
		y = new int[]{32,31,143,169,110};    
    Shape poly = new Polygon(x,y,x.length);
		regions.add(circle);
    regions.add(square);
    regions.add(poly);
  }
  
  public Vector<Polygon> getPolys() {
  	return polys;
  }

  public void setPolys() {
  	//this has a dummy set of ROIs
  	this.polys = new Vector<Polygon>();
		int[] x;
		int[] y;
		x = new int[]{3,48,92,49,2};
		y = new int[]{32,31,143,169,110};
		polys.add(new Polygon(x,y,x.length));

//		x = new int[]{50,132,139,93};
//		y = new int[]{33,33,131,143};
//		polys.add(new Polygon(x,y,x.length));

//		x = new int[]{134,197,195,141};
//		y = new int[]{33,33,131,131};
//		polys.add(new Polygon(x,y,x.length));

  }

}
