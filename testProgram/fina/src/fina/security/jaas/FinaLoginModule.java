package fina.security.jaas;

import java.rmi.RemoteException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.jboss.logging.Logger;
import org.jboss.security.NestableGroup;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.callback.UsernamePasswordHandler;

import fina2.security.AuthenticatedModeSession;
import fina2.security.AuthenticatedModeSessionBean;
import fina2.security.AuthenticatedModeSessionHome;
import fina2.security.User;
import fina2.security.UserHome;
import fina2.system.PropertySession;
import fina2.system.PropertySessionBean;
import fina2.system.PropertySessionHome;

public class FinaLoginModule implements LoginModule {

	/**
	 * log4j instance for FinaLoginModule class
	 */
	protected Logger log = Logger.getLogger(FinaLoginModule.class);

	private Principal identity;
	protected Subject subject;
	protected CallbackHandler callbackHandler;
	protected Map sharedState;
	protected Map options;
	protected boolean loginOk;

	private String username;
	private String password;
	private LoginContext loginContext;

	static private final String GUEST_USERNAME = "guest";
	static private final String GUEST_PASSWORD = "anonymous";
	static private final String GUEST_ROLE = "fina.guest";
	static private final String FINA_AUTHENTICATED_ROLE = "fina.authenticated.user";

	private static UserHome userHome;
	private static AuthenticatedModeSessionHome authHome;
	private static AuthenticatedModeSession authSession; 
	private String prop=null;
	
	private User user;

	/**
	 * Initialize this LoginModule.
	 * 
	 * @param subject
	 *            the <code>Subject</code> to be authenticated.
	 *            <p>
	 * 
	 * @param callbackHandler
	 *            a <code>CallbackHandler</code> for communicating with the end
	 *            user (prompting for usernames and passwords, for example).
	 *            <p>
	 * 
	 * @param sharedState
	 *            state shared with other configured LoginModules.
	 *            <p>
	 * 
	 * @param options
	 *            options specified in the login <code>Configuration</code> for
	 *            this particular <code>LoginModule</code>.
	 */
	public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {

		log.debug("Initializing FinA login module: " + this);

		this.subject = subject;
		this.callbackHandler = callbackHandler;
		this.sharedState = sharedState;
		this.options = options;
	}

	public boolean login() throws javax.security.auth.login.LoginException {

		log.debug("Trying to login");
		// prompt for a username and password
		if (callbackHandler == null) {
			throw new LoginException("Error: no CallbackHandler available to garner " + "authentication information from the user");
		}

		Callback[] callbacks = new Callback[2];
		callbacks[0] = new NameCallback("Username: ");
		callbacks[1] = new PasswordCallback("Password: ", false);

		try {
			callbackHandler.handle(callbacks);
			username = ((NameCallback) callbacks[0]).getName();
			char[] tmpPassword = ((PasswordCallback) callbacks[1]).getPassword();
			if (tmpPassword == null) {
				// treat a NULL password as an empty password
				tmpPassword = new char[0];
			}
			password = new String(tmpPassword);
			
			((PasswordCallback) callbacks[1]).clearPassword();

			// Initialize security EJB
			initialize();
			
			
			
			
		} catch (java.io.IOException ioe) {
			throw new LoginException(ioe.toString());
		} catch (UnsupportedCallbackException uce) {
			throw new LoginException("Error: " + uce.getCallback().toString() + " not available to garner authentication" + " information from the user");
		} 

		// verify the username/password
		log.debug("Authenticating user: " + username);
		if (authenticate()) {
			identity = new SimplePrincipal(username);
			loginOk = true;
			log.debug("Login successful");
			return loginOk;
		} else {
			identity = null;
			username = null;
			password = null;
			log.debug("Login failed");
			throw new FailedLoginException("Password Incorrect");
		}
	}

	public boolean commit() throws LoginException {
		log.debug("Commit, loginOk=" + loginOk);
		if (loginOk == false)
			return false;

		Set principals = subject.getPrincipals();
		Principal identity = getIdentity();
		principals.add(identity);
		Group[] roleSets = getRoleSets();
		for (int g = 0; g < roleSets.length; g++) {
			Group group = roleSets[g];
			String name = group.getName();
			Group subjectGroup = createGroup(name, principals);
			if (subjectGroup instanceof NestableGroup) {
				/*
				 * A NestableGroup only allows Groups to be added to it so we
				 * need to add a SimpleGroup to subjectRoles to contain the
				 * roles
				 */
				SimpleGroup tmp = new SimpleGroup("Roles");
				subjectGroup.addMember(tmp);
				subjectGroup = tmp;
			}
			// Copy the group members to the Subject group
			Enumeration members = group.members();
			while (members.hasMoreElements()) {
				Principal role = (Principal) members.nextElement();
				subjectGroup.addMember(role);
			}
		}

		if (loginContext != null) {
			loginContext.logout();
		}

		return true;
	}

