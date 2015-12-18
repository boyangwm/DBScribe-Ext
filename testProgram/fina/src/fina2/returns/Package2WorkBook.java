package fina2.returns;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.JFrame;

import fina2.Main;
import fina2.ui.UIManager;
import fina2.ui.sheet.Spreadsheet;
import fina2.ui.sheet.SpreadsheetsManager;
import fina2.ui.table.TableRow;

@SuppressWarnings("serial")
public class Package2WorkBook extends JFrame {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private ReturnSession returnSession;

	private List<TableRow> rows;

	private Spreadsheet totalSheet = SpreadsheetsManager.getInstance().createSpreadsheet();

	private LinkedHashMap<String, byte[]> returns = new LinkedHashMap<String, byte[]>();

	private File totalReturnsFile = null;

	private ReturnManagerFrame returnManagerFrame;

	private PackageInfo packageInfo;

	private Package2WorkBookProgressDialog progressDlg;

	public Package2WorkBook(List<TableRow> rows, ReturnManagerFrame returnManagerFrame, PackageInfo packageInfo) {
		this.rows = rows;
		this.returnManagerFrame = returnManagerFrame;
		this.packageInfo = packageInfo;
		initComponents();
	}

	public void initAndShowSheets() {
		returnSession = initReturnSession();

		createProgressDialog();

		progressDlg.setTitleMessage("Review Package: " + packageInfo.getName());

		progressDlg.setMessage("Loading Returns...");

		progressDlg.setTotalNumber(rows.size());

		if (returnSession == null) {
			return;
		}

		for (int i = 0; i < rows.size(); i++) {
			loadReturn(rows.get(i));
			progressDlg.setCurrentNumber(i + 1);
		}

		progressDlg.setMessage("Compiling Workbook...");
		progressDlg.setCurrentNumber(0);

		// Finally: Show loaded Returns.
		showLoadReturns();

		progressDlg.dispose();

		UIManager.resizeOooSheetPage(this);

		totalReturnsFile.delete();
	}

	private void loadReturn(TableRow tableRow) {
		Collection<TableRow> returnRows = new ArrayList<TableRow>();
		returnRows.add(tableRow);

		// Initial TMP ReturnAmendReviewFrame
		ReturnAmendReviewFrame tmp = new ReturnAmendReviewFrame(true, true);
		tmp.setRow(tableRow);
		tmp.setRows(new ArrayList<TableRow>(rows));
		ReturnPK returnPK = (ReturnPK) tableRow.getPrimaryKey();
		tmp.setReturnPK(returnPK);
		tmp.setReturnManagerFrame(returnManagerFrame);
		tmp.setPackageAmend(false);

		tmp.initReturnSheet(returnPK, tableRow);

		// Put return Content
		returns.put(tableRow.getValue(0), tmp.getSheet().getDocumentContent());

		// Dispose TMP
		tmp.getSheet().dispose();
		tmp.dispose();
	}

	private void createProgressDialog() {
		progressDlg = new Package2WorkBookProgressDialog(Package2WorkBook.this, ui.getString("fina2.return.package2workbook"), true);
		progressDlg.setSize(340, 140);

		Thread thread = new Thread() {
			public void run() {
				progressDlg.setVisible(true);
			}
		};
		thread.start();
	}

	private ReturnSession initReturnSession() {
		ReturnSession session = null;
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/returns/ReturnSession");
			ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);
			session = home.create();
		} catch (Exception ex) {
			if (progressDlg != null) {
				progressDlg.dispose();
			}
			Main.generalErrorHandler(ex);
		}
		return session;
	}

	private void showLoadReturns() {
		try {
			setVisible(true);

			// Create temporaty sheet
			Spreadsheet sheet = SpreadsheetsManager.getInstance().createSpreadsheet();
			jPanel2.add(sheet.getComponent());

			int count = 1;
			for (Entry<String, byte[]> e : returns.entrySet()) {
				String sheetName = e.getKey();
				byte[] value = e.getValue();
				if (totalReturnsFile == null) {
					initTotalSheet(sheetName, value);
				} else {
					sheet.read(value);
					totalSheet.copySheetTabs(sheet, sheetName);
				}
				progressDlg.setCurrentNumber(count + 1);
				count++;
			}

			returns.clear();

			// Destroy temporay sheet
			jPanel2.remove(sheet.getComponent());
			sheet.dispose();
		} catch (Exception ex) {
			if (progressDlg != null) {
				progressDlg.dispose();
			}
			Main.generalErrorHandler(ex);
		}
	}

	private void initTotalSheet(String sheetName, byte[] reportData) {
		try {
			String bankName = packageInfo.getBank().trim();
			//TODO
			totalReturnsFile = File.createTempFile(("R_" + bankName.substring(0, bankName.indexOf("[")) + "_" + packageInfo.getReturnType().trim() + "_").replace('.', '_'), "_tmp");

			FileOutputStream fos = new FileOutputStream(totalReturnsFile);
			fos.write(reportData);
			fos.close();

			totalSheet.readFromURL("file:///" + totalReturnsFile.getAbsolutePath().replace('\\', '/'));
			totalSheet.renameSheet(sheetName);
			totalSheet.showSheetTabs(true);
		} catch (Exception ex) {
			if (progressDlg != null) {
				progressDlg.dispose();
			}
			Main.generalErrorHandler(ex);
		}
	}

	private void initComponents() {
		jPanel1 = new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();

		setTitle(packageInfo);

		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exitForm(evt);
			}
		});

		jPanel3.setLayout(new java.awt.BorderLayout());

		getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

		jPanel3.add(totalSheet.getComponent(), java.awt.BorderLayout.CENTER);

		jPanel1.setLayout(new java.awt.BorderLayout());

		jPanel1.add(jPanel2, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

		setExtendedState(ReturnAmendReviewFrame.MAXIMIZED_BOTH);
	}

	public void setTitle(PackageInfo packageInfo) {
		StringBuffer title = new StringBuffer();
		title.append("FinA: ");
		title.append(ui.getString("fina2.returns.return"));
		title.append(" ");
		title.append(ui.getString("fina2.review"));
		title.append(" | ");
		title.append(packageInfo.getName());
		setTitle(title.toString());
	}

	private void exitForm(java.awt.event.WindowEvent evt) {
		totalSheet.dispose();
		dispose();
		totalReturnsFile.delete();
	}

	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
}
