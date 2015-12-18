package fina2.returns;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.sun.star.util.Date;

import fina2.BaseFrame;
import fina2.Main;
import fina2.i18n.Language;
import fina2.metadata.MDTConstants;
import fina2.ui.sheet.Spreadsheet;
import fina2.ui.sheet.SpreadsheetsManager;
import fina2.ui.table.TableRow;

public class ReturnViewFormatFrame extends javax.swing.JFrame {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private Collection defTables;
	private TableRow row;
	private ReturnDefinitionPK pk;

	private Spreadsheet sheet;

	public ReturnViewFormatFrame() {

		setIconImage(ui.getIcon("fina2.icon").getImage());

		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.print", "print.gif");
		ui.loadIcon("fina2.refresh", "refresh.gif");
		ui.loadIcon("fina2.close", "cancel.gif");
		ui.loadIcon("fina2.refresh", "refresh.gif");

		try {
			initComponents();
			setFont(ui.getFont());

			sheet = SpreadsheetsManager.getInstance().createSpreadsheet();
			panel.add(sheet.getComponent(), java.awt.BorderLayout.CENTER);
		} catch (Exception e) {
			e.printStackTrace();
		}
		BaseFrame.ensureVisible(this);
	}

	private void initSheet(ReturnDefinitionPK pk, TableRow row) {

		this.pk = pk;
		this.row = row;

		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/returns/ReturnSession");
			ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);

			ReturnSession session = home.create();

			byte[] buff = session.getReturnDefinitionReviewFormat(pk);

			if ((buff != null) && (buff.length > 0)) {

				sheet.read(buff);
				if (sheet.getLastRow() == 0 && sheet.getLastCol() == 0) {
					resetFormat();
				}
			} else {
				sheet.loadBlank();
				resetFormat();
			}

			sheet.showGrid(false);
			sheet.setViewMode(sheet.VIEW_SIMPLE);
			sheet.showSheetTabs(false);
			// sheet.setFontName(0, 0, sheet.getLastRow(), sheet.getLastCol(),
			// ui.getFont().getName());
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	public void show(ReturnDefinitionPK pk, TableRow row) {

		setTitle(row.getValue(0));

		super.show();

		initSheet(pk, row);
		sheet.afterShow();
	}

	public void saveFormat() {

		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			sheet.write(bo);
			bo.close();

			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/returns/ReturnSession");
			ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);

			ReturnSession session = home.create();

