package polyglot.ext.esj.ast;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.types.*;
import polyglot.ext.esj.types.*;
import polyglot.util.*;
import java.util.*;
import polyglot.ext.jl5.ast.*;
import polyglot.ext.jl5.parse.JL5Name;
import polyglot.ext.jl5.types.FlagAnnotations;

/**
 * NodeFactory for esj extension.
 */
public interface ESJNodeFactory extends JL5NodeFactory {
    // TODO: Declare any factory methods for new AST nodes.
    ESJPredMethodDecl ESJPredMethodDecl(Position pos, FlagAnnotations flags,
					TypeNode returnType, String name,
					List formals, List throwTypes, Block body, String quantMtdId, boolean quantKind,
					String quantVarN, List quantVarD, Expr quantListExpr, Expr quantClauseExpr);
    
    ESJEnsuredMethodDecl ESJEnsuredMethodDecl(Position pos, FlagAnnotations flags,
				       TypeNode returnType, String name,
				       List formals, List throwTypes, Block body, 
				       List paramTypes, Expr ensuresExpr);

    ESJQuantifyExpr ESJQuantifyExpr(Position pos, boolean quantKind, String quantVar, Expr quantListExpr, Expr quantClauseExpr);

    ESJQuantifyTypeExpr ESJQuantifyTypeExpr(Position pos, CanonicalTypeNode theType);
}
