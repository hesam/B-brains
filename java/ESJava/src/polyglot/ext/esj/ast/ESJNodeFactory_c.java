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
public class ESJNodeFactory_c extends JL5NodeFactory_c
    implements ESJNodeFactory {

    // TODO:  Implement factory methods for new AST nodes.
    // TODO:  Override factory methods for overriden AST nodes.
    // TODO:  Override factory methods for AST nodes with new extension nodes.

    public ESJEnsuredClassDecl ESJEnsuredClassDecl(Position pos, FlagAnnotations fl, String name, 
						   TypeNode superType, List interfaces, ClassBody body, 
						   List<ParamTypeNode> paramTypes) {
	return new ESJEnsuredClassDecl_c(pos, fl, name, superType, interfaces, body, paramTypes);
    }

    public ESJPredMethodDecl ESJPredMethodDecl(Position pos, FlagAnnotations flags, 
					       TypeNode returnType, String name, List formals, 
					       List throwTypes, Block body, List paramTypes, 
					       String quantMtdId, FormulaBinary.Operator quantKind, 
					       String quantVarN, List quantVarD, 
					       LocalInstance quantVarI,
					       Expr quantListExpr, ESJQuantifyClauseExpr quantClauseExpr) {	
    	return new ESJPredMethodDecl_c(pos, flags, returnType, name, formals, throwTypes, body, paramTypes, quantMtdId, quantKind, quantVarN, quantVarD, quantVarI, quantListExpr, quantClauseExpr);
    }


    public ESJLogPredMethodDecl ESJLogPredMethodDecl(Position pos, FlagAnnotations flags, 
						     TypeNode returnType, String name, 
						     List formals, List throwTypes, Block body, 
						     List paramTypes, boolean isFallBack) {	
    	return new ESJLogPredMethodDecl_c(pos, flags, returnType, name, formals, throwTypes, body, paramTypes, isFallBack);
    }

    public ESJEnsuredMethodDecl ESJEnsuredMethodDecl(Position pos, FlagAnnotations flags,
						     TypeNode returnType, String name,
						     List formals, List throwTypes, Block body, 
						     List paramTypes, Expr ensuresExpr, 
						     JL5Formal catchFormal) {
	return new ESJEnsuredMethodDecl_c(pos, flags, returnType, name, formals, throwTypes, body, paramTypes, ensuresExpr, catchFormal);

    }

    public ESJQuantifyExpr ESJQuantifyExpr(Position pos, FormulaBinary.Operator quantKind, String quantVarN, 
					   List quantVarD, LocalInstance quantVarI, 
					   Expr quantListExpr, Expr quantClauseExpr) {
	return new ESJQuantifyExpr_c(pos, quantKind, quantVarN, quantVarD, quantVarI, quantListExpr, quantClauseExpr);
    }

    public ESJLogQuantifyExpr ESJLogQuantifyExpr(Position pos, FormulaBinary.Operator quantKind, String quantVarN, 
					   List quantVarD, LocalInstance quantVarI, 
					   Expr quantListExpr, Expr quantClauseExpr) {
	return new ESJLogQuantifyExpr_c(pos, quantKind, quantVarN, quantVarD, quantVarI, quantListExpr, quantClauseExpr);
    }

    public ESJQuantifyTypeExpr ESJQuantifyTypeExpr(Position pos, CanonicalTypeNode theType) {
	return new ESJQuantifyTypeExpr_c(pos, theType);
    }

    public FormulaBinary FormulaBinary(Position pos, Expr left, Binary.Operator op, Expr right) {
	return new FormulaBinary_c(pos, left, op,  right);
    }

    public CmpBinary CmpBinary(Position pos, Expr left, Binary.Operator op, Expr right) {
	return new CmpBinary_c(pos, left, op,  right);
    }
    
    public ESJQuantVarLocalDecl ESJQuantVarLocalDecl(Position pos, FlagAnnotations flags, TypeNode type, String name, Expr init) {
	return new ESJQuantVarLocalDecl_c(pos, flags, type, name, init);
    }

    public ESJFieldDecl ESJFieldDecl(Position pos, FlagAnnotations flags, TypeNode type, String name, Expr init, boolean isPrime) {
	return new ESJFieldDecl_c(pos, flags, type, name, init, isPrime);
    }
}
