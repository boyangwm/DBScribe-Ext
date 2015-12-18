/*
 * ReturnManagerHandler.java
 *
 * Created on December 2, 2003, 3:21 PM
 */

package fina2.web;
import java.util.Iterator;
import java.util.Collection;
import java.util.Vector;
import fina2.returns.ReturnSessionHome;
import fina2.returns.ReturnSession;
import fina2.returns.ReturnPK;
import fina2.returns.ProcessSessionHome;
import fina2.returns.ProcessSession;
import fina2.returns.ReturnDefinitionHome;
import fina2.returns.ReturnDefinition;
import fina2.returns.ReturnDefinitionPK;
import fina2.returns.ReturnConstants;
import fina2.ui.table.TableRow;
import fina2.ui.table.TableRowImpl;
import fina2.web.Utilities;
import fina2.security.web.FinaPrincipal;
import fina2.returns.SchedulePK;
import fina2.returns.ReturnPK;
import fina2.returns.ValuesTableRow;
import fina2.returns.DefinitionTable;
import fina2.ui.tree.Node;
import fina2.metadata.MDTConstants;
import fina2.FinaException;
import fina2.metadata.MDTNodePK;
import javax.naming.Context;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession ;

/**
 *
 * @author  zbokuchava
 */
public class ReturnManagerHandler {
    
