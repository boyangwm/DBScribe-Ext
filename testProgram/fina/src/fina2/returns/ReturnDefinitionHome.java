/*
 * ReturnDefinitionHome.java
 *
 * Created on October 31, 2001, 11:26 AM
 */

package fina2.returns;

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
public interface ReturnDefinitionHome extends EJBHome {

    ReturnDefinition create(Handle userHandle) throws EJBException,
            CreateException, RemoteException;

    ReturnDefinition findByPrimaryKey(ReturnDefinitionPK param0)
            throws EJBException, FinderException, RemoteException;

}
