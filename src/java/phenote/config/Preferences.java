package phenote.config;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.bbop.util.CollectionUtil;
import org.bbop.util.MultiProperties;

/** Adapted from OBO-Edit */

public class Preferences {

	//initialize logger
	protected final static Logger LOG = Logger.getLogger(Preferences.class);

	// A lot of this stuff is relics from OBO-Edit that could probably be removed.
	// I'm leaving some of it here in case we want to add some of these preferences to Phenote.
	protected Font font;

	protected Color backgroundColor = null;

	protected Color buttonColor = null;

	protected Color selectionColor = null;

	protected Color lightSelectionColor = null;

	protected Color orange = new Color(255, 153, 0);
	protected Color purple = new Color(102, 0, 204);

	protected boolean confirmOnExit = true;

	protected boolean autosaveEnabled = true;

	protected boolean caseSensitiveSort = false;

	protected boolean showToolTips = true;

	protected boolean warnBeforeDelete = true;

	protected boolean allowExtendedCharacters = false;

	protected String browserCommand = "";

	protected String userName = System.getProperty("user.name");

	protected String fullName = "";

	protected String email = "";

	protected int autosaveWaitTime = 20;

	protected int autosaveExpirationDays = 7;

	protected boolean autoCommitTextEdits = false;

	protected boolean warnBeforeDiscardingEdits = true;

	protected String defaultDef;

	protected boolean usePersonalDefinition = false;

	protected boolean useModalProgressMonitors = !System.getProperty(
			"useModalProgressMonitors", "true").equals("false");

	protected static String appName = "Old Phenote"; // PhenotePlus now returns "Phenote" (1/7/2010)

	protected static boolean batchMode = false;  // true for applications like obo2obo that don't have a gui, false for OBO-Edit and Phenote

	protected static File installationDir;

	protected File autosavePath;

	protected String logFile = "";

	protected boolean proxyIsSet = false;
	protected String proxyHost = null;
	protected int proxyPort;
	protected String proxyProtocol = null;

	protected static Preferences preferences;

	public Preferences() {
	}

	public static Preferences getPreferences() {
		if (preferences == null) {
			XMLDecoder d;
			try {
				d = new XMLDecoder(new BufferedInputStream(new FileInputStream(
						Preferences.getPrefsXMLFile())));
				Preferences p = (Preferences) d.readObject();
				preferences = (Preferences) p;
				d.close();
			} catch (Exception e) {
				LOG.info("Couldn't read preferences file "
					 + Preferences.getPrefsXMLFile());
			}
			if (preferences == null)
				preferences = new Preferences();
			else
				LOG.info("Read preferences from " + Preferences.getPrefsXMLFile());

			GUIManager.addShutdownHook(new Runnable() {
				public void run() {
					try {
						writePreferences(getPreferences());
					} catch (IOException ex) {
						LOG.info("Could not write prefs to " + Preferences.getPrefsXMLFile());
						ex.printStackTrace();
					}
				}
			});
		}
		return preferences;
	}

	public static File getPrefsDir() {
		File prefsDir = new File(System.getProperty("user.home") + "/" + ".phenote");
		return prefsDir;
	}

	public static File getStandardDictionaryFile() {
		return new File(getPrefsDir()+"/dict", "standard.dict");
	}

	public static File getUserDefDictionaryFile() {
		return new File(getPrefsDir()+"/dict", "user.dict");
	}

	public static File getPeriodWordsFile() {
		return new File(getPrefsDir()+"/dict", "periodwords.dict");
	}

	public static File getAlwaysLowercaseFile() {
		return new File(getPrefsDir()+"/dict", "alwayslowercase.dict");
	}

	public static File getAllowedRepeatsFile() {
		return new File(getPrefsDir()+"/dict", "allowedrepeats.dict");
	}

	public boolean getAutoCommitTextEdits() {
		return autoCommitTextEdits;
	}

	public void setAutoCommitTextEdits(boolean autoCommitTextEdits) {
		this.autoCommitTextEdits = autoCommitTextEdits;
	}

	public boolean getWarnBeforeDiscardingEdits() {
		return warnBeforeDiscardingEdits;
	}

	public void setWarnBeforeDiscardingEdits(boolean warnBeforeDiscardingEdits) {
		this.warnBeforeDiscardingEdits = warnBeforeDiscardingEdits;
	}

	public void setAllowExtendedCharacters(boolean allowExtendedCharacters) {
		this.allowExtendedCharacters = allowExtendedCharacters;
	}

	public boolean getAllowExtendedCharacters() {
		return allowExtendedCharacters;
	}

