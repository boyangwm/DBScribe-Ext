/*
 * Parser.java
 *
 * Created on November 21, 2003, 6:59 AM
 */

package fina2.web;

import fina2.javascript.Parser;
import fina2.javascript.Tokenizer;
import fina2.javascript.element.Element;
import fina2.javascript.element.Scope;
import fina2.metadata.MDTNode;
import fina2.metadata.MDTNodeHome;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import javax.ejb.FinderException;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author  Administrator
 */
public class WizardParser
{
    private Scope tree;
    private String error;
    private Parser p;
    private String source;
    /** Creates a new instance of Parser */
    
    public WizardParser(String source, FunctionList fl)
    {
        p=new Parser(source, fl);
        this.source=source;
    }
    
    public void parse()
    {
        p.parse();
    }
    
    public String getError()
    {
        return error;
    }
    
    public void checkSyntax()
    {
        parse();
        Thread t = new Thread()
        {
            public void run()
            {
                try
                {
                    Collection c = findAllScopes();
                    
                    for(Iterator iter=c.iterator(); iter.hasNext(); )
                    {
                        Scope s = (Scope)iter.next();
                        fina2.script.Engine.evalNodeInterceptor(
                        s.print(0, false) //source.getText()
                        );
                    }
                    error="";
                } catch(Exception e)
                {
                    error=e.getMessage();
                }
            }
        };
        t.start();
        try
        {
            long t1 = System.currentTimeMillis();
            t.join(4000);
            long t2 = System.currentTimeMillis();
            if( (t2 - t1) >= 4000 )
            {
                error="unterminated loop";
                
                t.stop();
            }
            t = null;
            //System.gc();
        } catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public Collection findAllScopes()
    {
        Vector v = new Vector();
        try
        {
            DefaultMutableTreeNode root = (DefaultMutableTreeNode)p.getTreeModel().getRoot();
            Element rootScope = (Element)root.getUserObject();
            v.add(rootScope);
            findScopes(rootScope, v);
        } catch(Exception e)
        {
            e.printStackTrace();
        }
        return v;
    }
    
    public void findScopes(Element element, Collection v) throws Exception
    {
        for(Iterator iter=element.children(); iter.hasNext(); )
        {
            Element e = ((Element)iter.next()).getValidElement();
            if(e instanceof Scope)
                v.add(e);
            findScopes(e, v);
        }
    }
    
    public Collection getRefNodes(InitialContext jndi)
    {
        Collection refNodes=null;
        Collection  codes = null;
        try
        {
            Collection c = findAllScopes();
            for(Iterator iter=c.iterator(); iter.hasNext(); )
            {
                Scope s = (Scope)iter.next();
                codes = (Collection)((Vector)fina2.script.Engine.evalNodeInterceptor(
                s.print(0, false) //source.getText()
                )).clone();
            }
        } catch(Exception e)
        {
            refNodes = null;
        }
        try
        {
            Object ref = jndi.lookup("fina2/metadata/MDTNode");
            MDTNodeHome home = (MDTNodeHome)PortableRemoteObject.narrow(ref, MDTNodeHome.class);
            refNodes = new Vector();
            String c = "";
            try
            {
                for(Iterator iter=codes.iterator(); iter.hasNext(); )
                {
                    c = (String)iter.next();
                    MDTNode node = home.findByCode(c);
                    refNodes.add(node.getPrimaryKey());
                }
            } catch(FinderException e)
            {
                error="fina2.metadata.codeNotFound";
                return null;
            }
            /*ref = jndi.lookup("fina2/metadata/MDTSession");
            MDTSessionHome sessionHome = (MDTSessionHome)PortableRemoteObject.narrow (ref, MDTSessionHome.class);
             
            MDTSession session = sessionHome.create();*/
            
        } catch(Exception e)
        {
            error=e.toString();
            e.printStackTrace();
        }
        return refNodes;
    }
    
    public String getSource()
    {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)p.getTreeModel().getRoot();
        return ((Element)root.getUserObject()).print(0, false);
    }
}
