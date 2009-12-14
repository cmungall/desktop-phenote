package phenote.config;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;

import phenote.config.Preferences;

/** Adapted from apollo */

public class ProxyDialog extends JDialog implements ActionListener {
  String proxyHost;
  int proxyPort;
  String userName;
  String userPassword;
  boolean hasAuthentication = false;
  boolean hasProxy = false;

  JTextField server;
  JTextField port;
  JTextField name;
  JPasswordField password;
  JCheckBox  authBox;
  JCheckBox  proxyBox;
  JPanel     authP;
  JButton    ok;
  JButton    cancel;
  private ButtonGroup buttonGroup = new ButtonGroup();
  JRadioButton httpButton = new JRadioButton();
  JRadioButton socksButton = new JRadioButton();
  String proxyProtocol;

	//initialize logger
	protected final static Logger LOG = Logger.getLogger(ProxyDialog.class);

  /**
   * Use this construtor when you don't need to call back from
   * the dialog to its parent after the user has made a choice.
  **/
  public ProxyDialog(JFrame frame) {
    super(frame,"Proxy Settings",true);

    // See if there's a system proxy set
    String setStr = System.getProperty("http.proxySet");
    if (setStr == null)
      setStr = System.getProperty("proxySet");
    if (setStr != null)
      hasProxy = new Boolean(setStr).booleanValue();

    // See if user saved a proxy--if so, it overrides the one defined by the system.
    Preferences prefs = Preferences.getPreferences();
    if (prefs.getProxyHost() != null) {
	    hasProxy = prefs.getProxyIsSet();
	    proxyHost = prefs.getProxyHost();
	    proxyPort = prefs.getProxyPort();
	    proxyProtocol = prefs.getProxyProtocol();
	    if (proxyProtocol != null && proxyProtocol.equals("SOCKS")) {
		    socksButton.setSelected(true);
		    httpButton.setSelected(false);
	    } else {
		    socksButton.setSelected(false);
		    httpButton.setSelected(true);
	    }
	    LOG.info("Restoring saved proxy " + proxyProtocol + ": " + proxyHost + ":" + proxyPort);
    }
    else {
	    // See if there is a proxy defined on this system--if so, pre-set the server and port fields.
	    findSystemProxy();
	    proxyHost = getSystemProxyHost();
	    proxyPort = getSystemProxyPort();
    }

    init();
    if (frame==null) {
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      setLocation((screenSize.width - getSize().width) / 2,
                  (screenSize.height - getSize().height) / 2);
    }
  }

  public void actionPerformed(ActionEvent evt) {
    if (evt.getSource() == authBox) {
      hasAuthentication = authBox.isSelected();
      authP.setVisible(authBox.isSelected());
      name.setEnabled(hasAuthentication && hasProxy);
      password.setEnabled(hasAuthentication && hasProxy);
      pack();
    } else if (evt.getSource() == proxyBox) {
      hasProxy = proxyBox.isSelected();
      server.setEnabled(hasProxy);
      port.setEnabled(hasProxy);
      httpButton.setEnabled(hasProxy);
      socksButton.setEnabled(hasProxy);
      name.setEnabled(hasAuthentication && hasProxy);
      password.setEnabled(hasAuthentication && hasProxy);
      authBox.setEnabled(hasProxy);
    } else if (evt.getSource() == ok) {
      commitIfComplete();
    } else if (evt.getSource() == cancel) {
      setVisible(false);
      dispose();
    }
  }