	public void setWarnBeforeDelete(boolean warnBeforeDelete) {
		this.warnBeforeDelete = warnBeforeDelete;
	}

	public boolean getWarnBeforeDelete() {
		return warnBeforeDelete;
	}

	public void setShowToolTips(boolean showToolTips) {
		this.showToolTips = showToolTips;
	}

	public boolean getShowToolTips() {
		return showToolTips;
	}

	public void setCaseSensitiveSort(boolean caseSensitiveSort) {
		this.caseSensitiveSort = caseSensitiveSort;
	}

	public boolean getCaseSensitiveSort() {
		return caseSensitiveSort;
	}

	public void setAutosaveExpirationDays(int autosaveExpirationDays) {
		this.autosaveExpirationDays = autosaveExpirationDays;
	}

	public int getAutosaveExpirationDays() {
		return autosaveExpirationDays;
	}

	public void setAutosaveWaitTime(int autosaveWaitTime) {
		this.autosaveWaitTime = autosaveWaitTime;
	}

	public int getAutosaveWaitTime() {
		return autosaveWaitTime;
	}

	public void setAutosaveEnabled(boolean autosaveEnabled) {
		this.autosaveEnabled = autosaveEnabled;
	}

	public boolean getAutosaveEnabled() {
		return autosaveEnabled;
	}

	public File getAutosavePath() {
		if (autosavePath == null)
			autosavePath = new File(getPrefsDir(), "autosave");
		return autosavePath;
	}

