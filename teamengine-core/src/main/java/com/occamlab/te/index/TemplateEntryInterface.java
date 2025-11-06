package com.occamlab.te.index;

import java.io.File;
import java.util.List;

import javax.xml.namespace.QName;

public interface TemplateEntryInterface extends NamedEntry {

	File getTemplateFile();

	void setTemplateFile(File templateFile);

	List<QName> getParams();

	void setParams(List<QName> params);

	boolean usesContext();

	void setUsesContext(boolean usesContext);

}
