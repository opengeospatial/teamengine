package com.occamlab.te;

import java.awt.BorderLayout;
import java.net.URL;
import java.net.URLDecoder;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.FormView;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.occamlab.te.util.DomUtils;

/**
 * Swing-based form created from the content of a <ctl:form> element. This is
 * produced when not running the test harness in a web application context.
 */
public class SwingForm extends JFrame implements HyperlinkListener {
    static final long serialVersionUID = 7907599307261079944L;

    public static final String CTL_NS = "http://www.occamlab.com/ctl";
    
    protected boolean isPopup = false;
    protected SwingForm popupForm = null;
    protected String threadName = Thread.currentThread().getName();

    class CustomFormView extends FormView {
        public CustomFormView(javax.swing.text.Element elem) {
            super(elem);
        }

        protected void submitData(String data) {
            if (Core == null) {
                return;
            }
            
//            System.out.println("data: "+data);
            try {
                String kvps = data + "&=";
                int start = 0;
                int end = data.length();

                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = db.newDocument();
                Element root = doc.createElement("values");
                doc.appendChild(root);
                do {
                    String key;
                    String value;

                    int eq = kvps.indexOf("=", start);
                    int amp = kvps.indexOf("&", start);
                    if (amp > eq) {
                        key = kvps.substring(start, eq);
                        value = kvps.substring(eq + 1, amp);
                    } else {
                        key = kvps.substring(start, amp);
                        value = "";
                    }

                    Element valueElement = doc.createElement("value");
                    valueElement.setAttribute("key", key);
                    // Special case for file inputs
                    if (fileFields.contains(key)) {
                    	File f = new File(URLDecoder.decode(value, "UTF-8"));
                    	if (f != null) {
                            if (Core.formParsers.containsKey(key)) {
                                Element parser = Core.formParsers.get(key); 
                                URL url = f.toURI().toURL();
                                Element response = Core.parse(url.openConnection(), parser, doc);
                                Element content = DomUtils.getElementByTagName(response, "content");
                                if (content != null) {
                                    Element child = DomUtils.getChildElement(content);
                                    if (child != null) {
                                        valueElement.appendChild(child);
                                    }
                                }
                            } else {
//                    		value = Core.saveFileToWorkingDir(value);  // What does copying the file to a new dir accomplish?
		                Element fileEntry = doc.createElementNS(CTL_NS, "file-entry");
                                fileEntry.setAttribute("full-path", URLDecoder.decode(value, "UTF-8").replace('\\','/'));
		                valueElement.appendChild(fileEntry);
                            }
                    	}
                    }
                    else {
                    	valueElement.appendChild(doc.createTextNode(URLDecoder.decode(value, "UTF-8")));
                    }
                    root.appendChild(valueElement);
                    start = amp + 1;
                    //System.out.println("key|value: "+key+"|"+value);
                } while (start < end);

                Core.setFormResults(doc);
                if (popupForm != null) {
                    popupForm.dispose();
                }
            } catch (Throwable t) {
                t.printStackTrace(System.out);
            }
            Form.dispose();
            Form = null;
        }
    }

    class CustomViewFactory extends HTMLEditorKit.HTMLFactory {
        public javax.swing.text.View create(javax.swing.text.Element elem) {
            AttributeSet as = elem.getAttributes();
            HTML.Tag tag = (HTML.Tag)(as.getAttribute(StyleConstants.NameAttribute));
      
            if (tag==HTML.Tag.INPUT) {
                String type = "";
                String name = "";
                Enumeration e = as.getAttributeNames();
                while (e.hasMoreElements()) {
                    Object key = e.nextElement();
                    if (key == HTML.Attribute.TYPE) {
                        type = as.getAttribute(key).toString();
                    }
                    if (key == HTML.Attribute.NAME) {
                        name = as.getAttribute(key).toString();
                    }
                }

                if (type.equalsIgnoreCase("submit")) {
                    return new CustomFormView(elem);
                }

                if (type.equalsIgnoreCase("file")) {
                    fileFields.add(name);
                }
            }

            return super.create(elem);
        }
    }

    class CustomHTMLEditorKit extends HTMLEditorKit {
        static final long serialVersionUID = 5742710765916499050L;

        public javax.swing.text.ViewFactory getViewFactory() {
            return new CustomViewFactory();
        }
    }

    JEditorPane Jedit;
    TECore Core;
    SwingForm Form;
    ArrayList<String> fileFields = new ArrayList<String>();  

    SwingForm(String name, int width, int height, TECore core) {
        Core = core;
        Form = this;


        setTitle(name);
        setSize(width, height);
        // setBackground(Color.gray);
        getContentPane().setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        getContentPane().add(topPanel, BorderLayout.CENTER);

        Jedit = new JEditorPane();
        Jedit.setEditorKit(new CustomHTMLEditorKit());
        Jedit.setEditable(false);
        Jedit.addHyperlinkListener(this);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().add(Jedit, BorderLayout.CENTER);
        topPanel.add(scrollPane, BorderLayout.CENTER);

        if (core != null) {
            String html = core.getFormHtml();
    
            // For some reason, the <meta http-equiv="Content-Type"
            // content="text/html"> tag generated by SAXON screws up the
            // JEditorPane, so replace it with a dummy tag.
            int i = html.indexOf("<meta");
            if (i > 0) {
                html = html.substring(0, i + 1) + "blah" + html.substring(i + 5);
            }
            Jedit.setText(html);
            core.setFormHtml(null);
        }

        setVisible(true);
    }

    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            JEditorPane pane = (JEditorPane) e.getSource();
            if (e instanceof HTMLFrameHyperlinkEvent) {
                HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
                HTMLDocument doc = (HTMLDocument) pane.getDocument();
                doc.processHTMLFrameHyperlinkEvent(evt);
            } else {
                try {
                    if (isPopup) {
                        pane.setPage(e.getURL());
                    } else {
                        popupForm = new SwingForm(threadName, 700, 500, null);
                        popupForm.setLocation(this.getX() + 30, this.getY() + 30);
                        popupForm.isPopup = true;
                        popupForm.Jedit.setPage(e.getURL());
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }
}
