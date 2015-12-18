/*
 * Parser.java
 *
 * Created on November 21, 2003, 6:59 AM
 */

package fina2.web;

import fina2.javascript.Tokenizer;
import fina2.javascript.element.Element;
import fina2.javascript.element.Scope;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author  Administrator
 */
public class Parser
{
    private String source;
    private Scope tree;
    private static FunctionList fl;
    private String error;
    /** Creates a new instance of Parser */
    
    public Parser(FunctionList fl)
    {
        this("",fl);
    }
    
    public Parser(String source, FunctionList fl)
    {
        this.source = source;
        this.fl = fl;
    }
    
    public static String findDummy(fina2.javascript.element.Function fun)
    {
        try
        {
            String s = fl.findFunction(fun).getDummy();
            if(!s.equals(""))
                return s;
        } catch(Exception e)
        {
        }
        return fun.getName();
    }
    
    public static String findNonDummy(fina2.javascript.element.Function fun)
    {
        try
        {
            String s = fl.findFunction(fun).getName();
            if(!s.equals(""))
                return s;
        } catch(Exception e)
        {
        }
        return fun.getName();
    }
    
    public void parse(String source)
    {
        this.source = source;
        parse();
    }
    
    public void parse()
    {
        tree = new Scope();
        Tokenizer tokenizer = new Tokenizer(source);
        try
        {
            //Token token = tokenizer.nextToken();
            tree.parse(tokenizer, null);
        } catch(Exception e)
        {
            //System.out.println("EOF");
        }
    }
    
    public String print()
    {
        return tree.print(0, false);
    }
    
    public DefaultTreeModel getTreeModel()
    {
        DefaultTreeModel model = new DefaultTreeModel(
        tree.getNode()
        );
        return model;
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
    
    public Collection findAllScopes() {
        Vector v = new Vector();
        try {
            DefaultMutableTreeNode root = (DefaultMutableTreeNode)getTreeModel().getRoot();
            Element rootScope = (Element)root.getUserObject();
            v.add(rootScope);
	    correctScopes(rootScope);
            findScopes(rootScope, v);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return v;
    }
    public void correctScopes(Element element){
	System.out.println("entered");
        for(Iterator iter=element.children(); iter.hasNext(); ) {
            Element e = ((Element)iter.next()).getValidElement();
            if(e instanceof Scope){
		correctScopes(e);
	    }
        }
    }
    public void findScopes(Element element, Collection v) throws Exception {
        for(Iterator iter=element.children(); iter.hasNext(); ) {
            Element e = ((Element)iter.next()).getValidElement();
            if(e instanceof Scope){
               v.add(e);
	    }
            findScopes(e, v);
        }
    }
}
