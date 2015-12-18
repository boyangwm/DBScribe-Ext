package fina2.security.users;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import fina2.FinaTypeException;
import fina2.Main;
import fina2.security.UserPK;
import fina2.ui.AbstractDialog;

public class UserDialog extends AbstractDialog {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	/** The views */
	private AbstractView<UserPK> generalView = null;
	private AbstractView<UserPK> rolesView = null;
	private AbstractView<UserPK> fiView = null;
	private AbstractView<UserPK> permissionsView = null;
	private AbstractView<UserPK> returnsView = null;
	private AbstractView<UserPK> reportsView = null;
	private AbstractView<UserPK> returnVersionsView = null;

	/**
	 * Creates the instance of the dialog. This constructor version is called
	 * for action mode CREATE.
	 * 
	 * @throws Exception
	 *             if there are errors while loading data
	 */
	public UserDialog(JFrame owner) throws Exception {
		this(owner, AbstractView.ModeType.CREATE, null);
	}

	/**
	 * Creates the instance of the dialog with a given mode
	 * 
	 * @throws Exception
	 *             if there are errors while loading data
	 */
	public UserDialog(JFrame owner, AbstractView.ModeType modeType,
			UserPK userPK) throws Exception {
		super(owner, true);

		checkArguments(modeType, userPK);

		/* Can throw Exception while loading data */
		initComponents(modeType, userPK);
	}

	/**
	 * Throws IllegalArgumentException if actionMode is AMEND or REVIEW, and
	 * userPK is null.
	 */
	private void checkArguments(AbstractView.ModeType modeType, UserPK userPK)
			throws IllegalArgumentException {

		if ((modeType != AbstractView.ModeType.CREATE) && (userPK == null)) {
			String error = "For AMEND and REVIEW actions the userPK must be specified";
			throw new IllegalArgumentException(error);
		}
	}

	/** Inits the components */
	private void initComponents(AbstractView.ModeType modeType, UserPK userPK)
			throws Exception {
		setSize(550, 550);

		/* Main work area */
		JTabbedPane tabPane = initTabbedPane();
		tabPane.setFont(ui.getFont());

		/* Init tabs */
		initGeneralTab(tabPane, modeType, userPK);
		initRolesTab(tabPane, modeType, userPK);
		initBanksTab(tabPane, modeType, userPK);
		initPermissionsTab(tabPane, modeType, userPK);
		initReturnsTab(tabPane, modeType, userPK);
		initReportsTab(tabPane, modeType, userPK);
		initReturnVersionsTab(tabPane, modeType, userPK);
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
			AbstractView.ModeType modeType, UserPK userPK) throws Exception {

		/*
		 * Creating the general view
		 */
		String text = Main.getString("fina2.security.general");
		Icon icon = Main.getIcon("user.gif");
		UserGeneralView generalView = new UserGeneralView(modeType, userPK);
		this.generalView = generalView;

		tabPane.setFont(ui.getFont());
		tabPane.addTab(text, icon, generalView);

		/*
		 * Setting the dialog title
		 */
		String title = Main.getString("fina2.security.user");

		if (modeType != AbstractView.ModeType.CREATE) {
			/* Edit mode. Displaying the user data */
			title += ": " + generalView.getUserLogin() + " - "
					+ generalView.getUserName();
		}

		setTitle(title);
	}

	/** Inits the roles tab */
	private void initRolesTab(JTabbedPane tabPane,
			AbstractView.ModeType modeType, UserPK userPK) throws Exception {

		String text = Main.getString("fina2.security.roles");
		Icon icon = Main.getIcon("role.gif");
		rolesView = new UserRolesView(modeType, userPK);
		rolesView.setFont(ui.getFont());
		tabPane.addTab(text, icon, rolesView);
	}

	/** Inits the banks tab */
	private void initBanksTab(JTabbedPane tabPane,
			AbstractView.ModeType modeType, UserPK userPK) throws Exception {

		String text = Main.getString("fina2.security.fi");
		Icon icon = Main.getIcon("banks.gif");
		fiView = new UserFIView(modeType, userPK);

		tabPane.addTab(text, icon, fiView);
	}

	/** Inits the permissions tab */
	private void initPermissionsTab(JTabbedPane tabPane,
			AbstractView.ModeType modeType, UserPK userPK) throws Exception {

		String text = Main.getString("fina2.security.permissions");
		Icon icon = Main.getIcon("permission.gif");
		permissionsView = new UserPermissionsView(modeType, userPK);

		tabPane.addTab(text, icon, permissionsView);
	}

	/** Inits the returns tab */
	private void initReturnsTab(JTabbedPane tabPane,
			AbstractView.ModeType modeType, UserPK userPK) throws Exception {

		String text = Main.getString("fina2.returns");
		Icon icon = Main.getIcon("return_table.gif");
		returnsView = new UserReturnsView(modeType, userPK);

		tabPane.addTab(text, icon, returnsView);
	}

	/** Inits the reports tab */
	private void initReportsTab(JTabbedPane tabPane,
			AbstractView.ModeType modeType, UserPK userPK) throws Exception {

		String text = Main.getString("fina2.security.reports");
		Icon icon = Main.getIcon("amend.gif");
		reportsView = new UserReportsView(modeType, userPK);

		tabPane.addTab(text, icon, reportsView);
	}

	/** Inits the return versions tab */
	private void initReturnVersionsTab(JTabbedPane tabPane,
			AbstractView.ModeType modeType, UserPK userPK) throws Exception {

		String text = Main.getString("fina2.versions");
		Icon icon = Main.getIcon("return_vesions.gif");
		returnVersionsView = new UserReturnVersionsView(modeType, userPK);

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
	 * Checks a given input data. If this method finishes without throwing an
	 * exception, then the data is correct.
	 */
	private void checkData() throws FinaTypeException {
		generalView.check();
		rolesView.check();
		fiView.check();
		permissionsView.check();
		returnsView.check();
		reportsView.check();
		returnVersionsView.check();
	}

	/**
	 * Saves the given input data. If this method finishes without throwing an
	 * exception, then the data is saved successfully.
	 */
	private void saveData() throws FinaTypeException, Exception {

		/* Saving data of the general view */
		generalView.save();

		if (generalView.getModeType() == AbstractView.ModeType.CREATE) {
			/*
			 * The dialog is in create mode. A new user PK must be specified for
			 * other views
			 */
			UserPK rolePK = generalView.getKey();

			rolesView.setKey(rolePK);
			fiView.setKey(rolePK);
			permissionsView.setKey(rolePK);
			returnsView.setKey(rolePK);
			reportsView.setKey(rolePK);
			returnVersionsView.setKey(rolePK);
		}

		/* Saving the data of all views */
		rolesView.save();
		fiView.save();
		permissionsView.save();
		returnsView.save();
		reportsView.save();
		returnVersionsView.save();
	}

	/** Returns current user PK */
	public UserPK getUserPK() {
		return generalView.getKey();
	}

}
