package polyglot.ext.esj.ast;

import java.util.*;

import polyglot.ast.*;
import polyglot.types.*;

// an ast node for representing an expression in a predicate method
public interface ESJQuantifyClauseExpr extends Expr {

    public Expr expr();
    public Expr expr(Expr e);
}

