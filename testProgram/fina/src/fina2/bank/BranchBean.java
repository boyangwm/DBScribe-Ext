/*
 * BranchBean.java
 *
 * Created on March 17, 2002, 3:20 AM
 */

package fina2.bank;

/**
 *
 * @author  vasop
 * @version
 */

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.Handle;

import fina2.db.DatabaseUtil;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;
import fina2.regions.RegionStructureNodePK;

public class BranchBean implements EntityBean {

	private EntityContext ctx;
	public BranchPK pk;

	public long cityID;
	public int nameID;
	public int shortNameID;
	public int addressID;
	public int commentsID;
	public Date date;
	public Date dateOfChange;
	public int bankID;

	/** Creates new BranchBean */
	public BranchBean() {
	}

	public BranchPK ejbCreate() throws EJBException, CreateException {
		BranchPK pk = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement insert = null;
		try {

			nameID = LocaleUtil.allocateString(con);
			shortNameID = LocaleUtil.allocateString(con);
			addressID = LocaleUtil.allocateString(con);
			commentsID = LocaleUtil.allocateString(con);
			date = new Date();
			dateOfChange = null;

			ps = con.prepareStatement("select max(id) from IN_BANK_BRANCHES");

			insert = con
					.prepareStatement("insert into IN_BANK_BRANCHES ( id, nameStrID, shortNameStrID, addressStrID, commentsStrID) values(?,?,?,?,?)");
			rs = ps.executeQuery();

			if (rs.next())
				pk = new BranchPK(rs.getInt(1) + 1);
			else
				pk = new BranchPK(1);

			insert.setInt(1, pk.getId());
			insert.setInt(2, nameID);
			insert.setInt(3, shortNameID);
			insert.setInt(4, addressID);
			insert.setInt(5, commentsID);

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

	public BranchPK ejbFindByPrimaryKey(BranchPK pk) throws EJBException,
			FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id from IN_BANK_BRANCHES where id=?");
			ps.setInt(1, pk.getId());
			rs = ps.executeQuery();

			if (!rs.next()) {
				throw new FinderException("Branch is not found.");
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
			ps = con.prepareStatement("update IN_BANK_BRANCHES set "
					+ "bankRegionStrID=?, " + "nameStrID=?,  "
					+ "shortNameStrID=?, " + "addressStrID=?, "
					+ "commentsStrID=?, " + "creationDate=?, "
					+ "dateOfChange=?, " + "bankID=? " + "where id=?");
			ps.setLong(1, cityID);
			ps.setInt(2, nameID);
			ps.setInt(3, shortNameID);
			ps.setInt(4, addressID);
			ps.setInt(5, commentsID);
			ps.setDate(6, new java.sql.Date(date.getTime()));
			if (dateOfChange != null)
				ps.setDate(7, new java.sql.Date(dateOfChange.getTime()));
			else
				ps.setNull(7, java.sql.Types.DATE);
			ps.setInt(8, bankID);
			ps.setInt(9, ((BranchPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
			this.pk = (BranchPK) ctx.getPrimaryKey();
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
			ps = con.prepareStatement("select bankRegionStrID, nameStrID, shortNameStrID, addressStrID, commentsStrID, creationDate, dateOfChange, bankID "
					+ "from IN_BANK_BRANCHES where id=?");
			ps.setInt(1, ((BranchPK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			rs.next();
			cityID = rs.getLong("bankRegionStrID");
			nameID = rs.getInt("nameStrID");
			shortNameID = rs.getInt("shortNameStrID");
			addressID = rs.getInt("addressStrID");
			commentsID = rs.getInt("commentsStrID");
			date = rs.getDate("creationDate");
			dateOfChange = rs.getDate("dateOfChange");
			bankID = rs.getInt("bankID");

			this.pk = (BranchPK) ctx.getPrimaryKey();
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
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement del = null;
		try {
			ps = con.prepareStatement("select bankRegionStrID from IN_BANK_BRANCHES where id=?");
			ps.setInt(1, ((BranchPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();

			rs.next();
			del = con.prepareStatement("delete from SYS_STRINGS where id=?");
			del.setInt(1, rs.getInt(1));
			del.executeUpdate();
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
			ps = con.prepareStatement("select nameStrID from IN_BANK_BRANCHES where id=?");
			ps.setInt(1, ((BranchPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();
			rs.next();
			del.setInt(1, rs.getInt(1));
			del.executeUpdate();
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
			ps = con.prepareStatement("select shortNameStrID from IN_BANK_BRANCHES where id=?");
			ps.setInt(1, ((BranchPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();
			rs.next();
			del.setInt(1, rs.getInt(1));
			del.executeUpdate();
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
			ps = con.prepareStatement("select addressStrID from IN_BANK_BRANCHES where id=?");
			ps.setInt(1, ((BranchPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();
			rs.next();
			del.setInt(1, rs.getInt(1));
			del.executeUpdate();
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);
			ps = con.prepareStatement("select commentsStrID from IN_BANK_BRANCHES where id=?");
			ps.setInt(1, ((BranchPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();
			rs.next();
			del.setInt(1, rs.getInt(1));
			del.executeUpdate();

			DatabaseUtil.closeStatement(ps);
			ps = con.prepareStatement("delete from IN_BANK_BRANCHES "
					+ "where id=?");
			ps.setInt(1, ((BranchPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(del);
		}

	}

	public void unsetEntityContext() throws javax.ejb.EJBException,
			java.rmi.RemoteException {
		ctx = null;
	}

	public RegionStructureNodePK getBankRegionPK() {
		return new RegionStructureNodePK(cityID);
	}

	public void setBankRegionPK(RegionStructureNodePK pk) {
		cityID = pk.getId();
	}

	public String getName(Handle langHandle) throws RemoteException,
			EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		String s = "";
		try {
			s = LocaleUtil.getString(con, langPK, nameID);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return s;
	}

	public void setName(Handle langHandle, String paramName)
			throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		try {
			LocaleUtil.setString(con, langPK, nameID, paramName);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public String getShortName(Handle langHandle) throws RemoteException,
			EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		String s = "";
		try {
			s = LocaleUtil.getString(con, langPK, shortNameID);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return s;
	}

	public void setShortName(Handle langHandle, String paramShortName)
			throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		try {
			LocaleUtil.setString(con, langPK, shortNameID, paramShortName);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public String getAddress(Handle langHandle) throws RemoteException,
			EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		String s = "";
		try {
			s = LocaleUtil.getString(con, langPK, addressID);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return s;
	}

	public void setAddress(Handle langHandle, String paramAddress)
			throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		try {
			LocaleUtil.setString(con, langPK, addressID, paramAddress);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public String getComments(Handle langHandle) throws RemoteException,
			EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		String s = "";
		try {
			s = LocaleUtil.getString(con, langPK, commentsID);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return s;
	}

	public void setComments(Handle langHandle, String paramComments)
			throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		try {
			LocaleUtil.setString(con, langPK, commentsID, paramComments);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public String getDate(Handle langHandle) throws RemoteException,
			EJBException {
		return LocaleUtil.date2string(langHandle, date);
	}

	public void setDate(Handle langHandle, String date) throws RemoteException,
			EJBException, java.text.ParseException {
		this.date = LocaleUtil.string2date(langHandle, date);
	}

	public String getDateOfChange(Handle langHandle) throws RemoteException,
			EJBException {
		if (dateOfChange != null)
			return LocaleUtil.date2string(langHandle, dateOfChange);
		else
			return "";
	}

	public void setDateOfChange(Handle langHandle, String dateOfChange)
			throws RemoteException, EJBException, java.text.ParseException {
		if (!dateOfChange.trim().equals(""))
			this.dateOfChange = LocaleUtil
					.string2date(langHandle, dateOfChange);
		else
			this.dateOfChange = null;
	}

	public BankPK getBankPK() throws RemoteException, EJBException {
		return new BankPK(bankID);
	}

	public void setBankPK(BankPK bankPK) throws RemoteException, EJBException {
		bankID = bankPK.getId();
	}

}
