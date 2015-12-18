package fina2.returns;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.transaction.UserTransaction;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import fina2.db.DatabaseUtil;
import fina2.metadata.MDTConstants;
import fina2.util.StatisticsLogger;

public class Import extends DefaultHandler {

	private Connection con;
	private byte[] source;

	private String bankCode;
	private String definitionCode;
	private java.util.Date from;
	private java.util.Date end;
	private String fromStr;
	private String endStr;
	private String langCode;

	private Hashtable<String, Vector<ImportItem>> xmlItems;
	private Hashtable<String, ImportItem> returnItems;
	private Hashtable<Integer, Vector<Long>> varTables;

	private ImportItem item = null;

	private boolean error = false;
	private boolean parseDone = false;
	private boolean newReturn = false;
	private StringBuffer characters = new StringBuffer();

	private Logger logger = Logger.getLogger(Import.class);

	private String log;

	public Import(Connection con, byte[] source) {
		this.con = con;
		this.source = source;
		xmlItems = new Hashtable<String, Vector<ImportItem>>();
		returnItems = new Hashtable<String, ImportItem>();
		varTables = new Hashtable<Integer, Vector<Long>>();
	}

	@SuppressWarnings("unchecked")
	public String importReturn(javax.ejb.SessionContext ctx, Handle userHandle, Handle languageHandle, boolean forceReprocess, String versionCode) {
		synchronized (Import.class) {
			StatisticsLogger statLog = new StatisticsLogger("Return import statistics", logger, Level.INFO);
			HashMap<Integer, String> illegalRetIds = new HashMap<Integer, String>();
			log = "";
			int returnID = 0;
			try {

				// statLog.logStage("Parse source XML");
				SAXParserFactory parserFactory = SAXParserFactory.newInstance();
				parserFactory.setValidating(false);
				parserFactory.setNamespaceAware(false);
				SAXParser parser = parserFactory.newSAXParser();
				parser.parse(new ByteArrayInputStream(source), this);

				if (error) {
					logger.error(log);
					return log;
				}

				while (!parseDone) {
					Thread.sleep(50);
				}

				if (langCode.endsWith("_")) {
					langCode = langCode.substring(0, langCode.length() - 1);
				}

				// statLog.logStage("Select date format");
				PreparedStatement pstmt = con.prepareStatement("select dateFormat from SYS_LANGUAGES where rtrim(code)=?");

				pstmt.setString(1, langCode.trim());

				ResultSet rs = pstmt.executeQuery();

				if (!rs.next()) {
					log += "Error: Unsupported language: " + langCode + "\n";
					logger.error(log);
					return log;
				}

				String dateFormat = rs.getString(1);

				rs.close();
				pstmt.close();

				// logger.info("Importing Return.");
				StringBuffer buff = new StringBuffer();
				buff.append("Importing Return | Bank code : ").append(this.bankCode);
				buff.append(", Return code : ").append(this.definitionCode);
				buff.append(", Period from: ").append(this.fromStr);
				buff.append(", Period to: ").append(this.endStr).append(".");
				logger.info(buff.toString());

				try {
					SimpleDateFormat format = new SimpleDateFormat(dateFormat.trim());
					from = format.parse(fromStr);
					end = format.parse(endStr);
				} catch (ParseException ex) {
					log += "Error: Wrong date format\n";
					logger.error(log);
					return log;
				}

				// statLog.logStage("Get versionID");
				pstmt = con.prepareStatement("select v.id from IN_RETURN_VERSIONS v where v.code=?");
				pstmt.setString(1, versionCode);

				rs = pstmt.executeQuery();
				int versionId = 0;
				if (rs.next()) {
					versionId = rs.getInt(1);
				} else {
					log += "Error: Wrong version code\n";
					logger.error(log);
					return log;
				}

				rs.close();
				pstmt.close();

				// statLog.logStage("Check return status");
				pstmt = con.prepareStatement("select a.id, e.status from IN_RETURNS a, IN_RETURN_STATUSES e " + "where a.scheduleID in " + "(select b.id from IN_SCHEDULES b where b.periodID in "
						+ "(select c.id from IN_PERIODS c where c.fromDate=? and c.toDate=?) " + "and b.bankID in " + "(select d.id from IN_BANKS d where rtrim(d.code)=?) " + "and b.definitionID in "
						+ "(select g.id from IN_RETURN_DEFINITIONS g where rtrim(g.code)=?) ) "
						+ "and e.returnID=a.id and e.id in (select max(f.id) from IN_RETURN_STATUSES f where f.returnID=a.id and f.versionID=?)");
				pstmt.setDate(1, new java.sql.Date(from.getTime()));
				pstmt.setDate(2, new java.sql.Date(end.getTime()));
				pstmt.setString(3, bankCode.trim());
				pstmt.setString(4, definitionCode.trim());
				pstmt.setInt(5, versionId);
				rs = pstmt.executeQuery();

				int scheduleID = 0;
				newReturn = true;

				if (rs.next()) {
					returnID = rs.getInt(1);
					if (rs.getInt(2) == ReturnConstants.STATUS_VALIDATED) {
						log += "Error: Unable import validated return\n";
						logger.error(log);
						return log;
					}
					if (rs.getInt(2) == ReturnConstants.STATUS_ACCEPTED) {
						log += "Warn: Unable to import accepted return\n";
						logger.warn(log);
						return log;
					}
					newReturn = false;
				}
				rs.close();
				pstmt.close();

				// statLog.logStage("Check return schedule");
				PreparedStatement sch_stmt = con.prepareStatement("select b.id from IN_SCHEDULES b where b.periodID in " + "(select c.id from IN_PERIODS c where c.fromDate=? and c.toDate=?) "
						+ "and b.bankID in " + "(select d.id from IN_BANKS d where LTRIM(RTRIM(d.code))=?) " + "and b.definitionID in "
						+ "(select e.id from IN_RETURN_DEFINITIONS e where LTRIM(RTRIM(e.code))=?)");
				sch_stmt.setDate(1, new java.sql.Date(from.getTime()));
				sch_stmt.setDate(2, new java.sql.Date(end.getTime()));
				sch_stmt.setString(3, bankCode.trim());
				sch_stmt.setString(4, definitionCode.trim());
				ResultSet sch_rs = sch_stmt.executeQuery();

				if (sch_rs.next()) {
					scheduleID = sch_rs.getInt(1);
				} else {
					log += "Error: Schedule does not exists\n";
					logger.error(log);
					return log;
				}
				sch_rs.close();
				sch_stmt.close();

				if (!forceReprocess) {
					Map<String, String> expectedCodeNames = new LinkedHashMap<String, String>();
					if (forceReprocess)
						statLog.logStage("Check if return could be processed");
					expectedCodeNames = getProcessSession().canProcess(languageHandle, new SchedulePK(scheduleID), versionCode);

					if (expectedCodeNames.size() > 0) {
						String l = "Following items must be imported before.\n";
						int i = 0;
						int j = 0;
						for (Iterator<String> iter = expectedCodeNames.keySet().iterator(); iter.hasNext(); i++) {
							String code = (String) iter.next();
							if (returnItems.get(code) == null) {
								// /logger.debug(code+"sdasdasdasdasdasD");
								j++;
								String name = (String) expectedCodeNames.get(code);
								l += "    [" + code + "] " + name + "\n";
							}
						}
						l += "Return moved to queue.";
						if (j > 0) {
							log += l;
							// logger.error(log);
							logger.error(l);
							return l;
						}
					}
				}

				con.close();

				if (newReturn) {
					statLog.logStage("Create return");
					ReturnPK returnPK = getReturnSession().createReturn(userHandle, languageHandle, new SchedulePK(scheduleID), versionCode);
					returnID = returnPK.getId();
				} else {
					statLog.logStage("Reset return version");
					getReturnSession().resetReturnVersion(new ReturnPK(returnID), versionCode);
				}

				UserTransaction tx = ctx.getUserTransaction();
				tx.setTransactionTimeout(1800000);
				tx.begin();
				con = DatabaseUtil.getConnection();
				try {
					statLog.logStage("Import return item values");
					PreparedStatement update = con.prepareStatement("update IN_RETURN_ITEMS " + "set value=?, nvalue=? " + "where returnID=? and nodeID=? and versionID=? and tableID=?");
					PreparedStatement update2 = con.prepareStatement("update IN_RETURN_ITEMS " + "set value=?, nvalue=? "
							+ "where returnID=? and nodeID=? and versionID=? and tableID=? and rowNumber=?");
					PreparedStatement insert = con.prepareStatement("insert into IN_RETURN_ITEMS (id,returnID,tableID,nodeID,rowNumber,value,versionID) " + "values(?,?,?,?,?,?,?)");
					PreparedStatement nodetypes = con.prepareStatement("select DATATYPE from IN_MDT_NODES n where n.CODE=?");
					ResultSet nodecodes = null;

					SimpleDateFormat dateformat = new SimpleDateFormat(dateFormat.trim());
					HashMap<String, Integer> dateCodes = new HashMap<String, Integer>();
					HashMap<String, Object> numericCodes = new HashMap<String, Object>();

					int id = 1;
					int update_counter = 0;
					int insert_counter = 0;

					for (Iterator keys = xmlItems.keySet().iterator(); keys.hasNext();) {
						Object obj = keys.next();
						nodetypes.setString(1, xmlItems.get(obj).get(0).code);
						nodecodes = nodetypes.executeQuery();
						if (nodecodes.next()) {
							if ((int) nodecodes.getFloat("DATATYPE") == MDTConstants.DATATYPE_DATE)
								dateCodes.put(xmlItems.get(obj).get(0).code, (int) nodecodes.getFloat("DATATYPE"));
							else if ((int) nodecodes.getFloat("DATATYPE") == MDTConstants.DATATYPE_NUMERIC) {
								numericCodes.put(xmlItems.get(obj).get(0).code, (int) nodecodes.getFloat("DATATYPE"));
							}
						}
					}

					Hashtable rows = new Hashtable();
					logger.info("XML Item Number - " + xmlItems.size());
					long l = System.currentTimeMillis();

					for (Iterator keys = xmlItems.keySet().iterator(); keys.hasNext();) {
						Vector v = (Vector) xmlItems.get(keys.next());
						Hashtable codes = new Hashtable();
						for (Iterator iter = v.iterator(); iter.hasNext();) {
							ImportItem it = (ImportItem) iter.next();

							// if (dateCodes.containsKey(it.code)) {
							// try {
							// if ((it.value != null) &&
							// (!it.value.trim().equals("")))
							// dateformat.parse(it.value);
							// } catch (ParseException ex) {
							// log += "Error: Wrong date format. \n Code=" +
							// it.code + " Row=" + it.rowNumber + " Value=" +
							// it.value + "\n";
							// logger.error(log);
							// log = log.replace("Process ok.", "");
							// illegalRetIds.put(returnID,
							// "Error: Wrong date format. \n Code=" + it.code +
							// " Row=" + it.rowNumber + " Value=" + it.value +
							// "\n");
							// //
							// getReturnSession().changeReturnStatus(userHandle,
							// // languageHandle, new ReturnPK(returnID),
							// // ReturnConstants.STATUS_ERRORS,
							// // "Error: Wrong date format. \n Code=" +
							// // it.code + " Row=" + it.rowNumber +
							// // " Value=" + it.value + "\n",
							// // versionCode);
							// // tx.commit();
							// // return log;
							// }
							// } else if (numericCodes.containsKey(it.code)) {
							// try {
							// if ((it.value != null) &&
							// (!it.value.trim().equals("")))
							// Double.parseDouble(it.value.replace(',', '.'));
							// } catch (NumberFormatException ex) {
							// log += "Error: The value for node code " +
							// it.code + " must be numeric but is " + it.value +
							// "\n";
							// logger.error(log);
							// log = log.replace("Process ok.", "");
							// illegalRetIds.put(returnID,
							// "Error: The value for node code " + it.code +
							// " must be numeric but is " + it.value + "\n");
							// //
							// getReturnSession().changeReturnStatus(userHandle,
							// // languageHandle, new ReturnPK(returnID),
							// // ReturnConstants.STATUS_ERRORS,
							// // "Error: The value for node code " +
							// // it.code + " must be numeric but is " +
							// // it.value + "\n", versionCode);
							// // tx.commit();
							// // return log;
							// }
							// }

							codes.put(it.code, it.code);
							if (returnItems.get(it.code) == null) {
								log += "Warning: Return does not contain item with code \"" + it.code + "\"\n";
								continue;
							}

							ImportItem defIt = (ImportItem) returnItems.get(it.code);

							if (defIt.tableType == ReturnConstants.TABLETYPE_VARIABLE) {
								if (rows.get(new Integer(defIt.tableID * 10000 + it.rowNumber)) == null) {
									rows.put(new Integer(defIt.tableID * 10000 + it.rowNumber), new Integer(it.rowNumber));
									Vector vars = (Vector) varTables.get(new Integer(defIt.tableID));
									for (Iterator _iter = vars.iterator(); _iter.hasNext();) {
										long varNodeID = Long.parseLong((_iter.next().toString()));
										insert.setInt(1, id);
										insert.setInt(2, returnID);
										insert.setInt(3, defIt.tableID);
										insert.setLong(4, varNodeID);
										insert.setInt(5, it.rowNumber);
										insert.setString(6, " ");
										insert.setInt(7, versionId);
										insert.addBatch();
										insert_counter++;
										id++;
									}
								}
								update2.setString(1, it.value);
								ReturnHelper.assignNumericValue(update2, 2, it.value);
								update2.setInt(3, returnID);
								update2.setLong(4, defIt.nodeID);
								update2.setInt(5, versionId);
								update2.setInt(6, defIt.tableID);
								update2.setInt(7, it.rowNumber);
								update2.addBatch();
								update_counter++;
							} else {
								update.setString(1, it.value);
								ReturnHelper.assignNumericValue(update, 2, it.value);
								update.setInt(3, returnID);
								update.setLong(4, defIt.nodeID);
								update.setInt(5, versionId);
								update.setInt(6, defIt.tableID);
								update.addBatch();
								update_counter++;
							}
							id++;
						}
						for (Iterator iter = codes.keySet().iterator(); iter.hasNext();) {
							returnItems.remove(iter.next());
						}
					}
					logger.info("Time of iterating data " + (System.currentTimeMillis() - l));
					long ll = System.currentTimeMillis();
					logger.info("Number Of Inserted Rows " + insert_counter);
					logger.info("Number Of Updated Rows " + update_counter);
					insert.executeBatch();
					update.executeBatch();
					update2.executeBatch();
					logger.info("Time of Inserted and/or Updated rows: " + (System.currentTimeMillis() - ll));
					insert.close();
					update.close();
					update2.close();

					statLog.logStage("Update return versions");
					getReturnSession().updateReturnVersions(String.valueOf(returnID));

					tx.commit();
				} catch (Exception ex) {
					try {
						tx.rollback();
					} catch (Exception exx) {
					}
					throw new EJBException(ex);
				}

				if (returnItems.size() > 0) {
					String s = "";
					for (String ss : returnItems.keySet()) {
						if (!s.equals("")) {
							s += ", ";
						}
						s += ss;
					}
					log += "Warning: Following items are absent: " + s + "\n";
				}

				ReturnPK returnPK = new ReturnPK(returnID);
				String processMsg = "";

				statLog.logStage("Process return");
				boolean reprocess = (newReturn == false) || (forceReprocess == true);
				processMsg = getProcessSession().process(userHandle, languageHandle, returnPK, reprocess, versionCode);

				// if (processMsg.toLowerCase().indexOf("comparison") != -1)
				// processMsg += "\nReturn moved to queue.\n";
				//
				// statLog.logStage("Change return status");
				// if (processMsg.equals("")) {
				// processMsg = "Process ok.\n";
				// getReturnSession().changeReturnStatus(userHandle,
				// languageHandle, returnPK, ReturnConstants.STATUS_PROCESSED,
				// processMsg, versionCode);
				// } else {
				// getReturnSession().changeReturnStatus(userHandle,
				// languageHandle, returnPK, ReturnConstants.STATUS_ERRORS,
				// processMsg, versionCode);
				// }

				log += processMsg; // "Process ok.\n";

			} catch (Exception e) {
				// if (newReturn) {
				try {

					String errorMsg = e.getMessage();
					if (errorMsg.indexOf("Value does not match comparison rule") != -1) {
						log = log.replace("Process ok.", "");
						// getReturnSession().changeReturnStatus(userHandle,
						// languageHandle, new ReturnPK(returnID),
						// ReturnConstants.STATUS_ERRORS,
						// errorMsg.substring(errorMsg.indexOf("Value does not match comparison rule")),
						// versionCode);
					} else if (errorMsg.indexOf("Wrong version code") != -1) {
						log = log.replace("Process ok.", "");
						getReturnSession().changeReturnStatus(userHandle, languageHandle, new ReturnPK(returnID), ReturnConstants.STATUS_ERRORS,
								errorMsg.substring(errorMsg.indexOf("Wrong version code")), versionCode);
					} else if (errorMsg.indexOf("Error during parsing/formatting  following items") != -1) {
						log = log.replace("Process ok.", "");
						// getReturnSession().changeReturnStatus(userHandle,
						// languageHandle, new ReturnPK(returnID),
						// ReturnConstants.STATUS_ERRORS,
						// errorMsg.substring(errorMsg.indexOf("Error during parsing/formatting  following items")),
						// versionCode);
					} else {
						log = log.replace("Process ok.", "");
						statLog.logStage("Delete return");
						getReturnSession().deleteReturn(new ReturnPK(returnID), versionCode);
					}
				} catch (Exception ex) {
					log += "Exception: " + ex.toString() + "\n";
				}
				// }
				log += "Exception: " + e.toString() + "\n";
				logger.error(e.getMessage(), e);
			} finally {
				Iterator<Integer> illegalRetIter = illegalRetIds.keySet().iterator();
				while (illegalRetIter.hasNext()) {
					Integer keyId = illegalRetIter.next();
					String msg = illegalRetIds.get(keyId);
					log = log.replace("Process ok.", "");
					try {
						getReturnSession().changeReturnStatus(userHandle, languageHandle, new ReturnPK(keyId), ReturnConstants.STATUS_ERRORS, msg, versionCode);
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}
				}
				DatabaseUtil.closeConnection(con);
				if (log.indexOf("queue") == -1)
					statLog.logSummary();
			}

			logger.info(log);
		}
		return log;
	}

