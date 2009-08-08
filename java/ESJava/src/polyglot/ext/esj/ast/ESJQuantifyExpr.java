package polyglot.ext.esj.ast;

import java.util.*;

import polyglot.ast.*;
import polyglot.ext.jl5.ast.*;

// an ast node for representing an expression in a predicate method
public interface ESJQuantifyExpr extends Expr {

    public String id();
    public boolean quantKind();
    public String quantVar();
    public Expr quantListExpr();
    public ESJQuantifyClauseExpr quantClauseExpr();
    public JL5MethodDecl parentMethod();
    public void parentMethod(JL5MethodDecl m);

}

