/*
 * UserInfo.java
 *
 * Created on November 6, 2003, 10:28 AM
 */

package fina2.web;
import fina2.i18n.Language;
import fina2.security.User;
import javax.ejb.Handle;
import java.rmi.RemoteException;
/**
 *
 * @author  zbokuchava
 */
public class UserInfo {
    private Language language;
    
    private User user;
    
    private Handle userHandle;
    
    private Handle langHandle;
    
    /** Creates a new instance of UserInfo */
    public UserInfo() {
    }
    public UserInfo(User user, Language lang) throws RemoteException{
        this.language = lang;
        langHandle=language.getHandle();
        this.user = user;
        userHandle=this.user.getHandle();
    }
    public Language getLanguage() {
        return language;
    }
    
    /** Setter for property lang.
     * @param lang New value of property lang.
     *
     */
    public void setLanguage(Language lang) throws RemoteException  {
        this.language = lang;
        langHandle=language.getHandle();
    }
    
    /** Getter for property langHandle.
     * @return Value of property langHandle.
     *
     */
    public Handle getLanguageHandle() {
        return langHandle;
    }
    
    /** Setter for property langHandle.
     * @param langHandle New value of property langHandle.
     *
     */
    public void setLanguageHandle(Handle langHandle) {
        this.langHandle = langHandle;
        
    }
    
    /** Getter for property user.
     * @return Value of property user.
     *
     */
    public User getUser() {
        return user;
    }
    
    /** Setter for property user.
     * @param user New value of property user.
     *
     */
    public void setUser(User user) throws RemoteException{
        this.user = user;
        userHandle=this.user.getHandle();
    }
    
    /** Getter for property UserHandle.
     * @return Value of property UserHandle.
     *
     */
    public Handle getUserHandle() {
        return userHandle;
    }
    
    /** Setter for property UserHandle.
     * @param UserHandle New value of property UserHandle.
     *
     */
    public void setUserHandle(Handle userHandle) {
        this.userHandle = userHandle;
    }
  
}