    /** Creates a new instance of ReturnManagerHandler */
    TableRow row = null;
    Context jndiContext=null;
    Utilities util=null;
    FinaPrincipal userInfo=null;
    public ReturnManagerHandler() {
    }
    public ReturnManagerHandler(Context jndiContext, Utilities util, FinaPrincipal userInfo, TableRow row) {
	this.row=row;
	this.jndiContext=jndiContext;
	this.util=util;
	this.userInfo=userInfo;
    }
    public void accept() throws Exception{
	Object ref = jndiContext.lookup("fina2/returns/ReturnSession");
	ReturnSessionHome returnSessionHome= (ReturnSessionHome)PortableRemoteObject.narrow (ref, ReturnSessionHome.class);
	ReturnSession returnSession = returnSessionHome.create();
            
	ref = jndiContext.lookup("fina2/returns/ReturnDefinition");
	ReturnDefinitionHome returnDefinitionHome= (ReturnDefinitionHome)PortableRemoteObject.narrow (ref, ReturnDefinitionHome.class);
            
	Collection w = returnSession .getDependentReturns((ReturnPK)row.getPrimaryKey());
	getDR(w);
        
	String text = "";
	String msg = util.getString("fina2.returns.audit.text2")+util.getString(ReturnConstants.STATUS_ACCEPTED_STR);
	for(Iterator iter = w.iterator(); iter.hasNext();) {
		TableRow r = (TableRow)iter.next();
		Integer pki = new Integer(r.getValue(2));
		ReturnDefinitionPK pk = new ReturnDefinitionPK( pki.intValue() );
		ReturnDefinition rd = returnDefinitionHome.findByPrimaryKey(pk);
		int st = new Integer(r.getValue(1)).intValue();
		if( (st!=ReturnConstants.STATUS_ACCEPTED) ) {
		    text = text+"("+rd.getCode()+") "+rd.getDescription(userInfo.getLanguageHandle())+"\n";
		}
	    }
	    if(!text.equals("")) {
/////                    util.showLongMessageBox(null, msg, text);
	    } else {
		returnSession.toAuditLog("\""+util.getString("fina2.returns.accepted")+"\","+"\""+row.getValue(0)+"\",\""+row.getValue(1)+"\",\""+row.getValue(2)+"\",\""+row.getValue(4)+"\""+",\"\",\"\",\"\",\"\",\"\",\"\"", userInfo.getUserHandle(), userInfo.getLanguageHandle());
		changeStatus(ReturnConstants.STATUS_ACCEPTED, util.getString(ReturnConstants.STATUS_ACCEPTED_STR));
	    }
    }
    public void reProcess() throws Exception{
        long t1 = 0;
        long t2 = 0;
            
	Object ref = jndiContext.lookup("fina2/returns/ProcessSession");
	ProcessSessionHome processHome = (ProcessSessionHome)PortableRemoteObject.narrow (ref, ProcessSessionHome.class);
	ProcessSession processSession = processHome.create();
            
	ref = jndiContext.lookup("fina2/returns/ReturnSession");
	ReturnSessionHome returnHome = (ReturnSessionHome)PortableRemoteObject.narrow (ref, ReturnSessionHome.class);
	ReturnSession returnSession = returnHome.create();
            
	ref = jndiContext.lookup("fina2/returns/ReturnDefinition");
	ReturnDefinitionHome returnDefinitionHome = (ReturnDefinitionHome)PortableRemoteObject.narrow (ref, ReturnDefinitionHome.class);
            
	Collection v = returnSession.getUsedInReturns((ReturnPK)row.getPrimaryKey());
	getUR(v);
            
	Collection w = returnSession.getDependentReturns((ReturnPK)row.getPrimaryKey());
	getDR(w);
            
	String text = "";
	String msg = util.getString("fina2.returns.audit.text1");
	for(Iterator iter = v.iterator(); iter.hasNext();) {
	    TableRow r = (TableRow)iter.next();
	    Integer pki = new Integer(r.getValue(2));
	    ReturnDefinitionPK pk = new ReturnDefinitionPK( pki.intValue() );
	    ReturnDefinition rd = returnDefinitionHome.findByPrimaryKey(pk);
	    int st = new Integer(r.getValue(1)).intValue();
	    if( st==ReturnConstants.STATUS_ACCEPTED ) {
		text = text+"("+rd.getCode()+") "+rd.getDescription(userInfo.getLanguageHandle())+util.getString("fina2.returns.audit.text5")+"\""+util.getString(ReturnConstants.STATUS_ACCEPTED_STR)+"\"\n";
	    }
	}
	if(!text.equals("")) {
/////                util.showLongMessageBox(null, msg, text);
	} else {
	    text = "";
	    msg = util.getString("fina2.returns.audit.text2")+util.getString(ReturnConstants.STATUS_ACCEPTED_STR)+", "+util.getString(ReturnConstants.STATUS_PROCESSED_STR);
	    for(Iterator iter = w.iterator(); iter.hasNext();) {
		TableRow r = (TableRow)iter.next();
		Integer pki = new Integer(r.getValue(2));
		ReturnDefinitionPK pk = new ReturnDefinitionPK( pki.intValue() );
		ReturnDefinition rd = returnDefinitionHome.findByPrimaryKey(pk);
		int st = new Integer(r.getValue(1)).intValue();
		if( (st==ReturnConstants.STATUS_ACCEPTED) || (st==ReturnConstants.STATUS_PROCESSED) ) {

		} else {
		    text = text+"("+rd.getCode()+") "+rd.getDescription(userInfo.getLanguageHandle())+"\n";
		}
	    }
	    if(!text.equals("")) {
//////                    util.showLongMessageBox(null, msg, text);
	    } 
	    else {
		t1 = System.currentTimeMillis();
		System.out.println("Start process");
		msg = processSession.process(userInfo.getUserHandle(), (ReturnPK)row.getPrimaryKey(), true);
		System.out.println("End process");

		t2 = System.currentTimeMillis();


		System.out.println("Start status");
		if(msg.equals("")) {
		    msg = util.getString("fina2.returns.processed")+"\n";
		    returnSession.changeReturnStatus(
			 userInfo.getUserHandle(),
			 userInfo.getLanguageHandle(),
			(ReturnPK)row.getPrimaryKey(),
			ReturnConstants.STATUS_PROCESSED, msg 
		    );
		    returnSession.toAuditLog("\""+util.getString("fina2.returns.processed")+"\","+"\""+row.getValue(0)+"\",\""+row.getValue(1)+"\",\""+row.getValue(2)+"\",\""+row.getValue(4)+"\""+",\"\",\"\",\"\",\"\",\"\",\"\"", userInfo.getUserHandle(), userInfo.getLanguageHandle());
		    row.setValue(6, util.getString(ReturnConstants.STATUS_PROCESSED_STR));
		} else {
		    returnSession.changeReturnStatus(
			userInfo.getUserHandle(),
			userInfo.getLanguageHandle(),
			(ReturnPK)row.getPrimaryKey(),
			ReturnConstants.STATUS_ERRORS, msg 
		    );
		    returnSession.toAuditLog("\""+util.getString("fina2.returns.errors")+"\","+"\""+row.getValue(0)+"\",\""+row.getValue(1)+"\",\""+row.getValue(2)+"\",\""+row.getValue(4)+"\""+",\"\",\"\",\"\",\"\",\"\",\"\"", userInfo.getUserHandle(), userInfo.getLanguageHandle());
		    row.setValue(6, util.getString(ReturnConstants.STATUS_ERRORS_STR));
		}
		System.out.println("End status");

		int version = Integer.valueOf(row.getValue(5)).intValue();
		row.setValue(5, String.valueOf(version+1));
		//row.setValue(5, String.valueOf(_session.getVersion((ReturnPK)row.getPrimaryKey())));
		System.out.println("End status2");
	    }
	}
    }
    public void process() throws Exception{
        long t1 = 0;
        long t2 = 0;
        Object ref = jndiContext.lookup("fina2/returns/ProcessSession");
        ProcessSessionHome processsHome = (ProcessSessionHome)PortableRemoteObject.narrow (ref, ProcessSessionHome.class);

        ProcessSession processSession = processsHome.create();
            
            
        ref = jndiContext.lookup("fina2/returns/ReturnSession");
        ReturnSessionHome returnHome = (ReturnSessionHome)PortableRemoteObject.narrow (ref, ReturnSessionHome.class);
        ReturnSession returnSession = returnHome.create();
            
        ref = jndiContext.lookup("fina2/returns/ReturnDefinition");
        ReturnDefinitionHome returnDefinitionHome = (ReturnDefinitionHome)PortableRemoteObject.narrow (ref, ReturnDefinitionHome.class);
            
        Collection v = returnSession.getUsedInReturns((ReturnPK)row.getPrimaryKey());
        getUR(v);
            
        Collection w = returnSession.getDependentReturns((ReturnPK)row.getPrimaryKey());
        getDR(w);
            
        String text = "";
        String msg = util.getString("fina2.returns.audit.text1");
        for(Iterator iter = v.iterator(); iter.hasNext();) {
            TableRow r = (TableRow)iter.next();
            Integer pki = new Integer(r.getValue(2));
            ReturnDefinitionPK pk = new ReturnDefinitionPK( pki.intValue() );
            ReturnDefinition rd = returnDefinitionHome.findByPrimaryKey(pk);
            int st = new Integer(r.getValue(1)).intValue();
            if( st==ReturnConstants.STATUS_ACCEPTED ) {
                text = text+"("+rd.getCode()+") "+rd.getDescription(userInfo.getLanguageHandle())+util.getString("fina2.returns.audit.text5")+"\""+util.getString(ReturnConstants.STATUS_ACCEPTED_STR)+"\"\n";
            }
        }
        if(!text.equals("")) {
//                util.showLongMessageBox(null, msg, text);
        } 
        else {
            text = "";
            msg = util.getString("fina2.returns.audit.text2")+util.getString(ReturnConstants.STATUS_ACCEPTED_STR)+", "+util.getString(ReturnConstants.STATUS_PROCESSED_STR);
            for(Iterator iter = w.iterator(); iter.hasNext();) {
                TableRow r = (TableRow)iter.next();
                Integer pki = new Integer(r.getValue(2));
                ReturnDefinitionPK pk = new ReturnDefinitionPK( pki.intValue() );
                ReturnDefinition rd = returnDefinitionHome.findByPrimaryKey(pk);
                int st = new Integer(r.getValue(1)).intValue();
                if( (st==ReturnConstants.STATUS_ACCEPTED) || (st==ReturnConstants.STATUS_PROCESSED) ) {
                } else {
                    text = text+"("+rd.getCode()+") "+rd.getDescription(userInfo.getLanguageHandle())+"\n";
                }
            }
            if(!text.equals("")) {
//                    util.showLongMessageBox(null, msg, text);
            } 
            else {
                t1 = System.currentTimeMillis();

                msg = processSession.process(userInfo.getUserHandle(), (ReturnPK)row.getPrimaryKey(), false);
                System.out.println("process message start.");
                System.out.println(msg);
                System.out.println("process message end.");
                t2 = System.currentTimeMillis();
                int version = Integer.valueOf(row.getValue(5)).intValue();
                row.setValue(5, String.valueOf(version+1));
                if(msg.equals("")) {
                    msg = util.getString("fina2.returns.processed")+"\n";
                    returnSession.changeReturnStatus(
                        userInfo.getUserHandle(),
                        userInfo.getLanguageHandle(),
                        (ReturnPK)row.getPrimaryKey(),
                        ReturnConstants.STATUS_PROCESSED, msg 
                        );
                    returnSession.toAuditLog("\""+util.getString("fina2.returns.processed")+"\","+"\""+row.getValue(0)+"\",\""+row.getValue(1)+"\",\""+row.getValue(2)+"\",\""+row.getValue(4)+"\""+",\"\",\"\",\"\",\"\",\"\",\"\"", userInfo.getUserHandle(), userInfo.getLanguageHandle());
                    row.setValue(6, util.getString(ReturnConstants.STATUS_PROCESSED_STR));
                } 
                else {
                    returnSession.changeReturnStatus(
                        userInfo.getUserHandle(),
                        userInfo.getLanguageHandle(),
                        (ReturnPK)row.getPrimaryKey(),
                        ReturnConstants.STATUS_ERRORS, msg 
                    );
                    returnSession.toAuditLog("\""+util.getString("fina2.returns.errors")+"\","+"\""+row.getValue(0)+"\",\""+row.getValue(1)+"\",\""+row.getValue(2)+"\",\""+row.getValue(4)+"\""+",\"\",\"\",\"\",\"\",\"\",\"\"", userInfo.getUserHandle(), userInfo.getLanguageHandle());
                    row.setValue(6, util.getString(ReturnConstants.STATUS_ERRORS_STR));
                }
                System.out.println(((double)t2-(double)t1)/1000.0);
            }
        }
    }
    public Collection getUR(Collection v) throws Exception{
        Vector tmp = new Vector();
        ReturnSession returnSession= null;
        try {
            Object ref = jndiContext.lookup("fina2/returns/ReturnSession");
            ReturnSessionHome returnSessionHome = (ReturnSessionHome)PortableRemoteObject.narrow (ref, ReturnSessionHome.class);
            returnSession= returnSessionHome.create();
            Collection vv = null;
            for(Iterator iter = v.iterator(); iter.hasNext();) {
                TableRow r = (TableRow)iter.next();
                vv = returnSession.getUsedInReturns((ReturnPK)r.getPrimaryKey());
                getUR(vv);
                if(vv!=null)
                    tmp.addAll(vv);
            }
            v.addAll(tmp);
            return v;
        }
        finally{
            if(returnSession!=null)
                returnSession.remove();
        }
    }
    
