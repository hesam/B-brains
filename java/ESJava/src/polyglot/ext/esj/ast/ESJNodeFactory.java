package polyglot.ext.esj.ast;

import polyglot.ast.*;
import polyglot.types.*;
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

    ESJEnsuredClassDecl ESJEnsuredClassDecl(Position pos, FlagAnnotations fl, String name, 
					    TypeNode superType, List interfaces, ClassBody body, 
					    List<ParamTypeNode> paramTypes);

    ESJPredMethodDecl ESJPredMethodDecl(Position pos, FlagAnnotations flags,
					TypeNode returnType, String name,
					List formals, List throwTypes, Block body, 
					List paramTypes, String quantMtdId, 
					FormulaBinary.Operator quantKind, String quantVarN, List quantVarD, 
					LocalInstance quantVarI, Expr quantListExpr, 
					ESJQuantifyClauseExpr quantClauseExpr);

    ESJLogPredMethodDecl ESJLogPredMethodDecl(Position pos, FlagAnnotations flags,
					      TypeNode returnType, String name,
					      List formals, List throwTypes, Block body, 
					      List paramTypes, boolean isFallBack); 
    
    ESJEnsuredMethodDecl ESJEnsuredMethodDecl(Position pos, FlagAnnotations flags,
					      TypeNode returnType, String name,
					      List formals, List throwTypes, Block body, 
					      List paramTypes, Expr ensuresExpr, 
					      JL5Formal catchFormal);

    ESJQuantifyExpr ESJQuantifyExpr(Position pos, FormulaBinary.Operator quantKind, String quantVarN, List quantVarD, LocalInstance quantVarI, Expr quantListExpr, Expr quantClauseExpr);

    ESJLogQuantifyExpr ESJLogQuantifyExpr(Position pos, FormulaBinary.Operator quantKind, String quantVarN, List quantVarD, LocalInstance quantVarI, Expr quantListExpr, Expr quantClauseExpr);

    ESJQuantifyTypeExpr ESJQuantifyTypeExpr(Position pos, CanonicalTypeNode theType);

    FormulaBinary FormulaBinary(Position pos, Expr left, Binary.Operator op, Expr right);
    CmpBinary CmpBinary(Position pos, Expr left, Binary.Operator op, Expr right);
    ESJQuantVarLocalDecl ESJQuantVarLocalDecl(Position pos, FlagAnnotations flags, TypeNode type, String name, Expr init);

    ESJFieldDecl ESJFieldDecl(Position pos, FlagAnnotations flags, TypeNode type, String name, Expr init, boolean isPrime);
}
