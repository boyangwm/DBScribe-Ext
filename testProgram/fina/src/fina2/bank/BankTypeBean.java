/*
 * BankTypeBean.java
 *
 * Created on October 19, 2001, 7:32 PM
 */

package fina2.bank;

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

public class BankTypeBean implements EntityBean {

	private EntityContext ctx;

	public BankTypePK pk;
	public String code;
	public int description;
	private String oldDescription;
	private LoggerHelper log = new LoggerHelper(BankTypeBean.class, "Bank Type");

	public BankTypePK ejbCreate() throws EJBException, CreateException {
		BankTypePK pk = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement insert = null;
		try {

			description = LocaleUtil.allocateString(con);

			ps = con.prepareStatement("select max(id) from IN_BANK_TYPES");
			insert = con.prepareStatement("insert into IN_BANK_TYPES ( "
					+ "id,nameStrID) " + "values(?,?)");
			rs = ps.executeQuery();

			if (rs.next())
				pk = new BankTypePK(rs.getInt(1) + 1);
			else
				pk = new BankTypePK(1);

			// code = String.valueOf(pk.getId());
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

	public BankTypePK ejbFindByPrimaryKey(BankTypePK pk) throws EJBException,
			FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select id from IN_BANK_TYPES where id=?");
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
			ps = con.prepareStatement("update IN_BANK_TYPES set " + "code=?, "
					+ "nameStrID=? " + "where id=?");
			ps.setString(1, code + " ");
			ps.setInt(2, description);
			ps.setInt(3, ((BankTypePK) ctx.getPrimaryKey()).getId());
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
		log.getLogger().debug("bt load");
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select code, nameStrID "
					+ "from IN_BANK_TYPES where id=?");
			ps.setInt(1, ((BankTypePK) ctx.getPrimaryKey()).getId());

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
		ResultSet rs = null;
		PreparedStatement exec = null;
		try {
			exec = con
					.prepareStatement("select id from IN_BANKS where typeID=?");
			exec.setInt(1, ((BankTypePK) ctx.getPrimaryKey()).getId());
			rs = exec.executeQuery();
			if (!rs.next()) {
				PreparedStatement ps = con
						.prepareStatement("select nameStrID from IN_BANK_TYPES where id=?");
				ps.setInt(1, ((BankTypePK) ctx.getPrimaryKey()).getId());
				rs = ps.executeQuery();

				rs.next();
				PreparedStatement del = con
						.prepareStatement("delete from SYS_STRINGS where id=?");
				del.setInt(1, rs.getInt(1));
				del.executeUpdate();

				DatabaseUtil.closeStatement(ps);
				DatabaseUtil.closeStatement(del);

				ps = con.prepareStatement("delete from IN_BANK_TYPES "
						+ "where id=?");
				ps.setInt(1, ((BankTypePK) ctx.getPrimaryKey()).getId());
				ps.executeUpdate();

				int objectId = ((BankTypePK) ctx.getPrimaryKey()).getId();
				String user = ctx.getCallerPrincipal().getName();
				log.logObjectRemove(objectId, user);
				log.logPropertyValue("code", this.code, objectId, user);

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
		// store = false;

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
		// store = false;

		log.logPropertySet("description", desc, oldDescription,
				((BankTypePK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());

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

		log.logPropertySet("code", code, this.code, ((BankTypePK) ctx
				.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());

		if (code.equals(""))
			code = " ";
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select id from IN_BANK_TYPES where rtrim(code)=? and id != ?");
			ps.setString(1, code);
			ps.setInt(2, ((BankTypePK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			if (rs.next()) {
				throw new FinaTypeException(Type.CODE_NOT_UNIQUE, new String[] {
						"FinaTypeException.Bank.Type", code });
			}
			this.code = code.trim();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}
	
}
