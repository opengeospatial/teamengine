package com.occamlab.te;

import java.io.File;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class RecordedForm {

  public RecordedForm(File formSource, TECore teCore) {
    if (!formSource.exists()) {
      throw new RuntimeException("Could not find form file:" + formSource.getAbsolutePath());
    }

    try {
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(formSource);
      teCore.setFormResults(doc);
    } catch (Exception e) {
      throw new RuntimeException("Could not parse form file" + formSource.getAbsolutePath(), e);
    }
  }

  public static RecordedForm create(File formFile, TECore teCore) {
    return new RecordedForm(formFile, teCore);
  }

}