package fina2.i18n;

import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import net.coderazzi.filters.gui.TableFilterHeader;
import net.coderazzi.filters.gui.TableFilterHeader.Position;

import org.apache.log4j.Logger;

import fina2.Main;

@SuppressWarnings("serial")
public class LanguageBundleAmendDialog extends javax.swing.JDialog {

	private static Logger log = Logger.getLogger(LanguageBundleAmendDialog.class);

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private JTable bundlesTable;

	private List<Language> languages;

	public LanguageBundleAmendDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		initComponents();
		bundlesTable = new JTable();

		// Add Table Filter
		TableFilterHeader filterHeader = new TableFilterHeader(bundlesTable);
		filterHeader.setPosition(Position.TOP);
		filterHeader.setFont(ui.getFont());

	}

	private void initTable() {
		try {
			BundlesTableModel model = new BundlesTableModel(languages);
			bundlesTable.setModel(model);

			for (int i = 0; i < languages.size(); i++) {

				Language lang = languages.get(i);
				Font font = null;

				try {
					font = new Font(lang.getFontFace(), Font.PLAIN, lang.getFontSize());
				} catch (Exception ex) {
					font = ui.getFont();
				}

				TableCellRenderer renderer = new CustomTableCellRenderer(font);
				bundlesTable.getColumnModel().getColumn(i + 1).setCellRenderer(renderer);

				TableCellEditor editor = new CustomTableCellEditor(font, new JTextField());
				bundlesTable.getColumnModel().getColumn(i + 1).setCellEditor(editor);
			}
		} catch (Exception ex) {
			log.error("Error occured during resource bundles table initialization", ex);
			Main.generalErrorHandler(ex);
		}
		scrollPane.setViewportView(bundlesTable);
	}

	public void show(Language lang) {
		List<Language> languages = new ArrayList<Language>();
		languages.add(lang);
		show(languages);
	}

	@SuppressWarnings("deprecation")
	public void show(List<Language> languages) {
		this.languages = languages;
		initTable();
		java.awt.Toolkit t = java.awt.Toolkit.getDefaultToolkit();
		java.awt.Dimension d = t.getScreenSize();
		setSize(d.width / 2, d.height / 2);
		setLocationRelativeTo(getParent());
		super.show();
	}

	private void initComponents() {
		jPanel1 = new javax.swing.JPanel();
		scrollPane = new javax.swing.JScrollPane();
		jPanel4 = new javax.swing.JPanel();
		jPanel5 = new javax.swing.JPanel();
		helpButton = new javax.swing.JButton();
		jPanel6 = new javax.swing.JPanel();
		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		bundlesTable = new JTable();

		setTitle(ui.getString("fina2.messages"));
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

		getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);

		jPanel4.setLayout(new java.awt.BorderLayout());

		helpButton.setIcon(ui.getIcon("fina2.help"));
		helpButton.setFont(ui.getFont());
		helpButton.setText(ui.getString("fina2.help"));
		helpButton.setEnabled(false);
		jPanel5.add(helpButton);

		jPanel4.add(jPanel5, java.awt.BorderLayout.WEST);

		okButton.setIcon(ui.getIcon("fina2.ok"));
		okButton.setFont(ui.getFont());
		okButton.setText(ui.getString("fina2.ok"));
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				okButtonActionPerformed(evt);
			}
		});

		jPanel6.add(okButton);

		cancelButton.setIcon(ui.getIcon("fina2.cancel"));
		cancelButton.setFont(ui.getFont());
		cancelButton.setText(ui.getString("fina2.cancel"));
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		jPanel6.add(cancelButton);

		jPanel4.add(jPanel6, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel4, java.awt.BorderLayout.SOUTH);

		pack();
	}

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
		setVisible(false);
		dispose();
	}

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {

		BundlesTableModel model = (BundlesTableModel) bundlesTable.getModel();

		for (int i = 0; i < languages.size(); i++) {
			Properties messages = new Properties();
			for (String key : model.getKeys()) {
				String value = model.getMessages().get(i).getProperty(key);
				messages.put(key, value != null ? value : "");
			}

			try {
				ui.setLanguageBundle(languages.get(i).getHandle(), messages);
			} catch (Exception ex) {
				log.error("Error occured during storing bundles", ex);
				Main.generalErrorHandler(ex);
			}
		}
		setVisible(false);
		dispose();
	}

	private void closeDialog(java.awt.event.WindowEvent evt) {
		setVisible(false);
		dispose();
	}

	private javax.swing.JPanel jPanel1;
	private javax.swing.JScrollPane scrollPane;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JButton helpButton;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JButton okButton;
	private javax.swing.JButton cancelButton;
	// End of variables declaration//GEN-END:variables
}

class BundlesTableModel extends AbstractTableModel {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private List<Language> languages;

	private List<String> keys;

	private List<Properties> messages;

	private List<String> columnNames;

	public BundlesTableModel(List<Language> languages) throws Exception {

		this.languages = languages;
		this.messages = new ArrayList<Properties>();
		this.keys = new ArrayList<String>();
		this.columnNames = new ArrayList<String>();

		Set allKeys = new HashSet();
		for (Language lang : languages) {
			Properties msgs = ui.getLanguageBundle(lang.getHandle());
			messages.add(msgs);
			allKeys.addAll(msgs.keySet());
		}

		keys.addAll(allKeys);

		columnNames.add("Key");
		for (Language lang : languages) {
			columnNames.add(lang.getDescription() + " (" + lang.getCode() + ")");
		}
	}

	public int getRowCount() {
		return keys.size();
	}

	public int getColumnCount() {
		return languages.size() + 1;
	}

	public String getColumnName(int columnIndex) {
		return columnNames.get(columnIndex);
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex > 0;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		String value = keys.get(rowIndex);
		if (columnIndex != 0) {
			value = messages.get(columnIndex - 1).getProperty(value);
		}
		return value;
	}

	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		messages.get(columnIndex - 1).setProperty(keys.get(rowIndex), value.toString());
	}

	public List<String> getKeys() {
		return keys;
	}

	public List<Properties> getMessages() {
		return messages;
	}

	public List<Language> getLanguages() {
		return languages;
	}
}

class CustomTableCellRenderer extends DefaultTableCellRenderer {

	private Font font;

	public CustomTableCellRenderer(Font font) {
		this.font = font;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		cell.setFont(font);

		return cell;
	}
}

class CustomTableCellEditor extends DefaultCellEditor {

	private Font font;

	public CustomTableCellEditor(Font font, JTextField jTextField) {
		super(jTextField);
		this.font = font;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		Component cell = super.getTableCellEditorComponent(table, value, isSelected, row, column);
		cell.setFont(font);

		return cell;
	}
}
