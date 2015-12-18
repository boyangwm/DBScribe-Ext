package fina2.upload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.security.auth.login.LoginContext;

import org.apache.log4j.Logger;
import org.jboss.security.auth.callback.UsernamePasswordHandler;

import fina2.i18n.Language;

public class LocaleBean {

	private static Map<String, Map<String, String>> res = new HashMap<String, Map<String, String>>();
	private List<SelectItem> langs = new ArrayList<SelectItem>();
	private String langCode;
	private List<Language> languages;
	private Logger log = Logger.getLogger(LocaleBean.class);
	private static String jndiUrl = "jnp://localhost:1099";

	public LocaleBean() {
		
		try {
			ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
			if(ec.getRemoteUser() == null){
				LoginContext lc = new LoginContext("client-login", new UsernamePasswordHandler("guest", "anonymous".toCharArray()));
				lc.login();
			}
			if(System.getProperty("fina2.server_address") != null) {
				jndiUrl = System.getProperty("fina2.server_address");
			}
			InitialContext jndi = getInitialContext();
			Object ref = jndi.lookup("fina2/upload/UploadFileSession");
			UploadFileSession session = ((UploadFileSessionHome) PortableRemoteObject.narrow(ref, UploadFileSessionHome.class)).create();
			languages = session.getLanguages();
			for (Iterator<Language> it = languages.iterator(); it.hasNext();) {
				Language lang = it.next();
				SelectItem item = new SelectItem(lang.getCode(), lang.getDescription());
				langs.add(item);
			}
			langCode = languages.get(0).getCode();
		} catch (Exception ex) {
			log.error("Can't get Languages from Server", ex);
		}
	}

	public String getLangCode() {
		return langCode;
	}

	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}

	public List<SelectItem> getLangs() {
		return langs;
	}

	public void setLangs(List<SelectItem> langs) {
		this.langs = langs;
	}

	public Map<String, String> getRes() {
		
		if (res.get(langCode) == null) {
			try {
				InitialContext jndi = getInitialContext();
				Object ref = jndi.lookup("fina2/upload/UploadFileSession");
				UploadFileSession session = ((UploadFileSessionHome) PortableRemoteObject.narrow(ref, UploadFileSessionHome.class)).create();
				res.put(langCode, session.getMessageBundle(getLanguage()));
			} catch (Exception ex) {
				log.debug("Can't load Bundle for " + langCode, ex);
			}
		}
		return res.get(langCode);
	}
	
	public Language getLanguage(){
		
		Language result  = null;		
		try	{
			for (Iterator<Language> it = languages.iterator(); it.hasNext();) {
				Language lang = it.next();
				if(lang.getCode().equals(langCode)){
					result = lang;
				}		
			}
		} catch (Exception ex) {
			log.error("Can't Initialize Language", ex);
		}
		
		return result;
	}

	public static InitialContext getInitialContext() throws NamingException {
		
		Properties p = new Properties();
		p.put(Context.INITIAL_CONTEXT_FACTORY,"org.jnp.interfaces.NamingContextFactory");
		p.put(Context.URL_PKG_PREFIXES, " org.jboss.naming:org.jnp.interfaces");
		p.put(Context.PROVIDER_URL, jndiUrl);

		return new InitialContext(p);
	}

}
