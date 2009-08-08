package polyglot.ext.esj.visit;

import polyglot.visit.*;
import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.ext.esj.ast.*;
import polyglot.types.*;
import polyglot.ext.esj.types.*;
import polyglot.ext.jl5.types.*;
import polyglot.util.*;

import polyglot.frontend.Job;

//import polyglot.ext.esj.util.toJavaExpr;

import java.util.*;

/** Visitor that translates ESJ AST nodes to Java AST nodes, for
    subsequent output. **/
public class ESJTranslator extends ContextVisitor {

	// String for mangling the name of each dispatchee method
    protected static String dispatcheeStr = "$body";

	// String for making the formal names of the dispatcher method
    public static String argStr = "arg$";
    
    public ESJTranslator(Job job, TypeSystem ts, NodeFactory nf) {
	super(job, ts, nf);
	//System.out.println("init Translating...");
    }

    public Expr DesugarQuantifyExpr (ESJQuantifyExpr a)  {

	//System.out.println("desuaring begin...");
	boolean quantKind = a.quantKind();
	String quantId = a.id();
	String quantVarN = a.quantVar();
	Expr quantList = a.quantListExpr();
	ESJQuantifyClauseExpr quantExpr = a.quantClauseExpr();
	return nf.Call(null,null,quantId, new TypedList(new LinkedList(), Expr.class, false));
    }

    public Expr DesugarQuantifyTypeExpr (ESJQuantifyTypeExpr a)  {
	return nf.Call(null, a, "allInstances",new TypedList(new LinkedList(), Expr.class, false));
    }

    protected Node clearPureFlag(MethodDecl md) {
      return md.flags(md.flags().clear(((ESJTypeSystem)typeSystem()).Pure()));
    }

    protected Node leaveCall(Node n) throws SemanticException {
	if (n instanceof ESJQuantifyExpr) {
	    return super.leaveCall(DesugarQuantifyExpr((ESJQuantifyExpr)n));
	} else if (n instanceof ESJQuantifyTypeExpr) {
	    return super.leaveCall(DesugarQuantifyTypeExpr((ESJQuantifyTypeExpr)n));
	} else
	    return super.leaveCall(n);
    }

}

