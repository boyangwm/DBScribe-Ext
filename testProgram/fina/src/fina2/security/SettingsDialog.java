package fina2.security;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import fina2.Main;
import fina2.calendar.FinaCalendar;
import fina2.system.PropertySession;
import fina2.system.PropertySessionBean;
import fina2.system.PropertySessionHome;

@SuppressWarnings("serial")
public class SettingsDialog extends JDialog {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;
	private FinaCalendar fcalendar;
	private LDAPAuthorizatioDialog authorizatioDialog = new LDAPAuthorizatioDialog(null);;

	public SettingsDialog(Frame owner, String title, boolean modal) {
		super(owner, modal);
		this.setTitle(title);

		ui.loadIcon("fina2.cancel", "cancel.gif");
		ui.loadIcon("fina2.save", "save.gif");

		ui.loadIcon("fina2.mailTab", "fina2-mail.gif");
		ui.loadIcon("fina2.propertiesTab", "fina2-properties.jpg");
		ui.loadIcon("fina2.securityTab", "fina2-security.png");
		ui.loadIcon("fina2.updateTab", "fina2-update.png");

		initComponents();
		insertData();
		mergeSysParameters();
		this.pack();
	}

	// Close SecuritySettingsDialog
	private void closeDialog() {
		this.dispose();
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		insertData();
	}

	// Valid properties
	private void mergeSysParameters() {
		try {
			Map<String, String> props = main.loadSysProperties();

			Collection<String> dbParameters = props.keySet();
			Collection<String> curenParameters = new ArrayList<String>();

			// get Current parameters
			Class<?> clazz = PropertySession.class;
			Field[] fields = clazz.getFields();
			for (Field f : fields) {
				Object obj = f.get(clazz.getClass());
				if (obj instanceof String) {
					String val = (String) obj;
					curenParameters.add(val);
				}
			}

			List<String> notFound = new ArrayList<String>();
			for (String s : curenParameters) {
				if (!dbParameters.contains(s)) {
					notFound.add(s);
				}
			}

			if (notFound.size() > 0) {
				StringBuffer buff = new StringBuffer("Properties not found in database :\n");
				for (String s : notFound) {
					buff.append(s + "\n");
				}
				JOptionPane.showMessageDialog(null, buff.toString(), "Properties Not Found", JOptionPane.WARNING_MESSAGE);
			}
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}

	}

	// insert Data
	private void insertData() {
		try {
			Map<String, String> props = main.loadSysProperties();

			SpinnerNumberModel spinFailedLoginAttempModel = new SpinnerNumberModel(getSysPropertyValue(props, PropertySession.ALLOWED_LOGIN_ATTEMPT_NUMBER), 3, 99, 1);
			failedLoginAttemptNumSpinner.setModel(spinFailedLoginAttempModel);

			int value = getSysPropertyValue(props, PropertySession.PASSWORD_VALIDITY_PERIOD);
			if (value == -1) {
				value = 90;
				passNeverExpCheckBox.setSelected(true);
				passValPeriodSpinner.setEnabled(false);
			} else {
				passNeverExpCheckBox.setSelected(false);
				passValPeriodSpinner.setEnabled(true);
			}
			SpinnerNumberModel spinPasValPeriodModel = new SpinnerNumberModel(value, 1, 999, 1);
			passValPeriodSpinner.setModel(spinPasValPeriodModel);

			value = getSysPropertyValue(props, PropertySession.ALLOWED_ACCOUNT_INACTIVITY_PERIOD);
			if (value == -1) {
				value = 60;
				blockAccountCheckBox.setSelected(false);
				accountInactivityPeriodSpinner.setEnabled(false);
			} else {
				blockAccountCheckBox.setSelected(true);
				accountInactivityPeriodSpinner.setEnabled(true);
			}

			SpinnerNumberModel spinInactivityPeriodModel = new SpinnerNumberModel(value, 1, 999, 1);
			accountInactivityPeriodSpinner.setModel(spinInactivityPeriodModel);

			SpinnerNumberModel spinOldPassNumModel = new SpinnerNumberModel(getSysPropertyValue(props, PropertySession.OLD_STORED_PASSWORDS_NUMBER), 0, 99, 1);
			numOfOldPassSpinner.setModel(spinOldPassNumModel);

			SpinnerNumberModel spinPassMinLenModel = new SpinnerNumberModel(getSysPropertyValue(props, PropertySession.MINIMAL_PASSWORD_LENGTH), 1, 16, 1);
			passMinLengthSpinner.setModel(spinPassMinLenModel);

			value = getSysPropertyValue(props, PropertySession.PASSWORD_WITH_NUMS_AND_CHARS);
			passWithNumCharCheckBox.setSelected(value == 1);

			String updateStart = (String) props.get(PropertySession.UPDATE_START);
			if (updateStart != null) {
				if (updateStart.equals("TRUE")) {
					startUpdateCheckBox.setSelected(true);
				}
			}

			xmlFolderLocationTextField.setText(props.get(PropertySession.FOLDER_LOCATION));
			mfbXlsNamePatternTextField.setText(props.get(PropertySession.NAME_PATTERN));
			sheetProtectionPasswordTextField.setText(props.get(PropertySession.PROTECTION_PASSWORD));
			mfbUploadedFileUniqueTextField.setText(props.get(PropertySession.UPLOADED_FILE_UNIQUE));
			updateGuiFileLocationTextField.setText(props.get(PropertySession.UPDATE_GUI_FILE_LOCATION));
			convertedXmlsTextField.setText(props.get(PropertySession.CONVERTED_XMLS));
			maxReturnCountTextField.setText(props.get(PropertySession.MAX_RETURNS_SIZE));
			matrixPathTextField.setText(props.get(PropertySession.MATRIX_PATH));

			// process timeout set text
			timeOutField.setText(props.get(PropertySession.PROCESS_TIMEOUT));
			// insert LDAP parameters
			ldapUrlIpTextField.setText(props.get(PropertySession.LDAP_URL_IP));
			ldapIpJTextField.setText(props.get(PropertySession.LDAP_URL_PORT));
			organizationalUnitTextField.setText(props.get(PropertySession.LDAP_ORGANIZATIONAL_UNIT));
			domainComponentTestField.setText(props.get(PropertySession.LDAP_DOMAIN_COMPONENT));

			String autenticatedModes = props.get(PropertySession.FINA_AUTHENTICATED_MODES);

			StringTokenizer token = new StringTokenizer(autenticatedModes, ",");

			authorizationTypeComboBox.removeAllItems();
			while (token.hasMoreElements()) {
				authorizationTypeComboBox.addItem(token.nextElement());

			}

			if (isLdapAuthentication()) {
				setDisableLdapProperties(true);
				authorizationTypeComboBox.setSelectedItem("LDAP");
			} else {
				setDisableLdapProperties(false);
				authorizationTypeComboBox.setSelectedItem("FINA");
			}

			// set selected updates
			setSelectedUpdates(props);

			// insert Mail Parameters
			mailTabPanel.insertMailParametersData(props);
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
	}

	private void setSelectedUpdates(Map<String, String> props) {

		if (props.get(PropertySession.UPDATE_START).equals("FALSE")) {
			startUpdateCheckBox.setSelected(false);
			enableAllUpdates();
		} else {
			startUpdateCheckBox.setSelected(true);
		}
		if (getSysPropertyValue(props, PropertySession.UPDATE_ADDIN) < 0) {
			updateFinaAddinCheckBox.setSelected(false);
		} else {
			updateFinaAddinCheckBox.setSelected(true);
		}
		if (getSysPropertyValue(props, PropertySession.UPDATE_FINA_UPDATE) < 0) {
			updateFinaUpdateCheckBox.setSelected(false);
		} else {
			updateFinaUpdateCheckBox.setSelected(true);
		}
		if (getSysPropertyValue(props, PropertySession.UPDATE_RUN_BAT) < 0) {
			updateRunBatCheckBox.setSelected(false);
		} else {
			updateRunBatCheckBox.setSelected(true);
		}
		if (getSysPropertyValue(props, PropertySession.UPDATE_RESOURCES) < 0) {
			updateResourcesCheckBox.setSelected(false);
		} else {
			updateResourcesCheckBox.setSelected(true);
		}
	}

	private int getSysPropertyValue(Map<String, String> props, String key) throws NumberFormatException {
		int result = -1;
		String value = props.get(key);
		if (value != null) {
			result = Integer.parseInt(value);
		}
		return result;
	}

	public static boolean isLdapAuthentication() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/system/PropertySession");
			PropertySessionHome home = (PropertySessionHome) PortableRemoteObject.narrow(ref, PropertySessionHome.class);
			PropertySession session = home.create();
			String currentAuthentication = session.getSystemProperty(PropertySession.FINA_CURRENT_AUTHENTICATION);
			if (currentAuthentication != null)
				if (currentAuthentication.toLowerCase().equals("ldap"))
					return true;
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
		return false;
	}

