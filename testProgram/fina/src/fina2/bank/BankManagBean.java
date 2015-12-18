/*
 * BankManagBean.java
 *
 * Created on April 22, 2002, 5:03 PM
 */

package fina2.bank;

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

/**
 * 
 * @author vasop
 * @version
 */
public class BankManagBean implements EntityBean {

	private EntityContext ctx;
	public BankManagPK pk;

	public int nameID;
	public int lastNameID;
	public int managingBodyPK;
	public int postID;
	public String phone;
	public Date dateOfAppointment;
	public Date dateOfChange;
	public int registration1ID;
	public int registration2ID;
	public int registration3ID;
	public int comments1ID;
	public int comments2ID;
	public int bankID;

	public BankManagPK ejbCreate() throws EJBException, CreateException {
		BankManagPK pk = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement insert = null;
		try {

			nameID = LocaleUtil.allocateString(con);
			lastNameID = LocaleUtil.allocateString(con);
			postID = LocaleUtil.allocateString(con);
			registration1ID = LocaleUtil.allocateString(con);
			registration2ID = LocaleUtil.allocateString(con);
			registration3ID = LocaleUtil.allocateString(con);
			comments1ID = LocaleUtil.allocateString(con);
			comments2ID = LocaleUtil.allocateString(con);
			dateOfAppointment = new Date();
			dateOfChange = null;

			ps = con.prepareStatement("select max(id) from IN_BANK_MANAGEMENT");

			insert = con.prepareStatement("insert into IN_BANK_MANAGEMENT ( " + "id, nameStrID, lastNameStrID, postStrID, registrationStrId1, "
					+ "registrationStrId2, registrationStrId3, commentsStrID1, commentsStrID2) " + "values(?,?,?,?,?,?,?,?,?)");
			rs = ps.executeQuery();

			if (rs.next())
				pk = new BankManagPK(rs.getInt(1) + 1);
			else
				pk = new BankManagPK(1);

			insert.setInt(1, pk.getId());
			insert.setInt(2, nameID);
			insert.setInt(3, lastNameID);
			insert.setInt(4, postID);
			insert.setInt(5, registration1ID);
			insert.setInt(6, registration2ID);
			insert.setInt(7, registration3ID);
			insert.setInt(8, comments1ID);
			insert.setInt(9, comments2ID);

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

	public BankManagPK ejbFindByPrimaryKey(BankManagPK pk) throws EJBException, FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id from IN_BANK_MANAGEMENT where id=?");
			ps.setInt(1, pk.getId());
			rs = ps.executeQuery();

			if (!rs.next()) {
				throw new FinderException("Bank Manager is not found.");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return pk;
	}

	public void ejbStore() throws javax.ejb.EJBException, java.rmi.RemoteException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("update IN_BANK_MANAGEMENT set " + "nameStrID=?,  " + "lastNameStrID=?, " + "managingBodyID=?, " + "postStrID=?, " + "phone=?, " + "dateOfAppointment=?, "
					+ "cancelDate=?, " + "registrationStrId1=?, " + "registrationStrId2=?, " + "registrationStrId3=?, " + "commentsStrId1=?, " + "commentsStrId2=?, " + "bankID=? " + "where id=?");
			ps.setInt(1, nameID);
			ps.setInt(2, lastNameID);
			ps.setInt(3, managingBodyPK);
			ps.setInt(4, postID);
			ps.setString(5, phone);
			ps.setDate(6, new java.sql.Date(dateOfAppointment.getTime()));
			if (dateOfChange != null)
				ps.setDate(7, new java.sql.Date(dateOfChange.getTime()));
			else
				ps.setNull(7, java.sql.Types.DATE);
			ps.setInt(8, registration1ID);
			ps.setInt(9, registration2ID);
			ps.setInt(10, registration3ID);
			ps.setInt(11, comments1ID);
			ps.setInt(12, comments2ID);
			ps.setInt(13, bankID);
			ps.setInt(14, ((BankManagPK) ctx.getPrimaryKey()).getId());

			ps.executeUpdate();
			this.pk = (BankManagPK) ctx.getPrimaryKey();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}

	}

	public void ejbActivate() throws javax.ejb.EJBException, java.rmi.RemoteException {
	}

	public void ejbPassivate() throws javax.ejb.EJBException, java.rmi.RemoteException {
	}

