package fina2.reportoo.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.apache.log4j.Logger;

import fina2.FinaTypeException;
import fina2.FinaTypeException.Type;
import fina2.db.DatabaseUtil;
import fina2.i18n.LanguagePK;
import fina2.reportoo.ReportConstants;
import fina2.reportoo.ReportInfo;
import fina2.security.User;
import fina2.security.UserHome;
import fina2.security.UserPK;

public class ReportsSchedulerSessionBean implements SessionBean {
	SessionContext sessionContext;
	private static Logger log = Logger
			.getLogger(ReportsSchedulerSessionBean.class);
	private static Map processingReports = Collections
			.synchronizedMap(new HashMap());
	private static final long MINUTE = 60000;

	public void ejbCreate() throws CreateException {
	}

	public void ejbRemove() {
	}

	public void ejbActivate() {
	}

	public void ejbPassivate() {
	}

	public void setSessionContext(SessionContext sessionContext) {
		this.sessionContext = sessionContext;
	}

	public boolean canProcess(ScheduledReportInfo scheduleInfo, Date clientTime) {

		boolean canProcess = true;

		if (Math.abs(System.currentTimeMillis() - clientTime.getTime()) > 1 * MINUTE) {
			canProcess = false;
		} else {
			synchronized (processingReports) {
				if (isProcessing(scheduleInfo)) {
					canProcess = false;
				} else {
					processing(scheduleInfo);
				}
			}
		}
		return canProcess;
	}

	public void processing(ScheduledReportInfo scheduleInfo) {

		processingReports.put(scheduleInfo, new Date());
	}

	private boolean isProcessing(ScheduledReportInfo scheduleInfo) {

		boolean processing = false;

		Date processTime = (Date) processingReports.get(scheduleInfo);
		if (processTime != null) {

			if (System.currentTimeMillis() - processTime.getTime() > 2 * MINUTE) {
				processingReports.remove(scheduleInfo);
			} else {
				processing = true;
			}
		}
		return processing;
	}

