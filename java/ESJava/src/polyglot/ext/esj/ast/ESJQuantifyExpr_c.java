package polyglot.ext.esj.ast;

import java.util.*;
import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.ext.jl5.ast.*;
import polyglot.util.*;
import polyglot.types.*;
import polyglot.ext.esj.types.ESJTypeSystem;
import polyglot.visit.*;


// A temporary class used for passing an ordered dispatch method declaration.
public class ESJQuantifyExpr_c extends Expr_c implements ESJQuantifyExpr {

    protected static int idCtr = 0;
    protected boolean quantKind;
    protected String id,quantVar;
    protected Expr quantListExpr;
    protected ESJQuantifyClauseExpr quantClauseExpr;
    protected JL5MethodDecl parentMethod;

    public ESJQuantifyExpr_c(Position pos, boolean quantKind, String quantVar, Expr quantListExpr, Expr quantClauseExpr) {
	super(pos);
	this.id = (quantKind ? "univQuantify_": "existQuantify_") + Integer.toString(idCtr++);
	this.quantKind = quantKind;
	this.quantVar = quantVar;
	this.quantListExpr = quantListExpr;
	this.quantClauseExpr = new ESJQuantifyClauseExpr_c(pos, quantVar, quantClauseExpr);
    }

    public Expr quantListExpr() {
	return quantListExpr;
    }

    public ESJQuantifyClauseExpr quantClauseExpr() {
	return quantClauseExpr;
    }

    public String id() {
	return id;
    }

    public boolean quantKind() {
	return quantKind;
    }

    public String quantVar() {
	return quantVar;
    }

    public JL5MethodDecl parentMethod() {
	return parentMethod;
    }

    public void parentMethod(JL5MethodDecl m) {
	this.parentMethod = m;
    }

    public List acceptCFG(CFGBuilder v, List succs) {
	return new ArrayList();
    }
    
    public Term entry() {
	return null;
    }

    /** Reconstruct the pred expr. */
    protected ESJQuantifyExpr_c reconstruct(boolean quantKind, String quantVar, Expr quantListExpr, ESJQuantifyClauseExpr quantClauseExpr) {
	return this;
    }

      /** Visit the children of the method. */
    public Node visitChildren(NodeVisitor v) {
	Expr quantLExpr = (Expr) visitChild(this.quantListExpr, v);
	ESJQuantifyClauseExpr quantCExpr = (ESJQuantifyClauseExpr) visitChild(this.quantClauseExpr, v);
	return reconstruct(this.quantKind, this.quantVar, quantLExpr, quantCExpr);
    }

    
    public Node typeCheck(TypeChecker tc) throws SemanticException {
	ESJQuantifyExpr n = (ESJQuantifyExpr) super.typeCheck(tc);
	n = (ESJQuantifyExpr)n.type(tc.typeSystem().Boolean()); //FIXME
	/*
	System.out.println("ESJQuantifyExpr tc...");
	System.out.println(n);
	System.out.println(n.type());
	System.out.println(quantListExpr);
	//System.out.println(((Expr)(quantListExpr.typeCheck(tc))).type());
	System.out.println(quantClauseExpr);
	//quantClauseExpr = (ESJQuantifyClauseExpr)(quantClauseExpr.typeCheck(tc)); //FIXME
	System.out.println(quantClauseExpr.type());
	*/
	/*
	    // make sure the predicateExpr has type boolean
	if (!(quantClauseExpr.type().isBoolean())) {
	    throw new SemanticException("A quantify clause must have type "
				        + "boolean.", position());
					}*/
	    // make sure that the restrictions on array accesses are met
	//System.out.println("ESJQuantifyExpr tc done");
	return n;
    } 

}

