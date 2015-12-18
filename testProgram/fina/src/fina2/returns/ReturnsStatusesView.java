package fina2.returns;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import fina2.Main;
import fina2.security.User;
import fina2.ui.sheet.Spreadsheet;
import fina2.ui.sheet.SpreadsheetsManager;

public class ReturnsStatusesView {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;
	private Spreadsheet sheet;

	@SuppressWarnings("rawtypes")
	public ReturnsStatusesView(String title,
			Collection returnsStatusesCollection, String banksCode,
			String bankGroupsCode, String bankTypesCode, String returnsCode,
			String returnTypesCode, String from, String to) {
		this.returnsStatusesCollection = returnsStatusesCollection;
		this.banksCode = banksCode;
		this.bankGroupsCode = bankGroupsCode;
		this.bankTypesCode = bankGroupsCode;
		this.returnsCode = returnsCode;
		this.returnTypesCode = returnTypesCode;
		this.from = from;
		this.to = to;
		this.title = title;
	}

	public void show() {
		sheet = SpreadsheetsManager.getInstance().createSpreadsheet(title);
		this.initSheet(0, 0);
	}

	private void initSheet(int x, int y) {
		Object[][] dataCenter = center();
		Object[][] dataHeader = header();
		Object[][] dataFooter = footer();

		sheet.showGrid(false);
		sheet.setViewMode(Spreadsheet.VIEW_SIMPLE);

		// Header
		int hHeight = yHeader + x - 1;
		int hWidth = xHeader + y - 1;

		sheet.setFontName(x, y, hHeight, hWidth, ui.getFont().getName());
		sheet.setFontSize(x, y, hHeight, hWidth, ui.getFont().getSize() + 1);
		sheet.setFontWeight(x, y, hHeight, hWidth, Spreadsheet.BOLD);
		sheet.setDataArray(x, y, hHeight, hWidth, dataHeader);

		// Center
		int cx = yHeader + x - spaceRows + 1;
		int cHeight = yCenter + yHeader + x - spaceRows;
		int cWidth = xCenter + y - 1;

		sheet.setFontName(cx, y, cHeight, cWidth, ui.getFont().getName());
		sheet.setFontSize(cx, y, cHeight, cWidth, ui.getFont().getSize() - 2);
		sheet.setBorder(cx, y, cHeight, cWidth, Spreadsheet.LINE_YES,
				Spreadsheet.LINE_YES, Spreadsheet.LINE_YES,
				Spreadsheet.LINE_YES, Spreadsheet.LINE_YES,
				Spreadsheet.LINE_YES, (short) 1);
		sheet.setHorizontalAlign(cx, y + 2, cHeight, cWidth, Spreadsheet.CENTER);
		sheet.setFontWeight(cx, y, cHeight, cWidth, Spreadsheet.PLAIN);
		sheet.setDataArray(cx, y, cHeight, cWidth, dataCenter);

		// Footer
		int fx = yCenter + yHeader + x - spaceRows + 3;
		int fHeight = yCenter + yHeader + x - spaceRows + yFooter + 2;
		int fWidth = xFooter + y - 1;

		sheet.setFontName(fx, y, fHeight, fWidth, ui.getFont().getName());
		sheet.setFontSize(fx, y, fHeight, fWidth, ui.getFont().getSize() - 2);
		sheet.setBorder(fx, y, fHeight, fWidth, Spreadsheet.LINE_YES,
				Spreadsheet.LINE_YES, Spreadsheet.LINE_YES,
				Spreadsheet.LINE_YES, Spreadsheet.LINE_YES,
				Spreadsheet.LINE_YES, (short) 1);
		sheet.setDataArray(fx, y, fHeight, fWidth, dataFooter);

		sheet.setCellValue(fx - 1, fWidth,
				ui.getString("fina2.returnStatuses.viewSheet.total"));

		try {
			User user = (User) main.getUserHandle().getEJBObject();
			sheet.setCellValue(fx + 12, y + 1,
					ui.getString("fina2.returnStatuses.viewSheet.printedBy")
							+ " " + user.getName(main.getLanguageHandle())
							+ " @" + new Date().toString());
		} catch (RemoteException e) {
			Main.generalErrorHandler(e);
		}

		// Sheet Options
		sheet.setOptimalColWidth(x, 100);
		sheet.setCellWrap(yHeader + x - spaceRows + 1, y, yCenter + yHeader + x
				- spaceRows, xCenter + y - 1, true);
		sheet.setViewMode(Spreadsheet.VIEW_FULL);
	}