	public void setAutosavePath(File autosavePath) {
		this.autosavePath = autosavePath;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

	public void setBrowserCommand(String browserCommand) {
		this.browserCommand = browserCommand;
	}

	public String getBrowserCommand() {
		return browserCommand;
	}

	protected static ClassLoader getExtensionLoader() {
		return Preferences.class.getClassLoader();
	}

	public static File[] getExtensionPaths() {
		File[] out = { new File(getInstallationDirectory(), "extensions"),
				new File(getPrefsDir(), "extensions") };
		return out;
	}

	public boolean getConfirmOnExit() {
		return confirmOnExit;
	}

	public void setConfirmOnExit(boolean confirmOnExit) {
		this.confirmOnExit = confirmOnExit;
		GUIManager.setConfirmOnExit(confirmOnExit);
	}

	public Color getLightSelectionColor() {
		if (lightSelectionColor == null)
			lightSelectionColor = new Color(230, 230, 255);  // pale lavender-blue
		//		lightSelectionColor = Color.yellow;  // for testing
		return lightSelectionColor;
	}

	public void setLightSelectionColor(Color lightSelectionColor) {
		this.lightSelectionColor = lightSelectionColor;
	}

	// Color for the subselection (darker)
	public Color getSelectionColor() {
		if (selectionColor == null)
			//			selectionColor = new Color(204, 204, 255);
			// Slightly darker blue than before
			selectionColor = new Color(180, 190, 255);
		//		selectionColor = Color.orange;  // for testing
		return selectionColor;
	}

	public void setSelectionColor(Color selectionColor) {
		this.selectionColor = selectionColor;
	}

	public void setButtonColor(Color buttonColor) {
		this.buttonColor = buttonColor;
	}

	public Color getButtonColor() {
		if (buttonColor == null)
			buttonColor = new Color(100, 149, 237);
		return buttonColor;
	}

	public Color getBackgroundColor() {
		if (backgroundColor == null)
			backgroundColor = new Color(216, 223, 230);
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getPersonalDefinition() {
		return "<autodef>";
	}

	public void setPersonalDefinition(String defaultDef) {
		this.defaultDef = defaultDef;
	}

	public boolean getUsePersonalDefinition() {
		return usePersonalDefinition;
	}

	public void setUsePersonalDefinition(boolean usePersonalDefinition) {
		this.usePersonalDefinition = usePersonalDefinition;
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
		FontUIResource resource = new FontUIResource(font);
		Hashtable defaults = UIManager.getDefaults();
		Enumeration keys = defaults.keys();
		while (keys.hasMoreElements()) {
			Object o = keys.nextElement();
			if (o instanceof String) {
				String key = (String) o;
				if (!key.toLowerCase().contains("menu")
						&& key.toLowerCase().endsWith("font")) {
					defaults.put(key, resource);
				}
			}
		}
	}

	public static String getAppName() {
		return appName;
	}

        // Not used?
	public static void setAppName(String app) {
		appName = app;
	}

	public static boolean isBatchMode() {
		return batchMode;
	}
	public static void setBatchMode(boolean batch) {
		LOG.debug(appName + ": set batchMode to " + batchMode);
		batchMode = batch;
	}

	public static void writePreferences(Preferences preferences)
	throws IOException {
		XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(
				new FileOutputStream(getPrefsXMLFile())));
		LOG.info("Writing preferences to " + getPrefsXMLFile());
		encoder.writeObject(preferences);
		encoder.close();
	}

	protected static void fillInInstallationDirectory() {
		// read the installation directory from a system property, if possible
		String prop = System.getProperty("launcherDir");
		if (prop != null) {
			installationDir = new File(prop);
			if (installationDir.exists() && installationDir.isDirectory()) {
				return;
			}
		}

		// if not, try to figure it out from the classpath (not pretty)
		StringTokenizer tokenizer = new StringTokenizer(System
				.getProperty("java.class.path"), System
				.getProperty("path.separator"));
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			File file = new File(token);
			// For some reason, when you run Phenote from the command line, the classpath contains all the jars EXCEPT phenote.jar!
			if (file.getName().equals("phenote.jar")) {
				try {
					installationDir = file.getCanonicalFile().getParentFile()
					.getParentFile();
					return;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// If we still haven't found it, try "."
		installationDir = new File(".");
	}

	/** This is not how Phenote records its version (though maybe it should be) */
// 	public static VersionNumber getVersion() {
// 		if (version == null) {
// 			try {
// 				URL url = getExtensionLoader().getResource("org/oboedit/resources/VERSION");
// 				BufferedReader reader = new BufferedReader(
// 						new InputStreamReader(url.openStream()));
				
// 				version = new VersionNumber(reader.readLine());
// 				reader.close();
// 			} catch (Exception e) {
// 				try {
// 					version = new VersionNumber("0.0");
// 				} catch (ParseException e1) {
// 					e1.printStackTrace();
// 				}
// 			}
// 		}
// 		return version;
// 	}

	public static File getInstallationDirectory() {
		if (installationDir == null)
			fillInInstallationDirectory();
//                LOG.debug("Installation directory is " + installationDir); // DEL
		return installationDir;
	}


	public static File getPrefsXMLFile() {
		return new File(getPrefsDir(), "config.xml");
	}

	// So that Configuration Manager can get name of logfile
	public void setLogfile(String lf) {
		getPreferences().logFile = lf;
	}
	public String getLogfile() {
		return getPreferences().logFile;
	}

	//system dictionary files that will be updated with "Update system dictionary files"
	public static List<String> getSystemDictFilenames() {
		return CollectionUtil.list("allowedrepeats.dict", "alwayslowercase.dict", "standard.dict", "periodwords.dict");
	}
	// These are the files that will be deleted if you select "Reset all configuration files"
	// from the Configuration Manager.
	public static List<String> getPrefsFilenames() {
		return CollectionUtil.list("config.xml", "filter_prefs.xml", "verify.xml",
				"component_prefs", "perspectives");
	}

	public static Color lightSelectionColor() {
		return getPreferences().getLightSelectionColor();
	}

	public static Color defaultSelectionColor() {
		return getPreferences().getSelectionColor();
	}

	public static Color defaultBackgroundColor() {
		return getPreferences().getBackgroundColor();
	}

	public static Color defaultButtonColor() {
		return new Color(100, 149, 237);
	}

	public boolean getUseModalProgressMonitors() {
		return useModalProgressMonitors;
	}

	public void setUseModalProgressMonitors(boolean useModalProgressMonitors) {
		this.useModalProgressMonitors = useModalProgressMonitors;
	}

	public boolean getProxyIsSet() {
//                LOG.debug("getProxyIsSet: " + proxyIsSet); // DEL
		return proxyIsSet;
	}

	public String getProxyHost() {
//                LOG.debug("getProxyHost: " + proxyHost); // DEL
		return proxyHost;
	}
	public int getProxyPort() {
		return proxyPort;
	}

	public String  getProxyProtocol() {
		return proxyProtocol;
	}

	public void setProxyHost(String proxyHost) {
//                LOG.debug("setProxyHost " + proxyHost); // DEL
                if (proxyHost.startsWith("http://")) {
                        proxyHost = proxyHost.substring(7);
                        LOG.debug("setProxyHost: http:// stripped, now proxyHost is " + proxyHost); // DEL
                }
		this.proxyHost = proxyHost;
	}
	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}
	public void setProxyIsSet(boolean proxyIsSet) {
		this.proxyIsSet = proxyIsSet;
	}
	public void setProxyProtocol(String protocol) {
		this.proxyProtocol = protocol;
	}

}
