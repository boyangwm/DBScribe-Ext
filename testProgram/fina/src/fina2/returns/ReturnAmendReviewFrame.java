package fina2.returns;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import fina2.BaseFrame;
import fina2.Main;
import fina2.i18n.Language;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;
import fina2.metadata.MDTConstants;
import fina2.security.User;
import fina2.ui.ProcessDialog;
import fina2.ui.UIManager;
import fina2.ui.sheet.CellModifyListener;
import fina2.ui.sheet.ModifyListener;
import fina2.ui.sheet.Spreadsheet;
import fina2.ui.sheet.SpreadsheetsManager;
import fina2.ui.table.TableRow;
import fina2.ui.table.TableRowImpl;
import fina2.ui.tree.Node;

public class ReturnAmendReviewFrame extends javax.swing.JFrame {

	private static Logger log = Logger.getLogger(ReturnAmendReviewFrame.class);

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private ReturnPK returnPK;
	private String versionCode;
	private Collection defTables;

	private ReturnManagerFrame returnManagerFrame;
	private boolean packageAmend;

	private ArrayList rows;
	private TableRow row;

	private Spreadsheet sheet;
	private Vector t;
	private boolean autoCalculate = false;

	private HashMap modifyListeners = new HashMap();

	private int currentTabIndex = 0;

	private boolean returnChanged = false;

	private boolean review = false;
	private boolean isOneWorkBookReview = false;