	private Object[][] header() {
		xHeader = 3;
		yHeader = 7;

		Object[][] temp = new Object[yHeader][xHeader];

		temp[1][1] = ui.getString("fina2.returnStatuses.viewSheetTitle");
		temp[1][2] = from + "-" + to;

		spaceRows = 5;
		if (!banksCode.equals("") && !banksCode.equals("ALL")) {
			temp[2][0] = ui.getString("fina2.bank.bank");
			temp[2][1] = banksCode;
			spaceRows--;
		}

		if (!bankGroupsCode.equals("") && !bankGroupsCode.equals("ALL")) {
			temp[2][0] = ui.getString("fina2.bank.bankGroup");
			temp[2][1] = bankGroupsCode;
			spaceRows--;
		}

		if (!bankTypesCode.equals("") && !bankTypesCode.equals("ALL")) {
			temp[2][0] = ui.getString("fina2.bank.bankType");
			temp[2][1] = bankTypesCode;
			spaceRows--;
		}

		if (!returnsCode.equals("") && !returnsCode.equals("ALL")) {
			temp[2][0] = ui.getString("fina2.returns.return");
			temp[2][1] = returnsCode;
			spaceRows--;
		}

		if (!returnTypesCode.equals("") && !returnTypesCode.equals("ALL")) {
			temp[2][0] = ui.getString("fina2.returns.returnType");
			temp[2][1] = returnTypesCode;
			spaceRows--;
		}
		return temp;
	}

	@SuppressWarnings("rawtypes")
	private Object[][] center() {
		Vector gg = (Vector) returnsStatusesCollection;
		Vector si = (Vector) gg.get(0);

		xCenter = si.size();
		yCenter = returnsStatusesCollection.size();
		Object[][] tmp = new Object[yCenter][xCenter];
		for (int i = 0; i < yCenter; i++) {
			Vector s = (Vector) gg.get(i);
			for (int j = 0; j < xCenter; j++) {

				tmp[i][j] = (String) s.get(j);

				if (i > 0 && j > 1) {
					switch (Integer.valueOf((String) s.get(j)).intValue()) {
					case 0:
						tmp[i][j] = "x";
						break;
					case -1:
						tmp[i][j] = ui.getString("fina2.no");
						break;
					case -2:
						tmp[i][j] = ui.getString("fina2.no");
						break;
					case ReturnConstants.STATUS_CREATED:
						tmp[i][j] = "C";
						break;
					case ReturnConstants.STATUS_AMENDED:
						tmp[i][j] = "A";
						break;
					case ReturnConstants.STATUS_IMPORTED:
						tmp[i][j] = "I";
						break;
					case ReturnConstants.STATUS_PROCESSED:
						tmp[i][j] = "*";
						break;
					case ReturnConstants.STATUS_VALIDATED:
						tmp[i][j] = "V";
						break;
					case ReturnConstants.STATUS_RESETED:
						tmp[i][j] = "R";
						break;
					case ReturnConstants.STATUS_ACCEPTED:
						tmp[i][j] = "V";
						break;
					case ReturnConstants.STATUS_REJECTED:
						tmp[i][j] = "-";
						break;
					case ReturnConstants.STATUS_LOADED:
						tmp[i][j] = "L";
						break;
					case ReturnConstants.STATUS_ERRORS:
						tmp[i][j] = "?";
						break;
					case ReturnConstants.STATUS_QUEUED:
						tmp[i][j] = "Q";
						break;
					default:
						tmp[i][j] = "?";
					}
				}
			}
		}
		return tmp;
	}

