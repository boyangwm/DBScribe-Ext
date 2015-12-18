package fina2.system;

import java.rmi.RemoteException;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;

public interface PropertySession extends EJBObject {

	public static final String ALLOWED_LOGIN_ATTEMPT_NUMBER = "fina2.security.allowedNumberLoginAttempt";
	public static final String ALLOWED_ACCOUNT_INACTIVITY_PERIOD = "fina2.security.allowedAccountInactivityPerioed";
	public static final String OLD_STORED_PASSWORDS_NUMBER = "fina2.security.numberOfStoredOldPasswords";
	public static final String MINIMAL_PASSWORD_LENGTH = "fina2.security.passwordMinimalLen";
	public static final String PASSWORD_WITH_NUMS_AND_CHARS = "fina2.security.passwordWithNumsChars";
	public static final String PASSWORD_VALIDITY_PERIOD = "fina2.security.passwordValidityPeriod";
	public static final String IMPORT_THREADS_NUMBER = "fina2.returns.import.threadsNumber";
	public static final String FOLDER_LOCATION = "fina2.xml.folder.location";
	public static final String NAME_PATTERN = "fina2.mfb.xls.name.pattern";
	public static final String PROTECTION_PASSWORD = "fina2.sheet.protection.password";
	public static final String UPLOADED_FILE_UNIQUE = "fina2.mfb.uploaded.file.unique";
	public static final String UPDATE_START = "fina2.update.start";
	public static final String UPDATE_GUI_FILE_LOCATION = "fina2.update.gui.filelocation";
	public static final String CONVERTED_XMLS = "fina2.converted.xmls";

	public static final String FINA_AUTHENTICATED_MODES = "fina2.authenticatedModes";
	public static final String FINA_CURRENT_AUTHENTICATION = "fina2.current.authentication";
	public static final String LDAP_URL_IP = "fina2.authentication.ldap.urlIp";
	public static final String LDAP_URL_PORT = "fina2.authentication.ldap.urlPort";
	public static final String LDAP_ORGANIZATIONAL_UNIT = "fina2.authentication.ldap.organizationalUnit";
	public static final String LDAP_DOMAIN_COMPONENT = "fina2.authentication.ldap.domainComponent";

	public static final String UPDATE_ADDIN = "fina2.update.addin";
	public static final String UPDATE_RUN_BAT = "fina2.update.runBat";
	public static final String UPDATE_RESOURCES = "fina2.update.resources";
	public static final String UPDATE_FINA_UPDATE = "fina2.update.finaUpdate";
	public static final String MAX_RETURNS_SIZE = "fina2.max.returns";
	public static final String MATRIX_PATH = "fina2.dcs.matrix.path";

	public static final String MAIL_USER = "mail.user";
	public static final String MAIL_ADDRESS = "mail.address";
	public static final String MAIL_PASSWORD = "mail.password";

	// pop3
	public static final String MAIL_POP3_SSL_ENABLE = "mail.pop3.ssl.enable";
	public static final String MAIL_POP3_HOST = "mail.pop3.host";
	public static final String MAIL_POP3_PORT = "mail.pop3.port";
	public static final String MAIL_POP3_CONNECTION_TIMEUT = "mail.pop3.connectiontimeout";

	// smtp
	public static final String MAIL_SMTP_SSL_ENABLE = "mail.smtp.ssl.enable";
	public static final String MAIL_SMTP_HOST = "mail.smtp.host";
	public static final String MAIL_SMTP_PORT = "mail.smtp.port";
	public static final String MAIL_SMTP_CONNECTION_TIMEUT = "mail.smtp.connectiontimeout";
	public static final String MAIL_SMTP_STARTTSL_ENABLE = "mail.smtp.starttls.enable";

	// imap
	public static final String MAIL_IMAP_SSL_ENABLE = "mail.imap.ssl.enable";
	public static final String MAIL_IMAP_HOST = "mail.imap.host";
	public static final String MAIL_IMAP_PORT = "mail.imap.port";
	public static final String MAIL_IMAP_CONNECTION_TIMEUT = "mail.imap.connectiontimeout";

	// Exchange
	public static final String EXCHANGE_MAILBOX = "org.exjello.mail.mailbox";
	public static final String EXJELLO_MAIL_UNFILTERED = "org.exjello.mail.unfiltered";
	public static final String EXJELLO_MAIL_LIMIT = "org.exjello.mail.limit";
	public static final String EXJELLO_MAIL_DELETE = "org.exjello.mail.delete";

	// Mail Service
	public static final String MAIL_CHECK_INTERVAL = "fina2.dcs.mail.check.interval";
	public static final String LAST_READ_DATE = "fina2.mail.last.read.date";
	public static final String MAIL_CONNECTION_TYPE = "fina2.mail.connectionType";
	public static final String MAIL_SEND_RESPONCE_ENABLE = "fina2.mail.sendResponceEnable";
	public static final String MAIL_RESPONCE_CC = "fina2.mail.responceMailsCC";
	public static final String MAIL_RESPONCE_UNKNOWN_USER = "fina2.mail.responceUnknownUser";
	
	//process timeout
	public static final String PROCESS_TIMEOUT= "fina2.process.timeout";
	
	public static final String DEFAULT_LANGUAGE = "fina2.default.language.id";

	Map getSystemProperties() throws RemoteException, EJBException;

	void setSystemProperties(Map properties) throws RemoteException, EJBException;
	
	void setSystemProperties(Map<String,String> properties,Map<String,String> bundleProps,String login) throws RemoteException;

	String getSystemProperty(String key) throws RemoteException, EJBException;

	void setSystemProperty(String key, String value) throws RemoteException, EJBException;
}
