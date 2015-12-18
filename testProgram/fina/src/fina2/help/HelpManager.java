package fina2.help;

import java.awt.event.ActionListener;
import java.net.URL;

import javax.help.BadIDException;
import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.JHelp;
import javax.swing.JButton;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

public class HelpManager implements HelpManagerBase {
	private static final String errorPageId = "fina2.help.errorPage";
	private Logger log = Logger.getLogger(getClass());
	private HelpSet helpSet;
	private HelpBroker helpBroker;
	private ClassLoader classLoader;
	private boolean isCreateHelpSet = false;

	public HelpManager() {
		connection();
		if (helpSet != null) {
			isCreateHelpSet = true;
		}
	}

	public void createDisplayHelpFromSource(JButton helpButton, String helpId) {
		try {
			CSH.setHelpIDString(helpButton, helpId);
			ActionListener helper = new CSH.DisplayHelpFromSource(helpBroker);
			helpButton.addActionListener(helper);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	public void createDisplayHelpFromFocus(JButton button, String helpId) {
		try {
			CSH.setHelpIDString(button, helpId);
			if (helpBroker != null) {
				ActionListener helper = new CSH.DisplayHelpFromFocus(helpBroker);
				button.addActionListener(helper);
			}
		} catch (BadIDException ex) {
			CSH.setHelpIDString(button, errorPageId);
			if (helpBroker != null) {
				ActionListener helper = new CSH.DisplayHelpFromFocus(helpBroker);
				button.addActionListener(helper);
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	public void createDisplayHelpAfterTracking(JButton button, String helpId) {
		try {
			CSH.setHelpIDString(button, helpId);
			ActionListener helper = new CSH.DisplayHelpAfterTracking(helpBroker);
			button.addActionListener(helper);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	public void connection() {
		helpSet = null;
		try {
			classLoader = HelpManager.class.getClassLoader();
			URL hsURL = HelpSet.findHelpSet(classLoader, "jhelpset.hs");
			helpSet = new HelpSet(classLoader, hsURL);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		if (helpSet != null) {
			helpBroker = helpSet.createHelpBroker();
		} else {
			helpBroker = null;
		}

	}

	// TODO Help Test Running
	public static void main(String[] args) {
		JHelp helpViewer = null;
		try {

			ClassLoader cl = HelpManager.class.getClassLoader();

			URL url = HelpSet.findHelpSet(cl, "jhelpset.hs");

			helpViewer = new JHelp(new HelpSet(cl, url));

		} catch (Exception e) {
			e.printStackTrace();
		}

		JFrame frame = new JFrame();

		frame.setSize(500, 500);

		frame.getContentPane().add(helpViewer);

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		frame.setVisible(true);
	}

	public boolean IsHelpSystem() {
		return isCreateHelpSet;
	}

}