	@SuppressWarnings("rawtypes")
	private Object[][] footer() {
		xFooter = xCenter + 1;
		yFooter = 10;
		Object[][] temp = new Object[yFooter][xFooter];

		Vector all = (Vector) returnsStatusesCollection;
		Vector rets = (Vector) all.get(0);
		int arraySize = rets.size();

		int[] x = new int[arraySize];
		int[] no = new int[arraySize];
		int[] c = new int[arraySize];
		int[] a = new int[arraySize];
		int[] iArray = new int[arraySize];
		int[] star = new int[arraySize];
		int[] vArray = new int[arraySize];
		int[] r = new int[arraySize];
		int[] _array = new int[arraySize];
		int[] error = new int[arraySize];

		for (int i = 0; i < all.size(); i++) {
			Vector v = (Vector) all.get(i);
			for (int j = 0; j < rets.size(); j++) {
				if (i > 0 && j > 1) {
					switch (Integer.valueOf((String) v.get(j)).intValue()) {
					case 0:
						x[j]++;
						break;
					case -1:
						no[j]++;
						break;
					case -2:
						no[j]++;
						break;
					case ReturnConstants.STATUS_CREATED:
						c[j]++;
						break;
					case ReturnConstants.STATUS_AMENDED:
						a[j]++;
						break;
					case ReturnConstants.STATUS_IMPORTED:
						iArray[j]++;
						break;
					case ReturnConstants.STATUS_PROCESSED:
						star[j]++;
						break;
					case ReturnConstants.STATUS_VALIDATED:
						vArray[j]++;
						break;
					case ReturnConstants.STATUS_RESETED:
						r[j]++;
						break;
					case ReturnConstants.STATUS_ACCEPTED:
						vArray[j]++;
						break;
					case ReturnConstants.STATUS_REJECTED:
						_array[j]++;
						break;
					case ReturnConstants.STATUS_LOADED:
						error[j]++;
						break;
					case ReturnConstants.STATUS_ERRORS:
						error[j]++;
						break;
					case ReturnConstants.STATUS_QUEUED:
						error[j]++;
						break;
					default:
						error[j]++;
					}
				}
			}
		}
		temp[0][0] = "x";
		temp[0][1] = ui.getString("fina2.returns.notNecessary");
		arrayInsertMatrix(temp, x, 0);

		temp[1][0] = ui.getString("fina2.no");
		temp[1][1] = ui.getString("fina2.no");
		arrayInsertMatrix(temp, no, 1);

		temp[2][0] = "C";
		temp[2][1] = ui.getString("fina2.returns.created");
		arrayInsertMatrix(temp, c, 2);

		temp[3][0] = "A";
		temp[3][1] = ui.getString("fina2.returns.amended");
		arrayInsertMatrix(temp, a, 3);

		temp[4][0] = "I";
		temp[4][1] = ui.getString("fina2.returns.imported");
		arrayInsertMatrix(temp, iArray, 4);

		temp[5][0] = "*";
		temp[5][1] = ui.getString("fina2.returns.processed");
		arrayInsertMatrix(temp, star, 5);

		temp[6][0] = "V";
		temp[6][1] = ui.getString("fina2.returns.accepted");
		arrayInsertMatrix(temp, vArray, 6);

		temp[7][0] = "R";
		temp[7][1] = ui.getString("fina2.returns.reseted");
		arrayInsertMatrix(temp, r, 7);

		temp[8][0] = "-";
		temp[8][1] = ui.getString("fina2.returns.rejected");
		arrayInsertMatrix(temp, _array, 8);

		temp[9][0] = "?";
		temp[9][1] = ui.getString("fina2.returns.errors");
		arrayInsertMatrix(temp, error, 9);

		// insert totals
		temp[0][xFooter - 1] = arraySum(x);
		temp[1][xFooter - 1] = arraySum(no);
		temp[2][xFooter - 1] = arraySum(c);
		temp[3][xFooter - 1] = arraySum(a);
		temp[4][xFooter - 1] = arraySum(iArray);
		temp[5][xFooter - 1] = arraySum(star);
		temp[6][xFooter - 1] = arraySum(vArray);
		temp[7][xFooter - 1] = arraySum(r);
		temp[8][xFooter - 1] = arraySum(_array);
		temp[9][xFooter - 1] = arraySum(error);

		return temp;
	}

	private int arraySum(int[] array) {
		int src = 0;
		for (int a : array) {
			src += a;
		}
		return src;
	}

	private void arrayInsertMatrix(Object[][] matrix, int[] array, int index) {
		for (int i = 2; i < xFooter - 1; i++) {
			matrix[index][i] = array[i];
		}
	}

	@SuppressWarnings("rawtypes")
	private Collection returnsStatusesCollection;
	private String banksCode;
	private String bankGroupsCode;
	private String bankTypesCode;
	private String returnsCode;
	private String returnTypesCode;
	private String from;
	private String to;

	private String title;

	private int xCenter;
	private int yCenter;

	private int xHeader;
	private int yHeader;

	private int xFooter;
	private int yFooter;

	private int spaceRows;
}
