/*
 * ReturnTypeHome.java
 *
 * Created on October 31, 2001, 11:59 AM
 */

package fina2.returns;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public interface ReturnTypeHome extends EJBHome {

    ReturnType create() throws EJBException, CreateException, RemoteException;

    ReturnType findByPrimaryKey(ReturnTypePK pk) throws EJBException,
            FinderException, RemoteException;

}
