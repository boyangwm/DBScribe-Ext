package fina2.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Properties;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import fina2.db.DatabaseUtil;
import fina2.ui.table.TableRowImpl;

public class LanguageSessionBean implements SessionBean {

	private SessionContext ctx;

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
	 * <b>Permissions:</b> Languages.Review - Review list of banks.
	 * 
	 * @see EJBTable Pattern
	 */
	public Collection getLanguagesRows(Handle userHandle, Handle languageHandle) throws EJBException, RemoteException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		try {
			ps = con.prepareStatement("select id, code, name from SYS_LANGUAGES order by id");
			rs = ps.executeQuery();

			while (rs.next()) {
				TableRowImpl row = new TableRowImpl(new LanguagePK(rs.getInt("id")), 2);
				row.setValue(0, rs.getString("code").trim());
				row.setValue(1, rs.getString("name").trim());

				v.add(row);
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;
	}

	public Collection getLanguagesRowsEx(Handle userHandle, Handle languageHandle) throws EJBException, RemoteException {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector v = new Vector();
		try {
			ps = con.prepareStatement("select id, code, name, fontFace from SYS_LANGUAGES order by id");
			rs = ps.executeQuery();

			while (rs.next()) {
				TableRowImpl row = new TableRowImpl(new LanguagePK(rs.getInt("id")), 3);
				row.setValue(0, rs.getString("code").trim());
				row.setValue(1, rs.getString("name").trim());
				row.setValue(2, rs.getString("fontFace").trim());

				v.add(row);
			}
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return v;
	}

	public Properties getLanguageBundle(Handle langHandle) throws EJBException, RemoteException {

		Properties bundle = new Properties();
		FileInputStream fis = null;

		try {
			fis = new FileInputStream(getBundleFileName(langHandle));
			bundle.load(fis);
		} catch (FileNotFoundException ex) {
			// Ignore
		} catch (IOException ex) {
			throw new EJBException(ex);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException ex) {
				}
			}
		}

		return bundle;
	}

	public void setLanguageBundle(Handle langHandle, Properties bundle) throws EJBException, RemoteException {

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(getBundleFileName(langHandle));
			bundle.store(fos, null);
			fos.flush();
		} catch (IOException ex) {
			throw new EJBException(ex);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException ex) {
				}
			}
		}
	}

	private String getBundleFileName(Handle languageHandle) throws EJBException, RemoteException {

		Language lang = (Language) languageHandle.getEJBObject();
		String code = lang.getCode();
		StringBuffer fileName = new StringBuffer();
		fileName.append(getHomeDirectory()).append("/conf/messages_");
		fileName.append(code).append(".properties");
		File f = new File(fileName.toString());
		if (!f.exists()) {
			fileName = new StringBuffer();
			fileName.append(getHomeDirectory()).append("/conf/messages_");
			fileName.append(code.substring(3).toLowerCase()).append(".properties");
		}
		return fileName.toString();
	}

	private String getHomeDirectory() {
		String homeDir = System.getProperty("FINA2_HOME");
		if (homeDir == null) {
			homeDir = ".";
		}
		return homeDir;
	}
}
