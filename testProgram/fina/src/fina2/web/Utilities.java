/*
 * Utilities.java
 *
 * Created on October 31, 2003, 12:19 PM
 */

package fina2.web;
import fina2.i18n.Language;
import fina2.security.User;
import javax.ejb.Handle;
import java.util.Properties;
import java.io.IOException;
import java.io.FileInputStream;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;
import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;
import javax.naming.InitialContext;
import fina2.web.UtilitiesSession;
import fina2.web.UtilitiesSessionHome;
import fina2.ui.tree.Node;
import fina2.reportoo.server.ReportPK; 
import fina2.security.UserPK; 
import fina2.security.RolePK; 
import fina2.ui.menu.MenuPK; 
import fina2.returns.ReturnConstants;

import java.util.Date;
import java.util.SimpleTimeZone;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.text.DecimalFormat;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 *
 * @author  ZBokuchava
 */
public class Utilities {
    
    /** Creates a new instance of Utilities */
    static Utilities utilities=null;
    Properties messages=new Properties();
    String homeDir=null;
    String langCode=null;
    InitialContext context=null;
    UtilitiesSession utilitiesSession=null;
    
    public static String[][] actions=
    {
        {"fina2.actions.bankGroups","fina2.bank.bankGroupsAction"},
        {"fina2.actions.bankTypes","fina2.bank.bankTypesAction"},
        {"fina2.actions.banks","fina2.bank.banks"},
        {"fina2.actions.metadataComparisons","fina2.metadata.comparisonrules"},
        {"fina2.nbg.excel","fina2.action.excel"},
        {"fina2.actions.fileRobot","fina2.returns.fileRobot"},
        {"fina2.actions.formulaRepository","fina2.metadata.frAction"},
        {"fina2.actions.import","fina2.returns.import"},
        {"fina2.actions.languages","fina2.i18n.languages"},
        {"fina2.actions.licences","fina2.bank.licenceTypesAction"},
        {"fina2.actions.managingBody","fina2.bank.managingBodyAction"},
        {"fina2.actions.menuAmend","fina2.ui.menu.menuAmendAction"},
        {"fina2.actions.MDTAmend","fina2.metadata.MDTAmendAction"},
        {"fina2.period.periodAutoInsert","fina2.period.periodAutoInsert"},
        {"fina2.actions.periodTypes","fina2.period.periodTypes"},
        {"fina2.actions.periods","fina2.period.periods"},
        {"fina2.actions.region","fina2.bank.bankRegionsAction"},
        {"fina2.actions.reportManager","fina2.report.reportManager"},
        {"fina2.actions.returnDefinitions","fina2.returns.returnDefinitionsAction"},
        {"fina2.actions.returnManager","fina2.returns.returnManagerAction"},
        {"fina2.actions.returnsStatuses","fina2.action.returnsStatuses"},
        {"fina2.actions.returnTypes","fina2.returns.returnTypesAction"},
        {"fina2.returns.autoSchedule","fina2.returns.autoSchedule"},
        {"fina2.actions.schedules","fina2.returns.schedulesAction"},
        {"fina2.actions.users","fina2.security.usersAction"}
    };
    
    public Utilities() {
    }
    public Utilities(String homeDir, String langCode)  throws IOException {
        this.homeDir=homeDir;
        this.langCode=langCode;
        loadResource();
    }
    
    public java.lang.String getString(java.lang.String key) {
        String s = messages.getProperty(key);
        return s;
    }
    
    public java.lang.String getString(String key,String def) {
        String s = messages.getProperty(key);
        if(s==null)
            return def;
        return s;
    }
    public void loadResource() throws IOException {
        FileInputStream fi = new FileInputStream(homeDir+"/conf/messages_"+langCode+".properties");
        messages.load(fi);
        fi.close();
    }
    public static UserTransaction getUserTransaction(InitialContext jndi) throws NamingException {
        Object ref = jndi.lookup("java:/UserTransaction");
        UserTransaction trans = (UserTransaction)PortableRemoteObject.narrow (ref, UserTransaction.class);
        return trans;
    }
    public static boolean isValidCode(String code) {
        if(code==null)
            return false;
        if(code.length()==0 )
            return false;
        String s = "QWERTYUIOPASDFGHJKLZXCVBNM:{},.<>1234567890_&@#$!";
        StringBuffer sb = new StringBuffer(code.toUpperCase());
        for(int i=0; i<sb.length(); i++) {
            char c = sb.charAt(i);
            if(s.indexOf(c) == -1)
                return false;
        }
        return true;
    }
    
