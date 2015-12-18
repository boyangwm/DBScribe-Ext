package fina2.security.users;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import fina2.FinaTypeException;
import fina2.FinaTypeException.Type;
import fina2.Main;
import fina2.security.User;
import fina2.security.UserPK;
import fina2.servergate.SecurityGate;
import fina2.ui.UIManager;

/**
 * Represents user general data
 */
public class UserGeneralView extends AbstractView<UserPK> {

	public static final String DUMMY_PASSWORD = "@@@@@@@@";
	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	// Fields
	private JTextField loginField = null;
	private JTextField nameField = null;
	private JTextField titleField = null;

	private JTextField passwordField = null;
	private JTextField confirmField = null;

	private JCheckBox blockedCheckBox = null;
	private JCheckBox changePasswordCheckBox = null;

	private JTextField phoneField = null;
	private JTextField emailField = null;

	/** Creates an instance of the class */
	public UserGeneralView(ModeType modeType, UserPK userPK) throws Exception {
		super(modeType, userPK);

		initComponents();
		initData();
	}

	/** Inits the components */
	private void initComponents() {
		initGeneral();
		initFields();
	}

	/** Inits the data */
	private void initData() throws Exception {

		if (getModeType() != ModeType.CREATE) {
			/* Edit mode. Required loading data from server */
			loadData();
		}
	}

	/** Loads the data from the server */
	private void loadData() throws Exception {

		UserPK userPK = getKey();
		User user = SecurityGate.getUser(userPK);

		loginField.setText(user.getLogin());
		nameField.setText(user.getName(Main.main.getLanguageHandle()));
		titleField.setText(user.getTitle(Main.main.getLanguageHandle()));

		passwordField.setText(DUMMY_PASSWORD);
		confirmField.setText(DUMMY_PASSWORD);

		blockedCheckBox.setSelected(user.getBlocked());
		changePasswordCheckBox.setSelected(user.getChangePassword());

		phoneField.setText(user.getPhone());
		emailField.setText(user.getEmail());
	}

	/** Inits a general information for components */
	private void initGeneral() {

		/* To align whole content upper - just adding empty space bottom */
		setBorder(new EmptyBorder(0, 0, 150, 0));

		/* For components layout */
		setLayout(new GridBagLayout());
	}

	/** Returns the constraints for GridBagLayout */
	private GridBagConstraints getGridBagConstraints() {
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST; // To align components left
		c.insets.left = 11; // Space between labels and fields
		c.gridx = GridBagConstraints.RELATIVE; // Add directly after the
												// previous

		return c;
	}

	/** Inits the fields */
	private void initFields() {

		GridBagConstraints c = getGridBagConstraints();
		Dimension shortSize = new Dimension(150, 20);
		Dimension longSize = new Dimension(250, 20);

		int row = 0; // Row number
		int fieldPad = 6; // Space between the fields
		int groupPad = 11; // Space between a group of fields

		/*
		 * Init the fields
		 */

		loginField = createTextField(c, row, fieldPad, shortSize, "fina2.security.login", false);
		loginField.setFont(ui.getFont());
		nameField = createTextField(c, ++row, fieldPad, longSize, "fina2.login.userName", false);
		nameField.setFont(ui.getFont());
		titleField = createTextField(c, ++row, fieldPad, longSize, "fina2.security.title", false);
		titleField.setFont(ui.getFont());
		passwordField = createTextField(c, ++row, groupPad, shortSize, "fina2.security.password", true);
		passwordField.setFont(ui.getFont());
		confirmField = createTextField(c, ++row, fieldPad, shortSize, "fina2.security.confirmPassword", true);
		confirmField.setFont(ui.getFont());
		passwordField.addFocusListener(new PasswordFieldFocusAdapter());
		confirmField.addFocusListener(new PasswordFieldFocusAdapter());

		blockedCheckBox = createCheckBox(c, ++row, groupPad, "fina2.security.blocked");
		changePasswordCheckBox = createCheckBox(c, ++row, fieldPad, "fina2.security.changePassword");

		phoneField = createTextField(c, ++row, groupPad, shortSize, "fina2.security.phone", false);
		phoneField.setFont(ui.getFont());
		emailField = createTextField(c, ++row, fieldPad, shortSize, "fina2.security.email", false);
		emailField.setFont(ui.getFont());
	}

	/** Returns a login of current user */
	String getUserLogin() {
		return loginField.getText();
	}

	/** Returns a name of current user */
	String getUserName() {
		return nameField.getText();
	}

