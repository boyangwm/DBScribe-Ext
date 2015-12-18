/*
 * BranchManagHome.java
 *
 * Created on April 23, 2002, 7:59 PM
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
public interface BranchManagHome extends EJBHome {

    BranchManag create() throws EJBException, CreateException, RemoteException;

    BranchManag findByPrimaryKey(BranchManagPK pk) throws EJBException,
            FinderException, RemoteException;

}
