package fina2.returns;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import fina2.BaseFrame;
import fina2.FinaTypeException;
import fina2.Main;
import fina2.actions.ImportManagerAction;
import fina2.bank.Bank;
import fina2.bank.BankHome;
import fina2.bank.BankPK;
import fina2.calendar.FinaCalendar;
import fina2.i18n.LocaleUtil;
import fina2.security.User;
import fina2.security.UserPK;
import fina2.security.UserSession;
import fina2.security.UserSessionHome;
import fina2.servergate.SecurityGate;
import fina2.ui.AbstractDialog;
import fina2.ui.ProcessDialog;
import fina2.ui.UIManager;
import fina2.ui.UIManager.IndeterminateLoading;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableReviewFrame;
import fina2.ui.table.TableRow;
import fina2.ui.table.TableRowImpl;
import fina2.ui.treetable.JTreeTable;

public class ReturnManagerFrame extends BaseFrame implements FocusListener {
	private Logger log = Logger.getLogger(getClass());
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	TreeTableModel treeTableModel = new TreeTableModel();
	JTreeTable treeTable = new JTreeTable(treeTableModel);

	private ReturnsAmendReviewAction amendAction;
	private ReturnsAmendReviewAction reviewAction;
	private ReturnInsertAction insertAction;
	private ReturnDeleteAction deleteAction;
	private Package2WorkBookAction package2WorkBookAction;

	boolean canAmend = false;
	boolean canDelete = false;
	boolean canReview = false;
	boolean canProcess = false;
	boolean canAccept = false;
	boolean canReset = false;
	boolean canReject = false;

	private HashSet<String> returnVersionsForAmend = null;

	private static final int MAX_RETURN_COUNT = 1000;

	private JPopupMenu popupMenu;

	private FinaCalendar fcalendar;

	private IndeterminateLoading loading;
	private int maxReturnsCount;
	private static boolean initial = false;

