package polyglot.ext.esj.visit;

import polyglot.visit.*;
import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.ext.jl5.ast.*;
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

    /*
    public JL5MethodDecl DesugarEnsuredMethodDecl (ESJEnsuredMethodDecl methodDecl)  {
	System.out.println(methodDecl.body().statements());
	System.out.println(methodDecl.ensuresExpr());
	List extraMtdBody = new TypedList(new LinkedList(), Stmt.class, false);
	extraMtdBody.addAll(methodDecl.body().statements());
	extraMtdBody.add(((ESJNodeFactory)nf).JL5Assert(null, methodDecl.ensuresExpr(), null));

	System.out.println(methodDecl.catchFormal());

	List catches = new TypedList(new LinkedList(), Catch.class, false);
	Block extraMtdBlock = nf.Block(null, extraMtdBody);
	List catchBody = new TypedList(new LinkedList(), Stmt.class, false);
	catchBody.add(nf.Return(null, nf.Call(null, null, "fallback",
					      new TypedList(new LinkedList(), Expr.class, false))));
	Block catchBlock = nf.Block(null,catchBody);
	catches.add(((ESJNodeFactory)nf).JL5Catch(null, methodDecl.catchFormal(), catchBlock));
	List tryBody = new TypedList(new LinkedList(), Stmt.class, false);
	tryBody.add(nf.Try(null, extraMtdBlock, catches));
	Block tryCatchBlock = nf.Block(null, tryBody);
	methodDecl = (ESJEnsuredMethodDecl) methodDecl.body(tryCatchBlock);
	System.out.println(methodDecl.body());
	return (JL5MethodDecl) methodDecl;
    }
    */

    // quantify expr method desugars into a foreach stmt
    /*
    public JL5MethodDecl DesugarPredMethodDecl (ESJPredMethodDecl methodDecl)  {
	System.out.println(methodDecl.body().statements());
	String quantMtdId = methodDecl.id();	  
	boolean quantKind = methodDecl.quantKind();
	String quantVarN = methodDecl.quantVar();
	List quantVarD = methodDecl.quantVarD();
	Expr quantList = methodDecl.quantListExpr();
	Expr quantExpr = methodDecl.quantClauseExpr();

	System.out.println(quantMtdId);
	System.out.println(quantKind);
	System.out.println(quantVarN);
	System.out.println(quantList);
	System.out.println(quantList.type());
	System.out.println(quantExpr);
	System.out.println(quantVarD);
	System.out.println(((LocalDecl)(quantVarD.get(0))).name());
	List extraMtdBody = new TypedList(new LinkedList(), Stmt.class, false);
	List quantClauseStmts = new TypedList(new LinkedList(), Stmt.class, false);
	Expr quantMainIfExpr = quantKind ? nf.Unary(null, Unary.NOT, quantExpr) : quantExpr;
	Stmt quantMainStmt = ((ESJNodeFactory)nf).JL5If(null, quantMainIfExpr, 
				      ((ESJNodeFactory)nf).JL5Return(null, nf.BooleanLit(null, !quantKind)), null);
	quantClauseStmts.add(quantMainStmt);	    
	Stmt forLoopBody = nf.Block(null, quantClauseStmts);
	Stmt forLoop = ((ESJNodeFactory)nf).ExtendedFor(null,quantVarD, quantList, quantMainStmt);
	//extraMtdBody.add(forLoop);
	extraMtdBody.addAll(quantVarD);
	extraMtdBody.add(((ESJNodeFactory)nf).JL5Return(null, nf.BooleanLit(null, quantKind)));
	System.out.println(extraMtdBody);
	System.out.println(methodDecl.name());
	System.out.println(methodDecl.formals());
	Block extraMtdBlock = nf.Block(null, extraMtdBody);
	methodDecl = (ESJPredMethodDecl) methodDecl.body(extraMtdBlock);
	System.out.println(methodDecl.body());
	return (JL5MethodDecl) methodDecl;

	//return ((ESJNodeFactory)nf).JL5MethodDecl(null, fl, nf.CanonicalTypeNode(null,ts.Boolean()), methodDecl.name(), methodDecl.formals(), new TypedList(new LinkedList(), TypeNode.class, false), extraMtdBlock,new TypedList(new LinkedList(), TypeNode.class, false));
    }
*/

    // quantify expr desugars to a method call (defined above)
    public Expr DesugarQuantifyExpr (ESJQuantifyExpr a)  {
	boolean quantKind = a.quantKind();
	String quantId = a.parentMethod().name()  + "_" + a.id();
	String quantVarN = a.quantVar();
	Expr quantList = a.quantListExpr();
	ESJQuantifyClauseExpr quantExpr = a.quantClauseExpr();
	List args = new TypedList(new LinkedList(), Expr.class, false);
	for(Formal f : (List<Formal>)(a.parentMethod().formals())) {
	    args.add(new Local_c(null,f.name()));
	}
	return nf.Call(null,null,quantId, args);
    }

    public Expr DesugarQuantifyTypeExpr (ESJQuantifyTypeExpr a)  {
	return nf.Call(null, a.theType(), "allInstances",new TypedList(new LinkedList(), Expr.class, false));
    }

    protected Node clearPureFlag(MethodDecl md) {
      return md.flags(md.flags().clear(((ESJTypeSystem)typeSystem()).Pure()));
    }

    protected Node leaveCall(Node n) throws SemanticException {
	/*	if (n instanceof ESJPredMethodDecl) {
	    System.out.println("t1: " + n);
	    return super.leaveCall(DesugarPredMethodDecl((ESJPredMethodDecl)n));
	    } else */
	/*
	if (n instanceof ESJEnsuredMethodDecl) {
	    System.out.println("yep:" + n);
	    return super.leaveCall(DesugarEnsuredMethodDecl((ESJEnsuredMethodDecl)n));
	} else */ 
	if (n instanceof ESJQuantifyExpr) {
	    return super.leaveCall(DesugarQuantifyExpr((ESJQuantifyExpr)n));
	} else if (n instanceof ESJQuantifyTypeExpr) {
	    return super.leaveCall(DesugarQuantifyTypeExpr((ESJQuantifyTypeExpr)n));
	} else { 	
	    return super.leaveCall(n);
	}
    }


    /*
    public TypeNode array (TypeNode n, int dims) throws Exception
  {
    if (dims > 0)
      {
	if (n instanceof CanonicalTypeNode)
	  {
	    Type t = ((CanonicalTypeNode) n).type ();
	      return nf.CanonicalTypeNode (null, ts.arrayOf (t, dims));
	  }
	return nf.ArrayTypeNode (null, array (n, dims - 1));
      }
    else
      {
	return n;
      }
  }
*/

}



