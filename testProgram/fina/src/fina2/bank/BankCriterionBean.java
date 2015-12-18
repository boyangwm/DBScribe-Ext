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
import javax.ejb.RemoveException;

import fina2.FinaTypeException;
import fina2.FinaTypeException.Type;
import fina2.db.DatabaseUtil;
import fina2.i18n.LocaleUtil;
import fina2.util.LoggerHelper;

public class BankCriterionBean implements EntityBean {

	private EntityContext ctx;

	private int description;
	private String code;
	private boolean isDefault;

	private LoggerHelper log = new LoggerHelper(BankCriterionBean.class,
			"Bank Criterion");

	public BankCriterionPK ejbCreate() throws EJBException, CreateException {
		Connection con = DatabaseUtil.getConnection();
		BankCriterionPK pk = null;
		PreparedStatement ps = null;
		PreparedStatement insert = null;
		ResultSet rs = null;
		try {

			description = LocaleUtil.allocateString(con);
			ps = con.prepareStatement("select max(id) from IN_CRITERION");
			insert = con.prepareStatement("insert into IN_CRITERION ( "
					+ "id,nameStrID,isDefault) " + "values(?,?,?)");
			rs = ps.executeQuery();

			if (rs.next())
				pk = new BankCriterionPK(rs.getInt(1) + 1);
			else
				pk = new BankCriterionPK(1);

			insert.setInt(1, pk.getId());
			insert.setInt(2, description);
			insert.setInt(3, isDefault ? BankCriterionConstants.DEF_CRITERION
					: BankCriterionConstants.NON_DEF_CRITERION);

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

	public BankCriterionPK ejbFindByPrimaryKey(BankCriterionPK pk)
			throws EJBException, FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id from IN_CRITERION where id=?");
			ps.setInt(1, pk.getId());
			rs = ps.executeQuery();

			if (!rs.next()) {
				throw new FinderException("Bank Criterion is not found.");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return pk;
	}

	/**
	 * ejbActivate
	 * 
	 * @throws EJBException
	 * @throws RemoteException
	 * @todo Implement this javax.ejb.EntityBean method
	 */
	public void ejbActivate() throws EJBException, RemoteException {
	}

	/**
	 * ejbLoad
	 * 
	 * @throws EJBException
	 * @throws RemoteException
	 * @todo Implement this javax.ejb.EntityBean method
	 */
	public void ejbLoad() throws EJBException, RemoteException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select code, nameStrID, isDefault "
					+ "from IN_CRITERION where id=?");
			ps.setInt(1, ((BankCriterionPK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			rs.next();
			code = rs.getString("code").trim();
			description = rs.getInt("nameStrID");
			isDefault = rs.getInt("isDefault") == BankCriterionConstants.DEF_CRITERION;
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	/**
	 * ejbPassivate
	 * 
	 * @throws EJBException
	 * @throws RemoteException
	 * @todo Implement this javax.ejb.EntityBean method
	 */
	public void ejbPassivate() throws EJBException, RemoteException {
	}

	/**
	 * ejbRemove
	 * 
	 * @throws RemoveException
	 * @throws EJBException
	 * @throws RemoteException
	 * @todo Implement this javax.ejb.EntityBean method
	 */
	public void ejbRemove() throws RemoveException, EJBException,
			RemoteException {
		Connection con = DatabaseUtil.getConnection();
		ResultSet rs = null;
		PreparedStatement exec = null;
		try {
			exec = con
					.prepareStatement("select id from IN_BANK_GROUPS where criterionId=?");
			exec.setInt(1, ((BankCriterionPK) ctx.getPrimaryKey()).getId());
			rs = exec.executeQuery();
			if (!rs.next()) {
                                DatabaseUtil.closeResultSet(rs);
				PreparedStatement ps = con
						.prepareStatement("select nameStrID from IN_CRITERION where id=?");
				ps.setInt(1, ((BankCriterionPK) ctx.getPrimaryKey()).getId());
				rs = ps.executeQuery();

				rs.next();
				PreparedStatement del = con
						.prepareStatement("delete from SYS_STRINGS where id=?");
				del.setInt(1, rs.getInt(1));
				del.executeUpdate();

				DatabaseUtil.closeStatement(ps);
				DatabaseUtil.closeStatement(del);

				ps = con.prepareStatement("delete from IN_CRITERION "
						+ "where id=?");
				ps.setInt(1, ((BankCriterionPK) ctx.getPrimaryKey()).getId());
				ps.executeUpdate();

				int objectId = ((BankCriterionPK) ctx.getPrimaryKey()).getId();
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

	/**
	 * ejbStore
	 * 
	 * @throws EJBException
	 * @throws RemoteException
	 * @todo Implement this javax.ejb.EntityBean method
	 */
	public void ejbStore() throws EJBException, RemoteException {

		log.logObjectStore();
		if (code == null) {
			return;
		}

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			if (isDefault) {
				Statement stmt = con.createStatement();
				stmt.executeUpdate("update IN_CRITERION set isDefault="
						+ BankCriterionConstants.NON_DEF_CRITERION);
				DatabaseUtil.closeStatement(stmt);
			}

			ps = con.prepareStatement("update IN_CRITERION set " + "code=?, "
					+ "nameStrID=?, " + "isDefault=? " + "where id=?");
			ps.setString(1, code);
			ps.setInt(2, description);
			ps.setInt(3, isDefault ? BankCriterionConstants.DEF_CRITERION
					: BankCriterionConstants.NON_DEF_CRITERION);
			ps.setInt(4, ((BankCriterionPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	/**
	 * setEntityContext
	 * 
     * @param entityContext EntityContext
	 * @throws EJBException
	 * @throws RemoteException
	 * @todo Implement this javax.ejb.EntityBean method
	 */
	public void setEntityContext(EntityContext entityContext)
			throws EJBException, RemoteException {
		this.ctx = entityContext;
	}

	/**
	 * unsetEntityContext
	 * 
	 * @throws EJBException
	 * @throws RemoteException
	 * @todo Implement this javax.ejb.EntityBean method
	 */
	public void unsetEntityContext() throws EJBException, RemoteException {
		this.ctx = null;
	}

	public String getDescription(Handle langHandle) throws RemoteException,
			EJBException {
		return LocaleUtil.getSysString(langHandle, description);
	}

	public void setDescription(Handle langHandle, String param)
			throws RemoteException, EJBException {
		LocaleUtil.setSysString(langHandle, description, param);
	}

	public String getCode() throws RemoteException, EJBException {
		return this.code;
	}

	public void setCode(String param) throws RemoteException, EJBException,
			FinaTypeException {

		if (param == null) {
			return;
		} else if (param.equals(""))
			param = " ";

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select id from IN_CRITERION where rtrim(code)=? and id != ?");
			ps.setString(1, param.trim());
			ps.setInt(2, ((BankCriterionPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();
			if (rs.next()) {
				throw new FinaTypeException(Type.CODE_NOT_UNIQUE, new String[] {
						"FinaTypeException.Bank.Criterion", code });
			}
			this.code = param.trim();
			log.getLogger().debug("setCode = " + code);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public void setDefault(boolean value) {
		isDefault = value;
	}

	public boolean isDefault() {
		return isDefault;
	}
}
