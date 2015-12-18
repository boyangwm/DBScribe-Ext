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
import java.sql.Statement;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.Handle;

import fina2.FinaTypeException;
import fina2.FinaTypeException.Type;
import fina2.db.DatabaseUtil;
import fina2.i18n.LocaleUtil;
import fina2.util.LoggerHelper;

public class BankGroupBean implements EntityBean {

	private EntityContext ctx;

	public BankGroupPK pk;
	public String code;
	public int description;
	private int criterionId;
	private LoggerHelper log = new LoggerHelper(BankGroupBean.class,
			"Bank Group");

	public BankGroupPK ejbCreate() throws EJBException, CreateException {
		BankGroupPK pk = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement insert = null;
		try {

			description = LocaleUtil.allocateString(con);

			ps = con.prepareStatement("select max(id) from IN_BANK_GROUPS");
			insert = con.prepareStatement("insert into IN_BANK_GROUPS ( "
					+ "id,nameStrID) " + "values(?,?)");

			rs = ps.executeQuery();

			if (rs.next())
				pk = new BankGroupPK(rs.getInt(1) + 1);
			else
				pk = new BankGroupPK(1);

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

	public void ejbPostCreate() throws EJBException, CreateException {
	}

	public BankGroupPK ejbFindByPrimaryKey(BankGroupPK pk) throws EJBException,
			FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select id from IN_BANK_GROUPS where id=?");
			ps.setInt(1, pk.getId());
			rs = ps.executeQuery();

			if (!rs.next()) {
				throw new FinderException("Bank Group is not found.");
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

		if (code == null) {
			return;
		}
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("update IN_BANK_GROUPS set " + "code=?, "
					+ "nameStrID=?, " + "criterionId=? " + "where id=?");
			ps.setString(1, code + " ");
			ps.setInt(2, description);
			ps.setInt(3, criterionId);
			ps.setInt(4, ((BankGroupPK) ctx.getPrimaryKey()).getId());

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
			ps = con.prepareStatement("select code, nameStrID, criterionId "
					+ "from IN_BANK_GROUPS where id=?");
			ps.setInt(1, ((BankGroupPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();
			rs.next();
			code = rs.getString("code").trim();
			description = rs.getInt("nameStrID");
			criterionId = rs.getInt("criterionId");
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
					.prepareStatement("select bankid from MM_BANK_GROUP where bankgroupid=?");
			exec.setInt(1, ((BankGroupPK) ctx.getPrimaryKey()).getId());
			rs = exec.executeQuery();
			if (!rs.next()) {
                                DatabaseUtil.closeResultSet(rs);
				PreparedStatement ps = con
						.prepareStatement("select nameStrID from IN_BANK_GROUPS where id=?");
				ps.setInt(1, ((BankGroupPK) ctx.getPrimaryKey()).getId());
				rs = ps.executeQuery();

				rs.next();
				PreparedStatement del = con
						.prepareStatement("delete from SYS_STRINGS where id=?");
				del.setInt(1, rs.getInt(1));
				del.executeUpdate();

				DatabaseUtil.closeStatement(ps);
				DatabaseUtil.closeStatement(del);

				ps = con.prepareStatement("delete from IN_BANK_GROUPS "
						+ "where id=?");
				ps.setInt(1, ((BankGroupPK) ctx.getPrimaryKey()).getId());
				ps.executeUpdate();

				int objectId = ((BankGroupPK) ctx.getPrimaryKey()).getId();
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
		return LocaleUtil.getSysString(langHandle, description);
	}

	public void setDescription(Handle langHandle, String desc)
			throws RemoteException, EJBException {
		LocaleUtil.setSysString(langHandle, description, desc);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) throws EJBException, FinaTypeException {

		log.logPropertySet("code", code, this.code, ((BankGroupPK) ctx
				.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());

		if (code.equals(""))
			code = " ";

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select id from IN_BANK_GROUPS where rtrim(code)=? and id != ?");
			ps.setString(1, code.trim());
			ps.setInt(2, ((BankGroupPK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			if (rs.next()) {
				throw new FinaTypeException(Type.CODE_NOT_UNIQUE, new String[] {
						"FinaTypeException.Bank.Group", code });
			}
			this.code = code.trim();
			log.getLogger().debug("setCode = " + this.code);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public int getCriterionId() throws EJBException {
		return criterionId;
	}

	public void setCriterionId(int id) throws EJBException, FinaTypeException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select id from IN_CRITERION where id = ?");
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (!rs.next()) {
				throw new FinaTypeException(Type.BANK_CRITERION_NOT_FOUND);
			}

			this.criterionId = id;
			log.getLogger().debug("setCriterionId = " + id);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public void bankGroupAmend(Handle langHandle, String code, String desc)
			throws RemoteException, FinaTypeException {
		try {
			setCode(code);
			setDescription(langHandle, desc);
		} catch (FinaTypeException e) {
			throw e;
		} catch (Exception e) {
			log.getLogger().error(e.getMessage(), e);
			throw new EJBException(e);
		}
	}
}
