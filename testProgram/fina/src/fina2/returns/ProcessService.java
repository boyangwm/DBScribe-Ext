package fina2.returns;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.security.auth.login.LoginContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.jboss.security.auth.callback.UsernamePasswordHandler;
import org.jboss.system.ServiceMBeanSupport;

import fina2.db.DatabaseUtil;
import fina2.i18n.Language;
import fina2.i18n.LanguageHome;
import fina2.i18n.LanguagePK;
import fina2.returns.jaxb.RETURN;
import fina2.security.User;
import fina2.security.UserHome;
import fina2.security.UserPK;

public class ProcessService extends ServiceMBeanSupport {

	private Timer timer = new Timer(true);

	private TimerTask timerTask;

	private Logger log = Logger.getLogger(ProcessService.class);

	private User user = null;

	private Language lang = null;

	private String folderLoc = null;

	private ArrayList<ImportedReturn> filesToImport = new ArrayList<ImportedReturn>();

	private ArrayList<String> returnNames = new ArrayList<String>();

	protected void startService() throws Exception {
		log.info("Starting FinA Process Service");
		initUserAndLang();

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			ps = con.prepareStatement("SELECT VALUE FROM SYS_PROPERTIES WHERE PROP_KEY = ?");
			ps.setString(1, "fina2.xml.folder.location");
			rs = ps.executeQuery();
			if (rs.next()) {
				folderLoc = rs.getString("VALUE");
			} else {
				log.info("Could not find folder location");
				this.stopService();
			}
		} catch (Exception ex) {
			log.error("Cant initialize folder location");
			this.stopService();
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		timerTask = new TimerTask() {
			public void run() {
				processReturns();
			}
		};
		timer.schedule(timerTask, 0, getProcesTimeOutTime());
	}

	protected void stopService() throws Exception {
		log.info("Stopping FinA Process Service");
		super.stopService();
		log.info("FinA Process Service Stopped");
	}

	private void initUserAndLang() {
		try {
			if (user == null || lang == null) {
				InitialContext ct = new InitialContext();
				Object ref = ct.lookup("fina2/security/User");

				UserHome userHome = (UserHome) PortableRemoteObject.narrow(ref, UserHome.class);
				user = userHome.findByPrimaryKey(new UserPK((int) getSaId()));

				ref = ct.lookup("fina2/i18n/Language");
				LanguageHome langHome = (LanguageHome) PortableRemoteObject.narrow(ref, LanguageHome.class);
				lang = langHome.findByPrimaryKey(new LanguagePK(1));

			}
		} catch (Exception ex) {
			log.error(ex);
			ex.printStackTrace();
		}
	}

	public long getMaxId() {
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
			log.error(ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return maxId;
	}

	public long getSaId() {
		long maxId = 0;
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select id as SA_ID from SYS_USERS where lower(rtrim(login))='sa'");
			rs = ps.executeQuery();
			if (rs.next()) {
				maxId = rs.getLong("SA_ID");
			}
		} catch (Exception ex) {
			log.error(ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return maxId;
	}

	private void processReturns() {
		log.info("Searching xmls to import....................");
		try {
			LoginContext lc = new LoginContext("client-login", new UsernamePasswordHandler("guest", "anonymous".toCharArray()));
			lc.login();
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		filesToImport = getFilesToImport();
		for (int i = 0; i < filesToImport.size(); i++)
			importReturn(filesToImport.get(i));
		filesToImport.clear();
		for (int i = 0; i < returnNames.size(); i++) {
			File f = new File(folderLoc + returnNames.get(i));
			if (f.exists())
				f.delete();
		}
		returnNames.clear();
	}

	private void importReturn(ImportedReturn ir) {
		try {

			getImportManagerSession().started(ir.getId());
			ir.setStatus(ImportStatus.IN_PROGRESS);

			String log = getProcessSession().importReturn(ir.getUserHandle(), ir.getLanguageHandle(), ir.getXml(), false, ir.getVersion());

			if (log.indexOf("Process ok.") != -1) {
				getImportManagerSession().imported(ir.getId(), log);
				ir.setStatus(ImportStatus.IMPORTED);
			} else if (log.indexOf("queue.") != -1) {
				getImportManagerSession().queued(ir.getId(), log);
				ir.setStatus(ImportStatus.QUEUED);
			} else if (log.indexOf("Value does not match comparison rule") != -1) {
				getImportManagerSession().errors(ir.getId(), log);
				ir.setStatus(ImportStatus.ERRORS);
			} else if (log.indexOf("Error: Wrong date format") != -1) {
				getImportManagerSession().declined(ir.getId(), log);
				ir.setStatus(ImportStatus.DECLINED);
			} else {
				getImportManagerSession().rejected(ir.getId(), log);
				ir.setStatus(ImportStatus.REJECTED);
			}
			LinkedList<byte[]> xmls = new LinkedList<byte[]>();
			xmls.add(ir.getXml());
			getImportManagerSession().uploadImportedDocuments(user.getHandle(), lang.getHandle(), xmls);
			xmls.clear();
		} catch (Exception ex) {
			log.error("Error importing return. Imported return document ID: " + ir.getId());
		}
	}

	private ProcessSession getProcessSession() throws Exception {
		InitialContext jndi = new InitialContext();
		Object ref = jndi.lookup("fina2/returns/ProcessSession");
		ProcessSessionHome home = (ProcessSessionHome) PortableRemoteObject.narrow(ref, ProcessSessionHome.class);
		return home.create();
	}

	private ImportManagerSession getImportManagerSession() throws Exception {
		InitialContext jndi = new InitialContext();
		Object ref = jndi.lookup("fina2/returns/ImportManagerSession");
		ImportManagerSessionHome home = (ImportManagerSessionHome) PortableRemoteObject.narrow(ref, ImportManagerSessionHome.class);
		return home.create();
	}

	private ArrayList<ImportedReturn> getFilesToImport() {
		ArrayList<ImportedReturn> retList = new ArrayList<ImportedReturn>();
		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			int id = 0;
			ps = con.prepareStatement("SELECT MAX(ID) AS ID FROM IN_IMPORTED_RETURNS");

			JAXBContext jc = JAXBContext.newInstance("fina2.returns.jaxb");
			Unmarshaller unm = jc.createUnmarshaller();

			File dir = new File(folderLoc);
			String[] files = dir.list();

			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					returnNames.add(files[i]);
					File f = new File(folderLoc + files[i]);
					FileInputStream fis = new FileInputStream(f);
					byte[] xml = new byte[fis.available()];
					fis.read(xml);
					fis.close();

					rs = ps.executeQuery();
					if (rs.next()) {
						id = rs.getInt("ID");
					}
					ImportedReturn ir = new ImportedReturn();
					RETURN r = (RETURN) unm.unmarshal(f);
					ir.setId(id + 1);
					ir.setXml(xml);
					ir.setVersion(r.getHEADER().getVER());
					ir.setBankCode(r.getHEADER().getBANKCODE());
					ir.setLanguageHandle(lang.getHandle());
					ir.setUserHandle(user.getHandle());
					retList.add(ir);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return retList;
	}

	private Integer getProcesTimeOutTime() {

		Connection con = DatabaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Integer t = 60 * 60 * 1000;
		try {
			ps = con.prepareStatement("SELECT VALUE FROM SYS_PROPERTIES WHERE PROP_KEY=?");

			ps.setString(1, "fina2.process.timeout");
			rs = ps.executeQuery();
			if (rs.next()) {
				t = Integer.parseInt(rs.getString("VALUE"));
			}

		} catch (NumberFormatException ex) {
			return t;
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return t;
	}
}