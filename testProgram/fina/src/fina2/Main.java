/*
 * Main.java
 *
 * Created on October 15, 2001, 4:32 PM
 */
package fina2;

import java.awt.Frame;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;

import fina2.login.LoginFrame;
import fina2.system.PropertySession;
import fina2.system.PropertySessionHome;
import fina2.ui.UIManager;
import fina2.update.gui.GuiUpdateSession;
import fina2.update.gui.GuiUpdateSessionHome;

public class Main {

	public static Main main;
	public static String dateFormat;

	public fina2.ui.UIManager ui;

	private javax.ejb.Handle userHandle;
	private javax.ejb.Handle languageHandle;

	private String homeDir = ".";
	private static Properties jndiProperties;

	private LoginFrame loginFrame;
	private MainFrame mainFrame;

	private javax.swing.JWindow logoWindow;
	private javax.swing.JLabel loadLabel;
   
	@SuppressWarnings("rawtypes")
	private Vector framesToShow = new Vector();

	/** Creates new Main */
	public Main() {
		main = this;
		homeDir = System.getProperty("FINA2_HOME");
		if (homeDir == null) {
			homeDir = ".";
		}

		ui = new UIManager();

		try {
			jndiProperties = new Properties();
			FileInputStream fi = new FileInputStream(homeDir + "/conf/jndi.properties");
			jndiProperties.load(fi);
			fi.close();
			System.getProperties().putAll(jndiProperties);
			ui.loadConfig();
		} catch (IOException ex) {
			ex.printStackTrace();
			javax.swing.JOptionPane.showMessageDialog(null, "Unable to load configuration file.\n" + ex.getMessage(), "Fina2", javax.swing.JOptionPane.INFORMATION_MESSAGE);
			System.exit(1);
		}

		new fina2.actions.ExitAction();

		loginFrame = new LoginFrame();
		loginFrame.show();

		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> loadSysProperties() {
		Map<String, String> sysProperties = null;
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object propsRef = jndi.lookup("fina2/system/PropertySession");
			PropertySessionHome home = (PropertySessionHome) PortableRemoteObject.narrow(propsRef, PropertySessionHome.class);
			PropertySession session = home.create();
			sysProperties = session.getSystemProperties();
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
		return sysProperties;
	}

	public MainFrame getMainFrame() {
		return mainFrame;
	}

	public String getHomeDir() {
		return homeDir;
	}

	public static InitialContext getJndiContext() throws NamingException {
		return new InitialContext(jndiProperties);
	}

	public static Object getObjectHome(String jndiName) throws NamingException {
		Context ctx = fina2.Main.getJndiContext();
		Object obj = ctx.lookup(jndiName);
		return PortableRemoteObject.narrow(jndiName, obj.getClass());
	}

	public String getServerHost() {
		return jndiProperties.getProperty("java.naming.provider.url");
	}

	/** Returns the object from server */
	public static Object getRemoteObject(String lookupName, Class objClass) throws Exception {

		InitialContext jndi = getJndiContext();
		Object ref = jndi.lookup(lookupName);

		return PortableRemoteObject.narrow(ref, objClass);
	}

	public UserTransaction getUserTransaction(InitialContext jndi) throws NamingException {
		Object ref = jndi.lookup("UserTransaction");
		UserTransaction trans = (UserTransaction) PortableRemoteObject.narrow(ref, UserTransaction.class);
		return trans;
	}

	public javax.ejb.Handle getUserHandle() {
		return userHandle;
	}

	public javax.ejb.Handle getLanguageHandle() {
		return languageHandle;
	}

	public static javax.ejb.Handle getCurrentLanguage() {
		return main.languageHandle;
	}

	public void setLanguageHandle(javax.ejb.Handle h) {
		languageHandle = h;
	}

	public void setLoadingMessage(String msg) {
		if (loadLabel == null)
			return;
		loadLabel.setFont(ui.getFont());
		loadLabel.setText("  Loading: " + msg);
		logoWindow.toFront();
	}

	public void addToShow(Object frame) {
		framesToShow.add(frame);
	}

	public Properties getProperties() {
		return jndiProperties;
	}

	boolean reload;

	private void guiUpdate() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/update/gui/GuiUpdateSession");
			GuiUpdateSessionHome home = (GuiUpdateSessionHome) PortableRemoteObject.narrow(ref, GuiUpdateSessionHome.class);
			GuiUpdateSession session = home.create();

			Map<String, String> sysProperties = loadSysProperties();

			ui.loadIcon("fina2.warning", "warning-icon.png");
			ui.loadIcon("fina2.spinner", "spinner.gif");

			JLabel messageLabel = new JLabel(ui.getString("fina2.update.updateMessage.title"));
			messageLabel.setFont(ui.getFont());

			// is check start update property
			if (startUpdate(sysProperties)) {
				if (session.canSynchronize(getGuiBuildDate())) {
					int reply = JOptionPane.showConfirmDialog(main.getMainFrame(), ui.getString("fina2.GuiUpdate"), messageLabel.getText(), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, ui.getIcon("fina2.warning"));
					if (reply == JOptionPane.YES_OPTION) {
						// Other Update

						Map<String, byte[]> map = session.getOtherUpdateFiles(getIsFileSelection("fina-addin.jar", PropertySession.UPDATE_ADDIN, sysProperties), getIsFileSelection("fina-addin.zip", PropertySession.UPDATE_ADDIN, sysProperties), getIsFileSelection("fina-update.jar", PropertySession.UPDATE_FINA_UPDATE, sysProperties), getIsFileSelection("run.bat", PropertySession.UPDATE_RUN_BAT, sysProperties), getIsFileSelection("resources", PropertySession.UPDATE_RESOURCES, sysProperties));
						for (Entry<String, byte[]> e : map.entrySet()) {
							String key = e.getKey();
							byte[] val = e.getValue();
							writeFile(new File(key), val);
						}

						// Gui Update
						byte[] fileDataArray = session.getGuiFile();
						File temp = new File("./lib/temp");
						if (temp != null)
							temp.mkdir();
						File tempFile = new File(temp, "fina-gui.jar");
						BufferedOutputStream buff = new BufferedOutputStream(new FileOutputStream(tempFile));
						buff.write(fileDataArray);
						buff.close();
						Runtime runtime = Runtime.getRuntime();
						runtime.exec("java -classpath ./lib/fina-update.jar fina2.update.Main");
						System.exit(1);
					}
				}

			}
		} catch (FinaGuiUpdateException ex) {
			ui.showMessageBox(main.getMainFrame(), ui.getString(ex.getMessageUrl()));
		} catch (java.io.FileNotFoundException ex) {
			ui.showMessageBox(main.getMainFrame(), ui.getString("fina2.update.osUserNotHaveFinaFoldersWritePermision"));
			Logger log = Logger.getLogger("fina2.Main");
			log.error(ex.getMessage(), ex);
			System.exit(1);
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
			System.exit(1);
		}
	}

	private String getIsFileSelection(String fileName, String key, Map<String, String> sysProperties) {
		String isFile = sysProperties.get(key);
		if (isFile != null) {
			int status = Integer.parseInt(isFile);
			if (status > 0) {
				isFile = fileName;
			} else {
				isFile = null;
			}
		}
		return isFile;
	}

	private void writeFile(File file, byte[] array) throws IOException {
		BufferedOutputStream buff = new BufferedOutputStream(new FileOutputStream(file));
		buff.write(array);
		buff.close();
	}

	// get start update properties
	private boolean startUpdate(Map<String, String> sysProperties) {
		try {
			String prop = sysProperties.get(PropertySession.UPDATE_START);
			if (prop != null) {
				prop = prop.trim();
				if (prop.equals("TRUE"))
					return true;
			} else {
				throw new FinaGuiUpdateException(FinaGuiUpdateException.Type.PROPERTY_NOT_FOUND);
			}
		} catch (FinaGuiUpdateException ex) {
			ui.showMessageBox(main.getMainFrame(), ui.getString(ex.getMessageUrl()));
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
		return false;
	}

	// get GUI file build date
	private static String getGuiBuildDate() throws FinaGuiUpdateException {
		String BuildDate = null;
		try {
			JarFile jf = new JarFile("./lib/fina-gui.jar");
			Manifest m = jf.getManifest();
			Attributes versionAttribut = m.getMainAttributes();
			BuildDate = versionAttribut.getValue("Build-Date");
		} catch (Exception ex) {
			throw new FinaGuiUpdateException(FinaGuiUpdateException.Type.VERSION_NOT_FOUND);
		}
		return BuildDate;
	}

	public static String guiBuildDate() {
		String buildDate = null;
		try {
			buildDate = getGuiBuildDate();
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/update/gui/GuiUpdateSession");
			GuiUpdateSessionHome home = (GuiUpdateSessionHome) PortableRemoteObject.narrow(ref, GuiUpdateSessionHome.class);
			GuiUpdateSession session = home.create();
			if (session.canSynchronize(buildDate)) {
				buildDate = "<html><font color='#ff0080'>" + buildDate + "</font></html>";
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return buildDate;
		}
		return buildDate;
	}

	public void loginOk(javax.ejb.Handle userHandle, javax.ejb.Handle languageHandle, boolean _reload) {
		this.reload = _reload;
		this.userHandle = userHandle;
		this.languageHandle = languageHandle;

		try {
			ui.loadSelectedLanguageBundle();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Unable to load language message bundle.\n" + ex.getMessage(), "Fina International", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		// Run GUI update
		guiUpdate();

		ui.createFont();
		ui.createDateFormat();

		Thread thread = new Thread() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public void run() {

				loginFrame.hide();
				logoWindow.show();

				/*
				 * InternalFrame Frames Titles add font.
				 */
				javax.swing.UIManager.put("InternalFrame.titleFont", ui.getFont());

				framesToShow.removeAllElements();
				ui.initTopActions();
				new fina2.actions.ExitAction();
				if (reload) {
					framesToShow.removeAllElements();
					ui.initTopActions();
					new fina2.actions.ExitAction();
				}

				ui.loadMenuBar();

				setLoadingMessage("Main frame");
				mainFrame = new MainFrame();

				logoWindow.hide();
				mainFrame.show();

				// load unique frames
				Set uniqueFramesToShow = new HashSet();
				for (Object o : framesToShow) {
					uniqueFramesToShow.add(o);
				}

				for (java.util.Iterator iter = uniqueFramesToShow.iterator(); iter.hasNext();) {
					java.awt.Component f = null;
					try {
						f = ((java.awt.Component) iter.next());
					} catch (java.util.ConcurrentModificationException e) {
						e.printStackTrace();
						throw e;
					}
					if (f instanceof java.awt.Frame) {
						f.show();
						f.setVisible(true);
					} else {
						try {
							javax.swing.JInternalFrame frame = (javax.swing.JInternalFrame) f;
							getMainFrame().getDesktop().add(frame);
							frame.setIcon(false);
							frame.show();
							frame.setVisible(true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
		ui.loadIcon("fina2.logo", "fina2.gif");
		javax.swing.JFrame p = null;
		logoWindow = new javax.swing.JWindow(p);
		java.awt.Container pane = logoWindow.getContentPane();

		java.awt.BorderLayout layout = new java.awt.BorderLayout();
		pane.setLayout(layout);
		pane.setBackground(new java.awt.Color(102, 102, 153));
		javax.swing.JLabel logoLabel = new javax.swing.JLabel(ui.getIcon("fina2.logo"));
		loadLabel = new javax.swing.JLabel("  Loading...");
		loadLabel.setForeground(java.awt.Color.white);
		loadLabel.setFont(new java.awt.Font("Dialog", 0, 11));
		javax.swing.JLabel buildLabel = new javax.swing.JLabel("  Version: " + UIManager.guiVersion);// ui.getSysPropertiesValue("fina2.gui.version"));
		buildLabel.setForeground(java.awt.Color.white);
		buildLabel.setFont(new java.awt.Font("Dialog", 0, 11));

		pane.add(buildLabel, java.awt.BorderLayout.NORTH);
		pane.add(logoLabel, java.awt.BorderLayout.CENTER);
		pane.add(loadLabel, java.awt.BorderLayout.SOUTH);

		logoWindow.setSize(290, 290);
		logoWindow.setFont(ui.getFont());
		java.awt.Toolkit t = java.awt.Toolkit.getDefaultToolkit();
		java.awt.Dimension d = t.getScreenSize();
		logoWindow.setLocation(d.width / 2 - 145, d.height / 2 - 145);

		thread.start();
	}

	public static boolean isValidCode(String code) {
		String s = "QWERTYUIOPASDFGHJKLZXCVBNM:{},.<>1234567890_&@#$!";
		StringBuffer sb = new StringBuffer(code.toUpperCase());
		for (int i = 0; i < sb.length(); i++) {
			char c = sb.charAt(i);
			if (s.indexOf(c) == -1)
				return false;
		}
		return true;
	}

	public static void main(String[] args) {
		try {
			javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
			try {
				javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ex) {

			}
		}
		new Main();
	}

	private void jbInit() throws Exception {
	}

	/* Return Specified Message in Bundle if Exists or Key */
	public static String getParameterMessage(String key) {
		if (Main.getString(key) != null && !Main.getString(key).equalsIgnoreCase(""))
			return Main.getString(key);
		return key;
	}

	/** The Fina specific exception handler */
	public static void generalErrorHandler(Exception e) {

		String title = Main.getString("fina2.title");
		String message = null;

		if (title == null || title.equals("")) {
			title = "Fina International";
		}

		if (e instanceof FinaTypeException) {
			String[] pars = ((FinaTypeException) e).getParams();
			message = Main.getString(((FinaTypeException) e).getMessageUrl());

			if (pars != null) {
				for (int i = 0; i < pars.length; i++) {
					pars[i] = Main.getParameterMessage(pars[i]);
				}
				MessageFormat formatter = new MessageFormat(message);
				message = formatter.format(pars);
				formatter = null;
			}
		} else {
			Logger log = Logger.getLogger("fina2.Main");
			log.error(e.getMessage(), e);

			e.printStackTrace();
			title = Main.getString("fina2.title");
			message = Main.getString("FinaTypeException.GeneralError");
			if (message == null || message.equals(""))
				message = "FinA International General Error Contact Administration.";
		}
		main.ui.showMessageBox(main.getMainFrame(), title, message);
	}

	public static void errorHandler(Frame frame, String title, String massege) {

		main.ui.showMessageBox(frame, title, massege);
	}

	/** Returns a string with the given key from the resources */
	public static String getString(String key) {
		return main.ui.getString(key);
	}

	/** Returns an icon with the given key from the resources */
	public static Icon getIcon(String file) {
		main.ui.loadIcon(file, file);
		return main.ui.getIcon(file);
	}
}
