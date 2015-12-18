package fina2.returns;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.mozilla.javascript.JavaScriptException;

import fina2.FinaTypeException;
import fina2.ProcessException;
import fina2.db.DatabaseUtil;
import fina2.i18n.Language;
import fina2.i18n.LocaleUtil;
import fina2.metadata.MDTConstants;
import fina2.script.JSTree;
import fina2.security.ServerSecurityUtil;
import fina2.util.CommonUtils;
import fina2.util.StatisticsLogger;

@SuppressWarnings("serial")
public class ProcessSessionBean implements SessionBean {

	private SessionContext ctx;

	private static Logger log = Logger.getLogger(ProcessSessionBean.class);

	public void ejbCreate() throws CreateException, EJBException, RemoteException {
	}

	public void ejbActivate() throws javax.ejb.EJBException, java.rmi.RemoteException {
	}

	public void ejbPassivate() throws javax.ejb.EJBException, java.rmi.RemoteException {
	}

	public void ejbRemove() throws javax.ejb.EJBException, java.rmi.RemoteException {
	}

	public void setSessionContext(javax.ejb.SessionContext sessionContext) throws javax.ejb.EJBException, java.rmi.RemoteException {
		ctx = sessionContext;
	}

	public String process(Handle userHandle, Handle langHandle, ReturnPK returnPK, boolean reprocess, String versionCode) throws FinaTypeException, RemoteException, EJBException {

		StatisticsLogger statLog = new StatisticsLogger("Return processing statistics", log, Level.INFO);
		String message = "";
		Hashtable<String, String> messages = new Hashtable<String, String>();
		statLog.logStage("Check user permissions");
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.returns.process");
		String numberFormat = null;

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		SimpleDateFormat format = null;
		try {
			Language lang = (Language) langHandle.getEJBObject();
			numberFormat = lang.getNumberFormat();
			format = new SimpleDateFormat((lang.getDateFormat() != null && lang.getDateFormat().trim().length() != 0) ? lang.getDateFormat().trim() : "dd/MM/yyyy");
			// int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
			statLog.logStage("Get number format");

			statLog.logStage("Get bank and period IDs");
			ps = con.prepareStatement("select periodID, bankID from IN_SCHEDULES where id in " + "(select scheduleID from IN_RETURNS where id=?)");
			ps.setInt(1, returnPK.getId());

			rs = ps.executeQuery();
			rs.next();
			int periodID = rs.getInt(1);
			int bankID = rs.getInt(2);
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			statLog.logStage("Get version ID");
			int versionId = getVersionId(con, versionCode);

			statLog.logStage("Select nodes to process");
			Hashtable<String, Hashtable<Integer, ProcessItem>> nodes = new Hashtable<String, Hashtable<Integer, ProcessItem>>();
			ps = con.prepareStatement("select a.returnID,b.id,b.type,a.rowNumber,a.value,c.type,a.tableID,b.equation,b.code,c.evalType,b.dataType "
					+ "from IN_RETURN_ITEMS a, IN_MDT_NODES b, IN_DEFINITION_TABLES c " + "where c.id=a.tableID " + "and c.definitionID in "
					+ "(select e.definitionID from IN_SCHEDULES e where e.id in " + "    (select f.scheduleID from IN_RETURNS f where f.id=a.returnID)) " + "and b.id = a.nodeID and a.returnID=? "
					+ "and a.versionID=?");

			ps.setInt(1, returnPK.getId());
			ps.setInt(2, versionId);

			rs = ps.executeQuery();
			while (rs.next()) {
				ProcessItem item = new ProcessItem();
				item.returnID = rs.getInt(1);
				item.nodeID = rs.getLong(2);
				item.nodeType = rs.getInt(3);
				item.rowNumber = rs.getInt(4);
				item.value = rs.getString(5);
				item.tableType = rs.getInt(6);
				item.tableID = rs.getInt(7);
				item.equation = rs.getString(8);
				item.code = rs.getString(9).trim();
				item.tableEvalType = rs.getInt(10);
				item.dataType = rs.getInt(11);
				Hashtable<Integer, ProcessItem> hh = null;
				if (nodes.get(item.code) == null) {
					hh = new Hashtable<Integer, ProcessItem>();
					nodes.put(item.code, hh);
				} else {
					hh = nodes.get(item.code);
				}
				hh.put(item.rowNumber, item);
			}
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			statLog.logStage("Select dependent nodes");
			getDependents(con, nodes, periodID, bankID, returnPK.getId(), reprocess, versionId);

			// Generate comma separated list of node and return IDs
			boolean hasNodes = false;
			String ids = "";
			ArrayList<Long> idsList = new ArrayList<Long>();
			String retIDs = "";
			ArrayList<Integer> retIdsList = new ArrayList<Integer>();

			Hashtable<Integer, ProcessItem> hRetIDs = new Hashtable<Integer, ProcessItem>();

			log.info("Selected, iterating");
			for (Hashtable<Integer, ProcessItem> h : nodes.values()) {
				for (ProcessItem item : h.values()) {

					if (item.nodeType == MDTConstants.NODETYPE_NODE) {
						hasNodes = true;
					}

					// if (!ids.equals("")) {
					// ids += ",";
					// }
					// ids += item.nodeID;
					idsList.add(item.nodeID);

					if (hRetIDs.get(item.returnID) == null) {
						// if (!retIDs.equals("")) {
						// retIDs += ",";
						// }
						// retIDs += item.returnID;
						retIdsList.add(item.returnID);
						hRetIDs.put(item.returnID, item);
					}
				}
			}

			if (idsList.size() == 0) {
				ids = "0";
			} else {
				ids = idsList.toString().replace("[", "").replace("]", "").trim();
			}

			if (retIdsList.size() == 0) {
				retIDs = "0";
			} else {
				retIDs = retIdsList.toString().replace("[", "").replace("]", "").trim();
			}

			statLog.logStage("Select comparisions");
			String retIdArr[] = retIDs.split(",");
			HashMap<Integer, String> map = CommonUtils.getData(retIDs);

			Hashtable<Long, ComparisonItem> comparisons = new Hashtable<Long, ComparisonItem>();
			if (retIdArr.length < 500) {
				ps = con.prepareStatement("select a.nodeID, a.condition, a.equation, b.rowNumber, b.value " + "from IN_MDT_COMPARISON a, IN_RETURN_ITEMS b "
						+ "where a.nodeID=b.nodeID and b.returnID in (" + retIDs + ") " + " and b.versionID=" + versionId);

				rs = ps.executeQuery();

				while (rs.next()) {
					ComparisonItem item = new ComparisonItem();
					item.nodeID = rs.getInt(1);
					item.condition = rs.getInt(2);
					item.equation = rs.getString(3);
					item.rowNumber = rs.getInt(4);
					item.value = rs.getString(5);
					comparisons.put((long) item.nodeID, item);
				}
			} else {
				ComparisonItem item = new ComparisonItem();
				for (int i = 0; i < map.size(); i++) {
					ps = con.prepareStatement("select a.nodeID, a.condition, a.equation, b.rowNumber, b.value " + "from IN_MDT_COMPARISON a, IN_RETURN_ITEMS b "
							+ "where a.nodeID=b.nodeID and b.returnID in (" + map.get(i) + ") " + " and b.versionID=" + versionId);

					rs = ps.executeQuery();

					while (rs.next()) {

						item.nodeID = rs.getInt(1);
						item.condition = rs.getInt(2);
						item.equation = rs.getString(3);
						item.rowNumber = rs.getInt(4);
						item.value = rs.getString(5);
						comparisons.put((long) item.nodeID, item);
					}
				}
			}

			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			if (hasNodes) {
				statLog.logStage("Select nodes to process aggregations");
				String arr[] = ids.split(",");
				HashMap<Integer, String> m = CommonUtils.getData(ids);
				Hashtable<Long, Vector<ProcessItem>> hchilds = new Hashtable<Long, Vector<ProcessItem>>();

				if (arr.length < 500) {
					ps = con.prepareStatement("select b.id,b.type,b.evalMethod,b.parentID,a.value,a.returnID,c.id,b.code from IN_RETURN_ITEMS a, IN_MDT_NODES b, IN_DEFINITION_TABLES c "
							+ "where b.parentID in " + "    (select g.parentID from IN_MDT_NODES g where id in (" + ids + ")) " + "and c.id=a.tableID " + "and c.definitionID in "
							+ "(select e.definitionID from IN_SCHEDULES e where e.id in " + "    (select f.scheduleID from IN_RETURNS f where f.id=a.returnID) " + ") " + "and b.id = a.nodeID "
							+ "and a.returnID=? " + "and a.versionID=? " + "and b.type!=?");

					ps.setInt(1, returnPK.getId());
					ps.setInt(2, versionId);
					ps.setInt(3, MDTConstants.NODETYPE_VARIABLE);

					rs = ps.executeQuery();
					while (rs.next()) {
						ProcessItem item = new ProcessItem();
						item.nodeID = rs.getInt(1);
						item.nodeType = rs.getInt(2);
						item.nodeEvalType = rs.getInt(3);
						item.parentID = rs.getInt(4);
						item.value = rs.getString(5);
						item.returnID = rs.getInt(6);
						item.tableID = rs.getInt(7);
						item.code = rs.getString(8).trim();

						Vector<ProcessItem> v = null;
						if (hchilds.get(new Long(item.parentID)) == null) {
							v = new Vector<ProcessItem>();
							hchilds.put(new Long(item.parentID), v);
						} else {
							v = hchilds.get(new Long(item.parentID));
						}
						v.add(item);
					}

				} else {

					for (int i = 0; i < m.size(); i++) {
						ps = con.prepareStatement("select b.id,b.type,b.evalMethod,b.parentID,a.value,a.returnID,c.id,b.code from IN_RETURN_ITEMS a, IN_MDT_NODES b, IN_DEFINITION_TABLES c "
								+ "where b.parentID in " + "    (select g.parentID from IN_MDT_NODES g where id in (" + m.get(i) + ")) " + "and c.id=a.tableID " + "and c.definitionID in "
								+ "(select e.definitionID from IN_SCHEDULES e where e.id in " + "    (select f.scheduleID from IN_RETURNS f where f.id=a.returnID) " + ") " + "and b.id = a.nodeID "
								+ "and a.returnID=? " + "and a.versionID=? " + "and b.type!=?");

						ps.setInt(1, returnPK.getId());
						ps.setInt(2, versionId);
						ps.setInt(3, MDTConstants.NODETYPE_VARIABLE);

						rs = ps.executeQuery();
						while (rs.next()) {
							ProcessItem item = new ProcessItem();
							item.nodeID = rs.getLong(1);
							item.nodeType = rs.getInt(2);
							item.nodeEvalType = rs.getInt(3);
							item.parentID = rs.getLong(4);
							item.value = rs.getString(5);
							item.returnID = rs.getInt(6);
							item.tableID = rs.getInt(7);
							item.code = rs.getString(8).trim();

							Vector<ProcessItem> v = null;
							if (hchilds.get(new Long(item.parentID)) == null) {
								v = new Vector<ProcessItem>();
								hchilds.put(new Long(item.parentID), v);
							} else {
								v = hchilds.get(new Long(item.parentID));
							}
							v.add(item);
						}
					}
				}

				DatabaseUtil.closeResultSet(rs);
				DatabaseUtil.closeStatement(ps);

				Vector<Long> newKeys = new Vector<Long>();
				Hashtable<Long, Vector<ProcessItem>> newChilds = (Hashtable<Long, Vector<ProcessItem>>) hchilds.clone();
				while (hchilds.size() > 0) {
					for (Iterator<Long> iter = ((Hashtable<Long, Vector<ProcessItem>>) hchilds.clone()).keySet().iterator(); iter.hasNext();) {
						Long key = iter.next();
						Vector<ProcessItem> v = hchilds.get(key);
						int i = 0;
						for (Iterator<ProcessItem> viter = v.iterator(); viter.hasNext();) {
							ProcessItem item = viter.next();
							if (item.nodeType == MDTConstants.NODETYPE_NODE) {
								if (hchilds.get(new Long(item.nodeID)) != null) {
									i++;
								}
							}
						}
						if (i == 0) {
							newKeys.add(key);
							hchilds.remove(key);
						}
					}
				}
				statLog.logStage("Evaluate sum, average, min and max aggregations");
				Hashtable<String, Hashtable<Long, ProcessItem>> updates = new Hashtable<String, Hashtable<Long, ProcessItem>>();
				for (Iterator<Long> iter = newKeys.iterator(); iter.hasNext();) {
					Long key = iter.next();
					Vector<ProcessItem> v = newChilds.get(key);
					for (Iterator<ProcessItem> viter = v.iterator(); viter.hasNext();) {

						ProcessItem item = (ProcessItem) viter.next();
						JSTree tree = new JSTree(null, null, null, versionId);
						if (item.nodeType == MDTConstants.NODETYPE_NODE) {
							switch (item.nodeEvalType) {
							case MDTConstants.EVAL_SUM:
								item.value = String.valueOf(tree.evalSum(newChilds.get(item.nodeID)));
								break;
							case MDTConstants.EVAL_AVERAGE:
								item.value = String.valueOf(tree.evalAverage(newChilds.get(item.nodeID)));
								break;
							case MDTConstants.EVAL_MIN:
								item.value = String.valueOf(tree.evalMin(newChilds.get(item.nodeID)));
								break;
							case MDTConstants.EVAL_MAX:
								item.value = String.valueOf(tree.evalMax(newChilds.get(item.nodeID)));
								break;
							}
							Hashtable<Long, ProcessItem> h = null;
							if (updates.get(item.code) == null) {
								h = new Hashtable<Long, ProcessItem>();
								updates.put(item.code, h);
							} else {
								h = updates.get(item.code);
							}
							h.put((long) item.rowNumber, item);
						}
					}
				}

				statLog.logStage("Store aggregate evaluation results");
				try {
					ps = con.prepareStatement("update IN_RETURN_ITEMS set value=?, nvalue=? " + "where returnID=? and nodeID=? and versionID=?");

					for (Hashtable<Long, ProcessItem> items : updates.values()) {
						for (ProcessItem item : items.values()) {
							ps.setString(1, item.value);
							ReturnHelper.assignNumericValue(ps, 2, item.value);
							ps.setInt(3, item.returnID);
							ps.setLong(4, item.nodeID);
							ps.setInt(5, versionId);
							ps.addBatch();
							// ps.executeUpdate();
						}
					}
					ps.executeBatch();
					DatabaseUtil.closeStatement(ps);
				} catch (Exception ex) {
					DatabaseUtil.closeConnection(con);
					throw new EJBException(ex);
				}
			}

			statLog.logStage("Get return IDs");
			String returnIDs = getReturnIds(con, periodID, bankID);

			statLog.logStage("Get dependence node IDs");
			String depIDs = getDependenceIds(con, ids);

			log.debug("ids: " + ids);
			log.debug("returnIDs: " + returnIDs);
			log.debug("Version ID: " + versionId);

			statLog.logStage("Select nodes to process regular and comparision expressions");
			// //////////////////////////////////////////////////////

			String arr[] = depIDs.split(",");
			Hashtable<String, Hashtable<Integer, ProcessItem>> values = new Hashtable<String, Hashtable<Integer, ProcessItem>>();

			if (arr.length < 500) {
				// //////////////////////////////////////////////////////
				ps = con.prepareStatement("select a.returnID,b.id,b.type,a.rowNumber,a.value,c.type,a.tableID,b.code,c.evalType " + "from IN_RETURN_ITEMS a, IN_MDT_NODES b, IN_DEFINITION_TABLES c "
						+ "where a.nodeID in (" + depIDs + ") " + "and c.id=a.tableID " + "and c.definitionID = " + "(select e.definitionID from IN_SCHEDULES e where e.id in "
						+ "    (select f.scheduleID from IN_RETURNS f where f.id=a.returnID) " + ") " + "and b.id = a.nodeID " + "and a.returnID in (" + returnIDs + ") " + "and a.versionID="
						+ versionId);
				rs = ps.executeQuery();
				while (rs.next()) {
					ProcessItem item = new ProcessItem();
					item.returnID = rs.getInt(1);
					item.nodeID = rs.getLong(2);
					item.nodeType = rs.getInt(3);
					item.rowNumber = rs.getInt(4);
					item.value = rs.getString(5);
					item.tableType = rs.getInt(6);
					item.tableID = rs.getInt(7);
					item.code = rs.getString(8).trim();
					item.tableEvalType = rs.getInt(9);

					Hashtable<Integer, ProcessItem> hh = null;
					if (values.get(item.code) == null) {
						hh = new Hashtable<Integer, ProcessItem>();
						values.put(item.code, hh);
					} else {
						hh = values.get(item.code);
					}
					hh.put(item.rowNumber, item);
				}
			} else {
				HashMap<Integer, String> mm = CommonUtils.getData(depIDs);
				for (int i = 0; i < mm.size(); i++) {
					ps = con.prepareStatement("select a.returnID,b.id,b.type,a.rowNumber,a.value,c.type,a.tableID,b.code,c.evalType "
							+ "from IN_RETURN_ITEMS a, IN_MDT_NODES b, IN_DEFINITION_TABLES c " + "where a.nodeID in (" + mm.get(i) + ") " + "and c.id=a.tableID " + "and c.definitionID = "
							+ "(select e.definitionID from IN_SCHEDULES e where e.id in " + "    (select f.scheduleID from IN_RETURNS f where f.id=a.returnID) " + ") " + "and b.id = a.nodeID "
							+ "and a.returnID in (" + returnIDs + ") " + "and a.versionID=" + versionId);
					rs = ps.executeQuery();
					while (rs.next()) {
						ProcessItem item = new ProcessItem();
						item.returnID = rs.getInt(1);
						item.nodeID = rs.getLong(2);
						item.nodeType = rs.getInt(3);
						item.rowNumber = rs.getInt(4);
						item.value = rs.getString(5);
						item.tableType = rs.getInt(6);
						item.tableID = rs.getInt(7);
						item.code = rs.getString(8).trim();
						item.tableEvalType = rs.getInt(9);

						Hashtable<Integer, ProcessItem> hh = null;
						if (values.get(item.code) == null) {
							hh = new Hashtable<Integer, ProcessItem>();
							values.put(item.code, hh);
						} else {
							hh = values.get(item.code);
						}
						hh.put(item.rowNumber, item);
					}
				}
			}

			DatabaseUtil.closeStatement(ps);

			Hashtable dependents = new Hashtable();
			Hashtable involvedReturns = new Hashtable();
			Hashtable<String, Hashtable<Integer, ProcessItem>> updates = new Hashtable<String, Hashtable<Integer, ProcessItem>>();

			Map<Integer, List<ProcessException>> exceptionsMap = new HashMap<Integer, List<ProcessException>>();

			validateItems(nodes, format, exceptionsMap);

			statLog.logStage("Evaluate expressions");
			for (Hashtable<Integer, ProcessItem> items : nodes.values()) {
				evalScript(con, returnPK, items, values, updates, dependents, involvedReturns, comparisons, messages, versionId);
			}

			statLog.logStage("Evaluate comparision expressions");
			for (Hashtable<Integer, ProcessItem> items : nodes.values()) {
				try {
					evalCompScript(con, returnPK, items, values, updates, dependents, involvedReturns, comparisons, messages, versionId, numberFormat);
				} catch (ProcessException ex) {
					if (exceptionsMap.get(ex.getProcessItem().returnID) == null) {
						List<ProcessException> list = new ArrayList<ProcessException>();
						list.add(ex);
						exceptionsMap.put(ex.getProcessItem().returnID, list);
					} else {
						exceptionsMap.get(ex.getProcessItem().returnID).add(ex);
					}
				}
			}

			try {
				statLog.logStage("Store regular and comparision expression evaluation results");
				ps = con.prepareStatement("update IN_RETURN_ITEMS set value=?, nvalue=? " + "where  returnID=? and nodeID=? and versionID=? and rowNumber=? ");

				for (Hashtable<Integer, ProcessItem> items : updates.values()) {
					for (ProcessItem item : items.values()) {
						ps.setString(1, item.value);
						ReturnHelper.assignNumericValue(ps, 2, item.value);
						ps.setInt(3, item.returnID);
						ps.setLong(4, item.nodeID);
						ps.setInt(5, versionId);
						ps.setInt(6, item.rowNumber);
						ps.addBatch();
					}
				}
				ps.executeBatch();
			} catch (Exception ex) {
				throw new EJBException(ex);
			}

			String warningMessage = "";
			message = "";

			// Complect Process Exception
			for (Entry<Integer, List<ProcessException>> entry : exceptionsMap.entrySet()) {

				int returnId = entry.getKey();

				List<ProcessException> exceptions = entry.getValue();

				if (returnId != returnPK.getId()) {

					String returnDefinitionCode = "";

					PreparedStatement psReturnDefinitionCode = null;
					ResultSet rsReturnDefinitionCode = null;

					try {
						psReturnDefinitionCode = con
								.prepareStatement("select rd.code from in_returns as r, in_return_definitions as rd, in_schedules as s where r.scheduleid=s.id and s.definitionid=rd.id and r.id=?");
						psReturnDefinitionCode.setInt(1, returnId);

						rsReturnDefinitionCode = psReturnDefinitionCode.executeQuery();

						if (rsReturnDefinitionCode.next()) {
							returnDefinitionCode = rsReturnDefinitionCode.getString(1);

						}

					} catch (Exception ex) {
						log.error(ex.getMessage(), ex);
					} finally {
						DatabaseUtil.closeStatement(psReturnDefinitionCode);
						DatabaseUtil.closeResultSet(rsReturnDefinitionCode);
					}

					warningMessage = "Warning:\nError in return: " + returnDefinitionCode + "\n";

				}

				String errorMessage = "";

				for (ProcessException pex : exceptions) {
					errorMessage += pex.getMessage();
				}

				warningMessage += errorMessage;

				// Change Status
				try {

					getReturnSession().changeReturnStatus(userHandle, langHandle, new ReturnPK(returnId), ReturnConstants.STATUS_ERRORS, errorMessage, versionCode);

					log.info("Process ok.");

				} catch (Exception e) {
					log.error(e.getMessage(), e);
					throw new EJBException(e);
				}

				message += errorMessage;

			}

			if (exceptionsMap.get(returnPK.getId()) == null) {
				String processMessage = "Process ok.\n\n" + warningMessage;
				message = processMessage;
				getReturnSession().changeReturnStatus(userHandle, langHandle, returnPK, ReturnConstants.STATUS_PROCESSED, processMessage, versionCode);
			}

		} catch (SQLException ex) {
			throw new EJBException(ex);
		} catch (Exception ex) {
			throw new EJBException(ex);
		} finally {
			statLog.logSummary();
			DatabaseUtil.close(rs, ps, con);
		}

		return message;
	}

