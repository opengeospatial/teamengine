package com.occamlab.te.index;

import java.io.File;
import java.util.List;

import javax.xml.namespace.QName;

public interface TemplateEntry extends NamedEntry {
    public File getTemplateFile();
    public void setTemplateFile(File templateFile);
    public List<QName> getParams();
    public void setParams(List<QName> params);
    public boolean isUsesContext();
    public void setUsesContext(boolean usesContext);
}