    /** Getter for property lang.
     * @return Value of property lang.
     *
     */
    public UtilitiesSession getUtilitiesSession() throws javax.naming.NamingException, javax.ejb.CreateException, java.rmi.RemoteException{
        if(utilitiesSession==null){
            Object obj=context.lookup("fina2/web/UtilitiesSession");    
            UtilitiesSessionHome home =(UtilitiesSessionHome)PortableRemoteObject.narrow(obj, UtilitiesSessionHome.class);
            utilitiesSession =home.create();
            utilitiesSession.setMessages(messages); 
        }
        try{
            utilitiesSession.test();
        }
        catch(java.rmi.NoSuchObjectException e){
            Object obj=context.lookup("fina2/web/UtilitiesSession");    
            UtilitiesSessionHome home =(UtilitiesSessionHome)PortableRemoteObject.narrow(obj, UtilitiesSessionHome.class);
            utilitiesSession =home.create();
            utilitiesSession.setMessages(messages); 
        }
        return utilitiesSession;
    }
    
    /** Getter for property context.
     * @return Value of property context.
     *
     */
    public javax.naming.InitialContext getContext() {
        return context;
    }
    
    /** Setter for property context.
     * @param context New value of property context.
     *
     */
    public void setContext(javax.naming.InitialContext context) {
        this.context = context;
    }
    public static String date2string(Handle languageHandle, Date date) throws RemoteException {

        Language lang = (Language)languageHandle.getEJBObject();
        /*SimpleDateFormat format = new SimpleDateFormat(
            lang.getDateFormat()
        );
        
        return format.format(date);*/
        return date2string(lang, date);
    }

    public static String date2string(Language lang, Date date) throws RemoteException {

        //Language lang = (Language)languageHandle.getEJBObject();
        SimpleDateFormat format = new SimpleDateFormat(
            lang.getDateFormat()
        );
        ((java.util.SimpleTimeZone)java.util.SimpleTimeZone.getDefault()).setStartYear(250);
        ((java.util.SimpleTimeZone)format.getTimeZone()).setStartYear(250); //410576
        
        return format.format(date);
    }
    public static Date string2date(Language lang , String string) throws RemoteException, fina2.FinaException{

        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(
            lang.getDateFormat()
        );
        ((java.util.SimpleTimeZone)java.util.SimpleTimeZone.getDefault()).setStartYear(250);
        ((java.util.SimpleTimeZone)format.getTimeZone()).setStartYear(250);
        
        format.setLenient(false);
        try{
            java.util.Date d=format.parse(string);
            return d;
        }
        catch(Exception e){
            throw new fina2.FinaException("fina2.invalidDateFormat");
        }
    }

    public static Date string2date(Handle languageHandle, String string) throws RemoteException, fina2.FinaException{

        Language lang = (Language)languageHandle.getEJBObject();
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(
            lang.getDateFormat()
        );
        ((java.util.SimpleTimeZone)java.util.SimpleTimeZone.getDefault()).setStartYear(250);
        ((java.util.SimpleTimeZone)format.getTimeZone()).setStartYear(250);
        format.setLenient(false);
        try{
            java.util.Date d=format.parse(string);
            return d;
        }
        catch(Exception e){
            throw new fina2.FinaException("fina2.invalidDateFormat");
        }
    }

    public static String number2string(Handle languageHandle, double num) throws RemoteException {

        Language lang = (Language)languageHandle.getEJBObject();
        DecimalFormat format = new DecimalFormat(
            lang.getNumberFormat()
        );
        
        return format.format(num);
    }