	private void validateItems(Hashtable<String, Hashtable<Integer, ProcessItem>> nodes, SimpleDateFormat format, Map<Integer, List<ProcessException>> exceptionsMap) {

		for (Hashtable<Integer, ProcessItem> items : nodes.values()) {
			for (ProcessItem item : items.values()) {
				if (item.nodeType == MDTConstants.NODETYPE_INPUT) {

					try {
						if (item.dataType == MDTConstants.DATATYPE_NUMERIC) {
							try {
								if ((item.value != null) && (!item.value.trim().equals("")) && (!item.value.trim().equals("undefined")) && (!item.value.trim().equals("Infinity"))
										&& (!item.value.trim().equals("X")) && (!item.value.trim().equals("NaN"))) {

									Double.parseDouble(item.value);

								}
							} catch (NumberFormatException ex) {
								log.error(ex.getMessage(), ex);
								throw new ProcessException("Error: The value for node code " + item.code + " must be numeric but is " + item.value + "\n", ex, item,
										ProcessException.Reason.NumberFormat);
							}
						} else if (item.dataType == MDTConstants.DATATYPE_DATE) {
							try {
								if ((item.value != null) && (!item.value.trim().equals("")))
									format.parse(item.value);
							} catch (ParseException ex) {
								log.error(ex.getMessage(), ex);
								throw new ProcessException("Error: Wrong date format. \n Code=" + item.code + " Row=" + item.rowNumber + " Value=" + item.value + "\n", ex, item,
										ProcessException.Reason.WrongDateFormat);
							}
						}
					} catch (ProcessException ex) {
						if (exceptionsMap.get(ex.getProcessItem().returnID) == null) {
							List<ProcessException> list = new ArrayList<ProcessException>();
							list.add(ex);
							exceptionsMap.put(ex.getProcessItem().returnID, list);
						} else {
							exceptionsMap.get(ex.getProcessItem().returnID).add(ex);
						}
					}
				}

			}

		}
	}