	public ReturnAmendReviewFrame(boolean review) {

		this.review = review;

		setIconImage(ui.getIcon("fina2.icon").getImage());

		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.print", "print.gif");
		ui.loadIcon("fina2.refresh", "refresh.gif");
		ui.loadIcon("fina2.close", "cancel.gif");
		ui.loadIcon("fina2.insert", "insert.gif");
		ui.loadIcon("fina2.delete", "delete.gif");
		ui.loadIcon("fina2.return", "return_table.gif");
		ui.loadIcon("fina2.save", "save.gif");
		ui.loadIcon("fina2.save_process", "save_process.gif");

		try {
			sheet = SpreadsheetsManager.getInstance().createSpreadsheet();

			initComponents();

			if (review == false) {
				splitPane.setLeftComponent(sheet.getComponent());
			} else {
				jPanel3.add(sheet.getComponent());
			}
		} catch (Exception e) {
			Main.generalErrorHandler(e);
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
		BaseFrame.ensureVisible(this);
		splitPane.setDividerLocation(this.getWidth() - this.getWidth() / 6);
	}

	public ReturnAmendReviewFrame(boolean review, boolean isOneWorkBookReview) {
		this.review = review;
		this.isOneWorkBookReview = isOneWorkBookReview;
	}

	public ReturnAmendReviewFrame() {
		this(false);
	}

	protected void initReturnSheet(ReturnPK pk, TableRow row) {
		long l = System.currentTimeMillis();
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		autoCalculate = false;

		SimpleDateFormat sdf;

		this.returnPK = pk;
		this.row = row;
		this.versionCode = row.getValue(5);
		this.row = row;
		this.returnChanged = false;

		setTitle();

		int retPk = returnPK.getId();

		if (!isOneWorkBookReview) {
			versionCodeField.setText(this.versionCode);
		}

		if (!isOneWorkBookReview) {
			updateButtonsStates();
		}

		double number = 9999999.00;

		t = new Vector();

		try {

			Language lang = (Language) main.getLanguageHandle().getEJBObject();

			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/returns/ReturnSession");
			ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);
			ReturnSession session = home.create();

			String dateFormat = lang.getDateFormat();
			int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
			String encoding = lang.getXmlEncoding();
			sdf = new SimpleDateFormat(dateFormat);
			defTables = session.getReturnTables(main.getLanguageHandle(), pk);

			byte[] returnFormat = session.getReturnReviewFormat(pk);

			if (isOneWorkBookReview) {
				if (returnFormat != null) {
					sheet = SpreadsheetsManager.getInstance().createSpreadsheet(returnFormat);
				} else {
					byte[] emptyContent = loadEmptySheet();
					if (emptyContent != null) {
						sheet = SpreadsheetsManager.getInstance().createSpreadsheet(emptyContent);
					} else {
						sheet = SpreadsheetsManager.getInstance().createSpreadsheet();
						sheet.loadBlank();
					}
				}
			} else {
				if (returnFormat != null) {
					sheet.read(returnFormat);
				} else {
					sheet.loadBlank();
					sheet.removeRange(0, 0, sheet.getLastRow(), sheet.getLastCol(), sheet.REMOVE_ROWS);
				}
			}

			sheet.showGrid(false);
			sheet.setViewMode(sheet.VIEW_SIMPLE);
			sheet.showSheetTabs(false);
			// sheet.setProtected(false, "fina");

			boolean hasFormat = true;
			if (sheet.getLastRow() == 0 && sheet.getLastCol() == 0) {
				hasFormat = false;
			}

			TableRow addData = session.getReturnAdditionalData(pk, row.getValue(5), main.getLanguageHandle());

			int j = 1;
			// Bank name and code
			String codeName = row.getValue(4);
			codeName = codeName.replace("[", ",");
			String ss[] = codeName.split(",");
			if (ss.length > 1) {
				codeName = "[" + ss[0] + "] " + ss[1].replace("]", "");
			} else {
				codeName = codeName.replace(",", "[");
			}

			// Return definition description and code
			sheet.setCellValue(j++, 1, "[" + row.getValue(0) + "] " + row.getValue(3));
			// Bank code and description
			sheet.setCellValue(j++, 1, codeName);
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

			if (!review)
				prepareAutoProccess();
			// TODO loop
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
				Collection<ValuesTableRow> rows = session.getTableValuesRows(langID, encoding, retPk, table.getNode(), versionCode, j);

				// Normal
				// int k = j + 1;
				if (table.getType() == ReturnConstants.TABLETYPE_NORMAL) {
					System.out.println("NORMAL");
					long ll = System.currentTimeMillis();
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

					int ntRowIndexer = 0;

					for (Iterator _iter = rows.iterator(); _iter.hasNext();) {
						ValuesTableRow roww = (ValuesTableRow) _iter.next();
						int dataTypeOne = roww.getDataType(1);
						int typeOne = roww.getType(1);
						String valOne = roww.getValue(1);
						data[rc][0] = (String) roww.getValue(0);
						boolean isString = false;
						if (typeOne == MDTConstants.NODETYPE_INPUT || review) {

							sheet.setCellProtected(j, 2, false);
							addCellModifyListener(j, 2, valOne, true);

							if ((dataTypeOne == MDTConstants.DATATYPE_NUMERIC) || (typeOne == MDTConstants.NODETYPE_VARIABLE)) {

								// sheet.setCellNumberValidity(j, 2);
								if ((!valOne.equals("")) && (!valOne.equals("undefined")) && (!valOne.equals("Infinity")) && (!valOne.equals("X"))) {
									try {
										/*
										 * number = Double.valueOf(
										 * roww.getValue(1)).doubleValue();
										 */
										number = stringToDouble(valOne);
									} catch (Exception e) {
										// number = Double.NaN;
										data[rc][1] = valOne;
										isString = true;
									}
								} else {
									number = 0;
								}
								if (isString) {

									isString = false;

								} else if (Double.isNaN(number)) {
									data[rc][1] = "";// valOne;

								} else {
									data[rc][1] = number;
								}
							} else if (dataTypeOne == MDTConstants.DATATYPE_DATE) {
								String s = valOne;
								if (s != null && s.trim().length() != 0) {
									sdf.parse(s);
									data[rc][1] = s;
								} else
									data[rc][1] = "";
								if (typeOne == MDTConstants.NODETYPE_INPUT) {
									sheet.setCellProtected(2, j, false);
									sheet.setDateFormat(2, j, 2, j, dateFormat);
									sheet.setCellDateValidity(2, j);
								}
							} else {
								data[rc][1] = (String) valOne;

							}
							if (!review)
								addModifyListener(j, 2, roww.getNodeID(1), -1, defTableIndex, (typeOne == MDTConstants.DATATYPE_NUMERIC));
						} else {
							data[rc][1] = 0.0d;
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
					for (Iterator _iter = rows.iterator(); _iter.hasNext();) {
						ValuesTableRow roww = (ValuesTableRow) _iter.next();
						if (roww.getDataType(ntRowIndexer) == MDTConstants.DATATYPE_NUMERIC)
							sheet.setCellNumberValidity(j, 2, j - 1, 2);

					}
					j += 2;
					ntRowIndexer++;
					System.out.println("END NORMAL TABLE " + (System.currentTimeMillis() - ll) / 1000);

				}

				// Multiple
				if (table.getType() == ReturnConstants.TABLETYPE_MULTIPLE) {

					System.out.println("MULTIPLE");
					int rowN = j;
					int rc = 0;
					ValuesTableRow title = (ValuesTableRow) ((Vector) rows).get(0);

					Object[][] data = null;
					data = new Object[rows.size()][title.getColumnCount()];

					rows.remove(title);

					for (int i = 0; i < title.getColumnCount(); i++) {
						data[rc][i] = (String) title.getValue(i);
					}
					rc++;
					j++;

					tabl.start_row = j;
					tabl.start_col = 1;

					int mctRowIndexer = 0;

					for (Iterator _iter = rows.iterator(); _iter.hasNext();) {

						ValuesTableRow roww = (ValuesTableRow) _iter.next();
						for (int i = 0; i < title.getColumnCount(); i++) {
							if (i > 0) {
								if (roww.getType(i) == MDTConstants.NODETYPE_INPUT || review) {
									boolean isString = false;

									sheet.setCellProtected(j, i + 1, false);

									if (roww.getType(i) == MDTConstants.NODETYPE_VARIABLE) {
										sheet.setCellProtected(j, i + 1, true);
									} else if (roww.getDataType(i) == MDTConstants.DATATYPE_NUMERIC) {
										sheet.setCellNumberValidity(j, i + 1, j, i + 1);
									}

									addCellModifyListener(j, i + 1, roww.getValue(i) + "", true);

									if ((roww.getDataType(i) == MDTConstants.DATATYPE_NUMERIC) || (roww.getType(i) == MDTConstants.NODETYPE_VARIABLE)) {

										if ((!roww.getValue(i).equals("")) && (!roww.getValue(i).equals("undefined")) && (!roww.getValue(i).equals("Infinity")) && (!roww.getValue(i).equals("X"))) {
											try {
												if (roww.getValue(i).contains("%")) {
													number = stringToDouble(roww.getValue(i).replace("%", ""));
												} else {
													number = stringToDouble(roww.getValue(i));
												}
											} catch (Exception e) {
												data[rc][i] = roww.getValue(i);
												// number = Double.NaN;
												isString = true;
											}
										} else {
											isString = true;
											number = 0.0;
										}
										if (isString) {
											data[rc][i] = roww.getValue(i);

										} else if (Double.isNaN(number)) {
											data[rc][i] = "";// (String)
																// roww.getValue(i);
										} else {

											data[rc][i] = number;
										}

									} else if (roww.getDataType(i) == MDTConstants.DATATYPE_DATE) {
										if (roww.getType(i) == MDTConstants.NODETYPE_INPUT) {
											sheet.setCellProtected(i + 1, j, false);
											sheet.setDateFormat(i + 1, j, i + 1, j, dateFormat);
											sheet.setCellDateValidity(i + 1, j);
										}
										// TODO
										String s = roww.getValue(i);
										if (s != null && s.trim().length() != 0) {
											try {
												sdf.parse(s);
											} catch (ParseException pe) {
												pe.printStackTrace();
												log.error(pe.getMessage(), pe);
											}
											data[rc][i] = s;
										} else
											data[rc][i] = "";
									} else {
										data[rc][i] = (String) roww.getValue(i);
									}
									if (!review)
										addModifyListener(j, i + 1, roww.getNodeID(i), -1, defTableIndex, (roww.getDataType(i) == MDTConstants.DATATYPE_NUMERIC));

								} else {
									data[rc][i] = 0.0d;
								}
							} else {
								data[rc][i] = (String) roww.getValue(i);
							}
						}

						// TODO Bug Return Amend Text Type Modification
						// if ((roww.getDataType(mctRowIndexer) ==
						// MDTConstants.DATATYPE_NUMERIC)) {
						//
						// sheet.setCellNumberValidity(j, 2, j,
						// title.getColumnCount());
						// }

						mctRowIndexer++;

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
						sheet.setFontSize(rowN, 1, rowN, title.getColumnCount(), ui.getFont().getSize());
						sheet.setFontWeight(rowN, 1, rowN, title.getColumnCount(), Spreadsheet.BOLD);

						sheet.setBorder(rowN, 1, j - 1, title.getColumnCount(), sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, (short) 6);
					}
					j += 2;

				}

				// Variable
				if (table.getType() == ReturnConstants.TABLETYPE_VARIABLE) {
					System.out.println("VARIABLE");
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
						data[rc][i] = (String) title.getValue(i);

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

					int count = 0;
					boolean isString = false;
					for (Iterator _iter = rows.iterator(); _iter.hasNext(); rN++) {

						count++;

						if (rN > 0) {
							sheet.insertRange(j, 0, j, title.getColumnCount() + 1, sheet.INSERT_DOWN);
						}

						ValuesTableRow roww = (ValuesTableRow) _iter.next();

						// TODO new Field.
						int rowwIndexer = 0;

						for (int i = 0; i < title.getColumnCount(); i++) {
							if (roww.getType(i) == MDTConstants.NODETYPE_INPUT || review) {
								if (!review) {
									sheet.setCellProtected(j, i + 1, false);
									addCellModifyListener(j, i + 1, roww.getValue(i) + "", true);
								}

								if (roww.getDataType(i) == MDTConstants.DATATYPE_NUMERIC || (roww.getType(1) == MDTConstants.NODETYPE_VARIABLE)) {

									if ((!roww.getValue(i).equals("")) && (!roww.getValue(i).equals("undefined")) && (!roww.getValue(i).equals("Infinity")) && (!roww.getValue(i).equals("X"))) {
										try {
											number = stringToDouble(roww.getValue(i));
										} catch (Exception e) {
											number = Double.NaN;
										}
									} else {
										number = 0.0;
									}
									if (isString) {
										data[rc][i] = roww.getValue(i);
										isString = false;
									} else if (Double.isNaN(number)) {
										data[rc][i] = "";// new String((String)
															// roww.getValue(i));
									} else {
										data[rc][i] = new Double(number);
									}

								} else if (roww.getDataType(i) == MDTConstants.DATATYPE_DATE) {
									String s = roww.getValue(i);
									if (s != null && s.trim().length() != 0) {
										sdf.parse(s);
										data[rc][i] = s;
									} else
										data[rc][i] = "";

									if (!review) {
										sheet.setCellProtected(i + 1, j, false);
										sheet.setCellDateValidity(j, i + 1);
									}
									sheet.setDateFormat(i + 1, j, i + 1, j, dateFormat);

								} else {
									data[rc][i] = (String) roww.getValue(i);
									if (table.getEvalType() != 0) {
										data[rc + 1][i] = "";
									}
								}
								if (!review)
									addModifyListener(j, i + 1, roww.getNodeID(i), rN, defTableIndex, (roww.getDataType(i) == MDTConstants.DATATYPE_NUMERIC));
							} else {
								data[rc][i] = 0.0d;
								if (table.getEvalType() != 0) {
									data[rc + 1][i] = "";
								}
							}

							if (table.getEvalType() != 0
									&& ((roww.getType(i) == MDTConstants.NODETYPE_INPUT && roww.getDataType(i) == MDTConstants.DATATYPE_NUMERIC) || roww.getType(i) == MDTConstants.NODETYPE_VARIABLE)) {
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
								StringBuffer sb = new StringBuffer(fun);
								data[rc + 1][i] = sb.append("(").append(colChar).append(tableStartRow + 1).append(":").append(colChar).append((j + 1)).append(")").toString();
							}
						}

						// TODO Replace J
						if ((roww.getDataType(rowwIndexer) == MDTConstants.DATATYPE_NUMERIC))

							if (!review) {
								sheet.setCellNumberValidity(j, 1, j, title.getColumnCount());
							}

						// TODO indexer Increment
						rowwIndexer++;

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
							sheet.setFontSize(rowN, 1, rowN, title.getColumnCount(), ui.getFont().getSize());
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
								sheet.setFontSize(rowN, 1, rowN, title.getColumnCount(), ui.getFont().getSize());
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

							if ((data[rc][ii]) != null && (data[rc][ii]).toString().startsWith("=")) {
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
				sheet.setFontName(0, 0, sheet.getLastRow(), sheet.getLastCol(), ui.getFont().getName());
				sheet.setFontSize(0, 0, sheet.getLastRow(), sheet.getLastCol(), ui.getFont().getSize());
				sheet.setNumberFormat(0, 0, sheet.getLastRow(), sheet.getLastCol(), lang.getNumberFormat());
			}

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

		sheet.showGrid(false);
		sheet.setViewMode(sheet.VIEW_SIMPLE);
		sheet.showSheetTabs(false);

		if (!isOneWorkBookReview) {
			try {
				User user = (User) main.getUserHandle().getEJBObject();

				String userName = user.getLogin() + "[" + user.getName(main.getLanguageHandle()) + "]";

				sheet.setHeaderAndFooterCurrentUserName(userName);

			} catch (Exception ex) {
				Main.generalErrorHandler(ex);
			}
		}

		if (!isOneWorkBookReview) {
			if (!isVCTTable()) {
				insertButton.setEnabled(false);
				deleteButton.setEnabled(false);
			} else {
				insertButton.setEnabled(true);
				deleteButton.setEnabled(true);
			}
		}

		// if (!review) {
		sheet.setProtected(true, "fina");
		// }

		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		System.out.println("Return Review in " + (System.currentTimeMillis() - l) / 1000);
	}

	private Double stringToDouble(String value) {
		Double result = null;
		try {
			result = Double.parseDouble(value);
		} catch (NumberFormatException ex) {
			log.error(ex.getMessage(), ex);
		}
		return result;
	}

	private byte[] loadEmptySheet() {
		byte[] result = null;
		File emptySheetFile = new File("./resources/empty-sheet.ods");
		result = new byte[(int) emptySheetFile.length()];
		try {
			BufferedInputStream buff = new BufferedInputStream(new FileInputStream(emptySheetFile));
			buff.read(result);
			buff.close();
		} catch (Exception e) {
			result = null;
			log.error(e.getMessage(), e);
		}

		return result;

	}

	private void setTitle() {

		StringBuffer title = new StringBuffer();

		title.append("FinA: ");
		if (packageAmend == true) {
			title.append(ui.getString("fina2.package"));
		} else {
			title.append(ui.getString("fina2.returns.return"));
		}

		title.append(" ");
		if (review) {
			title.append(ui.getString("fina2.review"));
		} else {
			title.append(ui.getString("fina2.amend"));
		}

		title.append(" | ");
		title.append(ui.getString("fina2.bank.bank"));
		title.append(": ");
		title.append(row.getValue(4));
		title.append(" | ");
		title.append(row.getValue(1));
		title.append("-");
		title.append(row.getValue(2));
		title.append(" | ");
		title.append(row.getValue(0));
		title.append("-");
		title.append(row.getValue(3));
		title.append(" | ");
		title.append(row.getValue(7));
		title.append(" | ");
		title.append(ui.getString("fina2.version"));
		title.append(": ");
		title.append(row.getValue(5));

		setTitle(title.toString());
	}

	public void show(ReturnManagerFrame returnManagerFrame, Collection rows, boolean packageAmend) {
		super.show();

		this.row = (TableRow) rows.iterator().next();
		this.rows = new ArrayList(rows);
		this.returnPK = (ReturnPK) row.getPrimaryKey();
		this.returnManagerFrame = returnManagerFrame;
		this.packageAmend = packageAmend;

		setTitle();

		if (this.packageAmend) {

			jReturnBottonsPanel.remove(saveAsButton);

			int index = 0;
			for (Iterator iter = rows.iterator(); iter.hasNext(); index++) {
				TableRow row = (TableRow) iter.next();

				javax.swing.JPanel panel = new javax.swing.JPanel();
				tab.addTab(row.getValue(0), ui.getIcon("fina2.return"), panel);
			}
		} else {
			tab.setVisible(false);
			jPanel1.remove(jPackageButtonsPanel);
		}

		saveAndProcessButton.setVisible(returnManagerFrame.canProcess);
		acceptButton.setVisible(returnManagerFrame.canAccept);
		resetButton.setVisible(returnManagerFrame.canReset);
		rejectButton.setVisible(returnManagerFrame.canReject);

		saveAndProcessPackageButton.setVisible(returnManagerFrame.canProcess);
		acceptPackageButton.setVisible(returnManagerFrame.canAccept);
		resetPackageButton.setVisible(returnManagerFrame.canReset);
		rejectPackageButton.setVisible(returnManagerFrame.canReject);

		auditLogTempMap.clear();

		initReturnSheet(returnPK, row);
		sheet.afterShow();

	}

	public boolean isVCTTable() {
		boolean result = false;
		for (Iterator iter = t.iterator(); iter.hasNext();) {
			Table table = (Table) iter.next();
			if (table.type == ReturnConstants.TABLETYPE_VARIABLE) {
				result = true;
				break;
			}
		}
		return result;
	}

	private void initComponents() {

		saveButton = new javax.swing.JButton();
		saveAsButton = new javax.swing.JButton();
		versionCodeField = new javax.swing.JTextField();
		jPanel4 = new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		jPanel1 = new javax.swing.JPanel();
		insertButton = new javax.swing.JButton();
		deleteButton = new javax.swing.JButton();
		tab = new javax.swing.JTabbedPane();
		resetButton = new javax.swing.JButton();
		acceptButton = new javax.swing.JButton();
		rejectButton = new javax.swing.JButton();
		resetPackageButton = new javax.swing.JButton();
		acceptPackageButton = new javax.swing.JButton();
		rejectPackageButton = new javax.swing.JButton();
		jReturnBottonsPanel = new javax.swing.JPanel();
		jPackageButtonsPanel = new javax.swing.JPanel();
		jReturnVersionPanel = new javax.swing.JPanel();
		jMultipleTablePanel = new javax.swing.JPanel();
		saveAndProcessButton = new javax.swing.JButton();
		saveAndProcessPackageButton = new javax.swing.JButton();
		saveAsPackageButton = new javax.swing.JButton();
		splitPane = new javax.swing.JSplitPane();
		scrollPane = new javax.swing.JScrollPane();
		jPanel3 = new javax.swing.JPanel();

		setFont(ui.getFont());
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

		jPanel4.setLayout(new java.awt.BorderLayout());

		jPanel1.setLayout(new java.awt.GridBagLayout());

		Border versionBorder = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
		Border versionTitleBorder = new TitledBorder(versionBorder, ui.getString("fina2.returns.versionCode"));

		jReturnVersionPanel.setLayout(new java.awt.FlowLayout());
		jReturnVersionPanel.setBorder(versionTitleBorder);

		jPanel1.add(jReturnVersionPanel, UIManager.getGridBagConstraints(0, 0, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(55, 0, 5, 0)));

		versionCodeField.setFont(ui.getFont());
		versionCodeField.setEditable(false);
		versionCodeField.setPreferredSize(new java.awt.Dimension(120, 21));

		jReturnVersionPanel.add(versionCodeField);

		Border returnBorder = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
		Border returnTitleBorder = new TitledBorder(returnBorder, ui.getString("fina2.return"));

		jReturnBottonsPanel.setLayout(new java.awt.GridBagLayout());
		jReturnBottonsPanel.setBorder(returnTitleBorder);

		jPanel1.add(jReturnBottonsPanel, UIManager.getGridBagConstraints(0, 1, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(10, 0, 5, 0)));

		Border packageBorder = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
		Border packageTitleBorder = new TitledBorder(packageBorder, ui.getString("fina2.package"));

		jPackageButtonsPanel.setLayout(new java.awt.GridBagLayout());
		jPackageButtonsPanel.setBorder(packageTitleBorder);

		jPanel1.add(jPackageButtonsPanel, UIManager.getGridBagConstraints(0, 2, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(10, 0, 5, 0)));

		saveButton.setIcon(ui.getIcon("fina2.save"));
		saveButton.setFont(ui.getFont());
		saveButton.setText(ui.getString("fina2.returns.save"));
		saveButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		saveButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveButtonActionPerformed(evt);
			}
		});
		jReturnBottonsPanel.add(saveButton, UIManager.getGridBagConstraints(0, 0, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 5)));

		saveAsButton.setIcon(ui.getIcon("fina2.save"));
		saveAsButton.setFont(ui.getFont());
		saveAsButton.setText(ui.getString("fina2.saveAs"));
		saveAsButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		saveAsButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveAsButtonActionPerformed(evt);
			}
		});
		jReturnBottonsPanel.add(saveAsButton, UIManager.getGridBagConstraints(0, 1, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 5)));

		saveAndProcessButton.setIcon(ui.getIcon("fina2.save_process"));
		saveAndProcessButton.setFont(ui.getFont());
		saveAndProcessButton.setText(ui.getString("fina2.returns.saveAndProcess"));
		saveAndProcessButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		saveAndProcessButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveAndProcessButtonActionPerformed(evt);
			}
		});
		jReturnBottonsPanel.add(saveAndProcessButton, UIManager.getGridBagConstraints(0, 2, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 5)));

		acceptButton.setIcon(ui.getIcon("fina2.accept"));
		acceptButton.setFont(ui.getFont());
		acceptButton.setText(ui.getString("fina2.returns.accept"));
		acceptButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		acceptButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				acceptButtonActionPerformed(evt);
			}
		});
		jReturnBottonsPanel.add(acceptButton, UIManager.getGridBagConstraints(0, 3, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 5)));

		rejectButton.setIcon(ui.getIcon("fina2.reject"));
		rejectButton.setFont(ui.getFont());
		rejectButton.setText(ui.getString("fina2.returns.reject"));
		rejectButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		rejectButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rejectButtonActionPerformed(evt);
			}
		});
		jReturnBottonsPanel.add(rejectButton, UIManager.getGridBagConstraints(0, 4, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 5)));

		resetButton.setIcon(ui.getIcon("fina2.reset"));
		resetButton.setFont(ui.getFont());
		resetButton.setText(ui.getString("fina2.returns.reset"));
		resetButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		resetButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				resetButtonActionPerformed(evt);
			}
		});
		jReturnBottonsPanel.add(resetButton, UIManager.getGridBagConstraints(0, 5, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 5, 5)));

		saveAsPackageButton.setIcon(ui.getIcon("fina2.save"));
		saveAsPackageButton.setFont(ui.getFont());
		saveAsPackageButton.setText(ui.getString("fina2.saveAs"));
		saveAsPackageButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		saveAsPackageButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveAsPackageButtonActionPerformed(evt);
			}
		});
		jPackageButtonsPanel.add(saveAsPackageButton, UIManager.getGridBagConstraints(0, 0, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 5)));

		saveAndProcessPackageButton.setIcon(ui.getIcon("fina2.save_process"));
		saveAndProcessPackageButton.setFont(ui.getFont());
		saveAndProcessPackageButton.setText(ui.getString("fina2.returns.saveAndProcess"));
		saveAndProcessPackageButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		saveAndProcessPackageButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveAndProcessPackageButtonActionPerformed(evt);
			}
		});
		jPackageButtonsPanel.add(saveAndProcessPackageButton, UIManager.getGridBagConstraints(0, 1, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 5)));

		acceptPackageButton.setIcon(ui.getIcon("fina2.accept"));
		acceptPackageButton.setFont(ui.getFont());
		acceptPackageButton.setText(ui.getString("fina2.returns.accept"));
		acceptPackageButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		acceptPackageButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				acceptPackageButtonActionPerformed(evt);
			}
		});
		jPackageButtonsPanel.add(acceptPackageButton, UIManager.getGridBagConstraints(0, 2, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 5)));

		rejectPackageButton.setIcon(ui.getIcon("fina2.reject"));
		rejectPackageButton.setFont(ui.getFont());
		rejectPackageButton.setText(ui.getString("fina2.returns.reject"));
		rejectPackageButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		rejectPackageButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rejectPackageButtonActionPerformed(evt);
			}
		});
		jPackageButtonsPanel.add(rejectPackageButton, UIManager.getGridBagConstraints(0, 3, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 5)));

		resetPackageButton.setIcon(ui.getIcon("fina2.reset"));
		resetPackageButton.setFont(ui.getFont());
		resetPackageButton.setText(ui.getString("fina2.returns.reset"));
		resetPackageButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		resetPackageButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				resetPackageButtonActionPerformed(evt);
			}
		});
		jPackageButtonsPanel.add(resetPackageButton, UIManager.getGridBagConstraints(0, 4, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 5, 5)));

		Border multipleTableBorder = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
		Border multipleTableTitleBorder = new TitledBorder(multipleTableBorder, ui.getString("fina2.returns.multipleTable"));

		jMultipleTablePanel.setLayout(new java.awt.GridBagLayout());
		jMultipleTablePanel.setBorder(multipleTableTitleBorder);

		jPanel1.add(jMultipleTablePanel, UIManager.getGridBagConstraints(0, 4, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(10, 0, 0, 0)));

		insertButton.setIcon(ui.getIcon("fina2.insert"));
		insertButton.setFont(ui.getFont());
		insertButton.setText(ui.getString("fina2.returns.insertRow"));
		insertButton.setPreferredSize(saveAndProcessButton.getPreferredSize());
		insertButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				insertButtonActionPerformed(evt);
			}
		});
		jMultipleTablePanel.add(insertButton, UIManager.getGridBagConstraints(0, 0, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 5)));

		deleteButton.setIcon(ui.getIcon("fina2.delete"));
		deleteButton.setFont(ui.getFont());
		deleteButton.setText(ui.getString("fina2.returns.deleteRow"));
		deleteButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				deleteButtonActionPerformed(evt);
			}
		});
		jMultipleTablePanel.add(deleteButton, UIManager.getGridBagConstraints(0, 1, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 5, 5)));

		jPanel2.add(jPanel1);

		splitPane.setDividerSize(4);

		jPanel3.setLayout(new java.awt.BorderLayout());

		if (review == false) {
			jPanel3.add(splitPane, java.awt.BorderLayout.CENTER);
		}

		jPanel3.add(tab, java.awt.BorderLayout.SOUTH);

		tab.setFont(ui.getFont());
		tab.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
		tab.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

		tab.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				returnChanged();
			}
		});

		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setMinimumSize(new Dimension(10, 50));
		scrollPane.setViewportView(jPanel2);

		splitPane.setRightComponent(scrollPane);

		jPanel4.add(jPanel3, java.awt.BorderLayout.CENTER);

		getContentPane().add(jPanel4, java.awt.BorderLayout.CENTER);

		setFont(ui.getFont());
	} // GEN-END:initComponents

	private void returnChanged() {

		if (tab.getSelectedIndex() != currentTabIndex) {

			TableRow row = (TableRow) rows.get(tab.getSelectedIndex());

			if (this.returnChanged == true) {

				int result = ui.showConfirmDialog(this, ui.getString("fina2.returns.returnChanged"), ui.getString("fina2.returns.returnSaveQuestion"), JOptionPane.YES_NO_CANCEL_OPTION);

				if (result == JOptionPane.YES_OPTION) {
					// Save previously edited return's changes
					save();
					refreshFrame();
				} else if (result == JOptionPane.CANCEL_OPTION) {
					// Leave on the same return and correspondently on the same
					// tab
					tab.setSelectedIndex(currentTabIndex);
					return;
				}
			}

			// Clear all previously registered modify listeners
			clearModifyListeners();

			// Set the current tab index value
			this.currentTabIndex = tab.getSelectedIndex();

			initReturnSheet((ReturnPK) row.getPrimaryKey(), row);
		}
	}

	private void updateButtonsStates() {

		ArrayList singleRow = new ArrayList();
		singleRow.add(row);

		saveButton.setEnabled(returnManagerFrame.canAmend(singleRow));
		saveAndProcessButton.setEnabled(returnManagerFrame.canAmend(singleRow));
		acceptButton.setEnabled(returnManagerFrame.canAccept(singleRow));
		rejectButton.setEnabled(returnManagerFrame.canReject(singleRow));
		resetButton.setEnabled(returnManagerFrame.canReset(singleRow));

		acceptPackageButton.setEnabled(returnManagerFrame.canAccept(rows));
		rejectPackageButton.setEnabled(returnManagerFrame.canReject(rows));
		resetPackageButton.setEnabled(returnManagerFrame.canReset(rows));

		saveAndProcessPackageButton.setEnabled(returnManagerFrame.canAmend(singleRow) && returnManagerFrame.canProcess(rows, row));
	}

	private void saveAsButtonActionPerformed(java.awt.event.ActionEvent evt) {

		SaveAsVersionDialog dlg = new SaveAsVersionDialog(this);
		dlg.show((ReturnPK) row.getPrimaryKey(), this.versionCode);

		if (dlg.isOk()) {

			this.versionCode = dlg.getVersionCode();
			this.versionCodeField.setText(this.versionCode);

			this.row = cloneRow(this.row);
			this.row.setValue(5, this.versionCode);

			save(true, dlg.getNotes());
			refreshFrame();
		}
	}

	private void saveAsPackageButtonActionPerformed(java.awt.event.ActionEvent evt) {

		try {
			String bankCode = row.getValue(4).trim();
			log.info("########Before Format: " + row.getValue(2).trim());
			Date endDate = LocaleUtil.string2date(main.getLanguageHandle(), row.getValue(2).trim());
			log.info("########After Format: " + endDate);
			String returnTypeCode = row.getValue(7).trim();

			SaveAsVersionDialog dlg = new SaveAsVersionDialog(this);
			dlg.show(bankCode, endDate, returnTypeCode, versionCode);

			if (dlg.isOk()) {
				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi.lookup("fina2/returns/ReturnSession");
				ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);
				ReturnSession session = home.create();

				String newVersionCode = dlg.getVersionCode();
				String notes = dlg.getNotes();

				fina2.ui.ProcessDialog pdlg = ui.showProcessDialog(this, "Saving...");

				log.info("#########End Date Before copy package: " + endDate);

				session.copyPackage(main.getUserHandle(), bankCode, endDate, returnTypeCode, this.versionCode, newVersionCode, notes);
				pdlg.dispose();

				this.versionCode = newVersionCode;
				versionCodeField.setText(this.versionCode);

				// if (this.returnChanged == true) {
				save(true, notes);
				// }
				refreshFrame();

				ArrayList newRows = new ArrayList();
				for (Iterator iter = this.rows.iterator(); iter.hasNext();) {
					TableRow tableRow = (TableRow) iter.next();
					TableRow clonedRow = cloneRow(tableRow);

					if (tableRow == this.row) {
						this.row = clonedRow;
					}

					clonedRow.setValue(5, this.versionCode);
					clonedRow.setValue(6, ReturnConstants.STATUS_CREATED_STR);

					newRows.add(clonedRow);
				}

				this.rows = newRows;
			}
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private TableRow cloneRow(TableRow row) {
		TableRow newRow = new TableRowImpl(row.getPrimaryKey(), row.getColumnCount());
		for (int i = 0; i < row.getColumnCount(); i++) {
			newRow.setValue(i, row.getValue(i));
		}
		return newRow;
	}

	private void saveAndProcessButtonActionPerformed(java.awt.event.ActionEvent evt) {
		ArrayList currentRow = new ArrayList();
		currentRow.add(row);

		boolean needSave = false;
		if (this.returnChanged == true || !returnManagerFrame.canProcess(currentRow)) {
			needSave = true;
		}

		if (needSave == false || save() == true) {
			process(currentRow, needSave);
		}
		initReturnSheet(returnPK, row);
		sheet.afterShow();
	}

	private void saveAndProcessPackageButtonActionPerformed(java.awt.event.ActionEvent evt) {

		ArrayList currentRow = new ArrayList();
		currentRow.add(row);

		boolean needSave = false;
		if (this.returnChanged == true || !returnManagerFrame.canProcess(currentRow)) {
			needSave = true;
		}

		if (needSave == false || save() == true) {
			process(rows, needSave);
		}
	}

	private void process(final Collection returnRows, final boolean refresh) {

		Thread t = new Thread() {
			public void run() {
				ProcessDialog pdlg = ui.showProgressDialog(ReturnAmendReviewFrame.this, ui.getString("fina2.returns.processing"), rows.size());
				String resultMessage = returnManagerFrame.processReturns(returnRows, pdlg);
				pdlg.dispose();

				ImportDialog dlg = new ImportDialog(null, resultMessage);
				dlg.show();

				if (refresh == true) {
					refreshFrame();
				}

				returnManagerFrame.treeTable.updateUI();
				returnManagerFrame.updateButtonsStates();

				updateButtonsStates();
			}
		};
		t.start();
	}

	private void acceptButtonActionPerformed(java.awt.event.ActionEvent evt) {

		ArrayList currentRow = new ArrayList();
		currentRow.add(row);

		NoteBox noteBox = new NoteBox(this);
		noteBox.show(ui.getString(ReturnConstants.STATUS_ACCEPTED_STR));

		returnManagerFrame.acceptReturns(currentRow, noteBox.getMessage());
		updateButtonsStates();
	}

	private void rejectButtonActionPerformed(java.awt.event.ActionEvent evt) {

		ArrayList currentRow = new ArrayList();
		currentRow.add(row);

		NoteBox noteBox = new NoteBox(this);
		noteBox.show(ui.getString(ReturnConstants.STATUS_REJECTED_STR));

		returnManagerFrame.rejectReturns(currentRow, noteBox.getMessage());
		updateButtonsStates();
	}

	private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {

		ArrayList currentRow = new ArrayList();
		currentRow.add(row);

		NoteBox noteBox = new NoteBox(this);
		noteBox.show(ui.getString(ReturnConstants.STATUS_RESETED_STR));

		returnManagerFrame.resetReturns(currentRow, noteBox.getMessage());
		updateButtonsStates();
	}

	private void acceptPackageButtonActionPerformed(java.awt.event.ActionEvent evt) {

		NoteBox noteBox = new NoteBox(this);
		noteBox.show(ui.getString(ReturnConstants.STATUS_ACCEPTED_STR));

		returnManagerFrame.acceptReturns(rows, noteBox.getMessage());
		updateButtonsStates();
	}

	private void rejectPackageButtonActionPerformed(java.awt.event.ActionEvent evt) {

		NoteBox noteBox = new NoteBox(this);
		noteBox.show(ui.getString(ReturnConstants.STATUS_REJECTED_STR));

		returnManagerFrame.rejectReturns(rows, noteBox.getMessage());
		updateButtonsStates();
	}

	private void resetPackageButtonActionPerformed(java.awt.event.ActionEvent evt) {

		NoteBox noteBox = new NoteBox(this);
		noteBox.show(ui.getString(ReturnConstants.STATUS_RESETED_STR));

		returnManagerFrame.resetReturns(rows, noteBox.getMessage());
		updateButtonsStates();
	}

	private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {
		save();
		refreshFrame();
	}

	private void refreshFrame() {
		returnManagerFrame.initTable();
	}

	private boolean save() {
		return save(false, null);
	}

	private boolean save(boolean versionChanged, String notes) {

		boolean success = false;
		try {
			fina2.ui.ProcessDialog pdlg = ui.showProcessDialog(null, "Saving...");

			Language language = (Language) main.getLanguageHandle().getEJBObject();
			DecimalFormat df = new DecimalFormat(language.getNumberFormat().trim());

			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/returns/ReturnSession");
			ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);

			ReturnSession session = home.create();

			if (versionChanged) {
				session.resetReturnVersion(returnPK, versionCode);
			}

			int ii = 0;
			Vector rows_ = new Vector();
			for (Iterator iter = defTables.iterator(); iter.hasNext(); ii++) {
				DefinitionTable table = (DefinitionTable) iter.next();
				Table tabl = (Table) t.get(ii);

				Vector rr = new Vector();

				int r = tabl.start_row;
				int r_ = 0;
				int rowNumber = 0;
				for (Iterator it = tabl.rows.iterator(); it.hasNext();) {
					ValuesTableRow row = (ValuesTableRow) it.next();
					r_++;
					for (int c = 0; c < row.getColumnCount(); c++) {

						if (row.getType(c) == MDTConstants.NODETYPE_VARIABLE) {
							// For formulas values must be kept
							String value = row.getValue(c);
							Node node = new Node(new Long(row.getNodeID(c)), "", new Integer(row.getDataType(c)));
							node.putProperty("value", value);
							node.putProperty("row", new Integer(rowNumber));
							rr.add(node);
							// Keep values for formulas
							row.setValue(c, value);
						}

						if (row.getType(c) == MDTConstants.NODETYPE_INPUT) {

							if (row.getDataType(c) == MDTConstants.DATATYPE_TEXT) {
								String value = row.getValue(c).trim();
								String value_ = sheet.getCellValue(r, c + 1).trim();
								if (!value.equals(value_) || versionChanged) {
									Node node = new Node(new Long(row.getNodeID(c)), "", new Integer(row.getDataType(c)));
									node.putProperty("value", value_);
									node.putProperty("row", new Integer(rowNumber));
									rr.add(node);

									row.setValue(c, value_);
								} else if (table.getType() == ReturnConstants.TABLETYPE_VARIABLE) {
									Node node = new Node(new Long(row.getNodeID(c)), "", new Integer(row.getDataType(c)));
									node.putProperty("value", value_);
									node.putProperty("row", new Integer(rowNumber));
									rr.add(node);
								}
							}

							if (row.getDataType(c) == MDTConstants.DATATYPE_NUMERIC) {
								boolean addNode = false;
								Object value = 0;
								try {

									value = (new Double(row.getValue(c))).doubleValue();
								} catch (java.lang.NumberFormatException e) {
									addNode = true;
								}

								Object value_ = sheet.getCellValue(r, c + 1);
								try {
									if (value_.toString().trim().contains("%"))
										value_ = Double.toString(df.parse(value_.toString().trim().replace("%", "")).doubleValue() / 100.0d);
									else {
										if (!value_.toString().trim().equals("")) {
											value_ = Double.toString(df.parse(value_.toString().trim()).doubleValue());
										}
									}
								} catch (Exception e) {
									if (value_.toString().contains("%")) {
										value_ = Double.valueOf(value_.toString().replace("%", "")) / 100.0d;
									}
									if (value_ == null || value_.toString().trim().equals(""))
										value_ = Double.NaN;
									else if ((value_.toString().trim().length() > 0) && (!value_.toString().trim().replace(" ", "").matches("[0-9a-zA-Z]*")))
										value_ = 0;
								}
								if (value_ == null)
									value_ = 0;
								if ((!value.equals(value_)) || versionChanged || addNode) {
									Node node = new Node(new Long(row.getNodeID(c)), "", new Integer(row.getDataType(c)));
									if (value_.toString().contains("%"))
										node.putProperty("value", value_.toString().replace("%", ""));
									else
										node.putProperty("value", value_.toString());
									node.putProperty("row", new Integer(rowNumber));
									rr.add(node);

									row.setValue(c, value_.toString());
								} else if (table.getType() == ReturnConstants.TABLETYPE_VARIABLE) {
									Node node = new Node(new Long(row.getNodeID(c)), "", new Integer(row.getDataType(c)));

									if (value_.toString().contains("%"))
										node.putProperty("value", value_.toString().replace("%", ""));
									else
										node.putProperty("value", value_.toString());
									node.putProperty("row", new Integer(rowNumber));
									rr.add(node);
								}
							}
							if (row.getDataType(c) == MDTConstants.DATATYPE_DATE) {
								Object value = sheet.getCellValue(r, c + 1);
								Node node = new Node(new Long(row.getNodeID(c)), "", new Integer(row.getDataType(c)));
								node.putProperty("value", value);
								node.putProperty("row", new Integer(rowNumber));
								rr.add(node);
							}
						}
					}
					r++;
					rowNumber++;
					rows_.add(row);
				}
				session.setTableValuesRows(returnPK, table.getNode(), rr, versionCode);
			}

			pdlg.dispose();

			if (notes == null) {
				NoteBox noteBox = new NoteBox((java.awt.Frame) this.getParent());
				noteBox.show(ui.getString(ReturnConstants.STATUS_AMENDED_STR));
				notes = noteBox.getMessage();
			}

			session.changeReturnStatus(main.getUserHandle(), main.getLanguageHandle(), returnPK, ReturnConstants.STATUS_AMENDED, notes, versionCode);

			session.toAuditLog("\"" + ui.getString("fina2.returns.amended") + "\"," + "\"" + row.getValue(0) + "\",\"" + row.getValue(1) + "\",\"" + row.getValue(2) + "\",\"" + row.getValue(4) + "\""
					+ ",\"\",\"\",\"\",\"\",\"\",\"\"", main.getUserHandle(), main.getLanguageHandle());

			// Audit Log
			String auditLog = generateAutitLog(row.getValue(0), row.getValue(3));
			if (!auditLog.equals("")) {
				session.toAuditLog(auditLog, main.getUserHandle(), main.getLanguageHandle());
			}

			updateButtonsStates();

			this.returnChanged = false;
			success = true;

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

		return success;
	}

	private void insertButtonActionPerformed(java.awt.event.ActionEvent evt) {
		sheet.setProtected(false, "fina");
		autoCalculate = false;
		int table_num = 1;

		for (Iterator iter = defTables.iterator(); iter.hasNext();) {
			DefinitionTable table = (DefinitionTable) iter.next();
			Table tabl = (Table) t.get(table_num - 1);

			if (tabl.type == ReturnConstants.TABLETYPE_VARIABLE) {

				Vector v = new Vector(tabl.rows);

				int end_row = tabl.end_row;
				int end_col = tabl.end_col;

				if (table.getEvalType() != 0) {
					end_row--;
					end_col--;
				}

				if ((sheet.getStartSelRow() >= tabl.start_row) && (sheet.getStartSelCol() >= tabl.start_col) && (sheet.getEndSelRow() <= end_row) && (sheet.getEndSelCol() <= end_col)) {

					returnChanged = true;
					sheet.insertRange(sheet.getStartSelRow() + 1, 0, sheet.getStartSelRow() + 1, 200, sheet.INSERT_DOWN);

					adjustModifyListeners(table_num, sheet.getStartSelRow() - tabl.start_row + 1, true);

					ArrayList items = new ArrayList();
					ValuesTableRow roww = (ValuesTableRow) v.get(0);
					for (int i = tabl.start_col; i <= tabl.end_col; i++) {

						if (roww.getType(i - 1) == MDTConstants.NODETYPE_INPUT) {

							if (roww.getDataType(i - 1) == MDTConstants.DATATYPE_NUMERIC) {
								sheet.setCellNumber(sheet.getStartSelRow() + 1, i, 0);
							} else {
								sheet.setCellValue(sheet.getStartSelRow() + 1, i, "");
							}

							addModifyListener(sheet.getStartSelRow() + 1, i, roww.getNodeID(i - 1), sheet.getStartSelRow() - tabl.start_row + 1, table_num,
									(roww.getDataType(i - 1) == MDTConstants.DATATYPE_NUMERIC));
						} else {
							sheet.setCellNumber(sheet.getStartSelRow() + 1, i, 0.0);
						}

						if (table.getEvalType() != 0
								&& ((roww.getType(i - 1) == MDTConstants.NODETYPE_INPUT && roww.getDataType(i - 1) == MDTConstants.DATATYPE_NUMERIC) || roww.getType(i - 1) == MDTConstants.NODETYPE_VARIABLE)) {
							char colChar = (char) ((int) 'A' + i);

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

							fun = fun + "(" + colChar + (tabl.start_row + 1) + ":" + colChar + (tabl.end_row + 1) + ")";

							sheet.setCellFormula(tabl.end_row + 1, i, fun);
						}

						items.add(new Long(roww.getNodeID(i - 1)));
					}

					insertRow(sheet.getStartSelRow() - tabl.start_row + 1, items, table_num);
					v.insertElementAt(roww, (sheet.getStartSelRow() - tabl.start_row));

					tabl.end_row++;

					tabl.rows = v;
					for (int i = table_num; i < t.size(); i++) {
						Table tt = (Table) t.get(i);
						tt.start_row++;
						tt.end_row++;
					}
				}
			}
			table_num++;
		}

		if (review == false) {
			sheet.setProtected(true, "fina");
		}

		autoCalculate = true;
	} // GEN-LAST:event_insertButtonActionPerformed

	private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		sheet.setProtected(false, "fina");
		autoCalculate = false;

		int table_num = 1;
		for (Iterator iter = defTables.iterator(); iter.hasNext();) {
			DefinitionTable table = (DefinitionTable) iter.next();
			Table tabl = (Table) t.get(table_num - 1);

			if (tabl.type == ReturnConstants.TABLETYPE_VARIABLE) {
				Vector v = new Vector((Collection) tabl.rows);

				int end_row = tabl.end_row;
				int end_col = tabl.end_col;

				if (table.getEvalType() != 0) {
					end_row--;
					end_col--;
				}

				if ((sheet.getStartSelRow() >= tabl.start_row) && (sheet.getStartSelCol() >= tabl.start_col) && (sheet.getEndSelRow() <= end_row) && (sheet.getEndSelCol() <= end_col)) {

					returnChanged = true;
					removedRow(sheet.getStartSelRow() - tabl.start_row, table_num);
					adjustModifyListeners(table_num, sheet.getStartSelRow() - tabl.start_row, false);

					if (v.size() > 1) {

						sheet.removeRange(sheet.getStartSelRow(), 0, sheet.getStartSelRow(), 200, sheet.REMOVE_ROWS);
						v.remove(sheet.getStartSelRow() - tabl.start_row);

						tabl.end_row--;
						tabl.rows = v;
						for (int i = table_num + 1; i < t.size(); i++) {
							Table tt = (Table) t.get(i);
							tt.start_row--;
							tt.end_row--;
						}
					} else {
						ArrayList items = new ArrayList();

						ValuesTableRow roww = (ValuesTableRow) v.get(0);
						for (int i = tabl.start_col; i <= tabl.end_col; i++) {
							if (roww.getType(i - 1) == MDTConstants.NODETYPE_INPUT) {

								if (roww.getDataType(i - 1) == MDTConstants.DATATYPE_NUMERIC) {
									sheet.setCellNumber(tabl.start_row, i, 0);
								} else {
									sheet.setCellValue(tabl.start_row, i, "");
								}
							} else {
								sheet.setCellNumber(tabl.start_row, i, 0.0);
							}

							items.add(new Long(roww.getNodeID(i - 1)));
						}
						insertRow(0, items, table_num);
					}
				}
			}
			table_num++;
		}

		if (review == false) {
			sheet.setProtected(true, "fina");
		}

		autoCalculate = true;
	} // GEN-LAST:event_deleteButtonActionPerformed

	private void formComponentResized(java.awt.event.ComponentEvent evt) { // GEN-
		// FIRST
		// :
		// event_formComponentResized
		ui.putConfigValue("fina2.returns.ReturnViewFrame.width", new Integer(getWidth()));
		ui.putConfigValue("fina2.returns.ReturnViewFrame.height", new Integer(getHeight()));
	} // GEN-LAST:event_formComponentResized

	private void formComponentShown(java.awt.event.ComponentEvent evt) { // GEN-
		// FIRST
		// :
		// event_formComponentShown
		ui.putConfigValue("fina2.returns.ReturnViewFrame.visible", new Boolean(true));
	} // GEN-LAST:event_formComponentShown

	private void formComponentHidden(java.awt.event.ComponentEvent evt) { // GEN-
		// FIRST
		// :
		// event_formComponentHidden
		ui.putConfigValue("fina2.returns.ReturnViewFrame.visible", new Boolean(false));
	} // GEN-LAST:event_formComponentHidden

	private void formComponentMoved(java.awt.event.ComponentEvent evt) { // GEN-
		// FIRST
		// :
		// event_formComponentMoved
		ui.putConfigValue("fina2.returns.ReturnViewFrame.x", new Integer(getX()));
		ui.putConfigValue("fina2.returns.ReturnViewFrame.y", new Integer(getY()));
	} // GEN-LAST:event_formComponentMoved

	private void exitForm(java.awt.event.WindowEvent evt) { // GEN-FIRST:
		// event_exitForm
		sheet.dispose();
		cleanup();
		dispose();
	} // GEN-LAST:event_exitForm

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
			Main.generalErrorHandler(ex);
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
			Main.generalErrorHandler(ex);
		}
	}

	private void removedRow(int row, int tableID) {
		if (review) {
			return;
		}

		try {
			Object ref = fina2.Main.main.getJndiContext().lookup("fina2/returns/ProcessSession");
			ProcessSessionHome phome = (ProcessSessionHome) PortableRemoteObject.narrow(ref, ProcessSessionHome.class);

			ProcessSession psession = phome.create();
			psession.removeRow(sheet.hashCode(), tableID, row);

		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
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
			Main.generalErrorHandler(ex);
		}
	}

	private void cleanup() {
		if (review) {
			return;
		}

		try {
			Object ref = fina2.Main.main.getJndiContext().lookup("fina2/returns/ProcessSession");
			ProcessSessionHome phome = (ProcessSessionHome) PortableRemoteObject.narrow(ref, ProcessSessionHome.class);

			ProcessSession psession = phome.create();
			psession.cleanup(sheet.hashCode());

		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
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
								if (item.value == null) {
									item.value = "NaN";
								}
								if (item.value.equals("undefined") || item.value.equals("Infinity") || item.value.equals("NaN") || item.value.equals("X")) {

									number = 0;
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

	private javax.swing.JButton insertButton;
	private javax.swing.JButton saveButton;
	private javax.swing.JTextField versionCodeField;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JButton deleteButton;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JTabbedPane tab;
	private javax.swing.JButton acceptButton;
	private javax.swing.JButton rejectButton;
	private javax.swing.JButton resetButton;
	private javax.swing.JButton acceptPackageButton;
	private javax.swing.JButton rejectPackageButton;
	private javax.swing.JButton resetPackageButton;
	private javax.swing.JPanel jReturnBottonsPanel;
	private javax.swing.JPanel jPackageButtonsPanel;
	private javax.swing.JPanel jReturnVersionPanel;
	private javax.swing.JPanel jMultipleTablePanel;
	private javax.swing.JButton saveAndProcessButton;
	private javax.swing.JButton saveAndProcessPackageButton;
	private javax.swing.JButton saveAsPackageButton;
	private javax.swing.JButton saveAsButton;
	private javax.swing.JSplitPane splitPane;
	private javax.swing.JScrollPane scrollPane;
	private javax.swing.JPanel jPanel3;

	// End of variables declaration//GEN-END:variables

	private class Table {
		public int start_row;
		public int start_col;
		public int end_row;
		public int end_col;
		public int type;
		public Vector rows;
	}

	private void clearModifyListeners() {

		for (Iterator iter = modifyListeners.values().iterator(); iter.hasNext();) {
			ArrayList listeners = (ArrayList) iter.next();

			for (Iterator iter2 = listeners.iterator(); iter2.hasNext();) {
				InputModifyListener listener = (InputModifyListener) iter2.next();
				listener.removed = true;
			}
		}

		modifyListeners.clear();
	}

	private void adjustModifyListeners(int tableID, int rowNumber, boolean inserted) {

		ArrayList listeners = (ArrayList) modifyListeners.get(new Integer(tableID));
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			InputModifyListener listener = (InputModifyListener) iter.next();

			if (inserted == false && listener.rowNumber == rowNumber) {
				listener.removed = true;
				iter.remove();
			} else if (listener.rowNumber >= rowNumber) {
				if (inserted) {
					listener.rowNumber++;
				} else {
					listener.rowNumber--;
				}
			}
		}
	}

	private void addModifyListener(int r, int c, long nodeId, int rowNumber, int defTableIndex, boolean numeric) {

		ModifyListener listener = new InputModifyListener(nodeId, rowNumber);

		ArrayList list = (ArrayList) modifyListeners.get(defTableIndex);
		if (list == null) {
			list = new ArrayList();
			modifyListeners.put(defTableIndex, list);
		}
		list.add(listener);

		sheet.addModifyListener(r, c, listener, numeric);
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
				Main.generalErrorHandler(ex);
			}
		}
	}

	private String generateAutitLog(String returnCode, String returnName) {
		String src = "";
		for (Entry<CellInfo, String> entry : auditLogTempMap.entrySet()) {
			CellInfo cellInfo = entry.getKey();
			if (!entry.getValue().equals(cellInfo.getValue())) {
				src += "Action=Return Amend|R.code =" + returnCode + "|R=" + cellInfo.getR() + ",C=" + cellInfo.getC() + "|Old Value=" + entry.getValue() + "|New Value=" + cellInfo.getValue() + "\n";
				auditLogTempMap.remove(cellInfo);
				cellInfo.setValue(cellInfo.getValue());
				auditLogTempMap.put(cellInfo, (cellInfo.getValue() != null) ? cellInfo.getValue() : "");
			}
		}

		// Delete \n
		if (!src.equals("")) {
			StringBuilder sb = new StringBuilder(src);
			sb.deleteCharAt(sb.length() - 1);
			src = sb.toString();
		}

		return src;
	}

	private Map<CellInfo, String> auditLogTempMap = new ConcurrentHashMap<ReturnAmendReviewFrame.CellInfo, String>();

	private void addCellModifyListener(int r1, int c1, String oldValue, boolean numeric) {
		CellModifyListener listener = new AuditCellModifyListener();
		sheet.addCellModifyListener(c1, r1, listener, numeric);

		CellInfo info = new CellInfo();
		info.setR(r1);
		info.setC(c1);

		if (auditLogTempMap.get(info) == null) {
			auditLogTempMap.put(info, oldValue);
		}
	}

	class AuditCellModifyListener implements CellModifyListener {
		@Override
		public void modified(String newValue, int column, int row) {
			CellInfo cellInfo = new CellInfo();
			cellInfo.setC(column);
			cellInfo.setR(row);
			String oldValue = auditLogTempMap.get(cellInfo);
			if (oldValue != null) {
				cellInfo.setValue(newValue);
				auditLogTempMap.remove(cellInfo);
				auditLogTempMap.put(cellInfo, oldValue);
			}
		}
	}

	class CellInfo implements Serializable {
		private int r;
		private int c;
		private String newValue;

		public int getR() {
			return r;
		}

		public void setR(int r) {
			this.r = r;
		}

		public int getC() {
			return c;
		}

		public void setC(int c) {
			this.c = c;
		}

		public String getValue() {
			return newValue;
		}

		public void setValue(String value) {
			this.newValue = value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + c;
			result = prime * result + r;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CellInfo other = (CellInfo) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (c != other.c)
				return false;
			if (r != other.r)
				return false;
			return true;
		}

		private ReturnAmendReviewFrame getOuterType() {
			return ReturnAmendReviewFrame.this;
		}
	}

	public ReturnPK getReturnPK() {
		return returnPK;
	}

	public void setReturnPK(ReturnPK returnPK) {
		this.returnPK = returnPK;
	}

	public ArrayList getRows() {
		return rows;
	}

	public void setRows(ArrayList rows) {
		this.rows = rows;
	}

	public TableRow getRow() {
		return row;
	}

	public void setRow(TableRow row) {
		this.row = row;
	}

	public boolean isPackageAmend() {
		return packageAmend;
	}

	public void setPackageAmend(boolean packageAmend) {
		this.packageAmend = packageAmend;
	}

	public Spreadsheet getSheet() {
		return sheet;
	}

	public void setSheet(Spreadsheet sheet) {
		this.sheet = sheet;
	}

	public ReturnManagerFrame getReturnManagerFrame() {
		return returnManagerFrame;
	}

	public void setReturnManagerFrame(ReturnManagerFrame returnManagerFrame) {
		this.returnManagerFrame = returnManagerFrame;
	}
}
