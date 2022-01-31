package com.occamlab.te.saxon;

import java.util.List;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.SymbolicName.F;
import net.sf.saxon.trans.XPathException;

import com.occamlab.te.Test;
import com.occamlab.te.index.FunctionEntry;
import com.occamlab.te.index.Index;

public class TEFunctionLibrary implements FunctionLibrary {
    Configuration config = null;
    Index index = null;

    public TEFunctionLibrary(Configuration config, Index index) {
        this.config = config;
        this.index = index;
    }

    public Expression bind(StructuredQName functionName,
            Expression[] staticArgs, StaticContext env) throws XPathException {
        if (functionName.getURI().equals(Test.TE_NS)
                && functionName.getLocalPart().equals("get-type")) {
            return new GetTypeFunctionCall(functionName, staticArgs, env);
        }

        String key = functionName.getClarkName();
        List<FunctionEntry> functions = index.getFunctions(key);
        int argCount = staticArgs.length;

        if (functions != null) {
            for (FunctionEntry fe : functions) {
                if (argCount >= fe.getMinArgs() && argCount <= fe.getMaxArgs()) {
                    if (fe.isJava()) {
                        TEJavaFunctionCall fc = new TEJavaFunctionCall(fe,
                                functionName, staticArgs, env);
                        return fc;
                    } else {
                        TEXSLFunctionCall fc = new TEXSLFunctionCall(fe,
                                functionName, staticArgs, env);
                        return fc;
                    }
                }
            }
        }

        // Just return null rather than throw an exception, because there may be
        // another function library that supports this function
        return null;
    }

    public FunctionLibrary copy() {
        return new TEFunctionLibrary(config, index);
    }

    public boolean isAvailable(StructuredQName functionName, int arity) {
        String key = functionName.getClarkName();
        List<FunctionEntry> functions = index.getFunctions(key);
        if (functions != null) {
            for (FunctionEntry fe : functions) {
                if (arity == -1) {
                    return true;
                }
                if (arity >= fe.getMinArgs() && arity <= fe.getMaxArgs()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isAvailable(F functionName) {
        return false;
    }

    public Expression bind(F functionName, Expression[] staticArgs,
            StaticContext env, List<String> reasons) {
        return null;
    }

    public Function getFunctionItem(F functionName, StaticContext staticContext)
            throws XPathException {
        return null;
    }
}