	/** Creates a text field */
	private JTextField createTextField(GridBagConstraints c, int row, int topPad, Dimension fieldSize, String labelKey, boolean isPasswordField) {

		c.gridy = row;
		c.insets.top = topPad;

		/* Label */
		String text = Main.getString(labelKey) + ":";
		if (labelKey.equals("fina2.security.login")) {
			text = UIManager.formatedHtmlString(text);
		}
		if (labelKey.equals("fina2.login.userName")) {
			text = UIManager.formatedHtmlString(text);
		}
		if (labelKey.equals("fina2.security.password")) {
			text = UIManager.formatedHtmlString(text);
		}
		if (labelKey.equals("fina2.security.confirmPassword")) {
			text = UIManager.formatedHtmlString(text);
		}
		JLabel l = new JLabel(text);
		l.setFont(ui.getFont());
		add(l, c);

		/* Field */
		JTextField textField = null;

		if (isPasswordField) {
			textField = new JPasswordField();
			JPasswordField pass = (JPasswordField) textField;
			pass.setEchoChar('*');
		} else {
			textField = new JTextField();
		}

		textField.setPreferredSize(fieldSize);
		textField.setFont(ui.getFont());
		add(textField, c);

		return textField;
	}

	/** Creates a checkbox */
	private JCheckBox createCheckBox(GridBagConstraints c, int row, int topPad, String labelKey) {

		c.gridy = row;
		c.insets.top = topPad;

		/* Label */
		String text = Main.getString(labelKey) + ":";
		JLabel label = new JLabel(text);
		label.setFont(ui.getFont());
		add(label, c);

		/* Checkbox */
		JCheckBox checkBox = new JCheckBox();
		checkBox.setHorizontalAlignment(SwingConstants.RIGHT);
		add(checkBox, c);

		return checkBox;
	}

	/** Checks the data */
	public void check() throws FinaTypeException {

		/* Checking login */
		String text = loginField.getText().trim();
		if (text.equals("")) {
			throw new FinaTypeException(Type.SECURITY_EMPTY_LOGIN);
		}

		/* Checking name */
		text = nameField.getText().trim();
		if (text.equals("")) {
			throw new FinaTypeException(Type.SECURITY_EMPTY_NAME);
		}

		/* Checking a password */
		text = confirmField.getText();
		if (!passwordField.getText().equals(text)) {
			throw new FinaTypeException(Type.SECURITY_PASSWORD_NOT_MATCH);
		}

		Integer minimallength = ui.getMinimalLengthPassword();

		if (minimallength != null && passwordField.getText().trim().length() < minimallength) {

			String[] parameter = new String[1];
			parameter[0] = minimallength.toString();
			throw new FinaTypeException(Type.SECURITY_PASSWORD_TOO_SHORT, parameter);
		}
	}

	/** Saves the data */
	public void save() throws Exception {

		/* Getting user object. If this is a create mode, a new role is created */
		User user = getUser();

		try {
			/* Saving a user data */

			user.setLogin(loginField.getText().trim());
			user.setName(Main.main.getLanguageHandle(), nameField.getText());
			user.setTitle(Main.main.getLanguageHandle(), titleField.getText());

			if (!passwordField.getText().equals(DUMMY_PASSWORD)) {
				/* Password is set. Saving it */
				user.setPassword(passwordField.getText());
			}

			user.setBlocked(blockedCheckBox.isSelected());
			user.setChangePassword(changePasswordCheckBox.isSelected());

			user.setPhone(phoneField.getText());
			user.setEmail(emailField.getText());

			/* User data saved successfully */

		} catch (Exception e) {
			if (getModeType() == ModeType.CREATE) {
				/* Create mode. A created user should be deleted. */
				user.remove();
			}
			Main.generalErrorHandler(e);
		}
	}

	/**
	 * Returns a user object for the current view. If current mode type CREATE,
	 * a new user is created. For EDIT/REVIEW modes the current user is
	 * returned.
	 */
	private User getUser() throws Exception {

		/* The result user */
		User user = null;

		if (getModeType() == ModeType.CREATE) {
			/* Create mode. Creating a new user */
			user = SecurityGate.createUser();

			/* Setting a new user key */
			setKey((UserPK) user.getPrimaryKey());
		} else {
			/* EDIT/REVIEW mode. Getting the current role */
			UserPK userPK = getKey();
			user = SecurityGate.getUser(userPK);
		}

		/* The result user */
		return user;
	}
}

/**
 * Focus adapter for password fields
 */
class PasswordFieldFocusAdapter extends FocusAdapter {

	private boolean dummyPassword = false;

	public void focusGained(FocusEvent e) {
		JTextField field = (JPasswordField) e.getComponent();
		if (field.getText().equals(UserGeneralView.DUMMY_PASSWORD)) {
			dummyPassword = true;
			field.setText("");
		}
	}

	public void focusLost(FocusEvent e) {
		JTextField field = (JPasswordField) e.getComponent();
		if (field.getText().equals("") && dummyPassword == true) {
			field.setText(UserGeneralView.DUMMY_PASSWORD);
		}
	}
}
