package fina2.ui.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.UIManager;

import fina2.Main;
import fina2.metadata.MDTConstants;
import fina2.metadata.MDTNodePK;
import fina2.metadata.MDTSession;
import fina2.metadata.MDTSessionHome;
import fina2.ui.UIManager.IndeterminateLoading;
import fina2.ui.sheet.Spreadsheet;
import fina2.ui.sheet.SpreadsheetsManager;

@SuppressWarnings("serial")
public class TreeReviewFrame extends javax.swing.JFrame {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private EJBTree tree;

	private Spreadsheet sheet;

	private ArrayList<ArrayList<String>> all = new ArrayList<ArrayList<String>>();
	private ArrayList<String> rows = null;

	private int height;
	private int width;

	private String frameTitle;

	private IndeterminateLoading loading;

	/** Creates new form ReturnTypesFrame */
	public void show(final String frameTitle, final EJBTree tree) {
		this.frameTitle = frameTitle;
		this.tree = tree;
		loading = ui.createIndeterminateLoading(main.getMainFrame());
		Thread threadShow = new Thread() {
			public void run() {
				loading.start();
				Object[][] data = getPrintData();

				sheet = SpreadsheetsManager.getInstance().createSpreadsheet(frameTitle);

				loading.stop();
				initSheet(data, 1, 1);
			};
		};
		threadShow.start();
	}

	private void initSheet(Object[][] data, int x, int y) {
		sheet.showGrid(false);
		sheet.setViewMode(Spreadsheet.VIEW_SIMPLE);
		sheet.setFontName(x, y, x + height, y + width, ui.getFont().getName());

		sheet.setFontSize(x, y, x + 1, y + 1, ui.getFont().getSize() + 2);
		sheet.setFontWeight(x, y, x + 1, y + 1, Spreadsheet.BOLD);

		sheet.setFontSize(x + 2, y, x + 2, y + width, ui.getFont().getSize() + 1);
		sheet.setFontWeight(x + 2, y, x + 2, y + width, Spreadsheet.BOLD);

		sheet.setBorder(x + 2, y, height, width, Spreadsheet.LINE_YES, Spreadsheet.LINE_YES, Spreadsheet.LINE_YES, Spreadsheet.LINE_YES, Spreadsheet.LINE_YES, Spreadsheet.LINE_YES, (short) 1);

		// Set Data!
		sheet.setDataArray(x, y, height, width, data);
		sheet.setFontSize(x + 3, y, x + height, y + width, ui.getFont().getSize() - 1);
		sheet.setFontWeight(x + 3, y, x + height, y + width, Spreadsheet.PLAIN);
		sheet.setOptimalColWidth(x, 100);
		sheet.setViewMode(Spreadsheet.VIEW_FULL);
	}

	private Object[][] getPrintData() {
		Node node = tree.getSelectedNode();
		frameTitle = node.getLabel();
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/metadata/MDTSession");
			MDTSessionHome home1 = (MDTSessionHome) PortableRemoteObject.narrow(ref, MDTSessionHome.class);
			MDTSession session = home1.create();

			rows = new ArrayList<String>();
			rows.add(frameTitle);
			all.add(rows);

			rows = new ArrayList<String>();
			all.add(rows);

			rows = new ArrayList<String>();
			rows.add(ui.getString("fina2.type"));
			rows.add(ui.getString("fina2.code"));
			rows.add(ui.getString("fina2.description"));
			all.add(rows);

			printChild(session, node);

			Object[][] data = getObjectArray(all);

			return data;
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	private void printChild(MDTSession session, Node node) {

		try {
			rows = new ArrayList<String>();
			if (((Integer) node.getType()).intValue() == MDTConstants.NODETYPE_NODE) {
				rows.add(ui.getString("fina2.node"));
			}
			if (((Integer) node.getType()).intValue() == MDTConstants.NODETYPE_INPUT) {
				rows.add(ui.getString("fina2.input"));
			}
			if (((Integer) node.getType()).intValue() == MDTConstants.NODETYPE_VARIABLE) {
				rows.add(ui.getString("fina2.variable"));
			}
			rows.add((String) node.getProperty("code"));
			rows.add(node.getLabel());
			all.add(rows);

			Collection children = session.getChildNodes(main.getUserHandle(), main.getLanguageHandle(), (MDTNodePK) node.getPrimaryKey());

			for (Iterator iter = children.iterator(); iter.hasNext();) {
				Node child = (Node) iter.next();
				printChild(session, child);
			}
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private Object[][] getObjectArray(ArrayList<ArrayList<String>> data) {
		height = data.size();
		width = 3;

		Object[][] tmp = new Object[height][width];

		for (int i = 0; i < height; i++) {
			ArrayList<String> list = data.get(i);
			for (int j = 0; j < width; j++) {
				String value = null;
				try {
					value = list.get(j);
				} catch (IndexOutOfBoundsException ex) {
					// TODO ex.printStackTrace();
				}
				tmp[i][j] = value;
			}
		}
		return tmp;
	}
}
