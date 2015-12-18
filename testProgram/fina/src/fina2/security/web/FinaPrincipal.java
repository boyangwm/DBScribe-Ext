package fina2.security.web;

import java.security.*;
import fina2.security.User;
import java.rmi.RemoteException;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class FinaPrincipal implements Principal {

    private String name;
    private String password;
    private User user=null;
    private fina2.i18n.Language language=null;
    private javax.ejb.Handle userHandle=null;
    private javax.ejb.Handle languageHandle=null;

    public FinaPrincipal(fina2.security.User user, fina2.i18n.Language lang) throws RemoteException{
        this.language = lang;
        this.user = user;
        this.languageHandle=this.language.getHandle(); 
        this.userHandle=this.user.getHandle(); 
        name=user.getLogin();
        password=user.getPassword();
    }
    public String toString() {
        StringBuffer sb = new StringBuffer("FinaPrincipal[");
        sb.append(name);
        sb.append("]");
        return sb.toString();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) throws RemoteException{
        this.user = user;
	this.userHandle=this.user.getHandle();
    }
    
   
    /** Getter for property userHandle.
     * @return Value of property userHandle.
     *
     */
    public javax.ejb.Handle getUserHandle() throws RemoteException {
        if(userHandle!=null)
            userHandle=user.getHandle();
        return userHandle;
    }
    
    /** Setter for property userHandle.
     * @param userHandle New value of property userHandle.
     *
     */
    public void setUserHandle(javax.ejb.Handle userHandle) {
        this.userHandle = userHandle;
    }
    
    /** Getter for property languageHandle.
     * @return Value of property languageHandle.
     *
     */
    public javax.ejb.Handle getLanguageHandle() throws RemoteException{
        if(languageHandle!=null)
            languageHandle=language.getHandle();
        return languageHandle;
    }
    
    /** Setter for property languageHandle.
     * @param languageHandle New value of property languageHandle.
     *
     */
    public void setLanguageHandle(javax.ejb.Handle languageHandle) {
        this.languageHandle = languageHandle;
    }
    
    /** Getter for property language.
     * @return Value of property language.
     *
     */
    public fina2.i18n.Language getLanguage() {
        return language;
    }
    
    /** Setter for property language.
     * @param language New value of property language.
     *
     */
    public void setLanguage(fina2.i18n.Language language)  throws RemoteException {
        this.language = language;
        this.languageHandle=  this.language.getHandle();

    }
    
}