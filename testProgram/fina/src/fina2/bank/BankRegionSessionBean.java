/*
 * BankRegionSessionBean.java
 *
 * Created on March 25, 2002, 12:37 AM
 */

package fina2.bank;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import fina2.FinaTypeException;
import fina2.db.DatabaseUtil;
import fina2.i18n.LanguagePK;
import fina2.security.ServerSecurityUtil;
import fina2.ui.table.TableRowImpl;

/**
 * 
 * @author vasop
 * @version
 */
@Deprecated
public class BankRegionSessionBean implements SessionBean {

	private SessionContext ctx;

	/** Creates new BankRegionSessionBean */
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

	public Collection getRegionRows(Handle userHandle, Handle languageHandle) throws FinaTypeException, RemoteException, EJBException {
		fina2.security.User user = (fina2.security.User) userHandle.getEJBObject();

		ServerSecurityUtil.checkUserPermissions(userHandle, "fina2.bank.amend", "fina2.bank.review");

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		try {
			ps = con.prepareStatement("select a.id, b.value, c.value " + "from IN_REGIONS a, SYS_STRINGS b, SYS_STRINGS c " + "where b.id=a.cityStrID and (b.langID=?) " + "and c.id=a.regionStrID and (b.langID=?) " + "order by a.id, b.langID, c.langID DESC");
			ps.setInt(1, ((LanguagePK) languageHandle.getEJBObject().getPrimaryKey()).getId());
			ps.setInt(2, ((LanguagePK) languageHandle.getEJBObject().getPrimaryKey()).getId());
			rs = ps.executeQuery();

			TableRowImpl prevRow = null;
			while (rs.next()) {
				TableRowImpl row = new TableRowImpl(new BankRegionPK(rs.getInt(1)), 2);
				row.setValue(0, rs.getString(2).trim());
				row.setValue(1, rs.getString(3).trim());

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

	public boolean cityExists(String cityName) throws RemoteException, EJBException {
		boolean exists = false;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT s.value as city FROM SYS_STRINGS s,IN_REGIONS c WHERE c.citystrid=s.id");
			rs = ps.executeQuery();
			while (rs.next()) {
				String selectedCity = rs.getString("city");
				if (selectedCity.trim().toLowerCase().equals(cityName.trim().toLowerCase())) {
					exists = true;
					break;
				}
			}
		} catch (Exception ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return exists;
	}

	public void addCityRegion(String city, String region, Handle languageHandle) throws RemoteException, EJBException {
		int langId = ((LanguagePK) languageHandle.getEJBObject().getPrimaryKey()).getId();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT MAX(ID) as MAX_STRING_ID FROM SYS_STRINGS ");
			rs = ps.executeQuery();
			if (rs.next()) {
				int stringMaxId = rs.getInt("MAX_STRING_ID");
				int cityStrId = stringMaxId + 1;
				int regionStrId = cityStrId + 1;
				PreparedStatement pss = con.prepareStatement("SELECT MAX(ID) as MAX_REG_ID FROM IN_REGIONS ");
				ResultSet rss = pss.executeQuery();
				int id = 0;
				if (rss.next())
					id = rss.getInt("MAX_REG_ID") + 1;
				rss.close();
				pss.close();
				ps = con.prepareStatement("INSERT INTO IN_REGIONS(id,citystrid,regionstrid) VALUES(?,?,?)");
				ps.setInt(1, id);
				ps.setInt(2, cityStrId);
				ps.setInt(3, regionStrId);
				ps.execute();
				ps = con.prepareStatement("INSERT INTO SYS_STRINGS(id,langid,value) VALUES(?,?,?)");
				ps.setInt(1, cityStrId);
				ps.setInt(2, langId);
				ps.setString(3, city);
				ps.execute();
				ps = con.prepareStatement("INSERT INTO SYS_STRINGS(id,langid,value) VALUES(?,?,?)");
				ps.setInt(1, regionStrId);
				ps.setInt(2, langId);
				ps.setString(3, region);
				ps.execute();
			}
		} catch (Exception ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public void updateCityRegion(int id, String city, String region) throws RemoteException, EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = con.prepareStatement("SELECT * FROM IN_REGIONS WHERE id=?");
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				int cityId = rs.getInt("citystrid");
				int regionId = rs.getInt("regionstrid");
				ps = con.prepareStatement("UPDATE SYS_STRINGS SET value=? WHERE id=?");
				ps.setString(1, city);
				ps.setInt(2, cityId);
				ps.execute();
				ps = con.prepareStatement("UPDATE SYS_STRINGS SET value=? WHERE id=?");
				ps.setString(1, region);
				ps.setInt(2, regionId);
				ps.execute();
			}
		} catch (Exception ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	public int getId(String city, Handle languageHandle) throws RemoteException, EJBException {
		int id = -1;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		int langId = ((LanguagePK) languageHandle.getEJBObject().getPrimaryKey()).getId();
		try {
			ps = con.prepareStatement("SELECT id as CITY_ID FROM IN_REGIONS WHERE RTRIM(citystrid) in(SELECT s.id FROM SYS_STRINGS s WHERE s.value=? AND s.id IN(SELECT citystrid from IN_REGIONS) AND s.langid=?)");
			ps.setString(1, city);
			ps.setInt(2, langId);
			rs = ps.executeQuery();
			if (rs.next())
				id = rs.getInt("CITY_ID");
		} catch (Exception ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return id;
	}

	public boolean removeCity(int id, Handle languageHandle) throws RemoteException, EJBException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean removed = true;
		int langId = ((LanguagePK) languageHandle.getEJBObject().getPrimaryKey()).getId();
		try {
			ps = con.prepareStatement("SELECT s.value as CITY FROM SYS_STRINGS s WHERE s.id IN(SELECT r.citystrid FROM IN_REGIONS r WHERE r.id=?) AND s.langid=?");
			ps.setInt(1, id);
			ps.setInt(2, langId);
			rs = ps.executeQuery();
			if (rs.next()) {
				String cityToRemove = rs.getString("CITY");
				ps = con.prepareStatement("SELECT b.telex AS bank_city_region FROM IN_BANKS b WHERE RTRIM(b.telex) IS NOT NULL AND b.telex!=' '");
				rs = ps.executeQuery();
				while (rs.next()) {
					String cityRegion = rs.getString("bank_city_region");
					StringTokenizer st = new StringTokenizer(cityRegion, "/");
					String city = st.nextToken();
					if (city.trim().toLowerCase().equals(cityToRemove.trim().toLowerCase())) {
						removed = false;
						break;
					}

				}
				if (removed) {
					ps = con.prepareStatement("DELETE FROM IN_REGIONS WHERE id=?");
					ps.setInt(1, id);
					ps.execute();
				}
			}
		} catch (Exception ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return removed;
	}

	public boolean regionCityExists(String city, String region) throws RemoteException, EJBException {
		boolean exists = false;

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT i1.CITYSTRID,i2.REGIONSTRID,s1.value,s2.value FROM SYS_STRINGS s1,SYS_STRINGS s2,IN_REGIONS i1,IN_REGIONS i2 WHERE i1.citystrid=s1.id AND RTRIM(s1.value)=? AND i2.regionstrid=s2.id AND RTRIM(s2.value)=?");
			ps.setString(1, city);
			ps.setString(2, region);
			rs = ps.executeQuery();
			while(rs.next()) {
				if (Math.abs(rs.getInt(2) - rs.getInt(1)) == 1) {
					exists = true;
					break;
				}
			}
		} catch (Exception ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return exists;
	}
}