	public boolean abort() throws LoginException {
		log.debug("Abort");

		if (loginContext != null) {
			loginContext.logout();
		}

		return true;
	}

	public boolean logout() throws LoginException {
		log.debug("Logout");
		// Remove the user identity
		Principal identity = getIdentity();
		Set principals = subject.getPrincipals();
		principals.remove(identity);
		// Remove any added Groups...
		return true;
	}

	protected Group[] getRoleSets() throws LoginException {

		Group[] roleSets = new Group[1];
		roleSets[0] = new SimpleGroup("Roles");
		ArrayList groups = new ArrayList();
		groups.add(roleSets[0]);

		String[] roles = getRoles();
		for (int i = 0; i < roles.length; i++) {

			SimplePrincipal sp = new SimplePrincipal(roles[i]);
			roleSets[0].addMember(sp);
		}
		return roleSets;
	}

	private Group createGroup(String name, Set principals) {
		Group roles = null;
		Iterator iter = principals.iterator();
		while (iter.hasNext()) {
			Object next = iter.next();
			if ((next instanceof Group) == false)
				continue;
			Group grp = (Group) next;
			if (grp.getName().equals(name)) {
				roles = grp;
				break;
			}
		}
		// If we did not find a group create one
		if (roles == null) {
			roles = new NestableGroup(name);
			principals.add(roles);
		}
		return roles;
	}

	private Principal getIdentity() {
		return identity;
	}

	private boolean authenticate() {

		if (isGuest())
			return true;

		if (userHome != null) {
			
			try {
				
				
				authSession= authHome.create();
				prop=authSession.loadProperties().get(AuthenticatedModeSessionBean.FINA_CURRENT_AUTHENTICATION);
				if (prop.equals("LDAP")) {
					this.user = userHome.findByLogin(username);
				}		
				else {
					this.user = userHome.findByLoginPassword(username, password);					
				}

			} catch (Throwable ex) {
				log.error("Error occured during authentication!", ex);
			}
		}
		
		return (user != null);
	}

	private String[] getRoles() {

		String[] userRoles = null;
		if (isGuest()) {
			userRoles = new String[] { GUEST_ROLE };
		} else if (this.user != null) {
			try {
				ArrayList roles = new ArrayList();

				roles.add(FINA_AUTHENTICATED_ROLE);

				roles.addAll(this.user.getPermissions());

				userRoles = (String[]) roles.toArray(new String[0]);
			} catch (Exception ex) {
				log.error("Error occured during authentication!", ex);
			}
		}

		if (userRoles != null) {
			log.debug("User roles: " + Arrays.asList(userRoles));
		}

		return userRoles;
	}

	private void initialize() {

		try {
			if (!isGuest()) {

				loginContext = new LoginContext("client-login", new UsernamePasswordHandler(GUEST_USERNAME, GUEST_PASSWORD.toCharArray()));
				loginContext.login();

				if (userHome == null) {
					Context context = null;
					if (System.getProperty("fina2.server_address") == null) {
						context = new InitialContext();
					} else {
						Properties p = new Properties();
						p.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
						p.put(Context.URL_PKG_PREFIXES, " org.jboss.naming:org.jnp.interfaces");
						p.put(Context.PROVIDER_URL, System.getProperty("fina2.server_address"));
						context = new InitialContext(p);
					}
					// look up jndi name
					Object ref = context.lookup("fina2/security/User");
					Object ldapRef = context.lookup("fina2/security/AuthenticatedModeSession");
					
					authHome =(AuthenticatedModeSessionHome)PortableRemoteObject.narrow(ldapRef, AuthenticatedModeSessionHome.class);
					// look up jndi name and cast to Home interface
					userHome = (UserHome) PortableRemoteObject.narrow(ref, UserHome.class);
					
					
				}
			}
		} catch (Throwable ex) {
			log.error("Failed initializing security bean access.", ex);
		}
	}

	private boolean isGuest() {
		return (GUEST_USERNAME.equals(this.username) && GUEST_PASSWORD.equals(this.password));
	}
}
