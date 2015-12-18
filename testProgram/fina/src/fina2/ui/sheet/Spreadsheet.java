/*
 * Spreadsheet.java
 *
 * Created on 10 Jul 2002, 10:49
 */

package fina2.ui.sheet;

import com.sun.star.container.NoSuchElementException;
import com.sun.star.lang.WrappedTargetException;

/**
 * This interface must be implemented by driver to provide unified access to
 * spreadsheet component.
 */
public interface Spreadsheet {

	public static int PLAIN = 1;
	public static int BOLD = 2;
	public static int ITALIC = 3;
	public static int BOLD_ITALIC = 4;

	public static int LINE_YES = 1;
	public static int LINE_NO = 2;
	public static int LINE_DEFAULT = 3;

	public static int REMOVE_NONE = 1;
	public static int REMOVE_UP = 2;
	public static int REMOVE_LEFT = 3;
	public static int REMOVE_ROWS = 4;
	public static int REMOVE_COLUMNS = 5;

	public static int INSERT_NONE = 1;
	public static int INSERT_DOWN = 2;
	public static int INSERT_RIGHT = 3;
	public static int INSERT_ROWS = 4;
	public static int INSERT_COLUMNS = 5;

	public static int VIEW_SIMPLE = 1;
	public static int VIEW_FULL = 2;

	public static final int ROWS = 1;
	public static final int COLUMNS = 2;

	public static final int START_ROW = 1;
	public static final int START_COL = 2;
	public static final int END_ROW = 3;
	public static final int END_COL = 4;

	/**
	 * contents are aligned with the upper edge of the cell.
	 */
	public static int TOP = 1;
	/**
	 * contents are horizontally or vertically centered.
	 */
	public static int CENTER = 2;
	/**
	 * contents are aligned to the lower edge of the cell.
	 */
	public static int BOTTOM = 3;
	/**
	 * contents are aligned with the left edge of the cell.
	 */
	public static int LEFT = 4;
	/**
	 * contents are aligned to the right edge of the cell.
	 */
	public static int RIGHT = 5;

	public static int VALUE = 1;
	public static int FORMULA = 2;
	public static int EMPTY = 3;
	public static int TEXT = 4;

	/**
	 * this method loads blank document into the sheet
	 */
	public void loadBlank();

	/**
	 * this method must by called after parent container will be shown
	 */
	public void afterShow();

	public boolean isLoaded();

	/**
	 * returns GUI component java.awt.Component for using inside parent
	 * container.
	 */
	public java.awt.Component getComponent();

	/**
	 * gets text value of specified cell.
	 */
	public String getCellValue(int r, int c);

	/**
	 * sets text value of specified cell.
	 */
	public void setCellValue(int r, int c, String value);

	/**
	 * gets numeric value of specified cell.
	 */
	public double getCellNumber(int r, int c);

	/**
	 * sets numeric value of specified cell.
	 */
	public void setCellNumber(int r, int c, double number);

	/**
	 * returns number of selected column.
	 */
	public int getSelectedCol();

	/**
	 * returns number of selected row.
	 */
	public int getSelectedRow();

	/**
	 * returns first column of current selection.
	 */
	public int getStartSelCol();

	/**
	 * returns first row of current selection.
	 */
	public int getStartSelRow();

	/**
	 * returns last column of current selection.
	 */
	public int getEndSelCol();

	/**
	 * returns last row of current selection.
	 */
	public int getEndSelRow();

	/**
	 * returns formula of specified cell.
	 */
	public String getCellFormula(int r, int c);

	/**
	 * sets formula for specified cell.
	 */
	public void setCellFormula(int r, int c, String formula);

	public int getCellType(int r, int c);

	public void setAutoCalculate(boolean calculate);

	/**
	 * forces calculation of all formulas in current sheet.
	 */
	public void recalculate();

	/**
	 * sets font weight for content of specified cell.
	 */
	public void setFontWeight(int r, int c, int weight);

	/**
	 * sets font name for content of specified cell.
	 */
	public void setFontName(int r, int c, String font);

	/**
	 * sets font size for content of specified cell.
	 */
	public void setFontSize(int r, int c, float size);

	/**
	 * sets font color for content of specified cell.
	 */
	public void setFontColor(int r, int c, long color);

	/**
	 * enables/disables text wrapping for content of specified cell.
	 */
	public void setCellWrap(int r, int c, boolean wrap);

	/**
	 * sets property containing the horizontal alignment of text within the
	 * cells.
	 */
	public void setHorizontalAlign(int r, int c, int align);

