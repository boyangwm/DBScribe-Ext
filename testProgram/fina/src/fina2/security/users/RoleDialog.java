package fina2.security.users;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import fina2.FinaTypeException;
import fina2.Main;
import fina2.security.RolePK;
import fina2.ui.AbstractDialog;

@SuppressWarnings("serial")
public class RoleDialog extends AbstractDialog {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	/** General data view */
	private AbstractView<RolePK> generalView = null;

	/** Permissions view */
	private AbstractView<RolePK> permissionsView = null;

	/** Returns view */
	private AbstractView<RolePK> returnsView = null;

	/** Reports view */
	private AbstractView<RolePK> reportsView = null;

	/** Return versions view */
	private AbstractView<RolePK> returnVersionsView = null;

	/**
	 * Creates the instance of the dialog. This constructor version is called
	 * for action mode CREATE.
	 * 
	 * @throws Exception
	 *             if there are errors while loading data
	 */
	public RoleDialog(JFrame owner) throws Exception {
		this(owner, AbstractView.ModeType.CREATE, null);
	}

	/**
	 * Creates the instance of the dialog with the given mode
	 * 
	 * @throws Exception
	 *             if there are errors while loading data
	 */
	public RoleDialog(JFrame owner, AbstractView.ModeType modeType,
			RolePK rolePK) throws Exception {
		super(owner, true);

		checkArguments(modeType, rolePK);

		/* Can throw Exception while loading data */
		initComponents(modeType, rolePK);
	}

	/**
	 * Throws IllegalArgumentException if actionMode is AMEND or REVIEW, and
	 * rolePK is null.
	 */
	private void checkArguments(AbstractView.ModeType modeType, RolePK rolePK)
			throws IllegalArgumentException {

		if ((modeType != AbstractView.ModeType.CREATE) && (rolePK == null)) {
			String error = "For AMEND and REVIEW actions the RolePK must be specified";
			throw new IllegalArgumentException(error);
		}
	}

	/** Inits the components */
	private void initComponents(AbstractView.ModeType modeType, RolePK rolePK)
			throws Exception {
		setSize(550, 550);

		/* Main work area */
		JTabbedPane tabPane = initTabbedPane();
		tabPane.setFont(ui.getFont());

		/* Init tabs */
		initGeneralTab(tabPane, modeType, rolePK);
		initPermissionsTab(tabPane, modeType, rolePK);
		initReturnsTab(tabPane, modeType, rolePK);
		initReportsTab(tabPane, modeType, rolePK);
		initReturnVersionsTab(tabPane, modeType, rolePK);
	}

	/** Inits the tab pane */
	private JTabbedPane initTabbedPane() {

		JTabbedPane tabPane = new JTabbedPane();
		tabPane.setBorder(new EmptyBorder(10, 10, 10, 9));
		super.getMainPane().add(tabPane);

		return tabPane;
	}

	/** Inits the general tab */
	private void initGeneralTab(JTabbedPane tabPane,
			AbstractView.ModeType modeType, RolePK rolePK) throws Exception {

		/*
		 * Creating the general view
		 */
		RoleGeneralView generalView = new RoleGeneralView(modeType, rolePK);
		this.generalView = generalView;

		String text = Main.getString("fina2.security.general");
		Icon icon = Main.getIcon("role.gif");
		tabPane.addTab(text, icon, generalView);

		/*
		 * Setting the dialog title
		 */
		String title = Main.getString("fina2.security.role");

		if (modeType != AbstractView.ModeType.CREATE) {
			/* Not CREATE mode - display role data */
			title += ": " + generalView.getRoleCode() + " - "
					+ generalView.getRoleDescription();
		}

		setTitle(title);
	}

	/** Inits the permissions tab */
	private void initPermissionsTab(JTabbedPane tabPane,
			AbstractView.ModeType modeType, RolePK rolePK) throws Exception {

		String text = Main.getString("fina2.security.permissions");
		Icon icon = Main.getIcon("permission.gif");
		permissionsView = new RolePermissionsView(modeType, rolePK);

		tabPane.addTab(text, icon, permissionsView);
	}

	/** Inits the returns tab */
	private void initReturnsTab(JTabbedPane tabPane,
			AbstractView.ModeType modeType, RolePK rolePK) throws Exception {

		String text = Main.getString("fina2.returns");
		Icon icon = Main.getIcon("return_table.gif");
		returnsView = new RoleReturnsView(modeType, rolePK);

		tabPane.addTab(text, icon, returnsView);
	}

	/** Inits the reports tab */
	private void initReportsTab(JTabbedPane tabPane,
			AbstractView.ModeType modeType, RolePK rolePK) throws Exception {

		String text = Main.getString("fina2.security.reports");
		Icon icon = Main.getIcon("amend.gif");
		reportsView = new RoleReportsView(modeType, rolePK);

		tabPane.addTab(text, icon, reportsView);
	}

	/** Inits the return versions tab */
	private void initReturnVersionsTab(JTabbedPane tabPane,
			AbstractView.ModeType modeType, RolePK rolePK) throws Exception {

		String text = Main.getString("fina2.versions");
		Icon icon = Main.getIcon("return_vesions.gif");
		returnVersionsView = new RoleReturnVersionsView(modeType, rolePK);

		tabPane.addTab(text, icon, returnVersionsView);
	}

	/** Handles the OK button */
	protected void okButtonPressed() {
		try {
			/* Throws exception if checking failed */
			checkData();

			/* Throws exception if saving failed */
			saveData();

			/* The data checked and saved successfully. Closing the dialog */
			dispose();

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	/**
	 * Checks the given input data. If this method finishes without throwing an
	 * exception, the data is correct.
	 */
	private void checkData() throws FinaTypeException {
		generalView.check();
		permissionsView.check();
		returnsView.check();
		reportsView.check();
		returnVersionsView.check();
	}

	/**
	 * Saves the given input data. If this method finishes without throwing an
	 * exception, the data is saved successfully.
	 */
	private void saveData() throws FinaTypeException, Exception {

		/* Saving data of the general view */
		generalView.save();

		if (generalView.getModeType() == AbstractView.ModeType.CREATE) {
			/*
			 * The dialog is in create mode. A new role PK must be specified for
			 * the views
			 */
			RolePK rolePK = generalView.getKey();

			permissionsView.setKey(rolePK);
			returnsView.setKey(rolePK);
			reportsView.setKey(rolePK);
			returnVersionsView.setKey(rolePK);
		}

		/* Saving the data of all views */
		permissionsView.save();
		returnsView.save();
		reportsView.save();
		returnVersionsView.save();
	}

	/** Returns the role pk */
	public RolePK getRolePK() {
		return generalView.getKey();
	}
}
