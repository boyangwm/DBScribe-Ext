package fina2.system;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.jboss.mx.util.MBeanServerLocator;

import fina2.db.DatabaseUtil;

public class PropertySessionBean implements SessionBean {

	private static Logger log = Logger.getLogger(PropertySessionBean.class);
	public static final String serverVersion = "3.4.4c";
	public static final int START_XML_PROCESS_SERVICE_TIMEOUT = 20000;

	/** Creates new ManagingBodySessionBean */
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

	public Map getSystemProperties() throws RemoteException {

		Map properties = new HashMap();

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("select prop_key, value from SYS_PROPERTIES");
			rs = ps.executeQuery();
			while (rs.next()) {
				properties.put(rs.getString("prop_key"), rs.getString("value"));
			}
			properties.put("database.name", con.getMetaData().getDatabaseProductName());
			properties.put("database.version", con.getMetaData().getDatabaseProductVersion());

			// find the local MBeanServera
			MBeanServer server = MBeanServerLocator.locateJBoss();
			properties.put("jboss.version", server.getAttribute(new ObjectName("jboss.system:type=Server"), "Version"));
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}

		return properties;
	}

	public void setSystemProperties(Map properties) throws RemoteException {
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DatabaseUtil.getConnection();
			for (Iterator iter = properties.keySet().iterator(); iter.hasNext();) {
				String key = (String) iter.next();

				ps = con.prepareStatement("update SYS_PROPERTIES set value = ? where prop_key = ?");
				ps.setString(1, (String) properties.get(key));
				ps.setString(2, key);
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	public void setSystemProperties(Map<String, String> properties, Map<String, String> bundleProps, String login) throws RemoteException {
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DatabaseUtil.getConnection();

			String keys = "  ";
			for (Iterator<String> iter = properties.keySet().iterator(); iter.hasNext();) {
				String key = iter.next();
				keys += " '" + key + "' , ";
			}

			StringBuilder sb = new StringBuilder(keys);
			keys = sb.reverse().toString().replaceFirst(",", "");
			sb = new StringBuilder(keys);
			keys = sb.reverse().toString();

			log.info("############Before Change Properties#######################");
			ps = con.prepareStatement("SELECT prop_key,value FROM SYS_PROPERTIES WHERE RTRIM(prop_key) IN(" + keys + ") order by prop_key");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String key = rs.getString("prop_key");
				String val = rs.getString("value");
				if (key.equals(PropertySession.MAIL_PASSWORD) || key.equals("fina2.email.pwd"))
					log.info("User = " + login + " | Propery Key = " + key + " | Bundle Value = " + bundleProps.get(key) + " | Value = Password For Email");
				else
					log.info("User = " + login + " | Propery Key = " + key + " | Bundle Value = " + bundleProps.get(key) + " | Value = " + val);
			}
			log.info("############After Change Properties#######################");
			for (Iterator<String> iter = properties.keySet().iterator(); iter.hasNext();) {
				String key = iter.next();
				String val = properties.get(key);
				if (key.equals(PropertySession.MAIL_PASSWORD) || key.equals("fina2.email.pwd"))
					log.info("User = " + login + " | Propery Key = " + key + " | Bundle Value = " + bundleProps.get(key) + " | Value = Password For Email");
				else
					log.info("User = " + login + " | Propery Key = " + key + " | Bundle Value = " + bundleProps.get(key) + " | Value = " + val);
				ps = con.prepareStatement("update SYS_PROPERTIES set value = ? where prop_key = ?");
				ps.setString(1, val);
				ps.setString(2, key);
				ps.executeUpdate();
			}
			log.info("############Properties Updated#######################");
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}

	public String getSystemProperty(String key) throws RemoteException {

		String value = null;

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("select value from SYS_PROPERTIES where prop_key = ?");
			ps.setString(1, key);
			rs = ps.executeQuery();

			if (rs.next()) {
				value = rs.getString("value");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return value;
	}

	public void setSystemProperty(String key, String value) throws RemoteException {

		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("update SYS_PROPERTIES set value = ? where prop_key = ?");
			ps.setString(0, value);
			ps.setString(1, key);
			ps.executeUpdate();

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.closeConnection(con);
			DatabaseUtil.closeStatement(ps);
		}
	}
}
