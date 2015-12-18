/*
 * PeriodBean.java
 *
 * Created on October 31, 2001, 4:15 AM
 */

package fina2.period;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
import fina2.i18n.LocaleUtil;
import fina2.util.LoggerHelper;

public class PeriodBean implements EntityBean {

	private EntityContext ctx;

	public PeriodPK pk;

	public int typeID;
	public String periodNumber;
	public java.util.Date fromDate;
	public java.util.Date toDate;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	private LoggerHelper log = new LoggerHelper(PeriodBean.class,
			"Period Definition");

	public PeriodPK ejbCreate() throws EJBException, CreateException {
		PeriodPK pk = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		PreparedStatement insert = null;
		ResultSet rs = null;
		try {

			fromDate = toDate = new java.util.Date();
			ps = con.prepareStatement("select max(id) from IN_PERIODS");
			insert = con.prepareStatement("insert into IN_PERIODS (id) "
					+ "values(?)");

			rs = ps.executeQuery();

			if (rs.next())
				pk = new PeriodPK(rs.getInt(1) + 1);
			else
				pk = new PeriodPK(1);

			// code = String.valueOf(pk.getId());
			insert.setInt(1, pk.getId());

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

	public PeriodPK ejbFindByPrimaryKey(PeriodPK pk) throws EJBException,
			FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id from IN_PERIODS where id=?");
			ps.setInt(1, pk.getId());
			rs = ps.executeQuery();

			if (!rs.next()) {
				throw new FinderException("Period is not found.");
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
			ps = con.prepareStatement("update IN_PERIODS set "
					+ "periodTypeID=?, " + "periodNumber=?, " + "fromDate=?, "
					+ "toDate=? " + "where id=?");
			ps.setInt(1, typeID);
			ps.setString(2, periodNumber);
			ps.setDate(3, new java.sql.Date(fromDate.getTime()));
			ps.setDate(4, new java.sql.Date(toDate.getTime()));
			ps.setInt(5, ((PeriodPK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();

			this.pk = (PeriodPK) ctx.getPrimaryKey();
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
					.prepareStatement("select periodTypeID,periodNumber,fromDate,toDate "
							+ "from IN_PERIODS where id=?");
			ps.setInt(1, ((PeriodPK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			rs.next();
			typeID = rs.getInt("periodTypeID");
			periodNumber = rs.getString("periodNumber");
			fromDate = rs.getDate("fromDate");
			toDate = rs.getDate("toDate");

			this.pk = (PeriodPK) ctx.getPrimaryKey();
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
					.prepareStatement("select id from IN_SCHEDULES where periodID=?");
			exec.setInt(1, ((PeriodPK) ctx.getPrimaryKey()).getId());
			rs = exec.executeQuery();
			if (!rs.next()) {
				PreparedStatement ps = con
						.prepareStatement("delete from IN_PERIODS "
								+ "where id=?");
				ps.setInt(1, ((PeriodPK) ctx.getPrimaryKey()).getId());
				ps.executeUpdate();
				DatabaseUtil.closeStatement(ps);
			} else {
				throw new EJBException();
			}
			int objectId = ((PeriodPK) ctx.getPrimaryKey()).getId();
			String user = ctx.getCallerPrincipal().getName();
			log.logObjectRemove(objectId, user);
			log.logPropertyValue("period number", this.periodNumber, objectId,
					user);
			log.logPropertyValue("start date", getDate(this.fromDate),
					objectId, user);
			log.logPropertyValue("end date", getDate(this.toDate), objectId,
					user);
			log.logPropertyValue("type id", this.typeID, objectId, user);
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

	public PeriodTypePK getType() throws RemoteException, EJBException {
		return new PeriodTypePK(typeID);
	}

	public void setType(PeriodTypePK typePK) throws RemoteException,
			EJBException {
		log.logPropertySet("type id", typePK.getId(), this.typeID,
				((PeriodPK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());
		typeID = typePK.getId();
	}

	public String getPeriodNumber() throws RemoteException, EJBException {
		return periodNumber;
	}

	public void setPeriodNumber(String periodNumber) throws RemoteException,
			EJBException {
		log.logPropertySet("period number", periodNumber, this.periodNumber,
				((PeriodPK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());
		this.periodNumber = periodNumber;
	}

	public String getFromDate(Handle languageHandle) throws RemoteException,
			EJBException {

		return LocaleUtil.date2string(languageHandle, fromDate);

	}

	public void setFromDate(Handle languageHandle, String fromDate)
			throws RemoteException, EJBException, java.text.ParseException {

		java.util.Date fromDateTmp = LocaleUtil.string2date(languageHandle,
				fromDate);
		log.logPropertySet("start date", getDate(fromDateTmp),
				(this.fromDate == null) ? null : getDate(this.fromDate),
				((PeriodPK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());

		this.fromDate = fromDateTmp;

	}

	public String getToDate(Handle languageHandle) throws RemoteException,
			EJBException {

		return LocaleUtil.date2string(languageHandle, toDate);

	}

	public void setToDate(Handle languageHandle, String toDate)
			throws RemoteException, EJBException, java.text.ParseException,
			FinaTypeException {

		java.util.Date toDateTmp = LocaleUtil.string2date(languageHandle,
				toDate);
		log.logPropertySet("end date", getDate(toDateTmp),
				(this.toDate == null) ? null : getDate(this.toDate),
				((PeriodPK) ctx.getPrimaryKey()).getId(), ctx
						.getCallerPrincipal().getName());

		this.toDate = toDateTmp;
		/*
		 * if(this.toDate.getHours() > 12) {
		 * this.toDate.setDate(this.toDate.getDate()+1);
		 * this.toDate.setHours(12); }
		 */

		log.getLogger().debug("setToDate " + toDate + " " + this.toDate);
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con
					.prepareStatement("select id from IN_PERIODS where fromDate=? and toDate=? and periodTypeID=? and id!=?");
			ps.setDate(1, new java.sql.Date(fromDate.getTime()));
			ps.setDate(2, new java.sql.Date(this.toDate.getTime()));
			ps.setInt(3, typeID);
			ps.setInt(4, ((PeriodPK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			if (rs.next()) {
				throw new FinaTypeException(Type.CODE_NOT_UNIQUE, new String[] {
						"FinaTypeException.Period.Date", toDate });
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	private String getDate(Date date) {
		return dateFormat.format(date);
	}
}
