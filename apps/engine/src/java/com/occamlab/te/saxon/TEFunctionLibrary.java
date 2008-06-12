package com.occamlab.te.saxon;

import java.lang.reflect.Method;

import com.occamlab.te.Test;
import com.occamlab.te.index.FunctionEntry;
import com.occamlab.te.index.Index;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.functions.ExtensionFunctionCall;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.functions.JavaExtensionFunctionFactory;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Value;

public class TEFunctionLibrary implements FunctionLibrary {
    Configuration config = null;
    Index index = null;
    
    public TEFunctionLibrary(Configuration config, Index index) {
        this.config = config;
        this.index = index;
    }

    public Expression bind(StructuredQName functionName, Expression[] staticArgs, StaticContext env) throws XPathException {
        if (functionName.getNamespaceURI().equals(Test.TE_NS) && functionName.getLocalName().equals("get-type")) {
            return new GetTypeFunctionCall(functionName, staticArgs, env);
        }

        String key = functionName.getClarkName();
        FunctionEntry fe = index.getFunction(key);
        if (fe == null) {
            // Just return null rather than throw an exception, because there may be
            // another function library that supports this function
            return null;
        }

        int argCount = staticArgs.length;
        if (argCount < fe.getMinArgs() || argCount > fe.getMaxArgs()) {
            return null;
        }

        if (fe.isJava()) {
            TEJavaFunctionCall fc = new TEJavaFunctionCall(fe, functionName, staticArgs, env);
            return fc;
        } else {
            TEXSLFunctionCall fc = new TEXSLFunctionCall(fe, functionName, staticArgs, env);
            return fc;
        }
    }

    public FunctionLibrary copy() {
        return new TEFunctionLibrary(config, index);
    }

    public boolean isAvailable(StructuredQName functionName, int arity) {
        String key = functionName.getClarkName();
        FunctionEntry fe = index.getFunction(key);
        if (fe == null) {
            return false;
        }
        if (fe.isJava()) {
            return (arity >= fe.getMinArgs() && arity <= fe.getMaxArgs());
        }
        return true;
    }

}