	/**
	 * sets property containing the vertical alignment of text within the cells.
	 */
	public void setVerticalAlign(int r, int c, int align);

	public void setRowIterator(int r1, int r2);

	public void setColumnIterator(int c1, int c2);

	public void removeRowIterator(int r);

	public void removeColumnIterator(int c);

	public void setBorder(int row, int column, int left, int top, int right, int bottom, short width);

	public void setBorder(int r1, int c1, int r2, int c2, int left, int top, int right, int bottom, int horizontal, int vertical, short width);

	public void copyRange(int sourceR1, int sourceC1, int sourceR2, int sourceC2, int destR, int destC);

	public void moveRange(int sourceR1, int sourceC1, int sourceR2, int sourceC2, int destR, int destC);

	public void removeRange(int r1, int c1, int r2, int c2, int mode);

	public void insertRange(int r1, int c1, int r2, int c2, int mode);

	public void showGrid(boolean show);

	public void setViewMode(int mode);

	public void showSheetTabs(boolean show);

	public void showHeaders(boolean show);

	public String getNumberFormat(int r, int c);

	public void setNumberFormat(int r, int c, String format);

	public void setNumberFormat(int r1, int c1, int r2, int c2, String format);

	public void setDateFormat(int r1, int c1, int r2, int c2, String format);

	/**
	 * sets font weight for content of specified cell.
	 */
	public void setFontWeight(int r1, int c1, int r2, int c2, int weight);

	/**
	 * sets font name for content of specified cell.
	 */
	public void setFontName(int r1, int c1, int r2, int c2, String font);

	/**
	 * sets font size for content of specified cell.
	 */
	public void setFontSize(int r1, int c1, int r2, int c2, float size);

	/**
	 * sets font color for content of specified cell.
	 */
	public void setFontColor(int r1, int c1, int r2, int c2, long color);

	/**
	 * enables/disables text wrapping for content of specified cell.
	 */
	public void setCellWrap(int r1, int c1, int r2, int c2, boolean wrap);

	/**
	 * sets property containing the horizontal alignment of text within the
	 * cells.
	 */
	public void setHorizontalAlign(int r1, int c1, int r2, int c2, int align);

	/**
	 * sets property containing the vertical alignment of text within the cells.
	 */
	public void setVerticalAlign(int r1, int c1, int r2, int c2, int align);

	public void setOptimalColWidth(int c1, int c2);

	public void dispose();

	public void read(java.io.InputStream in);

	public void read(byte[] buff);

	public void readFromURL(String url);

	public void write(java.io.OutputStream out);

	public byte[] getDocumentContent();

	public String getID();

	public void convertToValues();

	public void replaceFunction(int r1, int c1, int r2, int c2, String fun, String replace, boolean completely);

	public int getLastCol();

	public int getLastRow();

	public void setHeader(int row);

	public void setFooter(int row);

	public int getHeader();

	public int getFooter();

	public void setProtected(boolean protect, String pass);

	public void setCellProtected(int r1, int c1, boolean protect);

	public void setCellNumberValidity(int r1, int c1);

	public void setCellNumberValidity(int r1, int c1, int r2, int c2);

	public void setCellDateValidity(int r1, int c1);

	public void setName(String name);

	public void setDataArray(int r1, int c1, int r2, int c2, Object[][] data);

	public Object[][] getDataArray(int r1, int c1, int r2, int c2);

	public void clearFormulas(int r1, int c1, int r2, int c2);

	public Object getFormat();

	public void setFormat(Object format);

	public int getColWidth(int r1, int c1, int r2, int c2);

	public void setColWidth(int r1, int c1, int r2, int c2, int w);

	public String getHorizontalAlign(int r1, int c1, int r2, int c2);

	public boolean isBorder(int r1, int c1, int r2, int c2);

	public void copyStrings(Spreadsheet dest);

	public void print();

	public void copySheetTabs(fina2.ui.sheet.Spreadsheet sh, String name);

	public void addModifyListener(int r1, int c1, int r2, int c2, final ModifyListener listener, final boolean isNumber);

	public void addModifyListener(int r1, int c1, final ModifyListener listener, boolean isNumber);

	public void addCellModifyListener(int c1, int r1, final CellModifyListener listener, final boolean isNumber);

	public void renameSheet(String name) throws Exception;

	public void selectSheetByName(String sheetName) throws Exception;

	public void unselect() throws Exception;

	public String getVersion();

	public void readAndHide(byte[] buff);

	public void exportAsPdf(byte[] b);

	public void setHeaderAndFooterCurrentUserName(String userName) throws Exception;
}
