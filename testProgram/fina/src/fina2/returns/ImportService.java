package fina2.returns;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.jboss.security.auth.callback.UsernamePasswordHandler;
import org.jboss.system.ServiceMBeanSupport;

import fina2.system.PropertySession;
import fina2.system.PropertySessionHome;

public class ImportService extends ServiceMBeanSupport {

	private Timer timer = new Timer(true);

	private TimerTask timerTask;

	private ExecutorService threadPool;

	private Map<String, BankReturnsImporter> importers;

	private Map<String, Future<?>> importerStatuses;

	private static Logger log = Logger.getLogger(ImportService.class);

	protected void startService() throws Exception {

		log.info("Staring FinA returns import service");
		updateLoginContext("guest", "anonymous");
		getImportManagerSession().resetStatuses();

		importers = new ConcurrentHashMap<String, BankReturnsImporter>();
		importerStatuses = new ConcurrentHashMap<String, Future<?>>();

		threadPool = Executors.newFixedThreadPool(getThreadsNumber());

		timerTask = new TimerTask() {
			public void run() {
				updateLoginContext("guest", "anonymous");
				importReturns();
			}
		};
		// Start timer
		timer.schedule(timerTask, 0, 1000);
		log.info("FinA returns import service started");
	}

	private int getThreadsNumber() throws NamingException, EJBException, RemoteException, CreateException {

		InitialContext jndi = new InitialContext();
		Object ref = jndi.lookup("fina2/system/PropertySession");
		PropertySessionHome home = (PropertySessionHome) PortableRemoteObject.narrow(ref, PropertySessionHome.class);

		PropertySession session = home.create();
		String prop = session.getSystemProperty(PropertySession.IMPORT_THREADS_NUMBER);

		int threadsNumber = 1;
		if (prop != null && !"".equals(prop)) {
			try {
				threadsNumber = Integer.parseInt(prop);
			} catch (NumberFormatException e) {
			}
		}
		log.debug("Threads number is " + threadsNumber);
		return threadsNumber;
	}

	protected void stopService() throws Exception {

		log.info("Stopping FinA returns import service");
		// Stop timer
		timerTask.cancel();

		stopThreadPool();

		log.info("FinA returns import service stoped");
	}

