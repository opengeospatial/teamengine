package com.occamlab.te.saxon;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Node;

import com.occamlab.te.TECore;
import com.occamlab.te.Test;
import com.occamlab.te.index.FunctionEntry;

import net.sf.saxon.Controller;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ExpressionTool;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.EmptyIterator;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Value;

public class TEJavaFunctionCall extends TEFunctionCall {
    FunctionEntry fe;
    Method method;
    
    public TEJavaFunctionCall(FunctionEntry fe, StructuredQName functionName, Expression[] staticArgs, StaticContext env) throws XPathException {
        super(functionName, staticArgs, env);
        this.fe = fe;
        Class c;
        try {
            c = Class.forName(fe.getClassName());
        } catch (ClassNotFoundException e) {
            throw new XPathException("Error: Unable to bind function " + functionName.getDisplayName() + " because class " + fe.getClassName() + " was not found.");
        }
        int argCount = staticArgs.length;
        if (fe.usesContext()) {
            argCount++;
        }
        Method[] methods = c.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            if (m.getName().equals(fe.getMethod()) && m.getParameterTypes().length == argCount) {
                method = m;
                return;
            }
        }
        throw new XPathException("Error: Unable to bind function " + functionName.getDisplayName() + " because method" + fe.getMethod() + " with " + Integer.toString(argCount) + " argument(s) was not found in class " + fe.getClassName());
    }
    
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        Controller controller = context.getController();
        ObjectValue ov = (ObjectValue)controller.getParameter("{" + Test.TE_NS + "}core");
        TECore core = (TECore)ov.getObject();

        Object instance = null;
        if (fe.isInitialized()) {
            instance = core.getFunctionInstance(fe.getId());
            if (instance == null) {
                List<Node> classParams = fe.getClassParams();
                Object[] classParamObjects = new Object[classParams.size()];
                Constructor[] constructors = method.getDeclaringClass().getConstructors();
                for (int i = 0; i < constructors.length; i++) {
                    Class<?>[] types = constructors[i].getParameterTypes();
                    if (types.length == classParams.size()) {
                        boolean constructorCorrect = true;
                        for (int j = 0; j < types.length; j++) {
                            Node n = classParams.get(j);
                            if (types[j].isAssignableFrom(Node.class)) {
                                classParamObjects[j] = n;
                            } else if (types[j] == String.class) {
                                classParamObjects[j] = n.getTextContent();
                            } else if (types[j] == Character.class) {
                                classParamObjects[j] = n.getTextContent().charAt(0);
                            } else if (types[j] == Boolean.class) {
                                classParamObjects[j] = Boolean.parseBoolean(n.getTextContent());
                            } else if (types[j] == Byte.class) {
                                classParamObjects[j] = Byte.parseByte(n.getTextContent());
                            } else if (types[j] == Short.class) {
                                classParamObjects[j] = Short.parseShort(n.getTextContent());
                            } else if (types[j] == Integer.class) {
                                classParamObjects[j] = Integer.parseInt(n.getTextContent());
                            } else if (types[j] == Float.class) {
                                classParamObjects[j] = Float.parseFloat(n.getTextContent());
                            } else if (types[j] == Double.class) {
                                classParamObjects[j] = Double.parseDouble(n.getTextContent());
                            } else {
                                constructorCorrect = false;
                                break;
                            }
                        }
                        if (constructorCorrect) {
                            try {
                                instance = constructors[i].newInstance(classParamObjects);
                                core.putFunctionInstance(fe.getId(), instance);
                            } catch (Exception e) {
                                throw new XPathException(e);
                            }
                            break;
                        }
                    }
                }
            }
        }
        Expression[] argExpressions = getArguments();
        Object[] javaArgs = new Object[argExpressions.length];
        Class[] types = method.getParameterTypes();
        int argsIndex = 0;
        if (fe.usesContext()) {
            ValueRepresentation vr = context.getContextItem();
            javaArgs[argsIndex] = Value.asValue(vr).convertToJava(types[argsIndex], context);
            argsIndex++;
        }
        for (int i = 0; i < argExpressions.length; i++) {
            ValueRepresentation vr = ExpressionTool.lazyEvaluate(argExpressions[i], context, 1);
            javaArgs[argsIndex] = Value.asValue(vr).convertToJava(types[argsIndex], context);
            argsIndex++;
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
}
