package fina2.bank;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import fina2.BaseFrame;
import fina2.Main;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableReviewFrame;
import fina2.ui.table.TableRowImpl;

@SuppressWarnings("serial")
@Deprecated
public class RegionCityDialog extends BaseFrame {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private InitialContext jndi = null;
	private Object ref = null;
	private BankRegionSessionHome regCityHome = null;
	private BankRegionSession regCitySession = null;

	private RegionCityCreateAction createRegCityAction;
	private RegionCityAmendAction amendRegCityAction;
	private RegionCityRemoveAction removeRegCityAction;
	private Vector<String> cities = new Vector<String>();
	private Vector<String> regions = new Vector<String>();
	private JTextField searchCityText;
	private JTextField searchRegionText;
	private JComboBox searchCityCombo;
	private JComboBox searchRegionCombo;
	private EJBTable table;
	private boolean city_hide_flag = false;
	private boolean region_hide_flag = false;
	private JLabel searchCityLabel;
	private JLabel searchRegionLabel;
	private String lastCityAmended;

	public void setCitySelected(String lastCityAmended) {
		this.lastCityAmended = lastCityAmended;
	}

	public EJBTable getTable() {
		return table;
	}

	private void initCitySuggestion() {
		try {

			Vector<String> colNames = new Vector<String>();
			colNames.add(ui.getString("fina2.bank.city"));
			colNames.add(ui.getString("fina2.bank.region"));

			Vector<TableRowImpl> regionCities = (Vector<TableRowImpl>) regCitySession.getRegionRows(main.getUserHandle(), main.getLanguageHandle());

			for (int i = 0; i < regionCities.size(); i++) {
				if (!cities.contains(regionCities.get(i).getValue(0).toString()))
					cities.add(regionCities.get(i).getValue(0).toString());
				if (!regions.contains(regionCities.get(i).getValue(1).toString()))
					regions.add(regionCities.get(i).getValue(1).toString());
			}
			regionCities.clear();
			Collections.sort(cities);
			Collections.sort(regions);

		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
	}

	public RegionCityDialog() {
		try {
			jndi = fina2.Main.getJndiContext();
			ref = jndi.lookup("fina2/bank/BankRegionSession");
			regCityHome = (BankRegionSessionHome) PortableRemoteObject.narrow(ref, BankRegionSessionHome.class);
			regCitySession = regCityHome.create();
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.print", "print.gif");
		ui.loadIcon("fina2.refresh", "refresh.gif");
		ui.loadIcon("fina2.close", "cancel.gif");

		ui.loadIcon("fina2.amend", "amend.gif");
		ui.loadIcon("fina2.create", "insert.gif");
		ui.loadIcon("fina2.review", "review.gif");
		ui.loadIcon("fina2.delete", "delete.gif");

		createRegCityAction = new RegionCityCreateAction(main.getMainFrame(), table, this);
		amendRegCityAction = new RegionCityAmendAction(main.getMainFrame(), table, this);
		removeRegCityAction = new RegionCityRemoveAction(main.getMainFrame(), table, this);

		table = new EJBTable();

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		initComponents();
		scrollPane.setViewportView(table);
	}

	public void show() {

		if (isVisible())
			return;
		try {
			/*
			 * fina2.security.User user = (fina2.security.User) main
			 * .getUserHandle().getEJBObject(); canAmend =
			 * user.hasPermission("fina2.bank.amend"); canDelete =
			 * user.hasPermission("fina2.bank.delete"); canReview =
			 * user.hasPermission("fina2.bank.review");
			 */
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

		initTable();
		if ((searchRegionText.getText() != null) && (searchRegionText.getText().trim().length() > 0))
			for (int i = 0; i < table.getRowCount(); i++) {
				if (!table.getTableRow(i).getValue(1).equals(searchRegionText.getText().trim())) {
					table.removeRow(i);
					i--;
				}
			}
		super.show();
	}

	private void setCityModel(DefaultComboBoxModel mdl, String str) {
		searchCityCombo.setModel(mdl);
		searchCityCombo.setSelectedIndex(-1);

		searchCityText.setText(str);
	}

	private void setRegionModel(DefaultComboBoxModel mdl, String str) {
		searchRegionCombo.setModel(mdl);
		searchRegionCombo.setSelectedIndex(-1);

		searchRegionText.setText(str);
	}

	private DefaultComboBoxModel getSuggestedModel(java.util.List<String> list, String text) {
		DefaultComboBoxModel m = new DefaultComboBoxModel();
		for (String s : list) {
			if (s.toLowerCase().trim().contains(text))
				m.addElement(s);
		}
		return m;
	}

	private void initComponents() {
		BaseFrame.ensureVisible(this);
		initCitySuggestion();
		setBounds(0, 0, 700, 500);
		setMinimumSize(new Dimension(700, 500));

		Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(d.width / 2 - getWidth() / 2, d.height / 2 - getHeight() / 2);

		tablePanel = new javax.swing.JPanel();
		scrollPane = new javax.swing.JScrollPane();
		buttonPanel = new javax.swing.JPanel();
		filterPanel = new JPanel();
		standartButtonPanel = new JPanel();

		addButton = new JButton();
		amendButton = new JButton();
		removeButton = new JButton();

		addButton.setFont(ui.getFont());
		addButton.setAction(createRegCityAction);
		addButton.setIcon(ui.getIcon("fina2.create"));
		addButton.setText(ui.getString("fina2.license.add"));

		amendButton.setFont(ui.getFont());
		amendButton.setAction(amendRegCityAction);
		amendButton.setIcon(ui.getIcon("fina2.amend"));
		amendButton.setText(ui.getString("fina2.amend"));

		removeButton.setFont(ui.getFont());
		removeButton.setAction(removeRegCityAction);
		removeButton.setIcon(ui.getIcon("fina2.delete"));
		removeButton.setText(ui.getString("fina2.license.delete"));

		searchCityLabel = new JLabel();
		searchCityLabel.setFont(ui.getFont());
		searchCityLabel.setText("Search City");
		searchCityLabel.setBounds(10, 10, 60, 20);

		searchCityCombo = new JComboBox();
		searchCityCombo.setFont(ui.getFont());
		searchCityCombo.setEditable(true);
		searchCityCombo.setBounds(75, 10, 200, 20);

		searchRegionLabel = new JLabel();
		searchRegionLabel.setFont(ui.getFont());
		searchRegionLabel.setText("Search Region");
		searchRegionLabel.setBounds(285, 10, 80, 20);

		searchRegionCombo = new JComboBox();
		searchRegionCombo.setFont(ui.getFont());
		searchRegionCombo.setEditable(true);
		searchRegionCombo.setBounds(365, 10, 205, 20);

		searchCityText = (JTextField) searchCityCombo.getEditor().getEditorComponent();
		searchCityText.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent ke) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {

						String text = searchCityText.getText();
						if (text.trim().length() == 0) {
							searchCityCombo.hidePopup();
							setCityModel(new DefaultComboBoxModel(cities), "");
						} else {
							DefaultComboBoxModel m = getSuggestedModel(cities, text.toLowerCase());
							if (m.getSize() == 0 || city_hide_flag) {
								searchCityCombo.hidePopup();
								city_hide_flag = false;
							} else {
								setCityModel(m, text);
								searchCityCombo.showPopup();
							}
						}

					}

				});
			}

