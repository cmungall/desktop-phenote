package phenote.main;

import java.io.File;

import javax.help.HelpBroker;
import javax.help.HelpSet;

public class HelpManager {
	protected static HelpBroker helpBroker;
	protected static String helpsetPath = "doc/phenote-website/help/Phenote.hs";

	public static HelpBroker getHelpBroker() {
		if (helpBroker == null) {
			HelpSet hs;
			File docsDir = new File(helpsetPath);

			try {
				hs = new HelpSet(null, docsDir.toURL());
			} catch (Exception ee) {
				System.out.println("HelpSet " + ee.getMessage());
				System.out.println("HelpSet " + docsDir + " not found");
				return null;
			}
			helpBroker = hs.createHelpBroker();
		}

		return helpBroker;
	}
}