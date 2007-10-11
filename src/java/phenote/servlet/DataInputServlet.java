package phenote.servlet;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterIFactory;
import phenote.datamodel.TermNotFoundException;
import phenote.edit.EditManager;


public class DataInputServlet extends HttpServlet {

  //private static final String CONFIG_FILE_PARAM = "configuration-file";
  // PhenoteServlet.class is the distinct name for this logger
  private static final Logger LOG = Logger.getLogger(DataInputServlet.class);

  public void doGet(final HttpServletRequest request, final HttpServletResponse response)
    throws IOException, ServletException {
    System.out.println("got get" +request);
    LOG.debug("servlet doGet " + new Date());
    // need to interact with the application on the Swing thread
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        CharacterI ch = CharacterIFactory.makeChar();
        Enumeration<?> e = request.getParameterNames();
        while (e.hasMoreElements()) {
          Object o = e.nextElement();
          LOG.debug("param "+o);
          // test if instance of String?
          String field = (String)o;
          String value = request.getParameter(field);
          try {
            ch.setValue(field,value);
          }
          catch (CharFieldException cfe) {
            LOG.error("field not found "+cfe);
          }
          catch (TermNotFoundException tnfe) {
            LOG.error("term not found in field "+field+" "+tnfe);
          }
        }
        EditManager.inst().addCharacter(ch);
      }
    });
  }

  /**
   * if <load-on-startup>1</load-on-startup> is in web.xml then init will
   * happen when web server started (or if code recompiled) - so this is where
   * the ontology reading & caching goes
   */
  public void init() throws ServletException {
    //initDate = new Date();
    super.init();
    // makes links for term info - put this method in Phenote?
    //HtmlUtil.setStandAlone(false);
    //phenote = Phenote.getPhenote();
    // from web.xml
    //String configFile = getInitParameter(CONFIG_FILE_PARAM);
    // just in case not in web.xml
    //if (configFile == null || configFile.equals(""))
    //  configFile = "/birn2.cfg";
    //boolean DEBUG = true;  if (DEBUG) configFile = "/fiddle.cfg";
//     try {
//       Config.inst().setConfigFile(configFile); // causes parse of file
//     } catch (ConfigException e) {
//       String m = "Error in config file: " + configFile + " " + e.getMessage();
//       LOG.error(m);
//       throw new ServletException(m); // ??
//     }
    // cheesy - revisit
    //String[] args = {"-c","initial-zfin.cfg"};
    //phenote.initConfig(args); // hardwire for now to zfin
    //phenote.initOntologies();
    // loads ontologies up - non intuitive?
//    OntologyDataAdapter.getInstance();
        // this is not running as a separate thread - investigate
//     System.out.println("ontologies loaded - starting file checking thread");
//     OntologyFileCheckThread ofct = new OntologyFileCheckThread();
//     ofct.start(); // is this not running as threaded?
//     System.out.println("file thread launched - moving on");
    }


//     public void doPost(HttpServletRequest request, HttpServletResponse response)
//             throws IOException, ServletException {

//         // dont know where this goes???
//       System.out.println("got post" +request);
//         LOG.debug("servlet doPost " + new Date());
//         LOG.info("is term comp request: " + isTermCompletionRequest(request));

//         PrintWriter out = response.getWriter();

//         if (isTermCompletionRequest(request)) {
//             String userInput = getTermCompletionParam(request);
//             LOG.info("ontology? " + getOntologyParamString(request) + " param entityInput? " + getTermCompletionParam(request));
//             response.setContentType("text/html");
//             //out.println("Content-Type: text/html; charset=ISO-8859-1"); // this messes things up
// //       String list = "<ul><li onmouseover=\"set_ontology()\" id=\"termId\" "+
// //         "onclick=\"set_ontology()\">"+userInput+"</li>\n"+
// //         "<li onmouseover=\"set_ontology()\" id=\"termId\" onclick=\"set_ontology()\">"+
// //         "test</li>\n<li onmouseover=\"set_ontology()\" id=\"termId\" onclick=\"set_ontology()\">dude</li></ul>";
//             String ontol = getOntologyParamString(request);
//             String field = getFieldParamString(request);
//             String s = "";
//             LOG.debug("printing to response writer: " + substring(s, 55) + "...");
//             out.println(s);
//         }

//     }

    private boolean isTermCompletionRequest(HttpServletRequest req) {
        return getTermCompletionParam(req) != null;
    }

    /**
     * this should be renamed from unintuitive "ontologyname"
     */
    private String getTermCompletionParam(HttpServletRequest req) {
        String par = req.getParameter("userInput"); // new way
        if (par == null)
            par = req.getParameter("qualityInput"); // for now - pase i think
        if (par == null)
            par = req.getParameter("entityInput");
        return par;
    }

    private String getOntologyParamString(HttpServletRequest req) {
        return req.getParameter("ontologyName");
    }

  /** field param string specifies what gui field the request came from - this is 
      used for UseTermInfo button to populate field from term info */
  private String getFieldParamString(HttpServletRequest req) {
    return req.getParameter("field");
  }




    private String substring(String s, int sz) {
        sz = (s.length() <= sz) ? s.length() : sz;
        return s.substring(0, sz);
    }

//     private boolean isTermInfoRequest(HttpServletRequest req) {
//         return getTermIdFromTermInfoRequest(req) != null;
//     }

//     private String getTermIdFromTermInfoRequest(HttpServletRequest req) {
//         return req.getParameter("termId");
//     }




//   private static StringBuffer fn(String fnName, String[] params) {
//     return HtmlUtil.fn(fnName,params);
//   }
//     StringBuffer s = new StringBuffer(fnName).append("(").append(q(params[0]));
//     for (int i=1; i<params.length; i++) s.append(",").append(q(params[i]));
//     s.append(")");    return s;   }

  private static StringBuffer dq(StringBuffer sb) {
    return new StringBuffer("\""+sb+"\"");
  }

  private static StringBuffer q(StringBuffer sb) {
    return new StringBuffer("'"+sb+"'");
  }

  private static String q(String s) {
    return "'"+s+"'";
  } 


}

