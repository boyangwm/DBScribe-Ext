/*
 * MDTSessionHome.java
 *
 * Created on October 19, 2001, 12:38 PM
 */

package fina2.metadata;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;

public interface MDTSessionHome extends EJBHome {

    MDTSession create() throws CreateException, EJBException, RemoteException;

}