			public void keyPressed(KeyEvent e) {
				String text = searchCityText.getText();
				int code = e.getKeyCode();
				if (code == KeyEvent.VK_ENTER) {
					if (!cities.contains(text)) {
						cities.addElement(text);
						Collections.sort(cities);
						setCityModel(getSuggestedModel(cities, text), text);
					}
					city_hide_flag = true;
				} else if (code == KeyEvent.VK_ESCAPE) {
					city_hide_flag = true;
				} else if (code == KeyEvent.VK_RIGHT) {
					for (int i = 0; i < cities.size(); i++) {
						String str = cities.elementAt(i);
						if (str.startsWith(text)) {
							searchCityCombo.setSelectedIndex(-1);
							searchCityText.setText(str);
							return;
						}
					}
				}
			}
		});

		searchCityCombo.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JComboBox b = (JComboBox) e.getSource();
				Object item = b.getSelectedItem();
				try {
					if (item != null) {

						String city = item.toString();

						if ((searchRegionText.getText() == null) || (searchRegionText.getText().trim().length() == 0))
							initTable();
						int rows = table.getRowCount();
						for (int i = 0; i < rows; i++) {
							if (table.getTableRow(i).getValue(0).trim().equals(city.trim())) {
								table.getSelectionModel().setSelectionInterval(i, i);
							}
						}

					}
				} catch (Exception ex) {
					Main.generalErrorHandler(ex);
				}

			}
		});

		searchRegionText = (JTextField) searchRegionCombo.getEditor().getEditorComponent();
		searchRegionText.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent ke) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {

						String text = searchRegionText.getText();
						if (text.trim().length() == 0) {
							searchRegionCombo.hidePopup();
							setRegionModel(new DefaultComboBoxModel(regions), "");
						} else {
							DefaultComboBoxModel m = getSuggestedModel(regions, text.toLowerCase());
							if (m.getSize() == 0 || region_hide_flag) {
								searchRegionCombo.hidePopup();
								region_hide_flag = false;
							} else {
								setRegionModel(m, text);
								searchRegionCombo.showPopup();
							}
						}

					}

				});
			}

			public void keyPressed(KeyEvent e) {
				String text = searchRegionText.getText();
				int code = e.getKeyCode();
				if (code == KeyEvent.VK_ENTER) {
					if (!regions.contains(text)) {
						regions.addElement(text);
						Collections.sort(regions);
						setRegionModel(getSuggestedModel(regions, text), text);
					}
					region_hide_flag = true;
				} else if (code == KeyEvent.VK_ESCAPE) {
					region_hide_flag = true;
				} else if (code == KeyEvent.VK_RIGHT) {
					for (int i = 0; i < regions.size(); i++) {
						String str = regions.elementAt(i);
						if (str.startsWith(text)) {
							searchRegionCombo.setSelectedIndex(-1);
							searchRegionText.setText(str);
							return;
						}
					}
				}
			}
		});

		searchRegionCombo.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JComboBox b = (JComboBox) e.getSource();
				Object item = b.getSelectedItem();
				try {
					if (item != null) {

						String region = item.toString();

						initTable();
						for (int i = 0; i < table.getRowCount(); i++) {
							if (!table.getTableRow(i).getValue(1).trim().equals(region.trim())) {
								table.removeRow(i);
								i--;
							}
						}
						if ((table != null) && (lastCityAmended != null)) {
							for (int i = 0; i < table.getRowCount(); i++)
								if (table.getTableRow(i).getValue(0).toString().trim().equals(lastCityAmended.trim())) {
									table.getSelectionModel().setSelectionInterval(i, i);
									break;
								}
						} else if (table != null) {
							table.getSelectionModel().setSelectionInterval(0, 0);

						}

					}
				} catch (Exception ex) {
					Main.generalErrorHandler(ex);
				}

			}
		});
		setTitle("Region/City Management");

		tablePanel.setLayout(new java.awt.BorderLayout());

		scrollPane.setPreferredSize(new java.awt.Dimension(350, 350));
		tablePanel.add(scrollPane, java.awt.BorderLayout.CENTER);

		getContentPane().add(tablePanel, java.awt.BorderLayout.CENTER);

		filterPanel.setLayout(null);
		filterPanel.setPreferredSize(new Dimension(350, 40));
		filterPanel.add(searchCityLabel);
		filterPanel.add(searchCityCombo);
		filterPanel.add(searchRegionLabel);
		filterPanel.add(searchRegionCombo);

		getContentPane().add(filterPanel, java.awt.BorderLayout.NORTH);

		standartButtonPanel.setLayout(null);
		standartButtonPanel.setPreferredSize(new Dimension(350, 40));

		helpButton = new JButton();
		helpButton.setBounds(4, 4, 100, 25);
		helpButton.setIcon(ui.getIcon("fina2.help"));
		helpButton.setText(ui.getString("fina2.help"));
		helpButton.setEnabled(false);

		printButton = new JButton();
		printButton.setBounds(370, 4, 100, 25);
		printButton.setIcon(ui.getIcon("fina2.print"));
		printButton.setText(ui.getString("fina2.print"));
		printButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TableReviewFrame printFrame = new TableReviewFrame();
				printFrame.show("", table);

			}
		});

		refreshButton = new JButton();
		refreshButton.setBounds(475, 4, 100, 25);
		refreshButton.setIcon(ui.getIcon("fina2.refresh"));
		refreshButton.setText(ui.getString("fina2.refresh"));

		refreshButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				initTableBy(searchRegionText.getText());
			}
		});

		closeButton = new JButton();
		closeButton.setBounds(580, 4, 100, 25);
		closeButton.setIcon(ui.getIcon("fina2.close"));
		closeButton.setText(ui.getString("fina2.close"));
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		standartButtonPanel.add(helpButton);
		standartButtonPanel.add(printButton);
		standartButtonPanel.add(refreshButton);
		standartButtonPanel.add(closeButton);

		getContentPane().add(standartButtonPanel, java.awt.BorderLayout.SOUTH);

		buttonPanel.setLayout(null);
		buttonPanel.setPreferredSize(new Dimension(110, this.getHeight()));

		addButton.setBounds(5, 5, 100, 25);
		amendButton.setBounds(5, 35, 100, 25);
		removeButton.setBounds(5, 65, 100, 25);

		buttonPanel.add(addButton);
		buttonPanel.add(amendButton);
		buttonPanel.add(removeButton);

		getContentPane().add(buttonPanel, java.awt.BorderLayout.EAST);

	}

	private void initTableBy(String region) {
		try {
			jndi = fina2.Main.getJndiContext();
			ref = jndi.lookup("fina2/bank/BankRegionSession");
			regCityHome = (BankRegionSessionHome) PortableRemoteObject.narrow(ref, BankRegionSessionHome.class);

			regCitySession = regCityHome.create();

			Vector<String> colNames = new Vector<String>();
			colNames.add(ui.getString("fina2.bank.city"));
			colNames.add(ui.getString("fina2.bank.region"));

			table.initTable(colNames, regCitySession.getRegionRows(main.getUserHandle(), main.getLanguageHandle()));

			if ((region != null) && (region.trim().length() > 0))
				for (int i = 0; i < table.getRowCount(); i++)
					if (!table.getTableRow(i).getValue(1).toString().trim().equals(region.trim())) {
						table.removeRow(i);
						i--;
					}
			if (table.getRowCount() > 0)
				table.getSelectionModel().setSelectionInterval(0, 0);
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
	}

	private void initTable() {
		try {
			jndi = fina2.Main.getJndiContext();
			ref = jndi.lookup("fina2/bank/BankRegionSession");
			regCityHome = (BankRegionSessionHome) PortableRemoteObject.narrow(ref, BankRegionSessionHome.class);

			regCitySession = regCityHome.create();

			Vector<String> colNames = new Vector<String>();
			colNames.add(ui.getString("fina2.bank.city"));
			colNames.add(ui.getString("fina2.bank.region"));

			table.initTable(colNames, regCitySession.getRegionRows(main.getUserHandle(), main.getLanguageHandle()));
			if (table != null && lastCityAmended != null)
				for (int i = 0; i < table.getRowCount(); i++)
					if (table.getTableRow(i).getValue(0).toString().trim().equals(lastCityAmended.trim())) {
						table.getSelectionModel().setSelectionInterval(i, i);
						break;
					}
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
	}

	private javax.swing.JPanel tablePanel;
	private javax.swing.JPanel buttonPanel;
	private javax.swing.JPanel filterPanel;
	private javax.swing.JPanel standartButtonPanel;
	private javax.swing.JScrollPane scrollPane;
	private javax.swing.JButton addButton;
	private javax.swing.JButton amendButton;
	private javax.swing.JButton removeButton;

}

@SuppressWarnings("serial")
class RegionCityCreateAction extends AbstractAction {

	private java.awt.Frame parent;
	private EJBTable table;
	private CreateAmendRegionCityDialog create = null;
	private RegionCityDialog rcd;

	public RegionCityCreateAction(java.awt.Frame parent, EJBTable table, RegionCityDialog rcd) {
		super();
		this.parent = parent;
		this.table = table;
		this.rcd = rcd;
		create = new CreateAmendRegionCityDialog(parent, false, rcd, table);
	}

	public void actionPerformed(java.awt.event.ActionEvent e) {
		create.setVisible(true);
	}

}

@SuppressWarnings("serial")
class RegionCityAmendAction extends AbstractAction {

	private java.awt.Frame parent;
	private EJBTable table;
	private CreateAmendRegionCityDialog amendDialog = null;
	private RegionCityDialog rcd;

	public RegionCityAmendAction(java.awt.Frame parent, EJBTable table, RegionCityDialog rcd) {
		super();
		this.parent = parent;
		this.table = table;
		amendDialog = new CreateAmendRegionCityDialog(parent, true, rcd, table);
		this.rcd = rcd;
	}

	public void actionPerformed(java.awt.event.ActionEvent e) {
		this.table = rcd.getTable();
		amendDialog.initCityRegion(table.getSelectedTableRow().getValue(0), table.getSelectedTableRow().getValue(1));
		amendDialog.setVisible(true);
	}
}

@SuppressWarnings("serial")
class RegionCityRemoveAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;
	private java.awt.Frame parent;
	private EJBTable table;
	private RegionCityDialog rcd;

	public RegionCityRemoveAction(java.awt.Frame parent, EJBTable table, RegionCityDialog rcd) {
		super();
		this.rcd = rcd;
		this.table = table;
		this.parent = parent;
	}

	public void actionPerformed(java.awt.event.ActionEvent e) {
		this.table = rcd.getTable();
		try {

			String city = table.getSelectedTableRow().getValue(0);
			if (ui.showConfirmBox(parent, "Would you like to remove selected city/region?")) {
				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi.lookup("fina2/bank/BankRegionSession");
				BankRegionSessionHome home = (BankRegionSessionHome) PortableRemoteObject.narrow(ref, BankRegionSessionHome.class);

				BankRegionSession session = home.create();
				int id = session.getId(city, main.getLanguageHandle());
				if (!session.removeCity(id, main.getLanguageHandle())) {
					ui.showMessageBox(parent, "Unable to delete,city region is in use");
				} else {
					rcd.setVisible(false);
					rcd.show();
				}
			}
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}

	}
}

