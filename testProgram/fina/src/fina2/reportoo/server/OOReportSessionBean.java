package fina2.reportoo.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.log4j.Logger;

import fina2.bank.BankCriterionConstants;
import fina2.db.DatabaseUtil;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;
import fina2.metadata.MDTConstants;
import fina2.period.PeriodPK;
import fina2.reportoo.FinaFunction;
import fina2.reportoo.ReportConstants;
import fina2.reportoo.ReportInfo;
import fina2.returns.ProcessItem;
import fina2.returns.ReturnConstants;
import fina2.security.ServerSecurityUtil;
import fina2.security.User;
import fina2.security.UserPK;
import fina2.ui.sheet.openoffice.OOIterator;
import fina2.ui.table.TableRowImpl;
import fina2.ui.tree.Node;
import fina2.util.CommonUtils;

public class OOReportSessionBean implements SessionBean, Serializable {
	private static Logger log = Logger.getLogger(OOReportSessionBean.class);

	private static Hashtable allBanksInfo = new Hashtable();
	private static Hashtable allPeersInfo = new Hashtable();
	private static Hashtable allPeriods = new Hashtable();
	private static Hashtable allOffsetsPK = new Hashtable();
	private static Hashtable allOffsetsSeq = new Hashtable();
	private static Hashtable defCriterions = new Hashtable();
	private static Hashtable peersBanks = new Hashtable();
	private static Hashtable criterionBanks = new Hashtable();
	private static Hashtable stageInfos = new Hashtable();
	private static Hashtable connections = new Hashtable();
	private static Hashtable prepStatements = new Hashtable();
	private static Hashtable allNodesInfo = new Hashtable();
	private static Hashtable periodTypePriority = new Hashtable();
	private static Hashtable reporInfos = new Hashtable();
	private static Hashtable bankValueForPct = new Hashtable();
	private static Hashtable allPeerBanks = new Hashtable();

	private SessionContext ctx;
	private GregorianCalendar calendar = new GregorianCalendar();

	public void ejbCreate() throws CreateException, EJBException,
			RemoteException {
	}

	public void ejbActivate() throws javax.ejb.EJBException,
			java.rmi.RemoteException {
	}

	public void ejbPassivate() throws javax.ejb.EJBException,
			java.rmi.RemoteException {
	}

	public void ejbRemove() throws javax.ejb.EJBException,
			java.rmi.RemoteException {
	}

	public void setSessionContext(javax.ejb.SessionContext ctx)
			throws javax.ejb.EJBException, java.rmi.RemoteException {
		this.ctx = ctx;
	}