	private ReturnSession getReturnSession() throws NamingException, CreateException, RemoteException {
		InitialContext jndi = new InitialContext();
		Object ref = jndi.lookup("fina2/returns/ReturnSession");
		ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);
		return home.create();
	}

	private String getDependenceIds(Connection con, String ids) throws SQLException {

		HashMap<Integer, String> mm = CommonUtils.getData(ids);
		String arr[] = ids.split(",");
		PreparedStatement ps = null;
		String depIDs = "";
		ResultSet rs = null;

		if (arr.length < 500) {
			ps = con.prepareStatement("select d.nodeID from IN_MDT_DEPENDENT_NODES d " + "where d.dependentNodeID in (" + ids + ")");

			rs = ps.executeQuery();

			while (rs.next()) {
				long nID = rs.getLong(1);
				if (depIDs.length() > 0) {
					depIDs += ",";
				}
				depIDs += nID;
			}
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
			// DatabaseUtil.closeConnection(con);
		} else {
			for (int i = 0; i < mm.size(); i++) {
				// con=DatabaseUtil.getConnection();
				ps = con.prepareStatement("select d.nodeID from IN_MDT_DEPENDENT_NODES d " + "where d.dependentNodeID in (" + mm.get(i) + ")");

				rs = ps.executeQuery();

				while (rs.next()) {
					long nID = rs.getLong(1);
					if (depIDs.length() > 0) {
						depIDs += ",";
					}
					depIDs += nID;
				}
				DatabaseUtil.closeResultSet(rs);
				DatabaseUtil.closeStatement(ps);
				// DatabaseUtil.closeConnection(con);
			}
		}

		if (depIDs.length() == 0) {
			depIDs = "0";
		}
		// DatabaseUtil.closeResultSet(rs);
		// DatabaseUtil.closeStatement(ps);

		return depIDs;
	}

	private String getDependentIds(Connection con, Hashtable<String, Hashtable<Integer, ProcessItem>> nodes) throws SQLException {

		ArrayList<Long> ids = new ArrayList<Long>();
		for (Hashtable<Integer, ProcessItem> h : nodes.values()) {
			for (ProcessItem item : h.values()) {
				ids.add(item.nodeID);
			}
		}

		ArrayList<Long> dependentIds = new ArrayList<Long>();
		getDependentIds(con, ids, dependentIds);

		String depIDs = dependentIds.toString().replace("[", "").replace("]", "").trim();
		if (depIDs.length() == 0) {
			depIDs = "0";
		}

		// for (Long depId : dependentIds) {
		// if (depIDs.length() > 0) {
		// depIDs += ",";
		// }
		// depIDs += depId;
		// }
		//
		// if (depIDs.length() == 0) {
		// depIDs = "0";
		// }

		return depIDs;
	}

	void getDependentIds(Connection con, Collection<Long> nodeIds, Collection<Long> dependentIds) throws SQLException {

		String ids = "";

		// for (Long nodeId : nodeIds) {
		//
		// if (!ids.equals("")) {
		// ids += ",";
		// }
		// ids += nodeId;
		// }
		// if (ids.equals("")) {
		// ids = "0";
		// }
		ids = nodeIds.toString().replace("[", "").replace("]", "").trim();
		if (ids.equals(""))
			ids = "0";
		HashMap<Integer, String> mm = CommonUtils.getData(ids);
		String arr[] = ids.split(",");

		/*
		 * PreparedStatement ps = con .prepareStatement(
		 * "select distinct d.dependentNodeID from IN_MDT_DEPENDENT_NODES d " +
		 * "where d.nodeID in (" + ids + ") and d.dependentNodeID not in (" +
		 * ids + ")");
		 */
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Long> depIds = new ArrayList<Long>();

		if (arr.length < 500) {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("select distinct d.dependentNodeID from IN_MDT_DEPENDENT_NODES d " + "where d.nodeID in (" + ids + ") and d.dependentNodeID not in (" + ids + ")");
			rs = ps.executeQuery();
			depIds = new ArrayList<Long>();
			while (rs.next()) {
				depIds.add(rs.getLong(1));
			}
			DatabaseUtil.close(rs, ps, con);
		} else {
			depIds = new ArrayList<Long>();
			for (int i = 0; i < mm.size(); i++) {
				con = DatabaseUtil.getConnection();
				ps = con.prepareStatement("select distinct d.dependentNodeID from IN_MDT_DEPENDENT_NODES d " + "where d.nodeID in (" + mm.get(i) + ") and d.dependentNodeID not in (" + mm.get(i) + ")");
				rs = ps.executeQuery();

				while (rs.next()) {
					depIds.add(rs.getLong(1));
				}
				DatabaseUtil.close(rs, ps, con);
			}

		}

		// DatabaseUtil.closeResultSet(rs);
		// DatabaseUtil.closeStatement(ps);

		if (depIds.size() > 0) {
			getDependentIds(con, depIds, dependentIds);
		}

		dependentIds.addAll(depIds);
	}

	private String getReturnIds(Connection con, int periodID, int bankID) throws SQLException {

		PreparedStatement ps = con.prepareStatement("select g.id from IN_RETURNS g where g.scheduleID in "
				+ "    (select h.id from IN_SCHEDULES h where h.periodID in (select j.id from IN_PERIODS j where j.toDate=(select k.toDate from IN_PERIODS k where k.id=?)) and h.bankID=?) ");

		ps.setInt(1, periodID);
		ps.setInt(2, bankID);
		ResultSet rs = ps.executeQuery();
		String returnIDs = "";
		while (rs.next()) {
			int rID = rs.getInt(1);
			if (returnIDs.length() > 0) {
				returnIDs += ",";
			}
			returnIDs += rID;
		}
		if (returnIDs.length() == 0) {
			returnIDs = "0";
		}
		DatabaseUtil.closeResultSet(rs);
		DatabaseUtil.closeStatement(ps);

		return returnIDs;
	}

	private static Hashtable allNodes = new Hashtable();
	private static Hashtable allValues = new Hashtable();
	private static Hashtable allChilds = new Hashtable();
	private static Hashtable versions = new Hashtable();

	public void prepareAutoProcess(long guid, int returnID, String versionCode) throws RemoteException, EJBException {

		Connection con = null;

		try {
			long t = System.currentTimeMillis();

			con = DatabaseUtil.getConnection();

			int versionId = getVersionId(con, versionCode);
			versions.put(new Long(guid), new Integer(versionId));

			Hashtable nodes = getNodes(con, returnID, versionId);
			logHashtableContent("Selected nodes", nodes);

			long[] longIdsArray = getNodeIdsArray(nodes);

			Hashtable childs = getChilds(con, returnID, longIdsArray, versionId);
			logChilds("Selected childs", childs);

			String returnIds = getReturnIds(con, returnID);
			Hashtable values = getValues(con, returnIds, longIdsArray, versionId);
			logHashtableContent("Selected values", values);

			setData(allNodes, guid, nodes);
			setData(allChilds, guid, childs);
			setData(allValues, guid, values);

			log.info("Auto process preparation finished in " + (System.currentTimeMillis() - t) + " ms.");

		} catch (SQLException ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public Hashtable getUpdates(long guid, int returnId, long nodeId, int rowNumber, String value) throws RemoteException, EJBException {

		Connection con = null;
		try {
			con = DatabaseUtil.getConnection();
			long t = System.currentTimeMillis();

			int versionId = ((Integer) versions.get(new Long(guid))).intValue();

			Hashtable nodes = getData(allNodes, guid);
			Hashtable childs = getData(allChilds, guid);
			Hashtable values = getData(allValues, guid);

			synchronized (nodes) {

				updateValues(nodeId, rowNumber, value, childs, values);

				logChilds("Current childs", childs);
				logHashtableContent("Current values", values);

				Hashtable updates = calculateNodeValues((Hashtable) childs.clone(), versionId);

				Map depCodes = null;
				if (nodeId != -1) {
					depCodes = new Hashtable();
					for (Iterator iter = updates.values().iterator(); iter.hasNext();) {

						Hashtable v = (Hashtable) iter.next();
						ProcessItem i = (ProcessItem) v.values().iterator().next();
						depCodes.putAll(getDependentCodes(i.nodeID));
					}

					depCodes.putAll(getDependentCodes(nodeId));

				}
				calculateVariableValues(con, new ReturnPK(returnId), nodes, values, updates, depCodes, rowNumber, versionId);

				updateValues(updates, childs, values);

				logHashtableContent("Calculated updates", updates);

				log.info("Updates recalculated in " + (System.currentTimeMillis() - t) + " ms.");

				return updates;
			}
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public void insertRow(long guid, int tableID, int rowNumber, int returnID, Collection itemIds) throws RemoteException, EJBException {

		log.info("Inserting VCT row. Table ID: " + tableID + " Row number: " + rowNumber);

		Hashtable nodes = getData(allNodes, guid);
		Hashtable values = getData(allValues, guid);

		synchronized (nodes) {
			updateRowNumber(tableID, rowNumber, nodes, true);
			updateRowNumber(tableID, rowNumber, values, true);

			Collection items = getItems(itemIds, tableID, rowNumber, returnID);

			insertItems(items, nodes);
			insertItems(cloneItems(items), values);

			logHashtableContent("Current nodes", nodes);
			logHashtableContent("Current values", values);
		}
	}

	public void removeRow(long guid, int tableID, int rowNumber) throws RemoteException, EJBException {

		log.info("Removing VCT row. Table ID: " + tableID + " Row number: " + rowNumber);

		Hashtable nodes = getData(allNodes, guid);
		Hashtable values = getData(allValues, guid);

		synchronized (nodes) {

			removeRowNumber(tableID, rowNumber, nodes);
			removeRowNumber(tableID, rowNumber, values);

			nodes = updateRowNumber(tableID, rowNumber, nodes, false);
			values = updateRowNumber(tableID, rowNumber, values, false);

			logHashtableContent("Current nodes", nodes);
			logHashtableContent("Current values", values);
		}
	}

	public void cleanup(long guid) throws RemoteException, EJBException {

		allNodes.remove(new Long(guid));
		allChilds.remove(new Long(guid));
		allValues.remove(new Long(guid));
	}

	private Collection cloneItems(Collection items) {

		ArrayList newItems = new ArrayList();

		for (Iterator iter = items.iterator(); iter.hasNext();) {

			ProcessItem item = (ProcessItem) iter.next();
			ProcessItem newItem = new ProcessItem();

			newItem.nodeID = item.nodeID;
			newItem.code = item.code;
			newItem.nodeType = item.nodeType;
			newItem.equation = item.equation;
			newItem.rowNumber = item.rowNumber;
			newItem.tableType = item.tableType;
			newItem.tableID = item.tableID;
			newItem.value = item.value;
			newItem.returnID = item.returnID;

			newItems.add(newItem);
		}

		return newItems;
	}

	private Collection getItems(Collection c, int tableId, int rowNumber, int returnID) throws EJBException {

		ArrayList items = new ArrayList();

		String ids = "";
		for (Iterator iter = c.iterator(); iter.hasNext();) {
			Long id = (Long) iter.next();
			if (!ids.equals("")) {
				ids += ",";
			}
			ids += id;
		}

		if (ids.equals("")) {
			ids = "0";
		}

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DatabaseUtil.getConnection();
			String arr[] = ids.split(",");
			HashMap<Integer, String> map = CommonUtils.getData(ids);
			if (arr.length < 500) {
				ps = con.prepareStatement("select m.id, m.code, m.type, m.equation, m.dataType from in_mdt_nodes m where m.id in(" + ids + ")");

				rs = ps.executeQuery();
				while (rs.next()) {
					ProcessItem item = new ProcessItem();

					item.nodeID = rs.getLong(1);
					item.code = rs.getString(2).trim();
					item.nodeType = rs.getInt(3);
					item.equation = rs.getString(4);
					item.rowNumber = rowNumber;
					item.tableType = ReturnConstants.TABLETYPE_VARIABLE;
					item.tableID = tableId;
					item.returnID = returnID;

					if (rs.getInt(5) == MDTConstants.DATATYPE_NUMERIC) {
						item.value = "0.0";
					} else {
						item.value = "";
					}

					items.add(item);
				}
			} else {
				for (int i = 0; i < map.size(); i++) {
					ps = con.prepareStatement("select m.id, m.code, m.type, m.equation, m.dataType from in_mdt_nodes m where m.id in(" + map.get(i) + ")");

					rs = ps.executeQuery();
					while (rs.next()) {
						ProcessItem item = new ProcessItem();

						item.nodeID = rs.getLong(1);
						item.code = rs.getString(2).trim();
						item.nodeType = rs.getInt(3);
						item.equation = rs.getString(4);
						item.rowNumber = rowNumber;
						item.tableType = ReturnConstants.TABLETYPE_VARIABLE;
						item.tableID = tableId;
						item.returnID = returnID;

						if (rs.getInt(5) == MDTConstants.DATATYPE_NUMERIC) {
							item.value = "0.0";
						} else {
							item.value = "";
						}

						items.add(item);
					}
				}

			}

		} catch (SQLException ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return items;
	}

	private void insertItems(Collection items, Hashtable nodes) {

		for (Iterator iter = items.iterator(); iter.hasNext();) {

			ProcessItem item = (ProcessItem) iter.next();
			Hashtable v = (Hashtable) nodes.get(item.code);
			if (v == null) {
				v = new Hashtable();
				nodes.put(item.code, v);
			}

			v.put(new Integer(item.rowNumber), item);
		}
	}

	private Map getDependentCodes(long nodeId) throws EJBException {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		Map codes = new Hashtable();

		try {
			con = DatabaseUtil.getConnection();

			ps = con.prepareStatement("select m.id, m.code from IN_MDT_NODES m, IN_MDT_DEPENDENT_NODES d " + "where d.nodeid = ? and d.dependentnodeid=m.id");
			ps.setLong(1, nodeId);

			rs = ps.executeQuery();

			while (rs.next()) {

				long id = rs.getLong(1);
				String code = rs.getString(2);

				codes.put(code.trim(), code.trim());
				codes.putAll(getDependentCodes(id));
			}

		} catch (SQLException ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return codes;
	}

	private void removeRowNumber(int tableID, int rowNumber, Hashtable nodes) {

		for (Iterator iter = nodes.values().iterator(); iter.hasNext();) {

			Hashtable tabl = (Hashtable) iter.next();
			for (Iterator viter = tabl.values().iterator(); viter.hasNext();) {
				ProcessItem item = (ProcessItem) viter.next();
				if (item.tableID == tableID && item.rowNumber == rowNumber) {
					viter.remove();
				}
			}
		}
	}

	private Hashtable updateRowNumber(int tableID, int rowNumber, Hashtable nodes, boolean increment) {

		ArrayList items = new ArrayList();
		for (Iterator iter = nodes.values().iterator(); iter.hasNext();) {

			Hashtable tabl = (Hashtable) iter.next();
			for (Iterator viter = tabl.values().iterator(); viter.hasNext();) {
				ProcessItem item = (ProcessItem) viter.next();
				items.add(item);
				if (item.tableID == tableID && item.rowNumber >= rowNumber) {

					if (increment) {
						item.rowNumber++;
					} else {
						item.rowNumber--;
					}
				}
			}
		}

		nodes.clear();
		for (Iterator iter = items.iterator(); iter.hasNext();) {

			ProcessItem item = (ProcessItem) iter.next();

			Hashtable v = (Hashtable) nodes.get(item.code);
			if (v == null) {
				v = new Hashtable();
				nodes.put(item.code, v);
			}

			v.put(new Integer(item.rowNumber), item);
		}

		return nodes;
	}

	private void calculateVariableValues(Connection con, ReturnPK returnPK, Hashtable nodes, Hashtable values, Hashtable updates, Map depCodes, int rowNumber, int versionId) {

		Hashtable dependents = new Hashtable();
		Hashtable involvedReturns = new Hashtable();
		Hashtable comparisons = new Hashtable();
		Hashtable messages = new Hashtable();

		for (Iterator keys = nodes.keySet().iterator(); keys.hasNext();) {

			String code = (String) keys.next();

			if (depCodes == null || depCodes.get(code) != null) {
				Hashtable items = (Hashtable) nodes.get(code);
				evalScript(con, returnPK, items, values, updates, dependents, involvedReturns, comparisons, messages, rowNumber, versionId);
			}
		}
	}

	private Hashtable calculateNodeValues(Hashtable childs, int versionId) {

		Vector newKeys = new Vector();
		Hashtable newChilds = (Hashtable) childs.clone();

		while (childs.size() > 0) {

			for (Iterator iter = ((Hashtable) childs.clone()).keySet().iterator(); iter.hasNext();) {

				Object parentId = iter.next();
				Vector v = (Vector) childs.get(parentId);
				int i = 0;
				for (Iterator viter = v.iterator(); viter.hasNext();) {
					ProcessItem item = (ProcessItem) viter.next();
					if (item.nodeType == MDTConstants.NODETYPE_NODE) {
						if (childs.get(new Long(item.nodeID)) != null) {
							i++;
						}
					}
				}
				if (i == 0) {
					newKeys.add(parentId);
					childs.remove(parentId);
				}
			}
		}

		Hashtable updates = new Hashtable();
		for (Iterator iter = newKeys.iterator(); iter.hasNext();) {
			Long key = (Long) iter.next();
			Vector v = (Vector) newChilds.get(key);
			for (Iterator viter = v.iterator(); viter.hasNext();) {
				ProcessItem item = (ProcessItem) viter.next();
				JSTree tree = new JSTree(null, null, null, versionId);
				if (item.nodeType == MDTConstants.NODETYPE_NODE) {
					switch (item.nodeEvalType) {
					case MDTConstants.EVAL_SUM:
						item.value = Double.toString(tree.evalSum((Collection) newChilds.get(new Long(item.nodeID))));
						break;
					case MDTConstants.EVAL_AVERAGE:
						item.value = Double.toString(tree.evalAverage((Collection) newChilds.get(new Long(item.nodeID))));
						break;
					case MDTConstants.EVAL_MIN:
						item.value = Double.toString(tree.evalMin((Collection) newChilds.get(new Long(item.nodeID))));
						break;
					case MDTConstants.EVAL_MAX:
						item.value = Double.toString(tree.evalMax((Collection) newChilds.get(new Long(item.nodeID))));
						break;
					}
					Hashtable h = null;
					if (updates.get(item.code) == null) {
						h = new Hashtable();
						updates.put(item.code, h);
					} else {
						h = (Hashtable) updates.get(item.code);
					}
					h.put(new Integer(item.rowNumber), item);
				}
			}
		}
		return updates;
	}

	private void updateValues(Hashtable updates, Hashtable childs, Hashtable values) {

		for (Iterator iter = updates.values().iterator(); iter.hasNext();) {
			Hashtable vls = (Hashtable) iter.next();

			for (Iterator viter = vls.values().iterator(); viter.hasNext();) {
				ProcessItem item = (ProcessItem) viter.next();

				updateValues(item.nodeID, item.rowNumber, item.value, childs, values);
			}
		}

	}

	private void updateValues(long nodeId, int rowNumber, String value, Hashtable childs, Hashtable values) {

		boolean updatedNode = false;

		log.info("Value was changed. Node ID: " + nodeId + ", Row number: " + rowNumber + ", value: " + value);

		for (Iterator iter = values.values().iterator(); iter.hasNext();) {
			Hashtable vls = (Hashtable) iter.next();

			for (Iterator viter = vls.values().iterator(); viter.hasNext();) {
				ProcessItem item = (ProcessItem) viter.next();
				if (item.nodeID == nodeId) {

					if (item.tableType != ReturnConstants.TABLETYPE_VARIABLE || item.rowNumber == rowNumber) {
						updatedNode = true;
						item.value = value;
					}
				}
			}
		}

		for (Iterator iter = childs.values().iterator(); iter.hasNext();) {
			Vector vec = (Vector) iter.next();
			for (Iterator items = vec.iterator(); items.hasNext();) {
				ProcessItem item = (ProcessItem) items.next();
				if (item.nodeID == nodeId && (item.tableType != ReturnConstants.TABLETYPE_VARIABLE || item.rowNumber == rowNumber)) {
					updatedNode = true;
					item.value = value;
				}
			}
		}

		if (!updatedNode) {
			log.warn("Value was not updated. Node ID: " + nodeId + ", Row number: " + rowNumber + ", value: " + value);
		}
	}

	private Hashtable getNodes(Connection con, int returnId, int versionId) throws SQLException {

		Hashtable nodes = new Hashtable();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = con.prepareStatement("select a.returnID,b.id,b.type,a.rowNumber,a.value,c.type,a.tableID,b.equation,b.code,c.evalType "
					+ "from IN_RETURN_ITEMS a, IN_MDT_NODES b, IN_DEFINITION_TABLES c " + "where c.id=a.tableID " + "and c.definitionID in "
					+ "(select e.definitionID from IN_SCHEDULES e where e.id in " + "    (select f.scheduleID from IN_RETURNS f where f.id=a.returnID) " + ") "
					+ "and b.id = a.nodeID and a.returnID=? " + "and a.versionID=?");
			ps.setInt(1, returnId);
			ps.setInt(2, versionId);

			rs = ps.executeQuery();
			while (rs.next()) {
				ProcessItem item = new ProcessItem();
				item.returnID = rs.getInt(1);
				item.nodeID = rs.getLong(2);
				item.nodeType = rs.getInt(3);
				item.rowNumber = rs.getInt(4);
				item.value = rs.getString(5);
				item.tableType = rs.getInt(6);
				item.tableID = rs.getInt(7);
				item.equation = rs.getString(8);
				item.code = rs.getString(9).trim();
				item.tableEvalType = rs.getInt(10);
				Hashtable hh = null;
				if (nodes.get(item.code) == null) {
					hh = new Hashtable();
					nodes.put(item.code, hh);
				} else {
					hh = (Hashtable) nodes.get(item.code);
				}
				hh.put(new Integer(item.rowNumber), item);
			}
			getDependents(con, nodes, 0, 0, returnId, false, versionId);
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
		}

		return nodes;
	}

	/**
	 * Generates SQL query for 'getChilds' function
	 * 
	 * @param paramLimit
	 *            "IN ()" will cause ORA-00936: missing expression so value
	 *            <b>MUST</b> be > 0
	 * @param returnId
	 * @param versionId
	 * @param nodetype
	 * @return SQL query <b>String</b>
	 */
	private String generateSql_forGetChilds(int paramLimit, int returnId, int versionId, int nodetype) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("select b.id,b.type,b.evalMethod,b.parentID,a.value,a.rowNumber,a.returnID,c.id,b.code from IN_RETURN_ITEMS a, (select x.id, x.type, x.evalMethod, x.parentID, x.code from IN_MDT_NODES x inner join ");
		buffer.append("(( SELECT distinct(parentID) parentID FROM IN_MDT_NODES WHERE id IN (");
		if (paramLimit > 0)
			buffer.append("?");
		for (int i = 1; i < paramLimit; i++) {
			buffer.append(",?");
		}
		buffer.append("))) y on x.parentID = y.parentID ) b, IN_DEFINITION_TABLES c ");
		buffer.append("where c.id=a.tableID and c.definitionID in (select definitionID from IN_SCHEDULES where id in (select scheduleID from IN_RETURNS where id=a.returnID)) ");
		buffer.append("and b.id = a.nodeID and a.returnID=");
		buffer.append(returnId);
		buffer.append(" and a.versionID=");
		buffer.append(versionId);
		buffer.append(" and b.type!=");
		buffer.append(nodetype);

		return buffer.toString();
	}

	/**
	 * Fills PreparedStatement with values from supplied params array
	 * 
	 * @param ps
	 *            statement
	 * @param paramLimit
	 *            number of parameters (?) in statement
	 * @param params
	 *            supplied array of values
	 * @param offset
	 *            offset of first value to use in array
	 * @throws SQLException
	 *             if something bad happens
	 */
	private void fillStatementWithLongParams(PreparedStatement ps, int paramLimit, long[] params, int offset) throws SQLException {
		ps.clearParameters();
		int i = 0;
		for (; i < paramLimit && offset + i < params.length; i++) {
			ps.setLong(i + 1, params[offset + i]);
		}

		// if params array doesn't contain enough values fill unset
		// statement parameters with last value from params array
		if (i < paramLimit && offset + i >= params.length) {
			long lastParam = params[params.length - 1];
			for (; i < paramLimit; i++) {
				ps.setLong(i + 1, lastParam);
			}
		}
	}

	private void questionResultSet_forGetChids(Hashtable childs, ResultSet rs) throws SQLException {
		while (rs.next()) {
			ProcessItem item = new ProcessItem();
			item.nodeID = rs.getLong(1);
			item.nodeType = rs.getInt(2);
			item.nodeEvalType = rs.getInt(3);
			item.parentID = rs.getLong(4);
			item.value = rs.getString(5);
			item.rowNumber = rs.getInt(6);
			item.returnID = rs.getInt(7);
			item.tableID = rs.getInt(8);
			item.code = rs.getString(9).trim();

			Vector v = null;
			if (childs.get(new Long(item.parentID)) == null) {
				v = new Vector();
				childs.put(new Long(item.parentID), v);
			} else {
				v = (Vector) childs.get(new Long(item.parentID));
			}
			v.add(item);
		}
	}

	private Hashtable getChilds(Connection con, int returnId, long[] ids, int versionId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;

		Hashtable childs = new Hashtable();
		int paramLimit = 500;

		try {
			if (DatabaseUtil.isMssql() || ids.length < paramLimit) {
				ps = con.prepareStatement(generateSql_forGetChilds(ids.length, returnId, versionId, MDTConstants.NODETYPE_VARIABLE));

				fillStatementWithLongParams(ps, ids.length, ids, 0);
				rs = ps.executeQuery();
				questionResultSet_forGetChids(childs, rs);
			} else {
				ps = con.prepareStatement(generateSql_forGetChilds(paramLimit, returnId, versionId, MDTConstants.NODETYPE_VARIABLE));

				for (int i = 0; i < ids.length; i += paramLimit) {
					fillStatementWithLongParams(ps, paramLimit, ids, i);
					rs = ps.executeQuery();
					questionResultSet_forGetChids(childs, rs);
				}
			}
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
		}

		return childs;
	}

	/**
	 * Generates SQL query for 'getValues' function
	 * 
	 * @param paramLimit
	 *            "IN ()" will cause ORA-00936: missing expression so value
	 *            <b>MUST</b> be > 0
	 * @param returnIDs
	 * @param versionId
	 * @return SQL query <b>String</b>
	 */
	private String generateSql_forGetValues(int paramLimit, String returnIDs, int versionId) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("select a.returnID,b.id,b.type,a.rowNumber,a.value,c.type,a.tableID,b.code,c.evalType from IN_RETURN_ITEMS a, IN_MDT_NODES b, IN_DEFINITION_TABLES c ");
		buffer.append("where a.nodeID in (select d.nodeID from IN_MDT_DEPENDENT_NODES d where d.dependentNodeID in (");
		if (paramLimit > 0)
			buffer.append("?");
		for (int i = 1; i < paramLimit; i++) {
			buffer.append(",?");
		}
		buffer.append(")) and c.id=a.tableID and c.definitionID = (select e.definitionID from IN_SCHEDULES e where e.id in ");
		buffer.append("(select f.scheduleID from IN_RETURNS f where f.id=a.returnID)) and b.id = a.nodeID and a.returnID in (");
		buffer.append(returnIDs);
		buffer.append(") and a.versionID=");
		buffer.append(versionId);

		return buffer.toString();
	}

	private void questionResultSet_forGetValues(Hashtable values, ResultSet rs) throws SQLException {
		Hashtable hh = null;
		while (rs.next()) {
			ProcessItem item = new ProcessItem();
			item.returnID = rs.getInt(1);
			item.nodeID = rs.getLong(2);
			item.nodeType = rs.getInt(3);
			item.rowNumber = rs.getInt(4);
			item.value = rs.getString(5);
			item.tableType = rs.getInt(6);
			item.tableID = rs.getInt(7);
			item.code = rs.getString(8).trim();
			item.tableEvalType = rs.getInt(9);

			if (values.get(item.code) == null) {
				hh = new Hashtable();
				values.put(item.code, hh);
			} else {
				hh = (Hashtable) values.get(item.code);
			}
			hh.put(new Integer(item.rowNumber), item);
		}
	}

	private Hashtable getValues(Connection con, String returnIDs, long[] ids, int versionId) throws SQLException {
		Hashtable values = new Hashtable();

		PreparedStatement ps = null;
		ResultSet rs = null;
		int paramLimit = 500;

		try {
			if (DatabaseUtil.isMssql() || (ids.length < paramLimit)) {

				ps = con.prepareStatement(generateSql_forGetValues(ids.length, returnIDs, versionId));
				fillStatementWithLongParams(ps, ids.length, ids, 0);

				rs = ps.executeQuery();
				questionResultSet_forGetValues(values, rs);
			} else {
				ps = con.prepareStatement(generateSql_forGetValues(paramLimit, returnIDs, versionId));
				for (int i = 0; i < ids.length; i += paramLimit) {
					fillStatementWithLongParams(ps, paramLimit, ids, i);

					rs = ps.executeQuery();
					questionResultSet_forGetValues(values, rs);
				}
			}
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
		}

		return values;
	}

	private String getReturnIds(Connection con, int returnId) throws SQLException {

		StringBuffer returnIDs = new StringBuffer();

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = con.prepareStatement("select r.id from IN_RETURNS r, IN_SCHEDULES s, IN_PERIODS p " + "  where r.scheduleid=s.id and s.periodid=p.id and s.bankid="
					+ "    (select s2.bankID from IN_SCHEDULES s2, IN_RETURNS r2 " + "      where r2.id=? and r2.scheduleid = s2.id) and p.todate="
					+ "    (select p2.todate from IN_SCHEDULES s2, IN_RETURNS r2, IN_PERIODS p2 " + "      where r2.id=? and r2.scheduleid=s2.id and p2.id=s2.periodid)");

			ps.setInt(1, returnId);
			ps.setInt(2, returnId);
			rs = ps.executeQuery();

			if (rs.next()) {
				returnIDs.append(rs.getInt(1));
			}
			while (rs.next()) {
				returnIDs.append(",");
				returnIDs.append(rs.getInt(1));
			}

			if (returnIDs.toString().equalsIgnoreCase("")) {
				returnIDs.append("0");
			}

		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
		}

		log.debug("Return ID(s): " + returnIDs);

		return returnIDs.toString();
	}

	private String getNodeIds(Hashtable nodes) {

		String ids = "";

		for (Iterator keys = nodes.keySet().iterator(); keys.hasNext();) {

			Hashtable h = (Hashtable) nodes.get(keys.next());
			ProcessItem item = (ProcessItem) h.values().iterator().next();
			if (!ids.equals(""))
				ids += ",";
			ids += item.nodeID;
		}

		if (ids.equals("")) {
			ids = "0";
		}

		return ids;
	}

	private long[] getNodeIdsArray(Hashtable nodes) {
		long[] ids = new long[nodes.size()];

		int counter = 0;
		if (nodes.size() > 0) {
			for (Iterator keys = nodes.keySet().iterator(); keys.hasNext();) {
				Hashtable h = (Hashtable) nodes.get(keys.next());
				ProcessItem item = (ProcessItem) h.values().iterator().next();
				ids[counter++] = item.nodeID;
			}
		} else {
			ids = new long[1];
			ids[0] = 0;
		}
		return ids;
	}

	private void logHashtableContent(String message, Hashtable hashtable) {

		log.debug("+ " + message + " +");
		for (Iterator keys = hashtable.keySet().iterator(); keys.hasNext();) {
			String nodeCode = (String) keys.next();
			Hashtable values = (Hashtable) hashtable.get(nodeCode);

			if (values != null) {
				for (Iterator vals = values.keySet().iterator(); vals.hasNext();) {
					Integer rowNumber = (Integer) vals.next();

					ProcessItem item = (ProcessItem) values.get(rowNumber);
					log.debug("Code: " + nodeCode + " , Row number: " + rowNumber + " , Value: " + item.value);
				}
			}
		}
	}

	private void logChilds(String message, Hashtable hashtable) {

		log.debug("+ " + message + " +");
		for (Iterator keys = hashtable.keySet().iterator(); keys.hasNext();) {
			Long parentId = (Long) keys.next();
			Vector values = (Vector) hashtable.get(parentId);

			if (values != null) {
				for (Iterator vals = values.iterator(); vals.hasNext();) {
					ProcessItem item = (ProcessItem) vals.next();
					log.debug("Parent ID: " + parentId + ", Code: " + item.code + " , Row number: " + item.rowNumber + ", Value: " + item.value);
				}
			}
		}
	}

	private void setData(Hashtable destTable, long guid, Hashtable sourceTable) {

		destTable.put(new Long(guid), sourceTable);
	}

	private Hashtable getData(Hashtable table, long guid) {

		return (Hashtable) table.get(new Long(guid));
	}

	private void evalScript(Connection con, ReturnPK returnPK, Hashtable items, Hashtable values, Hashtable updates, Hashtable dependents, Hashtable involvedReturns, Hashtable comparisons,
			Hashtable messages, int versionId) {

		evalScript(con, returnPK, items, values, updates, dependents, involvedReturns, comparisons, messages, -1, versionId);
	}

	private int getVersionId(Connection con, String versionCode) throws SQLException {

		PreparedStatement ps = null;
		ResultSet rs = null;
		int result;
		try {
			ps = con.prepareStatement("select v.id from IN_RETURN_VERSIONS v where v.code=?");
			ps.setString(1, versionCode);

			rs = ps.executeQuery();
			rs.next();
			result = rs.getInt(1);
		} finally {
			DatabaseUtil.closeStatement(ps);
		}
		return result;
	}

	private void evalScript(Connection con, ReturnPK returnPK, Hashtable items, Hashtable values, Hashtable updates, Hashtable dependents, Hashtable involvedReturns, Hashtable comparisons,
			Hashtable messages, int rowNumber, int versionId) {

		for (Iterator iter = items.keySet().iterator(); iter.hasNext();) {

			Integer rn = (Integer) iter.next();
			ProcessItem item = (ProcessItem) items.get(rn);

			if (item.tableType == ReturnConstants.TABLETYPE_VARIABLE && rowNumber != -1 && rn.intValue() != rowNumber) {
				continue;
			}

			JSTree tree = new JSTree(values, updates, dependents, con, returnPK, item, versionId);

			if (item.nodeType == MDTConstants.NODETYPE_VARIABLE) {

				if (involvedReturns.get(new Integer(item.returnID)) == null) {
					involvedReturns.put(new Integer(item.returnID), item);
				}

				try {
					log.debug("processing node: " + item.nodeID);
					log.info("Calculating ... " + item.equation);
					item.value = ScriptEngineService.getInstance().callFuction(tree, "function fina2_mdt_node() {\n" + item.equation + "\n}");
					log.info("###################RESULT###################" + item.value);
					Hashtable h = null;
					if (updates.get(item.code) == null) {
						h = new Hashtable();
						updates.put(item.code, h);
					} else {
						h = (Hashtable) updates.get(item.code);
					}
					h.put(new Integer(item.rowNumber), item);
				} catch (JavaScriptException ex) {
					log.debug(ex);
				}

				if (dependents.get(item.code) != null) {
					Hashtable d = (Hashtable) dependents.get(item.code);
					Iterator depIter = d.values().iterator();
					if (depIter.hasNext()) {
						Hashtable dItems = (Hashtable) depIter.next();
						Iterator dIter = dItems.values().iterator();
						if (dIter.hasNext()) {
							ProcessItem depItem = (ProcessItem) dIter.next();
							if ((item.tableType == ReturnConstants.TABLETYPE_VARIABLE) && (item.tableID == depItem.tableID) && (item.returnID == depItem.returnID)) {
								Hashtable depItems = new Hashtable();
								depItems.put(new Integer(item.rowNumber), dItems.get(new Integer(item.rowNumber)));
								evalScript(con, returnPK, depItems, values, updates, dependents, involvedReturns, comparisons, messages, versionId);
							} else {
								for (depIter = d.values().iterator(); depIter.hasNext();) {
									Hashtable depItems = (Hashtable) depIter.next();
									evalScript(con, returnPK, depItems, values, updates, dependents, involvedReturns, comparisons, messages, versionId);
								}
							}
						}
					}
				}
			}
		}
	}

	private void evalCompScript(Connection con, ReturnPK returnPK, Hashtable items, Hashtable values, Hashtable updates, Hashtable dependents, Hashtable involvedReturns,
			Hashtable<Long, ComparisonItem> comparisons, Hashtable messages, int versionId, String numberFormat) {

		DecimalFormat df = new DecimalFormat(numberFormat);

		for (Iterator iter = items.keySet().iterator(); iter.hasNext();) {
			ProcessItem item = (ProcessItem) items.get(iter.next());
			ComparisonItem comp = (ComparisonItem) comparisons.get(new Long(item.nodeID));
			if (comp != null) {
				double v1 = Double.NaN;
				double v2 = Double.NaN;
				String compValue = null;
				log.debug("comp found=" + comp.nodeID);
				try {
					JSTree tree = new JSTree(values, updates, dependents, con, returnPK, item, versionId);
					log.debug("eq: " + comp.equation);
					compValue = ScriptEngineService.getInstance().callFuction(tree, "function fina2_mdt_node() {\n" + comp.equation + "\n}");

					if ((compValue != null) && (compValue.trim().length() != 0) && (!compValue.equals("undefined"))) {
						v2 = Double.parseDouble(compValue);
						try {
							v2 = df.parse(df.format(v2)).doubleValue();
						} catch (Exception ex) {
							log.error(ex.getMessage(), ex);
						}
					} else {
						v2 = Double.NaN;
					}
					if (item.value == null || item.value.trim().length() == 0) {
						v1 = Double.NaN;
					} else {
						v1 = Double.parseDouble(item.value);
						try {
							v1 = df.parse(df.format(v1)).doubleValue();
						} catch (Exception ex) {
							log.error(ex.getMessage(), ex);
						}
					}

					log.info("v1=" + v1);
					log.info("v2=" + v2);
					switch (comp.condition) {
					case MDTConstants.COMP_EQUALS:

						if (!new Double(v1).equals(new Double(v2))) {
							String message = "Value does not match comparison rule.\n" + "    Node Info [" + item.code + "]\n    " + v1 + " MUST EQUALS " + v2 + "\n" + "    Value = " + item.value
									+ "\n" + "    Required value = " + compValue + "\n    Equation:" + comp.equation + "\n";
							messages.put(item.code, message);
							throw new ProcessException(message, item, ProcessException.Reason.ComparisonRule);
						}

						break;
					case MDTConstants.COMP_NOT_EQUALS:

						if (new Double(v1).equals(new Double(v2))) {
							String message = "Value does not match comparison rule.\n" + "    Node code [" + item.code + "]\n    " + v1 + " MUST NOT EQUALS " + v2 + "\n" + "    Value = " + item.value
									+ "\n" + "    Required value != " + compValue + "\n    Equation:" + comp.equation + "\n";
							messages.put(item.code, message);
							throw new ProcessException(message, item, ProcessException.Reason.ComparisonRule);
						}

						break;
					case MDTConstants.COMP_GREATER:

						if (v1 <= v2) {
							String message = "Value does not match comparison rule.\n" + "    Node code [" + item.code + "]\n" + v1 + " MUST BE GREATER THAN " + v2 + "    Value = " + item.value
									+ "\n" + "    Required value > " + compValue + "\n    Equation:" + comp.equation + "\n";
							messages.put(item.code, message);
							throw new ProcessException(message, item, ProcessException.Reason.ComparisonRule);
						}
						break;
					case MDTConstants.COMP_GREATER_EQUALS:
						if (v1 < v2) {
							String message = "Value does not match comparison rule.\n" + "    Node code [" + item.code + "]\n" + v1 + " GREATER OR EQUALS " + v2 + "    Value = " + item.value + "\n"
									+ "    Required value >= " + compValue + "\n   Equation:" + comp.equation + "\n";
							messages.put(item.code, message);
							throw new ProcessException(message, item, ProcessException.Reason.ComparisonRule);
						}
						break;
					case MDTConstants.COMP_LESS:
						if (v1 >= v2) {
							String message = "Value does not match comparison rule.\n" + "    Node code [" + item.code + "]\n" + v1 + " LESS THAN " + v2 + "    Value = " + item.value + "\n"
									+ "    Required value < " + compValue + "\n   Equation:" + comp.equation + "\n";
							messages.put(item.code, message);
							throw new ProcessException(message, item, ProcessException.Reason.ComparisonRule);
						}
						break;
					case MDTConstants.COMP_LESS_EQUALS:
						if (v1 > v2) {
							String message = "Value does not match comparison rule.\n" + "    Node code [" + item.code + "]\n" + v1 + " LESS OR EQUALS " + v2 + "    Value = " + item.value + "\n"
									+ "    Required value <= " + compValue + "\n   Equation:" + comp.equation + "\n";
							messages.put(item.code, message);
							throw new ProcessException(message, item, ProcessException.Reason.ComparisonRule);
						}
						break;
					}
				} catch (NumberFormatException nex) {
					log.error(nex.getMessage(), nex);
					Object val = (item.value != null && item.value.trim().length() > 0) ? item.value : Double.NaN;
					String message = "Error during parsing/formatting  following items \n 1. Item Value for code '" + item.code + "' : " + val + " \n 2. Comarison value: " + compValue
							+ " \n please check: \n 1.Comparison equation  on node [" + item.code + "] is : '" + comp.equation + "' \n 2.Item value which is " + val;
					messages.put(item.code, message);
					throw new ProcessException(message, nex, item, ProcessException.Reason.NumberFormat);
				} catch (JavaScriptException jex) {
					log.info("nodeID = " + item.nodeID);
					log.debug("javascriptexception " + jex);
					log.error("nodeID = " + item.nodeID);

				} catch (ProcessException ex) {
					log.info("nodeID = " + item.nodeID);
					log.error("otherexception " + ex);
					log.info("nodeID = " + item.nodeID);
					log.info("Item value = " + item.value);
					log.info("Item code = " + item.code);
					throw ex;
				} catch (Exception oex) {
					log.info("nodeID = " + item.nodeID);
					log.error("otherexception " + oex);
					log.info("nodeID = " + item.nodeID);
					log.info("Item value = " + item.value);
					log.info("Item code = " + item.code);
					throw new EJBException(oex);
				}
			}
		}
	}

	private Hashtable getDependents(Connection con, Hashtable<String, Hashtable<Integer, ProcessItem>> nodes, int periodID, int bankID, int returnID, boolean reprocess, int versionId)
			throws SQLException {

		String returnIds = "";
		if (reprocess) {
			returnIds = getReturnIds(con, periodID, bankID);
		} else {
			returnIds += returnID;
		}
		PreparedStatement ps = null;
		ResultSet rs = null;
		String depIds = getDependentIds(con, nodes);

		String arr[] = depIds.split(",");
		HashMap<Integer, String> map = CommonUtils.getData(depIds);
		Hashtable hh = null;
		if (arr.length < 500) {
			ps = con.prepareStatement("select a.returnID,b.id,b.type,a.rowNumber,a.value,c.type,a.tableID,b.equation,b.code,c.evalType "
					+ "from IN_RETURN_ITEMS a, IN_MDT_NODES b, IN_DEFINITION_TABLES c " + "where a.nodeID in (" + depIds + ") " + "and c.id=a.tableID " + "and c.definitionID in "
					+ "(select e.definitionID from IN_SCHEDULES e where e.id in " + " (select f.scheduleID from IN_RETURNS f where f.id=a.returnID) " + ") " + "and b.id = a.nodeID "
					+ "and a.returnID in (" + returnIds + ") " + "and a.versionID=" + versionId);

			rs = ps.executeQuery();
			while (rs.next()) {
				ProcessItem item = new ProcessItem();
				item.returnID = rs.getInt(1);
				item.nodeID = rs.getLong(2);
				item.nodeType = rs.getInt(3);
				item.rowNumber = rs.getInt(4);
				item.value = rs.getString(5);
				item.tableType = rs.getInt(6);
				item.tableID = rs.getInt(7);
				item.equation = rs.getString(8);
				item.code = rs.getString(9).trim();
				item.tableEvalType = rs.getInt(10);

				if (nodes.get(item.code) == null) {
					hh = new Hashtable();
					nodes.put(item.code, hh);
				} else {
					hh = (Hashtable) nodes.get(item.code);
				}
				hh.put(new Integer(item.rowNumber), item);
			}
		} else {
			for (int i = 0; i < map.size(); i++) {
				ps = con.prepareStatement("select a.returnID,b.id,b.type,a.rowNumber,a.value,c.type,a.tableID,b.equation,b.code,c.evalType "
						+ "from IN_RETURN_ITEMS a, IN_MDT_NODES b, IN_DEFINITION_TABLES c " + "where a.nodeID in (" + map.get(i) + ") " + "and c.id=a.tableID " + "and c.definitionID in "
						+ "(select e.definitionID from IN_SCHEDULES e where e.id in " + " (select f.scheduleID from IN_RETURNS f where f.id=a.returnID) " + ") " + "and b.id = a.nodeID "
						+ "and a.returnID in (" + returnIds + ") " + "and a.versionID=" + versionId);

				rs = ps.executeQuery();
				while (rs.next()) {
					ProcessItem item = new ProcessItem();
					item.returnID = rs.getInt(1);
					item.nodeID = rs.getLong(2);
					item.nodeType = rs.getInt(3);
					item.rowNumber = rs.getInt(4);
					item.value = rs.getString(5);
					item.tableType = rs.getInt(6);
					item.tableID = rs.getInt(7);
					item.equation = rs.getString(8);
					item.code = rs.getString(9).trim();
					item.tableEvalType = rs.getInt(10);

					if (nodes.get(item.code) == null) {
						hh = new Hashtable();
						nodes.put(item.code, hh);
					} else {
						hh = (Hashtable) nodes.get(item.code);
					}
					hh.put(new Integer(item.rowNumber), item);
				}
			}
		}

		DatabaseUtil.closeResultSet(rs);
		DatabaseUtil.closeStatement(ps);

		return nodes;
	}

	public Map<String, String> canProcess(Handle langHandle, SchedulePK schedulePK, String versionCode) throws RemoteException, EJBException {
		Map<String, String> codeNames = new ConcurrentHashMap<String, String>();
		int langID = ((fina2.i18n.LanguagePK) langHandle.getEJBObject().getPrimaryKey()).getId();
		Language lang = (Language) langHandle.getEJBObject();
		String encoding = lang.getXmlEncoding();
		Connection con = DatabaseUtil.getConnection();
		try {
			PreparedStatement stmt = con.prepareStatement("select a.nodeID from IN_DEFINITION_TABLES a, IN_SCHEDULES b " + "where a.definitionID=b.definitionID and b.id=? ");
			stmt.setInt(1, schedulePK.getId());
			ResultSet tabRs = stmt.executeQuery();

			Vector hNodes = new Vector();
			while (tabRs.next()) {
				long id = tabRs.getLong(1);
				selectChildNodes(con, hNodes, id);
			}

			String nodes = "";
			for (Iterator iter = hNodes.iterator(); iter.hasNext();) {
				if (!nodes.equals(""))
					nodes += ", ";
				nodes += iter.next();
			}

			if (hNodes.size() == 0)
				nodes = "0";

			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeResultSet(tabRs);

			if (!nodes.equals("")) {

				if (DatabaseUtil.isMssql()) {
					PreparedStatement ps = con.prepareStatement("select b.code, a.value " + "from IN_MDT_NODES b, SYS_STRINGS a " + "where " + "a.id = b.nameStrID and (a.langID=?) "
							+ "and b.required = 1 " + "and b.id not in (" + nodes + ") " + "and b.id in (select a.nodeID from IN_MDT_DEPENDENT_NODES a " + "              where a.dependentNodeID in ("
							+ nodes + ")) " + "and b.id not in (" + "        select r.nodeid from result_view r, in_schedules s, in_periods p "
							+ "         where r.todate=p.todate and r.bankid=s.bankid and p.id=s.periodid " + "              and r.versionCode=? and s.id=? " + ") order by a.langID DESC");

					ps.setInt(1, langID);
					ps.setString(2, versionCode);
					ps.setInt(3, schedulePK.getId());

					ResultSet rs = ps.executeQuery();
					String lastCode = "";
					while (rs.next()) {
						String code = rs.getString(1).trim();

						if (!code.equals(lastCode)) {
							codeNames.put(code, LocaleUtil.encode(rs.getString(2).trim(), encoding));
						}
					}

					DatabaseUtil.closeResultSet(rs);
					DatabaseUtil.closeStatement(ps);
				} else if (DatabaseUtil.isOracle()) {
					HashMap<Integer, String> map = CommonUtils.getData(nodes);
					String arr[] = nodes.split(",");
					if (arr.length < 500) {
						PreparedStatement ps = con.prepareStatement("select b.code, a.value " + "from IN_MDT_NODES b, SYS_STRINGS a " + "where " + "a.id = b.nameStrID and (a.langID=?) "
								+ "and b.required = 1 " + "and b.id not in (" + nodes + ") " + "and b.id in (select a.nodeID from IN_MDT_DEPENDENT_NODES a "
								+ "              where a.dependentNodeID in (" + nodes + ")) " + "and b.id not in (" + "        select r.nodeid from result_view r, in_schedules s, in_periods p "
								+ "         where r.todate=p.todate and r.bankid=s.bankid and p.id=s.periodid " + "              and r.versionCode=? and s.id=? " + ") ");

						ps.setInt(1, langID);
						ps.setString(2, versionCode);
						ps.setInt(3, schedulePK.getId());

						ResultSet rs = ps.executeQuery();
						String lastCode = "";
						while (rs.next()) {
							String code = rs.getString(1).trim();

							if (!code.equals(lastCode)) {
								codeNames.put(code, LocaleUtil.encode(rs.getString(2).trim(), encoding));
							}
						}

						DatabaseUtil.closeResultSet(rs);
						DatabaseUtil.closeStatement(ps);
					} else {
						for (int i = 0; i < map.size(); i++) {
							PreparedStatement ps = con.prepareStatement("select b.code, a.value " + "from IN_MDT_NODES b, SYS_STRINGS a " + "where " + "a.id = b.nameStrID and (a.langID=?) "
									+ "and b.required = 1 " + "and b.id not in (" + map.get(i) + ") " + "and b.id in (select a.nodeID from IN_MDT_DEPENDENT_NODES a "
									+ "              where a.dependentNodeID in (" + map.get(i) + ")) " + "and b.id not in ("
									+ "        select r.nodeid from result_view r, in_schedules s, in_periods p " + "         where r.todate=p.todate and r.bankid=s.bankid and p.id=s.periodid "
									+ "              and r.versionCode=? and s.id=? " + ") ");

							ps.setInt(1, langID);
							ps.setString(2, versionCode);
							ps.setInt(3, schedulePK.getId());

							ResultSet rs = ps.executeQuery();
							String lastCode = "";
							while (rs.next()) {
								String code = rs.getString(1).trim();

								if (!code.equals(lastCode)) {
									codeNames.put(code, LocaleUtil.encode(rs.getString(2).trim(), encoding));
								}
							}

							DatabaseUtil.closeResultSet(rs);
							DatabaseUtil.closeStatement(ps);
						}
					}
				}

			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}

		// Vector v = new Vector(codes);
		// v.addAll(names);
		// log.info("$$$$$$$$$$$$$$$$$$$$$$$$$$4DEP NODES " + codeNames);
		return codeNames;
	}

	private void selectChildNodes(Connection con, Vector nodes, long parentID) throws SQLException {

		PreparedStatement ps = con.prepareStatement("select id,type from IN_MDT_NODES where parentID=? ");
		ps.setLong(1, parentID);

		ResultSet rs = ps.executeQuery();

		while (rs.next()) {
			long id = rs.getLong(1);
			int type = rs.getInt(2);

			nodes.add(String.valueOf(id));

			if (type == MDTConstants.NODETYPE_NODE) {
				selectChildNodes(con, nodes, id);
			}
		}
		DatabaseUtil.closeResultSet(rs);
		DatabaseUtil.closeStatement(ps);
	}

	public static String importSync = "sync";

	public String importReturn(Handle userHandle, Handle languageHandle, byte[] xml, boolean forceReprocess, String versionCode) throws RemoteException, EJBException {
		synchronized (importSync) {
			Connection con = null;
			try {
				con = DatabaseUtil.getConnection();
				Import imp = new Import(con, xml);

				return imp.importReturn(ctx, userHandle, languageHandle, forceReprocess, versionCode);

			} catch (Exception e) {
				throw new EJBException(e);
			} finally {
				DatabaseUtil.closeConnection(con);
			}
		}
	}
}
