/*
 * Utilities.java
 *
 * Created on October 31, 2003, 12:19 PM
 */

package fina2.web;
import java.util.Properties;
import java.io.IOException;
import java.io.FileInputStream;

/**
 *
 * @author  ZBokuchava
 */
public class UtilitiesArray {
    
    /** Creates a new instance of Utilities */
    static UtilitiesArray UtilitiesArray =null;
    static java.util.Hashtable utilitiesTable=new java.util.Hashtable();
    String homeDir=null;
    private UtilitiesArray () {
    }
    public UtilitiesArray(String homeDir)  throws IOException {
        this.homeDir=homeDir;
    }
    
    public static UtilitiesArray getInstance(){
        
        if(UtilitiesArray ==null)
            UtilitiesArray =new UtilitiesArray ();
        return UtilitiesArray ;
    } 
    public static UtilitiesArray getInstance(String homeDir) throws IOException{
        
        if(UtilitiesArray ==null)
            UtilitiesArray =new UtilitiesArray(homeDir);
        return UtilitiesArray ;
    } 
    public Utilities getUtilities(String langCode) throws IOException {
        Utilities util=(Utilities)utilitiesTable.get(langCode);
        if(util==null){
            util=new Utilities(homeDir, langCode);
            utilitiesTable.put(langCode, util);
        }
        return util;
    }
}
