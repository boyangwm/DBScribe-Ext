/*
 * ReportHome.java
 *
 * Created on January 7, 2002, 3:33 PM
 */

package fina2.reportoo.server;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;
import javax.ejb.Handle;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public interface ReportHome extends EJBHome {

    Report create(Handle userHandle, ReportPK parentPK) throws EJBException,
            CreateException, RemoteException;

    Report findByPrimaryKey(ReportPK param0) throws EJBException,
            FinderException, RemoteException;

}
