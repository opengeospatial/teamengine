/**
 * **************************************************************************
 *
 * Contributor(s): 
 *	C. Heazel (WiSC): Added Fortify adjudication changes
 *
 ***************************************************************************
 */
package com.occamlab.te;

import java.io.File;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class RecordedForm {

  public RecordedForm(File formSource, TECore teCore) {
    if (!formSource.exists()) {
      throw new RuntimeException("Could not find form file:" + formSource.getAbsolutePath());
    }

    try {
      // Fortify Mod: prevent external entity injection
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setExpandEntityReferences(false);
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(formSource);
      //Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(formSource);
      teCore.setFormResults(doc);
    } catch (Exception e) {
      throw new RuntimeException("Could not parse form file" + formSource.getAbsolutePath(), e);
    }
  }

  public static RecordedForm create(File formFile, TECore teCore) {
    return new RecordedForm(formFile, teCore);
  }

}