package com.occamlab.te.saxon;

import static com.occamlab.te.saxon.GetTypeFunctionCall.getTypeName;

import java.io.CharArrayReader;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Controller;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.ObjectValue;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;

import com.occamlab.te.TECore;
import com.occamlab.te.Test;
import com.occamlab.te.index.FunctionEntry;
import com.occamlab.te.util.DomUtils;

public class TEXSLFunctionCall extends TEFunctionCall {
    FunctionEntry fe;

    public TEXSLFunctionCall(FunctionEntry fe, StructuredQName functionName,
            Expression[] staticArgs, StaticContext env) {
        super(functionName, staticArgs, env);
        this.fe = fe;
    }

    public static String getType(Expression expr, XPathContext context)
            throws XPathException {
        Sequence sequence = ExpressionTool.lazyEvaluate(expr, context, false);
        ItemType it = SequenceTool.getItemType(sequence, context.getConfiguration().getTypeHierarchy());
        if (it instanceof SchemaType) {
            return "xs:" + ((SchemaType) it).getName();
        }
        return "xs:any";
    }

    public SequenceIterator iterate(XPathContext context) throws XPathException {
        Controller controller = context.getController();
        ObjectValue<?> ov = (ObjectValue<?>) controller.getParameter(new StructuredQName("", Test.TE_NS, "core"));
        TECore core = (TECore) ov.getObject();

        Expression[] argExpressions = getArguments();
        String xml = "<params>\n";
        List<QName> params = fe.getParams();
        for (int i = 0; i < params.size(); i++) {
            QName param = params.get(i);
            xml += "<param";
            xml += " local-name=\"" + param.getLocalPart() + "\"";
            xml += " namespace-uri=\"" + param.getNamespaceURI() + "\"";
            xml += " prefix=\"" + param.getPrefix() + "\"";
            GroundedValue groundValue = ExpressionTool.eagerEvaluate(
                    argExpressions[i], context);
            Value v = Value.asValue(vr);
            try {
                Node n = (Node) v.convertToJava(Node.class, context);
                int type = n.getNodeType();
                if (type == Node.ATTRIBUTE_NODE) {
                    xml += ">\n";
                    Attr attr = (Attr) n;
                    xml += "<value " + attr.getNodeName() + "=\""
                            + attr.getValue().replace("&", "&amp;") + "\"";
                    if (attr.getPrefix() != null) {
                        xml += " xmlns:" + attr.getPrefix() + "=\""
                                + attr.getNamespaceURI() + "\"";
                    }
                    xml += "/>\n";
                } else if (type == Node.ELEMENT_NODE) {
                    xml += " type=\"node()\">\n";
                    xml += "<value>";
                    xml += DomUtils.serializeNode(n);
                    xml += "</value>\n";
                } else if (type == Node.DOCUMENT_NODE) {
                    xml += " type=\"document-node()\">\n";
                    xml += "<value>";
                    xml += DomUtils.serializeNode(n);
                    xml += "</value>\n";
                } else {
                    ItemType it = v.getItemType(context.getConfiguration()
                            .getTypeHierarchy());
                    xml += " type=\"" + getTypeName(it) + "\">\n";
                    xml += "<value>" + n.getNodeValue() + "</value>\n";
                }
            } catch (Exception e) {
                ItemType it = v.getItemType(context.getConfiguration()
                        .getTypeHierarchy());
                xml += " type=\"" + getTypeName(it) + "\">\n";
                xml += "<value>" + v.getStringValue() + "</value>\n";
            }
            xml += "</param>\n";
        }
        xml += "</params>";
        Source src = new StreamSource(new CharArrayReader(xml.toCharArray()));
        NodeInfo result = null;
        try {
            NodeInfo paramsNode = core.getEngine().getBuilder().build(src)
                    .getUnderlyingNode();
            result = core.executeXSLFunction(context, fe, paramsNode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (result == null) {
            return EmptyIterator.getInstance();
        } else {
            return result.iterateAxis(AxisInfo.CHILD);
        }
    }
}
