package polyglot.ext.esj;

import polyglot.lex.Lexer;
import polyglot.ext.esj.parse.Lexer_c;
import polyglot.ext.esj.parse.Grm;
import polyglot.ext.esj.ast.ESJNodeFactory_c;
import polyglot.ext.esj.types.ESJTypeSystem_c;
import polyglot.ext.esj.visit.*;


import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import polyglot.frontend.*;
import polyglot.main.*;

import java.util.*;
import java.io.*;

/**
 * Extension information for esj extension.
 */
public class ExtensionInfo extends polyglot.ext.jl5.ExtensionInfo {
    static {
        // force Topics to load
        Topics t = new Topics();
    }

    public String defaultFileExtension() {
        return "esj";
    }

    public String compilerName() {
        return "esjc";
    }

    public Parser parser(Reader reader, FileSource source, ErrorQueue eq) {
        Lexer lexer = new Lexer_c(reader, source.name(), eq);
        Grm grm = new Grm(lexer, ts, nf, eq);
        return new CupParser(grm, source, eq);
    }

    protected NodeFactory createNodeFactory() {
        return new ESJNodeFactory_c();
    }

    protected TypeSystem createTypeSystem() {
        return new ESJTypeSystem_c();
    }

    public static final Pass.ID PURITY_CHECK =
    new Pass.ID("check-purity");

    public static final Pass.ID IMPLEMENTATION_SIDE_TYPE_CHECK =
    new Pass.ID("implementation-side-type-check");
    
    public static final Pass.ID TRANSLATE_ESJ =
    new Pass.ID("translate-esj");

    public static final Pass.ID TESTING_ESJ =
    new Pass.ID("testing-esj");

    public List passes(Job job) {
        List passes = super.passes(job);

	// make sure that pure methods are actually pure, and that
	// only pure methods are called inside predicates
	afterPass(passes, polyglot.ext.jl5.ExtensionInfo.TYPE_CHECK_ALL,
		  new VisitorPass(PURITY_CHECK, job,
				  new PurityChecker(job, ts, nf)));

	removePass(passes, Pass.REACH_CHECK); //FIXME

	// now translate ESJ AST to Java AST
	beforePass(passes, Pass.PRE_OUTPUT_ALL,
		   new VisitorPass(TRANSLATE_ESJ, job,
				   new ESJTranslator(job, ts, nf)));

	afterPass(passes, TRANSLATE_ESJ,
		   new VisitorPass(TESTING_ESJ, job,
				   new ESJTesting(job, ts, nf)));
	


	removePass(passes, Pass.EXC_CHECK); //FIXME

        return passes;
    }

}
