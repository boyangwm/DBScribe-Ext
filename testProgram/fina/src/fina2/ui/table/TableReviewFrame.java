package fina2.ui.table;

import java.io.Serializable;
import java.util.Collection;

import fina2.Main;
import fina2.ui.UIManager.IndeterminateLoading;
import fina2.ui.sheet.Spreadsheet;
import fina2.ui.sheet.SpreadsheetsManager;

/**
 * @author N.Gochiashvili
 */
@SuppressWarnings("serial")
public class TableReviewFrame implements Serializable {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private Spreadsheet sheet;
	private String frameTitle;

	private int width;
	private int height;

	private IndeterminateLoading loading;

	public void show(final String frameTitle, final EJBTable table) {

		this.frameTitle = frameTitle;
		loading = ui.createIndeterminateLoading(main.getMainFrame());

		Thread threadShow = new Thread() {
			public void run() {
				loading.start();
				sheet = SpreadsheetsManager.getInstance().createSpreadsheet(frameTitle);
				loading.stop();
				initSheet(0, 1, table);
			};
		};
		threadShow.start();
	}

	private void initSheet(int x, int y, EJBTable table) {
		try {
			Object[][] data = getObjectArrayAndInitCoordinates(table, x, y);
			// Title
			sheet.setFontName(y, x, y, x, ui.getFont().getName());
			sheet.setFontSize(y, x, y, x, ui.getFont().getSize() + 2);
			sheet.setFontWeight(y, x, y, x, Spreadsheet.BOLD);
			sheet.setHorizontalAlign(y, x, Spreadsheet.LEFT);
			sheet.setVerticalAlign(y, x, Spreadsheet.CENTER);

			// rows title
			sheet.setFontName(y + 1, x, x + 2, width, ui.getFont().getName());
			sheet.setFontSize(y + 1, x, x + 2, width, ui.getFont().getSize() + 2);
			// sheet.setHorizontalAlign(dataRectangle.x + 1,
			// dataRectangle.width,
			// Spreadsheet.CENTER);
			// sheet.setVerticalAlign(dataRectangle.x + 1, dataRectangle.width,
			// Spreadsheet.CENTER);

			sheet.setBorder(y + 2, x, height + y - 1, width + x - 1, Spreadsheet.LINE_YES, Spreadsheet.LINE_YES, Spreadsheet.LINE_YES, Spreadsheet.LINE_YES, Spreadsheet.LINE_YES,
					Spreadsheet.LINE_YES, (short) 1);
			// Insert Fonts
			sheet.setFontName(y + 2, x, height, width, ui.getFont().getName());
			sheet.setFontSize(y + 2, x, height, width, ui.getFont().getSize() - 2);
			// Insert Data
			sheet.setDataArray(y, x, height + y - 1, width + x - 1, data);

			// Set one column horizontal Align
			sheet.setHorizontalAlign(y + 2, x, height, x, Spreadsheet.CENTER);

			sheet.setFontWeight(y + 2, x, height, width, Spreadsheet.PLAIN);
			sheet.setFontWeight(y + 1, x, 3, width, Spreadsheet.BOLD);
			// sheet.setOptimalColWidth(x, height);
			sheet.setOptimalColWidth(x + 1, 100);
			sheet.setCellWrap(y + 2, x, height, width, true);
			sheet.showGrid(false);
			sheet.setViewMode(Spreadsheet.VIEW_FULL);
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	@SuppressWarnings("unchecked")
	private Object[][] getObjectArrayAndInitCoordinates(EJBTable table, int x, int y) {
		Collection<TableRow> rows = table.getRows();
		Object[] columns = rows.toArray();
		if (columns.length == 0) {
			return null;
		}
		TableRow trowTmp = (TableRow) columns[0];
		// Init Coordinates
		this.width = trowTmp.getColumnCount() + 1;
		this.height = columns.length + 3;

		Object[][] tmp = new Object[height][width];
		tmp[0][0] = frameTitle;
		tmp[2][0] = "#";
		for (int i = 0; i < width - 1; i++) {
			tmp[2][i + 1] = table.getColumnName(i);
		}
		for (int i = 0; i < columns.length; i++) {
			TableRow col1 = (TableRow) columns[i];
			tmp[i + 3][0] = i + 1;
			for (int j = 0; j < col1.getColumnCount(); j++) {
				tmp[i + 3][j + 1] = col1.getValue(j);
			}
		}
		return tmp;
	}
}
