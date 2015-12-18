package fina2.reportoo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import fina2.Main;
import fina2.i18n.Language;
import fina2.i18n.LanguagePK;
import fina2.reportoo.repository.Folder;
import fina2.reportoo.repository.Formula;
import fina2.reportoo.repository.FormulaAmendDialog;
import fina2.reportoo.repository.ParametersDialog;
import fina2.reportoo.server.Report;
import fina2.reportoo.server.ReportHome;
import fina2.reportoo.server.ReportPK;
import fina2.returns.DefinitionTable;
import fina2.returns.ReturnConstants;
import fina2.returns.ReturnSession;
import fina2.returns.ReturnSessionHome;
import fina2.returns.ValuesTableRow;
import fina2.ui.sheet.Spreadsheet;
import fina2.ui.sheet.SpreadsheetsManager;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableRow;
import fina2.ui.table.TableRowImpl;
import fina2.ui.tree.EJBTree;
import fina2.ui.tree.Node;

public class Designer extends javax.swing.JFrame {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private Spreadsheet sheet;

	private FormulaAmendDialog formulaDialog;

	private EJBTable selectionTable;
	private EJBTable parametersTable;

	private Hashtable namedObjects;
	private Hashtable parametersObjects;

	private int headerRow = 0;
	private int footerRow = 5000;
	private Vector ti;
	private Vector types;

	private EJBTree tree;
	private DefaultMutableTreeNode node;
	private ReportPK reportPK;
	private ReportPK parentPK;

	private fina2.reportoo.repository.AmendFolderDialog folderDialog;

	private Logger log = Logger.getLogger(getClass());

	private JWindow errorWindow;
	private String errorString = ui.getString("fina2.report.invalidName");

