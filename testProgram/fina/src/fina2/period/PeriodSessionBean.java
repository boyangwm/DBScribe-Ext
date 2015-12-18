/*
 * PeriodSessionBean.java
 *
 * Created on October 30, 2001, 3:15 AM
 */

package fina2.period;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.log4j.Logger;

import fina2.FinaTypeException;
import fina2.db.DatabaseUtil;
import fina2.i18n.Language;
import fina2.i18n.LanguagePK;
import fina2.i18n.LocaleUtil;
import fina2.security.ServerSecurityUtil;
import fina2.ui.table.TableRow;
import fina2.ui.table.TableRowImpl;

/**
 * 
 * @author vasop
 */
public class PeriodSessionBean implements SessionBean {

	private SessionContext ctx;
	private Logger log = Logger.getLogger(PeriodSessionBean.class);

	public void ejbCreate() throws CreateException, EJBException, RemoteException {
		/* Write your code here */
	}

	public void ejbActivate() throws EJBException {
		/* Write your code here */
	}

	public void ejbPassivate() throws EJBException {
		/* Write your code here */
	}

	public void ejbRemove() throws EJBException {
		/* Write your code here */
	}

	public void setSessionContext(SessionContext ctx) throws EJBException {
		this.ctx = ctx;
	}

	/**
	 * Returns collection of fina.ui.table.TableRow objects with descriptions
	 * according to specified language. PermissionDeniedException may be thrown
	 * if user has not permission to access this feature.
	 * 
	 * <b>Permissions:</b> Periods.Types.Review - Review list of period types.
	 * 
	 * @see EJBTable Pattern
	 * @since 1
	 */

