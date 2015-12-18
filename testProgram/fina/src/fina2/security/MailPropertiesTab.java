package fina2.security;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import fina2.calendar.FinaCalendar;
import fina2.system.PropertySession;

@SuppressWarnings("serial")
public class MailPropertiesTab extends JPanel {
	// Mail Connection Types
	public static final String MAIL_POP3_SMTP = "POP3/SMTP";
	public static final String MAIL_IMAP_SMTP = "IMAP/SMTP";
	public static final String MAIL_OWA = "OWA";
	public static final String MAIL_EWS = "EWS";
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private static Border mailPropertiesComponentsBorder = BorderFactory.createLineBorder(new Color(171, 173, 179));
	private JButton saveButton;
	private FinaCalendar fcalendar;
	private fina2.Main main = fina2.Main.main;
	private final String lastReadDateFormat = "dd/MM/yyyy HH:mm:ss";
	private final DateFormat dateFormat = new SimpleDateFormat(lastReadDateFormat);

	public MailPropertiesTab(JButton saveButton) {
		this.saveButton = saveButton;
	}

	// initial Mail Properties Tab
	public void initMailPropertiesTab() {

		setLayout(null);
		Rectangle panlesRectangle = new Rectangle(5, 135, 440, 75);
		int panelSpaces = 102;
		int space = 22;

		JPanel homeParametersPanel = new JPanel();
		homeParametersPanel.setLayout(null);
		homeParametersPanel.setBounds(new Rectangle(5, 5, 450, 230));

		Rectangle homeLablesRectangle = new Rectangle(10, 0, 200, 19);
		Rectangle homeComponentsRectangle = new Rectangle(220, 0, 217, 19);

		maiUserlLabel = new JLabel();
		maiUserlLabel.setText("Mail User");
		maiUserlLabel.setFont(ui.getFont());
		maiUserlLabel.setBounds(homeLablesRectangle);
		homeParametersPanel.add(maiUserlLabel);

		mailUserTextField = new JTextField();
		mailPropertiesComponentsBorder = mailUserTextField.getBorder();
		mailUserTextField.setBorder(mailPropertiesComponentsBorder);
		mailUserTextField.setFont(ui.getFont());
		mailUserTextField.setBounds(homeComponentsRectangle);
		homeParametersPanel.add(mailUserTextField);

		mailAddresslLabel = new JLabel();
		mailAddresslLabel.setText("Mail Address");
		mailAddresslLabel.setFont(ui.getFont());
		homeLablesRectangle.y += space;
		mailAddresslLabel.setBounds(homeLablesRectangle);
		homeParametersPanel.add(mailAddresslLabel);

		mailAddressTextField = new JTextField();
		mailAddressTextField.setBorder(mailPropertiesComponentsBorder);
		mailAddressTextField.setFont(ui.getFont());
		homeComponentsRectangle.y += space;
		mailAddressTextField.setBounds(homeComponentsRectangle);
		mailAddressTextField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				String mail = mailAddressTextField.getText().trim();
				if (!mail.equals("") && mailConnectionTypeComboBox.getSelectedItem().equals(MAIL_POP3_SMTP)) {
					if (ui.isValidEmailAddress(mail)) {
						saveButton.setEnabled(true);
					} else {
						saveButton.setEnabled(false);
					}
				} else {
					saveButton.setEnabled(true);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		mailAddressTextField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				String mail = mailAddressTextField.getText().trim();
				if (!mail.equals("") && mailConnectionTypeComboBox.getSelectedItem().equals(MAIL_POP3_SMTP)) {
					if (ui.isValidEmailAddress(mail)) {
						saveButton.setEnabled(true);
						mailAddressTextField.setBorder(mailPropertiesComponentsBorder);
					} else {
						saveButton.setEnabled(false);
						mailAddressTextField.setBorder(BorderFactory.createLineBorder(Color.red));
						mailAddressTextField.setFocusable(true);
						// mainTab.setSelectedIndex(mainTab.getTabCount() - 1);
					}
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				saveButton.setEnabled(true);
				mailUserTextField.setBorder(mailPropertiesComponentsBorder);

			}
		});
		homeParametersPanel.add(mailAddressTextField);

		mailPasswordLabel = new JLabel();
		mailPasswordLabel.setText(ui.getString("fina2.settings.mail.password"));
		mailPasswordLabel.setFont(ui.getFont());
		homeLablesRectangle.y += space;
		mailPasswordLabel.setBounds(homeLablesRectangle);
		homeParametersPanel.add(mailPasswordLabel);

		mailPasswordField = new JPasswordField();
		mailPasswordField.setBorder(mailPropertiesComponentsBorder);
		mailPasswordField.setFont(ui.getFont());
		homeComponentsRectangle.y += space;
		mailPasswordField.setBounds(homeComponentsRectangle);
		homeParametersPanel.add(mailPasswordField);

