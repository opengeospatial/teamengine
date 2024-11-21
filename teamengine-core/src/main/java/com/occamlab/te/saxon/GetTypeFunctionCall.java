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

import java.util.List;

import javax.xml.namespace.QName;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ExpressionTool;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Value;

public class GetTypeFunctionCall extends TEFunctionCall {

	List<QName> params = null;

	boolean usesContext = false;

	TypeHierarchy th;

	public GetTypeFunctionCall(StructuredQName functionName, Expression[] staticArgs, StaticContext env) {
		super(functionName, staticArgs, env);
	}

	public static String getTypeName(ItemType it) throws XPathException {
		if (it instanceof SchemaType) {
			return "xs:" + ((SchemaType) it).getName();
		}
		return it.toString();
	}

	public SequenceIterator iterate(XPathContext context) throws XPathException {
		Expression[] argExpressions = getArguments();
		ValueRepresentation vr = ExpressionTool.lazyEvaluate(argExpressions[0], context, 1);
		ItemType it = Value.asValue(vr).getItemType(context.getConfiguration().getTypeHierarchy());
		String type = getTypeName(it);
		Value v = Value.convertJavaObjectToXPath(type, SequenceType.SINGLE_STRING, context);
		return v.iterate();
	}

}
