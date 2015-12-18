package fina2.returns;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.ejb.Handle;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

import com.sun.star.beans.PropertyValue;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.sdbc.XCloseable;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import fina2.i18n.Language;
import fina2.i18n.LanguageHome;
import fina2.i18n.LanguagePK;
import fina2.metadata.MDTConstants;
import fina2.ui.sheet.ModifyListener;
import fina2.ui.sheet.Spreadsheet;
import fina2.ui.sheet.SpreadsheetsManager;
import fina2.ui.sheet.openoffice.OOSheet;
import fina2.ui.table.TableRow;
import fina2.ui.table.TableRowImpl;

public class ReturnSheet {

	private Handle languageHandle = null;

	private Spreadsheet sheet;

	private boolean review;

	private String versionCode;

	private boolean autoCalculate;

	private ReturnPK returnPK;

	private TableRow row;

	private boolean returnChanged;

	private Collection defTables;

	private HashMap modifyListeners = new HashMap();

	private static Logger log = Logger.getLogger(ReturnSheet.class);

	private Vector t;

	public ReturnSheet(boolean review, int id) {
		this.review = review;

		try {
			log.info("Initilizing Return Sheet");
			sheet = SpreadsheetsManager.getInstance().createSpreadsheet();
			log.info("Return Sheet Initialized");
		} catch (Exception ex) {
			log.error(ex);
		}

	}

	public static InitialContext getInitialContext() throws NamingException {
		Properties p = new Properties();
		p.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
		p.put("java.naming.provider.url", "localhost");
		p.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
		return new InitialContext(p);
	}