  private void commitIfComplete() {
    if (proxyBox.isSelected()) {
      if (!server.getText().equals("") && !port.getText().equals("")) {
        LOG.debug("ProxyDialog:  user set proxy host to " + server.getText() + ", port to " + port.getText());
        try {
          Integer.parseInt(port.getText(),10);
        } catch (Exception e) {
          JOptionPane.showMessageDialog(this,"Port is not an integer","Error",JOptionPane.ERROR_MESSAGE);
          return;
        }
        if (hasAuthentication) {
          if (name.getText().equals("") || password.getText().equals("")) {
            JOptionPane.showMessageDialog(this,
                                          "You need to specify both name and password for authentication",
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
            return;
          }
          String authStr = name.getText() + ":" + password.getText();
//          Base64Encoder encoder = new Base64Encoder(authStr);
//          URLConnection.setDefaultRequestProperty("Proxy-Authorization","Basic " + encoder.processString());
//          LOG.debug("Processed string = " + encoder.processString());
	}
	
	proxyProtocol = (httpButton.isSelected() ? "HTTP" : "SOCKS");
        Properties props = System.getProperties();
	props.put("proxyProtocol", proxyProtocol);
	if (proxyProtocol.equals("HTTP")) {
		props.put("http.proxySet","true");
		props.put("http.proxyHost",server.getText());
		props.put("http.proxyPort",port.getText());
	}
	else {
		props.put("proxySet","true");
		props.put("proxyHost",server.getText());
		props.put("proxyPort",port.getText());
	}

	Preferences prefs = Preferences.getPreferences();
	prefs.setProxyIsSet(true);
	prefs.setProxyHost(server.getText());
	if (port.getText() != null && !port.getText().equals("")) 
		prefs.setProxyPort(Integer.parseInt(port.getText()));
	prefs.setProxyProtocol(proxyProtocol);

      } else {
        JOptionPane.showMessageDialog(this,"You need to specify both server and port","Error",JOptionPane.ERROR_MESSAGE);
        return;
      }
    } else {  // Proxy unset
      Properties props = System.getProperties();
      props.put("http.proxySet","false");
      props.put("proxySet","false");
      props.remove("http.proxyHost");
      props.remove("http.proxyPort");
      props.remove("proxyHost");
      props.remove("proxyPort");
      props.remove("proxyProtocol");
      Preferences prefs = Preferences.getPreferences();
      prefs.setProxyIsSet(false);
      prefs.setProxyHost(server.getText());
      if (server.getText().equals("")) {
	      prefs.setProxyPort(0);
	      prefs.setProxyProtocol("HTTP"); // go back to the default
      }
    }

    try {
	    Preferences.writePreferences(Preferences.getPreferences());
    } catch (IOException e) {
	    LOG.info("Error trying to write preferences");
	    e.printStackTrace();
    }
    setVisible(false);
    dispose();
  }

  public void init() {

    server   = new JTextField(30);
    server.setText(proxyHost);
    port     = new JTextField(6);
    port.setText(Integer.toString(proxyPort));
    server.setEnabled(hasProxy);
    port.setEnabled(hasProxy);
    
    httpButton.setText("HTTP");
    httpButton.setEnabled(hasProxy);
    buttonGroup.add(httpButton);
    socksButton.setText("SOCKS");
    socksButton.setEnabled(hasProxy);
    buttonGroup.add(socksButton);
    JPanel buttonGroupPanel = new JPanel();
    buttonGroupPanel.add(httpButton);
    buttonGroupPanel.add(socksButton);
    if (!httpButton.isSelected() && !socksButton.isSelected())
	    httpButton.setSelected(true);  // default is HTTP

    authBox  = new JCheckBox();
    authBox.setSelected(hasAuthentication);
    authBox.setEnabled(hasProxy);
    authBox.addActionListener(this);
    proxyBox  = new JCheckBox();
    proxyBox.setSelected(hasProxy);
    proxyBox.addActionListener(this);
    name     = new JTextField(30);
//    name.setText(UserName.getUserName());
    password = new JPasswordField(30);
    name.setEnabled(hasAuthentication && hasProxy);
    password.setEnabled(hasAuthentication && hasProxy);

    JPanel mainP = new JPanel();
    mainP.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    //gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.gridwidth = 1;
    mainP.add(new JLabel("Use proxy"),gbc);
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    mainP.add(proxyBox,gbc);
    gbc.gridwidth = 1;
    mainP.add(new JLabel("Server"),gbc);
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    mainP.add(server,gbc);
    gbc.gridwidth = 1;
    mainP.add(new JLabel("Port"),gbc);
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    mainP.add(port,gbc);
    gbc.gridwidth = 1;

    mainP.add(new JLabel("Protocol"),gbc);
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    mainP.add(buttonGroupPanel,gbc);
    gbc.gridwidth = 1;

    mainP.add(new JLabel("Use authentication"),gbc);
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    mainP.add(authBox,gbc);
    authP = new JPanel();
    authP.setLayout(new GridBagLayout());
    gbc.gridwidth = 1;
    authP.add(new JLabel("Username"),gbc);
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    authP.add(name,gbc);
    gbc.gridwidth = 1;
    authP.add(new JLabel("Password"),gbc);
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    authP.add(password,gbc);


    JPanel enclosureP = new JPanel();
    enclosureP.setLayout(new BorderLayout());
    enclosureP.add(mainP,BorderLayout.CENTER);
    enclosureP.add(authP,BorderLayout.SOUTH);

    ok = new JButton("OK");
    ok.addActionListener(this);
    cancel = new JButton("Cancel");
    cancel.addActionListener(this);
    JPanel buttonP = new JPanel();
    buttonP.add(ok);
    buttonP.add(cancel);

    getContentPane().add(enclosureP,BorderLayout.CENTER);
    getContentPane().add(buttonP,BorderLayout.SOUTH);
    authP.setVisible(false);
    pack();
  }

  public static void main(String [] args) {
    final JFrame frame = new JFrame();
    JButton show = new JButton("Show");
    JButton get = new JButton("Get");

    show.addActionListener(new ActionListener() {
                             public void actionPerformed(ActionEvent evt) {
                               ProxyDialog pd = new ProxyDialog(frame);
                               pd.setVisible(true);
                             }
                           }
                          );

    frame.getContentPane().setLayout(new GridLayout(2,1));
    frame.getContentPane().add(show);
    frame.pack();
    frame.setVisible(true);
  }

	public static boolean proxyIsSet() {
		if ((System.getProperty("http.proxySet") == null ||
		     System.getProperty("http.proxySet").equals("false")) &&
		    (System.getProperty("proxySet") == null ||
		     System.getProperty("proxySet").equals("false")))
			return false;
		else
			return true;
	}

	public static String getSystemProxyHost() {
		if (!proxyIsSet())
			return null;

		String proxyHost = System.getProperty("http.proxyHost");
		if (proxyHost == null)
			proxyHost = System.getProperty("proxyHost");
		LOG.debug("getSystemProxyHost: " + proxyHost);  // DEL
		return proxyHost;
	}
	public static int getSystemProxyPort() {
		String proxyPortString = System.getProperty("http.proxyPort");
		if (proxyPortString == null)
			proxyPortString = System.getProperty("proxyPort");
		if (proxyPortString != null) {
			int port = (int) Integer.parseInt(proxyPortString);
			return port;
		}
		else
			return 0;
	}

	/** If there is a proxy defined in the system, save the host and port as system properties */
	private void findSystemProxy() {
		System.setProperty("java.net.useSystemProxies","true");
		String url="http://www.yahoo.com/";
		try {
			Properties props = System.getProperties();
			List l = ProxySelector.getDefault().select(new URI(url));
			for (Iterator iter = l.iterator(); iter.hasNext(); ) {
				Proxy proxy = (Proxy) iter.next();
				InetSocketAddress addr = (InetSocketAddress)proxy.address();
				if(addr == null) {
					LOG.debug("No system proxy defined.");
				} else {
					LOG.debug("Found system proxy of type " + proxy.type());
					LOG.debug("Found proxy hostname : " + addr.getHostName());
					proxyHost = addr.getHostName();
					props.put("http.proxyHost",proxyHost);
					LOG.debug("proxy port : " + addr.getPort());
					proxyPort = (int) Integer.parseInt(String.valueOf(addr.getPort()));
					props.put("http.proxyPort",String.valueOf(addr.getPort()));
				}
			}
		} catch (Exception e) { LOG.info("Exception trying to find proxy: " + e.getMessage()); }
	}
}
