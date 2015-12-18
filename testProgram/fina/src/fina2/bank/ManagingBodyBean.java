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

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.Handle;

import fina2.db.DatabaseUtil;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;

public class ManagingBodyBean implements EntityBean {

	private EntityContext ctx;
	public ManagingBodyPK pk;

	public int managingBodyInt;

	/** Creates new BranchBean */
	public ManagingBodyPK ejbCreate() throws EJBException, CreateException {
		ManagingBodyPK pk = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement insert = null;
		try {

			managingBodyInt = LocaleUtil.allocateString(con);

			ps = con.prepareStatement("select max(id) from IN_MANAGING_BODIES");

			insert = con.prepareStatement("insert into IN_MANAGING_BODIES ( "
					+ "id, postStrId) " + "values(?,?)");
			rs = ps.executeQuery();

			if (rs.next())
				pk = new ManagingBodyPK(rs.getInt(1) + 1);
			else
				pk = new ManagingBodyPK(1);

			insert.setInt(1, pk.getId());
			insert.setInt(2, managingBodyInt);
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

	public ManagingBodyPK ejbFindByPrimaryKey(ManagingBodyPK pk)
			throws EJBException, FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select id from IN_MANAGING_BODIES where id=?");
			ps.setInt(1, pk.getId());
			rs = ps.executeQuery();

			if (!rs.next()) {
				throw new FinderException("Managing Body is not found.");
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
			ps = con.prepareStatement("update IN_MANAGING_BODIES set "
					+ "postStrId=? " + "where id=?");
			ps.setInt(1, managingBodyInt);
			ps.setInt(2, ((ManagingBodyPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();

			this.pk = (ManagingBodyPK) ctx.getPrimaryKey();
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
			ps = con.prepareStatement("select id, postStrID "
					+ "from IN_MANAGING_BODIES where id=?");
			ps.setInt(1, ((ManagingBodyPK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			rs.next();
			managingBodyInt = rs.getInt("postStrID");

			this.pk = (ManagingBodyPK) ctx.getPrimaryKey();
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
					.prepareStatement("select postStrId from IN_MANAGING_BODIES where id=?");
			ps.setInt(1, ((ManagingBodyPK) ctx.getPrimaryKey()).getId());
			rs = ps.executeQuery();

			rs.next();
			del = con.prepareStatement("delete from SYS_STRINGS where id=?");
			del.setInt(1, rs.getInt(1));
			del.executeUpdate();

			DatabaseUtil.closeStatement(ps);
			ps = con.prepareStatement("delete from IN_MANAGING_BODIES "
					+ "where id=?");
			ps.setInt(1, ((ManagingBodyPK) ctx.getPrimaryKey()).getId());
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

	public String getManagingBody(Handle langHandle) throws RemoteException,
			EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		String s = "";
		try {
			s = LocaleUtil.getString(con, langPK, managingBodyInt);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
		return s;
	}

	public void setManagingBody(Handle langHandle, String managingBodyString)
			throws RemoteException, EJBException {
		LanguagePK langPK = (LanguagePK) langHandle.getEJBObject()
				.getPrimaryKey();
		Connection con = DatabaseUtil.getConnection();
		try {
			LocaleUtil.setString(con, langPK, managingBodyInt,
					managingBodyString);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
		}
	}
}
