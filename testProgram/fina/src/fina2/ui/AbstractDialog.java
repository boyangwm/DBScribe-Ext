package fina2.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Defines a dialog box class. This dialog has the OK and Cancel buttons at the
 * bottom.
 */
@SuppressWarnings("serial")
public abstract class AbstractDialog extends JDialog {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	/**
	 * Contains information about dialog result
	 */
	public enum DialogResult {
		OK, CANCEL
	}

	/** Main work space of the dialog */
	private JPanel mainPanel = null;

	/** OK button */
	private JButton okButton = null;

	/** Cancel button */
	private JButton cancelButton = null;

	/** The dialog result */
	private DialogResult dialogResult = null;

	private JPanel southPanel;

	/** Inits an instance of the class */
	protected AbstractDialog(JFrame owner, boolean isModal) {
		super(owner, isModal);

		initBehaviour();
		initComponents();
	}

	/** Inits the dialog behaviour */
	private void initBehaviour() {
		/* Close by default */
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	/** Shows or hides this dialog depending on the value of the parameter */
	public void setVisible(boolean visible) {

		if (visible) {
			/* Locate the dialog relative to parent */
			setLocationRelativeTo(getParent());
		}

		super.setVisible(visible);
	}

	/** Inits the dialog components */
	private void initComponents() {
		/*
		 * The contents of the dialog is diveded into two parts: main panel and
		 * bottom-button panel. Main panel is located in BorderLayout.CENTER;
		 * botton-button - in BorderLayout.SOUTH.
		 */
		setLayout(new BorderLayout());

		/* Init main pane */
		initMainPane();

		/* Init the button panel */
		initButtonPanel();
	}

	/** Inits the main pane */
	private void initMainPane() {
		mainPanel = new JPanel();
		add(mainPanel, BorderLayout.CENTER); // add to the dialog

		mainPanel.setLayout(new BorderLayout());
	}

	/**
	 * Initializes the button panel. It contains the buttons OK and Cancel which
	 * are right aligned. Both buttons should have the same size in spite of the
	 * words length in any language.
	 */
	private void initButtonPanel() {
		southPanel = new JPanel(new BorderLayout());
		JPanel bottomPanel = new JPanel();
		southPanel.add(bottomPanel, BorderLayout.EAST);
		add(southPanel, BorderLayout.SOUTH); // add to the dialog

		bottomPanel.setBorder(new EmptyBorder(0, 5, 4, 4));

		/* For right alignment */
		FlowLayout buttonLayout = new FlowLayout();
		buttonLayout.setAlignment(FlowLayout.RIGHT);
		bottomPanel.setLayout(buttonLayout);

		/* Grid for the buttons - to make the buttons the same size */
		JPanel buttonPanel = new JPanel();
		bottomPanel.add(buttonPanel);

		buttonPanel.setLayout(new GridLayout(1, 2, 5, 0));

		/*
		 * OK button
		 */

		okButton = new JButton(ui.getString("fina2.ok"));
		okButton.setFont(ui.getFont());
		okButton.setIcon(ui.getIcon("fina2.ok"));
		buttonPanel.add(okButton);

		ActionListener okHandler = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				okButtonPressedInternal();
			}
		};

		okButton.addActionListener(okHandler);

		/*
		 * Cancel button
		 */
		String text = fina2.Main.getString("fina2.cancel");
		cancelButton = new JButton(text);
		cancelButton.setIcon(ui.getIcon("fina2.cancel"));
		cancelButton.setFont(ui.getFont());
		buttonPanel.add(cancelButton);

		ActionListener cancelHandler = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				cancelButtonPressedInternal();
			}
		};

		cancelButton.addActionListener(cancelHandler);
	}

	/** Handler for the OK button. Used internally. */
	private void okButtonPressedInternal() {
		okButtonPressed();
		dialogResult = DialogResult.OK;
	}

	/**
	 * Handler for the OK button. The concrete implementation of this class
	 * should specify the handling for this button.
	 */
	protected abstract void okButtonPressed();

	/** Handler for the Cancel button. Used internally */
	private void cancelButtonPressedInternal() {
		dialogResult = DialogResult.CANCEL;
		cancelButtonPressed();
	}

	/** Handles the Cancel button */
	protected void cancelButtonPressed() {
		dispose();
	}

	/** Sets the OK button text */
	protected final void setOkButtonText(String text) {
		okButton.setText(text);
	}

	/** Sets the Cancel button text */
	protected final void setCancelButtonText(String text) {
		cancelButton.setText(text);
	}

	/** Returns the main pane (workarea) */
	protected JComponent getMainPane() {
		return mainPanel;
	}

	/** Returns the dialog result - which button was pressed (OK, Cancel) */
	public DialogResult getDialogResult() {
		return dialogResult;
	}

	public JPanel getSouthPanel() {
		return southPanel;
	}

	public void setSouthPanel(JPanel southPanel) {
		this.southPanel = southPanel;
	}
}
