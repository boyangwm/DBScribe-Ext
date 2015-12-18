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
import java.util.HashMap;
import java.util.Iterator;

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
import fina2.i18n.LocaleUtil;
import fina2.reportoo.ReportConstants;
import fina2.reportoo.ReportInfo;
import fina2.security.User;
import fina2.security.UserHome;
import fina2.security.UserPK;

public class StoredReportsSessionBean implements SessionBean {

	SessionContext sessionContext;
	private static Logger log = Logger
			.getLogger(StoredReportsSessionBean.class);

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

	public byte[] getStoredReport(LanguagePK langPK, ReportPK pk, int hashCode) {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean found = false;
		byte[] reportResult = null;
		try {
			con = DatabaseUtil.getConnection();

			String sql = "select a.info, a.reportResult, b.namestrid, c.name "
					+ "from OUT_STORED_REPORTS a, out_reports b, sys_languages c "
					+ "where reportID=? and langID=? and hashCode=? "
					+ "and a.reportid = b.id and a.langid = c.id";

			ps = con.prepareStatement(sql);
			ps.setInt(1, pk.getId());
			ps.setInt(2, langPK.getId());
			ps.setInt(3, hashCode);
			rs = ps.executeQuery();

			String reportName = null;
			String langName = null;

			if (rs.next()) {
				reportResult = DatabaseUtil.getBlob(rs, "reportResult");
				reportName = LocaleUtil.getString(con, langPK, rs.getInt(3));
				langName = rs.getString(4);

				found = true;
			}

			/* Logging */
			if (found) {
				String msg = "Getting stored report. " + "User: "
						+ this.sessionContext.getCallerPrincipal().getName()
						+ ", Report: " + reportName + ", Language: " + langName
						+ ", HashCode: " + hashCode;

				log.info(msg);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return reportResult;
	}

	public ReportInfo getStoredReportInfo(LanguagePK langPK, ReportPK pk,
			int hashCode) {

		Connection con = null;
		ReportInfo reportInfo = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DatabaseUtil.getConnection();
			ps = con
					.prepareStatement("select info, reportResult from OUT_STORED_REPORTS "
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

	public void storeReport(ReportPK reportPK, LanguagePK langPK,
			UserPK userPK, ReportInfo info, byte[] report) {

		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DatabaseUtil.getConnection();

			ps = con.prepareStatement("delete from OUT_STORED_REPORTS "
					+ "where reportID=? and langID=? and hashCode=?");
			ps.setInt(1, reportPK.getId());
			ps.setInt(2, langPK.getId());
			ps.setInt(3, info.hashCode());
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			ps = con
					.prepareStatement("insert into OUT_STORED_REPORTS "
							+ "(reportID,langID,userID,hashCode,storeDate,info,reportResult) values (?,?,?,?,?,?,?)");
			ps.setInt(1, reportPK.getId());
			ps.setInt(2, langPK.getId());
			ps.setInt(3, userPK.getId());
			ps.setInt(4, info.hashCode());
			ps.setTimestamp(5, new java.sql.Timestamp(System
					.currentTimeMillis()));

			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(bo);
			oo.writeObject(info);

			byte[] infoBytes = bo.toByteArray();
			DatabaseUtil.setBlob(ps, infoBytes, 6);

			oo.close();
			bo.close();

			DatabaseUtil.setBlob(ps, report, 7);

			ps.executeUpdate();
		} catch (Exception ex) {
			log.error("Error during storing report", ex);
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	public StoredReportInfo getStoredReports(LanguagePK langPK, UserPK userPK)
			throws FinaTypeException {
		Connection con = null;
		try {

			User user = getUser(userPK);
			if (!user.hasPermission("fina2.reports.stored.manager")) {
				throw new FinaTypeException(Type.PERMISSIONS_DENIED,
						new String[] { "fina2.reports.stored.manager" });
			}

			HashMap storedReportsMap = new HashMap();

			StoredReportInfo root = new StoredReportInfo();
			root.setReportId(0);
			root.setName("        ");
			root.setFolder(true);
			storedReportsMap.put(new Integer(0), root);

			con = DatabaseUtil.getConnection();

			getFolders(langPK, con, storedReportsMap);
			getReports(langPK, con, storedReportsMap);

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

	private User getUser(UserPK userPK) throws Exception {
		InitialContext jndi = new InitialContext();
		Object ref = jndi.lookup("fina2/security/User");
		UserHome userHome = (UserHome) PortableRemoteObject.narrow(ref,
				UserHome.class);
		return userHome.findByPrimaryKey(userPK);
	}

	private void removeUnnecessaryFolders(StoredReportInfo parent) {
		ArrayList childrens = parent.getChildren();

		for (Iterator iter = childrens.iterator(); iter.hasNext();) {
			StoredReportInfo storedReport = (StoredReportInfo) iter.next();

			if (storedReport.isFolder()) {

				removeUnnecessaryFolders(storedReport);
				if (storedReport.getChildren().size() == 0) {
					iter.remove();
				}
			}
		}
	}

	private void getReports(LanguagePK langPK, Connection con,
			HashMap storedReportsMap) throws SQLException {

		PreparedStatement pstmt = con
				.prepareStatement("select oo.parentId, osr.reportId, osr.hashCode, osr.langID, "
						+ " ss.value, osr.storeDate, ss2.value, lang.name,ss.id,ss2.id "
						+ " from OUT_STORED_REPORTS osr, OUT_REPORTS oo, SYS_STRINGS ss, "
						+ " SYS_STRINGS ss2, SYS_LANGUAGES lang, SYS_USERS su "
						+ " where osr.reportId = oo.id and oo.nameStrId = ss.id and (ss.langID=?) and "
						+ " osr.userID = su.id and su.nameStrID = ss2.id and (ss2.langID=? ) and "
						+ " osr.langID = lang.id "
						+ " order by osr.reportId, osr.storeDate DESC, osr.hashCode, ss.langID DESC, ss2.langID DESC, ss.value");

		pstmt.setInt(1, langPK.getId());
		pstmt.setInt(2, langPK.getId());

		ResultSet rs = pstmt.executeQuery();

		int prevReportId = -1, prevHashCode = -1, prevLangId = -1;
		while (rs.next()) {
			int parentId = rs.getInt(1);
			int reportId = rs.getInt(2);
			int hashCode = rs.getInt(3);
			int langId = rs.getInt(4);
			String name = LocaleUtil.getString(con, langPK, rs.getInt(9));//rs.getString(5);
			java.util.Date storeDate = new java.util.Date(rs.getTimestamp(6)
					.getTime());
			String creatorUser = LocaleUtil.getString(con, langPK, rs.getInt(10));//rs.getString(7);
			String langName = rs.getString(8);

			if (prevReportId == reportId && prevHashCode == hashCode
					&& prevLangId == langId) {
				continue;
			} else {
				prevReportId = reportId;
				prevHashCode = hashCode;
				prevLangId = langId;

			}

			StoredReportInfo reportInfo = new StoredReportInfo();
			reportInfo.setName(name);
			reportInfo.setStoreDate(storeDate);
			reportInfo.setCreatorUser(creatorUser);
			reportInfo.setLanguageName(langName);
			reportInfo.setFolder(false);
			reportInfo.setReportId(reportId);
			reportInfo.setReportInfoHashCode(hashCode);
			reportInfo.setLangId(langId);

			StoredReportInfo parent = (StoredReportInfo) storedReportsMap
					.get(new Integer(parentId));

			if (parent != null) {
				parent.getChildren().add(reportInfo);
			}
		}
	}

	private void getFolders(LanguagePK langPK, Connection con,
			HashMap storedReportsMap) throws SQLException {

		PreparedStatement pstmt = con
				.prepareStatement("select oo.id, oo.parentID, ss.value, ss.langID,ss.id from OUT_REPORTS oo left outer join SYS_STRINGS ss "
						+ " on oo.nameStrID=ss.id and (ss.langID=?) and oo.type="
						+ ReportConstants.NODETYPE_FOLDER
						+ " order by oo.parentID, oo.id, ss.langID DESC, ss.value");

		pstmt.setInt(1, langPK.getId());

		ResultSet rs = pstmt.executeQuery();

		int prevId = -1;
		while (rs.next()) {
			int id = rs.getInt(1);
			int parentId = rs.getInt(2);
			String name =LocaleUtil.getString(con, langPK, rs.getInt(5));
             if(name==null)
            	 name="NONAME";
			if (prevId == id) {
				continue;
			} else {
				prevId = id;
			}

			StoredReportInfo reportInfo = new StoredReportInfo();
			reportInfo.setReportId(id);
			reportInfo.setName(name);
			reportInfo.setFolder(true);

			StoredReportInfo parent = (StoredReportInfo) storedReportsMap
					.get(new Integer(parentId));

			if (parent != null) {
				parent.getChildren().add(reportInfo);
			}

			storedReportsMap.put(new Integer(id), reportInfo);
		}
	}

	public void deleteStoredReports(Collection storedReports) {

		Connection con = null;
		try {
			con = DatabaseUtil.getConnection();

			deleteFolders(storedReports, con);
			deleteReports(storedReports, con);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	private void deleteFolders(Collection storedReports, Connection con)
			throws SQLException {

		for (Iterator iter = storedReports.iterator(); iter.hasNext();) {

			StoredReportInfo storedReport = (StoredReportInfo) iter.next();
			if (storedReport.isFolder()) {

				ArrayList reportIds = new ArrayList();
				getReportIds(storedReport, reportIds);

				StringBuffer ids = new StringBuffer();
				for (Iterator idsIter = reportIds.iterator(); idsIter.hasNext();) {

					Integer id = (Integer) idsIter.next();
					ids.append(id);
					ids.append(",");
				}

				if (ids.length() > 0) {

					ids.setLength(ids.length() - 1);
					PreparedStatement pstmt = con
							.prepareStatement("delete from OUT_STORED_REPORTS where reportID in ("
									+ ids + ")");

					pstmt.executeUpdate();
					DatabaseUtil.closeStatement(pstmt);
				}
			}
		}
	}

	private void getReportIds(StoredReportInfo reportInfo, Collection ids) {

		if (reportInfo.isFolder()) {

			for (Iterator iter = reportInfo.getChildren().iterator(); iter
					.hasNext();) {
				StoredReportInfo child = (StoredReportInfo) iter.next();
				getReportIds(child, ids);
			}
		} else {
			ids.add(new Integer(reportInfo.getReportId()));
		}
	}

	private void deleteReports(Collection storedReports, Connection con)
			throws SQLException {

		for (Iterator iter = storedReports.iterator(); iter.hasNext();) {

			StoredReportInfo storedReport = (StoredReportInfo) iter.next();
			if (!storedReport.isFolder()) {

				/*
				 * Logging
				 */

				String sql = "select b.namestrid, c.name "
						+ "from OUT_STORED_REPORTS a, out_reports b, sys_languages c "
						+ "where reportID=? and langID=? and hashCode=? "
						+ "and a.reportid = b.id and a.langid = c.id";

				PreparedStatement ps = con.prepareStatement(sql);

				ps.setInt(1, storedReport.getReportId());
				ps.setInt(2, storedReport.getLangId());
				ps.setInt(3, storedReport.getReportInfoHashCode());

				ResultSet rs = ps.executeQuery();

				String reportName = null;
				String langName = null;

				if (rs.next()) {
					reportName = LocaleUtil.getString(con, new LanguagePK(
							storedReport.getLangId()), rs.getInt(1));
					langName = rs.getString(2);
				}

				rs.close();
				ps.close();

				String msg = "Deleting stored report. " + "User: "
						+ sessionContext.getCallerPrincipal().getName()
						+ ", Report: " + reportName + ", Language: "
						+ langName.trim() + ", HashCode: "
						+ storedReport.getReportInfoHashCode();

				log.info(msg);

				/*
				 * Deleting
				 */

				sql = "delete from OUT_STORED_REPORTS "
						+ " where reportID=? and hashCode=? and langID=?";

				ps = con.prepareStatement(sql);

				ps.setInt(1, storedReport.getReportId());
				ps.setInt(2, storedReport.getReportInfoHashCode());
				ps.setInt(3, storedReport.getLangId());

				ps.executeUpdate();
				DatabaseUtil.closeStatement(ps);
			}
		}
	}
}