@SuppressWarnings("serial")
class CreateAmendRegionCityDialog extends JDialog {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private java.awt.Frame parent;
	private boolean amend;

	private Vector<String> v = new Vector<String>();

	private javax.swing.JLabel cityLabel;
	private javax.swing.JLabel regionLabel;
	private javax.swing.JTextField cityText;
	private javax.swing.JTextField regionText;
	private javax.swing.JComboBox regionCombo;

	private boolean hide_flag = false;

	private String city;
	private String oldCity;
	private String region;
	private String oldRegion;
	private javax.swing.JButton okButton;
	private javax.swing.JButton cancelButton;

	private CreateAmendAction createAction;
	private CreateAmendAction amendAction;
	private RegionCityDialog rcd;
	private EJBTable table;

	public void initCityRegion(String ct, String rg) {
		cityText.setText(ct);
		regionText.setText(rg);
		this.oldCity = ct;
		this.oldRegion = rg;
	}

	public String getOldCity() {
		return oldCity.trim();
	}

	public String getOldRegion() {
		return oldRegion;
	}

	public String getCity() {
		return (cityText.getText() != null) ? cityText.getText().trim() : "";
	}

	public String getRegion() {
		return (regionText.getText() != null) ? regionText.getText().trim() : "";
	}

	public CreateAmendRegionCityDialog(java.awt.Frame parent, boolean amend, RegionCityDialog rcd, EJBTable table) {
		super(parent, true);
		this.amend = amend;
		this.rcd = rcd;
		this.table = table;
		setSize(350, 140);
		setResizable(false);
		setLayout(null);

		initComponents();

		setLocationRelativeTo(parent);
	}

