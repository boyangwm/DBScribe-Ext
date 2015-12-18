package fina2.ui.sheet.openoffice;

import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import com.sun.star.awt.FontSlant;
import com.sun.star.awt.FontWeight;
import com.sun.star.beans.PropertyState;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.comp.beans.LocalOfficeConnection;
import com.sun.star.comp.beans.NoConnectionException;
import com.sun.star.comp.beans.OOoBean;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.container.XNameContainer;
import com.sun.star.container.XNamed;
import com.sun.star.frame.FrameSearchFlag;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XController;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XLayoutManager;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XStorable;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XOutputStream;
import com.sun.star.lang.DisposedException;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.Locale;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lib.uno.adapter.ByteArrayToXInputStreamAdapter;
import com.sun.star.lib.uno.adapter.OutputStreamToXOutputStreamAdapter;
import com.sun.star.sheet.CellDeleteMode;
import com.sun.star.sheet.CellFlags;
import com.sun.star.sheet.CellInsertMode;
import com.sun.star.sheet.ConditionOperator;
import com.sun.star.sheet.ValidationAlertStyle;
import com.sun.star.sheet.ValidationType;
import com.sun.star.sheet.XCalculatable;
import com.sun.star.sheet.XCellAddressable;
import com.sun.star.sheet.XCellRangeAddressable;
import com.sun.star.sheet.XCellRangeData;
import com.sun.star.sheet.XCellRangeMovement;
import com.sun.star.sheet.XCellRangesQuery;
import com.sun.star.sheet.XDatabaseRange;
import com.sun.star.sheet.XDatabaseRanges;
import com.sun.star.sheet.XHeaderFooterContent;
import com.sun.star.sheet.XSheetAnnotation;
import com.sun.star.sheet.XSheetAnnotationAnchor;
import com.sun.star.sheet.XSheetAnnotations;
import com.sun.star.sheet.XSheetAnnotationsSupplier;
import com.sun.star.sheet.XSheetCellCursor;
import com.sun.star.sheet.XSheetCellRanges;
import com.sun.star.sheet.XSheetCondition;
import com.sun.star.sheet.XSheetOutline;
import com.sun.star.sheet.XSpreadsheet;
import com.sun.star.sheet.XSpreadsheetDocument;
import com.sun.star.sheet.XSpreadsheetView;
import com.sun.star.sheet.XSpreadsheets;
import com.sun.star.sheet.XUsedAreaCursor;
import com.sun.star.style.XStyle;
import com.sun.star.style.XStyleFamiliesSupplier;
import com.sun.star.table.CellAddress;
import com.sun.star.table.CellContentType;
import com.sun.star.table.CellHoriJustify;
import com.sun.star.table.CellRangeAddress;
import com.sun.star.table.CellVertJustify;
import com.sun.star.table.TableBorder;
import com.sun.star.table.TableOrientation;
import com.sun.star.table.XCell;
import com.sun.star.table.XCellRange;
import com.sun.star.table.XColumnRowRange;
import com.sun.star.table.XTableColumns;
import com.sun.star.text.XText;
import com.sun.star.text.XTextRange;
import com.sun.star.uno.Any;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Type;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.CellProtection;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.URL;
import com.sun.star.util.XCloseable;
import com.sun.star.util.XModifyBroadcaster;
import com.sun.star.util.XModifyListener;
import com.sun.star.util.XNumberFormats;
import com.sun.star.util.XNumberFormatsSupplier;
import com.sun.star.util.XProtectable;
import com.sun.star.util.XReplaceDescriptor;
import com.sun.star.util.XReplaceable;
import com.sun.star.util.XURLTransformer;
import com.sun.star.view.XSelectionSupplier;

import fina2.ui.sheet.CellModifyListener;
import fina2.ui.sheet.ModifyListener;
import fina2.ui.sheet.Spreadsheet;

public class OOSheet implements Spreadsheet {

	private static final Logger log = Logger.getLogger(OOSheet.class);

	public OOoBean ooBean;
	// TODO Should be removed
	public static final String calcUrl = "file:///C:/fina-server/tmpreview.ods".replace(java.io.File.separatorChar, '/').replace(" ", "%20");
	private XModel model;
	private XSpreadsheetView view;
	private XSpreadsheet sheet;
	private XSpreadsheetDocument doc;
	private XSelectionSupplier selection;
	private XComponentContext xRemoteContext;
	private XMultiComponentFactory xRemoteServiceManager;
	private LocalOfficeConnection officeConnection;
	private XComponent xComponent;

	private boolean loaded = false;

	public OOSheet() {
		ooBean = new OOoBean();
	}