	private void stopThreadPool() {
		threadPool.shutdown();
		try {
			// Wait a while for existing import tasks to terminate
			if (!threadPool.awaitTermination(1, TimeUnit.MINUTES)) {
				threadPool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!threadPool.awaitTermination(1, TimeUnit.MINUTES)) {
					log.error("Pool did not terminate");
				}
			}
		} catch (InterruptedException ex) {
			// (Re-)Cancel if current thread also interrupted
			threadPool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	@SuppressWarnings("unchecked")
	private void importReturns() {

		Collection<ImportedReturn> retsToImport = null;
		try {
			retsToImport = getImportManagerSession().getReturnsToImport();
			// log.debug("Returns to import " + (retsToImport == null ? 0 :
			// retsToImport.size()));
		} catch (Exception ex) {
			log.error("Error loading returns to import", ex);
		}

		if (retsToImport != null && retsToImport.size() > 0) {
			for (ImportedReturn impRet : (Collection<ImportedReturn>) retsToImport) {
				BankReturnsImporter bri = importers.get(impRet.getBankCode());
				if (bri == null) {
					bri = new BankReturnsImporter(impRet.getBankCode());
					importers.put(impRet.getBankCode(), bri);
				}
				// log.debug("Adding imported return to bank return importer. Bank code: "
				// + bri.getBankCode() + ", Imported return ID: " +
				// impRet.getId());
				bri.addImportedReturn(impRet);
			}

			for (BankReturnsImporter bri : importers.values()) {
				if (importerStatuses.get(bri.getBankCode()) == null || (importerStatuses.get(bri.getBankCode()).isDone() && bri.isReturnsToImport())) {
					// log.debug("Processing bank returns. Bank returns number: "
					// + bri.getImportedReturns().size());
					importerStatuses.put(bri.getBankCode(), threadPool.submit(bri));
				}

				if (importerStatuses.get(bri.getBankCode()).isDone()) {
					bri.clear();
				}
			}
		}
	}

	private class BankReturnsImporter implements Runnable {

		private String bankCode;

		private Map<Integer, ImportedReturn> impRets;

		public BankReturnsImporter(String bankCode) {
			this.impRets = new ConcurrentHashMap<Integer, ImportedReturn>();
			this.bankCode = bankCode;

		}

		public void addImportedReturn(ImportedReturn impRet) {
			if (impRets.get(impRet.getId()) == null) {
				impRets.put(impRet.getId(), impRet);
			}
		}

		public Collection<ImportedReturn> getImportedReturns() {
			return impRets.values();
		}

		public boolean isReturnsToImport() {
			boolean result = false;
			for (ImportedReturn impRet : impRets.values()) {
				if (impRet.getStatus() == ImportStatus.QUEUED || impRet.getStatus() == ImportStatus.UPLOADED) {
					result = true;
					break;
				}
			}
			return result;
		}

		public void run() {
			for (ImportedReturn impRet : impRets.values()) {
				if (threadPool.isShutdown() == true) {
					break;
				} else {
					log.debug("Importing return. " + impRet);
					importReturn(impRet, impRets);
				}
			}
		}

		public String getBankCode() {
			return bankCode;
		}

		public void clear() {
			impRets.clear();
		}
	}

	private void importReturn(ImportedReturn ir, Map<Integer, ImportedReturn> impRetsMap) {
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
				getImportManagerSession().errors(ir.getId(), log.substring(log.indexOf("Value does not match comparison rule")));
				ir.setStatus(ImportStatus.ERRORS);
			} else if (log.indexOf("Error during parsing/formatting  following items") != -1) {
				getImportManagerSession().errors(ir.getId(), log.substring(log.indexOf("Error during parsing/formatting  following items")));
				ir.setStatus(ImportStatus.ERRORS);
			} else if (log.indexOf("Error: Wrong date format") != -1) {
				getImportManagerSession().errors(ir.getId(), log.substring(log.indexOf("Error: Wrong date format")));
				ir.setStatus(ImportStatus.ERRORS);

			} else if (log.indexOf("Error: The value for node code") != -1) {
				getImportManagerSession().errors(ir.getId(), log.substring(log.indexOf("Error: The value for node code")));
				ir.setStatus(ImportStatus.ERRORS);

			} else if (log.indexOf("Wrong version code") != -1) {
				getImportManagerSession().declined(ir.getId(), log.substring(log.indexOf("Wrong version code")));
				ir.setStatus(ImportStatus.DECLINED);

			} else if (log.indexOf("Schedule does not exists") != -1) {
				getImportManagerSession().declined(ir.getId(), log.substring(log.indexOf("Schedule does not exists")));
				ir.setStatus(ImportStatus.DECLINED);
			} else {
				getImportManagerSession().declined(ir.getId(), log);
				ir.setStatus(ImportStatus.DECLINED);
			}
			if (ir.getStatus().ordinal() != ImportStatus.QUEUED.ordinal()) {
				removeUnused(ir.getId(), impRetsMap);
			}

		} catch (Exception ex) {
			log.error("Error importing return. Imported return document ID: " + ir.getId());
		}
	}

	private void removeUnused(Integer id, Map<Integer, ImportedReturn> impRetsMap) {
		if (impRetsMap.containsKey(id)) {
			impRetsMap.remove(id);
		}
	}

	private void updateLoginContext(String username, String password) {
		try {
			LoginContext lc = new LoginContext("client-login", new UsernamePasswordHandler(username, password.toCharArray()));
			lc.login();
		} catch (LoginException ex) {
			log.error("Error updating login context", ex);
		}
	}

	private ImportManagerSession getImportManagerSession() throws Exception {

		InitialContext jndi = new InitialContext();
		Object ref = jndi.lookup("fina2/returns/ImportManagerSession");
		ImportManagerSessionHome home = (ImportManagerSessionHome) PortableRemoteObject.narrow(ref, ImportManagerSessionHome.class);
		return home.create();
	}

	private ProcessSession getProcessSession() throws Exception {

		InitialContext jndi = new InitialContext();
		Object ref = jndi.lookup("fina2/returns/ProcessSession");
		ProcessSessionHome home = (ProcessSessionHome) PortableRemoteObject.narrow(ref, ProcessSessionHome.class);
		return home.create();
	}
}
