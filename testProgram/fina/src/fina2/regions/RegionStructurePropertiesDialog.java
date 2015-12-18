package fina2.regions;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class RegionStructurePropertiesDialog extends JDialog {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private Map<Integer, String> propertiesMap;

	public RegionStructurePropertiesDialog(java.awt.Frame parent) {
		super(parent, true);

		initCompontnts();

		this.setSize(320, 250);
		this.setLocationRelativeTo(parent);
		this.setVisible(true);
		this.pack();
	}

	private void initCompontnts() {
		this.setTitle(ui.getString("fina2.region.properties"));
		southPanel = new JPanel();
		westPanel = new JPanel();
		maxLevelLabel = new JLabel();
		maxLevelSpinner = new JSpinner();
		saveButton = new JButton(ui.getIcon("fina2.save"));
		closeButton = new JButton(ui.getIcon("fina2.close"));

		container = this.getContentPane();
		container.setLayout(new BorderLayout());

		westPanel.setBorder(new EmptyBorder(5, 3, 3, 3));
		westPanel.setLayout(new GridBagLayout());

		// ---- maxLevelLabel ----
		maxLevelLabel.setText(ui.getString("fina2.region.maxLevel"));
		westPanel.add(maxLevelLabel, new GridBagConstraints(0, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
		westPanel.add(maxLevelSpinner, new GridBagConstraints(1, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		// Spinner

		maxLevelSpinner.setModel(new SpinnerNumberModel(1, 1, 100, 1));
		JSpinner.NumberEditor numberEditor = new JSpinner.NumberEditor(
				maxLevelSpinner);
		maxLevelSpinner.setEditor(numberEditor);
		maxLevelSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				int maxL = (Integer) maxLevelSpinner.getValue();
				int rowCount = table.getRowCount();
				if (rowCount < maxL) {
					for (int i = rowCount; i < maxL; i++) {
						Object[] rowData = new Object[2];
						rowData[0] = ui.getString("fina2.level") + " "
								+ (i + 1);
						rowData[1] = null;
						model.insertRow(i, rowData);
					}
				}
				if (rowCount > maxL) {
					model.removeRow(maxL);
				}
			}
		});
		container.add(westPanel, BorderLayout.NORTH);

		southPanel.setBorder(new EmptyBorder(3, 3, 5, 3));
		southPanel.setLayout(new GridBagLayout());

		// ---- saveButton ----
		saveButton.setText(ui.getString("fina2.returns.save"));
		southPanel.add(saveButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 5), 0, 0));
		saveButton.setFont(ui.getFont());
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveButtonAction();
			}
		});

		// ---- closeButton ----
		closeButton.setText(ui.getString("fina2.close"));
		southPanel.add(closeButton, new GridBagConstraints(1, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		closeButton.setFont(ui.getFont());
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeDialog();
			}
		});
		container.add(southPanel, BorderLayout.SOUTH);

		table = new JTable();
		table.setModel(new PropertiesTableModel());
		this.initTableRows();
		table.setFont(ui.getFont());
		table.getTableHeader().setFont(ui.getFont());
		container.add(new JScrollPane(table), BorderLayout.CENTER);
	}

	private void initTableRows() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/regions/RegionStructureSession");
			RegionStructureSessionHome home = (RegionStructureSessionHome) PortableRemoteObject
					.narrow(ref, RegionStructureSessionHome.class);
			RegionStructureSession session = home.create();

			Map<Integer, String> map = session.getProperties(main
					.getLanguageHandle());

			String maxL = map.get(0);
			map.remove(0);

			DefaultTableModel model = (DefaultTableModel) table.getModel();
			if (maxL != null) {
				int maxLevelSize = Integer.parseInt(maxL);
				for (int i = 0; i < maxLevelSize; i++) {
					String value = null;
					if (i < map.size()) {
						value = map.get(i + 1);
					}
					Object[] rowData = new Object[2];
					rowData[0] = ui.getString("fina2.level") + " " + (i + 1);
					rowData[1] = value;
					model.insertRow(i, rowData);
				}
				maxLevelSpinner.setValue(maxLevelSize);
			} else {
				// Set Default Data
				maxLevelSpinner.setValue(1);
				Object[] rowData = new Object[2];
				rowData[0] = ui.getString("fina2.level") + " " + 1;
				rowData[1] = null;
				model.insertRow(0, rowData);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void saveButtonAction() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/regions/RegionStructureSession");
			RegionStructureSessionHome home = (RegionStructureSessionHome) PortableRemoteObject
					.narrow(ref, RegionStructureSessionHome.class);
			RegionStructureSession session = home.create();

			propertiesMap = new HashMap<Integer, String>();

			int maxLevel = new Integer((Integer) maxLevelSpinner.getValue());

			propertiesMap.put(0, maxLevel + "");

			for (int i = 0; i < maxLevel; i++) {
				String s = (String) table.getValueAt(i, 1);
				propertiesMap.put(i + 1, s);
			}

			session.setProperties(propertiesMap, main.getLanguageHandle());
			closeDialog();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void closeDialog() {
		dispose();
	}

	class PropertiesTableModel extends DefaultTableModel {
		public PropertiesTableModel() {
			super(new Object[][] {}, new String[] {
					ui.getString("fina2.level"),
					ui.getString("fina2.reportoo.name") });
		}

		Class<?>[] columnTypes = new Class<?>[] { String.class, String.class };

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return columnTypes[columnIndex];
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return (columnIndex != 0);
		}
	}

	public Map<Integer, String> getPropertiesMap() {
		return propertiesMap;
	}

	private Container container;
	private JTable table;
	private JPanel westPanel;
	private JLabel maxLevelLabel;
	private JSpinner maxLevelSpinner;
	private JPanel southPanel;
	private JButton saveButton;
	private JButton closeButton;
}