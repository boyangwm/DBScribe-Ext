package fina2.security;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TreeMap;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import org.apache.log4j.Logger;

import fina2.db.DatabaseUtil;
import fina2.util.LoggerHelper;

@SuppressWarnings("serial")
public class AuthenticatedModeSessionBean implements SessionBean {
	@SuppressWarnings("unused")
	private SessionContext ctx;

	private LoggerHelper log = new LoggerHelper(AuthenticatedModeSessionBean.class, "AuthenticatedModeSessionBean");
	private Logger logg = log.getLogger();

	private InitialLdapContext authContext = null;
	private Hashtable<String, String> env = null;
	private TreeMap<String, String> authProps = new TreeMap<String, String>();

	public static final String FINA_AUTHENTICATED_MODES = "fina2.authenticatedModes";
	public static final String FINA_CURRENT_AUTHENTICATION = "fina2.current.authentication";
	public static final String LDAP_URL_IP = "fina2.authentication.ldap.urlIp";
	public static final String LDAP_URL_PORT = "fina2.authentication.ldap.urlPort";
	public static final String LDAP_ORGANIZATIONAL_UNIT = "fina2.authentication.ldap.organizationalUnit";
	public static final String LDAP_DOMAIN_COMPONENT = "fina2.authentication.ldap.domainComponent";

	private boolean userExists(String username) {
		boolean exists = false;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DatabaseUtil.getConnection();
			ps = con.prepareStatement("SELECT s.login FROM SYS_USERS s where RTRIM(s.login)=?");
			ps.setString(1, username);
			ps.execute();
			rs = ps.getResultSet();
			if (rs.next()) {
				exists = true;
			}
		} catch (Exception ex) {
			logg.error(ex.getMessage(), ex);
			throw new EJBException(ex.getMessage(), ex);
		} finally {
			DatabaseUtil.close(rs, ps, con);
		}
		return exists;
	}

	public TreeMap<String, String> loadProperties() {
		
			Connection con = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				con = DatabaseUtil.getConnection();
				ps = con.prepareStatement("SELECT * FROM SYS_PROPERTIES p WHERE RTRIM(p.prop_key) IN('fina2.authenticatedModes','fina2.current.authentication','fina2.authentication.ldap.urlIp','fina2.authentication.ldap.urlPort','fina2.authentication.ldap.organizationalUnit','fina2.authentication.ldap.domainComponent')");
				ps.execute();
				rs = ps.getResultSet();
				if (rs != null)
					while (rs.next()) {
						authProps.put(rs.getString("prop_key"), rs.getString("value"));
					}
			} catch (Exception ex) {
				logg.error(ex.getMessage(), ex);
				throw new EJBException(ex.getMessage(), ex);
			} finally {
				DatabaseUtil.close(rs, ps, con);
			}
		
		return authProps;
	}

	/**
	 * 
	 * @return users list
	 */
	public ArrayList<String> loadUsers() {
		ArrayList<String> users = new ArrayList<String>();
		try {
			loadProperties();

			authContext.setRequestControls(null);
			SearchControls searchControls = new SearchControls();
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			searchControls.setTimeLimit(30000);
			String name = "ou=" + authProps.get(LDAP_ORGANIZATIONAL_UNIT);
			String[] domainComponents = authProps.get(LDAP_DOMAIN_COMPONENT).split(",");
			if (domainComponents != null)
				for (int i = 0; i < domainComponents.length; i++) {
					name += ",dc=" + domainComponents[i];
				}
			NamingEnumeration<?> namingEnum = authContext.search(name, "(objectclass=user)", searchControls);
			while (namingEnum.hasMore()) {
				SearchResult result = (SearchResult) namingEnum.next();
				Attributes attrs = result.getAttributes();
				String s = attrs.get("cn").get().toString();
				users.add(s);
				System.out.println(s);
			}
			namingEnum.close();
		} catch (Exception ex) {
			logg.error(ex.getMessage(), ex);
			throw new EJBException(ex.getMessage(), ex);
		}
		return users;
	}

	/**
	 * 
	 * @param username
	 * @param password
	 * @return true if authentication succeeded ,otherwise returns false
	 * @throws AuthenticationException,NamingException 
	 */
	public boolean authenticateLdap(String username, String password) throws AuthenticationException,NamingException {

		boolean auth = false;
		String ldapHost = authProps.get(LDAP_URL_IP);
		String ldapPort = authProps.get(LDAP_URL_PORT);
		String organizationalUnit = authProps.get(LDAP_ORGANIZATIONAL_UNIT);
		String[] domainComponents = authProps.get(LDAP_DOMAIN_COMPONENT).split(",");
		String ldapUrl = "ldap://" + ldapHost + ":" + ldapPort;
		String baseDN = "CN=" + username + ",OU=" + organizationalUnit;
		if (domainComponents != null)
			for (int i = 0; i < domainComponents.length; i++) {
				baseDN += ",DC=" + domainComponents[i];
			}
		env = new Hashtable<String, String>(11);
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapUrl);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, baseDN);
		env.put(Context.SECURITY_CREDENTIALS, password);

		
			if (!userExists(username))
				throw new AuthenticationException("User Does not exists in db");
			authContext = new InitialLdapContext(env, null);
		
			return auth;
		
	}

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
}