			session.setReturnDefinitionReviewFormat(pk, bo.toByteArray());
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	public void resetFormat() {

		this.pk = pk;
		this.row = row;

		try {

			Language l = (Language) main.getLanguageHandle().getEJBObject();
			String nformat = l.getNumberFormat();
			java.text.DecimalFormat nf = new java.text.DecimalFormat(nformat);

			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/returns/ReturnSession");
			ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);

			ReturnSession session = home.create();

			defTables = session.getDefinitionTablesFormat(main.getLanguageHandle(), pk);

			sheet.removeRange(0, 0, sheet.getLastRow() + 1, sheet.getLastCol() + 1, sheet.REMOVE_ROWS);

			int j = 1;

			sheet.setFontName(j, 1, j + 4, 1, ui.getFont().getName());
			sheet.setFontSize(j, 1, j + 4, 1, ui.getFont().getSize());
			sheet.setFontWeight(j, 1, j + 4, 1, Spreadsheet.BOLD);

			// Return Code and Description
			sheet.setCellValue(j, 1, row.getValue(1));
			j++;

			// Bank Code and Description
			sheet.setCellValue(j, 1, ui.getString("fina2.bank.bank"));
			j++;

			// Form and To
			sheet.setCellValue(j, 1, ui.getString("fina2.from") + "-" + ui.getString("fina2.to"));
			j++;

			// Return Definition version
			sheet.setCellValue(j, 1, ui.getString("fina2.returns.returnVersion"));
			j++;

			// Return Definition status
			sheet.setCellValue(j, 1, ui.getString("fina2.status"));
			j++;

			j++;
			j++;

			double number = 9999999.00;

			for (Iterator iter = defTables.iterator(); iter.hasNext();) {

				DefinitionTable table = (DefinitionTable) iter.next();

				sheet.setCellWrap(j, 1, true);
				sheet.setFontName(j, 1, j, 1, ui.getFont().getName());
				sheet.setFontSize(j, 1, j, 1, ui.getFont().getSize() + 2);
				sheet.setFontWeight(j, 1, j, 1, Spreadsheet.BOLD);

				if (table.getType() != ReturnConstants.TABLETYPE_NORMAL)
					sheet.setCellValue(j, 1, table.getNodeName());
				else
					sheet.setCellValue(j, 1, table.getCode());

				j++;
				j++;

				Collection rows = session.getReviewTableFormatRows(main.getLanguageHandle(), table.getNode());

				// Normal
				if (table.getType() == ReturnConstants.TABLETYPE_NORMAL) {

					sheet.setCellWrap(j, 2, true);
					sheet.setHorizontalAlign(j, 2, Spreadsheet.CENTER);
					sheet.setVerticalAlign(j, 2, Spreadsheet.CENTER);
					// sheet.setFontName(j,2,j,2,ui.getFont().getName());
					sheet.setFontSize(j, 2, j, 2, ui.getFont().getSize());
					sheet.setFontWeight(j, 2, j, 2, Spreadsheet.BOLD);

					sheet.setCellValue(j, 2, table.getNodeName());

					ValuesTableRow title = (ValuesTableRow) ((Vector) rows).get(0);
					rows.remove(title);
					Object[][] data = null;
					data = new Object[rows.size()][title.getColumnCount()];

					j++;
					int rowN = j;
					int rc = 0;
					// rc++;

					for (Iterator _iter = rows.iterator(); _iter.hasNext();) {
						ValuesTableRow roww = (ValuesTableRow) _iter.next();

						data[rc][0] = new String((String) roww.getValue(0));

						if ((roww.getDataType(1) == MDTConstants.DATATYPE_NUMERIC) || (roww.getType(1) == MDTConstants.NODETYPE_VARIABLE)) {

							data[rc][1] = new Double(number);
							if (roww.getType(1) == MDTConstants.NODETYPE_INPUT) {
								sheet.setCellProtected(j, 2, false);
								sheet.setCellNumberValidity(j, 2);
							}
						} else if (roww.getDataType(1) == MDTConstants.DATATYPE_DATE) {

							if (roww.getType(1) == MDTConstants.NODETYPE_INPUT) {
								sheet.setCellProtected(j, 2, false);
								sheet.setDateFormat(2, j, 2, j, null);
								sheet.setCellDateValidity(j, 2);
							}
							data[rc][1] = new Date();

						}

						else {
							data[rc][1] = new String("text");
							if (roww.getType(1) == MDTConstants.NODETYPE_INPUT) {
								sheet.setCellProtected(j, 2, false);
							}
						}
						j++;
						rc++;
					}

					sheet.setDataArray(rowN, 1, j - 1, 2, data);

					// sheet.setNumberFormat(rowN, 2, j - 1, 2, nformat);
					sheet.setCellWrap(rowN, 1, j - 1, 1, true);
					sheet.setCellWrap(rowN, 2, j - 1, 2, false);
					sheet.setFontSize(rowN, 1, j - 1, 2, ui.getFont().getSize());

					sheet.setFontWeight(rowN, 1, j - 1, 1, Spreadsheet.BOLD);
					sheet.setFontWeight(rowN, 2, j - 1, 2, Spreadsheet.PLAIN);

					sheet.setBorder(rowN - 1, 1, j - 1, 2, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, (short) 6);

					j++;
					j++;
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

					for (Iterator _iter = rows.iterator(); _iter.hasNext();) {
						ValuesTableRow roww = (ValuesTableRow) _iter.next();
						for (int i = 0; i < title.getColumnCount(); i++) {
							if (i > 0) {

								if ((roww.getDataType(i) == MDTConstants.DATATYPE_NUMERIC) || (roww.getType(i) == MDTConstants.NODETYPE_VARIABLE)) {

									data[rc][i] = new Double(number);
									if (roww.getType(i) == MDTConstants.NODETYPE_INPUT) {
										sheet.setCellProtected(j, i + 1, false);
										sheet.setCellNumberValidity(j, i + 1);
									}
								} else if (roww.getDataType(i) == MDTConstants.DATATYPE_DATE) {

									if (roww.getType(i) == MDTConstants.NODETYPE_INPUT) {
										sheet.setCellProtected(j, i + 1, false);
										sheet.setDateFormat(i + 1, j, i + 1, j, null);
										sheet.setCellDateValidity(j, i + 1);
									}
									data[rc][i] = new Date();

								} else {
									if (roww.getType(i) == MDTConstants.NODETYPE_INPUT) {
										sheet.setCellProtected(j, i + 1, false);
									}
									data[rc][i] = new String("text");
								}
							} else {

								if (roww.getType(i) == MDTConstants.NODETYPE_INPUT) {
									sheet.setCellProtected(j, i + 1, false);
								}
								data[rc][i] = (String) roww.getValue(i);
							}
						}
						j++;
						rc++;
					}

					sheet.setDataArray(rowN, 1, j - 1, title.getColumnCount(), data);

					sheet.setCellWrap(rowN, 1, rowN, title.getColumnCount(), true);
					sheet.setHorizontalAlign(rowN, 1, rowN, title.getColumnCount(), Spreadsheet.CENTER);
					sheet.setVerticalAlign(rowN, 1, rowN, title.getColumnCount(), Spreadsheet.CENTER);
					// sheet.setFontName(rowN,1,rowN,title.getColumnCount(),ui.getFont().getName());
					sheet.setFontSize(rowN, 1, rowN, title.getColumnCount(), ui.getFont().getSize());
					sheet.setFontWeight(rowN, 1, rowN, title.getColumnCount(), Spreadsheet.BOLD);

					// sheet.setFontName(rowN+1,1,j-1,title.getColumnCount(),ui.getFont().getName());
					sheet.setFontSize(rowN + 1, 1, j - 1, title.getColumnCount(), ui.getFont().getSize());
					sheet.setFontWeight(rowN + 1, 1, j - 1, title.getColumnCount(), Spreadsheet.PLAIN);
					// sheet.setNumberFormat(rowN + 1, 1, j - 1,
					// title.getColumnCount(), nformat);

					sheet.setBorder(rowN, 1, j - 1, title.getColumnCount(), sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, (short) 6);
					j++;
					j++;
				}

				// Variable
				if (table.getType() == ReturnConstants.TABLETYPE_VARIABLE) {

					int rowN = j;

					int rc = 0;
					ValuesTableRow title = (ValuesTableRow) ((Vector) rows).get(0);

					Object[][] data = null;
					data = new Object[rows.size() + 1][title.getColumnCount()];

					rows.remove(title);
					for (int i = 0; i < title.getColumnCount(); i++) {
						data[rc][i] = new String((String) title.getValue(i));
					}

					j++;
					rc++;
					int rN = 0;
					int tableStartRow = j;
					for (Iterator _iter = rows.iterator(); _iter.hasNext(); rN++) {

						ValuesTableRow roww = (ValuesTableRow) _iter.next();
						for (int i = 0; i < title.getColumnCount(); i++) {

							if ((roww.getDataType(i) == MDTConstants.DATATYPE_NUMERIC) || (roww.getType(i) == MDTConstants.NODETYPE_VARIABLE)) {

								if (roww.getType(i) == MDTConstants.NODETYPE_INPUT) {
									sheet.setCellProtected(j, i + 1, false);
									sheet.setCellNumberValidity(j, i + 1);
								}
								data[rc][i] = new Double(number);
								data[rc + 1][i] = new Double(number);
							} else if (roww.getDataType(i) == MDTConstants.DATATYPE_DATE) {

								if (roww.getType(i) == MDTConstants.NODETYPE_INPUT) {
									sheet.setCellProtected(j, i + 1, false);
									sheet.setDateFormat(i + 1, j, i + 1, j, null);
									sheet.setCellDateValidity(j, i + 1);
								}
								data[rc][i] = new Date();
								data[rc + 1][i] = new Date();
							}

							else {
								data[rc][i] = new String("text");
								data[rc + 1][i] = new String("");
								if (roww.getType(i) == MDTConstants.NODETYPE_INPUT) {
									sheet.setCellProtected(j, i + 1, false);
								}
							}
						}
						j++;
						rc++;
					}
					if (rows.size() > 0) {
						j++;
					}

					sheet.setDataArray(rowN, 1, j - 1, title.getColumnCount(), data);

					sheet.setCellWrap(rowN, 1, rowN, title.getColumnCount(), true);
					sheet.setHorizontalAlign(rowN, 1, rowN, title.getColumnCount(), Spreadsheet.CENTER);
					sheet.setVerticalAlign(rowN, 1, rowN, title.getColumnCount(), Spreadsheet.CENTER);
					// sheet.setFontName(rowN,1,rowN,title.getColumnCount(),ui.getFont().getName());
					sheet.setFontSize(rowN, 1, rowN, title.getColumnCount(), ui.getFont().getSize());
					sheet.setFontWeight(rowN, 1, rowN, title.getColumnCount(), Spreadsheet.BOLD);

					// sheet.setFontName(rowN+1,1,j-1,title.getColumnCount(),ui.getFont().getName());
					sheet.setFontSize(rowN + 1, 1, j - 1, title.getColumnCount(), ui.getFont().getSize());
					sheet.setFontWeight(rowN + 1, 1, j - 1, title.getColumnCount(), Spreadsheet.PLAIN);
					// sheet.setNumberFormat(rowN + 1, 1, j - 1,
					// title.getColumnCount(), nformat);
					sheet.setFontWeight(rowN + 1, 1, j - 2, title.getColumnCount(), Spreadsheet.PLAIN);
					sheet.setFontWeight(rowN + 2, 1, j - 1, title.getColumnCount(), Spreadsheet.BOLD);

					sheet.setBorder(rowN, 1, j - 1, title.getColumnCount(), sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, sheet.LINE_YES, (short) 6);

					j++;
					j++;

				}
			}

			sheet.setFontName(0, 0, sheet.getLastRow(), sheet.getLastCol(), ui.getFont().getName());

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private void initComponents() { // GEN-BEGIN:initComponents
		jPanel3 = new javax.swing.JPanel();
		jPanel7 = new javax.swing.JPanel();
		helpButton = new javax.swing.JButton();
		jPanel8 = new javax.swing.JPanel();
		resetButton = new javax.swing.JButton();
		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		jPanel4 = new javax.swing.JPanel();
		panel = new javax.swing.JPanel();

		setTitle(ui.getString("fina2.returns.returnTypesAction"));
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

		jPanel3.setLayout(new java.awt.BorderLayout());

		helpButton.setIcon(ui.getIcon("fina2.help"));
		helpButton.setFont(ui.getFont());
		helpButton.setText(ui.getString("fina2.help"));
		helpButton.setEnabled(false);
		jPanel7.add(helpButton);

		jPanel3.add(jPanel7, java.awt.BorderLayout.WEST);

		resetButton.setIcon(ui.getIcon("fina2.refresh"));
		resetButton.setFont(ui.getFont());
		resetButton.setText(ui.getString("fina2.returns.reset"));
		resetButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				resetButtonActionPerformed(evt);
			}
		});

		jPanel8.add(resetButton);

		okButton.setIcon(ui.getIcon("fina2.ok"));
		okButton.setFont(ui.getFont());
		okButton.setText(ui.getString("fina2.ok"));
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				okButtonActionPerformed(evt);
			}
		});

		jPanel8.add(okButton);

		cancelButton.setIcon(ui.getIcon("fina2.close"));
		cancelButton.setFont(ui.getFont());
		cancelButton.setText(ui.getString("fina2.cancel"));
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		jPanel8.add(cancelButton);

		jPanel3.add(jPanel8, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

		jPanel4.setLayout(new java.awt.BorderLayout());

		panel.setLayout(new java.awt.BorderLayout());

		jPanel4.add(panel, java.awt.BorderLayout.CENTER);

		getContentPane().add(jPanel4, java.awt.BorderLayout.CENTER);

	} // GEN-END:initComponents

	private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_resetButtonActionPerformed
		resetFormat();
	} // GEN-LAST:event_resetButtonActionPerformed

	private void formComponentResized(java.awt.event.ComponentEvent evt) { // GEN-FIRST:event_formComponentResized
		ui.putConfigValue("fina2.returns.ReturnViewFrame.width", new Integer(getWidth()));
		ui.putConfigValue("fina2.returns.ReturnViewFrame.height", new Integer(getHeight()));
	} // GEN-LAST:event_formComponentResized

	private void formComponentShown(java.awt.event.ComponentEvent evt) { // GEN-FIRST:event_formComponentShown
		ui.putConfigValue("fina2.returns.ReturnViewFrame.visible", new Boolean(true));
	} // GEN-LAST:event_formComponentShown

	private void formComponentHidden(java.awt.event.ComponentEvent evt) { // GEN-FIRST:event_formComponentHidden
		ui.putConfigValue("fina2.returns.ReturnViewFrame.visible", new Boolean(false));
	} // GEN-LAST:event_formComponentHidden

	private void formComponentMoved(java.awt.event.ComponentEvent evt) { // GEN-FIRST:event_formComponentMoved
		ui.putConfigValue("fina2.returns.ReturnViewFrame.x", new Integer(getX()));
		ui.putConfigValue("fina2.returns.ReturnViewFrame.y", new Integer(getY()));
	} // GEN-LAST:event_formComponentMoved

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_cancelButtonActionPerformed
		sheet.dispose();
		dispose();

	} // GEN-LAST:event_cancelButtonActionPerformed

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_okButtonActionPerformed
		saveFormat();
		sheet.dispose();
		dispose();
	} // GEN-LAST:event_okButtonActionPerformed

	/** Exit the Application */
	private void exitForm(java.awt.event.WindowEvent evt) { // GEN-FIRST:event_exitForm
		sheet.dispose();
		dispose();
	} // GEN-LAST:event_exitForm

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JButton helpButton;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JButton resetButton;
	private javax.swing.JButton okButton;
	private javax.swing.JButton cancelButton;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel panel;
	// End of variables declaration//GEN-END:variables
}
