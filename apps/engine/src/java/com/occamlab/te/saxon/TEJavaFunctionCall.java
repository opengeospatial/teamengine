package com.occamlab.te.saxon;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Node;

import com.occamlab.te.Engine;
import com.occamlab.te.TEClassLoader;
import com.occamlab.te.TECore;
import com.occamlab.te.Test;
import com.occamlab.te.index.FunctionEntry;
import com.occamlab.te.util.Misc;

import net.sf.saxon.Controller;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.AxisAtomizingIterator;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ExpressionTool;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.AxisIterator;
import net.sf.saxon.om.EmptyIterator;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Value;

public class TEJavaFunctionCall extends TEFunctionCall {
    FunctionEntry fe;
    Method[] methods = null;
    
    public TEJavaFunctionCall(FunctionEntry fe, StructuredQName functionName, Expression[] staticArgs, StaticContext env) throws XPathException {
        super(functionName, staticArgs, env);
        this.fe = fe;
    }
    
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        Controller controller = context.getController();
        ObjectValue ov = (ObjectValue)controller.getParameter("{" + Test.TE_NS + "}core");
        TECore core = (TECore)ov.getObject();
        TEClassLoader cl = core.getEngine().getClassLoader(core.getOpts().getSourcesName());

        if (methods == null) {
            methods = new Method[fe.getMaxArgs() + 1];
            for (int i = fe.getMinArgs(); i <= fe.getMaxArgs(); i++) {
                try {
                    methods[i] = Misc.getMethod(fe.getClassName(), fe.getMethod(), cl, i);
                } catch (Exception e) {
                    throw new XPathException("Error: Unable to bind function " + fe.getName(), e);
                }
            }
        }

        Object instance = null;
        if (fe.isInitialized()) {
            instance = core.getFunctionInstance(fe.hashCode());
            if (instance == null) {
                try {
                    instance = Misc.makeInstance(fe.getClassName(), fe.getClassParams(), cl);
                    core.putFunctionInstance(fe.hashCode(), instance);
                } catch (Exception e) {
                    throw new XPathException(e);
                }
//                List<Node> classParams = fe.getClassParams();
//                Object[] classParamObjects = new Object[classParams.size()];
//                Constructor[] constructors = method.getDeclaringClass().getConstructors();
//                for (int i = 0; i < constructors.length; i++) {
//                    Class<?>[] types = constructors[i].getParameterTypes();
//                    if (types.length == classParams.size()) {
//                        boolean constructorCorrect = true;
//                        for (int j = 0; j < types.length; j++) {
//                            Node n = classParams.get(j);
//                            if (types[j].isAssignableFrom(Node.class)) {
//                                classParamObjects[j] = n;
//                            } else if (types[j] == String.class) {
//                                classParamObjects[j] = n.getTextContent();
//                            } else if (types[j] == Character.class) {
//                                classParamObjects[j] = n.getTextContent().charAt(0);
//                            } else if (types[j] == Boolean.class) {
//                                classParamObjects[j] = Boolean.parseBoolean(n.getTextContent());
//                            } else if (types[j] == Byte.class) {
//                                classParamObjects[j] = Byte.parseByte(n.getTextContent());
//                            } else if (types[j] == Short.class) {
//                                classParamObjects[j] = Short.parseShort(n.getTextContent());
//                            } else if (types[j] == Integer.class) {
//                                classParamObjects[j] = Integer.parseInt(n.getTextContent());
//                            } else if (types[j] == Float.class) {
//                                classParamObjects[j] = Float.parseFloat(n.getTextContent());
//                            } else if (types[j] == Double.class) {
//                                classParamObjects[j] = Double.parseDouble(n.getTextContent());
//                            } else {
//                                constructorCorrect = false;
//                                break;
//                            }
//                        }
//                        if (constructorCorrect) {
//                            try {
//                                instance = constructors[i].newInstance(classParamObjects);
//                                core.putFunctionInstance(fe.getId(), instance);
//                            } catch (Exception e) {
//                                throw new XPathException(e);
//                            }
//                            break;
//                        }
//                    }
//                }
            }
        }
        Expression[] argExpressions = getArguments();
        Object[] javaArgs = new Object[argExpressions.length];
        Method m;
        Class[] types;
        int argsIndex;
        if (fe.usesContext()) {
            m = methods[argExpressions.length + 1];
            types = m.getParameterTypes();
            ValueRepresentation vr = context.getContextItem();
            javaArgs[0] = Value.asValue(vr).convertToJava(types[0], context);
            argsIndex = 1;
        } else {
            m = methods[argExpressions.length];
            types = m.getParameterTypes();
            argsIndex = 0;
        }
        for (int i = 0; i < argExpressions.length; i++) {
            ValueRepresentation vr = ExpressionTool.lazyEvaluate(argExpressions[i], context, 1);
//            Value v = Value.asValue(vr);
//            boolean b = (v instanceof AtomicValue);
//            SequenceIterator it = Atomizer.getAtomizingIterator(v.iterate());
//            if (it instanceof AxisIterator) {
//                AxisAtomizingIterator aai = new AxisAtomizingIterator((AxisIterator)it);
//            }
//            Object o = it.next();
//            javaArgs[argsIndex] = v.convertToJava(String.class, context);
            javaArgs[argsIndex] = Value.asValue(vr).convertToJava(types[argsIndex], context);
            argsIndex++;
        }
        Object result;
        try {
            result = m.invoke(instance, javaArgs);
        } catch (Exception e) {
            Throwable cause = e;
            if (e instanceof InvocationTargetException) {
                cause = e.getCause();
            }
            String msg = "Error invoking " + fe.getId() + "\n" + cause.getClass().getName();
            if (cause.getMessage() != null) {
                msg += ": " + cause.getMessage();
            }
            throw new XPathException(msg, cause);
        }
        if (result == null) {
            return EmptyIterator.getInstance();
        } else {
            Value v = Value.convertJavaObjectToXPath(result, SequenceType.ANY_SEQUENCE, context);
            return v.iterate();
        }
    }
}
