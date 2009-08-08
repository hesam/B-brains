package polyglot.ext.esj.ast;

import java.util.*;

import polyglot.ast.*;


// an ast node for representing an expression in a predicate method
public interface ESJQuantifyExpr extends Expr {

    public String id();
    public boolean quantKind();
    public String quantVar();
    public Expr quantListExpr();
    public ESJQuantifyClauseExpr quantClauseExpr();
    public ESJPredMethodDecl parentMethod();
    public void parentMethod(ESJPredMethodDecl m);

}

