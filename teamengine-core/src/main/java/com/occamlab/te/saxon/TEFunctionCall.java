package com.occamlab.te.saxon;

/*-
 * #%L
 * TEAM Engine - Core Module
 * %%
 * Copyright (C) 2006 - 2024 Open Geospatial Consortium
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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

	public TEFunctionCall(StructuredQName functionName, Expression[] staticArgs, StaticContext env) {
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

	protected void checkArguments(ExpressionVisitor visitor) throws XPathException {
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
