package polyglot.ext.esj.ast;

import java.util.*;
import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.util.*;
import polyglot.types.*;
import polyglot.ext.esj.types.ESJTypeSystem;
import polyglot.visit.*;


// A temporary class used for passing an ordered dispatch method declaration.
public class ESJQuantifyClauseExpr_c extends Expr_c implements ESJQuantifyClauseExpr {

    protected Expr expr;
    protected String quantVar;

    public ESJQuantifyClauseExpr_c(Position pos, String quantVar, Expr expr) {
	super(pos);
	this.quantVar = quantVar;
	this.expr = expr;
    }

    public Expr expr() {
	return expr;
    }

    public String quantVar() {
	return quantVar;
    }

    public List acceptCFG(CFGBuilder v, List succs) {
	return new ArrayList();
    }
    
    public Term entry() {
	return null;
    }

    /** Reconstruct the pred expr. */
    protected ESJQuantifyClauseExpr_c reconstruct(String quantVar, Expr expr) {
	return this;
    }

      /** Visit the children of the method. */
    public Node visitChildren(NodeVisitor v) {
	/*Expr theExpr = (Expr) visitChild(this.expr, v);
	  return reconstruct(this.quantVar, theExpr);*/
	return this;
    }

    
    public Node typeCheck(TypeChecker tc) throws SemanticException {
	System.out.println("ESJQuantifyClauseExpr tc...");
	ESJQuantifyClauseExpr n = (ESJQuantifyClauseExpr) super.typeCheck(tc);
	n = (ESJQuantifyClauseExpr)n.type(tc.typeSystem().Boolean()); //FIXME

	/*
	    // make sure the predicateExpr has type boolean
	if (!(quantClauseExpr.type().isBoolean())) {
	    throw new SemanticException("A quantify clause must have type "
				        + "boolean.", position());
					}*/
	    // make sure that the restrictions on array accesses are met
	System.out.println("ESJQuantifyClauseExpr tc done");
	return n;
    } 

}

