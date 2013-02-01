package com.occamlab.te.saxon;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ExpressionVisitor;
import net.sf.saxon.expr.FunctionCall;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;

public class TEFunctionCall extends FunctionCall {
    public TEFunctionCall(StructuredQName functionName,
            Expression[] staticArgs, StaticContext env) {
        super();
        this.setFunctionName(functionName);
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

    public Expression copy() {
        throw new UnsupportedOperationException();
    }

    public ItemType getItemType(TypeHierarchy th) {
        return AnyItemType.getInstance();
    }
}
