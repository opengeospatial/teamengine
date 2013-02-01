package com.occamlab.te.saxon;

import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.value.ObjectValue;

public class ObjValue extends XdmValue {
    public ObjValue(Object o) {
        super(new ObjectValue(o));
    }
}
