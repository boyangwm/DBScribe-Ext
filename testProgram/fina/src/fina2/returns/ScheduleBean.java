/*
 * ScheduleBean.java
 *
 * Created on November 6, 2001, 4:03 PM
 */

package fina2.returns;

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

import fina2.FinaTypeException;
import fina2.FinaTypeException.Type;
import fina2.bank.BankPK;
import fina2.db.DatabaseUtil;
import fina2.period.PeriodPK;
import fina2.util.LoggerHelper;

/**
 * 
 * @author David Shalamberidze
 * @version
 */
public class ScheduleBean implements EntityBean {

	private EntityContext ctx;

	public SchedulePK pk;
	public int bank;
	public long definition;
	public int period;
	public int delay;

	private LoggerHelper log = new LoggerHelper(ScheduleBean.class, "Schedule");

	public SchedulePK ejbCreate(BankPK bankPK, ReturnDefinitionPK definitionPK, PeriodPK periodPK) throws FinaTypeException, EJBException, CreateException {
		pk = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement insert = null;
		try {
			ps = con.prepareStatement("select id from IN_SCHEDULES where bankID=? and definitionID=? and periodID=?");
			ps.setInt(1, bankPK.getId());
			ps.setLong(2, definitionPK.getId());
			ps.setInt(3, periodPK.getId());

			rs = ps.executeQuery();
			if (rs.next()) {
				throw new FinaTypeException(Type.RETURN_SCHEDULE_NOT_UNIQUE);
			}
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(ps);

			ps = con.prepareStatement("select max(id) from IN_SCHEDULES");
			insert = con.prepareStatement("insert into IN_SCHEDULES (id) " + "values(?)");

			rs = ps.executeQuery();

			if (rs.next())
				pk = new SchedulePK(rs.getInt(1) + 1);
			else
				pk = new SchedulePK(1);

			// code = String.valueOf(pk.getId());

			insert.setInt(1, pk.getId());

			insert.executeUpdate();

			log.logObjectCreate(pk.getId(), ctx.getCallerPrincipal().getName());
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(insert);
		}
		return pk;
	}

	public void ejbPostCreate(BankPK bankPK, ReturnDefinitionPK definitionPK, PeriodPK periodPK) throws FinaTypeException, EJBException, CreateException {
	}

	public SchedulePK ejbFindByPrimaryKey(SchedulePK pk) throws EJBException, FinderException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id from IN_SCHEDULES where id=?");
			ps.setInt(1, pk.getId());
			rs = ps.executeQuery();
			if (!rs.next())
				throw new FinderException("Schedule is not found.");
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
			ps = con.prepareStatement("update IN_SCHEDULES set " + "bankID=?, " + "definitionID=?, " + "periodID=?, " + "delay=? " + "where id=?");
			ps.setInt(1, bank);
			ps.setLong(2, definition);
			ps.setInt(3, period);
			ps.setInt(4, delay);
			ps.setInt(5, ((SchedulePK) ctx.getPrimaryKey()).getId());

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
			ps = con.prepareStatement("select bankID, definitionID, periodID, delay " + "from IN_SCHEDULES where id=?");
			ps.setInt(1, ((SchedulePK) ctx.getPrimaryKey()).getId());

			rs = ps.executeQuery();
			rs.next();
			bank = rs.getInt("bankID");
			definition = rs.getInt("definitionID");
			period = rs.getInt("periodID");
			delay = rs.getInt("delay");
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public void setEntityContext(javax.ejb.EntityContext ctx) throws javax.ejb.EJBException, java.rmi.RemoteException {
		this.ctx = ctx;
	}

	public boolean canDelete(int id) {
		boolean b = true;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement exec = null;
		ResultSet rs = null;
		try {
			exec = con.prepareStatement("select id from IN_RETURNS where scheduleID=?");
			exec.setInt(1, id);
			rs = exec.executeQuery();
			if (rs.next())
				b = false;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			DatabaseUtil.close(rs, exec, con);
		}
		return b;
	}

	public void ejbRemove() throws javax.ejb.RemoveException, javax.ejb.EJBException, java.rmi.RemoteException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps=null;
		try {
			ps = con.prepareStatement("delete from IN_SCHEDULES where id=?");
			ps.setInt(1, ((SchedulePK) ctx.getPrimaryKey()).getId());
			ps.executeUpdate();

			int objectId = ((SchedulePK) ctx.getPrimaryKey()).getId();
			String user = ctx.getCallerPrincipal().getName();
			log.logObjectRemove(objectId, user);
			log.logPropertyValue("bank id", this.bank, objectId, user);
			log.logPropertyValue("return definition id", this.definition, objectId, user);
			log.logPropertyValue("period id", this.period, objectId, user);
			log.logPropertyValue("due date", this.delay, objectId, user);

			DatabaseUtil.closeStatement(ps);

		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(null, ps, con);
		}
	}

	public void unsetEntityContext() throws javax.ejb.EJBException, java.rmi.RemoteException {
		ctx = null;
	}

	public void setBankPK(BankPK bankPK) {
		log.logPropertySet("bank id", bankPK.getId(), bank, ((SchedulePK) ctx.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());
		bank = bankPK.getId();
	}

	public BankPK getBankPK() {
		return new BankPK(bank);
	}

	public void setReturnDefinitionPK(ReturnDefinitionPK returnDefinitionPK) {
		log.logPropertySet("return definition id", returnDefinitionPK.getId(), definition, ((SchedulePK) ctx.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());
		definition = returnDefinitionPK.getId();
	}

	public ReturnDefinitionPK getReturnDefinitionPK() {
		return new ReturnDefinitionPK(definition);
	}

	public void setPeriodPK(PeriodPK periodPK) {
		log.logPropertySet("period id", periodPK.getId(), period, ((SchedulePK) ctx.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());
		period = periodPK.getId();
	}

	public PeriodPK getPeriodPK() {
		return new PeriodPK(period);
	}

	public void setDelay(int d) {
		log.logPropertySet("due date", d, delay, ((SchedulePK) ctx.getPrimaryKey()).getId(), ctx.getCallerPrincipal().getName());
		delay = d;
	}

	public int getDelay() {
		return delay;
	}
}