    public Collection getDR(Collection v) throws Exception{
        Vector tmp = new Vector();
        ReturnSession returnSession= null;
        try {
            Object ref = jndiContext.lookup("fina2/returns/ReturnSession");
            ReturnSessionHome returnSessionHome = (ReturnSessionHome)PortableRemoteObject.narrow (ref, ReturnSessionHome.class);
            returnSession= returnSessionHome.create();
            Collection vv = null;
            for(Iterator iter = v.iterator(); iter.hasNext();) {
                TableRow r = (TableRow)iter.next();
                vv = returnSession.getDependentReturns((ReturnPK)r.getPrimaryKey());
                getDR(vv);
                if(vv!=null)
                    tmp.addAll(vv);
                    
            }
            v.addAll(tmp);
            return v;
        }
        finally{
            if(returnSession!=null)
                returnSession.remove();
        }
    }
    public void delete() throws Exception{
        Object ref = jndiContext.lookup("fina2/returns/ReturnSession");
        ReturnSessionHome home = (ReturnSessionHome)PortableRemoteObject.narrow (ref, ReturnSessionHome.class);
        ReturnSession returnSession = home.create();
        returnSession.deleteReturn((ReturnPK)row.getPrimaryKey());
        returnSession.remove();
        return;
    }
    private void changeStatus(int status, String statusStr) throws Exception{
	Object ref = jndiContext.lookup("fina2/returns/ReturnSession");
	ReturnSessionHome returnSessionHome= (ReturnSessionHome)PortableRemoteObject.narrow (ref, ReturnSessionHome.class);

	ReturnSession returnSession= returnSessionHome.create();

	returnSession.changeReturnStatus(
	    userInfo.getUserHandle(),
	    userInfo.getLanguageHandle(),
	    (ReturnPK)row.getPrimaryKey(),
	    status, statusStr
	);
	row.setValue(6, statusStr);
    }
    public void reject() throws Exception{
	Object ref = jndiContext.lookup("fina2/returns/ReturnSession");
	ReturnSessionHome returnSessionHome = (ReturnSessionHome)PortableRemoteObject.narrow (ref, ReturnSessionHome.class);
	ReturnSession returnSession = returnSessionHome.create();

	ref = jndiContext.lookup("fina2/returns/ReturnDefinition");
	ReturnDefinitionHome returnDefinitionHome = (ReturnDefinitionHome)PortableRemoteObject.narrow (ref, ReturnDefinitionHome.class);

	Collection v = returnSession.getUsedInReturns((ReturnPK)row.getPrimaryKey());
	getUR(v);

	String text = "";
	String msg = util.getString("fina2.returns.audit.text1");
	for(Iterator iter = v.iterator(); iter.hasNext();) {
	    TableRow r = (TableRow)iter.next();
	    Integer pki = new Integer(r.getValue(2));
	    ReturnDefinitionPK pk = new ReturnDefinitionPK( pki.intValue() );
	    ReturnDefinition rd = returnDefinitionHome.findByPrimaryKey(pk);
	    int st = new Integer(r.getValue(1)).intValue();
	    if( st==ReturnConstants.STATUS_ACCEPTED ) {
		text = text+"("+rd.getCode()+") "+rd.getDescription(userInfo.getLanguageHandle())+util.getString("fina2.returns.audit.text5")+"\""+util.getString(ReturnConstants.STATUS_ACCEPTED_STR)+"\"\n";
	    }
	}
	if(!text.equals("")) {
/////	    util.showLongMessageBox(null, msg, text);
	} else {
	    text = "";
	    msg = util.getString("fina2.returns.audit.text3")+util.getString(ReturnConstants.STATUS_REJECTED_STR)+": ";
	    Vector rr = new Vector();
	    for(Iterator iter = v.iterator(); iter.hasNext();) {
		TableRow r = (TableRow)iter.next();
		Integer pki = new Integer(r.getValue(2));
		ReturnDefinitionPK pk = new ReturnDefinitionPK( pki.intValue() );
		ReturnDefinition rd = returnDefinitionHome.findByPrimaryKey(pk);
		int st = new Integer(r.getValue(1)).intValue();
		//if( st==ReturnConstants.STATUS_PROCESSED ) {
		    rr.add(new ReturnPK( new Integer(r.getValue(0)).intValue()));
		    if(!text.equals("")) text=text+"\n";
		    text = text+"("+rd.getCode()+") "+rd.getDescription(userInfo.getLanguageHandle());
		//}

	    }   
	    if(!text.equals("")) {
/////		fina2.util.LongConfirmBox dialog_yn = new fina2.util.LongConfirmBox(null);
////		if(dialog_yn.show(msg, text)) {
		changeStatus(ReturnConstants.STATUS_REJECTED, util.getString(ReturnConstants.STATUS_REJECTED_STR));

		for(Iterator it = rr.iterator(); it.hasNext();) {
		    ReturnPK rpk = (ReturnPK)it.next();
//		    TableRow ro = table.findRow(rpk);
		    returnSession.toAuditLog("\""+util.getString("fina2.returns.rejected")+"\","+"\"", 
/////			    ro.getValue(0)+"\",\""+ro.getValue(1)+"\",\""+
/////			    ro.getValue(2)+"\",\""+ro.getValue(4)+"\""+",\"\",\"\",\"\",\"\",\"\",\"\"", 
			    userInfo.getUserHandle(), userInfo.getLanguageHandle());

		    returnSession.changeReturnStatus(
			userInfo.getUserHandle(),
			userInfo.getLanguageHandle(),
			rpk,
			ReturnConstants.STATUS_REJECTED, util.getString("fina2.returns.audit.text4")+row.getValue(4)
			);
/////		    ro.setValue(6,util.getString(ReturnConstants.STATUS_REJECTED_STR));

		}
/////		}
	    }
	    else {
		returnSession.toAuditLog("\""+util.getString("fina2.returns.rejected")+"\","+"\""+row.getValue(0)+"\",\""+row.getValue(1)+"\",\""+row.getValue(2)+"\",\""+row.getValue(4)+"\""+",\"\",\"\",\"\",\"\",\"\",\"\"", userInfo.getUserHandle(), userInfo.getLanguageHandle());
		changeStatus(ReturnConstants.STATUS_REJECTED, util.getString(ReturnConstants.STATUS_REJECTED_STR));
	    }
	}
    }
    public void reset() throws Exception{
	Object ref = jndiContext.lookup("fina2/returns/ReturnSession");
	ReturnSessionHome returnSessionHome= (ReturnSessionHome)PortableRemoteObject.narrow (ref, ReturnSessionHome.class);
	ReturnSession returnSession= returnSessionHome.create();
            
	ref = jndiContext.lookup("fina2/returns/ReturnDefinition");
	ReturnDefinitionHome returnDefinitionHome= (ReturnDefinitionHome)PortableRemoteObject.narrow (ref, ReturnDefinitionHome.class);
            
	Collection v = returnSession.getUsedInReturns((ReturnPK)row.getPrimaryKey());
	getUR(v);
            
	String text = "";
	String msg = util.getString("fina2.returns.audit.text1");
	for(Iterator iter = v.iterator(); iter.hasNext();) {
	    TableRow r = (TableRow)iter.next();
	    Integer pki = new Integer(r.getValue(2));
	    ReturnDefinitionPK pk = new ReturnDefinitionPK( pki.intValue() );
	    ReturnDefinition rd = returnDefinitionHome.findByPrimaryKey(pk);
	    int st = new Integer(r.getValue(1)).intValue();
	    if( st==ReturnConstants.STATUS_ACCEPTED ) {
		text = text+"("+rd.getCode()+") "+rd.getDescription(userInfo.getLanguageHandle())+util.getString("fina2.returns.audit.text5")+"\""+util.getString(ReturnConstants.STATUS_ACCEPTED_STR)+"\"\n";
	    }
	}
	if(!text.equals("")) {
/////                util.showLongMessageBox(null, msg, text);
	} else {
	    text = "";
	    msg = util.getString("fina2.returns.audit.text3")+util.getString(ReturnConstants.STATUS_RESETED_STR)+": ";
	    Vector rr = new Vector();
	    for(Iterator iter = v.iterator(); iter.hasNext();) {
		TableRow r = (TableRow)iter.next();
		Integer pki = new Integer(r.getValue(2));
		ReturnDefinitionPK pk = new ReturnDefinitionPK( pki.intValue() );
		ReturnDefinition rd = returnDefinitionHome.findByPrimaryKey(pk);
		int st = new Integer(r.getValue(1)).intValue();
		if( st==ReturnConstants.STATUS_PROCESSED ) {
		    rr.add(new ReturnPK( new Integer(r.getValue(0)).intValue()));
		    if(!text.equals("")) text=text+"\n";
		    text = text+"("+rd.getCode()+") "+rd.getDescription(userInfo.getLanguageHandle());
		}

	    }   
	    if(!text.equals("")) {
////		fina2.util.LongConfirmBox dialog_yn = new fina2.util.LongConfirmBox(null);
/////                    if(dialog_yn.show(msg, text)) {
                
		changeStatus(ReturnConstants.STATUS_RESETED, util.getString(ReturnConstants.STATUS_RESETED_STR));
		for(Iterator it = rr.iterator(); it.hasNext();) {
		    ReturnPK rpk = (ReturnPK)it.next();
/////                        TableRow ro = table.findRow(rpk);
		    returnSession.toAuditLog("\""+util.getString("fina2.returns.reseted")+"\","+"\"", 
////				    ro.getValue(0)+"\",\""+
/////				    ro.getValue(1)+"\",\""+ 
/////				    ro.getValue(2)+"\",\""+
/////				    ro.getValue(4)+"\""+",\"\",\"\",\"\",\"\",\"\",\"\"", 
				userInfo.getUserHandle(), userInfo.getLanguageHandle());
/////		    ro.setValue(6,util.getString(ReturnConstants.STATUS_RESETED_STR));
		    returnSession.changeReturnStatus(
			userInfo.getUserHandle(),
			userInfo.getLanguageHandle(),
			rpk,
			ReturnConstants.STATUS_RESETED, util.getString("fina2.returns.audit.text4")+row.getValue(4)
			);
		}
/////                    }
	    }
	    else {
		returnSession.toAuditLog("\""+util.getString("fina2.returns.reseted")+"\","+"\""+row.getValue(0)+"\",\""+row.getValue(1)+"\",\""+row.getValue(2)+"\",\""+row.getValue(4)+"\""+",\"\",\"\",\"\",\"\",\"\",\"\"", userInfo.getUserHandle(), userInfo.getLanguageHandle());
		changeStatus(ReturnConstants.STATUS_RESETED, util.getString(ReturnConstants.STATUS_RESETED_STR));
	    }
	}
    }
    public void create(TableRow row) throws Exception {
	Object ref = jndiContext.lookup("fina2/returns/ReturnSession");
	ReturnSessionHome home = (ReturnSessionHome)PortableRemoteObject.narrow (ref, ReturnSessionHome.class);
	ReturnSession session = home.create();
	ReturnPK returnPK = null;
	try{
            returnPK = session.createReturn(
                userInfo.getUserHandle(),
                userInfo.getLanguageHandle(),
                (SchedulePK)row.getPrimaryKey()
            );
	}
	catch(Exception e){
	    throw e;
	}
	ref = jndiContext.lookup("fina2/returns/ProcessSession");
	ProcessSessionHome processSessionHome= (ProcessSessionHome)PortableRemoteObject.narrow (ref, ProcessSessionHome.class);
	ProcessSession processSession= processSessionHome.create();
	Vector codes = new Vector();
	Vector names = new Vector();
	codes = (Vector)processSession.canProcess(userInfo.getLanguageHandle(),(SchedulePK)row.getPrimaryKey(),codes, names);
	System.out.println("size="+codes.size());
	if(codes.size() > 0) {
	    String l = util.getString("fina2.returns.audit.text6")+"\n";
	    int i = codes.size()/2;

	    for(Iterator iter=codes.iterator(); iter.hasNext(); i++) {
		try {
		    String code = (String)iter.next();
		    String name = (String)codes.elementAt(i);
		    l += "    ["+code+"] "+name+"\n";
		    System.out.println("    ["+code+"] "+name);
		} catch(ArrayIndexOutOfBoundsException aex) {
		    break;
		}
	    }
	    session.deleteReturn(returnPK);
	    throw new Exception(util.getString("fina2.returns.cantCreate"));
	} 
	else {
	    session.toAuditLog("\""+util.getString("fina2.returns.created")+"\","+"\""+row.getValue(0)+"\",\""+row.getValue(1)+"\",\""+row.getValue(2)+"\",\""+row.getValue(4)+"\""+",\"\",\"\",\"\",\"\",\"\",\"\"", userInfo.getUserHandle(), userInfo.getLanguageHandle());
	}
 
    }
    public void amend(TableRow returnRow, ServletContext application, HttpSession session, HttpServletRequest request, HttpServletResponse  response) throws Exception{
        ReturnPK pk=(ReturnPK)returnRow.getPrimaryKey();
        prepareAmendData(returnRow, application, request, response);
        Vector oldData=(Vector)session.getAttribute("returns.returnmanager.amend.returndata"+pk.getId());
        if(oldData==null)
            throw new FinaException(util.getString("fina2.returns.noOldReturnData"));
//        Vector oldData =tbl.rows;
        Collection data=null; 
/////            NoteBox noteBox = new NoteBox((java.awt.Frame)this.getParent());
        boolean changed = false;
/////            int index = table.getSelectedRow();
            
        String nformat = userInfo.getLanguage().getNumberFormat();
        java.text.DecimalFormat nf = new java.text.DecimalFormat(nformat);
            
        Object ref = jndiContext.lookup("fina2/returns/ReturnSession");
        ReturnSessionHome home = (ReturnSessionHome)PortableRemoteObject.narrow (ref, ReturnSessionHome.class);
            
        ReturnSession returnSession = home.create();

        int ii=0;
        Vector rows_ = new Vector();

        ReturnSessionHome definitionHome = (ReturnSessionHome)PortableRemoteObject.narrow (ref, ReturnSessionHome.class);
        ReturnSession definitionSession = definitionHome.create();
        Collection defTables = definitionSession.getReturnTables(userInfo.getLanguageHandle(), pk);
        for(Iterator iter=defTables.iterator(); iter.hasNext(); ii++) {
            DefinitionTable table = (DefinitionTable)iter.next();
//                Table tabl = (Table)t.get(ii);
//                Table tabl_ = (Table)t_.get(ii);
            Table tbl=(Table)oldData.get(ii);
            removeDeleted(tbl.rows, tbl.nodePK.getId(), session, request);
            insertRows(tbl.rows, tbl.nodePK.getId(), session, request);
            Collection rows = tbl.rows;
//            Collection rows = tabl.rows;
            Vector rr = new Vector();
//            int r = tabl.start_row;
            int r_ = 0;
            int rowNumber = 0;
            for(Iterator it = rows.iterator(); it.hasNext();) {
                ValuesTableRow row = (ValuesTableRow)it.next(); 
                r_++;
                for(int c = 0; c<row.getColumnCount(); c++ ) {
                    if(row.getType(c)==MDTConstants.NODETYPE_VARIABLE) {
                        Node node = new Node(new Integer(row.getNodeID(c)),"", new Integer(row.getDataType(c)));
                        node.putProperty("value", "");
                        node.putProperty("row", new Integer(rowNumber));
                        rr.add(node);
                                
                        row.setValue(c, "");
                    }
                    if(row.getType(c)==MDTConstants.NODETYPE_INPUT) {
                        if(row.getDataType(c)==MDTConstants.DATATYPE_TEXT) {
                            String value = row.getValue(c).trim();
                            
                            String paramName=""+((MDTNodePK)table.getNode()).getId()+"_"+rowNumber+"_"+c;
                            String value_ = request.getParameter(paramName);
                            if(value_==null)
                                value_ =value;
                            ///////sheet.getCellValue(r, c+1).trim();
                            if(!value.equals(value_) && value_!=null) {
                                System.out.println("Old "+value+"      New "+value_);
                                Node node = new Node(new Integer(row.getNodeID(c)),"", new Integer(row.getDataType(c)));
                                node.putProperty("value", value_);
                                node.putProperty("row", new Integer(rowNumber));
                                rr.add(node);
                                
                                row.setValue(c, value_);
                            } else {
                                if(table.getType() == ReturnConstants.TABLETYPE_VARIABLE && value_!=null) {
                                    Node node = new Node(new Integer(row.getNodeID(c)),"", new Integer(row.getDataType(c)));
                                    node.putProperty("value", value_);
                                    node.putProperty("row", new Integer(rowNumber));
                                    rr.add(node);
                                }
                                
                            }
                        }
                        if(row.getDataType(c)==MDTConstants.DATATYPE_NUMERIC) {
                            double value = 0;
                            try {
                                value = (new Double(row.getValue(c))).doubleValue();
                            } catch(java.lang.NumberFormatException e) {
                                value = 0;
                            }
                            int n=((MDTNodePK)table.getNode()).getId();
                            String name=""+((MDTNodePK)table.getNode()).getId()+"_"+rowNumber+"_"+c;
                            String val=request.getParameter(""+((MDTNodePK)table.getNode()).getId()+"_"+rowNumber+"_"+c);
                            double value_ = value;
                            try {
                                value_=Double.parseDouble(request.getParameter(""+((MDTNodePK)table.getNode()).getId()+"_"+rowNumber+"_"+c));
                            } catch(java.lang.NumberFormatException e) {
                                value_ = value;
                            }
                            ///////sheet.getCellNumber(r, c+1);
                            if(value!=value_) {
                                System.out.println("Old "+value+"      New "+value_);
                                Node node = new Node(new Integer(row.getNodeID(c)),"", new Integer(row.getDataType(c)));
                                node.putProperty("value", new Double(value_).toString());
                                node.putProperty("row", new Integer(rowNumber));
                                rr.add(node);
                                
                                row.setValue(c, new Double(value_).toString());
                            }//if(value!=value_)
                            else {
                                if(table.getType() == ReturnConstants.TABLETYPE_VARIABLE) {
                                    Node node = new Node(new Integer(row.getNodeID(c)),"", new Integer(row.getDataType(c)));
                                    node.putProperty("value", new Double(value_).toString());
                                    node.putProperty("row", new Integer(rowNumber));
                                    rr.add(node);
                                }
                            }//if(value!=value_)
                        }//if(row.getDataType(c)==MDTConstants.DATATYPE_NUMERIC)
                    }//if(row.getType(c)==MDTConstants.NODETYPE_INPUT) 
                }//for(int c = 0; c<row.getColumnCount(); c++ ) 
///////                r++;
                rowNumber++;
                rows_.add(row);
            }//for(Iterator it = rows.iterator(); it.hasNext();)
            definitionSession.setTableValuesRows(
                    userInfo.getLanguageHandle(),pk,
                    table.getNode(),rr);
        }//for(Iterator iter=defTables.iterator(); iter.hasNext(); ii++) 
 
/////        application.getRequestDispatcher("/jsp/returns/notepage.jsp?status="+util.getString(ReturnConstants.STATUS_AMENDED_STR)).forward(request, response);
        
///        noteBox.show(util.getString(ReturnConstants.STATUS_AMENDED_STR));
        definitionSession.changeReturnStatus(
            userInfo.getUserHandle(),
            userInfo.getLanguageHandle(),
            pk,
            ReturnConstants.STATUS_AMENDED, util.getString(ReturnConstants.STATUS_AMENDED_STR)     
        );
        if(!changed) {
            definitionSession.toAuditLog("\""+util.getString("fina2.returns.amended")+"\","+"\""+row.getValue(0)+"\",\""+row.getValue(1)+"\",\""+row.getValue(2)+"\",\""+row.getValue(4)+"\""+",\"\",\"\",\"\",\"\",\"\",\"\"", userInfo.getUserHandle(), userInfo.getLanguageHandle());
        }
        definitionSession.setVersion(pk, definitionSession.getVersion(pk)+1);
    }
    public void prepareAmendData(TableRow row, ServletContext application, HttpServletRequest request, HttpServletResponse  response) throws Exception{
        Object ref = jndiContext.lookup("fina2/returns/ReturnSession");
        ReturnSessionHome returnHome = (ReturnSessionHome)PortableRemoteObject.narrow (ref, ReturnSessionHome.class);
        ReturnSession returnSession = returnHome .create();
        Collection v = returnSession.getUsedInReturns((ReturnPK)row.getPrimaryKey());
        getUR(v);
        ref = jndiContext.lookup("fina2/returns/ReturnDefinition");
        ReturnDefinitionHome definitionHome = (ReturnDefinitionHome)PortableRemoteObject.narrow (ref, ReturnDefinitionHome.class);
        String text = "";
        String msg = util.getString("fina2.returns.audit.text1");
        for(Iterator iter = v.iterator(); iter.hasNext();) {
            TableRow r = (TableRow)iter.next();
            Integer pki = new Integer(r.getValue(2));
            ReturnDefinitionPK pk = new ReturnDefinitionPK( pki.intValue() );
            ReturnDefinition rd = definitionHome.findByPrimaryKey(pk);
            int st = new Integer(r.getValue(1)).intValue();
            if( st==ReturnConstants.STATUS_ACCEPTED ) {
                text = text+"("+rd.getCode()+") "+rd.getDescription(userInfo.getLanguageHandle())+util.getString("fina2.returns.audit.text5")+"\""+util.getString(ReturnConstants.STATUS_ACCEPTED_STR)+"\"\n";
            }
        }
        if(!text.equals("")) {
            throw new Exception(text);
        } //if(!text.equals("")) 
        else {
            text = "";
            msg = util.getString("fina2.returns.audit.text3")+util.getString(ReturnConstants.STATUS_AMENDED_STR)+": ";
            Vector rr = new Vector();
            for(Iterator iter = v.iterator(); iter.hasNext();) {
                TableRow r = (TableRow)iter.next();
                Integer pki = new Integer(r.getValue(2));
                ReturnDefinitionPK pk = new ReturnDefinitionPK( pki.intValue() );
                ReturnDefinition rd = definitionHome.findByPrimaryKey(pk);
                int st = new Integer(r.getValue(1)).intValue();
                if( st==ReturnConstants.STATUS_PROCESSED ) {
                    rr.add(new ReturnPK( new Integer(r.getValue(0)).intValue()));
                    if(!text.equals("")) text=text+"\n";
                    text = text+"("+rd.getCode()+") "+rd.getDescription(userInfo.getLanguageHandle());
                }
            }   

            if(!text.equals("")) {

//                fina2.ui.LongConfirmBox dialog_yn = new fina2.ui.LongConfirmBox(null);
                application.getRequestDispatcher("/jsp/returns/dialog_yn.jsp?msg="+response.encodeURL(msg)+"&text="+response.encodeURL(text)).forward(request, response);
//                if(dialog_yn.show(msg, text))
//                    amendFrame.show((ReturnPK)row.getPrimaryKey(), table, row);
            } //if(!text.equals("")) 
        }//if(!text.equals("")) 
    }
    public void removeDeleted(Vector rows, int nodeId, HttpSession session, HttpServletRequest request){
        java.util.Map map=request.getParameterMap();
        java.util.Set set=map.keySet();
        java.util.Iterator it=null;
        int size=rows.size();
        for(int i=0;i<size;){
            ValuesTableRow row=(ValuesTableRow)rows.get(i);
            it=set.iterator();
            while(it.hasNext()){
                String key=it.next().toString();
                if(key.toLowerCase().startsWith("deleted_"+nodeId+"_"+row.getId())){
                    rows.remove(row);
                    break;
                }
            }
            if(rows.contains(row))
                i++;
            else
                size--;
        }
    }
    public int getMaxId(Vector rows){
        int size=rows.size();
        int maxId=0;
        for(int i=0;i<size;i++){
            ValuesTableRow row=(ValuesTableRow)rows.get(i);
            maxId=Math.max(maxId, row.getId());
        }
        return ++maxId;
    }
    public Vector getValues(java.util.TreeMap treeMap,String key){
        key=key.substring(0, key.lastIndexOf('_'));
        Vector values=new Vector();
        java.util.Set set=treeMap.keySet();
        java.util.Iterator it=null;
        it=set.iterator();
        while(it.hasNext()){
            String sameRowKey=it.next().toString();
            if(sameRowKey.toLowerCase().startsWith(key)){
                String[] val=(String[])treeMap.get(sameRowKey);
                values.add(val[0]);
            }
        }
        return values;
    }
    public Table getTable(Vector tables, int tableNodeId){
        for(int i=0;i<tables.size();i++){
            Table table=(Table)tables.get(i);
            if(table.nodePK.getId()==tableNodeId)
                return table;
        }
        return null;
    }
    public void insertRows(Vector rows, int tableNodeId, HttpSession session, HttpServletRequest request){
        Vector tables=(Vector)session.getAttribute("returns.returnmanager.amend.returndata"+((ReturnPK)row.getPrimaryKey()).getId());
        if(tables==null)
            return;
        java.util.Map map=request.getParameterMap();
        java.util.TreeMap treeMap=new java.util.TreeMap(map);
        java.util.Set set=treeMap.keySet();
        java.util.Iterator it=null;
        Table table=getTable(tables, tableNodeId);
        int size=table.rows.size();
        int maxId=getMaxId(table.rows);
        it=set.iterator();
        while(it.hasNext()){
            String key=it.next().toString();
            if(key.toLowerCase().startsWith("inserted_"+tableNodeId)){
                int startPos=key.indexOf("_", "inserted_".length())+1;
                int endPos=key.indexOf("_", startPos);
                int rowIndex=Integer.parseInt(key.substring(startPos, endPos));
                insertRow(table, rowIndex, tableNodeId);
                break;
            }
        }
    }
    public void insertRow(Table table, int tableID, int rowIndex) {
        if(table.type == ReturnConstants.TABLETYPE_VARIABLE && table.nodePK.getId()==tableID){ 
            Vector v = new Vector(table.rows);
            ValuesTableRow row = (ValuesTableRow)v.get(0);
            v.insertElementAt(row, rowIndex);
            table.rows = v;
            return;
        }
    }
    public void deleteRow(int tableID, int rowIndex, HttpSession session) throws Exception{
        Vector tables=(Vector)session.getAttribute("returns.returnmanager.amend.returndata"+((ReturnPK)row.getPrimaryKey()).getId());
        if(tables==null)
            return;
        for(int i=0; i<tables.size();i++) {
            Table table = (Table)tables.get(i);
            if(table.type == ReturnConstants.TABLETYPE_VARIABLE && table.nodePK.getId()==tableID){ 
                table.rows.remove(rowIndex);
                return;
            }
        }
    }
}
