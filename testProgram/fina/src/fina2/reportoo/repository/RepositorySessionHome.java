/*
 * RepositorySessionHome.java
 *
 * Created on 10 Сентябрь 2002 г., 0:10
 */

package fina2.reportoo.repository;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;

/**
 *
 * @author  Shota Shalamberidze
 * @version 
 */
public interface RepositorySessionHome extends EJBHome {

    RepositorySession create() throws CreateException, EJBException,
            RemoteException;

}
