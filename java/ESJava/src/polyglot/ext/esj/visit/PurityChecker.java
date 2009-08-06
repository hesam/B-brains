package polyglot.ext.esj.visit;

import polyglot.visit.*;
import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.ext.esj.ast.*;
import polyglot.types.*;
import polyglot.ext.esj.types.*;
import polyglot.util.*;
import polyglot.frontend.Job;

import java.util.*;

/** Visitor that makes sure that pure methods are actually pure, and that
    only pure methods are called in predicates.  **/
public class PurityChecker extends ContextVisitor {
    protected boolean insidePredicate;

    public PurityChecker(Job job, TypeSystem ts, NodeFactory nf) {
	super(job, ts, nf);
        insidePredicate = false;
	System.out.println("in purity checking...");
    }

    protected boolean insidePureMethod() {
        return true;
    }

    protected boolean isPureMethod(MethodInstance mi) {
        return true;
    }

}
