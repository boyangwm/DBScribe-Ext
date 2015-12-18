/*
 * RoleBean.java
 *
 * Created on October 30, 2001, 2:49 PM
 */

package fina2.security;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.Handle;

import fina2.FinaTypeException;
import fina2.FinaTypeException.Type;
import fina2.db.DatabaseUtil;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;
import fina2.util.LoggerHelper;

/**
 * 
 * @author David Shalamberidze
 * @version
 */
public class RoleBean implements EntityBean {

	private EntityContext ctx;

	public RolePK pk;
	public int description;
	private String oldDescription;
	private LoggerHelper log = new LoggerHelper(RoleBean.class, "Group");

	/** The role's code */
	private String code = null;

	/** Creates a new role */
	public RolePK ejbCreate() throws EJBException, CreateException {
		RolePK pk = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		PreparedStatement insert = null;
		ResultSet rs = null;
		try {

			description = LocaleUtil.allocateString(con);

			ps = con.prepareStatement("select max(id) from SYS_ROLES");
			insert = con
					.prepareStatement("insert into SYS_ROLES (id,nameStrID) values(?,?)");

			rs = ps.executeQuery();
			rs.next();

			pk = new RolePK(rs.getInt(1) + 1);

			insert.setInt(1, pk.getId());
			insert.setInt(2, description);

			insert.executeUpdate();

			log.logObjectCreate(pk.getId(), ctx.getCallerPrincipal().getName());
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(insert);
		}
		return pk;
	}

	public RolePK ejbFindByPrimaryKey(RolePK pk) throws EJBException,
			FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id from SYS_ROLES where id=?");
			ps.setInt(1, pk.getId());
			rs = ps.executeQuery();

			if (!rs.next())
				throw new FinderException("Role is not found.");
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return pk;
	}

	/** Saves the role data */
	public void ejbStore() throws javax.ejb.EJBException,
			java.rmi.RemoteException {

		log.logObjectStore();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con
					.prepareStatement("update SYS_ROLES set code=?, nameStrID=? where id=?");

			ps.setString(1, code);
			ps.setInt(2, description);
			ps.setInt(3, ((RolePK) ctx.getPrimaryKey()).getId());

			ps.executeUpdate();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	public void ejbPostCreate() throws EJBException, CreateException {
	}

	public void ejbActivate() throws javax.ejb.EJBException,
			java.rmi.RemoteException {
	}

	public void ejbPassivate() throws javax.ejb.EJBException,
			java.rmi.RemoteException {
	}

	/** Loads the role data */
	public void ejbLoad() throws javax.ejb.EJBException,
			java.rmi.RemoteException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "select a.nameStrID, a.code from SYS_ROLES a where id=?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, ((RolePK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			rs.next();

			description = rs.getInt("nameStrID");
			code = rs.getString("code");
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public void setEntityContext(javax.ejb.EntityContext ctx)
			throws javax.ejb.EJBException, java.rmi.RemoteException {
		this.ctx = ctx;
	}

	public void ejbRemove() throws javax.ejb.RemoveException,
			javax.ejb.EJBException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement conf = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		PreparedStatement del = null;
		try {
			conf = con
					.prepareStatement("select userID from SYS_USERS_ROLES where roleID=?");
			conf.setInt(1, ((RolePK) ctx.getPrimaryKey()).getId());
			rs = conf.executeQuery();
			if (rs.next()) {
				throw new Exception("fina2.security.roleIsNotEmpty");
			}

			ps = con
					.prepareStatement("select nameStrID from SYS_ROLES where id=?");
			ps.setInt(1, ((RolePK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();

			rs.next();
			del = con.prepareStatement("delete from SYS_STRINGS where id=?");
			del.setInt(1, rs.getInt(1));
			del.executeUpdate();

			DatabaseUtil.closeStatement(del);
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("delete from SYS_ROLE_MENUS "
					+ "where roleID=?");
			ps.setInt(1, ((RolePK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("delete from SYS_USERS_ROLES "
					+ "where roleID=?");
			ps.setInt(1, ((RolePK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("delete from SYS_ROLE_PERMISSIONS "
					+ "where roleID=?");
			ps.setInt(1, ((RolePK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			/*
			 * Deleting from table sys_role_returns
			 */

			int roleId = ((RolePK) ctx.getPrimaryKey()).getId();
			String sql = "delete from sys_role_returns where role_id = ?";

			ps = con.prepareStatement(sql);
			ps.setInt(1, roleId);
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			/*
			 * Deleting from table sys_role_reports
			 */

			sql = "delete from sys_role_reports where role_id = ? ";

			ps = con.prepareStatement(sql);
			ps.setInt(1, roleId);
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			/*
			 * Deleting from table sys_role_reports
			 */

			sql = "delete from sys_role_return_versions where role_id = ? ";

			ps = con.prepareStatement(sql);
			ps.setInt(1, roleId);
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			/*
			 * Deleting from table sys_roles
			 */

			sql = "delete from SYS_ROLES where id=?";
			ps = con.prepareStatement(sql);
			ps.setInt(1, roleId);
			ps.executeUpdate();
			DatabaseUtil.closeStatement(ps);

			/* Finished */

			/* Log information */
			String userName = ctx.getCallerPrincipal().getName();
			log.logObjectRemove(roleId, userName);

		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(del);
			DatabaseUtil.closeStatement(conf);
		}
	}

	public void unsetEntityContext() throws javax.ejb.EJBException,
			java.rmi.RemoteException {
		ctx = null;
	}

	public String getDescription(Handle langHandle) throws RemoteException,
			EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();

		Connection con = DatabaseUtil.getConnection();
		String s = "";
		try {
			s = LocaleUtil.getString(con, langPK, description);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		this.oldDescription = s;

		return s;
	}

	public void setDescription(Handle langHandle, String desc)
			throws FinaTypeException, RemoteException, EJBException {

		log.logPropertySet("description", desc, oldDescription, ((RolePK) ctx
				.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());
		this.oldDescription = desc;

		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select a.nameStrID from SYS_ROLES a, SYS_STRINGS b "
							+ "where b.id=a.nameStrID and b.value=? and a.id!=?");
			ps.setString(1, desc);
			ps.setInt(2, ((RolePK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();

			if (rs.next()) {
				throw new FinaTypeException(Type.CODE_NOT_UNIQUE, new String[] {
						"FinaTypeException.Role.Description", code });
			}

			LocaleUtil.setString(con, langPK, description, desc);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

	}

	/** Returns the role code */
	public String getCode() {
		return code;
	}

	/** Sets the role code */
	public void setCode(String code) throws RemoteException, FinaTypeException {

		log.logPropertySet("code", code, this.code, ((RolePK) ctx
				.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "select a.code from SYS_ROLES a "
					+ "where a.id!=? and a.code=?";

			ps = con.prepareStatement(sql);
			ps.setInt(1, ((RolePK) ctx.getPrimaryKey()).getId());
			ps.setString(2, code);

			rs = ps.executeQuery();
			if (rs.next()) {
				throw new FinaTypeException(Type.CODE_NOT_UNIQUE, new String[] {
						"FinaTypeException.Role", code });
			}
			this.code = code;

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}
}