	/** Creates new form Designer */
	public Designer() {

		// iter = new fina2.ui.sheet.openoffice.OOIterator();

		try {
			sheet = SpreadsheetsManager.getInstance().createSpreadsheet();

			namedObjects = new Hashtable();
			parametersObjects = new Hashtable();

			ui.loadIcon("fina2.new", "new.gif");
			ui.loadIcon("fina2.amend", "amend.gif");
			ui.loadIcon("fina2.delete", "delete.gif");
			ui.loadIcon("fina2.folder", "folder.gif");
			ui.loadIcon("fina2.reportoo.iterator", "insert.gif");
			ui.loadIcon("fina2.reportoo.table", "return_table.gif");
			ui.loadIcon("fina2.reportoo.formula", "format_cells.gif");
			ui.loadIcon("fina2.save", "save.gif");

			formulaDialog = new FormulaAmendDialog(this, true);

			types = new Vector(7);
			types.add(ui.getString("fina2.select"));
			types.add(ui.getString("fina2.reportoo.iteratorType.bank"));
			types.add(ui.getString("fina2.reportoo.iteratorType.peer"));
			types.add(ui.getString("fina2.reportoo.iteratorType.node"));
			types.add(ui.getString("fina2.reportoo.iteratorType.period"));
			types.add(ui.getString("fina2.reportoo.iteratorType.offset"));
			types.add(ui.getString("fina2.reportoo.iteratorType.table"));

			initComponents();

			tabbedPanel.setTitleAt(0, ui.getString("fina2.reportoo.iterators"));
			tabbedPanel.setTitleAt(1, ui.getString("fina2.reportoo.formulas"));
			tabbedPanel.setTitleAt(2, ui.getString("fina2.reportoo.parameters"));

			formulasTreePanel.getFormulasTree().addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent evt) {
					repTreeValueChanged(evt);
				}
			});

			selectionTable = new EJBTable();
			parametersTable = new EJBTable();

			selectionTable.addSelectionListener(new javax.swing.event.ListSelectionListener() {
				public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
					if (selectionTable.getSelectedRow() == -1) {
						amendIteratorButton.setEnabled(false);
						deleteIteratorButton.setEnabled(false);
					} else {
						amendIteratorButton.setEnabled(true);
						deleteIteratorButton.setEnabled(true);
					}
				}
			});

			parametersTable.addSelectionListener(new javax.swing.event.ListSelectionListener() {
				public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
					if (parametersTable.getSelectedRow() == -1) {
						amendParameterButton.setEnabled(false);
						deleteParameterButton.setEnabled(false);
					} else {
						amendParameterButton.setEnabled(true);
						deleteParameterButton.setEnabled(true);
					}
				}
			});

			selectionScrollPane.setViewportView(selectionTable);
			parametersScrollPane.setViewportView(parametersTable);

			Component component = sheet.getComponent();
			component.setMinimumSize(new Dimension((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 250, 500));
			component.setFocusable(true);
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(component, BorderLayout.CENTER);
			splitPane.setLeftComponent(panel);

			folderDialog = new fina2.reportoo.repository.AmendFolderDialog(this, true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}

	public void show(EJBTree tree, DefaultMutableTreeNode node, ReportPK pk, ReportPK parentPK) {

		this.tree = tree;
		this.node = node;
		this.reportPK = pk;
		this.parentPK = parentPK;

		super.show();

		formulasTreePanel.initTree(null);
		initTable();

		loadConf();

		splitPane.setDividerLocation((int) (this.getWidth() * .8));

		if (pk == null) {
			sheet.loadBlank();
			namedObjects = new Hashtable();
			parametersObjects = new Hashtable();
		} else {
			load(reportPK);
		}

		sheet.setViewMode(sheet.VIEW_FULL);
		sheet.showSheetTabs(false);

		sheet.afterShow();

	}

	private void create() {
		try {
			InitialContext ctx = fina2.Main.getJndiContext();

			Object ref = ctx.lookup("fina2/reportoo/server/Report");
			ReportHome home = (ReportHome) PortableRemoteObject.narrow(ref, ReportHome.class);

			Report r = home.create(main.getUserHandle(), parentPK);

			reportPK = (ReportPK) r.getPrimaryKey();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private void load(ReportPK pk) {
		try {
			LanguagePK langPK = (LanguagePK) main.getLanguageHandle().getEJBObject().getPrimaryKey();
			InitialContext ctx = fina2.Main.getJndiContext();

			Object ref = ctx.lookup("fina2/reportoo/server/Report");
			ReportHome home = (ReportHome) PortableRemoteObject.narrow(ref, ReportHome.class);

			Report r = home.findByPrimaryKey(pk);

			nameText.setText(r.getDescription(main.getLanguageHandle()));

			sheet.read(r.getTemplate());

			// Copy language specific strings
			byte[] langReport = r.getLangTemplate(langPK);

			if (langReport != null) {
				Spreadsheet langSheet = SpreadsheetsManager.getInstance().createSpreadsheet(langReport);

				langSheet.setAutoCalculate(false);
				langSheet.clearFormulas(0, 0, langSheet.getLastRow(), langSheet.getLastCol());
				langSheet.copyStrings(sheet);
				langSheet.dispose();
			}

			sheet.recalculate();

			// Report r = home.create(new ReportPK(1));
			ReportInfo i = r.getInfo();

			namedObjects = i.iterators;
			parametersObjects = i.parameters;
			if (namedObjects == null) {
				namedObjects = new Hashtable();
			}

			if (parametersObjects == null) {
				parametersObjects = new Hashtable();
			}

			for (Iterator it = namedObjects.keySet().iterator(); it.hasNext();) {
				String name = (String) it.next();
				fina2.ui.sheet.openoffice.OOIterator iter = (fina2.ui.sheet.openoffice.OOIterator) namedObjects.get(name);

				iter.setSheet(sheet);
				TableRowImpl row = new TableRowImpl(iter, 2);
				row.setValue(0, (String) types.get(iter.getType()));
				row.setValue(1, name);
				selectionTable.addRow(row);
			}

			for (Iterator it = parametersObjects.keySet().iterator(); it.hasNext();) {
				String name = (String) it.next();
				fina2.ui.sheet.openoffice.OOParameter iter = (fina2.ui.sheet.openoffice.OOParameter) parametersObjects.get(name);
				iter.setSheet(sheet);
				TableRowImpl row = new TableRowImpl(iter, 2);
				row.setValue(0, (String) types.get(iter.getType()));
				row.setValue(1, name);
				parametersTable.addRow(row);
			}

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private void loadConf() {
		int x = 0, y = 0, w = 100, h = 200;
		boolean v = false;

		try {
			x = ((Integer) ui.getConfigValue("fina2.report.Designer.x")).intValue();
			y = ((Integer) ui.getConfigValue("fina2.report.Designer.y")).intValue();
			w = ((Integer) ui.getConfigValue("fina2.report.Designer.width")).intValue();
			h = ((Integer) ui.getConfigValue("fina2.report.Designer.height")).intValue();
			v = ((Boolean) ui.getConfigValue("fina2.report.Designer.visible")).booleanValue();
		} catch (Exception e) {
		}

		setLocation(x, y);
		// setLocation(x+10, y+10);
		setSize(w, h);

		if (v)
			main.addToShow(this); // this.show();
	}

	private void initTable() {
		Vector colNames = new Vector();
		colNames.add(ui.getString("fina2.type"));
		colNames.add(ui.getString("fina2.description"));
		Vector rows = new Vector();
		selectionTable.initTable(colNames, rows);
		parametersTable.initTable(colNames, rows);
	}

	private void createNode(Object n, DefaultMutableTreeNode parent, boolean root) {
		if (n instanceof Folder) {
			DefaultMutableTreeNode node = parent;
			if (!root) {
				node = new DefaultMutableTreeNode(n);
				parent.add(node);
			}
			Folder f = (Folder) n;
			for (Iterator iter = f.getChildren().iterator(); iter.hasNext();) {
				createNode(iter.next(), node, false);
			}
		} else {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(n);
			parent.add(node);
		}
	}

	public void placeReturn(fina2.returns.ReturnDefinitionPK pk, String startString, String endString) {
		int vj = 0;
		boolean hasVar = false;

		try {

			Language l = (Language) main.getLanguageHandle().getEJBObject();
			String nformat = l.getNumberFormat();
			java.text.DecimalFormat nf = new java.text.DecimalFormat(nformat);

			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/returns/ReturnSession");
			ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);

			ReturnSession session = home.create();

			Collection defTables = session.getDefinitionTablesFormat(main.getLanguageHandle(), pk);

			int c = sheet.getSelectedCol(); // 1; //Start column
			int j = sheet.getSelectedRow(); // Start row

			/*
			 * sheet.setFontName(j,c,j+4,c,ui.getFont().getName());
			 * sheet.setFontSize(j,c,j+4,c,ui.getFont().getSize());
			 * sheet.setFontWeight(j,c,j+4,c,Spreadsheet.BOLD);
			 * 
			 * 
			 * sheet.setCellValue(j,1, row.getValue(1)); j++;
			 * sheet.setCellValue(j,1, ui.getString("fina2.bank.bank")); j++;
			 * sheet.setCellValue(j,1, ui.getString("fina2.from")); j++;
			 * sheet.setCellValue(j,1, ui.getString("fina2.to")); j++;
			 * sheet.setCellValue(j,1, ui.getString("fina2.status")); j++;
			 * 
			 * j++; j++;
			 */
			vj = j;

			double number = 9999999.00;

			for (Iterator iter = defTables.iterator(); iter.hasNext();) {

				DefinitionTable table = (DefinitionTable) iter.next();

				// sheet.setCellWrap(j,1,true);
				// sheet.setFontName(j,1,j,1,ui.getFont().getName());
				// sheet.setFontSize(j,1,j,1,ui.getFont().getSize()+2);
				// sheet.setFontWeight(j,1,j,1,Spreadsheet.BOLD);

				sheet.setCellWrap(j, c, true);
				sheet.setFontName(j, c, j, c, ui.getFont().getName());
				sheet.setFontSize(j, c, j, c, ui.getFont().getSize() + 2);
				sheet.setFontWeight(j, c, j, c, Spreadsheet.BOLD);

				if (table.getType() != ReturnConstants.TABLETYPE_NORMAL)
					sheet.setCellValue(j, c, table.getNodeName());
				else
					sheet.setCellValue(j, c, table.getCode());

				j++;
				j++;

				Collection rows = session.getReviewTableFormatRows(main.getLanguageHandle(), table.getNode());

				// Normal
				if (table.getType() == ReturnConstants.TABLETYPE_NORMAL) {

					sheet.setCellWrap(j, c + 1, true);
					sheet.setHorizontalAlign(j, c + 1, Spreadsheet.CENTER);
					sheet.setVerticalAlign(j, c + 1, Spreadsheet.CENTER);
					// sheet.setFontName(j,2,j,2,ui.getFont().getName());
					sheet.setFontSize(j, c + 1, j, c + 1, ui.getFont().getSize());
					sheet.setFontWeight(j, c + 1, j, c + 1, Spreadsheet.BOLD);

					sheet.setCellValue(j, c + 1, table.getNodeName());

					ValuesTableRow title = (ValuesTableRow) ((Vector) rows).get(0);
					rows.remove(title);
					// Object[][] data = null;
					// data = new Object[rows.size()][title.getColumnCount()];

					j++;
					int rowN = j;
					int rc = 0;
					// rc++;

					for (Iterator _iter = rows.iterator(); _iter.hasNext();) {
						ValuesTableRow roww = (ValuesTableRow) _iter.next();

						sheet.setCellValue(j, c, roww.getValue(0));
						// data[rc][0]=new String((String)roww.getValue(0));

						/*
						 * if((roww.getDataType(1)==MDTConstants.DATATYPE_NUMERIC
						 * ) ||
						 * (roww.getType(1)==MDTConstants.NODETYPE_VARIABLE)) {
						 * data[rc][1]=new Double(number);
						 * 
						 * } else { //sheet.setCellValue(j,2,roww.getValue(1));
						 * //data[rc][1]=(String)roww.getValue(1);
						 * 
						 * data[rc][1]=new String("text");
						 * 
						 * }
						 */
						sheet.setCellFormula(j, c + 1, startString + "\"" + roww.getCode(1) + "\"" + endString);

						j++;
						rc++;

					}
					// sheet.setDataArray(rowN,1,j-1,2,data);

					sheet.setNumberFormat(rowN, c + 1, j - 1, c + 1, nformat);
					sheet.setCellWrap(rowN, c, j - 1, c, true);

					sheet.setCellWrap(rowN, c + 1, j - 1, c + 1, false);
					sheet.setFontSize(rowN, c, j - 1, c + 1, ui.getFont().getSize());

					sheet.setFontWeight(rowN, c, j - 1, c, Spreadsheet.BOLD);
					sheet.setFontWeight(rowN, c + 1, j - 1, c + 1, Spreadsheet.PLAIN);

					sheet.setBorder(rowN - 1, c, j - 1, c + 1, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, (short) 6);

					j++;
					j++;
				}

				// Multiple
				if (table.getType() == ReturnConstants.TABLETYPE_MULTIPLE) {

					// sheet.setCellValue(j,2,table.getNodeName());

					// j++;
					int rowN = j;
					// int colN = 0;
					int rc = 0;
					ValuesTableRow title = (ValuesTableRow) ((Vector) rows).get(0);
					// Object[][] data = null;
					// data = new Object[rows.size()][title.getColumnCount()];

					rows.remove(title);

					for (int i = 0; i < title.getColumnCount(); i++) {
						sheet.setCellValue(j, c + i, title.getValue(i));

						// data[rc][i]=new String((String)title.getValue(i));
					}
					rc++;

					j++;
					for (Iterator _iter = rows.iterator(); _iter.hasNext();) {
						ValuesTableRow roww = (ValuesTableRow) _iter.next();
						for (int i = 0; i < title.getColumnCount(); i++) {
							// sheet.setCellValue(j,i+1,roww.getValue(i));
							// data[rc][i]=new String((String)roww.getValue(i));

							// sheet.setFontName(j,i+1,ui.getFont().getName());
							// sheet.setFontSize(j,i+1,ui.getFont().getSize());
							// sheet.setFontWeight(j,i+1,Spreadsheet.PLAIN);
							if (i > 0) {

								/*
								 * if((roww.getDataType(i)==MDTConstants.
								 * DATATYPE_NUMERIC) ||
								 * (roww.getType(i)==MDTConstants
								 * .NODETYPE_VARIABLE)) {
								 * //if((!roww.getValue(i).equals("")) &&
								 * (!roww.getValue(i).equals("undefined")) &&
								 * (!roww.getValue(i).equals("Infinity")) &&
								 * (!roww.getValue(i).equals("NaN")) &&
								 * (!roww.getValue(i).equals("X"))) { // try {
								 * // number =
								 * Double.valueOf(roww.getValue(i)).doubleValue
								 * ();
								 * //nf.parse(roww.getValue(i)).doubleValue();
								 * // } catch(Exception e) { // number =
								 * Double.NaN; // } //} else { // number = 0.0;
								 * //nf.parse("0.00").doubleValue(); //}
								 * 
								 * //if(Double.isNaN(number)) { //
								 * data[rc][i]=new
								 * String((String)roww.getValue(i));
								 * //sheet.setCellValue(j,2,new
								 * String((String)roww.getValue(1))); //} else {
								 * // data[rc][i]=new Double(number);
								 * data[rc][i]=new Double(number);
								 * //sheet.setCellNumber(j,2, number);
								 * 
								 * //}
								 * 
								 * } else {
								 * //sheet.setCellValue(j,2,roww.getValue(1));
								 * //data[rc][i]=(String)roww.getValue(i);
								 * data[rc][i]=new String("text"); }
								 */

								sheet.setCellFormula(j, c + i, startString + '"' + roww.getCode(i) + '"' + endString);
							} else {
								// sheet.setFontName(j,i+1,ui.getFont().getName());
								// sheet.setFontSize(j,i+1,ui.getFont().getSize());
								// sheet.setFontWeight(j,i+1,Spreadsheet.PLAIN);

								sheet.setCellValue(j, c, roww.getValue(i));
								// data[rc][i]=(String)roww.getValue(i);
							}

						}
						j++;
						rc++;
					}

					// sheet.setDataArray(rowN,1,j-1,title.getColumnCount(),data);

					sheet.setCellWrap(rowN, c, rowN, c + title.getColumnCount(), true);
					sheet.setHorizontalAlign(rowN, c, rowN, c + title.getColumnCount(), Spreadsheet.CENTER);
					sheet.setVerticalAlign(rowN, c, rowN, c + title.getColumnCount(), Spreadsheet.CENTER);
					// sheet.setFontName(rowN,1,rowN,title.getColumnCount(),ui.getFont().getName());
					sheet.setFontSize(rowN, c, rowN, c + title.getColumnCount(), ui.getFont().getSize());
					sheet.setFontWeight(rowN, c, rowN, c + title.getColumnCount(), Spreadsheet.BOLD);

					// sheet.setFontName(rowN+1,1,j-1,title.getColumnCount(),ui.getFont().getName());
					sheet.setFontSize(rowN + 1, c, j - 1, c + title.getColumnCount(), ui.getFont().getSize());
					sheet.setFontWeight(rowN + 1, c, j - 1, c + title.getColumnCount(), Spreadsheet.PLAIN);
					sheet.setNumberFormat(rowN + 1, c, j - 1, c + title.getColumnCount(), nformat);

					sheet.setBorder(rowN, c, j - 1, c + title.getColumnCount(), sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, (short) 6);
					j++;
					j++;
				}
			}

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

	}

	public void _placeReturn(fina2.returns.ReturnDefinitionPK pk, String startString, String endString) {

		int vj = 0;
		boolean hasVar = false;

		try {

			Language l = (Language) main.getLanguageHandle().getEJBObject();
			String nformat = l.getNumberFormat();
			java.text.DecimalFormat nf = new java.text.DecimalFormat(nformat);

			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/returns/ReturnSession");
			ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);

			ReturnSession session = home.create();

			Collection defTables = session.getDefinitionTablesFormat(main.getLanguageHandle(), pk);

			int c = sheet.getSelectedCol(); // 1; //Start column
			int j = sheet.getSelectedRow(); // Start row
			int jj = 0;
			int i = 0;
			int table_start_r = 1;
			int table_end_r = 1;
			int table_start_c = 1;
			int table_end_c = 1;

			vj = j;

			double number = 9999999.00;

			for (Iterator iter = defTables.iterator(); iter.hasNext();) {
				DefinitionTable table = (DefinitionTable) iter.next();

				sheet.setCellWrap(j, c, true);
				sheet.setFontName(j, c, j, c, ui.getFont().getName());
				sheet.setFontSize(j, c, j, c, ui.getFont().getSize() + 2);
				sheet.setFontWeight(j, c, j, c, Spreadsheet.BOLD);

				if (table.getType() != ReturnConstants.TABLETYPE_NORMAL)
					sheet.setCellValue(j, c, table.getNodeName());
				else
					sheet.setCellValue(j, c, table.getCode());

				j++;
				j++;
				table_start_r = j;
				Collection parents = session.getReviewTableFormatRows(main.getLanguageHandle(), table.getNode());

				// Multiple
				if (table.getType() == ReturnConstants.TABLETYPE_MULTIPLE) {
					sheet.setCellWrap(j, c + 1, true);
					sheet.setHorizontalAlign(j, c + 1, Spreadsheet.CENTER);
					sheet.setVerticalAlign(j, c + 1, Spreadsheet.CENTER);
					sheet.setFontName(j, c + 1, j, c + 1, ui.getFont().getName());
					sheet.setFontSize(j, c + 1, j, c + 1, ui.getFont().getSize());
					sheet.setFontWeight(j, c + 1, j, c + 1, Spreadsheet.BOLD);
					sheet.setCellValue(j, c + 1, table.getNodeName());

					j++;

					table_start_c = c; // 1;
					table_end_c = c + 1; // 2;
					for (Iterator _iter = parents.iterator(); _iter.hasNext();) {
						Node parent = (Node) _iter.next();

						sheet.setCellWrap(table_start_r, table_end_c, true);
						sheet.setHorizontalAlign(table_start_r, table_end_c, Spreadsheet.CENTER);
						sheet.setVerticalAlign(table_start_r, table_end_c, Spreadsheet.CENTER);
						sheet.setFontName(table_start_r, table_end_c, table_start_r, table_end_c, ui.getFont().getName());
						sheet.setFontSize(table_start_r, table_end_c, table_start_r, table_end_c, ui.getFont().getSize());
						sheet.setFontWeight(table_start_r, table_end_c, table_start_r, table_end_c, Spreadsheet.BOLD);
						sheet.setCellValue(table_start_r, table_end_c, parent.getLabel());

						jj = j;
						Vector childs = parent.getChildren();
						for (Iterator _iter_c = childs.iterator(); _iter_c.hasNext();) {
							Node child = (Node) _iter_c.next();
							sheet.setFontName(j, c, j, c, ui.getFont().getName());
							sheet.setFontSize(j, c, j, c, ui.getFont().getSize());
							sheet.setFontWeight(j, c, j, c, Spreadsheet.PLAIN);
							sheet.setCellValue(j, c, child.getLabel());

							sheet.setFontName(j, table_end_c, j, table_end_c, ui.getFont().getName());
							sheet.setFontSize(j, table_end_c, j, table_end_c, ui.getFont().getSize());
							sheet.setFontWeight(j, table_end_c, j, table_end_c, Spreadsheet.PLAIN);

							/*
							 * if(((Integer)child.getProperty("dataType")).intValue
							 * () == MDTConstants.DATATYPE_NUMERIC ||
							 * ((Integer)child.getType()).intValue() ==
							 * MDTConstants.NODETYPE_VARIABLE) {
							 * sheet.setNumberFormat
							 * (j,table_end_c,j,table_end_c,nformat);
							 * sheet.setCellNumber(j, table_end_c, number); }
							 * else { sheet.setCellValue(j, table_end_c,
							 * "text"); }
							 */
							sheet.setCellFormula(j, table_end_c, startString + "\"" + child.getProperty("code") + "\"" + endString);
							j++;
						}

						table_end_c++;
						table_end_r = j;
						j = jj;
					}
					j++;
					j = table_end_r;

					sheet.setBorder(table_start_r, c, table_end_r - 1, table_end_c - 1, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, (short) 6);
				}

				// Normal
				if (table.getType() == ReturnConstants.TABLETYPE_NORMAL) {
					sheet.setCellWrap(j, c + 1, true);
					sheet.setHorizontalAlign(j, c + 1, Spreadsheet.CENTER);
					sheet.setVerticalAlign(j, c + 1, Spreadsheet.CENTER);
					sheet.setFontName(j, c + 1, j, c + 1, ui.getFont().getName());
					sheet.setFontSize(j, c + 1, j, c + 1, ui.getFont().getSize());
					sheet.setFontWeight(j, c + 1, j, c + 1, Spreadsheet.BOLD);
					sheet.setCellValue(j, c + 1, table.getNodeName());
					j++;

					for (Iterator _iter = parents.iterator(); _iter.hasNext();) {
						Node parent = (Node) _iter.next();

						sheet.setCellWrap(j, c, true);
						sheet.setFontName(j, c, ui.getFont().getName());
						sheet.setFontSize(j, c, ui.getFont().getSize());
						sheet.setFontWeight(j, c, Spreadsheet.BOLD);
						sheet.setCellValue(j, c, parent.getLabel());

						sheet.setCellWrap(j, c + 1, j, c + 1, false);
						sheet.setFontName(j, c + 1, j, c + 1, ui.getFont().getName());
						sheet.setFontSize(j, c + 1, j, c + 1, ui.getFont().getSize());
						sheet.setFontWeight(j, c + 1, j, c + 1, Spreadsheet.PLAIN);

						/*
						 * if(((Integer)parent.getProperty("dataType")).intValue(
						 * ) == MDTConstants.DATATYPE_NUMERIC ||
						 * ((Integer)parent.getType()).intValue() ==
						 * MDTConstants.NODETYPE_VARIABLE) {
						 * sheet.setNumberFormat(j,c+1,nformat);
						 * sheet.setCellNumber(j, c+1, number); } else {
						 * sheet.setCellValue(j,c+1,"text"); }
						 */

						sheet.setCellFormula(j, c + 1, startString + "\"" + parent.getProperty("code") + "\"" + endString);
						j++;
						Vector childs = parent.getChildren();
						for (Iterator _iter_c = childs.iterator(); _iter_c.hasNext();) {
							Node child = (Node) _iter_c.next();

							sheet.setFontName(j, c, j, c, ui.getFont().getName());
							sheet.setFontSize(j, c, j, c, ui.getFont().getSize());
							sheet.setFontWeight(j, c, j, c, Spreadsheet.PLAIN);
							sheet.setCellValue(j, c, child.getLabel());

							sheet.setFontName(j, c + 1, j, c + 1, ui.getFont().getName());
							sheet.setFontSize(j, c + 1, j, c + 1, ui.getFont().getSize());
							sheet.setFontWeight(j, c + 1, j, c + 1, Spreadsheet.PLAIN);

							/*
							 * if(((Integer)child.getProperty("dataType")).intValue
							 * () == MDTConstants.DATATYPE_NUMERIC ||
							 * ((Integer)child.getType()).intValue() ==
							 * MDTConstants.NODETYPE_VARIABLE) {
							 * sheet.setNumberFormat(j,c+1,nformat);
							 * sheet.setCellNumber(j, c+1, number); } else {
							 * sheet.setCellValue(j,c+1,"text"); }
							 */
							sheet.setCellFormula(j, c + 1, startString + "\"" + child.getProperty("code") + "\"" + endString);
							j++;

						}
					}

					table_end_r = j - 1;

					sheet.setBorder(table_start_r, c, table_end_r, c + 1, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, (short) 6);

				}

				j++;
				j++;
			}

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() { // GEN-BEGIN:initComponents
		jPanel1 = new javax.swing.JPanel();
		jToolBar1 = new javax.swing.JToolBar();
		jPanel7 = new javax.swing.JPanel();
		jPanel8 = new javax.swing.JPanel();
		headerButton = new javax.swing.JButton();
		footerButton = new javax.swing.JButton();
		jLabel1 = new javax.swing.JLabel();
		nameText = new javax.swing.JTextField();
		saveButton = new javax.swing.JButton();
		saveAsButton = new javax.swing.JButton();
		jPanel4 = new javax.swing.JPanel();
		jPanel9 = new javax.swing.JPanel();
		placeReturnButton = new javax.swing.JButton();
		formulaParamsButton = new javax.swing.JButton();
		splitPane = new javax.swing.JSplitPane();
		tabbedPanel = new javax.swing.JTabbedPane();
		iteratorsPanel = new javax.swing.JPanel();
		jPanel5 = new javax.swing.JPanel();
		createIteratorButton = new javax.swing.JButton();
		amendIteratorButton = new javax.swing.JButton();
		deleteIteratorButton = new javax.swing.JButton();
		selectionScrollPane = new javax.swing.JScrollPane();
		formulasPanel = new javax.swing.JPanel();
		jPanel6 = new javax.swing.JPanel();
		newRepFormulaButton = new javax.swing.JButton();
		amendRepFormulaButton = new javax.swing.JButton();
		deleteRepFormulaButton = new javax.swing.JButton();
		repFolderButton = new javax.swing.JButton();
		formulasTreePanel = new RepositoryFormulasPanel();
		parametersPanel = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		createParameterButton = new javax.swing.JButton();
		amendParameterButton = new javax.swing.JButton();
		deleteParameterButton = new javax.swing.JButton();
		parametersScrollPane = new javax.swing.JScrollPane();
		textFildBorder = nameText.getBorder();

		setTitle(ui.getString("fina2.report.reportDesigner"));
		addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent evt) {
				formComponentResized(evt);
			}

			public void componentMoved(java.awt.event.ComponentEvent evt) {
				formComponentMoved(evt);
			}

			public void componentShown(java.awt.event.ComponentEvent evt) {
				formComponentShown(evt);
			}

			public void componentHidden(java.awt.event.ComponentEvent evt) {
				formComponentHidden(evt);
			}
		});
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exitForm(evt);
			}
		});

		jPanel1.setLayout(new java.awt.BorderLayout());

		jPanel1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(4, 5, 4, 5)));
		headerButton.setFont(ui.getFont());
		headerButton.setText(ui.getString("fina2.report.setResetHeader"));
		headerButton.setMargin(new java.awt.Insets(1, 5, 1, 5));
		headerButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				headerButtonActionPerformed(evt);
			}
		});

		jPanel8.add(headerButton);

		footerButton.setFont(ui.getFont());
		footerButton.setText(ui.getString("fina2.report.setResetFooter"));
		footerButton.setMargin(new java.awt.Insets(1, 5, 1, 5));
		footerButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				footerButtonActionPerformed(evt);
			}
		});

		jPanel8.add(footerButton);

		jPanel7.add(jPanel8);

		jLabel1.setText("name");
		jLabel1.setFont(new java.awt.Font("Dialog", 0, 11));
		jPanel7.add(jLabel1);

		nameText.setColumns(12);
		nameText.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!ui.isValidReportName(nameText.getText())) {
					if (errorWindow == null) {
						errorWindow = ui.showErrorWindow(jPanel7, errorString, nameText.getLocationOnScreen());
					} else {
						if (!errorWindow.isVisible()) {
							errorWindow = ui.showErrorWindow(jPanel7, errorString, nameText.getLocationOnScreen());
						}
					}
					nameText.setBorder(BorderFactory.createLineBorder(Color.RED));
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				nameText.setBorder(textFildBorder);
				if (errorWindow != null) {
					errorWindow.dispose();
				}
			}
		});
		jPanel7.add(nameText);

		saveButton.setFont(ui.getFont());
		saveButton.setText(ui.getString("fina2.report.save"));
		saveButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if (ui.isValidReportName(nameText.getText())) {
					saveButtonActionPerformed(evt);
					if (errorWindow != null) {
						errorWindow.dispose();
					}
				} else {
					if (errorWindow == null) {
						errorWindow = ui.showErrorWindow(jPanel7, errorString, nameText.getLocationOnScreen());
					} else {
						if (!errorWindow.isVisible()) {
							errorWindow = ui.showErrorWindow(jPanel7, errorString, nameText.getLocationOnScreen());
						}
					}
				}

				// if (ui.isNameValid(nameText.getText())) {
				// saveButtonActionPerformed(evt);
				// } else {
				// ui.showMessageBox(null, "Error saving report name",
				// " The name of report must contain only letters, '_' and/or digits !",
				// JOptionPane.ERROR_MESSAGE);
				// }
			}
		});

		jPanel7.add(saveButton);

		saveAsButton.setFont(ui.getFont());
		saveAsButton.setText(ui.getString("fina2.report.saveAs"));
		saveAsButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if (ui.isValidReportName(nameText.getText())) {
					saveAsButtonActionPerformed(evt);
					if (errorWindow != null) {
						errorWindow.dispose();
					}
				} else {
					if (errorWindow == null) {
						errorWindow = ui.showErrorWindow(jPanel7, errorString, nameText.getLocationOnScreen());
					} else {
						if (!errorWindow.isVisible()) {
							errorWindow = ui.showErrorWindow(jPanel7, errorString, nameText.getLocationOnScreen());
						}
					}
				}
				// if (ui.isNameValid(nameText.getText())) {
				// saveAsButtonActionPerformed(evt);
				// } else {
				// ui.showMessageBox(null, "Error saving report name",
				// " The name of report must contain only letters, '_' and/or digits !",
				// JOptionPane.ERROR_MESSAGE);
				// }
			}
		});

		jPanel7.add(saveAsButton);
		jPanel7.add(jPanel4);

		placeReturnButton.setFont(ui.getFont());
		placeReturnButton.setText(ui.getString("fina2.reportoo.placeReturn"));
		placeReturnButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				placeReturnButtonActionPerformed(evt);
			}
		});

		jPanel7.add(placeReturnButton);
		jPanel7.add(jPanel9);

		formulaParamsButton.setFont(ui.getFont());
		formulaParamsButton.setIcon(ui.getIcon("fina2.reportoo.formula"));
		formulaParamsButton.setText(ui.getString("fina2.reportoo.insertUpdateRepositoryFormula"));
		formulaParamsButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				formulaParamsButtonActionPerformed(evt);
			}
		});

		jPanel7.add(formulaParamsButton);

		jToolBar1.add(jPanel7);

		jPanel1.add(jToolBar1, java.awt.BorderLayout.CENTER);

		getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

		splitPane.setDividerSize(4);
		tabbedPanel.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
		tabbedPanel.setFont(ui.getFont());
		iteratorsPanel.setLayout(new java.awt.BorderLayout());

		createIteratorButton.setIcon(ui.getIcon("fina2.new"));
		createIteratorButton.setToolTipText("Create");
		createIteratorButton.setFont(ui.getFont());
		createIteratorButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		createIteratorButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				createIteratorButtonActionPerformed(evt);
			}
		});

		jPanel5.add(createIteratorButton);

		amendIteratorButton.setIcon(ui.getIcon("fina2.amend"));
		amendIteratorButton.setFont(ui.getFont());
		amendIteratorButton.setToolTipText("Amend");
		amendIteratorButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		amendIteratorButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				amendIteratorButtonActionPerformed(evt);
			}
		});

		jPanel5.add(amendIteratorButton);

		deleteIteratorButton.setIcon(ui.getIcon("fina2.delete"));
		deleteIteratorButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		deleteIteratorButton.setFont(ui.getFont());
		deleteIteratorButton.setToolTipText("Delete");
		deleteIteratorButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				deleteIteratorButtonActionPerformed(evt);
			}
		});

		jPanel5.add(deleteIteratorButton);

		iteratorsPanel.add(jPanel5, java.awt.BorderLayout.SOUTH);

		iteratorsPanel.add(selectionScrollPane, java.awt.BorderLayout.CENTER);

		tabbedPanel.addTab("Iterators", null, iteratorsPanel, "");

		formulasPanel.setLayout(new java.awt.BorderLayout());

		newRepFormulaButton.setIcon(ui.getIcon("fina2.new"));
		newRepFormulaButton.setToolTipText("Create");
		newRepFormulaButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		newRepFormulaButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				newRepFormulaButtonActionPerformed(evt);
			}
		});

		jPanel6.add(newRepFormulaButton);

		amendRepFormulaButton.setIcon(ui.getIcon("fina2.amend"));
		amendRepFormulaButton.setToolTipText("Amend");
		amendRepFormulaButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		amendRepFormulaButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				amendRepFormulaButtonActionPerformed(evt);
			}
		});

		jPanel6.add(amendRepFormulaButton);

		deleteRepFormulaButton.setIcon(ui.getIcon("fina2.delete"));
		deleteRepFormulaButton.setToolTipText("Delete");
		deleteRepFormulaButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		deleteRepFormulaButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				deleteRepFormulaButtonActionPerformed(evt);
			}
		});

		jPanel6.add(deleteRepFormulaButton);

		repFolderButton.setIcon(ui.getIcon("fina2.folder"));
		repFolderButton.setToolTipText("New Folder");
		repFolderButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		repFolderButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				repFolderButtonActionPerformed(evt);
			}
		});

		jPanel6.add(repFolderButton);

		formulasPanel.add(jPanel6, java.awt.BorderLayout.SOUTH);

		formulasPanel.add(formulasTreePanel, java.awt.BorderLayout.CENTER);

		tabbedPanel.addTab("Formulas", null, formulasPanel, "");

		parametersPanel.setLayout(new java.awt.BorderLayout());

		parametersPanel.setFont(ui.getFont());
		createParameterButton.setIcon(ui.getIcon("fina2.new"));
		createParameterButton.setToolTipText("Create");
		createParameterButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		createParameterButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				createParameterButtonActionPerformed(evt);
			}
		});

		jPanel3.add(createParameterButton);

		amendParameterButton.setIcon(ui.getIcon("fina2.amend"));
		amendParameterButton.setToolTipText("Amend");
		amendParameterButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		amendParameterButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				amendParameterButtonActionPerformed(evt);
			}
		});

		jPanel3.add(amendParameterButton);

		deleteParameterButton.setIcon(ui.getIcon("fina2.delete"));
		deleteParameterButton.setToolTipText("Delete");
		deleteParameterButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		deleteParameterButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				deleteParameterButtonActionPerformed(evt);
			}
		});

		jPanel3.add(deleteParameterButton);

		parametersPanel.add(jPanel3, java.awt.BorderLayout.SOUTH);

		parametersPanel.add(parametersScrollPane, java.awt.BorderLayout.CENTER);

		tabbedPanel.addTab("Parameters", null, parametersPanel, "");

		tabbedPanel.setMaximumSize(new Dimension(200, tabbedPanel.getHeight()));

		tabbedPanel.setPreferredSize(new Dimension(200, tabbedPanel.getHeight()));

		splitPane.setRightComponent(tabbedPanel);

		splitPane.setOneTouchExpandable(true);

		getContentPane().add(splitPane, java.awt.BorderLayout.CENTER);

	} // GEN-END:initComponents

	private void saveAsButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_saveAsButtonActionPerformed
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			sheet.write(bo);
			byte[] template = bo.toByteArray();
			bo.close();

			if (template == null || template.length == 0) {
				ui.showMessageBox(this, ui.getString("fina2.reportoo.CouldNotSaveReport"));
				return;
			}

			InitialContext ctx = fina2.Main.getJndiContext();

			Object ref = ctx.lookup("fina2/reportoo/server/Report");
			ReportHome home = (ReportHome) PortableRemoteObject.narrow(ref, ReportHome.class);

			Report r = home.findByPrimaryKey(reportPK);

			parentPK = r.getParentPK();
			SelectFolderDialog dlg = new SelectFolderDialog(this, true);
			dlg.show(parentPK);
			if (dlg.getTableRow() == null)
				return;
			parentPK = (ReportPK) dlg.getTableRow().getPrimaryKey();
			reportPK = null;

			create();
			// node = (DefaultMutableTreeNode)node.getParent();
			tree.gotoNode(parentPK);
			node = tree.getSelectedTreeNode();
			saveButtonActionPerformed(evt);

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	} // GEN-LAST:event_saveAsButtonActionPerformed

	private void placeReturnButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_placeReturnButtonActionPerformed
		PlaceReturnDialog dlg = new PlaceReturnDialog(this, true);
		dlg.setLocationRelativeTo(this);
		dlg.show();
	} // GEN-LAST:event_placeReturnButtonActionPerformed

	private void footerButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_footerButtonActionPerformed
		int r = sheet.getSelectedRow();
		footerRow = r;
		if (r == 0)
			footerRow = 5000;
		sheet.setFooter(r);
	} // GEN-LAST:event_footerButtonActionPerformed

	private void headerButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_headerButtonActionPerformed
		int r = sheet.getSelectedRow();
		headerRow = r;
		sheet.setHeader(r);
	} // GEN-LAST:event_headerButtonActionPerformed

	private void amendParameterButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_amendParameterButtonActionPerformed

		TableRow row = parametersTable.getSelectedTableRow();

		if (row.getPrimaryKey() instanceof fina2.ui.sheet.openoffice.OOParameter) {
			ParameterWizard wiz = new ParameterWizard(this, true);

			fina2.ui.sheet.openoffice.OOParameter iter = (fina2.ui.sheet.openoffice.OOParameter) row.getPrimaryKey();

			String oldName = iter.getName();
			wiz.show(iter, parametersTable.getRows(), sheet);
			row.setValue(0, (String) types.get(iter.getType()));
			row.setValue(1, iter.getName());

			namedObjects.remove(oldName);
			parametersObjects.put(iter.getName(), iter);
		}

	} // GEN-LAST:event_amendParameterButtonActionPerformed

	private void deleteParameterButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_deleteParameterButtonActionPerformed
		TableRow row = parametersTable.getSelectedTableRow();
		if (row.getPrimaryKey() instanceof fina2.ui.sheet.openoffice.OOParameter) {
			fina2.ui.sheet.openoffice.OOParameter iter = (fina2.ui.sheet.openoffice.OOParameter) row.getPrimaryKey();
			iter.remove();
		}
		parametersObjects.remove(row.getValue(1));
		parametersTable.removeRow(parametersTable.getSelectedRow());
	} // GEN-LAST:event_deleteParameterButtonActionPerformed

	private void createParameterButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_createParameterButtonActionPerformed

		ParameterWizard wiz = new ParameterWizard(this, true);
		fina2.ui.sheet.openoffice.OOParameter iter = null;
		wiz.show(iter, parametersTable.getRows(), sheet);
		if (wiz.Ok()) {
			iter = wiz.getIterator();
			/*
			 * if(iter.getOrientation()==iter.COL_ITERATOR) { start_cr =
			 * sheet.getStartSelCol(); end_cr = sheet.getEndSelCol(); }
			 * if(iter.getOrientation()==iter.ROW_ITERATOR) { start_cr =
			 * sheet.getStartSelRow(); end_cr = sheet.getEndSelRow(); }
			 * iter.setCoordinate(sheet, start_cr, end_cr);
			 */
			TableRowImpl row = new TableRowImpl(iter, 2);
			// row.setValue(0, ui.getString("fina2.reportoo.iterator"));
			row.setValue(0, (String) types.get(iter.getType()));
			row.setValue(1, iter.getName());
			parametersTable.addRow(row);
			parametersObjects.put(iter.getName(), iter);

		}
	} // GEN-LAST:event_createParameterButtonActionPerformed

	private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_saveButtonActionPerformed

		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			sheet.write(bo);
			byte[] template = bo.toByteArray();
			bo.close();

			if (template == null || template.length == 0) {
				ui.showMessageBox(this, ui.getString("fina2.reportoo.CouldNotSaveReport"));
				return;
			}
			boolean isAmend = true;
			if (reportPK == null) {
				// Create new report
				create();
				isAmend = false;
			}

			InitialContext ctx = fina2.Main.getJndiContext();
			Object ref = ctx.lookup("fina2/reportoo/server/Report");
			ReportHome home = (ReportHome) PortableRemoteObject.narrow(ref, ReportHome.class);

			Report report = home.findByPrimaryKey(reportPK);

			// Set type
			report.setType(ReportConstants.NODETYPE_REPORT);

			ReportInfo info = new ReportInfo();
			info.iterators = namedObjects;
			info.parameters = parametersObjects;
			info.header = sheet.getHeader();
			info.footer = sheet.getFooter();
			if (info.footer == 0) {
				info.footer = 5000;
			}
			boolean nameExists = true;
			// Set report info
			report.setInfo(info);

			// Set template
			report.setTemplate((LanguagePK) main.getLanguageHandle().getEJBObject().getPrimaryKey(), template);

			try {
				// Set description
				report.setDescription(main.getLanguageHandle(), nameText.getText());
			} catch (Exception e) {
				e.printStackTrace();
				nameExists = false;

				if (!isAmend) {
					home.remove(reportPK);
					reportPK = null;
				}
				ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.report.exists"));

			}

			if (nameExists) {
				if (parentPK == null) {
					Node n = (Node) node.getUserObject();
					n.setLabel(nameText.getText());
					((DefaultTreeModel) tree.getModel()).nodeChanged(node);
				} else {
					Node newNode = new Node(report.getPrimaryKey(), nameText.getText(), new Integer(ReportConstants.NODETYPE_REPORT));

					DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(node);
					newTreeNode.setUserObject(newNode);

					((DefaultTreeModel) tree.getModel()).insertNodeInto(newTreeNode, node, node.getChildCount());

					parentPK = null;
					tree.scrollPathToVisible(new TreePath(newTreeNode.getPath()));
					node = newTreeNode;
				}
			}
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

	} // GEN-LAST:event_saveButtonActionPerformed

	private void amendIteratorButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_amendIteratorButtonActionPerformed
		TableRow row = selectionTable.getSelectedTableRow();
		if (row.getPrimaryKey() instanceof fina2.ui.sheet.openoffice.OOIterator) {
			IteratorWizard wiz = new IteratorWizard(this, true);
			fina2.ui.sheet.openoffice.OOIterator iter = (fina2.ui.sheet.openoffice.OOIterator) row.getPrimaryKey();
			String oldName = iter.getName();
			wiz.show(iter, selectionTable.getRows(), parametersObjects.values(), sheet);
			row.setValue(0, (String) types.get(iter.getType()));
			row.setValue(1, iter.getName());
			namedObjects.remove(oldName);
			namedObjects.put(iter.getName(), iter);
		}
		/*
		 * if(row.getPrimaryKey() instanceof fina2.ui.sheet.openoffice.OOTable)
		 * { TableWizard wiz = new TableWizard(this, true);
		 * fina2.ui.sheet.openoffice.OOTable table =
		 * (fina2.ui.sheet.openoffice.OOTable)row.getPrimaryKey();
		 * wiz.show(table, selectionTable.getRows(), sheet); row.setValue(1,
		 * table.getName()); namedObjects.put(table,table);
		 * sheet.setCellFormula(sheet.getStartSelRow(), sheet.getEndSelCol(),
		 * wiz.getFormulaText()); }
		 */
		// System.out.println(namedObjects);
	} // GEN-LAST:event_amendIteratorButtonActionPerformed

	private void deleteIteratorButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_deleteIteratorButtonActionPerformed
		TableRow row = selectionTable.getSelectedTableRow();
		if (row.getPrimaryKey() instanceof fina2.ui.sheet.openoffice.OOIterator) {
			fina2.ui.sheet.openoffice.OOIterator iter = (fina2.ui.sheet.openoffice.OOIterator) row.getPrimaryKey();
			iter.remove();
		}
		namedObjects.remove(row.getValue(1));
		selectionTable.removeRow(selectionTable.getSelectedRow());
		// System.out.println(namedObjects);
	} // GEN-LAST:event_deleteIteratorButtonActionPerformed

	private void createIteratorButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_createIteratorButtonActionPerformed
		int start_cr = 0, end_cr = 0;
		// selDialog = new SelectTypeDialog(this, true);
		// selDialog.show(null);
		// if(selDialog.getType()==1) {
		IteratorWizard wiz = new IteratorWizard(this, true);
		fina2.ui.sheet.openoffice.OOIterator iter = null;
		wiz.show(iter, selectionTable.getRows(), parametersObjects.values(), sheet);
		if (wiz.Ok()) {
			iter = wiz.getIterator();
			if (iter.getOrientation() == iter.COL_ITERATOR) {
				start_cr = sheet.getStartSelCol();
				end_cr = sheet.getEndSelCol();
			}
			if (iter.getOrientation() == iter.ROW_ITERATOR) {
				start_cr = sheet.getStartSelRow();
				end_cr = sheet.getEndSelRow();
			}
			iter.setCoordinate(sheet, start_cr, end_cr);
			TableRowImpl row = new TableRowImpl(iter, 2);
			// row.setValue(0, ui.getString("fina2.reportoo.iterator"));
			row.setValue(0, (String) types.get(iter.getType()));
			row.setValue(1, iter.getName());
			selectionTable.addRow(row);
			namedObjects.put(iter.getName(), iter);
		}
		// }
		/*
		 * if(selDialog.getType()==2) { TableWizard wiz = new TableWizard(this,
		 * true); //table.create(sheet, "", "", sheet.getStartSelCol(),
		 * sheet.getEndSelRow()); fina2.ui.sheet.openoffice.OOTable table =
		 * null; wiz.show(table, selectionTable.getRows(), sheet); if(wiz.Ok())
		 * { table = wiz.getTable(); TableRowImpl row = new TableRowImpl(table,
		 * 2); row.setValue(0, ui.getString("fina2.reportoo.table"));
		 * row.setValue(1, table.getName()); selectionTable.addRow(row);
		 * namedObjects.put(table,table);
		 * sheet.setCellFormula(sheet.getStartSelRow(), sheet.getEndSelCol(),
		 * wiz.getFormulaText()); }
		 * 
		 * 
		 * }
		 */
		// System.out.println(namedObjects);
	} // GEN-LAST:event_createIteratorButtonActionPerformed

	private void formComponentHidden(java.awt.event.ComponentEvent evt) { // GEN-FIRST:event_formComponentHidden
		ui.putConfigValue("fina2.report.Designer.visible", new Boolean(false));
	} // GEN-LAST:event_formComponentHidden

	private void formComponentShown(java.awt.event.ComponentEvent evt) { // GEN-FIRST:event_formComponentShown
		ui.putConfigValue("fina2.report.Designer.visible", new Boolean(true));
	} // GEN-LAST:event_formComponentShown

	private void formComponentMoved(java.awt.event.ComponentEvent evt) { // GEN-FIRST:event_formComponentMoved
		ui.putConfigValue("fina2.report.Designer.x", new Integer(getX()));
		ui.putConfigValue("fina2.report.Designer.y", new Integer(getY()));
	} // GEN-LAST:event_formComponentMoved

	private void formComponentResized(java.awt.event.ComponentEvent evt) { // GEN-FIRST:event_formComponentResized
		ui.putConfigValue("fina2.report.Designer.width", new Integer(getWidth()));
		ui.putConfigValue("fina2.report.Designer.height", new Integer(getHeight()));
	} // GEN-LAST:event_formComponentResized

	private Formula parseFormula(String s) {
		try {
			String sid = s.substring(s.indexOf('(') + 1, s.indexOf(';')).trim();

			int id = Integer.valueOf(sid).intValue();

			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/reportoo/repository/RepositorySession");
			fina2.reportoo.repository.RepositorySessionHome home = (fina2.reportoo.repository.RepositorySessionHome) PortableRemoteObject.narrow(ref,
					fina2.reportoo.repository.RepositorySessionHome.class);

			fina2.reportoo.repository.RepositorySession session = home.create();

			return session.findFormula(id);
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		return null;
	}

	private Hashtable parseFormulaParams(String s) {
		Hashtable h = new Hashtable();
		String sv = s.substring(s.indexOf('"') + 1, s.lastIndexOf('"')).trim();
		StringTokenizer st = new StringTokenizer(sv, "=;");
		while (true) {
			if (!st.hasMoreTokens())
				break;
			String name = st.nextToken();

			if (!st.hasMoreTokens())
				break;
			String value = st.nextToken();
			h.put(name, value);
		}
		return h;
	}

	private void formulaParamsButtonActionPerformed(java.awt.event.ActionEvent evt) {

		ParametersDialog dlg = new ParametersDialog(this, true);
		dlg.setLocationRelativeTo(this);

		String formulaStr = sheet.getCellFormula(sheet.getSelectedRow(), sheet.getSelectedCol());
		if (formulaStr.startsWith("=com.sun.star.sheet.addin.CalcAddins.repository(") && formulaStr.trim().endsWith("\")")) {
			dlg.setFormula(parseFormula(formulaStr), parseFormulaParams(formulaStr));
		}

		dlg.show();
		if (dlg.isOk()) {
			sheet.setCellFormula(sheet.getSelectedRow(), sheet.getSelectedCol(), "=REPOSITORY(" + dlg.getFormula().getId() + ";\"" + dlg.getParameters() + "\")");
		}
	}

	private void repTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {
		DefaultMutableTreeNode node = null;
		TreePath path = formulasTreePanel.getFormulasTree().getSelectionPath();
		if (path != null)
			node = (DefaultMutableTreeNode) path.getLastPathComponent();

		if ((node == null) || (node.getUserObject() instanceof Folder)) {
			repFolderButton.setEnabled(true);
		} else {
			repFolderButton.setEnabled(false);
		}

		if ((node != null) && (node.getUserObject() instanceof Folder)) {
			newRepFormulaButton.setEnabled(true);
			amendRepFormulaButton.setEnabled(true);
		} else {
			newRepFormulaButton.setEnabled(false);
			amendRepFormulaButton.setEnabled(false);
		}

		if ((node != null) && (node.getUserObject() instanceof Formula)) {
			amendRepFormulaButton.setEnabled(true);
		} else {
			// amendRepFormulaButton.setEnabled(false);
		}

		if ((node != null) && (node != formulasTreePanel.getFormulasTree().getModel().getRoot()))
			deleteRepFormulaButton.setEnabled(true);
		else
			deleteRepFormulaButton.setEnabled(false);
	}

	private void deleteRepFormulaButtonActionPerformed(ActionEvent evt) {

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) formulasTreePanel.getFormulasTree().getSelectionPath().getLastPathComponent();
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/reportoo/repository/RepositorySession");
			fina2.reportoo.repository.RepositorySessionHome home = (fina2.reportoo.repository.RepositorySessionHome) PortableRemoteObject.narrow(ref,
					fina2.reportoo.repository.RepositorySessionHome.class);

			fina2.reportoo.repository.RepositorySession session = home.create();

			if (node.getUserObject() instanceof Folder) {
				session.deleteFolder(((Folder) node.getUserObject()).getId());
			} else {
				session.deleteFormula(((Formula) node.getUserObject()).getId());
			}
			((DefaultTreeModel) formulasTreePanel.getFormulasTree().getModel()).removeNodeFromParent(node);
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	} // GEN-LAST:event_deleteRepFormulaButtonActionPerformed

	private void amendRepFormulaButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_amendRepFormulaButtonActionPerformed
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) formulasTreePanel.getFormulasTree().getSelectionPath().getLastPathComponent();

		if (node.getUserObject() instanceof Folder) {
			int id = ((Folder) node.getUserObject()).getId();
			try {
				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi.lookup("fina2/reportoo/repository/RepositorySession");
				fina2.reportoo.repository.RepositorySessionHome home = (fina2.reportoo.repository.RepositorySessionHome) PortableRemoteObject.narrow(ref,
						fina2.reportoo.repository.RepositorySessionHome.class);

				fina2.reportoo.repository.RepositorySession session = home.create();

				folderDialog.show(id);
				if (!folderDialog.isOk())
					return;

				session.setFolderName(id, folderDialog.getName());

				((Folder) node.getUserObject()).setName(folderDialog.getName());
				DefaultTreeModel model = (DefaultTreeModel) formulasTreePanel.getFormulasTree().getModel();
				model.nodeChanged(node);
			} catch (Exception e) {
				Main.generalErrorHandler(e);
			}
			return;
		}

		formulaDialog = new FormulaAmendDialog(this, true);
		formulaDialog.setLocationRelativeTo(this);
		int id = ((Formula) node.getUserObject()).getId();
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/reportoo/repository/RepositorySession");
			fina2.reportoo.repository.RepositorySessionHome home = (fina2.reportoo.repository.RepositorySessionHome) PortableRemoteObject.narrow(ref,
					fina2.reportoo.repository.RepositorySessionHome.class);

			fina2.reportoo.repository.RepositorySession session = home.create();

			formulaDialog.setFormula(session.findFormula(id));

			formulaDialog.show();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		if (!formulaDialog.isOk())
			return;

		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/reportoo/repository/RepositorySession");
			fina2.reportoo.repository.RepositorySessionHome home = (fina2.reportoo.repository.RepositorySessionHome) PortableRemoteObject.narrow(ref,
					fina2.reportoo.repository.RepositorySessionHome.class);

			fina2.reportoo.repository.RepositorySession session = home.create();

			Formula f = formulaDialog.getFormula();
			f.setId(id);
			session.updateFormula(id, f);

			node.setUserObject(f);
			DefaultTreeModel model = (DefaultTreeModel) formulasTreePanel.getFormulasTree().getModel();
			model.nodeChanged(node);
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	} // GEN-LAST:event_amendRepFormulaButtonActionPerformed

	private void repFolderButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_repFolderButtonActionPerformed
		try {
			folderDialog.show(-1);
			if (!folderDialog.isOk())
				return;

			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/reportoo/repository/RepositorySession");
			fina2.reportoo.repository.RepositorySessionHome home = (fina2.reportoo.repository.RepositorySessionHome) PortableRemoteObject.narrow(ref,
					fina2.reportoo.repository.RepositorySessionHome.class);

			fina2.reportoo.repository.RepositorySession session = home.create();

			Folder f = new Folder(folderDialog.getName()); // "Folder");
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) formulasTreePanel.getFormulasTree().getModel().getRoot();
			TreePath path = formulasTreePanel.getFormulasTree().getSelectionPath();
			if (path != null)
				parent = (DefaultMutableTreeNode) path.getLastPathComponent();

			int parentID = ((Folder) parent.getUserObject()).getId();

			int id = session.createFolder(f.getName(), parentID);
			f.setId(id);

			DefaultMutableTreeNode node = new DefaultMutableTreeNode(f);

			DefaultTreeModel model = (DefaultTreeModel) formulasTreePanel.getFormulasTree().getModel();

			model.insertNodeInto(node, parent, parent.getChildCount());
			formulasTreePanel.getFormulasTree().setSelectionPath(new TreePath(node.getPath()));
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	} // GEN-LAST:event_repFolderButtonActionPerformed

	private void newRepFormulaButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_newRepFormulaButtonActionPerformed
		formulaDialog = new FormulaAmendDialog(this, true);
		formulaDialog.setLocationRelativeTo(this);
		formulaDialog.setFormula(new Formula());
		formulaDialog.show();
		if (!formulaDialog.isOk())
			return;

		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/reportoo/repository/RepositorySession");
			fina2.reportoo.repository.RepositorySessionHome home = (fina2.reportoo.repository.RepositorySessionHome) PortableRemoteObject.narrow(ref,
					fina2.reportoo.repository.RepositorySessionHome.class);

			fina2.reportoo.repository.RepositorySession session = home.create();

			Formula f = formulaDialog.getFormula();

			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) formulasTreePanel.getFormulasTree().getModel().getRoot();
			TreePath path = formulasTreePanel.getFormulasTree().getSelectionPath();
			if (path != null)
				parent = (DefaultMutableTreeNode) path.getLastPathComponent();

			int parentID = ((Folder) parent.getUserObject()).getId();

			int id = session.createFormula(f, parentID);
			f.setId(id);

			DefaultMutableTreeNode node = new DefaultMutableTreeNode(f);
			DefaultTreeModel model = (DefaultTreeModel) formulasTreePanel.getFormulasTree().getModel();
			model.insertNodeInto(node, parent, parent.getChildCount());
			formulasTreePanel.getFormulasTree().setSelectionPath(new TreePath(node.getPath()));

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	} // GEN-LAST:event_newRepFormulaButtonActionPerformed

	/** Exit the Application */
	private void exitForm(java.awt.event.WindowEvent evt) { // GEN-FIRST:event_exitForm
		sheet.dispose();
		dispose();
	} // GEN-LAST:event_exitForm

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.border.Border textFildBorder;
	private javax.swing.JButton amendIteratorButton;
	private javax.swing.JButton amendParameterButton;
	private javax.swing.JButton amendRepFormulaButton;
	private javax.swing.JButton createIteratorButton;
	private javax.swing.JButton createParameterButton;
	private javax.swing.JButton deleteIteratorButton;
	private javax.swing.JButton deleteParameterButton;
	private javax.swing.JButton deleteRepFormulaButton;
	private javax.swing.JButton footerButton;
	private javax.swing.JButton formulaParamsButton;
	private RepositoryFormulasPanel formulasTreePanel;
	private javax.swing.JPanel formulasPanel;
	private javax.swing.JButton headerButton;
	private javax.swing.JPanel iteratorsPanel;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel9;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JToolBar jToolBar1;
	private javax.swing.JTextField nameText;
	private javax.swing.JButton newRepFormulaButton;
	private javax.swing.JPanel parametersPanel;
	private javax.swing.JScrollPane parametersScrollPane;
	private javax.swing.JButton placeReturnButton;
	private javax.swing.JButton repFolderButton;
	private javax.swing.JButton saveAsButton;
	private javax.swing.JButton saveButton;
	private javax.swing.JScrollPane selectionScrollPane;
	private javax.swing.JSplitPane splitPane;
	private javax.swing.JTabbedPane tabbedPanel;

}
