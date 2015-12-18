package fina2.returns;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import com.sun.codemodel.fmt.JTextFile;

import fina2.BaseFrame;
import fina2.Main;
import fina2.calendar.FinaCalendar;
import fina2.i18n.LocaleUtil;
import fina2.security.User;
import fina2.security.UserPK;
import fina2.system.PropertySession;
import fina2.system.PropertySessionHome;
import fina2.ui.AbstractDialog;
import fina2.ui.UIManager;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableReviewFrame;
import fina2.ui.table.TableRow;
import fina2.ui.table.TableRowImpl;
import fina2.ui.treetable.JTreeTable;

public class ImportManagerFrame extends BaseFrame implements FocusListener {

	private static Logger log = Logger.getLogger(ImportManagerFrame.class);

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private ImportManagerTreeTableModel treeTableModel = new ImportManagerTreeTableModel();
	private JTreeTable treeTable = new JTreeTable(treeTableModel);

	private JScrollPane treeTablePanel = new JScrollPane();
	private JSplitPane centerPanel = new JSplitPane();
	private JPanel filterPanel = new JPanel();
	private JPanel buttonsPanel = new JPanel();
	private JPanel helpPanel = new JPanel();
	private JPanel southPanel = new JPanel();
	private JPanel eastPanel = new JPanel();
	private JPanel closePrintPanel = new JPanel();
	private JPanel northPanel = new JPanel();
	private JPanel detailsPanel = new JPanel();

	private JTextField fromText = new JTextField();
	private JTextField toText = new JTextField();
	private JTextField uploadedAfterText = new JTextField();
	private JTextField importStartText = new JTextField();
	private JTextField importEndText = new JTextField();
	private JTextField uploadTimeText = new JTextField();
	private JTextField xlsFinaUserText = new JTextField();

	private JLabel importStartLabel = new JLabel();
	private JLabel importEndLabel = new JLabel();
	private JLabel uploadTimeLabel = new JLabel();
	private JLabel xlsFinaUserLabel = new JLabel();

	private JComboBox versionList = new JComboBox();
	private JComboBox codeList = new JComboBox();
	private JComboBox statusList = new JComboBox();
	private JComboBox userList = new JComboBox();

	private JButton fiSelectionButton = new JButton();
	private JButton filterButton = new JButton();
	private JButton importButton = new JButton();
	private JButton removeButton = new JButton();

	private JCheckBox autoRefreshCheck = new JCheckBox();

	private JTextArea messageText = new JTextArea();

	private JPopupMenu popupMenu = new JPopupMenu();

	private RemoveAction removeAction;
	private ImportAction importAction;

	private Timer timer = new Timer(true);
	private TimerTask timerTask;

	private FinaCalendar fcalendar;

	UIManager.IndeterminateLoading loading = ui.createIndeterminateLoading(main.getMainFrame());

	int maxReturnsCount = 1000;

	private JTextField statisticTextField;
	private static boolean initial = false;

