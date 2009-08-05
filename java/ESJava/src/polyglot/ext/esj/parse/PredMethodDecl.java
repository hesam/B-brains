package polyglot.ext.esj.parse;

import java.util.*;

import polyglot.ast.*;

// A temporary class used for passing an ordered dispatch method declaration.
public class PredMethodDecl {
    protected Expr quantListExpr, quantClauseExpr;
    protected int quantKind;
    protected String quantVar;

    PredMethodDecl(int quantKind, String quantVar, Expr quantListExpr, Expr quantClauseExpr) {
	this.quantKind = quantKind;
	this.quantVar = quantVar;
	this.quantListExpr = quantListExpr;
	this.quantClauseExpr = quantClauseExpr;
    }

    public Expr quantListExpr() {
	return quantListExpr;
    }

    public Expr quantClauseExpr() {
	return quantClauseExpr;
    }

    public int quantKind() {
	return quantKind;
    }

    public String quantVar() {
	return quantVar;
    }

}

