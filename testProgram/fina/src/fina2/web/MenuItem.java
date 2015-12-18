/*
 * FinaMunuItem.java
 *
 * Created on 10 December 2003, 02:08
 */

package fina2.web;
import fina2.web.Utilities;
/**
 *
 * @author  Administrator
 */
public class MenuItem {
    
    /** Creates a new instance of FinaMunuItem */
    public static final int SUBMENU=2;
    public static final int MENUITEM=1;
    private int type=MENUITEM;
    private String description=null;
    protected String descriptionKey=null;
    private String name=null;
    private String url=null;
    private String iconUrl=null;
    private FinaAction action=null;
    java.util.Vector children=new java.util.Vector();

    public MenuItem(FinaAction action, Utilities util) {
        this.action=action;
        this.action.setUtil(util);
        description=action.getDescription();
        name=action.getName();
        iconUrl=action.getIconUrl();
        url=action.getUrl();
    }
    public MenuItem() {
    }
    public MenuItem(String desc) {
        description=desc;
    }
    public boolean add(MenuItem item){
        return children.add(item);
    }
    public void add(int index, MenuItem item){
        children.add(index, item);
    }
    public MenuItem remove(int index){
        return (MenuItem)children.remove(index);
    }
    public int getChildCount(){
        return children.size();
    }
    public int getType(){
        return type;
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
    
    /** Setter for property type.
     * @param type New value of property type.
     *
     */
    public void setType(int type) {
        this.type = type;
    }
    public String[] getPermissions(){
        return null;
    }
    
    /** Getter for property action.
     * @return Value of property action.
     *
     */
    public fina2.web.FinaAction getAction() {
        return action;
    }
    
    /** Setter for property action.
     * @param action New value of property action.
     *
     */
    public void setAction(fina2.web.FinaAction action, Utilities util) {
        this.action=action;
        this.action.setUtil(util);
        description=action.getDescription();
        name=action.getName();
        iconUrl=action.getIconUrl();
        url=action.getUrl();
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

    public Utilities getUtil() {
        return action.getUtil();
    }
    
    /** Setter for property util.
     * @param util New value of property util.
     *
     */
    public void setUtil(Utilities util) {
        action.setUtil(util);
    }
}
