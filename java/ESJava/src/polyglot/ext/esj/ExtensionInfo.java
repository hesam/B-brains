package polyglot.ext.esj;

import polyglot.lex.Lexer;
import polyglot.ext.esj.parse.Lexer_c;
import polyglot.ext.esj.parse.Grm;
import polyglot.ext.esj.ast.ESJNodeFactory_c;
import polyglot.ext.esj.types.ESJTypeSystem_c;

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

    public static final Pass.ID PREDICATE_POLARITY =
    new Pass.ID("predicate-polarity");

    public static final Pass.ID PURITY_CHECK =
    new Pass.ID("check-purity");

    public static final Pass.ID CANONICALIZE_PREDICATES =
    new Pass.ID("canonicalize-predicates");

    public static final Pass.ID CREATE_PREDICATE_TYPES =
    new Pass.ID("create-predicate-types");

    public static final Pass.ID CREATE_PREDICATE_TYPES_ALL =
    new Pass.ID("create-predicate-types-barrier");
    
    public static final Pass.ID EXTRACT_BINDINGS =
    new Pass.ID("extract-bindings");

    public static final Pass.ID BUILD_DISPATCHERS =
    new Pass.ID("build-dispatchers");

    public static final Pass.ID IMPLEMENTATION_SIDE_TYPE_CHECK =
    new Pass.ID("implementation-side-type-check");
    
    public static final Pass.ID TRANSLATE_ESJ =
    new Pass.ID("translate-esj");

}
