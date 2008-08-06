package com.occamlab.te.saxon;

import static com.occamlab.te.saxon.GetTypeFunctionCall.getTypeName;

import java.io.CharArrayReader;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;

import com.occamlab.te.Globals;
import com.occamlab.te.TECore;
import com.occamlab.te.Test;
import com.occamlab.te.index.FunctionEntry;
import com.occamlab.te.util.DomUtils;

import net.sf.saxon.Controller;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ExpressionTool;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.instruct.ParameterSet;
import net.sf.saxon.om.EmptyIterator;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.s9api.S9APIUtils;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Value;

public class TEXSLFunctionCall extends TEFunctionCall {
    FunctionEntry fe;
    
    public TEXSLFunctionCall(FunctionEntry fe, StructuredQName functionName, Expression[] staticArgs, StaticContext env) {
        super(functionName, staticArgs, env);
        this.fe = fe;
    }
    
    public static String getType(Expression expr, XPathContext context) throws XPathException {
        ValueRepresentation vr = ExpressionTool.lazyEvaluate(expr, context, 1);
        ItemType it = Value.asValue(vr).getItemType(context.getConfiguration().getTypeHierarchy());
        if (it instanceof SchemaType) {
            return "xs:" + ((SchemaType)it).getName();
        }
        return "xs:any";
    }
    
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        Controller controller = context.getController();
        ObjectValue ov = (ObjectValue)controller.getParameter("{" + Test.TE_NS + "}core");
        TECore core = (TECore)ov.getObject();
            
        Expression[] argExpressions = getArguments();
        String xml = "<params>\n";
        List<QName> params = fe.getParams();
        for (int i = 0; i < params.size(); i++) {
            QName param = params.get(i);
            xml += "<param";
            xml += " local-name=\"" + param.getLocalPart() + "\"";
            xml += " namespace-uri=\"" + param.getNamespaceURI() + "\"";
            xml += " prefix=\"" + param.getPrefix() + "\"";
            ValueRepresentation vr = ExpressionTool.lazyEvaluate(argExpressions[i], context, 1);
//            if (vr instanceof NodeInfo) {
//                NodeInfo ni = (NodeInfo)vr;
//                int kind = ni.getNodeKind();
//                if (kind == Type.DOCUMENT || kind == Type.ELEMENT) {
//                    xml += ">\n";
//                    xml += "<value>";
//                    xml += S9APIUtils.makeNode(ni).toString();
//                    xml += "</value>\n";
//                } else if (kind == Type.ATTRIBUTE) {
//                    xml += ">\n";
//                    xml += "<value " + ni.getDisplayName() + "=\"" + ni.getStringValue() + "\"";
//                    if (ni.getPrefix() != null) {
//                        xml += " xmlns:" + ni.getPrefix() + "=\"" + ni.getURI() + "\"";
//                    }
//                    xml += "/>\n";
//                }
//            } else {
            Value v = Value.asValue(vr);
            try {
                Node n = (Node)v.convertToJava(Node.class, context);
                int type = n.getNodeType();
                if (type == Node.ATTRIBUTE_NODE) {
                    xml += ">\n";
                    Attr attr = (Attr)n;
                    xml += "<value " + attr.getNodeName() + "=\"" + attr.getValue() + "\"";
                    if (attr.getPrefix() != null) {
                        xml += " xmlns:" + attr.getPrefix() + "=\"" + attr.getNamespaceURI() + "\"";
                    }
                    xml += "/>\n";
                } else if (type == Node.ELEMENT_NODE || type == Node.DOCUMENT_NODE) {
                    xml += ">\n";
                    xml += "<value>";
                    xml += DomUtils.serializeNode(n);
                    xml += "</value>\n";
                } else {
                    ItemType it = v.getItemType(context.getConfiguration().getTypeHierarchy());
                    xml += " type=\"" + getTypeName(it) + "\">\n";
                    xml += "<value>" + n.getNodeValue() + "</value>\n";
                }
            } catch (XPathException e) {
                ItemType it = v.getItemType(context.getConfiguration().getTypeHierarchy());
                xml += " type=\"" + getTypeName(it) + "\">\n";
                xml += "<value>" + v.getStringValue() + "</value>\n";
            }
            xml += "</param>\n";
        }
        xml += "</params>";
//System.out.println(xml);
        Source src = new StreamSource(new CharArrayReader(xml.toCharArray()));
//        XdmValue result = null;
        NodeInfo result = null;
        try {
//            result = core.executeTemplate(fe, Globals.builder.build(src), context);
            NodeInfo paramsNode = core.getEngine().getBuilder().build(src).getUnderlyingNode();
            result = core.executeXSLFunction(context, fe, paramsNode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (result == null) {
            return EmptyIterator.getInstance();
        } else {
//            Value v = Value.asValue(result.getUnderlyingValue());
            Value v = Value.asValue(result);
            return v.iterate();
        }
    }
}