	private boolean isSelectedFinaAutorization() {
		String selItem = (String) authorizationTypeComboBox.getSelectedItem();
		if (selItem != null)
			if (selItem.toLowerCase().equals("fina"))
				return true;
		return false;
	}

	SettingsDialog getSettingsDialog() {
		return this;
	}

	// initial All Components
	private void initComponents() {
		this.setResizable(false);
		container = this.getContentPane();
		container.setLayout(null);

		// initial Methods
		initMainButtons();
		initTabs();
		initPropertiesTab();
		initUpdateTab();
		mailTabPanel.initMailPropertiesTab();

		// initial Security Components
		securityTabPanel.setLayout(null);

		securityPanel = new JPanel();
		securityPanel.setBounds(0, 0, 450, 260);
		securityPanel.setLayout(null);

		// Rectangles
		Rectangle labelsRectangle = new Rectangle(10, 10, 340, 20);
		Rectangle compRectangle = new Rectangle(360, 10, 44, 20);
		int space = 27;

		allowedLoginAttemptNumberLabel = new JLabel();
		allowedLoginAttemptNumberLabel.setText(ui.getString("fina2.security.allowedLoginAttemptNumber"));
		allowedLoginAttemptNumberLabel.setFont(ui.getFont());
		allowedLoginAttemptNumberLabel.setBounds(labelsRectangle);
		securityPanel.add(allowedLoginAttemptNumberLabel);

		failedLoginAttemptNumSpinner = new JSpinner();
		failedLoginAttemptNumSpinner.setBounds(compRectangle);
		securityPanel.add(failedLoginAttemptNumSpinner);

		passwordNeverExpiresLabel = new JLabel();
		passwordNeverExpiresLabel.setText(ui.getString("fina2.security.passwordNeverExpires"));
		passwordNeverExpiresLabel.setFont(ui.getFont());
		labelsRectangle.y += space;
		passwordNeverExpiresLabel.setBounds(labelsRectangle);
		securityPanel.add(passwordNeverExpiresLabel);

		passNeverExpCheckBox = new JCheckBox();
		compRectangle.y += space;
		compRectangle.x -= 4;
		passNeverExpCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				passNeverExpCheckBox1ItemStateChanged(e);
			}
		});
		passNeverExpCheckBox.setBounds(compRectangle);
		securityPanel.add(passNeverExpCheckBox);

		passwordValidityPeriodLabel = new JLabel();
		labelsRectangle.y += space;
		passwordValidityPeriodLabel.setBounds(labelsRectangle);
		passwordValidityPeriodLabel.setText(ui.getString("fina2.security.passwordValidityPeriod"));
		passwordValidityPeriodLabel.setFont(ui.getFont());
		securityPanel.add(passwordValidityPeriodLabel);

		passValPeriodSpinner = new JSpinner();
		compRectangle.y += space;
		compRectangle.x += 4;
		passValPeriodSpinner.setBounds(compRectangle);
		securityPanel.add(passValPeriodSpinner);

		allowedAccountInactivityPerioedLabel = new JLabel();
		labelsRectangle.y += space;
		allowedAccountInactivityPerioedLabel.setBounds(labelsRectangle);
		allowedAccountInactivityPerioedLabel.setFont(ui.getFont());
		allowedAccountInactivityPerioedLabel.setText(ui.getString("fina2.security.allowedAccountInactivityPerioed"));
		securityPanel.add(allowedAccountInactivityPerioedLabel);

		accountInactivityPeriodSpinner = new JSpinner();
		compRectangle.y += space;
		accountInactivityPeriodSpinner.setBounds(compRectangle);
		securityPanel.add(accountInactivityPeriodSpinner);

		blockAccountAfterDefinedPeriodLabel = new JLabel();
		labelsRectangle.y += space;
		blockAccountAfterDefinedPeriodLabel.setText(ui.getString("fina2.security.blockAccountAfterDefinedPeriod"));
		blockAccountAfterDefinedPeriodLabel.setFont(ui.getFont());
		blockAccountAfterDefinedPeriodLabel.setBounds(labelsRectangle);
		securityPanel.add(blockAccountAfterDefinedPeriodLabel);

		blockAccountCheckBox = new JCheckBox();
		compRectangle.y += space;
		compRectangle.x -= 4;
		blockAccountCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				blockAccountCheckBoxItemStateChanged(e);
			}
		});

		blockAccountCheckBox.setBounds(compRectangle);
		securityPanel.add(blockAccountCheckBox);

		numberOfStoredOldPasswordsLabel = new JLabel();
		labelsRectangle.y += space;
		numberOfStoredOldPasswordsLabel.setBounds(labelsRectangle);
		numberOfStoredOldPasswordsLabel.setText(ui.getString("fina2.security.numberOfStoredOldPasswords"));
		numberOfStoredOldPasswordsLabel.setFont(ui.getFont());
		securityPanel.add(numberOfStoredOldPasswordsLabel);

		numOfOldPassSpinner = new JSpinner();
		compRectangle.y += space;
		compRectangle.x += 4;
		numOfOldPassSpinner.setBounds(compRectangle);
		securityPanel.add(numOfOldPassSpinner);

		passwordMinimalLenLabel = new JLabel();
		labelsRectangle.y += space;
		passwordMinimalLenLabel.setBounds(labelsRectangle);
		passwordMinimalLenLabel.setText(ui.getString("fina2.security.passwordMinimalLen"));
		passwordMinimalLenLabel.setFont(ui.getFont());
		securityPanel.add(passwordMinimalLenLabel);

		passMinLengthSpinner = new JSpinner();
		compRectangle.y += space;
		passMinLengthSpinner.setBounds(compRectangle);
		securityPanel.add(passMinLengthSpinner);

		passwordWithNumsCharsLabel = new JLabel();
		labelsRectangle.y += space;
		passwordWithNumsCharsLabel.setBounds(labelsRectangle);
		passwordWithNumsCharsLabel.setText(ui.getString("fina2.security.passwordWithNumsChars"));
		passwordWithNumsCharsLabel.setFont(ui.getFont());
		securityPanel.add(passwordWithNumsCharsLabel);

		passWithNumCharCheckBox = new JCheckBox();
		compRectangle.y += space;
		compRectangle.x -= 4;
		passWithNumCharCheckBox.setBounds(compRectangle);
		securityPanel.add(passWithNumCharCheckBox);

		authorizationTypeLabel = new JLabel();
		labelsRectangle.y += space;
		authorizationTypeLabel.setBounds(labelsRectangle);
		authorizationTypeLabel.setText(ui.getString("fina2.security.authorizationType"));
		authorizationTypeLabel.setFont(ui.getFont());
		securityPanel.add(authorizationTypeLabel);

		authorizationTypeComboBox = new JComboBox();
		compRectangle.y += space;
		compRectangle.x += 4;
		compRectangle.width += 40;
		authorizationTypeComboBox.setBounds(compRectangle);
		authorizationTypeComboBox.setFont(ui.getFont());

		authorizationTypeComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (isSelectedFinaAutorization()) {
					setDisableLdapProperties(false);
				} else {
					/*
					 * TODO When add LDAP Authentication visible LDAP
					 * Parameters.
					 */
					// setDisableLdapProperties(true);
					authorizatioDialog.setVisible(true, false);
					authorizationTypeComboBox.setSelectedItem("FINA");
				}
			}
		});

		securityPanel.add(authorizationTypeComboBox);

		securityTabPanel.add(securityPanel);

		ldapPropertiesPanel = new JPanel(null);
		ldapPropertiesPanel.setFont(ui.getFont());
		ldapPropertiesPanel.setBounds(4, 260, 442, 115);
		ldapPropertiesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray), ui.getString("fina2.authorization.ldap.ldapParameters"), TitledBorder.LEFT, TitledBorder.TOP, ui.getFont()));
		initLDAPPropertiesComponents();
		securityTabPanel.add(ldapPropertiesPanel);

		mainTab.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {

				if (ui.getString(PropertySession.PROCESS_TIMEOUT) != null) {
					if (timeOutField.getText().trim().length() == 0 || Integer.parseInt(timeOutField.getText()) < PropertySessionBean.START_XML_PROCESS_SERVICE_TIMEOUT) {

						{

							if (mainTab.getSelectedIndex() != 1) {
								mainTab.setSelectedIndex(1);
								ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.process.invalidnumber"));

								return;
							}

						}
					}
				}

			}

		});
	}

	private void setDisableLdapProperties(boolean enable) {
		ldapUrlIpTextField.setEditable(enable);
		ldapIpJTextField.setEditable(enable);
		organizationalUnitTextField.setEditable(enable);
		domainComponentTestField.setEditable(enable);
	}

	private void initLDAPPropertiesComponents() {
		// Rectangles
		Rectangle labelsRectangle = new Rectangle(20, 25, 200, 20);
		Rectangle textFieldRectangle = new Rectangle(210, 25, 100, 20);
		int space = 27;

		ldapURLIpLabel = new JLabel();
		ldapURLIpLabel.setText(ui.getString("fina2.authorization.ldap.ldapUrlIp"));
		ldapURLIpLabel.setFont(ui.getFont());
		ldapURLIpLabel.setBounds(labelsRectangle);
		ldapPropertiesPanel.add(ldapURLIpLabel);

		ldapUrlIpTextField = new JTextField();
		ldapUrlIpTextField.setBounds(textFieldRectangle);
		ldapUrlIpTextField.setFont(ui.getFont());
		ldapPropertiesPanel.add(ldapUrlIpTextField);

		ldapPortLabel = new JLabel();
		ldapPortLabel.setText(ui.getString("fina2.authorization.ldap.port"));
		ldapPortLabel.setFont(ui.getFont());
		ldapPortLabel.setBounds(330, 25, 50, 20);
		ldapPropertiesPanel.add(ldapPortLabel);

		ldapIpJTextField = new JTextField();
		ldapIpJTextField.setBounds(375, 25, 50, 20);
		ldapPropertiesPanel.add(ldapIpJTextField);

		organizationalUnitLabel = new JLabel();
		labelsRectangle.y += space;
		organizationalUnitLabel.setBounds(labelsRectangle);
		organizationalUnitLabel.setText(ui.getString("fina2.authorization.ldap.organizationalUnit"));
		organizationalUnitLabel.setFont(ui.getFont());
		ldapPropertiesPanel.add(organizationalUnitLabel);

		organizationalUnitTextField = new JTextField();
		textFieldRectangle.y += space;
		textFieldRectangle.width += 117;
		organizationalUnitTextField.setBounds(textFieldRectangle);
		organizationalUnitTextField.setFont(ui.getFont());
		ldapPropertiesPanel.add(organizationalUnitTextField);

		domainComponentLabel = new JLabel();
		labelsRectangle.y += space;
		domainComponentLabel.setBounds(labelsRectangle);
		domainComponentLabel.setText(ui.getString("fina2.authorization.ldap.domainComponent"));
		domainComponentLabel.setFont(ui.getFont());
		ldapPropertiesPanel.add(domainComponentLabel);

		domainComponentTestField = new JTextField();
		textFieldRectangle.y += space;
		domainComponentTestField.setBounds(textFieldRectangle);
		domainComponentTestField.setFont(ui.getFont());
		ldapPropertiesPanel.add(domainComponentTestField);
	}

	// initial all tabs
	private void initTabs() {
		mainTab = new JTabbedPane();

		mainTab.setFont(ui.getFont());
		mainTab.setBounds(0, 0, 454, 505);

		securityTabPanel = new JPanel();
		securityTabPanel.setBounds(10, 0, 454, 440);

		propertiesTabPanel = new JPanel();
		propertiesTabPanel.setBounds(0, 0, 454, 440);

		updateTabPanel = new JPanel();
		updateTabPanel.setBounds(0, 0, 454, 440);

		mailTabPanel = new MailPropertiesTab(saveButton);
		mailTabPanel.setBounds(0, 0, 454, 440);

		mainTab.addTab(ui.getString("fina2.security.securityTabName"), ui.getIcon("fina2.securityTab"), securityTabPanel);
		mainTab.addTab(ui.getString("fina2.security.propertiesTabName"), ui.getIcon("fina2.propertiesTab"), propertiesTabPanel);
		mainTab.addTab(ui.getString("fina2.update"), ui.getIcon("fina2.updateTab"), updateTabPanel);
		mainTab.addTab(ui.getString("fina2.settings.mailTabTitle"), ui.getIcon("fina2.mailTab"), mailTabPanel);

		container.add(mainTab, BorderLayout.CENTER);
	}

	// initial Properties Tab
	private void initPropertiesTab() {
		propertiesTabPanel.setLayout(null);

		// rectangles
		Rectangle labelsRectangle = new Rectangle(10, 19, 260, 14);
		Rectangle textFielsdRectanle = new Rectangle(230, 17, 170, 25);
		Rectangle buttonsRectangle = new Rectangle(403, 17, 40, 25);
		int space = 33;

		xmlFolderLocationLabel = new JLabel();
		xmlFolderLocationLabel.setBounds(labelsRectangle);
		xmlFolderLocationLabel.setText(ui.getString("fina2.xml.folder.location"));
		xmlFolderLocationLabel.setFont(ui.getFont());
		propertiesTabPanel.add(xmlFolderLocationLabel);

		xmlFolderLocationTextField = new JTextField();
		xmlFolderLocationTextField.setBounds(textFielsdRectanle);
		xmlFolderLocationTextField.setEditable(false);
		propertiesTabPanel.add(xmlFolderLocationTextField);

		xmlFolderLocationButton = new JButton();
		xmlFolderLocationButton.setBounds(buttonsRectangle);
		xmlFolderLocationButton.setIcon(ui.getIcon("fina2.amend"));
		xmlFolderLocationButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				xmlFolderLocationTextField.setEditable(!xmlFolderLocationTextField.isEditable());

			}
		});
		/*
		 * xmlFolderLocationButton.addActionListener(new ActionListener() {
		 * 
		 * @Override public void actionPerformed(ActionEvent e) { if
		 * (!xmlFolderLocationTextField.isEditable()) { JFileChooser dlg = null;
		 * String path = xmlFolderLocationTextField.getText(); if (path == null)
		 * { dlg = new JFileChooser(); } else { dlg = new JFileChooser(path); }
		 * dlg.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		 * dlg.setMultiSelectionEnabled(false);
		 * 
		 * dlg.setFileFilter(null); if (dlg.showOpenDialog(null) ==
		 * JFileChooser.CANCEL_OPTION) { return; }
		 * 
		 * File file = dlg.getSelectedFile(); if (file != null) {
		 * xmlFolderLocationTextField.setText(file.getPath().replaceAll('\\' +
		 * "\\", "\\\\\\\\") + "\\\\"); } }
		 * 
		 * xmlFolderLocationTextField.setEditable(!xmlFolderLocationTextField.
		 * isEditable()); } });
		 */
		propertiesTabPanel.add(xmlFolderLocationButton);

		mfbXlsNamePatternLabel = new JLabel();
		labelsRectangle.y += space;
		mfbXlsNamePatternLabel.setBounds(labelsRectangle);
		mfbXlsNamePatternLabel.setText(ui.getString("fina2.mfb.xls.name.pattern"));
		mfbXlsNamePatternLabel.setFont(ui.getFont());
		propertiesTabPanel.add(mfbXlsNamePatternLabel);

		mfbXlsNamePatternTextField = new JTextField();
		textFielsdRectanle.y += space;
		mfbXlsNamePatternTextField.setBounds(textFielsdRectanle);
		mfbXlsNamePatternTextField.setEditable(false);
		propertiesTabPanel.add(mfbXlsNamePatternTextField);

		mfbXlsNamePatternButton = new JButton();
		buttonsRectangle.y += space;
		mfbXlsNamePatternButton.setBounds(buttonsRectangle);
		mfbXlsNamePatternButton.setIcon(ui.getIcon("fina2.amend"));
		mfbXlsNamePatternButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mfbXlsNamePatternTextField.setEditable(!mfbXlsNamePatternTextField.isEditable());
			}
		});
		propertiesTabPanel.add(mfbXlsNamePatternButton);

		sheetProtectionPasswordLabel = new JLabel();
		labelsRectangle.y += space;
		sheetProtectionPasswordLabel.setBounds(labelsRectangle);
		sheetProtectionPasswordLabel.setText(ui.getString("fina2.sheet.protection.password"));
		sheetProtectionPasswordLabel.setFont(ui.getFont());
		propertiesTabPanel.add(sheetProtectionPasswordLabel);

		sheetProtectionPasswordTextField = new JTextField();
		textFielsdRectanle.y += space;
		sheetProtectionPasswordTextField.setBounds(textFielsdRectanle);
		sheetProtectionPasswordTextField.setEditable(false);
		sheetProtectionPasswordTextField.setFont(ui.getFont());
		propertiesTabPanel.add(sheetProtectionPasswordTextField);

		sheetProtectionPasswordButton = new JButton();
		buttonsRectangle.y += space;
		sheetProtectionPasswordButton.setBounds(buttonsRectangle);
		sheetProtectionPasswordButton.setIcon(ui.getIcon("fina2.amend"));
		sheetProtectionPasswordButton.setFont(ui.getFont());
		sheetProtectionPasswordButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sheetProtectionPasswordTextField.setEditable(!sheetProtectionPasswordTextField.isEditable());
			}
		});
		propertiesTabPanel.add(sheetProtectionPasswordButton);

		mfbUploadedFileUniqueLabel = new JLabel();
		labelsRectangle.y += space;
		mfbUploadedFileUniqueLabel.setBounds(labelsRectangle);
		mfbUploadedFileUniqueLabel.setText(ui.getString("fina2.mfb.uploaded.file.unique"));
		mfbUploadedFileUniqueLabel.setFont(ui.getFont());
		propertiesTabPanel.add(mfbUploadedFileUniqueLabel);

		mfbUploadedFileUniqueTextField = new JTextField();
		textFielsdRectanle.y += space;
		mfbUploadedFileUniqueTextField.setBounds(textFielsdRectanle);
		mfbUploadedFileUniqueTextField.setEditable(false);
		mfbUploadedFileUniqueTextField.setFont(ui.getFont());
		propertiesTabPanel.add(mfbUploadedFileUniqueTextField);

		mfbUploadedFileUniqueButton = new JButton();
		buttonsRectangle.y += space;
		mfbUploadedFileUniqueButton.setBounds(buttonsRectangle);
		mfbUploadedFileUniqueButton.setIcon(ui.getIcon("fina2.amend"));
		mfbUploadedFileUniqueButton.setFont(ui.getFont());
		mfbUploadedFileUniqueButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mfbUploadedFileUniqueTextField.setEditable(!mfbUploadedFileUniqueTextField.isEditable());
			}
		});
		propertiesTabPanel.add(mfbUploadedFileUniqueButton);

		updateGuiFileLocationLabel = new JLabel();
		labelsRectangle.y += space;
		updateGuiFileLocationLabel.setBounds(labelsRectangle);
		updateGuiFileLocationLabel.setText(ui.getString("fina2.update.guiFileLocation"));
		updateGuiFileLocationLabel.setFont(ui.getFont());
		propertiesTabPanel.add(updateGuiFileLocationLabel);

		updateGuiFileLocationTextField = new JTextField();
		textFielsdRectanle.y += space;
		updateGuiFileLocationTextField.setBounds(textFielsdRectanle);
		updateGuiFileLocationTextField.setEditable(false);
		updateGuiFileLocationTextField.setFont(ui.getFont());
		propertiesTabPanel.add(updateGuiFileLocationTextField);

		updateGuiFileLocationButton = new JButton();
		buttonsRectangle.y += space;
		updateGuiFileLocationButton.setBounds(buttonsRectangle);
		updateGuiFileLocationButton.setFont(ui.getFont());
		updateGuiFileLocationButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateGuiFileLocationTextField.setEditable(!updateGuiFileLocationTextField.isEditable());
			}
		});
		updateGuiFileLocationButton.setIcon(ui.getIcon("fina2.amend"));
		propertiesTabPanel.add(updateGuiFileLocationButton);

		convertedXmlsLabel = new JLabel();
		labelsRectangle.y += space;
		convertedXmlsLabel.setBounds(labelsRectangle);
		convertedXmlsLabel.setText(ui.getString("fina2.converted.xmls"));
		convertedXmlsLabel.setFont(ui.getFont());
		propertiesTabPanel.add(convertedXmlsLabel);

		convertedXmlsTextField = new JTextField();
		textFielsdRectanle.y += space;
		convertedXmlsTextField.setBounds(textFielsdRectanle);
		convertedXmlsTextField.setEditable(false);
		convertedXmlsTextField.setFont(ui.getFont());
		propertiesTabPanel.add(convertedXmlsTextField);

		convertedXmlsButton = new JButton();
		buttonsRectangle.y += space;
		convertedXmlsButton.setBounds(buttonsRectangle);
		convertedXmlsButton.setFont(ui.getFont());
		convertedXmlsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				convertedXmlsTextField.setEditable(!convertedXmlsTextField.isEditable());
			}
		});
		convertedXmlsButton.setIcon(ui.getIcon("fina2.amend"));
		propertiesTabPanel.add(convertedXmlsButton);

		maxReturnCountLabel = new JLabel();
		labelsRectangle.y += space;
		maxReturnCountLabel.setBounds(labelsRectangle);
		maxReturnCountLabel.setText(ui.getString("fina2.returns.maxReturnCount"));
		maxReturnCountLabel.setFont(ui.getFont());
		propertiesTabPanel.add(maxReturnCountLabel);

		// insert numbers
		PlainDocument insertNumbersDocument = new PlainDocument() {
			@Override
			public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
				try {
					Integer.parseInt(str);
					super.insertString(offs, str, a);
				} catch (NumberFormatException ex) {
					java.awt.Toolkit.getDefaultToolkit().beep();
				}
			}
		};
		// insert number for timeout
		PlainDocument insertNumbersDocumenfortimeout = new PlainDocument() {
			@Override
			public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
				try {

					if (timeOutField.getText().length() > 0 && timeOutField.getText().length() > 8) {
						Integer.parseInt("T");
					} else {
						Integer.parseInt(str);
					}
					super.insertString(offs, str, a);

				} catch (NumberFormatException ex) {
					java.awt.Toolkit.getDefaultToolkit().beep();
				}
			}
		};

		maxReturnCountTextField = new JTextField(insertNumbersDocument, "", 20);
		textFielsdRectanle.y += space;
		maxReturnCountTextField.setBounds(textFielsdRectanle);
		maxReturnCountTextField.setEditable(false);
		maxReturnCountTextField.setFont(ui.getFont());
		propertiesTabPanel.add(maxReturnCountTextField);

		maxReturnCountButton = new JButton();
		buttonsRectangle.y += space;
		maxReturnCountButton.setBounds(buttonsRectangle);
		maxReturnCountButton.setFont(ui.getFont());
		maxReturnCountButton.setIcon(ui.getIcon("fina2.amend"));
		maxReturnCountButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				maxReturnCountTextField.setEditable(!maxReturnCountTextField.isEditable());
			}
		});

		propertiesTabPanel.add(maxReturnCountButton);

		matrixPathLabel = new JLabel();
		labelsRectangle.y += space;
		matrixPathLabel.setBounds(labelsRectangle);
		matrixPathLabel.setText(ui.getString(PropertySession.MATRIX_PATH));
		matrixPathLabel.setFont(ui.getFont());
		propertiesTabPanel.add(matrixPathLabel);

		matrixPathTextField = new JTextField();
		textFielsdRectanle.y += space;
		matrixPathTextField.setBounds(textFielsdRectanle);
		matrixPathTextField.setEditable(false);
		matrixPathTextField.setFont(ui.getFont());
		propertiesTabPanel.add(matrixPathTextField);

		matrixPathButton = new JButton();
		buttonsRectangle.y += space;
		matrixPathButton.setBounds(buttonsRectangle);
		matrixPathButton.setFont(ui.getFont());
		matrixPathButton.setIcon(ui.getIcon("fina2.amend"));
		matrixPathButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				matrixPathTextField.setEditable(!matrixPathTextField.isEditable());
			}
		});

		propertiesTabPanel.add(matrixPathButton);

		timeOutLabel = new JLabel();
		labelsRectangle.y += space;
		timeOutLabel.setBounds(labelsRectangle);
		timeOutLabel.setText(ui.getString(PropertySession.PROCESS_TIMEOUT));
		timeOutLabel.setFont(ui.getFont());
		propertiesTabPanel.add(timeOutLabel);

		timeOutField = new JTextField(insertNumbersDocumenfortimeout, "", 8);
		textFielsdRectanle.y += space;
		timeOutField.setBounds(textFielsdRectanle);
		timeOutField.setEditable(false);
		timeOutField.setFont(ui.getFont());
		propertiesTabPanel.add(timeOutField);

		timeOutButton = new JButton();
		buttonsRectangle.y += space;
		timeOutButton.setBounds(buttonsRectangle);
		timeOutButton.setFont(ui.getFont());
		timeOutButton.setIcon(ui.getIcon("fina2.amend"));
		timeOutButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				timeOutField.setEditable(!timeOutField.isEditable());
			}
		});

		propertiesTabPanel.add(timeOutButton);

	}

	// initial update tab components
	private void initUpdateTab() {

		updateTabPanel.setLayout(null);
		// rectangles
		Rectangle labelRectangle = new Rectangle(10, 19, 340, 14);
		Rectangle checkBoxRectangle = new Rectangle(390, 19, 44, 20);
		int space = 33;

		startUpdateLabel = new JLabel();
		startUpdateLabel.setBounds(labelRectangle);
		startUpdateLabel.setText(ui.getString("fina2.update.start"));
		startUpdateLabel.setFont(ui.getFont());
		updateTabPanel.add(startUpdateLabel);

		startUpdateCheckBox = new JCheckBox();
		startUpdateCheckBox.setBounds(checkBoxRectangle);
		startUpdateCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				enableAllUpdates();
			}
		});
		updateTabPanel.add(startUpdateCheckBox);

		updateFinaUpdateLabel = new JLabel();
		labelRectangle.y += space;
		updateFinaUpdateLabel.setBounds(labelRectangle);
		updateFinaUpdateLabel.setText(ui.getString("fina2.update.finaUpdate"));
		updateFinaUpdateLabel.setFont(ui.getFont());
		updateTabPanel.add(updateFinaUpdateLabel);

		updateFinaUpdateCheckBox = new JCheckBox();
		checkBoxRectangle.y += space;
		updateFinaUpdateCheckBox.setBounds(checkBoxRectangle);
		updateTabPanel.add(updateFinaUpdateCheckBox);

		updateFinaAddinLabel = new JLabel();
		labelRectangle.y += space;
		updateFinaAddinLabel.setBounds(labelRectangle);
		updateFinaAddinLabel.setText(ui.getString("fina2.update.addin"));
		updateFinaAddinLabel.setFont(ui.getFont());
		updateTabPanel.add(updateFinaAddinLabel);

		updateFinaAddinCheckBox = new JCheckBox();
		checkBoxRectangle.y += space;
		updateFinaAddinCheckBox.setBounds(checkBoxRectangle);
		updateTabPanel.add(updateFinaAddinCheckBox);

		updateRunBatLabel = new JLabel();
		labelRectangle.y += space;
		updateRunBatLabel.setBounds(labelRectangle);
		updateRunBatLabel.setText(ui.getString("fina2.update.runBat"));
		updateRunBatLabel.setFont(ui.getFont());
		updateTabPanel.add(updateRunBatLabel);

		updateRunBatCheckBox = new JCheckBox();
		checkBoxRectangle.y += space;
		updateRunBatCheckBox.setBounds(checkBoxRectangle);
		updateTabPanel.add(updateRunBatCheckBox);

		updateResourcesLabel = new JLabel();
		labelRectangle.y += space;
		updateResourcesLabel.setBounds(labelRectangle);
		updateResourcesLabel.setText(ui.getString("fina2.update.resources"));
		updateResourcesLabel.setFont(ui.getFont());
		updateTabPanel.add(updateResourcesLabel);

		updateResourcesCheckBox = new JCheckBox();
		checkBoxRectangle.y += space;
		updateResourcesCheckBox.setBounds(checkBoxRectangle);
		updateTabPanel.add(updateResourcesCheckBox);

	}

	private void enableAllUpdates() {
		updateFinaUpdateCheckBox.setEnabled(startUpdateCheckBox.isSelected());
		updateFinaAddinCheckBox.setEnabled(startUpdateCheckBox.isSelected());
		updateRunBatCheckBox.setEnabled(startUpdateCheckBox.isSelected());
		updateResourcesCheckBox.setEnabled(startUpdateCheckBox.isSelected());
	}

	// initial Main Buttons("Save and Close Buttons").
	private void initMainButtons() {
		saveButton = new JButton(ui.getIcon("fina2.save"));
		saveButton.setText(ui.getString("fina2.returns.save"));
		saveButton.setFont(ui.getFont());
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveButtonActionPerformed(e);
			}
		});
		saveButton.setBounds(new Rectangle(240, 510, 100, 23));
		container.add(saveButton);

		closeButton = new JButton(ui.getIcon("fina2.close"));
		closeButton.setText(ui.getString("fina2.close"));
		closeButton.setFont(ui.getFont());
		closeButton.setBounds(new Rectangle(350, 510, 100, 23));
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				closeDialog();
			}
		});
		container.add(closeButton);
	}

	public void passNeverExpCheckBox1ItemStateChanged(ItemEvent e) {
		passValPeriodSpinner.setEnabled(!passNeverExpCheckBox.isSelected());
	}

	public void blockAccountCheckBoxItemStateChanged(ItemEvent e) {
		accountInactivityPeriodSpinner.setEnabled(blockAccountCheckBox.isSelected());
	}

	public void showCalendar(JTextField textField) {
		if (fcalendar != null && FinaCalendar.ACTIVE) {
			fcalendar.exit();
			return;
		}
		fcalendar = new FinaCalendar(textField);

		// previously selected date
		java.util.Date selectedDate = fcalendar.parseDate(textField.getText());
		fcalendar.setSelectedDate(selectedDate);
		if (!FinaCalendar.ACTIVE) {
			fcalendar.start(textField);
			fcalendar.getScreen().setModal(true);
		}
	}

	public void saveButtonActionPerformed(ActionEvent actionEvent) {

		if (timeOutField.getText().trim().length() == 0 || Integer.parseInt(timeOutField.getText()) < PropertySessionBean.START_XML_PROCESS_SERVICE_TIMEOUT) {
			mainTab.setSelectedIndex(1);
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.process.invalidnumber"));
			return;

		}
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/system/PropertySession");
			PropertySessionHome home = (PropertySessionHome) PortableRemoteObject.narrow(ref, PropertySessionHome.class);

			PropertySession session = home.create();
			Map<String, String> props = new TreeMap<String, String>();

			props.put(PropertySession.ALLOWED_LOGIN_ATTEMPT_NUMBER, failedLoginAttemptNumSpinner.getValue().toString());

			props.put(PropertySession.PASSWORD_VALIDITY_PERIOD, passNeverExpCheckBox.isSelected() ? "-1" : passValPeriodSpinner.getValue().toString());

			props.put(PropertySession.ALLOWED_ACCOUNT_INACTIVITY_PERIOD, !blockAccountCheckBox.isSelected() ? "-1" : accountInactivityPeriodSpinner.getValue().toString());

			props.put(PropertySession.OLD_STORED_PASSWORDS_NUMBER, numOfOldPassSpinner.getValue().toString());

			props.put(PropertySession.MINIMAL_PASSWORD_LENGTH, passMinLengthSpinner.getValue().toString());

			props.put(PropertySession.PASSWORD_WITH_NUMS_AND_CHARS, passWithNumCharCheckBox.isSelected() ? "1" : "0");

			props.put(PropertySession.FOLDER_LOCATION, xmlFolderLocationTextField.getText().trim());

			props.put(PropertySession.NAME_PATTERN, mfbXlsNamePatternTextField.getText().trim());

			props.put(PropertySession.PROTECTION_PASSWORD, sheetProtectionPasswordTextField.getText().trim());

			props.put(PropertySession.UPLOADED_FILE_UNIQUE, mfbUploadedFileUniqueTextField.getText().trim());

			props.put(PropertySession.UPDATE_GUI_FILE_LOCATION, updateGuiFileLocationTextField.getText());

			props.put(PropertySession.CONVERTED_XMLS, convertedXmlsTextField.getText());
			props.put(PropertySession.MAX_RETURNS_SIZE, maxReturnCountTextField.getText().trim());
			props.put(PropertySession.MATRIX_PATH, matrixPathTextField.getText());

			// save process timeout
			props.put(PropertySession.PROCESS_TIMEOUT, timeOutField.getText());

			// save updates properties
			if (startUpdateCheckBox.isSelected()) {
				props.put(PropertySession.UPDATE_START, "TRUE");
			} else {
				props.put(PropertySession.UPDATE_START, "FALSE");
			}
			props.put(PropertySession.UPDATE_FINA_UPDATE, updateFinaUpdateCheckBox.isSelected() ? "1" : "-1");
			props.put(PropertySession.UPDATE_ADDIN, updateFinaAddinCheckBox.isSelected() ? "1" : "-1");
			props.put(PropertySession.UPDATE_RUN_BAT, updateRunBatCheckBox.isSelected() ? "1" : "-1");
			props.put(PropertySession.UPDATE_RESOURCES, updateResourcesCheckBox.isSelected() ? "1" : "-1");

			String ldapUrlIp = ldapUrlIpTextField.getText().trim();
			String ldapUrlPort = ldapIpJTextField.getText().trim();
			String organizationUnit = organizationalUnitTextField.getText().trim();
			String domainComponent = domainComponentTestField.getText().trim();

			props.put(PropertySession.LDAP_URL_IP, ldapUrlIp);
			props.put(PropertySession.LDAP_URL_PORT, ldapUrlPort);
			props.put(PropertySession.LDAP_ORGANIZATIONAL_UNIT, organizationUnit);
			props.put(PropertySession.LDAP_DOMAIN_COMPONENT, domainComponent);

			if (isSelectedFinaAutorization()) {
				props.put(PropertySession.FINA_CURRENT_AUTHENTICATION, "FINA");
			} else {
				props.put(PropertySession.FINA_CURRENT_AUTHENTICATION, "LDAP");
			}

			// Save Mail Properties
			try {
				props.putAll(mailTabPanel.saveData());
			} catch (Exception ex) {
				ui.showMessageBox(null, ui.getString("fina2.security.datetime"));
				mainTab.setSelectedComponent(mailTabPanel);
				return;
			}

			// session.setSystemProperties(props);
			User user = (User) main.getUserHandle().getEJBObject();
			Properties bundleProps = ui.getMessages();
			Map<String, String> bundlePropsMap = new HashMap<String, String>();
			Iterator bundlePropsKeys = bundleProps.keySet().iterator();
			while (bundlePropsKeys.hasNext()) {
				Object bundlePropsKey = bundlePropsKeys.next();
				if (bundlePropsKey != null) {
					bundlePropsMap.put(bundlePropsKey.toString(), bundleProps.getProperty(bundlePropsKey.toString()));
				}
			}
			session.setSystemProperties(props, bundlePropsMap, user.getLogin());
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
		dispose();
	}

	public class LDAPAuthorizatioDialog extends JDialog {

		private Container container;

		private JTextField userNameTextFileld;
		private JPasswordField passwordFiled;

		private JButton loginButton;
		private JButton cancelButton;

		private JLabel authorizationStatusLabel;

		public LDAPAuthorizatioDialog(java.awt.Frame parent) {
			super(parent, true);
			this.setSize(500, 300);

			initComponents();

			this.setLocationRelativeTo(parent);
		}

		private void closeDialog() {
			authorizationStatusLabel.setVisible(false);
			getLDAPAuthorizatioDialog().pack();
			this.dispose();
			this.setVisible(false);
		}

		public void setVisible(boolean b, boolean isChange) {
			if (!isChange) {
				if (!this.isVisible()) {
					if (getSettingsDialog().isVisible()) {
						super.setVisible(b);
					}
				}
			}
		}

		private LDAPAuthorizatioDialog getLDAPAuthorizatioDialog() {
			return this;
		}

		private void initComponents() {
			this.setTitle("LDAP Authorization Dialog");
			container = this.getContentPane();
			container.setLayout(new GridBagLayout());

			GridBagConstraints gbc = new GridBagConstraints();

			gbc.anchor = GridBagConstraints.WEST;
			gbc.insets = new Insets(5, 10, 5, 5);

			// UserName Field
			gbc.gridwidth = GridBagConstraints.RELATIVE;
			JLabel userNameLabel = new JLabel(ui.getString("fina2.userNameLabel"));
			userNameLabel.setFont(ui.getFont());
			container.add(userNameLabel, gbc);
			gbc.gridwidth = GridBagConstraints.REMAINDER;

			userNameTextFileld = new JTextField(16);
			userNameTextFileld.setFont(ui.getFont());
			container.add(userNameTextFileld, gbc);

			// Password Field
			gbc.gridwidth = GridBagConstraints.RELATIVE;
			JLabel passwordLabel = new JLabel(ui.getString("fina2.login.password"));
			passwordLabel.setFont(ui.getFont());
			container.add(passwordLabel, gbc);

			gbc.gridwidth = GridBagConstraints.REMAINDER;
			passwordFiled = new JPasswordField(16);
			passwordFiled.setFont(ui.getFont());
			container.add(passwordFiled, gbc);

			// authorizationStatusLabel
			gbc.gridwidth = GridBagConstraints.RELATIVE;

			authorizationStatusLabel = new JLabel(ui.getString("fina2.login.invalidUser"));
			authorizationStatusLabel.setFont(ui.getFont());
			authorizationStatusLabel.setVisible(false);
			authorizationStatusLabel.setForeground(Color.RED);

			gbc.gridwidth = GridBagConstraints.REMAINDER;
			container.add(new JPanel().add(authorizationStatusLabel), gbc);

			JPanel buttonsPanel = new JPanel();

			// Login Button
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.insets = new Insets(10, 0, 5, 0);

			loginButton = new JButton(ui.getIcon("fina2.ok"));
			loginButton.setFont(ui.getFont());
			loginButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					authorizationTypeComboBox.setSelectedItem("FINA");
					authorizationStatusLabel.setVisible(true);
					getLDAPAuthorizatioDialog().pack();
				}
			});
			loginButton.setText(ui.getString("fina2.ok"));
			buttonsPanel.add(loginButton);

			// Cancel Button
			cancelButton = new JButton(ui.getIcon("fina2.cancel"));
			cancelButton.setText(ui.getString("fina2.cancel"));
			cancelButton.setFont(ui.getFont());
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					closeDialog();
				}
			});
			buttonsPanel.add(cancelButton);

			container.add(buttonsPanel, gbc);

			this.pack();
		}
	}

	private Container container;

	private JTabbedPane mainTab;

	// Panels
	private JPanel securityTabPanel;
	private JPanel propertiesTabPanel;
	private JPanel updateTabPanel;

	private JPanel securityPanel;
	private JPanel ldapPropertiesPanel;

	// Buttons
	private JButton saveButton;
	private JButton closeButton;

	// Security Components
	private JLabel allowedLoginAttemptNumberLabel;
	private JSpinner failedLoginAttemptNumSpinner;

	private JLabel passwordNeverExpiresLabel;
	private JCheckBox passNeverExpCheckBox;

	private JLabel passwordValidityPeriodLabel;
	private JSpinner passValPeriodSpinner;

	private JLabel allowedAccountInactivityPerioedLabel;
	private JSpinner accountInactivityPeriodSpinner;

	private JLabel blockAccountAfterDefinedPeriodLabel;
	private JCheckBox blockAccountCheckBox;

	private JLabel numberOfStoredOldPasswordsLabel;
	private JSpinner numOfOldPassSpinner;

	private JLabel passwordMinimalLenLabel;
	private JSpinner passMinLengthSpinner;

	private JLabel passwordWithNumsCharsLabel;
	private JCheckBox passWithNumCharCheckBox;

	private JLabel authorizationTypeLabel;
	private JComboBox authorizationTypeComboBox;

	// LDAP Properties components
	private JLabel ldapURLIpLabel;
	private JTextField ldapUrlIpTextField;

	private JLabel ldapPortLabel;
	private JTextField ldapIpJTextField;

	private JLabel organizationalUnitLabel;
	private JTextField organizationalUnitTextField;

	private JLabel domainComponentLabel;
	private JTextField domainComponentTestField;

	// Properties Tab Components
	private JLabel xmlFolderLocationLabel;
	private JTextField xmlFolderLocationTextField;
	private JButton xmlFolderLocationButton;

	private JLabel mfbXlsNamePatternLabel;
	private JTextField mfbXlsNamePatternTextField;
	private JButton mfbXlsNamePatternButton;

	private JLabel sheetProtectionPasswordLabel;
	private JTextField sheetProtectionPasswordTextField;
	private JButton sheetProtectionPasswordButton;

	private JLabel mfbUploadedFileUniqueLabel;
	private JTextField mfbUploadedFileUniqueTextField;
	private JButton mfbUploadedFileUniqueButton;

	private JLabel updateGuiFileLocationLabel;
	private JTextField updateGuiFileLocationTextField;
	private JButton updateGuiFileLocationButton;

	private JLabel convertedXmlsLabel;
	private JTextField convertedXmlsTextField;
	private JButton convertedXmlsButton;

	private JLabel maxReturnCountLabel;
	private JTextField maxReturnCountTextField;
	private JButton maxReturnCountButton;

	private JLabel matrixPathLabel;
	private JTextField matrixPathTextField;
	private JButton matrixPathButton;

	// Update tab Components
	private JLabel startUpdateLabel;
	private JCheckBox startUpdateCheckBox;

	private JLabel updateFinaUpdateLabel;
	private JCheckBox updateFinaUpdateCheckBox;

	private JLabel updateFinaAddinLabel;
	private JCheckBox updateFinaAddinCheckBox;

	private JLabel updateRunBatLabel;
	private JCheckBox updateRunBatCheckBox;

	private JLabel updateResourcesLabel;
	private JCheckBox updateResourcesCheckBox;
	// Mail tab Components
	private MailPropertiesTab mailTabPanel;

	// timeout
	private JLabel timeOutLabel;
	private JTextField timeOutField;
	private JButton timeOutButton;

}
