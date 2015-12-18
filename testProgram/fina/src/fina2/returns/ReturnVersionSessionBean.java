package fina2.returns;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.log4j.Logger;

import fina2.FinaTypeException;
import fina2.FinaTypeException.Type;
import fina2.db.DatabaseUtil;
import fina2.i18n.LocaleUtil;
import fina2.security.ServerSecurityUtil;
import fina2.security.User;
import fina2.security.UserBean;

public class ReturnVersionSessionBean implements SessionBean {

	SessionContext sessionContext;

	private static Logger log = Logger
			.getLogger(ReturnVersionSessionBean.class);

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

	/** Returns a list of return versions for given user */
	public Collection getReturnVersions(Handle langHandle, Handle userHandle)
			throws RemoteException, EJBException, fina2.FinaTypeException {

		/* All return verision for given user */
		return getReturnVersions(langHandle, userHandle, false);
	}

	/** Returns a list of return versions for given user */
	public Collection getReturnVersions(Handle langHandle, Handle userHandle,
			boolean amendOnly) throws fina2.FinaTypeException, RemoteException {
		
		String user=((User)userHandle.getEJBObject()).getLogin();
		

		/* The result list */
		ArrayList returnVersions = new ArrayList();
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String amendOnlySql = "";
		String sql="";
		/* Can throw PermissionDeniedException */
		
		
		try {
			if(!user.equals("sa")) {
				ServerSecurityUtil.checkUserPermissions(userHandle,
						 "fina2.returns.version.review");
			
			con = DatabaseUtil.getConnection();
		
			if (amendOnly) {
				/* Amend only return versions */
				amendOnlySql = " and a.can_amend = 1 ";
			}

			 sql= "select v.id, v.sequence, v.code, v.descStrID "
					+ "from IN_RETURN_VERSIONS v where v.id in "
					+ "("
					+ "    select a.version_id from sys_user_return_versions a where a.user_id = ? "
					+ amendOnlySql
					+ "    union "
					+ "    select a.version_id from sys_role_return_versions a where a.role_id in "
					+ "    (select a.roleid from sys_users_roles a where a.userid = ?) "
					+ amendOnlySql + ") " + "order by v.sequence ";

			ps = con.prepareStatement(sql);

			int userId = UserBean.getUserId(userHandle);
			ps.setInt(1, userId);
			ps.setInt(2, userId);

			rs = ps.executeQuery();

			/* Copying the result set to the result list */
			while (rs.next()) {

				int versionId = rs.getInt(1);
				int sequence = rs.getInt(2);
				String code = rs.getString(3).trim();
				int descStrId = rs.getInt(4);
				String desc = LocaleUtil.getString(con, langHandle, descStrId);

				ReturnVersion rv = new ReturnVersion();
				rv.setId(versionId);
				rv.setSequence(sequence);
				rv.setCode(code);
				rv.setDescStrId(descStrId);
				rv.setDescription(desc);

				/* Adding to the result list */
				returnVersions.add(rv);
			}
			}
			else {
				con = DatabaseUtil.getConnection();
				 sql= "select v.id, v.sequence, v.code, v.descStrID "
						+ "from IN_RETURN_VERSIONS v ";

				ps = con.prepareStatement(sql);


				rs = ps.executeQuery();

				/* Copying the result set to the result list */
				while (rs.next()) {

					int versionId = rs.getInt(1);
					int sequence = rs.getInt(2);
					String code = rs.getString(3).trim();
					int descStrId = rs.getInt(4);
					String desc = LocaleUtil.getString(con, langHandle, descStrId);

					ReturnVersion rv = new ReturnVersion();
					rv.setId(versionId);
					rv.setSequence(sequence);
					rv.setCode(code);
					rv.setDescStrId(descStrId);
					rv.setDescription(desc);

					/* Adding to the result list */
					returnVersions.add(rv);
				}
			}
		} catch (Exception e) {
			log.error("Error while retrieving return versions", e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		/* The result list: contains only given user return versions */
		return returnVersions;
	}

	public ReturnVersion createReturnVersion(ReturnVersion rv, Handle langHandle)
			throws RemoteException, EJBException, FinaTypeException {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement insert = null;
		try {
			con = DatabaseUtil.getConnection();

			// Check if code unique
			checkCodeUniqueness(con, rv.getCode(), -1);

			// Get unique ID
			ps = con.prepareStatement("select max(id) from IN_RETURN_VERSIONS");

			rs = ps.executeQuery();
			rs.next();
			rv.setId(rs.getInt(1) + 1);

			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			// Get version number
			ps = con
					.prepareStatement("select max(sequence) from IN_RETURN_VERSIONS");

			rs = ps.executeQuery();

			rs.next();
			rv.setSequence(rs.getInt(1) + 1);

			// Store description
			rv.setDescStrId(LocaleUtil.allocateString(con));
			LocaleUtil.setSysString(langHandle, rv.getDescStrId(), rv
					.getDescription() != null ? rv.getDescription() : "");

			insert = con.prepareStatement("insert into IN_RETURN_VERSIONS ("
					+ "id,sequence,code,descStrID) values(?,?,?,?)");

			insert.setInt(1, rv.getId());
			insert.setInt(2, rv.getSequence());
			insert.setString(3, rv.getCode());
			insert.setInt(4, rv.getDescStrId());

			insert.executeUpdate();
		} catch (SQLException e) {
			log.error("Error creating return version", e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(insert);
		}
		return rv;
	}

	public void updateReturnVersion(ReturnVersion rv, Handle langHandle)
			throws RemoteException, EJBException, FinaTypeException {

		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DatabaseUtil.getConnection();
			// Check if code unique
			checkCodeUniqueness(con, rv.getCode(), rv.getId());

			LocaleUtil.setSysString(langHandle, rv.getDescStrId(), rv
					.getDescription());

			ps = con.prepareStatement("update IN_RETURN_VERSIONS set "
					+ "sequence=?, code=?, descStrId=? where id=?");

			ps.setInt(1, rv.getSequence());
			ps.setString(2, rv.getCode());
			ps.setInt(3, rv.getDescStrId());
			ps.setInt(4, rv.getId());

			ps.executeUpdate();
		} catch (SQLException e) {
			log.error("Error updating return version", e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	public void deleteReturnVersion(ReturnVersion rv) throws RemoteException,
			EJBException {

		Connection con = null;
		PreparedStatement del = null;
		try {
			con = DatabaseUtil.getConnection();
			if (!isUsed(con, rv.getId())) {

				del = con
						.prepareStatement("delete from SYS_STRINGS where id=?");
				del.setInt(1, rv.getDescStrId());

				del.executeUpdate();
				del.close();

				del = con
						.prepareStatement("delete from IN_RETURN_VERSIONS where id=?");
				del.setInt(1, rv.getId());

				del.executeUpdate();
			} else {
				throw new EJBException("Return version is used.");
			}

		} catch (SQLException e) {
			log.error("Error deleting return version", e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(del);
		}
	}

	private boolean isUsed(Connection con, int id) throws SQLException {

		boolean result = false;

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select id from IN_RETURN_STATUSES where versionId=?");
			ps.setInt(1, id);

			rs = ps.executeQuery();
			if (rs.next()) {
				result = true;
			}
		} catch (SQLException e) {
			log.error("Error during checking if return version is in use", e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
		}
		return result;
	}

	private void checkCodeUniqueness(Connection con, String code, int id)
			throws RemoteException, EJBException, FinaTypeException {

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select id from IN_RETURN_VERSIONS where code=? and id!=?");
			ps.setString(1, code);
			ps.setInt(2, id);

			rs = ps.executeQuery();
			if (rs.next()) {
				log.info("Return version code " + code + " is not unique");
				throw new FinaTypeException(Type.CODE_NOT_UNIQUE, new String[] {
						"FinaTypeException.Return.Version", code });
			}
		} catch (SQLException e) {
			log.error("Error during checking code uniqueness", e);
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeStatement(ps);
			DatabaseUtil.closeResultSet(rs);
		}
	}
}
