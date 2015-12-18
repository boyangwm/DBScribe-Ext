/*
 * LicenceBean.java
 *
 * Created on November 12, 2001, 1:34 PM
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

import fina2.FinaTypeException;
import fina2.FinaTypeException.Type;
import fina2.db.DatabaseUtil;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;

/**
 * 
 * @author Kakha Kakhidze
 * @version
 */
public class LicenceBean implements EntityBean {

	private EntityContext ctx;
	private LicencePK pk;
	private int typeID;
	private String code;
	private Date date;
	private Date dateOfChange;
	// public int registrationFormID;
	private int reasonID;
	private int operational;
	private int bankID;

	/** Creates new LicenceBean */
	public LicencePK ejbCreate() throws EJBException, CreateException {
		LicencePK pk = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement insert = null;
		try {
			date = new Date();
			dateOfChange = null;
			reasonID = LocaleUtil.allocateString(con);
			// registrationFormID = LocaleUtil.allocateString(con);

			ps = con.prepareStatement("select max(id) from IN_LICENCES");
			insert = con.prepareStatement("insert into IN_LICENCES ( "
					+ "id, reasonStrID) " + "values(?,?)");
			rs = ps.executeQuery();

			if (rs.next())
				pk = new LicencePK(rs.getInt(1) + 1);
			else
				pk = new LicencePK(1);

			insert.setInt(1, pk.getId());
			insert.setInt(2, reasonID);

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

	public LicencePK ejbFindByPrimaryKey(LicencePK pk) throws EJBException,
			FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id from IN_LICENCES where id=?");
			ps.setInt(1, pk.getId());
			rs = ps.executeQuery();

			if (!rs.next()) {
				throw new FinderException("Licence is not found.");
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
			ps = con.prepareStatement("update IN_LICENCES set " + "typeID=?, "
					+ "code=?,  " + "creationDate=?, " + "dateOfChange=?, "
					+ "reasonStrID=?, " + "operational=?, " + "bankID=? "
					+ "where id=?");
			ps.setInt(1, this.typeID);
			ps.setString(2, this.code + "");
			ps.setDate(3, new java.sql.Date(date.getTime()));
			if (dateOfChange != null)
				ps.setDate(4, new java.sql.Date(dateOfChange.getTime()));
			else
				ps.setNull(4, java.sql.Types.DATE);
			ps.setInt(5, reasonID);
			ps.setInt(6, operational);
			ps.setInt(7, bankID);
			ps.setInt(8, ((LicencePK) ctx.getPrimaryKey()).getId());

			ps.executeUpdate();

			this.pk = (LicencePK) ctx.getPrimaryKey();
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
			ps = con
					.prepareStatement("select typeID, code, creationDate, dateOfChange, reasonStrID, operational, bankID "
							+ "from IN_LICENCES where id=?");
			ps.setInt(1, ((LicencePK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			rs.next();
			typeID = rs.getInt("typeID");
			code = rs.getString("code").trim();
			date = rs.getDate("creationDate");
			dateOfChange = rs.getDate("dateOfChange");
			reasonID = rs.getInt("reasonStrID");
			operational = rs.getInt("operational");
			bankID = rs.getInt("bankID");

			this.pk = (LicencePK) ctx.getPrimaryKey();
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
			ps = con
					.prepareStatement("select reasonStrID from IN_LICENCES where id=?");
			ps.setInt(1, ((LicencePK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();

			rs.next();
			del = con.prepareStatement("delete from SYS_STRINGS where id=?");
			del.setInt(1, rs.getInt(1));
			del.executeUpdate();

			DatabaseUtil.closeStatement(ps);
			ps = con
					.prepareStatement("delete from IN_LICENCES " + "where id=?");
			ps.setInt(1, ((LicencePK) ctx.getPrimaryKey()).getId());
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

	public LicenceTypePK getTypePK() {
		return new LicenceTypePK(typeID);
	}

	public void setTypePK(LicenceTypePK pk) {
		this.typeID = pk.getId();
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) throws RemoteException, EJBException,
			FinaTypeException {
		if (code.equals(""))
			code = " ";

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select id from IN_LICENCES where rtrim(code)=? and id != ?");
			ps.setString(1, code.trim());
			ps.setInt(2, ((LicencePK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			if (rs.next()) {
				throw new FinaTypeException(Type.CODE_NOT_UNIQUE, new String[] {
						"FinaTypeException.Bank.Licence", code });
			}
			this.code = code.trim();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
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

	/*
        public String getRegistrationForm(Handle langHandle) throws RemoteException, EJBException {
            LanguagePK langPK = (LanguagePK)langHandle.getEJBObject().getPrimaryKey();
            Connection con = DatabaseUtil.getConnection();
            String s = "";
            try {
                s = LocaleUtil.getString(con, langPK, registrationFormID);
                con.close();
            }  catch(SQLException e) {
                try { con.close(); } catch(SQLException ex) {}
                throw new EJBException(e);
            }

            return s;
        }

        public void setRegistrationForm(Handle langHandle, String registrationForm) throws RemoteException, EJBException {

            LanguagePK langPK = (LanguagePK)langHandle.getEJBObject().getPrimaryKey();

            Connection con = DatabaseUtil.getConnection();

            try {

                LocaleUtil.setString(con, langPK, registrationFormID, registrationForm);

                con.close();
            } catch(SQLException e) {
                try { con.close(); } catch(SQLException ex) {}
                throw new EJBException(e);
            }

        }
	 */
	public String getReason(Handle langHandle) throws RemoteException,
			EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		String s = "";
		try {
			s = LocaleUtil.getString(con, langPK, reasonID);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return s;
	}

	public void setReason(Handle langHandle, String paramReason)
			throws RemoteException, EJBException {

		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();

		Connection con = DatabaseUtil.getConnection();
		try {
			LocaleUtil.setString(con, langPK, reasonID, paramReason);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}

	public int getOperational() throws RemoteException, EJBException {
		return operational;
	}

	public void setOperational(int operational) throws RemoteException,
			EJBException {
		this.operational = operational;
	}

	public BankPK getBankPK() throws RemoteException, EJBException {
		return new BankPK(bankID);
	}

	public void setBankPK(BankPK bankPK) throws RemoteException, EJBException {
		this.bankID = bankPK.getId();
	}
}
