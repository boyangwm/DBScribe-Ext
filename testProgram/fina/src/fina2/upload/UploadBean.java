package fina2.upload;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

import fina2.FinaTypeException;
import fina2.i18n.Language;
import fina2.returns.cbn.BodyType;
import fina2.returns.cbn.CallReport;
import fina2.returns.cbn.FooterType;
import fina2.returns.cbn.HeaderType;
import fina2.security.User;
import fina2.security.UserHome;
import fina2.xls.XLSReader;
import fina2.xls.XLSValidator;

public class UploadBean {

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	private User user;
	private Language lang;
	private List<SelectItem> users = new ArrayList<SelectItem>();
	private List<SelectItem> banks = new ArrayList<SelectItem>();
	private List<SelectItem> statuses = new ArrayList<SelectItem>();
	private List<UploadedFileInfo> files = new ArrayList<UploadedFileInfo>();
	private List<ImportedReturnInfo> returns = new ArrayList<ImportedReturnInfo>();
	private Map<String, byte[]> uploadFiles = new HashMap<String, byte[]>();
	private String username;
	private Date toDate;
	private Date fromDate;
	private int selectedStatus = -1;
	private String selectedUser = "ALL";
	private String selectedBank = "ALL";
	private String fileId;
	private String filename;
	private LocaleBean bundle;
	private boolean admin;
	private boolean xlsFile;
	private boolean xlsFileNameValid;
	private boolean xlsProtected;
	private boolean xlsProtPasswValid;
	private boolean xlsValid;
	private int maxUploadFiles = 10;
	private Logger log = Logger.getLogger(UploadBean.class);