		mailCheckIntervalLabel = new JLabel();
		mailCheckIntervalLabel.setFont(ui.getFont());
		mailCheckIntervalLabel.setText(ui.getString("fina2.settings.mail.checkInterval"));
		homeLablesRectangle.y += space;
		mailCheckIntervalLabel.setBounds(homeLablesRectangle);
		homeParametersPanel.add(mailCheckIntervalLabel);

		SpinnerNumberModel model = new SpinnerNumberModel(1000, 999, 999999999, 1);
		mailCheckIntervalSpinner = new JSpinner(model);
		mailCheckIntervalSpinner.setBorder(mailPropertiesComponentsBorder);
		JSpinner.NumberEditor de = new JSpinner.NumberEditor(mailCheckIntervalSpinner);
		mailCheckIntervalSpinner.setEditor(de);
		homeComponentsRectangle.width -= 120;
		homeComponentsRectangle.y += space;
		mailCheckIntervalSpinner.setBounds(homeComponentsRectangle);
		homeParametersPanel.add(mailCheckIntervalSpinner);

		mailLastReadDateLabel = new JLabel();
		mailLastReadDateLabel.setFont(ui.getFont());
		mailLastReadDateLabel.setText(ui.getString("fina2.settings.mail.lastReadDate"));
		homeLablesRectangle.y += space;
		mailLastReadDateLabel.setBounds(homeLablesRectangle);
		homeParametersPanel.add(mailLastReadDateLabel);

		mailLastReadDateTextField = new JTextField();
		mailLastReadDateTextField.setEditable(false);
		mailLastReadDateTextField.setBorder(mailPropertiesComponentsBorder);

		mailLastReadDateTextField.setHorizontalAlignment(JTextField.RIGHT);
		mailLastReadDateTextField.setFont(ui.getFont());
		homeComponentsRectangle.y += space;
		mailLastReadDateTextField.setBounds(new Rectangle(220, 88, 180, 20));
		mailLastReadDateTextField.setToolTipText(lastReadDateFormat);
		homeParametersPanel.add(mailLastReadDateTextField);

		JButton dateChangeButton = new JButton();
		dateChangeButton.setBounds(405, 88, 30, 20);

