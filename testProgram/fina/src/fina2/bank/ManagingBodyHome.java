/*
 * BranchHome.java
 *
 * Created on March 17, 2002, 3:18 AM
 */

package fina2.bank;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

/**
 *
 * @author  vasop
 * @version 
 */
public interface ManagingBodyHome extends EJBHome {

    ManagingBody create() throws EJBException, CreateException, RemoteException;

    ManagingBody findByPrimaryKey(ManagingBodyPK pk) throws EJBException,
            FinderException, RemoteException;

}
