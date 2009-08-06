package polyglot.ext.esj.types;

import polyglot.frontend.Source;
import polyglot.types.*;
import polyglot.ext.jl.types.TypeSystem_c;
import polyglot.ext.jl5.types.JL5TypeSystem_c;
import polyglot.util.*;
import polyglot.ast.Binary;
import polyglot.ast.Unary;
import polyglot.ast.Special;
import java.util.*;

public class ESJTypeSystem_c extends JL5TypeSystem_c
    implements ESJTypeSystem {
    // TODO: implement new methods in ESJTypeSystem.
    // TODO: override methods as needed from TypeSystem_c.
    protected Flags PURE;

    protected void initFlags() {
        super.initFlags();
        PURE = createNewFlag("pure",
                             Public().Private().Protected().Static().Final());
    }

    public Flags Pure() {
        return PURE;
    }

    public boolean canOverride(MethodInstance mi, MethodInstance mj) {
        // Cannot override pure method with a non-pure method.
        if (! mi.flags().contains(PURE) && mj.flags().contains(PURE))
            return false;
        return super.canOverride(mi, mj);
    }

}

