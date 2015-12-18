package fina2.security;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.log4j.Logger;

import fina2.db.DatabaseUtil;

/**
 * RoleSessionBean class
 */
public class RoleSessionBean implements SessionBean {

	/* Not necessary in the current implementation */
	public void ejbCreate() {
	}

	public void ejbActivate() {
	}

	public void ejbPassivate() {
	}

	public void ejbRemove() {
	}

	/** The session context */
	private SessionContext sessionContext;

	/** For logging */
	private Logger log = Logger.getLogger(RoleSessionBean.class);

	/** Sets the session context */
	public void setSessionContext(SessionContext context)
			throws RemoteException {
		sessionContext = context;
	}

	/** Returns the list of given role permissions */
	public List<SecurityItem> getRolePermissions(RolePK rolePK,
			Handle languageHandle) throws RemoteException, EJBException {

        /* The result list.
         * At first it contains all permissions. Then the given role permissions
         * will be retrieved and set in the result list.
		 */
		List<SecurityItem> permissions = (new UserSessionBean())
				.getAllPermissions(languageHandle);

		int roleId = rolePK.getId();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String idList = "";
		try {
			/* Selecting the role permissions */
			String sql = "select a.permissionid from sys_role_permissions a where a.roleid = ?";

			ps = con.prepareStatement(sql);
			ps.setInt(1, roleId);

			rs = ps.executeQuery();

			/* Looping the role permissions */
			while (rs.next()) {
				/* The given role has some permissions */

				int id = rs.getInt(1);
				idList += id + ", ";

				/* Looking for this id in permission list */
				for (SecurityItem item : permissions) {
					if (item.getId() == id) {
						/* Found. This role has this permission */
						item.setReview(true);
						break;
					}
				}
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		/* Log information */
		printLog("Getting role permissions", roleId, "Permission id(s): "
				+ idList);

		/* The result list */
		return permissions;
	}

	/** Returns a given role users */
	public List<SecurityItem> getRoleUsers(RolePK rolePK, Handle languageHandle)
			throws RemoteException, EJBException {

		/*
		 * The result list. At first it contains all permissions. Then the given
		 * role permissions will be retrieved and set in the result list.
		 */
		List<SecurityItem> users = (new UserSessionBean())
				.getAllUsers(languageHandle);

		int roleId = rolePK.getId();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String idList = "";

		try {
			/* Selecting the role users */
			String sql = "select a.userid from sys_users_roles a where a.roleid = ?";

			ps = con.prepareStatement(sql);
			ps.setInt(1, roleId);
			rs = ps.executeQuery();

			/* Looping the role users */
			while (rs.next()) {
				/* The given role has some users */

				int id = rs.getInt(1);
				idList += id + ", ";

				/* Looking for this id in users list */
				for (SecurityItem item : users) {
					if (item.getId() == id) {
						/* Found. This role has this user */
						item.setReview(true);
						break;
					}
				}
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		/* Log information */
		printLog("Getting role users", roleId, "Users id(s): " + idList);

		/* The result list */
		return users;
	}

	/** Returns a role return versions */
	public List<SecurityItem> getRoleReturnVersions(RolePK rolePK,
			Handle languageHandle) throws RemoteException, EJBException {

		/*
		 * The result list. At first it contains all return versions. Then a
		 * given role return versions are retrieved and set in the result list.
		 */
		List<SecurityItem> versions = (new UserSessionBean())
				.getAllReturnVersions(languageHandle);

		int roleId = rolePK.getId();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String idList = "";

		try {
			/* Selecting the role return versions */
			String sql = "select a.version_id, a.can_amend "
					+ "from sys_role_return_versions a "
					+ "where a.role_id = ? ";

			ps = con.prepareStatement(sql);
			ps.setInt(1, roleId);
			rs = ps.executeQuery();

			/* Looping through the role's return versions */
			while (rs.next()) {
				/* The given role has some return versions */

				int id = rs.getInt(1);
				idList += id + ", ";

				/* Looking for this id in versions list */
				for (SecurityItem item : versions) {
					if (item.getId() == id) {
						/* Found. This role has this return version */
						item.setReview(true);

						boolean canAmend = (rs.getInt(2) == 1) ? true : false;
						item.setAmend(canAmend);
						break;
					}
				}
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		/* Log information */
		printLog("Getting role return versions", roleId, "Versions id(s): "
				+ idList);

		/* The result list */
		return versions;
	}

	/** Updates a role permissions */
	public void setRolePermissions(RolePK rolePK, Set<Integer> permissions)
			throws RemoteException, EJBException {

		Connection con = DatabaseUtil.getConnection();
		int roleId = rolePK.getId();
		String idList = "";

		try {
			// Deleting role old permissions data
			String sql = "delete from SYS_ROLE_PERMISSIONS where roleID=?";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, roleId);
			ps.executeUpdate();
			ps.close();

			// Adding new permissions
			sql = "insert into SYS_ROLE_PERMISSIONS (roleID, permissionID) values (?,?)";
			ps = con.prepareStatement(sql);

			for (int permId : permissions) {
				ps.setInt(1, roleId);
				ps.setInt(2, permId);
				ps.executeUpdate();

				idList += permId + ", ";
			}

			ps.close();

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}

		// Log information
		printLog("Setting role permissions", roleId, "Permission id(s): "
				+ idList);
	}

	/** Updates a role return versions */
	public void setRoleReturnVersions(RolePK rolePK, List<SecurityItem> versions)
			throws RemoteException, EJBException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		int roleId = rolePK.getId();
		String idList = "";

		try {
			/*
			 * Deleting role old return versions
			 */
			String sql = "delete from SYS_ROLE_RETURN_VERSIONS where role_id = ?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, roleId);
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			/*
			 * Adding new return versions
			 */
			sql = "insert into SYS_ROLE_RETURN_VERSIONS (role_id, version_id, can_amend) "
					+ "values (?, ?, ?)";
			ps = con.prepareStatement(sql);

			for (SecurityItem item : versions) {

				ps.setInt(1, roleId);
				ps.setInt(2, item.getId());

				int canAmend = (item.getAmend() == SecurityItem.Status.YES) ? 1
						: 0;
				ps.setInt(3, canAmend);

				ps.executeUpdate();

				idList += item.getId() + ", ";
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}

		/* Log information */
		printLog("Setting role return versions", roleId, "Versions id(s): "
				+ idList);
	}

	/** Updates a given role returns */
	public void setRoleReturns(RolePK rolePK, Set<Integer> returns)
			throws RemoteException, EJBException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		int roleId = rolePK.getId();
		String idList = "";

		try {
			/*
			 * Deleting the role old returns
			 */
			String sql = "delete from SYS_ROLE_RETURNS where role_id = ?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, roleId);
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			/*
			 * Setting the new returns
			 */
			sql = "insert into SYS_ROLE_RETURNS (role_id, definition_id) values (?, ?)";
			ps = con.prepareStatement(sql);

			for (int returnId : returns) {
				ps.setInt(1, roleId);
				ps.setInt(2, returnId);
				ps.executeUpdate();

				idList += returnId + ", ";
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}

		/* Log information */
		printLog("Setting role returns", roleId, "Returns id(s): " + idList);
	}

	/** Updates a given role reports */
	public void setRoleReports(RolePK rolePK, Set<Integer> reports)
			throws RemoteException, EJBException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		int roleId = rolePK.getId();
		String idList = "";

		try {
			/*
			 * Deleting the role old reports
			 */
			String sql = "delete from SYS_ROLE_REPORTS where role_id = ?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, roleId);
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);
			/*
			 * Setting the new reports
			 */
			sql = "insert into SYS_ROLE_REPORTS (role_id, report_id) values (?, ?)";
			ps = con.prepareStatement(sql);

			for (int reportId : reports) {
				ps.setInt(1, roleId);
				ps.setInt(2, reportId);
				ps.executeUpdate();

				idList += reportId + ", ";
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}

		/* Log information */
		printLog("Setting role reports", roleId, "Report id(s): " + idList);
	}

	/** Returns a given role returns */
	public Map<Integer, TreeSecurityItem> getRoleReturns(RolePK rolePK,
			Handle languageHandle) throws RemoteException, EJBException {

		/*
		 * The result map. At first it contains all returns. Then the given role
		 * returns will be retrieved and set in the result map.
		 */
		Map<Integer, TreeSecurityItem> returns = (new UserSessionBean())
				.getAllReturns(languageHandle);

		String idList = "";
		int roleId = rolePK.getId();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "select a.definition_id from sys_role_returns a where a.role_id = ?";

			ps = con.prepareStatement(sql);
			ps.setInt(1, roleId);

			rs = ps.executeQuery();

			while (rs.next()) {
				/* The role has some returns */

				int returnId = rs.getInt(1);
				idList += returnId + ", ";

				/* Setting role return into the result map */
				TreeSecurityItem item = returns.get(returnId);
				item.setReview(true);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		/* Log information */
		printLog("Getting role returns", roleId, "Return id(s): " + idList);

		/* The result map */
		return returns;
	}

	/** Returns a given role reports */
	public Map<Integer, TreeSecurityItem> getRoleReports(RolePK rolePK,
			Handle languageHandle) throws RemoteException, EJBException {

		/*
		 * The result map. At first it contains all reports. Then the given role
		 * reports are retrieved and set in the result map.
		 */
		Map<Integer, TreeSecurityItem> reports = (new UserSessionBean())
				.getAllReports(languageHandle);

		String idList = "";
		int roleId = rolePK.getId();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "select report_id from sys_role_reports where role_id = ?";

			ps = con.prepareStatement(sql);
			ps.setInt(1, roleId);
			rs = ps.executeQuery();

			while (rs.next()) {
				/* The role has some reports */

				int reportId = rs.getInt(1);
				idList += reportId + ", ";

				/* Setting the role report into the result map */
				TreeSecurityItem item = reports.get(reportId);
				if(item!=null)
				item.setReview(true);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		/* Log information */
		printLog("Getting role reports", roleId, "Report id(s): " + idList);

		/* The result map */
		return reports;
	}

	/** Prints log information */
	private void printLog(String description, int roleId, String details) {

		StringBuffer logText = new StringBuffer();
		logText.append("Caller user: ");
		logText.append(sessionContext.getCallerPrincipal().getName());
		logText.append(". Role id: ");
		logText.append(roleId);
		logText.append(". " + details);

		log.info(description);
		log.info(logText.toString());
	}
}