	private TreeSet<Long> loadIds(int userId) {
		TreeSet<Long> ids = new TreeSet<Long>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String userRepSql = "select c.reportID as repId from SYS_USER_REPORTS c where c.userID="
					+ userId;
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement(userRepSql);
			rs = ps.executeQuery();
			while (rs.next()) {
				ids.add(rs.getLong("repId"));
			}
			/*
			 * DatabaseUtil.close(rs, ps, con);
			 * 
			 * con = DatabaseUtil.getConnection(); ps =
			 * con.prepareStatement(userRepSql); rs = ps.executeQuery();
			 */
			String roleRepSql = "SELECT c.report_Id AS repId FROM SYS_ROLE_REPORTS c WHERE c.ROLE_ID IN(SELECT u.roleId FROM SYS_USERS_ROLES u WHERE u.userId="
					+ userId + ")";
			ps = con.prepareStatement(roleRepSql);
			rs = ps.executeQuery();
			while (rs.next()) {
				ids.add(rs.getLong("repId"));
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return ids;
	}

	public LinkedHashMap<ReportPK, ReportInfo> getInfos(Handle userHandle,
			ReportPK pk) throws RemoteException, EJBException {
		LinkedHashMap<ReportPK, ReportInfo> h = new LinkedHashMap<ReportPK, ReportInfo>();

		fina2.security.User user = (fina2.security.User) userHandle
				.getEJBObject();
		// UserPK userPK = (UserPK)userHandle.getEJBObject().getPrimaryKey();
		int userId = ((fina2.security.UserPK) user.getPrimaryKey()).getId();
		String sql = "";
		if (!user.getLogin().equals("sa")) {
			// sql =
			// "and a.id in (select c.reportID from SYS_USER_REPORTS c where c.userID="
			// + userId + ") ";
			sql = "and a.id IN"
					+ loadIds(userId).toString().replace('[', '(')
							.replace(']', ')');
		}
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select a.id, a.info from OUT_REPORTS a where (a.id=? or a.parentID=?) and a.type=? order by a.sequence,a.id ASC");
			ps.setInt(1, pk.getId());
			ps.setInt(2, pk.getId());
			ps.setInt(3, ReportConstants.NODETYPE_REPORT);
			rs = ps.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				byte[] buf = getBlob(rs, 2);
				ReportInfo info = null;
				try {
					ByteArrayInputStream bi = new ByteArrayInputStream(buf);
					ObjectInputStream oi = new ObjectInputStream(bi);
					info = (ReportInfo) oi.readObject();
					oi.close();
					bi.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}

				if (info != null)
					h.put(new ReportPK(id), info);
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return h;
	}

	public Collection getRootFolders(Handle langHandle) throws RemoteException,
			EJBException {

		Vector v = new Vector();
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select a.id, b.value "
					+ "from OUT_REPORTS a, SYS_STRINGS b "
					+ "where a.type=? and b.id=a.nameStrID and (b.langID=? or b.langID=1) and a.parentID=0 "
					+ "order by a.id, b.langID DESC, b.value");

			ps.setInt(1, ReportConstants.NODETYPE_FOLDER);
			ps.setInt(2, langPK.getId());
			rs = ps.executeQuery();

			int prevId = -1;

			while (rs.next()) {
				int id = rs.getInt(1);
				String name = rs.getString(2);

				if (prevId == id) {
					continue;
				} else {
					prevId = id;
				}

				if (name == null)
					name = "";
				TableRowImpl row = new TableRowImpl(new ReportPK(id), 1);
				row.setValue(0, name.trim());
				v.add(row);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;
	}

	public Hashtable getNames(Handle userHandle, Handle langHandle, ReportPK pk)
			throws RemoteException, EJBException {
		Hashtable h = new Hashtable();

		fina2.security.User user = (fina2.security.User) userHandle
				.getEJBObject();
		int userId = ((fina2.security.UserPK) user.getPrimaryKey()).getId();
		// UserPK userPK = (UserPK)userHandle.getEJBObject().getPrimaryKey();
		String sql = "";
		String ids = "";
		if (!user.getLogin().equals("sa")) {
			// sql =
			// "and a.id in (select c.reportID from SYS_USER_REPORTS c where c.userID="
			// + ((fina2.security.UserPK) user.getPrimaryKey()).getId() + ") ";
			// sql = "and a.id IN" + loadIds(userId).toString().replace('[',
			// '(').replace(']', ')');

			TreeSet<Long> idsSet = loadIds(userId);
			if (idsSet.size() == 0) {
				idsSet.add(0L);
			}
			ids = idsSet.toString().replace("[", "").replace("]", "");

		}
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if (!ids.equals("")) {
				String queryString = "select a.id, b.id from OUT_REPORTS a, SYS_STRINGS b where (a.id=? or a.parentID=?) and a.type=? and b.id=a.nameStrID and b.langID=? and a.id IN ( ";

				HashMap<Integer, String> map = CommonUtils.getData(ids);

				for (int i = 0; i < map.size(); i++) {
					log.info("======================================================: "
							+ i);
					log.info(map.get(i));
					log.info("======================================================: "
							+ i);
					ps = con.prepareStatement(queryString + map.get(i) + ")");

					ps.setInt(1, pk.getId());
					ps.setInt(2, pk.getId());
					ps.setInt(3, ReportConstants.NODETYPE_REPORT);
					ps.setInt(4, langPK.getId());
					rs = ps.executeQuery();

					while (rs.next()) {
						int id = rs.getInt(1);
						String name = LocaleUtil.getString(con, langPK,
								rs.getInt(2));
						if (name == null)
							name = "";
						h.put(new ReportPK(id), name.trim());
					}

					/**
					 * Close
					 */
					DatabaseUtil.closeStatement(ps);
					DatabaseUtil.closeResultSet(rs);

				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return h;
	}

	/** Returns the tree of reports for given user */
	public Node getTreeNodes(Handle userHandle, Handle languageHandle)
			throws RemoteException, fina2.FinaTypeException {
		ServerSecurityUtil.checkUserPermissions(userHandle,
				"fina2.report.amend", "fina2.report.generate");

		// ---------------------------------------------------------------------
		// The result root node
		Node root = new Node(new ReportPK(0), "        ", new Integer(-1));

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			// -----------------------------------------------------------------
			// Selecting only user reports id list

			User user = (User) userHandle.getEJBObject();
			int userId = ((UserPK) user.getPrimaryKey()).getId();

			String sql = "select a.reportid from sys_user_reports a where a.userid = ? "
					+ "union "
					+ "select report_id from sys_role_reports a "
					+ "where a.role_id in ( "
					+ "    select a.roleid from sys_users_roles a  where a.userid = ? "
					+ ") ";

			ps = con.prepareStatement(sql);
			ps.setInt(1, userId);
			ps.setInt(2, userId);

			rs = ps.executeQuery();
			String userReports = "";
			while (rs.next()) {
				if (!userReports.equals("")) {
					userReports = userReports + ", ";
				}
				userReports = userReports + rs.getInt(1);
			}
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			// -----------------------------------------------------------------
			// Selecting reports

			String clause = "";

			if (!userReports.equals("")) {

				// There are some user reports. Select only them.
				// clause = "(a.id in (" + userReports +
				// ") or a.parentid = 0) ";
				clause = "(a.id=" + userReports.replace(",", " or a.id=")
						+ " or a.parentid = 0) ";

			} else {
				// Select only folders
				clause = "a.parentid = 0 ";
			}

			sql = "select a.parentid, a.id, a.namestrid, a.type "
					+ "from out_reports a " + "where " + clause
					+ "order by a.parentId, a.sequence";

			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			HashMap<ReportPK, Node> nodes = new HashMap<ReportPK, Node>();

			while (rs.next()) {

				// Report id
				ReportPK parentPk = new ReportPK(rs.getInt(1));
				ReportPK reportPk = new ReportPK(rs.getInt(2));

				// Report name
				int strId = rs.getInt(3);
				String reportName = LocaleUtil.getString(con, languageHandle,
						strId);

				// Node type (1 - folder, 2 - report)
				Integer nodeType = new Integer(rs.getInt(4));

				// Node for the tree
				if (reportName == null || reportName.trim().equals("")) {
					reportName = "NONAME";
				}
				Node node = new Node(reportPk, reportName, nodeType);

				if (parentPk.getId() == 0) {
					// Top level folder. Adding to the root.
					root.addChild(node);
				} else {
					// Report or second-level folder. Adding to the top level
					// folder.
					Node parentNode = nodes.get(parentPk);
					if (parentNode != null) {
						parentNode.addChild(node);
					}
				}

				if (nodes.get(reportPk) == null) {
					// Adding to the nodes map - required looking up as parent
					nodes.put(reportPk, node);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		// The result root node
		return root;
	}

	public FinaFunction[] calculate(String reportId, FinaFunction[] invFuncs)
			throws EJBException {

		initStage(reportId, "Sorting Functions");
		Arrays.sort(invFuncs, FinaFunctionComparator.comparator);

		calculatePctValues(reportId, invFuncs);

		try {
			for (int i = 0; i < invFuncs.length; i++) {
				calcCleanup(invFuncs, i, reportId);
				calculateFunction(reportId, invFuncs[i]);
			}

			logStatistics(reportId, "Functions calculating");
		} finally {
			clearPreparedStatements(reportId);
			closeConnection(reportId);
		}
		return invFuncs;
	}

	private void calculatePctValues(String reportId, FinaFunction[] invFuncs)
			throws EJBException {

		initStage(reportId, "Caching data for PCT Function");
		try {
			List<PctParameters> genPctFuncs = new ArrayList<PctParameters>();
			for (int i = 0; i < invFuncs.length; i++) {
				if (invFuncs[i].getFunctionName().equals("pctvalex")) {
					Object[] pctf = (Object[]) invFuncs[i].getArguments()[4];
					for (int j = 0; j < pctf.length; j++) {
						PctParameters pctPrms = parsePctParameters((String) pctf[j]);
						pctPrms.bankCode = (String) invFuncs[i].getArguments()[1];
						pctPrms.peerCode = (String) invFuncs[i].getArguments()[2];
						pctPrms.versionCode = (String) invFuncs[i]
								.getArguments()[3];

						genPctFuncs.add(pctPrms);
					}
				} else if (invFuncs[i].getFunctionName().equals("pctvalue")
						|| invFuncs[i].getFunctionName().equals("pctvaluever")) {

					PctParameters pctPrms = new PctParameters();
					pctPrms.nodeCode = (String) invFuncs[i].getArguments()[0];
					pctPrms.bankCode = (String) invFuncs[i].getArguments()[1];
					pctPrms.peerCode = (String) invFuncs[i].getArguments()[2];
					pctPrms.period = ((Integer) (invFuncs[i].getArguments()[3]))
							.intValue();
					pctPrms.periodFunction = (String) invFuncs[i]
							.getArguments()[4];
					pctPrms.periodOffset = ((Integer) (invFuncs[i]
							.getArguments()[5])).intValue();
					pctPrms.versionCode = ReportConstants.LATEST_VERSION;
					if (invFuncs[i].getFunctionName().equals("pctvaluever")) {
						pctPrms.versionCode = (String) (invFuncs[i]
								.getArguments()[6]);
					}
					genPctFuncs.add(pctPrms);
				}
			}

			cacheBankValuesForPct(reportId, genPctFuncs);

		} catch (Exception ex) {
			String err = "Error during caching bank values for PCT function.";
			log.error(err, ex);
			throw new EJBException(err, ex);
		}
	}

	private void calcCleanup(FinaFunction[] invFuncs, int i, String reportId)
			throws EJBException {

		if (i > 0
				&& FinaFunctionComparator.comparator.compare(invFuncs[i],
						invFuncs[i - 1]) != 0) {

			clearPreparedStatements(reportId);
		}
	}

	private void clearPreparedStatements(String reportId) throws EJBException {

		initStage(reportId, "Closing unnecessary prepared statements");
		Hashtable statements = getHash(prepStatements, reportId);

		try {
			Iterator iter = statements.values().iterator();
			while (iter.hasNext()) {
				PreparedStatement ps = (PreparedStatement) iter.next();
				ps.close();
			}
		} catch (SQLException ex) {
			String err = "Error during closing prepared statements.";
			log.error(err, ex);
			throw new EJBException(err, ex);
		}

		statements.clear();
	}

	private void calculateFunction(String reportId, FinaFunction function)
			throws EJBException {

		try {

			Object[] args = getArguments(reportId, function.getArguments());
			Method method = getClass().getMethod(function.getFunctionName(),
					getArgumentTypes(args));
			function.setResult(method.invoke(this, args));

			initStage(reportId,
					function.toString() + " result: " + function.getResult());

		} catch (Exception ex) {

			StringBuffer buff = new StringBuffer();

			buff.append("Error during calculating function. ");
			buff.append(function.toString());

			log.error(buff.toString(), ex);

			throw new EJBException(buff.toString(), ex);
		}
	}

	private Object[] getArguments(String reportId, Object[] source) {

		Object[] args = new Object[source.length + 1];

		args[0] = reportId;
		System.arraycopy(source, 0, args, 1, source.length);

		return args;
	}

	private Class[] getArgumentTypes(Object args[]) {

		Class[] argTypes = new Class[args.length];

		for (int i = 0; i < argTypes.length; i++) {

			if (args[i].getClass().isArray()) {
				argTypes[i] = args[i].getClass();
			} else if (args[i] instanceof Integer) {
				argTypes[i] = int.class;
			} else if (args[i] instanceof Double) {
				argTypes[i] = double.class;
			} else if (args[i] instanceof String) {
				argTypes[i] = String.class;
			} else {
				argTypes[i] = String.class;
			}
		}
		return argTypes;
	}

	private void closeConnection(String reportId) {
		Connection connection = (Connection) connections.get(reportId);
		if (connection != null) {
			try {
				connection.close();
			} catch (Exception ex) {
			}
			connections.remove(reportId);
		}
	}

	public List calculateVCTValues(String reportId, Collection VCTIters)
			throws EJBException {

		ArrayList values = new ArrayList();
		VCTManager vm = VCTManager.getInstance(reportId);

		Connection con = DatabaseUtil.getConnection();
		try {
			for (Iterator iter = VCTIters.iterator(); iter.hasNext();) {
				OOIterator ooIter = (OOIterator) iter.next();

				initStage(reportId, "Processing VCT iterator. Iterator name: "
						+ ooIter.getName());
				List iterValues = vm.processVCTIterator(ooIter, con);
				values.add(iterValues);
			}
			logStatistics(reportId, "VCT values calculating");
		} catch (Exception ex) {
			log.error("Error during storing report", ex);
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.closeConnection(con);
		}

		return values;
	}

	public void preCalculation(String reporId, LanguagePK langPK,
			ReportInfo reportInfo) throws EJBException {

		Connection con = con = DatabaseUtil.getConnection();
		try {
			reporInfos.put(reporId, reportInfo);
			selectBanksAndBankGroupsData(reporId, langPK, con);
			selectPeriodsData(reporId, langPK, con);
			selectNodesData(reporId, langPK, con);
			selectPeriodTypePriority(reporId, con);
			logStatistics(reporId, "Pre-calculating");

		} catch (Exception ex) {
			log.error("Error during precalculation", ex);
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	private void selectPeriodsData(String reporId, LanguagePK langPK,
			Connection con) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			initStage(reporId, "Selecting periods");

			ps = con.prepareStatement("select c.id,a.code,b.value,c.fromDate,c.toDate,c.periodNumber from IN_PERIOD_TYPES a, SYS_STRINGS b, IN_PERIODS c "
					+ "where a.nameStrID=b.id and (b.langID=?) and a.id=c.periodTypeID "
					+ "order by c.periodTypeID, c.fromDate");
			ps.setInt(1, langPK.getId());
			rs = ps.executeQuery();
			int pseq = 1;
			String oldCode = "";
			while (rs.next()) {
				PeriodPK _pk = new PeriodPK(rs.getInt(1));
				String code = rs.getString(2).trim();
				String name = rs.getString(3);
				name = (name == null) ? "NONAME" : name;
				Date fromDate = new Date(rs.getDate(4).getTime());
				Date toDate = new Date(rs.getDate(5).getTime());
				int num = rs.getInt(6);
				Hashtable h = getHash(allPeriods, reporId);
				OOPeriodPK o = (OOPeriodPK) h.get(_pk);
				if (o == null) {
					o = new OOPeriodPK(_pk.getId());
					h.put(_pk, o);
				}
				o.setTypeCode(code);
				o.setTypeName(name);
				o.setFromDate(fromDate);
				o.setToDate(toDate);
				o.setNumber(num);

				if (!oldCode.equals(code)) {
					pseq = 1;
					oldCode = code;
				}
				h = getHash(allOffsetsPK, reporId);
				Hashtable hh = (Hashtable) h.get(code);
				if (hh == null) {
					hh = new Hashtable();
					h.put(code, hh);
				}
				hh.put(_pk, new Integer(pseq));

				h = getHash(allOffsetsSeq, reporId);
				hh = (Hashtable) h.get(code);
				if (hh == null) {
					hh = new Hashtable();
					h.put(code, hh);
				}
				hh.put(new Integer(pseq), _pk);
				pseq++;
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
		}
	}

	private void selectBanksAndBankGroupsData(String reportId,
			LanguagePK langPK, Connection con) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			initStage(reportId, "Selecting banks and bank groups");

			ps = con.prepareStatement("select a.code,b.value,c.code,a.id,b.id from IN_BANKS a, SYS_STRINGS b, IN_BANK_GROUPS c "
					+ "where a.nameStrID=b.id and (b.langID=?) order by a.id");
			ps.setInt(1, langPK.getId());
			rs = ps.executeQuery();
			while (rs.next()) {
				String code = rs.getString(1).trim();
				String name = LocaleUtil.getString(con, langPK, rs.getInt(5));// rs.getString(2);

				long bankId = rs.getLong(4);

				Hashtable h = getHash(allBanksInfo, reportId);
				OOBank o = (OOBank) h.get(code);
				if (o == null) {
					o = new OOBank(code);
					h.put(code, o);
				}
				o.setName(name);
				o.setId(bankId);
			}
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			Statement stmt = con.createStatement();
			rs = stmt
					.executeQuery("select g.code, b.code, c.code, c.isDefault from mm_bank_group m, in_banks b, in_bank_groups g, in_criterion c "
							+ "where m.bankid=b.id and m.bankgroupid=g.id and g.criterionid=c.id order by b.code");
			while (rs.next()) {
				String peerCode = rs.getString(1).trim();
				String bankCode = rs.getString(2).trim();
				String criterionCode = rs.getString(3).trim();
				boolean isDefault = rs.getInt(4) == BankCriterionConstants.DEF_CRITERION;

				getHash(criterionBanks, reportId).put(
						getBankCriterionKey(bankCode, criterionCode), peerCode);
				List banks = (List) getHash(peersBanks, reportId).get(peerCode);
				if (banks == null) {
					banks = new LinkedList();
				}
				banks.add(bankCode);
				getHash(peersBanks, reportId).put(peerCode, banks);

				if (isDefault) {
					defCriterions.put(reportId, criterionCode);
				}
			}
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("select a.code,b.value,a.id,b.id from IN_BANK_GROUPS a, SYS_STRINGS b "
					+ "where a.nameStrID=b.id and (b.langID=?)");
			ps.setInt(1, langPK.getId());
			rs = ps.executeQuery();
			while (rs.next()) {
				String code = rs.getString(1).trim();
				String name = LocaleUtil.getString(con, langPK, rs.getInt(4));// rs.getString(2);
				long id = rs.getLong(3);
				Hashtable h = getHash(allPeersInfo, reportId);
				OOPeer o = (OOPeer) h.get(code);
				if (o == null) {
					o = new OOPeer(code);
					h.put(code, o);
				}
				o.setName(name);
				o.setId(id);
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
		}
	}

	private void selectNodesData(String reporId, LanguagePK langPK,
			Connection con) {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			initStage(reporId, "Selecting nodes");
			stmt = con.createStatement();

			String query;
			if (DatabaseUtil.isOracle()) {// Oracle8 DB doesn't support ANSI SQL
											// 99
				query = "select a.code, b.value, a.id from IN_MDT_NODES a LEFT "
						+ "OUTER JOIN SYS_STRINGS b on a.nameStrID=b.id and (b.langID="
						+ langPK.getId() + ")";
			} else {
				query = "select a.code, b.value, a.id from IN_MDT_NODES a LEFT "
						+ "OUTER JOIN SYS_STRINGS b on a.nameStrID=b.id and (b.langID="
						+ langPK.getId() + ")";
			}

			rs = stmt.executeQuery(query);
			while (rs.next()) {
				String code = rs.getString(1).trim();
				String name = rs.getString(2);
				long nodeId = rs.getLong(3);

				Hashtable h = getHash(allNodesInfo, reporId);
				OONode o = (OONode) h.get(code);
				if (o == null) {
					o = new OONode(code);
					h.put(code, o);
				}
				o.setName(name);
				o.setId(nodeId);
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
		}
	}

	private void selectPeriodTypePriority(String reporId, Connection con) {
		Statement st = null;
		ResultSet rs = null;
		try {
			initStage(reporId, "Selecting period types");

			st = con.createStatement();
			rs = st.executeQuery("select p.periodtypeid from in_periods p group by p.periodtypeid "
					+ "order by max(p.todate-p.fromdate) ");

			int index = 0;
			while (rs.next()) {
				Hashtable h = getHash(periodTypePriority, reporId);
				h.put(new Long(rs.getLong(1)), new Integer(index++));
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(st);
		}
	}

	public void postCalculation(String reporId) throws EJBException {

		allBanksInfo.remove(reporId);
		allPeersInfo.remove(reporId);
		allPeriods.remove(reporId);
		allOffsetsPK.remove(reporId);
		allOffsetsSeq.remove(reporId);
		defCriterions.remove(reporId);
		peersBanks.remove(reporId);
		criterionBanks.remove(reporId);
		stageInfos.remove(reporId);
		allNodesInfo.remove(reporId);
		periodTypePriority.remove(reporId);
		reporInfos.remove(reporId);
		connections.remove(reporId);
		prepStatements.remove(reporId);
		allPeerBanks.remove(reporId);
		bankValueForPct.remove(reporId);
		VCTManager.clean(reporId);
	}

	public String bankgroupcode(String reportId, String code) {

		String defCriterionCode = (String) defCriterions.get(reportId);
		return (defCriterionCode == null) ? "#N/A" : bankgroupcodecriterion(
				reportId, code, defCriterionCode);
	}

	public String bankgroupcodecriterion(String reportId, String code,
			String criterion) {

		String groupCode = (String) getHash(criterionBanks, reportId).get(
				getBankCriterionKey(code, criterion));

		return groupCode == null ? "#N/A" : groupCode;
	}

	public String bankname(String reportId, String code) {

		Hashtable h = getHash(allBanksInfo, reportId);
		OOBank bank = (OOBank) h.get(code);

		if (bank == null || bank.getName() == null)
			return "#N/A";

		return bank.getName();
	}

	public int numofbanks(String reportId) {

		Hashtable h = (Hashtable) getHash(allBanksInfo, reportId);
		return h == null ? 0 : h.size();
	}

	public int numofbankspeer(String reportId, String peerCode) {

		List banks = (List) getHash(peersBanks, reportId).get(peerCode);

		return (banks == null) ? 0 : banks.size();
	}

	public int numoffisbytype(String reportId, String fiType) {
		int fisNumber = 0;
		Connection conn = getConnection(reportId);
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			ps = conn
					.prepareStatement("SELECT COUNT(*) AS FICOUNT FROM IN_BANKS b ,IN_BANK_TYPES bt WHERE b.TYPEID=bt.ID and RTRIM(bt.CODE)=?");
			ps.setString(1, fiType);
			rs = ps.executeQuery();
			if (rs.next()) {
				fisNumber = rs.getInt("FICOUNT");
			}

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		} finally {

		}
		return fisNumber;
	}

	public int numoffisubmited(String reportId, String fiTypeCode,
			String retCode, String periodStart, String periodEnd,
			String status, String version) {
		int n = 0;
		java.sql.Date periodFrom;
		java.sql.Date periodTo;
		int statusOrdinal = -1;
		String datePattern = "dd/MM/yyyy";
		DateFormat df = new SimpleDateFormat(datePattern);
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		String sql = "";
		try {
			periodFrom = new java.sql.Date(df.parse(periodStart).getTime());
			periodTo = new java.sql.Date(df.parse(periodEnd).getTime());
			status = status.trim().toLowerCase();

			if (status.equals("created")) {
				statusOrdinal = ReturnConstants.STATUS_CREATED;
			} else if (status.equals("amended")) {
				statusOrdinal = ReturnConstants.STATUS_AMENDED;
			} else if (status.equals("imported")) {
				statusOrdinal = ReturnConstants.STATUS_IMPORTED;
			} else if (status.equals("processed")) {
				statusOrdinal = ReturnConstants.STATUS_PROCESSED;
			} else if (status.equals("reset")) {
				statusOrdinal = ReturnConstants.STATUS_RESETED;
			} else if (status.equals("accepted")) {
				statusOrdinal = ReturnConstants.STATUS_ACCEPTED;
			} else if (status.equals("rejected")) {
				statusOrdinal = ReturnConstants.STATUS_REJECTED;
			} else if (status.equals("errors")) {
				statusOrdinal = ReturnConstants.STATUS_ERRORS;
			}
			sql = "select COUNT(returns.id) as numofreturns from IN_BANKS banks,IN_RETURNS returns, IN_PERIODS periods,IN_SCHEDULES schedules,   IN_RETURN_STATUSES retStats, IN_RETURN_TYPES retTypes,IN_RETURN_VERSIONS retVersion,IN_RETURN_DEFINITIONS definitions where  returns.ID=retStats.RETURNID and ";
			String fiTypeCodeSql = "";
			String retCodeSql = "";
			String periodFromSql = " periods.fromDate>=? and ";
			String periodToSql = "  periods.toDate<=? and ";
			String statusSql = "";
			String versionSql = "";
			if (!fiTypeCode.toLowerCase().equals("all")) {
				fiTypeCodeSql = " banks.ID in(select b.id from IN_BANKS b,IN_BANK_TYPES bt where RTRIM(bt.CODE)='"
						+ fiTypeCode.trim() + "' and b.TYPEID=bt.ID ) and ";
			}
			if (!retCode.toLowerCase().trim().equals("all")) {
				retCodeSql = " RTRIM(definitions.CODE)='" + retCode.trim()
						+ "' and ";
			}
			if (statusOrdinal != -1) {
				statusSql = " retStats.STATUS=" + statusOrdinal + " and ";
			}
			if (!version.toLowerCase().trim().equals("all")) {
				versionSql = " RTRIM(retVersion.CODE)='" + version.trim()
						+ "' and ";
			}

			sql += fiTypeCodeSql + " " + retCodeSql + " " + periodFromSql + " "
					+ periodToSql + " " + statusSql + " " + versionSql + " ";
			sql += " banks.id = schedules.bankID and returns.scheduleID = schedules.id  and periods.id = schedules.periodID "
					+ " and definitions.id = schedules.definitionID and retStats.returnID = returns.id and definitions.typeID = retTypes.id and retVersion.id = retStats.versionId"
					+ " and retStats.ID in(select max(j.id) from IN_RETURN_STATUSES j where j.returnID=returns.id group by j.versionid)  ";
			ps = con.prepareStatement(sql);
			ps.setDate(1, periodFrom);
			ps.setDate(2, periodTo);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				n = rs.getInt("numofreturns");
			}
		} catch (ParseException e) {
			log.error("Error during parsing date field");
			log.error(e.getMessage(), e);
		} catch (Exception ex) {
			log.error(sql);
			log.error(ex.getMessage(), ex);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return n;
	}

	public long numoffiexp(String reportId, String periodCode,
			String expression, String fiTypeCode) {
		return -1;
	}

	public String peergroupname(String reportId, String code) {

		Hashtable h = getHash(allPeersInfo, reportId);
		OOPeer peer = (OOPeer) h.get(code);
		if (peer == null || peer.getName() == null)
			return "#N/A";

		return peer.getName();
	}

	public int period(String reportId, String typeCode, int year, int month,
			int day) {

		int result = 0;
		Hashtable h = getHash(allPeriods, reportId);

		Calendar cal = Calendar.getInstance();
		cal.set(year, month - 1, day, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);

		for (java.util.Iterator iter = h.values().iterator(); iter.hasNext();) {
			OOPeriodPK period = (OOPeriodPK) iter.next();
			if (period.getTypeCode() != null
					&& period.getTypeCode().equals(typeCode)
					&& period.getFromDate() != null
					&& period.getFromDate().equals(cal.getTime())) {
				result = period.getId();
			}
		}

		return result;
	}

	public int periodfrom(String reportId, int period) {

		int periodFrom = 0;

		Hashtable h = getHash(allPeriods, reportId);
		OOPeriodPK periodPK = (OOPeriodPK) h.get(new PeriodPK(period));
		if (periodPK != null) {
			periodFrom = (int) (periodPK.getFromDate().getTime() >> 24);
		}

		return periodFrom;
	}

	public String periodfromdate(String reportId, int period) {

		String periodFrom = null;

		Hashtable h = getHash(allPeriods, reportId);
		OOPeriodPK periodPK = (OOPeriodPK) h.get(new PeriodPK(period));
		if (periodPK != null) {
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			periodFrom = format.format(periodPK.getFromDate());
		}

		return periodFrom;
	}

	public int periodnumber(String reportId, int period) {

		Hashtable h = getHash(allPeriods, reportId);
		OOPeriodPK o = (OOPeriodPK) h.get(new PeriodPK(period));

		if (o == null || o.getTypeCode() == null)
			return 0;

		return o.getNumber();
	}

	public int periodoffset(String reportId, int period, int offset) {
		int retVal = -1;
		PeriodPK pk = calcOffset(reportId, new PeriodPK(period), offset);
		if (pk != null) {
			retVal = pk.getId();
		}

		return retVal;
	}

	public int periodto(String reportId, int period) {

		int periodTo = 0;

		Hashtable h = getHash(allPeriods, reportId);
		OOPeriodPK periodPK = (OOPeriodPK) h.get(new PeriodPK(period));

		if (periodPK != null) {
			periodTo = (int) (periodPK.getToDate().getTime() >> 24);
		}
		return periodTo;
	}

	public String periodtodate(String reportId, int period) {

		String periodTo = null;

		Hashtable h = getHash(allPeriods, reportId);
		OOPeriodPK periodPK = (OOPeriodPK) h.get(new PeriodPK(period));

		if (periodPK != null) {
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			periodTo = format.format(periodPK.getToDate());
		}

		return periodTo;
	}

	public String periodtypecode(String reportId, int period) {

		Hashtable h = getHash(allPeriods, reportId);
		OOPeriodPK o = (OOPeriodPK) h.get(new PeriodPK(period));

		if (o == null || o.getTypeCode() == null)
			return "#N/A";

		return o.getTypeCode();

	}

	public String periodtypename(String reportId, int period) {

		Hashtable h = getHash(allPeriods, reportId);
		OOPeriodPK o = (OOPeriodPK) h.get(new PeriodPK(period));

		if (o == null || o.getTypeName() == null)
			return "#N/A";

		return o.getTypeName();

	}

	public double selbanksvalue(String reportId, String nodeCode,
			String banksParameterName, String banksFunction, int periodId,
			String periodFunction, int periodOffset) throws SQLException {

		return selbanksvaluever(reportId, nodeCode, banksParameterName,
				banksFunction, periodId, periodFunction, periodOffset,
				ReportConstants.LATEST_VERSION);

	}

	public double selbanksvaluever(String reportId, String nodeCode,
			String banksParameterName, String banksFunction, int periodId,
			String periodFunction, int periodOffset, String versionCode)
			throws SQLException {
		double retVal = Double.NaN;

		if (isAggregate(nodeCode)) {
			JSAggregateHelper js = JSAggregateHelper.selBankHelper(this,
					reportId, banksParameterName, banksFunction, periodId,
					periodFunction, periodOffset);
			try {
				double d = JSAggregateHelper.evalReportAggregate(js,
						generateJSAggregate(nodeCode));
				return d;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return Double.NaN;
			}
		} else {
			// NON AGREGATE
			// get bank ids'
			ReportInfo info = (ReportInfo) reporInfos.get(reportId);
			Collection param = ((fina2.ui.sheet.Parameter) info.parameters
					.get(banksParameterName)).getValues();

			Hashtable htBanks = getHash(allBanksInfo, reportId);
			StringBuffer banks = new StringBuffer();
			for (Iterator iter = param.iterator(); iter.hasNext();) {
				String code = (String) iter.next();
				OOBank bank = (OOBank) htBanks.get(code);
				if (bank != null) {
					banks.append(bank.getId());
					if (iter.hasNext()) {
						banks.append(", ");
					}
				}
			}

			retVal = selBankValue(reportId, nodeCode, banksFunction, periodId,
					periodFunction, periodOffset, banks, versionCode);
		}
		return retVal;

	}

	private double peerValue(String reportId, String nodeCode,
			String banksFunction, int periodId, String periodFunction,
			int periodOffset, String peerCode, String versionCode)
			throws SQLException {

		double retVal = Double.NaN;
		int bankFunc = AgregateFunction.getCode(banksFunction);
		int periodFunc = PeriodFunction.getCode(periodFunction);
		OONode node = (OONode) getHash(allNodesInfo, reportId).get(nodeCode);
		PeriodPK periodPk = calcOffset(reportId, new PeriodPK(periodId),
				periodOffset);

		OOPeriodPK period = (periodPk != null) ? (OOPeriodPK) getHash(
				allPeriods, reportId).get(periodPk) : null;

		if (node != null && period != null && bankFunc != AgregateFunction.UNK) {
			switch (periodFunc) {
			case PeriodFunction.YDTAVERAGE:
				Date startDate = getYdtBeggining(period, reportId,
						getYdtPeriodCode(periodFunction));
				if (startDate != null) {
					retVal = peerValueAgregate(reportId, node, peerCode,
							bankFunc, startDate, period.getToDate(),
							PeriodFunction.AVG, versionCode);
				}
				break;
			case PeriodFunction.YAVERAGE:
				retVal = peerValueAgregate(reportId, node, peerCode, bankFunc,
						getYearBeggining(period.getFromDate()),
						period.getToDate(), PeriodFunction.AVG, versionCode);
				break;
			case PeriodFunction.AVG:
			case PeriodFunction.SUM:
			case PeriodFunction.MIN:
			case PeriodFunction.MAX:
			case PeriodFunction.COUNT:
				retVal = peerValueAgregate(reportId, node, peerCode, bankFunc,
						period.getFromDate(), period.getToDate(), periodFunc,
						versionCode);
				break;
			case PeriodFunction.LAST:
				retVal = peerValueLast(reportId, node, bankFunc,
						period.getFromDate(), period.getToDate(), peerCode,
						versionCode);
				break;
			}
		}
		return retVal;
	}

	private double selBankValue(String reportId, String nodeCode,
			String banksFunction, int periodId, String periodFunction,
			int periodOffset, StringBuffer banks, String versionCode)
			throws SQLException {
		double retVal = Double.NaN;
		int bankFunc = AgregateFunction.getCode(banksFunction);
		int periodFunc = PeriodFunction.getCode(periodFunction);
		OONode node = (OONode) getHash(allNodesInfo, reportId).get(nodeCode);
		PeriodPK periodPk = calcOffset(reportId, new PeriodPK(periodId),
				periodOffset);
		OOPeriodPK period = (periodPk != null) ? (OOPeriodPK) getHash(
				allPeriods, reportId).get(periodPk) : null;

		if (node != null && period != null && banks.length() > 0
				&& bankFunc != AgregateFunction.UNK) {
			switch (periodFunc) {
			case PeriodFunction.YDTAVERAGE:
				Date startDate = getYdtBeggining(period, reportId,
						getYdtPeriodCode(periodFunction));
				if (startDate != null) {
					retVal = selBankValueAgregate(reportId, node,
							banks.toString(), bankFunc, startDate,
							period.getToDate(), PeriodFunction.AVG, versionCode);
				}
				break;
			case PeriodFunction.YAVERAGE:
				retVal = selBankValueAgregate(reportId, node, banks.toString(),
						bankFunc, getYearBeggining(period.getFromDate()),
						period.getToDate(), PeriodFunction.AVG, versionCode);
				break;
			case PeriodFunction.AVG:
			case PeriodFunction.SUM:
			case PeriodFunction.MIN:
			case PeriodFunction.MAX:
			case PeriodFunction.COUNT:
				retVal = selBankValueAgregate(reportId, node, banks.toString(),
						bankFunc, period.getFromDate(), period.getToDate(),
						periodFunc, versionCode);
				break;
			case PeriodFunction.LAST:
				retVal = selBankValueLast(reportId, node, banks.toString(),
						bankFunc, period.getFromDate(), period.getToDate(),
						periodFunc, versionCode);
				break;
			}
		}
		return retVal;
	}

	public String textvalue(String reportId, String nodeCode, String bankCode,
			int period, int periodOffset) throws SQLException {

		return textvaluever(reportId, nodeCode, bankCode, period, periodOffset,
				ReportConstants.LATEST_VERSION);
	}

	public String textvaluever(String reportId, String nodeCode,
			String bankCode, int period, int periodOffset, String versionCode)
			throws SQLException {

		String retVal = "#N/A";

		OONode node = (OONode) getHash(allNodesInfo, reportId).get(nodeCode);
		OOBank bank = (OOBank) getHash(allBanksInfo, reportId).get(bankCode);
		PeriodPK periodPk = calcOffset(reportId, new PeriodPK(period),
				periodOffset);

		StringBuffer sql = new StringBuffer();
		sql.append("select t.value from result_view t where t.nodeid=? and ")
				.append("t.bankid=? AND t.periodid=? ")
				.append(versionFilter(versionCode));

		if (bank != null && node != null && periodPk != null) {

			PreparedStatement ps = getPreparedStatement(reportId,
					sql.toString());

			ps.setLong(1, node.getId());
			ps.setLong(2, bank.getId());
			ps.setLong(3, periodPk.getId());

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				retVal = rs.getString(1);
			}
		}

		return retVal;
	}

	public double vctvalue(String reportId, String nodeCode, String iterName,
			String groupByValue, String function) {

		if (isAggregate(nodeCode)) {
			JSAggregateHelper js = JSAggregateHelper.vctValueHelper(this,
					reportId, iterName, groupByValue, function);
			try {
				double d = JSAggregateHelper.evalReportAggregate(js,
						generateJSAggregate(nodeCode));
				return d;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return Double.NaN;
			}
		} else {
			VCTManager vm = VCTManager.getInstance(reportId);
			return vm.getVctFunctionResult(iterName, nodeCode, groupByValue,
					function);
		}
	}

	public double peervalue(String reportId, String nodeCode, String peerGroup,
			String peerGroupFunction, int periodId, String periodFunction,
			int periodOffset) throws SQLException {

		return peervaluever(reportId, nodeCode, peerGroup, peerGroupFunction,
				periodId, periodFunction, periodOffset,
				ReportConstants.LATEST_VERSION);
	}

	public double peervaluever(String reportId, String nodeCode,
			String peerGroup, String peerGroupFunction, int periodId,
			String periodFunction, int periodOffset, String versionCode)
			throws SQLException {

		double retVal = Double.NaN;

		if (isAggregate(nodeCode)) {
			JSAggregateHelper js = JSAggregateHelper.peerValueHelper(this,
					reportId, peerGroup, peerGroupFunction, periodId,
					periodFunction, periodOffset, versionCode);
			try {
				double d = JSAggregateHelper.evalReportAggregate(js,
						generateJSAggregate(nodeCode));
				return d;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return Double.NaN;
			}
		} else {
			retVal = peerValue(reportId, nodeCode, peerGroupFunction, periodId,
					periodFunction, periodOffset, peerGroup, versionCode);
		}
		return retVal;
	}

	public double pctvalue(String reportId, String nodeCode, String bankCode,
			String peerCode, int periodId, String periodFunction,
			int periodOffset) throws SQLException {
		return pctvaluever(reportId, nodeCode, bankCode, peerCode, periodId,
				periodFunction, periodOffset, ReportConstants.LATEST_VERSION);
	}

	public double pctvaluever(String reportId, String nodeCode,
			String bankCode, String peerCode, int periodId,
			String periodFunction, int periodOffset, String versionCode)
			throws SQLException {

		double retVal = Double.NaN;

		OOPeer peer = (OOPeer) getHash(allPeersInfo, reportId).get(peerCode);
		OOBank bank = (OOBank) getHash(allBanksInfo, reportId).get(bankCode);
		PeriodPK periodPk = calcOffset(reportId, new PeriodPK(periodId),
				periodOffset);
		OOPeriodPK period = (periodPk != null) ? (OOPeriodPK) getHash(
				allPeriods, reportId).get(periodPk) : null;

		if (period != null && peer != null) {
			double bankValue = loadBankValuesForPct(reportId, nodeCode, period,
					(int) bank.getId(), periodFunction, versionCode);

			Collection bankGroup = (Collection) getHash(allPeerBanks, reportId)
					.get(peerCode);
			retVal = 1;
			for (Iterator iter = bankGroup.iterator(); iter.hasNext();) {
				OOBank peerBank = (OOBank) iter.next();
				double peerValue = loadBankValuesForPct(reportId, nodeCode,
						period, peerBank.getId(), periodFunction, versionCode);
				if (peerValue > bankValue) {
					retVal++;
				}
			}
		}

		return retVal;
	}

	public double pctvalex(String reportId, String expression, String bankCode,
			String peerCode, String versionCode, Object pctf[]) {

		log.debug("Calculating PCTVALEX function");
		log.debug("Expression: " + expression);
		log.debug("Bank code: " + bankCode);
		log.debug("Peer code: " + peerCode);
		log.debug("Version code: " + versionCode);
		for (int i = 0; i < pctf.length; i++) {
			log.debug("PCT parameters: " + pctf[i]);
		}

		OOPeer peer = (OOPeer) getHash(allPeersInfo, reportId).get(peerCode);
		if (peer == null) {
			log.error("Invalid peer code detected. Peer code: " + peerCode);
			return Double.NaN;
		}

		String jScript = null;
		try {
			// Prepare JScript for execution
			Object[] args = new Object[30];
			for (int i = 0; i < args.length; i++) {
				args[i] = "helper.getBankValuesForPct(" + i + ")";
			}
			jScript = "return " + MessageFormat.format(expression, args);
		} catch (Exception ex) {
			log.error("Error processing PCT expression. Expression: "
					+ expression, ex);
			return Double.NaN;
		}

		// Calculate bank value
		double bankValue = getBankValuesForPct(reportId, bankCode, versionCode,
				pctf, jScript);
		if (Double.isNaN(bankValue)) {
			return Double.NaN;
		}

		double result = 1;
		Collection bankGroup = (Collection) getHash(allPeerBanks, reportId)
				.get(peerCode);
		for (Iterator iter = bankGroup.iterator(); iter.hasNext();) {
			OOBank peerBank = (OOBank) iter.next();
			// Calculate peer bank value
			double peerValue = getBankValuesForPct(reportId,
					peerBank.getCode(), versionCode, pctf, jScript);
			// Compare bank and peer values
			if (!Double.isNaN(peerValue) && peerValue > bankValue) {
				result++;
			}
		}
		return result;
	}

	private double getBankValuesForPct(String reportId, String bankCode,
			String versionCode, Object[] pctf, String jScript) {

		JSAggregateHelper jsHelper = JSAggregateHelper.pctValExHelper(this,
				reportId, bankCode, versionCode, pctf);
		try {
			return JSAggregateHelper.evalReportAggregate(jsHelper, jScript);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return Double.NaN;
		}
	}

	public PctParameters parsePctParameters(String pctf) throws ParseException {
		PctParameters result = new PctParameters();

		// Example of PCTF format: "node01/node02;0;sum;-1"
		MessageFormat mf = new MessageFormat(
				"{0};{1,number,integer};{2};{3,number,integer}");
		Object pctArgs[] = mf.parse(pctf);

		result.nodeCode = (String) pctArgs[0];
		result.period = ((Long) pctArgs[1]).intValue();
		result.periodFunction = (String) pctArgs[2];
		result.periodOffset = ((Long) pctArgs[3]).intValue();

		return result;
	}

	public double getBankValuesForPct(String reportId, String nodeCode,
			String bankCode, int periodId, String periodFunction,
			int periodOffset, String versionCode) throws SQLException {

		double result = Double.NaN;

		OOBank bank = (OOBank) getHash(allBanksInfo, reportId).get(bankCode);
		PeriodPK periodPk = calcOffset(reportId, new PeriodPK(periodId),
				periodOffset);
		OOPeriodPK period = (periodPk != null) ? (OOPeriodPK) getHash(
				allPeriods, reportId).get(periodPk) : null;

		if (period != null && bank != null) {
			result = loadBankValuesForPct(reportId, nodeCode, period,
					(int) bank.getId(), periodFunction, versionCode);
		}

		return result;
	}

	public double bankvalue(String reportId, String nodeCode, String bankCode,
			int periodId, String periodFunction, int periodOffset)
			throws SQLException {

		return bankvaluever(reportId, nodeCode, bankCode, periodId,
				periodFunction, periodOffset, ReportConstants.LATEST_VERSION);
	}

	public double bankvaluever(String reportId, String nodeCode,
			String bankCode, int periodId, String periodFunction,
			int periodOffset, String versionCode) throws SQLException {

		double retVal = Double.NaN;

		if (isAggregate(nodeCode)) {
			JSAggregateHelper js = JSAggregateHelper.bankValueHelper(this,
					reportId, bankCode, periodId, periodFunction, periodOffset,
					versionCode);
			try {
				double d = JSAggregateHelper.evalReportAggregate(js,
						generateJSAggregate(nodeCode));
				return d;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return Double.NaN;
			}
		} else {
			// NON AGREGATE
			int periodFunc = PeriodFunction.getCode(periodFunction);
			OONode node = (OONode) getHash(allNodesInfo, reportId)
					.get(nodeCode);
			OOBank bank = (OOBank) getHash(allBanksInfo, reportId)
					.get(bankCode);
			PeriodPK periodPk = calcOffset(reportId, new PeriodPK(periodId),
					periodOffset);
			OOPeriodPK period = (periodPk != null) ? (OOPeriodPK) getHash(
					allPeriods, reportId).get(periodPk) : null;

			if (bank != null && node != null && period != null) {
				switch (periodFunc) {
				case PeriodFunction.YDTAVERAGE:
					Date startDate = getYdtBeggining(period, reportId,
							getYdtPeriodCode(periodFunction));
					if (startDate != null) {
						retVal = bankValueAgregate(reportId, node, bank,
								startDate, period.getToDate(),
								PeriodFunction.AVG, versionCode);
					}
					break;
				case PeriodFunction.YAVERAGE:
					retVal = bankValueAgregate(reportId, node, bank,
							getYearBeggining(period.getFromDate()),
							period.getToDate(), PeriodFunction.AVG, versionCode);
					break;
				case PeriodFunction.AVG:
				case PeriodFunction.SUM:
				case PeriodFunction.MIN:
				case PeriodFunction.MAX:
				case PeriodFunction.COUNT:
					retVal = bankValueAgregate(reportId, node, bank,
							period.getFromDate(), period.getToDate(),
							periodFunc, versionCode);
					break;
				case PeriodFunction.LAST:
					retVal = bankValueLast(reportId, node, bank,
							period.getFromDate(), period.getToDate(),
							periodFunc, versionCode);
					break;
				}
			}
		}
		return retVal;
	}

	public String nodename(String reportId, String code) {

		String retVal = "#N/A";

		Hashtable h = getHash(allNodesInfo, reportId);
		OONode node = (OONode) h.get(code);

		if (node != null && node.getName() != null) {
			retVal = node.getName();
		}

		return retVal;
	}

	public double allbanksvalue(String reportId, String nodeCode,
			String banksFunction, int periodId, String periodFunction,
			int periodOffset) throws SQLException {

		return allbanksvaluever(reportId, nodeCode, banksFunction, periodId,
				periodFunction, periodOffset, ReportConstants.LATEST_VERSION);

	}

	public double allbanksvaluever(String reportId, String nodeCode,
			String banksFunction, int periodId, String periodFunction,
			int periodOffset, String versionCode) throws SQLException {
		double retVal = Double.NaN;

		if (isAggregate(nodeCode)) {
			JSAggregateHelper js = JSAggregateHelper.allBankHelper(this,
					reportId, banksFunction, periodId, periodFunction,
					periodOffset, versionCode);
			try {
				double d = JSAggregateHelper.evalReportAggregate(js,
						generateJSAggregate(nodeCode));
				return d;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return Double.NaN;
			}
		} else {
			// NON AGREGATE
			int bankFunc = AgregateFunction.getCode(banksFunction);
			int periodFunc = PeriodFunction.getCode(periodFunction);
			OONode node = (OONode) getHash(allNodesInfo, reportId)
					.get(nodeCode);
			PeriodPK periodPk = calcOffset(reportId, new PeriodPK(periodId),
					periodOffset);
			OOPeriodPK period = (periodPk != null) ? (OOPeriodPK) getHash(
					allPeriods, reportId).get(periodPk) : null;

			if (node != null && period != null
					&& bankFunc != AgregateFunction.UNK) {
				switch (periodFunc) {
				case PeriodFunction.YDTAVERAGE:
					Date startDate = getYdtBeggining(period, reportId,
							getYdtPeriodCode(periodFunction));
					if (startDate != null) {
						retVal = allBankValueAgregate(reportId, node, bankFunc,
								startDate, period.getToDate(),
								PeriodFunction.AVG, versionCode);
					}
					break;
				case PeriodFunction.YAVERAGE:
					retVal = allBankValueAgregate(reportId, node, bankFunc,
							getYearBeggining(period.getFromDate()),
							period.getToDate(), PeriodFunction.AVG, versionCode);
					break;
				case PeriodFunction.AVG:
				case PeriodFunction.SUM:
				case PeriodFunction.MIN:
				case PeriodFunction.MAX:
				case PeriodFunction.COUNT:
					retVal = allBankValueAgregate(reportId, node, bankFunc,
							period.getFromDate(), period.getToDate(),
							periodFunc, versionCode);
					break;
				case PeriodFunction.LAST:
					retVal = allBankValueLast(reportId, node, bankFunc,
							period.getFromDate(), period.getToDate(),
							periodFunc, versionCode);
					break;
				}
			}
			return retVal;
		}
	}

	public String repository(String reportId, int formulaID, String parameters) {
		return "#N/A";
	}

	public String curbank(String reportId) {
		return "#N/A";
	}

	public String curnode(String reportId) {
		return "#N/A";
	}

	public int curoffset(String reportId) {
		return -1;
	}

	public String curpeergroup(String reportId) {
		return "#N/A";
	}

	public int curperiod(String reportId) {
		return -1;
	}

	public String curvctvalue(String reportId) {
		return "#N/A";
	}

	private static String getYdtPeriodCode(String periodFunction) {
		String retVal = null;

		int pos = periodFunction.indexOf(":");

		if (pos > 0
				&& periodFunction.substring(0, pos).equals(
						PeriodFunction.F_YDTAVERAGE)) {
			retVal = periodFunction.substring(pos + 1);
		}

		return retVal;
	}

	private Date getYdtBeggining(OOPeriodPK value, String reportId,
			String periodTypeCode) {
		Date retVal = null;
		Hashtable h = getHash(allPeriods, reportId);
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(value.getFromDate());
		calendar.set(Calendar.DAY_OF_YEAR, 1);
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		Date d = calendar.getTime();
		String typeCode = (periodTypeCode != null) ? periodTypeCode : value
				.getTypeCode();
		for (Iterator iter = h.values().iterator(); iter.hasNext();) {
			OOPeriodPK period = (OOPeriodPK) iter.next();
			if (period.getTypeCode().equalsIgnoreCase(typeCode)
					&& d.equals(period.getToDate())) {
				retVal = period.getFromDate();
				break;
			}
		}
		return retVal;
	}

	private synchronized Date getYearBeggining(Date value) {
		calendar.setTime(value);
		calendar.set(Calendar.DAY_OF_YEAR, 1);
		return calendar.getTime();
	}

	private static final Hashtable getHash(Hashtable parent, Object key) {
		Hashtable result = (Hashtable) parent.get(key);
		if (result == null) {
			result = new Hashtable();
			parent.put(key, result);
		}
		return result;
	}

	private double retriveResult(String reportId, ResultSet rs)
			throws SQLException {
		/*
		 * double retVal = Double.NaN; Long oldPeriodType = null; while
		 * (rs.next()) { Long periodType = new Long(rs.getLong(1)); double value
		 * = rs.getDouble(2); if (comparePeriodTypePriorities(reportId,
		 * periodType, oldPeriodType) > 0) { retVal = value; } } return retVal;
		 */
		double retVal = Double.NaN;
		// Long oldPeriodType = null;
		while (rs.next()) {
			if (comparePeriodTypePriorities(reportId, rs.getLong(1), null) > 0) {
				retVal = rs.getDouble(2);
			}
		}
		rs.close();
		return retVal;
	}

	private double bankValueAgregate(String reportId, OONode node, OOBank bank,
			Date periodStart, Date periodEnd, int periodFunc, String versionCode)
			throws SQLException {

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT tr.periodtypeid, ")
				.append(PeriodFunction.getSqlFunction(periodFunc))
				.append("(tr.tagr) FROM (SELECT t.periodtypeid, sum(t.nvalue) as tagr ")
				.append("FROM result_view t WHERE t.nodeid=? AND t.bankid=? AND t.nvalue is not NULL ")
				.append("AND t.fromdate >=? AND t.todate<=? ")
				.append(versionFilter(versionCode))
				.append("GROUP BY t.periodtypeid, t.periodid) tr GROUP BY tr.periodtypeid");

		PreparedStatement ps = getPreparedStatement(reportId, sql.toString());

		ps.setLong(1, node.getId());
		ps.setLong(2, bank.getId());
		ps.setDate(3, new java.sql.Date(periodStart.getTime()));
		ps.setDate(4, new java.sql.Date(periodEnd.getTime()));

		// ResultSet rs = ;
		sql = null;

		return retriveResult(reportId, ps.executeQuery());
	}

	private double bankValueLast(String reportId, OONode node, OOBank bank,
			Date periodStart, Date periodEnd, int periodFunc, String versionCode)
			throws SQLException {

		StringBuffer sql = new StringBuffer(150);
		sql.append("SELECT t.periodtypeid, sum(nvalue) ")
				.append("FROM result_view t WHERE t.nodeid=? AND t.bankid=? AND t.nvalue is not NULL AND ")
				.append("t.fromdate >=? AND t.todate=? ")
				.append(versionFilter(versionCode))
				.append("GROUP BY t.periodtypeid, t.periodid ");

		PreparedStatement ps = getPreparedStatement(reportId, sql.toString());

		ps.setLong(1, node.getId());
		ps.setLong(2, bank.getId());
		ps.setDate(3, new java.sql.Date(periodStart.getTime()));
		ps.setDate(4, new java.sql.Date(periodEnd.getTime()));

		ResultSet rs = ps.executeQuery();

		return retriveResult(reportId, rs);
	}

	private double allBankValueAgregate(String reportId, OONode node,
			int bankFunc, Date periodStart, Date periodEnd, int periodFunc,
			String versionCode) throws SQLException {

		StringBuffer sql = new StringBuffer(150);
		sql.append("SELECT periodtypeid, ")
				.append(AgregateFunction.getSqlFunction(bankFunc))
				.append("(agr) from (SELECT tr.bankid, tr.periodtypeid, ")
				.append(PeriodFunction.getSqlFunction(periodFunc))
				.append("(tr.tagr) as agr FROM (SELECT t.bankid, t.periodtypeid, sum(t.nvalue) as tagr ")
				.append("FROM result_view t WHERE t.nodeid=? AND t.nvalue is not NULL AND t.fromdate >=? AND t.todate<=? ")
				.append(versionFilter(versionCode))
				.append("GROUP BY t.bankid, t.periodtypeid, t.periodid) tr GROUP BY tr.bankid, tr.periodtypeid) res group by res.periodtypeid");

		PreparedStatement ps = getPreparedStatement(reportId, sql.toString());

		ps.setLong(1, node.getId());
		ps.setDate(2, new java.sql.Date(periodStart.getTime()));
		ps.setDate(3, new java.sql.Date(periodEnd.getTime()));

		ResultSet rs = ps.executeQuery();

		return retriveResult(reportId, rs);
	}

	private double allBankValueLast(String reportId, OONode node, int bankFunc,
			Date periodStart, Date periodEnd, int periodFunc, String versionCode)
			throws SQLException {

		StringBuffer sql = new StringBuffer(150);
		sql.append("SELECT periodtypeid, ")
				.append(AgregateFunction.getSqlFunction(bankFunc))
				.append("(agr) from (SELECT t.bankid, t.periodtypeid, ")
				.append("sum(nvalue) as agr FROM result_view t WHERE t.nodeid=? AND t.nvalue is not NULL ")
				.append("AND t.fromdate >=? AND t.todate=? ")
				.append(versionFilter(versionCode))
				.append("GROUP BY t.bankid, t.periodtypeid, t.periodid) res group by res.periodtypeid");

		PreparedStatement ps = getPreparedStatement(reportId, sql.toString());

		ps.setLong(1, node.getId());
		ps.setDate(2, new java.sql.Date(periodStart.getTime()));
		ps.setDate(3, new java.sql.Date(periodEnd.getTime()));

		ResultSet rs = ps.executeQuery();

		return retriveResult(reportId, rs);
	}

	private double peerValueAgregate(String reportId, OONode node,
			String peerCode, int bankFunc, Date periodStart, Date periodEnd,
			int periodFunc, String versionCode) throws SQLException {
		StringBuffer sql = new StringBuffer(300);

		sql.append("SELECT periodtypeid, ")
				.append(AgregateFunction.getSqlFunction(bankFunc))
				.append("(agr) from (SELECT tr.bankid, tr.periodtypeid, ")
				.append(PeriodFunction.getSqlFunction(periodFunc))
				.append("(tr.tagr) as agr FROM (SELECT t.bankid, t.periodtypeid, sum(t.nvalue) as tagr ")
				.append("FROM result_view t, IN_BANKS b, IN_BANK_GROUPS bg, MM_BANK_GROUP mbg ")
				.append("WHERE t.nodeid=? AND t.nvalue is not NULL AND t.bankid=b.id ")
				.append("AND t.fromdate >=? AND t.todate<=? ")
				.append(versionFilter(versionCode))
				.append("AND mbg.bankID=b.ID AND mbg.bankgroupID=bg.ID AND RTRIM(bg.code)=? ")
				.append("GROUP BY t.bankid, t.periodtypeid, t.periodid) tr ")
				.append("GROUP BY tr.bankid, tr.periodtypeid) res group by res.periodtypeid");

		PreparedStatement ps = getPreparedStatement(reportId, sql.toString());

		ps.setLong(1, node.getId());
		ps.setDate(2, new java.sql.Date(periodStart.getTime()));
		ps.setDate(3, new java.sql.Date(periodEnd.getTime()));
		ps.setString(4, peerCode);

		ResultSet rs = ps.executeQuery();

		return retriveResult(reportId, rs);
	}

	private double peerValueLast(String reportId, OONode node, int bankFunc,
			Date periodStart, Date periodEnd, String peerCode,
			String versionCode) throws SQLException {

		StringBuffer sql = new StringBuffer(150);
		sql.append("SELECT periodtypeid, ")
				.append(AgregateFunction.getSqlFunction(bankFunc))
				.append("(agr) from (SELECT t.bankid, t.periodtypeid, sum(nvalue) as agr ")
				.append("FROM result_view t, IN_BANKS b, IN_BANK_GROUPS bg, MM_BANK_GROUP mbg ")
				.append("WHERE t.nodeid=? AND t.nvalue is not NULL AND t.bankid = b.ID ")
				.append("AND t.fromdate >=? AND t.todate=? ")
				.append(versionFilter(versionCode))
				.append("AND mbg.bankID=b.ID AND mbg.bankgroupID=bg.ID AND RTRIM(bg.code)=? ")
				.append("GROUP BY t.bankid, t.periodtypeid, t.periodid) res group by res.periodtypeid");

		PreparedStatement ps = getPreparedStatement(reportId, sql.toString());

		ps.setLong(1, node.getId());
		ps.setDate(2, new java.sql.Date(periodStart.getTime()));
		ps.setDate(3, new java.sql.Date(periodEnd.getTime()));
		ps.setString(4, peerCode);

		ResultSet rs = ps.executeQuery();

		return retriveResult(reportId, rs);
	}

	private double selBankValueAgregate(String reportId, OONode node,
			String banks, int bankFunc, Date periodStart, Date periodEnd,
			int periodFunc, String versionCode) throws SQLException {
		StringBuffer sql = new StringBuffer(300);

		sql.append("SELECT periodtypeid, ")
				.append(AgregateFunction.getSqlFunction(bankFunc))
				.append("(agr) from (SELECT tr.bankid, tr.periodtypeid, ")
				.append(PeriodFunction.getSqlFunction(periodFunc))
				.append("(tr.tagr) as agr FROM (SELECT t.bankid, t.periodtypeid, sum(t.nvalue) as tagr ")
				.append("FROM result_view t WHERE t.nodeid=? AND t.nvalue is not NULL AND t.fromdate >=? AND t.todate<=? ")
				.append("AND t.bankid in (")
				.append(banks)
				.append(") ")
				.append(versionFilter(versionCode))
				.append("GROUP BY t.bankid, t.periodtypeid, t.periodid) tr ")
				.append("GROUP BY tr.bankid, tr.periodtypeid) res group by res.periodtypeid");

		PreparedStatement ps = getPreparedStatement(reportId, sql.toString());

		ps.setLong(1, node.getId());
		ps.setDate(2, new java.sql.Date(periodStart.getTime()));
		ps.setDate(3, new java.sql.Date(periodEnd.getTime()));

		ResultSet rs = ps.executeQuery();

		return retriveResult(reportId, rs);
	}

	private double selBankValueLast(String reportId, OONode node, String banks,
			int bankFunc, Date periodStart, Date periodEnd, int periodFunc,
			String versionCode) throws SQLException {
		StringBuffer sql = new StringBuffer(150);
		sql.append("SELECT periodtypeid, ")
				.append(AgregateFunction.getSqlFunction(bankFunc))
				.append("(agr) from (SELECT t.bankid, t.periodtypeid, sum(nvalue) as agr ")
				.append("FROM result_view t WHERE t.nodeid=? AND t.nvalue is not NULL AND t.bankid in (")
				.append(banks)
				.append(") AND t.fromdate >=? AND t.todate=? ")
				.append(versionFilter(versionCode))
				.append("GROUP BY t.bankid, t.periodtypeid, t.periodid) res group by res.periodtypeid");

		PreparedStatement ps = getPreparedStatement(reportId, sql.toString());

		ps.setLong(1, node.getId());
		ps.setDate(2, new java.sql.Date(periodStart.getTime()));
		ps.setDate(3, new java.sql.Date(periodEnd.getTime()));

		ResultSet rs = ps.executeQuery();

		return retriveResult(reportId, rs);
	}

	private void cacheBankValuesForPct(String reportId,
			List<PctParameters> pctParamsList) throws SQLException {
		Map sqlParams = new HashMap();

		for (PctParameters pctParams : pctParamsList) {

			// Convert parameters to objects
			int periodFunc = PeriodFunction.getCode(pctParams.periodFunction);
			OOBank bank = (OOBank) getHash(allBanksInfo, reportId).get(
					pctParams.bankCode);
			OOPeer peer = (OOPeer) getHash(allPeersInfo, reportId).get(
					pctParams.peerCode);
			PeriodPK periodPk = calcOffset(reportId, new PeriodPK(
					pctParams.period), pctParams.periodOffset);
			OOPeriodPK period = (periodPk != null) ? (OOPeriodPK) getHash(
					allPeriods, reportId).get(periodPk) : null;

			// Store parameters for SQL
			if (period != null && peer != null && bank != null
					&& periodFunc != PeriodFunction.UNK) {
				if (isAggregate(pctParams.nodeCode)) {
					StringTokenizer st = new StringTokenizer(
							pctParams.nodeCode, " +-*/()");
					while (st.hasMoreTokens()) {
						storePctCacheSql(reportId, sqlParams, st.nextToken(),
								periodFunc, pctParams.periodFunction, bank,
								peer, period, pctParams.versionCode);
					}
				} else {
					storePctCacheSql(reportId, sqlParams, pctParams.nodeCode,
							periodFunc, pctParams.periodFunction, bank, peer,
							period, pctParams.versionCode);
				}
			}
		}

		// Prepare banks' list for unique node and period
		Map sortedSqlParams = new HashMap();
		for (Iterator iter = sqlParams.values().iterator(); iter.hasNext();) {
			PctFuncContainer pctFuncContent = (PctFuncContainer) iter.next();

			Set groupBanks = getAllGroupBanks(reportId, pctFuncContent.peers);
			groupBanks.addAll(pctFuncContent.banks);
			String banks = getBankIdList(groupBanks);
			pctFuncContent.bankList = banks;

			// prepare sorting key
			StringBuffer key = new StringBuffer(100);
			key.append(pctFuncContent.periodFunc).append('[').append(banks)
					.append(']').append(pctFuncContent.period.getId())
					.append(',').append(pctFuncContent.node.getId())
					.append(',').append(pctFuncContent.versionCode);

			sortedSqlParams.put(key.toString(), pctFuncContent);
		}

		// Sort params for faster SQL execution
		List sortedKeys = new LinkedList(sortedSqlParams.keySet());
		Collections.sort(sortedKeys);

		initStage(reportId, "Start PCT data caching...");

		// execute SQL and store results in cache
		for (Iterator iter = sortedKeys.iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			PctFuncContainer pctFuncContainer = (PctFuncContainer) sortedSqlParams
					.get(key);

			switch (pctFuncContainer.periodFunc) {
			case PeriodFunction.YAVERAGE:
				cacheBankValuesForPctAgregate(
						reportId,
						pctFuncContainer.node,
						pctFuncContainer.bankList,
						getYearBeggining(pctFuncContainer.period.getFromDate()),
						pctFuncContainer.period.getToDate(),
						PeriodFunction.AVG, pctFuncContainer.versionCode);
				break;
			case PeriodFunction.YDTAVERAGE:
				Date startDate = getYdtBeggining(pctFuncContainer.period,
						reportId,
						getYdtPeriodCode(pctFuncContainer.periodFunction));
				if (startDate != null) {
					cacheBankValuesForPctAgregate(reportId,
							pctFuncContainer.node, pctFuncContainer.bankList,
							startDate, pctFuncContainer.period.getToDate(),
							PeriodFunction.AVG, pctFuncContainer.versionCode);
				}
				break;
			case PeriodFunction.AVG:
			case PeriodFunction.SUM:
			case PeriodFunction.MIN:
			case PeriodFunction.MAX:
			case PeriodFunction.COUNT:
				cacheBankValuesForPctAgregate(reportId, pctFuncContainer.node,
						pctFuncContainer.bankList,
						pctFuncContainer.period.getFromDate(),
						pctFuncContainer.period.getToDate(),
						pctFuncContainer.periodFunc,
						pctFuncContainer.versionCode);
				break;
			case PeriodFunction.LAST:
				cacheBankValuesForPctLast(reportId, pctFuncContainer.node,
						pctFuncContainer.bankList,
						pctFuncContainer.period.getFromDate(),
						pctFuncContainer.period.getToDate(),
						pctFuncContainer.periodFunc,
						pctFuncContainer.versionCode);
			}

			initStage(
					reportId,
					"Caching PCT data for: ["
							+ pctFuncContainer.node
							+ ", {"
							+ pctFuncContainer.bankList
							+ "}, "
							+ pctFuncContainer.period.getId()
							+ ", "
							+ PeriodFunction
									.getFinaFunction(pctFuncContainer.periodFunc)
							+ "]");
		}
	}

	private void storePctCacheSql(String reportId, Map sqlParams,
			String nodeCode, int periodFunc, String periodFunction,
			OOBank bank, OOPeer peer, OOPeriodPK period, String versionCode) {
		OONode node = (OONode) getHash(allNodesInfo, reportId).get(nodeCode);
		if (node != null) {
			String key = PeriodFunction.getFinaFunction(periodFunc)
					+ period.getId() + "," + node.toString() + ","
					+ versionCode;

			PctFuncContainer p = (PctFuncContainer) sqlParams.get(key);
			if (p == null) {
				sqlParams.put(key, new PctFuncContainer(periodFunction,
						periodFunc, node, bank, peer, period, versionCode));
			} else {
				p.add(bank, peer);
			}
		}
	}

	private String getBankIdList(Collection c) {
		StringBuffer strBuf = new StringBuffer();
		for (Iterator iter = c.iterator(); iter.hasNext();) {
			OOBank bank = (OOBank) iter.next();
			strBuf.append(bank.getId());
			if (iter.hasNext()) {
				strBuf.append(", ");
			}
		}
		return strBuf.toString();
	}

	private Set getGroupBanks(String reportId, OOPeer group) {
		Set banks = new HashSet();
		Collection bankCodes = (Collection) getHash(peersBanks, reportId).get(
				group.toString());
		Hashtable htBanks = getHash(allBanksInfo, reportId);
		for (Iterator iter = bankCodes.iterator(); iter.hasNext();) {
			String code = (String) iter.next();
			OOBank bank = (OOBank) htBanks.get(code);
			if (bank != null) {
				banks.add(bank);
			}
		}
		return banks;
	}

	private Set getAllGroupBanks(String reportId, Collection bankGroups) {
		Set retVal = new HashSet();
		Hashtable cache = getHash(allPeerBanks, reportId);
		for (Iterator iter = bankGroups.iterator(); iter.hasNext();) {
			OOPeer peer = (OOPeer) iter.next();
			Set bankSet = (Set) cache.get(peer.toString());
			if (bankSet == null) {
				bankSet = getGroupBanks(reportId, peer);
				cache.put(peer.toString(), bankSet);
			}
			retVal.addAll(bankSet);
		}
		return retVal;
	}

	private void cacheBankValuesForPctAgregate(String reportId, OONode node,
			String banks, Date periodStart, Date periodEnd, int periodFunc,
			String versionCode) throws SQLException {

		StringBuffer sql = new StringBuffer(300);
		sql.append("SELECT tr.bankid, tr.periodtypeid, ")
				.append(PeriodFunction.getSqlFunction(periodFunc))
				.append("(tr.tagr) FROM (SELECT t.bankid, t.periodtypeid, sum(t.nvalue) as tagr ")
				.append("FROM result_view t WHERE t.nodeid=? AND t.bankid in (")
				.append(banks)
				.append(") ")
				.append("AND t.nvalue is not NULL AND t.fromdate >=? AND t.todate<=? ")
				.append(versionFilter(versionCode))
				.append("GROUP BY t.bankid, t.periodtypeid, t.periodid) tr GROUP BY tr.bankid, tr.periodtypeid ORDER BY tr.bankid");

		PreparedStatement ps = getPreparedStatement(reportId, sql.toString());

		ps.setLong(1, node.getId());
		ps.setDate(2, new java.sql.Date(periodStart.getTime()));
		ps.setDate(3, new java.sql.Date(periodEnd.getTime()));

		ResultSet rs = ps.executeQuery();

		storeResultSetForPct(reportId, node, periodStart, periodEnd,
				periodFunc, versionCode, rs);
	}

	private void cacheBankValuesForPctLast(String reportId, OONode node,
			String banks, Date periodStart, Date periodEnd, int periodFunc,
			String versionCode) throws SQLException {

		StringBuffer sql = new StringBuffer(300);

		sql.append("SELECT t.bankid, t.periodtypeid, sum(nvalue) ")
				.append("FROM result_view t WHERE t.nodeid=? AND t.bankid in (")
				.append(banks)
				.append(") ")
				.append("AND t.nvalue is not NULL AND t.fromdate >=? AND t.todate=? ")
				.append(versionFilter(versionCode))
				.append("GROUP BY t.bankid, t.periodtypeid, t.periodid ORDER BY t.bankid");

		PreparedStatement ps = getPreparedStatement(reportId, sql.toString());

		ps.setLong(1, node.getId());
		ps.setDate(2, new java.sql.Date(periodStart.getTime()));
		ps.setDate(3, new java.sql.Date(periodEnd.getTime()));

		ResultSet rs = ps.executeQuery();

		storeResultSetForPct(reportId, node, periodStart, periodEnd,
				periodFunc, versionCode, rs);
	}

	private void storeResultSetForPct(String reportId, OONode node,
			Date periodStart, Date periodEnd, int periodFunc,
			String versionCode, ResultSet rs) throws SQLException {
		Long oldPeriodType = null;
		Long oldBankId = null;
		double bankVal = Double.NaN;
		while (rs.next()) {
			Long bankId = new Long(rs.getLong(1));
			Long periodType = new Long(rs.getLong(2));
			double value = rs.getDouble(3);

			if (oldBankId != null && !oldBankId.equals(bankId)) {
				storeBankValuesForPct(reportId, node.toString(), periodStart,
						periodEnd, oldBankId.intValue(), periodFunc, bankVal,
						versionCode);
				oldPeriodType = null;
			}
			oldBankId = bankId;

			if (comparePeriodTypePriorities(reportId, periodType, oldPeriodType) > 0) {
				bankVal = value;
			}
		}
		if (oldBankId != null) {
			storeBankValuesForPct(reportId, node.toString(), periodStart,
					periodEnd, oldBankId.intValue(), periodFunc, bankVal,
					versionCode);
		}
	}

	private void storeBankValuesForPct(String reportId, String node,
			Date periodStart, Date periodEnd, long bankId, int periodFunc,
			double value, String versionCode) {
		String key = generateBankValueKey(node, periodStart, periodEnd, bankId,
				periodFunc, versionCode);

		Hashtable h = getHash(bankValueForPct, reportId);
		h.put(key, new Double(value));
	}

	public double loadBankValuesForPct(String reportId, String nodeCode,
			OOPeriodPK period, long bankId, String periodFunc,
			String versionCode) {
		double retVal = Double.NaN;

		if (isAggregate(nodeCode)) {
			JSAggregateHelper js = JSAggregateHelper.pctValueHelper(this,
					reportId, bankId, period, periodFunc, versionCode);

			try {
				double d = JSAggregateHelper.evalReportAggregate(js,
						generateJSAggregate(nodeCode));
				return d;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return Double.NaN;
			}
		} else {

			String key;

			if ((PeriodFunction.getCode(periodFunc) == PeriodFunction.YAVERAGE)) {
				Date periodStart = getYearBeggining(period.getFromDate());
				Date periodEnd = period.getToDate();
				key = generateBankValueKey(nodeCode, periodStart, periodEnd,
						bankId, PeriodFunction.AVG, versionCode);
			} else if ((PeriodFunction.getCode(periodFunc) == PeriodFunction.YDTAVERAGE)) {
				Date periodStart = getYdtBeggining(period, reportId, periodFunc);
				Date periodEnd = period.getToDate();
				key = (periodStart == null) ? null : generateBankValueKey(
						nodeCode, periodStart, periodEnd, bankId,
						PeriodFunction.AVG, versionCode);
			} else {
				Date periodStart = period.getFromDate();
				Date periodEnd = period.getToDate();
				key = generateBankValueKey(nodeCode, periodStart, periodEnd,
						bankId, PeriodFunction.getCode(periodFunc), versionCode);
			}

			Double val = null;
			if (key != null) {
				val = (Double) getHash(bankValueForPct, reportId).get(key);
			}

			if (val != null) {
				retVal = val.doubleValue();
			}

			return retVal;
		}
	}

	private String generateBankValueKey(String node, Date periodStart,
			Date periodEnd, long bankId, int periodFunc, String versionCode) {
		StringBuffer key = new StringBuffer(70);
		key.append(periodFunc).append(',').append(bankId).append(',')
				.append(periodStart.getTime()).append(',')
				.append(periodEnd.getTime()).append(',').append(node)
				.append(',').append(versionCode);
		return key.toString();
	}

	private int comparePeriodTypePriorities(String reportId,
			Long firsPeriodTypId, Long secondPeriodTypId) {
		int retVal = 0;
		Hashtable h = getHash(periodTypePriority, reportId);

		Integer first = (firsPeriodTypId != null) ? (Integer) h
				.get(firsPeriodTypId) : null;

		Integer second = (secondPeriodTypId != null) ? (Integer) h
				.get(secondPeriodTypId) : null;

		if (first != null && second != null) {
			retVal = first.compareTo(second);
		} else {
			if (first == null && second != null) {
				retVal = -1;
			} else if (first != null && second == null) {
				retVal = 1;
			}
		}
		return retVal;
	}

	private PreparedStatement getPreparedStatement(String reportId, String sql)
			throws SQLException {
		Hashtable h = getHash(prepStatements, reportId);
		PreparedStatement ps = (PreparedStatement) h.get(sql);
		if (ps == null) {
			Connection con = getConnection(reportId);
			ps = con.prepareStatement(sql);
			h.put(sql, ps);

		}
		return ps;
	}

	private boolean isAggregate(String code) {
		if ((code.indexOf('/') != -1) || (code.indexOf('+') != -1)
				|| (code.indexOf('-') != -1) || (code.indexOf('*') != -1)
				|| (code.indexOf('(') != -1) || (code.indexOf(')') != -1)) {
			return true;
		}
		return false;
	}

	private Vector getAggregateCodes(String code) {
		Vector v = new Vector();

		StringTokenizer st = new StringTokenizer(code, " +-/*()");
		while (st.hasMoreTokens()) {
			v.add(st.nextToken());
		}
		return v;
	}

	private Vector getAggregateOperations(String code) {
		Vector v = new Vector();

		for (int i = 0; i < code.length(); i++) {
			char c = code.charAt(i);
			if ((c == '/') || (c == '+') || (c == '-') || (c == '*')
					|| (c == '(') || (c == ')')) {
				v.add(new Character(c));
			}
		}
		return v;
	}

	private String generateJSAggregate(String aggNodeCode) {
		String js = "return ";

		// AggregateRequest agg = (AggregateRequest)iter.next();
		// String aggNodeCode = (String)iter.next();

		java.util.Iterator citer = getAggregateCodes(aggNodeCode).iterator();
		java.util.Iterator oiter = getAggregateOperations(aggNodeCode)
				.iterator();
		char op = ' ';
		int aggCount = 0;
		for (aggCount = 0; true; aggCount++) {
			// while(true) {
			if (!citer.hasNext())
				break;
			String code = (String) citer.next();
			if (code.toLowerCase().equals("sign")) {
				js += "helper.sign";
				aggCount--;
				if (oiter.hasNext()) {
					op = ((Character) oiter.next()).charValue();
					js += op;
				}
				continue;
			}

			if (oiter.hasNext()) {
				op = ((Character) oiter.next()).charValue();
				if (op == '(') {
					js += '(';
					// } else {
					if ((!oiter.hasNext()) && (op == '(')) {
						js += "helper.getValue('" + code + "')"; // "convert(float,t"+(aggCount+1)+".value)";
						continue;
					} // else {
					if (oiter.hasNext())
						op = ((Character) oiter.next()).charValue();
				}
				if (op == ')') {

					js += "helper.getValue('" + code + "')"; // "convert(float,t"+(aggCount+1)+".value)";
					js += ')';
					if (oiter.hasNext()) {
						op = ((Character) oiter.next()).charValue();
						js += op;
					}
				} else {

					js += "helper.getValue('" + code + "')"; // "convert(float,t"+(aggCount+1)+".value)";
					js += op;
					// }
				}
			} else {
				js += "helper.getValue('" + code + "')"; // "convert(float,t"+(aggCount+1)+".value)";
			}
		}
		return js + ";\n";
	}

	private PeriodPK calcOffset(String reportID, PeriodPK pk, int offset) {
		Hashtable h = getHash(allPeriods, reportID);
		OOPeriodPK o = (OOPeriodPK) h.get(pk);
		if (o == null) {
			return null;
		}
		h = getHash(allOffsetsPK, reportID);
		h = getHash(h, o.getTypeCode());
		Integer seq = (Integer) h.get(pk);
		if (seq == null) {
			return null;
		}
		Integer _seq = new Integer(seq.intValue() + offset);
		h = getHash(allOffsetsSeq, reportID);
		h = getHash(h, o.getTypeCode());
		return (PeriodPK) h.get(_seq);
	}

	private byte[] getBlob(ResultSet rs, int columnIndex) throws IOException,
			SQLException {

		byte buff[] = null;
		if (!DatabaseUtil.isOracle()) {
			buff = rs.getBytes(columnIndex);
		} else {
			ByteArrayOutputStream st = new ByteArrayOutputStream();
			java.io.InputStream in = rs.getBinaryStream(columnIndex);
			ReportBean.copy(in, st);
			buff = st.toByteArray();
			st.close();
		}
		return buff;
	}

	private void initStage(String reportId, String stageName) {

		log.info("Report ID " + reportId + ":" + stageName);

		StageInfo stageInfo = new StageInfo();

		stageInfo.stageName = stageName;
		stageInfo.stageStartTime = System.currentTimeMillis();

		getStageInfos(reportId).add(stageInfo);
	}

	private ArrayList getStageInfos(String reportId) {

		ArrayList infos = (ArrayList) stageInfos.get(reportId);
		if (infos == null) {
			infos = new ArrayList();
			stageInfos.put(reportId, infos);
		}
		return infos;
	}

	private String versionFilter(String versionCode) {

		StringBuffer sql = new StringBuffer();

		sql.append(" AND ");

		if (versionCode == null
				|| versionCode.equalsIgnoreCase(ReportConstants.LATEST_VERSION)) {
			sql.append("t.versionCode = t.latestVersionCode ");
		} else {
			sql.append("t.versionCode = '");
			sql.append(versionCode);
			sql.append("' ");
		}

		return sql.toString();
	}

	private void logStatistics(String reportId, String title) {

		ArrayList infos = getStageInfos(reportId);
		if (infos.size() > 0) {

			log.info("/------------------------------/");
			log.info("/ " + title + " statistics");
			log.info("/------------------------------/");
			StageInfo firstStage = (StageInfo) infos.get(0);
			long currentTime = System.currentTimeMillis();
			long totalDuration = currentTime - firstStage.stageStartTime;

			for (int i = 0; i < infos.size(); i++) {

				StageInfo stage = (StageInfo) infos.get(i);
				long stageEndTime;
				if (i < getStageInfos(reportId).size() - 1) {
					stageEndTime = ((StageInfo) infos.get(i + 1)).stageStartTime;
				} else {
					stageEndTime = currentTime;
				}

				long duration = stageEndTime - stage.stageStartTime;

				StringBuffer buff = new StringBuffer();
				buff.append("Stage: \"");
				buff.append(stage.stageName);
				buff.append("\", duration: ");
				buff.append(duration);
				buff.append("ms (");
				buff.append((int) ((double) duration / totalDuration * 100));
				buff.append("%)");
				log.info(buff.toString());
			}

			StringBuffer buff = new StringBuffer();
			buff.append("Total time: ");
			buff.append(totalDuration);
			buff.append("ms, stage total count: ");
			buff.append(infos.size());
			buff.append(", stage average duration: ");
			buff.append(totalDuration / infos.size());
			buff.append("ms");

			log.info(buff.toString());

			infos.clear();
		}
	}

	private String getBankCriterionKey(String bank, String criterion) {
		return bank + " / " + criterion;
	}

	private Connection getConnection(String reportId) throws EJBException {
		Connection retVal = (Connection) connections.get(reportId);
		if (retVal == null) {
			retVal = DatabaseUtil.getConnection();
			connections.put(reportId, retVal);
		}
		return retVal;
	}

	/** Sets the given report sequence */
	public void setReportSequence(int reportId, int index) {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {

			String sql = "update out_reports set sequence = ? where id = ?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, index);
			ps.setInt(2, reportId);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}
}

class StageInfo {

	public String stageName;

	public long stageStartTime;
}

class OONode {

	private String code;
	private String name;
	private long id;

	OONode(String s) {
		code = s;
	}

	void setName(String name) {
		this.name = name;
	}

	public void setId(long id) {
		this.id = id;
	}

	String getName() {
		return name;
	}

	public long getId() {
		return id;
	}

	public String toString() {
		return code;
	}

	public boolean equals(Object o) {
		return o.toString().equals(code);
	}

	public int hashCode() {
		return code.hashCode();
	}
}

class OOPeer {

	private String code;
	private String name;
	private long id;

	OOPeer(String s) {
		code = s;
	}

	void setName(String name) {
		this.name = name;
	}

	String getName() {
		return name;
	}

	void setId(long id) {
		this.id = id;
	}

	long getId() {
		return this.id;
	}

	public String toString() {
		return code;
	}

	public boolean equals(Object o) {
		return o.toString().equals(code);
	}

	public int hashCode() {
		return code.hashCode();
	}
}

class OOBank {

	private String code;
	private String name;
	private long id;

	OOBank(String s) {
		code = s;
	}

	void setName(String name) {
		this.name = name;
	}

	String getName() {
		return name;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String toString() {
		return code;
	}

	public boolean equals(Object o) {
		return o.toString().equals(code);
	}

	public int hashCode() {
		return code.hashCode();
	}
}

class PctParameters {
	String bankCode;
	String peerCode;
	String versionCode;
	String nodeCode;
	int period;
	String periodFunction;
	int periodOffset;
}

class AggregateRequest {
	String nodeCode;
	String peerBankCode;
	String peerFuncrion;
	PeriodPK periodPK;
	String periodFunction;
	int offset;
}

class PctFuncContainer {
	String periodFunction;
	int periodFunc;
	OONode node;
	OOPeriodPK period;
	Set banks = new HashSet();
	Set peers = new HashSet();
	String bankList;
	String versionCode;

	PctFuncContainer(String periodFunction, int periodFunc, OONode node,
			OOBank bank, OOPeer peer, OOPeriodPK period, String versionCode) {
		this.periodFunction = periodFunction;
		this.periodFunc = periodFunc;
		this.node = node;
		this.period = period;
		this.banks.add(bank);
		this.peers.add(peer);
		this.versionCode = versionCode;
	}

	void add(OOBank bank, OOPeer peer) {
		this.banks.add(bank);
		this.peers.add(peer);
	}
}

class FinaFunctionComparator implements Comparator {
	static FinaFunctionComparator comparator = new FinaFunctionComparator();

	final static String ALL_BANK_VALUE = "allbanksvalue";
	final static String BANK_VALUE = "bankvalue";
	final static String SEL_BANK_VALUE = "selbankvalue";

	final static String ALL_BANK_VALUE_VER = "allbanksvaluever";
	final static String BANK_VALUE_VER = "bankvaluever";
	final static String SEL_BANK_VALUE_VER = "selbanksvaluever";
	final static String TEXT_VALUE_VER = "textvaluever";
	final static String PEER_VALUE_VER = "peervaluever";

	public int compare(Object o1, Object o2) {

		FinaFunction f1 = (FinaFunction) o1;
		FinaFunction f2 = (FinaFunction) o2;

		String f1Name = f1.getFunctionName();
		String f2Name = f2.getFunctionName();

		if (f1Name.equals(f2Name)) {
			if (f1Name.equals(BANK_VALUE)) {
				return compBankValue(f1, f2);
			} else if (f1Name.equals(ALL_BANK_VALUE)) {
				return compAllBankValue(f1, f2);
			} else if (f1Name.equals(SEL_BANK_VALUE)) {
				return compSelBankValue(f1, f2);
			} else if (f1Name.equals(BANK_VALUE_VER)) {
				return compBankValueVer(f1, f2);
			} else if (f1Name.equals(ALL_BANK_VALUE_VER)) {
				return compAllBankValueVer(f1, f2);
			} else if (f1Name.equals(SEL_BANK_VALUE_VER)) {
				return compSelBankValueVer(f1, f2);
			} else if (f1Name.equals(TEXT_VALUE_VER)) {
				return compTextValueVer(f1, f2);
			} else if (f1Name.equals(PEER_VALUE_VER)) {
				return compPeerValueVer(f1, f2);
			}
		}
		return f1Name.compareTo(f2Name);
	}

	private int compBankValue(FinaFunction f1, FinaFunction f2) {

		String periodFunction1 = (String) f1.getArguments()[3];
		String periodFunction2 = (String) f2.getArguments()[3];

		return periodFunction1.compareTo(periodFunction2);
	}

	private int compBankValueVer(FinaFunction f1, FinaFunction f2) {

		String version1 = (String) f1.getArguments()[5];
		String version2 = (String) f2.getArguments()[5];

		if (compBankValue(f1, f2) == 0) {
			return version1.compareTo(version2);
		} else {
			return compBankValue(f1, f2);
		}
	}

	private int compAllBankValue(FinaFunction f1, FinaFunction f2) {

		String periodFunction1 = (String) f1.getArguments()[3];
		String periodFunction2 = (String) f2.getArguments()[3];

		String banksFunction1 = (String) f1.getArguments()[1];
		String banksFunction2 = (String) f2.getArguments()[1];

		if (periodFunction1.compareTo(periodFunction2) == 0) {
			return banksFunction1.compareTo(banksFunction2);
		} else {
			return periodFunction1.compareTo(periodFunction2);
		}
	}

	private int compAllBankValueVer(FinaFunction f1, FinaFunction f2) {

		String version1 = (String) f1.getArguments()[5];
		String version2 = (String) f2.getArguments()[5];

		if (compAllBankValue(f1, f2) == 0) {
			return version1.compareTo(version2);
		} else {
			return compAllBankValue(f1, f2);
		}
	}

	private int compSelBankValue(FinaFunction f1, FinaFunction f2) {

		String banksParameterName1 = (String) f1.getArguments()[1];
		String banksParameterName2 = (String) f2.getArguments()[1];

		String banksFunction1 = (String) f1.getArguments()[2];
		String banksFunction2 = (String) f2.getArguments()[2];

		if (banksFunction1.compareTo(banksFunction2) == 0) {
			return banksParameterName1.compareTo(banksParameterName2);
		} else {
			return banksFunction1.compareTo(banksFunction2);
		}
	}

	private int compSelBankValueVer(FinaFunction f1, FinaFunction f2) {

		String version1 = (String) f1.getArguments()[6];
		String version2 = (String) f2.getArguments()[6];

		if (compSelBankValue(f1, f2) == 0) {
			return version1.compareTo(version2);
		} else {
			return compSelBankValue(f1, f2);
		}
	}

	private int compTextValueVer(FinaFunction f1, FinaFunction f2) {

		String version1 = (String) f1.getArguments()[4];
		String version2 = (String) f2.getArguments()[4];

		return version1.compareTo(version2);
	}

	private int compPeerValueVer(FinaFunction f1, FinaFunction f2) {

		String version1 = (String) f1.getArguments()[6];
		String version2 = (String) f2.getArguments()[6];

		return version1.compareTo(version2);
	}

}
