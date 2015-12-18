/*
 * LicenceTypeBean.java
 *
 * Created on November 7, 2001, 5:54 PM
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
import javax.ejb.RemoveException;

import fina2.db.DatabaseUtil;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;
import fina2.util.LoggerHelper;

/**
 * 
 * @author Kakha Kakhidze
 * @version
 */
public class LicenceTypeBean implements EntityBean {

	private EntityContext ctx;
	public LicenceTypePK pk;
	public int description;
	private String oldDescription;
	private LoggerHelper log = new LoggerHelper(LicenceTypeBean.class, "License Type");

	public LicenceTypePK ejbCreate() throws EJBException, CreateException {
		LicenceTypePK pk = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement insert = null;
		try {

			description = LocaleUtil.allocateString(con);
			ps = con.prepareStatement("select max(id) from IN_LICENCE_TYPES");
			insert = con.prepareStatement("insert into IN_LICENCE_TYPES ( " + "id,nameStrID) " + "values(?,?)");
			rs = ps.executeQuery();

			if (rs.next())
				pk = new LicenceTypePK(rs.getInt(1) + 1);
			else
				pk = new LicenceTypePK(1);

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

	public LicenceTypePK ejbFindByPrimaryKey(LicenceTypePK pk) throws EJBException, FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id from IN_LICENCE_TYPES where id=?");
			ps.setInt(1, pk.getId());
			rs = ps.executeQuery();

			if (!rs.next()) {
				throw new FinderException("Licence Type is not found.");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return pk;
	}

	public void ejbStore() throws javax.ejb.EJBException, java.rmi.RemoteException {

		log.logObjectStore();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("update IN_LICENCE_TYPES set " + "nameStrID=? " + "where id=?");
			ps.setInt(1, description);
			ps.setInt(2, ((LicenceTypePK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();

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
			ps = con.prepareStatement("select nameStrID " + "from IN_LICENCE_TYPES where id=?");
			ps.setInt(1, ((LicenceTypePK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			rs.next();
			description = rs.getInt("nameStrID");
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
			ps = con.prepareStatement("select id,bankid from IN_LICENCES where TYPEID=?");
			ps.setInt(1, ((LicenceTypePK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();
			String bankCodes = "";
			String sep = ", ";
			PreparedStatement codePS = null;
			ResultSet codesRS = null;
			boolean isUsed = false;
			while (rs.next()) {
				codePS = con.prepareStatement("select code from IN_BANKS where id=?");
				codePS.setInt(1, rs.getInt("bankid"));
				codesRS = codePS.executeQuery();
				if (codesRS.next()) {
					bankCodes += codesRS.getString("code");
					bankCodes += sep;
				}
				isUsed = true;
			}
			if (!bankCodes.equals(""))
				bankCodes = bankCodes.substring(0, bankCodes.lastIndexOf(sep));
			if (isUsed) {
				throw new javax.ejb.RemoveException(bankCodes);
			}

			DatabaseUtil.closeResultSet(codesRS);
			DatabaseUtil.closeStatement(codePS);

			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("select nameStrID from IN_LICENCE_TYPES where id=?");
			ps.setInt(1, ((LicenceTypePK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();

			rs.next();
			del = con.prepareStatement("delete from SYS_STRINGS where id=?");
			del.setInt(1, rs.getInt(1));
			del.executeUpdate();

			DatabaseUtil.closeStatement(ps);
			ps = con.prepareStatement("delete from IN_LICENCE_TYPES " + "where id=?");
			ps.setInt(1, ((LicenceTypePK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();

			int objectId = ((LicenceTypePK) ctx.getPrimaryKey()).getId();
			String user = ctx.getCallerPrincipal().getName();
			log.logObjectRemove(objectId, user);
		} catch (RemoveException e) {
			throw e;
		} catch (Exception e) {
			log.getLogger().error(e.getMessage(), e);
		} finally {

			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(del);
		}
	}

	public void unsetEntityContext() throws javax.ejb.EJBException, java.rmi.RemoteException {
		ctx = null;
	}

	public String getDescription(Handle langHandle) throws RemoteException, EJBException {

		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();

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

	public void setDescription(Handle langHandle, String desc) throws RemoteException, EJBException {

		log.logPropertySet("description", desc, this.oldDescription, ((LicenceTypePK) ctx.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());
		this.oldDescription = desc;

		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject().getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		try {
			LocaleUtil.setString(con, langPK, description, desc);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}
}
