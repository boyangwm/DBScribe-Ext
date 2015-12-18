package fina2.security.web;

import org.apache.catalina.Container;
import java.beans.PropertyChangeListener;
import java.security.Principal;
import java.security.cert.X509Certificate;
import org.apache.catalina.Realm;
import fina2.security.UserHome;
import fina2.security.User;
import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;
import java.util.Properties; 
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class FinaRealm implements Realm {

    private Container container;
    public fina2.security.UserHome userHome=null;
    public fina2.i18n.LanguageHome langHome=null;
    private javax.naming.Context jndiContext=null;
    protected static final String info = "fina2.security.FinaRealm/1.0";

    public Container getContainer() {
        return this.container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    /**
     * Return descriptive information about this Realm implementation.
     * @return Realm implementation information.
     */
    public String getInfo() {
        return this.info;
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
    }

    /**
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return null.
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in authenticating this username
     * @return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return null.
     */
    public Principal authenticate(String username, String credentials) {

        System.out.println(   username +"---"+credentials);
        container.getLogger().log("Authenticate request arrived for user="+
                                  username);

        Principal principal = null;
        try {
            initEJBs();
            int pos=username.lastIndexOf('_');
            
            java.util.StringTokenizer st=new java.util.StringTokenizer(username, "_");
            int langId=new Integer(username.substring(pos+1)).intValue();
            username=username.substring(0, pos);
            //st.nextToken();
            User user=userHome.findByLoginPassword(username, credentials);
            
            fina2.i18n.Language lang = langHome.findByPrimaryKey(new fina2.i18n.LanguagePK (langId));

            if (user != null) {
                FinaPrincipal fp = new FinaPrincipal(user, lang);
                principal = fp;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        catch(Throwable  e){
            e.printStackTrace();
        }
        return principal;
    }

    /**
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return null.
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in authenticating
     * this username.
     * @return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return null.
     */
    public Principal authenticate(String username, byte[] credentials) {
        return authenticate(username, credentials.toString());
    }

    public Principal authenticate(String parm1, String parm2, String parm3,
                                  String parm4, String parm5, String parm6,
                                  String parm7, String parm8) {
        return null;
    }

    public Principal authenticate(X509Certificate[] parm1) {
        return null;
    }

    public boolean hasRole(Principal principal, String role) {
        container.getLogger().log("HasRole request. role="+
                                  role);
        boolean hasRole = false;
        try {
            if (principal instanceof FinaPrincipal) {
                if(((FinaPrincipal)principal).getName().equalsIgnoreCase("sa"))
                    return true;
                hasRole = ((FinaPrincipal)principal).getUser().hasPermission(
                    role);
            }
        } catch (RemoteException ex) {
        /** @todo Add error handling code here. */
        }

        return hasRole;
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
    }

    private void initEJBs() throws Exception{
        try {
            if (userHome == null || langHome==null) {
                if(jndiContext==null){
                    Properties prop=new Properties();
                    javax.servlet.ServletContext servletContext=
                        ((org.apache.catalina.core.StandardContext)container).getServletContext();
                    String home=servletContext.getRealPath("/");
                    prop.load(new java.io.FileInputStream(home+"/../conf/jndi.properties"));
                    jndiContext = new javax.naming.InitialContext(prop);
                }   
                
                Object ref = jndiContext.lookup("fina2/security/User");
                userHome=(UserHome)PortableRemoteObject.narrow(ref, UserHome.class);

                ref = jndiContext.lookup("fina2/i18n/Language");
                langHome = (fina2.i18n.LanguageHome)PortableRemoteObject.narrow (ref, fina2.i18n.LanguageHome.class);
               
            }
        } 
        catch (Exception e) { 
            e.printStackTrace();
            throw e;
        }
    }
    
}