	public void ejbLoad() throws javax.ejb.EJBException, java.rmi.RemoteException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select nameStrID, lastNameStrID, managingBodyID, postStrID, phone, dateOfAppointment, cancelDate, "
					+ "registrationStrId1, registrationStrId2, registrationStrId3, commentsStrId1, commentsStrId2 from IN_BANK_MANAGEMENT where id=?");
			ps.setInt(1, ((BankManagPK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			rs.next();
			nameID = rs.getInt("nameStrID");
			lastNameID = rs.getInt("lastNameStrID");
			managingBodyPK = rs.getInt("managingBodyID");
			postID = rs.getInt("postStrID");
			phone = rs.getString("phone");
			dateOfAppointment = rs.getDate("dateOfAppointment");
			dateOfChange = rs.getDate("cancelDate");
			registration1ID = rs.getInt("registrationStrId1");
			registration2ID = rs.getInt("registrationStrId2");
			registration3ID = rs.getInt("registrationStrId3");
			comments1ID = rs.getInt("commentsStrId1");
			comments2ID = rs.getInt("commentsStrId2");

			this.pk = (BankManagPK) ctx.getPrimaryKey();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public void setEntityContext(javax.ejb.EntityContext ctx) throws javax.ejb.EJBException, java.rmi.RemoteException {
		this.ctx = ctx;
	}

	public void ejbRemove() throws javax.ejb.RemoveException, javax.ejb.EJBException, java.rmi.RemoteException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement del = null;
		try {
			del = con.prepareStatement("delete from SYS_STRINGS where id=? or id=? or id=? or id=? or id=? or id=? or id=? or id=?");
			ps = con.prepareStatement("select nameStrID,lastNameStrId,postStrId,registrationStrId1,registrationStrId2, "
					+ "registrationStrId3,commentsStrId1,commentsStrId2 from IN_BANK_MANAGEMENT where id=?");

			ps.setInt(1, ((BankManagPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();
			rs.next();
			del.setInt(1, rs.getInt(1));
			del.setInt(2, rs.getInt(2));
			del.setInt(3, rs.getInt(3));
			del.setInt(4, rs.getInt(4));
			del.setInt(5, rs.getInt(5));
			del.setInt(6, rs.getInt(6));
			del.setInt(7, rs.getInt(7));
			del.setInt(8, rs.getInt(8));
			del.executeUpdate();

			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("delete from IN_BANK_MANAGEMENT " + "where id=?");
			ps.setInt(1, ((BankManagPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(del);
		}

	}

	public void unsetEntityContext() throws javax.ejb.EJBException, java.rmi.RemoteException {
		ctx = null;
	}

	public ManagingBodyPK getManagingBodyPK() {
		return new ManagingBodyPK(managingBodyPK);
	}

	public void setManagingBodyPK(ManagingBodyPK managingBodyPK) {
		this.managingBodyPK = managingBodyPK.getId();
	}

	public String getName(Handle langHandle) throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();
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

	public void setName(Handle langHandle, String nameStr) throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		try {
			LocaleUtil.setString(con, langPK, nameID, nameStr);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public String getLastName(Handle langHandle) throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		String s = "";
		try {
			s = LocaleUtil.getString(con, langPK, lastNameID);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return s;
	}

	public void setLasttName(Handle langHandle, String lastNameStr) throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		try {
			LocaleUtil.setString(con, langPK, lastNameID, lastNameStr);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public String getPost(Handle langHandle) throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		String s = "";
		try {
			s = LocaleUtil.getString(con, langPK, postID);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return s;
	}

	public void setPost(Handle langHandle, String postStr) throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		try {
			LocaleUtil.setString(con, langPK, postID, postStr);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public String getPhone() throws RemoteException, EJBException {
		return phone;
	}

	public void setPhone(String phone) throws RemoteException, EJBException {
		this.phone = phone;
	}

	public String getDate(Handle langHandle) throws RemoteException, EJBException {
		return LocaleUtil.date2string(langHandle, dateOfAppointment);
	}

	public void setDate(Handle langHandle, String dateOfAppointment) throws RemoteException, EJBException, java.text.ParseException {
		this.dateOfAppointment = LocaleUtil.string2date(langHandle, dateOfAppointment);
	}

	public String getDateOfChange(Handle langHandle) throws RemoteException, EJBException {
		if (dateOfChange != null)
			return LocaleUtil.date2string(langHandle, dateOfChange);
		else
			return "";
	}

	public void setDateOfChange(Handle langHandle, String dateOfChange) throws RemoteException, EJBException, java.text.ParseException {
		if (!dateOfChange.trim().equals(""))
			this.dateOfChange = LocaleUtil.string2date(langHandle, dateOfChange);
		else
			this.dateOfChange = null;
	}

	public String getRegistration1(Handle langHandle) throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		String s = "";
		try {
			s = LocaleUtil.getString(con, langPK, registration1ID);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return s;
	}

	public void setRegistration1(Handle langHandle, String registration1Str) throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		try {
			LocaleUtil.setString(con, langPK, registration1ID, registration1Str);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public String getRegistration2(Handle langHandle) throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		String s = "";
		try {
			s = LocaleUtil.getString(con, langPK, registration2ID);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return s;
	}

	public void setRegistration2(Handle langHandle, String registration2Str) throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		try {
			LocaleUtil.setString(con, langPK, registration2ID, registration2Str);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public String getRegistration3(Handle langHandle) throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		String s = "";
		try {
			s = LocaleUtil.getString(con, langPK, registration3ID);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return s;
	}

	public void setRegistration3(Handle langHandle, String registration3Str) throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		try {
			LocaleUtil.setString(con, langPK, registration3ID, registration3Str);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public String getComments1(Handle langHandle) throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		String s = "";
		try {
			s = LocaleUtil.getString(con, langPK, comments1ID);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return s;
	}

	public void setComments1(Handle langHandle, String comments1Str) throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		try {
			LocaleUtil.setString(con, langPK, comments1ID, comments1Str);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public String getComments2(Handle langHandle) throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		String s = "";
		try {
			s = LocaleUtil.getString(con, langPK, comments2ID);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return s;
	}

	public void setComments2(Handle langHandle, String comments2Str) throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		try {
			LocaleUtil.setString(con, langPK, comments2ID, comments2Str);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public BankPK getBankPK() throws RemoteException, EJBException {
		return new BankPK(bankID);
	}

	public void setBankPK(BankPK bankPK) throws RemoteException, EJBException {
		bankID = bankPK.getId();
	}
}
