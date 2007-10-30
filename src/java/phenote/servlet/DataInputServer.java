package phenote.servlet;

//import org.mortbay.http.HttpContext;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.ServletHandler;

/** this class kicks off the servlet. modeled after igb's UnibrowControlServer 
 * maybe this should go in dataadapter? as its gonna end up loading new data 
 * actually for now it just scrolls/zooms to range
 remodeled after org.mortbay.jetty.example.MinimalServlets */

public class DataInputServer {

  // -----------------------------------------------------------------------
  // Class/static variables
  // -----------------------------------------------------------------------

  protected final static Logger logger = LogManager.getLogger(DataInputServer.class);

  private static final int DEFAULT_SERVER_PORT = 8080; // igb is 7085
  private static final String SERVLET_NAME = "phenote";
  
  public DataInputServer() {

    // for now just setting port to default - eventually should test if port
    // is free, and if not try other ports (like igb does)
    int serverPort = DEFAULT_SERVER_PORT;
    
    //HttpServer httpServer = new HttpServer();
    Server server = new Server();
    // Create a port listener
    //SocketListener listener = new SocketListener();
    Connector connector = new SocketConnector();
    //listener.setPort(serverPort);
    connector.setPort(serverPort);
    //httpServer.addListener(listener);
    server.setConnectors(new Connector[]{connector});
    // Create a servlet container
    ServletHandler handler = new ServletHandler();
    //context.addHandler(servlets);
    server.setHandler(handler);
    String servletClassName = "phenote.servlet.DataInputServlet";
    handler.addServletWithMapping(servletClassName, "/"+SERVLET_NAME); // phenote? phenote-input?
    
    // Map a servlet onto the container
//     ServletHolder sholder = handler.addServlet(SERVLET_NAME, "/"+SERVLET_NAME+"/*",
//                                                 servletClassName);
//     sholder.setInitOrder(1);
    // Create a context
    //HttpContext context = new HttpContext();  ????
    //context.setContextPath("/");
    //httpServer.addContext(context); // ???
    
    String s =  "http://localhost:"+serverPort+"/"+SERVLET_NAME;
    logger.debug("http server starting at "+s);
    System.out.println("http server starting at "+s);
    try {
      // Start the http server
      server.start(); // throws MultiException
      // this doesnt return - stays in it
      server.join();
   }
    catch (Exception e) {
      logger.debug("http server wont start", e);
      System.out.println("http server wont start"+ e);
    }

    //logger.debug("http server started at "+s);
    //ystem.out.println("http server started at "+s);
    //DataInputServlet smartAtlasController = 
    // (DataInputServlet)sholder.getServlet();
  }

}
