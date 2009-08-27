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
    protected FormulaBinary.Operator quantKind;
    protected String id,quantVarN;
    protected List quantVarD;
    protected LocalInstance quantVarI;
    protected Expr quantListExpr;
    protected ESJQuantifyClauseExpr quantClauseExpr;
    protected JL5MethodDecl parentMethod;

    public ESJQuantifyExpr_c(Position pos, FormulaBinary.Operator quantKind, String quantVarN, List quantVarD, LocalInstance quantVarI, Expr quantListExpr, Expr quantClauseExpr) {
	super(pos);
	this.id = (quantKind == FormulaBinary.ALL ? "univQuantify_": "existQuantify_") + Integer.toString(idCtr++);
	this.quantKind = quantKind;
	this.quantVarN = quantVarN;
	this.quantVarD = quantVarD;
	this.quantVarI = quantVarI;
	this.quantListExpr = quantListExpr;
	this.quantClauseExpr = new ESJQuantifyClauseExpr_c(pos, quantClauseExpr);
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

    public FormulaBinary.Operator quantKind() {
	return quantKind;
    }

    public String quantVarN() {
	return quantVarN;
    }

    public List quantVarD() {
	return quantVarD;
    }

    public LocalInstance quantVarI() {
	return quantVarI;
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

    // Reconstruct the pred expr.
    protected ESJQuantifyExpr_c reconstruct(FormulaBinary.Operator quantKind, String quantVarN, List quantVarD, Expr quantListExpr, ESJQuantifyClauseExpr quantClauseExpr) {
	
	if (quantListExpr != this.quantListExpr || quantClauseExpr != this.quantClauseExpr) {
	    ESJQuantifyExpr_c n = (ESJQuantifyExpr_c) copy();
	    n.quantKind = quantKind;
	    n.quantVarN = quantVarN;
	    n.quantVarD = quantVarD; //TypedList.copyAndCheck(quantVarD, LocalDecl.class, true);
	    n.quantVarI = quantVarI;
	    n.quantListExpr = quantListExpr;
	    n.quantClauseExpr = quantClauseExpr;
	    return n;
	}
	return this;
    }

    // Visit the children of the method. 

    public Node visitChildren(NodeVisitor v) {
	List quantVarD = (List) visitList(this.quantVarD, v);
	Expr quantListExpr = (Expr) visitChild(this.quantListExpr, v);
	ESJQuantifyClauseExpr quantClauseExpr = (ESJQuantifyClauseExpr) visitChild(this.quantClauseExpr, v);
	return reconstruct(this.quantKind, this.quantVarN, quantVarD, quantListExpr, quantClauseExpr);
    }

    
    public Node typeCheck(TypeChecker tc) throws SemanticException {
	ESJQuantifyExpr n = (ESJQuantifyExpr) super.typeCheck(tc);
	n = (ESJQuantifyExpr)n.type(tc.typeSystem().Boolean()); //FIXME
	return n;
    } 
    /*
    public Context enterScope(Node child, Context c) {
	System.out.println(child);
	System.out.println("qi2: " + quantVarI);
	if (child instanceof ESJQuantifyClauseExpr) {
	    c.addVariable(quantVarI);
	    //child.addDecls(c);
	    
	}

	return super.enterScope(child, c);
	}
    */

}

