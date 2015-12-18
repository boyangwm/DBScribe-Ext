/*
 * ProcessSessionHome.java
 *
 * Created on November 19, 2001, 2:16 PM
 */

package fina2.returns;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;

public interface ProcessSessionHome extends EJBHome {

    ProcessSession create() throws CreateException, EJBException,
            RemoteException;

}
