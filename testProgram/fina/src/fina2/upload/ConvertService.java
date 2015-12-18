package fina2.upload;

import java.rmi.RemoteException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.security.auth.login.LoginContext;
import javax.transaction.UserTransaction;
import javax.xml.ws.BindingProvider;

import org.apache.log4j.Logger;
import org.jboss.security.auth.callback.UsernamePasswordHandler;
import org.jboss.system.ServiceMBeanSupport;

import fina2.db.DatabaseUtil;
import fina2.i18n.Language;
import fina2.i18n.LanguageHome;
import fina2.i18n.LanguagePK;
import fina2.security.User;
import fina2.security.UserHome;
import fina2.security.UserPK;
import fina2.upload.converter.ConverterMessage;
import fina2.upload.converter.ConverterService;
import fina2.upload.converter.ConverterServiceImplService;

public class ConvertService extends ServiceMBeanSupport {

	private Timer timer = new Timer(true);

	private TimerTask timerTask;

	private User user;

	private Language lang;

	private String address;

	private static Logger log = Logger.getLogger(ConverterService.class);

	@Override
	protected void startService() throws Exception {

		log.info("starting FinA auto import service");
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("SELECT VALUE FROM SYS_PROPERTIES WHERE PROP_KEY = ?");
			ps.setString(1, "fina2.converter.address");
			rs = ps.executeQuery();
			if (rs.next()) {
				address = rs.getString("VALUE");
			} else {
				log.info("Could not find converter service address");
				this.stopService();
			}
			if (address != null)
				log.info("FinA auto import service started");
		} catch (Exception ex) {
			log.error("Can't Initialize Converter Service Address", ex);
			this.stopService();
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		timerTask = new TimerTask() {
			public void run() {
				//convertAndImport();
			}
		};
		timer.schedule(timerTask, 0, 60000);
	}

	@Override
	protected void stopService() throws Exception {
		log.info("stopping FinA auto import service");
		super.stopService();
		log.info("FinA auto import service stopped");
	}

	public long getMaxId() throws RemoteException, EJBException {
		long maxId = 0;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select max(id) as maxId from SYS_USERS");
			rs = ps.executeQuery();
			if (rs.next()) {
				maxId = rs.getLong("maxId");
			}
		} catch (Exception ex) {
			throw new EJBException(ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return maxId;
	}

	private void convertAndImport() {

		try {
			LoginContext lc = new LoginContext("client-login", new UsernamePasswordHandler("guest", "anonymous".toCharArray()));
			lc.login();
		} catch (Exception ex) {
		}

		if (user == null || lang == null) {
			try {
				InitialContext jndi = new InitialContext();
				Object ref = jndi.lookup("fina2/security/User");
				UserHome userHome = (UserHome) PortableRemoteObject.narrow(ref, UserHome.class);

				user = userHome.findByPrimaryKey(new UserPK((int)getMaxId()));
				ref = jndi.lookup("fina2/i18n/Language");
				LanguageHome langHome = (LanguageHome) PortableRemoteObject.narrow(ref, LanguageHome.class);
				lang = langHome.findByPrimaryKey(new LanguagePK(5));
			} catch (Exception ex) {
				log.error("Can't Initialize User and Language", ex);
				return;
			}
		}
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<FileInfo> files = new ArrayList<FileInfo>();
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("SELECT id,fileName,uploadedFile FROM SYS_UPLOADEDFILE WHERE status = ?");
			ps.setInt(1, UploadFileStatus.UPLOADED.ordinal());
			rs = ps.executeQuery();
			while (rs.next()) {
				Blob blob = rs.getBlob("uploadedFile");
				byte[] b = new byte[(int) blob.length()];
				blob.getBinaryStream().read(b, 0, b.length);
				FileInfo file = new FileInfo(rs.getInt("id"), rs.getString("fileName"), b);
				files.add(file);
			}
			for (Iterator<FileInfo> it = files.iterator(); it.hasNext();) {
				convert(it.next());
			}
		} catch (Exception ex) {
			log.error("Can't get uploaded files", ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
	}

	private void convert(FileInfo file) {

		UserTransaction ut = null;
		try {
			ut = getUserTransaction(new InitialContext());
			ConverterService service = new ConverterServiceImplService().getConverterServiceImplPort();
			((BindingProvider) service).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, address);
			ConverterMessage message = service.convert(file.getFilename(), file.getFile());
			ut.begin();
			if (message.isStatus()) {
				importReturns(file.getId(), message.getReturns());
			} else {
				reject(file);
				log.error("Invalid file. Message from service '" + message.getMessage() + "'");
			}
			ut.commit();
		} catch (Exception ex) {
			try {
				ut.rollback();
			} catch (Exception ex1) {
				// log.error("RollBack Failed", ex1);
			}
			// log.error("Error Comunicating to Web Service", ex);
		}
	}

	private void importReturns(int fileId, LinkedList<byte[]> returns) {

		try {
			InitialContext jndi = new InitialContext();
			Object ref = jndi.lookup("fina2/upload/UploadFileSession");
			UploadFileSession session = ((UploadFileSessionHome) PortableRemoteObject.narrow(ref, UploadFileSessionHome.class)).create();
			session.importReturns(returns, user, lang, fileId);
		} catch (Exception ex) {
			log.error("Can't Import Returns", ex);
		}
	}

	private void reject(FileInfo file) {

		try {
			InitialContext jndi = new InitialContext();
			Object ref = jndi.lookup("fina2/upload/UploadFileSession");
			UploadFileSession session = ((UploadFileSessionHome) PortableRemoteObject.narrow(ref, UploadFileSessionHome.class)).create();
			session.reject(file.getId());
		} catch (Exception ex) {
			log.error("Can't Reject File", ex);
		}
	}

	private UserTransaction getUserTransaction(InitialContext jndi) throws NamingException {

		Object ref = jndi.lookup("UserTransaction");
		UserTransaction trans = (UserTransaction) PortableRemoteObject.narrow(ref, UserTransaction.class);
		return trans;
	}

	private class FileInfo {

		private int id;
		private String filename;
		private byte[] file;

		public FileInfo(int id, String filename, byte[] file) {
			this.id = id;
			this.filename = filename;
			this.file = file;
		}

		public int getId() {
			return id;
		}

		public String getFilename() {
			return filename;
		}

		public byte[] getFile() {
			return file;
		}
	}
}
