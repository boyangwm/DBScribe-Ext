/*
 * ReturnTypeBean.java
 *
 * Created on October 31, 2001, 12:00 PM
 */

package fina2.returns;

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
public class ReturnTypeBean implements EntityBean {

	private EntityContext ctx;

	public ReturnTypePK pk;
	public String code;
	public int description;
	private String oldDescription;
	private LoggerHelper log = new LoggerHelper(ReturnTypeBean.class,
			"Return Type");

	public ReturnTypePK ejbCreate() throws EJBException, CreateException {
		pk = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		PreparedStatement insert = null;
		ResultSet rs = null;
		try {
			description = LocaleUtil.allocateString(con);
			ps = con.prepareStatement("select max(id) from IN_RETURN_TYPES");
			insert = con.prepareStatement("insert into IN_RETURN_TYPES ( "
					+ "id,nameStrID) " + "values(?,?)");

			rs = ps.executeQuery();

			if (rs.next())
				pk = new ReturnTypePK(rs.getInt(1) + 1);
			else
				pk = new ReturnTypePK(1);

			code = String.valueOf(pk.getId());

			insert.setInt(1, pk.getId());
			// insert.setString(2, code);
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

	public void ejbPostCreate() throws EJBException, CreateException {
	}

	public ReturnTypePK ejbFindByPrimaryKey(ReturnTypePK pk)
			throws EJBException, FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select id from IN_RETURN_TYPES where id=?");
			ps.setInt(1, pk.getId());
			rs = ps.executeQuery();

			if (!rs.next()) {
				throw new FinderException("Bank Type is not found.");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return pk;
	}

	public void ejbStore() throws javax.ejb.EJBException,
			java.rmi.RemoteException {

		log.logObjectStore();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("update IN_RETURN_TYPES set "
					+ "code=?, " + "nameStrID=? " + "where id=?");
			ps.setString(1, code + " ");
			ps.setInt(2, description);
			ps.setInt(3, ((ReturnTypePK) ctx.getPrimaryKey()).getId());

			ps.executeUpdate();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	public void ejbActivate() throws javax.ejb.EJBException,
			java.rmi.RemoteException {
	}

	public void ejbPassivate() throws javax.ejb.EJBException,
			java.rmi.RemoteException {
	}

	public void ejbLoad() throws javax.ejb.EJBException,
			java.rmi.RemoteException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select code, nameStrID "
					+ "from IN_RETURN_TYPES where id=?");
			ps.setInt(1, ((ReturnTypePK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			rs.next();
			code = rs.getString("code").trim();
			description = rs.getInt("nameStrID");
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
			javax.ejb.EJBException, java.rmi.RemoteException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement exec = null;
		ResultSet rs = null;
		try {
			exec = con
					.prepareStatement("select id from IN_RETURN_DEFINITIONS where typeID=?");
			exec.setInt(1, ((ReturnTypePK) ctx.getPrimaryKey()).getId());
			rs = exec.executeQuery();
			if (!rs.next()) {
				PreparedStatement ps = con
						.prepareStatement("select nameStrID from IN_RETURN_TYPES where id=?");
				ps.setInt(1, ((ReturnTypePK) ctx.getPrimaryKey()).getId());
				rs = ps.executeQuery();

				rs.next();
				PreparedStatement del = con
						.prepareStatement("delete from SYS_STRINGS where id=?");
				del.setInt(1, rs.getInt(1));
				del.executeUpdate();

				DatabaseUtil.closeStatement(del);
				DatabaseUtil.closeStatement(ps);

				ps = con.prepareStatement("delete from IN_RETURN_TYPES "
						+ "where id=?");
				ps.setInt(1, ((ReturnTypePK) ctx.getPrimaryKey()).getId());
				ps.executeUpdate();

				int objectId = ((ReturnTypePK) ctx.getPrimaryKey()).getId();
				String user = ctx.getCallerPrincipal().getName();
				log.logObjectRemove(objectId, user);
				log.logPropertyValue("code", this.code, objectId, user);
				log.logPropertyValue("description", this.oldDescription,
						objectId, user);

				DatabaseUtil.closeStatement(ps);
			} else {
				throw new EJBException();
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, exec, con);
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
			throws RemoteException, EJBException {

		log.logPropertySet("description", desc, this.oldDescription,
				((ReturnTypePK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());
		this.oldDescription = desc;

		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		try {
			LocaleUtil.setString(con, langPK, description, desc);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) throws EJBException, FinaTypeException {

		log.logPropertySet("code", code, this.code, ((ReturnTypePK) ctx
				.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());

		if (code.equals(""))
			code = " ";
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select id from IN_RETURN_TYPES where rtrim(code)=? and id != ?");
			ps.setString(1, code.trim());
			ps.setInt(2, ((ReturnTypePK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			if (rs.next()) {
				throw new FinaTypeException(Type.CODE_NOT_UNIQUE, new String[] {
						"FinaTypeException.Return.Type", code });
			}
			this.code = code.trim();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}
}
