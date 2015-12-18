package fina2.about;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import fina2.Main;
import fina2.system.PropertySessionBean;
import fina2.ui.UIManager;
import fina2.ui.sheet.SpreadsheetsManager;

@SuppressWarnings("serial")
public class AboutVersionsFrame extends JInternalFrame {

	private JButton closeButton;
	private JLabel logoLabel;
	private JLabel javaLabel;
	private UIManager ui = Main.main.ui;
	private JTextPane aboutFinaText;
	private JTable systemTable;
	private Color bg;
	private Color color;
	TableModel systemTableModel;
	private Logger log = Logger.getLogger(getClass());

	public AboutVersionsFrame() {
		super();
		ui.loadIcon("fina2.AboutVersionFina", "fina.png");
		ui.loadIcon("fina2.AboutVersionJava", "java.png");
		initComponents();
	}

	public void initComponents() {
		getContentPane().setLayout(null);
		setBounds(0, 0, 420, 305);
		Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(d.width / 2 - getWidth() / 2, d.height / 2 - getHeight() / 2);
		setTitle(ui.getString("fina2.AboutVersionFrame.title"));

		closeButton = new JButton(ui.getString("fina2.close"), ui.getIcon("fina2.close"));
		closeButton.setFont(ui.getFont());
		closeButton.setBounds(310, 237, 90, 25);
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeButtonActionPerformed(e);
			}
		});
		add(closeButton);

		bg = this.getBackground();
		color = new Color(0, 0, 128);

		logoLabel = new JLabel(ui.getIcon("fina2.AboutVersionFina"));
		logoLabel.setBounds(12, 22, 150, 80);
		add(logoLabel);

		javaLabel = new JLabel(ui.getIcon("fina2.AboutVersionJava"));
		javaLabel.setBounds(42, 98, 90, 150);
		add(javaLabel);

		String version = ui.getSysPropertiesValue("jboss.version");

		String spreadsheetVersion = null;
		boolean load = true;
		load = Boolean.parseBoolean(ui.getConfigValue("arch.data.model.isValid") + "");
		if (load) {
			try {
				spreadsheetVersion = SpreadsheetsManager.getInstance().createSpreadsheet().getVersion();
			} catch (Exception ex) {
				ex.printStackTrace();
				log.error(ex.getMessage(), ex);
			}
		}
		systemTableModel = new DefaultTableModel(new String[][] { { ui.getString("fina2.gui.version"),// ui.getSysPropertiesValue("fina2.gui.version")
				UIManager.guiVersion }, { ui.getString("fina2.jboss.server.version"), version.substring(0, version.indexOf("(")) }, { ui.getString("fina2.java.version"), System.getProperty("java.version") }, { ui.getString("fina2.server.version"), PropertySessionBean.serverVersion }, { ui.getString("fina2.openoffice.version"), spreadsheetVersion }, { ui.getSysPropertiesValue("database.name"), ui.getSysPropertiesValue("database.version") }, { ui.getString("fina2.update.buildDate"), Main.guiBuildDate() } }, new String[] { ui.getString("fina2.Versions"), ui.getString("fina2.Values") });
		systemTable = new JTable();
		systemTable.setFont(ui.getFont());
		systemTable.setEnabled(false);
		systemTable.setModel(systemTableModel);
		systemTable.setBounds(175, 115, 232, 183);
		systemTable.setBorder(BorderFactory.createLineBorder(bg));
		systemTable.setShowHorizontalLines(false);
		systemTable.setShowVerticalLines(false);
		systemTable.setBackground(bg);
		systemTable.setForeground(color);
		getContentPane().add(systemTable);

		aboutFinaText = new JTextPane();
		aboutFinaText.setFont(ui.getFont());
		aboutFinaText.setText(ui.getString("fina2.AboutFinaText"));
		aboutFinaText.setBounds(175, 10, 232, 97);
		aboutFinaText.setEditable(false);
		aboutFinaText.setBackground(bg);
		aboutFinaText.setForeground(color);
		getContentPane().add(aboutFinaText);
	}

	public void closeButtonActionPerformed(ActionEvent e) {
		this.setVisible(false);
		this.dispose();
	}
}