	public ImportManagerFrame() {

		removeAction = new RemoveAction(this);
		importAction = new ImportAction(this);

		try {
			jbInit();
			createExpandCollapseAllPopupMenu();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		BaseFrame.ensureVisible(this);

		centerPanel.setDividerLocation((int) (this.getHeight() * 0.6));
	}

	public void show() {
		if (!isVisible()) {
			initTable();
			super.show();
		}
	}

	private void jbInit() throws Exception {

		// Set title
		setTitle(ui.getString("fina2.returns.importManager.title"));

		initBaseComponents();
		getContentPane().setLayout(new BorderLayout());
		centerPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
		centerPanel.setToolTipText("");

		centerPanel.add(treeTablePanel, JSplitPane.TOP);
		centerPanel.add(detailsPanel, JSplitPane.BOTTOM);

		treeTable.getTree().addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent evt) {
				updateFrame();
			}
		});

		treeTablePanel.getViewport().add(treeTable);

		getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);
		getContentPane().add(eastPanel, java.awt.BorderLayout.EAST);
		getContentPane().add(northPanel, java.awt.BorderLayout.NORTH);
		getContentPane().add(southPanel, java.awt.BorderLayout.SOUTH);

		setFont(ui.getFont());
		// ---------------------------------
		// Filter panel initialization
		// ---------------------------------
		northPanel.setLayout(new BorderLayout());
		northPanel.add(filterPanel, BorderLayout.WEST);

		// Code
		codeList.setFont(ui.getFont());
		addFilterControl("fina2.code", codeList);

		// From
		fromText.setFont(ui.getFont());
		fromText.setPreferredSize(new Dimension(90, (int) codeList.getPreferredSize().getHeight()));
		addFilterControl("fina2.from", fromText);

		// To
		toText.setFont(ui.getFont());
		toText.setPreferredSize(new Dimension(90, (int) codeList.getPreferredSize().getHeight()));
		addFilterControl("fina2.to", toText);

		fromText.addFocusListener(this);
		toText.addFocusListener(this);

		// Import after
		uploadedAfterText.setFont(ui.getFont());
		uploadedAfterText.setPreferredSize(new Dimension(120, (int) codeList.getPreferredSize().getHeight()));
		addFilterControl("fina2.returns.import.uploadAfter", uploadedAfterText);

		// ---------------------------------------------------------------------
		// FI selection button
		fiSelectionButton = new JButton();
		fiSelectionButton.setIcon(Main.getIcon("banks.gif"));
		fiSelectionButton.setFont(ui.getFont());
		fiSelectionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				fiSelectionActionPerformed(evt);
			}
		});
		addFilterControl(null, fiSelectionButton);

		// Setting button text. The text is loaded from conf file.
		// See also: FISelectionDialog.
		updateFiSelectionButtonText();
		// ---------------------------------------------------------------------

		// Version
		versionList.setFont(ui.getFont());
		addFilterControl("fina2.version", versionList);

		// User
		userList.setFont(ui.getFont());
		addFilterControl("fina2.user", userList);

		// Status
		statusList.setMaximumRowCount(9);
		statusList.setFont(ui.getFont());
		addFilterControl("fina2.status", statusList);

		// Filter button
		filterButton.setIcon(ui.getIcon("fina2.filter"));
		filterButton.setFont(ui.getFont());
		filterButton.setText(ui.getString("fina2.filter"));
		filterButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				Thread thread = new Thread() {
					public void run() {
						loading.start();
						filterButtonActionPerformed(evt);
						loading.stop();
					}

				};
				thread.start();
			}
		});
		addFilterControl(null, filterButton);

		autoRefreshCheck.setFont(ui.getFont());
		autoRefreshCheck.setText(ui.getString("fina2.returns.import.autoRefresh"));
		autoRefreshCheck.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				autoRefreshCheckPerformed(e);
			}
		});

		addFilterControl(null, autoRefreshCheck);

		// --------------------------------
		// Details panel initialization
		// --------------------------------
		detailsPanel.setLayout(new BorderLayout());

		messageText.setEditable(false);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().add(messageText);

		detailsPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel detailBorderPanel = new JPanel();
		detailBorderPanel.setLayout(new BorderLayout());
		detailBorderPanel.setBorder(new EmptyBorder(new java.awt.Insets(10, 5, 10, 1)));

		JPanel detailControlsPanel = new JPanel();
		detailControlsPanel.setLayout(new GridBagLayout());

		detailBorderPanel.add(detailControlsPanel, BorderLayout.WEST);

		detailsPanel.add(detailBorderPanel, BorderLayout.NORTH);

		uploadTimeLabel.setText(ui.getString("fina2.returns.import.uploadTime"));
		detailControlsPanel.add(uploadTimeLabel, UIManager.getGridBagConstraints(0, 0, -1, -1, -1, -1, -1, GridBagConstraints.VERTICAL, new java.awt.Insets(0, 0, 0, 0)));

		uploadTimeText.setFont(ui.getFont());
		uploadTimeText.setEditable(false);
		uploadTimeText.setPreferredSize(new Dimension(130, (int) codeList.getPreferredSize().getHeight()));
		detailControlsPanel.add(uploadTimeText, UIManager.getGridBagConstraints(1, 0, -1, -1, -1, -1, -1, GridBagConstraints.VERTICAL, new java.awt.Insets(0, 10, 0, 0)));

		importStartLabel.setText(ui.getString("fina2.returns.import.start"));
		importStartLabel.setFont(ui.getFont());
		detailControlsPanel.add(importStartLabel, UIManager.getGridBagConstraints(2, 0, -1, -1, -1, -1, -1, GridBagConstraints.VERTICAL, new java.awt.Insets(0, 20, 0, 0)));

		importStartText.setFont(ui.getFont());
		importStartText.setEditable(false);
		importStartText.setPreferredSize(new Dimension(130, (int) codeList.getPreferredSize().getHeight()));
		detailControlsPanel.add(importStartText, UIManager.getGridBagConstraints(3, 0, -1, -1, -1, -1, -1, GridBagConstraints.VERTICAL, new java.awt.Insets(0, 10, 0, 0)));

		importEndLabel.setText(ui.getString("fina2.returns.import.end"));
		importEndLabel.setFont(ui.getFont());
		detailControlsPanel.add(importEndLabel, UIManager.getGridBagConstraints(4, 0, -1, -1, -1, -1, -1, GridBagConstraints.VERTICAL, new java.awt.Insets(0, 20, 0, 0)));

		importEndText.setFont(ui.getFont());
		importEndText.setEditable(false);
		importEndText.setPreferredSize(new Dimension(130, (int) codeList.getPreferredSize().getHeight()));
		detailControlsPanel.add(importEndText, UIManager.getGridBagConstraints(5, 0, -1, -1, -1, -1, -1, GridBagConstraints.VERTICAL, new java.awt.Insets(0, 10, 0, 0)));

		xlsFinaUserLabel.setText("Xls Uploaded");
		xlsFinaUserLabel.setFont(ui.getFont());
		detailControlsPanel.add(xlsFinaUserLabel, UIManager.getGridBagConstraints(6, 0, -1, -1, -1, -1, -1, GridBagConstraints.VERTICAL, new java.awt.Insets(0, 20, 0, 0)));

		xlsFinaUserText.setFont(ui.getFont());
		xlsFinaUserText.setEditable(false);
		xlsFinaUserText.setPreferredSize(new Dimension(130, (int) codeList.getPreferredSize().getHeight()));
		detailControlsPanel.add(xlsFinaUserText, UIManager.getGridBagConstraints(7, 0, -1, -1, -1, -1, -1, GridBagConstraints.VERTICAL, new java.awt.Insets(0, 10, 0, 0)));
		// --------------------------------
		// Buttons panel initialization
		// --------------------------------
		buttonsPanel.setLayout(new GridBagLayout());

		// Remove button
		eastPanel.setLayout(new BorderLayout());
		eastPanel.setBorder(new EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));

		JPanel panel = new JPanel();
		panel.add(buttonsPanel);

		eastPanel.add(panel, BorderLayout.NORTH);

		importButton.setFont(ui.getFont());
		importButton.setAction(importAction);
		importButton.setHorizontalAlignment(SwingConstants.LEFT);
		buttonsPanel.add(importButton, UIManager.getGridBagConstraints(0, 0, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 0, 0)));

		removeButton.setFont(ui.getFont());
		removeButton.setAction(removeAction);
		removeButton.setHorizontalAlignment(SwingConstants.LEFT);
		buttonsPanel.add(removeButton, UIManager.getGridBagConstraints(0, 1, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		if (ui.getHelpManager().IsHelpSystem()) {
			ui.getHelpManager().createDisplayHelpFromFocus(helpButton, "Import_Manager");
		} else {
			helpButton.setEnabled(false);
		}
		helpPanel.add(helpButton);

		closePrintPanel.add(printButton);

		closePrintPanel.add(closeButton);

		JPanel helpPanel = new JPanel();
		helpPanel.add(helpButton);

		statisticTextField = new JTextField();
		statisticTextField.setEditable(false);
		statisticTextField.setFont(ui.getFont());
		statisticTextField.setForeground(Color.GRAY);
		statisticTextField.setBorder(BorderFactory.createEmptyBorder());
		statisticTextField.setColumns(28);
		statisticTextField.setBackground(helpPanel.getBackground());

		JPanel statisticsPanel = new JPanel();
		statisticsPanel.add(statisticTextField);

		helpPanel.add(statisticsPanel);

		southPanel.setLayout(new BorderLayout());
		southPanel.add(helpPanel, BorderLayout.WEST);
		southPanel.add(closePrintPanel, BorderLayout.EAST);
		southPanel.add(new JPanel(), BorderLayout.NORTH);
	}

	void initTable() {

		try {
			// Load max returns count
			this.maxReturnsCount = ui.loadMaxReturnSize();
			// Return codes
			InitialContext jndi = main.getJndiContext();
			Object ref = jndi.lookup("fina2/returns/ReturnSession");
			ReturnSessionHome returnHome = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);

			ReturnSession returnSession = returnHome.create();
			if (!initial) {
				Vector code = new Vector();
				code.add(ui.getString("fina2.all"));
				code.addAll((Vector) returnSession.getReturnDefinitionsRows(main.getUserHandle(), main.getLanguageHandle()));
				codeList.setModel(new DefaultComboBoxModel(code));

				// Import statuses
				Vector status = new Vector(9);
				status.add(ui.getString("fina2.all"));
				status.add(ui.getString(ImportStatus.UPLOADED.getCode()));
				status.add(ui.getString(ImportStatus.IN_PROGRESS.getCode()));
				status.add(ui.getString(ImportStatus.QUEUED.getCode()));
				status.add(ui.getString(ImportStatus.REJECTED.getCode()));
				status.add(ui.getString(ImportStatus.IMPORTED.getCode()));
				status.add(ui.getString(ImportStatus.DECLINED.getCode()));
				status.add(ui.getString(ImportStatus.ERRORS.getCode()));
				statusList.setModel(new DefaultComboBoxModel(new Vector(status)));
			}
			// Return versions
			ref = jndi.lookup("fina2/returns/ReturnVersionSession");
			ReturnVersionSessionHome home = (ReturnVersionSessionHome) PortableRemoteObject.narrow(ref, ReturnVersionSessionHome.class);
			ReturnVersionSession session = home.create();

			if (!initial) {
				Collection versions = session.getReturnVersions(main.getLanguageHandle(), main.getUserHandle());
				Vector versionCodes = new Vector();
				versionCodes.add(ui.getString("fina2.all"));
				for (Iterator iter = versions.iterator(); iter.hasNext();) {
					ReturnVersion rv = (ReturnVersion) iter.next();
					versionCodes.add(rv.getCode());
				}
				versionList.setModel(new DefaultComboBoxModel(versionCodes));
			}
			// Users
			ImportManagerSession importMgrSession = getImportManager();
			if (!initial) {
				Vector users = new Vector();
				users.add(ui.getString("fina2.all"));
				users.addAll(importMgrSession.getImporterUsers(main.getUserHandle(), main.getLanguageHandle()));
				userList.setModel(new DefaultComboBoxModel(users));

				versionList.setSelectedItem(ui.getConfigValue("fina2.returns.ImportManagerFrame.versionCodeListItem", ui.getString("fina2.all")));
				codeList.setSelectedItem(ui.getConfigValue("fina2.returns.ImportManagerFrame.codeListItem", ui.getString("fina2.all")));
				statusList.setSelectedItem(ui.getConfigValue("fina2.returns.ImportManagerFrame.statusListItem", ui.getString("fina2.all")));
				userList.setSelectedItem(ui.getConfigValue("fina2.returns.ImportManagerFrame.userListItem", ui.getString("fina2.all")));
			}
			if (versionList.getSelectedItem() == null) {
				versionList.setSelectedIndex(0);
			}

			Date from = (Date) ui.getConfigValue("fina2.returns.ImportManagerFrame.fromText");
			Date to = (Date) ui.getConfigValue("fina2.returns.ImportManagerFrame.toText");
			Date importedAfter = (Date) ui.getConfigValue("fina2.returns.ImportManagerFrame.importAfterText");

			if (from == null || from.equals(new Date(0L))) {
				fromText.setText("");
			} else {
				fromText.setText(LocaleUtil.date2string(main.getLanguageHandle(), from));
			}

			if (to == null || to.equals(new Date(0L))) {
				toText.setText("");
			} else {
				toText.setText(LocaleUtil.date2string(main.getLanguageHandle(), to));
			}

			if (importedAfter == null || importedAfter.equals(new Date(0L))) {
				uploadedAfterText.setText("");
			} else {
				uploadedAfterText.setText(LocaleUtil.time2string(main.getLanguageHandle(), importedAfter));
			}

			int stat = statusList.getSelectedIndex();

			// Loading selected FI
			Set<Integer> selectedFI = (Set<Integer>) ui.getConfigValue("fina2.returns.importManager.selectedFI", new HashSet());

			String user = (userList.getSelectedItem() != null) ? userList.getSelectedItem().toString() : "";
			if (userList.getSelectedItem() instanceof TableRowImpl) {
				user = ((TableRowImpl) userList.getSelectedItem()).getPrimaryKey().toString();
			}

			// -----------------------------------------------------------------
			// Retrieving imported return documents

			Vector rows = (Vector) importMgrSession.getImportedDocuments(main.getUserHandle(), main.getLanguageHandle(), selectedFI, (codeList.getSelectedItem() != null) ? codeList.getSelectedItem()
					.toString() : null, --stat, (Date) ui.getConfigValue("fina2.returns.ImportManagerFrame.fromText", new Date(0L)), (Date) ui.getConfigValue(
					"fina2.returns.ImportManagerFrame.toText", new Date(0L)), (Date) ui.getConfigValue("fina2.returns.ImportManagerFrame.importAfterText", new Date(0L)), user, (versionList
					.getSelectedItem() != null) ? versionList.getSelectedItem().toString() : null, maxReturnsCount, FISelectionView.getFiSelected());

			for (TableRowImpl row : (Vector<TableRowImpl>) rows) {
				row.setValue(6, ui.getString(row.getValue(6)));
			}
			treeTablePanel.getViewport().setBackground(Color.white);
			if (selectedFI.size() > 0) {
				treeTableModel.setImportedReturns(rows);
				DefaultTreeCellRenderer tcr = new DefaultTreeCellRenderer();
				tcr.setOpenIcon(Main.main.ui.getIcon("fina2.folder"));
				tcr.setClosedIcon(Main.main.ui.getIcon("fina2.folder"));
				tcr.setLeafIcon(Main.main.ui.getIcon("fina2.return"));

				treeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				treeTable.getColumnModel().getColumn(0).setPreferredWidth(200);
				treeTable.setFont(ui.getFont());

				treeTable.getTree().setCellRenderer(tcr);
				treeTable.getTree().setRootVisible(rows.size() != 0);

				treeTable.updateUI();
			} else {
				rows.clear();
				treeTableModel.setImportedReturns(rows);
				treeTable.getTree().setRootVisible(false);
				treeTable.getTree().setVisible(false);
				treeTable.updateUI();
			}
			updateFrame();

			String returnsStatistic = rows.size() + "";
			if (rows.size() == this.maxReturnsCount) {
				ui.showMessageBox(main.getMainFrame(), ui.getString("fina2.returns.tooManyReturnsFound"));
				returnsStatistic += "+";
			}
			statisticTextField.setText("FIs[" + selectedFI.size() + "] | Packages[" + +treeTableModel.getChildCount(treeTableModel.getRoot()) + "]" + " | Returns[" + returnsStatistic + "]");
			saveState();
			initial = true;
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
	}

	private void saveState() throws RemoteException, ParseException {
		ui.putConfigValue("fina2.returns.ImportManagerFrame.codeListItem", (codeList.getSelectedItem() != null) ? codeList.getSelectedItem() : "");
		ui.putConfigValue("fina2.returns.ImportManagerFrame.statusListItem", (statusList.getSelectedItem() != null) ? statusList.getSelectedItem() : "");
		ui.putConfigValue("fina2.returns.ImportManagerFrame.userListItem", (userList.getSelectedItem() != null) ? userList.getSelectedItem() : "");
		ui.putConfigValue("fina2.returns.ImportManagerFrame.versionCodeListItem", (versionList.getSelectedItem() != null) ? versionList.getSelectedItem() : "");

		if (fromText.getText().trim().equals("")) {
			ui.putConfigValue("fina2.returns.ImportManagerFrame.fromText", new Date(0L));
		} else {
			ui.putConfigValue("fina2.returns.ImportManagerFrame.fromText", LocaleUtil.string2date(main.getLanguageHandle(), fromText.getText().trim()));
		}
		if (toText.getText().trim().equals("")) {
			ui.putConfigValue("fina2.returns.ImportManagerFrame.toText", new Date(0L));
		} else {
			ui.putConfigValue("fina2.returns.ImportManagerFrame.toText", LocaleUtil.string2date(main.getLanguageHandle(), toText.getText().trim()));
		}
		if (uploadedAfterText.getText().trim().equals("")) {
			ui.putConfigValue("fina2.returns.ImportManagerFrame.importAfterText", new Date(0L));
		} else {
			Date importedAfter = null;
			try {
				importedAfter = LocaleUtil.string2time(main.getLanguageHandle(), uploadedAfterText.getText().trim());
			} catch (ParseException ex1) {
				importedAfter = LocaleUtil.string2date(main.getLanguageHandle(), uploadedAfterText.getText().trim());
			}
			ui.putConfigValue("fina2.returns.ImportManagerFrame.importAfterText", importedAfter);
		}

	}

	public void createExpandCollapseAllPopupMenu() {

		/** Expand All */
		popupMenu = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem(ui.getString("fina2.expandAll"));
		menuItem.setActionCommand(ui.getString("fina2.expandAll"));
		menuItem.setIcon(ui.getIcon("fina2.expand_all"));
		menuItem.setFont(ui.getFont());
		menuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				expandAllActionPerformed(evt);
			}
		});
		popupMenu.add(menuItem);

		/** Collapse All */
		menuItem = new JMenuItem(ui.getString("fina2.collapseAll"));
		menuItem.setActionCommand(ui.getString("fina2.collapseAll"));
		menuItem.setIcon(ui.getIcon("fina2.collapse_all"));
		menuItem.setFont(ui.getFont());
		menuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				collapseAllActionPerformed(evt);
			}
		});
		popupMenu.add(menuItem);

		MouseListener popupListener = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				showPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				showPopup(e);
			}

			private void showPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		};
		treeTable.getTableHeader().setFont(ui.getFont());
		treeTable.addMouseListener(popupListener);
		treeTable.getTree().addMouseListener(popupListener);
		treeTable.getTableHeader().addMouseListener(popupListener);
	}

	private void updateFrame() {

		TreePath treePath = treeTable.getTree().getSelectionPath();
		if (treePath != null && treePath.getParentPath() != null) {
			boolean packageSelected = false;
			Object o = treePath.getLastPathComponent();
			Collection rows = null;
			if (o instanceof TableRow) {
				rows = new ArrayList();
				rows.add(o);
			} else {
				packageSelected = true;
				PackageInfo packageInfo = (PackageInfo) o;
				rows = packageInfo.getItems();
			}
			removeButton.setEnabled(canRemove(rows));

			if (rows.size() == 1 && packageSelected == false) {
				TableRow row = (TableRow) rows.iterator().next();
				importStartText.setText(row.getValue(7));
				importEndText.setText(row.getValue(8));
				messageText.setText(row.getValue(9));
				uploadTimeText.setText(row.getValue(10));
				xlsFinaUserText.setText(row.getValue(12));
			} else {
				importStartText.setText("");
				importEndText.setText("");
				messageText.setText("");
				uploadTimeText.setText("");
				xlsFinaUserText.setText("");
			}

		} else {
			removeButton.setEnabled(false);
			importStartText.setText("");
			importEndText.setText("");
			messageText.setText("");
			uploadTimeText.setText("");
		}
	}

	public Collection getSelectedDocuments() {
		Collection rows = null;
		TreePath treePath = treeTable.getTree().getSelectionPath();
		if (treePath != null && treePath.getParentPath() != null) {
			Object o = treePath.getLastPathComponent();
			if (o instanceof TableRow) {
				rows = new ArrayList();
				rows.add(o);
			} else {
				PackageInfo packageInfo = (PackageInfo) o;
				rows = packageInfo.getItems();
			}
		}
		return rows;
	}

	public boolean canRemove(Collection rows) {
		boolean result = true;
		for (Iterator iter = rows.iterator(); iter.hasNext();) {
			TableRow row = (TableRow) iter.next();
			String status = row.getValue(6);
			if (status == ui.getString(ImportStatus.IMPORTED.getCode()) || status == ui.getString(ImportStatus.IN_PROGRESS.getCode())) {
				result = false;
				break;
			} else if (status == ui.getString(ImportStatus.QUEUED.getCode())) {
				result = true;
				break;
			}
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void printButtonActionPerformed(java.awt.event.ActionEvent evt) {

		EJBTable table = new EJBTable();

		Vector colNames = new Vector();
		colNames.add(ui.getString("fina2.bank.bank"));
		colNames.add(ui.getString("fina2.returns.returnDefinition"));
		colNames.add("Type");
		colNames.add(ui.getString("fina2.user"));
		colNames.add("Submitter");
		colNames.add(ui.getString("fina2.status"));
		colNames.add(ui.getString("fina2.from"));
		colNames.add(ui.getString("fina2.to"));
		colNames.add(ui.getString("fina2.version"));
		colNames.add(ui.getString("fina2.returns.import.uploadTime"));
		colNames.add(ui.getString("fina2.returns.import.start"));
		colNames.add(ui.getString("fina2.returns.import.end"));
		colNames.add(ui.getString("fina2.returns.import.message"));

		Collection returns = treeTableModel.getReturns();

		table.initTable(colNames, correctionReturnRows(returns));

		TableReviewFrame printFrame = new TableReviewFrame();
		printFrame.show(getTitle(), table);
	}

	private Collection<TableRow> correctionReturnRows(Collection<TableRow> returns) {
		Collection<TableRow> result = new ArrayList<TableRow>();
		for (TableRow row : returns) {
			TableRow newRow = new TableRowImpl(row.getPrimaryKey(), 12);
			newRow.setValue(0, row.getValue(3));
			newRow.setValue(1, row.getValue(0));
			newRow.setValue(2, row.getValue(11));
			newRow.setValue(3, row.getValue(5));
			newRow.setValue(4, row.getValue(12));
			newRow.setValue(5, row.getValue(6));
			newRow.setValue(6, row.getValue(1));
			newRow.setValue(7, row.getValue(2));
			newRow.setValue(8, row.getValue(4));
			newRow.setValue(9, row.getValue(7));
			newRow.setValue(10, row.getValue(8));
			String message = row.getValue(9);
			message = message.replaceAll("\n", "");
			newRow.setValue(11, message);
			result.add(newRow);
		}
		return result;
	}

	public void filterButtonActionPerformed(java.awt.event.ActionEvent evt) {

		try {
			ui.putConfigValue("fina2.returns.ImportManagerFrame.codeListItem", (codeList.getSelectedItem() != null) ? codeList.getSelectedItem() : "");
			ui.putConfigValue("fina2.returns.ImportManagerFrame.statusListItem", (statusList.getSelectedItem() != null) ? statusList.getSelectedItem() : "");
			ui.putConfigValue("fina2.returns.ImportManagerFrame.userListItem", (userList.getSelectedItem() != null) ? userList.getSelectedItem() : "");
			ui.putConfigValue("fina2.returns.ImportManagerFrame.versionCodeListItem", (versionList.getSelectedItem() != null) ? versionList.getSelectedItem() : "");

			if (fromText.getText().trim().equals("")) {
				ui.putConfigValue("fina2.returns.ImportManagerFrame.fromText", new Date(0L));
			} else {
				ui.putConfigValue("fina2.returns.ImportManagerFrame.fromText", LocaleUtil.string2date(main.getLanguageHandle(), fromText.getText().trim()));
			}
			if (toText.getText().trim().equals("")) {
				ui.putConfigValue("fina2.returns.ImportManagerFrame.toText", new Date(0L));
			} else {
				ui.putConfigValue("fina2.returns.ImportManagerFrame.toText", LocaleUtil.string2date(main.getLanguageHandle(), toText.getText().trim()));
			}
			if (uploadedAfterText.getText().trim().equals("")) {
				ui.putConfigValue("fina2.returns.ImportManagerFrame.importAfterText", new Date(0L));
			} else {
				Date importedAfter = null;
				try {
					importedAfter = LocaleUtil.string2time(main.getLanguageHandle(), uploadedAfterText.getText().trim());
				} catch (ParseException ex1) {
					importedAfter = LocaleUtil.string2date(main.getLanguageHandle(), uploadedAfterText.getText().trim());
				}
				ui.putConfigValue("fina2.returns.ImportManagerFrame.importAfterText", importedAfter);
			}
			initTable();
		} catch (java.text.ParseException ex) {
			Main.errorHandler(main.getMainFrame(), ui.getString("fina2.title"), ui.getString("fina2.invalidDate"));
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
	}

	/** Action handler for bankSelectionButton */
	private void fiSelectionActionPerformed(ActionEvent evt) {

		try {
			User user = (User) main.getUserHandle().getEJBObject();
			UserPK userPK = (UserPK) user.getPrimaryKey();

			FISelectionDialog banksDialog = new FISelectionDialog(main.getMainFrame(), userPK, "fina2.returns.importManager.selectedFI", "fina2.returns.importManager.fiSelectionButtonText");

			banksDialog.setVisible(true);

			/*
			 * The selected banks are saved in the configuration file by
			 * BankSelectionView: see BankSelectionView.save().
			 */
			if (banksDialog.getDialogResult() == AbstractDialog.DialogResult.OK) {
				// User clicked OK button. Making necessary changes.
				updateFiSelectionButtonText();
			}
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
	}

	private void autoRefreshCheckPerformed(ItemEvent e) {

		boolean selected = e.getStateChange() == ItemEvent.SELECTED;
		codeList.setEnabled(!selected);
		fromText.setEditable(!selected);
		toText.setEditable(!selected);
		uploadedAfterText.setEditable(!selected);
		fiSelectionButton.setEnabled(!selected);
		versionList.setEnabled(!selected);
		userList.setEnabled(!selected);
		statusList.setEnabled(!selected);
		filterButton.setEnabled(!selected);
		treeTable.setEnabled(!selected);
		treeTable.setFocusable(!selected);

		if (selected) {
			timerTask = new TimerTask() {
				public void run() {
					filterButtonActionPerformed(null);
				}
			};
			// Start timer
			timer.schedule(timerTask, 0, 1000);
		} else {
			if (timerTask != null) {
				timerTask.cancel();
			}
		}
	}

	public ImportManagerSession getImportManager() throws Exception {
		InitialContext jndi = main.getJndiContext();
		Object ref = jndi.lookup("fina2/returns/ImportManagerSession");
		ImportManagerSessionHome importMgrHome = (ImportManagerSessionHome) PortableRemoteObject.narrow(ref, ImportManagerSessionHome.class);
		return importMgrHome.create();
	}

	public void monitorUploaded(Date uploadTime) throws RemoteException {

		codeList.setSelectedIndex(0);
		fromText.setText("");
		toText.setText("");
		uploadedAfterText.setText(LocaleUtil.time2string(main.getLanguageHandle(), uploadTime));
		versionList.setSelectedIndex(0);
		statusList.setSelectedIndex(0);
		userList.setSelectedIndex(1); // Always current user
		autoRefreshCheck.setSelected(true);

		/*
		 * ui.putConfigValue("fina2.returns.importManager.selectedFI", new
		 * HashSet());
		 */
		ui.putConfigValue("fina2.returns.importManager.fiSelectionButtonText", "");
		updateFiSelectionButtonText();

		expandAll(treeTable.getTree(), true);
	}

	/**
	 * Updates the text of fiSelectionButton. The text is loaded from conf file.
	 * 
	 * @see FISelectionDialog#defineFISelectionButtonText
	 */
	private void updateFiSelectionButtonText() {
		fiSelectionButton.setText((String) ui.getConfigValue("fina2.returns.importManager.fiSelectionButtonText"));
	}

	private void expandAllActionPerformed(java.awt.event.ActionEvent evt) {
		expandAll(treeTable.getTree(), true);
	}

	private void collapseAllActionPerformed(java.awt.event.ActionEvent evt) {
		expandAll(treeTable.getTree(), false);
	}

	private void expandAll(JTree tree, boolean expand) {
		for (int i = expand ? 0 : 1; i < tree.getRowCount(); i++) {
			if (expand) {
				tree.expandRow(i);
			} else {
				tree.collapseRow(i);
			}
		}
	}

	private void addFilterControl(String labelCode, Component control) {

		JLabel label = new JLabel();
		label.setFont(ui.getFont());

		if (labelCode != null) {
			label.setText(ui.getString(labelCode));
		} else {
			label.setText(" ");
		}

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(label, BorderLayout.NORTH);
		panel.add(control, BorderLayout.CENTER);

		filterPanel.add(panel, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(0, 5, 5, 0)));
	}

	@Override
	protected void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		ui.putConfigValue("fina2.returns.ImportManagerFrame.visible", new Boolean(false));
		setVisible(false);
		dispose();
	}

	@Override
	public void focusGained(FocusEvent e) {
		if (fcalendar != null && fcalendar.ACTIVE) {
			fcalendar.exit();
			return;
		}
		fcalendar = new FinaCalendar((JTextField) e.getComponent());

		// previously selected date
		java.util.Date selectedDate = fcalendar.parseDate(((JTextField) (e.getComponent())).getText());
		fcalendar.setSelectedDate(selectedDate);
		if (!fcalendar.ACTIVE) {
			fcalendar.start(e.getComponent());
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		// TODO Auto-generated method stub

	}

}

class ImportAction extends AbstractAction {

	private static Logger log = Logger.getLogger(ImportAction.class);

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;
	private ImportManagerFrame importMgrFrame;

	ImportAction(ImportManagerFrame importMgrFrame) {

		super();
		this.importMgrFrame = importMgrFrame;

		ui.loadIcon("fina2.returns.import", "import.gif");

		putValue(AbstractAction.NAME, ui.getString("fina2.returns.import"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.returns.import"));
	}

	public void actionPerformed(ActionEvent actionEvent) {

		String path = (String) ui.getConfigValue("fina2.returns.importPath");
		JFileChooser dlg = null;
		if (path == null) {
			dlg = new JFileChooser();
		} else {
			dlg = new JFileChooser(path);
		}
		dlg.setMultiSelectionEnabled(true);
		if (dlg.showOpenDialog(null) == dlg.CANCEL_OPTION) {
			return;
		}

		File[] files = dlg.getSelectedFiles();
		ui.putConfigValue("fina2.returns.importPath", files[0].getParent());
		String xmlname = null;
		try {
			LinkedList<byte[]> xmls = new LinkedList<byte[]>();
			for (int i = 0; i < files.length; i++) {
				FileInputStream fi = new FileInputStream(files[i]);
				xmlname = files[i].getName();
				byte[] xml = new byte[fi.available()];
				fi.read(xml);
				fi.close();

				xmls.add(xml);
			}

			ImportManagerSession importMgrSession = importMgrFrame.getImportManager();

			Date uploadTime = importMgrSession.uploadImportedDocuments(main.getUserHandle(), main.getLanguageHandle(), xmls);
			importMgrFrame.monitorUploaded(uploadTime);

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			ui.showMessageBox(null, ui.getString("fina2.title"), "XML " + xmlname + " is not valid,please check!");
		}
	}
}

class RemoveAction extends AbstractAction {

	private static Logger log = Logger.getLogger(RemoveAction.class);

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;
	private ImportManagerFrame importMgrFrame;

	RemoveAction(ImportManagerFrame importMgrFrame) {

		super();
		this.importMgrFrame = importMgrFrame;

		ui.loadIcon("fina2.delete", "delete.gif");

		putValue(AbstractAction.NAME, ui.getString("fina2.delete"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.delete"));
	}

	public void actionPerformed(ActionEvent actionEvent) {
		try {
			ImportManagerSession importMgrSession = importMgrFrame.getImportManager();

			ArrayList docIds = new ArrayList();
			for (TableRow row : (Collection<TableRow>) importMgrFrame.getSelectedDocuments()) {
				docIds.add((Integer) row.getPrimaryKey());
			}

			importMgrSession.deleteUploadedDocuments(docIds);
			importMgrFrame.filterButtonActionPerformed(null);

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			ui.showMessageBox(null, ui.getString("fina2.title"), ex.toString());
		}
	}
}