	private void initSuggestion() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/bank/BankRegionSession");
			BankRegionSessionHome home = (BankRegionSessionHome) PortableRemoteObject.narrow(ref, BankRegionSessionHome.class);

			BankRegionSession session = home.create();

			Vector<String> colNames = new Vector<String>();
			colNames.add(ui.getString("fina2.bank.city"));
			colNames.add(ui.getString("fina2.bank.region"));

			Vector<TableRowImpl> regions = (Vector<TableRowImpl>) session.getRegionRows(main.getUserHandle(), main.getLanguageHandle());
			for (int i = 0; i < regions.size(); i++) {
				if (!v.contains(regions.get(i).getValue(1).toString()))
					v.add(regions.get(i).getValue(1).toString());
			}
			regions.clear();
			Collections.sort(v);

		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
	}

	private void setModel(DefaultComboBoxModel mdl, String str) {
		regionCombo.setModel(mdl);
		regionCombo.setSelectedIndex(-1);

		regionText.setText(str);
	}

	private DefaultComboBoxModel getSuggestedModel(java.util.List<String> list, String text) {
		DefaultComboBoxModel m = new DefaultComboBoxModel();
		for (String s : list) {
			if (text != null)
				if (s.toLowerCase().trim().contains(text.toLowerCase().trim()))
					m.addElement(s);
		}
		return m;
	}

	private void initComponents() {
		initSuggestion();
		cityLabel = new JLabel();
		cityLabel.setFont(ui.getFont());
		cityLabel.setText(ui.getString("fina2.bank.city"));
		cityLabel.setBounds(30, 20, 50, 20);
		getContentPane().add(cityLabel);

		cityText = new JTextField();
		cityText.setFont(ui.getFont());
		cityText.setText("");
		cityText.setBounds(84, 20, 220, 20);
		getContentPane().add(cityText);

		regionLabel = new JLabel();
		regionLabel.setFont(ui.getFont());
		regionLabel.setText(ui.getString("fina2.bank.region"));
		regionLabel.setBounds(30, 45, 50, 20);
		getContentPane().add(regionLabel);
		regionCombo = new JComboBox();
		regionCombo.setEditable(true);
		regionCombo.setBounds(84, 45, 220, 20);

		regionText = (JTextField) regionCombo.getEditor().getEditorComponent();

		regionText.setFont(ui.getFont());

		regionText.setBounds(84, 45, 220, 20);

		regionText = (JTextField) regionCombo.getEditor().getEditorComponent();
		regionText.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent ke) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {

						String text = regionText.getText();
						if (text.trim().length() == 0) {
							regionCombo.hidePopup();
							setModel(new DefaultComboBoxModel(v), "");
						} else {
							DefaultComboBoxModel m = getSuggestedModel(v, text);
							if (m.getSize() == 0 || hide_flag) {
								regionCombo.hidePopup();
								hide_flag = false;
							} else {
								setModel(m, text);
								regionCombo.showPopup();
							}
						}

					}

				});
			}

			public void keyPressed(KeyEvent e) {
				String text = regionText.getText();
				int code = e.getKeyCode();
				if (code == KeyEvent.VK_ENTER) {
					if (!v.contains(text)) {
						v.addElement(text);
						Collections.sort(v);
						setModel(getSuggestedModel(v, text), text);
					}
					hide_flag = true;
				} else if (code == KeyEvent.VK_ESCAPE) {
					hide_flag = true;
				} else if (code == KeyEvent.VK_RIGHT) {
					for (int i = 0; i < v.size(); i++) {
						String str = v.elementAt(i);
						if (str.startsWith(text)) {
							regionCombo.setSelectedIndex(-1);
							regionText.setText(str);
							return;
						}
					}
				}
			}
		});
		getContentPane().add(regionCombo);

		createAction = new CreateAmendAction(amend, this, rcd);

		okButton = new JButton();
		okButton.setAction(createAction);
		okButton.setText("Ok");
		okButton.setBounds(100, 75, 80, 25);
		getContentPane().add(okButton);

		cancelButton = new JButton();
		cancelButton.addActionListener(new AbstractAction() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				cancelButtonActionPerformed();
			}
		});
		cancelButton.setText("Cancel");
		cancelButton.setBounds(185, 75, 80, 25);

		getContentPane().add(cancelButton);
	}

	private void cancelButtonActionPerformed() {
		this.setVisible(false);
	}
}

