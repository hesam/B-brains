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
	System.out.println("init Translating...");
    }

    public Expr DesugarQuantifyExpr (ESJQuantifyExpr a) {

	System.out.println("desuaring begin...");
    	      int quantKind = a.quantKind();
	      String quantVarN = a.quantVar();
	      Expr quantList = a.quantListExpr();
	      ESJQuantifyClauseExpr quantExpr = a.quantClauseExpr();
	      return nf.BooleanLit(null, false);
	      /*
	      List l = new TypedList(new LinkedList(), Stmt.class, false);
	      
	      FlagAnnotations fl = new FlagAnnotations(); 
              fl.classicFlags(Flags.NONE);
              fl.annotations(new TypedList(new LinkedList(), AnnotationElem.class, false));
	      VarDeclarator quantListVar = new VarDeclarator(null, "quantList");
	      Expr quantListVarExpr = new JL5Name(this, null, "quantList").toExpr();
	      quantListVar.init = quantList;   
	      List arraylistSubTp = new TypedList(new LinkedList(), TypeNode.class, false);
	      TypeNode arraylistTp = new JL5Name(this, null, "ArrayList").toType();
	      arraylistSubTp.add(new JL5Name(this, null, "Integer").toType());
              List quantListVarD = this.variableDeclarators(nf.JL5AmbTypeNode(null, ((AmbTypeNode)arraylistTp).qual(), ((AmbTypeNode)arraylistTp).name(), arraylistSubTp), quantListVar, fl); 
	      Stmt forLoop;
	      VarDeclarator itrForLoopVar =  new VarDeclarator(null, "i");
	      Expr itrForLoopVarExpr = new JL5Name(this, null, "i").toExpr();

	      itrForLoopVar.init = nf.IntLit(null, IntLit.INT, 0);
              List itrForLoopVarD = this.variableDeclarators(nf.CanonicalTypeNode(null, this.ts.Int()), itrForLoopVar, fl); 

              List forLoopInit = new TypedList(new LinkedList(), ForInit.class, false);
              forLoopInit.addAll(itrForLoopVarD);


	      Expr forLoopCond = nf.Binary(this.pos(a, a), 
	       	    		                   itrForLoopVarExpr, 
						   Binary.LT, 
						   nf.JL5Call(null, quantListVarExpr,
                "size", new TypedList(new LinkedList(), Expr.class, false), new TypedList(new LinkedList(), TypeNode.class, false)));

	      List forLoopUpd = new TypedList(new LinkedList(), Eval.class, false);
              forLoopUpd.add(nf.Eval(null, nf.Unary(null, Unary.POST_INC, itrForLoopVarExpr)));
	      List quantClauseStmts = new TypedList(new LinkedList(), Stmt.class, false);

	      VarDeclarator quantVar = new VarDeclarator(null, quantVarN);
	      List getCallOpts = new TypedList(new LinkedList(), Expr.class, false);
              getCallOpts.add(itrForLoopVarExpr);
	      quantVar.init = nf.JL5Call(null,
	      		        nf.JL5Call(null, quantListVarExpr, "get", getCallOpts, new TypedList(new LinkedList(), TypeNode.class, false)),
				"intValue", new TypedList(new LinkedList(), Expr.class, false), new TypedList(new LinkedList(), TypeNode.class, false));   
	      List quantVarD = this.variableDeclarators(nf.CanonicalTypeNode(null, this.ts.Int()), quantVar, fl);
	      quantClauseStmts.addAll(quantVarD);
	      
	      quantClauseStmts.add(nf.JL5If(null, nf.Unary(null, Unary.NOT, quantExpr), 
	          nf.JL5Return(null, nf.BooleanLit(null, false)), null));	    
	      Stmt forLoopBody = nf.Block(null, quantClauseStmts);
	      forLoop = nf.For(null, forLoopInit, forLoopCond, forLoopUpd, forLoopBody);
	      l.addAll(quantListVarD);
	      l.add(forLoop);
	      l.add(nf.JL5Return(null, nf.BooleanLit(null, true)));
	      return l;
	      */

    }


    protected Node clearPureFlag(MethodDecl md) {
      return md.flags(md.flags().clear(((ESJTypeSystem)typeSystem()).Pure()));
    }

    protected Node leaveCall(Node n) throws SemanticException {
	if (n instanceof ESJQuantifyExpr) {
	    //return super.leaveCall(nf.BooleanLit(null, false));
	    return super.leaveCall(DesugarQuantifyExpr((ESJQuantifyExpr)n));
	} else
	    return super.leaveCall(n);
    }

}

