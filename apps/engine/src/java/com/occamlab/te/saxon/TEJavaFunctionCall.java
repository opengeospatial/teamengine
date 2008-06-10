package com.occamlab.te.saxon;

import java.lang.reflect.Method;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ExpressionTool;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.EmptyIterator;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Value;

public class TEJavaFunctionCall extends TEFunctionCall {
    Method method = null;
    Object instance = null;
    
    public TEJavaFunctionCall(StructuredQName functionName, Expression[] staticArgs, StaticContext env) {
        super(functionName, staticArgs, env);
    }
    
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        Expression[] argExpressions = getArguments();
        Object[] javaArgs = new Object[argExpressions.length];
        Class[] types = method.getParameterTypes();
        for (int i = 0; i < argExpressions.length; i++) {
            ValueRepresentation vr = ExpressionTool.lazyEvaluate(argExpressions[i], context, 1);
            javaArgs[i] = Value.asValue(vr).convertToJava(types[i], context);
        }
        Object result;
        try {
            result = method.invoke(instance, javaArgs);
        } catch (Exception e) {
            throw new XPathException(e);
        }
        if (result == null) {
            return EmptyIterator.getInstance();
        } else {
            Value v = Value.convertJavaObjectToXPath(result, SequenceType.ANY_SEQUENCE, context);
            return v.iterate();
        }
    }
    
    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
