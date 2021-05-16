package com.occamlab.te.saxon;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FunctionCall;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;

public class TEFunctionCall extends FunctionCall {
	
    private StructuredQName functionName;
	
    public TEFunctionCall(StructuredQName functionName,
            Expression[] staticArgs, StaticContext env) {
        super();
        setFunctionName(functionName);
        this.setArguments(staticArgs);
    }

    public Expression preEvaluate(ExpressionVisitor visitor) {
        return this;
    }

    public int getImplementationMethod() {
        return ITERATE_METHOD;
    }

    protected void checkArguments(ExpressionVisitor visitor)
            throws XPathException {
        // Assume arguments are OK
    }

    protected int computeCardinality() {
        return StaticProperty.ALLOWS_ZERO_OR_MORE;
    }
    
    public Expression copy(RebindingMap rebindings) {
        throw new UnsupportedOperationException();
    }
    
    public ItemType getItemType() {
        return AnyItemType.getInstance();
    }
    
    public void setFunctionName(StructuredQName fName) {
        this.functionName = fName;	
    }

    public StructuredQName getFunctionName() {
        return functionName;
    }
	
    public Function getTargetFunction(XPathContext context) throws XPathException {
        return null;
    }
}
