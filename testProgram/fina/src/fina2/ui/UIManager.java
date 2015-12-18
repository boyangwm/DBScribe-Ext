package fina2.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.Handle;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;

import fina2.Main;
import fina2.actions.AboutVersionAction;
import fina2.help.HelpManager;
import fina2.help.HelpManagerBase;
import fina2.i18n.Language;
import fina2.i18n.LanguageSession;
import fina2.i18n.LanguageSessionHome;
import fina2.reportoo.server.StoredReportInfo;
import fina2.security.User;
import fina2.system.PropertySession;
import fina2.system.PropertySessionHome;
import fina2.ui.menu.MenuSession;
import fina2.ui.menu.MenuSessionHome;
import fina2.ui.tree.Node;

/**
 * @author FinA
 * 
 */
public class UIManager {
	@SuppressWarnings("rawtypes")
	private Hashtable actions;
	@SuppressWarnings("rawtypes")
	private Hashtable allActions;
	@SuppressWarnings("rawtypes")
	private Hashtable icons;
	@SuppressWarnings("rawtypes")
	private Hashtable config;
	private Properties messages;
	private Map<String, String> SysProperties;
	private Font font;
	private DateFormat dateFormat;
	private JMenuBar menuBar;
	private Main main;
	public static final String guiVersion = "3.4.4c";
	private Logger logger = Logger.getLogger(getClass());
	private HelpManagerBase helpManager;

	@SuppressWarnings("rawtypes")
	public UIManager() {
		main = Main.main;
		font = new Font("Dialog", Font.PLAIN, 11); // Default font
		actions = new Hashtable();
		allActions = new Hashtable();
		icons = new Hashtable();
		config = new Hashtable();
		messages = new Properties();
		loadIcon("fina2.icon", "icon.gif");
		helpManager = new HelpManager();
	}

	public void initTopActions() {
		actions.clear();
		allActions.clear();

		// new fina2.actions.ExitAction();
		new fina2.actions.MenuAmendAction();
		new fina2.actions.LanguagesAction();
		new fina2.actions.MDTAmendAction();
		new fina2.actions.FormulaRepositoryAction();
		new fina2.actions.BankTypesAction();
		new fina2.actions.BanksAction();
		new fina2.actions.BankGroupsAction();
		// new fina2.actions.BankRegionsAction();
		new fina2.actions.ManagingBodyAction();
		new fina2.actions.UsersAction();
		new fina2.actions.ReturnTypesAction();
		new fina2.actions.ReturnDefinitionsAction();
		new fina2.actions.PeriodTypesAction();
		new fina2.actions.PeriodsAction();
		new fina2.actions.PeriodAutoInsertAction();
		new fina2.actions.SchedulesAction();
		new fina2.actions.ScheduleAutoInsertAction();
		new fina2.actions.LicenceTypesAction();
		new fina2.actions.ReturnManagerAction();
		new fina2.actions.ComparisonsAmendAction();
		new fina2.actions.ReportManagerAction();
		new fina2.actions.ReturnsStatusesAction();
		new fina2.actions.StoredReportManagerAction();
		new fina2.actions.ReportSchedulerManagerAction();
		new fina2.actions.SecuritySettingsAction();
		new fina2.actions.ReturnVersionsAction();
		new fina2.actions.ImportManagerAction();
		new fina2.actions.AboutVersionAction();
		new fina2.actions.RegionCityAction();
		new fina2.nbg.ExcelAction();
		new fina2.actions.LogoutAction();
	}

	@SuppressWarnings("unchecked")
	public void addAction(String key, AbstractAction action) {
		actions.put(key, action);
		allActions.put(key, action);
	}

	public AbstractAction getAction(String key) {
		return (AbstractAction) actions.get(key);
	}

	@SuppressWarnings("rawtypes")
	public Hashtable getActions() {
		return actions;
	}

	public AbstractAction getAllAction(String key) {
		return (AbstractAction) allActions.get(key);
	}

	@SuppressWarnings("rawtypes")
	public Hashtable getAllActions() {
		return allActions;
	}

