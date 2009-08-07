package polyglot.ext.esj.ast;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.types.*;
import polyglot.ext.esj.types.*;
import polyglot.util.*;
import java.util.*;
import polyglot.ext.jl5.ast.*;
import polyglot.ext.jl5.types.FlagAnnotations;


/**
 * NodeFactory for esj extension.
 */
public class ESJNodeFactory_c extends JL5NodeFactory_c
    implements ESJNodeFactory {

    // TODO:  Implement factory methods for new AST nodes.
    // TODO:  Override factory methods for overriden AST nodes.
    // TODO:  Override factory methods for AST nodes with new extension nodes.
    public ESJPredMethodDecl ESJPredMethodDecl(Position pos, FlagAnnotations flags, TypeNode returnType, String name, List formals, List throwTypes, Block body) {
    	return new ESJPredMethodDecl_c(pos, flags, returnType, name, formals, throwTypes, body);
    }

    public ESJQuantifyExpr ESJQuantifyExpr(Position pos, boolean quantKind, String quantVar, Expr quantListExpr, Expr quantClauseExpr) {
	return new ESJQuantifyExpr_c(pos, quantKind, quantVar, quantListExpr, quantClauseExpr);
    }
}