	public OOSheet(InputStream in) {

		try {
			officeConnection = new LocalOfficeConnection();
			officeConnection.setUnoUrl("uno:socket,host=localhost,port=8100;urp;StarOffice.ServiceManager");

			xRemoteContext = officeConnection.getComponentContext();
			xRemoteServiceManager = xRemoteContext.getServiceManager();

			read(in);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public OOSheet(byte[] buff) {

		try {
			officeConnection = new LocalOfficeConnection();
			officeConnection.setUnoUrl("uno:socket,host=localhost,port=8100;urp;StarOffice.ServiceManager");

			xRemoteContext = officeConnection.getComponentContext();
			xRemoteServiceManager = xRemoteContext.getServiceManager();

			read(buff);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public OOSheet(String frameTitle) {
		initPrintComponents(frameTitle);
	}

	public void read(InputStream in) {

		try {
			preLoad();
			if (ooBean != null) {
				ooBean.loadFromStream(in, null);
			} else {
				load(in);
			}
			postLoad();

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void readFromURL(String url) {

		try {
			preLoad();
			if (ooBean != null) {
				ooBean.loadFromURL(url, null);
			}
			postLoad();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void read(byte[] buff) {

		try {
			preLoad();
			if (ooBean != null) {
				ooBean.loadFromByteArray(buff, null);
			} else {
				load(buff);
			}
			postLoad();

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void readAndHide(byte[] buff) {
		try {
			preLoad();
			if (ooBean != null) {
				ooBean.setVisible(false);

				PropertyValue p[] = new PropertyValue[1];
				p[0] = new PropertyValue();
				p[0].Name = "Hidden";
				p[0].Value = true;
				ooBean.loadFromByteArray(buff, p);
				ooBean.setVisible(false);
			} else {
				load(buff);
			}
			postLoad();

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void write(OutputStream out) {

		try {
			if (ooBean != null) {
				ooBean.storeToStream(out, null);
			} else {
				store(new OutputStreamToXOutputStreamAdapter(out));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public byte[] getDocumentContent() {

		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		try {
			write(bo);
			return bo.toByteArray();
		} finally {
			try {
				bo.close();
			} catch (IOException ex) {
			}
		}
	}

	public void loadBlank() {

		try {
			preLoad();
			ooBean.loadFromURL("private:factory/scalc", null);
			postLoad();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void afterShow() {

		try {
			ooBean.aquireSystemWindow();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public boolean isLoaded() {
		return loaded;
	}

	private void load(InputStream is) throws Exception {

		int s = 4096;
		int r = 0, n = 0;
		byte[] buffer = new byte[s];
		byte[] newBuffer = null;
		while ((r = is.read(buffer, n, buffer.length - n)) > 0) {
			n += r;
			if (is.available() > buffer.length - n) {
				newBuffer = new byte[buffer.length * 2];
				System.arraycopy(buffer, 0, newBuffer, 0, n);
				buffer = newBuffer;
			}
		}
		if (buffer.length != n) {
			newBuffer = new byte[n];
			System.arraycopy(buffer, 0, newBuffer, 0, n);
			buffer = newBuffer;
		}

		load(buffer);
	}

	private void load(byte[] buffer) throws Exception {

		Object desktop = xRemoteServiceManager.createInstanceWithContext("com.sun.star.frame.Desktop", xRemoteContext);

		XComponentLoader loader = (XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class, desktop);

		XInputStream xStream = new ByteArrayToXInputStreamAdapter(buffer);

		PropertyValue[] args = new PropertyValue[2];

		args[0] = new PropertyValue();
		args[0].Name = "InputStream";
		args[0].Value = xStream;
		args[0].State = PropertyState.DIRECT_VALUE;

		args[1] = new PropertyValue();
		args[1].Name = "Hidden";
		args[1].Value = new Boolean(true);

		xComponent = loader.loadComponentFromURL("private:stream", "_blank", 0, args);
	}

	private void store(XOutputStream xStream) throws Exception {

		PropertyValue[] args = new PropertyValue[1];

		args[0] = new PropertyValue();
		args[0].Name = "OutputStream";
		args[0].Value = xStream;
		args[0].State = PropertyState.DIRECT_VALUE;

		XStorable storable = (XStorable) UnoRuntime.queryInterface(XStorable.class, doc);
		storable.storeAsURL("private:stream", args);
	}

	private void preLoad() throws Exception {
		if (loaded) {
			log.debug("Closing previously loaded document before loading new one.");
			if (ooBean != null) {
				ooBean.clear();
			} else {
				closeDocument();
			}
		}
	}

	private void postLoad() throws Exception {

		loaded = true;

		if (ooBean != null) {

			XController controller = ooBean.getFrame().getController();
			model = controller.getModel();

			view = (XSpreadsheetView) UnoRuntime.queryInterface(XSpreadsheetView.class, controller);

			doc = (XSpreadsheetDocument) UnoRuntime.queryInterface(XSpreadsheetDocument.class, model);

			XSpreadsheets sheets = doc.getSheets();

			com.sun.star.sheet.XSpreadsheets xSheets = doc.getSheets();

			// Remove other Sheets
			deleteOtherSheets(xSheets);

			XIndexAccess oIndexSheets = (XIndexAccess) UnoRuntime.queryInterface(XIndexAccess.class, sheets);

			sheet = (XSpreadsheet) getAnyObject(oIndexSheets.getByIndex(0));

			selection = (XSelectionSupplier) UnoRuntime.queryInterface(XSelectionSupplier.class, view);
		} else {
			model = (XModel) UnoRuntime.queryInterface(XModel.class, xComponent);

			doc = (XSpreadsheetDocument) UnoRuntime.queryInterface(XSpreadsheetDocument.class, xComponent);

			XSpreadsheets sheets = doc.getSheets();

			// Remove other Sheets
			deleteOtherSheets(sheets);

			XIndexAccess oIndexSheets = (XIndexAccess) UnoRuntime.queryInterface(XIndexAccess.class, sheets);

			sheet = (XSpreadsheet) getAnyObject(oIndexSheets.getByIndex(0));

			log.info("doc: " + doc);
			log.info("model: " + model);
			log.info("sheet: " + sheet);
		}
	}

	private void deleteOtherSheets(XSpreadsheets xSheets) throws NoSuchElementException, WrappedTargetException {
		// Remove other Sheets
		String[] sheetsName = xSheets.getElementNames();
		for (int i = 1; i < sheetsName.length; i++) {
			xSheets.removeByName(sheetsName[i]);
		}
	}

	public void dispose() {

		if (!loaded) {
			return;
		}

		log.debug("Disposing sheet...");
		try {
			if (ooBean != null) {
				ooBean.stopOOoConnection();
			} else {
				closeDocument();
				officeConnection.dispose();
			}
			loaded = false;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void closeDocument() {
		try {
			XCloseable xCloseable = (XCloseable) UnoRuntime.queryInterface(XCloseable.class, doc);
			xCloseable.close(true);
		} catch (DisposedException ex) {
			log.error(ex.getMessage(), ex);
		} catch (CloseVetoException ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	/**
	 * Called by the garbage collector on an object when garbage collection
	 * determines that there are no more references to the object.
	 * 
	 * @throws Throwable
	 *             the <code>Exception</code> raised by this method
	 */
	protected void finalize() throws Throwable {

		if (loaded) {
			log.warn("Disposing sheet in finalize method.");
			dispose();
		}
	}

	public String getID() {
		log.debug("Sheet id: " + String.valueOf(doc.hashCode()));
		return String.valueOf(doc.hashCode());
	}

	public void group(int orientation, int startRow, int startCol, int endRow, int endCol) {
		try {
			XSheetOutline outline = (XSheetOutline) UnoRuntime.queryInterface(XSheetOutline.class, sheet);
			CellRangeAddress addr = new CellRangeAddress((short) 0, startCol, startRow, endCol, endRow);
			if (orientation == COLUMNS)
				outline.group(addr, TableOrientation.COLUMNS);
			else
				outline.group(addr, TableOrientation.ROWS);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void ungroup(int orientation, int startRow, int startCol, int endRow, int endCol) {
		try {
			XSheetOutline outline = (XSheetOutline) UnoRuntime.queryInterface(XSheetOutline.class, sheet);
			CellRangeAddress addr = new CellRangeAddress((short) 0, startCol, startRow, endCol, endRow);
			if (orientation == COLUMNS)
				outline.ungroup(addr, TableOrientation.COLUMNS);
			else
				outline.ungroup(addr, TableOrientation.ROWS);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public int getDatabaseRangeValue(String name, int valueType) {

		try {
			XPropertySet docProp = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, doc);
			Object rangesObj = docProp.getPropertyValue("DatabaseRanges");
			XNameAccess ranges = (XNameAccess) UnoRuntime.queryInterface(XNameAccess.class, rangesObj);
			XDatabaseRange range = (XDatabaseRange) getAnyObject(ranges.getByName(name));
			CellRangeAddress addr = range.getDataArea();

			switch (valueType) {
			case START_ROW:
				return addr.StartRow;
			case START_COL:
				return addr.StartColumn;
			case END_ROW:
				return addr.EndRow;
			case END_COL:
				return addr.EndColumn;
			}
		} catch (Exception e) {
		} // Ignore

		return 0;
	}

	public void removeDatabaseRange(String name) {
		try {
			XPropertySet docProp = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, doc);
			Object rangesObj = docProp.getPropertyValue("DatabaseRanges");
			XDatabaseRanges dbr = (XDatabaseRanges) getAnyObject(rangesObj);

			XNameAccess ranges = (XNameAccess) UnoRuntime.queryInterface(XNameAccess.class, rangesObj);
			XDatabaseRange range = null;
			try {
				range = (XDatabaseRange) ranges.getByName(name);
			} catch (Exception ex) {
			}

			if (range != null) {
				dbr.removeByName(name);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void addDatabaseRange(String name, int startRow, int startCol, int endRow, int endCol) {
		try {
			XPropertySet docProp = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, doc);
			Object rangesObj = docProp.getPropertyValue("DatabaseRanges");
			XDatabaseRanges ranges = (XDatabaseRanges) UnoRuntime.queryInterface(XDatabaseRanges.class, rangesObj);

			CellRangeAddress addr = new CellRangeAddress((short) 0, startCol, startRow, endCol, endRow);

			ranges.addNewByName(name, addr);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void setCellFormula(int r, int c, String formula) {
		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCell cell = range.getCellByPosition(c, r);
			cell.setFormula(formula);
			XPropertySet xProp = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cell);
			xProp.setPropertyValue("FormulaLocal", formula);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void setCellNumber(int r, int c, double number) {
		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCell cell = range.getCellByPosition(c, r);
			cell.setValue(number);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public int getStartSelRow() {
		try {
			XCellRangeAddressable range = (XCellRangeAddressable) UnoRuntime.queryInterface(XCellRangeAddressable.class, selection.getSelection());
			return range.getRangeAddress().StartRow;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return -1;
		}
	}

	public void setCellValue(int r, int c, String value) {
		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCell cell = range.getCellByPosition(c, r);
			XTextRange text = (XTextRange) UnoRuntime.queryInterface(XTextRange.class, cell);
			text.setString(value);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void setAutoCalculate(boolean calculate) {
		try {
			XCalculatable calc = (XCalculatable) UnoRuntime.queryInterface(XCalculatable.class, doc);
			calc.enableAutomaticCalculation(calculate);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void recalculate() {
		try {
			XCalculatable calc = (XCalculatable) UnoRuntime.queryInterface(XCalculatable.class, doc);
			calc.calculateAll();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public int getEndSelRow() {
		try {
			XCellRangeAddressable range = (XCellRangeAddressable) UnoRuntime.queryInterface(XCellRangeAddressable.class, selection.getSelection());
			return range.getRangeAddress().EndRow;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return -1;
		}
	}

	public int getSelectedCol() {
		try {
			XCellRangeAddressable range = (XCellRangeAddressable) UnoRuntime.queryInterface(XCellRangeAddressable.class, selection.getSelection());
			return range.getRangeAddress().StartColumn;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return -1;
		}
	}

	public int getSelectedRow() {
		try {
			XCellRangeAddressable range = (XCellRangeAddressable) UnoRuntime.queryInterface(XCellRangeAddressable.class, selection.getSelection());
			return range.getRangeAddress().StartRow;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return -1;
		}
	}

	public int getStartSelCol() {
		try {
			XCellRangeAddressable range = (XCellRangeAddressable) UnoRuntime.queryInterface(XCellRangeAddressable.class, selection.getSelection());
			return range.getRangeAddress().StartColumn;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return -1;
		}
	}

	public String getCellValue(int r, int c) {
		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCell cell = range.getCellByPosition(c, r);
			XTextRange text = (XTextRange) UnoRuntime.queryInterface(XTextRange.class, cell);
			return text.getString();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public double getCellNumber(int r, int c) {
		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCell cell = range.getCellByPosition(c, r);
			return cell.getValue();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Double.NaN;
		}
	}

	public String getCellFormula(int r, int c) {
		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCell cell = range.getCellByPosition(c, r);
			return cell.getFormula();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public int getEndSelCol() {
		try {
			XCellRangeAddressable range = (XCellRangeAddressable) UnoRuntime.queryInterface(XCellRangeAddressable.class, selection.getSelection());
			return range.getRangeAddress().EndColumn;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return -1;
		}
	}

	public Component getComponent() {
		return ooBean;
	}

	public void setFontColor(int r, int c, long color) {
		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			// XCellRange cell = range.getCellRangeByPosition(c1, r1, c2, r2);
			XCell cell = range.getCellByPosition(c, r);
			com.sun.star.text.XText text = (com.sun.star.text.XText) UnoRuntime.queryInterface(com.sun.star.text.XText.class, cell);

			XPropertySet ps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, text);

			ps.setPropertyValue("CharColor", new Long(color));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void setFontSize(int r, int c, float size) {
		setFontSize(r, c, r, c, size);
	}

	public void setFontWeight(int r, int c, int weight) {
		setFontWeight(r, c, r, c, weight);
	}

	public void setFontName(int r, int c, String font) {
		setFontName(r, c, r, c, font);
	}

	public void setCellWrap(int r, int c, boolean wrap) {
		setCellWrap(r, c, r, c, wrap);
	}

	public void setVerticalAlign(int r, int c, int align) {
		setVerticalAlign(r, c, r, c, align);
	}

	public void setHorizontalAlign(int r, int c, int align) {
		setHorizontalAlign(r, c, r, c, align);
	}

	public void setRowIterator(int r1, int r2) {
	}

	public void setColumnIterator(int c1, int c2) {
	}

	public void removeColumnIterator(int c) {
	}

	public void removeRowIterator(int r) {
	}

	public void setBorder(int r, int c, int left, int top, int right, int bottom, short width) {

		setBorder(r, c, r, c, left, top, right, bottom, LINE_DEFAULT, LINE_DEFAULT, width);
	}

	public void setBorder(int r1, int c1, int r2, int c2, int left, int top, int right, int bottom, int horizontal, int vertical, short width) {

		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cells = range.getCellRangeByPosition(c1, r1, c2, r2);

			XPropertySet ps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cells);

			TableBorder border = (TableBorder) ps.getPropertyValue("TableBorder");

			switch (left) {
			case LINE_YES:
				border.LeftLine.OuterLineWidth = width;
				border.IsLeftLineValid = true;
				break;
			case LINE_NO:
				border.LeftLine.OuterLineWidth = 0;
				border.IsLeftLineValid = false;
				break;
			case LINE_DEFAULT:
				break;
			}
			switch (top) {
			case LINE_YES:
				border.TopLine.OuterLineWidth = width;
				border.IsTopLineValid = true;
				break;
			case LINE_NO:
				border.TopLine.OuterLineWidth = 0;
				border.IsTopLineValid = false;
				break;
			case LINE_DEFAULT:
				break;
			}
			switch (right) {
			case LINE_YES:
				border.RightLine.OuterLineWidth = width;
				border.IsRightLineValid = true;
				break;
			case LINE_NO:
				border.RightLine.OuterLineWidth = 0;
				border.IsRightLineValid = false;
				break;
			case LINE_DEFAULT:
				break;
			}
			switch (bottom) {
			case LINE_YES:
				border.BottomLine.OuterLineWidth = width;
				border.IsBottomLineValid = true;
				break;
			case LINE_NO:
				border.BottomLine.OuterLineWidth = 0;
				border.IsBottomLineValid = false;
				break;
			case LINE_DEFAULT:
				break;
			}

			switch (horizontal) {
			case LINE_YES:
				border.HorizontalLine.OuterLineWidth = width;
				border.IsHorizontalLineValid = true;
				break;
			case LINE_NO:
				border.HorizontalLine.OuterLineWidth = 0;
				border.IsHorizontalLineValid = false;
				break;
			case LINE_DEFAULT:
				break;
			}

			switch (vertical) {
			case LINE_YES:
				border.VerticalLine.OuterLineWidth = width;
				border.IsVerticalLineValid = true;
				break;
			case LINE_NO:
				border.VerticalLine.OuterLineWidth = 0;
				border.IsVerticalLineValid = false;
				break;
			case LINE_DEFAULT:
				break;
			}

			ps.setPropertyValue("TableBorder", border);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void copyRange(int sourceR1, int sourceC1, int sourceR2, int sourceC2, int destR, int destC) {

		try {
			XCellRangeMovement m = (XCellRangeMovement) UnoRuntime.queryInterface(XCellRangeMovement.class, sheet);

			CellAddress dest = new CellAddress((short) 0, destC, destR);

			CellRangeAddress source = new CellRangeAddress((short) 0, sourceC1, sourceR1, sourceC2, sourceR2);

			m.copyRange(dest, source);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void moveRange(int sourceR1, int sourceC1, int sourceR2, int sourceC2, int destR, int destC) {

		try {
			XCellRangeMovement m = (XCellRangeMovement) UnoRuntime.queryInterface(XCellRangeMovement.class, sheet);

			XCellRangeAddressable range = (XCellRangeAddressable) UnoRuntime.queryInterface(XCellRangeAddressable.class, selection.getSelection());

			CellAddress dest = new CellAddress(range.getRangeAddress().Sheet, destC, destR);
			CellRangeAddress source = new CellRangeAddress(range.getRangeAddress().Sheet, sourceC1, sourceR1, sourceC2, sourceR2);
			m.moveRange(dest, source);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void insertRange(int r1, int c1, int r2, int c2, int mode) {

		try {
			XCellRangeMovement m = (XCellRangeMovement) UnoRuntime.queryInterface(XCellRangeMovement.class, sheet);

			CellRangeAddress source = new CellRangeAddress((short) 0, c1, r1, c2, r2);

			CellInsertMode md = null;
			switch (mode) {
			case INSERT_NONE:
				md = CellInsertMode.NONE;
				break;
			case INSERT_DOWN:
				md = CellInsertMode.DOWN;
				break;
			case INSERT_RIGHT:
				md = CellInsertMode.RIGHT;
				break;
			case INSERT_ROWS:
				md = CellInsertMode.ROWS;
				break;
			case INSERT_COLUMNS:
				md = CellInsertMode.COLUMNS;
				break;
			}
			m.insertCells(source, md);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void removeRange(int r1, int c1, int r2, int c2, int mode) {

		try {
			XCellRangeMovement m = (XCellRangeMovement) UnoRuntime.queryInterface(XCellRangeMovement.class, sheet);

			XCellRangeAddressable range = (XCellRangeAddressable) UnoRuntime.queryInterface(XCellRangeAddressable.class, selection.getSelection());

			CellRangeAddress source = new CellRangeAddress(range.getRangeAddress().Sheet, c1, r1, c2, r2);

			CellDeleteMode md = null;
			switch (mode) {
			case REMOVE_NONE:
				md = CellDeleteMode.NONE;
				break;
			case REMOVE_UP:
				md = CellDeleteMode.UP;
				break;
			case REMOVE_LEFT:
				md = CellDeleteMode.LEFT;
				break;
			case REMOVE_ROWS:
				md = CellDeleteMode.ROWS;
				break;
			case REMOVE_COLUMNS:
				md = CellDeleteMode.COLUMNS;
				break;
			}
			m.removeRange(source, md);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void showGrid(boolean show) {
		try {
			XPropertySet ps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, view);
			ps.setPropertyValue("ShowGrid", new Boolean(show));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void setViewMode(int mode) {
		try {
			// Always show menubar
			showElement("private:resource/menubar/menubar", true);

			// Hide unnecessary toolbars
			showElement("private:resource/toolbar/toolbar", false);
			showElement("private:resource/toolbar/alignmentbar", false);
			showElement("private:resource/toolbar/arrowshapes", false);
			showElement("private:resource/toolbar/basicshapes", false);
			showElement("private:resource/toolbar/calloutshapes", false);
			showElement("private:resource/toolbar/colorbar", false);
			showElement("private:resource/toolbar/drawbar", false);
			showElement("private:resource/toolbar/drawobjectbar", false);
			showElement("private:resource/toolbar/extrusionobjectbar", false);
			showElement("private:resource/toolbar/fontworkobjectbar", false);
			showElement("private:resource/toolbar/fontworkshapetypes", false);
			showElement("private:resource/toolbar/formatobjectbar", false);
			showElement("private:resource/toolbar/formcontrols", false);
			showElement("private:resource/toolbar/formdesign", false);
			showElement("private:resource/toolbar/formsfilterbar", false);
			showElement("private:resource/toolbar/formsnavigationbar", false);
			showElement("private:resource/toolbar/formsobjectbar", false);
			showElement("private:resource/toolbar/formtextobjectbar", false);
			showElement("private:resource/toolbar/fullscreenbar", false);
			showElement("private:resource/toolbar/graphicobjectbar", false);
			showElement("private:resource/toolbar/insertbar", false);
			showElement("private:resource/toolbar/insertcellsbar", false);
			showElement("private:resource/toolbar/insertobjectbar", false);
			showElement("private:resource/toolbar/mediaobjectbar", false);
			showElement("private:resource/toolbar/moreformcontrols", false);
			showElement("private:resource/toolbar/previewbar", false);
			showElement("private:resource/toolbar/starshapes", false);
			showElement("private:resource/toolbar/symbolshapes", false);
			showElement("private:resource/toolbar/viewerbar", false);

			switch (mode) {
			case VIEW_SIMPLE:

				showFunctionBar(false);
				showElement("private:resource/toolbar/standardbar", false);
				showElement("private:resource/toolbar/textobjectbar", false);
				showElement("private:resource/statusbar/statusbar", false);
				break;
			case VIEW_FULL:

				showFunctionBar(true);
				showElement("private:resource/toolbar/standardbar", true);
				showElement("private:resource/toolbar/textobjectbar", true);
				showElement("private:resource/statusbar/statusbar", true);
				break;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void showFunctionBar(boolean show) throws Exception {

		PropertyValue[] props = new PropertyValue[1];
		props[0] = new PropertyValue();
		props[0].Name = "InputLineVisible";
		props[0].Value = new Boolean(show);

		executeCommand(".uno:InputLineVisible", props);
	}

	public void showSheetTabs(boolean show) {
		try {
			XPropertySet ps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, view);
			ps.setPropertyValue("HasSheetTabs", new Boolean(show));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void showHeaders(boolean show) {
		try {
			XPropertySet ps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, view);
			ps.setPropertyValue("HasColumnRowHeaders", new Boolean(show));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public String getNumberFormat(int r, int c) {
		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCell cell = range.getCellByPosition(c, r); // RangeByPosition(c1,
			// r1, c2, r2);

			XPropertySet ps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cell);

			Integer f = (Integer) ps.getPropertyValue("NumberFormat");

			XNumberFormatsSupplier nfs = (XNumberFormatsSupplier) UnoRuntime.queryInterface(XNumberFormatsSupplier.class, doc);
			XNumberFormats formats = nfs.getNumberFormats();

			XPropertySet fps = formats.getByKey(f.intValue());
			String format = (String) fps.getPropertyValue("FormatString");

			return format;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return "Standard";
		}
	}

	public void setNumberFormat(int r, int c, String format) {
		setNumberFormat(r, c, r, c, format);
	}

	public void setNumberFormat(int r1, int c1, int r2, int c2, String format) {
		try {
			XNumberFormatsSupplier nfs = (XNumberFormatsSupplier) UnoRuntime.queryInterface(XNumberFormatsSupplier.class, doc);
			XNumberFormats formats = nfs.getNumberFormats();

			Locale l = new Locale("en", "US", "");
			int f = 0;
			try {
				f = formats.addNew(format, l);
			} catch (Exception e) {
				f = formats.queryKey(format, l, true);
			}

			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cell = range.getCellRangeByPosition(c1, r1, c2, r2);

			XPropertySet ps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cell);

			ps.setPropertyValue("NumberFormat", new Integer(f));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void setDateFormat(int r1, int c1, int r2, int c2, String format) {
		try {// Set the date value.
			com.sun.star.table.XCell xCell = sheet.getCellRangeByPosition(r1, c1, r1, c1).getCellByPosition(0, 0);

			xCell.setFormula(format);

			// Set standard date format.
			com.sun.star.util.XNumberFormatsSupplier xFormatsSupplier = (com.sun.star.util.XNumberFormatsSupplier) UnoRuntime.queryInterface(com.sun.star.util.XNumberFormatsSupplier.class, doc);
			com.sun.star.util.XNumberFormatTypes xFormatTypes = (com.sun.star.util.XNumberFormatTypes) UnoRuntime.queryInterface(com.sun.star.util.XNumberFormatTypes.class,
					xFormatsSupplier.getNumberFormats());
			int nFormat = xFormatTypes.getStandardFormat(com.sun.star.util.NumberFormat.DATE, new com.sun.star.lang.Locale());

			com.sun.star.beans.XPropertySet xPropSet = (com.sun.star.beans.XPropertySet) UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, xCell);
			xPropSet.setPropertyValue("NumberFormat", new Integer(nFormat));
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	/**
	 * enables/disables text wrapping for content of specified cell.
	 */
	public void setCellWrap(int r1, int c1, int r2, int c2, boolean wrap) {
		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cell = range.getCellRangeByPosition(c1, r1, c2, r2);

			XPropertySet ps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cell);

			ps.setPropertyValue("IsTextWrapped", new Boolean(wrap));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * sets font color for content of specified cell.
	 */
	public void setFontColor(int r1, int c1, int r2, int c2, long color) {
		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cell = range.getCellRangeByPosition(c1, r1, c2, r2);

			XPropertySet ps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cell);

			ps.setPropertyValue("CharColor", new Long(color));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * sets font name for content of specified cell.
	 */
	public void setFontName(int r1, int c1, int r2, int c2, String font) {
		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cell = range.getCellRangeByPosition(c1, r1, c2, r2);

			XPropertySet ps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cell);

			ps.setPropertyValue("CharFontName", font);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * sets font size for content of specified cell.
	 */
	public void setFontSize(int r1, int c1, int r2, int c2, float size) {
		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cell = range.getCellRangeByPosition(c1, r1, c2, r2);

			XPropertySet ps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cell);

			ps.setPropertyValue("CharHeight", new Float(size));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * sets font weight for content of specified cell.
	 */
	public void setFontWeight(int r1, int c1, int r2, int c2, int weight) {
		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cell = range.getCellRangeByPosition(c1, r1, c2, r2);

			XPropertySet ps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cell);
			switch (weight) {
			case PLAIN:
				ps.setPropertyValue("CharWeight", new Float(FontWeight.NORMAL));
				ps.setPropertyValue("CharPosture", FontSlant.NONE);
				break;
			case BOLD:
				ps.setPropertyValue("CharWeight", new Float(FontWeight.BOLD));
				ps.setPropertyValue("CharPosture", FontSlant.NONE);
				break;
			case ITALIC:
				ps.setPropertyValue("CharWeight", new Float(FontWeight.NORMAL));
				ps.setPropertyValue("CharPosture", FontSlant.ITALIC);
				break;
			case BOLD_ITALIC:
				ps.setPropertyValue("CharWeight", new Float(FontWeight.BOLD));
				ps.setPropertyValue("CharPosture", FontSlant.ITALIC);
				break;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * sets property containing the horizontal alignment of text within the
	 * cells.
	 */
	public void setHorizontalAlign(int r1, int c1, int r2, int c2, int align) {
		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cell = range.getCellRangeByPosition(c1, r1, c2, r2);

			XPropertySet ps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cell);
			switch (align) {
			case LEFT:
				ps.setPropertyValue("HoriJustify", CellHoriJustify.LEFT);
				break;
			case CENTER:
				ps.setPropertyValue("HoriJustify", CellHoriJustify.CENTER);
				break;
			case RIGHT:
				ps.setPropertyValue("HoriJustify", CellHoriJustify.RIGHT);
				break;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * sets property containing the vertical alignment of text within the cells.
	 */
	public void setVerticalAlign(int r1, int c1, int r2, int c2, int align) {
		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cell = range.getCellRangeByPosition(c1, r1, c2, r2);

			XPropertySet ps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cell);
			switch (align) {
			case TOP:
				ps.setPropertyValue("VertJustify", CellVertJustify.TOP);
				break;
			case CENTER:
				ps.setPropertyValue("VertJustify", CellVertJustify.CENTER);
				break;
			case BOTTOM:
				ps.setPropertyValue("VertJustify", CellVertJustify.BOTTOM);
				break;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void setOptimalColWidth(int c1, int c2) {
		try {
			XCellRange r = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange range = r.getCellRangeByPosition(c1, 1, c2, 1);

			XColumnRowRange cr = (XColumnRowRange) UnoRuntime.queryInterface(XColumnRowRange.class, range);
			XTableColumns cols = cr.getColumns();
			XEnumerationAccess ea = (XEnumerationAccess) UnoRuntime.queryInterface(XEnumerationAccess.class, cols);
			for (XEnumeration en = ea.createEnumeration(); en.hasMoreElements();) {

				Object o = en.nextElement();
				XPropertySet ps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, o);
				ps.setPropertyValue("OptimalWidth", new Boolean(true));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void convertToValues() {
		try {
			XSheetCellCursor cursor = sheet.createCursor();
			XUsedAreaCursor used = (XUsedAreaCursor) UnoRuntime.queryInterface(XUsedAreaCursor.class, cursor);
			used.gotoStartOfUsedArea(false);
			used.gotoEndOfUsedArea(true);

			XCellRangeData d = (XCellRangeData) UnoRuntime.queryInterface(XCellRangeData.class, cursor);
			Object[][] data = d.getDataArray();

			d.setDataArray(data);
		} catch (java.lang.Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void replaceFunction(int r1, int c1, int r2, int c2, String fun, String replace, boolean completely) {
		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cells = range.getCellRangeByPosition(c1, r1, c2, r2);
			XReplaceable replaceable = (XReplaceable) UnoRuntime.queryInterface(XReplaceable.class, cells);
			XReplaceDescriptor desc = replaceable.createReplaceDescriptor();
			desc.setSearchString(fun);
			desc.setReplaceString(replace);
			replaceable.replaceAll(desc);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public int getLastCol() {
		try {
			XSheetCellCursor cursor = sheet.createCursor();
			XUsedAreaCursor used = (XUsedAreaCursor) UnoRuntime.queryInterface(XUsedAreaCursor.class, cursor);
			used.gotoStartOfUsedArea(false);
			used.gotoEndOfUsedArea(true);

			XCellRangeAddressable r = (XCellRangeAddressable) UnoRuntime.queryInterface(XCellRangeAddressable.class, cursor);

			return r.getRangeAddress().EndColumn;
		} catch (java.lang.Exception e) {
			log.error(e.getMessage(), e);
			return 1000;
		}
	}

	public int getLastRow() {
		try {
			XSheetCellCursor cursor = sheet.createCursor();
			XUsedAreaCursor used = (XUsedAreaCursor) UnoRuntime.queryInterface(XUsedAreaCursor.class, cursor);
			used.gotoStartOfUsedArea(false);
			used.gotoEndOfUsedArea(true);

			XCellRangeAddressable r = (XCellRangeAddressable) UnoRuntime.queryInterface(XCellRangeAddressable.class, cursor);

			return r.getRangeAddress().EndRow;
		} catch (java.lang.Exception e) {
			log.error(e.getMessage(), e);
			return 1000;
		}
	}

	public void setHeader(int row) {
		setHeaderFooter(row, true);
	}

	public void setFooter(int row) {
		setHeaderFooter(row, false);
	}

	private void setHeaderFooter(int row, boolean header) {

		String label = header ? "@header@" : "@footer@";
		String note = header ? "Header" : "Footer";
		int shift = header ? 1 : -1;

		try {
			XPropertySet docProp = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, doc);
			Object rangesObj = docProp.getPropertyValue("DatabaseRanges");
			XNameAccess ranges = (XNameAccess) UnoRuntime.queryInterface(XNameAccess.class, rangesObj);

			XDatabaseRange range = null;
			try {
				range = (XDatabaseRange) getAnyObject(ranges.getByName(label));
			} catch (Exception ex) {
			} // Ignore

			int prevRow = -1;
			if (range != null) {
				CellRangeAddress addr = range.getDataArea();
				prevRow = addr.StartRow;

				removeNote(new CellAddress((short) 0, addr.StartColumn, addr.StartRow - shift));

				XDatabaseRanges dbs = (XDatabaseRanges) getAnyObject(rangesObj);
				dbs.removeByName(label);
			}

			if (prevRow != row + shift) {
				addDatabaseRange(label, row + shift, 0, row + shift, 0);
				insertNote(new CellAddress((short) 0, 0, row), note);
			}
		} catch (java.lang.Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public int getHeader() {
		return getDatabaseRangeValue("@header@", Spreadsheet.START_ROW);
	}

	public int getFooter() {
		return getDatabaseRangeValue("@footer@", Spreadsheet.START_ROW);
	}

	private void removeNote(CellAddress addr) {
		try {
			XSheetAnnotationsSupplier sup = (XSheetAnnotationsSupplier) UnoRuntime.queryInterface(XSheetAnnotationsSupplier.class, sheet);

			XSheetAnnotations ans = sup.getAnnotations();

			for (int i = 0; i < ans.getCount(); i++) {
				XSheetAnnotation an = (XSheetAnnotation) getAnyObject(ans.getByIndex(i));
				if ((addr.Row == an.getPosition().Row) && (addr.Column == an.getPosition().Column)) {
					ans.removeByIndex(i);
					break;
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void insertNote(CellAddress addr, String note) {
		try {
			XSheetAnnotationsSupplier sup = (XSheetAnnotationsSupplier) UnoRuntime.queryInterface(XSheetAnnotationsSupplier.class, sheet);
			XSheetAnnotations ans = sup.getAnnotations();
			ans.insertNew(addr, note);

			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);

			XCell cell = range.getCellByPosition(addr.Column, addr.Row);
			XSheetAnnotationAnchor xAnnotAnchor = (XSheetAnnotationAnchor) UnoRuntime.queryInterface(XSheetAnnotationAnchor.class, cell);
			XSheetAnnotation xAnnotation = xAnnotAnchor.getAnnotation();
			xAnnotation.setIsVisible(true);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void setProtected(boolean protect, String pass) {
		try {
			XProtectable prot = (XProtectable) UnoRuntime.queryInterface(XProtectable.class, sheet);
			if (protect) {
				prot.protect(pass);
			} else {
				prot.unprotect(pass);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void setCellProtected(int r1, int c1, boolean protect) {
		try {
			// if (protect) {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cells = range.getCellRangeByPosition(c1, r1, c1, r1);

			XPropertySet ps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cells);

			CellProtection prot = (CellProtection) ps.getPropertyValue("CellProtection");

			prot.IsLocked = protect;
			ps.setPropertyValue("CellProtection", prot);
			// }
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void setCellNumberValidity(int r1, int c1) {

		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cells = range.getCellRangeByPosition(c1, r1, c1, r1);

			XPropertySet xCellPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cells);

			XPropertySet xValidPropSet = (XPropertySet) getAnyObject(xCellPropSet.getPropertyValue("Validation"));

			xValidPropSet.setPropertyValue("Type", ValidationType.DECIMAL);
			xValidPropSet.setPropertyValue("ShowErrorMessage", new Boolean(true));
			xValidPropSet.setPropertyValue("ErrorMessage", "This is an invalid value!");
			xValidPropSet.setPropertyValue("ErrorAlertStyle", ValidationAlertStyle.STOP);

			// condition
			XSheetCondition xCondition = (XSheetCondition) UnoRuntime.queryInterface(XSheetCondition.class, xValidPropSet);
			xCondition.setOperator(ConditionOperator.NOT_EQUAL);
			xCondition.setFormula1("9999999999999999999999999.0");

			// apply on cell range
			xCellPropSet.setPropertyValue("Validation", xValidPropSet);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void setCellNumberValidity(int r1, int c1, int r2, int c2) {
		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cells = range.getCellRangeByPosition(c1, r1, c2, r2);

			XPropertySet xCellPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cells);

			XPropertySet xValidPropSet = (XPropertySet) getAnyObject(xCellPropSet.getPropertyValue("Validation"));

			xValidPropSet.setPropertyValue("Type", ValidationType.DECIMAL);
			xValidPropSet.setPropertyValue("ShowErrorMessage", new Boolean(true));
			xValidPropSet.setPropertyValue("ErrorMessage", "This is an invalid value!");
			xValidPropSet.setPropertyValue("ErrorAlertStyle", ValidationAlertStyle.STOP);

			// condition
			XSheetCondition xCondition = (XSheetCondition) UnoRuntime.queryInterface(XSheetCondition.class, xValidPropSet);
			xCondition.setOperator(ConditionOperator.NOT_EQUAL);
			xCondition.setFormula1("9999999999999999999999999.0");

			// apply on cell range
			xCellPropSet.setPropertyValue("Validation", xValidPropSet);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void setCellDateValidity(int r1, int c1) {

		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cells = range.getCellRangeByPosition(c1, r1, c1, r1);

			XPropertySet xCellPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cells);

			XPropertySet xValidPropSet = (XPropertySet) getAnyObject(xCellPropSet.getPropertyValue("Validation"));

			xValidPropSet.setPropertyValue("Type", ValidationType.DATE);
			xValidPropSet.setPropertyValue("ShowErrorMessage", new Boolean(true));
			xValidPropSet.setPropertyValue("ErrorMessage", "This is an invalid value!");
			xValidPropSet.setPropertyValue("ErrorAlertStyle", ValidationAlertStyle.STOP);

			// condition
			XSheetCondition xCondition = (XSheetCondition) UnoRuntime.queryInterface(XSheetCondition.class, xValidPropSet);
			xCondition.setOperator(ConditionOperator.NOT_EQUAL);

			// apply on cell range
			xCellPropSet.setPropertyValue("Validation", xValidPropSet);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void setName(String name) {
		ooBean.setName(name);
	}

	public void setDataArray(int r1, int c1, int r2, int c2, Object[][] data) {
		try {

			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cells = range.getCellRangeByPosition(c1, r1, c2, r2);
			com.sun.star.sheet.XCellRangeData dataRange = (com.sun.star.sheet.XCellRangeData) UnoRuntime.queryInterface(com.sun.star.sheet.XCellRangeData.class, cells);

			dataRange.setDataArray(data);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public Object[][] getDataArray(int r1, int c1, int r2, int c2) {

		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cells = range.getCellRangeByPosition(c1, r1, c2, r2);
			com.sun.star.sheet.XCellRangeData dataRange = (com.sun.star.sheet.XCellRangeData) UnoRuntime.queryInterface(com.sun.star.sheet.XCellRangeData.class, cells);

			Object[][] d = dataRange.getDataArray();

			return d;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return new Object[0][0];
	}

	public void clearFormulas(int r1, int c1, int r2, int c2) {

		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cells = range.getCellRangeByPosition(c1, r1, c2, r2);
			com.sun.star.sheet.XSheetOperation op = (com.sun.star.sheet.XSheetOperation) UnoRuntime.queryInterface(com.sun.star.sheet.XSheetOperation.class, cells);

			op.clearContents(com.sun.star.sheet.CellFlags.FORMULA);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public Object getFormat() {

		XSpreadsheet sheet = null;
		try {
			XSpreadsheets sheets = doc.getSheets();
			XIndexAccess oIndexSheets = (XIndexAccess) UnoRuntime.queryInterface(XIndexAccess.class, sheets);

			sheet = (XSpreadsheet) oIndexSheets.getByIndex(0);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return sheet;
	}

	public void setFormat(Object format) {

		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cells = range.getCellRangeByPosition(1, 1, 100, 100);

			XPropertySet ps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cells);

			XPropertySet psS = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cells);

			Integer f = (Integer) psS.getPropertyValue("NumberFormat");
			ps.setPropertyValue("NumberFormat", f);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public int getColWidth(int r1, int c1, int r2, int c2) {

		int w = 0;
		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cells = range.getCellRangeByPosition(c1, r1, c2, r2);
			com.sun.star.table.XColumnRowRange xColRowRange = (com.sun.star.table.XColumnRowRange) UnoRuntime.queryInterface(com.sun.star.table.XColumnRowRange.class, cells);
			com.sun.star.table.XTableColumns xColumns = xColRowRange.getColumns();
			Object aColumnObj = xColumns.getByIndex(0);
			XPropertySet xPropSet = (com.sun.star.beans.XPropertySet) UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, aColumnObj);

			w = ((Integer) xPropSet.getPropertyValue("Width")).intValue();

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return w;
	}

	public void setColWidth(int r1, int c1, int r2, int c2, int w) {

		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cells = range.getCellRangeByPosition(c1, r1, c2, r2);
			com.sun.star.table.XColumnRowRange xColRowRange = (com.sun.star.table.XColumnRowRange) UnoRuntime.queryInterface(com.sun.star.table.XColumnRowRange.class, cells);
			com.sun.star.table.XTableColumns xColumns = xColRowRange.getColumns();
			Object aColumnObj = xColumns.getByIndex(0);
			XPropertySet xPropSet = (com.sun.star.beans.XPropertySet) UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, aColumnObj);

			xPropSet.setPropertyValue("Width", new Integer(w));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public String getHorizontalAlign(int r1, int c1, int r2, int c2) {

		String align = "default";
		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cell = range.getCellRangeByPosition(c1, r1, c2, r2);

			XPropertySet ps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cell);

			CellHoriJustify a = (CellHoriJustify) ps.getPropertyValue("HoriJustify");

			if (a.equals(CellHoriJustify.LEFT))
				align = "left";
			if (a.equals(CellHoriJustify.CENTER))
				align = "center";
			if (a.equals(CellHoriJustify.RIGHT))
				align = "right";

			try {
				new Double(this.getCellValue(r1, c1));
				align = "right";
			} catch (Exception e) {
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return align;
	}

	public boolean isBorder(int r1, int c1, int r2, int c2) {

		boolean hasBorder = false;
		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cells = range.getCellRangeByPosition(c1, r1, c2, r2);

			XPropertySet ps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cells);

			TableBorder border = (TableBorder) ps.getPropertyValue("TableBorder");

			if ((border.RightLine.OuterLineWidth > 0) && (border.LeftLine.OuterLineWidth > 0) && (border.BottomLine.OuterLineWidth > 0) && (border.TopLine.OuterLineWidth > 0)) {
				hasBorder = true;
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return hasBorder;
	}

	public int getCellType(int r, int c) {

		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCell cell = range.getCellByPosition(c, r);

			switch (cell.getType().getValue()) {
			case CellContentType.EMPTY_value:
				return EMPTY;
			case CellContentType.FORMULA_value:
				return FORMULA;
			case CellContentType.VALUE_value:
				return VALUE;
			case CellContentType.TEXT_value:
				return TEXT;
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return EMPTY;
	}

	public void copyStrings(fina2.ui.sheet.Spreadsheet dest) {

		try {
			XCellRangesQuery query = (XCellRangesQuery) UnoRuntime.queryInterface(XCellRangesQuery.class, sheet);

			XSheetCellRanges ranges = query.queryContentCells((short) CellFlags.STRING);

			CellRangeAddress[] addrs = ranges.getRangeAddresses();
			for (int i = 0; i < addrs.length; i++) {
				Object[][] data = getDataArray(addrs[i].StartRow, addrs[i].StartColumn, addrs[i].EndRow, addrs[i].EndColumn);

				dest.setDataArray(addrs[i].StartRow, addrs[i].StartColumn, addrs[i].EndRow, addrs[i].EndColumn, data);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void print() {

		try {
			executeCommand(".uno:Print");
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	public void copySheetTabs(Spreadsheet source, String name) {

		try {
			OOSheet srcSheet = (OOSheet) source;

			String docURL = ooBean.getDocument().getURL();
			String docName = docURL.substring(docURL.lastIndexOf('/') + 1);

			PropertyValue[] props = new PropertyValue[3];

			props[0] = new PropertyValue();
			props[0].Name = "DocName";
			props[0].Value = docName;
			props[1] = new PropertyValue();
			props[1].Name = "Index";
			props[1].Value = new Integer(32000);
			props[2] = new PropertyValue();
			props[2].Name = "Copy";
			props[2].Value = new Boolean(true);

			srcSheet.executeCommand(".uno:Move", props);

			// Rename newly inserted sheet
			renameLastSheet(name);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void renameSheet(String name) throws Exception {

		XNameAccess nameAccess = (XNameAccess) UnoRuntime.queryInterface(XNameAccess.class, doc.getSheets());
		String[] sheetsNames = nameAccess.getElementNames();

		nameAccess = (XNameAccess) UnoRuntime.queryInterface(XNameAccess.class, doc.getSheets());
		sheetsNames = nameAccess.getElementNames();

		// Remove all sheets except the first one
		for (int i = 1; i < sheetsNames.length; i++) {
			doc.getSheets().removeByName(sheetsNames[i]);
		}
		renameLastSheet(name);
	}

	private void renameLastSheet(String name) throws Exception {

		XSpreadsheets sheets = doc.getSheets();
		XIndexAccess indexAccess = (XIndexAccess) UnoRuntime.queryInterface(XIndexAccess.class, sheets);

		XSpreadsheet lastSheet = (XSpreadsheet) getAnyObject(indexAccess.getByIndex(indexAccess.getCount() - 1));

		XNamed named = (XNamed) UnoRuntime.queryInterface(XNamed.class, lastSheet);
		named.setName(name);
	}

	public void selectSheetByName(String sheetName) throws Exception {

		selection.select(doc.getSheets().getByName(sheetName));
	}

	public void unselect() throws Exception {
		selection.select(null);
	}

	private void executeCommand(String sURL) throws Exception {
		executeCommand(sURL, new PropertyValue[0]);
	}

	private void executeCommand(String sURL, PropertyValue[] lArguments) throws Exception {

		URL[] aURL = new URL[1];
		aURL[0] = new URL();
		aURL[0].Complete = sURL;

		XURLTransformer xParser = (XURLTransformer) UnoRuntime.queryInterface(XURLTransformer.class, ooBean.getMultiServiceFactory().createInstance("com.sun.star.util.URLTransformer"));

		xParser.parseStrict(aURL);

		XDispatch xDispatcher = ooBean.getFrame().queryDispatch(aURL[0], "", FrameSearchFlag.SELF | FrameSearchFlag.CHILDREN);

		if (xDispatcher != null) {
			xDispatcher.dispatch(aURL[0], lArguments);
		}
	}

	public void addModifyListener(int r1, int c1, int r2, int c2, final ModifyListener listener, final boolean isNumber) {

		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCellRange cells = range.getCellRangeByPosition(c1, r1, c2, r2);

			XModifyBroadcaster mb = (XModifyBroadcaster) UnoRuntime.queryInterface(XModifyBroadcaster.class, cells);

			mb.addModifyListener(new XModifyListener() {

				private String newValue = null;

				public void modified(com.sun.star.lang.EventObject eventObject) {

					XCell cell = (XCell) UnoRuntime.queryInterface(XCell.class, eventObject.Source);

					if (isNumber) {

						newValue = Double.toString(cell.getValue());

						// TODO Bug: 3571425
						// newValue = String.valueOf(cell.getValue());
					} else {
						XTextRange text = (XTextRange) UnoRuntime.queryInterface(XTextRange.class, cell);
						newValue = text.getString();
					}

					Thread t = new Thread(new Runnable() {
						public void run() {
							listener.modified(newValue);
						}
					});
					t.start();
				}

				public void disposing(EventObject eo) {
				}
			});

		} catch (java.lang.Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void addModifyListener(int r1, int c1, final ModifyListener listener, final boolean isNumber) {

		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);
			XCell cell = range.getCellByPosition(c1, r1);

			XModifyBroadcaster mb = (XModifyBroadcaster) UnoRuntime.queryInterface(XModifyBroadcaster.class, cell);

			mb.addModifyListener(new XModifyListener() {

				private String newValue = null;

				public void modified(com.sun.star.lang.EventObject eventObject) {

					XCell cell = (XCell) UnoRuntime.queryInterface(XCell.class, eventObject.Source);

					if (isNumber) {

						newValue = Double.toString(cell.getValue());

						// TODO Bug: 3571425
						// newValue = String.valueOf(cell.getValue());

					} else {
						XTextRange text = (XTextRange) UnoRuntime.queryInterface(XTextRange.class, cell);
						newValue = text.getString();
					}

					Thread t = new Thread(new Runnable() {
						public void run() {
							listener.modified(newValue);
						}
					});
					t.start();
				}

				public void disposing(EventObject eo) {
				}
			});

		} catch (java.lang.Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void terminate() {

		try {

			LocalOfficeConnection lc = new LocalOfficeConnection();

			XComponentContext xRemoteContext = lc.getComponentContext();
			XMultiComponentFactory xRemoteServiceManager = xRemoteContext.getServiceManager();

			Object desktop = xRemoteServiceManager.createInstanceWithContext("com.sun.star.frame.Desktop", xRemoteContext);
			XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);

			log.debug("Terminating OO. Result: " + xDesktop.terminate());
			Runtime.getRuntime().exec("tskill soffice");
		} catch (java.lang.Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private boolean showElement(String element, boolean visible) throws WrappedTargetException, UnknownPropertyException, NoConnectionException {

		XLayoutManager xLayoutManager = getLayoutManager();
		if (visible == true) {
			if (!getLayoutManager().isElementVisible(element)) {
				xLayoutManager.createElement(element);
				return xLayoutManager.showElement(element);
			} else {
				return true;
			}
		} else {
			return xLayoutManager.hideElement(element);
		}
	}

	private XLayoutManager getLayoutManager() throws WrappedTargetException, UnknownPropertyException, NoConnectionException {

		XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, ooBean.getFrame());

		XLayoutManager xLayoutManager = (XLayoutManager) UnoRuntime.queryInterface(XLayoutManager.class, xPropSet.getPropertyValue("LayoutManager"));

		return xLayoutManager;
	}

	private Object getAnyObject(Object o) {

		Object result = o;
		if (o instanceof Any) {
			result = ((Any) o).getObject();
		}
		return result;
	}

	public String getVersion() {

		String version = "Undefined";
		try {
			officeConnection = new LocalOfficeConnection();
			officeConnection.setUnoUrl("uno:socket,host=localhost,port=8100;urp;StarOffice.ServiceManager");
			/*
			 * if (xRemoteContext == null) { xRemoteContext =
			 * ooBean.getOOoConnection().getComponentContext(); }
			 */
			xRemoteContext = officeConnection.getComponentContext();

			XMultiComponentFactory xMultiComponentFactory = xRemoteContext.getServiceManager();
			Object oProvider = xMultiComponentFactory.createInstanceWithContext("com.sun.star.configuration.ConfigurationProvider", xRemoteContext);

			XMultiServiceFactory xConfigurationServiceFactory = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, oProvider);

			PropertyValue[] lArgs = new PropertyValue[1];
			lArgs[0] = new PropertyValue();
			lArgs[0].Name = "nodepath";
			lArgs[0].Value = "/org.openoffice.Setup/Product";

			Object configAccess = xConfigurationServiceFactory.createInstanceWithArguments("com.sun.star.configuration.ConfigurationAccess", lArgs);

			XNameAccess xNameAccess = (XNameAccess) UnoRuntime.queryInterface(XNameAccess.class, configAccess);

			version = xNameAccess.getByName("ooSetupVersion").toString();

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		return version;
	}

	public void exportAsPdf(byte[] b) {
		try {

			log.info("Exporting As Pdf");

			XStorable xStorable = null;

			Object desktop = null;
			Object document = null;

			officeConnection = new LocalOfficeConnection();
			officeConnection.setUnoUrl("uno:socket,host=localhost,port=8100;urp;StarOffice.ServiceManager");

			xRemoteContext = officeConnection.getComponentContext();
			xRemoteServiceManager = xRemoteContext.getServiceManager();

			// Get a desktop instance
			desktop = xRemoteServiceManager.createInstanceWithContext("com.sun.star.frame.Desktop", xRemoteContext);

			// Get a reference to the desktop interface that can load files
			XComponentLoader loader = (XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class, desktop);

			// Set the document opener to not display an OO window
			PropertyValue[] loaderValues = new PropertyValue[1];
			loaderValues[0] = new PropertyValue();
			loaderValues[0].Name = "Hidden";
			loaderValues[0].Value = new Boolean(true);

			int lastDot = calcUrl.lastIndexOf('.');

			// Open the document in Open Office
			document = loader.loadComponentFromURL(calcUrl, "_blank", 0, loaderValues);

			// Get a reference to the document interface that can store files
			xStorable = (XStorable) UnoRuntime.queryInterface(XStorable.class, document);

			// Set the arguments to save to PDF.
			PropertyValue[] saveArgs = new PropertyValue[2];
			saveArgs[0] = new PropertyValue();
			saveArgs[0].Name = "Overwrite";
			saveArgs[0].Value = new Boolean(true);

			// Choose appropriate output filter
			saveArgs[1] = new PropertyValue();
			saveArgs[1].Name = "FilterName";
			saveArgs[1].Value = "calc_pdf_Export";

			// The converted file will have the same name with a PDF extension
			String sSaveUrl = calcUrl.substring(0, lastDot) + ".pdf";

			// Save the file
			xStorable.storeToURL(sSaveUrl, saveArgs);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	public void initPrintComponents(String sheetName) {
		try {
			officeConnection = new LocalOfficeConnection();
			officeConnection.setUnoUrl("uno:socket,host=localhost,port=8100;urp;StarOffice.ServiceManager");
			xRemoteContext = officeConnection.getComponentContext();
			xRemoteServiceManager = xRemoteContext.getServiceManager();

			XComponentLoader aLoader = (XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class,
					xRemoteServiceManager.createInstanceWithContext("com.sun.star.frame.Desktop", xRemoteContext));

			xComponent = aLoader.loadComponentFromURL("private:factory/scalc", "_blank", 0, new com.sun.star.beans.PropertyValue[0]);
			doc = (com.sun.star.sheet.XSpreadsheetDocument) UnoRuntime.queryInterface(com.sun.star.sheet.XSpreadsheetDocument.class, xComponent);
			XModel model = (XModel) UnoRuntime.queryInterface(XModel.class, xComponent);
			XController controller = model.getCurrentController();
			view = (XSpreadsheetView) UnoRuntime.queryInterface(XSpreadsheetView.class, controller);
			/*
			 * create XFrame
			 */
			// XFrame frame = controller.getFrame();
			// XWindow window = frame.getContainerWindow();
			this.setXSpreadsheet((short) 0, sheetName);
		} catch (MalformedURLException e) {
			log.error(e.getMessage(), e);
		} catch (com.sun.star.uno.Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void setXSpreadsheet(short byIndex, String sheetName) throws com.sun.star.uno.Exception {
		com.sun.star.sheet.XSpreadsheets xSheets = doc.getSheets();

		try {
			xSheets.insertNewByName(sheetName, (short) 0);
		} catch (com.sun.star.uno.RuntimeException ex) {
			xSheets.insertNewByName("NONAME", (short) 0);
			System.out.println(ex.getMessage());
		}

		String[] sheetsName = xSheets.getElementNames();

		for (int i = 1; i < sheetsName.length; i++) {
			xSheets.removeByName(sheetsName[i]);
		}

		com.sun.star.container.XIndexAccess xSheetsIA = (com.sun.star.container.XIndexAccess) UnoRuntime.queryInterface(com.sun.star.container.XIndexAccess.class, xSheets);
		sheet = (com.sun.star.sheet.XSpreadsheet) UnoRuntime.queryInterface(com.sun.star.sheet.XSpreadsheet.class, xSheetsIA.getByIndex(byIndex));

	}

	public void addCellModifyListener(int c1, int r1, final CellModifyListener listener, final boolean isNumber) {
		try {
			XCellRange range = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);

			if (range != null) {
				XCell cells = range.getCellByPosition(c1, r1);

				XModifyBroadcaster mb = (XModifyBroadcaster) UnoRuntime.queryInterface(XModifyBroadcaster.class, cells);

				if (mb != null) {
					mb.addModifyListener(new XModifyListener() {
						private String newValue = null;

						@Override
						public void modified(EventObject arg0) {
							XCell cell = (XCell) UnoRuntime.queryInterface(XCell.class, arg0.Source);

							XCellAddressable addressable = (XCellAddressable) UnoRuntime.queryInterface(XCellAddressable.class, cell);

							final CellAddress cellAddress = addressable.getCellAddress();

							if (isNumber) {
								newValue = Double.toString(cell.getValue());
							} else {
								XTextRange text = (XTextRange) UnoRuntime.queryInterface(XTextRange.class, cell);
								newValue = text.getString();
							}
							Thread t = new Thread(new Runnable() {
								public void run() {
									listener.modified(newValue, cellAddress.Column, cellAddress.Row);
								}
							});
							t.start();
						}

						@Override
						public void disposing(EventObject arg0) {
						}

					});
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error(ex.getMessage(), ex);
		}
	}

	@Override
	public void setHeaderAndFooterCurrentUserName(String userName) throws Exception {

		// Replace Constant
		String constant = "[fina.user.name]";

		// Get the StyleFamiliesSupplier interface of the document
		XStyleFamiliesSupplier xSupplier = (XStyleFamiliesSupplier) UnoRuntime.queryInterface(XStyleFamiliesSupplier.class, doc);

		XNameAccess styleFamilies = xSupplier.getStyleFamilies();

		// Access the 'PageStyles' Family
		XNameContainer xFamily = (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class, styleFamilies.getByName("PageStyles"));

		// Default Style
		String styleName = "Default";

		Object styleObject = xFamily.getByName(styleName);
		XStyle style = (XStyle) UnoRuntime.queryInterface(XStyle.class, styleObject);

		// Get the property set of the style
		XPropertySet xStyleProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, style);

		// activate header and footer
		if (xStyleProps.getPropertyValue("HeaderOn").equals(Boolean.FALSE)) {
			xStyleProps.setPropertyValue("HeaderOn", new Boolean(true));
		}
		if (xStyleProps.getPropertyValue("FooterOn").equals(Boolean.FALSE)) {
			xStyleProps.setPropertyValue("FooterOn", new Boolean(true));
		}

		String leftPageHeaderContent = "LeftPageHeaderContent";
		replaceConstantHeaderAndFooter(leftPageHeaderContent, xStyleProps, constant, userName);

		String leftPageFooterContent = "LeftPageFooterContent";
		replaceConstantHeaderAndFooter(leftPageFooterContent, xStyleProps, constant, userName);

		String rightPageHeaderContent = "RightPageHeaderContent";
		replaceConstantHeaderAndFooter(rightPageHeaderContent, xStyleProps, constant, userName);

		String rightPageFooterContent = "RightPageFooterContent";
		replaceConstantHeaderAndFooter(rightPageFooterContent, xStyleProps, constant, userName);

	}

	private void replaceConstantHeaderAndFooter(String pageHeaderAndFooter, XPropertySet xStyleProps, String constant, String userName) throws Exception {

		XHeaderFooterContent RPHC = (XHeaderFooterContent) AnyConverter.toObject(new Type(XHeaderFooterContent.class), xStyleProps.getPropertyValue(pageHeaderAndFooter));

		XText center = RPHC.getCenterText();
		XText left = RPHC.getLeftText();
		XText right = RPHC.getRightText();

		if (center.getString().contains(constant)) {
			center.setString(center.getString().replace(constant, userName));
		}

		if (left.getString().contains(constant)) {
			left.setString(left.getString().replace(constant, userName));
		}

		if (right.getString().contains(constant)) {
			right.setString(right.getString().replace(constant, userName));
		}

		xStyleProps.setPropertyValue(pageHeaderAndFooter, RPHC);

	}
}