	private ReturnSession getReturnSession() throws NamingException, CreateException, RemoteException {

		InitialContext jndi = new InitialContext();

		Object ref = jndi.lookup("fina2/returns/ReturnSession");
		ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);

		return home.create();
	}

	private ProcessSession getProcessSession() throws NamingException, CreateException, RemoteException {

		InitialContext jndi = new InitialContext();

		Object ref = jndi.lookup("fina2/returns/ProcessSession");
		ProcessSessionHome home = (ProcessSessionHome) PortableRemoteObject.narrow(ref, ProcessSessionHome.class);

		return home.create();
	}

	private void loadReturnItems() throws SQLException {
		PreparedStatement pstmt = con.prepareStatement("select a.nodeID, a.id, a.type from IN_DEFINITION_TABLES a "
				+ "where a.definitionID in (select b.id from IN_RETURN_DEFINITIONS b where rtrim(b.code)=?)");
		pstmt.setString(1, definitionCode.trim());
		ResultSet rs = pstmt.executeQuery();
		while (rs.next()) {
			selectChildNodes(con, returnItems, rs.getLong(1), rs.getInt(2), rs.getInt(3));
		}
		rs.close();
		pstmt.close();
	}

	public void processingInstruction(String target, String data) {

	}

	public void startDocument() {
	}

	public void startElement(String uri, String local, String raw, Attributes attrs) {

		characters.setLength(0);
		if (raw.equals("ITEM")) {
			item = new ImportItem();
		}
	}

	public void characters(char ch[], int start, int length) {

		characters.append(ch, start, length);
	}

	public void ignorableWhitespace(char ch[], int start, int length) {
	}

	public void endElement(String uri, String local, String raw) {

		if (raw.equals("BANKCODE")) {
			bankCode = characters.toString();
		}

		if (raw.equals("RETURNCODE")) {
			definitionCode = characters.toString();
		}

		if (raw.equals("PERIODFROM")) {
			fromStr = characters.toString();
		}

		if (raw.equals("PERIODEND")) {
			endStr = characters.toString();
		}

		if (raw.equals("LNG")) {
			langCode = characters.toString();
		}

		if (raw.equals("BODY")) {
			try {
				loadReturnItems();
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			}
		}

		if (raw.equals("ITEMCODE")) {
			item.code = characters.toString();
		}

		if (raw.equals("ROW")) {
			try {
				item.rowNumber = Integer.valueOf(characters.toString()).intValue();
			} catch (NumberFormatException e) {
				item.rowNumber = -1;
			}
		}

		if (raw.equals("VALUE")) {
			item.value = characters.toString();
		}

		if (raw.equals("ITEM")) {
			Vector v = new Vector();
			if (xmlItems.get(item.code) == null) {
				xmlItems.put(item.code, v);
			} else {
				v = (Vector) xmlItems.get(item.code);
			}
			v.add(item);
		}

		if (raw.equals("RETURN")) {
			parseDone = true;
		}
	}

	public void endDocument() {
		if (!parseDone) {
			log += "Error: Invalid XML file";
			error = true;
		}
	}

	//
	// ErrorHandler methods
	//

	public void warning(SAXParseException ex) {
		System.err.println("[Warning] " +
		// getLocationString(ex)+": "+
				ex.getMessage());
		log += "Warning: " + ex.getMessage();
	}

	public void error(SAXParseException ex) {
		System.err.println("[Error] " +
		// getLocationString(ex)+": "+
				ex.getMessage());

		log += "Error: " + ex.getMessage();
		error = true;
	}

	public void fatalError(SAXParseException ex) throws SAXException {
		System.err.println("[Fatal Error] " + ex.getMessage());

		log += "Error: " + ex.getMessage();
		log += "Xml Line: " + ex.getLineNumber();
		log += ", Xml Column: " + ex.getColumnNumber();
		log += ", Item Code = " + item.code + ", Item Row Number=" + item.rowNumber + " ";

		error = true;
		throw ex;
	}

	private void selectChildNodes(Connection con, Hashtable h, long parentID, int tableID, int tableType) throws SQLException {

		PreparedStatement pstmt = con.prepareStatement("select id,code,type from IN_MDT_NODES where parentID=? and disabled=0");
		pstmt.setLong(1, parentID);
		ResultSet rs = pstmt.executeQuery();

		while (rs.next()) {
			long id = rs.getLong(1);
			String code = rs.getString(2).trim();
			int type = rs.getInt(3);

			if (type == MDTConstants.NODETYPE_INPUT)
				h.put(code, new ImportItem(id, code, tableID, tableType)); // new
																			// Integer(id));

			if ((type == MDTConstants.NODETYPE_NODE) && (tableType != ReturnConstants.TABLETYPE_VARIABLE)) {
				selectChildNodes(con, h, id, tableID, tableType);
			}
		}
		rs.close();
		pstmt.close();

		if (tableType == ReturnConstants.TABLETYPE_VARIABLE) {
			pstmt = con.prepareStatement("select id from IN_MDT_NODES where parentID=? and disabled=0 order by sequence");
			pstmt.setLong(1, parentID);
			// pstmt.setInt(2, MDTConstants.NODETYPE_VARIABLE);
			rs = pstmt.executeQuery();

			Vector<Long> v = new Vector<Long>();
			varTables.put(new Integer(tableID), v);
			while (rs.next()) {
				v.add(new Long(rs.getLong(1)));
			}
			rs.close();
			pstmt.close();
		}
	}
}

class ImportItem {

	long nodeID;
	String code;
	int tableID;
	int tableType;
	String value;
	int rowNumber;

	ImportItem() {
	}

	ImportItem(long nodeID, String code, int tableID, int tableType) {
		this.nodeID = nodeID;
		this.code = code;
		this.tableID = tableID;
		this.tableType = tableType;
	}
}
