/*
 * BranchManagAmendDialog.java
 *
 * Created on March 20, 2002, 9:34 PM
 */

package fina2.bank;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Field;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.border.Border;
import javax.transaction.UserTransaction;

import fina2.Main;
import fina2.ui.UIManager;
import fina2.ui.table.TableRow;
import fina2.ui.tree.Node;

/**
 * 
 * @author vasop
 */
public class ManagAmendDialog extends javax.swing.JDialog {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private ManagingBodySelectDialog managingBodyTypeDialog;
	private ManagingBodyPK managingBodyPK;

	private BankManag bankManag;
	private BranchManag branchManag;

	private BankManagPK bankManagPK;
	private BranchManagPK branchManagPK;
	private Node node;
	private BankPK bankPK;
	private BranchPK branchPK;
	private boolean canAmend;
	private boolean ok;
	private boolean bankManagInd;

	private String errorString = "This field is Requierd";

	/** Creates new form BranchManagAmendDialog */
	public ManagAmendDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);

		managingBodyTypeDialog = new ManagingBodySelectDialog(parent, true);

		ui.loadIcon("fina2.cancel", "cancel.gif");
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.ok", "ok.gif");
		ui.loadIcon("fina2.find", "find.gif");

		initComponents();
	}

	public Node getNode() {
		return node;
	}

	public void show(Node node, BankPK bankPK, boolean canAmend) {
		this.node = node;
		this.bankPK = bankPK;
		this.canAmend = canAmend;
		bankManagInd = true;

		nameText.setEditable(canAmend);
		lastNameText.setEditable(canAmend);
		postText.setEditable(canAmend);
		phoneText.setEditable(canAmend);
		appointmantDateText.setEditable(canAmend);
		cancelDateText.setEditable(canAmend);
		registration1Text.setEditable(canAmend);
		registration2Text.setEditable(canAmend);
		registration3Text.setEditable(canAmend);
		comments1Text.setEditable(canAmend);
		comments2Text.setEditable(canAmend);

		if (node != null) {

			bankManagPK = (BankManagPK) node.getPrimaryKey();

			try {
				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi.lookup("fina2/bank/BankManag");
				BankManagHome home = (BankManagHome) PortableRemoteObject.narrow(ref, BankManagHome.class);
				bankManag = home.findByPrimaryKey(bankManagPK);

				ref = jndi.lookup("fina2/bank/ManagingBody");
				ManagingBodyHome managingBodyHome = (ManagingBodyHome) PortableRemoteObject.narrow(ref, ManagingBodyHome.class);
				managingBodyPK = bankManag.getManagingBodyPK();
				ManagingBody managingBody = managingBodyHome.findByPrimaryKey(managingBodyPK);

				nameText.setText(bankManag.getName(main.getLanguageHandle()));
				lastNameText.setText(bankManag.getLastName(main.getLanguageHandle()));
				managingBodyText.setText(managingBody.getManagingBody(main.getLanguageHandle()));
				postText.setText(bankManag.getPost(main.getLanguageHandle()));
				phoneText.setText(bankManag.getPhone());
				appointmantDateText.setText(bankManag.getDate(main.getLanguageHandle()));
				cancelDateText.setText(bankManag.getDateOfChange(main.getLanguageHandle()));
				registration1Text.setText(bankManag.getRegistration1(main.getLanguageHandle()));
				registration2Text.setText(bankManag.getRegistration2(main.getLanguageHandle()));
				registration3Text.setText(bankManag.getRegistration3(main.getLanguageHandle()));
				comments1Text.setText(bankManag.getComments1(main.getLanguageHandle()));
				comments2Text.setText(bankManag.getComments2(main.getLanguageHandle()));
			} catch (Exception e) {
				Main.generalErrorHandler(e);
				return;
			}
		} else {
			nameText.setText("");
			lastNameText.setText("");
			managingBodyText.setText("");
			postText.setText("");
			phoneText.setText("");
			appointmantDateText.setText("");
			cancelDateText.setText("");
			registration1Text.setText("");
			registration2Text.setText("");
			registration3Text.setText("");
			comments1Text.setText("");
			comments2Text.setText("");
		}
		setLocationRelativeTo(getParent());
		show();
	}

	public void show(Node node, BranchPK branchPK, boolean canAmend) {
		this.node = node;
		this.branchPK = branchPK;
		this.canAmend = canAmend;
		bankManagInd = false;

		nameText.setEditable(canAmend);
		lastNameText.setEditable(canAmend);
		postText.setEditable(canAmend);
		phoneText.setEditable(canAmend);
		appointmantDateText.setEditable(canAmend);
		cancelDateText.setEditable(canAmend);
		registration1Text.setEditable(canAmend);
		registration2Text.setEditable(canAmend);
		registration3Text.setEditable(canAmend);
		comments1Text.setEditable(canAmend);
		comments2Text.setEditable(canAmend);

		if (node != null) {

			branchManagPK = (BranchManagPK) node.getPrimaryKey();

			try {
				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi.lookup("fina2/bank/BranchManag");
				BranchManagHome home = (BranchManagHome) PortableRemoteObject.narrow(ref, BranchManagHome.class);
				branchManag = home.findByPrimaryKey(branchManagPK);

				ref = jndi.lookup("fina2/bank/ManagingBody");
				ManagingBodyHome managingBodyHome = (ManagingBodyHome) PortableRemoteObject.narrow(ref, ManagingBodyHome.class);
				managingBodyPK = branchManag.getManagingBodyPK();
				ManagingBody managingBody = managingBodyHome.findByPrimaryKey(managingBodyPK);

				nameText.setText(branchManag.getName(main.getLanguageHandle()));
				lastNameText.setText(branchManag.getLastName(main.getLanguageHandle()));
				managingBodyText.setText(managingBody.getManagingBody(main.getLanguageHandle()));
				postText.setText(branchManag.getPost(main.getLanguageHandle()));
				phoneText.setText(branchManag.getPhone());
				appointmantDateText.setText(branchManag.getDate(main.getLanguageHandle()));
				cancelDateText.setText(branchManag.getDateOfChange(main.getLanguageHandle()));
				registration1Text.setText(branchManag.getRegistration1(main.getLanguageHandle()));
				registration2Text.setText(branchManag.getRegistration2(main.getLanguageHandle()));
				registration3Text.setText(branchManag.getRegistration3(main.getLanguageHandle()));
				comments1Text.setText(branchManag.getComments1(main.getLanguageHandle()));
				comments2Text.setText(branchManag.getComments2(main.getLanguageHandle()));
			} catch (Exception e) {
				Main.generalErrorHandler(e);
				return;
			}
		} else {
			nameText.setText("");
			lastNameText.setText("");
			managingBodyText.setText("");
			postText.setText("");
			phoneText.setText("");
			appointmantDateText.setText("");
			cancelDateText.setText("");
			registration1Text.setText("");
			registration2Text.setText("");
			registration3Text.setText("");
			comments1Text.setText("");
			comments2Text.setText("");
		}
		setLocationRelativeTo(getParent());
		show();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {// GEN-BEGIN:initComponents
		jPanel1 = new javax.swing.JPanel();
		nameLabel = new javax.swing.JLabel();
		nameText = new javax.swing.JTextField();
		lastNameLabel = new javax.swing.JLabel();
		lastNameText = new javax.swing.JTextField();
		managingBodyLabel = new javax.swing.JLabel();
		managingBodyText = new javax.swing.JTextField();
		managingBodyButton = new javax.swing.JButton();
		postLabel = new javax.swing.JLabel();
		postText = new javax.swing.JTextField();
		phoneLabel = new javax.swing.JLabel();
		phoneText = new javax.swing.JTextField();
		appointmantDateLabel = new javax.swing.JLabel();
		appointmantDateText = new javax.swing.JTextField();
		cancelDateLabel = new javax.swing.JLabel();
		cancelDateText = new javax.swing.JTextField();
		registration1Label = new javax.swing.JLabel();
		registration1Text = new javax.swing.JTextField();
		registration2Label = new javax.swing.JLabel();
		registration2Text = new javax.swing.JTextField();
		registration3Label = new javax.swing.JLabel();
		registration3Text = new javax.swing.JTextField();
		comments1Label = new javax.swing.JLabel();
		comments1Text = new javax.swing.JTextField();
		comments2Label = new javax.swing.JLabel();
		comments2Text = new javax.swing.JTextField();
		jPanel2 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		helpButton = new javax.swing.JButton();
		jPanel4 = new javax.swing.JPanel();
		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		textFildBorder = nameText.getBorder();

		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		jPanel1.setLayout(new java.awt.GridBagLayout());

		jPanel1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(30, 70, 30, 70)));
		nameLabel.setText(UIManager.formatedHtmlString(ui.getString("fina2.bank.staffName")));
		nameLabel.setFont(ui.getFont());
		jPanel1.add(nameLabel, UIManager.getGridBagConstraints(-1, -1, 10, 10, -1, -1, java.awt.GridBagConstraints.EAST, -1, null));

		nameText.setColumns(15);
		nameText.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!ui.isValidName(nameText.getText())) {
					if (!isVisibleAnyErrorWindow(nameTextErrorWindow))
						if (nameTextErrorWindow == null) {
							nameTextErrorWindow = ui.showErrorWindow(jPanel1, errorString, nameText.getLocationOnScreen());
						} else {
							nameTextErrorWindow.setVisible(true);
						}
					nameText.setBorder(BorderFactory.createLineBorder(Color.RED));
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				nameText.setBorder(textFildBorder);
				if (nameTextErrorWindow != null) {
					nameTextErrorWindow.setVisible(false);
				}
			}
		});
		nameText.setFont(ui.getFont());
		jPanel1.add(nameText, UIManager.getGridBagConstraints(1, 0, -1, -1, -1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		lastNameLabel.setText(UIManager.formatedHtmlString(ui.getString("fina2.bank.staffLastName")));
		lastNameLabel.setFont(ui.getFont());
		jPanel1.add(lastNameLabel, UIManager.getGridBagConstraints(2, 0, 10, 10, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 5, 0, 0)));

		lastNameText.setColumns(15);
		lastNameText.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!ui.isValidName(lastNameText.getText())) {
					if (!isVisibleAnyErrorWindow(lastNameTextErrorWindow))
						if (lastNameTextErrorWindow == null) {
							lastNameTextErrorWindow = ui.showErrorWindow(jPanel1, errorString, lastNameText.getLocationOnScreen());
						} else {
							lastNameTextErrorWindow.setVisible(true);
						}
					lastNameText.setBorder(BorderFactory.createLineBorder(Color.RED));
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				lastNameText.setBorder(textFildBorder);
				if (lastNameTextErrorWindow != null) {
					lastNameTextErrorWindow.setVisible(false);
				}
			}
		});
		lastNameText.setFont(ui.getFont());
		jPanel1.add(lastNameText, UIManager.getGridBagConstraints(3, 0, -1, -1, -1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		managingBodyLabel.setText(UIManager.formatedHtmlString(ui.getString("fina2.bank.managingBody")));
		managingBodyLabel.setFont(ui.getFont());
		jPanel1.add(managingBodyLabel, UIManager.getGridBagConstraints(0, 1, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		managingBodyText.setEditable(false);
		managingBodyText.setColumns(15);
		managingBodyText.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!ui.isValidName(managingBodyText.getText())) {
					if (!isVisibleAnyErrorWindow(managingBodyErrorWindow))
						if (managingBodyErrorWindow == null) {
							managingBodyErrorWindow = ui.showErrorWindow(jPanel1, errorString, managingBodyText.getLocationOnScreen());
						} else {
							managingBodyErrorWindow.setVisible(true);
						}
					managingBodyText.setBorder(BorderFactory.createLineBorder(Color.RED));
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				managingBodyText.setBorder(textFildBorder);
				if (managingBodyErrorWindow != null) {
					managingBodyErrorWindow.setVisible(false);
				}
			}
		});
		managingBodyText.setFont(ui.getFont());
		jPanel1.add(managingBodyText, UIManager.getGridBagConstraints(1, 1, -1, -1, -1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		managingBodyButton.setIcon(ui.getIcon("fina2.find"));
		managingBodyButton.setFont(ui.getFont());
		managingBodyButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				managingBodyText.setBorder(textFildBorder);
				if (managingBodyErrorWindow != null) {
					managingBodyErrorWindow.setVisible(false);
				}
				managingBodyButtonActionPerformed(evt);
			}
		});

		jPanel1.add(managingBodyButton, UIManager.getGridBagConstraints(2, 1, -1, -1, -1, -1, -1, -1, new java.awt.Insets(0, 5, 0, 0)));

		postLabel.setText(UIManager.formatedHtmlString(ui.getString("fina2.bank.staffPost")));
		postLabel.setFont(ui.getFont());
		jPanel1.add(postLabel, UIManager.getGridBagConstraints(0, 2, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		postText.setColumns(15);
		postText.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!ui.isValidName(postText.getText())) {
					if (!isVisibleAnyErrorWindow(postTextErrorWindow))
						if (postTextErrorWindow == null) {
							postTextErrorWindow = ui.showErrorWindow(jPanel1, errorString, postText.getLocationOnScreen());
						} else {
							postTextErrorWindow.setVisible(true);
						}
					postText.setBorder(BorderFactory.createLineBorder(Color.RED));
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				postText.setBorder(textFildBorder);
				if (postTextErrorWindow != null) {
					postTextErrorWindow.setVisible(false);
				}
			}
		});
		postText.setFont(ui.getFont());
		jPanel1.add(postText, UIManager.getGridBagConstraints(1, 2, -1, -1, -1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		phoneLabel.setText(UIManager.formatedHtmlString(ui.getString("fina2.bank.phone")));
		phoneLabel.setFont(ui.getFont());
		jPanel1.add(phoneLabel, UIManager.getGridBagConstraints(2, 2, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		phoneText.setColumns(15);
		phoneText.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!ui.isValidName(phoneText.getText())) {
					if (!isVisibleAnyErrorWindow(phoneTextErrorwinWindow))
						if (phoneTextErrorwinWindow == null) {
							phoneTextErrorwinWindow = ui.showErrorWindow(jPanel1, errorString, phoneText.getLocationOnScreen());
						} else {
							phoneTextErrorwinWindow.setVisible(true);
						}
					phoneText.setBorder(BorderFactory.createLineBorder(Color.RED));
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				phoneText.setBorder(textFildBorder);
				if (phoneTextErrorwinWindow != null) {
					phoneTextErrorwinWindow.setVisible(false);
				}
			}
		});
		phoneText.setFont(ui.getFont());
		jPanel1.add(phoneText, UIManager.getGridBagConstraints(3, 2, -1, -1, -1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		appointmantDateLabel.setText(UIManager.formatedHtmlString(ui.getString("fina2.bank.staffDateOfAppointmant")));
		appointmantDateLabel.setFont(ui.getFont());
		jPanel1.add(appointmantDateLabel, UIManager.getGridBagConstraints(0, 3, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		appointmantDateText.setColumns(15);
		appointmantDateText.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!ui.isValidName(appointmantDateText.getText())) {
					if (!isVisibleAnyErrorWindow(appointmantDateWindow))
						if (appointmantDateWindow == null) {
							appointmantDateWindow = ui.showErrorWindow(jPanel1, errorString, appointmantDateText.getLocationOnScreen());
						} else {
							appointmantDateWindow.setVisible(true);
						}
					appointmantDateText.setBorder(BorderFactory.createLineBorder(Color.RED));
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				appointmantDateText.setBorder(textFildBorder);
				if (appointmantDateWindow != null) {
					appointmantDateWindow.setVisible(false);
				}
			}
		});
		appointmantDateText.setFont(ui.getFont());
		jPanel1.add(appointmantDateText, UIManager.getGridBagConstraints(1, 3, -1, -1, -1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		cancelDateLabel.setText(UIManager.formatedHtmlString(ui.getString("fina2.bank.staffCancelDate")));
		cancelDateLabel.setFont(ui.getFont());
		jPanel1.add(cancelDateLabel, UIManager.getGridBagConstraints(2, 3, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		cancelDateText.setColumns(15);
		cancelDateText.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!ui.isValidName(cancelDateText.getText())) {
					if (!isVisibleAnyErrorWindow(cancelDateWindow))
						if (cancelDateWindow == null) {
							cancelDateWindow = ui.showErrorWindow(jPanel1, errorString, cancelDateText.getLocationOnScreen());
						} else {
							cancelDateWindow.setVisible(true);
						}
					cancelDateText.setBorder(BorderFactory.createLineBorder(Color.RED));
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				cancelDateText.setBorder(textFildBorder);
				if (cancelDateWindow != null) {
					cancelDateWindow.setVisible(false);
				}
			}
		});
		cancelDateText.setFont(ui.getFont());
		jPanel1.add(cancelDateText, UIManager.getGridBagConstraints(3, 3, -1, -1, -1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		registration1Label.setText(ui.getString("fina2.bank.staffRegistrationForm1"));
		registration1Label.setFont(ui.getFont());
		jPanel1.add(registration1Label, UIManager.getGridBagConstraints(0, 4, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		registration1Text.setColumns(40);
		registration1Text.setFont(ui.getFont());
		jPanel1.add(registration1Text, UIManager.getGridBagConstraints(1, 4, -1, -1, 3, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		registration2Label.setText(ui.getString("fina2.bank.staffRegistrationForm2"));
		registration2Label.setFont(ui.getFont());
		jPanel1.add(registration2Label, UIManager.getGridBagConstraints(0, 5, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		registration2Text.setColumns(40);
		registration2Text.setFont(ui.getFont());
		jPanel1.add(registration2Text, UIManager.getGridBagConstraints(1, 5, -1, -1, 3, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		registration3Label.setText(ui.getString("fina2.bank.staffRegistrationForm3"));
		registration3Label.setFont(ui.getFont());
		jPanel1.add(registration3Label, UIManager.getGridBagConstraints(0, 6, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		registration3Text.setColumns(40);
		registration3Text.setFont(ui.getFont());
		jPanel1.add(registration3Text, UIManager.getGridBagConstraints(1, 6, -1, -1, 3, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		comments1Label.setText(ui.getString("fina2.bank.staffComments1"));
		comments1Label.setFont(ui.getFont());
		jPanel1.add(comments1Label, UIManager.getGridBagConstraints(0, 7, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		comments1Text.setColumns(40);
		comments1Text.setFont(ui.getFont());
		jPanel1.add(comments1Text, UIManager.getGridBagConstraints(1, 7, -1, -1, 3, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		comments2Label.setText(ui.getString("fina2.bank.staffComments2"));
		comments2Label.setFont(ui.getFont());
		jPanel1.add(comments2Label, UIManager.getGridBagConstraints(0, 8, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		comments2Text.setColumns(40);
		comments2Text.setFont(ui.getFont());
		jPanel1.add(comments2Text, UIManager.getGridBagConstraints(1, 8, -1, -1, 3, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

		jPanel2.setLayout(new java.awt.BorderLayout());

		helpButton.setIcon(ui.getIcon("fina2.help"));
		helpButton.setFont(ui.getFont());
		helpButton.setText(ui.getString("fina2.help"));
		helpButton.setEnabled(false);
		jPanel3.add(helpButton);

		jPanel2.add(jPanel3, java.awt.BorderLayout.WEST);

		okButton.setIcon(ui.getIcon("fina2.ok"));
		okButton.setFont(ui.getFont());
		okButton.setText(ui.getString("fina2.ok"));
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if (ui.isValidName(nameText.getText()) && ui.isValidName(lastNameText.getText()) && ui.isValidName(postText.getText()) && ui.isValidName(phoneText.getText())
						&& ui.isValidName(appointmantDateText.getText()) && ui.isValidName(cancelDateText.getText())) {
					if (ui.isValidName(managingBodyText.getText())) {
						okButtonActionPerformed(evt);
					} else {
						managingBodyText.setFocusable(true);
					}
				}
			}
		});

		jPanel4.add(okButton);

		cancelButton.setIcon(ui.getIcon("fina2.cancel"));
		cancelButton.setFont(ui.getFont());
		cancelButton.setText(ui.getString("fina2.cancel"));
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		jPanel4.add(cancelButton);

		jPanel2.add(jPanel4, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

		pack();
	}// GEN-END:initComponents

	private boolean isVisibleAnyErrorWindow(JWindow window) {
		Class<?> clazz = this.getClass();
		Field[] fields = clazz.getDeclaredFields();
		boolean result = false;

		for (Field f : fields) {
			Object obj = null;
			try {
				obj = f.get(this);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			if (obj != null)
				if (obj instanceof JWindow) {
					JWindow w = (JWindow) obj;
					if (!w.equals(window)) {
						if (w.isVisible()) {
							result = true;
						}
					}
				}
		}
		return result;
	}

	private void disabeAllErrorWindows() {
		Class<?> clazz = this.getClass();
		Field[] fields = clazz.getDeclaredFields();

		for (Field f : fields) {
			Object obj = null;
			try {
				obj = f.get(this);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			if (obj != null)
				if (obj instanceof JWindow) {
					JWindow w = (JWindow) obj;
					w.dispose();
				}
		}
	}

	private void managingBodyButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:
		// event_managingBodyButtonActionPerformed
		managingBodyTypeDialog.show();
		TableRow row = managingBodyTypeDialog.getTableRow();
		if (row == null) {
			managingBodyPK = null;
			return;
		}
		managingBodyPK = (ManagingBodyPK) row.getPrimaryKey();
		managingBodyText.setText(row.getValue(0));
	}// GEN-LAST:event_managingBodyButtonActionPerformed

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-
		// FIRST
		// :
		// event_okButtonActionPerformed
		if (canAmend) {

			if (appointmantDateText.getText().trim().equals("")) {
				ui.showMessageBox(null, ui.getString("fina2.title"), "Please specify date of appointment.");
				return;
			}
			try {
				InitialContext jndi = fina2.Main.getJndiContext();
				if (bankManagInd) {
					Object ref = jndi.lookup("fina2/bank/BankManag");
					BankManagHome home = (BankManagHome) PortableRemoteObject.narrow(ref, BankManagHome.class);
					if (node == null) {
						bankManag = home.create();
					}
				} else {
					Object ref = jndi.lookup("fina2/bank/BranchManag");
					BranchManagHome home = (BranchManagHome) PortableRemoteObject.narrow(ref, BranchManagHome.class);

					if (node == null) {
						branchManag = home.create();
					}
				}
				UserTransaction trans = main.getUserTransaction(jndi);
				trans.begin();

				try {
					if (bankManagInd) {
						bankManag.setName(main.getLanguageHandle(), nameText.getText().trim());
						bankManag.setLasttName(main.getLanguageHandle(), lastNameText.getText().trim());
						bankManag.setManagingBodyPK((ManagingBodyPK) managingBodyPK);
						bankManag.setPost(main.getLanguageHandle(), postText.getText().trim());
						bankManag.setPhone(phoneText.getText().trim());
						bankManag.setDate(main.getLanguageHandle(), appointmantDateText.getText().trim());
						bankManag.setDateOfChange(main.getLanguageHandle(), cancelDateText.getText().trim());
						bankManag.setRegistration1(main.getLanguageHandle(), registration1Text.getText().trim());
						bankManag.setRegistration2(main.getLanguageHandle(), registration2Text.getText().trim());
						bankManag.setRegistration3(main.getLanguageHandle(), registration3Text.getText().trim());
						bankManag.setComments1(main.getLanguageHandle(), comments1Text.getText().trim());
						bankManag.setComments2(main.getLanguageHandle(), comments2Text.getText().trim());
						bankManag.setBankPK(bankPK);
					} else {
						branchManag.setName(main.getLanguageHandle(), nameText.getText().trim());
						branchManag.setLasttName(main.getLanguageHandle(), lastNameText.getText().trim());
						branchManag.setManagingBodyPK((ManagingBodyPK) managingBodyPK);
						branchManag.setPost(main.getLanguageHandle(), postText.getText().trim());
						branchManag.setPhone(phoneText.getText().trim());
						branchManag.setDate(main.getLanguageHandle(), appointmantDateText.getText().trim());
						branchManag.setDateOfChange(main.getLanguageHandle(), cancelDateText.getText().trim());
						branchManag.setRegistration1(main.getLanguageHandle(), registration1Text.getText().trim());
						branchManag.setRegistration2(main.getLanguageHandle(), registration2Text.getText().trim());
						branchManag.setRegistration3(main.getLanguageHandle(), registration3Text.getText().trim());
						branchManag.setComments1(main.getLanguageHandle(), comments1Text.getText().trim());
						branchManag.setComments2(main.getLanguageHandle(), comments2Text.getText().trim());
						branchManag.setBranchPK(branchPK);
					}

					if (managingBodyPK == null | nameText.getText().equals("") | lastNameText.getText().equals("") | postText.getText().equals("") | appointmantDateText.getText().equals("")) {
						trans.rollback();
						if (node == null) {
							if (bankManagInd) {
								bankManag.remove();
								bankManag = null;
							} else {
								branchManag.remove();
								branchManag = null;
							}
						}
						ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.bank.fieldRequired"));
						return;
					}

					trans.commit();
				} catch (Exception e) {
					trans.rollback();
					if (node == null) {
						if (bankManagInd) {
							bankManag.remove();
							bankManag = null;
						} else {
							branchManag.remove();
							branchManag = null;
						}
					}
					Main.generalErrorHandler(e);
					return;
				}
				if (node == null) {
					if (bankManagInd) {
						node = new Node(bankManag.getPrimaryKey(), "[" + managingBodyText.getText().trim() + "] " + nameText.getText().trim() + " " + lastNameText.getText().trim(), new Integer(3));
						managingBodyPK = bankManag.getManagingBodyPK();
					} else {
						node = new Node(branchManag.getPrimaryKey(), "[" + managingBodyText.getText().trim() + "] " + nameText.getText().trim() + " " + lastNameText.getText().trim(), new Integer(5));
						managingBodyPK = branchManag.getManagingBodyPK();
					}
				} else {
					node.setLabel("[" + managingBodyText.getText().trim() + "] " + nameText.getText().trim() + " " + lastNameText.getText().trim());
					if (bankManagInd) {
						managingBodyPK = bankManag.getManagingBodyPK();
					} else {
						managingBodyPK = branchManag.getManagingBodyPK();
					}
				}
			} catch (Exception e) {
				Main.generalErrorHandler(e);
			}
		}

		dispose();

	}// GEN-LAST:event_okButtonActionPerformed

	private void setBorderAllJTextFild(Border border) {
		Class<?> clazz = this.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (Field f : fields) {
			Object obj = null;
			try {
				obj = f.get(this);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			if (obj != null)
				if (obj instanceof JTextField) {
					JTextField text = (JTextField) obj;
					text.setBorder(border);
				}
		}
	}

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		// -
		// FIRST
		// :
		disabeAllErrorWindows();
		setBorderAllJTextFild(textFildBorder);
		// event_cancelButtonActionPerformed
		setVisible(false);
		dispose();
	}// GEN-LAST:event_cancelButtonActionPerformed

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt) {// GEN-FIRST:
		cancelButtonActionPerformed(null);
	}// GEN-LAST:event_closeDialog

	// Error windows
	private JWindow nameTextErrorWindow;
	private JWindow lastNameTextErrorWindow;
	private JWindow managingBodyErrorWindow;
	private JWindow postTextErrorWindow;
	private JWindow phoneTextErrorwinWindow;
	private JWindow appointmantDateWindow;
	private JWindow cancelDateWindow;

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.border.Border textFildBorder;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JLabel nameLabel;
	private javax.swing.JTextField nameText;
	private javax.swing.JLabel lastNameLabel;
	private javax.swing.JTextField lastNameText;
	private javax.swing.JLabel managingBodyLabel;
	private javax.swing.JTextField managingBodyText;
	private javax.swing.JButton managingBodyButton;
	private javax.swing.JLabel postLabel;
	private javax.swing.JTextField postText;
	private javax.swing.JLabel phoneLabel;
	private javax.swing.JTextField phoneText;
	private javax.swing.JLabel appointmantDateLabel;
	private javax.swing.JTextField appointmantDateText;
	private javax.swing.JLabel cancelDateLabel;
	private javax.swing.JTextField cancelDateText;
	private javax.swing.JLabel registration1Label;
	private javax.swing.JTextField registration1Text;
	private javax.swing.JLabel registration2Label;
	private javax.swing.JTextField registration2Text;
	private javax.swing.JLabel registration3Label;
	private javax.swing.JTextField registration3Text;
	private javax.swing.JLabel comments1Label;
	private javax.swing.JTextField comments1Text;
	private javax.swing.JLabel comments2Label;
	private javax.swing.JTextField comments2Text;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JButton helpButton;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JButton okButton;
	private javax.swing.JButton cancelButton;
	// End of variables declaration//GEN-END:variables

}