	public ReturnManagerFrame() {

		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.print", "print.gif");
		ui.loadIcon("fina2.refresh", "refresh.gif");
		ui.loadIcon("fina2.close", "cancel.gif");

		ui.loadIcon("fina2.amend", "amend.gif");
		ui.loadIcon("fina2.review", "review.gif");
		ui.loadIcon("fina2.review_all", "review_all.gif");
		ui.loadIcon("fina2.create", "insert.gif");
		ui.loadIcon("fina2.filter", "filter.gif");
		ui.loadIcon("fina2.delete", "delete.gif");
		ui.loadIcon("fina2.notes", "notes.gif");

		ui.loadIcon("fina2.process", "process.gif");

		ui.loadIcon("fina2.accept", "accept.gif");
		ui.loadIcon("fina2.reject", "reject.gif");
		ui.loadIcon("fina2.reset", "reset.gif");

		ui.loadIcon("fina2.folder", "folder.gif");
		ui.loadIcon("fina2.return", "return_table.gif");

		ui.loadIcon("fina2.expand_all", "expand_all.gif");
		ui.loadIcon("fina2.collapse_all", "collapse_all.gif");

		ui.loadIcon("package2workbook", "document-convert-icon.png");

		package2WorkBookAction = new Package2WorkBookAction(treeTable, this);

		amendAction = new ReturnsAmendReviewAction(treeTable, this, false);
		reviewAction = new ReturnsAmendReviewAction(treeTable, this, true);
		insertAction = new ReturnInsertAction(main.getMainFrame(), treeTable, treeTableModel, this);
		deleteAction = new ReturnDeleteAction(main.getMainFrame(), treeTable, treeTableModel, this);

		treeTable.getTree().addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent evt) {
				updateButtonsStates();
			}
		});

		initComponents();

		createExpandCollapseAllPopupMenu();

		scrollPane.setViewportView(treeTable);
		BaseFrame.ensureVisible(this);

		treeTable.getTableHeader().setFont(ui.getFont());
		treeTable.getTree().setFont(ui.getFont());

		loading = ui.createIndeterminateLoading(main.getMainFrame());
	}

	public void updateButtonsStates() {

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

			insertAction.setEnabled(canAmend);
			amendAction.setEnabled(canAmend && canAmend(rows));
			reviewAction.setEnabled(canAmend || canReview);
			deleteAction.setEnabled(canDelete && canDelete(rows));
			processButton.setEnabled(canProcess(rows));
			printButton.setEnabled(canAmend || canReview);

			notesButton.setEnabled((canAmend || canReview) && packageSelected == false);
			resetButton.setEnabled(canReset(rows));
			acceptButton.setEnabled(canAccept(rows));
			rejectButton.setEnabled(canReject(rows));
		} else {
			insertAction.setEnabled(canAmend);
			amendAction.setEnabled(false);
			reviewAction.setEnabled(false);
			deleteAction.setEnabled(false);
			processButton.setEnabled(false);
			printButton.setEnabled(false);
			notesButton.setEnabled(false);
			resetButton.setEnabled(false);
			acceptButton.setEnabled(false);
			rejectButton.setEnabled(false);
		}
	}

	public boolean canAmend(Collection rows) {

		boolean result = true;
		for (Iterator iter = rows.iterator(); iter.hasNext();) {
			TableRow row = (TableRow) iter.next();
			String status = row.getValue(6);

			if (status == ui.getString(ReturnConstants.STATUS_ACCEPTED_STR) || status == ui.getString(ReturnConstants.STATUS_VALIDATED_STR)
			// || !canAmendBank(row)
					|| !canAmendReturnVersion(row)) {

				result = false;
				break;
			}
		}

		return result;
	}

	public boolean canProcess(Collection rows) {

		boolean result = true;
		for (Iterator iter = rows.iterator(); iter.hasNext();) {
			TableRow row = (TableRow) iter.next();
			String status = row.getValue(6);

			if (status == ui.getString(ReturnConstants.STATUS_VALIDATED_STR) || status == ui.getString(ReturnConstants.STATUS_ACCEPTED_STR)
					|| status == ui.getString(ReturnConstants.STATUS_REJECTED_STR) || status == ui.getString(ReturnConstants.STATUS_RESETED_STR)
					|| status == ui.getString(ReturnConstants.STATUS_CREATED_STR) || status == ui.getString(ReturnConstants.STATUS_IMPORTED_STR)
					|| status == ui.getString(ReturnConstants.STATUS_LOADED_STR) || status == ui.getString(ReturnConstants.STATUS_ERRORS_STR)) {
				result = false;
				break;
			}
		}

		return result && canProcess;
	}

	/**
	 * Checks whether a current user can amend return version of given table row
	 */
	private boolean canAmendReturnVersion(TableRow row) {

		String versionCode = row.getValue(5);
		return returnVersionsForAmend.contains(versionCode);
	}

	public boolean canProcess(Collection rows, TableRow excludeRow) {

		boolean result = true;
		for (Iterator iter = rows.iterator(); iter.hasNext();) {
			TableRow row = (TableRow) iter.next();
			String status = row.getValue(6);

			if (row == excludeRow) {
				continue;
			}

			if (status == ui.getString(ReturnConstants.STATUS_VALIDATED_STR) || status == ui.getString(ReturnConstants.STATUS_ACCEPTED_STR)
					|| status == ui.getString(ReturnConstants.STATUS_REJECTED_STR) || status == ui.getString(ReturnConstants.STATUS_RESETED_STR)
					|| status == ui.getString(ReturnConstants.STATUS_CREATED_STR) || status == ui.getString(ReturnConstants.STATUS_IMPORTED_STR)
					|| status == ui.getString(ReturnConstants.STATUS_LOADED_STR) || status == ui.getString(ReturnConstants.STATUS_ERRORS_STR)) {
				result = false;
				break;
			}
		}

		return result && canProcess;
	}

	public boolean canDelete(Collection rows) {

		boolean result = true;
		for (Iterator iter = rows.iterator(); iter.hasNext();) {
			TableRow row = (TableRow) iter.next();
			String status = row.getValue(6);

			if (status == ui.getString(ReturnConstants.STATUS_VALIDATED_STR) || status == ui.getString(ReturnConstants.STATUS_ACCEPTED_STR)) {
				result = false;
				break;
			}
		}

		return result;
	}

	public boolean canAccept(Collection rows) {

		boolean result = true;
		for (Iterator iter = rows.iterator(); iter.hasNext();) {
			TableRow row = (TableRow) iter.next();
			String status = row.getValue(6);

			if (status == ui.getString(ReturnConstants.STATUS_VALIDATED_STR) || status == ui.getString(ReturnConstants.STATUS_ACCEPTED_STR)
					|| status == ui.getString(ReturnConstants.STATUS_REJECTED_STR) || status == ui.getString(ReturnConstants.STATUS_RESETED_STR)
					|| status == ui.getString(ReturnConstants.STATUS_AMENDED_STR) || status == ui.getString(ReturnConstants.STATUS_CREATED_STR)
					|| status == ui.getString(ReturnConstants.STATUS_IMPORTED_STR) || status == ui.getString(ReturnConstants.STATUS_LOADED_STR)
					|| status == ui.getString(ReturnConstants.STATUS_ERRORS_STR)) {
				result = false;
				break;
			}
		}

		return result && canAccept;
	}

	public boolean canReject(Collection rows) {

		boolean result = true;
		for (Iterator iter = rows.iterator(); iter.hasNext();) {
			TableRow row = (TableRow) iter.next();
			String status = row.getValue(6);

			if (status == ui.getString(ReturnConstants.STATUS_VALIDATED_STR) || status == ui.getString(ReturnConstants.STATUS_ACCEPTED_STR)
					|| status == ui.getString(ReturnConstants.STATUS_REJECTED_STR) || status == ui.getString(ReturnConstants.STATUS_RESETED_STR)
					|| status == ui.getString(ReturnConstants.STATUS_CREATED_STR) || status == ui.getString(ReturnConstants.STATUS_IMPORTED_STR)
					|| status == ui.getString(ReturnConstants.STATUS_LOADED_STR) || status == ui.getString(ReturnConstants.STATUS_ERRORS_STR)) {
				result = false;
				break;
			}
		}

		return result && canReject;
	}

	public boolean canReset(Collection rows) {

		boolean result = true;
		for (Iterator iter = rows.iterator(); iter.hasNext();) {
			TableRow row = (TableRow) iter.next();
			String status = row.getValue(6);

			if (status == ui.getString(ReturnConstants.STATUS_RESETED_STR) || status == ui.getString(ReturnConstants.STATUS_AMENDED_STR)
					|| status == ui.getString(ReturnConstants.STATUS_PROCESSED_STR) || status == ui.getString(ReturnConstants.STATUS_CREATED_STR)
					|| status == ui.getString(ReturnConstants.STATUS_IMPORTED_STR) || status == ui.getString(ReturnConstants.STATUS_LOADED_STR)
					|| status == ui.getString(ReturnConstants.STATUS_ERRORS_STR)) {
				result = false;
				break;
			}
		}

		return result && canReset;
	}

	/*
	 * public boolean canAmendBank(TableRow row) {
	 * 
	 * return (banksForAmend.get(row.getValue(4)) != null); }
	 */

	@SuppressWarnings({ "unused", "unchecked" })
	void initTable() {

		try {
			// Load max returns count
			this.maxReturnsCount = ui.loadMaxReturnSize();
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/returns/ReturnSession");
			ReturnSessionHome returnHome = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);

			ReturnSession returnSession = returnHome.create();
			if (!initial) {
				Vector code = new Vector();
				code.add(ui.getString("fina2.all"));
				code.addAll((Vector) returnSession.getReturnDefinitionsRows(main.getUserHandle(), main.getLanguageHandle()));
				codeList.setModel(new javax.swing.DefaultComboBoxModel(code));

				Vector type = new Vector();
				type.add(ui.getString("fina2.all"));
				type.addAll((Vector) returnSession.getReturnTypesRows(main.getUserHandle(), main.getLanguageHandle()));
				typeList.setModel(new javax.swing.DefaultComboBoxModel(type));

				Vector status = new Vector(7);
				status.add(ui.getString("fina2.all"));
				status.add(ui.getString(ReturnConstants.STATUS_CREATED_STR));
				status.add(ui.getString(ReturnConstants.STATUS_AMENDED_STR));
				status.add(ui.getString(ReturnConstants.STATUS_IMPORTED_STR));
				status.add(ui.getString(ReturnConstants.STATUS_PROCESSED_STR));
				status.add(ui.getString(ReturnConstants.STATUS_RESETED_STR));
				status.add(ui.getString(ReturnConstants.STATUS_ACCEPTED_STR));
				status.add(ui.getString(ReturnConstants.STATUS_REJECTED_STR));
				status.add(ui.getString(ReturnConstants.STATUS_ERRORS_STR));
				// Collections.sort(status);
				statusList.setModel(new javax.swing.DefaultComboBoxModel(new Vector(status)));
			}
			ref = jndi.lookup("fina2/returns/ReturnVersionSession");
			ReturnVersionSessionHome home = (ReturnVersionSessionHome) PortableRemoteObject.narrow(ref, ReturnVersionSessionHome.class);
			ReturnVersionSession session = home.create();

			// -----------------------------------------------------------------
			// Return versions

			// Adding to the filter combobox
			if (!initial) {
				Collection versions = session.getReturnVersions(main.getLanguageHandle(), main.getUserHandle());

				Vector versionCodes = new Vector();

				versionCodes.add(ui.getString("fina2.all"));

				for (Iterator iter = versions.iterator(); iter.hasNext();) {
					ReturnVersion rv = (ReturnVersion) iter.next();
					versionCodes.add(rv.getCode());
				}

				versionCombo.setModel(new javax.swing.DefaultComboBoxModel(versionCodes));
			}
			// For amend only
			Collection versionsForAmend = session.getReturnVersions(main.getLanguageHandle(), main.getUserHandle(), true);

			returnVersionsForAmend = new HashSet<String>();

			for (Object item : versionsForAmend) {
				ReturnVersion rv = (ReturnVersion) item;
				returnVersionsForAmend.add(rv.getCode());
				System.out.println(rv.getCode());
			}
			// -----------------------------------------------------------------

			ref = jndi.lookup("fina2/security/UserSession");
			UserSessionHome userHome = (UserSessionHome) PortableRemoteObject.narrow(ref, UserSessionHome.class);
			UserSession userSession = userHome.create();
			if (!initial) {
				versionCombo.setSelectedItem(ui.getConfigValue("fina2.returns.ReturnManagerFrame.versionCodeListItem"));
				codeList.setSelectedItem(ui.getConfigValue("fina2.returns.ReturnManagerFrame.codeListItem"));
				statusList.setSelectedItem(ui.getConfigValue("fina2.returns.ReturnManagerFrame.statusListItem"));
				typeList.setSelectedItem(ui.getConfigValue("fina2.returns.ReturnManagerFrame.typeListItem"));
			}
			if (versionCombo.getSelectedItem() == null) {
				versionCombo.setSelectedIndex(0);
			}
			if (codeList.getSelectedItem() == null) {
				codeList.setSelectedIndex(0);
			}
			if (statusList.getSelectedItem() == null) {
				statusList.setSelectedIndex(0);
			}
			if (typeList.getSelectedItem() == null) {
				typeList.setSelectedIndex(0);
			}
			if (!initial) {
				Date fromDate = (Date) ui.getConfigValue("fina2.returns.ReturnManagerFrame.fromText");
				if (fromDate != null) {
					if (fromDate.equals(new Date(0L))) {
						fromText.setText("");
					} else {
						fromText.setText(LocaleUtil.date2string(main.getLanguageHandle(), (Date) ui.getConfigValue("fina2.returns.ReturnManagerFrame.fromText")));
					}
				} else {
					fromDate = new Date();
				}

				Date toDate = (Date) ui.getConfigValue("fina2.returns.ReturnManagerFrame.toText");
				if (toDate != null) {
					if (toDate.equals(new Date(0L))) {
						toText.setText("");
					} else {
						toText.setText(LocaleUtil.date2string(main.getLanguageHandle(), (Date) ui.getConfigValue("fina2.returns.ReturnManagerFrame.toText")));
					}

				} else {
					toDate = new Date();
				}
			}
			saveState();

			int stat = statusList.getSelectedIndex();
			if (stat > 4)
				stat++;

			// Loading selected FI
			Set<Integer> selectedFI = (Set<Integer>) ui.getConfigValue("fina2.returns.selectedFI");

			// Get Current userPK
			UserPK userPK = (fina2.security.UserPK) ((fina2.security.User) main.getUserHandle().getEJBObject()).getPrimaryKey();
			UserSession uSession = SecurityGate.getUserSession();

			// Get current user's banks id
			List<Integer> userBanksId = uSession.getUserBanksId(userPK);
			// Current selection list
			Set<Integer> currentSelBanksId = new HashSet<Integer>();

			// If Current User doesn't have no one banks permission
			if (selectedFI != null) {
				if (userBanksId.size() < 1) {
					selectedFI.clear();
				} else {
					/* add bank id, while user has permission of bank */
					for (Integer id : selectedFI) {
						if (userBanksId.contains(id)) {
							// add Selection bank id in currentSelBanksId list.
							currentSelBanksId.add(id);
							// FI Selection Button set correct Text
							fiSelectionButton.setText(ui.getString("fina2.security.fi"));
						}
					}
				}
			}
			// Retrieving return rows from the server
			@SuppressWarnings("rawtypes")
			Vector rows = (Vector) returnSession.getReturnsRows(main.getUserHandle(), main.getLanguageHandle(), currentSelBanksId, (codeList.getSelectedItem() != null) ? codeList.getSelectedItem()
					.toString() : "", stat, (Date) ui.getConfigValue("fina2.returns.ReturnManagerFrame.fromText"), (Date) ui.getConfigValue("fina2.returns.ReturnManagerFrame.toText"), (typeList
					.getSelectedItem() != null) ? typeList.getSelectedItem().toString() : "", (versionCombo.getSelectedItem() != null) ? versionCombo.getSelectedItem().toString() : "",
					this.maxReturnsCount);

			String created = ui.getString(ReturnConstants.STATUS_CREATED_STR);
			String amended = ui.getString(ReturnConstants.STATUS_AMENDED_STR);
			String imported = ui.getString(ReturnConstants.STATUS_IMPORTED_STR);
			String processed = ui.getString(ReturnConstants.STATUS_PROCESSED_STR);
			String reseted = ui.getString(ReturnConstants.STATUS_RESETED_STR);
			String accepted = ui.getString(ReturnConstants.STATUS_ACCEPTED_STR);
			String rejected = ui.getString(ReturnConstants.STATUS_REJECTED_STR);
			String errors = ui.getString(ReturnConstants.STATUS_ERRORS_STR);

			for (java.util.Iterator iter = rows.iterator(); iter.hasNext();) {
				TableRowImpl row = (TableRowImpl) iter.next();

				switch (Integer.valueOf(row.getValue(6)).intValue()) {
				case ReturnConstants.STATUS_CREATED:
					row.setValue(6, created);
					break;
				case ReturnConstants.STATUS_AMENDED:
					row.setValue(6, amended);
					break;
				case ReturnConstants.STATUS_IMPORTED:
					row.setValue(6, imported);
					break;
				case ReturnConstants.STATUS_PROCESSED:
					row.setValue(6, processed);
					break;
				case ReturnConstants.STATUS_RESETED:
					row.setValue(6, reseted);
					break;
				case ReturnConstants.STATUS_ACCEPTED:
					row.setValue(6, accepted);
					break;
				case ReturnConstants.STATUS_REJECTED:
					row.setValue(6, rejected);
					break;
				case ReturnConstants.STATUS_ERRORS:
					row.setValue(6, errors);
					break;
				default:
					row.setValue(6, " ");
				}
			}

			treeTableModel.setReturns(rows);

			scrollPane.getViewport().setBackground(Color.white);

			DefaultTreeCellRenderer tcr = new DefaultTreeCellRenderer();
			tcr.setOpenIcon(Main.main.ui.getIcon("fina2.folder"));
			tcr.setClosedIcon(Main.main.ui.getIcon("fina2.folder"));
			tcr.setLeafIcon(Main.main.ui.getIcon("fina2.return"));

			treeTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			treeTable.getColumnModel().getColumn(0).setPreferredWidth(200);
			treeTable.setFont(ui.getFont());

			treeTable.getTree().setCellRenderer(tcr);
			treeTable.getTree().setRootVisible(rows.size() != 0);

			treeTable.updateUI();
			updateButtonsStates();

			String returnsStatistic = rows.size() + "";
			if (rows.size() == this.maxReturnsCount) {
				ui.showMessageBox(main.getMainFrame(), ui.getString("fina2.returns.tooManyReturnsFound"));
				returnsStatistic += "+";
			}

			if (selectedFI != null)
				statisticTextField.setText("FIs[" + selectedFI.size() + "] | Packages[" + +treeTableModel.getChildCount(treeTableModel.getRoot()) + "]" + " | Returns[" + returnsStatistic + "]");

			treeTable.getTree().updateUI();
			treeTable.updateUI();
			initial = true;
		} catch (Exception e) {
			Main.generalErrorHandler(e);
			ui.putConfigValue("fina2.returns.ReturnManagerFrame.visible", new Boolean(false));
		}
	}

	private void saveState() throws RemoteException, ParseException {
		ui.putConfigValue("fina2.returns.ReturnManagerFrame.codeListItem", codeList.getSelectedItem());
		ui.putConfigValue("fina2.returns.ReturnManagerFrame.statusListItem", statusList.getSelectedItem());
		ui.putConfigValue("fina2.returns.ReturnManagerFrame.typeListItem", typeList.getSelectedItem());
		ui.putConfigValue("fina2.returns.ReturnManagerFrame.versionCodeListItem", versionCombo.getSelectedItem());
		if (fromText.getText().trim().equals("")) {
			ui.putConfigValue("fina2.returns.ReturnManagerFrame.fromText", new Date(0L));
		} else
			ui.putConfigValue("fina2.returns.ReturnManagerFrame.fromText", LocaleUtil.string2date(main.getLanguageHandle(), fromText.getText().trim()));
		if (toText.getText().trim().equals("")) {
			ui.putConfigValue("fina2.returns.ReturnManagerFrame.toText", new Date(0L));
		} else
			ui.putConfigValue("fina2.returns.ReturnManagerFrame.toText", LocaleUtil.string2date(main.getLanguageHandle(), toText.getText().trim()));

	}

	public void show() {
		if (isVisible())
			return;
		try {
			fina2.security.User user = (fina2.security.User) main.getUserHandle().getEJBObject();
			canAmend = user.hasPermission("fina2.returns.amend");
			canDelete = user.hasPermission("fina2.returns.delete");
			canReview = user.hasPermission("fina2.returns.review");
			canProcess = user.hasPermission("fina2.returns.process");
			canAccept = user.hasPermission("fina2.returns.accept");
			canReset = user.hasPermission("fina2.returns.reset");
			canReject = user.hasPermission("fina2.returns.reject");
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		amendAction.setEnabled(canAmend);
		amendButton.setVisible(canAmend);

		reviewAction.setEnabled(canReview || canAmend);
		reviewButton.setVisible(canReview || canAmend);

		insertAction.setEnabled(canAmend);
		createButton.setVisible(canAmend);

		deleteAction.setEnabled(canDelete);
		deleteButton.setVisible(canDelete);

		printButton.setVisible(canAmend || canReview);
		notesButton.setVisible(canAmend || canReview);

		processButton.setEnabled(canProcess);
		processButton.setVisible(canProcess);

		acceptButton.setVisible(canAccept);
		resetButton.setVisible(canReset);
		rejectButton.setVisible(canReject);

		initTable();

		super.show();

	}

	private void initComponents() { // GEN-BEGIN:initComponents
		jPanel3 = new javax.swing.JPanel();
		jPanel7 = new javax.swing.JPanel();
		jPanel8 = new javax.swing.JPanel();
		jPanel4 = new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		scrollPane = new javax.swing.JScrollPane();
		jPanel1 = new javax.swing.JPanel();
		versionCombo = new javax.swing.JComboBox();
		fromText = new javax.swing.JTextField();
		toText = new javax.swing.JTextField();
		codeList = new javax.swing.JComboBox();
		statusList = new javax.swing.JComboBox();
		typeList = new javax.swing.JComboBox();
		filterButton = new javax.swing.JButton();
		jPanel5 = new javax.swing.JPanel();
		jPanel6 = new javax.swing.JPanel();
		jPanel10 = new javax.swing.JPanel();
		createButton = new javax.swing.JButton();
		amendButton = new javax.swing.JButton();
		deleteButton = new javax.swing.JButton();
		processButton = new javax.swing.JButton();
		resetButton = new javax.swing.JButton();
		acceptButton = new javax.swing.JButton();
		rejectButton = new javax.swing.JButton();
		reviewButton = new javax.swing.JButton();
		notesButton = new javax.swing.JButton();
		jPanel9 = new javax.swing.JPanel();
		jLabel6 = new javax.swing.JLabel();
		statisticTextField = new JTextField();

		setTitle(ui.getString("fina2.returns.returnManagerAction"));
		setFont(ui.getFont());
		initBaseComponents();

		jPanel3.setLayout(new java.awt.BorderLayout());

		if (ui.getHelpManager().IsHelpSystem()) {
			ui.getHelpManager().createDisplayHelpFromFocus(helpButton, "Return_Manager");
		} else {
			helpButton.setEnabled(false);
		}
		jPanel7.add(helpButton);

		statisticTextField.setEditable(false);
		statisticTextField.setFont(ui.getFont());
		statisticTextField.setForeground(Color.GRAY);
		statisticTextField.setBorder(BorderFactory.createEmptyBorder());
		statisticTextField.setColumns(28);
		statisticTextField.setBackground(jPanel7.getBackground());

		JPanel statisticsPanel = new JPanel();
		statisticsPanel.add(statisticTextField);

		jPanel7.add(statisticsPanel);

		jPanel3.add(jPanel7, java.awt.BorderLayout.WEST);

		jPanel8.add(printButton);

		jPanel8.add(refreshButton);

		jPanel8.add(closeButton);

		jPanel3.add(jPanel8, java.awt.BorderLayout.EAST);

		jPanel3.add(jPanel4, java.awt.BorderLayout.NORTH);

		getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

		jPanel2.setLayout(new java.awt.BorderLayout());

		scrollPane.setFont(ui.getFont());
		jPanel2.add(scrollPane, java.awt.BorderLayout.CENTER);

		jPanel1.setLayout(new java.awt.GridBagLayout());

		jPanel1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));

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
		versionCombo.setFont(ui.getFont());
		addFilterControl("fina2.version", versionCombo);

		// Status
		statusList.setMaximumRowCount(9);
		statusList.setFont(ui.getFont());
		addFilterControl("fina2.status", statusList);

		// Type
		typeList.setFont(ui.getFont());
		typeList.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				// TODO
			}
		});
		addFilterControl("fina2.type", typeList);

		// Filter button
		filterButton.setIcon(ui.getIcon("fina2.filter"));
		filterButton.setFont(ui.getFont());
		filterButton.setText(ui.getString("fina2.filter"));
		filterButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				Thread t = new Thread() {
					public void run() {
						loading.start();
						filterButtonActionPerformed(evt);
						loading.stop();

					}
				};
				t.start();
			}
		});
		addFilterControl(null, filterButton);

		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BorderLayout());

		northPanel.add(jPanel1, BorderLayout.WEST);

		jPanel2.add(northPanel, java.awt.BorderLayout.NORTH);

		getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

		jPanel5.setLayout(new java.awt.BorderLayout());

		jPanel5.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 5, 1, 5)));
		jPanel10.setLayout(new java.awt.GridBagLayout());

		// Create button
		createButton.setFont(ui.getFont());
		createButton.setAction(insertAction);
		createButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel10.add(createButton, UIManager.getGridBagConstraints(0, 0, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 0, 0)));

		// Amend button
		amendButton.setFont(ui.getFont());
		amendButton.setAction(amendAction);
		amendButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel10.add(amendButton, UIManager.getGridBagConstraints(0, 1, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		// Process button
		processButton.setIcon(ui.getIcon("fina2.process"));
		processButton.setFont(ui.getFont());
		processButton.setText(ui.getString("fina2.returns.process"));
		processButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		processButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				processButtonActionPerformed(evt);
			}
		});
		jPanel10.add(processButton, UIManager.getGridBagConstraints(0, 2, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		// Review button
		reviewButton.setIcon(ui.getIcon("fina2.review"));
		reviewButton.setFont(ui.getFont());
		reviewButton.setText(ui.getString("fina2.review"));
		reviewButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

		reviewButton.setAction(reviewAction);
		jPanel10.add(reviewButton, UIManager.getGridBagConstraints(0, 3, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		// Delete button
		deleteButton.setFont(ui.getFont());
		deleteButton.setAction(deleteAction);
		deleteButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel10.add(deleteButton, UIManager.getGridBagConstraints(0, 4, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		// Accept button
		acceptButton.setIcon(ui.getIcon("fina2.accept"));
		acceptButton.setFont(ui.getFont());
		acceptButton.setText(ui.getString("fina2.returns.accept"));
		acceptButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		acceptButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				acceptButtonActionPerformed(evt);
			}
		});
		jPanel10.add(acceptButton, UIManager.getGridBagConstraints(0, 5, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(15, 0, 0, 0)));

		// Reject button
		rejectButton.setIcon(ui.getIcon("fina2.reject"));
		rejectButton.setFont(ui.getFont());
		rejectButton.setText(ui.getString("fina2.returns.reject"));
		rejectButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		rejectButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rejectButtonActionPerformed(evt);
			}
		});
		jPanel10.add(rejectButton, UIManager.getGridBagConstraints(0, 6, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		// Reset button
		resetButton.setIcon(ui.getIcon("fina2.reset"));
		resetButton.setFont(ui.getFont());
		resetButton.setText(ui.getString("fina2.returns.reset"));
		resetButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		resetButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				resetButtonActionPerformed(evt);
			}
		});
		jPanel10.add(resetButton, UIManager.getGridBagConstraints(0, 7, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		// Notes button
		notesButton.setIcon(ui.getIcon("fina2.notes"));
		notesButton.setFont(ui.getFont());
		notesButton.setText(ui.getString("fina2.returns.statusHistory"));
		notesButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		notesButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				notesButtonActionPerformed(evt);
			}
		});
		jPanel10.add(notesButton, UIManager.getGridBagConstraints(0, 8, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		jPanel6.add(jPanel10);

		jPanel5.add(jPanel6, java.awt.BorderLayout.CENTER);

		jPanel9.setLayout(new java.awt.GridBagLayout());

		jPanel9.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));
		jLabel6.setText(" ");
		jLabel6.setFont(ui.getFont());
		jPanel9.add(jLabel6, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, -1, -1, new java.awt.Insets(10, 0, 10, 0)));

		jPanel5.add(jPanel9, java.awt.BorderLayout.NORTH);

		getContentPane().add(jPanel5, java.awt.BorderLayout.EAST);

	} // GEN-END:initComponents

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

		jPanel1.add(panel, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(0, 5, 5, 0)));
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
			JMenuItem package2WorkBookMenuItem = new JMenuItem(ui.getString("fina2.return.package2workbook"));
			JPopupMenu.Separator separator = new JPopupMenu.Separator();

			public void mousePressed(MouseEvent e) {
				showPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				showPopup(e);
			}

			private void showPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					addPackage2WorkBookItem();
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			private void addPackage2WorkBookItem() {
				popupMenu.remove(package2WorkBookMenuItem);
				popupMenu.remove(separator);

				Object o = treeTable.getTree().getLastSelectedPathComponent();

				if (o != null) {
					if (!(o instanceof TableRow)) {
						package2WorkBookMenuItem.setAction(package2WorkBookAction);
						package2WorkBookMenuItem.setIcon(ui.getIcon("package2workbook"));
						package2WorkBookMenuItem.setFont(ui.getFont());
						package2WorkBookMenuItem.setActionCommand(ui.getString("fina2.return.package2workbook"));
						popupMenu.add(separator);
						popupMenu.add(package2WorkBookMenuItem);
					}
				}
			}
		};

		treeTable.addMouseListener(popupListener);
		treeTable.getTree().addMouseListener(popupListener);
		treeTable.getTableHeader().addMouseListener(popupListener);
	}

	/**
	 * Updates the text of fiSelectionButton. The text is loaded from conf file.
	 * 
	 * @see FISelectionDialog#defineFISelectionButtonText
	 */
	private void updateFiSelectionButtonText() {
		String text = (String) ui.getConfigValue("fina2.returns.fiSelectionButtonText");
		fiSelectionButton.setText(text);
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

	private void notesButtonActionPerformed(java.awt.event.ActionEvent evt) {
		TableRow row = (TableRow) treeTable.getTree().getLastSelectedPathComponent();

		NotesReviewDialog notesReviewDialog = new NotesReviewDialog(main.getMainFrame(), true);
		notesReviewDialog.show((ReturnPK) row.getPrimaryKey(), row);
	}

	protected void printButtonActionPerformed(java.awt.event.ActionEvent evt) {

		EJBTable table = new EJBTable();

		Vector colNames = new Vector();
		colNames.add(ui.getString("fina2.bank.bank"));
		colNames.add(ui.getString("fina2.from"));
		colNames.add(ui.getString("fina2.to"));
		colNames.add(ui.getString("fina2.returns.returnDefinition"));
		colNames.add(ui.getString("fina2.code"));
		colNames.add(ui.getString("fina2.version"));
		colNames.add(ui.getString("fina2.status"));
		colNames.add(ui.getString("fina2.type"));

		table.initTable(colNames, treeTableModel.getReturns());

		TableReviewFrame printFrame = new TableReviewFrame();
		printFrame.show(getTitle(), table);
	}

	// TODO "synchronized" - es aucilebelia!
	private synchronized void filterButtonActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			ui.putConfigValue("fina2.returns.ReturnManagerFrame.codeListItem", codeList.getSelectedItem());
			ui.putConfigValue("fina2.returns.ReturnManagerFrame.statusListItem", statusList.getSelectedItem());
			ui.putConfigValue("fina2.returns.ReturnManagerFrame.typeListItem", typeList.getSelectedItem());
			ui.putConfigValue("fina2.returns.ReturnManagerFrame.versionCodeListItem", versionCombo.getSelectedItem());
			if (fromText.getText().trim().equals("")) {
				ui.putConfigValue("fina2.returns.ReturnManagerFrame.fromText", new Date(0L));
			} else
				ui.putConfigValue("fina2.returns.ReturnManagerFrame.fromText", LocaleUtil.string2date(main.getLanguageHandle(), fromText.getText().trim()));
			if (toText.getText().trim().equals("")) {
				ui.putConfigValue("fina2.returns.ReturnManagerFrame.toText", new Date(0L));
			} else
				ui.putConfigValue("fina2.returns.ReturnManagerFrame.toText", LocaleUtil.string2date(main.getLanguageHandle(), toText.getText().trim()));

			initTable();
		} catch (java.text.ParseException e) {
			Main.errorHandler(main.getMainFrame(), Main.getString("fina2.title"), Main.getString("fina2.invalidDate"));
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	/** Action handler for bankSelectionButton */
	private void fiSelectionActionPerformed(ActionEvent evt) {

		try {
			User user = (User) main.getUserHandle().getEJBObject();
			UserPK userPK = (UserPK) user.getPrimaryKey();

			FISelectionDialog banksDialog = new FISelectionDialog(main.getMainFrame(), userPK, "fina2.returns.selectedFI", "fina2.returns.fiSelectionButtonText");

			banksDialog.setVisible(true);

			/*
			 * The selected banks are saved in the configuration file by
			 * BankSelectionView: see BankSelectionView.save().
			 */
			if (banksDialog.getDialogResult() == AbstractDialog.DialogResult.OK) {
				// User clicked OK button. Making necessary changes.
				updateFiSelectionButtonText();
			}
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {

		Collection rows = null;
		TreePath[] tp = treeTable.getTree().getSelectionPaths();
		for (int i = 0; i < tp.length; i++) {
			Object o = tp[i].getLastPathComponent();
			if (o instanceof TableRow) {
				rows = new ArrayList();
				rows.add(o);
			} else {
				PackageInfo packageInfo = (PackageInfo) o;
				rows = packageInfo.getItems();
			}

			NoteBox noteBox = new NoteBox(main.getMainFrame());
			noteBox.show(ui.getString(ReturnConstants.STATUS_RESETED_STR));

			resetReturns(rows, noteBox.getMessage());
		}
	}

	public void resetReturns(Collection rows, String message) {

		try {
			for (Iterator iter = rows.iterator(); iter.hasNext();) {
				TableRow row = (TableRow) iter.next();

				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi.lookup("fina2/returns/ReturnSession");
				ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);
				ReturnSession session = home.create();

				session.toAuditLog("\"" + ui.getString("fina2.returns.reseted") + "\"," + "\"" + row.getValue(0) + "\",\"" + row.getValue(1) + "\",\"" + row.getValue(2) + "\",\"" + row.getValue(4)
						+ "\"" + ",\"\",\"\",\"\",\"\",\"\",\"\"", main.getUserHandle(), main.getLanguageHandle());

				session.changeReturnStatus(main.getUserHandle(), main.getLanguageHandle(), (ReturnPK) row.getPrimaryKey(), ReturnConstants.STATUS_RESETED, message, getVersionCode(row));

				row.setValue(6, ui.getString(ReturnConstants.STATUS_RESETED_STR));
			}
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

		treeTable.updateUI();
		updateButtonsStates();
	}

	private void acceptButtonActionPerformed(java.awt.event.ActionEvent evt) {

		Collection rows = null;
		TreePath tp[] = treeTable.getTree().getSelectionPaths();

		for (int i = 0; i < tp.length; i++) {
			Object o = tp[i].getLastPathComponent();
			if (o instanceof TableRow) {
				rows = new ArrayList();
				rows.add(o);
			} else {
				PackageInfo packageInfo = (PackageInfo) o;
				rows = packageInfo.getItems();
			}

			NoteBox noteBox = new NoteBox(main.getMainFrame());
			noteBox.show(ui.getString(ReturnConstants.STATUS_ACCEPTED_STR));

			acceptReturns(rows, noteBox.getMessage());
		}
	}

	public void acceptReturns(Collection rows, String message) {
		try {
			for (Iterator iter = rows.iterator(); iter.hasNext();) {
				TableRow row = (TableRow) iter.next();

				InitialContext jndi = fina2.Main.getJndiContext();

				Object ref = jndi.lookup("fina2/returns/ReturnSession");
				ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);
				ReturnSession session = home.create();

				session.toAuditLog("\"" + ui.getString("fina2.returns.accepted") + "\"," + "\"" + row.getValue(0) + "\",\"" + row.getValue(1) + "\",\"" + row.getValue(2) + "\",\"" + row.getValue(4)
						+ "\"" + ",\"\",\"\",\"\",\"\",\"\",\"\"", main.getUserHandle(), main.getLanguageHandle());

				session.changeReturnStatus(main.getUserHandle(), main.getLanguageHandle(), (ReturnPK) row.getPrimaryKey(), ReturnConstants.STATUS_ACCEPTED, message, getVersionCode(row));

				row.setValue(6, ui.getString(ReturnConstants.STATUS_ACCEPTED_STR));
			}
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

		treeTable.updateUI();
		updateButtonsStates();
	}

	private void rejectButtonActionPerformed(java.awt.event.ActionEvent evt) {

		Collection rows = null;
		TreePath[] tp = treeTable.getTree().getSelectionPaths();
		for (int i = 0; i < tp.length; i++) {
			Object o = tp[i].getLastPathComponent();
			if (o instanceof TableRow) {
				rows = new ArrayList();
				rows.add(o);
			} else {
				PackageInfo packageInfo = (PackageInfo) o;
				rows = packageInfo.getItems();
			}
			NoteBox noteBox = new NoteBox(main.getMainFrame());
			noteBox.show(ui.getString(ReturnConstants.STATUS_REJECTED_STR));

			rejectReturns(rows, noteBox.getMessage());
		}
	}

	public void rejectReturns(Collection rows, String message) {

		try {

			for (Iterator iter = rows.iterator(); iter.hasNext();) {
				TableRow row = (TableRow) iter.next();

				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi.lookup("fina2/returns/ReturnSession");
				ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);
				ReturnSession session = home.create();

				session.toAuditLog("\"" + ui.getString("fina2.returns.rejected") + "\"," + "\"" + row.getValue(0) + "\",\"" + row.getValue(1) + "\",\"" + row.getValue(2) + "\",\"" + row.getValue(4)
						+ "\"" + ",\"\",\"\",\"\",\"\",\"\",\"\"", main.getUserHandle(), main.getLanguageHandle());

				session.changeReturnStatus(main.getUserHandle(), main.getLanguageHandle(), (ReturnPK) row.getPrimaryKey(), ReturnConstants.STATUS_REJECTED, message, getVersionCode(row));

				row.setValue(6, ui.getString(ReturnConstants.STATUS_REJECTED_STR));
			}
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

		treeTable.updateUI();
		updateButtonsStates();
	}

	private void processButtonActionPerformed(java.awt.event.ActionEvent evt) {
		Thread t = new Thread() {
			public void run() {
				Collection rows = null;
				Object o = treeTable.getTree().getLastSelectedPathComponent();
				if (o instanceof TableRow) {
					rows = new ArrayList();
					rows.add(o);
				} else {
					PackageInfo packageInfo = (PackageInfo) o;
					rows = packageInfo.getItems();
				}

				ProcessDialog pdlg = ui.showProgressDialog(main.getMainFrame(), ui.getString("fina2.returns.processing"), rows.size());
				String resultMessage = processReturns(rows, pdlg);
				pdlg.dispose();

				ImportDialog dlg = new ImportDialog(main.getMainFrame(), resultMessage);
				dlg.show();
				updateButtonsStates();
				treeTable.updateUI();
				treeTable.getTree().updateUI();
			}
		};
		t.start();
	}

	public String processReturns(Collection rows, ProcessDialog dlg) {

		String resultMsg = null;
		ReturnSession _session = null;
		TableRow _row = null;
		try {

			int index = 0;
			for (Iterator iter = rows.iterator(); iter.hasNext(); index++) {
				TableRow row = (TableRow) iter.next();
				_row = row;
				InitialContext jndi = fina2.Main.getJndiContext();

				Object ref = jndi.lookup("fina2/returns/ProcessSession");
				ProcessSessionHome home = (ProcessSessionHome) PortableRemoteObject.narrow(ref, ProcessSessionHome.class);
				ProcessSession session = home.create();

				Object _ref = jndi.lookup("fina2/returns/ReturnSession");
				ReturnSessionHome _home = (ReturnSessionHome) PortableRemoteObject.narrow(_ref, ReturnSessionHome.class);
				_session = _home.create();

				StringBuffer message = new StringBuffer();

				message.append("<html>Return code: [").append(row.getValue(0)).append("] <br>");
				message.append("Total " + index + " of " + rows.size() + "</html>");
				dlg.setMessage(message.toString());

				resultMsg = session.process(main.getUserHandle(), main.getLanguageHandle(), (ReturnPK) row.getPrimaryKey(), true, getVersionCode(row));

				// if (resultMsg.equals("")) {
				// resultMsg = ui.getString("fina2.returns.processed") + "\n";
				//
				// _session.changeReturnStatus(main.getUserHandle(),
				// main.getLanguageHandle(), (ReturnPK) row.getPrimaryKey(),
				// ReturnConstants.STATUS_PROCESSED, resultMsg,
				// getVersionCode(row));
				//
				// _session.toAuditLog(
				// "\"" + ui.getString("fina2.returns.processed") + "\"," + "\""
				// + row.getValue(0) + "\",\"" + row.getValue(1) + "\",\"" +
				// row.getValue(2) + "\",\"" + row.getValue(4)
				// + "\",\"\",\"\",\"\",\"\",\"\",\"\"", main.getUserHandle(),
				// main.getLanguageHandle());
				//
				// row.setValue(6,
				// ui.getString(ReturnConstants.STATUS_PROCESSED_STR));
				// } else {
				//
				// _session.changeReturnStatus(main.getUserHandle(),
				// main.getLanguageHandle(), (ReturnPK) row.getPrimaryKey(),
				// ReturnConstants.STATUS_ERRORS, resultMsg,
				// getVersionCode(row));
				//
				// _session.toAuditLog(
				// "\"" + ui.getString("fina2.returns.errors") + "\"," + "\"" +
				// row.getValue(0) + "\",\"" + row.getValue(1) + "\",\"" +
				// row.getValue(2) + "\",\"" + row.getValue(4)
				// + "\",\"\",\"\",\"\",\"\",\"\",\"\"", main.getUserHandle(),
				// main.getLanguageHandle());
				//
				// row.setValue(6,
				// ui.getString(ReturnConstants.STATUS_ERRORS_STR));
				// break;
				// }

				dlg.incProgress();
			}
		} catch (Exception e) {
			resultMsg = e.getMessage();
			try {
				// if (resultMsg.indexOf("Error: Wrong date format") != -1) {
				// _session.changeReturnStatus(main.getUserHandle(),
				// main.getLanguageHandle(), (ReturnPK) _row.getPrimaryKey(),
				// ReturnConstants.STATUS_ERRORS,
				// resultMsg =
				// resultMsg.substring(resultMsg.indexOf("Error: Wrong date format")),
				// getVersionCode(_row));
				// }
				// else if
				// (resultMsg.indexOf("Value does not match comparison rule") !=
				// -1) {
				// _session.changeReturnStatus(main.getUserHandle(),
				// main.getLanguageHandle(), (ReturnPK) _row.getPrimaryKey(),
				// ReturnConstants.STATUS_ERRORS,
				// resultMsg =
				// resultMsg.substring(resultMsg.indexOf("Value does not match comparison rule")),
				// getVersionCode(_row));
				// }
				// else
				// if (resultMsg.indexOf("Error: The value for node code") !=
				// -1) {
				// _session.changeReturnStatus(main.getUserHandle(),
				// main.getLanguageHandle(), (ReturnPK) _row.getPrimaryKey(),
				// ReturnConstants.STATUS_ERRORS,
				// resultMsg =
				// resultMsg.substring(resultMsg.indexOf("Error: The value for node code")),
				// getVersionCode(_row));
				// } else
				if (resultMsg.indexOf("Wrong version code") != -1) {
					_session.changeReturnStatus(main.getUserHandle(), main.getLanguageHandle(), (ReturnPK) _row.getPrimaryKey(), ReturnConstants.STATUS_ERRORS,
							resultMsg = resultMsg.substring(resultMsg.indexOf("Wrong version code")), getVersionCode(_row));
				}
				// else if
				// (resultMsg.indexOf("Error during parsing/formatting  following items")
				// != -1) {
				// _session.changeReturnStatus(main.getUserHandle(),
				// main.getLanguageHandle(), (ReturnPK) _row.getPrimaryKey(),
				// ReturnConstants.STATUS_ERRORS,
				// resultMsg =
				// resultMsg.substring(resultMsg.indexOf("Error during parsing/formatting  following items")),
				// getVersionCode(_row));
				// }
				else {
					_session.changeReturnStatus(main.getUserHandle(), main.getLanguageHandle(), (ReturnPK) _row.getPrimaryKey(), ReturnConstants.STATUS_ERRORS, resultMsg = resultMsg,
							getVersionCode(_row));
				}

				_session.toAuditLog(
						"\"" + ui.getString("fina2.returns.errors") + "\"," + "\"" + _row.getValue(0) + "\",\"" + _row.getValue(1) + "\",\"" + _row.getValue(2) + "\",\"" + _row.getValue(4)
								+ "\",\"\",\"\",\"\",\"\",\"\",\"\"", main.getUserHandle(), main.getLanguageHandle());
				_row.setValue(6, ui.getString(ReturnConstants.STATUS_ERRORS_STR));
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
				ex.printStackTrace();
				JOptionPane.showMessageDialog(null, "Error during change status to 'ERRORS'");
			}

			// JOptionPane.showMessageDialog(null, (index != -1) ?
			// e.getMessage().substring(index) : e.getMessage(), "Error",
			// JOptionPane.ERROR_MESSAGE);
			// Main.generalErrorHandler(e);
		}

		return resultMsg;
	}

	protected void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		ui.putConfigValue("fina2.returns.ReturnManagerFrame.visible", new Boolean(false));
		setVisible(false);
		dispose();
	}

	protected void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {
		initTable();
	}

	/** Exit the Application */

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton acceptButton;
	private javax.swing.JButton amendButton;
	private javax.swing.JButton fiSelectionButton;
	private javax.swing.JComboBox versionCombo;
	private javax.swing.JComboBox codeList;
	private javax.swing.JButton createButton;
	private javax.swing.JButton deleteButton;
	private javax.swing.JButton filterButton;
	private javax.swing.JTextField fromText;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel10;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JPanel jPanel9;
	private javax.swing.JButton notesButton;
	private javax.swing.JButton processButton;
	private javax.swing.JButton rejectButton;
	private javax.swing.JButton resetButton;
	private javax.swing.JButton reviewButton;
	private javax.swing.JScrollPane scrollPane;
	private javax.swing.JComboBox statusList;
	private javax.swing.JTextField toText;
	private javax.swing.JComboBox typeList;
	private JTextField statisticTextField;

	// End of variables declaration//GEN-END:variables

	private String getVersionCode(TableRow row) {
		return row.getValue(5);
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

class ReturnsAmendReviewAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;
	private Logger log = Logger.getLogger(getClass());

	private JTreeTable treeTable;
	private ReturnManagerFrame returnManagerFrame;
	private boolean review;

	ReturnsAmendReviewAction(JTreeTable treeTable, ReturnManagerFrame returnManagerFrame, boolean review) {
		super();

		this.treeTable = treeTable;
		this.returnManagerFrame = returnManagerFrame;
		this.review = review;

		if (review) {
			ui.loadIcon("fina2.review", "review.gif");
			putValue(AbstractAction.NAME, ui.getString("fina2.review"));
			putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.review"));
		} else {
			ui.loadIcon("fina2.amend", "amend.gif");
			putValue(AbstractAction.NAME, ui.getString("fina2.amend"));
			putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.amend"));
		}
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {

		Thread t = new Thread() {
			public void run() {
				try {
					boolean packageAmend = false;
					Collection rows = null;
					Object o = treeTable.getTree().getLastSelectedPathComponent();
					if (o instanceof TableRow) {
						rows = new ArrayList();
						rows.add(o);
					} else {
						packageAmend = true;
						PackageInfo packageInfo = (PackageInfo) o;
						rows = packageInfo.getItems();
					}

					ReturnAmendReviewFrame amendFrame = new ReturnAmendReviewFrame(review);
					fina2.ui.ProcessDialog pdlg = ui.showProcessDialog(amendFrame, "Loading...");
					amendFrame.setExtendedState(ReturnAmendReviewFrame.MAXIMIZED_BOTH);
					amendFrame.show(returnManagerFrame, rows, packageAmend);
					pdlg.dispose();
					amendFrame.setVisible(true);
					// TODO Test Nick
					UIManager.resizeOooSheetPage(amendFrame);
				} catch (Exception e) {
					Main.generalErrorHandler(e);
					e.printStackTrace();
					log.error(e.getMessage(), e);
				}
			}
		};

		t.start();
	}
}

class ReturnInsertAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private SelectScheduleDialog selectScheduleDialog;
	private java.awt.Frame parent;
	private JTreeTable treeTable;
	private TreeTableModel treeTableModel;
	private ReturnManagerFrame returnManagerFrame;
	private TableRow tableRow;

	private fina2.Main main = fina2.Main.main;

	ReturnInsertAction(java.awt.Frame parent, JTreeTable treeTable, TreeTableModel treeTableModel, ReturnManagerFrame returnManagerFrame) {
		super();

		selectScheduleDialog = new SelectScheduleDialog(parent, true);
		ui.loadIcon("fina2.insert", "insert.gif");

		this.parent = parent;
		this.treeTable = treeTable;
		this.treeTableModel = treeTableModel;
		this.returnManagerFrame = returnManagerFrame;

		putValue(AbstractAction.NAME, ui.getString("fina2.create"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.insert"));

	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		// Checking whether user has any return versions for amend
		if (!ImportManagerAction.checkUserReturnVersions()) {
			// Current user has no return version for amend
			return;
		}

		selectScheduleDialog.show();

		final Collection<TableRow> rows = selectScheduleDialog.getSeleTableRows();

		final List<TableRow> existRows = new ArrayList<TableRow>();

		if (rows != null) {

			final IndeterminateLoading loading = ui.createIndeterminateLoading(null);

			Thread thread = new Thread() {
				public void run() {
					loading.start();

					for (TableRow row : rows) {
						if (row != null) {
							try {
								String versionCode = selectScheduleDialog.getVersionCode();
								InitialContext jndi = fina2.Main.getJndiContext();

								Object ref = jndi.lookup("fina2/returns/ReturnSession");
								ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);
								ReturnSession session = home.create();
								ReturnPK returnPK = null;
								try {
									returnPK = session.createReturn(main.getUserHandle(), main.getLanguageHandle(), (SchedulePK) row.getPrimaryKey(), versionCode);
								} catch (RuntimeException ex) {
									JLabel messageLabel = new JLabel();
									messageLabel.setFont(ui.getFont());
									messageLabel.setText(ex.getMessage());
									JOptionPane.showMessageDialog(null, messageLabel, "Message", JOptionPane.WARNING_MESSAGE);
								}

								ref = jndi.lookup("fina2/returns/ProcessSession");
								ProcessSessionHome phome = (ProcessSessionHome) PortableRemoteObject.narrow(ref, ProcessSessionHome.class);
								ProcessSession psession = phome.create();

								Map<String, String> nameCodesMap = new LinkedHashMap<String, String>();
								nameCodesMap = psession.canProcess(main.getLanguageHandle(), (SchedulePK) row.getPrimaryKey(), versionCode);

								if (nameCodesMap.size() > 0) {
									String l = ui.getString("fina2.returns.audit.text6") + "\n";
									int i = nameCodesMap.size() / 2;

									for (Iterator<String> iter = nameCodesMap.keySet().iterator(); iter.hasNext(); i++) {
										try {
											String code = (String) iter.next();
											String name = nameCodesMap.get(code);
											l += "    [" + code + "] " + name + "\n";
											System.out.println("    [" + code + "] " + name);
										} catch (ArrayIndexOutOfBoundsException aex) {
											break;
										}
									}
									ui.showLongMessageBox(parent, ui.getString("fina2.returns.cantCreate"), l);
									session.deleteReturn(returnPK, versionCode);

								} else {
									// inject Schedule EJB.
									Object scheduleObject = jndi.lookup("fina2/returns/Schedule");
									ScheduleHome scheduleHome = (ScheduleHome) PortableRemoteObject.narrow(scheduleObject, ScheduleHome.class);

									// get Schedule selection row.
									SchedulePK schedulePK = (SchedulePK) row.getPrimaryKey();

									// find selection row.
									Schedule schedule = scheduleHome.findByPrimaryKey(schedulePK);

									// get selection schedule bankPk.
									BankPK bankPK = schedule.getBankPK();

									// inject bank EJB.
									Object bankObj = jndi.lookup("fina2/bank/Bank");
									BankHome bankHome = (BankHome) PortableRemoteObject.narrow(bankObj, BankHome.class);

									// find selection schedule bank.
									Bank bank = bankHome.findByPrimaryKey(bankPK);

									// get selection schedule bank code.
									String bankName = row.getValue(4);
									// get selection schedule bank short name.
									bankName += "[" + bank.getShortName(main.getLanguageHandle()) + "]";

									// TreeTable insert new return.
									TableRowImpl new_row = new TableRowImpl(returnPK, 8);
									new_row.setValue(0, row.getValue(0));
									new_row.setValue(1, row.getValue(1));
									new_row.setValue(2, row.getValue(2));
									new_row.setValue(3, row.getValue(3));
									new_row.setValue(4, bankName);
									new_row.setValue(5, selectScheduleDialog.getVersionCode());
									new_row.setValue(6, ui.getString(ReturnConstants.STATUS_CREATED_STR));
									new_row.setValue(7, row.getValue(5));

									TreePath addPath = treeTableModel.addRow(new_row);
									treeTable.updateUI();
									returnManagerFrame.updateButtonsStates();

									treeTable.getTree().makeVisible(addPath);
									treeTable.getTree().setSelectionPath(addPath);

									session.toAuditLog("\"" + ui.getString("fina2.returns.created") + "\"," + "\"" + row.getValue(0) + "\",\"" + row.getValue(1) + "\",\"" + row.getValue(2) + "\",\""
											+ row.getValue(4) + "\"" + ",\"\",\"\",\"\",\"\",\"\",\"\"", main.getUserHandle(), main.getLanguageHandle());
								}

								System.out.println(System.currentTimeMillis());

							} catch (FinaTypeException ex) {
								existRows.add(row);
							} catch (Exception e) {
								Main.generalErrorHandler(e);
							}
						}
					}
					loading.stop();
					if (existRows.size() > 0) {
						FinaTypeException tmpException = new FinaTypeException(FinaTypeException.Type.RETURNS_RETURN_NOT_UNIQUE);
						StringBuffer buff = new StringBuffer();
						String returnRowShowFormat = "# | " + ui.getString("fina2.security.fi") + " " + ui.getString("fina2.bank.bankCode") + " | " + ui.getString("fina2.sname") + " | "
								+ ui.getString("fina2.period.fromDate") + "-" + ui.getString("fina2.period.toDate") + " | " + ui.getString("fina2.rtype");
						buff.append(ui.getString(tmpException.getMessageUrl()) + ":\n" + "[" + returnRowShowFormat + "] \n");
						int counter = 1;
						for (TableRow row : existRows) {
							buff.append(counter + ". ");
							buff.append(row.getValue(4) + " | ");
							buff.append(row.getValue(0) + " | ");
							buff.append(row.getValue(1) + "-");
							buff.append(row.getValue(2) + " | ");
							buff.append(row.getValue(5) + "\n");
							counter++;
						}
						String messageText = buff.toString();
						JTextArea textArea = new JTextArea(8, 46);
						textArea.setFont(ui.getFont());
						textArea.setText(messageText);
						textArea.setEditable(false);
						JScrollPane scrollPane = new JScrollPane(textArea);
						loading.stop();
						JOptionPane.showMessageDialog(parent, scrollPane, "Fina International", JOptionPane.WARNING_MESSAGE);
					}
				}
			};
			thread.start();
		}

	}
}

class ReturnDeleteAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private java.awt.Frame parent;
	private JTreeTable treeTable;
	private TreeTableModel treeTableModel;
	private ReturnManagerFrame returnManagerFrame;

	ReturnDeleteAction(java.awt.Frame parent, JTreeTable treeTable, TreeTableModel treeTableModel, ReturnManagerFrame returnManagerFrame) {

		super();
		ui.loadIcon("fina2.delete", "delete.gif");

		this.parent = parent;
		this.treeTable = treeTable;
		this.treeTableModel = treeTableModel;
		this.returnManagerFrame = returnManagerFrame;

		putValue(AbstractAction.NAME, ui.getString("fina2.delete"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.delete"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {

		try {
			if (!ui.showConfirmBox(parent, ui.getString("fina2.returns.returnDeleteQuestion"))) {
				return;
			}

			TreePath tp[] = treeTable.getTree().getSelectionPaths();
			Collection rows = null;
			for (int i = 0; i < tp.length; i++) {
				Object o = tp[i].getLastPathComponent();
				if (o instanceof TableRow) {
					rows = new ArrayList();
					rows.add(o);
				} else {
					PackageInfo packageInfo = (PackageInfo) o;
					rows = packageInfo.getItems();
				}

				for (Iterator iter = rows.iterator(); iter.hasNext();) {
					TableRow row = (TableRow) iter.next();

					InitialContext jndi = fina2.Main.getJndiContext();

					Object ref = jndi.lookup("fina2/returns/ReturnSession");
					ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);

					ReturnSession session = home.create();
					session.deleteReturn((ReturnPK) row.getPrimaryKey(), row.getValue(5));
					// session.toAuditLog("\""+ui.getString("fina2.returns.processed"
					// )+"\","+"\""+row.getValue(0)+"\",\""+row.getValue(1)+"\",\""+
					// row.getValue(2)+"\",\""+row.getValue(4)+"\""+
					// ",\"\",\"\",\"\",\"\",\"\",\"\"", main.getUserHandle(),
					// main.getLanguageHandle());
				}

				treeTable.getTree().removeSelectionPath(tp[i]);
				treeTableModel.removeRows(rows);
			}
			treeTable.getTree().setRootVisible(treeTableModel.getReturns().size() != 0);
			treeTable.updateUI();
			returnManagerFrame.updateButtonsStates();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}
}

@SuppressWarnings("serial")
class Package2WorkBookAction extends AbstractAction {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private JTreeTable treeTable;
	private ReturnManagerFrame returnManagerFrame;

	public Package2WorkBookAction(JTreeTable treeTable, ReturnManagerFrame returnManagerFrame) {
		this.treeTable = treeTable;
		this.returnManagerFrame = returnManagerFrame;
		ui.loadIcon("package2workbook", "document-convert-icon.png");
		putValue(AbstractAction.NAME, ui.getString("fina2.return.package2workbook"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Thread thread = new Thread() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {

				Collection<TableRow> rows = null;

				Object o = treeTable.getTree().getLastSelectedPathComponent();

				PackageInfo packageInfo = null;
				if (!(o instanceof TableRow)) {
					packageInfo = (PackageInfo) o;
					rows = packageInfo.getItems();
				}

				Package2WorkBook package2WorkBook = new Package2WorkBook((List<TableRow>) rows, returnManagerFrame, packageInfo);
				package2WorkBook.initAndShowSheets();
				package2WorkBook.setExtendedState(Package2WorkBook.MAXIMIZED_BOTH);
				package2WorkBook.setVisible(true);
				package2WorkBook.setFocusable(true);
			}
		};
		thread.start();

	}
}