	public UploadBean() {
		init();
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public boolean isXlsValid(File temp) {
			if (XLSValidator.isXLSFile(temp) && XLSValidator.isXLSNameValid(temp.getName()) && XLSValidator.isXLSProtected(temp) && XLSValidator.isXLSProtectionPasswordValid(temp)) {
				setXlsValid(true);
			} else {
				setXlsValid(false);
			}
		return xlsValid;
	}

	public void setXlsValid(boolean xlsValid) {
		this.xlsValid = xlsValid;
	}

	public boolean isXlsFile() {
		return xlsFile;
	}

	public void setXlsFile(boolean xlsFile) {
		this.xlsFile = xlsFile;
	}

	public boolean isXlsFileNameValid() {
		return xlsFileNameValid;
	}

	public void setXlsFileNameValid(boolean xlsFileNameValid) {
		this.xlsFileNameValid = xlsFileNameValid;
	}

	public boolean isXlsProtected() {
		return xlsProtected;
	}

	public void setXlsProtected(boolean xlsProtected) {
		this.xlsProtected = xlsProtected;
	}

	public boolean isXlsProtPasswValid() {
		return xlsProtPasswValid;
	}

	public void setXlsProtPasswValid(boolean xlsProtPasswValid) {
		this.xlsProtPasswValid = xlsProtPasswValid;
	}

	public List<ImportedReturnInfo> getReturns() {
		return returns;
	}

	public void setReturns(List<ImportedReturnInfo> returns) {
		this.returns = returns;
	}

	public User getUser() {
		return user;
	}

	public Language getLang() {
		return lang;
	}

	public List<SelectItem> getUsers() {
		return users;
	}

	public List<SelectItem> getBanks() {
		return banks;
	}

	public List<SelectItem> getStatuses() {
		return statuses;
	}

	public List<UploadedFileInfo> getFiles() {
		return files;
	}

	public Map<String, byte[]> getUploadFiles() {
		return uploadFiles;
	}

	public String getUsername() {
		return username;
	}

	public Date getToDate() {
		return toDate;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public int getSelectedStatus() {
		return selectedStatus;
	}

	public String getSelectedUser() {
		return selectedUser;
	}

	public String getSelectedBank() {
		return selectedBank;
	}

	public String getFileId() {
		return fileId;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setLang(Language lang) {
		this.lang = lang;
	}

	public void setUsers(List<SelectItem> users) {
		this.users = users;
	}

	public void setBanks(List<SelectItem> banks) {
		this.banks = banks;
	}

	public void setStatuses(List<SelectItem> statuses) {
		this.statuses = statuses;
	}

	public void setFiles(List<UploadedFileInfo> files) {
		this.files = files;
	}

	public void setUploadFiles(Map<String, byte[]> uploadFiles) {
		this.uploadFiles = uploadFiles;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public void setSelectedStatus(int selectedStatus) {
		this.selectedStatus = selectedStatus;
	}

	public void setSelectedUser(String selectedUser) {
		this.selectedUser = selectedUser;
	}

	public void setSelectedBank(String selectedBank) {
		this.selectedBank = selectedBank;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public void init() {

		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		admin = ec.isUserInRole("fina2.web.admin");
		username = ec.getRemoteUser();

		try {
			InitialContext jndi = LocaleBean.getInitialContext();
			Object ref = jndi.lookup("fina2/security/User");
			UserHome userHome = (UserHome) PortableRemoteObject.narrow(ref, UserHome.class);
			user = userHome.findByLogin(username);
			bundle = (LocaleBean) ec.getSessionMap().get("bundle");
			lang = bundle.getLanguage();
			initUsersAndBanks();
		} catch (Exception ex) {
			log.error("Initializing User and Language failed", ex);
		}
		statuses.add(new SelectItem(-1, "ALL"));
		statuses.add(new SelectItem(UploadFileStatus.UPLOADED.ordinal(), bundle.getRes().get(UploadFileStatus.UPLOADED.getCode())));
		statuses.add(new SelectItem(UploadFileStatus.CONVERTED.ordinal(), bundle.getRes().get(UploadFileStatus.CONVERTED.getCode())));
		statuses.add(new SelectItem(UploadFileStatus.REJECTED.ordinal(), bundle.getRes().get(UploadFileStatus.REJECTED.getCode())));
	}

	public void getUploadedFiles() {

		try {
			InitialContext jndi = LocaleBean.getInitialContext();
			Object ref = jndi.lookup("fina2/upload/UploadFileSession");
			UploadFileSession session = ((UploadFileSessionHome) PortableRemoteObject.narrow(ref, UploadFileSessionHome.class)).create();
			files = session.getUploadedFiles(selectedUser, selectedBank, selectedStatus, fromDate, toDate);
			initUsersAndBanks();
		} catch (Exception ex) {
			log.error("Can't get uploaded files ", ex);
		}
	}

	public void uploadListener(UploadEvent event) {

		try {
			UploadItem file = event.getUploadItem();
			File f = file.getFile();
			FileInputStream fis = new FileInputStream(f);

			if (admin) {
				ZipInputStream zis = new ZipInputStream(fis);
				ZipEntry entry = null;
				boolean zip = false;
				while ((entry = zis.getNextEntry()) != null) {
					zip = true;
					readUploadedFile(zis, entry.getName());
				}
				if (!zip) {
					fis.close();
					fis = new FileInputStream(file.getFile());
					readUploadedFile(fis, file.getFileName());
				}
			} else {
				readUploadedFile(fis, file.getFileName());
			}
		} catch (Exception ex) {
			uploadFiles.clear();
			log.error("Can't upload file", ex);
		}

	}

	private void readUploadedFile(InputStream is, String fileName) throws IOException {
		uploadFiles.put(fileName, toByteArray(is));
	}

	private byte[] toByteArray(InputStream input) throws IOException {

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		copy(input, output);
		return output.toByteArray();
	}

	private int copy(InputStream input, OutputStream output) throws IOException {

		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	public void clearFiles() {
		uploadFiles.clear();
	}

	public void reject() {

		try {
			InitialContext jndi = LocaleBean.getInitialContext();
			Object ref = jndi.lookup("fina2/upload/UploadFileSession");
			UploadFileSession session = ((UploadFileSessionHome) PortableRemoteObject.narrow(ref, UploadFileSessionHome.class)).create();
			session.reject(Integer.parseInt(fileId));
			updateFiles();
			initUsersAndBanks();
		} catch (FinaTypeException ex) {
			FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getRes().get(ex.getMessageUrl()), null);
			FacesContext.getCurrentInstance().addMessage(null, fm);
		} catch (Exception ex) {
			log.error("Can't Reject file - " + fileId, ex);
		}
	}

	public void remove() {

		try {
			InitialContext jndi = LocaleBean.getInitialContext();
			Object ref = jndi.lookup("fina2/upload/UploadFileSession");
			UploadFileSession session = ((UploadFileSessionHome) PortableRemoteObject.narrow(ref, UploadFileSessionHome.class)).create();
			session.remove(Integer.parseInt(fileId));
			removeFile();
			initUsersAndBanks();
		} catch (Exception ex) {
			log.error("Can't Delete file - " + fileId, ex);
		}
	}

	public void importXMLFiles() {

		try {
			InitialContext jndi = LocaleBean.getInitialContext();
			Object ref = jndi.lookup("fina2/upload/UploadFileSession");
			UploadFileSession session = ((UploadFileSessionHome) PortableRemoteObject.narrow(ref, UploadFileSessionHome.class)).create();
			List<byte[]> returns = new ArrayList<byte[]>();
			for (Iterator<String> it = uploadFiles.keySet().iterator(); it.hasNext();) {
				returns.add(uploadFiles.get(it.next()));
			}
			session.importReturns(returns, user, lang, Integer.parseInt(fileId));
			uploadFiles.clear();
			initUsersAndBanks();
		} catch (FinaTypeException ex) {
			uploadFiles.clear();
			FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getRes().get(ex.getMessageUrl()), null);
			FacesContext.getCurrentInstance().addMessage(null, fm);
		} catch (Exception ex) {
			uploadFiles.clear();
			log.error("Can't Import Returns", ex);
		}
	}

	public String logout() {

		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		((HttpServletRequest) ec.getRequest()).getSession().invalidate();

		return "main";
	}

	public void upload() {

		try {
			InitialContext jndi = LocaleBean.getInitialContext();
			Object ref = jndi.lookup("fina2/upload/UploadFileSession");
			UploadFileSession session = ((UploadFileSessionHome) PortableRemoteObject.narrow(ref, UploadFileSessionHome.class)).create();
			session.upload(uploadFiles);
			getUploadedFiles();
			uploadFiles.clear();
			initUsersAndBanks();

		} catch (Exception ex) {
			uploadFiles.clear();
			log.error("Can't Upload files ", ex);
		}
	}

	public int getMaxUploadFiles() {
		return maxUploadFiles;
	}

	public void getImportedReturns() {

		try {
			InitialContext jndi = LocaleBean.getInitialContext();
			Object ref = jndi.lookup("fina2/upload/UploadFileSession");
			UploadFileSession session = ((UploadFileSessionHome) PortableRemoteObject.narrow(ref, UploadFileSessionHome.class)).create();
			returns = session.getImportedReturns(Integer.parseInt(fileId));
			initUsersAndBanks();
		} catch (Exception ex) {
			log.error("Can't Upload files ", ex);
		}
	}

	private void initUsersAndBanks() {

		try {
			users.clear();
			banks.clear();
			InitialContext jndi = LocaleBean.getInitialContext();
			Object ref = jndi.lookup("fina2/upload/UploadFileSession");
			UploadFileSession session = ((UploadFileSessionHome) PortableRemoteObject.narrow(ref, UploadFileSessionHome.class)).create();

			users.add(new SelectItem("ALL"));
			for (Iterator<String> it = session.getUsers().iterator(); it.hasNext();) {
				users.add(new SelectItem(it.next()));
			}

			banks.add(new SelectItem("ALL"));
			for (Iterator<String> it = session.getBanks().iterator(); it.hasNext();) {
				banks.add(new SelectItem(it.next()));
			}
		} catch (Exception ex) {
			log.error("Can't initialize Users and Banks", ex);
		}
	}

	private void updateFiles() {

		for (Iterator<UploadedFileInfo> it = files.iterator(); it.hasNext();) {
			UploadedFileInfo file = it.next();
			if (file.getFileId() == Integer.parseInt(this.fileId)) {
				file.setStatus(UploadFileStatus.REJECTED.getCode());
				break;
			}
		}
	}

	private void removeFile() {

		for (Iterator<UploadedFileInfo> it = files.iterator(); it.hasNext();) {
			UploadedFileInfo file = it.next();
			if (file.getFileId() == Integer.parseInt(this.fileId)) {
				files.remove(file);
				break;
			}
		}
	}

	public boolean validateXlsFile(File temp) {
		setXlsFile(XLSValidator.isXLSFile(temp));
		setXlsFileNameValid(XLSValidator.isXLSNameValid(temp.getName()));
		setXlsProtected(XLSValidator.isXLSProtected(temp));
		setXlsProtPasswValid(XLSValidator.isXLSProtectionPasswordValid(temp));
		if (XLSValidator.isXLSFile(temp) && XLSValidator.isXLSNameValid(temp.getName()) && XLSValidator.isXLSProtected(temp) && XLSValidator.isXLSProtectionPasswordValid(temp)) {
			setXlsValid(true);
			return true;
		} else {
			setXlsValid(false);
			return false;
		}
	}

	public void validateXlsData() {
		try {

			InitialContext jndi = LocaleBean.getInitialContext();
			Object ref = jndi.lookup("fina2/upload/UploadFile");
			UploadFileHome home = (UploadFileHome) PortableRemoteObject.narrow(ref, UploadFileHome.class);
			UploadFile file = home.findByPrimaryKey(new UploadFilePK(Integer.parseInt(fileId)));

			File temp = new File(getFilename());
			FileOutputStream fos = new FileOutputStream(temp);
			fos.write(file.getFile());
			fos.flush();
			fos.close();

				
			Object refSession = jndi.lookup("fina2/upload/UploadFileSession");
			UploadFileSession ufs = ((UploadFileSessionHome) PortableRemoteObject.narrow(refSession, UploadFileSessionHome.class)).create();
			if(validateXlsFile(temp)) {
			ufs.setStatus(Integer.parseInt(fileId.trim()), UploadFileStatus.UPLOADED_VALIDATED.ordinal());
			for (int j = 0; j < files.size(); j++)
				if (files.get(j).getFileId() == ((int) Integer.parseInt(fileId.trim()))) {
					files.get(j).setStatus(UploadFileStatus.UPLOADED_VALIDATED.getCode());
				}
			}
			else {
				ufs.setStatus(Integer.parseInt(fileId.trim()), UploadFileStatus.UPLOADED_NOT_VALIDATED.ordinal());
				for (int j = 0; j < files.size(); j++)
					if (files.get(j).getFileId() == ((int) Integer.parseInt(fileId.trim()))) {
						files.get(j).setStatus(UploadFileStatus.UPLOADED_NOT_VALIDATED.getCode());
					}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void convertXlsToXmls() {
		try {
			log.info("Converting....");
			InitialContext jndi = LocaleBean.getInitialContext();
			Object ref = jndi.lookup("fina2/upload/UploadFile");
			UploadFileHome home = (UploadFileHome) PortableRemoteObject.narrow(ref, UploadFileHome.class);
			UploadFile file = home.findByPrimaryKey(new UploadFilePK(Integer.parseInt(fileId)));

			File temp = new File(file.getFileName());
			FileOutputStream fos = new FileOutputStream(temp);
			fos.write(file.getFile());
			fos.flush();
			fos.close();
			
			Object refSession = jndi.lookup("fina2/upload/UploadFileSession");
			UploadFileSession ufs = ((UploadFileSessionHome) PortableRemoteObject.narrow(refSession, UploadFileSessionHome.class)).create();

			if (validateXlsFile(temp)) {
				
				XLSReader xlsReader = new XLSReader(new File("C:\\fina-server\\Matrix.xls"), temp);
				String sheets[] = xlsReader.getAllSheets();

				JAXBContext jc = JAXBContext.newInstance("fina2.returns.cbn");
				Marshaller mar = jc.createMarshaller();
				for (int i = 0; i < sheets.length; i++) {
					CallReport cr = new CallReport();

					HeaderType ht = cr.getHEADER();
					HeaderType tempHt = xlsReader.getHeaderType(sheets[i]);

					ht.setAS_AT(tempHt.getAS_AT());
					ht.setCALLREPORT_DESC(tempHt.getCALLREPORT_DESC());
					ht.setCALLREPORT_ID(tempHt.getCALLREPORT_ID());
					ht.setINST_CODE(tempHt.getINST_CODE());
					ht.setINST_NAME(tempHt.getINST_NAME());

					BodyType bt = cr.getBODY();
					bt.setITEMS_INFO(xlsReader.getAllItems(sheets[i]));

					FooterType ft = cr.getFOOTER();
					FooterType tempFt = xlsReader.getFooterType(sheets[i]);

					ft.setAUTH_SIGNATORY(tempFt.getAUTH_SIGNATORY());
					ft.setCONTACT_DETAILS(tempFt.getCONTACT_DETAILS());
					ft.setDESC(tempFt.getDESC());

					mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
					mar.marshal(cr, new File("C:\\fina-server\\MFB" + sheets[i] + ".xml"));

					log.info("Converted successfully...");
					ufs.setStatus(Integer.parseInt(fileId.trim()), UploadFileStatus.UPLOADED_CONVERTED.ordinal());
					for (int j = 0; j < files.size(); j++)
						if (files.get(j).getFileId() == ((int) Integer.parseInt(fileId.trim()))) {
							files.get(j).setStatus(UploadFileStatus.UPLOADED_CONVERTED.getCode());
						}
				}
			}
			else {
				log.info("Could not convert....");
				ufs.setStatus(Integer.parseInt(fileId.trim()), UploadFileStatus.UPLOADED_NOT_CONVERTED.ordinal());
				for (int j = 0; j < files.size(); j++)
					if (files.get(j).getFileId() == ((int) Integer.parseInt(fileId.trim()))) {
						files.get(j).setStatus(UploadFileStatus.UPLOADED_NOT_CONVERTED.getCode());
					}
			}
		} catch (Exception ex) {
			log.error("Could not convert....");
			log.error(ex.getMessage(), ex);
			for (int j = 0; j < files.size(); j++)
				if (files.get(j).getFileId() == ((int) Integer.parseInt(fileId.trim()))) {
					files.get(j).setStatus(UploadFileStatus.REJECTED.getCode());
				}
		}
	}

}
