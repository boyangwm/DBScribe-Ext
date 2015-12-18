package fina2.update.gui;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import fina2.FinaGuiUpdateException;
import fina2.db.DatabaseUtil;
import fina2.system.PropertySession;
import fina2.system.PropertySessionHome;
import fina2.util.LoggerHelper;

@SuppressWarnings("serial")
public class GuiUpdateSessionBean implements SessionBean {
	@SuppressWarnings("unused")
	private SessionContext ctx;

	private LoggerHelper log = new LoggerHelper(GuiUpdateSessionBean.class, "GuiSynchronizationSessionBean");

	public void ejbCreate() throws CreateException, EJBException, RemoteException {
	}

	@Override
	public void ejbActivate() throws EJBException, RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void ejbPassivate() throws EJBException, RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void ejbRemove() throws EJBException, RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException {
		ctx = arg0;
	}

	public boolean canSynchronize(String oldGuiVersionDate) throws EJBException, RemoteException, FinaGuiUpdateException {
		File guiFileLocation = getGuiFileLocation();
		String newGuiVersion = getGuiVersion(guiFileLocation);
		if (newGuiVersion != null)
			return compareVersion(newGuiVersion, oldGuiVersionDate);
		return false;
	}

	private boolean compareVersion(String newVersion, String oldVersion) throws FinaGuiUpdateException {
		try {
			String datePattern = "yyyy-MM-dd HH:mm:ss";
			Date newDate = parseDate(newVersion, datePattern);
			Date oldDate = parseDate(oldVersion, datePattern);
			if (!newDate.equals(oldDate))
				return true;
		} catch (Exception ex) {
			log.getLogger().error(ex.getMessage(), ex);
			throw new FinaGuiUpdateException(FinaGuiUpdateException.Type.VERSION_NOT_FOUND);
		}
		return false;
	}

	public Date parseDate(String date, String pattern) throws FinaGuiUpdateException {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		try {
			return sdf.parse(date);
		} catch (Exception e) {
			throw new FinaGuiUpdateException(FinaGuiUpdateException.Type.VERSION_NOT_FOUND);
		}
	}

	private File getGuiFileLocation() throws FinaGuiUpdateException, EJBException, RemoteException {
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		File guiFileLocation = null;
		try {
			InitialContext jndi = new InitialContext();
			Object ref = jndi.lookup("fina2/system/PropertySession");
			PropertySessionHome home = (PropertySessionHome) PortableRemoteObject.narrow(ref, PropertySessionHome.class);
			PropertySession session = home.create();
			String file = session.getSystemProperty(PropertySession.UPDATE_GUI_FILE_LOCATION);
			if (file != null)
				guiFileLocation = new File(file);
		} catch (Exception ex) {
			log.getLogger().error(ex.getMessage(), ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		if (guiFileLocation == null) {
			throw new FinaGuiUpdateException(FinaGuiUpdateException.Type.PROPERTY_NOT_FOUND);
		}
		return guiFileLocation;
	}

	private File findFinaGuiFile(File guiFileLocation) throws FinaGuiUpdateException {
		File[] files = guiFileLocation.listFiles();
		File guiFile = null;
		for (File f : files) {
			if (f.getName().equals("fina-gui.jar")) {
				guiFile = f;
			}
		}
		if (guiFile != null) {
		} else {
			throw new FinaGuiUpdateException(FinaGuiUpdateException.Type.FILE_NOT_FOUND);
		}
		return guiFile;
	}

	private String getGuiVersion(File guiFileLocation) throws FinaGuiUpdateException {
		String buildDate = null;
		File guiFile = null;
		if (guiFileLocation.isDirectory()) {
			guiFile = findFinaGuiFile(guiFileLocation);
			if (guiFile.exists() && guiFile.isFile()) {
				try {
					JarFile jf = new JarFile(guiFile);
					Manifest m = jf.getManifest();
					Attributes versionAttribut = m.getMainAttributes();
					buildDate = versionAttribut.getValue("Build-Date");
				} catch (java.util.zip.ZipException ex) {
					throw new FinaGuiUpdateException(FinaGuiUpdateException.Type.INVALID_FILE);
				} catch (IOException ex) {
					log.getLogger().error(ex.getMessage(), ex);
					throw new FinaGuiUpdateException(FinaGuiUpdateException.Type.INVALID_FILE);
				}
			}
		} else {
			throw new FinaGuiUpdateException(FinaGuiUpdateException.Type.INVALID_PROPERTY);
		}
		if (buildDate == null) {
			throw new FinaGuiUpdateException(FinaGuiUpdateException.Type.VERSION_NOT_FOUND);
		}
		return buildDate;
	}

	public byte[] getGuiFile() throws EJBException, RemoteException, FinaGuiUpdateException {
		File guiFile = findFinaGuiFile(getGuiFileLocation());
		if (guiFile.exists())
			return readFile(guiFile);
		return null;
	}

	public Map<String, byte[]> getOtherUpdateFiles(String... files) throws EJBException, RemoteException, FinaGuiUpdateException {
		String strFileSeparator = System.getProperty("file.separator");
		Map<String, byte[]> map = new HashMap<String, byte[]>();
		File filesLocation = getGuiFileLocation();
		byte[] array = null;

		File libFile = new File(filesLocation, "lib");

		if (libFile.exists() && libFile.isDirectory()) {
			String[] libFiles = libFile.list();
			for (String lib : libFiles) {
				File file = new File(libFile, lib);
				array = readFile(file);
				map.put("." + strFileSeparator + "lib" + strFileSeparator + lib, array);
			}
		}
		for (String s : files) {
			if (s != null) {
				File file = new File(filesLocation, s);
				if (file.exists()) {
					if (file.isDirectory() && s.equals("resources")) {
						String[] resFiles = file.list();
						if (resFiles.length > 0) {
							for (String r : resFiles) {
								File res = new File(file, r);
								if (!res.isHidden()) {
									map.put("." + strFileSeparator + "resources" + strFileSeparator + r, readFile(res));
								}
							}
						}
					} else {
						if (s.equals("run.bat")) {
							array = readFile(file);
							map.put("." + strFileSeparator + s, array);
						} else {
							array = readFile(file);
							map.put("." + strFileSeparator + "lib" + strFileSeparator + s, array);
						}
					}
				}

			}
		}

		return map;
	}

	private byte[] readFile(File file) {
		byte[] array = null;
		BufferedInputStream buff = null;
		try {
			buff = new BufferedInputStream(new FileInputStream(file));
			array = new byte[(int) file.length()];
			buff.read(array);
		} catch (IOException ex) {
			log.getLogger().error(ex.getMessage(), ex);
		} finally {
			try {
				buff.close();
			} catch (IOException e) {
				log.getLogger().error(e.getMessage(), e);
			}
		}
		return array;
	}
}