	public void setLanguageBundle(Handle languageHandle, Properties bundle) throws Exception {
		getLanguageSession().setLanguageBundle(languageHandle, bundle);
		if (main.getLanguageHandle().getEJBObject().isIdentical(languageHandle.getEJBObject())) {
			messages = bundle;
		}
	}

	public void loadSelectedLanguageBundle() throws Exception {
		messages = getLanguageSession().getLanguageBundle(main.getLanguageHandle());
	}

	public Properties getLanguageBundle(Handle languageHandle) throws Exception {
		Properties bundle = null;
		if (!main.getLanguageHandle().getEJBObject().isIdentical(languageHandle.getEJBObject())) {
			bundle = getLanguageSession().getLanguageBundle(languageHandle);
		}
		if (bundle == null || bundle.size() == 0) {
			bundle = new Properties();
			bundle.putAll(messages);
		}
		return bundle;
	}

	private LanguageSession getLanguageSession() throws Exception {
		InitialContext jndi = fina2.Main.getJndiContext();
		Object ref = jndi.lookup("fina2/i18n/LanguageSession");
		LanguageSessionHome home = (LanguageSessionHome) PortableRemoteObject.narrow(ref, LanguageSessionHome.class);
		return home.create();
	}

	@SuppressWarnings("rawtypes")
	public void loadConfig() throws IOException {
		ObjectInputStream in = null;
		File file = null;
		try {
			file = new File(main.getHomeDir() + "/conf/fina2.conf");
			if (!file.exists()) {
				file.createNewFile();
				config = new Hashtable();

				java.awt.Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

				putConfigValue("fina2.MainFrame.width", new Integer(d.width * 6 / 8));
				putConfigValue("fina2.MainFrame.height", new Integer(d.height * 6 / 8));
				putConfigValue("fina2.MainFrame.x", new Integer(d.width / 8));
				putConfigValue("fina2.MainFrame.y", new Integer(d.height / 8));

			} else {
				in = new ObjectInputStream(new FileInputStream(main.getHomeDir() + "/conf/fina2.conf"));
				config = (Hashtable) in.readObject();
			}
		} catch (Exception e) {
			if (file != null) {
				file.delete();
				file.createNewFile();
			}
			logger.error(e.getMessage(), e);
		} finally {
			if (in != null)
				in.close();
		}
	}