    public static double string2number(Handle languageHandle, String string) throws RemoteException, fina2.FinaException{

        Language lang = (Language)languageHandle.getEJBObject();
        DecimalFormat format = new DecimalFormat(
            lang.getNumberFormat()
        );
        
        double d=-1;
        try{
            d=format.parse(string).doubleValue();
            return d;
        }
        catch(Exception e){
            throw new fina2.FinaException("fina2.invalidNumberFormat");
        }
    }
    public static String number2string(Language lang, double num) throws RemoteException {

        DecimalFormat format = new DecimalFormat(
            lang.getNumberFormat()
        );
        return format.format(num);
    }

    public static double string2PositiveNumber(Language lang, String string) throws RemoteException, fina2.FinaException{

        DecimalFormat format = new DecimalFormat(
            lang.getNumberFormat()
        );
        
        double d=-1;
        try{
            Number n=null;
            n=format.parse(string);
            if(String.valueOf(n).length()<string.length())
                throw new fina2.FinaException("fina2.invalidNumberFormat");
            d=n.doubleValue();
        }
        catch(fina2.FinaException e){
            throw e;
        }
        catch(Exception e){
            throw new fina2.FinaException("fina2.invalidNumberFormat");
        }
        if(d<0)
            throw new fina2.FinaException("fina2.invalidPositiveNumberValue");
        return d;
    }
    public static double string2number(Language lang, String string) throws RemoteException, fina2.FinaException{

        DecimalFormat format = new DecimalFormat(
            lang.getNumberFormat()
        );
        
        double d=-1;
        try{
            d=format.parse(string).doubleValue();
            return d;
        }
        catch(Exception e){
            throw new fina2.FinaException("fina2.invalidNumberFormat");
        }
    }
    public static String getEscapedJavascriptString(String source)
    {
        StringTokenizer st=new StringTokenizer(source,"\r\n",true);
        StringBuffer sbret=new StringBuffer();
        String str=null;
        while(st.hasMoreTokens())
        {
            str=st.nextToken();
            if(sbret.length()!=0)
                sbret.append("+");
            if(str.equals("\r") || str.equals("\n"))
            {
                sbret.append("'\\r\\n'");
                if(str.equals("\r"))
                    str=st.nextToken();
            }
            else
                sbret.append("'"+str.replace('\'','"')+"'");
        }
        return sbret.toString();
    }
    public static String getNodeTreeBuildString(Node node,int level)
    {
        StringBuffer sbret=new StringBuffer();
        sbret.append("[");
        sbret.append("'"+node.getLabel()+"'");
        sbret.append(",");
        Vector v=node.getChildren();
        if(v.size()==0)
        {
            sbret.append("[]");
        }
        else
        {
            sbret.append("\r\n");
            for(int i=0;i<v.size();i++)
            {
                if(i!=0)
                    sbret.append(",");
                //sbret.append("'");
                sbret.append(getNodeTreeBuildString((Node)v.get(i),level+1));
                //sbret.append("'");
            }
        }
        sbret.append("]");
        return sbret.toString();
    }
    
    public static String getNodeTreeUserObjectBuildString(Node node,int level)
    {
        StringBuffer sbret=new StringBuffer();
        sbret.append("[");
        sbret.append("'"+((ReportPK)node.getPrimaryKey()).getId()+"'");
        sbret.append(",");
        sbret.append(((Integer)node.getType()).intValue());
        sbret.append(","); 
        Vector v=node.getChildren();
        if(v.size()==0)
        {
            sbret.append("[]");
        }
        else
        {
            sbret.append("\r\n");
            for(int i=0;i<v.size();i++)
            {
                if(i!=0)
                    sbret.append(",");
                //sbret.append("'");
                sbret.append(getNodeTreeUserObjectBuildString((Node)v.get(i),level+1));
                //sbret.append("'");
            }
        }
        sbret.append("]");
        return sbret.toString();
    } 
    public static String getUserNodeTreeUserObjectBuildString(Node node,int level)
    {
        StringBuffer sbret=new StringBuffer();
        sbret.append("[");
        if(node.getPrimaryKey() instanceof UserPK)
            sbret.append("'"+((UserPK)node.getPrimaryKey()).getId()+"'");
        else
            if(node.getPrimaryKey() instanceof RolePK)
                sbret.append("'"+((RolePK)node.getPrimaryKey()).getId()+"'");
        sbret.append(",");
        Vector v=node.getChildren();
        if(v.size()==0)
        {
            sbret.append("[]");
        }
        else
        {
            sbret.append("\r\n");
            for(int i=0;i<v.size();i++)
            {
                if(i!=0)
                    sbret.append(",");
                //sbret.append("'");
                sbret.append(getUserNodeTreeUserObjectBuildString((Node)v.get(i),level+1));
                //sbret.append("'");
            }
        }
        sbret.append("]");
        return sbret.toString();
    }
    