		dateChangeButton.setIcon(ui.getIcon("fina2.amend"));
		dateChangeButton.addActionListener((new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				mailLastReadDateTextField.setEditable(!mailLastReadDateTextField.isEditable());
			}
		}));
		homeParametersPanel.add(dateChangeButton);

		mailSendResponceLabel = new JLabel();
		mailSendResponceLabel.setFont(ui.getFont());
		mailSendResponceLabel.setText(ui.getString("fina2.settings.mail.sendResponceEnable"));
		homeLablesRectangle.y += space;
		mailSendResponceLabel.setBounds(homeLablesRectangle);
		homeParametersPanel.add(mailSendResponceLabel);

		mailSendResponceCheckBox = new JCheckBox();
		homeComponentsRectangle.y += space;
		homeComponentsRectangle.x -= 4;
		homeComponentsRectangle.width += 60;
		mailSendResponceCheckBox.setBounds(homeComponentsRectangle);
		homeParametersPanel.add(mailSendResponceCheckBox);

		mailResponceCCLabel = new JLabel();
		mailResponceCCLabel.setFont(ui.getFont());
		mailResponceCCLabel.setText(ui.getString("fina2.mail.responce.cc"));
		homeLablesRectangle.y += space;
		mailResponceCCLabel.setBounds(homeLablesRectangle);
		homeParametersPanel.add(mailResponceCCLabel);

		mailResponceCCTextField = new JTextField();
		mailResponceCCTextField.setBorder(mailPropertiesComponentsBorder);
		mailResponceCCTextField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				String mailResponceCC = mailResponceCCTextField.getText().trim();

				if (!mailResponceCC.equals("")) {
					for (String s : mailResponceCC.split(",")) {
						if (ui.isValidEmailAddress(s)) {
							saveButton.setEnabled(true);
							mailResponceCCTextField.setBorder(mailPropertiesComponentsBorder);
						} else {
							saveButton.setEnabled(false);
							mailResponceCCTextField.setBorder(BorderFactory.createLineBorder(Color.red));
							mailResponceCCTextField.setFocusable(true);
							// mainTab.setSelectedIndex(mainTab.getTabCount() -
							// 1);
							break;
						}
					}

				}

			}

			@Override
			public void focusGained(FocusEvent e) {
				saveButton.setEnabled(true);
				mailResponceCCTextField.setBorder(mailPropertiesComponentsBorder);
			}
		});
		mailResponceCCTextField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				String mailResponceCC = mailResponceCCTextField.getText().trim();
				if (!mailResponceCC.equals("")) {
					for (String s : mailResponceCC.split(","))
						if (ui.isValidEmailAddress(s)) {
							saveButton.setEnabled(true);
						} else {
							saveButton.setEnabled(false);
							break;
						}
				} else {
					saveButton.setEnabled(true);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		mailResponceCCTextField.setFont(ui.getFont());
		mailResponceCCTextField.setToolTipText("Example: \nemail1@domain.com,email2@domain.com");
		homeComponentsRectangle.x += 4;
		homeComponentsRectangle.width += 60;
		homeComponentsRectangle.y += space;
		mailResponceCCTextField.setBounds(homeComponentsRectangle);
		homeParametersPanel.add(mailResponceCCTextField);

		mailResponceUnknownUserLabel = new JLabel();
		mailResponceUnknownUserLabel.setFont(ui.getFont());
		mailResponceUnknownUserLabel.setText(ui.getString("fina2.mail.responce.readUnknownEmail"));
		homeLablesRectangle.y += space;
		mailResponceUnknownUserLabel.setBounds(homeLablesRectangle);
		homeParametersPanel.add(mailResponceUnknownUserLabel);

		mailResponceUnknownUserCheckBox = new JCheckBox();
		homeComponentsRectangle.x -= 4;
		homeComponentsRectangle.y += space;
		mailResponceUnknownUserCheckBox.setBounds(homeComponentsRectangle);
		homeParametersPanel.add(mailResponceUnknownUserCheckBox);

		mailConnectionTypeJLabel = new JLabel();
		mailConnectionTypeJLabel.setFont(ui.getFont());
		mailConnectionTypeJLabel.setText(ui.getString("fina2.settings.mail.connectionType"));
		homeLablesRectangle.y += space;
		mailConnectionTypeJLabel.setBounds(homeLablesRectangle);
		homeParametersPanel.add(mailConnectionTypeJLabel);

		mailConnectionTypeComboBox = new JComboBox();
		mailConnectionTypeComboBox.setFont(ui.getFont());
		homeComponentsRectangle.y += space;
		homeComponentsRectangle.x += 4;
		homeComponentsRectangle.width -= 60;
		mailConnectionTypeComboBox.setBounds(homeComponentsRectangle);

		mailConnectionTypeComboBox.addItem(MAIL_POP3_SMTP);
		mailConnectionTypeComboBox.addItem(MAIL_IMAP_SMTP);
		mailConnectionTypeComboBox.addItem(MAIL_OWA);
		mailConnectionTypeComboBox.addItem(MAIL_EWS);
		mailConnectionTypeComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getItem().equals(MAIL_POP3_SMTP)) {
					enableExchangeSpecificatiosn(false);
					pop3ParametersPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray), "POP3"));
					pop3HostLabel.setText(ui.getString("fina2.settings.mail.pop3Host"));
					pop3PortLabel.setText(ui.getString("fina2.settings.mail.pop3Port"));
				}
				if (e.getItem().equals(MAIL_IMAP_SMTP)) {
					enableExchangeSpecificatiosn(false);
					pop3ParametersPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray), "IMAP"));
					pop3HostLabel.setText(ui.getString("fina2.settings.mail.imapHost"));
					pop3PortLabel.setText(ui.getString("fina2.settings.mail.imapPort"));
				}
				if (e.getItem().equals(MAIL_OWA)) {
					enableExchangeSpecificatiosn(true);
				}
				if (e.getItem().equals(MAIL_EWS)) {
					enableExchangeSpecificatiosn(true);
				}

			}
		});
		homeParametersPanel.add(mailConnectionTypeComboBox);

		add(homeParametersPanel);

		pop3ParametersPanel = new JPanel();
		pop3ParametersPanel.setLayout(null);
		// pop3ParametersPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray),
		// "POP3"));
		panlesRectangle.y += panelSpaces;
		pop3ParametersPanel.setBounds(panlesRectangle);

		Rectangle pop3PropertiesLabelsRectangleL = new Rectangle(10, 0, 100, 19);
		Rectangle pop3PropertiesComponentsRectangleL = new Rectangle(105, 0, 150, 19);

		Rectangle pop3PropertiesLabelsRectangleR = new Rectangle(270, 0, 100, 19);
		Rectangle pop3PropertiesComponentsRectangleR = new Rectangle(360, 0, 50, 19);

		pop3HostLabel = new JLabel();
		pop3HostLabel.setFont(ui.getFont());
		pop3HostLabel.setText(ui.getString("fina2.settings.mail.pop3Host"));
		pop3PropertiesLabelsRectangleL.y += space;
		pop3HostLabel.setBounds(pop3PropertiesLabelsRectangleL);
		pop3ParametersPanel.add(pop3HostLabel);

		pop3HostTextField = new JTextField();
		pop3HostTextField.setBorder(mailPropertiesComponentsBorder);
		pop3HostTextField.setFont(ui.getFont());
		pop3PropertiesComponentsRectangleL.y += space;
		pop3HostTextField.setBounds(pop3PropertiesComponentsRectangleL);
		pop3ParametersPanel.add(pop3HostTextField);

		pop3PortLabel = new JLabel();
		pop3PortLabel.setFont(ui.getFont());
		pop3PortLabel.setText(ui.getString("fina2.settings.mail.pop3Port"));
		pop3PropertiesLabelsRectangleR.y += space;
		pop3PortLabel.setBounds(pop3PropertiesLabelsRectangleR);
		pop3ParametersPanel.add(pop3PortLabel);

		// insert numbers
		PlainDocument insertNumbersDocumentPOP3 = new PlainDocument() {
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
		pop3PortTextField = new JTextField(insertNumbersDocumentPOP3, "", 20);
		pop3PortTextField.setBorder(mailPropertiesComponentsBorder);
		pop3PortTextField.setFont(ui.getFont());
		pop3PropertiesComponentsRectangleR.y += space;
		pop3PortTextField.setBounds(pop3PropertiesComponentsRectangleR);
		pop3ParametersPanel.add(pop3PortTextField);

		pop3ConnectionTimeoutLabel = new JLabel();
		pop3ConnectionTimeoutLabel.setFont(ui.getFont());
		pop3ConnectionTimeoutLabel.setText(ui.getString("fina2.settings.mail.pop3ConnectionTimeout"));
		pop3PropertiesLabelsRectangleL.y += space;
		pop3ConnectionTimeoutLabel.setBounds(pop3PropertiesLabelsRectangleL);
		pop3ParametersPanel.add(pop3ConnectionTimeoutLabel);

		pop3ConnectionTimeoutSpinner = new JSpinner();
		pop3ConnectionTimeoutSpinner.setBorder(mailPropertiesComponentsBorder);
		pop3ConnectionTimeoutSpinner.setFont(ui.getFont());
		pop3PropertiesComponentsRectangleL.y += space;
		pop3PropertiesComponentsRectangleL.width -= 80;
		pop3ConnectionTimeoutSpinner.setBounds(pop3PropertiesComponentsRectangleL);
		pop3ParametersPanel.add(pop3ConnectionTimeoutSpinner);

		pop3SSLEnableLabel = new JLabel();
		pop3SSLEnableLabel.setFont(ui.getFont());
		pop3SSLEnableLabel.setText(ui.getString("fina2.settings.mail.pop3SSLEnable"));
		pop3PropertiesLabelsRectangleR.y += space;
		pop3SSLEnableLabel.setBounds(pop3PropertiesLabelsRectangleR);
		pop3ParametersPanel.add(pop3SSLEnableLabel);

		pop3SSLEnableCheckBox = new JCheckBox();
		pop3PropertiesComponentsRectangleR.y += space;
		pop3PropertiesComponentsRectangleR.x -= 4;
		pop3SSLEnableCheckBox.setBounds(pop3PropertiesComponentsRectangleR);
		pop3ParametersPanel.add(pop3SSLEnableCheckBox);

		add(pop3ParametersPanel);

		JPanel smtpParametersPanel = new JPanel();
		smtpParametersPanel.setLayout(null);
		smtpParametersPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray), "SMTP"));
		panlesRectangle.y += panelSpaces - 25;
		smtpParametersPanel.setBounds(panlesRectangle);

		Rectangle smtpPropertiesLabelsRectangleL = new Rectangle(10, 0, 100, 19);
		Rectangle smtpPropertiesComponentsRectangleL = new Rectangle(105, 0, 150, 19);

		Rectangle smtpPropertiesLabelsRectangleR = new Rectangle(270, 0, 100, 19);
		Rectangle smtpPropertiesComponentsRectangleR = new Rectangle(360, 0, 50, 19);

		smtpHostLabel = new JLabel();
		smtpHostLabel.setFont(ui.getFont());
		smtpHostLabel.setText(ui.getString("fina2.settings.mail.smtpHost"));
		smtpPropertiesLabelsRectangleL.y += space;
		smtpHostLabel.setBounds(smtpPropertiesLabelsRectangleL);
		smtpParametersPanel.add(smtpHostLabel);

		smtpHostTextField = new JTextField();
		smtpHostTextField.setBorder(mailPropertiesComponentsBorder);
		smtpHostTextField.setFont(ui.getFont());
		smtpPropertiesComponentsRectangleL.y += space;
		smtpHostTextField.setBounds(smtpPropertiesComponentsRectangleL);
		smtpParametersPanel.add(smtpHostTextField);

		smtpPortLabel = new JLabel();
		smtpPortLabel.setFont(ui.getFont());
		smtpPortLabel.setText(ui.getString("fina2.settings.mail.smtpPort"));
		smtpPropertiesLabelsRectangleR.y += space;
		smtpPortLabel.setBounds(smtpPropertiesLabelsRectangleR);
		smtpParametersPanel.add(smtpPortLabel);

		// insert numbers
		PlainDocument insertNumbersDocumentSMTP = new PlainDocument() {
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
		smtpPortTextField = new JTextField(insertNumbersDocumentSMTP, "", 20);
		smtpPortTextField.setBorder(mailPropertiesComponentsBorder);
		smtpPortTextField.setFont(ui.getFont());
		smtpPropertiesComponentsRectangleR.y += space;
		smtpPortTextField.setBounds(smtpPropertiesComponentsRectangleR);
		smtpParametersPanel.add(smtpPortTextField);

		smtpTSLEnableLabel = new JLabel();
		smtpTSLEnableLabel.setFont(ui.getFont());
		smtpTSLEnableLabel.setText(ui.getString("fina2.settings.mail.smtpTSLEnable"));
		smtpPropertiesLabelsRectangleL.y += space;
		smtpTSLEnableLabel.setBounds(smtpPropertiesLabelsRectangleL);
		smtpParametersPanel.add(smtpTSLEnableLabel);

		smtpTSLEnableCheckBox = new JCheckBox();
		smtpPropertiesComponentsRectangleL.y += space;
		smtpPropertiesComponentsRectangleL.x -= 4;
		smtpTSLEnableCheckBox.setBounds(smtpPropertiesComponentsRectangleL);
		smtpParametersPanel.add(smtpTSLEnableCheckBox);

		smtpSSLEnableLabel = new JLabel();
		smtpSSLEnableLabel.setFont(ui.getFont());
		smtpSSLEnableLabel.setText(ui.getString("fina2.settings.mail.smtpSSLEnable"));
		smtpPropertiesLabelsRectangleR.y += space;
		smtpSSLEnableLabel.setBounds(smtpPropertiesLabelsRectangleR);
		smtpParametersPanel.add(smtpSSLEnableLabel);

		smtpSSLEnableCheckBox = new JCheckBox();
		smtpPropertiesComponentsRectangleR.y += space;
		smtpPropertiesComponentsRectangleR.x -= 4;
		smtpSSLEnableCheckBox.setBounds(smtpPropertiesComponentsRectangleR);
		smtpParametersPanel.add(smtpSSLEnableCheckBox);

		add(smtpParametersPanel);

		JPanel exchangeSpecificationsPanle = new JPanel();
		exchangeSpecificationsPanle.setLayout(null);
		exchangeSpecificationsPanle.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray), "Exchange"));
		panlesRectangle.y += panelSpaces - 25;
		exchangeSpecificationsPanle.setBounds(panlesRectangle);
		Rectangle exchangePropertiesLabelsRectangleL = new Rectangle(10, 0, 100, 19);
		Rectangle exchangePropertiesComponentsRectangleL = new Rectangle(105, 0, 150, 19);
		Rectangle exchangePropertiesLabelsRectangleR = new Rectangle(270, 0, 100, 19);
		Rectangle exchangePropertiesComponentsRectangleR = new Rectangle(360, 0, 50, 19);

		exchangeMailBoxLabel = new JLabel();
		exchangeMailBoxLabel.setFont(ui.getFont());
		exchangeMailBoxLabel.setText(ui.getString("fina2.fina2.settings.mail.exchangeMailBox"));
		exchangePropertiesLabelsRectangleL.y += space;
		exchangeMailBoxLabel.setBounds(exchangePropertiesLabelsRectangleL);
		exchangeSpecificationsPanle.add(exchangeMailBoxLabel);

		exchangeMailBoxTextField = new JTextField();
		exchangeMailBoxTextField.setBorder(mailPropertiesComponentsBorder);
		exchangeMailBoxTextField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				String mailBox = exchangeMailBoxTextField.getText().trim();
				if (!mailBox.equals("")) {
					if (ui.isValidEmailAddress(mailBox)) {
						saveButton.setEnabled(true);
						exchangeMailBoxTextField.setBorder(mailPropertiesComponentsBorder);
					} else {
						saveButton.setEnabled(false);
						exchangeMailBoxTextField.setBorder(BorderFactory.createLineBorder(Color.red));
						exchangeMailBoxTextField.setFocusable(true);
						// mainTab.setSelectedIndex(mainTab.getTabCount() - 1);
					}
				}

			}

			@Override
			public void focusGained(FocusEvent e) {
				saveButton.setEnabled(true);
				exchangeMailBoxTextField.setBorder(mailPropertiesComponentsBorder);
			}
		});
		exchangeMailBoxTextField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				String mailBox = exchangeMailBoxTextField.getText().trim();
				if (!mailBox.equals("")) {
					if (ui.isValidEmailAddress(mailBox)) {
						saveButton.setEnabled(true);
					} else {
						saveButton.setEnabled(false);
					}
				} else {
					saveButton.setEnabled(true);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		exchangeMailBoxTextField.setFont(ui.getFont());
		exchangePropertiesComponentsRectangleL.y += space;
		exchangeMailBoxTextField.setBounds(exchangePropertiesComponentsRectangleL);
		exchangeSpecificationsPanle.add(exchangeMailBoxTextField);

		exchangeUnfilteredLabel = new JLabel();
		exchangeUnfilteredLabel.setFont(ui.getFont());
		exchangeUnfilteredLabel.setText(ui.getString("fina2.fina2.settings.mail.exchangeUnfiltered"));
		exchangePropertiesLabelsRectangleR.y += space;
		exchangeUnfilteredLabel.setBounds(exchangePropertiesLabelsRectangleR);
		exchangeSpecificationsPanle.add(exchangeUnfilteredLabel);

		exchangeUnfilteredCheckBox = new JCheckBox();
		exchangeUnfilteredCheckBox.setFont(ui.getFont());
		exchangePropertiesComponentsRectangleR.y += space;
		exchangePropertiesComponentsRectangleR.x -= 4;
		exchangeUnfilteredCheckBox.setBounds(exchangePropertiesComponentsRectangleR);
		exchangeSpecificationsPanle.add(exchangeUnfilteredCheckBox);

		exchangeMailLimitLabel = new JLabel();
		exchangeMailLimitLabel.setFont(ui.getFont());
		exchangeMailLimitLabel.setText(ui.getString("fina2.fina2.settings.mail.exchangeLimit"));
		exchangePropertiesLabelsRectangleL.y += space;
		exchangeMailLimitLabel.setBounds(exchangePropertiesLabelsRectangleL);
		exchangeSpecificationsPanle.add(exchangeMailLimitLabel);

		exchangeMailLimitTextSpinner = new JSpinner();
		exchangeMailLimitTextSpinner.setBorder(mailPropertiesComponentsBorder);
		exchangeMailLimitTextSpinner.setFont(ui.getFont());
		exchangePropertiesComponentsRectangleL.y += space;
		exchangeMailLimitTextSpinner.setBounds(exchangePropertiesComponentsRectangleL);
		exchangeSpecificationsPanle.add(exchangeMailLimitTextSpinner);

		exchangeMailDeleteLabel = new JLabel();
		exchangeMailDeleteLabel.setFont(ui.getFont());
		exchangeMailDeleteLabel.setText(ui.getString("fina2.fina2.settings.mail.exchangeMailDelete"));
		exchangePropertiesLabelsRectangleR.y += space;
		exchangeMailDeleteLabel.setBounds(exchangePropertiesLabelsRectangleR);
		exchangeSpecificationsPanle.add(exchangeMailDeleteLabel);

		exchangeMailDeleteCheckBox = new JCheckBox();
		exchangePropertiesComponentsRectangleR.y += space;
		exchangeMailDeleteCheckBox.setBounds(exchangePropertiesComponentsRectangleR);
		exchangeSpecificationsPanle.add(exchangeMailDeleteCheckBox);
		add(exchangeSpecificationsPanle);
	}

	private void enableExchangeSpecificatiosn(boolean disable) {
		exchangeMailBoxTextField.setEditable(disable);
		exchangeUnfilteredCheckBox.setEnabled(disable);
		exchangeMailLimitTextSpinner.setEnabled(disable);
		exchangeMailDeleteCheckBox.setEnabled(disable);
	}

	public void insertMailParametersData(Map<String, String> props) {
		String connectionTypeString = props.get(PropertySession.MAIL_CONNECTION_TYPE);
		if (connectionTypeString != null) {
			int ConnectionType = Integer.parseInt(connectionTypeString);
			switch (ConnectionType) {
			case 1: {
				mailConnectionTypeComboBox.setSelectedItem(MAIL_POP3_SMTP);
				enableExchangeSpecificatiosn(false);
				pop3ParametersPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray), "POP3"));

				pop3HostTextField.setText(props.get(PropertySession.MAIL_POP3_HOST));
				pop3PortTextField.setText(props.get(PropertySession.MAIL_POP3_PORT));

				String connectionTimeoutString = props.get(PropertySession.MAIL_POP3_CONNECTION_TIMEUT);
				if (connectionTimeoutString != null) {
					pop3ConnectionTimeoutSpinner.setValue(Integer.parseInt(connectionTimeoutString));
				}
				pop3SSLEnableCheckBox.setSelected(getSysPropertyBooleanValue(props, PropertySession.MAIL_POP3_SSL_ENABLE));

				break;
			}
			case 2: {
				mailConnectionTypeComboBox.setSelectedItem(MAIL_IMAP_SMTP);
				pop3ParametersPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray), "IMAP"));
				pop3HostLabel.setText(ui.getString("fina2.settings.mail.imapHost"));
				pop3PortLabel.setText(ui.getString("fina2.settings.mail.imapPort"));

				enableExchangeSpecificatiosn(false);

				pop3HostTextField.setText(props.get(PropertySession.MAIL_IMAP_HOST));
				pop3PortTextField.setText(props.get(PropertySession.MAIL_IMAP_PORT));

				String connectionTimeoutString = props.get(PropertySession.MAIL_IMAP_CONNECTION_TIMEUT);
				if (connectionTimeoutString != null) {
					pop3ConnectionTimeoutSpinner.setValue(Integer.parseInt(connectionTimeoutString));
				}
				pop3SSLEnableCheckBox.setSelected(getSysPropertyBooleanValue(props, PropertySession.MAIL_IMAP_SSL_ENABLE));

				break;
			}
			case 3: {
				mailConnectionTypeComboBox.setSelectedItem(MAIL_OWA);
				enableExchangeSpecificatiosn(true);
				break;
			}
			case 4: {
				mailConnectionTypeComboBox.setSelectedItem(MAIL_OWA);
				enableExchangeSpecificatiosn(true);
				break;
			}
			default: {
				mailConnectionTypeComboBox.setSelectedItem(MAIL_POP3_SMTP);
			}
			}

			mailUserTextField.setText(props.get(PropertySession.MAIL_USER));
			mailAddressTextField.setText(props.get(PropertySession.MAIL_ADDRESS));
			mailPasswordField.setText(props.get(PropertySession.MAIL_PASSWORD));

			String mailCheckIntervalString = props.get(PropertySession.MAIL_CHECK_INTERVAL);
			if (mailCheckIntervalString != null && (!mailCheckIntervalString.equals(""))) {
				mailCheckIntervalSpinner.setValue(Integer.parseInt(mailCheckIntervalString));
			}
			String lasReadDateString = props.get(PropertySession.LAST_READ_DATE);
			if (lasReadDateString.length() > 1) {
				long miliseconds = Long.parseLong(lasReadDateString);

				Date date = new Date(miliseconds);
				if (date != null)
					mailLastReadDateTextField.setText(dateFormat.format(date));

			} else {
				mailLastReadDateTextField.setText("");

			}
			mailSendResponceCheckBox.setSelected(getSysPropertyBooleanValue(props, PropertySession.MAIL_SEND_RESPONCE_ENABLE));
			mailResponceCCTextField.setText(props.get(PropertySession.MAIL_RESPONCE_CC));

			int responceUnknownUser = getSysPropertyValue(props, PropertySession.MAIL_RESPONCE_UNKNOWN_USER);
			if (responceUnknownUser > 0) {
				mailResponceUnknownUserCheckBox.setSelected(true);
			} else {
				mailResponceUnknownUserCheckBox.setSelected(false);
			}

			smtpHostTextField.setText(props.get(PropertySession.MAIL_SMTP_HOST));
			smtpPortTextField.setText(props.get(PropertySession.MAIL_SMTP_PORT));

			smtpTSLEnableCheckBox.setSelected(getSysPropertyBooleanValue(props, PropertySession.MAIL_SMTP_STARTTSL_ENABLE));
			smtpSSLEnableCheckBox.setSelected(getSysPropertyBooleanValue(props, PropertySession.MAIL_SMTP_SSL_ENABLE));
			exchangeMailBoxTextField.setText(props.get(PropertySession.EXCHANGE_MAILBOX));
			exchangeUnfilteredCheckBox.setSelected(getSysPropertyBooleanValue(props, PropertySession.EXJELLO_MAIL_UNFILTERED));
			String exchangeMailLimitString = props.get(PropertySession.EXJELLO_MAIL_LIMIT);
			if (exchangeMailLimitString != null) {
				exchangeMailLimitTextSpinner.setValue(Integer.parseInt(exchangeMailLimitString));
			}
			exchangeMailDeleteCheckBox.setSelected(getSysPropertyBooleanValue(props, PropertySession.EXJELLO_MAIL_DELETE));
		}
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

	private int getSysPropertyValue(Map<String, String> props, String key) throws NumberFormatException {
		int result = -1;
		String value = props.get(key);
		if (value != null) {
			result = Integer.parseInt(value);
		}
		return result;
	}

	boolean getSysPropertyBooleanValue(Map<String, String> props, String key) {
		String valString = props.get(key);
		if (valString != null) {
			return Boolean.parseBoolean(valString);
		}
		return false;
	}

	public Map<String, String> saveData() throws ParseException {
		Map<String, String> props = new HashMap<String, String>();

		// Save Mail parameters
		props.put(PropertySession.MAIL_USER, mailUserTextField.getText().trim());
		props.put(PropertySession.MAIL_ADDRESS, mailAddressTextField.getText().trim());
		props.put(PropertySession.MAIL_PASSWORD, mailPasswordField.getText().trim());

		props.put(PropertySession.MAIL_CHECK_INTERVAL, mailCheckIntervalSpinner.getValue() + "");

		String lastReadDateString = mailLastReadDateTextField.getText();

		if (lastReadDateString.trim().length() > 0) {
			Date date = null;

			date = dateFormat.parse(lastReadDateString);
			props.put(PropertySession.LAST_READ_DATE, Long.toString(date.getTime()));
		} else {
			props.put(PropertySession.LAST_READ_DATE, "");
		}
		props.put(PropertySession.MAIL_SEND_RESPONCE_ENABLE, Boolean.toString(mailSendResponceCheckBox.isSelected()));

		props.put(PropertySession.MAIL_RESPONCE_CC, mailResponceCCTextField.getText());

		if (mailResponceUnknownUserCheckBox.isSelected()) {
			props.put(PropertySession.MAIL_RESPONCE_UNKNOWN_USER, "1");
		} else {
			props.put(PropertySession.MAIL_RESPONCE_UNKNOWN_USER, "-1");
		}

		String mailConnectionTypeString = mailConnectionTypeComboBox.getSelectedItem().toString();
		if (mailConnectionTypeString.equals(MAIL_POP3_SMTP)) {
			props.put(PropertySession.MAIL_CONNECTION_TYPE, "1");
			props.put(PropertySession.MAIL_POP3_HOST, pop3HostTextField.getText().trim());
			props.put(PropertySession.MAIL_POP3_PORT, pop3PortTextField.getText().trim());
			props.put(PropertySession.MAIL_POP3_CONNECTION_TIMEUT, pop3ConnectionTimeoutSpinner.getValue().toString());
			props.put(PropertySession.MAIL_POP3_SSL_ENABLE, Boolean.toString(pop3SSLEnableCheckBox.isSelected()));
		}
		if (mailConnectionTypeString.equals(MAIL_IMAP_SMTP)) {
			props.put(PropertySession.MAIL_CONNECTION_TYPE, "2");
			props.put(PropertySession.MAIL_IMAP_HOST, pop3HostTextField.getText().trim());
			props.put(PropertySession.MAIL_IMAP_PORT, pop3PortTextField.getText().trim());
			props.put(PropertySession.MAIL_IMAP_CONNECTION_TIMEUT, pop3ConnectionTimeoutSpinner.getValue().toString());
			props.put(PropertySession.MAIL_IMAP_SSL_ENABLE, Boolean.toString(pop3SSLEnableCheckBox.isSelected()));
		}
		if (mailConnectionTypeString.equals(MAIL_OWA)) {
			props.put(PropertySession.MAIL_CONNECTION_TYPE, "3");
		}
		if (mailConnectionTypeString.equals(MAIL_EWS)) {
			props.put(PropertySession.MAIL_CONNECTION_TYPE, "4");
		}

		props.put(PropertySession.MAIL_SMTP_CONNECTION_TIMEUT, pop3ConnectionTimeoutSpinner.getValue().toString());
		props.put(PropertySession.MAIL_SMTP_HOST, smtpHostTextField.getText().trim());
		props.put(PropertySession.MAIL_SMTP_PORT, smtpPortTextField.getText().trim());
		props.put(PropertySession.MAIL_SMTP_STARTTSL_ENABLE, Boolean.toString(smtpTSLEnableCheckBox.isSelected()));
		props.put(PropertySession.MAIL_SMTP_SSL_ENABLE, Boolean.toString(smtpSSLEnableCheckBox.isSelected()));
		props.put(PropertySession.EXCHANGE_MAILBOX, exchangeMailBoxTextField.getText().trim());
		props.put(PropertySession.EXJELLO_MAIL_UNFILTERED, Boolean.toString(exchangeUnfilteredCheckBox.isSelected()));
		props.put(PropertySession.EXJELLO_MAIL_LIMIT, exchangeMailLimitTextSpinner.getValue().toString());
		props.put(PropertySession.EXJELLO_MAIL_DELETE, Boolean.toString(exchangeMailDeleteCheckBox.isSelected()));

		return props;
	}

	private JPanel pop3ParametersPanel;

	// mail home parameters
	private JLabel maiUserlLabel;
	private JTextField mailUserTextField;

	// Add
	private JLabel mailAddresslLabel;
	private JTextField mailAddressTextField;

	private JLabel mailPasswordLabel;
	private JPasswordField mailPasswordField;

	private JLabel mailCheckIntervalLabel;
	private JSpinner mailCheckIntervalSpinner;

	private JLabel mailLastReadDateLabel;
	private JTextField mailLastReadDateTextField;

	private JLabel mailSendResponceLabel;
	private JCheckBox mailSendResponceCheckBox;

	private JLabel mailConnectionTypeJLabel;
	private JComboBox mailConnectionTypeComboBox;

	private JLabel mailResponceUnknownUserLabel;
	private JCheckBox mailResponceUnknownUserCheckBox;

	private JLabel mailResponceCCLabel;
	private JTextField mailResponceCCTextField;

	// pop3 parameters
	private JLabel pop3HostLabel;
	private JTextField pop3HostTextField;

	private JLabel pop3PortLabel;
	private JTextField pop3PortTextField;

	private JLabel pop3ConnectionTimeoutLabel;
	private JSpinner pop3ConnectionTimeoutSpinner;

	private JLabel pop3SSLEnableLabel;
	private JCheckBox pop3SSLEnableCheckBox;

	// SMTP Parameters
	private JLabel smtpHostLabel;
	private JTextField smtpHostTextField;

	private JLabel smtpPortLabel;
	private JTextField smtpPortTextField;

	private JLabel smtpSSLEnableLabel;
	private JCheckBox smtpSSLEnableCheckBox;

	private JLabel smtpTSLEnableLabel;
	private JCheckBox smtpTSLEnableCheckBox;

	// Exchange specifications
	private JLabel exchangeMailBoxLabel;
	private JTextField exchangeMailBoxTextField;

	private JLabel exchangeUnfilteredLabel;
	private JCheckBox exchangeUnfilteredCheckBox;

	private JLabel exchangeMailLimitLabel;
	private JSpinner exchangeMailLimitTextSpinner;

	private JLabel exchangeMailDeleteLabel;
	private JCheckBox exchangeMailDeleteCheckBox;
}