	public ReportInfo getScheduledReportInfo(LanguagePK langPK, ReportPK pk,
			int hashCode) {

		Connection con = null;
		ReportInfo reportInfo = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("select info from OUT_REPORTS_SCHEDULE "
					+ "where reportID=? and langID=? and hashCode=?");
			ps.setInt(1, pk.getId());
			ps.setInt(2, langPK.getId());
			ps.setInt(3, hashCode);
			rs = ps.executeQuery();

			if (rs.next()) {

				byte[] buf = DatabaseUtil.getBlob(rs, "info");

				ByteArrayInputStream bi = new ByteArrayInputStream(buf);
				ObjectInputStream oi = new ObjectInputStream(bi);
				reportInfo = (ReportInfo) oi.readObject();
				oi.close();
				bi.close();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return reportInfo;
	}

	public void deleteScheduledReports(Collection scheduledReports) {
		Connection con = null;
		try {
			con = DatabaseUtil.getConnection();
			deleteFolders(scheduledReports, con);
			deleteReports(scheduledReports, con);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public void scheduleReport(fina2.i18n.LanguagePK langPK, ReportPK pk,
			UserPK userPK, Date scheduleDate, boolean onDemand, ReportInfo info) {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con
					.prepareStatement("insert into OUT_REPORTS_SCHEDULE "
							+ "(reportID, langID, info, hashCode, status, scheduleTime, onDemand, userID) "
							+ "values (?,?,?,?,?,?,?,?)");
			ps.setInt(1, pk.getId());
			ps.setInt(2, langPK.getId());

			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(bo);
			oo.writeObject(info);

			byte[] buff = bo.toByteArray();
			DatabaseUtil.setBlob(ps, buff, 3);

			oo.close();
			bo.close();

			ps.setInt(4, info.hashCode());
			ps.setInt(5, ScheduledReportInfo.STATUS_SCHEDULED);

			if (scheduleDate != null && !onDemand)
				ps.setTimestamp(6, new java.sql.Timestamp(scheduleDate
						.getTime()));
			else
				ps.setNull(6, java.sql.Types.TIMESTAMP);

			ps.setInt(7, onDemand ? 1 : 0);
			ps.setInt(8, userPK.getId());
			ps.executeUpdate();
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	public void setStatus(ScheduledReportInfo scheduledReport, int status) {

		Connection con = null;
		PreparedStatement ps = null;
		try {
			processingReports.remove(scheduledReport);

			String extSql = null;
			if (scheduledReport.isOnDemand()) {
				extSql = " and onDemand = 1";
			} else {
				extSql = " and scheduleTime =?";
			}

			con = DatabaseUtil.getConnection();
			ps = con
					.prepareStatement("update OUT_REPORTS_SCHEDULE set status=? "
							+ "where reportID=? and langID=? and hashCode=?"
							+ extSql);

			ps.setInt(1, status);
			ps.setInt(2, scheduledReport.getReportId());
			ps.setInt(3, scheduledReport.getLangId());
			ps.setInt(4, scheduledReport.getReportInfoHashCode());
			if (!scheduledReport.isOnDemand()) {
				ps.setTimestamp(5, new java.sql.Timestamp(scheduledReport
						.getScheduleTime().getTime()));
			}
			ps.executeUpdate();
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	private void deleteFolders(Collection scheduledReports, Connection con)
			throws SQLException {

		for (Iterator iter = scheduledReports.iterator(); iter.hasNext();) {

			ScheduledReportInfo scheduledReport = (ScheduledReportInfo) iter
					.next();
			if (scheduledReport.isFolder()) {

				ArrayList reportIds = new ArrayList();
				getReportIds(scheduledReport, reportIds);

				StringBuffer ids = new StringBuffer();
				for (Iterator idsIter = reportIds.iterator(); idsIter.hasNext();) {

					Integer id = (Integer) idsIter.next();
					ids.append(id);
					ids.append(",");
				}

				if (ids.length() > 0) {

					ids.setLength(ids.length() - 1);
					PreparedStatement pstmt = con
							.prepareStatement("delete from OUT_REPORTS_SCHEDULE where reportID in ("
									+ ids + ")");

					pstmt.executeUpdate();
					DatabaseUtil.closeStatement(pstmt);
				}
			}
		}
	}

	private void getReportIds(ScheduledReportInfo reportInfo, Collection ids) {

		if (reportInfo.isFolder()) {

			for (Iterator iter = reportInfo.getChildren().iterator(); iter
					.hasNext();) {
				ScheduledReportInfo child = (ScheduledReportInfo) iter.next();
				getReportIds(child, ids);
			}
		} else {
			ids.add(new Integer(reportInfo.getReportId()));
		}
	}

	private void deleteReports(Collection scheduledReports, Connection con)
			throws SQLException {

		for (Iterator iter = scheduledReports.iterator(); iter.hasNext();) {

			ScheduledReportInfo scheduledReport = (ScheduledReportInfo) iter
					.next();
			if (!scheduledReport.isFolder()) {

				String extSql = null;
				if (scheduledReport.isOnDemand()) {
					extSql = " and onDemand = 1";
				} else {
					extSql = " and scheduleTime =?";
				}

				PreparedStatement pstmt = con
						.prepareStatement("delete from OUT_REPORTS_SCHEDULE "
								+ " where reportID=? and hashCode=? and langID=?"
								+ extSql);

				pstmt.setInt(1, scheduledReport.getReportId());
				pstmt.setInt(2, scheduledReport.getReportInfoHashCode());
				pstmt.setInt(3, scheduledReport.getLangId());
				if (!scheduledReport.isOnDemand()) {
					pstmt.setTimestamp(4, new java.sql.Timestamp(
							scheduledReport.getScheduleTime().getTime()));
				}

				pstmt.executeUpdate();
				DatabaseUtil.closeStatement(pstmt);
			}
		}
	}

	public ScheduledReportInfo getScheduledReports(LanguagePK langPK,
			UserPK userPK) throws fina2.FinaTypeException {

		Connection con = null;

		try {
			User user = getUser(userPK);
			if (!user.hasPermission("fina2.reports.scheduler.manager"))
				throw new FinaTypeException(Type.PERMISSIONS_DENIED,
						new String[] { "fina2.reports.scheduler.manager" });
			if (!user.hasPermission("fina2.reports.scheduler.add"))
				throw new FinaTypeException(Type.PERMISSIONS_DENIED,
						new String[] { "fina2.reports.scheduler.add" });

			HashMap scheduledReportsMap = new HashMap();

			ScheduledReportInfo root = new ScheduledReportInfo();
			root.setReportId(0);
			root.setName("        ");
			root.setFolder(true);
			scheduledReportsMap.put(new Integer(0), root);

			con = DatabaseUtil.getConnection();

			getFolders(langPK, con, scheduledReportsMap);
			getReports(langPK, con, scheduledReportsMap, user);

			removeUnnecessaryFolders(root);

			return root;
		} catch (FinaTypeException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	private void removeUnnecessaryFolders(ScheduledReportInfo parent) {

		ArrayList childrens = parent.getChildren();

		for (Iterator iter = childrens.iterator(); iter.hasNext();) {
			ScheduledReportInfo scheduleddReport = (ScheduledReportInfo) iter
					.next();

			if (scheduleddReport.isFolder()) {

				removeUnnecessaryFolders(scheduleddReport);
				if (scheduleddReport.getChildren().size() == 0) {
					iter.remove();
				}
			}
		}
	}

	private void getReports(LanguagePK langPK, Connection con,
			HashMap scheduledReportsMap, User user) throws Exception {

		boolean schedulerManager = user
				.hasPermission("fina2.reports.scheduler.manager");

		StringBuffer buff = new StringBuffer();

		buff
				.append("select oo.parentId, ors.reportId, ors.hashCode, ors.langID,");
		buff
				.append(" ss.value, ors.scheduleTime, ors.onDemand, ors.status, ors.userID, ss2.value, lang.name ");
		buff
				.append(" from OUT_REPORTS_SCHEDULE ors, OUT_REPORTS oo, SYS_STRINGS ss, ");
		buff.append(" SYS_STRINGS ss2, SYS_LANGUAGES lang, SYS_USERS su ");
		buff
				.append(" where ors.reportId = oo.id and oo.nameStrId = ss.id and (ss.langID=? or ss.langID=1) and ");
		buff
				.append(" ors.userID = su.id and su.nameStrID = ss2.id and (ss2.langID=? or ss2.langID=1) and ");
		buff.append(" ors.langID = lang.id ");

		if (!schedulerManager) {
			buff.append(" and ors.userID=?");
		}

		buff
				.append(" order by ors.reportId, ors.scheduleTime DESC, ors.hashCode, ors.onDemand, ss.langID DESC, ss2.langID DESC, ss.value");

		PreparedStatement pstmt = con.prepareStatement(buff.toString());

		pstmt.setInt(1, langPK.getId());
		pstmt.setInt(2, langPK.getId());

		if (!schedulerManager) {

			pstmt.setInt(3, ((UserPK) user.getPrimaryKey()).getId());
		}

		ResultSet rs = pstmt.executeQuery();

		int prevReportId = -1, prevHashCode = -1, prevLangId = -1, prevOnDemand = -1;
		java.sql.Timestamp prevScheduleTime = null;

		while (rs.next()) {
			int parentId = rs.getInt(1);
			int reportId = rs.getInt(2);
			int hashCode = rs.getInt(3);
			int langId = rs.getInt(4);
			String name = rs.getString(5);
			java.sql.Timestamp scheduleTime = rs.getTimestamp(6);
			int onDemand = rs.getInt(7);
			int status = rs.getInt(8);
			int userID = rs.getInt(9);
			String creatorUser = rs.getString(10);
			String langName = rs.getString(11);

			if (prevReportId == reportId
					&& prevHashCode == hashCode
					&& prevLangId == langId
					&& compareScheduledTimes(prevOnDemand, prevScheduleTime,
							onDemand, scheduleTime)) {
				continue;
			} else {
				prevReportId = reportId;
				prevHashCode = hashCode;
				prevOnDemand = onDemand;
				prevLangId = langId;
				prevScheduleTime = scheduleTime;
			}

			ScheduledReportInfo reportInfo = new ScheduledReportInfo();
			reportInfo.setName(name);
			reportInfo.setOnDemand(onDemand == 1 ? true : false);
			reportInfo.setStatus(status);
			reportInfo.setUserId(userID);
			reportInfo.setCreatorUser(creatorUser);
			reportInfo.setLanguageName(langName);
			reportInfo.setFolder(false);
			reportInfo.setReportId(reportId);
			reportInfo.setReportInfoHashCode(hashCode);
			reportInfo.setLangId(langId);
			if (scheduleTime != null) {
				reportInfo.setScheduleTime(new Date(scheduleTime.getTime()));
			}

			if (isProcessing(reportInfo)) {
				reportInfo.setStatus(ScheduledReportInfo.STATUS_PROCESSING);
			}

			ScheduledReportInfo parent = (ScheduledReportInfo) scheduledReportsMap
					.get(new Integer(parentId));

			if (parent != null) {
				parent.getChildren().add(reportInfo);
			}
		}
	}

	private User getUser(UserPK userPK) throws Exception {

		InitialContext jndi = new InitialContext();
		Object ref = jndi.lookup("fina2/security/User");
		UserHome userHome = (UserHome) PortableRemoteObject.narrow(ref,
				UserHome.class);

		return userHome.findByPrimaryKey(userPK);
	}

	private boolean compareScheduledTimes(int onDemand1, Date scheduleTime1,
			int onDemand2, Date scheduleTime2) {

		boolean equal = false;

		if (onDemand1 == onDemand2
				&& (onDemand1 == 1 || scheduleTime1.equals(scheduleTime2))) {
			equal = true;
		}

		return equal;
	}

	private void getFolders(LanguagePK langPK, Connection con,
			HashMap scheduledReportsMap) throws SQLException {

		PreparedStatement pstmt = con
				.prepareStatement("select oo.id, oo.parentID, ss.value, ss.langID from OUT_REPORTS oo, SYS_STRINGS ss "
						+ " where ss.id=oo.nameStrID and (ss.langID=? or ss.langID=1) and oo.type="
						+ ReportConstants.NODETYPE_FOLDER
						+ " order by oo.parentID, oo.id, ss.langID DESC, ss.value");

		pstmt.setInt(1, langPK.getId());

		ResultSet rs = pstmt.executeQuery();

		int prevId = -1;
		while (rs.next()) {
			int id = rs.getInt(1);
			int parentId = rs.getInt(2);
			String name = rs.getString(3);

			if (prevId == id) {
				continue;
			} else {
				prevId = id;
			}

			ScheduledReportInfo reportInfo = new ScheduledReportInfo();
			reportInfo.setReportId(id);
			reportInfo.setName(name);
			reportInfo.setFolder(true);

			ScheduledReportInfo parent = (ScheduledReportInfo) scheduledReportsMap
					.get(new Integer(parentId));

			if (parent != null) {
				parent.getChildren().add(reportInfo);
			}
			scheduledReportsMap.put(new Integer(id), reportInfo);
		}
	}
}