    public static String getMenuNodeTreeUserObjectBuildString(Node node,int level)
    {
        StringBuffer sbret=new StringBuffer();
        sbret.append("[");
        sbret.append("'"+((MenuPK)node.getPrimaryKey()).getId()+"'");
        sbret.append(",");
        sbret.append(((Integer)node.getType()).intValue());
        sbret.append(",");
        Vector v=node.getChildren();
        if(v.size()==0)
        {
            sbret.append("[]");
        }
        else
        {
            sbret.append("\r\n");
            for(int i=0;i<v.size();i++)
            {
                if(i!=0)
                    sbret.append(",");
                //sbret.append("'");
                sbret.append(getMenuNodeTreeUserObjectBuildString((Node)v.get(i),level+1));
                //sbret.append("'");
            }
        }
        sbret.append("]");
        return sbret.toString();
    }     
    public static boolean validDate(Language lang,String source)
    {
        try
        {
            string2date(lang, source);
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
    public int statusString2Int(String status){
        if(status.equals(getString(ReturnConstants.STATUS_CREATED_STR)))
            return ReturnConstants.STATUS_CREATED;
        if(status.equals(getString(ReturnConstants.STATUS_AMENDED_STR)))
            return ReturnConstants.STATUS_AMENDED;
        if(status.equals(getString(ReturnConstants.STATUS_IMPORTED_STR)))
            return ReturnConstants.STATUS_IMPORTED;
        if(status.equals(getString(ReturnConstants.STATUS_PROCESSED_STR)))
            return ReturnConstants.STATUS_PROCESSED;
        if(status.equals(getString(ReturnConstants.STATUS_RESETED_STR)))
            return ReturnConstants.STATUS_RESETED;
        if(status.equals(getString(ReturnConstants.STATUS_ACCEPTED_STR)))
            return ReturnConstants.STATUS_ACCEPTED;
        if(status.equals(getString(ReturnConstants.STATUS_REJECTED_STR)))
            return ReturnConstants.STATUS_REJECTED;
        if(status.equals(getString(ReturnConstants.STATUS_ERRORS_STR)))
            return ReturnConstants.STATUS_ERRORS;
        return 0;
    }
    public String statusInt2String(int status){
        switch(status){
            case ReturnConstants.STATUS_CREATED:
                return getString(ReturnConstants.STATUS_CREATED_STR);
            case ReturnConstants.STATUS_AMENDED:
                return getString(ReturnConstants.STATUS_AMENDED_STR);
            case ReturnConstants.STATUS_IMPORTED:
                return getString(ReturnConstants.STATUS_IMPORTED_STR);
            case ReturnConstants.STATUS_PROCESSED:
                return getString(ReturnConstants.STATUS_PROCESSED_STR);
            case ReturnConstants.STATUS_RESETED:
                return getString(ReturnConstants.STATUS_RESETED_STR);
            case ReturnConstants.STATUS_ACCEPTED:
                return getString(ReturnConstants.STATUS_ACCEPTED_STR);
            case ReturnConstants.STATUS_REJECTED:
                return getString(ReturnConstants.STATUS_REJECTED_STR);
            case ReturnConstants.STATUS_ERRORS:
                return getString(ReturnConstants.STATUS_ERRORS_STR);
            default :
                    return " ";
        }
    }
/* 
 *vano  
    public static Date string2date(Language lang,String source) throws RemoteException,ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat(lang.getDateFormat());
        return format.parse(source);
    } 
 */
}