	public Collection getPeriodRows(Handle userHandle, Handle languageHandle, String type, Date fromDate, Date toDate) throws RemoteException, FinaTypeException {
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.periods.amend", "fina2.periods.review");

		String filtr1 = " ";
		String filtr2 = " ";
		String filtr3 = " ";

		if (!type.trim().equalsIgnoreCase("ALL"))
			filtr1 = " and b.value=\'" + type.trim() + "\' ";
		if (!fromDate.equals(new Date(0L)))
			filtr2 = " and a.fromDate >= ? ";
		if (!toDate.equals(new Date(0L)))
			filtr3 = " and a.toDate <= ? ";

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		try {
			ps = con.prepareStatement("select a.id, b.value, a.periodNumber, a.fromDate, a.toDate " + "from IN_PERIODS a, SYS_STRINGS b, IN_PERIOD_TYPES c "
					+ "where c.id=a.periodTypeID and b.id=c.nameStrID and (b.langID=? or b.langID=1) " + filtr1 + filtr2 + filtr3 + "order by a.id, b.langID DESC");
			ps.setInt(1, langID);

			if (!fromDate.equals(new Date(0L)))
				ps.setDate(2, new java.sql.Date(fromDate.getTime()));
			if (!toDate.equals(new Date(0L))) {
				if (!fromDate.equals(new Date(0L)))
					ps.setDate(3, new java.sql.Date(toDate.getTime()));
				else
					ps.setDate(2, new java.sql.Date(toDate.getTime()));
			}

			rs = ps.executeQuery();

			// Language lang = (Language)languageHandle.getEJBObject();

			TableRowImpl prevRow = null;
			while (rs.next()) {
				TableRowImpl row = new TableRowImpl(new PeriodPK(rs.getInt(1)), 4);
				String desc = rs.getString(2);
				if (desc == null)
					desc = "";
				else
					desc = LocaleUtil.encode(desc.trim(), encoding);
				row.setValue(0, desc);
				String s = rs.getString(3);
				if (s == null)
					s = "";
				row.setValue(1, s.trim());
				row.setValue(2, LocaleUtil.date2string(lang, rs.getDate(4)));
				row.setValue(3, LocaleUtil.date2string(lang, rs.getDate(5)));

				if ((prevRow != null) && (prevRow.getPrimaryKey().equals(row.getPrimaryKey()))) {
					prevRow = row;
					continue;
				}
				prevRow = row;

				v.add(row);
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;
	}

	/** Returns a list of periods */
	public Collection getPeriodRows(Handle userHandle, Handle languageHandle) throws RemoteException, FinaTypeException {

		// Can throw PermissionDeniedException
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.periods.amend", "fina2.periods.review");

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector<TableRowImpl> v = new Vector<TableRowImpl>();
		try {
			String sql = "select a.id, b.namestrid, a.periodNumber, a.fromDate, a.toDate " + "from IN_PERIODS a, IN_PERIOD_TYPES b " + "where b.id=a.periodTypeID " + "order by b.id, a.fromdate";

			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			Language lang = (Language) languageHandle.getEJBObject();

			while (rs.next()) {

				// ID
				int id = rs.getInt(1);
				PeriodPK pk = new PeriodPK(id);
				TableRowImpl row = new TableRowImpl(pk, 5);

				// Period type
				String desc = LocaleUtil.getString(con, languageHandle, rs.getInt(2));
				row.setValue(0, desc);

				// Period number
				String number = rs.getString(3);
				row.setValue(1, number.trim());

				// From date
				String fromDate = LocaleUtil.date2string(lang, rs.getDate(4));
				row.setValue(2, fromDate);

				// To date
				String toDate = LocaleUtil.date2string(lang, rs.getDate(5));
				row.setValue(3, toDate);

				// Add to the result vector
				v.add(row);
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;
	}

	public Collection getPeriodTypeRows(Handle userHandle, Handle languageHandle) throws RemoteException, FinaTypeException {
		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.periods.amend", "fina2.periods.review");

		Language lang = (Language) languageHandle.getEJBObject();
		int langID = ((LanguagePK) lang.getPrimaryKey()).getId();
		String encoding = lang.getXmlEncoding();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		try {
			ps = con.prepareStatement("select a.id, a.code, b.value, b.langID from IN_PERIOD_TYPES a left outer join SYS_Strings b on b.id=a.nameStrID and (b.langID=?) order by a.id, b.langID DESC");
			ps.setInt(1, langID);
			rs = ps.executeQuery();
			TableRowImpl prevRow = null;
			while (rs.next()) {
				TableRowImpl row = new TableRowImpl(new PeriodTypePK(rs.getInt(1)), 2);
				String s = rs.getString(2);
				if (s == null)
					s = "";
				row.setValue(0, s.trim());

				String desc = rs.getString(3);
				if (desc == null)
					desc = "NONAME";
				else
					desc = LocaleUtil.encode(desc.trim(), encoding);
				row.setValue(1, desc);

				if ((prevRow != null) && (prevRow.getPrimaryKey().equals(row.getPrimaryKey()))) {
					prevRow = row;
					continue;
				}
				prevRow = row;

				v.add(row);
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;
	}

	public Collection getPeriodInsertRows(Handle languageHandle, String type, int frequencyType, java.util.Date fromDate, int startPeriodNumber, int numberOfPeriods) throws RemoteException,
			FinaTypeException {
		Vector v = new Vector();
		GregorianCalendar gc;
		Date d;
		int y = fromDate.getYear();
		int f;
		int p = 0;

		if (frequencyType == 0)
			f = 1;
		else if (frequencyType == 1)
			f = 3;
		else if (frequencyType == 2)
			f = 6;
		else
			f = 12;

		try {

			TableRowImpl prevRow = null;
			for (int i = 1; i <= 12; i++) {
				if ((i - ((int) (i / f)) * f) == 0) {
					gc = new GregorianCalendar(y, i - 1, 1);
					d = new Date(y, i - 1, 1);
					p++;
					if ((fromDate.equals(d) || fromDate.before(d)) && (startPeriodNumber <= p && (startPeriodNumber + numberOfPeriods - 1) >= p)) {

						TableRowImpl row = new TableRowImpl(null, 4);
						row.setValue(0, type);
						row.setValue(1, String.valueOf(p));
						row.setValue(2, LocaleUtil.date2string(languageHandle, new Date(y, i - (f - 1) - 1, 1)));
						row.setValue(3, LocaleUtil.date2string(languageHandle, new Date(y, i - 1, gc.getActualMaximum(gc.DAY_OF_MONTH))));

						v.add(row);
					}
				}
			}
		} catch (Exception e) {
			throw new EJBException(e);
		}
		return v;
	}

	public Collection getPeriodInsertRows(Handle languageHandle, String type, java.util.Date fromDate, int startPeriodNumber, int numberOfPeriods, int daysInPeriods, int daysBetweenPeriods)
			throws EJBException, FinaTypeException {

		Vector v = new Vector();

		Calendar fronDateCalendar = Calendar.getInstance();
		fronDateCalendar.setTime(fromDate);
		int year = fronDateCalendar.get(Calendar.YEAR);
		int month = fronDateCalendar.get(Calendar.MONTH);
		int day = fronDateCalendar.get(Calendar.DAY_OF_MONTH);

		Date currentDate;
		GregorianCalendar gc;
		int p = 0;

		try {

			gc = new GregorianCalendar(year, month, day);

			int minActual = gc.getActualMinimum(Calendar.DAY_OF_YEAR);

			do {
				currentDate = gc.getTime();

				p++;

				if ((fromDate.equals(currentDate) || fromDate.before(currentDate)) && (startPeriodNumber <= p && (startPeriodNumber + numberOfPeriods - 1) >= p)) {

					TableRowImpl row = new TableRowImpl(null, 4);
					row.setValue(0, type);
					row.setValue(1, String.valueOf(p));
					row.setValue(2, LocaleUtil.date2string(languageHandle, currentDate));

					Calendar tmpCalendar = (Calendar) gc.clone();
					tmpCalendar.add(Calendar.DAY_OF_MONTH, daysInPeriods - 1);

					row.setValue(3, LocaleUtil.date2string(languageHandle, tmpCalendar.getTime()));

					v.add(row);
				}

				gc.add(Calendar.DAY_OF_MONTH, daysInPeriods - 1);
				gc.add(Calendar.DAY_OF_MONTH, daysBetweenPeriods + 1);

			} while (gc.get(Calendar.DAY_OF_YEAR) > minActual);

		} catch (Exception e) {
			throw new EJBException(e);
		}

		return v;
	}

	public Collection savePeriods(Handle langHandle, PeriodTypePK typePK, Collection rows) throws EJBException, FinaTypeException {
		int typeID;
		Vector rep = new Vector();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ex = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ex = con.prepareStatement("select p.id, t.code from IN_PERIODS p, IN_PERIOD_TYPES t where p.fromDate=? and p.toDate=? and p.periodTypeID=? and p.periodTypeID = t.id ");

			ps = con.prepareStatement("update IN_PERIODS set " + "periodTypeID=?, " + "periodNumber=?, " + "fromDate=?, " + "toDate=? " + "where id=?");

			log.info("Autoinserting Periods.");
			typeID = typePK.getId();
			for (Iterator iter = rows.iterator(); iter.hasNext();) {
				TableRow tableRow = (TableRow) iter.next();

				java.sql.Date fromDate = new java.sql.Date(LocaleUtil.string2date(langHandle, tableRow.getValue(2)).getTime());
				java.sql.Date toDate = new java.sql.Date(LocaleUtil.string2date(langHandle, tableRow.getValue(3)).getTime());

				ex.setDate(1, fromDate);
				ex.setDate(2, toDate);
				ex.setInt(3, typeID);

				rs = ex.executeQuery();
				if (!rs.next()) {
					int periodId = ((PeriodPK) periodCreate()).getId();
					ps.setInt(1, typeID);
					ps.setString(2, tableRow.getValue(1));
					ps.setDate(3, fromDate);
					ps.setDate(4, toDate);
					ps.setInt(5, periodId);
					ps.executeUpdate();

					// { Logging
					StringBuffer buff = new StringBuffer();

					buff.append("Caller user: ");
					buff.append(ctx.getCallerPrincipal().getName());
					buff.append(", Period id: ");
					buff.append(periodId);
					buff.append(", Period type id: ");
					buff.append(typeID);
					buff.append(", Period number: ");
					buff.append(tableRow.getValue(1));
					buff.append(", Start date: ");
					buff.append(fromDate);
					buff.append(", End date: ");
					buff.append(toDate);

					log.info(buff.toString());
					// }
				} else {
					TableRowImpl row = new TableRowImpl(new PeriodPK(rs.getInt(1)), 3);
					String s = rs.getString(2);
					if (s == null)
						s = "";
					row.setValue(0, s.trim());
					row.setValue(1, tableRow.getValue(2));
					row.setValue(2, tableRow.getValue(3));
					rep.add(row);
				}
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(ex);
		}
		return rep;
	}

	private PeriodPK periodCreate() throws EJBException, CreateException {
		PeriodPK pk = null;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		PreparedStatement insert = null;
		ResultSet rs = null;
		try {

			ps = con.prepareStatement("select max(id) from IN_PERIODS");
			insert = con.prepareStatement("insert into IN_PERIODS (id) " + "values(?)");

			rs = ps.executeQuery();

			if (rs.next())
				pk = new PeriodPK(rs.getInt(1) + 1);
			else
				pk = new PeriodPK(1);

			insert.setInt(1, pk.getId());
			insert.executeUpdate();
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
			DatabaseUtil.closeStatement(insert);
		}
		return pk;
	}
}
