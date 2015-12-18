/*
 * FinaAction.java
 *
 * Created on 31 Èþëü 2002 ã., 20:19
 */

package fina2.web;
import fina2.web.Utilities;
/**
 *
 * @author  Administrator
 * @version 
 */
 //abstract 
 public class FinaAction {
    protected String description=null;
    protected String strID=null;
    protected String name=null;
    protected String url=null;
    protected String iconUrl=null;
    protected String[] permissions=null;
    private Utilities util=null;
//    abstract 
    public String[] getPermissions(){
        return permissions;
    }
    public  void setPermissions(String[] perissions){
        this.permissions=permissions;
    }
    
    public FinaAction() {
    }
    public FinaAction(String name, String strID, String url, String iconUrl, String[] permissions) {
        this.name=name;
        
        this.strID=strID;
        this.url=url;
        this.iconUrl=iconUrl;
        this.permissions=permissions;
    }
    /** Getter for property description.
     * @return Value of property description.
     *
     */
    public java.lang.String getDescription() {
        return description;
    }
    
    /** Setter for property description.
     * @param description New value of property description.
     *
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }
    
    
    /** Getter for property name.
     * @return Value of property name.
     *
     */
    public java.lang.String getName() {
        return name;
    }
    
    /** Setter for property name.
     * @param name New value of property name.
     *
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }
    
    /** Getter for property iconURL.
     * @return Value of property iconURL.
     *
     */
    
    /** Getter for property url.
     * @return Value of property url.
     *
     */
    public java.lang.String getUrl() {
        return url;
    }
    
    /** Setter for property url.
     * @param url New value of property url.
     *
     */
    public void setUrl(java.lang.String url) {
        this.url = url;
    }
    
    /** Getter for property iconUrl.
     * @return Value of property iconUrl.
     *
     */
    public java.lang.String getIconUrl() {
        return iconUrl;
    }
    
    /** Setter for property iconUrl.
     * @param iconUrl New value of property iconUrl.
     *
     */
    public void setIconUrl(java.lang.String iconUrl) {
        this.iconUrl = iconUrl;
    }
    
    /** Getter for property util.
     * @return Value of property util.
     *
     */
    public Utilities getUtil() {
        return util;
    }
    
    /** Setter for property util.
     * @param util New value of property util.
     *
     */
    public void setUtil(Utilities util) {
        this.util = util;
    }
    
}