	@SuppressWarnings("unchecked")
	public void initReturnSheet(ReturnPK pk, TableRow row, String fontName, int fontSize) {

		// TODO setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		autoCalculate = false;

		this.returnPK = pk;
		this.versionCode = row.getValue(5);
		this.row = row;
		this.returnChanged = false;
		
		
		OOSheet oo = ((OOSheet) sheet);
		
		
		// TODO setTitle();
		// TODO versionCodeField.setText(this.versionCode);
		// TODO updateButtonsStates();

		double number = 9999999.00;
	
		Vector t = new Vector();

		try {
			InitialContext jndi = getInitialContext();
			Object ref = jndi.lookup("fina2/returns/ReturnSession");
			ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);
			ReturnSession session = home.create();

			Object langRef = jndi.lookup("fina2/i18n/Language");
			LanguageHome _home = (LanguageHome) PortableRemoteObject.narrow(langRef, LanguageHome.class);
			Language lang = _home.findByPrimaryKey(new LanguagePK(1));
			languageHandle = lang.getHandle();

			defTables = session.getReturnTables(languageHandle, pk);
			
			
			
			byte[] returnFormat = session.getReturnReviewFormat(pk);
			if (returnFormat != null) {
				sheet.readAndHide(returnFormat);
			} else {
				sheet.loadBlank();
				sheet.removeRange(0, 0, sheet.getLastRow(), sheet.getLastCol(), sheet.REMOVE_ROWS);
			}
			
			 sheet.showGrid(false);
			// sheet.setViewMode(sheet.VIEW_SIMPLE);
			 sheet.showSheetTabs(false);
			 sheet.setProtected(false, "fina");

			boolean hasFormat = true;
			if (sheet.getLastRow() == 0 && sheet.getLastCol() == 0) {
				hasFormat = false;
			}

			TableRow addData = session.getReturnAdditionalData(pk, row.getValue(5), languageHandle);
			
			int j = 1;
			
			
			// Bank name and code
			sheet.setCellValue(j++, 1, addData.getValue(0) + " (" + row.getValue(4) + ")");
			// Return definition description and code
			sheet.setCellValue(j++, 1, row.getValue(3) + " (" + row.getValue(0) + ")");
			// Period to - from Period description and code
			sheet.setCellValue(j++, 1, row.getValue(1) + "-" + row.getValue(2) + " " + addData.getValue(1) + " (" + addData.getValue(2) + ")");
			// Version description and code
			sheet.setCellValue(j++, 1, addData.getValue(3) + " (" + row.getValue(5) + ")");
			// Return status
			sheet.setCellValue(j++, 1, row.getValue(6));

			if (!hasFormat) {
				sheet.setFontWeight(1, 1, j - 1, 1, Spreadsheet.BOLD);
			}

			j += 2;

			prepareAutoProccess();

			int defTableIndex = 1;
			for (Iterator iter = defTables.iterator(); iter.hasNext(); defTableIndex++) {

				DefinitionTable table = (DefinitionTable) iter.next();

				Table tabl = new Table();

				if (table.getType() != ReturnConstants.TABLETYPE_NORMAL)
					sheet.setCellValue(j, 1, table.getNodeName());
				else
					sheet.setCellValue(j, 1, table.getCode());

				if (!hasFormat) {
					sheet.setFontWeight(j, 1, Spreadsheet.BOLD);
				}

				j += 2;

				Collection rows = session.getTableValuesRows(languageHandle, returnPK, table.getNode(), versionCode);

				// Normal
				if (table.getType() == ReturnConstants.TABLETYPE_NORMAL) {

					sheet.setCellValue(j, 2, table.getNodeName());

					ValuesTableRow title = (ValuesTableRow) ((Vector) rows).get(0);
					rows.remove(title);
					Object[][] data = null;
					data = new Object[rows.size()][title.getColumnCount()];

					j++;

					tabl.start_row = j;
					tabl.start_col = 1;

					int rowN = j;
					int rc = 0;

					for (Iterator _iter = rows.iterator(); _iter.hasNext();) {
						ValuesTableRow roww = (ValuesTableRow) _iter.next();

						data[rc][0] = new String((String) roww.getValue(0));
						if (roww.getType(1) == MDTConstants.NODETYPE_INPUT || review) {

							sheet.setCellProtected(j, 2, false);
							if ((roww.getDataType(1) == MDTConstants.DATATYPE_NUMERIC) || (roww.getType(1) == MDTConstants.NODETYPE_VARIABLE)) {

								sheet.setCellNumberValidity(j, 2);
								if ((!roww.getValue(1).equals("")) && (!roww.getValue(1).equals("undefined")) && (!roww.getValue(1).equals("Infinity")) && (!roww.getValue(1).equals("NaN")) && (!roww.getValue(1).equals("X"))) {
									try {
										/*
										 * number = Double.valueOf(
										 * roww.getValue(1)).doubleValue();
										 */
										number = Double.valueOf(roww.getValue(1).replace(',', '.'));
									} catch (Exception e) {
										number = Double.NaN;
									}
								} else {
									number = 0.0;
								}

								if (Double.isNaN(number)) {
									data[rc][1] = new String((String) roww.getValue(1));
								} else {
									data[rc][1] = new Double(number);
								}
							} else if (roww.getDataType(1) == MDTConstants.DATATYPE_DATE) {
								data[rc][1] = new com.sun.star.util.Date();
								if (roww.getType(1) == MDTConstants.NODETYPE_INPUT) {
									sheet.setCellProtected(2, j, false);
									sheet.setDateFormat(2, j, 2, j, null);
									sheet.setCellDateValidity(2, j);
								}
							} else {
								data[rc][1] = (String) roww.getValue(1);

							}

							addModifyListener(j, 2, roww.getNodeID(1), -1, defTableIndex, (roww.getDataType(1) == MDTConstants.DATATYPE_NUMERIC));
						} else {
							data[rc][1] = new Double(0.0);
						}

						j++;
						rc++;

					}

					tabl.end_row = j - 1;
					tabl.end_col = 2;
					tabl.rows = new Vector(rows);
					tabl.type = table.getType();
					t.add(tabl);

					sheet.setDataArray(rowN, 1, j - 1, 2, data);

					if (!hasFormat) {
						sheet.setCellWrap(rowN - 1, 2, true);
						sheet.setHorizontalAlign(rowN - 1, 2, Spreadsheet.CENTER);
						sheet.setVerticalAlign(rowN - 1, 2, Spreadsheet.CENTER);
						sheet.setFontWeight(rowN - 1, 2, rowN - 1, 2, Spreadsheet.BOLD);
						sheet.setBorder(rowN - 1, 1, j - 1, 2, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, (short) 6);
					}
					j += 2;
				}

				// Multiple
				if (table.getType() == ReturnConstants.TABLETYPE_MULTIPLE) {

					int rowN = j;
					int rc = 0;
					ValuesTableRow title = (ValuesTableRow) ((Vector) rows).get(0);

					Object[][] data = null;
					data = new Object[rows.size()][title.getColumnCount()];

					rows.remove(title);

					for (int i = 0; i < title.getColumnCount(); i++) {
						data[rc][i] = new String((String) title.getValue(i));
					}
					rc++;
					j++;

					tabl.start_row = j;
					tabl.start_col = 1;

					for (Iterator _iter = rows.iterator(); _iter.hasNext();) {
						ValuesTableRow roww = (ValuesTableRow) _iter.next();
						for (int i = 0; i < title.getColumnCount(); i++) {
							if (i > 0) {
								if (roww.getType(i) == MDTConstants.NODETYPE_INPUT || review) {

									sheet.setCellProtected(j, i + 1, false);
									if ((roww.getDataType(i) == MDTConstants.DATATYPE_NUMERIC) || (roww.getType(i) == MDTConstants.NODETYPE_VARIABLE)) {

										if ((!roww.getValue(i).equals("")) && (!roww.getValue(i).equals("undefined")) && (!roww.getValue(i).equals("Infinity")) && (!roww.getValue(i).equals("NaN")) && (!roww.getValue(i).equals("X"))) {
											try {

												number = Double.valueOf(roww.getValue(i)).doubleValue();

												number = Double.valueOf(roww.getValue(i).replace(',', '.'));
											} catch (Exception e) {
												number = Double.NaN;
											}
										} else {
											number = 0.0;
										}

										sheet.setCellNumberValidity(j, i + 1);
										if (Double.isNaN(number)) {
											data[rc][i] = new String((String) roww.getValue(i));
										} else {
											data[rc][i] = new Double(number);
										}

									} else if (roww.getDataType(i) == MDTConstants.DATATYPE_DATE) {
										if (roww.getType(i) == MDTConstants.NODETYPE_INPUT) {
											sheet.setCellProtected(i + 1, j, false);
											sheet.setDateFormat(i + 1, j, i + 1, j, null);
											sheet.setCellDateValidity(i + 1, j);
										}
										data[rc][i] = new com.sun.star.util.Date();
									} else {
										data[rc][i] = (String) roww.getValue(i);
									}

									addModifyListener(j, i + 1, roww.getNodeID(i), -1, defTableIndex, (roww.getDataType(i) == MDTConstants.DATATYPE_NUMERIC));

								} else {
									data[rc][i] = new Double(0.0);
								}
							} else {
								data[rc][i] = (String) roww.getValue(i);
							}
						}
						j++;
						rc++;
					}

					tabl.end_row = j - 1;
					tabl.end_col = title.getColumnCount();
					tabl.rows = new Vector(rows);
					tabl.type = table.getType();
					t.add(tabl);

					sheet.setDataArray(rowN, 1, j - 1, title.getColumnCount(), data);

					if (!hasFormat) {
						sheet.setCellWrap(rowN, 1, rowN, title.getColumnCount(), true);
						sheet.setHorizontalAlign(rowN, 1, rowN, title.getColumnCount(), Spreadsheet.CENTER);
						sheet.setVerticalAlign(rowN, 1, rowN, title.getColumnCount(), Spreadsheet.CENTER);
						sheet.setFontSize(rowN, 1, rowN, title.getColumnCount(), fontSize);
						sheet.setFontWeight(rowN, 1, rowN, title.getColumnCount(), Spreadsheet.BOLD);

						sheet.setBorder(rowN, 1, j - 1, title.getColumnCount(), sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, (short) 6);
					}
					j += 2;
				}

				// Variable
				if (table.getType() == ReturnConstants.TABLETYPE_VARIABLE) {

					int rowN = j;

					int rc = 0;
					ValuesTableRow title = (ValuesTableRow) ((Vector) rows).get(0);

					Object[][] data = null;
					if (table.getEvalType() != 0) {
						data = new Object[rows.size() + 1][title.getColumnCount()];
					} else {
						data = new Object[rows.size()][title.getColumnCount()];
					}

					rows.remove(title);
					for (int i = 0; i < title.getColumnCount(); i++) {
						data[rc][i] = new String((String) title.getValue(i));

					}
					j++;
					rc++;

					tabl.start_row = j;
					tabl.start_col = 1;

					int rN = 0;
					int tableStartRow = j;
					if (rows.size() == 0) {
						sheet.removeRange(j, 0, j + 1, title.getColumnCount() + 1, sheet.REMOVE_UP);

					}
					for (Iterator _iter = rows.iterator(); _iter.hasNext(); rN++) {

						if (rN > 0) {
							sheet.insertRange(j, 0, j, title.getColumnCount() + 1, sheet.INSERT_DOWN);
						}

						ValuesTableRow roww = (ValuesTableRow) _iter.next();
						for (int i = 0; i < title.getColumnCount(); i++) {
							if (roww.getType(i) == MDTConstants.NODETYPE_INPUT || review) {
								sheet.setCellProtected(j, i + 1, false);

								if (roww.getDataType(i) == MDTConstants.DATATYPE_NUMERIC || (roww.getType(1) == MDTConstants.NODETYPE_VARIABLE)) {

									if ((!roww.getValue(i).equals("")) && (!roww.getValue(i).equals("undefined")) && (!roww.getValue(i).equals("Infinity")) && (!roww.getValue(i).equals("NaN")) && (!roww.getValue(i).equals("X"))) {
										try {
											/*
											 * number = Double.valueOf(
											 * roww.getValue(i)) .doubleValue();
											 */
											number = Double.valueOf(roww.getValue(i).replace(',', '.'));
										} catch (Exception e) {
											number = Double.NaN;
										}
									} else {
										number = 0.0;
									}

									sheet.setCellNumberValidity(j, i + 1);
									if (Double.isNaN(number)) {
										data[rc][i] = new String((String) roww.getValue(i));
									} else {
										data[rc][i] = new Double(number);
									}

								} else if (roww.getDataType(i) == MDTConstants.DATATYPE_DATE) {
									data[rc][i] = new com.sun.star.util.Date();
									sheet.setCellProtected(i + 1, j, false);
									sheet.setDateFormat(i + 1, j, i + 1, j, null);
									sheet.setCellDateValidity(j, i + 1);
								} else {
									data[rc][i] = new String(roww.getValue(i));
									if (table.getEvalType() != 0) {
										data[rc + 1][i] = new String("");
									}
								}

								addModifyListener(j, i + 1, roww.getNodeID(i), rN, defTableIndex, (roww.getDataType(i) == MDTConstants.DATATYPE_NUMERIC));
							} else {
								data[rc][i] = new Double(0.0);
								if (table.getEvalType() != 0) {
									data[rc + 1][i] = new String("");
								}
							}

							if (table.getEvalType() != 0 && ((roww.getType(i) == MDTConstants.NODETYPE_INPUT && roww.getDataType(i) == MDTConstants.DATATYPE_NUMERIC) || roww.getType(i) == MDTConstants.NODETYPE_VARIABLE)) {
								String colChar = null;
								if ((i + 1) < 26) {
									colChar = new String(new char[] { (char) ((int) 'A' + i + 1) });
								} else if ((i + 1) == 26) {
									colChar = "AA";
								} else {
									colChar = "A".concat(new String(new char[] { (char) ((int) 'A' + i % 25) }));
								}

								String fun = "=SUM";
								switch (table.getEvalType()) {
								case ReturnConstants.EVAL_AVERAGE:
									fun = "=AVERAGE";
									break;
								case ReturnConstants.EVAL_MIN:
									fun = "=MIN";
									break;
								case ReturnConstants.EVAL_MAX:
									fun = "=MAX";
									break;
								}
								data[rc + 1][i] = new String(fun + "(" + colChar + (tableStartRow + 1) + ":" + colChar + (j + 1) + ")");
							}
						}
						j++;
						rc++;
					}

					if (rows.size() == 1) {
						ValuesTableRow roww = (ValuesTableRow) rows.iterator().next();
						if (roww.isBlank()) {
							ArrayList items = new ArrayList();
							for (int i = 0; i < title.getColumnCount(); i++) {
								items.add(new Long(roww.getNodeID(i)));
							}
							insertRow(0, items, defTableIndex);
						}
					}

					if (rows.size() > 0) {
						j++;
					}

					if (table.getEvalType() != 0) {

						if (rows.size() > 0) {
							sheet.setDataArray(rowN, 1, j - 1, title.getColumnCount(), data);
						}
						if (!hasFormat) {
							sheet.setCellWrap(rowN, 1, rowN, title.getColumnCount(), true);
							sheet.setHorizontalAlign(rowN, 1, rowN, title.getColumnCount(), Spreadsheet.CENTER);
							sheet.setVerticalAlign(rowN, 1, rowN, title.getColumnCount(), Spreadsheet.CENTER);
							sheet.setFontSize(rowN, 1, rowN, title.getColumnCount(), fontSize);
							sheet.setFontWeight(rowN, 1, rowN, title.getColumnCount(), Spreadsheet.BOLD);
							sheet.setBorder(rowN, 1, j - 1, title.getColumnCount(), sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, (short) 6);
						}
					} else {
						if (rows.size() > 0) {

							sheet.setDataArray(rowN, 1, j - 2, title.getColumnCount(), data);
							sheet.removeRange(j - 1, 0, j - 1, title.getColumnCount() + 1, sheet.REMOVE_UP);
							if (!hasFormat) {
								sheet.setCellWrap(rowN, 1, rowN, title.getColumnCount(), true);
								sheet.setHorizontalAlign(rowN, 1, rowN, title.getColumnCount(), Spreadsheet.CENTER);
								sheet.setVerticalAlign(rowN, 1, rowN, title.getColumnCount(), Spreadsheet.CENTER);
								sheet.setFontSize(rowN, 1, rowN, title.getColumnCount(), fontSize);
								sheet.setFontWeight(rowN, 1, rowN, title.getColumnCount(), Spreadsheet.BOLD);
								sheet.setBorder(rowN, 1, j - 2, title.getColumnCount(), sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, (short) 6);
							}
						}
					}

					if (rows.size() > 0) {
						tabl.end_row = j;
						if (table.getEvalType() != 0) {
							tabl.end_row = j - 1;
						}
					} else {
						tabl.end_row = tabl.start_row;
					}

					if (table.getEvalType() == 0) {
						j--;
					}

					tabl.end_col = title.getColumnCount();

					tabl.rows = new Vector(rows);
					tabl.type = table.getType();
					t.add(tabl);

					if ((table.getEvalType() != 0) && (rows.size() > 0)) {
						for (int ii = 0; ii < title.getColumnCount(); ii++) {

							if ((data[rc][ii]).toString().startsWith("=")) {
								sheet.setCellFormula(j - 1, ii + 1, (String) data[rc][ii]);
							} else {
								sheet.setCellValue(j - 1, ii + 1, "");
							}

						}
					}
					j += 2;
				}
			}

			updateAutoProcessedValues();
			autoCalculate = true;

			if (!hasFormat) {
				sheet.setFontName(0, 0, sheet.getLastRow(), sheet.getLastCol(), fontName);
				sheet.setFontSize(0, 0, sheet.getLastRow(), sheet.getLastCol(), fontSize);
				sheet.setNumberFormat(0, 0, sheet.getLastRow(), sheet.getLastCol(), lang.getNumberFormat());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		sheet.showGrid(false);
		//sheet.setViewMode(sheet.VIEW_SIMPLE);
		sheet.showSheetTabs(false);

		// TODO if (!isVCTTable()) {
		// TODO insertButton.setEnabled(false);
		// TODO deleteButton.setEnabled(false);
		// TODO } else {
		// TODO insertButton.setEnabled(true);
		// TODO deleteButton.setEnabled(true);
		// TODO }

		if (review == true) {

			sheet.setProtected(true, "fina");
			
			byte b[] = oo.getDocumentContent();
			
			File f = new File(OOSheet.calcUrl.replace("file:///", "").replace("%20", " "));
			try {
				f.createNewFile();
				FileOutputStream fos = new FileOutputStream(f);
				fos.write(b);
				fos.close();
			    sheet.exportAsPdf(b);  	
               
			     	
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			

		}

		// TODO setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	private void prepareAutoProccess() {
		if (review) {
			return;
		}

		try {
			Object ref = fina2.Main.main.getJndiContext().lookup("fina2/returns/ProcessSession");
			ProcessSessionHome phome = (ProcessSessionHome) PortableRemoteObject.narrow(ref, ProcessSessionHome.class);

			ProcessSession psession = phome.create();
			psession.prepareAutoProcess(sheet.hashCode(), returnPK.getId(), versionCode);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void addModifyListener(int r, int c, long nodeId, int rowNumber, int defTableIndex, boolean numeric) {

		ModifyListener listener = new InputModifyListener(nodeId, rowNumber);

		ArrayList list = (ArrayList) modifyListeners.get(new Integer(defTableIndex));
		if (list == null) {
			list = new ArrayList();
			modifyListeners.put(new Integer(defTableIndex), list);
		}
		list.add(listener);

		sheet.addModifyListener(r, c, listener, numeric);
	}

	private void insertRow(int rowNumber, ArrayList items, int tableID) {

		if (review) {
			return;
		}

		try {
			Object ref = fina2.Main.main.getJndiContext().lookup("fina2/returns/ProcessSession");
			ProcessSessionHome phome = (ProcessSessionHome) PortableRemoteObject.narrow(ref, ProcessSessionHome.class);

			ProcessSession psession = phome.create();
			psession.insertRow(sheet.hashCode(), tableID, rowNumber, returnPK.getId(), items);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void updateAutoProcessedValues() {
		if (review) {
			return;
		}

		try {
			Object ref = fina2.Main.main.getJndiContext().lookup("fina2/returns/ProcessSession");
			ProcessSessionHome phome = (ProcessSessionHome) PortableRemoteObject.narrow(ref, ProcessSessionHome.class);

			ProcessSession psession = phome.create();
			update(psession.getUpdates(sheet.hashCode(), returnPK.getId(), -1, -1, null));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void update(Hashtable updates) {

		sheet.setProtected(false, "fina");

		Hashtable adjUpdates = new Hashtable();

		for (Iterator iter = updates.values().iterator(); iter.hasNext();) {
			Hashtable vals = (Hashtable) iter.next();
			for (Iterator viter = vals.values().iterator(); viter.hasNext();) {
				ProcessItem item = (ProcessItem) viter.next();

				Hashtable v = (Hashtable) adjUpdates.get(new Long(item.nodeID));
				if (v == null) {
					v = new Hashtable();
					adjUpdates.put(new Long(item.nodeID), v);
				}

				v.put(new Integer(item.rowNumber), item);
			}
		}

		int j = 0;
		for (Iterator iter = defTables.iterator(); iter.hasNext(); j++) {
			DefinitionTable table = (DefinitionTable) iter.next();
			Table tabl = (Table) t.get(j);

			int rIndex = 0;
			for (Iterator rowIter = tabl.rows.iterator(); rowIter.hasNext(); rIndex++) {

				ValuesTableRow row = (ValuesTableRow) rowIter.next();
				for (int cIndex = 0; cIndex < row.getColumnCount(); cIndex++) {

					Hashtable v = (Hashtable) adjUpdates.get(new Long(row.getNodeID(cIndex)));

					if (v != null) {

						ProcessItem item = null;
						if (table.getType() == ReturnConstants.TABLETYPE_VARIABLE) {
							item = (ProcessItem) v.get(new Integer(rIndex));
						} else {
							item = (ProcessItem) v.values().iterator().next();
						}

						if (item != null) {

							int rowPos = tabl.start_row + rIndex;
							int colPos = tabl.start_col + cIndex;

							double number;
							try {
								if (item.value.equals("undefined") || item.value.equals("Infinity") || item.value.equals("NaN") || item.value.equals("X")) {

									number = 0.0;
								} else {
									number = Double.parseDouble(item.value);
								}

								if (sheet.getCellNumber(rowPos, colPos) != number) {
									sheet.setCellNumber(rowPos, colPos, number);
								}
							} catch (Exception e) {

								if (!sheet.getCellValue(rowPos, colPos).equals(item.value)) {
									sheet.setCellValue(rowPos, colPos, item.value == null ? "" : item.value);
								}
							}
						}
					}
				}
			}
		}

		if (review == false) {
			sheet.setProtected(true, "fina");
		}
	}

	private class Table {

		public int start_row;
		public int start_col;
		public int end_row;
		public int end_col;
		public int type;
		public Vector rows;
	}

	class InputModifyListener implements ModifyListener {

		long nodeId;
		int rowNumber;
		boolean removed;

		InputModifyListener(long nodeId, int rowNumber) {
			this.nodeId = nodeId;
			this.rowNumber = rowNumber;
		}

		InputModifyListener(long nodeId) {
			this(nodeId, 0);
		}

		public void modified(String newValue) {

			if (!autoCalculate || removed || review) {
				return;
			}

			try {
				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi.lookup("fina2/returns/ProcessSession");
				ProcessSessionHome phome = (ProcessSessionHome) PortableRemoteObject.narrow(ref, ProcessSessionHome.class);

				ProcessSession psession = phome.create();

				returnChanged = true;

				long t1 = System.currentTimeMillis();

				Hashtable updates = psession.getUpdates(sheet.hashCode(), returnPK.getId(), nodeId, rowNumber, newValue);
				update(updates);

				log.debug("Updates recalculated in " + (System.currentTimeMillis() - t1) + " ms.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
