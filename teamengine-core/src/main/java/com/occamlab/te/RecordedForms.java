package com.occamlab.te;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecordedForms {

  List<File> files = new ArrayList();

  int current = 0;

  public RecordedForms(List<File> recordedForms) {
    this.files = recordedForms;
  }

  /**
   * Returns true if no pre-recorded forms are found
   * 
   * @return boolean
   */
  public boolean isEmpty() {
    return files.isEmpty();
  }

  public void addRecordedForm(String file) {
    this.files.add(new File(file));
  }

  public File next() {
    File result = files.get(current);
    if (current < (files.size() - 1)) {
      current++;
    }
    return result;
  }

}