@SuppressWarnings("serial")
class CreateAmendAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private boolean amend;
	private CreateAmendRegionCityDialog createAmendRCD;
	private RegionCityDialog rcd;

	public CreateAmendAction(boolean amend, CreateAmendRegionCityDialog createAmendRCD, RegionCityDialog rcd) {
		this.amend = amend;
		this.createAmendRCD = createAmendRCD;
		this.rcd = rcd;
	}

	public void actionPerformed(java.awt.event.ActionEvent e) {
		try {

			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/bank/BankRegionSession");
			BankRegionSessionHome home = (BankRegionSessionHome) PortableRemoteObject.narrow(ref, BankRegionSessionHome.class);

			BankRegionSession session = home.create();
			if (!amend) {
				String city = createAmendRCD.getCity();
				String region = createAmendRCD.getRegion();
				if ((city == null) || (city.trim().length() == 0) || (region == null) || (region.trim().length() == 0))
					ui.showMessageBox(main.getMainFrame(), "City/Region is Required");
				else {
					if (session.cityExists(city)) {
						ui.showMessageBox(main.getMainFrame(), "City Already Exists");
					} else {
						session.addCityRegion(city, createAmendRCD.getRegion(), main.getLanguageHandle());
						createAmendRCD.setVisible(false);
						rcd.setVisible(false);
						rcd.setCitySelected(city);
						rcd.show();
					}
				}
			} else {
				String oldCity = createAmendRCD.getOldCity();
				String newCity = createAmendRCD.getCity();
				String newRegion = createAmendRCD.getRegion();
				String oldRegion = createAmendRCD.getOldRegion();

				if ((newCity == null) || (newRegion == null) || (newCity.trim().length() == 0) || (newRegion.trim().length() == 0))
					ui.showMessageBox(main.getMainFrame(), "City/Region is Required");
				else {
					int id = session.getId(oldCity, main.getLanguageHandle());
					if (newCity.trim().equals(oldCity.trim()) && newRegion.trim().equals(oldRegion.trim())) {
						createAmendRCD.setVisible(false);
						rcd.setVisible(false);
						rcd.setCitySelected(newCity);
						rcd.show();
					} else if (session.regionCityExists(newCity, newRegion)) {
						ui.showMessageBox(main.getMainFrame(), "City Already Belongs To This Region");
					} else {
						session.updateCityRegion(id, newCity, newRegion);
						createAmendRCD.setVisible(false);
						rcd.setVisible(false);
						rcd.setCitySelected(newCity);
						rcd.show();

					}
				}
			}
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
	}
}
