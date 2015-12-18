package fina2.metadata;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;
import javax.ejb.Handle;

public interface MDTNodeHome extends EJBHome {

    MDTNode create(MDTNodePK parentPK) throws EJBException, CreateException,
            RemoteException;

    MDTNode findByPrimaryKey(MDTNodePK param0) throws EJBException,
            FinderException, RemoteException;

    MDTNode findByCode(String code) throws EJBException, FinderException,
            RemoteException;

    MDTNode findByCodeExact(String code) throws EJBException, FinderException,
            RemoteException;

    MDTNode findByDescription(Handle languageHandle, String description)
            throws EJBException, FinderException, RemoteException;

    Collection findByParent(MDTNodePK param0) throws EJBException,
            FinderException, RemoteException;

    Collection findDependentNodes(MDTNodePK pk) throws EJBException,
            RemoteException, FinderException;

}