	@SuppressWarnings("unused")
	public void saveConfig() throws IOException {
		ObjectOutputStream out = null;
		try {
			java.io.File f = new java.io.File(main.getHomeDir() + "/conf/fina2.conf");
			out = new ObjectOutputStream(new FileOutputStream(main.getHomeDir() + "/conf/fina2.conf"));

			out.writeObject(config);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null)
				out.close();
		}
	}

	/**
	 * Checks if report name is valid
	 * 
	 * @param name
	 *            Report name to validate
	 * @return true if valid, otherwise false
	 */
	public boolean isNameValid(String name) {
		boolean valid = true;
		if (name == null && !"".equals(name)) {
			valid = false;
		} else {
			for (int i = 0; i < name.length(); i++) {
				if (!Character.isLetterOrDigit(name.charAt(i)) && name.charAt(i) != '_' && name.charAt(i) != ' ') {
					valid = false;
				}
			}
		}
		return valid;
	}

	/**
	 * sorts by date
	 * 
	 * @param root
	 */
	@SuppressWarnings({ "unchecked" })
	public void sortByDate(StoredReportInfo root) {
		Comparator<StoredReportInfo> comp = new Comparator<StoredReportInfo>() {
			public int compare(StoredReportInfo s1, StoredReportInfo s2) {
				return s1.getStoreDate().compareTo(s2.getStoreDate());
			}
		};
		ArrayList<StoredReportInfo> rootchildren = root.getChildren();
		ArrayList<StoredReportInfo> listarray[] = new ArrayList[rootchildren.size()];
		for (int i = 0; i < listarray.length; i++)
			listarray[i] = new ArrayList<StoredReportInfo>();
		for (int i = 0; i < listarray.length; i++)
			listarray[i].add(rootchildren.get(i));
		for (int i = 0; i < listarray.length; i++)
			for (int j = 0; j < listarray[i].get(0).getChildren().size(); j++) {
				Collections.sort((ArrayList<StoredReportInfo>) listarray[i].get(0).getChildren(), comp);
			}
	}

	/**
	 * sorts criterionRows ascending
	 * 
	 * @param criterionRows
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void sortCriterionRowsData(Collection criterionRows) {
		ArrayList arr = new ArrayList();
		Iterator iterat = criterionRows.iterator();
		while (iterat.hasNext()) {
			arr.add(iterat.next());
		}
		Comparator comp = new Comparator() {
			public int compare(Object o1, Object o2) {
				return o1.toString().compareTo(o2.toString());
			}
		};
		Collections.sort(arr, comp);
		criterionRows.removeAll(criterionRows);
		criterionRows.addAll(arr);
		arr.clear();
	}

	/**
	 * sorts only children of passed node
	 * 
	 * @param node
	 */
	@SuppressWarnings("unchecked")
	public static void sortNodesChildren(DefaultMutableTreeNode node) {

		Comparator<DefaultMutableTreeNode> nodecomparator = new Comparator<DefaultMutableTreeNode>() {
			public int compare(DefaultMutableTreeNode nodeone, DefaultMutableTreeNode nodetwo) {
				return nodeone.getUserObject().toString().compareTo(nodetwo.getUserObject().toString());
			}
		};

		ArrayList<DefaultMutableTreeNode> nodeList = Collections.list(node.children());
		Collections.sort(nodeList, nodecomparator);
		node.removeAllChildren();
		for (int i = 0; i < nodeList.size(); i++) {
			node.add(nodeList.get(i));
		}
	}

	/**
	 * sorts all tree
	 * 
	 * @param root
	 */
	@SuppressWarnings("unchecked")
	public static void sortAllTree(DefaultMutableTreeNode root) {
		Enumeration<DefaultMutableTreeNode> en = root.breadthFirstEnumeration();
		DefaultMutableTreeNode current = null;
		while (en.hasMoreElements()) {
			current = en.nextElement();
			if (!current.isLeaf()) {
				sortNodesChildren(current);
			}
		}
	}

	public void removeConfigValue(String key) {
		config.remove(key);
	}

	@SuppressWarnings("unchecked")
	public void putConfigValue(String key, Object value) {
		config.put(key, value);
	}

	public Object getConfigValue(String key) {
		return config.get(key);
	}

	public Object getConfigValue(String key, Object def) {
		Object obj = config.get(key);
		if (obj == null)
			return def;
		return obj;
	}

	public String getString(String key) {
		String s = messages.getProperty(key);
		return s;
	}

	public String getString(String key, String def) {
		String s = messages.getProperty(key);
		if (s == null) {
			return def;
		}
		return s;
	}

	@SuppressWarnings("unchecked")
	public void loadIcon(String key, String path) {
		if (icons.containsKey(key)) {
			return;
		}
		icons.put(key, new ImageIcon("./resources/" + path));
	}

	public ImageIcon getIcon(String key) {
		return (ImageIcon) icons.get(key);
	}

	public void createFont() {
		try {
			Language lang = (Language) main.getLanguageHandle().getEJBObject();
			font = new Font(lang.getFontFace(), Font.PLAIN, lang.getFontSize());
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	public void createFont(String fontFace, Integer fontSize) {
		try {
			font = new Font(fontFace, Font.PLAIN, fontSize);
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
	}

	public Font getFont() {
		return font;
	}

	@SuppressWarnings("deprecation")
	public void showMessageBox(java.awt.Frame parent, String title, String message) {
		MessageBox msgBox = new MessageBox(parent);
		msgBox.setTitle(title);
		msgBox.setMessage(message);
		msgBox.pack();
		msgBox.setLocationRelativeTo(parent);
		msgBox.show();
	}

	public void showMessageBox(java.awt.Component parent, String title, String message, int messageType) {

		Object fnt = javax.swing.UIManager.get("OptionPane.messageFont");
		Object fnt1 = javax.swing.UIManager.get("OptionPane.font");
		Object fnt2 = javax.swing.UIManager.get("OptionPane.buttonFont");

		javax.swing.UIManager.put("OptionPane.messageFont", getFont());
		javax.swing.UIManager.put("OptionPane.font", getFont());
		javax.swing.UIManager.put("OptionPane.buttonFont", getFont());

		JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);

		javax.swing.UIManager.put("OptionPane.messageFont", fnt);
		javax.swing.UIManager.put("OptionPane.font", fnt1);
		javax.swing.UIManager.put("OptionPane.buttonFont", fnt2);
	}

	public int showConfirmDialog(java.awt.Component parent, String title, String message, int messageType) {

		Object fnt = javax.swing.UIManager.get("OptionPane.messageFont");
		Object fnt1 = javax.swing.UIManager.get("OptionPane.font");
		Object fnt2 = javax.swing.UIManager.get("OptionPane.buttonFont");

		javax.swing.UIManager.put("OptionPane.messageFont", getFont());
		javax.swing.UIManager.put("OptionPane.font", getFont());
		javax.swing.UIManager.put("OptionPane.buttonFont", getFont());

		int result = JOptionPane.showConfirmDialog(parent, message, title, messageType);

		javax.swing.UIManager.put("OptionPane.messageFont", fnt);
		javax.swing.UIManager.put("OptionPane.font", fnt1);
		javax.swing.UIManager.put("OptionPane.buttonFont", fnt2);

		return result;
	}

	public void showMessageBox(java.awt.Frame parent, String message) {
		showMessageBox(parent, getString("fina2.title"), message);
	}

	@SuppressWarnings("deprecation")
	public void showLongMessageBox(java.awt.Frame parent, String title, String msg, String longMsg) {
		LongMessageBox msgBox = new LongMessageBox(parent);
		msgBox.setTitle(title);
		msgBox.setMessage(msg);
		msgBox.setLongMessage(longMsg);
		msgBox.pack();
		msgBox.setLocationRelativeTo(parent);
		msgBox.show();
	}

	public void showLongMessageBox(java.awt.Frame parent, String msg, String longMsg) {
		showLongMessageBox(parent, getString("fina2.title"), msg, longMsg);
	}

	public boolean showConfirmBox(java.awt.Frame parent, String title, String message) {
		ConfirmBox msgBox = new ConfirmBox(parent);
		msgBox.setTitle(title);
		return msgBox.show(message);
	}

	public boolean showConfirmBox(java.awt.Frame parent, String message) {
		return showConfirmBox(parent, getString("fina2.title"), message);
	}

	public boolean showLongConfirmBox(java.awt.Frame parent, String title, String message, String longMessage) {
		LongConfirmBox msgBox = new LongConfirmBox(parent);
		msgBox.setTitle(title);
		msgBox.setLongMessage(longMessage);
		return msgBox.show(message, longMessage);
	}

	public boolean showLongConfirmBox(java.awt.Frame parent, String message, String longMessage) {
		return showLongConfirmBox(parent, getString("fina2.title"), message, longMessage);
	}

	private ProcessDialog processDlg;

	public ProcessDialog showProcessDialog(java.awt.Frame parent, String message) {
		processDlg = new ProcessDialog(parent, true);

		processDlg.setTitle(message);
		processDlg.setMessage(message);

		Thread t = new Thread() {
			public void run() {
				processDlg.show();
			}
		};
		t.start();
		return processDlg;
	}

	public ProcessDialog showProgressDialog(java.awt.Frame parent, String message, int maxProgress) {
		processDlg = new ProcessDialog(parent, true, maxProgress);

		processDlg.setTitle(message);
		processDlg.setMessage(message);

		Thread t = new Thread() {
			public void run() {
				processDlg.show();
			}
		};
		t.start();
		return processDlg;
	}

	@SuppressWarnings("rawtypes")
	public JMenuBar loadMenuBar() {
		try {
			InitialContext jndi = Main.getJndiContext();
			Object ref = jndi.lookup("fina2/ui/menu/MenuSession");
			MenuSessionHome home = (MenuSessionHome) PortableRemoteObject.narrow(ref, MenuSessionHome.class);

			MenuSession session = home.create();
			Node root = session.getUserMenuTree(main.getUserHandle(), main.getLanguageHandle());

			menuBar = new JMenuBar();

			for (Iterator iter = root.getChildren().iterator(); iter.hasNext();) {

				JMenuItem item = createMenu(null, (Node) iter.next());

				if (item instanceof JMenu) {
					JMenu menu = (JMenu) item;
					if (menu.getMenuComponentCount() > 0) {
						menuBar.add(item);
					}
				} else {
					menuBar.add(item);
				}
			}

			menuBar.setFont(getFont());
			System.gc();

			return menuBar;
		} catch (Exception e) {
			Main.errorHandler(null, Main.getString("fina2.title"), Main.getString("fina2.ui.menuLoadError"));
			System.exit(1);
			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	private JMenuItem createMenu(JMenu menu, Node node) {

		if (node.isLeaf()) {
			JMenuItem m = new JMenuItem(node.getLabel());
			if (((Integer) node.getType()).intValue() == 1) {
				javax.swing.AbstractAction a = getAllAction((String) node.getProperty("actionKey"));

				if (a == null) {
					return null;
				} else if (a instanceof fina2.actions.FinaAction) {
					try {
						fina2.actions.FinaAction fa = (fina2.actions.FinaAction) a;
						User user = (User) main.getUserHandle().getEJBObject();
						if ((!user.hasPermissions(fa.getPermissions())) && (!(fa instanceof AboutVersionAction))) {
							actions.remove(node.getProperty("actionKey"));
							return null;
						} else {
							m.setAction(a);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					m.setAction(a);
				}
			}
			if (((Integer) node.getType()).intValue() == 2) {
				m.setAction(new fina2.ui.menu.ApplicationAction((String) node.getProperty("application")));
			}
			m.setText(node.getLabel());
			m.setFont(getFont());
			if (menu != null)
				menu.add(m);
			return m;
		} else {
			JMenu m = new JMenu(node.getLabel());
			m.setFont(getFont());
			if (menu != null)
				menu.add(m);
			for (Iterator iter = node.getChildren().iterator(); iter.hasNext();) {
				Node n = (Node) iter.next();
				JMenuItem item = createMenu(m, n);
				if (item != null)
					m.add(item);
			}

			return m;
		}

	}

	public JMenuBar getMenuBar() {
		return menuBar;
	}

	public Properties getMessages() {
		return messages;
	}

	public static GridBagConstraints getGridBagConstraints(int x, int y, int ix, int iy, int w, int h, int a, int f, Insets in) {

		GridBagConstraints grid = new GridBagConstraints();

		if (x >= 0) {
			grid.gridx = x;
		}
		if (y >= 0) {
			grid.gridy = y;
		}
		if (w >= 0) {
			grid.gridwidth = w;
		}
		if (h >= 0) {
			grid.gridheight = h;
		}
		if (ix >= 0) {
			grid.ipadx = ix;
		}
		if (iy >= 0) {
			grid.ipady = iy;
		}
		if (a >= 0) {
			grid.anchor = a;
		}
		if (f >= 0) {
			grid.fill = f;
		}
		if (in != null) {
			grid.insets = in;
		}
		return grid;
	}

	@SuppressWarnings("unchecked")
	public String getSysPropertiesValue(String key) {
		if (SysProperties == null) {
			try {
				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi.lookup("fina2/system/PropertySession");
				PropertySessionHome home = (PropertySessionHome) PortableRemoteObject.narrow(ref, PropertySessionHome.class);
				PropertySession session = home.create();
				SysProperties = session.getSystemProperties();
			} catch (Exception e) {
				Main.generalErrorHandler(e);
			}
		}
		return SysProperties.get(key);
	}

	public static String formatedHtmlString(String name) {
		String prefix = "<html>";
		String suffix = "<font color=\"#ff0080\"> * <font><html>";
		StringBuilder sb = new StringBuilder();
		sb.append(prefix);
		sb.append(name);
		sb.append(suffix);
		return sb.toString();
	}

	public static void resizeOooSheetPage(JFrame frame) {
		System.out.println("Resize :" + frame.getTitle());
		Rectangle r = frame.getBounds();
		r.height = r.height + 1;
		r.width = r.width + 1;
		frame.setBounds(r);
	}

	public IndeterminateLoading createIndeterminateLoading(java.awt.Frame parent) {
		return new IndeterminateLoading(parent);
	}

	@SuppressWarnings("serial")
	public class IndeterminateLoading extends JDialog implements Serializable {
		private JProgressBar progress;

		private Thread thread;

		public IndeterminateLoading(java.awt.Frame parent) {
			super(parent, true);
			initComponents();
			setLocationRelativeTo(getParent());
		}

		@SuppressWarnings("deprecation")
		public void show() {
			super.show();
		}

		public void start() {
			thread = new Thread() {
				@Override
				public void run() {
					show();
					progress.updateUI();
				}
			};
			thread.start();
		}

		public void stop() {
			this.dispose();
			this.setVisible(false);
			if (thread != null)
				thread.interrupt();
		}

		private void initComponents() {
			this.setSize(100, 100);
			this.setTitle(getString("fina2.indeterminateioading"));
			this.setFont(getFont());
			this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

			progress = new JProgressBar();
			progress.setIndeterminate(true);

			progress.setCursor(new Cursor(Cursor.WAIT_CURSOR));

			Dimension dimension = new Dimension(170, 16);
			progress.setPreferredSize(dimension);

			this.getContentPane().add(progress);
			this.setResizable(false);
			this.pack();
		}
	}

	public void createDateFormat() {
		try {
			Language lang = (Language) main.getLanguageHandle().getEJBObject();
			dateFormat = new SimpleDateFormat(lang.getDateFormat());
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
	}

	public DateFormat getDateFromat() {
		return dateFormat;
	}

	// Load maximum return count. Default is 1000;
	public int loadMaxReturnSize() {
		int maxReturnsCount = 1000;
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/system/PropertySession");
			PropertySessionHome home = (PropertySessionHome) PortableRemoteObject.narrow(ref, PropertySessionHome.class);
			PropertySession session = home.create();
			String maxReturnSizeString = session.getSystemProperty(PropertySession.MAX_RETURNS_SIZE);
			if (maxReturnSizeString != null)
				maxReturnsCount = Integer.parseInt(maxReturnSizeString);
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
		return maxReturnsCount;
	}

	private boolean matchesCompile(String name, String expression) {
		CharSequence inputStr = name;
		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		return matcher.matches();
	}

	public boolean isValidEmailAddress(String emailAddress) {
		String expression = "^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		return matchesCompile(emailAddress, expression);

	}

	public boolean isValidReportName(String name) {
		String expression = "[a-z,A-Z,0-9,_,.]{1,50}";
		return matchesCompile(name, expression);
	}

	public boolean isValidName(String name) {
		return !name.equals("");
	}

	public boolean isValidCode(String code) {
		String expression = "[a-z,A-Z,0-9,_,.]{1,12}";
		return matchesCompile(code, expression);
	}

	public boolean isValidDescription(String description, boolean isRequierd) {
		if (isRequierd && description.equals("")) {
			return false;
		}
		if (description.length() > 255) {
			return false;
		}
		return true;
	}

	public JWindow showErrorWindow(final JPanel panel, String erroString, Point loc) {

		StringBuilder sb = new StringBuilder(erroString);
		sb.insert(0, "  ");
		sb.insert(sb.length(), "  ");

		erroString = sb.toString();

		JLabel errorLabel = new JLabel(erroString);

		loadIcon("exclamationIcon", "exclamation.gif");

		errorLabel.setIcon(getIcon("exclamationIcon"));

		Window topLevelWin = SwingUtilities.getWindowAncestor(panel);
		final JWindow errorWindow = new JWindow(topLevelWin);
		JPanel contentPane = (JPanel) errorWindow.getContentPane();
		contentPane.add(errorLabel);
		contentPane.setBackground(panel.getBackground());
		contentPane.setBorder(BorderFactory.createLineBorder(Color.red));
		errorWindow.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				panel.setFocusable(false);
				errorWindow.dispose();
				errorWindow.setVisible(false);
			}

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
			}
		});

		errorWindow.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					panel.setFocusable(false);
					errorWindow.dispose();
					errorWindow.setVisible(false);
				}
			}
		});

		errorWindow.setFocusable(true);
		errorWindow.pack();
		errorWindow.setLocation(loc.x + 20, loc.y - 25);
		errorWindow.setVisible(true);

		return errorWindow;
	}

	public HelpManagerBase getHelpManager() {
		return helpManager;
	}

	public boolean isValidLength(String text, boolean isDescription) {
		if (isDescription) {
			if (text.length() > 255)
				return false;
			return true;
		} else {
			if (text.length() > 100)
				return false;
			return true;
		}
	}

	@SuppressWarnings("deprecation")
	public boolean isCorrectDate(String date) {
		SimpleDateFormat sdf = (SimpleDateFormat) getDateFromat();

		Date testDate = null;
		try {

			testDate = sdf.parse(date);
		}

		catch (ParseException e) {
			return false;
		}
		if ((testDate.getYear() + 1900) > 9999 || (testDate.getYear()+1900 < 1970))
			return false;
		if (!sdf.format(testDate).equals(date)) {
			return false;
		}
		return true;
	}

	public Integer getMinimalLengthPassword() {
		Integer passwordMinimalLengthInt = null;
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/system/PropertySession");
			PropertySessionHome home = (PropertySessionHome) PortableRemoteObject.narrow(ref, PropertySessionHome.class);
			PropertySession session = home.create();
			String passwordMinimalLengthString = session.getSystemProperty(PropertySession.MINIMAL_PASSWORD_LENGTH);
			passwordMinimalLengthInt = Integer.parseInt(passwordMinimalLengthString);
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		return passwordMinimalLengthInt;
	}

	/*
	 * String change format to server static data format(Static Server date
	 * format is 'dd/MM/yyyy').
	 */
	public String stringFormatedToServerDataFormat(String string) throws ParseException {
		if (string.length() == 0 || string == null)
			return "";
		SimpleDateFormat format = (SimpleDateFormat) dateFormat.clone();
		Date date = format.parse(string);
		format.applyPattern("dd/MM/yyyy");
		return format.format(date);
	}

	public String stringToCurrentDateFormat(String string) throws ParseException {
		if (string.length() == 0 || string == null)
			return "";
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		Date date = format.parse(string);
		return dateFormat.format(date);
	}
	
	public String getDefaultLanguage() {
		String defaultnalguage = null;
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/system/PropertySession");
			PropertySessionHome home = (PropertySessionHome) PortableRemoteObject.narrow(ref, PropertySessionHome.class);
			PropertySession session = home.create();
			defaultnalguage = session.getSystemProperty(PropertySession.DEFAULT_LANGUAGE);
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		if (defaultnalguage != null) {
			return defaultnalguage;
		} else {

			return null;
		}
	}

	public void setDefaultLanguage(String defaultlanguage) {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/system/PropertySession");
			PropertySessionHome home = (PropertySessionHome) PortableRemoteObject.narrow(ref, PropertySessionHome.class);
			PropertySession session = home.create();
			HashMap<String, String> hashMap = new HashMap<String, String>();
			hashMap.put(PropertySession.DEFAULT_LANGUAGE, defaultlanguage);
			session.setSystemProperties(hashMap);
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

	}
}
