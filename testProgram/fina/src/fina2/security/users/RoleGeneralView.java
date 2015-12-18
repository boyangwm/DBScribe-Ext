package fina2.security.users;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.MaskFormatter;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.undo.UndoableEdit;

import fina2.FinaTypeException;
import fina2.Main;
import fina2.FinaTypeException.Type;
import fina2.security.Role;
import fina2.security.RolePK;
import fina2.servergate.SecurityGate;
import fina2.ui.UIManager;

/**
 * Represents role general data, such as code and description
 */
public class RoleGeneralView extends AbstractView<RolePK> {
    private fina2.ui.UIManager ui = fina2.Main.main.ui;

    // Fields
    private JTextField codeField = null;
    private JTextField descriptionField = null;

    /** Creates the instance of the class */
    public RoleGeneralView(ModeType modeType, RolePK rolePK) throws Exception {
	super(modeType, rolePK);

	initComponents();
	initData();
    }

    /** Inits the components */
    private void initComponents() {
	initGeneral();
	initFields();
    }

    /** Inits a general information for components */
    private void initGeneral() {

	/* To align whole content upper - just adding empty space bottom */
	setBorder(new EmptyBorder(0, 0, 150, 0));

	/* For components layout */
	setLayout(new GridBagLayout());
    }

    /** Inits the fields */
    private void initFields() {

	GridBagConstraints c = getGridBagConstraints();

	int row = 0; // Row number
	int topPad = 11; // Space between fields

	/* Init the fields */
	initCodeField(c, row, topPad);
	initDescriptionField(c, ++row, topPad);
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

    /** Inits the code field */
    private void initCodeField(GridBagConstraints c, int row, int topPad) {

	c.gridy = row;
	c.insets.top = topPad;

	/* Label */
	String text = Main.getString("fina2.code") + ":";
	text = UIManager.formatedHtmlString(text);
	add(new JLabel(text), c);

	/* Field */
	@SuppressWarnings("serial")
	PlainDocument maxLengthDocument = new PlainDocument() {
	    @Override
	    public void insertString(int offs, String str, AttributeSet a)
		    throws BadLocationException {
		if (getLength() + str.length() > 12)
		    java.awt.Toolkit.getDefaultToolkit().beep();
		else
		    super.insertString(offs, str, a);
	    }
	};
	codeField = new JTextField(maxLengthDocument, "", 12);
	codeField.setPreferredSize(new Dimension(100, 20));
	add(codeField, c);
	codeField.setFont(ui.getFont());
    }

    /** Inits the description field */
    private void initDescriptionField(GridBagConstraints c, int row, int topPad) {

	c.gridy = row;
	c.insets.top = topPad;

	/* Label */
	String text = Main.getString("fina2.description") + ":";
	text = UIManager.formatedHtmlString(text);
	add(new JLabel(text), c);

	/* Field */
	descriptionField = new JTextField();
	descriptionField.setPreferredSize(new Dimension(250, 20));
	add(descriptionField, c);
	descriptionField.setFont(ui.getFont());
    }

    /** Inits the data */
    private void initData() throws Exception {

	if (getModeType() != ModeType.CREATE) {
	    /* Not create mode. Required loading data from server */
	    loadData();
	}
    }

    /** Loads the data from the server */
    private void loadData() throws Exception {

	RolePK rolePK = getKey();
	Role role = SecurityGate.getRole(rolePK);

	/* Code */
	String code = role.getCode();
	codeField.setText(code);

	/* Description */
	String description = role.getDescription(Main.main.getLanguageHandle());
	descriptionField.setText(description);
    }

    /** Returns the role code */
    public String getRoleCode() {
	return codeField.getText().trim();
    }

    /** Returns the role name */
    public String getRoleDescription() {
	return descriptionField.getText();
    }

    /** Checks the data */
    public void check() throws FinaTypeException {

	/* Code */
	String code = codeField.getText().trim();
	if (code.length() == 0) {
	    /* Code is empty */
	    throw new FinaTypeException(Type.SECURITY_ROLE_CODE_EMPTY);
	}

	/* Description */
	String description = descriptionField.getText().trim();
	if (description.length() == 0) {
	    /* Description is empty */
	    throw new FinaTypeException(Type.SECURITY_ROLE_DESCRIPTION_EMPTY);
	}

    }

    /** Saves the data */
    public void save() throws Exception {

	/* Getting role object. If this is a create mode, a new role is created */
	Role role = getRole();

	try {
	    /* Code field */
	    String text = codeField.getText().trim();
	    role.setCode(text);

	    /* Description field */
	    text = descriptionField.getText();
	    role.setDescription(Main.main.getLanguageHandle(), text);

	} catch (FinaTypeException e) {
	    e.printStackTrace();
	    Main.generalErrorHandler(e);
	} catch (Exception e) {

	    if (getModeType() == ModeType.CREATE) {
		/* CREATE mode. The created role should be deleted */
		role.remove();
	    }

	    /* Passing further */
	    throw e;
	}
    }

    /**
     * Returns a role object for current view. If current mode type CREATE, a
     * new role is created. For EDIT/REVIEW modes the current role is returned.
     */
    private Role getRole() throws Exception {

	/* The result role */
	Role role = null;

	if (getModeType() == ModeType.CREATE) {
	    /* CREATE mode. Creating a new role */
	    role = SecurityGate.createRole();

	    /* Setting the new role key */
	    setKey((RolePK) role.getPrimaryKey());
	} else {
	    /* EDIT/REVIEW mode. Getting the current role */
	    RolePK rolePK = getKey();
	    role = SecurityGate.getRole(rolePK);
	}

	/* The result role */
	return role;
    }
}
