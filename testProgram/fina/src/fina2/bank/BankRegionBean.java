/*
 * BankRegionBean.java
 *
 * Created on March 25, 2002, 4:57 AM
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

import fina2.db.DatabaseUtil;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;

/**
 * 
 * @author vasop
 * @version
 */
@Deprecated
public class BankRegionBean implements EntityBean {

	private EntityContext ctx;
	private BankRegionPK pk;

	public int cityStrID;
	public int regionStrID;
	public String cityStr;
	public String regionStr;

	/** Creates new BankRegionBean */
	public BankRegionPK ejbCreate() throws EJBException, CreateException {
		BankRegionPK pk = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement insert = null;
		try {
			cityStrID = LocaleUtil.allocateString(con);
			regionStrID = LocaleUtil.allocateString(con);

			ps = con.prepareStatement("select max(id) from IN_REGIONS");
			insert = con.prepareStatement("insert into IN_REGIONS ( "
					+ "id,cityStrId,regionStrId) " + "values(?,?,?)");

			rs = ps.executeQuery();
			if (rs.next())
				pk = new BankRegionPK(rs.getInt(1) + 1);
			else
				pk = new BankRegionPK(1);

			insert.setInt(1, pk.getId());
			insert.setInt(2, cityStrID);
			insert.setInt(3, regionStrID);
			insert.executeUpdate();
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

	public BankRegionPK ejbFindByPrimaryKey(BankRegionPK pk)
			throws EJBException, FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id from IN_REGIONS where id=?");
			ps.setInt(1, pk.getId());
			rs = ps.executeQuery();

			if (!rs.next()) {
				throw new FinderException("Region is not found.");
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
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("update IN_REGIONS set "
					+ "cityStrId=?, " + "regionStrId=? " + "where id=?");
			ps.setInt(1, cityStrID);
			ps.setInt(2, regionStrID);
			ps.setInt(3, ((BankRegionPK) ctx.getPrimaryKey()).getId());

			ps.executeUpdate();

			this.pk = (BankRegionPK) ctx.getPrimaryKey();
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
			ps = con.prepareStatement("select cityStrID, regionStrID "
					+ "from IN_REGIONS where id=?");
			ps.setInt(1, ((BankRegionPK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			rs.next();
			cityStrID = rs.getInt("cityStrID");
			regionStrID = rs.getInt("regionStrID");

			this.pk = (BankRegionPK) ctx.getPrimaryKey();
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

	public void unsetEntityContext() throws javax.ejb.EJBException,
			java.rmi.RemoteException {
		ctx = null;
	}

	public void ejbRemove() throws javax.ejb.RemoveException,
			javax.ejb.EJBException, java.rmi.RemoteException {
		if (notUsedBankRegion(((BankRegionPK) ctx.getPrimaryKey()).getId())) {
			Connection con = DatabaseUtil.getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;
			PreparedStatement del = null;
			try {
				ps = con
						.prepareStatement("select cityStrID from IN_REGIONS where id=?");
				ps.setInt(1, ((BankRegionPK) ctx.getPrimaryKey()).getId());
				rs = ps.executeQuery();

				rs.next();
				del = con
						.prepareStatement("delete from SYS_STRINGS where id=?");
				del.setInt(1, rs.getInt(1));
				del.executeUpdate();

				DatabaseUtil.closeStatement(ps);
				ps = con
						.prepareStatement("select regionStrID from IN_REGIONS where id=?");
				ps.setInt(1, ((BankRegionPK) ctx.getPrimaryKey()).getId());
				rs = ps.executeQuery();
				rs.next();
				del.setInt(1, rs.getInt(1));
				del.executeUpdate();

				DatabaseUtil.closeStatement(ps);
				ps = con.prepareStatement("delete from IN_REGIONS "
						+ "where id=?");
				ps.setInt(1, ((BankRegionPK) ctx.getPrimaryKey()).getId());
				ps.executeUpdate();
				DatabaseUtil.closeStatement(ps);
			} catch (Exception e) {
				throw new EJBException(e);
			} finally {
				DatabaseUtil.close(rs, ps, con);
				DatabaseUtil.closeStatement(del);
			}
		}
	}

	private boolean notUsedBankRegion(int regionID) {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean regionUsed = false;
		try {
			ps = con
					.prepareStatement("select ID from IN_BANK_BRANCHES where bankRegionStrId=?");
			ps.setInt(1, regionID);
			rs = ps.executeQuery();
			if (!rs.next())
				regionUsed = true;
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return regionUsed;
	}

	public String getCity(Handle langHandle) throws RemoteException,
			EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		String s = "";
		try {
			s = LocaleUtil.getString(con, langPK, cityStrID);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return s;
	}

	public void setCity(Handle langHandle, String cityStr)
			throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		try {
			LocaleUtil.setString(con, langPK, cityStrID, cityStr);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public String getRegion(Handle langHandle) throws RemoteException,
			EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		String s = "";
		try {
			s = LocaleUtil.getString(con, langPK, regionStrID);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return s;
	}

	public void setRegion(Handle langHandle, String regionStr)
			throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		try {
			LocaleUtil.setString(con, langPK, regionStrID, regionStr);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}
}
