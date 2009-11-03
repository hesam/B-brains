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

    public ESJEnsuredClassDecl ESJEnsuredClassDecl(Position pos, FlagAnnotations fl, 
						   String name, TypeNode superType, 
						   List interfaces, ClassBody body, 
						   List<ParamTypeNode> paramTypes) {
	return new ESJEnsuredClassDecl_c(pos, fl, name, superType, interfaces, body, paramTypes);
    }

    public ESJLogVarClassDecl ESJLogVarClassDecl(Position pos, FlagAnnotations fl, 
						 String name, TypeNode superType, 
						 List interfaces, ClassBody body, 
						 List<ParamTypeNode> paramTypes) {
	return new ESJLogVarClassDecl_c(pos, fl, name, superType, interfaces, body, paramTypes);
    }

    public ESJMethodDecl ESJMethodDecl(Position pos, FlagAnnotations flags,
				       TypeNode returnType, String name,
				       List formals,
				       List throwTypes, Block body, List paramTypes, 
				       boolean isPredicate) {
	return new ESJMethodDecl_c(pos, flags, returnType, name, formals, throwTypes, body, paramTypes, isPredicate);
    }

    public ESJPredMethodDecl ESJPredMethodDecl(Position pos, FlagAnnotations flags, 
					       TypeNode returnType, String name, List formals, 
					       List throwTypes, Block body, List paramTypes, 
					       String quantMtdId, FormulaBinary.Operator quantKind, 
					       String quantVarN, List quantVarD,
					       Expr quantListExpr, 
					       ESJQuantifyClauseExpr quantClauseExpr, 
					       boolean isComprehension) {	
    	return new ESJPredMethodDecl_c(pos, flags, returnType, name, formals, throwTypes, body, paramTypes, quantMtdId, quantKind, quantVarN, quantVarD, quantListExpr, quantClauseExpr, isComprehension);
    }


    public ESJLogPredMethodDecl ESJLogPredMethodDecl(Position pos, FlagAnnotations flags, 
						     TypeNode returnType, String name, 
						     List formals, List throwTypes, Block body, 
						     List paramTypes, List quantVarD, 
						     List quantVarD2, boolean isPredicate, 
						     boolean isFallBack, boolean isLogVar) {	
    	return new ESJLogPredMethodDecl_c(pos, flags, returnType, name, formals, throwTypes, body, paramTypes, quantVarD, quantVarD2, isPredicate, isFallBack, isLogVar);
    }

    public ESJEnsuredMethodDecl ESJEnsuredMethodDecl(Position pos, FlagAnnotations flags,
						     TypeNode returnType, String name,
						     List formals, List throwTypes, Block body, 
						     List paramTypes, Expr ensuresExpr, 
						     JL5Formal catchFormal, 
						     List modifiableFields, 
						     Expr modifiableObjets) {
	return new ESJEnsuredMethodDecl_c(pos, flags, returnType, name, formals, throwTypes, body, paramTypes, ensuresExpr, catchFormal, modifiableFields, modifiableObjets);

    }

    public ESJQuantifyExpr ESJQuantifyExpr(Position pos, FormulaBinary.Operator quantKind, 
					   String quantVarN, 
					   List quantVarD, List quantVarD2, 
					   Expr quantListExpr, Expr quantClauseExpr,
					   boolean isComprehension) {
	return new ESJQuantifyExpr_c(pos, quantKind, quantVarN, quantVarD, quantVarD2, quantListExpr, quantClauseExpr, isComprehension);
    }

    public ESJLogQuantifyExpr ESJLogQuantifyExpr(Position pos, FormulaBinary.Operator quantKind,
						 String quantVarN, List quantVarD, 
						 List quantVarD2, Expr quantListExpr, 
						 Expr quantClauseExpr, 
						 ESJLogPredMethodDecl parentMethod, 
						 boolean isComprehension) {
	return new ESJLogQuantifyExpr_c(pos, quantKind, quantVarN, quantVarD, quantVarD2, quantListExpr, quantClauseExpr, parentMethod, isComprehension);
    }

    public ESJQuantifyTypeExpr ESJQuantifyTypeExpr(Position pos, String theType) {
	return new ESJQuantifyTypeExpr_c(pos, theType);
    }

    public FormulaBinary FormulaBinary(Position pos, Expr left, Binary.Operator op, Expr right) {
	return new FormulaBinary_c(pos, left, op,  right);
    }

    public CmpBinary CmpBinary(Position pos, Expr left, Binary.Operator op, Expr right) {
	return new CmpBinary_c(pos, left, op,  right);
    }
    
    public ESJFieldDecl ESJFieldDecl(Position pos, FlagAnnotations flags, TypeNode type, String name, Expr init, boolean isOld) {
	return new ESJFieldDecl_c(pos, flags, type, name, init, isOld);
    }

    public ESJFieldClosure ESJFieldClosure(Position pos, Receiver target, String name, FormulaBinary.Operator kind, List multiNames, String theType) {
	return new  ESJFieldClosure_c(pos, target, name, kind, multiNames, theType);
    }

    public ESJFieldCall ESJFieldCall(Position pos, Receiver target, String name, List arguments) {
	return new ESJFieldCall_c(pos, target, name, arguments);
    }

    public ESJFieldClosureCall ESJFieldClosureCall(Position pos, Receiver target, String name, List arguments, FormulaBinary.Operator kind) {
	return new ESJFieldClosureCall_c(pos, target, name, arguments, kind);
    }
}
